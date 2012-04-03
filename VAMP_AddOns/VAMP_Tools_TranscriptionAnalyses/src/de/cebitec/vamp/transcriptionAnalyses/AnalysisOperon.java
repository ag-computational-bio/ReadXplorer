package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.PutativeOperon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.OperonAdjacency;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * @author MKD, rhilker
 * 
 * Carries out the analysis of a data set for operons.
 */
public class AnalysisOperon implements ThreadListener, AnalysisI<List>, JobI {

    private final ProgressHandle progressHandle;
    private DataVisualisationI parent;
    private TrackViewer trackViewer;
    private TrackConnector trackCon;
    private int minNumberReads;
    private int genomeSize;
    private List<PersistantAnnotation> genomeAnnotations;
    private List<Operon> operonList;
    private MappingThreadAnalyses mappingThread;
    private int nbRequests;
    private int nbCarriedOutRequests;
    private int numUniqueBmMappingsGenome;
    private int transcritomeLength;
    private boolean operonDetectionAutomatic;
    private HashMap<Integer, PutativeOperon> annotationReadCount; //annotation id of mappings to count for annotation
    private List<OperonAdjacency> operonAdjacencies;
    private int lastGene = 0;
    private int averageReadLength = 0;
    private int averageSeqPairLength = 0;

