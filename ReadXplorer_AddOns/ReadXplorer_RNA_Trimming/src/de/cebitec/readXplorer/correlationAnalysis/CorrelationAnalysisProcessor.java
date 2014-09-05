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
package de.cebitec.readXplorer.correlationAnalysis;

import de.cebitec.readXplorer.correlationAnalysis.CorrelationAnalysisAction.CorrelationCoefficient;
import de.cebitec.readXplorer.databackend.IntervalRequest;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException;
import de.cebitec.readXplorer.databackend.ThreadListener;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.Coverage;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistent;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.util.classification.MappingClass;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * CorrelationAnalysisProcessor is a process of analysing correlation
 * of two tracks.
 * @author Evgeny Anisiforov
 */
public class CorrelationAnalysisProcessor implements ThreadListener {
    
    private static final int MINIMUMINTERVALLENGTH = 90000;
    
    private Integer rightBound;
    private final Integer minCorrelation;
    private final Integer minPeakCoverage;
    private StrandDirection currentDirection;
    private boolean canceled = false;
    private final CorrelationCoefficient correlationCoefficient;
    private CorrelationResult analysisResult;
    private final ArrayList<CorrelatedInterval> correlationsList;
    
    private int steps;
    private int currentStep = 0;
    private int currentPosition = 1;
    private int currentTotalPos = 0;
    private Integer intervalLength;
    private CorrelationResultPanel resultView;
    
    private List<CoverageAndDiffResultPersistent> resultList;
    private List<TrackConnector> trackConnectors;
    private boolean ready = false;
    private final Map<Integer, PersistentChromosome> chromMap;
    private int lastChromId = 0;
    
    public enum StrandDirection { FWD, REV };
    
    /**
     * Creates a new CorrelationAnalysisProcessor and starts computing the
     * correlation analysis.
     * @param cc
     * @param referenceViewer
     * @param tracks
     * @param intervalLength
     * @param minCorrelation
     * @param minPeakCoverage
     */
    public CorrelationAnalysisProcessor(CorrelationAnalysisAction.CorrelationCoefficient cc,
            ReferenceViewer referenceViewer, List<PersistentTrack> tracks,
            Integer intervalLength, Integer minCorrelation, Integer minPeakCoverage) {

        this.ready = false;
        this.currentPosition = 1;
        this.currentDirection = StrandDirection.FWD;
        this.correlationsList = new ArrayList<>();

        this.trackConnectors = new ArrayList<>();
        Map<Integer, PersistentTrack> trackMap = new HashMap<>();
        SaveFileFetcherForGUI fetcher = new SaveFileFetcherForGUI();
        for (PersistentTrack track : tracks) {
            try {
                trackConnectors.add(fetcher.getMultiTrackConnector(track));
            } catch (UserCanceledTrackPathUpdateException ex) {
                SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                continue;
            }
            trackMap.put(track.getId(), track);
        }
        
        this.analysisResult = new CorrelationResult(this.correlationsList, trackMap, 
                referenceViewer.getReference(), false, -1, -1);
        Map<String, Object> params = new HashMap<>();
        params.put("CorrelationCoefficient", cc);
        params.put("intervalLength", intervalLength);
        params.put("minCorrelation", minCorrelation);
        params.put("minPeakCoverage", minPeakCoverage);
        this.analysisResult.setAnalysisParameters(params);

        this.chromMap = referenceViewer.getReference().getChromosomes();
        this.rightBound = PersistentReference.calcWholeGenomeLength(chromMap);
        this.createProcessHandle(NbBundle.getMessage(CorrelationAnalysisAction.class, "CTL_CorrelationAnalysisProcess.name", "FWD"));

        this.intervalLength = intervalLength;
        this.minCorrelation = minCorrelation;
        this.minPeakCoverage = minPeakCoverage;
        this.correlationCoefficient = cc;

        CorrelationResultTopComponent tc = CorrelationResultTopComponent.findInstance();
        tc.open();
        tc.requestActive();
        resultView = CorrelationResultTopComponent.findInstance().openResultTab(referenceViewer);
        this.resultView.setAnalysisResult(this.analysisResult);
        requestNextStep();
    }
    
