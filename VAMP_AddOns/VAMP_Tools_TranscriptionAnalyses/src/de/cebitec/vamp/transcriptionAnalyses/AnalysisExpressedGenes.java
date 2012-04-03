package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.dataStructures.ExpressedGene;
import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.api.objects.JobI;
import de.cebitec.vamp.databackend.GenomeRequest;
import de.cebitec.vamp.databackend.MappingThreadAnalyses;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.DataVisualisationI;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.ArrayList;
import java.util.HashMap;
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
    private int maxNumberReads;
    private int genomeSize;
    private List<PersistantAnnotation> genomeAnnotations;
    private HashMap<Integer, ExpressedGene> annotationReadCount; //annotation id to count of mappings for annotation
    private List<ExpressedGene> expressedGenes;
    private List<PersistantMapping> mappingsAll;
    
    MappingThreadAnalyses mappingThread;
    private int nbRequests;
    private int nbCarriedOutRequests;
    
    private int lastAnnotationIdx;
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
    public AnalysisExpressedGenes(DataVisualisationI parent, TrackViewer trackViewer, int minNumberReads, int maxNumberReads) {
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AnalysisGeneStart.class, "MSG_AnalysesWorker.progress.name"));
        this.parent = parent;
        this.trackViewer = trackViewer;
        this.minNumberReads = minNumberReads;
        this.maxNumberReads = maxNumberReads;
        
        this.nbCarriedOutRequests = 0;
        this.expressedGenes = new ArrayList<ExpressedGene>();
        this.mappingsAll = new ArrayList<PersistantMapping>();
        this.annotationReadCount = new HashMap<Integer, ExpressedGene>();
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
        this.genomeAnnotations = refConnector.getAnnotationsForClosedInterval(0, genomeSize);
        int numMappingsTotal = 0;
        int numUnneededMappings = 0;
        List<PersistantTrack> tracksAll = ProjectConnector.getInstance().getTracks();
        for (PersistantTrack track : tracksAll) {
            TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(track);
            numMappingsTotal += connector.getNumOfUniqueMappings();
            if (track.getId() < trackCon.getTrackID()) {
                numUnneededMappings += connector.getNumOfUniqueMappings();
            }
        }
        int numInterestingMappings = numUnneededMappings + trackCon.getNumOfUniqueMappings();
        
        for (PersistantAnnotation annotation : this.genomeAnnotations) {
            this.annotationReadCount.put(annotation.getId(), new ExpressedGene(annotation));
        }
        
        
//        int coveredPerfectPos = trackCon.getCoveredPerfectPos();
        //use for RPKM
        int coveredBestMatchPos = trackCon.getCoveredBestMatchPos();
        int totalExonModelLength = 0; //calculate the total length of the transcriptome
        for (PersistantAnnotation annotation : this.genomeAnnotations) {
            totalExonModelLength += annotation.getStop() - annotation.getStart();
        }
        totalExonModelLength /= 1000;

        for (int trackId : trackIds) {
            
            int stepSize = 50000;
            int from = numUnneededMappings;
            int to = numInterestingMappings - numUnneededMappings > stepSize ? numUnneededMappings + stepSize : numInterestingMappings;
            int additionalRequest = numInterestingMappings % stepSize == 0 ? 0 : 1;
            this.nbRequests = (numInterestingMappings - numUnneededMappings) / stepSize + additionalRequest; 
            this.progressHandle.switchToDeterminate(this.nbRequests + 1); //+ 1 for subsequent calculations
            this.progressHandle.progress("Request " + (nbCarriedOutRequests) + " of " + nbRequests, nbCarriedOutRequests);
            
            this.mappingThread = new MappingThreadAnalyses(trackId, this.nbRequests);
            this.mappingThread.start();
            
            while (to < numInterestingMappings) {
                GenomeRequest mappingRequest = new GenomeRequest(from, to, this);
                this.mappingThread.addRequest(mappingRequest);
                
                from = to + 1;
                to += stepSize;
            }

            //calc last interval until genomeSize
            to = numInterestingMappings;
            GenomeRequest mappingRequest = new GenomeRequest(from, to, this);
            this.mappingThread.addRequest(mappingRequest);
        }
    }
    
    
    @Override
    public void receiveData(Object data) {
        this.progressHandle.progress("Request " + (++nbCarriedOutRequests) + " of " + nbRequests, nbCarriedOutRequests);

        List<PersistantMapping> mappings = new ArrayList<PersistantMapping>();
        
        if (data.getClass() == mappings.getClass()) {
            mappings = (List<PersistantMapping>) data;
            this.detectExpressedGenes(mappings);
//            this.mappingsAll.addAll(mappings);

            //when the last request is finished signalize the parent to collect the data
            if (nbCarriedOutRequests >= nbRequests - 1) {
//                this.sortMappings(this.mappingsAll);
                this.findExpressedGenes();
                this.parent.showData(true);
                this.nbCarriedOutRequests = 0;
                this.progressHandle.finish();
            }
        }
    }

    
    @Override
    public List<ExpressedGene> getResults() {
        return this.expressedGenes;
    }
    
