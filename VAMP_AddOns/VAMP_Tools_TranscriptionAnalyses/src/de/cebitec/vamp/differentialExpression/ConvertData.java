package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author kstaderm
 */
public class ConvertData {

    public static synchronized List<Pair<Double, Double>> mAplotData(Vector<Vector> resultTable, DeAnalysisHandler.Tool usedTool) {
        List<Pair<Double, Double>> ret = null;
        switch (usedTool) {
            case BaySeq:
                ret = convertBaySeqResults(resultTable);
                break;
            case DeSeq:
                ret = convertDESeqResults(resultTable);
                break;
            case SimpleTest:
                ret = convertSimpleTestResults(resultTable);
                break;
        }
        return ret;
    }

    private static List<Pair<Double, Double>> convertBaySeqResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static List<Pair<Double, Double>> convertDESeqResults(Vector<Vector> resultTable) {
        List<Pair<Double, Double>> ret = new ArrayList<>();
        //For GNU R Version:
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            Double R = (Double) row.get(3);
            Double G = (Double) row.get(4);
            Pair<Double, Double> MA = createMAvalues(G,R);
            ret.add(MA);            
        }
//        //For new, Java internal Version
//        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
//            Vector row = it.next();
//            Double R = (Double) row.get(4);
//            Double G = (Double) row.get(6);
//            Pair<Double, Double> MA = createMAvalues(G,R);
//            ret.add(MA);            
//        }
        return ret;
    }

    private static List<Pair<Double, Double>> convertSimpleTestResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private static Pair<Double, Double> createMAvalues(Double G, Double R){       
        Double M = (Math.log(R)/Math.log(2))-(Math.log(G)/Math.log(2));
        Double A = ((Math.log(R)/Math.log(2))+(Math.log(G)/Math.log(2)))/2;
        return new Pair<>(M,A);
    }
}
