package de.cebitec.vamp.databackend;

import de.cebitec.vamp.api.objects.JobI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * Class for handling the data threads for one of the currently started analyses.
 * CAUTION: You cannot query coverage and mapping tables with the same AnalysisHandler at
 * the same time. You have to use two separate handlers.
 */
public class AnalysesHandler implements ThreadListener, Observable, JobI {
    
    public static final String DATA_TYPE_COVERAGE = "Coverage";
    public static final String DATA_TYPE_MAPPINGS = "Mappings";
    
    private final ProgressHandle progressHandle;
    private DataVisualisationI parent;
    private TrackConnector trackConnector;
    private int refSeqLength;
    
    private int nbCovRequests;
    private int nbMappingRequests;
    private int nbRequests;
    private int nbCarriedOutRequests;
    private String queryType;
    
    private ArrayList<Observer> observers;
    private boolean coverageNeeded;
    private boolean mappingsNeeded;
    
    /**
     * Creates a new analysis handler, ready for extracting mapping and 
     * coverage information for the given track.
     * CAUTION: You cannot query coverage and mapping tables with the same AnalysisHandler at
     * the same time. You have to use two separate handlers.
     * @param trackConnector the track connector for which the analyses are carried out
     * @param parent the parent for visualization of the results
     */
    public AnalysesHandler (TrackConnector trackConnector, DataVisualisationI parent) {
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AnalysesHandler.class, "MSG_AnalysesWorker.progress.name"));
        this.observers = new ArrayList<>();
        this.parent = parent;
        this.trackConnector = trackConnector;
        this.coverageNeeded = false;
        this.mappingsNeeded = false;
        this.nbCovRequests = 0;
        this.nbMappingRequests = 0;
        
    }
    
    /**
     * Needs to be called in order to start the transcription analyses. Creates the
     * needed database requests and carries them out. The parent has to be a
     * ThreadListener in order to receive the coverage or mapping data.
     * Afterwards the results are returned to the observers of this analyses 
     * handler by the {@link receiveData()} method.
     */
    public void startAnalysis() {

        this.queryType = this.coverageNeeded ? DATA_TYPE_COVERAGE : DATA_TYPE_MAPPINGS;
        this.nbRequests = 0;
        this.progressHandle.start();
        
        this.refSeqLength = trackConnector.getRefSequenceLength();

        if (this.coverageNeeded) {

            //decide upon stepSize of a single request and analyse coverage of whole genome
            final int stepSize = 200000;
            int from = 1;
            int to = this.refSeqLength > stepSize ? stepSize : this.refSeqLength;
            int additionalRequest = this.refSeqLength % stepSize == 0 ? 0 : 1;
            this.nbCovRequests = this.refSeqLength / stepSize + additionalRequest;
            this.nbRequests += this.nbCovRequests;
            this.progressHandle.switchToDeterminate(this.nbRequests);
            this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);

            while (to < this.refSeqLength) {
                IntervalRequest coverageRequest = new IntervalRequest(from, to, this, Properties.BEST_MATCH_COVERAGE);
                trackConnector.addCoverageAnalysisRequest(coverageRequest);
                
                from = to + 1;
                to += stepSize;
            }

            //calc last interval until genomeSize
            to = this.refSeqLength;
            trackConnector.addCoverageAnalysisRequest(new IntervalRequest(from, to, this, Properties.BEST_MATCH_COVERAGE));
        
            
        } else if (this.mappingsNeeded) {
            
            int stepSize = 50000;
            
            if (trackConnector.isDbUsed()) {
                //calculate which mappings are needed from the db
                int numUnneededMappings = 0;
                List<PersistantTrack> tracksAll = ProjectConnector.getInstance().getTracks();
                for (PersistantTrack track : tracksAll) {
                    TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(track);
                    if (track.getId() < trackConnector.getTrackID()) {
                        numUnneededMappings += connector.getNumOfUniqueMappings();
                    }
                }
                int numInterestingMappings = numUnneededMappings + trackConnector.getNumOfUniqueMappings();
                int from = numUnneededMappings;
                int to = numInterestingMappings - numUnneededMappings > stepSize
                        ? numUnneededMappings + stepSize : numInterestingMappings;

                int additionalRequest = numInterestingMappings % stepSize == 0 ? 0 : 1;
                this.nbMappingRequests = (numInterestingMappings - numUnneededMappings) / stepSize + additionalRequest;

                this.nbRequests += this.nbMappingRequests;
                this.progressHandle.switchToDeterminate(this.nbRequests);
                this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);

                while (to < numInterestingMappings) {
                    trackConnector.addMappingAnalysisRequest(new IntervalRequest(from, to, this));

                    from = to + 1;
                    to += stepSize;
                }

                //calc last interval until genomeSize
                to = numInterestingMappings;
                IntervalRequest mappingRequest = new IntervalRequest(from, to, this);
                trackConnector.addMappingAnalysisRequest(mappingRequest);

            } else {
                this.nbRequests = this.refSeqLength / stepSize + 1;
                this.progressHandle.switchToDeterminate(this.nbRequests);
                this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);
                int from = 0;
                int to = stepSize;
                while (to < this.refSeqLength) {
                    trackConnector.addMappingAnalysisRequest(new IntervalRequest(from, to, this, Properties.MAPPINGS_WO_DIFFS));

                    from = to + 1;
                    to += stepSize;
                }
                
                //calc last interval until genomeSize
                to = this.refSeqLength;
                trackConnector.addMappingAnalysisRequest(new IntervalRequest(from, to, this, Properties.MAPPINGS_WO_DIFFS));
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
            this.parent.showData(new Pair<>(trackConnector.getTrackID(), dataType));
            this.nbCarriedOutRequests = 0;
            this.progressHandle.finish();
        }
    }

    /**
     * @return True, if the analysis works with coverage, false otherwise.
     */
    public boolean isCoverageNeeded() {
        return this.coverageNeeded;
    }

    /**
     * Set before an anylsis is is started. True, if the analysis works with
     * coverage, false otherwise. By default it is false.
     * @param coverageNeeded True, if the analysis works with
     * coverage, false otherwise.
     */
    public void setCoverageNeeded(boolean coverageNeeded) {
        this.coverageNeeded = coverageNeeded;
    }
    
    /**
     * @return True, if the analysis works with mappings, false otherwise.
     */
    public boolean isMappingsNeeded() {
        return this.mappingsNeeded;
    }

    /**
     * Set before an anylsis is is started. True, if the analysis works with
     * mappings, false otherwise. By default it is false.
     * @param mappingsNeeded True, if the analysis works with
     * mappings, false otherwise.
     */
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
