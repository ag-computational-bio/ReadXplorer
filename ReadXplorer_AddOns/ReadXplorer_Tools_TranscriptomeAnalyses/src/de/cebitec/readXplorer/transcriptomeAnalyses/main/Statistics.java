package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.util.Observer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import jsc.distributions.Normal;

/**
 * This class contains all of the statistics calculations and simulations.
 *
 *
 * @author jritter
 */
public class Statistics implements Observer {

    private double bg;
    List<PersistantMapping> mappings;
    /*
     * uniqueCounts are just counted mappings in font of CDSs
     * totalCounts are all counted mappings
     * basetotal are counted bases in all mappings together
     */
    private int totalCount, uniqueCounts, basetotal;
    /**
     * Key: Start position of mapping, Value: List of mappingIDs
     */
    private HashMap<Integer, List<Long>> mappingsOnRrnAndTrna;
    private double mml, mm, mc;
    /*
     * arrays used to store the positions of regionsccounts
     * */
    private int[] forward, reverse;
    private int[] fwdCoverage, revCoverage;
    private int[] region2Exclude;
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;
    /**
     * Key, FeatureID of the first Feature,
     */
    private HashMap<Integer, OperonAdjacency> putativeOperonAdjacenciesFWD, putativeOperonAdjacenciesREV;

    public Statistics(int refSeqLength, double fraction, HashMap<Integer, List<Integer>> forwardCDSs, HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, PersistantFeature> allRegionsInHash, int[] region2Exclude) {

        this.totalCount = 0;
        this.uniqueCounts = 0;
        this.basetotal = 0;
        this.forward = new int[refSeqLength];
        this.reverse = new int[refSeqLength];
        this.fwdCoverage = new int[refSeqLength];
        this.revCoverage = new int[refSeqLength];
        this.forwardCDSs = forwardCDSs;
        this.reverseCDSs = reverseCDSs;
        this.allRegionsInHash = allRegionsInHash;
        this.region2Exclude = region2Exclude;
        this.mappings = new ArrayList<>();
        this.putativeOperonAdjacenciesFWD = new HashMap<>();
        this.putativeOperonAdjacenciesREV = new HashMap<>();
        this.mappingsOnRrnAndTrna = new HashMap<>();
    }

    /**
     *
     */
    public void initMappingsStatistics() {
        this.mml = basetotal / totalCount;
        this.mm = uniqueCounts / 1000000;
        this.mc = basetotal / 1000000;
    }

