package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SAMBAMParser implements MappingParserI, Observer {

    private static String name = "SAM/BAM Parser";
    private static String[] fileExtension = new String[]{"bam", "BAM", "Bam", "sam", "SAM", "Sam"};
    private static String fileDescription = "BAM or SAM Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private int errors = 0;
    private HashMap<String, Integer> seqToIDMap;
    private ArrayList<Observer> observers;
    private String errorMsg;
    private int noUniqueMappings;
    private ArrayList<String> readnames;

    public SAMBAMParser() {
        this.gapOrderIndex = new HashMap<Integer, Integer>();
        this.seqToIDMap = new HashMap<String, Integer>();
        this.observers = new ArrayList<Observer>();
        this.readnames = new ArrayList<String>();
    }

    @Override
//    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError {
        int lineno = 0;
        String filepath = trackJob.getFile().getAbsolutePath();
        String readname = null;
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
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, NbBundle.getMessage(SAMBAMParser.class,
                "Parser.Parsing.Start", filepath));
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
                int start = first.getAlignmentStart();
                readSeqwithoutGaps = first.getReadString();
            if (!first.getReadUnmappedFlag()) {

                int stop = 0;
                boolean isReverseStrand = first.getReadNegativeStrandFlag();
                byte direction = (byte) (isReverseStrand ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD);

                    readname = first.getReadName();
           
                    cigar = first.getCigarString();
                    readSeqwithoutGaps = first.getReadString().toLowerCase();

                    errors = 0;

                int length = sequenceString.length();
                    if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S")|| cigar.contains("N")) {
                    stop = ParserCommonMethods.countStopPosition(cigar, start, readSeqwithoutGaps.length());
                    refSeqwithoutgaps = sequenceString.substring(start - 1, stop).toLowerCase();
                    String[] refandRead = ParserCommonMethods.createMappingOfRefAndRead(cigar, refSeqwithoutgaps, readSeqwithoutGaps);
                    refSeq = refandRead[0];
                    readSeq = refandRead[1];
                    } else {
                        stop = start + readSeqwithoutGaps.length() - 1;
                        refSeqwithoutgaps = sequenceString.substring(start - 1, stop).toLowerCase();
                        refSeq = refSeqwithoutgaps;
                        readSeq = readSeqwithoutGaps;
                    }
                    //check parameters
                if (length < start || length < stop) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorReadPosition",
                            filepath, lineno, start, stop, length));
                    continue;
                }
                    if (readname == null || readname.isEmpty()) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorReadname",
                            filepath, lineno, readname));
                        continue;
                    }

                    if (start >= stop) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorStartStop",
                            filepath, lineno, start, stop));
                        continue;
                    }
                    if (direction == 0) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorDirection", filepath, lineno));
                        continue;
                    }
                    if (readSeq == null || readSeq.isEmpty()) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorReadEmpty",
                            filepath, lineno, readSeq));
                        continue;
                    }
                    if (refSeq == null || refSeq.isEmpty()) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorRef",
                            filepath, lineno, refSeq));
                        continue;
                    }
                    if (readSeq.length() != refSeq.length()) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorReadLength",
                            filepath, lineno, readSeq, refSeq));
                        continue;
                    }
                    if (errors < 0 || errors > readSeq.length()) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorRead",
                            errors, filepath, lineno));
                    continue;
                }
                if (!cigar.matches("[MHISDPXN=\\d]+")) {
                    this.sendErrorMsg(NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.checkMapping.ErrorCigar", cigar, filepath, lineno));
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
                DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction);
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();

                    //   System.out.println("error" + errors);
                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                    int seqID;

                //  readSeq=isReverseStrand?SequenceUtils.getReverseComplement(readSeq):readSeq;
                      readSeqwithoutGaps = isReverseStrand ?  SequenceUtils.getReverseComplement(readSeqwithoutGaps) : readSeqwithoutGaps;
                if (this.seqToIDMap.containsKey(readSeqwithoutGaps)) {
                    seqID = this.seqToIDMap.get(readSeqwithoutGaps);
                    } else {
                        seqID = ++noUniqueReads; //int seqID = readnameToSequenceID.get(readname);
                    this.seqToIDMap.put(readSeqwithoutGaps, seqID);
                    } //readnameToSequenceID.get(readname);
                    mappingContainer.addParsedMapping(mapping, seqID);
             
        //            this.processReadname(seqID, readname);
                    if (!itor.hasNext()) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, NbBundle.getMessage(SAMBAMParser.class,
                            "Parser.Iterator.noMoreData",filepath));
                    }
                } else {
                    ++counterUnmapped;
                }
            }
        } catch (Exception e){
            this.sendErrorMsg(e.getMessage());
        }

        //TODO:Check if   necessary 
        mappingContainer.setNumberOfUniqueMappings(noUniqueMappings);//TODO: check if counting is correct here
        mappingContainer.setNumberOfUniqueSeq(noUniqueReads);
       // mappingContainer.setNumberOfReads(this.readnames.size());
        this.seqToIDMap = null; //release resources
        this.readnames = null;
        
        if (mappingContainer.getMappedSequenceIDs().isEmpty()) { //if track does not contain any reads
            throw new ParsingException(NbBundle.getMessage(SAMBAMParser.class, "Parser.Empty.Track.Error"));
        }
        if (counterUnmapped > 0){
            this.sendErrorMsg("Number of unmapped reads in file: "+counterUnmapped);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, NbBundle.getMessage(SAMBAMParser.class,
                "Parser.Parsing.Finished",filepath));
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,NbBundle.getMessage(SAMBAMParser.class,
                "Parser.Parsing.Successfully"));
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
                    if (direction == SequenceUtils.STRAND_REV) {
                        base = SequenceUtils.getDnaComplement(base, readSeq);
                    }

                    ParsedReferenceGap gap = new ParsedReferenceGap(absPos, base, this.getOrderForGap(absPos));
                    gaps.add(gap);
                    // note: do not increase position. that means that next base of read is mapped
                    // to the same position as this gap. two subsequent gaps map to the same position!
                } else {
                    // store the upper case char from input file, if this is a modification in the read
                    char c = readSeq.charAt(i);
                    c = Character.toUpperCase(c);
                    if (direction == SequenceUtils.STRAND_REV) {
                        c = SequenceUtils.getDnaComplement(c, readSeq);
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
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : this.observers) {
            observer.update(this.errorMsg);
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
