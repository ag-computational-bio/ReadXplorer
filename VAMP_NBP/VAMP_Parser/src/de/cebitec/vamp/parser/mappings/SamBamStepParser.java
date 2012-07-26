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
        this.seqToIDMap = new HashMap<String, Integer>();
        this.observers = new ArrayList<Observer>();
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
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException {
        this.seqToIDMap = new HashMap<String, Integer>();
        String readname;
//        String refName = null;
        String refSeq;
        String readSeq;
        //  int flag = 0;
        String readSeqwithoutGaps;
        String cigar;
        String filename = trackJob.getFile().getName();
        this.noUniqueMappings = 0;
        int start;
        int stop;
        int sumReadLength = 0;
        int refSeqLength = sequenceString.length();
        int errors;
        byte direction;

        //     String refSeqfulllength = null;
        String refSeqwithoutgaps;
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        mappingContainer.setFirstMappingContainer(trackJob.isFirstJob());

        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Start", filename));

        if (itorAll == null) {
            SAMFileReader sam = new SAMFileReader(trackJob.getFile());
            SAMRecordIterator itor = sam.iterator();
            itorAll = itor;
        }
        SAMRecord first;
        int end = trackJob.getStop();
        end += shift;

        while (lineno < end) {
            lineno++;


            first = (record == null) ? (itorAll.hasNext() ? itorAll.next() : null) : record;
            //no more mappings
            if (first == null) {
                mappingContainer.setLastMappingContainer(true);
                break;
            }
            record = null;

            if (!first.getReadUnmappedFlag()) {

                readname = first.getReadName();
                start = first.getAlignmentStart();
                cigar = first.getCigarString();
                readSeqwithoutGaps = first.getReadString().toLowerCase();

                if (cigar.contains("D") || cigar.contains("I") || cigar.contains("S") || cigar.contains("N")) {
                    stop = ParserCommonMethods.countStopPosition(cigar, start, readSeqwithoutGaps.length());
                    refSeqwithoutgaps = sequenceString.substring(start - 1, stop).toLowerCase();
                    String[] refAndRead = ParserCommonMethods.createMappingOfRefAndRead(cigar, refSeqwithoutgaps, readSeqwithoutGaps);
                    refSeq = refAndRead[0];
                    readSeq = refAndRead[1];
                } else {
                    stop = start + readSeqwithoutGaps.length() - 1;
                    refSeqwithoutgaps = sequenceString.substring(start - 1, stop).toLowerCase();
                    refSeq = refSeqwithoutgaps;
                    readSeq = readSeqwithoutGaps;
                }
                
                direction = first.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;

                //check parameters
                if (refSeqLength < start || refSeqLength < stop) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorReadPosition",
                            filename, lineno, start, stop, refSeqLength));
                    continue;
                }
                if (readname == null || readname.isEmpty()) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorReadname", filename, lineno, readname));
                    continue;
                }

                if (start >= stop) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorStartStop", filename, lineno, start, stop));
                    continue;
                }
                if (direction == 0) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorDirection", filename, lineno));
                    continue;
                }
                if (readSeq == null || readSeq.isEmpty()) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorReadEmpty", filename, lineno, readSeq));
                    continue;
                }
                if (refSeq == null || refSeq.isEmpty()) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorRef", filename, lineno, refSeq));
                    continue;
                }
                if (readSeq.length() != refSeq.length()) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorReadLength", filename, lineno, readSeq, refSeq));
                    continue;
                }
                if (!cigar.matches("[MHISDPXN=\\d]+")) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorCigar", cigar, filename, lineno));
                    continue;
                }
                //saruman starts genome at 0 other algorithms like bwa start genome at 1
                DiffAndGapResult result = ParserCommonMethods.createDiffsAndGaps(readSeq, refSeq, start, direction);
                List<ParsedDiff> diffs = result.getDiffs();
                List<ParsedReferenceGap> gaps = result.getGaps();
                errors = result.getErrors();

                if (errors < 0 || errors > readSeq.length()) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorRead", errors, filename, lineno));
                    continue;
                }
                
                //saruman starts genome at 0 other algorithms like bwa start genome at 1
                ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);



                readSeqwithoutGaps = first.getReadNegativeStrandFlag() ? SequenceUtils.getReverseComplement(readSeqwithoutGaps) : readSeqwithoutGaps;
                int seqID;
                if (this.seqToIDMap.containsKey(readSeqwithoutGaps)) {
                    seqID = this.seqToIDMap.get(readSeqwithoutGaps);
                } else {
                    seqID = ++noUniqueSeq;
                    this.seqToIDMap.put(readSeqwithoutGaps, seqID);
                } //int seqID = readnameToSequenceID.get(readname);

                mappingContainer.addParsedMapping(mapping, seqID);
                sumReadLength += (stop - start);
                this.seqPairProcessor.processReadname(seqID, readname);

                if (lineno == end) {
                    
                    mappingContainer.setSumReadLength(mappingContainer.getSumReadLength() + sumReadLength);
                    
                    shift++;
                    record = itorAll.hasNext() ? itorAll.next() : null;
                    if (record != null) {

                        String read = record.getReadString().toLowerCase();
                        read = record.getReadNegativeStrandFlag() ? SequenceUtils.getReverseComplement(read) : read;
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
                        "Parser.Parsing.CorruptData", lineno, first.getReadName()));
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
    
    /**
     * Dummy method.
     * @return null, because it is not needed here.
     */
    @Override
    public Object getAdditionalData() {
        return null;
    }
}
