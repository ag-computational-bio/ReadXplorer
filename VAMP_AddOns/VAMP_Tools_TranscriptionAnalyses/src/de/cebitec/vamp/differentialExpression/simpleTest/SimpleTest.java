package de.cebitec.vamp.differentialExpression.simpleTest;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author kstaderm
 */
public class SimpleTest implements SimpleTestI {

    private List<SimpleTestObserver> observers;
    private Vector<Vector> results;
    private Vector rowNames;
    private Vector colNames;
    private Map<Double[], Double> meanCache;

    public SimpleTest() {
        this.meanCache = new HashMap<>();
        this.observers = new LinkedList<>();
        this.colNames = new Vector(Arrays.asList(new String[]{"Region", "Start",
            "Stop", "MeanA", "VarA", "MeanB", "VarB", "RatioAB", "RatioBA", "Confidence"}));
    }

    @Override
    public void performAnalysis(PersistantFeature[] regionNames, int[] start, int[] stop, int[][] groupA,
            int[][] groupB, double cutOff) throws IllegalArgumentException {

        notifyObservers(SimpleTestStatus.RUNNING);

        int regionLength = regionNames.length;
        for (int i = 0; i < groupA.length; i++) {
            if (regionLength != groupA[i].length) {
                notifyObservers(SimpleTestStatus.FAILED);
                throw new IllegalArgumentException("There must be an entry in groupA and groupB for each region!");
            }
        }
        for (int i = 0; i < groupB.length; i++) {
            if (regionLength != groupB[i].length) {
                notifyObservers(SimpleTestStatus.FAILED);
                throw new IllegalArgumentException("There must be an entry in groupA and groupB for each region!");
            }
        }
        if (regionLength != start.length || regionLength != stop.length) {
            notifyObservers(SimpleTestStatus.FAILED);
            throw new IllegalArgumentException("There must be an entry in groupA and groupB for each region!");
        }

        //Compute mean and variance between the replicates of each group
        MeanVarianceGroup meanVarGroupA = computeMeanAndVar(groupA);
        MeanVarianceGroup meanVarGroupB = computeMeanAndVar(groupB);
        Double[] varA = meanVarGroupA.getVar();
        Double[] varB = meanVarGroupB.getVar();
        Double[] meanA = meanVarGroupA.getMean();
        Double[] meanB = meanVarGroupB.getMean();

        List<PersistantFeature> regionNamesList = new LinkedList<>();

        results = new Vector<>();

        for (int i = 0; i < regionLength; i++) {
            //Filter out regions with low mean values
            if (meanA[i] > cutOff || meanB[i] > cutOff) {
                regionNamesList.add(regionNames[i]);

                Vector currentResult = new Vector();
                currentResult.add(regionNames[i]);
                currentResult.add(start[i]);
                currentResult.add(stop[i]);
                currentResult.add(meanA[i]);
                currentResult.add(varA[i]);
                currentResult.add(meanB[i]);
                currentResult.add(varB[i]);
                currentResult.add(computeRatio(meanA[i], meanB[i]));
                currentResult.add(computeRatio(meanB[i], meanA[i]));
                if (groupA.length < 2 && groupB.length < 2) {
                    currentResult.add(new Double(-1.0d));
                } else {
                    currentResult.add(computeConfidence(meanA[i], meanB[i], varA[i], varB[i]));
                }

                results.add(currentResult);
            }
        }

        rowNames = new Vector(regionNamesList);

        notifyObservers(SimpleTestStatus.FINISHED);
    }

    private MeanVarianceGroup computeMeanAndVar(int[][] group) {
        Double[] mean = new Double[group[0].length];
        Double[] var = new Double[group[0].length];
        for (int j = 0; j < group[0].length; j++) {
            Double[] rowValues = new Double[group.length];
            for (int i = 0; i < group.length; i++) {
                rowValues[i] = new Double(group[i][j]);
            }
            mean[j] = mean(rowValues);
            var[j] = round(variance(rowValues));
        }
        return new MeanVarianceGroup(mean, var);
    }

    private Double round(Double d) {
        Double ret;
        long l = Math.round(d);
        ret = new Double(l);
        return ret;
    }

    private Double mean(Double[] values) {
        if (meanCache.containsKey(values)) {
            return meanCache.get(values);
        }
        Double mean = 0d;
        for (int i = 0; i < values.length; i++) {
            mean = mean + values[i];
        }
        mean = (mean / values.length);
        meanCache.put(values, mean);
        return mean;
    }

    private Double variance(Double[] values) {
        Double var = 0d;
        for (int i = 0; i < values.length; i++) {
            var = var + Math.pow((values[i] - mean(values)), 2);
        }
        var = (var / (values.length - 1));
        return var;
    }

    @Override
    public void addObserver(SimpleTestObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(SimpleTestObserver o) {
        observers.remove(o);
    }

    private void notifyObservers(SimpleTestStatus status) {
        for (Iterator<SimpleTestObserver> it = observers.iterator(); it.hasNext();) {
            SimpleTestObserver observer = it.next();
            observer.update(this, status);
        }
    }

    @Override
    public Vector<Vector> getResults() {
        return results;
    }

    @Override
    public Vector getColumnNames() {
        return colNames;
    }

    @Override
    public Vector getRowNames() {
        return rowNames;
    }

    private Double computeRatio(Double one, Double two) {
        if (one == 0) {
            one = 1d;
        }
        if (two == 0) {
            two = 1d;
        }
        return (one / two);
    }

    private Double computeConfidence(Double meanA, Double meanB, Double varA, Double varB) {
        if (meanA == 0) {
            meanA = 1d;
        }
        if (meanB == 0) {
            meanB = 1d;
        }
        Double confidence;
        confidence = -(Math.log10((((varA / meanA) + (varB / meanB)) / 2)));
        return confidence;
    }

    private static class MeanVarianceGroup {

        private Double[] mean;
        private Double[] var;

        public MeanVarianceGroup(Double[] mean, Double[] var) {
            this.mean = mean;
            this.var = var;
        }

        public Double[] getMean() {
            return mean;
        }

        public Double[] getVar() {
            return var;
        }
    }
}
