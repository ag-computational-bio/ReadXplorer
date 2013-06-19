package de.cebitec.vamp.differentialExpression.plot;

import de.cebitec.vamp.differentialExpression.DeAnalysisHandler;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.BaySeq;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.DeSeq;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.SimpleTest;
import de.cebitec.vamp.differentialExpression.ResultDeAnalysis;
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

    public static List<Pair<Double, Double>> ratioABagainstConfidence(ResultDeAnalysis result) {
        List<Pair<Double, Double>> ret = new ArrayList<>();
        Vector resultTable = result.getTableContents();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            Double X = (Double) row.get(7);
            Double Y = (Double) row.get(9);
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.add(values);
        }
        return ret;
    }

    public static List<Pair<Double, Double>> ratioBAagainstConfidence(ResultDeAnalysis result) {
        List<Pair<Double, Double>> ret = new ArrayList<>();
        Vector resultTable = result.getTableContents();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            Double X = (Double) row.get(8);
            Double Y = (Double) row.get(9);
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.add(values);
        }
        return ret;
    }

    public static List<Pair<Double, Double>> createMAvalues(ResultDeAnalysis result, DeAnalysisHandler.Tool usedTool) {
        List<Pair<Double, Double>> input = new ArrayList<>();
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
        List<Pair<Double, Double>> ret = new ArrayList<>();
        for (Iterator<Pair<Double, Double>> it = input.iterator(); it.hasNext();) {
            Pair<Double, Double> pair = it.next();
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
            ret.add(new Pair<>(A, M));
        }
        return ret;
    }

    private static List<Pair<Double, Double>> convertBaySeqResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static List<Pair<Double, Double>> convertDESeqResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static List<Pair<Double, Double>> convertSimpleTestResults(Vector<Vector> resultTable) {
        List<Pair<Double, Double>> ret = new ArrayList<>();
        //For GNU R Version:
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            Double X = (Double) row.get(3);
            Double Y = (Double) row.get(5);
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.add(values);
        }
        return ret;
    }
}
