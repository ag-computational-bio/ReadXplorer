package de.cebitec.readXplorer.coverageAnalysis;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Carries out the logic behind the covered or uncovered interval analysis.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class AnalysisCoverage implements Observer, AnalysisI<CoverageIntervalContainer> {

    private CoverageIntervalContainer intervalContainer;
    private List<CoverageInterval> intervalsSumOrFwd;
    private List<CoverageInterval> intervalsRev;
    private final TrackConnector connector;
    private final ParameterSetCoverageAnalysis parameters;
    private int[] coverageArraySumOrFwd;
    private int[] coverageArrayRev;
    private List<CoverageInterval> tempIntervals;

    /**
     * Carries out the logic behind the covered or uncovered interval analysis.
     * @param connector the track connector determining the data set to analyze
     * @param parameters the parameter set to use for the analysis
     */
    public AnalysisCoverage(TrackConnector connector, ParameterSetCoverageAnalysis parameters) {
        this.connector = connector;
        this.parameters = parameters;
        this.tempIntervals = new ArrayList<>();
        this.intervalsSumOrFwd = new ArrayList<>();
        this.intervalsRev = new ArrayList<>();
    }

    /**
     * Only processes CoverageAndDiffResultPersistant objects.
     * @param data the CoverageAndDiffResultPersistant to process
     */
    @Override
    public void update(Object data) {
        if (data instanceof CoverageAndDiffResultPersistant) {
            CoverageAndDiffResultPersistant coverageResult = ((CoverageAndDiffResultPersistant) data);
            this.processResult(coverageResult);
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
     * @param coverageResult the result to process
     */
    private void processResult(CoverageAndDiffResultPersistant coverageResult) {
        
        /**
         * Algorithm:
         * 1. check tempIntervalList for overlap with current coverage 
         * 2. store the one or both (fwd, rev) overlapping intervals for start and for stop of the current coverage in tempVariables 
         * 3. call iteration method with novel interval object or overlapping
         * object (only one method for iteration is needed per strand then)
         * 4. iterate over coverage array (either fwd or reverse) 
         * 5. if coverage drops below (or is higher than) threshold - add interval 
         * 5. if opposite case: just continue checking the current coverage
         */
        
        if (intervalContainer == null) { //initialize result, if it was not used yet
            intervalContainer = new CoverageIntervalContainer();
        }

        PersistantCoverage coverage = coverageResult.getCoverage();

        //since read classes are inclusive, we simply check which array to use, starting with the larges read class
        if (this.parameters.getReadClassParams().isCommonMatchUsed()) {
            coverageArraySumOrFwd = coverage.getCommonFwdMult();
            coverageArrayRev = coverage.getCommonRevMult();
        } else if (this.parameters.getReadClassParams().isBestMatchUsed()) {
            coverageArraySumOrFwd = coverage.getBestMatchFwdMult();
            coverageArrayRev = coverage.getBestMatchRevMult();
        } else if (this.parameters.getReadClassParams().isPerfectMatchUsed()) {
            coverageArraySumOrFwd = coverage.getPerfectFwdMult();
            coverageArrayRev = coverage.getPerfectRevMult();
        }
        
        if (this.parameters.isSumCoverageOfBothStrands()) {
            coverageArraySumOrFwd = this.sumValues(coverageArraySumOrFwd, coverageArrayRev);
        }
        
        /* check temp intervals at first, which might be elongated by the new result */
        int chromId = coverageResult.getRequest().getChromId();
        byte strandFwdOrBoth = this.parameters.isSumCoverageOfBothStrands() ? SequenceUtils.STRAND_BOTH : SequenceUtils.STRAND_FWD;
        
        CoverageInterval overlapIntervalSumOrFwdStart = new CoverageInterval(connector.getTrackID(), chromId, strandFwdOrBoth);
        CoverageInterval overlapIntervalRevStart = new CoverageInterval(connector.getTrackID(), chromId, SequenceUtils.STRAND_REV);
        CoverageInterval overlapIntervalSumOrFwdEnd = new CoverageInterval(connector.getTrackID(), chromId, strandFwdOrBoth);
        CoverageInterval overlapIntervalRevEnd = new CoverageInterval(connector.getTrackID(), chromId, SequenceUtils.STRAND_REV);
        CoverageInterval currentTempInterval;
        int startPos = coverageResult.getRequest().getFrom();
        for (int i = 0; i < tempIntervals.size(); i++) {
            currentTempInterval = tempIntervals.get(i);
            if (startPos - 1 == currentTempInterval.getStop() && currentTempInterval.getChromId() == chromId) {
                if (    currentTempInterval.getStrandString().equals(SequenceUtils.STRAND_BOTH_STRING)
                     || currentTempInterval.getStrandString().equals(SequenceUtils.STRAND_FWD_STRING)) {

                    overlapIntervalSumOrFwdStart = tempIntervals.get(i);
                } else {
                    overlapIntervalRevStart = tempIntervals.get(i);
                }
            }
            if (coverageResult.getRequest().getTo() + 1 == currentTempInterval.getStart()) {
                if (currentTempInterval.getStrandString().equals(SequenceUtils.STRAND_BOTH_STRING)
                        || currentTempInterval.getStrandString().equals(SequenceUtils.STRAND_FWD_STRING)) {

                    overlapIntervalSumOrFwdEnd = tempIntervals.get(i);
                } else {
                    overlapIntervalRevEnd = tempIntervals.get(i);
                }
            }
        }
        
        this.calculateIntervals(chromId, overlapIntervalSumOrFwdStart, overlapIntervalSumOrFwdEnd, coverageArraySumOrFwd, 
                strandFwdOrBoth, coverageResult.getRequest().getFrom(), intervalsSumOrFwd);
        if (!this.parameters.isSumCoverageOfBothStrands()) {
            this.calculateIntervals(chromId, overlapIntervalRevStart, overlapIntervalRevEnd, coverageArrayRev, 
                    SequenceUtils.STRAND_REV, coverageResult.getRequest().getFrom(), intervalsRev);
        }
        
        intervalContainer.setIntervalsSumOrFwd(intervalsSumOrFwd);
        intervalContainer.setIntervalsRev(intervalsRev);
    }
    
    /**
     * Calculates the intervals of the coverage analysis = the actual result.
     * @param currentInterval a possible overlap interval or an empty interval. 
     * If it is a possible overlap interval, this means, it is the only interval
     * in the whole analysis, which can be extended by the current coverageArray
     * @param coverageArray the coverage array which shall be analyzed
     * @param strand the strand from which the coverage originates
     * @param refIntervalStart start position of the coverageArray in reference
     * sequence coordinates
     * @param intervalListToAddTo the interval list, to which detected intervals 
     * shall be added
     */
    private void calculateIntervals(int chromId, CoverageInterval currentInterval, CoverageInterval tempEndInterval, int[] coverageArray, byte strand, 
            int refIntervalStart, List<CoverageInterval> intervalListToAddTo) {
        
        int summedCoverage = 0;
        boolean isInInterval = false;
        int startPos = currentInterval.getStart();
        boolean addToTempIntervals = false;
        boolean isCoverageOk = coverageArray.length > 0 &&
                              (coverageArray[0] >= parameters.getMinCoverageCount() && parameters.isDetectCoveredIntervals() ||
                               coverageArray[0] < parameters.getMinCoverageCount() && !parameters.isDetectCoveredIntervals());
        
        // if possible overlap interval does not overlap, because start coverage of current coverageArray is too low (high)
        if (startPos > 0 && !isCoverageOk) {
            intervalListToAddTo.add(currentInterval);
            tempIntervals.remove(currentInterval);
            currentInterval = new CoverageInterval(connector.getTrackID(), chromId, strand);
            startPos = currentInterval.getStart();
        } else if (startPos < 0 && isCoverageOk) {
            addToTempIntervals = true; //an interval possibly overlapping the left boundary of the interval, needs to be stored in temp intervals
        }
        
        for (int i = 0; i < coverageArray.length; i++) {
            
            isCoverageOk = coverageArray[i] >= parameters.getMinCoverageCount() && parameters.isDetectCoveredIntervals() ||
                           coverageArray[i] < parameters.getMinCoverageCount() && !parameters.isDetectCoveredIntervals();
            
            if (isCoverageOk) {
                isInInterval = true;
                if (startPos <= 0) {
                    startPos = refIntervalStart + i;
                }

                summedCoverage += coverageArray[i];
                
                //when reaching the end of the coverage array: add a possible overlap interval
                if ((i + 1) >= coverageArray.length) {
                    //add possible overlap interval to tempList or update the given temp overlap interval
                    if (tempEndInterval.getStart() > 0) {
                        tempEndInterval.setStart(startPos);
                        if (currentInterval.getStart() > 0) {
                            this.tempIntervals.remove(currentInterval);
                        }
                    } else {
                        this.storeInterval(currentInterval, startPos, refIntervalStart + i, summedCoverage, tempIntervals);
                    }
                    //no reinitialization of currentInterval needed, since we are at the end of the data package
                }
            } else {
                if (isInInterval) { //check if this is the first position which is below (over) given threshold
                    if (addToTempIntervals) { //i - 1, because current position does not satisfy the preliminaries anymore
                        this.storeInterval(currentInterval, startPos, refIntervalStart + i - 1, summedCoverage, tempIntervals);
                        addToTempIntervals = false;
                    } else {
                        this.storeInterval(currentInterval, startPos, refIntervalStart + i - 1, summedCoverage, intervalListToAddTo);
                    }
                    //reinitialize currentInterval for next interval
                    currentInterval = new CoverageInterval(connector.getTrackID(), chromId, strand);
                    startPos = currentInterval.getStart();
                    isInInterval = false;
                    summedCoverage = 0;
                }
                //else do nothing, just continue
            }
        }
    }

    /**
     * Sums the values of both given arrays for the same index.
     * @param intArray1 array one
     * @param intArray2 array two
     * @return new array containing at each index i the sum of the values from
     * intArray1 and intArray2
     */
    private int[] sumValues(int[] intArray1, int[] intArray2) {
        int[] sumArray = new int[intArray1.length];
        for (int i = 0; i < intArray1.length; ++i) {
            sumArray[i] = intArray1[i] + intArray2[i];
        }
        return sumArray;
    }

    /**
     * Method for updating the currentInterval and adding it to the given 
     * interval list.
     * @param currentInterval the interval which shall be updated
     * @param startPos the new start pos of the interval in reference coordinates
     * @param stopPos the new stop pos of the interval in reference coordinates
     * @param summedCoverage the summedCoverage values to add to the 
     * currentInterval
     * @param intervalList the interval list, to which the currentInterval shall
     * be added
     */
    private void storeInterval(CoverageInterval currentInterval, int startPos, int stopPos, 
            int summedCoverage, List<CoverageInterval> intervalList) {
               
        tempIntervals.remove(currentInterval); // if it was contained in the temp intervals, we have to remove it now
        
        currentInterval.setStart(startPos);
        currentInterval.setStop(stopPos);
        int meanCov = (currentInterval.getMeanCoverage() + (summedCoverage / currentInterval.getLength()));
        if (currentInterval.getMeanCoverage() > 0) {
            meanCov /= 2;
        }
        currentInterval.setMeanCoverage(meanCov);
        intervalList.add(currentInterval);
    }

    /**
     * Call this method, when the analysis is finished. It adds the last
     * temporary intervals, which might be overlapping with results from other
     * data packages to the final result list. This is important, otherwise
     * these intervals don't show up in the results.
     */
    public void finishAnalysis() {
        //add all remaining temp intervals to results, but only, if this is the last data package
        for (CoverageInterval interval : tempIntervals) {
            if (interval.getStrandString().equals(SequenceUtils.STRAND_REV_STRING)) {
                intervalsRev.add(interval);
            } else {
                intervalsSumOrFwd.add(interval);
            }
        }
        tempIntervals.clear();
    }
    
    
//        part from process result
//        if (this.parameters.isSumCoverageOfBothStrands()) {
//            
//            if (this.isSequenceOverlapping) { //react, if an overlap with an interval was detected
//                generateDataForResultObject(isCoverageForIntervallSumOrFwdGenerated, coverageResult, coverageArraySumOrFwd, coverageArrayRev, tempIndex, tempIntervals, SequenceUtils.STRAND_BOTH, intervalsSumOrFwd);
//            } else {
//                generateDataForResultObject(isCoverageForIntervallSumOrFwdGenerated, coverageResult, coverageArraySumOrFwd, coverageArrayRev, SequenceUtils.STRAND_BOTH, intervalsSumOrFwd);
//            }
//            result.setIntervalsSumOrFwd(intervalsSumOrFwd);
//
//        } else {
//            //Start with Fwd-Strand
//            if (this.isSequenceOverlapping) {
//                generateDataForResultObject(isCoverageForIntervallSumOrFwdGenerated, coverageResult, coverageArraySumOrFwd, tempIndex, tempIntervalsFwd, SequenceUtils.STRAND_FWD, intervalsSumOrFwd);
//            } else {
//                generateDataForResultObject(isCoverageForIntervallSumOrFwdGenerated, coverageResult, coverageArraySumOrFwd, SequenceUtils.STRAND_FWD, intervalsSumOrFwd);
//            }
//
//            //Continue with Rev-Strand
//            /* check temp intervals at first, which might be elongated by the
//             * new result */
//            startPos = coverageResult.getLowerBound();
//            for (int i = 0; i < tempIntervalsRev.size(); i++) {
//                lastStopPos = tempIntervalsRev.get(i).getStart();
//                if (startPos - 1 == lastStopPos) {
//                    tempIndex = i;
//                    isSequenceOverlapping = true;
//                } else {
//                    isSequenceOverlapping = false;
//                }
//            }
//            if (isSequenceOverlapping) {
//                generateDataForResultObject(isCoverageForRevIntervallGenerated, coverageResult, coverageArrayRev, tempIndex, tempIntervalsRev, SequenceUtils.STRAND_REV, intervalsRev);
//            } else {
//                generateDataForResultObject(isCoverageForRevIntervallGenerated, coverageResult, coverageArrayRev, SequenceUtils.STRAND_REV, intervalsRev);
//            }
//            
//        }
    

//    /**
//     * Method for sum coverage count of both strands + no overlapping of
//     * Sequence with package
//     * @param coverageResult
//     * @param coverageArrayFwd
//     * @param coverageArrayRev
//     */
//    private void generateDataForResultObject(boolean isCoverageForIntervalSumOrFwdGenerated, CoverageAndDiffResultPersistant coverageResult, int[] coverageArrayFwd, int[] coverageArrayRev, byte strand, ArrayList<CoverageInterval> coverageClassAny) {
//        for (int i = 0; i < coverageArrayFwd.length; i++) {
//            if (coverageArrayFwd[i] + coverageArrayRev[i] >= this.parameters.getMinCoverageCount()) {
//                //START coverage hier einmal setzten und wenn sie gesetzt ist nicht noch einmal setzten.
//                if (isStartPosSet == false) {
//                    startPos = coverageResult.getLowerBound() + i;
//                    isStartPosSet = true;
//                }
//                //Alle CoverageInterval Werte aufsummieren
//                sumOfCoverage = sumOfCoverage + coverageArrayFwd[i] + coverageArrayRev[i];
//                // Wenn dass array zu ende ist, aber die Seuqenz noch weitergeht.
//                // müsste hier zudem nich sumFwdCoverage reseten
//                if ((i + 1) < coverageArrayFwd.length) { // Müsste richtig sein, da ja bei richtigen boolschen Ausdruck die Schleife ausgeführt wird
//                } else {
//                    addDataForResult_noOverlapSequence(coverageResult, tempIntervals, i, strand);
//                }
//                // Setzte Zähler wieder auf Null, da hier ja nun die richtige CoverageInterval herrscht
//                countInsufficientCoverage = 0;
//                isCoverageForIntervalSumOrFwdGenerated = true;
//            } else {
//                //abfrage was passiert wenn ich hier über z.B. 10 Positionen gehe und es jeweils nu reine coverage gab die zu klein ist!!
//                // Dies kann dadurch gemacht werden, dass man hier unten einen zähler mitlaufen lässt, der zählt wie oft man in
//                // die else anweisung läuft. wenn man einmal drin war, wird coverageClassSumOrFwd nicht mehr gefüllt
//                // Zähler spring auf null, wenn er wieder in die if-Anweisung oben kommt, also wenn die CoverageInterval wieder
//                // über minCoverage kommt.
//                // Zusätzlich abfragen, ob die CoverageInterval über 60
//                countInsufficientCoverage++;
//                // Setzte die Variabel startCoverageIsSet auf false, da sie nun neu gesetzt werden muss
//                isStartPosSet = false;
//                // fülle das Objekt CoverageInterval nur, wenn man eine Lücke hat!
//                if (countInsufficientCoverage == 1 && isCoverageForIntervalSumOrFwdGenerated) {
//                    addDataForResult_noOverlapSequence(coverageResult, coverageClassAny, i, strand);
//                    sumOfCoverage = 0;
//                    isCoverageForIntervalSumOrFwdGenerated = false;
//                }
//
//            }
//        }
//    }
//
//    /**
//     * Method for sum coverage count of both strands + overlapping Sequence with
//     * ppackage
//     *
//     * @param coverageResult
//     * @param coverageArrayFwd
//     * @param coverageArrayRev
//     * @param tempCoverage
//     */
//    private void generateDataForResultObject(boolean isCoverageForIntervalSumOrFwdGenerated, CoverageAndDiffResultPersistant coverageResult,
//            int[] coverageArrayFwd, int[] coverageArrayRev, int j, ArrayList<CoverageInterval> tempCoverageAny, byte strand, ArrayList<CoverageInterval> coverageClassAny) {
//        int tempIndex = 0;
//
//        // Alte Werte aus tempCoverage füllen in die Variabeln hier
//        int meanSumFwdCoverage = tempIntervals.get(j).getMeanCoverage();
//        startPos = tempIntervals.get(j).getStart();
//        isStartPosSet = true;
//        isCoverageForIntervalSumOrFwdGenerated = true;
//        //Es wurde noch ein Overlap zusammengefügt, weshalb die Variabel hier nun auf false gesetzt wird
//        this.mergeOverlapOfSequencesIsDone = false;
//
//        for (int i = 0; i < coverageArrayFwd.length; i++) {
//
//            if (coverageArrayFwd[i] + coverageArrayRev[i] >= this.parameters.getMinCoverageCount()) {
//
//                if (isStartPosSet == false) {
//                    startPos = coverageResult.getLowerBound() + i;
//                    isStartPosSet = true;
//                }
//                //Alle CoverageInterval Werte aufsummieren
//                sumOfCoverage = sumOfCoverage + coverageArrayFwd[i] + coverageArrayRev[i];
//                // Wenn dass array zu ende ist, aber die sequenz noch weitergeht:
//                // müsste hier zudem nich sumFwdCoverage reseten
//                if ((i + 1) < coverageArrayFwd.length) {
//                    tempIndex = i;
//                    // Setzte Zähler wieder auf Null, da hier ja nun die richtige CoverageInterval herrscht             
//                } else {
//                    addDataForResult_OverlapSequence(coverageResult, tempCoverageAny, i, j, meanSumFwdCoverage);
//                    tempIndex = i;
//                    // muss hier false setzten, damit ich die gleiche Sequenz unten nicht noch einmal anlege
//                    isCoverageForIntervalSumOrFwdGenerated = false;
//                }
//                tempIndex = i;
//                // Setzte Zähler wieder auf Null, da hier ja nun die richtige CoverageInterval herrscht
//                countInsufficientCoverage = 0;
//                isCoverageForIntervalSumOrFwdGenerated = true;
//            } else {
//                countInsufficientCoverage++;
//                isStartPosSet = false;
//                // fülle das Objekt CoverageInterval nur, wenn man eine Lücke hat!
//                if (countInsufficientCoverage == 1 && isCoverageForIntervalSumOrFwdGenerated) {
//
//                    // Das zusammenfügen der Sequenzen mit Bezug zur vorherigen Sequenz soll nu einmal geschehen. Dies erreicht man durch setzten
//                    // der Variabel mergeOverlapOfSequencesIsDone
//                    if (mergeOverlapOfSequencesIsDone) {
//                        addDataForResult_noOverlapSequence(coverageResult, coverageClassAny, i, strand);
//                        sumOfCoverage = 0;
//                        tempIndex = i;
//                        isCoverageForIntervalSumOrFwdGenerated = false;
//
//                    } else {
//                        addDataForResult_OverlapSequence(coverageResult, tempCoverageAny, i, j, meanSumFwdCoverage);
//                        sumOfCoverage = 0;
//                        tempIndex = i;
//                        isCoverageForIntervalSumOrFwdGenerated = false;
//                        mergeOverlapOfSequencesIsDone = true;
//                    }
//
//                }
//            }
//        }
//    }
//
//    /**
//     * * Method for coverage count of each strand + no end of Sequence with
//     * package
//     *
//     * @param coverageResult
//     * @param coverageArrayFwdOrRev
//     */
//    private void generateDataForResultObject(boolean isCoverageForIntervalAnyGenerated, CoverageAndDiffResultPersistant coverageResult,
//            int[] coverageArrayFwdOrRev, int j, ArrayList<CoverageInterval> tempCoverageAny, byte strand, ArrayList<CoverageInterval> coverageClassAny) {
//        int tempIndex = 0;
//
//        // Alte Werte aus tempCoverage füllen in die Variabeln hier
//        int meanFwdOrRevCoverage = tempIntervals.get(j).getMeanCoverage();
//        startPos = tempIntervals.get(j).getStart();
//        isStartPosSet = true;
//        isCoverageForIntervalAnyGenerated = true;
//        mergeOverlapOfSequencesIsDone = true;
//
//        for (int i = 0; i < coverageArrayFwdOrRev.length; i++) {
//
//            if (coverageArrayFwdOrRev[i] >= this.parameters.getMinCoverageCount()) {
//                //Alle CoverageInterval Werte aufsummieren
//                sumOfCoverage = sumOfCoverage + coverageArrayFwdOrRev[i];
//                // Wenn dass array zu ende ist, aber die sequenz noch weitergeht:
//                // müsste hier zudem nich sumFwdCoverage reseten
//                if ((i + 1) < coverageArrayFwdOrRev.length) {
//                    tempIndex = i;
//                } else {
//                    addDataForResult_OverlapSequence(coverageResult, tempCoverageAny, i, j, meanFwdOrRevCoverage);
//                    // muss hier false setzten, damit ich die gleiche Sequenz unten nicht noch einmal anlege
//                    isCoverageForIntervalAnyGenerated = false;
//                    tempIndex = i;
//                }
//                if (isStartPosSet == false) {
//                    startPos = coverageResult.getLowerBound() + i;
//                    isStartPosSet = true;
//                }
//                tempIndex = i;
//                // Setzte Zähler wieder auf Null, da hier ja nun die richtige CoverageInterval herrscht
//                countInsufficientCoverage = 0;
//                isCoverageForIntervalAnyGenerated = true;
//            } else {
//                countInsufficientCoverage++;
//                isStartPosSet = false;
//                // fülle das Objekt CoverageInterval nur, wenn man eine Lücke hat!
//                if (countInsufficientCoverage == 1 && isCoverageForIntervalAnyGenerated) {
//
//
//                    // Das zusammenfügen der Sequenzen mit Bezug zur vorherigen Sequenz soll nu einmal geschehen. Dies erreicht man durch setzten
//                    // der Variabel mergeOverlapOfSequencesIsDone
//                    if (mergeOverlapOfSequencesIsDone) {
//                        addDataForResult_noOverlapSequence(coverageResult, coverageClassAny, i, strand);
//                        sumOfCoverage = 0;
//                        tempIndex = i;
//                        isCoverageForIntervalAnyGenerated = false;
//
//                    } else {
//                        addDataForResult_OverlapSequence(coverageResult, tempCoverageAny, i, j, meanFwdOrRevCoverage);
//                        sumOfCoverage = 0;
//                        tempIndex = i;
//                        isCoverageForIntervalAnyGenerated = false;
//                        mergeOverlapOfSequencesIsDone = true;
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Method for coverage count of each strand + end of Sequence with package
//     *
//     * @param coverageResult
//     * @param coverageArrayAny
//     */
//    private void generateDataForResultObject(boolean isCoverageForIntervalAnyGenerated, CoverageAndDiffResultPersistant coverageResult, int[] coverageArrayAny, byte strand, ArrayList<CoverageInterval> coverageClassAny) {
//        int tempIndex = 0;
//        for (int i = 0; i < coverageArraySumOrFwd.length; i++) {
//            if (coverageArrayAny[i] >= this.parameters.getMinCoverageCount()) {
//                //START coverage hier einmal setzten und wenn sie gesetzt ist nicht noch einmal setzten.
//                if (isStartPosSet == false) {
//                    startPos = coverageResult.getLowerBound() + i;
//                    isStartPosSet = true;
//                }
//                //Alle CoverageInterval Werte aufsummieren
//                sumOfCoverage = sumOfCoverage + coverageArrayAny[i];
//                // Wenn dass array zu ende ist, aber die Seuqenz noch weitergeht.
//                // müsste hier zudem nich sumFwdCoverage reseten
//                if ((i + 1) < coverageArraySumOrFwd.length) { // Müsste richtig sein, da ja bei richtigen boolschen Ausdruck die Schleife ausgeführt wird
//                    tempIndex = i;
//
//                } else {
//                    addDataForResult_noOverlapSequence(coverageResult, tempIntervals, i, strand);
//                    // muss hier false setzten, damit ich die gleiche Sequenz unten nicht noch einmal anlege
//                    isCoverageForIntervalAnyGenerated = false;
//                    tempIndex = i;
//                }
//                // Setzte Zähler wieder auf Null, da hier ja nun die richtige CoverageInterval herrscht
//                countInsufficientCoverage = 0;
//                isCoverageForIntervalAnyGenerated = true;
//            } else {
//                //abfrage was passiert wenn ich hier über z.B. 10 Positionen gehe und es jeweils nu reine coverage gab die zu klein ist!!
//                // Dies kann dadurch gemacht werden, dass man hier unten einen zähler mitlaufen lässt, der zählt wie oft man in
//                // die else anweisung läuft. wenn man einmal drin war, wird coverageClassSumOrFwd nicht mehr gefüllt
//                // Zähler spring auf null, wenn er wieder in die if-Anweisung oben kommt, also wenn die CoverageInterval wieder
//                // über minCoverage kommt.
//                countInsufficientCoverage++;
//                // Setzte die Variabel startCoverageIsSet auf false, da sie nun neu gesetzt werden muss
//                isStartPosSet = false;
//                // fülle das Objekt CoverageInterval nur, wenn man eine Lücke hat!
//                if (countInsufficientCoverage == 1 && isCoverageForIntervalAnyGenerated) {
//                    addDataForResult_noOverlapSequence(coverageResult, coverageClassAny, i, strand);
//                    sumOfCoverage = 0;
//                    tempIndex = i;
//                    isCoverageForIntervalAnyGenerated = false;
//                }
//            }
//        }
//    }
//
//    private void addDataForResult_noOverlapSequence(CoverageAndDiffResultPersistant coverageResult, ArrayList<CoverageInterval> listToAdd, int i, byte strand) {
//        // Man muss hier deshalb coverageResult.getLowerBound verwenden, weil man mit jedem Datenpacket ja das i resetet!!!
//        // muss hier i-1 nehmen, weil er sonst schon die Stelle mit der zu geringen CoverageInterval als stopp Sequenz nimmt
//        stopCoverage = coverageResult.getLowerBound() + i;
//        // Füllen des CoverageInterval OBjektes mit (int track, String strand, int start, int stopp, int length, int coverage)
//        // CoverageInterval wird hier als mean coverage angegeben
//
//        int lengthSequence = stopCoverage - startPos;
//        int meanCoverage = sumOfCoverage / (stopCoverage - startPos);
//        int stoppSequence = stopCoverage-1;
//
//        CoverageInterval newCoverageInstance = new CoverageInterval(this.connector.getTrackID(), strand, startPos, stoppSequence, lengthSequence,
//                meanCoverage);
//        listToAdd.add(newCoverageInstance);
//        //Füllen der temporären Variabel
//        // nehme das zuletzt gefüllte Objekt der ArrayList tempCoverage (ist vom Typ CoverageInterval) und fülle es in die ArrayList coverageClassSumOrFwd
//    }
//
//    private void addDataForResult_OverlapSequence(CoverageAndDiffResultPersistant coverageResult, ArrayList<CoverageInterval> tempCoverageAny, int i, int j, int meanSumFwdCoverage) {
//        // Man muss hier deshalb coverageResult.getLowerBound verwenden, weil man mit jedem Datenpacket ja das i resetet!!!
//        // muss hier i-1 nehmen, weil er sonst schon die Stelle mit der zu geringen CoverageInterval als stopp Sequenz nimmt
//        stopCoverage = coverageResult.getLowerBound() + i;
//        // Füllen des CoverageInterval Objektes mit (int track, String strand, int start, int stopp, int length, int coverage) 
//        // hier setter methoden impelementieren. Track, strand und start bleiben gleich
//
//        int meanCoverageTemp = sumOfCoverage / (stopCoverage - startPos);
//        int lengthSequence = stopCoverage - startPos;      
//         int stoppSequence = stopCoverage-1;
//        
//        tempCoverageAny.get(j).setLength(lengthSequence);
//        tempCoverageAny.get(j).setStop(stoppSequence);
//                // Nehme alten Mittelwert und neuen Mittelwert und berrechne daraus den neuen Mittelwert
//        int meanCoverage = (meanCoverageTemp + meanSumFwdCoverage) / 2;
//        tempCoverageAny.get(j).setMeanCoverage(meanCoverage);
//    }
}
