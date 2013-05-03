/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.databackend.CoverageAndDiffRequest;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * CorrelationAnalysisProcessor is a process of analysing correlation
 * of multiple (at least two) tracks
 * @author Evgeny Anisiforov
 */
public class CorrelationAnalysisProcessor implements ThreadListener {
    //private final InputOutput io;
    private Integer rightBound;
    private final Integer minCorrelation;
    private final Integer minPeakCoverage;
    private StrangDirection currentDirection;
    private boolean canceled = false;

    private void createProcessHandle(String title) {
        this.ph = ProgressHandleFactory.createHandle(title, new Cancellable() {

            public boolean cancel() {
                return handleCancel();
            }
        });
        ph.start();
        ph.switchToDeterminate(this.rightBound);
    }
    
    public enum StrangDirection { FWD, REV };
    
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
    
    public CorrelationAnalysisProcessor(ReferenceViewer referenceViewer, List<PersistantTrack> list, 
            Integer intervalLength, Integer minCorrelation, Integer minPeakCoverage) {
        
        this.ready = false;
        this.currentPosition = 1;
        this.currentDirection = StrangDirection.FWD;
        
        ArrayList<TrackConnector> tcl = new ArrayList<TrackConnector>();
        for(PersistantTrack track : list) {
            tcl.add(ProjectConnector.getInstance().getMultiTrackConnector(track));
        }
        this.rightBound = tcl.get(0).getRefSequenceLength();
        
        this.createProcessHandle(NbBundle.getMessage(CorrelationAnalysisAction.class, "CTL_CorrelationAnalysisProcess.name", "FWD"));
        
        this.trackConnectorList = tcl;
        this.intervalLength = intervalLength;
        this.minCorrelation = minCorrelation;
        this.minPeakCoverage = minPeakCoverage;
        
        
        CorrelationResultTopComponent tc = CorrelationResultTopComponent.findInstance();
        tc.open();
        tc.requestActive();
        resultView = CorrelationResultTopComponent.findInstance().openResultTab(referenceViewer);
        
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
    
    private int getCoverageAt(CoverageAndDiffResultPersistant coverageResult, int position, StrangDirection direction) {
        if (direction==StrangDirection.REV) return getRevCoverageAt(coverageResult, position);
        else return getFwdCoverageAt(coverageResult, position);
    }
    
    
    /** checks if all coverage results in the currently loaded result list
     * contain zero coverage on the current position */
    private boolean allCoverageEqualZero(StrangDirection direction, int position) {
        for (CoverageAndDiffResultPersistant result : this.resultList) {
            if (getCoverageAt(result, position, direction)!=0)
                return false;
        }
        return true;
    }
    
    /** computes the maximum peak covage from the currently loaded result list */
    private int getPeakCoverage(StrangDirection direction, int position) {
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
    
    
    private double[] copyCoverage(CoverageAndDiffResultPersistant coverageResult, StrangDirection direction, int from, int to) {
        if (to<from) throw new IllegalArgumentException("from value must be less than the to value");
        double[] result = new double[to-from];
        int writeIndex = 0;
        for(int i=from; i<to; i++ ) {
            result[writeIndex] = getCoverageAt(coverageResult, i, direction);
            writeIndex++;
        }
        return result;
    }
    
    private void computeStep(StrangDirection direction) {
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
            if (this.currentPosition<maximumCoveredPosition-this.intervalLength) {
                int to = currentPosition+this.intervalLength;
                double[] x = copyCoverage(this.resultList.get(0), direction, currentPosition, to);
                double[] y = copyCoverage(this.resultList.get(1), direction, currentPosition, to);
                double peakCov1 = this.getPeakCoverageFromArray(x);
                double peakCov2 = this.getPeakCoverageFromArray(y);
                
                if ((peakCov1>=this.minPeakCoverage) && 
                        (peakCov2>=this.minPeakCoverage)) {
                
                    double correlation = new PearsonsCorrelation().correlation(x, y);
                    double minCorr = ((double) this.minCorrelation) / 100.0;

                    if ((correlation>minCorr) || (correlation<(minCorr*(-1)))) {
                        this.showMsg("correlation of interval ["+this.currentPosition+"-"+to+"] is "+correlation+" on "+direction );
                        this.resultView.addData(direction, this.currentPosition, to, correlation,
                                Math.min(peakCov1, peakCov2)
                                );
                    }
                    
                }
                else {
                    this.showMsg("ignore correlation of interval ["+this.currentPosition+"-"+to
                            +"] is because min coverage is "+peakCov1+" and "+peakCov2 );
                        
                }
                this.currentPosition = to+1;
            }
        }
        
        
        if (this.currentPosition<this.rightBound-this.intervalLength) { //&& (!wasCanceled)) {
            ph.progress(this.currentPosition);
            if (canceled) this.finish(); 
            else requestNextStep();
        }
        else {
            
            if (direction.equals(StrangDirection.FWD)) {
                ph.finish();
                this.currentDirection = StrangDirection.REV;
                this.createProcessHandle(NbBundle.getMessage(CorrelationAnalysisAction.class, "CTL_CorrelationAnalysisProcess.name", "REV"));
                
                //compute again from the beginning with another strang direction
                this.currentPosition=1;
                requestNextStep();
            }
            else {
                this.finish();
            }
            
        }
    }
    
    private void finish() {
        ph.finish();
        ready = true;
        this.resultView.ready();
    }
    
    private ProgressHandle ph;
    
    private void requestNextStep() {
        
                    this.resultList = new ArrayList<CoverageAndDiffResultPersistant> ();
       
                    int t = currentStep;
                    if (currentStep % 2 == 0) t = steps - currentStep;
                    
                    //currentPosition = t * (CoverageThread.MINIMUMINTERVALLENGTH / SCANFACTOR) + 100;
                    this.showMsg("Requesting position=" + currentPosition);
                    
                    try {                    
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    
                    for(TrackConnector tc : this.trackConnectorList) {
                        tc.addCoverageRequest(new CoverageAndDiffRequest(currentPosition, currentPosition+100, this));
                    }
     
    }
    
    private boolean handleCancel() {
        this.showMsg("handleCancel");
        this.canceled = true;
        return true;
    }
    
    
    /* receive data from CoverageThread and save it. Wait until the data for all tracks has arrived */
    @Override
    public synchronized void receiveData(Object data) {
        if (data instanceof CoverageAndDiffResultPersistant) {
            this.resultList.add((CoverageAndDiffResultPersistant) data);
            if (this.resultList.size()==this.trackConnectorList.size()) {
                this.computeStep(this.currentDirection);
            }
        }
    }

    @Override
    public void notifySkipped() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    private List<CoverageAndDiffResultPersistant> resultList;
    private List<TrackConnector> trackConnectorList;
    private boolean ready = false;
}
