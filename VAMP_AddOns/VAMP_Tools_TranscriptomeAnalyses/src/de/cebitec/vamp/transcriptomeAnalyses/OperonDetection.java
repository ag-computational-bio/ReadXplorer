package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.transcriptomeAnalyses.datastructure.Operon;
import de.cebitec.vamp.transcriptomeAnalyses.datastructure.OperonAdjacency;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jritter
 */
public class OperonDetection {
    
        /**
     * The List contains the featureID of the first and second feature. The
     * Integer represents the count mappings are span over this Operon.
     */
    private List<Operon> fwdOperons, revOperons;

    public OperonDetection(TrackConnector tc, HashMap<Integer, OperonAdjacency> putativeFwdOperonAdj, HashMap<Integer, OperonAdjacency> putativeRevOperonAdj, int bg) {
        
        this.fwdOperons = concatOperonAdjacenciesToOperons(putativeFwdOperonAdj, tc, bg);
        this.revOperons = concatOperonAdjacenciesToOperons(putativeRevOperonAdj, tc, bg);
    }
    
    
      /**
     * Creating Operon Data.
     *
     * @param putativeOperonAdjacencies
     * @return a List of Operons.
     */
    private List<Operon> concatOperonAdjacenciesToOperons(HashMap<Integer, OperonAdjacency> putativeOperonAdjacencies, TrackConnector trackConnector, int bg) {
        Integer[] sortedStartingFeatureIDs = new Integer[putativeOperonAdjacencies.keySet().size()];
        putativeOperonAdjacencies.keySet().toArray(sortedStartingFeatureIDs);

        List<Operon> operons = new ArrayList<Operon>();
        List<OperonAdjacency> operonAdjacencies = new ArrayList<OperonAdjacency>();
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
    
        private void createOperonData() {
////=============================================================
////unless ($opt_O) {
////    print STDERR "Writing operon data...\n";
////    foreach my $pos (sort by_number(keys(%op2sort))) {
//	print OP "$op2sort{$pos}\n";
//    }
//}
    }
}
