package de.cebitec.vamp.differentialExpression.simpleTest;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.differentialExpression.ProcessingLog;
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
    private Vector<Vector> resultsNormalized;
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
        List<Double> meanCountsA = calculateMeanCountsForEachReplicate(groupA);
        List<Double> meanCountsB = calculateMeanCountsForEachReplicate(groupB);
        Double averageMeanCounts = calculateTotalMeanCount(meanCountsA, meanCountsB);
        Double[] normalizationRatiosA = calculateNormalizationRatios(meanCountsA, averageMeanCounts);
        Double[] normalizationRatiosB = calculateNormalizationRatios(meanCountsB, averageMeanCounts);

        SimpleTest.MeanVarianceGroup meanVarGroupA = computeMeanAndVar(groupA, normalizationRatiosA);
        SimpleTest.MeanVarianceGroup meanVarGroupB = computeMeanAndVar(groupB, normalizationRatiosB);
        Double[] varA = meanVarGroupA.getVar();
        Double[] varB = meanVarGroupB.getVar();
        Double[] meanA = meanVarGroupA.getMean();
        Double[] meanB = meanVarGroupB.getMean();

        Double[] varANormalized = meanVarGroupA.getVarNormalized();
        Double[] varBNormalized = meanVarGroupB.getVarNormalized();
        Double[] meanANormalized = meanVarGroupA.getMeanNormalized();
        Double[] meanBNormalized = meanVarGroupB.getMeanNormalized();

        List<PersistantFeature> regionNamesList = new LinkedList<>();

        results = new Vector<>();
        resultsNormalized = new Vector<>();

        for (int i = 0; i < regionLength; i++) {
            //Filter out regions with low mean values
            if (meanA[i] > cutOff || meanB[i] > cutOff) {
                regionNamesList.add(regionNames[i]);

                Vector currentResult = new Vector();
                Vector currentResultNormalized = new Vector();

                currentResult.add(regionNames[i]);
                currentResultNormalized.add(regionNames[i]);
                currentResult.add(start[i]);
                currentResultNormalized.add(start[i]);
                currentResult.add(stop[i]);
                currentResultNormalized.add(stop[i]);
                currentResult.add(meanA[i]);
                currentResultNormalized.add(meanANormalized[i]);
                currentResult.add(varA[i]);
                currentResultNormalized.add(varANormalized[i]);
                currentResult.add(meanB[i]);
                currentResultNormalized.add(meanBNormalized[i]);
                currentResult.add(varB[i]);
                currentResultNormalized.add(varBNormalized[i]);
                currentResult.add(computeRatio(meanA[i], meanB[i]));
                currentResultNormalized.add(computeRatio(meanANormalized[i], meanBNormalized[i]));
                currentResult.add(computeRatio(meanB[i], meanA[i]));
                currentResultNormalized.add(computeRatio(meanBNormalized[i], meanANormalized[i]));
                if (groupA.length < 2 && groupB.length < 2) {
                    currentResult.add(new Double(-1.0d));
                } else {
                    currentResult.add(computeConfidence(meanA[i], meanB[i], varA[i], varB[i]));
                }
                if (groupA.length < 2 && groupB.length < 2) {
                    currentResultNormalized.add(new Double(-1.0d));
                } else {
                    currentResultNormalized.add(computeConfidence(meanANormalized[i], meanBNormalized[i], varANormalized[i], varBNormalized[i]));
                }
                results.add(currentResult);
                resultsNormalized.add(currentResultNormalized);
            }
        }

        rowNames = new Vector(regionNamesList);

        ProcessingLog log = ProcessingLog.getInstance();
        log.addProperty("Average mean counts", averageMeanCounts);
        log.addProperty("Normalization ratios for group A", normalizationRatiosA);
        log.addProperty("Normalization ratios for group B", normalizationRatiosB);
        notifyObservers(SimpleTestStatus.FINISHED);
    }

    private Double[] calculateNormalizationRatios(List<Double> meanCounts, Double totalMeanCount) {
        Double[] ret = new Double[meanCounts.size()];
        for (int i = 0; i < meanCounts.size(); i++) {
            ret[i] = (totalMeanCount / meanCounts.get(i));
        }
        return ret;
    }

    private Double calculateTotalMeanCount(List<Double> groupA, List<Double> groupB) {
        Double ret = 0d;
        int numberOfReplicates = groupA.size() + groupB.size();
        for (Iterator<Double> it = groupA.iterator(); it.hasNext();) {
            Double double1 = it.next();
            ret = ret + double1;
        }
        for (Iterator<Double> it = groupB.iterator(); it.hasNext();) {
            Double double1 = it.next();
            ret = ret + double1;
        }
        ret = (ret / numberOfReplicates);
        return ret;
    }

    private Double calculateMeanCountForReplicate(int[] replicate) {
        Double ret = 0d;
        int numberOfFeatures = replicate.length;
        for (int i = 0; i < replicate.length; i++) {
            ret = ret + replicate[i];
        }
        return (ret / numberOfFeatures);
    }

    private List<Double> calculateMeanCountsForEachReplicate(int[][] group) {
        List<Double> ret = new LinkedList<>();
        for (int i = 0; i < group.length; i++) {
            ret.add(calculateMeanCountForReplicate(group[i]));
        }
        return ret;
    }

    private SimpleTest.MeanVarianceGroup computeMeanAndVar(int[][] group, Double[] normalizationRatios) {
        Double[] mean = new Double[group[0].length];
        Double[] var = new Double[group[0].length];
        Double[] meanNormalized = new Double[group[0].length];
        Double[] varNormalized = new Double[group[0].length];
        for (int j = 0; j < group[0].length; j++) {
            Double[] rowValues = new Double[group.length];
            Double[] rowValuesNormalized = new Double[group.length];
            for (int i = 0; i < group.length; i++) {
                rowValues[i] = new Double(group[i][j]);
                rowValuesNormalized[i] = new Double((group[i][j] * normalizationRatios[i]));
            }
            mean[j] = mean(rowValues);
            var[j] = round(variance(rowValues));
            meanNormalized[j] = mean(rowValuesNormalized);
            varNormalized[j] = round(variance(rowValuesNormalized));
        }
        return new SimpleTest.MeanVarianceGroup(mean, var, meanNormalized, varNormalized);
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
    public Vector<Vector> getResultsNormalized() {
        return resultsNormalized;
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
        Double confidence;
        if (meanA == 0) {
            meanA = 1d;
        }
        if (meanB == 0) {
            meanB = 1d;
        }
        confidence = -(Math.log10((((varA / meanA) + (varB / meanB)) / 2)));
        return confidence;
    }

    private static class MeanVarianceGroup {

        private Double[] mean;
        private Double[] var;
        private Double[] meanNormalized;
        private Double[] varNormalized;

        public MeanVarianceGroup(Double[] mean, Double[] var, Double[] meanNormalized, Double[] varNormalized) {
            this.mean = mean;
            this.var = var;
            this.meanNormalized = meanNormalized;
            this.varNormalized = varNormalized;
        }

        public Double[] getMean() {
            return mean;
        }

        public Double[] getVar() {
            return var;
        }

        public Double[] getMeanNormalized() {
            return meanNormalized;
        }

        public Double[] getVarNormalized() {
            return varNormalized;
        }
    }
}
