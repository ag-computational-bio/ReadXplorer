package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author MKD, rhilker
 * 
 * Carries out the analysis of a data set for operons.
 */
public class AnalysisOperon implements Observer, AnalysisI<List<Operon>> {

    private TrackViewer trackViewer;
    private TrackConnector trackCon;
    private int minNumberReads;
    private int genomeSize;
    private List<PersistantAnnotation> genomeAnnotations;
    private List<Operon> operonList; //final result list of OperonAdjacencies
    private boolean operonDetectionAutomatic;
    private HashMap<Integer, OperonAdjacency> annoToPutativeOperonMap; //annotation id of mappings to count for annotations
    private List<OperonAdjacency> operonAdjacencies; 
    private int averageReadLength = 0;
    private int averageSeqPairLength = 0;
    private int lastMappingIdx;
    private int readsAnnotation1 = 0;
    private int spanningReads = 0;
    private int readsAnnotation2 = 0;
    private int internalReads = 0;

    /**
     * Carries out the analysis of a data set for operons.
     * @param trackViewer the trackViewer whose data is to be analyzed
     * @param minNumberReads the minimal number of spanning reads between neighboring genes
     * @param operonDetectionAutomatic true, if the minimal number of spanning reads is not given and
     *      should be calculated by the software
     */
    public AnalysisOperon(TrackViewer trackViewer, int minNumberReads, boolean operonDetectionAutomatic) {
        this.trackViewer = trackViewer;
        this.minNumberReads = minNumberReads;
        this.operonDetectionAutomatic = operonDetectionAutomatic;
        this.operonList = new ArrayList<Operon>();
        this.annoToPutativeOperonMap = new HashMap<Integer, OperonAdjacency>();
        this.operonAdjacencies = new ArrayList<OperonAdjacency>();
        
        this.initDatastructures();
    }
        
    /**
     * Initializes the initial data structures needed for an operon detection analysis.
     * This includes the detection of all neighboring annotations before the actual analysis.
     */
    private void initDatastructures() {
        this.trackCon = trackViewer.getTrackCon();
        List<Integer> trackIds = new ArrayList<Integer>();
        trackIds.add(trackCon.getTrackID());
        averageReadLength = trackCon.getAverageReadLength();
        averageSeqPairLength = trackCon.getAverageSeqPairLength();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackViewer.getReference().getId());
        this.genomeSize = refConnector.getRefGen().getSequence().length();
        this.genomeAnnotations = refConnector.getAnnotationsForClosedInterval(0, genomeSize);
        
        ////////////////////////////////////////////////////////////////////////////
        // detecting all neighboring annotations which are not overlapping more than 20bp as putative operons
        ////////////////////////////////////////////////////////////////////////////
        
