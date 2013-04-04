package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.parser.output.SamBamSorter;
import de.cebitec.vamp.util.ErrorLimit;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.SamUtils;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.openide.util.NbBundle;

/**
 *
 * @author jstraube
 */
public class SamBamStepParser implements MappingParserI, Observer {

    private static String name = "SAM/BAM Stepwise Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam", "bam", "BAM", "Bam"};
    private static String fileDescription = "SAM/BAM Read Mappings";
    
    private SeqPairProcessorI seqPairProcessor;
    private HashMap<String, Integer> seqToIDMap;
    private int noUniqueMappings;
    private ArrayList<Observer> observers;
    private String msg;
    private int noUniqueSeq = 0;
    //private BufferedReader brall=null;
    private SAMRecordIterator itorAll = null;
    private int lineno = 0;
    private int shift = 0;
    private SAMRecord record = null;

    /**
     * Parser for stepwise parsing of sam and bam data files in vamp.
     */
    public SamBamStepParser() {
        this.seqToIDMap = new HashMap<>();
        this.observers = new ArrayList<>();
        this.seqPairProcessor = new SeqPairProcessorDummy();
    }
    
    /**
     * Parser for stepwise parsing of sam and bam data files in vamp. Use
     * this constructor for parsing sequence pair data along with the ordinary
     * track data.
     * @param seqPairProcessor the specific sequence pair processor for handling
     *      sequence pair data
     */
    public SamBamStepParser(SeqPairProcessorI seqPairProcessor) {
        this();
        this.seqPairProcessor = seqPairProcessor;
    }
    
    /**
     * Does nothing, as the sam bam step parser currently does not need any
     * conversions.
     * @param trackJob
     * @param referenceSequence
     * @return true
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Object convert(TrackJob trackJob, String referenceSequence) throws ParsingException, OutOfMemoryError {
        return true;
    }

    /**
     * Sorts the file by read sequence.
     * @param trackJob the trackjob to sort
     * @return true, if the method succeeded
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        boolean success = true;
        if (!trackJob.isSorted()) {
            SamBamSorter sorter = new SamBamSorter();
            sorter.registerObserver(this);
            success = sorter.sortSamBam(trackJob, SAMFileHeader.SortOrder.readseq, SamUtils.SORT_READSEQ_STRING);
        }
        return success;
    }
    
    /**
     * First calls the preprocessing method, which sorts the sam/bam file by
     * read sequence in this implementation and then parses the input 
     * determined by the track job.
     * @param trackJob the track job to parse
     * @param refSeqWhole complete reference sequence 
     */
    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, String refSeqWhole) throws ParsingException {
        
        this.preprocessData(trackJob);
        
        this.seqToIDMap = new HashMap<>();
        String readname;
        String refSeq;
        String readSeq;
        //  int flag = 0;
        String readSeqWithoutGaps;
        String cigar;
        String filename = trackJob.getFile().getName();
        String refName = trackJob.getRefGen().getName();
        this.noUniqueMappings = 0;
        int start;
        int stop;
        int sumReadLength = 0;
        int differences;
        boolean isRevStrand;
        byte direction;
        int seqID;
        DiffAndGapResult result;
        List<ParsedDiff> diffs;
        List<ParsedReferenceGap> gaps;
        String[] refAndRead;
        
        String refSeqWithoutGaps;
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        mappingContainer.setFirstMappingContainer(trackJob.isFirstJob());

        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Start", filename));

        if (itorAll == null) {
            SAMFileReader sam = new SAMFileReader(trackJob.getFile());
            sam.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator itor = sam.iterator();
            itorAll = itor;
        }
        ErrorLimit errorLimit = new ErrorLimit();
        
        SAMRecord recrod;
        int end = trackJob.getStop();
        end += shift;

        while (lineno < end) {
            try {
                lineno++;

                recrod = (record == null) ? (itorAll.hasNext() ? itorAll.next() : null) : record;
                //no more mappings
                if (recrod == null) {
                    mappingContainer.setLastMappingContainer(true);
                    break;
                }
                record = null;
                int errorCount = 0;
                int maxErrorCount = 20;
                if (!recrod.getReadUnmappedFlag() && recrod.getReferenceName().equals(refName)) {

                    readname = recrod.getReadName();
                    start = recrod.getAlignmentStart();
                    cigar = recrod.getCigarString();
                    readSeqWithoutGaps = recrod.getReadString();

                    if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S") || cigar.contains("N")) {
                        stop = ParserCommonMethods.countStopPosition(cigar, start, readSeqWithoutGaps.length());
                        refSeqWithoutGaps = refSeqWhole.substring(start - 1, stop);
                        refAndRead = ParserCommonMethods.createMappingOfRefAndRead(cigar, refSeqWithoutGaps, readSeqWithoutGaps);
                        refSeq = refAndRead[0];
                        readSeq = refAndRead[1];
                    } else {
                        stop = start + readSeqWithoutGaps.length() - 1;
                        refSeqWithoutGaps = refSeqWhole.substring(start - 1, stop);
                        refSeq = refSeqWithoutGaps;
                        readSeq = readSeqWithoutGaps;
                    }

                    isRevStrand = recrod.getReadNegativeStrandFlag();

                    //check parameters
                    if (!ParserCommonMethods.checkReadSam(this, readSeq, refSeqWhole.length(), cigar, start, stop, filename, lineno)) {
                        continue; //continue, and ignore read, if it contains inconsistent information
                    }

                    //saruman starts genome at 0 other algorithms like bwa start genome at 1
                    result = ParserCommonMethods.createDiffsAndGaps(cigar, readSeq, refSeq, isRevStrand, start);
                    diffs = result.getDiffs();
                    gaps = result.getGaps();
                    differences = result.getDifferences();

                    if (differences < 0 || differences > readSeq.length()) {
                        this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                                "Parser.checkMapping.ErrorRead", differences, filename, lineno));
                        continue;
                    }

                    //saruman starts genome at 0 other algorithms like bwa start genome at 1
                    direction = isRevStrand ? (byte) -1 : 1;
                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, differences);

                    if (recrod.getReadNegativeStrandFlag()) {
                        readSeqWithoutGaps = SequenceUtils.getReverseComplement(readSeqWithoutGaps);
                    }

                    if (this.seqToIDMap.containsKey(readSeqWithoutGaps)) {
                        seqID = this.seqToIDMap.get(readSeqWithoutGaps);
                    } else {
                        seqID = ++noUniqueSeq;
                        this.seqToIDMap.put(readSeqWithoutGaps, seqID);
                    } //int seqID = readnameToSequenceID.get(readname);

                    mappingContainer.addParsedMapping(mapping, seqID);
                    sumReadLength += (stop - start);
                    this.seqPairProcessor.processReadname(seqID, readname);

                    if (lineno == end) {

                        mappingContainer.setSumReadLength(mappingContainer.getSumReadLength() + sumReadLength);

                        ++shift;
                        record = itorAll.hasNext() ? itorAll.next() : null;
                        if (record != null) {

                            String read = record.getReadString();
                            if (record.getReadNegativeStrandFlag()) {
                                read = SequenceUtils.getReverseComplement(read);
                            }

                            if (this.seqToIDMap.containsKey(read)) {
                                end += 1;
                            }

                        } else {
                            mappingContainer.setLastMappingContainer(true);
                            break;
                        }
                    }

                } else {
                    //skip error messages, if too many occur to prevent bug in the output panel
                    if (errorLimit.allowOutput()) {
                        this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.Parsing.CorruptData", lineno, recrod.getReadName()));
                    }
                }
                
            } catch (SAMFormatException e) {
                //skip error messages, if too many occur to prevent bug in the output panel
                if (!e.getMessage().contains("MAPQ should be 0")) {
                    if (errorLimit.allowOutput()) {
                        this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                        "Parser.Parsing.CorruptData", lineno, e.toString()));
                    }
                } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored  
            }
            
            
        }
        if (errorLimit.getSkippedCount()>0) {
                        this.sendMsg( "... "+(errorLimit.getSkippedCount())+" more errors occured");
        }
        

        this.seqToIDMap = null; //release resources

        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Finished", filename));
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

    @Override
    public void update(Object args) {
        this.notifyObservers(args);
    }

    /**
     * Method setting and sending the msg to all observers.
     * @param msg the msg to send (can be an error or any other message).
     */
    private void sendMsg(final String msg) {
        this.msg = msg;
        this.notifyObservers(null);
    }

    @Override
    public SeqPairProcessorI getSeqPairProcessor() {
        return this.seqPairProcessor;
    }
}
