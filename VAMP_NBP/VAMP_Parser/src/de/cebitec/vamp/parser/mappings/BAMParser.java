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
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.openide.util.NbBundle;

/**
 * 
 * @author jstraube, rhilker
 */
public class BAMParser implements MappingParserI, Observer {

    private static String name = "BAM Parser";
    private static String[] fileExtension = new String[]{"bam", "BAM", "Bam"};
    private static String fileDescription = "BAM Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private int errors = 0;
    private HashMap<String, String> mappedRefAndReadPlusGaps = new HashMap<String, String>();
    private HashMap<String, Integer> seqToIDMap;
    private ArrayList<String> readnames;
    private ArrayList<Observer> observers;
    private String errorMsg;
    private int noUniqueMappings;

    public BAMParser() {
        this.gapOrderIndex = new HashMap<Integer, Integer>();
        this.seqToIDMap = new HashMap<String, Integer>();
        this.observers = new ArrayList<Observer>();
        this.readnames = new ArrayList<String>();
    }

    @Override
//    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException {
        int flag = 0;
        int lineno = 0;
        String readname = null;
        String refName = null;
        String refSeq = null;
        String readSeq = null;
        String readSeqwithoutGaps = null;
        String cigar = null;
        String refSeqwithoutgaps = null;
        noUniqueMappings = 0;
        int noUniqueReads = 0;
        int counterUnmapped = 0;

        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        mappingContainer.registerObserver(this);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings Parser from file \"{0}\"", trackJob.getFile().getAbsolutePath());

        SAMFileReader sam = new SAMFileReader(trackJob.getFile());
        SAMRecordIterator itor;
        try {
            itor = sam.iterator();
        } catch (RuntimeException e) {
            throw new ParsingException(e.getMessage() + ". !! Track will be empty, thus not be stored !!");
        }
        try {
            while (itor.hasNext()) {
                SAMRecord first = itor.next();
                flag = first.getFlags();

                int start = first.getAlignmentStart();
                readSeqwithoutGaps = first.getReadString();
                if (isMappedSequence(flag, start)) {
                    int stop = 0;
                    boolean isForwardStrand = first.getReadNegativeStrandFlag();
                    byte direction = 1;
                    if (isForwardStrand == true) {
                        direction = -1;
                    }
                    readname = first.getReadName();
                    refName = first.getReferenceName();
                    cigar = first.getCigarString();
                    readSeqwithoutGaps = first.getReadString().toLowerCase();
                    //   System.out.println("rSeq " + readname + "flag " + flag + "refName " + refName + "read " + readSeqwithoutGaps);
                    errors = 0;

                    if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S")) {
                        //need the stop position
                        stop = this.countStopPosition(cigar, start, readSeqwithoutGaps.length() - 1);
                    } else {
                        stop = start + readSeqwithoutGaps.length() - 1;
                    }

                    refSeqwithoutgaps = sequenceString.substring(start - 1, stop).toLowerCase();

                    if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S")) {

                        refSeq = this.createMappingOfRefAndRead(cigar, refSeqwithoutgaps, readSeqwithoutGaps);
                        readSeq = mappedRefAndReadPlusGaps.get(refSeq);
                        mappedRefAndReadPlusGaps.clear();
                    } else {
                        refSeq = refSeqwithoutgaps;
                        readSeq = readSeqwithoutGaps;
                    }
                    //check parameters
                    if (readname == null || readname.isEmpty()) {
                        this.sendErrorMsg("Could not read readname in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found read name: " + readname);
                        continue;
                    }

                    if (start >= stop) {
                        this.sendErrorMsg("Start bigger than stop in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found start: " + start + ", stop: " + stop);
                        continue;
                    }
                    if (direction == 0) {
                        this.sendErrorMsg("Could not parse direction in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be 0 oder 1");
                        continue;
                    }
                    if (readSeq == null || readSeq.isEmpty()) {
                        this.sendErrorMsg("Read sequence could not be parsed in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + readSeq);
                        continue;
                    }
                    if (refSeq == null || refSeq.isEmpty()) {
                        this.sendErrorMsg("Reference sequence could not be parsed in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + refSeq);
                        continue;
                    }
                    if (readSeq.length() != refSeq.length()) {
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
//                if (!readnameToSequenceID.containsKey(readname)) {
//                    throw new ParsingException("Could not find sequence id mapping for read  " + readname + ""
//                            + " in " + trackJob.getFile().getAbsolutePath() + "line " + lineno + ". "
//                            + "Please make sure you are referencing the correct read data set!");
//                }
                    //TODO: calc reads for stats bam parser
                    // Reads with an error already skip this part because of "continue" statements
                    //!!Thats wrong you can have one read mapped on different positions
                    DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction, cigar);
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();

                    //   System.out.println("error" + errors);
                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                    int seqID;
                    if (this.seqToIDMap.containsKey(readSeq)) {
                        seqID = this.seqToIDMap.get(readSeq);
                    } else {
                        seqID = ++noUniqueReads; //int seqID = readnameToSequenceID.get(readname);
                        this.seqToIDMap.put(readSeq, seqID);
                    } //readnameToSequenceID.get(readname);
                    mappingContainer.addParsedMapping(mapping, seqID);
                    System.out.println(mapping.toString());
                    this.processReadname(seqID, readname);
                    if (!itor.hasNext()) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Iterator has no more data from \"{0}\"", trackJob.getFile().getAbsolutePath());
                    }
                } else {
                    ++counterUnmapped;
                }
            }
        } catch (Exception e){
            this.sendErrorMsg(e.getMessage());
        }