    /**
     * Carries out the analysis of a data set for operons.
     * @param parent the parent, which should visualize the results after the analysis
     * @param trackViewer the trackViewer whose data is to be analyzed
     * @param minNumberReads the minimal number of spanning reads between neighboring genes
     * @param operonDetectionAutomatic true, if the minimal number of spanning reads is not given and
     *      should be calculated by the software
     */
    public AnalysisOperon(DataVisualisationI parent, TrackViewer trackViewer, int minNumberReads, boolean operonDetectionAutomatic) {
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AnalysisOperon.class, "MSG_AnalysesWorker.progress.name"));
        this.parent = parent;
        this.trackViewer = trackViewer;
        this.minNumberReads = minNumberReads;
        this.operonDetectionAutomatic = operonDetectionAutomatic;
        this.nbCarriedOutRequests = 0;
        this.operonList = new ArrayList<Operon>();
        this.annotationReadCount = new HashMap<Integer, PutativeOperon>();
        this.operonAdjacencies = new ArrayList<OperonAdjacency>();
    }

    @Override
    public void startAnalysis() {

        this.progressHandle.start();
        this.trackCon = trackViewer.getTrackCon();
        List<Integer> trackIds = new ArrayList<Integer>();
        trackIds.add(trackCon.getTrackID());
        averageReadLength = trackCon.getAverageReadLength();
        averageSeqPairLength = trackCon.getAverageSeqPairLength();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackViewer.getReference().getId());
        this.genomeSize = refConnector.getRefGen().getSequence().length();
        this.genomeAnnotations = refConnector.getAnnotationsForClosedInterval(0, genomeSize);
        int numUnneededMappings = 0;
        numUniqueBmMappingsGenome = trackCon.getNumOfUniqueBmMappings();
        transcritomeLength = trackCon.getCoveredBestMatchPos();

        List<PersistantTrack> tracksAll = ProjectConnector.getInstance().getTracks();
        for (PersistantTrack track : tracksAll) {
            TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(track);
            if (track.getId() < trackCon.getTrackID()) {
                numUnneededMappings += connector.getNumOfUniqueMappings();
            }
        }
        int numInterestingMappings = numUnneededMappings
                + trackCon.getNumOfUniqueMappings();

        for (int i = 0; i < this.genomeAnnotations.size() - 1; i++) {

            PersistantAnnotation annotation1 = this.genomeAnnotations.get(i);
            PersistantAnnotation annotation2 = this.genomeAnnotations.get(i + 1);
            if (annotation1.getStrand() == annotation2.getStrand()) {
                if (annotation2.getStart() <= annotation1.getStop()) {
                    //do nothing
                } else {
                    this.annotationReadCount.put(annotation1.getId(), new PutativeOperon(annotation1, annotation2));
                }
            }
        }

        for (int trackId : trackIds) {

            int stepSize = 50000;
            int from = numUnneededMappings;
            int to = numInterestingMappings - numUnneededMappings > stepSize ? 
                    numUnneededMappings + stepSize : numInterestingMappings;
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
            this.sumReadCounts(mappings);
        }

        //when the last request is finished signalize the parent to collect the data
        if (nbCarriedOutRequests >= nbRequests - 1) {
            Date currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Collect the data", currentTimestamp);
            this.findOperons();
            this.parent.showData(true);
            this.nbCarriedOutRequests = 0;
            this.progressHandle.finish();
        }
    }

    /**
     * Sums up the read counts for the annotations the mappings are located in.
     * @param mappings the set of mappings to be investigated
     */
    public void sumReadCounts(List<PersistantMapping> mappings) {
        PersistantAnnotation annotation1;
        PersistantAnnotation annotation2;

        for (int i = 0; i < this.genomeAnnotations.size() - 1; i++) {
            annotation1 = this.genomeAnnotations.get(i);
            annotation2 = this.genomeAnnotations.get(i + 1);
            int annotation1Start = annotation1.getStart();
            int annotation1Stop = annotation1.getStop();
            int annotation2Start = annotation2.getStart();
            int annotation2Stop = annotation2.getStop();
            int readsGene1 = 0;
            int spanningReads = 0;
            int readsGene2 = 0;
            int internalReads = 0;
            int currentId = annotation1.getId();

            if (annotationReadCount.get(currentId) != null) {
                for (int j = 0; j < mappings.size(); ++j) {
                    PersistantMapping mapping = mappings.get(j);
                    if (mapping.getStart() >= annotation1Start && mapping.getStart() <= annotation1Stop && mapping.getStop() < annotation2Start) {
                        readsGene1++;
                    } else if (mapping.getStart() > annotation1Stop && mapping.getStop() >= annotation2Start && mapping.getStop() <= annotation2Stop) {
                        readsGene2++;
                    } else if (mapping.getStart() <= annotation1Stop && mapping.getStop() >= annotation2Start) {
                        spanningReads++;
                    } else if (mapping.getStart() > annotation1Stop && mapping.getStop() < annotation2Start) {
                        internalReads++;
                    }
//                    if (mapping.getStart() <= featStop_Gen1 && mapping.getStart()>=featStart_Gen1) {
//                        if (mapping.getStop() >= featStart_Gen2 && mapping.getStop() <=featStop_Gen2) {
//                            read_cover_Gen1_and_Gen2++;
//                        } else if(mapping.getStop() <featStart_Gen2) {
//                            read_cover_Gen1++;
//                        }
//
//                    } else if (mapping.getStop() >= featStart_Gen2 && mapping.getStart() > featStop_Gen1 && mapping.getStop()<=featStop_Gen2) {
//                        read_cover_Gen2++;
//                    } else if(mapping.getStart() > featStop_Gen1 && mapping.getStop()< featStart_Gen2) {
//                        read_cover_none++;
//                    }

                }
                annotationReadCount.get(currentId).setReadsGene1(annotationReadCount.get(currentId).getReadsGene1() + readsGene1);
                annotationReadCount.get(currentId).setSpanningReads(annotationReadCount.get(currentId).getSpanningReads() + spanningReads);
                annotationReadCount.get(currentId).setReadsGene2(annotationReadCount.get(currentId).getReadsGene2() + readsGene2);
                annotationReadCount.get(currentId).setInternalReads(annotationReadCount.get(currentId).getInternalReads() + internalReads);

            }
        }
    }
    
    /**
     * Method for identifying operons after all read counts were summed up for each
     * genome annotation.
     */
    public void findOperons() {
        Set<Integer> s = annotationReadCount.keySet();
        Iterator i = s.iterator();
        Object[] keyss = annotationReadCount.keySet().toArray();
        Arrays.sort(keyss);
        int key;
        for (int z = 0; z < keyss.length; z++) {
            
            key = (Integer) keyss[z];
            int spanningReads = annotationReadCount.get(key).getSpanningReads();
            int readsGene1 = annotationReadCount.get(key).getReadsGene1();
            int readsGene2 = annotationReadCount.get(key).getReadsGene2();
            int internalReads = annotationReadCount.get(key).getInternalReads();
            int allReads = spanningReads + readsGene1 + readsGene2 + internalReads;
            int threshold = 0;
            PersistantAnnotation gene1 = annotationReadCount.get(key).getGene1();
            PersistantAnnotation gene2 = annotationReadCount.get(key).getGene2();

            if (trackCon.getNumOfSeqPairs() > 0) {
                //System.out.println("mkdmkd "+ trackCon.getNumOfSeqPairs());
                if (!operonDetectionAutomatic) {
                    threshold = (numUniqueBmMappingsGenome * averageSeqPairLength) / transcritomeLength;
                } else {
                    threshold = minNumberReads;

                }
            } else {
                if (!operonDetectionAutomatic) {
                    threshold = (numUniqueBmMappingsGenome * averageReadLength) / transcritomeLength;
                } else {
                    threshold = minNumberReads;

                }
            }



            /* Detect an operon only, if the number of spanning reads is larger than
             * the threshold. */
            if (spanningReads > threshold) {
                OperonAdjacency operonAdjacency = new OperonAdjacency(gene1, gene2);
                operonAdjacency.setReadsGene1(readsGene1);
                operonAdjacency.setSpanningReads(spanningReads);
                operonAdjacency.setReadsGene2(readsGene2);
                operonAdjacency.setInternalReads(internalReads);

                if (lastGene == gene1.getId() && lastGene != 0) {

                    operonAdjacencies.add(operonAdjacency);
                    lastGene = gene2.getId();
                } else if (lastGene != gene1.getId() && lastGene != 0) {

                    Operon op = new Operon();

                    for (int y = 0; y < operonAdjacencies.size(); y++) {
                        op.getOperon().add(operonAdjacencies.get(y));
                    }
                    operonList.add(op);
                    operonAdjacencies.clear();
                    operonAdjacencies.add(operonAdjacency);

                    lastGene = gene2.getId();

                } else if (lastGene == 0) {
                    operonAdjacencies.add(operonAdjacency);
                    lastGene = gene2.getId();
                }

                // TODO: check if parameter ok or new parameter
//            } else if (gene2.getStart() - gene1.getStop() > averageReadLength &&
//                    minimalCoverage > minNumberReads) {
//                //create operon
            }

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
    public List getResults() {
        return this.operonList;

    }
}
