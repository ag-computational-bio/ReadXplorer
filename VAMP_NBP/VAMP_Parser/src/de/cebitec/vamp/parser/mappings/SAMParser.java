package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJobs;
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
 *
 * @author jstraube
 */
public class SAMParser implements MappingParserI {

    private static String name = "SAM Parser";
    private static String[] fileExtension = new String[]{"sam","SAM","Sam"};
    private static String fileDescription = "SAM Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private ArrayList<String> unmappedReads = new ArrayList<String>();
    private int errors = 0;
    private HashMap<String, String> mappedRefAndReadPlusGaps = new HashMap<String, String>();

    public SAMParser() {
        gapOrderIndex = new HashMap<Integer, Integer>();
    }

    @Override
    public ParsedMappingContainer parseInput(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
        String readname = null;
        String position = null;
        String refName = null;
        String refSeq = null;
        String readSeq = null;
        int flag = 0;
        String readSeqwithoutGaps = null;
        String cigar = null;

        String refSeqfulllength = null;
        String refSeqwithoutgaps = null;
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings Parser from file \"{0}\"", trackJob.getFile().getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;
            while ((line = br.readLine()) != null) {

                lineno++;
                //parsing the SAM format in following parts
                if (!line.startsWith("@")) {

                    String[] readSeqLine = line.split("\\s+");
                    readname = readSeqLine[0];
                    flag = Integer.parseInt(readSeqLine[1]);
                    refName = readSeqLine[2];
                    position = readSeqLine[3];
                    //  String mappingQuality = readSeqLine[4];
                    cigar = readSeqLine[5];
                    //   String inferredInsertSize = readSeqLine[8];
                    readSeqwithoutGaps = readSeqLine[9].toLowerCase();
                 //   System.out.println("rSeq " + readname + "flag " + flag + "refName " + refName + "pos " + position + readSeqwithoutGaps);
                    //System.out.println("rSeq " + readname );
                    if (isMappedSequence(flag, position)) {
                        
                        if (refSeqfulllength == null) {
                            refSeqfulllength = sequenceString;

                        }
                        int start = Integer.parseInt(position);
                        int stop = 0;
                        errors = 0;
                        if (cigar.contains("D") || cigar.contains("I")) {
                            //need the stop position
                            stop = countStopPosition(cigar, start, readSeqwithoutGaps.length());

                        } else {
                            stop = start + readSeqwithoutGaps.length()-1;
                        }

                        //   System.out.println("pos" + start + "-" + stop);
                          //saruman starts genome at 0 other algorithms like bwa start genome at 1
                       // refSeqwithoutgaps = refSeqfulllength.substring(start, stop+1).toLowerCase();
                        refSeqwithoutgaps = refSeqfulllength.substring(start-1, stop).toLowerCase();

                        if (cigar.contains("D") || cigar.contains("I")) {
                            //fallen gleiche refs raus?
                            refSeq = createMappingOfRefAndRead(cigar, refSeqwithoutgaps, readSeqwithoutGaps);
                            readSeq = mappedRefAndReadPlusGaps.get(refSeq);
                        } else {
                            refSeq = refSeqwithoutgaps;
                            readSeq = readSeqwithoutGaps;
                        }
                        byte direction = 0;

                        if (isForwardRead(flag)) {
                            //is forward
                            direction = 1;

                        } else {
                            //is reverse
                            direction = -1;
                        }
                        //check parameters
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
                                    + "Must be 0 oder 1");
                        }
                        if (readSeq == null || readSeq.isEmpty()) {
                            throw new ParsingException("read sequence could not be parsed in "
                                    + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                    + "Found: " + readSeq);
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
                        //saruman starts genome at 0 other algorithms like bwa start genome at 1
                        // DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start+1 , direction);
                        DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start , direction);
                        List<ParsedDiff> diffs = result.getDiffs();
                        List<ParsedReferenceGap> gaps = result.getGaps();
                        //saruman starts genome at 0 other algorithms like bwa start genome at 1
                       // ParsedMapping mapping = new ParsedMapping(start+1 , stop+1, direction, diffs, gaps, errors);

                        ParsedMapping mapping = new ParsedMapping(start , stop, direction, diffs, gaps, errors);
                        int seqID = readnameToSequenceID.get(readname);
                        mappingContainer.addParsedMapping(mapping, seqID);

                    } else {
                        unmappedReads.add(readname);
                        continue;
                    }
                    continue;
                }
                continue;
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing mapping data from \"{0}\"", trackJob.getFile().getAbsolutePath());

        } catch (IOException ex) {
            throw new ParsingException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mapping data successfully parsed");
        return mappingContainer;
    }
    //Why do you do this??

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
        errors = 0;
        int absPos;
        gapOrderIndex.clear();

        for (int i = 0, basecounter = 0; i < readSeq.length(); i++) {
            if (readSeq.toLowerCase().charAt(i) != refSeq.toLowerCase().charAt(i)) {
                errors++;
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

    /**
     * This method tries to convert the cigar string to the mapping again because
     * SAM format has no other mapping information
     * @param cigar contains mapping information of reference and read sequence
     * @param refSeq
     * @param readSeq
     * @return the refSeq with gaps in fact of insertions in the reads
     */
    public String createMappingOfRefAndRead(String cigar, String refSeq, String readSeq) {
        String newRefSeqwithGaps = null;
        String newreadSeq = null;
        String subSeq2Val = null;
        int refpos = 0;
        int numberOfInsertions = 0;
        int numberofDeletion = 0;
        int pos = 0;
        int readPos = 0;
        mappedRefAndReadPlusGaps = new HashMap<String, String>();
        for (char c : cigar.toCharArray()) {
            int index = pos;

            if (c == 'D' || c == 'I' || c == 'M') {
                if (index == 1) {
                    subSeq2Val = (String) cigar.subSequence(index - 1, index);
                }
                if (index == 2) {
                    subSeq2Val = (String) cigar.subSequence(index - 2, index);
                }

                if (index >= 3) {
                    subSeq2Val = (String) cigar.subSequence(index - 3, index);

                }
                if (index > 2) {
                    if (!subSeq2Val.matches("[0-9]*")) {
                        subSeq2Val = (String) cigar.subSequence(index - 2, index);

                    }
                }

                if (!subSeq2Val.matches("[0-9]*")) {
                    subSeq2Val = (String) cigar.subSequence(index - 1, index);

                }
                if (c == 'D') {
                    //deletion of the read
                    numberofDeletion = Integer.parseInt(subSeq2Val);

                    refpos = refpos + numberofDeletion;

                    while (numberofDeletion > 0) {
                        if (readSeq.length() != readPos) {
                            readSeq = readSeq.substring(0, readPos).concat("_") + readSeq.substring(readPos, readSeq.length());
                        } else {
                            readSeq = readSeq.substring(0, readPos).concat("_");
                        }
                        numberofDeletion--;
                        newreadSeq = readSeq;
                      //  Logger.getLogger(this.getClass().getName()).log(Level.INFO,"Case Deletion"+ "read " + readSeq + "ref  " +refSeq);
                    }

                } else if (c == 'I') {
                    //insertion of the  read
                    numberOfInsertions = Integer.parseInt(subSeq2Val);

                    readPos = readPos + numberOfInsertions;
                    while (numberOfInsertions > 0) {

                        if (refpos != refSeq.length()) {
                            refSeq = refSeq.substring(0, refpos).concat("_") + refSeq.substring(refpos, refSeq.length());
                        } else {
                            refSeq = refSeq.substring(0, refpos).concat("_");
                        }
                        newRefSeqwithGaps = refSeq;
                       
                        numberOfInsertions--;
                    }
                } else if (c == 'M') {
                    //fuer match/mismatch werden die positionen einfach weiter gesetzt
                    readPos = readPos + Integer.parseInt(subSeq2Val);
                    refpos = refpos + Integer.parseInt(subSeq2Val);
                    newRefSeqwithGaps = refSeq;
                    newreadSeq = readSeq;
                }
            }
            pos++;
        }
        //  System.out.println("ref  " + newRefSeqwithGaps);
       //  System.out.println("read " + newreadSeq);
        mappedRefAndReadPlusGaps.put(newRefSeqwithGaps, newreadSeq);
        return newRefSeqwithGaps;

    }

    /**
     * In fact that deletions in the read stretches the stop position of the
     * ref genome mapping, we need to count the number of deletions to calculate
     * the stopposition of the read to the ref genome.
     * @param cigar contains mapping information
     * @param startPosition of the mapping
     * @param readLength the length of the read
     * @return
     */
    public int countStopPosition(String cigar, Integer startPosition, Integer readLength) {
        int stopPosition;
        int numberofDeletion = 0;
        int numberofInsertion = 0;
        String subSeq2Val = null;
        int pos = 0;
        for (char c : cigar.toCharArray()) {
            int index = pos;
            if (c == 'D' || c == 'I') {
                if (index == 1) {
                    subSeq2Val = (String) cigar.subSequence(index - 1, index);
                }
                if (index == 2) {
                    subSeq2Val = (String) cigar.subSequence(index - 2, index);
                }
                if (index >= 3) {
                    subSeq2Val = (String) cigar.subSequence(index - 3, index);

                }
                if (index > 2) {
                    if (!subSeq2Val.matches("[0-9]*")) {
                        subSeq2Val = (String) cigar.subSequence(index - 2, index);
                    }
                }
                //checks wheater we have a number with 2 positions
                if (!subSeq2Val.matches("[0-9]*")) {
                    subSeq2Val = (String) cigar.subSequence(index - 1, index);
                }
                if (c == 'D') {
                    numberofDeletion = Integer.parseInt(subSeq2Val) + numberofDeletion;
                } else {
                    numberofInsertion = Integer.parseInt(subSeq2Val) + numberofInsertion;
                }
            }
            pos++;
        }
        stopPosition = startPosition + readLength -1 + numberofDeletion - numberofInsertion;
        return stopPosition;
    }

    /**!!!!!!!!!!!!!!!!!! Attention the papers are not sure which bit stands for the direction
     * converts the the dezimal number into binary code and checks if ???? 32 or 16????? if 1 or 0
     * SARUMAN SAM data use the 16 ...
     * @param flag contains information wheater the read is mapped on the rev or fw stream
     * @return wheater the read is mapped on the rev or fw stream
     */
    public boolean isForwardRead(Integer flag) {
        boolean isForward = true;
        if (flag >= 16) {
            String binaryValue = Integer.toBinaryString(flag);
            int binaryLength = binaryValue.length();
            String b = binaryValue.substring(binaryLength - 5, binaryLength - 4);

            if (b.equals("1")) {
                isForward = false;
            } else {
                isForward = true;
            }
        }
        return isForward;
    }

    /**
     *  converts the the dezimal number(flag) into binary code and checks if 4 is 1 or 0
     * @param flag
     * @param startPosition of mapping
     * @return
     */
    public boolean isMappedSequence(Integer flag, String startPosition) {
        boolean isMapped = true;
        if (flag >= 4) {
            String binaryValue = Integer.toBinaryString(flag);
            int binaryLength = binaryValue.length();
            String b = binaryValue.substring(binaryLength - 3, binaryLength - 2);

            if (b.equals("1") || startPosition.equals("0")) {
                isMapped = false;
            } else {
                isMapped = true;
            }
        }
        return isMapped;
    }

    @Override
    public String getParserName() {
        return name;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    @Override
    public ParsedRun parseInputForReadData(TrackJobs trackJob) throws ParsingException {
       String readname = null;
       String position = null;
        int flag = 0;

        String readSeqwithoutGaps = null;
        ParsedRun run = new ParsedRun(fileDescription);
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing run from file \"{0}\"", trackJob.getFile().getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;
            while ((line = br.readLine()) != null) {

                lineno++;
                //parsing the SAM format in following parts
                if (!line.startsWith("@")) {

                    String[] readSeqLine = line.split("\\s+");
                    readname = readSeqLine[0];
                    flag = Integer.parseInt(readSeqLine[1]);
                    position = readSeqLine[3];
                    readSeqwithoutGaps = readSeqLine[9].toLowerCase();
                    if (isMappedSequence(flag, position)) {
                        run.addReadData(readSeqwithoutGaps, readname);
                    }
                }
            }
                            } catch (IOException ex) {
            throw new ParsingException(ex);
        }
        run.setTimestamp(trackJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Read data successfully parsed");
        return run;

    }

    public boolean snpHasStretch(String genome, int snp) {
        String beforeSNP = genome.substring(0, 1);

        if (snp == 1) {
            beforeSNP = genome.substring(snp - 1, snp);
        }
        if (snp == 2) {
            beforeSNP = genome.substring(snp - 2, snp);
        }
        if (snp == 3) {
            beforeSNP = genome.substring(snp - 3, snp);
        }
        if (snp >= 4) {
            beforeSNP = genome.substring(snp - 4, snp);
        }
        System.out.println("before" + beforeSNP);
        String afterSNP = genome.substring(snp, snp + 4);
        System.out.println("afterSnp:" + afterSNP);
        boolean hasStretch = false;
        if (beforeSNP.matches("[atgc]{4,8}") || afterSNP.matches("[atgc]{4,8}")) {
            hasStretch = true;
        }
        if (beforeSNP.matches("[atgc]{1,8}") && afterSNP.matches("[atgc]{3,8}")) {
            System.out.println("1-3" + hasStretch);
        }
        if (beforeSNP.matches("[atgc]{3,8}")) {
            String charBefore = beforeSNP.substring(beforeSNP.length() - 1, beforeSNP.length());
            System.out.println("charbefore " + charBefore);
            String charAfter = afterSNP.substring(0, 1);
            String regex = charBefore.concat("{1}");
            if (charAfter.matches(regex)) {
                System.out.println("3-1" + hasStretch);
            }
        }
        if (afterSNP.matches("[atgc]{3,8}")) {
            String charBefore = beforeSNP.substring(beforeSNP.length() - 1, beforeSNP.length());
            String regex = afterSNP.substring(0, 1).concat("{1}");
            if (charBefore.matches(regex)) {
                System.out.println("3-1" + hasStretch + " " + regex);
            }
        }
        System.out.println(hasStretch);
        return hasStretch;
    }

    @Override
    public void registerObserver(Observer observer) {
        //TODO: to be done
    }

    @Override
    public void removeObserver(Observer observer) {
        //TODO: to be done
    }

    @Override
    public void notifyObservers() {
        //TODO: to be done
    }

}