//        HashSet<Integer> s = new HashSet<Integer>();
//        Iterator<Integer> it = readnameToSequenceID.values().iterator();
//        while (it.hasNext()) {
//            int i = it.next();
//            s.add(i);
//        }
//       // it.remove();
//
//        int noOfReads = readnameToSequenceID.keySet().size();
//        int noOfUniqueSeq = s.size();
//        s.clear();
//        readnameToSequenceID.clear();

        mappingContainer.setNumberOfUniqueMappings(noUniqueMappings);//TODO: check if counting is correct here
        mappingContainer.setNumberOfUniqueSeq(noUniqueReads);
        mappingContainer.setNumberOfReads(this.readnames.size());
        this.seqToIDMap = null; //release resources
        this.readnames = null;
        
        if (mappingContainer.getMappedSequenceIDs().isEmpty()) { //if track does not contain any reads
            throw new ParsingException(NbBundle.getMessage(JokParser.class, "Parser.Empty.Track.Error"));
        }
        if (counterUnmapped > 0){
            this.sendErrorMsg("Number of unmapped reads in file: "+counterUnmapped);
        }
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing mapping data from \"{0}\"", trackJob.getFile().getAbsolutePath());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mapping data successfully parsed");
        return mappingContainer;
    }
    
    /**
     * This method saves which gap is the first if we have more than one gap.
     * @param gapPos position of the gap
     */
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

    private DiffAndGapResult createDiffsAndGaps(String readSeq, String refSeq, int start, byte direction, String cigar) {
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
     * M can be a Match or Mismatch, D is a deletion on the read, I insertion on the read, S softclipped read
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
        int softclipped = 0;

        for (char c : cigar.toCharArray()) {
            int index = pos;

            if (c == 'D' || c == 'I' || c == 'M' || c == 'S') {
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

                    refpos += numberofDeletion;

                    while (numberofDeletion > 0) {
                        if (readSeq.length() != readPos) {
                            readSeq = readSeq.substring(0, readPos).concat("_") + readSeq.substring(readPos, readSeq.length());
                        } else {
                            readSeq = readSeq.substring(0, readPos).concat("_");
                        }
                        numberofDeletion--;
                        newreadSeq = readSeq;
                        readPos += 1;
                        //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq + "cigar" + cigar);
                    }

                } else if (c == 'I') {
                    //insertion of the  read
                    numberOfInsertions = Integer.parseInt(subSeq2Val);

                    readPos += numberOfInsertions;
                    while (numberOfInsertions > 0) {

                        if (refpos != refSeq.length()) {
                            refSeq = refSeq.substring(0, refpos).concat("_") + refSeq.substring(refpos, refSeq.length());
                        } else {
                            refSeq = refSeq.substring(0, refpos).concat("_");
                        }
                        newRefSeqwithGaps = refSeq;
                        numberOfInsertions--;
                        refpos += 1;

                        //   Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq);
                    }
                } else if (c == 'M') {
                    //for match/mismatch thr positions just move forward
                    readPos += Integer.parseInt(subSeq2Val);
                    refpos += Integer.parseInt(subSeq2Val);
                    newRefSeqwithGaps = refSeq;
                    newreadSeq = readSeq;
                } else if (c == 'S') {
                    if (index > 3) {
                        //soft clipping of the last bases
                        newreadSeq = newreadSeq.substring(0, readSeq.length() - Integer.parseInt(subSeq2Val));
                    } else {
                        //soft clipping of the first bases
                        readPos += Integer.parseInt(subSeq2Val);
                        softclipped = Integer.parseInt(subSeq2Val);
                    }
                }
            }
            pos++;
        }
        newreadSeq = newreadSeq.substring(softclipped, newreadSeq.length());
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
            if (c == 'D' || c == 'I' || c == 'S') {
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
        stopPosition = startPosition + readLength + numberofDeletion - numberofInsertion;
        return stopPosition;
    }

    /**
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
    public boolean isMappedSequence(Integer flag, int startPosition) {
        boolean isMapped = true;
        if (flag >= 4) {
            String binaryValue = Integer.toBinaryString(flag);
            int binaryLength = binaryValue.length();
            String b = binaryValue.substring(binaryLength - 3, binaryLength - 2);

            if (b.equals("1") || startPosition == 0) {
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
    public ParsedRun parseInputForReadData(TrackJob trackJob) throws ParsingException {
        String readSeqwithoutGaps = null;
        ParsedRun run = new ParsedRun("descrp");
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing read data from file \"{0}\"", trackJob.getFile().getAbsolutePath());
        SAMFileReader sam = new SAMFileReader(trackJob.getFile());
        SAMRecordIterator itor = sam.iterator();
        while (itor.hasNext()) {
            SAMRecord first = itor.next();
            int flag = first.getFlags();
            int start = first.getAlignmentStart();
            String readname = first.getReadName();
            if (isMappedSequence(flag, start)) {
                readSeqwithoutGaps = first.getReadString();
                String editReadSeq = readSeqwithoutGaps.toLowerCase();
                run.addReadData(editReadSeq, readname);
            }
        }
        itor = null;
        run.setTimestamp(trackJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing read data from file \"{0}\"", trackJob.getFile().getAbsolutePath());
        if (run == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "run is empty", trackJob.getFile().getAbsolutePath());
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "run is not empty", trackJob.getFile().getAbsolutePath());
        }
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

    /**
     * Method setting and sending the error msg to all observers.
     * @param errorMsg the error msg to send
     */
    private void sendErrorMsg(final String errorMsg){
        this.errorMsg = errorMsg;
        this.notifyObservers();
    }

    @Override
    public void update(Object args) {
        if (args instanceof Boolean && (Boolean) args == true) {
            ++this.noUniqueMappings;
        }
    }

    
    @Override
    public void processReadname(int seqID, String readName) {
        //count reads
        if (!this.readnames.contains(readName)){
            this.readnames.add(readName);
        }
    }
}
