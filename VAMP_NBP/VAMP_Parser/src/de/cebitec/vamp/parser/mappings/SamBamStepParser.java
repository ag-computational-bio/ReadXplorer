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
 * @author jstraube
 */
public class SamBamStepParser implements MappingParserI {

    private static String name = "SAM/BAM Stepwise Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam", "bam", "BAM", "Bam"};
    private static String fileDescription = "SAM Output";
    
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

    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, String refSeqWhole) throws ParsingException {
        this.seqToIDMap = new HashMap<>();
        String readname;
        String refSeq;
        String readSeq;
        //  int flag = 0;
        String readSeqWithoutGaps;
        String cigar;
        String filename = trackJob.getFile().getName();
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
            SAMRecordIterator itor = sam.iterator();
            itorAll = itor;
        }
        SAMRecord nextRecord;
        int end = trackJob.getStop();
        end += shift;

        while (lineno < end) {
            lineno++;


            nextRecord = (record == null) ? (itorAll.hasNext() ? itorAll.next() : null) : record;
            //no more mappings
            if (nextRecord == null) {
                mappingContainer.setLastMappingContainer(true);
                break;
            }
            record = null;

            if (!nextRecord.getReadUnmappedFlag()) {

                readname = nextRecord.getReadName();
                start = nextRecord.getAlignmentStart();
                cigar = nextRecord.getCigarString();
                readSeqWithoutGaps = nextRecord.getReadString();

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
                
                isRevStrand = record.getReadNegativeStrandFlag();

                //check parameters
                if (!ParserCommonMethods.checkRead(this, readSeq, refSeqWhole.length(), cigar, start, stop, filename, lineno)) {
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

                if (nextRecord.getReadNegativeStrandFlag()) {
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
                this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                        "Parser.Parsing.CorruptData", lineno, nextRecord.getReadName()));
            }

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
