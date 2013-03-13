package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Observer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carries out the analysis of a data set for operons.
 *
 * @author MKD, rhilker
 */
public class AnalysisOperon implements Observer, AnalysisI<List<Operon>> {

    private TrackConnector trackConnector;
    private int minNumberReads;
    private int genomeSize;
    private List<PersistantFeature> genomeFeatures;
    private List<Operon> operonList; //final result list of OperonAdjacencies
    private boolean operonDetectionAutomatic;
    private HashMap<Integer, OperonAdjacency> featureToPutativeOperonMap; //feature id of mappings to count for features
    private List<OperonAdjacency> operonAdjacencies; 
    private int averageReadLength = 0;
    private int averageSeqPairLength = 0;
    private int lastMappingIdx;
    private int readsFeature1 = 0;
    private int spanningReads = 0;
    private int readsFeature2 = 0;
    private int internalReads = 0;

    /**
     * Carries out the analysis of a data set for operons.
     * @param trackConnector the trackConnector whose data is to be analyzed
     * @param minNumberReads the minimal number of spanning reads between neighboring features
     * @param operonDetectionAutomatic true, if the minimal number of spanning reads is not given and
     *      should be calculated by the software
     */
    public AnalysisOperon(TrackConnector trackConnector, int minNumberReads, boolean operonDetectionAutomatic) {
        this.trackConnector = trackConnector;
        this.minNumberReads = minNumberReads;
        this.operonDetectionAutomatic = operonDetectionAutomatic;
        this.operonList = new ArrayList<>();
        this.featureToPutativeOperonMap = new HashMap<>();
        this.operonAdjacencies = new ArrayList<>();
        
        this.initDatastructures();
    }
        
    /**
     * Initializes the initial data structures needed for an operon detection analysis.
     * This includes the detection of all neighboring features before the actual analysis.
     */
    private void initDatastructures() {
        averageReadLength = trackConnector.getAverageReadLength();
        averageSeqPairLength = trackConnector.getAverageSeqPairLength();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.genomeSize = trackConnector.getRefSequenceLength();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, genomeSize);
        
        ////////////////////////////////////////////////////////////////////////////
        // detecting all neighboring features which are not overlapping more than 20bp as putative operons
        ////////////////////////////////////////////////////////////////////////////
        
