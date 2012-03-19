/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptionAnalyses;

/**
 *
 * @author MKD
 */
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
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.DataVisualisationI;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import java.util.Iterator;

public class AnalysisOperon implements ThreadListener, AnalysisI<List>, JobI {

    private final ProgressHandle progressHandle;
    private DataVisualisationI parent;
    private TrackViewer trackViewer;
    private int minNumberReads;
    private int genomeSize;
    private List<PersistantFeature> genomeFeatures;
    private List<Operon> operonList;
    private MappingThreadAnalyses mappingThread;
    private int nbRequests;
    private int nbCarriedOutRequests;
    private int WholNumberMappingGenom;
    private int TranscritomLength;
    private boolean operonDetectionAutomatic;
    private HashMap<Integer, PutativOperon> featureReadCount; //feature id to count of mappings for feature
    private List<OperonAdjacency> neighbarOperon;
    private int lastGene = 0;
    private int average_Read_Length = 0;
    TrackConnector trackCon;
    private int average_SeqPair_length = 0;

    public AnalysisOperon(DataVisualisationI parent, TrackViewer trackViewer, int minNumberReads, boolean operonDetectionAutomatic) {
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AnalysisOperon.class, "MSG_AnalysesWorker.progress.name"));
        this.parent = parent;
        this.trackViewer = trackViewer;
        this.minNumberReads = minNumberReads;
        this.operonDetectionAutomatic = operonDetectionAutomatic;
        this.nbCarriedOutRequests = 0;
        this.operonList = new ArrayList<Operon>();
        this.featureReadCount = new HashMap<Integer, PutativOperon>();
        this.neighbarOperon = new ArrayList<OperonAdjacency>();
    }

    @Override
    public void startAnalysis() {

        this.progressHandle.start();
        this.trackCon = trackViewer.getTrackCon();
        List<Integer> trackIds = new ArrayList<Integer>();
        trackIds.add(trackCon.getTrackID());
        average_Read_Length = trackCon.getAverageReadLength();
        average_SeqPair_length = trackCon.getAverageSeqPairLenght();
        //System.out.println(average_SeqPair_length);
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackViewer.getReference().getId());
        this.genomeSize = refConnector.getRefGen().getSequence().length();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, genomeSize);
        int numMappingsTotal = 0;
        int numUnneededMappings = 0;
        WholNumberMappingGenom = trackCon.getNumOfUniqueBmMappings();
        TranscritomLength = trackCon.getCoveredBestMatchPos();

        List<PersistantTrack> tracksAll = ProjectConnector.getInstance().getTracks();
        for (PersistantTrack track : tracksAll) {
            TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(track);
            numMappingsTotal += connector.getNumOfUniqueMappings();
            if (track.getId() < trackCon.getTrackID()) {
                numUnneededMappings += connector.getNumOfUniqueMappings();
            }
        }
        int numInterestingMappings = numUnneededMappings
                + trackCon.getNumOfUniqueMappings();

        for (int i = 0; i < this.genomeFeatures.size() - 1; i++) {

            PersistantFeature feature1 = this.genomeFeatures.get(i);
            PersistantFeature feature2 = this.genomeFeatures.get(i + 1);
            if (feature1.getStrand() == feature2.getStrand()) {
                if (feature2.getStart() <= feature1.getStop()) {
                    //do nothing
                } else {

                    this.featureReadCount.put(feature1.getId(), new PutativOperon(feature1, feature2));
                }
            }

        }


        for (int trackId : trackIds) {

            int stepSize = 50000;
            int from = numUnneededMappings;
            int to = numInterestingMappings - numUnneededMappings > stepSize
                    ? numUnneededMappings + stepSize : numInterestingMappings;
            int additionalRequest = numInterestingMappings % stepSize
                    == 0 ? 0 : 1;
            this.nbRequests = (numInterestingMappings
                    - numUnneededMappings) / stepSize + additionalRequest;
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

        List<PersistantMapping> mappcoverage = new ArrayList<PersistantMapping>();
        if (data.getClass() == mappcoverage.getClass()) {

            mappcoverage = (List<PersistantMapping>) data;
            this.operonDetection(mappcoverage);


        }

        //when the last request is finished signalize the parent to collect the data
        if (nbCarriedOutRequests >= nbRequests - 1) {
            Date currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
            //Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Collect the data");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Collect the data", currentTimestamp);
            this.findOperon();
            this.parent.showData(true);
            this.nbCarriedOutRequests = 0;
            this.progressHandle.finish();
        }
    }

    public void operonDetection(List<PersistantMapping> mappings) {
        PersistantFeature feature_Gen1;
        PersistantFeature feature_Gen2;



        for (int i = 0; i < this.genomeFeatures.size() - 1; i++) {
            feature_Gen1 = this.genomeFeatures.get(i);
            feature_Gen2 = this.genomeFeatures.get(i + 1);
            int featStart_Gen1 = feature_Gen1.getStart();
            int featStop_Gen1 = feature_Gen1.getStop();
            int featStart_Gen2 = feature_Gen2.getStart();
            int featStop_Gen2 = feature_Gen2.getStop();
            int read_cover_Gen1 = 0;
            int read_cover_Gen1_and_Gen2 = 0;
            int read_cover_Gen2 = 0;
            int read_cover_none = 0;
            int current_ID = feature_Gen1.getId();

            if (featureReadCount.get(current_ID) != null) {
                for (int j = 0; j < mappings.size(); ++j) {
                    PersistantMapping mapping = mappings.get(j);
                    if (mapping.getStart() >= featStart_Gen1 && mapping.getStart() <= featStop_Gen1 && mapping.getStop() < featStart_Gen2) {
                        read_cover_Gen1++;
                    } else if (mapping.getStart() > featStop_Gen1 && mapping.getStop() >= featStart_Gen2 && mapping.getStop() <= featStop_Gen2) {
                        read_cover_Gen2++;
                    } else if (mapping.getStart() >= featStart_Gen1 && mapping.getStart() <= featStop_Gen1 && mapping.getStop() >= featStart_Gen2 && mapping.getStop() <= featStop_Gen2) {
                        read_cover_Gen1_and_Gen2++;
                    } else if (mapping.getStart() > featStop_Gen1 && mapping.getStop() < featStart_Gen2) {
                        read_cover_none++;
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
                featureReadCount.get(current_ID).setRead_cover_Gen1(read_cover_Gen1);
                featureReadCount.get(current_ID).setRead_cover_Gen1_and_Gen2(read_cover_Gen1_and_Gen2);
                featureReadCount.get(current_ID).setRead_cover_Gen2(read_cover_Gen2);
                featureReadCount.get(current_ID).setRead_cover_none(read_cover_none);

            }




        }
    }

    public void findOperon() {
        Set<Integer> s = featureReadCount.keySet();
        Iterator i = s.iterator();
        Object[] keyss = featureReadCount.keySet().toArray();
        Arrays.sort(keyss);
        for (int z = 0; z < keyss.length; z++) {
            //System.out.println(featureReadCount.get(keyss[z]).getGenFeature1().getId());
            //}
            //for (Integer key : s) {
            int nb_cover_Gen1_and_Gen2 = featureReadCount.get(keyss[z]).getRead_cover_Gen1_and_Gen2();
            int nb_cover_Gen1 = featureReadCount.get(keyss[z]).getRead_cover_Gen1();
            int nb_cover_Gen2 = featureReadCount.get(keyss[z]).getRead_cover_Gen2();
            int nb_cover_none = featureReadCount.get(keyss[z]).getRead_cover_none();
            int allReadCover = nb_cover_Gen1_and_Gen2 + nb_cover_Gen1 + nb_cover_Gen2 + nb_cover_none;
            int numberSignifikant = 0;
            PersistantFeature feature_Gen1 = featureReadCount.get(keyss[z]).getGenFeature1();
            PersistantFeature feature_Gen2 = featureReadCount.get(keyss[z]).getGenFeature2();

            if (trackCon.getNumOfSeqPairs() > 0) {
                //System.out.println("mkdmkd "+ trackCon.getNumOfSeqPairs());
                if (!operonDetectionAutomatic) {
                    numberSignifikant = (WholNumberMappingGenom * average_SeqPair_length) / TranscritomLength;
                } else {
                    numberSignifikant = minNumberReads;

                }
            } else {
                if (!operonDetectionAutomatic) {
                    numberSignifikant = (WholNumberMappingGenom * average_Read_Length) / TranscritomLength;
                } else {
                    numberSignifikant = minNumberReads;

                }
            }




            if (allReadCover > numberSignifikant) {
                OperonAdjacency operon_gene = new OperonAdjacency(feature_Gen1, feature_Gen2);
                operon_gene.setRead_cover_Gen1(nb_cover_Gen1);
                operon_gene.setRead_cover_Gen1_and_Gen2(nb_cover_Gen1_and_Gen2);
                operon_gene.setRead_cover_Gen2(nb_cover_Gen2);
                operon_gene.setRead_cover_none(nb_cover_none);


                if (lastGene == feature_Gen1.getId() && lastGene != 0) {

                    neighbarOperon.add(operon_gene);
//                    System.out.println(feature_Gen1.getId() + "==" + feature_Gen2.getId());
                    lastGene = feature_Gen2.getId();
                } else if (lastGene != feature_Gen1.getId() && lastGene != 0) {

                    Operon op = new Operon();

                    for (int y = 0; y < neighbarOperon.size(); y++) {
                        op.getOperon().add(neighbarOperon.get(y));
                    }
                    operonList.add(op);
                    neighbarOperon.clear();
                    neighbarOperon.add(operon_gene);

                    lastGene = feature_Gen2.getId();

                } else if (lastGene == 0) {
                    neighbarOperon.add(operon_gene);
                    lastGene = feature_Gen2.getId();
                }

            }

        }

//        for(int a=0;a<operonList.size();a++){
//            List<OperonAdjacency> o=operonList.get(a).getOperon();
//            for(){}
//            
//        }
        //      int allReadCover = read_cover_Gen1 + read_cover_Gen1_and_Gen2 + read_cover_Gen2 + read_cover_none;
//            //numberSignifikant = (this.WholNumberMappingGenom * 35) / this.TranscritomLength;
//            if (!operonDetectionAutomatic) {
//                numberSignifikant = (WholNumberMappingGenom * 35) / TranscritomLength;
//            } else {
//                numberSignifikant = minNumberReads;
//
//            }
//
//            if (allReadCover > numberSignifikant) {
//                OperonAdjacency operon_gene = new OperonAdjacency(feature_Gen1, feature_Gen2);
//                operon_gene.setRead_cover_Gen1(read_cover_Gen1);
//                operon_gene.setRead_cover_Gen1_and_Gen2(read_cover_Gen1_and_Gen2);
//                operon_gene.setRead_cover_Gen2(read_cover_Gen2);
//                operon_gene.setRead_cover_Gen1(read_cover_Gen1);
//
//                if (lastGene == feature_Gen1.getId() && lastGene != 0) {
//                    operon.setOperon(operon_gene);
//                    lastGene = feature_Gen2.getId();
//
//
//                } else if (lastGene != feature_Gen1.getId() && lastGene != 0) {
//                    operonList.add(operon);
//
//                    operon.clearList();
//
//                    operon.setOperon(operon_gene);
//                    lastGene = feature_Gen2.getId();
//
//                } else if (lastGene == 0) {
//                    operon.setOperon(operon_gene);
//                    lastGene = feature_Gen2.getId();
//
//                }
//
//            }
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
