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
import de.cebitec.vamp.util.StatsContainer;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 * Class for handling the data threads for one of the currently started
 * analyses. CAUTION: You cannot query coverage and mapping tables with the same
 * AnalysisHandler at the same time. You have to use two separate handlers.
 */
public class AnalysesHandler implements ThreadListener, Observable, JobI {

    public static final String DATA_TYPE_COVERAGE = "Coverage";
    public static final String DATA_TYPE_MAPPINGS = "Mappings";
    public static final byte COVERAGE_QUERRIES_FINISHED = 1;
    public static final byte MAPPING_QUERRIES_FINISHED = 2;
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
    private byte desiredData = Properties.NORMAL;
    private ParametersReadClasses readClassParams;

    /**
     * Creates a new analysis handler, ready for extracting mapping and coverage
     * information for the given track. CAUTION: You cannot query coverage and
     * mapping tables with the same AnalysisHandler at the same time. You have
     * to use two separate handlers.
     * @param trackConnector the track connector for which the analyses are
     * carried out
     * @param parent the parent for visualization of the results
     * @param handlerTitle title of the analysis handler
     * @param readClassParams The parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads.
     */
    public AnalysesHandler(TrackConnector trackConnector, DataVisualisationI parent, 
            String handlerTitle, ParametersReadClasses readClassParams) {
        this.progressHandle = ProgressHandleFactory.createHandle(handlerTitle);
        this.observers = new ArrayList<>();
        this.parent = parent;
        this.trackConnector = trackConnector;
        this.coverageNeeded = false;
        this.mappingsNeeded = false;
        this.nbCovRequests = 0;
        this.nbMappingRequests = 0;
        this.readClassParams = readClassParams;
    }

    /**
     * Needs to be called in order to start the transcription analyses. Creates
     * the needed database requests and carries them out. The parent has to be a
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
                trackConnector.addCoverageAnalysisRequest(new IntervalRequest(from, to, this, readClassParams));

                from = to + 1;
                to += stepSize;
            }

            //calc last interval until genomeSize
            to = this.refSeqLength;
            trackConnector.addCoverageAnalysisRequest(new IntervalRequest(from, to, this, readClassParams));


        } else if (this.mappingsNeeded) {

            int stepSize = 50000;

            if (trackConnector.isDbUsed()) {
                //calculate which mappings are needed from the db
                int numUnneededMappings = 0;
                List<PersistantTrack> tracksAll = ProjectConnector.getInstance().getTracks();
                for (PersistantTrack track : tracksAll) {
                    TrackConnector connector;
                    try {
                        connector = ProjectConnector.getInstance().getTrackConnector(track);
                    } catch (FileNotFoundException ex) {
                        //This can only happen is SamBam files are used but in this
                        //case we are in DbUsed mode. This means this Exception will
                        //never be thrown.
                        return;
                    }
                    if (track.getId() < trackConnector.getTrackID()) {
                        numUnneededMappings += connector.getTrackStats().getStatsMap().get(StatsContainer.NO_UNIQ_MAPPINGS);
                    } else {
                        break;
                    }
                }
                int numInterestingMappings = numUnneededMappings + trackConnector.getTrackStats().getStatsMap().get(StatsContainer.NO_UNIQ_MAPPINGS);
                int from = numUnneededMappings;
                int to = numInterestingMappings - numUnneededMappings > stepSize
                        ? numUnneededMappings + stepSize : numInterestingMappings;

                int additionalRequest = numInterestingMappings % stepSize == 0 ? 0 : 1;
                this.nbMappingRequests = (numInterestingMappings - numUnneededMappings) / stepSize + additionalRequest;

                this.nbRequests += this.nbMappingRequests;
                this.progressHandle.switchToDeterminate(this.nbRequests);
                this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);

                while (to < numInterestingMappings) {
                    trackConnector.addMappingAnalysisRequest(new IntervalRequest(from, to, this, desiredData, readClassParams));
                    from = to + 1;
                    to += stepSize;
                }

                //calc last interval until genomeSize
                to = numInterestingMappings;
                trackConnector.addMappingAnalysisRequest(new IntervalRequest(from, to, this, desiredData, readClassParams));
            } else {
                this.desiredData = Properties.REDUCED_MAPPINGS == desiredData ? desiredData : Properties.MAPPINGS_WO_DIFFS;
                this.nbRequests = this.refSeqLength / stepSize + 1;
                this.progressHandle.switchToDeterminate(this.nbRequests);
                this.progressHandle.progress("Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);
                int from = 0;
                int to = stepSize;
                while (to < this.refSeqLength) {
                    trackConnector.addMappingAnalysisRequest(new IntervalRequest(from, to, this, desiredData, readClassParams));
                    from = to + 1;
                    to += stepSize;
                }

                //calc last interval until genomeSize
                to = this.refSeqLength;
                trackConnector.addMappingAnalysisRequest(new IntervalRequest(from, to, this, desiredData, readClassParams));
            }
        } else {
            this.progressHandle.finish();
        }
    }

    @Override
    public void receiveData(Object data) {
        this.progressHandle.progress(this.queryType + " request "
                + (nbCarriedOutRequests + 1) + " of " + nbRequests, ++nbCarriedOutRequests);
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
     *
     * @param coverageNeeded True, if the analysis works with coverage, false
     * otherwise.
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
     * @param mappingsNeeded True, if the analysis works with mappings, false
     * otherwise.
     */
    public void setMappingsNeeded(boolean mappingsNeeded) {
        this.mappingsNeeded = mappingsNeeded;
    }

    /**
     * Sets the desired data for this analysis handler to 
     * Properties.REDUCED_MAPPINGS or back to Properties.NORMAL.
     * @param reducedMappingsNeeded true, if only reduced mappings are needed,
     * false otherwise
     */
    public void setReducedMappingsNeeded(boolean reducedMappingsNeeded) {
        if (reducedMappingsNeeded) {
            this.desiredData = Properties.REDUCED_MAPPINGS;
        } else {
            this.desiredData = Properties.NORMAL;
        }
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
                observer.update(COVERAGE_QUERRIES_FINISHED);
            } else if (this.nbCarriedOutRequests == this.nbRequests) {
                observer.update(MAPPING_QUERRIES_FINISHED);
            }
        }
    }

    @Override
    public void notifySkipped() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
