package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredFeature;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.RPKMvalue;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Martin Tötsches
 */
public class AnalysisRPKM implements Observer, AnalysisI<List<RPKMvalue>> {
    
    private TrackConnector trackConnector;
    private List<RPKMvalue> rpkmValues;
    private List<PersistantMapping> mappingsAll;
    private int refSeqLength;
    private List<PersistantFeature> genomeFeatures;
    private HashMap<Integer, FilteredFeature> featureReadCount;
    private List<FilteredFeature> filteredFeatures;
    private int totalExonReads = 0;
    private int minNumberReads;
    private int maxNumberReads;
    
    private int lastMappingIdx;
    private int currentCount;
    
    public AnalysisRPKM(TrackConnector trackConnector, int minNumberReads, int maxNumberReads) {
        this.trackConnector = trackConnector;
        this.filteredFeatures = new ArrayList<>();
        this.mappingsAll = new ArrayList<>();
        this.rpkmValues = new ArrayList<>();
        this.featureReadCount = new HashMap<>();
        this.lastMappingIdx = 0;
        this.minNumberReads = minNumberReads;
        this.maxNumberReads = maxNumberReads;
        this.initDatastructures();
    }
    
     private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.refSeqLength = this.trackConnector.getRefSequenceLength();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, refSeqLength);
        
        for (PersistantFeature feature : this.genomeFeatures) {
            this.featureReadCount.put(feature.getId(), new FilteredFeature(feature,0));
        }
        
        int coveredPerfectPos = trackConnector.getNumOfPerfectUniqueMappingsCalculate();
        //use for RPKM in other analyses
        int coveredBestMatchPos = trackConnector.getNumOfUniqueMappingsCalculate();
        //int totalExonReadLength = 0; //calculate the total length of the reads of the transcriptome
        /*for (PersistantFeature feature : this.genomeFeatures) {
            this.totalExonModelLength += feature.getStop() - feature.getStart();
        }
        this.totalExonModelLength /= 1000;*/
    }
    
    @Override
    public void update(Object data) {
         MappingResultPersistant mappingResult = new MappingResultPersistant(null, 0, 0);
        
        if (data.getClass() == mappingResult.getClass()) {
            List<PersistantMapping> mappings = ((MappingResultPersistant) data).getMappings();
            this.updateReadCountForFeatures(mappings);
        } else
        if (data instanceof Byte && ((Byte) data) == 2) { //2 means mapping analysis is finished
            this.calculateRPKMvalues();
            for (RPKMvalue rpkm : rpkmValues) {
                if (rpkm.getRpkm() != 0) {
                System.out.println("Feature: " + rpkm.getFeature().getFeatureName());
                System.out.println("RPKM: " + rpkm.getRpkm());
                }
            }
        }
    }

    @Override
    public List<RPKMvalue> getResults() {
        return this.rpkmValues;
    }
    
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
                this.totalExonReads += this.featureReadCount.get(feature.getId()).getReadCount();
                this.currentCount = 0;
            }
            //TODO: solution for more than one feature overlapping mapping request boundaries
    }
    
    public void calculateRPKMvalues() {
        for (Integer id : this.featureReadCount.keySet()) {
            System.out.println("READCOUNT: " + this.featureReadCount.get(id).getReadCount());
            System.out.println("MIN: " + this.minNumberReads);
            System.out.println("MAX: " + this.maxNumberReads);
            if (this.featureReadCount.get(id).getReadCount() >= this.minNumberReads && this.featureReadCount.get(id).getReadCount() <= this.maxNumberReads) {
                PersistantFeature feature = this.featureReadCount.get(id).getFilteredFeature();
                int start = this.featureReadCount.get(id).getFilteredFeature().getStart();
                int stop = this.featureReadCount.get(id).getFilteredFeature().getStop();
                double exonLength = (double) (((double) stop - (double) start)/ (double) 1000);
                System.out.println("EL: " + exonLength);
                double mappedReads = (double) (this.featureReadCount.get(id).getReadCount());
                System.out.println("MR: " + mappedReads);
                System.out.println("TL: " + this.totalExonReads);
                double rpkm = 0;
                if (mappedReads != 0) {
                    rpkm = ((double) (this.totalExonReads / 1000)) / (mappedReads * exonLength);
                }
                this.rpkmValues.add(new RPKMvalue(feature, rpkm));
            }
        }
    }
}
