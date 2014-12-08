
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 *
 * @author jritter
 */
public class OperonDetection {

    private final int trackId;


    public OperonDetection( int trackId ) {
        this.trackId = trackId;
    }


    public int getTrackId() {
        return trackId;
    }


    /**
     * Creating Operon Data.
     *
     * @param putativeOperonAdjacencies
     * @param bg                        Background Threshold.
     * <p>
     * @return a List of Operon.
     */
    public List<Operon> concatOperonAdjacenciesToOperons( TreeMap<Integer, OperonAdjacency> putativeOperonAdjacencies, double bg ) {
//        Integer[] sortedStartingFeatureIDs = new Integer[putativeOperonAdjacencies.keySet().size()];
//        putativeOperonAdjacencies.keySet().toArray(sortedStartingFeatureIDs);

        List<Operon> operons = new CopyOnWriteArrayList<>();
        List<OperonAdjacency> operonAdjacencies = new ArrayList<>();
        int lastAnnoId = 0;
        Operon op;
        PersistentFeature feature1;
        PersistentFeature feature2;
        OperonAdjacency opAdj;
        int spanningReads;
        for( Integer leadingFeatureID : putativeOperonAdjacencies.keySet() ) {
            opAdj = putativeOperonAdjacencies.get( leadingFeatureID );
            feature1 = opAdj.getFeature1();
            feature2 = opAdj.getFeature2();
            spanningReads = opAdj.getSpanningReads();

            if( spanningReads > bg ) {
                if( lastAnnoId != feature1.getId() && lastAnnoId != 0 ) {
                    op = new Operon( trackId );
                    op.addAllOperonAdjacencies( operonAdjacencies );
                    operons.add( op ); //only here the operons are added to final list
                    operonAdjacencies.clear();
                }
                // check
                boolean check = false;
                for( OperonAdjacency operonAdjacency : operonAdjacencies ) {
                    if( operonAdjacency.getFeature1().getLocus().equals( feature2.getLocus() ) ) {
                        check = true;
                    }
                }

                if( !check ) {
                    operonAdjacencies.add( opAdj );
                    lastAnnoId = feature2.getId();
                }
            }
        }

        for( Operon operon : operons ) {
            for( OperonAdjacency operonAdjacency : operon.getOperonAdjacencies() ) {

                PersistentFeature featureA = operonAdjacency.getFeature1();
                PersistentFeature featureB = operonAdjacency.getFeature2();
                if( featureA.getType() == FeatureType.RRNA ) {
                    operon.removeAdjaceny( operonAdjacency );
                }
                else if( featureB.getType() == FeatureType.RRNA ) {
                    operon.removeAdjaceny( operonAdjacency );
                }
                else if( featureA.getType() == FeatureType.TRNA ) {
                    operon.removeAdjaceny( operonAdjacency );
                }
                else if( featureB.getType() == FeatureType.TRNA ) {
                    operon.removeAdjaceny( operonAdjacency );
                }
            }
            if( operon.getOperonAdjacencies().isEmpty() ) {
                operons.remove( operon );
            }
        }

        return operons;
    }


}