//    /**
//     * Carries out the detection of predicted expressed genes.
//     * @param mappings the coverage for predicting the gene starts
//     */
//    public void detectExpressedGenes(List<PersistantMapping> mappings) {
//            PersistantAnnotation annotation;
//            boolean fstFittingMapping = true;
//            boolean nextFeature = false;
//            int readCount = 0;
//            
//            for (int i = this.lastFeatureIdx; i < this.genomeFeatures.size(); ++i) {
//                annotation = this.genomeFeatures.get(i);
//                int featStart = annotation.getStart();
//                int featStop = annotation.getStop();
//                nextFeature = false; //false, if the analysis of the current annotation is not finished
//                fstFittingMapping = true;
//                readCount = 0;
//
//                for (int j = this.lastMappingIdx; j < mappings.size(); ++j) {
//                    PersistantMapping mapping = mappings.get(j);
//
//                    //mappings identified within a annotation
//                    if (mapping.getStop() > featStart && annotation.getStrand() == mapping.getStrand()
//                            && mapping.getStart() < featStop) {
//
//                        if (fstFittingMapping == true) {
//                            this.lastMappingIdx = j;
//                            fstFittingMapping = false;
//                        }
//                        this.currentCount += mapping.getNbReplicates();
//
//
//                        //still mappings left, but need next annotation
//                    } else if (mapping.getStart() > featStop) {
//                        readCount = this.currentCount;
//                        nextFeature = true;
//                        break;
//                    }
//                }
//
//                //store last annotation index & readcount for next call of receiveData
//                //this.currentCount is still set and will be reused during next call
//                if (!nextFeature) {
//                    this.lastFeatureIdx = i;
//                    break;
//                }
//
//                //store expressed genes
//                if (readCount > this.minNumberReads) {
//                    ExpressedGene gene = new ExpressedGene(annotation);
//                    gene.setReadCount(this.currentCount);
//                    this.expressedGenes.add(gene);
//                }
//                
//                this.currentCount = 0;
//            }
//            
//            this.lastMappingIdx = 0;
//            //TODO: solution for more than one annotation overlapping mapping request boundaries
//            
//    }
    
        /**
     * Carries out the detection of predicted expressed genes.
     * @param mappings the coverage for predicting the gene starts
     */
    public void detectExpressedGenes(List<PersistantMapping> mappings) {
            PersistantAnnotation annotation;
            boolean fstFittingMapping = true;
            boolean nextAnnotation = false;
            int readCount = 0;
            
            for (int i = 0; i < this.genomeAnnotations.size(); ++i) {
                annotation = this.genomeAnnotations.get(i);
                int featStart = annotation.getStart();
                int featStop = annotation.getStop();
                nextAnnotation = false; //false, if the analysis of the current annotation is not finished
                fstFittingMapping = true;
                readCount = 0;

                for (int j = this.lastMappingIdx; j < mappings.size(); ++j) {
                    PersistantMapping mapping = mappings.get(j);

                    //mappings identified within a annotation
                    if (mapping.getStop() > featStart && annotation.getStrand() == mapping.getStrand()
                            && mapping.getStart() < featStop) {

                        if (fstFittingMapping == true) {
                            this.lastMappingIdx = j;
                            fstFittingMapping = false;
                        }
                        this.currentCount += mapping.getNbReplicates();


                        //still mappings left, but need next annotation
                    } else if (mapping.getStart() > featStop) {
                        readCount = this.currentCount;
                        nextAnnotation = true;
                        break;
                    }
                }

                //store last annotation index & readcount for next call of receiveData
                //this.currentCount is still set and will be reused during next call
                if (!nextAnnotation) {
                    this.lastAnnotationIdx = i;
                    break;
                }

                //store expressed genes
                //TODO initialize this 
//                if (readCount > this.minNumberReads) {
//                    ExpressedGene gene = new ExpressedGene(annotation);
                    this.annotationReadCount.get(annotation.getId()).setReadCount(this.annotationReadCount.get(annotation.getId()).getReadCount() + this.currentCount);
//                    this.expressedGenes.add(gene);
//                }
                
                this.currentCount = 0;
            }
            
            this.lastMappingIdx = 0;
            //TODO: solution for more than one annotation overlapping mapping request boundaries
            
    }

    
    @Override
    public int getNbCarriedOutRequests() {
        return this.nbCarriedOutRequests;
    }

    
    @Override
    public int getNbTotalRequests() {
        return this.nbRequests;
    }

    /**
     * Sorts the mappings by start position.
     * @param mappingsAll mapping list to sort by start position
     */
    private void sortMappings(List<PersistantMapping> mappingsAll) {
        HashMap<Integer, ArrayList<PersistantMapping>> mappingsToPos = new HashMap<Integer, ArrayList<PersistantMapping>>();
        int start;
        
        for (PersistantMapping mapping : mappingsAll) {
            start = mapping.getStart();
            if (!mappingsToPos.containsKey(start)) {
                mappingsToPos.put(start, new ArrayList<PersistantMapping>());
            }
            mappingsToPos.get(start).add(mapping);
        }
        
        this.mappingsAll.clear();
        for (int i = 0; i < this.genomeSize; ++i) {
            List<PersistantMapping> mappingsAtPos = mappingsToPos.get(i);
            if (mappingsAtPos != null) {
                this.mappingsAll.addAll(mappingsAtPos);
            }
        }
    }

    private void findExpressedGenes() {
        int readCount;
        for (Integer id : this.annotationReadCount.keySet()) {
            readCount = this.annotationReadCount.get(id).getReadCount();
            if (readCount > this.minNumberReads && readCount < this.maxNumberReads) {
                this.expressedGenes.add(this.annotationReadCount.get(id));
            }
        }
    }
    
    
}
