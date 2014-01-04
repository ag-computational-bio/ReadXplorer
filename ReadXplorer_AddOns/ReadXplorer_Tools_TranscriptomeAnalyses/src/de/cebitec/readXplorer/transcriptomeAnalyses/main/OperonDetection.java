package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jritter
 */
public class OperonDetection {

    private int trackId;

    public OperonDetection(int trackId) {
        this.trackId = trackId;
    }

    public int getTrackId() {
        return trackId;
    }

    /**
     * Creating Operon Data.
     *
     * @param putativeOperonAdjacencies
     * @param trackConnector Trackconnector.
     * @param bg Background Threshold.
     * @return a List of Operon.
     */
    public List<Operon> concatOperonAdjacenciesToOperons(HashMap<Integer, OperonAdjacency> putativeOperonAdjacencies, TrackConnector trackConnector, double bg) {
        Integer[] sortedStartingFeatureIDs = new Integer[putativeOperonAdjacencies.keySet().size()];
        putativeOperonAdjacencies.keySet().toArray(sortedStartingFeatureIDs);

        List<Operon> operons = new ArrayList<Operon>();
        List<OperonAdjacency> operonAdjacencies = new ArrayList<>();
        int lastAnnoId = 0;
        Operon op;
        PersistantFeature feature1;
        PersistantFeature feature2;
        OperonAdjacency opAdj;
        int spanningReads;
        for (Integer leadingFeatureID : sortedStartingFeatureIDs) {
            opAdj = putativeOperonAdjacencies.get(leadingFeatureID);
            feature1 = opAdj.getFeature1();
            feature2 = opAdj.getFeature2();
            spanningReads = opAdj.getSpanningReads();

            if (spanningReads > bg) {
                if (lastAnnoId != feature1.getId() && lastAnnoId != 0) {
                    op = new Operon(trackConnector.getTrackID());
                    op.addAllOperonAdjacencies(operonAdjacencies);
                    operons.add(op); //only here the operons are added to final list
                    operonAdjacencies.clear();
                }
                operonAdjacencies.add(opAdj);
                lastAnnoId = feature2.getId();
            }
        }

        return operons;
    }
}
