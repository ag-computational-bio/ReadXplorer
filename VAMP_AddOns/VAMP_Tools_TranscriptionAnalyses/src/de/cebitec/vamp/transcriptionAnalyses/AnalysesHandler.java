package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.JobI;
import de.cebitec.vamp.databackend.IntervalRequest;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.DataVisualisationI;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * @author -Rolf Hilker
 * 
 * Class for handling the data threads for one of the currently started analyses.
 * CAUTION: You cannot query coverage and mapping tables with the same AnalysisHandler at
 * the same time.
 */
public class AnalysesHandler implements ThreadListener, Observable, JobI {
    
    public static final String DATA_TYPE_COVERAGE = "Coverage";
    public static final String DATA_TYPE_MAPPINGS = "Mappings";
    
    private final ProgressHandle progressHandle;
    private DataVisualisationI parent;
    private TrackViewer trackViewer;
    private int genomeSize;
    
    //varibles for transcription start site detection
    private int nbCovRequests;
    private int nbMappingRequests;
    private int nbRequests;
    private int nbCarriedOutRequests;
    private String queryType;
    
    private ArrayList<Observer> observers;
    private boolean coverageNeeded;
    private boolean mappingsNeeded;
    
    /**
     * CAUTION: You cannot query coverage and mapping tables with the same AnalysisHandler at
     * the same time.
     * @param trackViewer the track viewer for which the analysis is carried out
     * @param parent the parent for visualization of the results
     */
    public AnalysesHandler (TrackViewer trackViewer, DataVisualisationI parent) {
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AnalysisTranscriptionStart.class, "MSG_AnalysesWorker.progress.name"));
        this.observers = new ArrayList<>();
        this.parent = parent;
        this.trackViewer = trackViewer;
        this.coverageNeeded = false;
        this.mappingsNeeded = false;
        this.nbCovRequests = 0;
        this.nbMappingRequests = 0;
        
    }
    
    /**
     * Needs to be called in order to start the transcription analyses. Creates the
     * needed database requests and carries them out. The parent has to be a
     * ThreadListener in order to receive the coverage or mapping data.
     * Afterwards the results can be received by calling {@link getResults()} on the
     * observers of this analyses handler.
     */
    public void startAnalysis() {

        this.queryType = this.coverageNeeded ? DATA_TYPE_COVERAGE : DATA_TYPE_MAPPINGS;
        this.nbRequests = 0;
        this.progressHandle.start();
        TrackConnector trackCon = this.trackViewer.getTrackCon();
        
        int refId = this.trackViewer.getReference().getId();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(refId);
        this.genomeSize = refConnector.getRefGenome().getRefLength();

        if (this.coverageNeeded) {

            //decide upon stepSize of a single request and analyse coverage of whole genome
            int stepSize = 200000;
            int from = 1;
            int to = this.genomeSize > stepSize ? stepSize : this.genomeSize;
            int additionalRequest = this.genomeSize % stepSize == 0 ? 0 : 1;
            this.nbCovRequests = this.genomeSize / stepSize + additionalRequest;
            this.nbRequests += this.nbCovRequests;
            this.progressHandle.switchToDeterminate(this.nbRequests);
            this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);

            while (to < this.genomeSize) {
                IntervalRequest coverageRequest = new IntervalRequest(from, to, this, Properties.BEST_MATCH_COVERAGE);
                trackCon.addCoverageAnalysisRequest(coverageRequest);
                
                from = to + 1;
                to += stepSize;
            }

            //calc last interval until genomeSize
            to = this.genomeSize;
            trackCon.addCoverageAnalysisRequest(new IntervalRequest(from, to, this, Properties.BEST_MATCH_COVERAGE));
        } else
        
        if (this.mappingsNeeded) {
            
            int stepSize = 50000;
            
            if (trackCon.isDbUsed()) {
                //calculate which mappings are needed from the db
                int numUnneededMappings = 0;
                List<PersistantTrack> tracksAll = ProjectConnector.getInstance().getTracks();
                for (PersistantTrack track : tracksAll) {
                    TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(track);
                    if (track.getId() < trackCon.getTrackID()) {
                        numUnneededMappings += connector.getNumOfUniqueMappings();
                    }
                }
                int numInterestingMappings = numUnneededMappings + trackCon.getNumOfUniqueMappings();
                int from = numUnneededMappings;
                int to = numInterestingMappings - numUnneededMappings > stepSize
                        ? numUnneededMappings + stepSize : numInterestingMappings;

                int additionalRequest = numInterestingMappings % stepSize == 0 ? 0 : 1;
                this.nbMappingRequests = (numInterestingMappings - numUnneededMappings) / stepSize + additionalRequest;

                this.nbRequests += this.nbMappingRequests;
                this.progressHandle.switchToDeterminate(this.nbRequests);
                this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);

                while (to < numInterestingMappings) {
                    trackCon.addMappingAnalysisRequest(new IntervalRequest(from, to, this));

                    from = to + 1;
                    to += stepSize;
                }

                //calc last interval until genomeSize
                to = numInterestingMappings;
                IntervalRequest mappingRequest = new IntervalRequest(from, to, this);
                trackCon.addMappingAnalysisRequest(mappingRequest);

            } else {
                this.nbRequests = this.genomeSize / stepSize + 1;
                this.progressHandle.switchToDeterminate(this.nbRequests);
                this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);
                int from = 0;
                int to = stepSize;
                while (to < this.genomeSize) {
                    trackCon.addMappingAnalysisRequest(new IntervalRequest(from, to, this, Properties.MAPPINGS_WO_DIFFS));

                    from = to + 1;
                    to += stepSize;
                }
                
                //calc last interval until genomeSize
                to = this.genomeSize;
                trackCon.addMappingAnalysisRequest(new IntervalRequest(from, to, this, Properties.MAPPINGS_WO_DIFFS));
            }
        } else {
            this.progressHandle.finish();
        }
    }

    @Override
    public void receiveData(Object data) {        
        this.progressHandle.progress(this.queryType + " request " + 
                (nbCarriedOutRequests + 1) + " of " + nbRequests, ++nbCarriedOutRequests);
        this.notifyObservers(data);

        //when the last request is finished signalize the parent to collect the data
        if (this.nbCarriedOutRequests >= this.nbRequests) {
            String dataType = this.coverageNeeded ? DATA_TYPE_COVERAGE : DATA_TYPE_MAPPINGS;
            this.parent.showData(dataType);
            this.nbCarriedOutRequests = 0;
            this.progressHandle.finish();
        }
    }

    public boolean isCoverageNeeded() {
        return this.coverageNeeded;
    }

    public void setCoverageNeeded(boolean coverageNeeded) {
        this.coverageNeeded = coverageNeeded;
    }

    public boolean isMappingsNeeded() {
        return this.mappingsNeeded;
    }

    public void setMappingsNeeded(boolean mappingsNeeded) {
        this.mappingsNeeded = mappingsNeeded;
    }

    @Override
    public int getNbCarriedOutRequests() {
        return this.nbCarriedOutRequests;
    }

    @Override
    public int getNbTotalRequests() {
        return this.nbRequests;
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
            if (this.nbCarriedOutRequests == this.nbCovRequests) {
                byte coverageQuerriesFinished = 1;
                observer.update(coverageQuerriesFinished);
            } else 
            if (this.nbCarriedOutRequests == this.nbRequests) {
                byte mappingQuerriesFinished = 2;
                observer.update(mappingQuerriesFinished);
            }
        }
    }
}