    private void createProcessHandle(String title) {
        this.ph = ProgressHandleFactory.createHandle(title, new Cancellable() {

            @Override
            public boolean cancel() {
                return handleCancel();
            }
        });
        ph.start();
        ph.switchToDeterminate(this.rightBound);
    }
        
    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
       
        //this.io.getOut().println(msg);
    }
    
    private int getCoverageAt(CoverageAndDiffResultPersistent coverageResult, int position, StrandDirection direction) {
        Coverage coverage = coverageResult.getCovManager().getCoverage(MappingClass.COMMON_MATCH);
        if (direction == StrandDirection.REV) {
            return coverage.getRevCov(position);
        } else {
            return coverage.getFwdCov(position);
        }
    }
    
    
    /**
     * Checks if all coverage results in the currently loaded result list
     * contain zero coverage at the current position
     */
    private boolean allCoverageEqualZero(StrandDirection direction, int position) {
        for (CoverageAndDiffResultPersistent result : this.resultList) {
            if (getCoverageAt(result, position, direction) != 0) {
                return false;
            }
        }
        return true;
    }
    
    /** computes the maximum peak covage from the currently loaded result list */
    private int getPeakCoverage(StrandDirection direction, int position) {
        int peakCoverage = 0;
        for (CoverageAndDiffResultPersistent result : this.resultList) {
            peakCoverage = Math.max(peakCoverage, getCoverageAt(result, position, direction));
        }
        return peakCoverage;
    }
    
    /** computes the maximum peak covage from the currently loaded result list */
    private double getPeakCoverageFromArray(double[] data) {
        double peakCoverage = 0;
        for (double d : data) {
            peakCoverage = Math.max(peakCoverage, d);
        }
        return peakCoverage;
    }
    
    /**
     * copy coverage from the coverageResult to a double-Array to be 
     * passed to the statistics package
     * @param coverageResult
     * @param direction
     * @param from
     * @param to
     * @return 
     */
    private double[] copyCoverage(CoverageAndDiffResultPersistent coverageResult, StrandDirection direction, int from, int to) {
        if (to < from) { throw new IllegalArgumentException("from value must be less than the to value"); }
        double[] result = new double[to-from];
        int writeIndex = 0;
        for(int i=from; i<to; i++ ) {
            result[writeIndex] = getCoverageAt(coverageResult, i, direction);
            writeIndex++;
        }
        return result;
    }
    
    /**
     * computeStep is called, after the data of all tracks for the current step 
     * has been received.
     * @param direction 
     */
    private void computeStep(StrandDirection direction) {
        int maximumCoveredPosition = this.resultList.get(0).getCovManager().getRightBound();
        int chromId = this.resultList.get(0).getRequest().getChromId();
        int track1Id = this.trackConnectors.get(0).getTrackID();
        int track2Id = this.trackConnectors.get(1).getTrackID();
        
        if (lastChromId != chromId) { //reset for new chromosome
            currentPosition = 0;
        }
        
        while (this.currentPosition < maximumCoveredPosition - this.intervalLength) {
            //ignore areas containing zeros
            while ((this.currentPosition < maximumCoveredPosition - this.intervalLength)
                    && allCoverageEqualZero(direction, this.currentPosition))            
            {
                ++this.currentPosition;
                ++this.currentTotalPos;
            }

            //compute correlation
            //TODO: multiple tracks, not only two
            if (this.currentPosition < maximumCoveredPosition - this.intervalLength) {
                int to = currentPosition + this.intervalLength;
                double[] x = copyCoverage(this.resultList.get(0), direction, currentPosition, to);
                double[] y = copyCoverage(this.resultList.get(1), direction, currentPosition, to);
                double peakCov1 = this.getPeakCoverageFromArray(x);
                double peakCov2 = this.getPeakCoverageFromArray(y);

                if ((peakCov1 >= this.minPeakCoverage) && (peakCov2 >= this.minPeakCoverage)) {

                    double correlation = 0;
                    if (this.correlationCoefficient.equals(CorrelationCoefficient.PEARSON)) {
                        correlation = new PearsonsCorrelation().correlation(x, y);
                    } else if (this.correlationCoefficient.equals(CorrelationCoefficient.SPEARMAN)) {
                        correlation = new SpearmansCorrelation().correlation(x, y);
                    }
                    double minCorr = ((double) this.minCorrelation) / 100.0;

                    if ((correlation > minCorr) || (correlation < (minCorr * (-1)))) {
                        this.showMsg("correlation of interval [" + this.currentPosition + "-" + to + "] is " + correlation + " on " + direction);
                        CorrelatedInterval resultLine = new CorrelatedInterval(direction, track1Id, track2Id, chromId, this.currentPosition, to, correlation,
                                Math.min(peakCov1, peakCov2));
                        this.correlationsList.add(resultLine);
                        this.resultView.addData(resultLine);
                    }

                } else {
                    this.showMsg("ignore correlation of interval [" + this.currentPosition + "-" + to
                            + "] is because min coverage is " + peakCov1 + " and " + peakCov2);
                }
                this.currentPosition = to + 1;
                this.currentTotalPos = currentTotalPos + this.intervalLength + 1;
            }
            this.lastChromId = chromId;
        }
        
        
        if (this.currentTotalPos < this.rightBound - this.intervalLength) {
            ph.progress(this.currentTotalPos);
            if (canceled) { this.finish(); }
            else { requestNextStep(); }
        }
        else {
            
            if (direction.equals(StrandDirection.FWD)) {
                ph.finish();
                this.currentDirection = StrandDirection.REV;
                this.createProcessHandle(NbBundle.getMessage(CorrelationAnalysisAction.class, "CTL_CorrelationAnalysisProcess.name", "REV"));
                
                //compute again from the beginning with the other strand direction
                this.currentPosition = 1;
                requestNextStep();
            }
            else {
                this.finish();
            }
            
        }
    }
    
    /** 
     *  This method is called to indicate that the analysis execution 
     *  is finished. The resultView will be notified to display an 
     *  appropriate message.
     */
    private void finish() {
        ph.finish();
        ready = true;
        this.resultView.ready(this.analysisResult);
    }
    
    private ProgressHandle ph;
    
    /**
     * this method is called to request the coverage for the next step
     */
    private void requestNextStep() {
        this.resultList = new ArrayList<>();
        int t = currentStep;
        if (currentStep % 2 == 0) {
            t = steps - currentStep;
        }

        this.showMsg("Requesting position=" + currentPosition);

        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        for (TrackConnector tc : this.trackConnectors) {
            tc.addCoverageRequest(new IntervalRequest(currentPosition, currentPosition + MINIMUMINTERVALLENGTH, tc.getRefGenome().getActiveChromId(), this, true));
        }
    }
    
    /**
     * this method is called, if the user clicks on the cancel button
     * to stop the execution of this analysis
     * @return 
     */
    private boolean handleCancel() {
        this.showMsg("handleCancel");
        this.canceled = true;
        return true;
    }
    
    
    /** Receive data from CoverageThread and save it. 
     * Wait until the data for all tracks has arrived */
    @Override
    public synchronized void receiveData(Object data) {
        if (data instanceof CoverageAndDiffResultPersistent) {
            //TODO: possible problem when results from the same track are quicker returned than from the other track. Separate resultlist
            this.resultList.add((CoverageAndDiffResultPersistent) data);
            if (this.resultList.size() == this.trackConnectors.size()) {
                this.computeStep(this.currentDirection);
            }
        }
    }

    @Override
    public void notifySkipped() {
        
    }
}
