package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredFeature;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Carries out the logic behind the filtered features analysis.
 * 
 * @author -Rolf Hilker-
 */
public class AnalysisFilterGenes implements Observer, AnalysisI<List<FilteredFeature>> {

    private TrackConnector trackConnector;
    private int minNumberReads;
    private int maxNumberReads;
    private int refSeqLength;
    private List<PersistantFeature> genomeFeatures;
    private HashMap<Integer, FilteredFeature> featureReadCount; //feature id to count of mappings for feature
    private List<FilteredFeature> filteredFeatures;
    private List<PersistantMapping> mappingsAll;
    
    private int lastMappingIdx;
    private int currentCount;

    /**
     * Carries out the logic behind the filtered features analysis.
     * 
     * @param trackConnector the track viewer for which the analyses should be carried out
     * @param minNumberReads minimum number of reads which have to be found within
     *      a feature in order to classify it as an filtered feature
     * @param maxNumberReads  maximum number of reads which are allowed to be 
     *      found within a feature in order to classify it as an filtered feature
     */
    public AnalysisFilterGenes(TrackConnector trackConnector, int minNumberReads, int maxNumberReads) {
        this.trackConnector = trackConnector;
        this.minNumberReads = minNumberReads;
        this.maxNumberReads = maxNumberReads;
        
        this.filteredFeatures = new ArrayList<>();
        this.mappingsAll = new ArrayList<>();
        this.featureReadCount = new HashMap<>();
        this.lastMappingIdx = 0;
        this.lastMappingIdx = 0;
        
        this.initDatastructures();
    }
    
    /**
     * Initializes the initial data structures needed for filtering features by read count.
     */
    private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.refSeqLength = this.trackConnector.getRefSequenceLength();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, refSeqLength);
        
        for (PersistantFeature feature : this.genomeFeatures) {
            this.featureReadCount.put(feature.getId(), new FilteredFeature(feature));
        }
        
        //        int coveredPerfectPos = trackCon.getCoveredPerfectPos();
        //use for RPKM in other analyses
//        int coveredBestMatchPos = trackCon.getCoveredBestMatchPos();
//        int totalExonModelLength = 0; //calculate the total length of the transcriptome
//        for (PersistantFeature feature : this.genomeFeatures) {
//            totalExonModelLength += feature.getStop() - feature.getStart();
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
            this.updateReadCountForFeatures(mappings);
        } else
        if (data instanceof Byte && ((Byte) data) == 2) { //2 means mapping analysis is finished
            this.findFilteredFeatures();
        }
    }
    
    /**
     * Updates the read count for the features with the given mappings.
     * @param mappings the mappings
     */
    public void updateReadCountForFeatures(List<PersistantMapping> mappings) {
            PersistantFeature feature;
            boolean fstFittingMapping;
            
            for (int i = 0; i < this.genomeFeatures.size(); ++i) {
                feature = this.genomeFeatures.get(i);
                int featStart = feature.getStart();
                int featStop = feature.getStop();
                fstFittingMapping = true;

                for (int j = this.lastMappingIdx; j < mappings.size(); ++j) {
                    PersistantMapping mapping = mappings.get(j);

                    //mappings identified within a feature
                    if (mapping.getStop() > featStart && feature.isFwdStrand() == mapping.isFwdStrand()
                            && mapping.getStart() < featStop) {

                        if (fstFittingMapping == true) {
                            this.lastMappingIdx = j;
                            fstFittingMapping = false;
                        }
                        this.currentCount += mapping.getNbReplicates();


                        //still mappings left, but need next feature
                    } else if (mapping.getStart() > featStop) {
                        break;
                    }
                }

                //store filtered features
                this.featureReadCount.get(feature.getId()).setReadCount(this.featureReadCount.get(feature.getId()).getReadCount() + this.currentCount);
                this.currentCount = 0;
            }
            
            this.lastMappingIdx = 0;
            //TODO: solution for more than one feature overlapping mapping request boundaries
            
    }

    /**
     * Detects all features, which satisfy the given minimum number of reads and
     * do not exceed the maximum number of reads and stores them in the FilteredFeatures
     * data structure.
     */
    private void findFilteredFeatures() {
        int readCount;
        for (Integer id : this.featureReadCount.keySet()) {
            readCount = this.featureReadCount.get(id).getReadCount();
            if (readCount > this.minNumberReads && readCount < this.maxNumberReads) {
                this.filteredFeatures.add(this.featureReadCount.get(id));
            }
        }
    }
    
    @Override
    public List<FilteredFeature> getResults() {
        return this.filteredFeatures;
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
