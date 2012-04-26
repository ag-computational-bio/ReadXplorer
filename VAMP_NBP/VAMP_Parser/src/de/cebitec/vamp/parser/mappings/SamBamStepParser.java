package de.cebitec.vamp.parser.mappings;

import java.util.ArrayList;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.HashMap;
import org.openide.util.NbBundle;
import java.util.List;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

/**
 *
 * @author jstraube
 */
public class SamBamStepParser implements MappingParserI {

    private static String name = "SAM/BAM Stepwise Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam", "bam", "BAM", "Bam"};
    private static String fileDescription = "SAM Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private int errors = 0;
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

    public SamBamStepParser() {
        this.gapOrderIndex = new HashMap<Integer, Integer>();
        this.seqToIDMap = new HashMap<String, Integer>();
        this.observers = new ArrayList<Observer>();

    }

    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException {
        this.seqToIDMap = new HashMap<String, Integer>();
        String readname = null;
//        String refName = null;
        String refSeq = null;
        String readSeq = null;
        //  int flag = 0;
        String readSeqwithoutGaps = null;
        String cigar = null;
        String filename = trackJob.getFile().getName();
        this.noUniqueMappings = 0;
        int start;
        int stop;
        int sumReadLength = 0;

        //     String refSeqfulllength = null;
        String refSeqwithoutgaps = null;
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        mappingContainer.setFirstMappingContainer(trackJob.isFirstJob());
        //TODO check why if there is too much output we get a java heap space exception 
        // mappingContainer.registerObserver(this);

        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Start", filename));

        if (itorAll == null) {
            SAMFileReader sam = new SAMFileReader(trackJob.getFile());
            SAMRecordIterator itor = sam.iterator();
            itorAll = itor;
        }
        SAMRecord first = null;
        int end = trackJob.getStop();
        end = end + shift;

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

                stop = 0;
                errors = 0;

                int length = sequenceString.length();

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

                byte direction = 0;
                // 1 = fwd, -1 = rev
                direction = first.getReadNegativeStrandFlag() ? (byte) -1 : 1;

                //check parameters
                if (length < start || length < stop) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorReadPosition",
                            filename, lineno, start, stop, length));
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
                if (errors < 0 || errors > readSeq.length()) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorRead", errors, filename, lineno));
                    continue;
                }
                if (!cigar.matches("[MHISDPXN=\\d]+")) {
                    this.sendMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorCigar", cigar, filename, lineno));
                    continue;
                }
                //saruman starts genome at 0 other algorithms like bwa start genome at 1
                DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction);
                List<ParsedDiff> diffs = result.getDiffs();
                List<ParsedReferenceGap> gaps = result.getGaps();

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


                if (lineno == end) {
                    
                    mappingContainer.setSumReadLength(mappingContainer.getSumReadLength() + sumReadLength);
                    
                    shift++;
                    record = itorAll.hasNext() ? itorAll.next() : null;
                    if (record != null) {

                        String read = record.getReadString().toLowerCase();
                        read = record.getReadNegativeStrandFlag() ? SequenceUtils.getReverseComplement(read) : read;
                        if (this.seqToIDMap.containsKey(read)) {
                            end = end + 1;
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
    /**
     * This method calculates the order of the gap infact that for a gap we dont include a new position to reference genome
     *  but we notice the number of gaps to one position of the ref genome
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
                    if (direction == -1) {
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
                    if (direction == -1) {
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
    public void processReadname(int seqID, String readName) {
        //TODO:Readnames
    }
}
