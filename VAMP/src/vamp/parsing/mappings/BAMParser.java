package vamp.parsing.mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import vamp.databackend.connector.ReferenceConnector;
import vamp.parsing.common.ParsedDiff;
import vamp.parsing.common.ParsedMapping;
import vamp.parsing.common.ParsedMappingContainer;
import vamp.parsing.common.ParsedReferenceGap;
import vamp.parsing.common.ParsingException;
import vamp.databackend.connector.ProjectConnector;
import vamp.importer.TrackJobs;
import vamp.parsing.common.ParsedRun;

/**
 *
 * @author jstraube
 */
public class BAMParser implements MappingParserI {

    private static String name = "BAM Parser";
    private static String[] fileExtension = new String[]{"bam", "BAM", "Bam"};
    private static String fileDescription = "BAM Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private HashSet<String> unmappedReads = new HashSet<String>();
    private int errors = 0;
    private HashMap<String, String> mappedRefAndReadPlusGaps = new HashMap<String, String>();

    public BAMParser() {
        gapOrderIndex = new HashMap<Integer, Integer>();
    }

    @Override
    public ParsedMappingContainer parseInput(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException {
        String readname = null;
        String refName = null;
        String refSeq = null;
        String readSeq = null;
        int flag = 0;
        ReferenceConnector genome = null;
        String readSeqwithoutGaps = null;
        String cigar = null;
        String refSeqfulllength = null;
        String refSeqwithoutgaps = null;
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        try {
            Long id = trackJob.getRefGen().getID();
            genome = ProjectConnector.getInstance().getRefGenomeConnector(id);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Coudnt get the ref genome\"" + trackJob.getFile().getAbsolutePath() + "\"" + ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings Parser from file \"" + trackJob.getFile().getAbsolutePath() + "\"");


        SAMFileReader sam = new SAMFileReader(trackJob.getFile());

        SAMRecordIterator itor = sam.iterator();

        int lineno = 0;

        while (itor.hasNext()) {

            SAMRecord first = (SAMRecord) itor.next();
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
                if (refSeqfulllength == null) {
                    refSeqfulllength = genome.getRefGen().getSequence();

                }
                errors = 0;

                if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S")) {
                    //need the stop position
                    stop = countStopPosition(cigar, start, readSeqwithoutGaps.length() - 1);

                } else {

                    stop = start + readSeqwithoutGaps.length() - 1;

                }
                refSeqwithoutgaps = refSeqfulllength.substring(start - 1, stop).toLowerCase();

                if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S")) {

                    refSeq = createMappingOfRefAndRead(cigar, refSeqwithoutgaps, readSeqwithoutGaps);
                    readSeq = mappedRefAndReadPlusGaps.get(refSeq);
                    mappedRefAndReadPlusGaps.clear();
                } else {
                    refSeq = refSeqwithoutgaps;
                    readSeq = readSeqwithoutGaps;
                }
                //check parameters
                if (readname == null || readname.equals("")) {
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
                if (readSeq == null || readSeq.equals("")) {
                    throw new ParsingException("read sequence could not be parsed in "
                            + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                            + "Found: " + readSeq);
                }
                if (refSeq == null || refSeq.equals("")) {
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

                DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction, cigar);
                List<ParsedDiff> diffs = result.getDiffs();
                List<ParsedReferenceGap> gaps = result.getGaps();

                //   System.out.println("error" + errors);
                ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                int seqID = readnameToSequenceID.get(readname);
                mappingContainer.addParsedMapping(mapping, seqID);
                if (!itor.hasNext()) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Iterator has no more data from \"" + trackJob.getFile().getAbsolutePath() + "\"");
                }
            }
        }
        HashSet<Integer> s = new HashSet<Integer>();
        Iterator it = readnameToSequenceID.values().iterator();
        while (it.hasNext()) {
            int i = (Integer) it.next();
            s.add(i);
        }
        it.remove();

        int noOfReads = readnameToSequenceID.keySet().size();
        int noOfUniqueSeq = s.size();
        mappingContainer.setNumberOfReads(noOfReads);
        mappingContainer.setNumberOfUniqueSeq(noOfUniqueSeq);
        s.clear();
        readnameToSequenceID.clear();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing mapping data from \"" + trackJob.getFile().getAbsolutePath() + "\"");
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mapping data successfully parsed");
        return mappingContainer;
    }
    //this method save which gap is the first if we have more than ine gap
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown char " + base + "!");
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

                    refpos = refpos + numberofDeletion;

                    while (numberofDeletion > 0) {
                        if (readSeq.length() != readPos) {
                            readSeq = readSeq.substring(0, readPos).concat("_") + readSeq.substring(readPos, readSeq.length());
                        } else {
                            readSeq = readSeq.substring(0, readPos).concat("_");
                        }
                        numberofDeletion--;
                        newreadSeq = readSeq;
                        readPos = readPos + 1;
                        //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq + "cigar" + cigar);
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
                        refpos = refpos + 1;

                        //   Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq);
                    }
                } else if (c == 'M') {
                    //for match/mismatch thr positions just move forward
                    readPos = readPos + Integer.parseInt(subSeq2Val);
                    refpos = refpos + Integer.parseInt(subSeq2Val);
                    newRefSeqwithGaps = refSeq;
                    newreadSeq = readSeq;
                } else if (c == 'S') {
                    if (index > 3) {
                        newreadSeq = newreadSeq.substring(0, readSeq.length()-Integer.parseInt(subSeq2Val));
                    } else {
                        readPos = readPos + Integer.parseInt(subSeq2Val);
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
    public ParsedRun parseInputForReadData(TrackJobs trackJob) throws ParsingException {
        int flag = 0;
        String readSeqwithoutGaps = null;
        String readname = null;
        ParsedRun run = new ParsedRun("descrp");
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing read data from file \"" + trackJob.getFile().getAbsolutePath() + "\"");

        SAMFileReader sam = new SAMFileReader(trackJob.getFile());

        SAMRecordIterator itor = sam.iterator();
        while (itor.hasNext()) {
            SAMRecord first = (SAMRecord) itor.next();
            flag = first.getFlags();
            int start = first.getAlignmentStart();
            readname = first.getReadName();
            if (isMappedSequence(flag, start)) {
                readSeqwithoutGaps = first.getReadString();
                String editReadSeq = readSeqwithoutGaps.toLowerCase();
                run.addReadData(editReadSeq, readname);


            }

        }
        run.setTimestamp(trackJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing read data from file \"" + trackJob.getFile().getAbsolutePath() + "\"");
        return run;
    }

    private class DiffAndGapResult {

        private List<ParsedDiff> diffs;
        private List<ParsedReferenceGap> gaps;

        public DiffAndGapResult(List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps) {
            this.diffs = diffs;
            this.gaps = gaps;
        }

        public List<ParsedDiff> getDiffs() {
            return diffs;
        }

        public List<ParsedReferenceGap> getGaps() {
            return gaps;
        }
    }
}
