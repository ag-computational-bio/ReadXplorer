package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.api.objects.JobI;
import de.cebitec.vamp.databackend.CoverageThreadAnalyses;
import de.cebitec.vamp.databackend.GenomeRequest;
import de.cebitec.vamp.databackend.MappingThreadAnalyses;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.view.dataVisualisation.DataVisualisationI;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * @author -Rolf Hilker-
 * 
 * Carries out the logic behind the expressed genes analysis.
 */
public class AnalysisExpressedGenes implements ThreadListener, AnalysisI<List<PersistantFeature>>, JobI {

    private final ProgressHandle progressHandle;
    private DataVisualisationI parent;
    private TrackViewer trackViewer;
    private int minNumberReads;
    private int genomeSize;
    private List<PersistantFeature> genomeFeatures;
    private List<PersistantFeature> expressedGenes;
    
    MappingThreadAnalyses mappingThread;
    private int nbRequests;
    private int nbCarriedOutRequests;

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
        this.expressedGenes = new ArrayList<PersistantFeature>();
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
        this.genomeSize = refConnector.getRefGen().getSequence().length(); //TODO: evtl. auslagern?
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, genomeSize);
        
//        this.nbRequests = this.genomeFeatures.size();
//        this.progressHandle.switchToDeterminate(this.nbRequests);

        for (int trackId : trackIds) {
            this.mappingThread = new MappingThreadAnalyses(trackId, this.nbRequests);
            mappingThread.start();
            
            int stepSize = 200000;
            int from = 1;
            int to = genomeSize > stepSize ? stepSize : genomeSize;
            int additionalRequest = genomeSize % stepSize == 0 ? 0 : 1;
            this.nbRequests = genomeSize / stepSize + additionalRequest;
            this.progressHandle.switchToDeterminate(this.nbRequests);
            PersistantFeature feat = this.genomeFeatures.get(0);
            
            while (to < genomeSize) {
                GenomeRequest coverageRequest = new GenomeRequest(from, to, this, feat);
                mappingThread.addRequest(coverageRequest);
                
                from = to + 1;
                to += stepSize;
            }

            //calc last interval until genomeSize
            to = genomeSize;
            GenomeRequest coverageRequest = new GenomeRequest(from, to, this, feat);
            mappingThread.addRequest(coverageRequest);
 
            
//            for (PersistantFeature feature : this.genomeFeatures) {
//                mappingThread.addRequest(new GenomeRequest(feature.getStart(), feature.getStop(), this, feature));
//            }
        }
    }
    
    
    @Override
    public void receiveData(Object data) {
        this.progressHandle.progress("Request " + nbCarriedOutRequests + " of " + nbRequests, ++nbCarriedOutRequests);

        Pair<PersistantFeature, Collection<PersistantMapping>> mappings = (Pair<PersistantFeature, Collection<PersistantMapping>>) data;
        this.detectExpressedGenes(mappings);

        //when the last request is finished signalize the parent to collect the data
        if (nbCarriedOutRequests >= nbRequests) {
            this.parent.showData(true);
            this.nbCarriedOutRequests = 0;
            this.progressHandle.finish();
        }
    }

    
    @Override
    public List<PersistantFeature> getResults() {
        return this.expressedGenes;
    }
    
    /**
     * Carries out the detection of predicted expressed genes.
     * @param mappings the coverage for predicting the gene starts
     */
    public void detectExpressedGenes(Pair<PersistantFeature, Collection<PersistantMapping>> mappings) {
//            PersistantFeature feature;
//            boolean fstFittingFeature = true;
//            
//            for (int i=this.lastFeatureIdxExprGenesFwd; i<this.genomeFeatures.size(); ++i) {
//                feature = this.genomeFeatures.get(i);
//                if (feature.getStrand() == SequenceUtils.STRAND_FWD) {
//                    int start = feature.getStart();
//                    int stop = feature.getStop();
//                    
//                    if (start < mappings.getRightBound() && stop > mappings.getLeftBound()) {
//                        
//                        if (fstFittingFeature == true) {
//                            this.lastFeatureIdxExprGenesFwd = i;
//                            fstFittingFeature = false;
//                        }
//                        
//                        if (start < mappings.getLeftBound()) { // overlaps the left or both (unlikely) bounds of the interval
//                            
//                        } else if (stop < mappings.getRightBound()) { // perfectly fits in interval
//                            
//                        } else { //means: stop > coverage.getRightBound(), overlaps the right bound of the interval
//                            
//                        }
//                    }
//                }
//                
//                //either use coverage estimation or mapping information here!
//            }
        
        //check for features in the given interval and then for mappings belonging to each
        //feature

        int size = mappings.getSecond().size();
        for (PersistantMapping mapping : mappings.getSecond()) {
            size += mapping.getNbReplicates();
        }
        if (size > this.minNumberReads) {
            this.expressedGenes.add(mappings.getFirst());
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
    
    
    
}
