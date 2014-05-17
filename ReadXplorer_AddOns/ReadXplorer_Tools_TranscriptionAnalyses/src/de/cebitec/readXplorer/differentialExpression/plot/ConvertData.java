/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.differentialExpression.plot;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.differentialExpression.DeAnalysisHandler;
import static de.cebitec.readXplorer.differentialExpression.DeAnalysisHandler.Tool.BaySeq;
import static de.cebitec.readXplorer.differentialExpression.DeAnalysisHandler.Tool.DeSeq;
import static de.cebitec.readXplorer.differentialExpression.DeAnalysisHandler.Tool.ExpressTest;
import de.cebitec.readXplorer.differentialExpression.ResultDeAnalysis;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Pair;
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
        return createDataPairForFeature(result.getTableContents(), 0, 7, 9);
    }

    public static Map<PersistantFeature, Pair<Double, Double>> ratioBAagainstConfidence(ResultDeAnalysis result) {
        return createDataPairForFeature(result.getTableContents(), 0, 8, 9);
    }

    public static Map<PersistantFeature, Pair<Double, Double>> createMAvalues(ResultDeAnalysis result, DeAnalysisHandler.Tool usedTool, Integer[] sampleA, Integer[] sampleB) {
        Map<PersistantFeature, Pair<Double, Double>> input = new HashMap<>();
        switch (usedTool) {
            case BaySeq:
                input = convertBaySeqResults(result.getTableContents(), sampleA, sampleB);
                break;
            case DeSeq:
                input = createDataPairForFeature(result.getTableContents(), 0, 3, 4);
                break;
            case ExpressTest:
                input = createDataPairForFeature(result.getTableContents(), 0, 4, 6);
                break;
        }
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        for (Iterator<PersistantFeature> it = input.keySet().iterator(); it.hasNext();) {
            PersistantFeature key = it.next();
            Pair<Double, Double> pair = input.get(key);
            Double R = pair.getFirst();
            Double G = pair.getSecond();
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
                //to the Y-Axis is the second one.
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
                X += GeneralUtils.convertNumber(Double.class, (Number) row.get(index));
            }
            X /= sampleA.length;
            Double Y = 0d;
            for (int i = 0; i < sampleB.length; i++) {
                int index = sampleB[i] + BAY_SEQ_OFFSET;
                Y += GeneralUtils.convertNumber(Double.class, (Number) row.get(index));
            }
            Y /= sampleB.length;
            Pair<Double, Double> values = new Pair<>(X, Y);
            ret.put(key, values);
        }
        return ret;
    }
    
    /**
     * Creates a map of the PersistantFeatures in the resultTable to a pair of
     * Double values. E.g. this method can be used to get a data point
     * associated to the genomic features.
     * @param resultTable the table to iterate through
     * @param columnFeature the column, in which the PersistantFeatures are stored
     * @param column1 the column of the x value of the data point associated with a PersistantFeature
     * @param column2 the column of the y value of the data point associated with a PersistantFeature
     * @return 
     */
    private static Map<PersistantFeature, Pair<Double, Double>> createDataPairForFeature(Vector<Vector> resultTable, int columnFeature, int column1, int column2) {
        Map<PersistantFeature, Pair<Double, Double>> ret = new HashMap<>();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            PersistantFeature key = (PersistantFeature) row.get(columnFeature);
            Double x = (Double) row.get(column1);
            Double y = (Double) row.get(column2);
            Pair<Double, Double> values = new Pair<>(x, y);
            ret.put(key, values);
        }
        return ret;
    }
}
