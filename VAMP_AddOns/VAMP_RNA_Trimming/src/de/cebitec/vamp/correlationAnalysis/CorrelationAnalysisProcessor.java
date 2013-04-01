/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.CoverageAndDiffRequest;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * CorrelationAnalysisProcessor is a process of analysing correlation
 * of multiple (at least two) tracks
 * @author Evgeny Anisiforov
 */
public class CorrelationAnalysisProcessor implements ThreadListener {
    private final InputOutput io;
    private Integer rightBound;
    private final Integer minCorrelation;
    private StrangDirection currentDirection;
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
       
        this.io.getOut().println(msg);
    }
    
    
    public CorrelationAnalysisProcessor(List<PersistantTrack> list, Integer intervalLength, Integer minCorrelation) {
        this.io = IOProvider.getDefault().getIO("CorrelationAnalysis", true);
        this.io.setOutputVisible(true);
        this.io.getOut().println("");
        
        CentralLookup.getDefault().add(this);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();
        
        this.ready = false;
        this.currentPosition = 1;
        this.currentDirection = StrangDirection.FWD;
        
        this.ph = ProgressHandleFactory.createHandle("Analysing correlation..", new Cancellable() {

            public boolean cancel() {
                return handleCancel();
            }
        });
        
        ArrayList<TrackConnector> tcl = new ArrayList<TrackConnector>();
        for(PersistantTrack track : list) {
            tcl.add(ProjectConnector.getInstance().getMultiTrackConnector(track));
        }
        this.rightBound = tcl.get(0).getRefSequenceLength();
        ph.start();
        ph.switchToDeterminate(this.rightBound);
        
        this.trackConnectorList = tcl;
        this.intervalLength = intervalLength;
        this.minCorrelation = minCorrelation;
        requestNextStep();
    }
    
    private int getFwdCoverageAt(CoverageAndDiffResultPersistant coverageResult, int position) {
        return coverageResult.getCoverage().getBestMatchFwdMult(position)
                + coverageResult.getCoverage().getCommonFwdMult(position)
                + coverageResult.getCoverage().getPerfectFwdMult(position);
    }
    
    private int getRevCoverageAt(CoverageAndDiffResultPersistant coverageResult, int position) {
        return coverageResult.getCoverage().getBestMatchRevMult(position)
                + coverageResult.getCoverage().getCommonRevMult(position)
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
                double correlation = new PearsonsCorrelation().correlation(x, y);
                double minCorr = ((double) this.minCorrelation) / 100.0;
                if ((correlation>minCorr) || (correlation<(minCorr*(-1)))) {
                    this.showMsg("correlation of interval ["+this.currentPosition+"-"+to+"] is "+correlation+" on "+direction );
                }
                this.currentPosition = to+1;
            }
        }
        
        
        if (this.currentPosition<this.rightBound-this.intervalLength) { //&& (!wasCanceled)) {
            ph.progress(this.currentPosition);
            requestNextStep();
        }
        else {
            
            if (direction.equals(StrangDirection.FWD)) {
                //compute again from the beginning with another strang direction
                this.currentPosition=1;
                ph.progress(this.currentPosition);
                ph.setDisplayName("Analysing correlation.. (Reverse strang direction)");
                this.currentDirection = StrangDirection.REV;
                requestNextStep();
            }
            else {
                ph.finish();
                ready = true;
            }
            
        }
    }
    
    
    
    private ProgressHandle ph;
    
    private void requestNextStep() {
        
                    this.resultList = new ArrayList<CoverageAndDiffResultPersistant> ();
        
        
                    //TrackConnector tc = ProjectConnector.getInstance().getTrackConnector(track);
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

                    
                    //currentStep++;
                    //make sure, that we do not show too many steps
                    //if (currentStep>steps) ;// ph.progress(steps);
                        //currentStep = steps;
                    //else ph.progress(currentStep);
                    
                    
                    
                    
                    
    }
    
    private boolean handleCancel() {
        this.showMsg("handleCancel");
        /*if (null == theTask) {
            return false;
        }

        return theTask.cancel();*/
        return false;
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