        for (int i = 0; i < this.genomeFeatures.size() - 1; i++) {

            PersistantFeature feature1 = this.genomeFeatures.get(i);
            PersistantFeature feature2 = this.genomeFeatures.get(i + 1);
            //we currently only exclude exons from the detection 
            if (feature1.getType() != FeatureType.EXON) {
                if (feature1.isFwdStrand() == feature2.isFwdStrand() && feature2.getType() != FeatureType.EXON) {
                    if (feature2.getStart() + 20 <= feature1.getStop()) { //features may overlap at the ends, happens quite often
                        //do nothing
                    } else {
                        this.featureToPutativeOperonMap.put(feature1.getId(), new OperonAdjacency(feature1, feature2));
                    }
                } else { // check next features until one on the same strand is found which is not an exon.
                    /*
                     * We keep all neighboring features on the same strand,
                     * even if their distance is not larger than 1000bp.
                     */
                    int featureIndex = i + 2;
                    while ((feature1.isFwdStrand() != feature2.isFwdStrand() || 
                            feature2.getType() == FeatureType.EXON) && 
                            featureIndex < this.genomeFeatures.size() - 1) {
                        
                        feature2 = this.genomeFeatures.get(featureIndex++);
                    }
                    if (feature1.isFwdStrand() == feature2.isFwdStrand() && feature2.getStart() - feature1.getStop() < 1000) {
                        this.featureToPutativeOperonMap.put(feature1.getId(), new OperonAdjacency(feature1, feature2));
                    }
                }
            }
        }
    }

    /**
     * Sums the read counts for a new list of mappings or calls the finish method.
     * @param data the data to handle: Either a list of mappings or "2" = mapping querries are done.
     */
    @Override
    public void update(Object data) {
        //the mappings are sorted by their start position!
        MappingResultPersistant mappingResult = new MappingResultPersistant(null, 0, 0);
        if (data.getClass() == mappingResult.getClass()) {

        List<PersistantMapping> mappings = ((MappingResultPersistant) data).getMappings();
            this.sumReadCounts(mappings);
        }
        if (data instanceof Byte && ((Byte) data) == 2) {
            this.finish();
        }
    }
    
    /**
     * Method to be called when the analysis is finished. Starts the detection of
     * operons after the read counts for all mappings have been stored.
     */
    public void finish() {
        Date currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Detecting operons", currentTimestamp);
        this.findOperons();
    }

    /**
     * Sums up the read counts for the features the mappings are located in.
     * @param mappings the set of mappings to be investigated
     */
    public void sumReadCounts(List<PersistantMapping> mappings) {

        PersistantFeature feature1;
        PersistantFeature feature2;
        boolean fstFittingMapping;
        PersistantMapping mapping;
        OperonAdjacency putativeOperon;

        for (int i = 0; i < this.genomeFeatures.size(); ++i) {
            feature1 = this.genomeFeatures.get(i);
            int id1 = feature1.getId();
            fstFittingMapping = true;

            //we can already neglect all features not forming a putative operon
            if (this.featureToPutativeOperonMap.containsKey(id1)) {
                feature2 = this.featureToPutativeOperonMap.get(id1).getFeature2();
                
                this.readsFeature1 = 0;
                this.readsFeature2 = 0;
                this.spanningReads = 0;
                this.internalReads = 0;

                int feature1Stop = feature1.getStop();
                int feature2Start = feature2.getStart();
                int feature2Stop = feature2.getStop();

                for (int j = this.lastMappingIdx; j < mappings.size(); ++j) {
                    mapping = mappings.get(j);

                    if (mapping.getStart() > feature2Stop ) {
                        break; //since the mappings are sorted by start position
                    } else if (mapping.isFwdStrand() != feature1.isFwdStrand() || mapping.getStop() < feature1Stop) {
                        continue;
                    }

                    //mappings identified between both features
                    if (mapping.getStart() <= feature1Stop && mapping.getStop() > feature1Stop && mapping.getStop() < feature2Start) {
                        readsFeature1 += mapping.getNbReplicates();
                    } else if (mapping.getStart() > feature1Stop && mapping.getStart() < feature2Start && mapping.getStop() >= feature2Start) {
                        readsFeature2 += mapping.getNbReplicates();
                    } else if (mapping.getStart() <= feature1Stop && mapping.getStop() >= feature2Start) {
                        spanningReads += mapping.getNbReplicates();
                    } else if (mapping.getStart() > feature1Stop && mapping.getStop() < feature2Start) {
                        internalReads += mapping.getNbReplicates();
                    }

                    if (fstFittingMapping) { //TODO: either add to each if clause above or add surrounding if clause!
                        this.lastMappingIdx = j;
                        fstFittingMapping = false;
                    }
                }

                putativeOperon = featureToPutativeOperonMap.get(id1);
                putativeOperon.setReadsFeature1(putativeOperon.getReadsFeature1() + readsFeature1);
                putativeOperon.setReadsFeature2(putativeOperon.getReadsFeature2() + readsFeature2);
                putativeOperon.setSpanningReads(putativeOperon.getSpanningReads() + spanningReads);
                putativeOperon.setInternalReads(putativeOperon.getInternalReads() + internalReads);
            }
        }
        this.lastMappingIdx = 0;
    }
    
    /**
     * Method for identifying operons after all read counts were summed up for each
     * genome feature.
     */
    public void findOperons() {
        Object[] featureIds = featureToPutativeOperonMap.keySet().toArray();
        Arrays.sort(featureIds);
        OperonAdjacency putativeOperon;

        /*
         * If we have sequence pairs, we calculate the average seq pair length
         * and if we only have single reads, we use the average read length.
         */
        //TODO: incorporate sequence pair handling in the detection. currently only reads are used
        int minimumSpanningReads;
//        if (trackCon.getNumOfSeqPairs() > 0 && operonDetectionAutomatic) {
//            minimumSpanningReads = (numUniqueBmMappings * averageSeqPairLength) / transcritomeLength;
//        } else if (operonDetectionAutomatic) {
//            minimumSpanningReads = (numUniqueBmMappings * averageReadLength) / transcritomeLength;
//        } else {
            minimumSpanningReads = minNumberReads;
//        }
//        System.out.println("Threshold: " + minimumSpanningReads + ", = uniqBMM = " + numUniqueBmMappings + ", avReadLength = "
//                + averageReadLength + ", transcriptome length = " + transcritomeLength + "Result: " + numUniqueBmMappings * 280 / transcritomeLength);

        int count = 0;
        int lastAnnoId = 0;
        PersistantFeature feature1;
        PersistantFeature feature2;
        Operon operon;
        for (int i = 0; i < featureIds.length; i++) {
            
            putativeOperon = featureToPutativeOperonMap.get((Integer) featureIds[i]);
            spanningReads = putativeOperon.getSpanningReads();
            internalReads = putativeOperon.getInternalReads();
            feature1 = putativeOperon.getFeature1();
            feature2 = putativeOperon.getFeature2();

            /* Detect an operon only, if the number of spanning reads is larger than
             * the threshold. */
            if (spanningReads >= minimumSpanningReads) {

                //only in this case a new operon starts:
                if (lastAnnoId != feature1.getId() && lastAnnoId != 0) {

                    operon = new Operon(trackConnector.getTrackID());
                    operon.addAllOperonAdjacencies(operonAdjacencies);
                    operonList.add(operon); //only here the operons are added to final list
                    operonAdjacencies.clear();
                }
                operonAdjacencies.add(putativeOperon);
                lastAnnoId = feature2.getId();

            // TODO: check if parameter ok or new parameter
            } else if (feature2.getStart() - feature1.getStop() > averageReadLength &&
                    internalReads > minNumberReads) {
                //create operon
                System.out.println("found case " + ++count);
            }
        }
        if (!operonAdjacencies.isEmpty()) {
            operon = new Operon(trackConnector.getTrackID());
            operon.addAllOperonAdjacencies(operonAdjacencies);
            operonList.add(operon); //only here the operons are added to final list
        }
    }

    @Override
    public List<Operon> getResults() {
        return this.operonList;

    }
}
