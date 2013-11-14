package de.cebitec.readXplorer.correlationAnalysis;

import de.cebitec.readXplorer.correlationAnalysis.CorrelationAnalysisAction.CorrelationCoefficient;
import de.cebitec.readXplorer.databackend.IntervalRequest;
import de.cebitec.readXplorer.databackend.ThreadListener;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * CorrelationAnalysisProcessor is a process of analysing correlation
 * of two tracks
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
    
    public enum StrandDirection { FWD, REV };
    
    private int steps;
    private int currentStep = 0;
    private int currentPosition = 1;
    private Integer intervalLength;
        
    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
       
        //this.io.getOut().println(msg);
    }
    
    private CorrelationResultPanel resultView;
    
    /** creates a new CorrelationAnalysisProcessor and starts computing the 
     *  correlation analysis 
     * @param cc
     * @param referenceViewer
     * @param list
     * @param intervalLength
     * @param minCorrelation
     * @param minPeakCoverage 
     */ 
    public CorrelationAnalysisProcessor(CorrelationAnalysisAction.CorrelationCoefficient cc,
            ReferenceViewer referenceViewer, List<PersistantTrack> list, 
            Integer intervalLength, Integer minCorrelation, Integer minPeakCoverage) {
        
        this.ready = false;
        this.currentPosition = 1;
        this.currentDirection = StrandDirection.FWD;
        this.correlationsList = new ArrayList<>();
        
        ArrayList<TrackConnector> tcl = new ArrayList<>();
        HashMap<Integer, PersistantTrack> trackNamesList = new HashMap<>();
        SaveTrackConnectorFetcherForGUI fetcher = new SaveTrackConnectorFetcherForGUI();
        for (PersistantTrack track : list) {
            try {
                tcl.add(fetcher.getMultiTrackConnector(track));
            } catch (UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog(null, "The path of one of the selected tracks could not be resolved. The analysis will be canceled now.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
                continue;
            }
            trackNamesList.put(track.getId(), track);
        }
        this.analysisResult = new CorrelationResult(this.correlationsList, trackNamesList, false);
        HashMap<String,Object> params = new HashMap<>();
        params.put("CorrelationCoefficient", cc);
        params.put("intervalLength", intervalLength);
        params.put("minCorrelation", minCorrelation);
        params.put("minPeakCoverage", minPeakCoverage);
        this.analysisResult.setAnalysisParameters(params);
        
        this.rightBound = tcl.get(0).getRefSequenceLength();
        
        this.createProcessHandle(NbBundle.getMessage(CorrelationAnalysisAction.class, "CTL_CorrelationAnalysisProcess.name", "FWD"));
        
        this.trackConnectorList = tcl;
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
    
    private int getFwdCoverageAt(CoverageAndDiffResultPersistant coverageResult, int position) {
        return coverageResult.getCoverage().getCommonFwdMult(position)
                + coverageResult.getCoverage().getPerfectFwdMult(position);
    }
    
    private int getRevCoverageAt(CoverageAndDiffResultPersistant coverageResult, int position) {
        return coverageResult.getCoverage().getCommonRevMult(position)
                + coverageResult.getCoverage().getPerfectRevMult(position);
    }
    
    private int getCoverageAt(CoverageAndDiffResultPersistant coverageResult, int position, StrandDirection direction) {
        if (direction == StrandDirection.REV) {return getRevCoverageAt(coverageResult, position); }
        else { return getFwdCoverageAt(coverageResult, position); }
    }
    
    
    /** checks if all coverage results in the currently loaded result list
     * contain zero coverage on the current position */
    private boolean allCoverageEqualZero(StrandDirection direction, int position) {
        for (CoverageAndDiffResultPersistant result : this.resultList) {
            if (getCoverageAt(result, position, direction) != 0) {
                return false;
            }
        }
        return true;
    }
    
    /** computes the maximum peak covage from the currently loaded result list */
    private int getPeakCoverage(StrandDirection direction, int position) {
        int peakCoverage = 0;
        for (CoverageAndDiffResultPersistant result : this.resultList) {
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
    private double[] copyCoverage(CoverageAndDiffResultPersistant coverageResult, StrandDirection direction, int from, int to) {
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
     * computeStep is called, after the data of all track for the current step 
     * has been received.
     * @param direction 
     */
    private void computeStep(StrandDirection direction) {
        int maximumCoveredPosition = this.resultList.get(0).getCoverage().getRightBound();
        while (this.currentPosition<maximumCoveredPosition-this.intervalLength) {
            //ignore areas containing zeros
            while ((this.currentPosition<maximumCoveredPosition-this.intervalLength)
                    && allCoverageEqualZero(direction, this.currentPosition))            
            {
                this.currentPosition++;
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
                        CorrelatedInterval resultLine = new CorrelatedInterval(direction, this.currentPosition, to, correlation,
                                Math.min(peakCov1, peakCov2));
                        this.correlationsList.add(resultLine);
                        this.resultView.addData(resultLine);
                    }

                } else {
                    this.showMsg("ignore correlation of interval [" + this.currentPosition + "-" + to
                            + "] is because min coverage is " + peakCov1 + " and " + peakCov2);
                }
                this.currentPosition = to + 1;
            }
        }
        
        
        if (this.currentPosition<this.rightBound-this.intervalLength) { 
            ph.progress(this.currentPosition);
            if (canceled) { this.finish(); }
            else { requestNextStep(); }
        }
        else {
            
            if (direction.equals(StrandDirection.FWD)) {
                ph.finish();
                this.currentDirection = StrandDirection.REV;
                this.createProcessHandle(NbBundle.getMessage(CorrelationAnalysisAction.class, "CTL_CorrelationAnalysisProcess.name", "REV"));
                
                //compute again from the beginning with another strang direction
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

        for (TrackConnector tc : this.trackConnectorList) {
            tc.addCoverageRequest(new IntervalRequest(currentPosition, currentPosition + MINIMUMINTERVALLENGTH, this, true));
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
        if (data instanceof CoverageAndDiffResultPersistant) {
            this.resultList.add((CoverageAndDiffResultPersistant) data);
            if (this.resultList.size() == this.trackConnectorList.size()) {
                this.computeStep(this.currentDirection);
            }
        }
    }

    @Override
    public void notifySkipped() {
        
    }
    
    
    private List<CoverageAndDiffResultPersistant> resultList;
    private List<TrackConnector> trackConnectorList;
    private boolean ready = false;
}