        for (int i = 0; i < this.genomeAnnotations.size() - 1; i++) {

            PersistantAnnotation annotation1 = this.genomeAnnotations.get(i);
            PersistantAnnotation annotation2 = this.genomeAnnotations.get(i + 1);
            //we currently only exclude exons from the detection 
            if (annotation1.getType() != FeatureType.EXON) {
                if (annotation1.isFwdStrand() == annotation2.isFwdStrand() && annotation2.getType() != FeatureType.EXON) {
                    if (annotation2.getStart() + 20 <= annotation1.getStop()) { //genes may overlap at the ends, happens quite often
                        //do nothing
                    } else {
                        this.annoToPutativeOperonMap.put(annotation1.getId(), new OperonAdjacency(annotation1, annotation2));
                    }
                } else { // check next annotations until one on the same strand is found which is not an exon.
                    /*
                     * We keep all neighboring annotations on the same strand,
                     * even if their distance is not larger than 1000bp.
                     */
                    int annoIndex = i + 2;
                    while ((annotation1.isFwdStrand() != annotation2.isFwdStrand() || 
                            annotation2.getType() == FeatureType.EXON) && 
                            annoIndex < this.genomeAnnotations.size() - 1) {
                        
                        annotation2 = this.genomeAnnotations.get(annoIndex++);
                    }
                    if (annotation1.isFwdStrand() == annotation2.isFwdStrand() && annotation2.getStart() - annotation1.getStop() < 1000) {
                        this.annoToPutativeOperonMap.put(annotation1.getId(), new OperonAdjacency(annotation1, annotation2));
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
        List<PersistantMapping> mappings = new ArrayList<PersistantMapping>();
        if (data.getClass() == mappings.getClass()) {

            mappings = (List<PersistantMapping>) data;
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
     * Sums up the read counts for the annotations the mappings are located in.
     * @param mappings the set of mappings to be investigated
     */
    public void sumReadCounts(List<PersistantMapping> mappings) {

        PersistantAnnotation annotation1;
        PersistantAnnotation annotation2;
        boolean fstFittingMapping = true;

        for (int i = 0; i < this.genomeAnnotations.size(); ++i) {
            annotation1 = this.genomeAnnotations.get(i);
            int id1 = annotation1.getId();
            fstFittingMapping = true;

            //we can already neglect all annotations not forming a putative operon
            if (this.annoToPutativeOperonMap.containsKey(id1)) {
                annotation2 = this.annoToPutativeOperonMap.get(id1).getAnnotation2();
                
                this.readsAnnotation1 = 0;
                this.readsAnnotation2 = 0;
                this.spanningReads = 0;
                this.internalReads = 0;

                int annotation1Stop = annotation1.getStop();
                int annotation2Start = annotation2.getStart();
                int annotation2Stop = annotation2.getStop();

                for (int j = this.lastMappingIdx; j < mappings.size(); ++j) {
                    PersistantMapping mapping = mappings.get(j);

                    if (mapping.getStart() > annotation2Stop ) {
                        break; //since the mappings are sorted by start position
                    } else if (mapping.isFwdStrand() != annotation1.isFwdStrand() || mapping.getStop() < annotation1Stop) {
                        continue;
                    }

                    //mappings identified between both annotations
                    if (mapping.getStart() <= annotation1Stop && mapping.getStop() > annotation1Stop && mapping.getStop() < annotation2Start) {
                        readsAnnotation1 += mapping.getNbReplicates();
                    } else if (mapping.getStart() > annotation1Stop && mapping.getStart() < annotation2Start && mapping.getStop() >= annotation2Start) {
                        readsAnnotation2 += mapping.getNbReplicates();
                    } else if (mapping.getStart() <= annotation1Stop && mapping.getStop() >= annotation2Start) {
                        spanningReads += mapping.getNbReplicates();
                    } else if (mapping.getStart() > annotation1Stop && mapping.getStop() < annotation2Start) {
                        internalReads += mapping.getNbReplicates();
                    }

                    if (fstFittingMapping == true) {
                        this.lastMappingIdx = j;
                        fstFittingMapping = false;
                    }
                }

                OperonAdjacency putativeOperon = annoToPutativeOperonMap.get(id1);
                putativeOperon.setReadsAnnotation1(putativeOperon.getReadsAnnotation1() + readsAnnotation1);
                putativeOperon.setReadsAnnotation2(putativeOperon.getReadsAnnotation2() + readsAnnotation2);
                putativeOperon.setSpanningReads(putativeOperon.getSpanningReads() + spanningReads);
                putativeOperon.setInternalReads(putativeOperon.getInternalReads() + internalReads);
            }
        }
        this.lastMappingIdx = 0;
    }
    
    /**
     * Method for identifying operons after all read counts were summed up for each
     * genome annotation.
     */
    public void findOperons() {
        Object[] annoIds = annoToPutativeOperonMap.keySet().toArray();
        Arrays.sort(annoIds);
        OperonAdjacency putativeOperon;

        /*
         * If we have sequence pairs, we calculate the average seq pair length
         * and if we only have single reads, we use the average read length.
         */
        //TODO: incorporate sequence pair handling in the detection. currently only reads are used
        int minimumSpanningReads = 0;
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
        for (int i = 0; i < annoIds.length; i++) {
            
            putativeOperon = annoToPutativeOperonMap.get((Integer) annoIds[i]);
            spanningReads = putativeOperon.getSpanningReads();
            internalReads = putativeOperon.getInternalReads();
            PersistantAnnotation anno1 = putativeOperon.getAnnotation1();
            PersistantAnnotation anno2 = putativeOperon.getAnnotation2();

            /* Detect an operon only, if the number of spanning reads is larger than
             * the threshold. */
            if (spanningReads >= minimumSpanningReads) {

                //only in this case a new operon starts:
                if (lastAnnoId != anno1.getId() && lastAnnoId != 0) {

                    Operon operon = new Operon();
                    operon.addAllOperonAdjacencies(operonAdjacencies);
                    operonList.add(operon); //only here the operons are added to final list
                    operonAdjacencies.clear();
                }
                operonAdjacencies.add(putativeOperon);
                lastAnnoId = anno2.getId();

            // TODO: check if parameter ok or new parameter
            } else if (anno2.getStart() - anno1.getStop() > averageReadLength &&
                    internalReads > minNumberReads) {
                //create operon
                System.out.println("found case " + ++count);
            }
        }
        if (!operonAdjacencies.isEmpty()) {
            Operon operon = new Operon();
            operon.addAllOperonAdjacencies(operonAdjacencies);
            operonList.add(operon); //only here the operons are added to final list
        }
    }

    @Override
    public List<Operon> getResults() {
        return this.operonList;

    }
}
