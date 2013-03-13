package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.ErrorLimit;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Creates and stores the position table for a sam or bam file, which needs
 * to be sorted by position. The data to store is directly forwarded to the 
 * observer, which should then further process it (store it in the db).
 *
 * @author -Rolf Hilker-
 */
public class SamBamPosTableCreator implements Observable {

    private List<Observer> observers;
    private CoverageContainer coverageContainer;
    private Map<Integer, Integer> mappingInfos;
    
    /**
     * Creates and stores the position table for a sam or bam file, which needs
     * to be sorted by position. The data to store is directly forwarded to the 
     * observer, which should then further process it (store it in the db).
     */
    public SamBamPosTableCreator() {
        this.observers = new ArrayList<>();
        this.coverageContainer = new CoverageContainer();
        this.mappingInfos = new HashMap<>();
    }
    
    /**
     * Creates the position table for the given track job, which needs to be
     * sorted by position. The data to store is directly forwarded to the
     * observer, which should then further process it (store it in the db).
     * @param trackJob track job whose position table needs to be created
     * @param refSeqWhole reference genome sequence
     * @return  
     */
    public ParsedTrack createPosTable(TrackJob trackJob, String refSeqWhole) {
        
        long startTime = System.currentTimeMillis();
        String fileName = trackJob.getFile().getName();
        String refName = trackJob.getRefGen().getName();
        this.notifyObservers(NbBundle.getMessage(SamBamPosTableCreator.class, "PosTableCreator.Start", fileName));
        
        List<ParsedMapping> batchOverlappingMappings = new ArrayList<>();
        ParsedTrack track;

        final int batchSize = 300000;
        int nextBatch = batchSize;
        int lineno = 0;
        
        String lastReadSeq = "";
        String readName = "";
        int noReads = 0;
        int noUniqueMappings = 0;
        int noBestMatch = 0;
        int noPerfect = 0;
        List<String> readNamesSameSeq = new ArrayList<>();
        List<Integer> readsDifferentPos = new ArrayList<>();

        ParsedMapping mapping;
        String refSeq;
        int start;
        int stop;
        int differences;
        boolean isRevStrand;
        byte direction;
        String readSeq;
        String cigar;
        int readClass;
        DiffAndGapResult diffGapResult;
        List<Pair<Integer, Integer>> coveredCommonIntervals = new ArrayList<>();
        List<Pair<Integer, Integer>> coveredBestMatchIntervals = new ArrayList<>();
        List<Pair<Integer, Integer>> coveredPerfectIntervals = new ArrayList<>();
        coveredCommonIntervals.add(new Pair<>(0, 0));
        coveredBestMatchIntervals.add(new Pair<>(0, 0));
        coveredPerfectIntervals.add(new Pair<>(0, 0));
        int lastIndex;
        Integer classification;
        ErrorLimit errorLimit = new ErrorLimit();

        try (SAMFileReader sam = new SAMFileReader(trackJob.getFile())) {
            SAMRecordIterator samItor = sam.iterator();

            SAMRecord record;
            while (samItor.hasNext()) {
                try {
                    ++lineno;

                    record = samItor.next();
                    if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {

                        cigar = record.getCigarString();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        readSeq = record.getReadString();
                        refSeq = refSeqWhole.substring(start - 1, stop);

                        classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                        if (classification != null) {
                            readClass = (int) classification;
                        } else {
                            readClass = Properties.COMPLETE_COVERAGE;
                        }

                        if (!ParserCommonMethods.checkRead(this, readSeq, refSeqWhole.length(), cigar, start, stop, fileName, lineno)) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }

                        //statistics claculations: count no reads and distinct sequences ////////////
                        if (!lastReadSeq.equals(readSeq)) { //same seq counted multiple times when mapping to diff. pos
                            noReads += readNamesSameSeq.size();
                            if (readsDifferentPos.size() == 1) {
                                ++noUniqueMappings;
                            }
                            readNamesSameSeq.clear();
                            readsDifferentPos.clear();
                        }
                        if (!readNamesSameSeq.contains(readName)) {
                            readNamesSameSeq.add(readName);
                        }
                        if (!readsDifferentPos.contains(start)) {
                            readsDifferentPos.add(start);
                        }
                        lastReadSeq = readSeq;

                        this.updateIntervals(coveredCommonIntervals, start, stop);
                        if (readClass == Properties.PERFECT_COVERAGE) {
                            this.updateIntervals(coveredPerfectIntervals, start, stop);
                            this.updateIntervals(coveredBestMatchIntervals, start, stop);
                            ++noPerfect;
                            ++noBestMatch;
                        } else if (readClass == Properties.BEST_MATCH_COVERAGE) {
                            this.updateIntervals(coveredBestMatchIntervals, start, stop);
                            ++noBestMatch;
                        }
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
                    } else {
                        //skip error messages, if too many occur to prevent bug in the output panel
                        if (errorLimit.allowOutput()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamPosTableCreator.class,
                                "Parser.Parsing.CorruptData", lineno, record.getReadName()));
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
                
            }
            if (errorLimit.getSkippedCount()>0) {
                     this.notifyObservers( "... "+(errorLimit.getSkippedCount())+" more errors occured");
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
        
        mappingInfos.put(1, -1); //no of mappings not existent, since we work on existing reads
        mappingInfos.put(2, noPerfect);
        mappingInfos.put(3, noBestMatch);
        mappingInfos.put(4, noUniqueMappings);
        mappingInfos.put(6, noReads);

        ParsedMappingContainer statsContainer = new ParsedMappingContainer();
        statsContainer.setMappingInfos(this.mappingInfos);
        track = new ParsedTrack(trackJob, statsContainer, coverageContainer);
        this.notifyObservers(track);
        this.mappingInfos = new HashMap<>();
        this.coverageContainer = new CoverageContainer();
        
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
    
}
