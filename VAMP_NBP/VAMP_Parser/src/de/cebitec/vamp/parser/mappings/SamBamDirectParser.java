package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.DirectAccessDataContainer;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.NbBundle;

/**
 * Sam/Bam parser for the data needed for a direct file access track. This means
 * the classification of the reads has to be carried out.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamDirectParser implements MappingParserI {

    private static String name = "SAM/BAM Direct Access Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam", "bam", "BAM", "Bam"};
    private static String fileDescription = "SAM/BAM Read Mappings";
    
    private SeqPairProcessorI seqPairProcessor;
    private List<Observer> observers;
    private int nbUniqueSeq;

    /**
     * Parser for parsing sam and bam data files for direct access in vamp.
     */
    public SamBamDirectParser() {
        this.observers = new ArrayList<>();
        this.seqPairProcessor = new SeqPairProcessorDummy();
    }
    
    /**
     * Parser for parsing sam and bam data files for direct access in vamp. 
     * Use this constructor for parsing sequence pair data along with the 
     * ordinary track data.
     * @param seqPairProcessor the specific sequence pair processor for handling
     *      sequence pair data
     */
    public SamBamDirectParser(SeqPairProcessorI seqPairProcessor) {
        this();
        this.seqPairProcessor = seqPairProcessor;
    }

    //TODO: think about statistic inclusion in some kind of ParsedMappingContainer?
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

    /**
     * Parses the input determined by the track job.
     * @param trackJob the track job to parse
     * @param refSeqWhole the reference sequence
     * @return a direct access data container constisting of:
     * a classification map: The key is the readname and each name
     * links to a pair consisting of the number of occurences of the read name
     * in the dataset (no mappings) and the lowest diff rate among all hits.
     * Remember that replicates are not needed, they can be deduced from the 
     * reads querried from an interval!
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public DirectAccessDataContainer parseInput(TrackJob trackJob, String refSeqWhole) throws ParsingException, OutOfMemoryError {

        String fileName = trackJob.getFile().getName();
        long startTime = System.currentTimeMillis();
        this.notifyObservers(NbBundle.getMessage(JokParser.class, "Parser.Parsing.Start", fileName));

        int lineno = 0;

        /*
         * id of each read sequence. Since the same file is used in both
         * iterations we don't need to store a mapping between read seq and id.
         * We assign the same id to each read sequence in both iterations. If
         * the file would change inbetween, this would not work!
         */
        String lastReadSeq = "";
        int seqId = -1;
        //mapping of read name to number of occurences of the read and the lowest error number
        Map<String, Pair<Integer, Integer>> classificationMap = new HashMap<>();

        String refSeq;
        int start;
        int stop;
        int differences;
        boolean isRevStrand;
        String readSeq;
        String cigar;
        String readName;
        Pair<Integer, Integer> classificationPair;
        DiffAndGapResult diffGapResult;

        try (SAMFileReader sam = new SAMFileReader(trackJob.getFile())) {
            SAMRecordIterator samItor = sam.iterator();

            SAMRecord record;
            while (samItor.hasNext()) {
                try {
                    ++lineno;

                    record = samItor.next();
                    if (!record.getReadUnmappedFlag()) {

                        cigar = record.getCigarString();
                        readSeq = record.getReadString();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        refSeq = refSeqWhole.substring(start - 1, stop);

                        if (!ParserCommonMethods.checkRead(this, readSeq, refSeqWhole.length(), cigar, start, stop, fileName, lineno)) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }

                        /*
                         * The cigar values are as follows: 0 (M) = alignment match
                         * (both, match or mismatch), 1 (I) = insertion, 2 (D) =
                         * deletion, 3 (N) = skipped, 4 (S) = soft clipped, 5 (H) =
                         * hard clipped, 6 (P) = padding, 7 (=) = sequene match, 8
                         * (X) = sequence mismatch. H not needed, because these
                         * bases are not present in the read sequence!
                         */
                        //count differences to reference
                        isRevStrand = record.getReadNegativeStrandFlag();
                        diffGapResult = ParserCommonMethods.createDiffsAndGaps(cigar, readSeq, refSeq, isRevStrand, start);
                        differences = diffGapResult.getDifferences();

                        // add data to map
                        readName = record.getReadName();
                        if (!classificationMap.containsKey(readName)) {
                            classificationMap.put(readName, new Pair<>(0, Integer.MAX_VALUE));
                        }
                        classificationPair = classificationMap.get(readName);
                        classificationPair.setFirst(classificationPair.getFirst() + 1);
                        if (classificationPair.getSecond() > differences) {
                            classificationPair.setSecond(differences);
                        }

                        // increase seqId for new read sequence and reset other fields
                        if (!lastReadSeq.equals(readSeq)) {
                            ++seqId;
                        }
                        lastReadSeq = readSeq;

                        this.seqPairProcessor.processReadname(seqId, readName);

                        //saruman starts genome at 0 other algorithms like bwa start genome at 1

                    } else {
                        this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                "Parser.Parsing.CorruptData", lineno, record.getReadName()));
                    }
                } catch (SAMFormatException e) {
                    this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                            "Parser.Parsing.CorruptData", lineno, e.toString()));
                }

            }

            samItor.close();
            this.nbUniqueSeq = seqId + 1;
        } catch (RuntimeEOFException e) {
            this.notifyObservers("Last read in the file is incomplete, ignoring it.");
        }

        long finish = System.currentTimeMillis();
        String msg = NbBundle.getMessage(SamBamDirectParser.class, "Parser.Parsing.Successfully", fileName);
        this.notifyObservers(Benchmark.calculateDuration(startTime, finish, msg));

        return new DirectAccessDataContainer(new CoverageContainer(), classificationMap);
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
            observer.update(data);
        }
    }

    /**
     * @return an empty sequence pair processor, since direct access tracks do not need it.
     */
    @Override
    public SeqPairProcessorI getSeqPairProcessor() {
        return this.seqPairProcessor;
    }

    /**
     * @return the number of unique sequences in the dataset
     */
    public int getNbUniqueSeq() {
        return this.nbUniqueSeq;
    }
    
}
