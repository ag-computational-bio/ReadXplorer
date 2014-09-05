/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.CoverageContainer;
import de.cebitec.readXplorer.parser.common.ParsedTrack;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.DiscreteCountingDistribution;
import de.cebitec.readXplorer.util.ErrorLimit;
import de.cebitec.readXplorer.util.MessageSenderI;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.StatsContainer;
import de.cebitec.readXplorer.util.classification.MappingClass;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

    
/**
 * Creates and stores the statistics for a track, which needs to be sorted by
 * position. The data to store is directly forwarded to the observer, which
 * should then further process it (store it in the db).
 *
 * @author -Rolf Hilker-
 */
public class SamBamStatsParser implements Observable, MessageSenderI {

    private List<Observer> observers;
    private CoverageContainer coverageContainer;
    private StatsContainer statsContainer;
    private DiscreteCountingDistribution readLengthDistribution;
    private ErrorLimit errorLimit;
    
    /**
     * Creates and stores the statistics for a track, which needs
     * to be sorted by position. The data to store is directly forwarded to the 
     * observer, which should then further process it (store it in the db).
     */
    public SamBamStatsParser() {
        this.observers = new ArrayList<>();
        this.coverageContainer = new CoverageContainer();
        this.errorLimit = new ErrorLimit(100);
        this.readLengthDistribution = new DiscreteCountingDistribution(400);
        readLengthDistribution.setType(Properties.READ_LENGTH_DISTRIBUTION);
    }
    
    /**
     * Creates the global track statistics for the given track job, which needs
     * to be sorted by position. The data to store is directly forwarded to the
     * observer, which should then further process it (store it in the db).
     * @param trackJob track job whose position table needs to be created
     * @param chromLengthMap mapping of chromosome name 
     * @return  
     */
    @SuppressWarnings("fallthrough")
    @NbBundle.Messages({"StatsParser.Finished=Finished creating track statistics for {0}. ", 
                        "StatsParser.Start=Start creating track statistics for {0}"})
    public ParsedTrack createTrackStats(TrackJob trackJob, Map<String, Integer> chromLengthMap) {
        
        long startTime = System.currentTimeMillis();
        long finish;
        String fileName = trackJob.getFile().getName();
        this.notifyObservers(Bundle.StatsParser_Start(fileName));

//        int noMappings = 0;
//        long starti = System.currentTimeMillis();
        int lineno = 0;
        
        String lastReadSeq = "";
        List<Integer> readsDifferentPos = new ArrayList<>();
        int seqCount = 0;
        int start;
        int stop;
        String readName;
        String readSeq;
        String cigar;
        MappingClass mappingClass;
        int mapCount;
        List<Pair<Integer, Integer>> coveredCommonIntervals = new ArrayList<>();
        List<Pair<Integer, Integer>> coveredBestMatchIntervals = new ArrayList<>();
        List<Pair<Integer, Integer>> coveredPerfectIntervals = new ArrayList<>();
        coveredCommonIntervals.add(new Pair<>(0, 0));
        coveredBestMatchIntervals.add(new Pair<>(0, 0));
        coveredPerfectIntervals.add(new Pair<>(0, 0));
        Byte classification;
        Integer mappingCount;
//        HashMap<String, Object> readNameSet = new HashMap<>();
        
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
                    if (!record.getReadUnmappedFlag() && chromLengthMap.containsKey(record.getReferenceName())) {

                        cigar = record.getCigarString();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        readSeq = record.getReadString();

                        if (!CommonsMappingParser.checkReadSam(this, readSeq, chromLengthMap.get(record.getReferenceName()), cigar, start, stop, fileName, lineno)) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }

                        //statistics calculations: count no mappings in classifications and distinct sequences ////////////
                        mappingCount = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                        if (mappingCount != null) {
                            mapCount = mappingCount;
                        } else {
                            mapCount = 0;
                        }

                        classification = Byte.valueOf(record.getAttribute(Properties.TAG_READ_CLASS).toString());
                        if (classification != null) {
                            mappingClass = MappingClass.getFeatureType(classification);
                            if (mapCount == 1) {
                                switch (mappingClass) { //fallthrough is necessary
                                    case SINGLE_PERFECT_MATCH : 
                                    case PERFECT_MATCH :
                                        statsContainer.increaseValue(StatsContainer.NO_UNIQ_PERF_MAPPINGS, mapCount);
                                    case SINGLE_BEST_MATCH : 
                                    case BEST_MATCH :
                                        statsContainer.increaseValue(StatsContainer.NO_UNIQ_BM_MAPPINGS, mapCount);
                                    default :
                                        statsContainer.increaseValue(StatsContainer.NO_UNIQ_MAPPINGS, mapCount);
                                }
                            }
                        } else {
                            mappingClass = MappingClass.COMMON_MATCH;
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
                        switch (mappingClass) { //fallthrough is necessary
                            case SINGLE_PERFECT_MATCH : 
                            case PERFECT_MATCH :
                                this.updateIntervals(coveredPerfectIntervals, start, stop);
                                statsContainer.increaseValue(StatsContainer.NO_PERFECT_MAPPINGS, mapCount);
                            case SINGLE_BEST_MATCH : 
                            case BEST_MATCH :
                                this.updateIntervals(coveredBestMatchIntervals, start, stop);
                                statsContainer.increaseValue(StatsContainer.NO_BESTMATCH_MAPPINGS, mapCount);
                            case SINGLE_COMMON_MATCH :
                            default :
                                statsContainer.increaseValue(StatsContainer.NO_COMMON_MAPPINGS, mapCount);
                        }
                        //saruman starts genome at 0 other algorithms like bwa start genome at 1
                        
//                        //can be used for debugging performance
//                        if (++noMappings % 10000 == 0) {
//                            long finish = System.currentTimeMillis();
//                            this.notifyObservers(Benchmark.calculateDuration(starti, finish, noMappings + " mappings processed. "));
//                            starti = System.currentTimeMillis();
//                        }
                    }
                } catch (Exception e) {
                    //skip error messages, if too many occur to prevent bug in the output panel
                    if (e.getMessage() == null || !e.getMessage().contains("MAPQ should be 0")) {
                        //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored  
                        this.sendMsgIfAllowed(NbBundle.getMessage(SamBamStatsParser.class,
                                "Parser.Parsing.CorruptData", lineno, e.toString()));
                        Exceptions.printStackTrace(e);
                    }
                }
                if ((lineno % 500000) == 0)  {//output process info only on every XX line
                    finish = System.currentTimeMillis();
                    this.notifyObservers(Benchmark.calculateDuration(startTime, finish, lineno + " mappings processed in "));
                }
            }
            if (errorLimit.getSkippedCount() > 0) {
                this.notifyObservers( "... " + (errorLimit.getSkippedCount()) + " more errors occurred");
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

        ParsedTrack track = new ParsedTrack(trackJob, coverageContainer);
        statsContainer.setReadLengthDistribution(readLengthDistribution);
        track.setStatsContainer(statsContainer);
        this.coverageContainer = new CoverageContainer();
        
        finish = System.currentTimeMillis();
        String msg = Bundle.StatsParser_Finished(fileName);
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

    @Override
    public void sendMsgIfAllowed(String msg) {
        if (this.errorLimit.allowOutput()) {
            this.notifyObservers(msg);
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
            coveredBases += interval.getSecond() - interval.getFirst();
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
     * Sets the statistics container for handling statistics for the extended track.
     * @param statsContainer the stats container
     */
    public void setStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }

    /**
     * @return The statistics parser for handling statistics for the extended track.
     */
    public StatsContainer getStatsContainer() {
        return statsContainer;
    }
}
