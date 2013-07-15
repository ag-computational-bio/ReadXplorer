package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.DiscreteCountingDistribution;
import de.cebitec.vamp.util.ErrorLimit;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.util.StatsContainer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Creates and stores the position table for a sam or bam file, which needs to
 * be sorted by position and creates most of its statistics. The data to store
 * is directly forwarded to the observer, which should then further process it
 * (store it in the db).
 * @deprecated Storing of position table in the db not needed anymore
 *
 * @author -Rolf Hilker-
 */
@Deprecated
public class SamBamPosTableCreator implements Observable {

    private List<Observer> observers;
    private CoverageContainer coverageContainer;
    private StatsContainer statsContainer;
    private DiscreteCountingDistribution readLengthDistribution;
    
    /**
     * Creates and stores the position table for a sam or bam file, which needs
     * to be sorted by position and creates most of its statistics. The data to
     * store is directly forwarded to the observer, which should then further
     * process it (store it in the db).
     */
    public SamBamPosTableCreator() {
        this.observers = new ArrayList<>();
        this.coverageContainer = new CoverageContainer();
        this.readLengthDistribution = new DiscreteCountingDistribution(400);
        readLengthDistribution.setType(Properties.READ_LENGTH_DISTRIBUTION);
    }
    
    /**
     * Creates the position table for the given track job, which needs to be
     * sorted by position and creates most of its statistics. The data to store
     * is directly forwarded to the observer, which should then further process
     * it (store it in the db).
     * @param trackJob track job whose position table needs to be created
     * @param refSeqWhole reference genome sequence
     * @return  
     */
    @SuppressWarnings("fallthrough")
    public ParsedTrack createPosTable(TrackJob trackJob, String refSeqWhole) {
        
        long startTime = System.currentTimeMillis();
        String fileName = trackJob.getFile().getName();
        String refName = trackJob.getRefGen().getName();
        this.notifyObservers(NbBundle.getMessage(SamBamPosTableCreator.class, "PosTableCreator.Start", fileName));
        
        List<ParsedMapping> batchOverlappingMappings = new ArrayList<>();
        ParsedTrack track;

//        int noMappings = 0;
//        long starti = System.currentTimeMillis();
        final int batchSize = 300000;
        int nextBatch = batchSize;
        int lineno = 0;
        
        String lastReadSeq = "";
        List<Integer> readsDifferentPos = new ArrayList<>();
        int seqCount = 0;

        ParsedMapping mapping;
        String refSeq;
        int start;
        int stop;
        int differences;
        boolean isRevStrand;
        byte direction;
        String readName;
        String readSeq;
        String cigar;
        int readClass;
        int mapCount;
        DiffAndGapResult diffGapResult;
        List<Pair<Integer, Integer>> coveredCommonIntervals = new ArrayList<>();
        List<Pair<Integer, Integer>> coveredBestMatchIntervals = new ArrayList<>();
        List<Pair<Integer, Integer>> coveredPerfectIntervals = new ArrayList<>();
        coveredCommonIntervals.add(new Pair<>(0, 0));
        coveredBestMatchIntervals.add(new Pair<>(0, 0));
        coveredPerfectIntervals.add(new Pair<>(0, 0));
        Integer classification;
        Integer mappingCount;
        ErrorLimit errorLimit = new ErrorLimit();
        Set<String> readNameSet = new HashSet<>();
        
//        String[] nameArray;
//        String shortReadName;

        try (SAMFileReader sam = new SAMFileReader(trackJob.getFile())) {
            sam.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samItor = sam.iterator();

            SAMRecord record;
            while (samItor.hasNext()) {
                try {
                    ++lineno;

                    record = samItor.next();
                    readName = record.getReadName();
                    if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {

                        cigar = record.getCigarString();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        readSeq = record.getReadString();
                        refSeq = refSeqWhole.substring(start - 1, stop);

                        if (!ParserCommonMethods.checkReadSam(this, readSeq, refSeqWhole.length(), cigar, start, stop, fileName, lineno)) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }

                        //statistics calculations: count no reads and distinct sequences ////////////
                        //illumina read hack to reduce memory footprint
//                        nameArray = readName.split(":");
//                        shortReadName = nameArray[2] + nameArray[3] + nameArray[4];
//                        nameArray = shortReadName.split("#");
//                        shortReadName = nameArray[0] + "/" + nameArray[1].split("/")[1];
//                        readNameSet.add(shortReadName);
                        if (statsContainer.getStatsMap().get(StatsContainer.NO_READS) <= 0) {
                            readNameSet.add(readName);
                        }
                        
                        mappingCount = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                        if (mappingCount != null) {
                            mapCount = mappingCount;
                        } else {
                            mapCount = 0;
                        }

                        classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                        if (classification != null) {
                            readClass = classification;
                            if (mapCount == 1) {
                                switch (classification) { //fallthrough is necessary
                                    case (int) Properties.PERFECT_COVERAGE: 
                                        statsContainer.increaseValue(StatsContainer.NO_UNIQ_PERF_MAPPINGS, mapCount);
                                    case (int) Properties.BEST_MATCH_COVERAGE:
                                        statsContainer.increaseValue(StatsContainer.NO_UNIQ_BM_MAPPINGS, mapCount);
                                    default:
                                        statsContainer.increaseValue(StatsContainer.NO_UNIQ_MAPPINGS, mapCount);
                                }
                            }
                        } else {
                            readClass = Properties.COMPLETE_COVERAGE;
                            if (mapCount == 1) {
                                statsContainer.increaseValue(StatsContainer.NO_UNIQ_MAPPINGS, mapCount);
                            }
                        }
                        readLengthDistribution.increaseDistribution(readSeq.length());
                        
                        if (!lastReadSeq.equals(readSeq)) { //same seq counted multiple times when multiple reads with same sequence
                            if (readsDifferentPos.size() == 1) { //1 means all reads since last clean started at same pos
                                if (seqCount == 1) { // only one sequence found at same position
                                    statsContainer.increaseValue(StatsContainer.NO_UNIQUE_SEQS, seqCount);
                                } else {
                                    statsContainer.increaseValue(StatsContainer.NO_REPEATED_SEQ, 1);
                                    //counting the repeated seq and not in how many reads they are contained
                                }
                            }
                            readsDifferentPos.clear();
                        }
                        if (!readsDifferentPos.contains(start)) {
                            readsDifferentPos.add(start);
                            seqCount = 0;
                        }
                        ++seqCount;
                        lastReadSeq = readSeq;

                        this.updateIntervals(coveredCommonIntervals, start, stop);
                        if (readClass == Properties.PERFECT_COVERAGE) {
                            this.updateIntervals(coveredPerfectIntervals, start, stop);
                            this.updateIntervals(coveredBestMatchIntervals, start, stop);
                            statsContainer.increaseValue(StatsContainer.NO_PERFECT_MAPPINGS, 1);
                            statsContainer.increaseValue(StatsContainer.NO_BESTMATCH_MAPPINGS, 1);
                        } else if (readClass == Properties.BEST_MATCH_COVERAGE) {
                            this.updateIntervals(coveredBestMatchIntervals, start, stop);
                            statsContainer.increaseValue(StatsContainer.NO_BESTMATCH_MAPPINGS, 1);
                        }
                        statsContainer.increaseValue(StatsContainer.NO_COMMON_MAPPINGS, 1);
                        ////////////////////////////////////////////////////////////////////////

                        if (classification == null
                                || classification == (int) Properties.PERFECT_COVERAGE
                                || classification == (int) Properties.BEST_MATCH_COVERAGE) {
                            // if the classification is not stored, we have to use all available mappings for the pos table
                            // (this practice will cause detecting unwanted SNPs, but the alternative is 0 SNPs)

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

                            //store data for position table
                            direction = isRevStrand ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                            mapping = new ParsedMapping(start, stop, direction, diffGapResult.getDiffs(), diffGapResult.getGaps(), differences);
                            mapping.setIsBestMapping(readClass == Properties.PERFECT_COVERAGE || readClass == Properties.BEST_MATCH_COVERAGE);
                            this.coverageContainer.addMapping(mapping); //this is not the case for ordinary database import!
                            this.coverageContainer.savePositions(mapping);

                            //store position table for each 300.000 positions in db
                            //at first store mappings which overlap next batch position separately
                            if (stop > nextBatch) {
                                batchOverlappingMappings.add(mapping);
                            }

                            //saruman starts genome at 0 other algorithms like bwa start genome at 1
                        }
                        
                        //handling that always has to be performed
                        if (start > nextBatch) { //e.g. 300.001
                            track = new ParsedTrack(trackJob, null, coverageContainer);
                            track.setBatchPos(stop);
                            this.notifyObservers(track);
                            this.coverageContainer.clearCoverageContainer();
                            this.refillCoverageContainer(batchOverlappingMappings, nextBatch);
                            nextBatch += batchSize;
                            batchOverlappingMappings.clear();
                        }
                        
//                        //can be used for debugging performance
//                        if (++noMappings % 10000 == 0) {
//                            long finish = System.currentTimeMillis();
//                            this.notifyObservers(Benchmark.calculateDuration(starti, finish, noMappings + " mappings processed. "));
//                            starti = System.currentTimeMillis();
//                        }
                    } else {
                        //skip error messages, if too many occur to prevent bug in the output panel
                        if (errorLimit.allowOutput()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamPosTableCreator.class,
                                "Parser.Parsing.CorruptData", lineno, readName));
                        }
                    }
                } catch (Exception e) {
                    //skip error messages, if too many occur to prevent bug in the output panel
                    if (!e.getMessage().contains("MAPQ should be 0")) {
                    //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored  
                        if (errorLimit.allowOutput()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                            "Parser.Parsing.CorruptData", lineno, e.toString()));
                        }
                    }
                }
                if ((lineno % 500000) == 0)  {//output process info only on every XX line
                    this.notifyObservers(lineno + " mappings processed ...");
                }
            }
            if (errorLimit.getSkippedCount() > 0) {
                this.notifyObservers( "... " + (errorLimit.getSkippedCount()) + " more errors occured");
            }
            samItor.close();

        } catch (RuntimeEOFException e) {
            this.notifyObservers("Last read in file is incomplete, ignoring it!");
        } catch (Exception e) {
            Exceptions.printStackTrace(e); //TODO: correct error handling or remove
        }
        
        //finish statistics and return the track with the statistics data in the end
        coverageContainer.setCoveredCommonMatchPositions(this.getCoveredBases(coveredCommonIntervals));
        coverageContainer.setCoveredBestMatchPositions(this.getCoveredBases(coveredBestMatchIntervals));
        coverageContainer.setCoveredPerfectPositions(this.getCoveredBases(coveredPerfectIntervals));

        track = new ParsedTrack(trackJob, new ParsedMappingContainer(), coverageContainer);
        statsContainer.increaseValue(StatsContainer.NO_READS, readNameSet.size());
        statsContainer.setReadLengthDistribution(readLengthDistribution);
        track.setStatsContainer(statsContainer);
        track.setBatchPos(nextBatch);
        this.notifyObservers(track);
        this.coverageContainer = new CoverageContainer();
        readNameSet.clear();
        
        long finish = System.currentTimeMillis();
        String msg = NbBundle.getMessage(SamBamDirectParser.class, "PosTableCreator.Finished", fileName);
        this.notifyObservers(Benchmark.calculateDuration(startTime, finish, msg));
        
        return track;
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
     * Refills the coverage container and the position table with all positions,
     * that have a diff and are larger than the current clear position.
     * @param batchOverlappingMappings the mappings used to refill the data structures
     * @param clearPos the position up to which the diffs should not be stored anymore
     */
    private void refillCoverageContainer(List<ParsedMapping> batchOverlappingMappings, int clearPos) {
        for (ParsedMapping mapping : batchOverlappingMappings) {
            this.coverageContainer.addMapping(mapping);
            this.coverageContainer.savePositions(mapping);
            //now we have to remove the positions already stored completely in
            //the db (which are all pos < start
            this.coverageContainer.clearCoverageContainerUpTo(clearPos);
        }
    }
    
    /**
     * Counts all  bases, which are covered by reads in this data set
     * @param coveredIntervals the covered intervals of the data set
     * @return the number of bases covered in the data set
     */
    private int getCoveredBases(List<Pair<Integer, Integer>> coveredIntervals) {
        int coveredBases = 0;
        for (Pair<Integer, Integer> interval : coveredIntervals) {
            coveredBases += (int) interval.getSecond() - (int) interval.getFirst();
        }
        return coveredBases;
    }

    /**
     * Update the last interval of the given list or create a new interval in 
     * the given list, if the new boundaries are beyond the boundaries of the 
     * last interval in the list.
     * @param coveredIntervals list of covered intervals
     * @param start start pos of new interval to add
     * @param stop stop pos of new interval to add
     */
    private void updateIntervals(List<Pair<Integer, Integer>> coveredIntervals, int start, int stop) {
        //store coverage statistic
        int lastIndex = coveredIntervals.size() - 1;
        if (coveredIntervals.get(lastIndex).getSecond() < start) { //add new pair
            coveredIntervals.add(new Pair<>(start, stop));
        } else { //increase length of first pair (start remains, stop is enlarged)
            coveredIntervals.get(lastIndex).setSecond(stop);
        }
    }   
    
    /**
     * Adds a statistics parser for handling statistics for the extended track.
     * @param statsContainer  the stats container
     */
    public void addStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }

    /**
     * @return The statistics parser for handling statistics for the extended track.
     */
    public StatsContainer getStatsContainer() {
        return statsContainer;
    }
    
}
