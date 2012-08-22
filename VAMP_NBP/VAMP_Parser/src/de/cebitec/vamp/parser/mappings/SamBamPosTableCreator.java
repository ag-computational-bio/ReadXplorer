package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
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
    
    /**
     * Creates and stores the position table for a sam or bam file, which needs
     * to be sorted by position. The data to store is directly forwarded to the 
     * observer, which should then further process it (store it in the db).
     */
    public SamBamPosTableCreator() {
        this.observers = new ArrayList<>();
        this.coverageContainer = new CoverageContainer();
    }
    
    /**
     * Creates the position table for the given track job, which needs to be
     * sorted by position. The data to store is directly forwarded to the
     * observer, which should then further process it (store it in the db).
     * @param trackJob track job whose position table needs to be created
     * @param refSeqWhole reference genome sequence 
     */
    public void createPosTable(TrackJob trackJob, String refSeqWhole) {
        
        long startTime = System.currentTimeMillis();
        String fileName = trackJob.getFile().getName();
        this.notifyObservers(NbBundle.getMessage(SamBamPosTableCreator.class, "PosTableCreator.Start", fileName));
        
        List<ParsedMapping> batchOverlappingMappings = new ArrayList<>();
        ParsedTrack track;

        final int batchSize = 300000;
        int nextBatch = batchSize;
        int lineno = 0;

        ParsedMapping mapping;
        String refSeq;
        int start;
        int stop;
        int differences;
        boolean isRevStrand;
        byte direction;
        String readSeq;
        String cigar;
        DiffAndGapResult diffGapResult;
        List<Pair<Integer, Integer>> coveredIntervals = new ArrayList<>();
        coveredIntervals.add(new Pair<>(0, 0));
        int lastIndex;
        Integer classification;

        try (SAMFileReader sam = new SAMFileReader(trackJob.getFile())) {
            SAMRecordIterator samItor = sam.iterator();

            SAMRecord record;
            while (samItor.hasNext()) {
                try {
                    ++lineno;

                    record = samItor.next();
                    if (!record.getReadUnmappedFlag()) {
                        classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                        if (    classification == null
                             || classification == (int) Properties.PERFECT_COVERAGE 
                             || classification == (int) Properties.BEST_MATCH_COVERAGE) {
                            // if the classification is not stored, we have to use all available mappings for the pos table
                            // (this practice will cause detecting unwanted SNPs, but the alternative is 0 SNPs)
                            
                            cigar = record.getCigarString();
                            start = record.getAlignmentStart();
                            stop = record.getAlignmentEnd();
                            readSeq = record.getReadString();
                            refSeq = refSeqWhole.substring(start - 1, stop);

                            if (!ParserCommonMethods.checkRead(this, readSeq, refSeqWhole.length(), cigar, start, stop, fileName, lineno)) {
                                continue; //continue, and ignore read, if it contains inconsistent information
                            }

                            //store coverage statistic
                            lastIndex = coveredIntervals.size() - 1;
                            if (coveredIntervals.get(lastIndex).getSecond() < start) { //add new pair
                                coveredIntervals.add(new Pair<>(start, stop));
                            } else { //increase length of first pair (start remains, stop is enlarged)
                                coveredIntervals.get(lastIndex).setSecond(stop);
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

                            //store data for position table
                            direction = isRevStrand ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                            mapping = new ParsedMapping(start, stop, direction, diffGapResult.getDiffs(), diffGapResult.getGaps(), differences);
                            mapping.setIsBestMapping(true);
                            this.coverageContainer.addMapping(mapping); //this is not the case for ordinary database import!
                            this.coverageContainer.savePositions(mapping);

                            //store position table for each 300.000 positions in db
                            //at first store mappings which overlap next batch position separately
                            if (stop > nextBatch) {
                                batchOverlappingMappings.add(mapping);
                            }
                            if (start > nextBatch) { //e.g. 300.001
                                track = new ParsedTrack(trackJob, null, coverageContainer);
                                this.notifyObservers(track);
                                this.coverageContainer.clearCoverageContainer();
                                this.refillCoverageContainer(batchOverlappingMappings, nextBatch);
                                nextBatch += batchSize;
                                batchOverlappingMappings.clear();
                            }

                            //saruman starts genome at 0 other algorithms like bwa start genome at 1
                        }
                    } else {
                        this.notifyObservers(NbBundle.getMessage(SamBamPosTableCreator.class,
                                "Parser.Parsing.CorruptData", lineno, record.getReadName()));
                    }
                } catch (SAMFormatException e) {
                    this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                            "Parser.Parsing.CorruptData", lineno, e.toString()));
                }
            }
            samItor.close();

        } catch (RuntimeEOFException e) {
            this.notifyObservers("Last read in file is incomplete, ignoring it!");
        } catch (Exception e) {
            Exceptions.printStackTrace(e); //TODO: correct error handling or remove
        }
        
        coverageContainer.setCoveredCommonMatchPositions(this.getCoveredBases(coveredIntervals));

        track = new ParsedTrack(trackJob, null, coverageContainer);
        this.notifyObservers(track);
        this.coverageContainer.clearCoverageContainer();

        
        long finish = System.currentTimeMillis();
        String msg = NbBundle.getMessage(SamBamDirectParser.class, "PosTableCreator.Finished", fileName);
        this.notifyObservers(Benchmark.calculateDuration(startTime, finish, msg));
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
        for(Pair interval : coveredIntervals) {
            coveredBases += (int) interval.getSecond() - (int) interval.getFirst();
        }
        return coveredBases;
    }
    
}
