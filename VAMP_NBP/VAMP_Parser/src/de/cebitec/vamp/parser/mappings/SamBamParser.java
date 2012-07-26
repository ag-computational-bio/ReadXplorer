package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.openide.util.NbBundle;

/**
 * 
 * @author jstraube, rhilker
 */
public class SamBamParser implements MappingParserI {

    private static String name = "SAM/BAM Parser";
    private static String[] fileExtension = new String[]{"bam", "BAM", "Bam", "sam", "SAM", "Sam"};
    private static String fileDescription = "BAM or SAM Output";
    
    private SeqPairProcessorI seqPairProcessor;
    private HashMap<String, Integer> seqToIDMap;
    private String msg;
    private int noUniqueMappings;
    private ArrayList<String> readnames;
    private ArrayList<Observer> observers;

    public SamBamParser() {
        this.seqToIDMap = new HashMap<String, Integer>();
        this.readnames = new ArrayList<String>();
        this.observers = new ArrayList<Observer>();
        this.seqPairProcessor = new SeqPairProcessorDummy();
    }
    
    /**
     * Parser for parsing sam and bam data files for direct access in vamp. Use
     * this constructor for parsing sequence pair data along with the ordinary
     * track data.
     * @param seqPairProcessor the specific sequence pair processor for handling
     * sequence pair data
     */
    public SamBamParser(SeqPairProcessorI seqPairProcessor) {
        this();
        this.seqPairProcessor = seqPairProcessor;
    }

    @Override
//    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError {
        int lineno = 0;
        String filepath = trackJob.getFile().getAbsolutePath();
        String readname;
        String refSeq;
        String readSeq;
        String readSeqwithoutGaps;
        String cigar;
        String refSeqwithoutgaps;
        noUniqueMappings = 0;
        int noUniqueReads = 0;
        int counterUnmapped = 0;
        int sumReadLength = 0;
        HashMap<Integer, Integer> gapOrderIndex = new HashMap<Integer, Integer>();
        int errors;
        int start;
        int stop;
        SAMRecord first;
        boolean isReverseStrand;
        byte direction;
        int length;
        DiffAndGapResult result;
        ParsedMapping mapping;
        int seqID;

        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Start", filepath));

        SAMFileReader sam = new SAMFileReader(trackJob.getFile());        
        SAMRecordIterator itor;
        try {
            itor = sam.iterator();
        } catch (RuntimeException e) {
            throw new ParsingException(e.getMessage() + ". !! Track will be empty, thus not be stored !!");
        }
        try {
            while (itor.hasNext()) {
                first = itor.next();
                start = first.getAlignmentStart();
                if (!first.getReadUnmappedFlag()) {

                    isReverseStrand = first.getReadNegativeStrandFlag();
                    direction = isReverseStrand ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                    readname = first.getReadName();
                    cigar = first.getCigarString();
                    readSeqwithoutGaps = first.getReadString().toLowerCase();

                    length = sequenceString.length();
                    if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S") || cigar.contains("N")) {
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
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorReadPosition",
                                filepath, lineno, start, stop, length));
                        continue;
                    }
                    if (readname == null || readname.isEmpty()) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorReadname",
                                filepath, lineno, readname));
                        continue;
                    }

                    if (start >= stop) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorStartStop",
                                filepath, lineno, start, stop));
                        continue;
                    }
                    if (direction == 0) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorDirection", filepath, lineno));
                        continue;
                    }
                    if (readSeq == null || readSeq.isEmpty()) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorReadEmpty",
                                filepath, lineno, readSeq));
                        continue;
                    }
                    if (refSeq == null || refSeq.isEmpty()) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorRef",
                                filepath, lineno, refSeq));
                        continue;
                    }
                    if (readSeq.length() != refSeq.length()) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorReadLength",
                                filepath, lineno, readSeq, refSeq));
                        continue;
                    }
                    if (!cigar.matches("[MHISDPXN=\\d]+")) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
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
                    result = ParserCommonMethods.createDiffsAndGaps(readSeq, refSeq, start, direction);
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();
                    errors = result.getErrors();
                    
                    if (errors < 0 || errors > readSeq.length()) {
                        this.sendMsg(NbBundle.getMessage(SamBamParser.class,
                                "Parser.checkMapping.ErrorRead",
                                errors, filepath, lineno));
                        continue;
                    }

                    mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);

                    //  readSeq=isReverseStrand?SequenceUtils.getReverseComplement(readSeq):readSeq;
                    readSeqwithoutGaps = isReverseStrand ? SequenceUtils.getReverseComplement(readSeqwithoutGaps) : readSeqwithoutGaps;
                    if (this.seqToIDMap.containsKey(readSeqwithoutGaps)) {
                        seqID = this.seqToIDMap.get(readSeqwithoutGaps);
                    } else {
                        seqID = ++noUniqueReads; //int seqID = readnameToSequenceID.get(readname);
                        this.seqToIDMap.put(readSeqwithoutGaps, seqID);
                    } //readnameToSequenceID.get(readname);
                    mappingContainer.addParsedMapping(mapping, seqID);
                    sumReadLength += (stop - start);
                    this.seqPairProcessor.processReadname(seqID, readname);
                    
                    if (!itor.hasNext()) {
                        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Iterator.noMoreData", filepath));
                    }
                } else {
                    ++counterUnmapped;
                }
            }
        } catch (Exception e) {
            this.sendMsg(e.getMessage());
        }

//        int numberMappings = mappingContainer.getMappingInformations().get(1);
//        numberMappings = numberMappings == 0 ? 1 : numberMappings;
        mappingContainer.setSumReadLength(sumReadLength);

        this.seqToIDMap = null; //release resources
        this.readnames = null;
        
        if (mappingContainer.getMappedSequenceIDs().isEmpty()) { //if track does not contain any reads
            throw new ParsingException(NbBundle.getMessage(SamBamParser.class, "Parser.Empty.Track.Error"));
        }
        if (counterUnmapped > 0){
            this.sendMsg("Number of unmapped reads in file: "+counterUnmapped);
        }

        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Finished", filepath));
        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Successfully"));

        return mappingContainer;
    }

    @Override
    public String getName() {
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
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(this.msg);
        }
    }

    /**
     * Method setting and sending the msg to all observers.
     * @param msg the  msg to send (can be an error or any other message).
     */
    private void sendMsg(final String msg) {
        this.msg = msg;
        this.notifyObservers(null);
    }

    @Override
    public SeqPairProcessorI getSeqPairProcessor() {
        return this.seqPairProcessor;
    }

    /**
     * Dummy method.
     * @return null, because it is not needed here.
     */
    @Override
    public Object getAdditionalData() {
        return null;
    }
    
}
