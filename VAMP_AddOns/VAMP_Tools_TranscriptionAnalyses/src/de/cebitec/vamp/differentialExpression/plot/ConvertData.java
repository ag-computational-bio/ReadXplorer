package de.cebitec.vamp.differentialExpression.plot;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.differentialExpression.DeAnalysisHandler;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.BaySeq;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.DeSeq;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.SimpleTest;
import de.cebitec.vamp.differentialExpression.ResultDeAnalysis;
import de.cebitec.vamp.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author kstaderm
 */
public class ConvertData {

    public static Map<PersistantFeature, Pair<Double, Double>> ratioABagainstConfidence(ResultDeAnalysis result) {
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        Vector resultTable = result.getTableContents();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            PersistantFeature key = (PersistantFeature) row.get(0);
            Double X = (Double) row.get(7);
            Double Y = (Double) row.get(9);
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.put(key, values);
        }
        return ret;
    }

    public static Map<PersistantFeature, Pair<Double, Double>> ratioBAagainstConfidence(ResultDeAnalysis result) {
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        Vector resultTable = result.getTableContents();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            PersistantFeature key = (PersistantFeature) row.get(0);
            Double X = (Double) row.get(8);
            Double Y = (Double) row.get(9);
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.put(key, values);
        }
        return ret;
    }

    public static Map<PersistantFeature, Pair<Double, Double>> createMAvalues(ResultDeAnalysis result, DeAnalysisHandler.Tool usedTool) {
        Map<PersistantFeature, Pair<Double, Double>> input = new HashMap<>();
        switch (usedTool) {
            case BaySeq:
                input = convertBaySeqResults(result.getTableContents());
                break;
            case DeSeq:
                input = convertDESeqResults(result.getTableContents());
                break;
            case SimpleTest:
                input = convertSimpleTestResults(result.getTableContents());
                break;
        }
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        for (Iterator<PersistantFeature> it = input.keySet().iterator(); it.hasNext();) {
            PersistantFeature key = it.next();
            Pair<Double, Double> pair = input.get(key);
            Double R = (Double) pair.getFirst();
            Double G = (Double) pair.getSecond();
            Double M = (Math.log(R) / Math.log(2)) - (Math.log(G) / Math.log(2));
            Double A;
            if (R == 0) {
                A = (Math.log(G) / Math.log(2));
            } else {
                if (G == 0) {
                    A = (Math.log(R) / Math.log(2));
                } else {
                    A = ((Math.log(R) / Math.log(2)) + (Math.log(G) / Math.log(2))) / 2;
                }
            }
            //Values have to be added in other order then one would think, because
            //the A value is shown on the X-Axis and the M value on the Y-Axis. So at
            //this point the values are in correct order for plotting, meaning that 
            //the value corresponding to the X-Axis is the first and the one corresponding
            //to the Y-Axis is the secound one.
            ret.put(key, new Pair<>(A, M));
        }
        return ret;
    }

    private static Map<PersistantFeature, Pair<Double, Double>> convertBaySeqResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Map<PersistantFeature, Pair<Double, Double>> convertDESeqResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Map<PersistantFeature, Pair<Double, Double>> convertSimpleTestResults(Vector<Vector> resultTable) {
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        //For GNU R Version:
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            PersistantFeature key = (PersistantFeature) row.get(0);
            Double X = (Double) row.get(3);
            Double Y = (Double) row.get(5);
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.put(key, values);
        }
        return ret;
    }
}
