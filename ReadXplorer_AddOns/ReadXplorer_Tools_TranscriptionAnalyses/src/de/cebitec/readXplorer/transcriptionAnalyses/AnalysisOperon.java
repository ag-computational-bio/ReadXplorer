package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.StatsContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carries out the analysis of a data set for operons.
 *
 * @author MKD, rhilker
 */
public class AnalysisOperon implements Observer, AnalysisI<List<Operon>> {

    private TrackConnector trackConnector;
    private int genomeSize;
    private List<PersistantFeature> genomeFeatures;
    private List<Operon> operonList; //final result list of OperonAdjacencies
    private HashMap<Integer, OperonAdjacency> featureToPutativeOperonMap; //feature id of mappings to count for features
    private List<OperonAdjacency> operonAdjacencies; 
    private int averageReadLength = 0;
    private int averageReadPairLength = 0;
    private int lastMappingIdx;
    private int readsFeature1 = 0;
    private int spanningReads = 0;
    private int readsFeature2 = 0;
    private int internalReads = 0;
    private final ParameterSetOperonDet operonDetParameters;

    /**
     * Carries out the analysis of a data set for operons.
     * @param trackConnector the trackConnector whose data is to be analyzed
     * @param operonDetParameters contains the parameters to use for the operon
     * detection
     */
    public AnalysisOperon(TrackConnector trackConnector, ParameterSetOperonDet operonDetParameters) {
        this.trackConnector = trackConnector;
        this.operonDetParameters = operonDetParameters;
        this.operonList = new ArrayList<>();
        this.featureToPutativeOperonMap = new HashMap<>();
        this.operonAdjacencies = new ArrayList<>();
        
        this.initDatastructures();
    }
        
    /**
     * Initializes the initial data structures needed for an operon detection
     * analysis. This includes the detection of all neighboring features before
     * the actual analysis.
     */
    private void initDatastructures() {
        Map<String, Integer> statsMap = trackConnector.getTrackStats().getStatsMap();
        averageReadLength = statsMap.get(StatsContainer.AVERAGE_READ_LENGTH);
        averageReadPairLength = statsMap.get(StatsContainer.AVERAGE_SEQ_PAIR_SIZE);
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.genomeSize = trackConnector.getRefSequenceLength();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, genomeSize);
        
        ////////////////////////////////////////////////////////////////////////////
        // detecting all neighboring features which are not overlapping more than 20bp and
        // have a distance smaller than 1000 bp from stop of 1 to start of 2 as putative operons
        ////////////////////////////////////////////////////////////////////////////
        
        for (int i = 0; i < this.genomeFeatures.size() - 1; i++) {

            PersistantFeature feature1 = this.genomeFeatures.get(i);
            boolean reachedEnd = false;
            
            if (operonDetParameters.getSelFeatureTypes().contains(feature1.getType())) {

                int featureIndex = i + 1; //find feature 2
                while (feature1.isFwdStrand() != genomeFeatures.get(featureIndex).isFwdStrand()
                        || !feature1.getType().equals(genomeFeatures.get(featureIndex).getType())) {
                    if (featureIndex < genomeFeatures.size() - 1) {
                        ++featureIndex;
                    } else {
                        reachedEnd = true;
                        break;
                    }
                }
                
                if (!reachedEnd) {
                    PersistantFeature feature2 = genomeFeatures.get(featureIndex);

                    if (feature2.getStart() + 20 > feature1.getStop() && //features may overlap at the ends, happens quite often
                            feature2.getStart() - feature1.getStop() < 1000) { //only features with a max. distance of 1000 are treated as putative operons
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
            minimumSpanningReads = operonDetParameters.getMinSpanningReads();
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
                    internalReads > operonDetParameters.getMinSpanningReads()) {
                //TODO: think about creating an operon
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
