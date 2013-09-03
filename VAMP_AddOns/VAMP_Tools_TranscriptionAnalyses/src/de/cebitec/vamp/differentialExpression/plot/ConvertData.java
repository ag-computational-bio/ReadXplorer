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

    private static final int BAY_SEQ_OFFSET = 3;
    private static final int CUT_OFF = 30;

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

    public static Map<PersistantFeature, Pair<Double, Double>> createMAvalues(ResultDeAnalysis result, DeAnalysisHandler.Tool usedTool, Integer[] sampleA, Integer[] sampleB) {
        Map<PersistantFeature, Pair<Double, Double>> input = new HashMap<>();
        switch (usedTool) {
            case BaySeq:
                input = convertBaySeqResults(result.getTableContents(), sampleA, sampleB);
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
            if ((R > CUT_OFF) || (G > CUT_OFF)) {

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
        }
        return ret;
    }

    private static Map<PersistantFeature, Pair<Double, Double>> convertBaySeqResults(Vector<Vector> resultTable, Integer[] sampleA, Integer[] sampleB) {
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            PersistantFeature key = (PersistantFeature) row.get(0);
            Double X = 0d;
            for (int i = 0; i < sampleA.length; i++) {
                int index = sampleA[i] + BAY_SEQ_OFFSET;
                X = X + (Double) row.get(index);
            }
            X = X / sampleA.length;
            Double Y = 0d;
            for (int i = 0; i < sampleB.length; i++) {
                int index = sampleB[i] + BAY_SEQ_OFFSET;
                Y = Y + (Double) row.get(index);
            }
            Y = Y / sampleB.length;
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.put(key, values);
        }
        return ret;
    }

    private static Map<PersistantFeature, Pair<Double, Double>> convertDESeqResults(Vector<Vector> resultTable) {
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            PersistantFeature key = (PersistantFeature) row.get(0);
            Double X = (Double) row.get(2);
            Double Y = (Double) row.get(3);
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.put(key, values);
        }
        return ret;
    }

    private static Map<PersistantFeature, Pair<Double, Double>> convertSimpleTestResults(Vector<Vector> resultTable) {
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
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
