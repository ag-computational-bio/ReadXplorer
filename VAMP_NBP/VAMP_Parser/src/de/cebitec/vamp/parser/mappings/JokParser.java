package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier
 */
public class JokParser implements MappingParserI {

    private static String name = "Jok Output Parser";
    private static String[] fileExtension = new String[]{"out", "Jok", "jok", "JOK"};
    private static String fileDescription = "Jok Output";
    private HashMap<Integer, Integer> gapOrderIndex;

    public JokParser() {
        gapOrderIndex = new HashMap<Integer, Integer>();
    }

    @Override
    public ParsedMappingContainer parseInput(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        //  ParsedRun run = new ParsedRun("");

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings from file \"{0}\"", trackJob.getFile().getAbsolutePath());

            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                lineno++;

                // tokenize input line
                String[] tokens = line.split("\\s", 8);

                // cast tokens
                String readname = tokens[0];
                int start = Integer.parseInt(tokens[1]);
                int stop = Integer.parseInt(tokens[2]);
                start++;
                stop++; // some people (no names here...) start counting at 0, I count genome position starting with 1
                byte direction = 0;
                if (tokens[3].equals(">>")) {
                    direction = 1;
                } else if (tokens[3].equals("<<")) {
                    direction = -1;
                }
                String readSeq = tokens[4];
                String refSeq = tokens[5];
                int errors = Integer.parseInt(tokens[6]);
                // check tokens
                //check for empty mappings saruman in some cases produce
                if (readSeq != null && !readSeq.isEmpty() && refSeq != null && !refSeq.isEmpty()) {
                    if (readname == null || readname.isEmpty()) {
                        throw new ParsingException("could not read readname in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found read name: " + readname);
                    }
                    if (start >= stop) {
                        throw new ParsingException("start bigger than stop in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found start: " + start + ", stop: " + stop);
                    }
                    if (direction == 0) {
                        throw new ParsingException("could not parse direction in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be >> oder <<");
                    }
                    if (readSeq == null || readSeq.isEmpty()) {
                        throw new ParsingException("read sequence could not be parsed in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + readSeq + tokens.length);
                    }
                    if (refSeq == null || refSeq.isEmpty()) {
                        throw new ParsingException("reference sequence could not be parsed in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + refSeq);
                    }
                    if (readSeq.length() != refSeq.length()) {
                        throw new ParsingException("alignment sequences have different length in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + "! "
                                + "Found read sequence: " + readSeq + ", reference sequence: " + refSeq);
                    }
                    if (errors < 0 || errors > readSeq.length()) {
                        throw new ParsingException("Error number has invalid value " + errors + ""
                                + " in " + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be bigger or equal to zero and smaller that alignment length."
                                + "readname");
                    }
                    if (!readnameToSequenceID.containsKey(readname)) {
                        throw new ParsingException("Could not find sequence id mapping for read  " + readname + ""
                                + " in " + trackJob.getFile().getAbsolutePath() + "line " + lineno + ". "
                                + "Please make sure you are referencing the correct read data set!");
                    }

                    DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction);
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();
                    //dont ask me why but we have to do it 
                    if(!gaps.isEmpty()|| !diffs.isEmpty()){
                        stop = stop -1;
                    }

                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                    int seqID = readnameToSequenceID.get(readname);
                    mappingContainer.addParsedMapping(mapping, seqID);
                }
            }
            Iterator<Integer> it = readnameToSequenceID.values().iterator();
            HashSet<Integer> s = new HashSet<Integer>();
            while (it.hasNext()) {
                int i = it.next();
                s.add(i);
            }
            // it.remove();
            //Data for statics no of reads and number of unique reads
            int noOfReads = readnameToSequenceID.keySet().size();
            int noOfUniqueSeq = s.size();
            mappingContainer.setNumberOfReads(noOfReads);
            mappingContainer.setNumberOfUniqueSeq(noOfUniqueSeq);
            s.clear();
            readnameToSequenceID.clear();
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising mapping data from \"{0}" + "\"" + "no of mappings" + "{1}", new Object[]{trackJob.getFile().getAbsolutePath(), noOfReads});

        } catch (IOException ex) {
            throw new ParsingException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mapping data successfully parsed");
        return mappingContainer;
    }

    private int getOrderForGap(int gapPos) {
        if (!gapOrderIndex.containsKey(gapPos)) {
            gapOrderIndex.put(gapPos, 0);
        }
        int order = gapOrderIndex.get(gapPos);

        // increase order for next request
        gapOrderIndex.put(gapPos, order + 1);

        return order;
    }

    private Character getReverseComplement(char base, String readSeq) {
        Character rev = ' ';
        if (base == 'A') {
            rev = 'T';
        } else if (base == 'C') {
            rev = 'G';
        } else if (base == 'G') {
            rev = 'C';
        } else if (base == 'T') {
            rev = 'A';
        } else if (base == 'N') {
            rev = 'N';
        } else if (base == '_') {
            rev = '_';
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown char {0}!Sequence: {1}", new Object[]{base, readSeq});
        }

        return rev;
    }

    private DiffAndGapResult createDiffsAndGaps(String readSeq, String refSeq, int start, byte direction) {
        List<ParsedDiff> diffs = new ArrayList<ParsedDiff>();
        List<ParsedReferenceGap> gaps = new ArrayList<ParsedReferenceGap>();

        int absPos;
        gapOrderIndex.clear();

        for (int i = 0, basecounter = 0; i < readSeq.length(); i++) {
            if (readSeq.charAt(i) != refSeq.charAt(i)) {
                absPos = start + basecounter;
                if (refSeq.charAt(i) == '_') {
                    // store a lower case char, if this is a gap in genome
                    Character base = readSeq.charAt(i);
                    base = Character.toUpperCase(base);
                    if (direction == -1) {
                        base = getReverseComplement(base, readSeq);
                    }

                    ParsedReferenceGap gap = new ParsedReferenceGap(absPos, base, this.getOrderForGap(absPos));
                    gaps.add(gap);
                    // note: do not increase position. that means that next base of read is mapped
                    // to the same position as this gap. two subsequent gaps map to the same position!
                } else {
                    // store the upper case char from input file, if this is a modification in the read
                    char c = readSeq.charAt(i);
                    c = Character.toUpperCase(c);
                    if (direction == -1) {
                        c = getReverseComplement(c, readSeq);
                    }
                    ParsedDiff d = new ParsedDiff(absPos, c);
                    diffs.add(d);
                    basecounter++;
                }
            } else {
                basecounter++;
            }
        }

        return new DiffAndGapResult(diffs, gaps);
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String getParserName() {
        return name;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    @Override
    public ParsedRun parseInputForReadData(TrackJobs trackJob) throws ParsingException {
        ParsedRun run = new ParsedRun(fileDescription);
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing read data from mappings from file \"{0}\"", trackJob.getFile().getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));
            int no = 0;
            int lineno = 0;
            String line = null;

            while ((line = br.readLine()) != null) {
                lineno++;
                // tokenize input line
                String[] tokens = line.split("\\s", 8);
                // cast tokens
                String readname = tokens[0];
                String readSeq = tokens[4];
                String editReadSeq = readSeq;
                 //deletes all _ which represents gaps in the read seq
                editReadSeq = readSeq.replaceAll("_", "");
                if (editReadSeq.matches("[ATGCN]+")) {

                    if (tokens[3].equals("<<")) {
                        editReadSeq = reverseComplement(editReadSeq);
                    }
                    editReadSeq = editReadSeq.toUpperCase();
                    //if there are other modifications

                    run.addReadData(editReadSeq, readname);
                    if (lineno == 2000000) {
                        no += lineno;
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Parsed reads:{0}", no);
                        lineno = 0;
                    }
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "read doesnt match [ATGCN]+ {0} {1}", new Object[]{readname, editReadSeq});
                }
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing read data from mapping data from \"{0}\"", trackJob.getFile().getAbsolutePath());

        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
        run.setTimestamp(trackJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read data successfully parsed{0}", run.getSequences().size());
        return run;
    }

    public String reverseComplement(String readSeq) {
        String revBase = "";
        for (int i = 0; i < readSeq.length(); i++) {
            Character base = readSeq.charAt(i);
            base = Character.toUpperCase(base);
            base = getReverseComplement(base, readSeq);
            revBase = base + revBase;
        }
        return revBase;
    }
    
}
