package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.CoverageContainer;
import de.cebitec.readXplorer.parser.common.DiffAndGapResult;
import de.cebitec.readXplorer.parser.common.DirectAccessDataContainer;
import de.cebitec.readXplorer.parser.common.ParsedClassification;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.ErrorLimit;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.StatsContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.NbBundle;

/**
 * Sam/Bam parser for the data needed for a direct file access track. This means
 * the classification of the reads has to be carried out. The sam/bam file has
 * to be sorted by readname for this classification.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamDirectParser implements MappingParserI, Observer {

    private static String name = "SAM/BAM Direct Access Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam", "bam", "BAM", "Bam"};
    private static String fileDescription = "SAM/BAM Read Mappings";
    
    private List<Observer> observers;
    private StatsContainer statsContainer;

    /**
     * Parser for parsing sam and bam data files for direct access in readXplorer.
     */
    public SamBamDirectParser() {
        this.observers = new ArrayList<>();
        this.statsContainer = new StatsContainer();
        this.statsContainer.prepareForTrack();
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

    /**
     * Does nothing, as the sam bam direct parser currently does not need any conversions.
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
     * Currently does nothing, since no preprocessing is required.
     * @param trackJob the trackjob to preprocess
     * @return true, if the method succeeded, false otherwise
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
//        SamBamSorter sorter = new SamBamSorter();
//        sorter.registerObserver(this);
//         sorter.sortSamBam(trackJob, SAMFileHeader.SortOrder.readseq, SamUtils.SORT_READSEQ_STRING);
        return true;
    }

    /**
     * First calls the preprocessing method, which sorts the sam/bam file by
     * readname in this implementation and then parses the input determined by 
     * the track job.
     * @param trackJob the track job to parse
     * @param refSeqWhole the reference sequence
     * @return a direct access data container constisting of:
     * a classification map: The key is the readname and each name
     * links to a pair consisting of the number of occurrences of the read name
     * in the dataset (no mappings) and the lowest diff rate among all hits.
     * Remember that replicates are not needed, they can be deduced from the 
     * reads querried from an interval!
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public DirectAccessDataContainer parseInput(TrackJob trackJob, String refSeqWhole) throws ParsingException, OutOfMemoryError {

        boolean success = (boolean) this.preprocessData(trackJob);
        
        String fileName = trackJob.getFile().getName();
        String refName = trackJob.getRefGen().getName();
        long startTime = System.currentTimeMillis();
        this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class, "Parser.Parsing.Start", fileName));

        int lineno = 0;

        //mapping of read name to number of occurences of the read and the lowest mismatch count
        Map<String, ParsedClassification> classificationMap = new HashMap<>();

        String refSeq;
        int start;
        int stop;
        int mismatches;
        boolean isRevStrand;
        String readSeq;
        String cigar;
        String readName;
        ParsedClassification classificationData;
        DiffAndGapResult diffGapResult;
        ErrorLimit errorLimit = new ErrorLimit();

        try (SAMFileReader sam = new SAMFileReader(trackJob.getFile())) {
            sam.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMFileHeader.SortOrder sortOrder = sam.getFileHeader().getSortOrder();
            SAMRecordIterator samItor = sam.iterator();

            SAMRecord record;
            while (samItor.hasNext()) {
                try {
                    ++lineno;

                    record = samItor.next();
                    if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {

                        cigar = record.getCigarString();
                        readSeq = record.getReadString();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        refSeq = refSeqWhole.substring(start - 1, stop);

                        if (!ParserCommonMethods.checkReadSam(this, readSeq, refSeqWhole.length(), cigar, start, stop, fileName, lineno)) {
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
                        mismatches = diffGapResult.getDifferences();
                        readName = ParserCommonMethods.elongatePairedReadName(record);
                        if (!classificationMap.containsKey(readName)) {
                            classificationMap.put(readName, new ParsedClassification(sortOrder));
                        }
                        classificationData = classificationMap.get(readName);
                        classificationData.addReadStart(start);
                        classificationData.updateMinMismatches(mismatches);

                        //saruman starts genome at 0 other algorithms like bwa start genome at 1

                    } else { // else read is unmapped or belongs to another reference
                        if (errorLimit.allowOutput()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                "Parser.Parsing.CorruptData", lineno, record.getReadName()));
                        }
                    }
                } catch (SAMFormatException e) {
                    if (errorLimit.allowOutput()) {
                        if (!e.getMessage().contains("MAPQ should be 0")) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                "Parser.Parsing.CorruptData", lineno, e.toString()));
                        }
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored  
                }

            }
            if (errorLimit.getSkippedCount() > 0) {
                     this.notifyObservers( "... " + errorLimit.getSkippedCount() + " more errors occured");
            }

            samItor.close();
        } catch (RuntimeEOFException e) {
            this.notifyObservers("Last read in the file is incomplete, ignoring it.");
        } catch (Exception e) {
            this.notifyObservers(e.getMessage());
        }

        long finish = System.currentTimeMillis();
        String msg = NbBundle.getMessage(SamBamDirectParser.class, "Parser.Parsing.Successfully", fileName);
        this.notifyObservers(Benchmark.calculateDuration(startTime, finish, msg));
        statsContainer.increaseValue(StatsContainer.NO_READS, classificationMap.size());

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

    @Override
    public void update(Object args) {
        this.notifyObservers(args);
    }
    
    /**
     * Adds a statistics container for handling statistics for the extended track.
     * @param statsContainer the container
     */
    @Override
    public void setStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }

    /**
     * @return The statistics parser for handling statistics for the extended
     * track.
     */
    public StatsContainer getStatsContainer() {
        return statsContainer;
    }
}
