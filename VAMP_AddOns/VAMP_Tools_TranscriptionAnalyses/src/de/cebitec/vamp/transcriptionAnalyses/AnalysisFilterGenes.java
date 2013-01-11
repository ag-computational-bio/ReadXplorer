package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredAnnotation;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Carries out the logic behind the filtered genes analysis.
 * 
 * @author -Rolf Hilker-
 */
public class AnalysisFilterGenes implements Observer, AnalysisI<List<FilteredAnnotation>> {

    private TrackConnector trackConnector;
    private int minNumberReads;
    private int maxNumberReads;
    private int refSeqLength;
    private List<PersistantAnnotation> genomeAnnotations;
    private HashMap<Integer, FilteredAnnotation> annotationReadCount; //annotation id to count of mappings for annotation
    private List<FilteredAnnotation> filteredGenes;
    private List<PersistantMapping> mappingsAll;
    
    private int lastMappingIdx;
    private int currentCount;

    /**
     * Carries out the logic behind the filtered genes analysis.
     * When executing the filtered genes analysis the minNumberReads always has
     * to be set, in order to find genes with at least that number of reads.
     * 
     * @param trackConnector the track viewer for which the analyses should be carried out
     * @param minNumberReads minimum number of reads which have to be found within
     *      a gene in order to classify it as an filtered gene
     * @param maxNumberReads  maximum number of reads which are allowed to be 
     *      found within a gene in order to classify it as an filtered gene
     */
    public AnalysisFilterGenes(TrackConnector trackConnector, int minNumberReads, int maxNumberReads) {
        this.trackConnector = trackConnector;
        this.minNumberReads = minNumberReads;
        this.maxNumberReads = maxNumberReads;
        
        this.filteredGenes = new ArrayList<>();
        this.mappingsAll = new ArrayList<>();
        this.annotationReadCount = new HashMap<>();
        this.lastMappingIdx = 0;
        this.lastMappingIdx = 0;
        
        this.initDatastructures();
    }
    
    /**
     * Initializes the initial data structures needed for filtering annotations by read count.
     */
    private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.refSeqLength = this.trackConnector.getRefSequenceLength();
        this.genomeAnnotations = refConnector.getAnnotationsForClosedInterval(0, refSeqLength);
        
        for (PersistantAnnotation annotation : this.genomeAnnotations) {
            this.annotationReadCount.put(annotation.getId(), new FilteredAnnotation(annotation));
        }
        
        //        int coveredPerfectPos = trackCon.getCoveredPerfectPos();
        //use for RPKM in other analyses
//        int coveredBestMatchPos = trackCon.getCoveredBestMatchPos();
//        int totalExonModelLength = 0; //calculate the total length of the transcriptome
//        for (PersistantAnnotation annotation : this.genomeAnnotations) {
//            totalExonModelLength += annotation.getStop() - annotation.getStart();
//        }
//        totalExonModelLength /= 1000;
    }
    
    /**
     * Updates the read count for a new list of mappings or calls the findFilteredGenes method.
     * @param data the data to handle: Either a list of mappings or "2" = mapping querries are done.
     */
    @Override
    public void update(Object data) {
        MappingResultPersistant mappingResult = new MappingResultPersistant(null, 0, 0);
        
        if (data.getClass() == mappingResult.getClass()) {
            List<PersistantMapping> mappings = ((MappingResultPersistant) data).getMappings();
            this.updateReadCountForAnnotations(mappings);
        } else
        if (data instanceof Byte && ((Byte) data) == 2) { //2 means mapping analysis is finished
            this.findFilteredGenes();
        }
    }
    
    /**
     * Updates the read count for the annotations with the given mappings.
     * @param mappings the mappings
     */
    public void updateReadCountForAnnotations(List<PersistantMapping> mappings) {
            PersistantAnnotation annotation;
            boolean fstFittingMapping;
            
            for (int i = 0; i < this.genomeAnnotations.size(); ++i) {
                annotation = this.genomeAnnotations.get(i);
                int featStart = annotation.getStart();
                int featStop = annotation.getStop();
                fstFittingMapping = true;

                for (int j = this.lastMappingIdx; j < mappings.size(); ++j) {
                    PersistantMapping mapping = mappings.get(j);

                    //mappings identified within a annotation
                    if (mapping.getStop() > featStart && annotation.isFwdStrand() == mapping.isFwdStrand()
                            && mapping.getStart() < featStop) {

                        if (fstFittingMapping == true) {
                            this.lastMappingIdx = j;
                            fstFittingMapping = false;
                        }
                        this.currentCount += mapping.getNbReplicates();


                        //still mappings left, but need next annotation
                    } else if (mapping.getStart() > featStop) {
                        break;
                    }
                }

                //store filtered genes
                this.annotationReadCount.get(annotation.getId()).setReadCount(this.annotationReadCount.get(annotation.getId()).getReadCount() + this.currentCount);
                this.currentCount = 0;
            }
            
            this.lastMappingIdx = 0;
            //TODO: solution for more than one annotation overlapping mapping request boundaries
            
    }

    /**
     * Detects all annotations, which satisfy the given minimum number of reads and
     * do not exceed the maximum number of reads and stores them in the FilteredGenes
     * data structure.
     */
    private void findFilteredGenes() {
        int readCount;
        for (Integer id : this.annotationReadCount.keySet()) {
            readCount = this.annotationReadCount.get(id).getReadCount();
            if (readCount > this.minNumberReads && readCount < this.maxNumberReads) {
                this.filteredGenes.add(this.annotationReadCount.get(id));
            }
        }
    }
    
    @Override
    public List<FilteredAnnotation> getResults() {
        return this.filteredGenes;
    }

    /**
     * Sorts the mappings by start position.
     * @param mappingsAll mapping list to sort by start position
     */
    private void sortMappings(List<PersistantMapping> mappingsAll) {
        HashMap<Integer, ArrayList<PersistantMapping>> mappingsToPos = new HashMap<>();
        int start;
        
        for (PersistantMapping mapping : mappingsAll) {
            start = mapping.getStart();
            if (!mappingsToPos.containsKey(start)) {
                mappingsToPos.put(start, new ArrayList<PersistantMapping>());
            }
            mappingsToPos.get(start).add(mapping);
        }
        
        this.mappingsAll.clear();
        for (int i = 0; i < this.refSeqLength; ++i) {
            List<PersistantMapping> mappingsAtPos = mappingsToPos.get(i);
            if (mappingsAtPos != null) {
                this.mappingsAll.addAll(mappingsAtPos);
            }
        }
    }
    
    
}
