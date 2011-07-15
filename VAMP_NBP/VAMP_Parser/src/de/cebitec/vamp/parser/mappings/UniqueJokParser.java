package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a slightly different version of the JokParser. In contrast to
 * the JokParser, this parser expects filtered mapping results, such that each
 * duplicate read was filtered BEFORE the mapping and the abundance of this read
 * is encoded in the readname by adding #x to the readname, with x being the
 * number of equal sequences found during the filtering step.
 * @author ddoppmeier
 */
public class UniqueJokParser implements MappingParserI, Observer {

    private static String name = "Unique Reads Jok Output Parser";
    private static String[] fileExtension = new String[]{"out"};
    private static String fileDescription = "Jok Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private ArrayList<Observer> observers;
    private String errorMsg;
    private int noUniqueMappings;

    public UniqueJokParser() {
        this.gapOrderIndex = new HashMap<Integer, Integer>();
        this.observers = new ArrayList<Observer>();
    }

    @Override
//    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException {
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        mappingContainer.registerObserver(this);

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings from file \"{0}\"", trackJob.getFile().getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));
            int lineno = 0;
            int start;
            int stop;
            int errors;
            this.noUniqueMappings = 0;
            int noUniqueSeq = 0;
            String line = null;
            while ((line = br.readLine()) != null) { //reads the input file line per line
                lineno++;

                // tokenize input line
                String[] tokens = line.split("\\s", 8);
                if (tokens.length == 7) { // if the length is not correct the read is not parsed

                    // cast tokens
                    String readname = tokens[0];
                    start = -2;
                    stop = -1;
                    try {
                        start = Integer.parseInt(tokens[1]);
                        stop = Integer.parseInt(tokens[2]);
                        start++;
                        stop++; // some people (no names here...) start counting at 0, I count genome position starting with 1
                    } catch (NumberFormatException e) { //
                        if (!tokens[1].equals("*")) {
                            this.sendErrorMsg("Value for current start position in "
                                    + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number or *. "
                                    + "Found start: " + tokens[1]);
                        }
                        if (!tokens[2].equals("*")) {
                            this.sendErrorMsg("Value for current stop position in "
                                    + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number or *. "
                                    + "Found stop: " + tokens[2]);
                        }
                        continue; //*'s are ignored = unmapped read
                    }
                    byte direction = 0;
                    if (tokens[3].equals(">>")) {
                        direction = 1;
                    } else if (tokens[3].equals("<<")) {
                        direction = -1;
                    }
                    String readSeq = tokens[4];
                    String refSeq = tokens[5];
                    errors = 0;
                    try {
                        errors = Integer.parseInt(tokens[6]);
                    } catch (NumberFormatException e){
                        this.sendErrorMsg("Value for errors in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number. "
                                + "Found error count: " + tokens[6]);
                        continue;
                    }

                    // check tokens
                    if (readname == null || readname.isEmpty()) {
                        this.sendErrorMsg("Could not read readname in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found read name: " + readname);
                        continue;
                    }
                    // split the readname into name and counting information
                    String[] parts = readname.split("#");
                    int count = Integer.parseInt(parts[parts.length - 1]); //check for uniqueness & catch format exception

                    if (start >= stop) {
                        this.sendErrorMsg("Start bigger than stop in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found start: " + start + ", stop: " + stop);
                        continue;
                    }
                    if (direction == 0) {
                        this.sendErrorMsg("Could not parse direction in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be >> oder <<");
                        continue;
                    }
                    if (readSeq == null || readSeq.isEmpty()) {
                        this.sendErrorMsg("Read sequence could not be parsed in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + readSeq + tokens.length);
                        continue;
                    }
                    if (refSeq == null || refSeq.isEmpty()) {
                        this.sendErrorMsg("Reference sequence could not be parsed in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + refSeq);
                        continue;
                    }
                    if (readSeq.length() != refSeq.length() && !refSeq.equals(("*"))) {
                        this.sendErrorMsg("Alignment sequences have different length in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + "! "
                                + "Found read sequence: " + readSeq + ", reference sequence: " + refSeq);
                        continue;
                    }
                    if (errors < 0 || errors > readSeq.length()) {
                        this.sendErrorMsg("Error number has invalid value " + errors
                                + " in " + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be bigger or equal to zero and smaller than alignment length.");
                        continue;
                    }
//                    if (!readnameToSequenceID.containsKey(readname)) {
//                        this.sendErrorMsg("Could not find sequence id mapping for read " + readname
//                                + " in " + trackJob.getFile().getAbsolutePath() + "line " + lineno + ". "
//                                + "Please make sure you are referencing the correct read data set!");
//                        readErroneous = true;
//                    }

                    // Reads with an error already skip this part because of "continue" statements
                    DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction);
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();

                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                    mapping.setCount(count);

                    mappingContainer.addParsedMapping(mapping, ++noUniqueSeq); //since all duplicate reads have been filtered before
                    this.processReadname(count, readname); //TODO: correct this!!!! parser not up to date!
                } else {
                    this.sendErrorMsg("The current read in line " + lineno + "is missing some data: ".concat(line));
                }
            }

            mappingContainer.setNumberOfUniqueMappings(noUniqueMappings);
            mappingContainer.setNumberOfUniqueSeq(noUniqueSeq);
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising mapping data from \"{0}\"", trackJob.getFile().getAbsolutePath());

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

    private Character getReverseComplement(char base) {
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown char {0}!", base);
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
                        base = getReverseComplement(base);
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
                        c = getReverseComplement(c);
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
    public ParsedRun parseInputForReadData(TrackJob trackJob) throws ParsingException {
        ParsedRun run = new ParsedRun(fileDescription);
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing read data from mappings from file \"{0}\"", trackJob.getFile().getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;

            while ((line = br.readLine()) != null) {
                lineno++;
                // tokenize input line
                String[] tokens = line.split("\\t", 8);

                // cast tokens
                String readname = tokens[0];
                String readSeq = tokens[4];
                String editReadSeq = readSeq;
                if (editReadSeq.contains("_")) {
                    editReadSeq = editReadSeq.replaceAll("_", "");
                }
                editReadSeq = editReadSeq.toLowerCase();
                run.addReadData(editReadSeq, readname);
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing read data from mapping data from \"{0}\"", trackJob.getFile().getAbsolutePath());

        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
        run.setTimestamp(trackJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read data successfully parsed");
        return run;
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : this.observers){
            observer.update(this.errorMsg);
        }
    }

    @Override
    public void update(Object args) {
        if (args instanceof Boolean && (Boolean) args == true) {
            ++this.noUniqueMappings;
        }
    }

    /**
     * Method setting and sending the error msg to all observers.
     * @param errorMsg the error msg to send
     */
    private void sendErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
        this.notifyObservers();
    }

    @Override
    public void processReadname(int seqID, String readName) {
        //nothing to do here
    }
}
