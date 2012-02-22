package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.api.objects.JobI;
import de.cebitec.vamp.databackend.GenomeRequest;
import de.cebitec.vamp.databackend.MappingThreadAnalyses;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.view.dataVisualisation.DataVisualisationI;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * @author -Rolf Hilker-
 * 
 * Carries out the logic behind the expressed genes analysis.
 */
public class AnalysisExpressedGenes implements ThreadListener, AnalysisI<List<ExpressedGene>>, JobI {

    private final ProgressHandle progressHandle;
    private DataVisualisationI parent;
    private TrackViewer trackViewer;
    private int minNumberReads;
    private int genomeSize;
    private List<PersistantFeature> genomeFeatures;
    private List<ExpressedGene> expressedGenes;
    
    MappingThreadAnalyses mappingThread;
    private int nbRequests;
    private int nbCarriedOutRequests;
    
    private int lastFeatureIdx;
    private int lastMappingIdx;
    private int currentCount;

    /**
     * Carries out the logic behind the expressed genes analysis.
     * When executing the expressed genes analysis the minNumberReads always has
     * to be set, in order to find genes with at least that number of reads.
     * 
     * @param trackViewer the track viewer for which the analyses should be carried out
     * @param minNumberReads minimum number of reads which have to be found within
     *      a gene in order to classify it as an expressed gene
     */
    public AnalysisExpressedGenes(DataVisualisationI parent, TrackViewer trackViewer, int minNumberReads) {
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AnalysisGeneStart.class, "MSG_AnalysesWorker.progress.name"));
        this.parent = parent;
        this.trackViewer = trackViewer;
        this.minNumberReads = minNumberReads;
        
        this.nbCarriedOutRequests = 0;
        this.expressedGenes = new ArrayList<ExpressedGene>();
        this.lastMappingIdx = 0;
        this.lastMappingIdx = 0;
    }
    
    /**
     * Needs to be called in order to start the epxressed genes analysis. Creates the
     * needed database requests and carries them out. The parent has to be a
     * ThreadListener in order to receive the coverage
     * Afterwards the results are returned by {@link getResults()}
     */
    @Override
    public void startAnalysis() {

        this.progressHandle.start();
        TrackConnector trackCon = trackViewer.getTrackCon();
        List<Integer> trackIds = new ArrayList<Integer>();
        trackIds.add(trackCon.getTrackID());
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackViewer.getReference().getId());
        this.genomeSize = refConnector.getRefGen().getSequence().length();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, genomeSize);
        
//        int coveredPerfectPos = trackCon.getCoveredPerfectPos();
        //use for RPKM
        int coveredBestMatchPos = trackCon.getCoveredBestMatchPos();

        for (int trackId : trackIds) {
            
            int stepSize = 200000;
            int from = 1;
            int to = genomeSize > stepSize ? stepSize : genomeSize;
            int additionalRequest = genomeSize % stepSize == 0 ? 0 : 1;
            this.nbRequests = genomeSize / stepSize + additionalRequest;
            this.progressHandle.switchToDeterminate(this.nbRequests);
            
            this.mappingThread = new MappingThreadAnalyses(trackId, this.nbRequests);
            mappingThread.start();
            
            while (to < genomeSize) {
                GenomeRequest mappingRequest = new GenomeRequest(from, to, this);
                mappingThread.addRequest(mappingRequest);
                
                from = to + 1;
                to += stepSize;
            }

            //calc last interval until genomeSize
            to = genomeSize;
            GenomeRequest mappingRequest = new GenomeRequest(from, to, this);
            mappingThread.addRequest(mappingRequest);
        }
    }
    
    
    @Override
    public void receiveData(Object data) {
        this.progressHandle.progress("Request " + (++nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);

        List<PersistantMapping> mappings = (List<PersistantMapping>) data;
        this.detectExpressedGenes(mappings);

        //when the last request is finished signalize the parent to collect the data
        if (nbCarriedOutRequests >= nbRequests) {
            this.parent.showData(true);
            this.nbCarriedOutRequests = 0;
            this.progressHandle.finish();
        }
    }

    
    @Override
    public List<ExpressedGene> getResults() {
        return this.expressedGenes;
    }
    
    /**
     * Carries out the detection of predicted expressed genes.
     * @param mappings the coverage for predicting the gene starts
     */
    public void detectExpressedGenes(List<PersistantMapping> mappings) {
            PersistantFeature feature;
            boolean fstFittingMapping = true;
            boolean nextFeature = false;
            int readCount = 0;
            
            for (int i = this.lastFeatureIdx; i < this.genomeFeatures.size(); ++i) {
                feature = this.genomeFeatures.get(i);
                int featStart = feature.getStart();
                int featStop = feature.getStop();
                nextFeature = false; //false, if the analysis of the current feature is not finished
                fstFittingMapping = true;
                readCount = 0;

                for (int j = this.lastMappingIdx; j < mappings.size(); ++j) {
                    PersistantMapping mapping = mappings.get(j);

                    //mappings identified within a feature
                    if (mapping.getStop() > featStart && feature.getStrand() == mapping.getStrand()
                            && mapping.getStart() < featStop) {

                        if (fstFittingMapping == true) {
                            this.lastMappingIdx = j;
                            fstFittingMapping = false;
                        }
                        this.currentCount += mapping.getNbReplicates();


                        //still mappings left, but need next feature
                    } else if (mapping.getStart() > featStop) {
                        readCount = this.currentCount;
                        nextFeature = true;
                        break;
                    }
                }

                //store last feature index & readcount for next call of receiveData
                //this.currentCount is still set and will be reused during next call
                if (!nextFeature) {
                    this.lastFeatureIdx = i;
                    break;
                }

                //store expressed genes
                if (readCount > this.minNumberReads) {
                    ExpressedGene gene = new ExpressedGene(feature);
                    gene.setReadCount(this.currentCount);
                    this.expressedGenes.add(gene);
                }
                
                this.currentCount = 0;
            }
            
            this.lastMappingIdx = 0;
            //TODO: solution for more than one feature overlapping mapping request boundaries
            
    }

    
    @Override
    public int getNbCarriedOutRequests() {
        return this.nbCarriedOutRequests;
    }

    
    @Override
    public int getNbTotalRequests() {
        return this.nbRequests;
    }
    
    
    
}
