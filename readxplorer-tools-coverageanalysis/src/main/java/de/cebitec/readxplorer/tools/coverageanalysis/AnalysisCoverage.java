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

package de.cebitec.readxplorer.tools.coverageanalysis;


import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.api.objects.AnalysisI;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageAndDiffResult;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageManager;
import de.cebitec.readxplorer.utils.Observer;
import java.util.ArrayList;
import java.util.List;


/**
 * Carries out the logic behind the covered or uncovered interval analysis.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class AnalysisCoverage implements Observer,
                                         AnalysisI<CoverageIntervalContainer> {

    private CoverageIntervalContainer intervalContainer;
    private final List<CoverageInterval> intervalsSumOrFwd;
    private final List<CoverageInterval> intervalsRev;
    private final TrackConnector connector;
    private final ParameterSetCoverageAnalysis parameters;
    private int[] coverageArraySumOrFwd;
    private int[] coverageArrayRev;
    private final List<CoverageInterval> tempIntervals;


    /**
     * Carries out the logic behind the covered or uncovered interval analysis.
     * <p>
     * @param connector  the track connector determining the data set to analyze
     * @param parameters the parameter set to use for the analysis
     */
    public AnalysisCoverage( TrackConnector connector, ParameterSetCoverageAnalysis parameters ) {
        this.connector = connector;
        this.parameters = parameters;
        this.tempIntervals = new ArrayList<>();
        this.intervalsSumOrFwd = new ArrayList<>();
        this.intervalsRev = new ArrayList<>();
    }


    /**
     * Only processes CoverageAndDiffResultPersistent objects.
     * <p>
     * @param data the CoverageAndDiffResultPersistent to process
     */
    @Override
    public void update( Object data ) {
        if( data instanceof CoverageAndDiffResult ) {
            CoverageAndDiffResult coverageResult = ((CoverageAndDiffResult) data);
            this.processResult( coverageResult );
        }
    }


    /**
     * @return The container with all detected intervals of this analysis.
     */
    @Override
    public CoverageIntervalContainer getResults() {
        return this.intervalContainer;
    }


    /**
     * Actually processes a new result.
     * <p>
     * @param coverageResult the result to process
     */
    private void processResult( CoverageAndDiffResult coverageResult ) {

        /**
         * Algorithm: 1. check tempIntervalList for overlap with current
         * coverage 2. store the one or both (fwd, rev) overlapping intervals
         * for start and for stop of the current coverage in tempVariables 3.
         * call iteration method with novel interval object or overlapping
         * object (only one method for iteration is needed per strand then) 4.
         * iterate over coverage array (either fwd or reverse) 5. if coverage
         * drops below (or is higher than) threshold - add interval 5. if
         * opposite case: just continue checking the current coverage
         */

        if( intervalContainer == null ) { //initialize result, if it was not used yet
            intervalContainer = new CoverageIntervalContainer();
        }

        CoverageManager coverage = coverageResult.getCovManager();
        coverageArraySumOrFwd = coverage.getTotalCoverage( parameters.getReadClassParams().getExcludedClasses() ).getFwdCov();
        coverageArrayRev = coverage.getTotalCoverage( parameters.getReadClassParams().getExcludedClasses() ).getRevCov();

        if( this.parameters.isSumCoverageOfBothStrands() ) {
            coverageArraySumOrFwd = this.sumValues( coverageArraySumOrFwd, coverageArrayRev );
        }

        /* check temp intervals at first, which might be elongated by the new
         * result */
        int chromId = coverageResult.getRequest().getChromId();
        Strand strand = this.parameters.isSumCoverageOfBothStrands() ? Strand.Both : Strand.Forward;

        CoverageInterval overlapIntervalSumOrFwdStart = new CoverageInterval( connector.getTrackID(), chromId, strand );
        CoverageInterval overlapIntervalRevStart = new CoverageInterval( connector.getTrackID(), chromId, Strand.Reverse );
        CoverageInterval overlapIntervalSumOrFwdEnd = new CoverageInterval( connector.getTrackID(), chromId, strand );
        CoverageInterval overlapIntervalRevEnd = new CoverageInterval( connector.getTrackID(), chromId, Strand.Reverse );
        CoverageInterval currentTempInterval;
        int startPos = coverageResult.getRequest().getFrom();
        for( CoverageInterval tempInterval : tempIntervals ) {
            currentTempInterval = tempInterval;
            if( startPos - 1 == currentTempInterval.getStop() && currentTempInterval.getChromId() == chromId ) {
                if( currentTempInterval.getStrandString().equals( Strand.Both.toString() ) ||
                    currentTempInterval.getStrandString().equals( Strand.Forward.toString() ) ) {

                    overlapIntervalSumOrFwdStart = tempInterval;

                } else {
                    overlapIntervalRevStart = tempInterval;
                }
            }
            if( coverageResult.getRequest().getTo() + 1 == currentTempInterval.getStart() ) {
                if( currentTempInterval.getStrandString().equals( Strand.Both.toString() ) ||
                    currentTempInterval.getStrandString().equals( Strand.Forward.toString() ) ) {

                    overlapIntervalSumOrFwdEnd = tempInterval;

                } else {
                    overlapIntervalRevEnd = tempInterval;
                }
            }
        }

        this.calculateIntervals( chromId, overlapIntervalSumOrFwdStart, overlapIntervalSumOrFwdEnd, coverageArraySumOrFwd,
                                 strand, coverageResult.getRequest().getFrom(), intervalsSumOrFwd );
        if( !this.parameters.isSumCoverageOfBothStrands() ) {
            this.calculateIntervals( chromId, overlapIntervalRevStart, overlapIntervalRevEnd, coverageArrayRev,
                                     Strand.Reverse, coverageResult.getRequest().getFrom(), intervalsRev );
        }

        intervalContainer.setIntervalsSumOrFwd( intervalsSumOrFwd );
        intervalContainer.setIntervalsRev( intervalsRev );
    }


    /**
     * Calculates the intervals of the coverage analysis = the actual result.
     * <p>
     * @param currentInterval     a possible overlap interval or an empty
     *                            interval. If it is a possible overlap
     *                            interval, this means, it is the only interval
     *                            in the whole analysis, which can be extended
     *                            by the current coverageArray
     * @param coverageArray       the coverage array which shall be analyzed
     * @param strand              the strand from which the coverage originates
     * @param refIntervalStart    start position of the coverageArray in
     *                            reference sequence coordinates
     * @param intervalListToAddTo the interval list, to which detected intervals
     *                            shall be added
     */
    private void calculateIntervals( int chromId, CoverageInterval currentInterval, CoverageInterval tempEndInterval, int[] coverageArray, Strand strand,
                                     int refIntervalStart, List<CoverageInterval> intervalListToAddTo ) {

        int summedCoverage = 0;
        boolean isInInterval = false;
        int startPos = currentInterval.getStart();
        boolean addToTempIntervals = false;
        boolean isCoverageOk = coverageArray.length > 0 &&
                               (coverageArray[0] >= parameters.getMinCoverageCount() && parameters.isDetectCoveredIntervals() ||
                                coverageArray[0] < parameters.getMinCoverageCount() && !parameters.isDetectCoveredIntervals());

        // if possible overlap interval does not overlap, because start coverage of current coverageArray is too low (high)
        if( startPos > 0 && !isCoverageOk ) {
            intervalListToAddTo.add( currentInterval );
            tempIntervals.remove( currentInterval );
            currentInterval = new CoverageInterval( connector.getTrackID(), chromId, strand );
            startPos = currentInterval.getStart();
        } else if( startPos < 0 && isCoverageOk ) {
            addToTempIntervals = true; //an interval possibly overlapping the left boundary of the interval, needs to be stored in temp intervals
        }

        for( int i = 0; i < coverageArray.length; i++ ) {

            isCoverageOk = coverageArray[i] >= parameters.getMinCoverageCount() && parameters.isDetectCoveredIntervals() ||
                           coverageArray[i] < parameters.getMinCoverageCount() && !parameters.isDetectCoveredIntervals();

            if( isCoverageOk ) {
                isInInterval = true;
                if( startPos <= 0 ) {
                    startPos = refIntervalStart + i;
                }

                summedCoverage += coverageArray[i];

                //when reaching the end of the coverage array: add a possible overlap interval
                if( (i + 1) >= coverageArray.length ) {
                    //add possible overlap interval to tempList or update the given temp overlap interval
                    if( tempEndInterval.getStart() > 0 ) {
                        tempEndInterval.setStart( startPos );
                        if( currentInterval.getStart() > 0 ) {
                            this.tempIntervals.remove( currentInterval );
                        }
                    } else {
                        this.storeInterval( currentInterval, startPos, refIntervalStart + i, summedCoverage, tempIntervals );
                    }
                    //no reinitialization of currentInterval needed, since we are at the end of the data package
                }
            } else if( isInInterval ) { //check if this is the first position which is below (over) given threshold
                if( addToTempIntervals ) { //i - 1, because current position does not satisfy the preliminaries anymore
                    this.storeInterval( currentInterval, startPos, refIntervalStart + i - 1, summedCoverage, tempIntervals );
                    addToTempIntervals = false;
                } else {
                    this.storeInterval( currentInterval, startPos, refIntervalStart + i - 1, summedCoverage, intervalListToAddTo );
                }
                //reinitialize currentInterval for next interval
                currentInterval = new CoverageInterval( connector.getTrackID(), chromId, strand );
                startPos = currentInterval.getStart();
                isInInterval = false;
                summedCoverage = 0;
                //else do nothing, just continue
            }
        }
    }


    /**
     * Sums the values of both given arrays for the same index.
     * <p>
     * @param intArray1 array one
     * @param intArray2 array two
     * <p>
     * @return new array containing at each index i the sum of the values from
     *         intArray1 and intArray2
     */
    private int[] sumValues( int[] intArray1, int[] intArray2 ) {
        int[] sumArray = new int[intArray1.length];
        for( int i = 0; i < intArray1.length; i++ ) {
            sumArray[i] = intArray1[i] + intArray2[i];
        }
        return sumArray;
    }


    /**
     * Method for updating the currentInterval and adding it to the given
     * interval list.
     * <p>
     * @param currentInterval the interval which shall be updated
     * @param startPos        the new start pos of the interval in reference
     *                        coordinates
     * @param stopPos         the new stop pos of the interval in reference
     *                        coordinates
     * @param summedCoverage  the summedCoverage values to add to the
     *                        currentInterval
     * @param intervalList    the interval list, to which the currentInterval
     *                        shall be added
     */
    private void storeInterval( CoverageInterval currentInterval, int startPos, int stopPos,
                                int summedCoverage, List<CoverageInterval> intervalList ) {

        tempIntervals.remove( currentInterval ); // if it was contained in the temp intervals, we have to remove it now

        currentInterval.setStart( startPos );
        currentInterval.setStop( stopPos );
        int meanCov = (currentInterval.getMeanCoverage() + (summedCoverage / currentInterval.getLength()));
        if( currentInterval.getMeanCoverage() > 0 ) {
            meanCov /= 2;
        }
        currentInterval.setMeanCoverage( meanCov );
        intervalList.add( currentInterval );
    }


    /**
     * Call this method, when the analysis is finished. It adds the last
     * temporary intervals, which might be overlapping with results from other
     * data packages to the final result list. This is important, otherwise
     * these intervals don't show up in the results.
     */
    public void finishAnalysis() {
        //add all remaining temp intervals to results, but only, if this is the last data package
        for( CoverageInterval interval : tempIntervals ) {
            if( interval.getStrandString().equals( Strand.Reverse.toString() ) ) {
                intervalsRev.add( interval );
            } else {
                intervalsSumOrFwd.add( interval );
            }
        }
        tempIntervals.clear();
    }


}