    /**
     * Parses all mappings of one Track. Generates a forward and reverse CDS
     * mapping Arrays, where the information of the covered features is stored.
     * It also counts all unique mappings and the total base count of the
     * mappings. By the way it cunstructs a List with OperonAdjacencies for
     * further analyses.
     *
     * @param mappings List of PersistenMappings.
     * @param forwardCDSs List of Lists, whereby the intern List a list of the
     * featureIDs of features in forward direction is, that occure on
     * thatPosition.
     * @param reverseCDSs List of Lists, whereby the intern List a list of the
     * featureIDs of features in forward direction is, that occure on
     * thatPosition.
     * @param allRegionsInHash is a hashMap of Features, whereby the Key the
     * FeatureID of Feature value is.
     * @param refSeqLength Length of the reference genome.
     * @param region2Exclude int[] in genomesize length. 1 on position i means,
     * that on these position a feature occur which we want to exclude.
     */
    public void parseMappings(List<PersistantMapping> mappings) {

        // Sorting all mappings
        Collections.sort(mappings);
        for (PersistantMapping mapping : mappings) {
            this.totalCount++;
            int start = mapping.getStart();
            int stop = mapping.getStop();
            long id = mapping.getId();
            
            boolean directionFWD = mapping.isFwdStrand();

            if (start < stop) {
                // count only non t/rRNA mappings
                if (region2Exclude[start] == 0) {
                    this.uniqueCounts++;
                }
                // count the bases in total
                if (region2Exclude[start] == 0 || region2Exclude[stop] == 0) {
                    this.basetotal += stop - start + 1;
                }
                
                // Hash is in preparation for mappings which covers a r or t RNA 
                // this hashmap needed for excluding TSSs of a r or t RNA
                if (region2Exclude[start] != 0 || region2Exclude[stop] != 0) {
                    int pos;
                    if(region2Exclude[start] != 0) {
                        pos = start;
                    } else {
                        pos = stop;
                    }
                    if(mappingsOnRrnAndTrna.containsKey(pos)) {
                        mappingsOnRrnAndTrna.get(pos).add(id);
                    } else {
                        List<Long> list = new ArrayList<>();
                        list.add(id);
                        mappingsOnRrnAndTrna.put(pos, list);
                    }
                }
                
                
                //	# sum up the total coverage at each positions
                //	# (this is needed for extending genes later)
                if (directionFWD) {
                    this.forward[start]++;
                    for (int i = start; i < stop; i++) {// map {$_++} @{$coverage{fwd}}[$sstart..$sstop];
                        this.fwdCoverage[i]++;
                    }
                    if (forwardCDSs.containsKey(Integer.valueOf(start)) && forwardCDSs.containsKey(Integer.valueOf(stop))) {
                        for (int featureIDfwd1 : forwardCDSs.get(start)) {
                            for (int featureIDfwd2 : forwardCDSs.get(stop)) {
                                if (featureIDfwd1 != featureIDfwd2) {
                                    if (this.putativeOperonAdjacenciesFWD.get(featureIDfwd1) != null) {
                                        this.putativeOperonAdjacenciesFWD.get(featureIDfwd1).setSpanningReads(this.putativeOperonAdjacenciesFWD.get(featureIDfwd1).getSpanningReads() + 1);
                                    } else {
                                        OperonAdjacency operonA = new OperonAdjacency(allRegionsInHash.get(featureIDfwd1), allRegionsInHash.get(featureIDfwd2));
                                        operonA.setSpanningReads(operonA.getSpanningReads() + 1);
                                        this.putativeOperonAdjacenciesFWD.put(featureIDfwd1, operonA);
                                    }
                                }
                            }
                        }
                    }

                } else {
                    this.reverse[stop]++;
                    for (int i = start; i < stop; i++) {// map {$_++} @{$coverage{rev}}[$sstart..$sstop];
                        this.revCoverage[i]++;
                    }

                    if (reverseCDSs.containsKey(Integer.valueOf(stop)) && reverseCDSs.containsKey(Integer.valueOf(start))) {
                        for (int featureIDrev1 : reverseCDSs.get(stop)) {
                            for (int featureIDrev2 : reverseCDSs.get(start)) {
                                if (featureIDrev1 != featureIDrev2) {
                                    if (this.putativeOperonAdjacenciesREV.get(featureIDrev1) != null) {
                                        this.putativeOperonAdjacenciesREV.get(featureIDrev1).setSpanningReads(this.putativeOperonAdjacenciesREV.get(featureIDrev1).getSpanningReads() + 1);
                                    } else {
                                        OperonAdjacency operonA = new OperonAdjacency(allRegionsInHash.get(featureIDrev1), allRegionsInHash.get(featureIDrev2));
                                        operonA.setSpanningReads(operonA.getSpanningReads() + 1);
                                        this.putativeOperonAdjacenciesREV.put(featureIDrev1, operonA);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * TODO not yet implemented right!
     *
     * @param start
     * @param stop
     * @param mm
     * @param mc
     */
    private void calculateStatistics(int start, int stop, int mm, int mc) {

        int length = stop - start;
        if (length < 0) {
            length = -length;
        }
        length++;

// @starts  = ($start < $stop ? @forward[($start)..($stop)] : @reverse[($stop)..($start)]);

        double[] starts = new double[3]; // have to be initialized TODO
        double[] logdata = new double[starts.length];
        for (int i = 0; i < starts.length; i++) {
            logdata[i] = Math.log(starts[i]);
        }

        double sumlog = 0;
        for (double d : logdata) {
            sumlog += d;
        }
        double mean = mean(logdata);

        double sumlogmean = (sumlog > 0 ? (Math.exp(mean) * logdata.length / length * 1000 / mm) : 0);

//    my @covered = ($start < $stop ?  @{$coverage{fwd}}[($start)..($stop)] :  @{$coverage{rev}}[($stop)..($start)]);
        double[] covered = new double[length]; // TODO initialize right!

        double covsum = 0;
        for (double d : covered) {
            covsum += d;
        }

        // TODO covered must be sorted!!
        double covmedian = (this.median(covered) * 1000 / mc);
        double covsummean = (covsum / length * 1000 / mc);
    }

    /**
     * Calculates the inverse of the normal distribution. The mean is count of
     * unique mappings divided by the length of the reference * 2. The variance
     * is equals mean.
     *
     * @param fraction Fraction needed for calculation of allowed false
     * pasitives.
     * @param refSeqLength length of reference genome.
     * @return
     */
    public double calculateBackgroundCutoff(double fraction, int refSeqLength) {
//        int length = refSeqLength * 2;
        double mean = (double) this.uniqueCounts / refSeqLength;
        double standardDiviation = Math.sqrt(mean);
        double inverseCdf = 0;
        jsc.distributions.Normal normal = new Normal(mean, standardDiviation);

        inverseCdf = normal.inverseCdf(1 - (fraction / 1000));

//    warn "Background cutoff is calculated to be $bg, but is manually set to $opt_f...\n";
//    $bg = $opt_f;
//    warn "Background cutoff is calculated to be $bg...\n";
//    warn "Average and variance: $mean...\n";

        return inverseCdf;

    }

    /**
     *
     * @param m
     * @return
     */
    private double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    /**
     * the array double[] m MUST BE SORTED
     *
     */
    private double median(double[] m) {
        int middle = m.length / 2;
        if (m.length % 2 == 1) {
            return m[middle];
        } else {
            return (m[middle - 1] + m[middle]) / 2.0;
        }
    }

    public double getBg() {
        return this.bg;
    }

    public int[] getForward() {
        return forward;
    }

    public int[] getReverse() {
        return reverse;
    }

    public int[] getFwdCoverage() {
        return fwdCoverage;
    }

    public int[] getRevCoverage() {
        return revCoverage;
    }

    public HashMap<Integer, OperonAdjacency> getPutativeOperonAdjacenciesFWD() {
        return putativeOperonAdjacenciesFWD;
    }

    public HashMap<Integer, OperonAdjacency> getPutativeOperonAdjacenciesREV() {
        return putativeOperonAdjacenciesREV;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getUniqueCounts() {
        return uniqueCounts;
    }

    public int getBasetotal() {
        return basetotal;
    }

    public List<PersistantMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<PersistantMapping> mappings) {
        this.mappings.addAll(mappings);
    }

    @Override
    public void update(Object args) {
        if (args instanceof MappingResultPersistant) {
            MappingResultPersistant results = (MappingResultPersistant) args;
//            parseMappings(results.getMappings());
            setMappings(results.getMappings());
        }
    }

    public double getMml() {
        return mml;
    }

    public double getMm() {
        return mm;
    }

    public double getMc() {
        return mc;
    }

    public HashMap<Integer, List<Long>> getMappingsOnRrnAndTrna() {
        return mappingsOnRrnAndTrna;
    }

    public void setBg(double bg) {
        this.bg = bg;
    }
}
