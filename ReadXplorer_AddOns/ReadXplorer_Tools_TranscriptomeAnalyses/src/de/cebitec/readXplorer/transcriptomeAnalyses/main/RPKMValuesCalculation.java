package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.RPKMvalue;
import de.cebitec.readXplorer.util.FeatureType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author jritter
 */
public class RPKMValuesCalculation {

    //my %results;
    private List<RPKMvalue> rpkmValues;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;
    private StatisticsOnMappingData stats;
    private final int[][] forwardStarts, reverseStarts;
    private final int[][] forwardCoverage, reverseCoverage;
    private final double mm, mc;
    private int trackId;

    public RPKMValuesCalculation(HashMap<Integer, PersistantFeature> persFeatures, StatisticsOnMappingData stats, int trackId) {
        this.allRegionsInHash = persFeatures;
        this.rpkmValues = new ArrayList<>();
        this.stats = stats;
        this.forwardStarts = this.stats.getForwardReadStarts();
        this.reverseStarts = this.stats.getReverseReadStarts();
        this.forwardCoverage = this.stats.getFwdCoverage();
        this.reverseCoverage = this.stats.getRevCoverage();
        this.mm = this.stats.getMm();
        this.mc = this.stats.getMc();
        this.trackId = trackId;

    }

    public void calculationExpressionValues(PersistantReference refGenome) {

        Map<Integer, PersistantFeature> allRegionsSorted = new TreeMap<>(this.allRegionsInHash);
        Set<Integer> keys = allRegionsSorted.keySet();
        PersistantFeature feature;
        HashMap<Integer, PersistantChromosome> chromosomes = (HashMap<Integer, PersistantChromosome>) refGenome.getChromosomes();

        for (Integer id : keys) {
            feature = allRegionsSorted.get(id);
            if (feature.getType() == FeatureType.RRNA || feature.getType() == FeatureType.TRNA) {
                continue;
            }

            int start = feature.getStart();
            int stop = feature.getStop();
            boolean isFwd = feature.isFwdStrand();
            int chromId = feature.getChromId();
            int chromNo = chromosomes.get(chromId).getChromNumber();
            RPKMvalue rpkm = null;
            if (isFwd) {
                System.out.println("Feature fwd: " + feature.getName());
                rpkm = this.calculateStatistics(chromNo, chromId, start, stop, forwardStarts, forwardCoverage, this.mm, this.mc);
                rpkm.setFeature(feature);
                rpkmValues.add(rpkm);
            } else {
                System.out.println("Feature rev: " + feature.getName());
                rpkm = this.calculateStatistics(chromNo, chromId, start, stop, reverseStarts, reverseCoverage, this.mm, this.mc);
                rpkm.setFeature(feature);
                rpkmValues.add(rpkm);
            }
        }
    }

    /**
     *
     * @param start Startposition of analyzed feature.
     * @param stop Stopposition of analyzed feature.
     * @param starts
     * @param covered
     * @param mm number of mapped reads per Million.
     * @param mc
     * @return RPKMvalue object with all rpkm values.
     */
    private RPKMvalue calculateStatistics(int chromNo, int chromId, int start, int stop, int[][] starts, int[][] covered, double mm, double mc) {

        int length = stop - start;
        List<Double> logdata = new ArrayList<>();
        double rpkm = 0;
        double logRpkm = 0;
        double coverageRpkm = 0;
        double sumLog = 0;
        int covsum = 0;
        double covsumlog = 0;
        double coverageLogRpkm = 0;

        // Here we count the occurrences of start position which are not empty (count)
        // and add up the number of mappings per start (sum)
        // Also generate an array with logarithmic values (number of mappings per start)
        int count = 0;
        int sum = 0;
        for (int i = start; i < stop; i++) {
            int j = starts[chromNo - 1][i];
            if (j != 0) {
                count++;
                sum += j;
                Integer integer = new Integer(j);
                double logValue = Math.log(integer.doubleValue());
                sumLog += logValue;
                logdata.add(logValue);
            }
        }

        double relcov = (count / length * 100);
        rpkm = sum / length * 1000 / mm;

        // ===================================================================

        double mean = mean(logdata);
        if (sumLog > 0) {
            logRpkm = Math.exp(mean) * count / length * 1000 / mm;
        }

        // ===================================================================

        List<Double> covLogdata = new ArrayList<>();
        List<Double> sortedCoveredArr = new ArrayList<>();
        count = 0;
        for (int i = start; i < stop; i++) {
            int j = covered[chromNo - 1][i];
            if (j != 0) {
                count++;
                covsum += j;
                Integer integer = new Integer(j);
                double logValue = Math.log(integer.doubleValue());
                sortedCoveredArr.add(integer.doubleValue());
                covLogdata.add(logValue);
                covsumlog += logValue;
            }
        }

        Collections.sort(sortedCoveredArr);
        double covmedian = 0.0;
        if (!sortedCoveredArr.isEmpty()) {
            covmedian = (median(sortedCoveredArr) * 1000 / mc);
        }

        coverageRpkm = (covsum / length * 1000 / mc);


//    #===============================================================================	   

        if (covsumlog != 0) {
            coverageLogRpkm = Math.exp(mean(covLogdata) * count / length * 1000 / mc);
        }

        return new RPKMvalue(null, rpkm, logRpkm, coverageRpkm, coverageLogRpkm, sum, covsum, this.trackId , chromId);
    }

    /**
     * the array double[] m MUST BE SORTED
     *
     */
    private double median(List<Double> m) {
        int length = m.size();
        System.out.println("Length: " + length);
        int middle = length / 2;
        System.out.println("Middle: " + middle);
        if (length % 2 == 1) {
            return m.get(middle);
        } else {
            return (m.get(middle - 1) + m.get(middle)) / 2.0;
        }
    }

    public List<RPKMvalue> getRpkmValues() {
        return rpkmValues;
    }

    /**
     *
     * @param m
     * @return
     */
    private double mean(List<Double> m) {
        double sum = 0;
        for (int i = 0; i < m.size(); i++) {
            sum += m.get(i);
        }
        return sum / m.size();
    }
}
