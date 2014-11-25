package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.util.Observer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jsc.distributions.Normal;

/**
 * This class is calculated and includes all the statistical values ​​from a
 * mapping dataset.
 *
 * @author jritter
 */
public class StatisticsOnMappingData implements Observer {

    private double bgThreshold;
    /*
     * uniqueCounts are just counted mappingResults in font of CDSs
     * totalCounts are all counted mappingResults
     * basetotal are counted bases in all mappingResults together
     */
    private int totalCount, uniqueCounts, basetotal;
    private double mml, mm, mc;
    /*
     * arrays used to store the positions of regionsccounts
     * */
    private int[][] fwdReadStarts, revReadStarts;
    private int[][] fwdCoverage, revCoverage;
    private List<int[]> region2Exclude;
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;
    /**
     * Key, FeatureID of the first Feature,
     */
    private HashMap<Integer, OperonAdjacency> putativeOperonAdjacenciesFWD, putativeOperonAdjacenciesREV;
    private final PersistantReference refGenome;

    public StatisticsOnMappingData(PersistantReference refGenome, double fraction, HashMap<Integer, List<Integer>> forwardCDSs,
            HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, PersistantFeature> allRegionsInHash, List<int[]> region2Exclude) {

        this.refGenome = refGenome;
        this.totalCount = 0;
        this.uniqueCounts = 0;
        this.basetotal = 0;
        int chromCount = refGenome.getNoChromosomes();
        this.fwdReadStarts = new int[chromCount][];
        this.revReadStarts = new int[chromCount][];
        this.fwdCoverage = new int[chromCount][];
        this.revCoverage = new int[chromCount][];

        Map<Integer, PersistantChromosome> chroms = refGenome.getChromosomes();
        for (PersistantChromosome chrom : chroms.values()) {
            int chromId = chrom.getId();
            int chromNo = refGenome.getChromosome(chromId).getChromNumber();
            this.fwdReadStarts = new int[chromNo][chrom.getLength()];
            this.revReadStarts = new int[chromNo][chrom.getLength()];
            this.fwdCoverage = new int[chromNo][chrom.getLength()];
            this.revCoverage = new int[chromNo][chrom.getLength()];
        }
        
        this.forwardCDSs = forwardCDSs;
        this.reverseCDSs = reverseCDSs;
        this.allRegionsInHash = allRegionsInHash;
        this.region2Exclude = region2Exclude;
        this.putativeOperonAdjacenciesFWD = new HashMap<>();
        this.putativeOperonAdjacenciesREV = new HashMap<>();
    }

    /**
     * This Constructor needet for import of existing analysis tables.
     *
     * @param refGenome Persistant Reference.
     * @param mml Mean mapping length.
     * @param mm Mean mapping.
     * @param mc Mapping count.
     * @param bg Backgrount threshold.
     */
    public StatisticsOnMappingData(PersistantReference refGenome, double mml, double mm, double mc, double bg) {
        this.refGenome = refGenome;
        this.mml = mml;
        this.mm = mm;
        this.mc = mc;
        this.bgThreshold = bg;
    }

    /**
     * Initializes the three statistic values mean-mapping-length, mean-mappings
     * count and mapping count.
     */
    public void initMappingsStatistics() {
        this.mml = basetotal / totalCount;
        this.mm = uniqueCounts / 1000000;
        this.mc = basetotal / 1000000;
    }

    /**
     * Parses all mappingResults of one Track. Generates a forward and reverse
     * CDS mapping Arrays, where the information of the covered features is
     * stored. It also counts all unique mappingResults and the total base count
     * of the mappingResults. By the way it cunstructs a List with
     * OperonAdjacencies for further analyses.
     *
     * @param mappingResults List of PersistenMappings.
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
//    public void parseMappings(List<MappingResultPersistant> mappingResults) {
    private void parseMappings(MappingResultPersistant result) {

        // Sorting all mappingResults
        int chromId = result.getRequest().getChromId();
        int chromNo = refGenome.getChromosome(chromId).getChromNumber();

        List<PersistantMapping> mappings = result.getMappings();
        Collections.sort(mappings);

        for (PersistantMapping mapping : mappings) {
            this.totalCount++;
            int start = mapping.getStart();
            int stop = mapping.getStop();

            boolean isFwd = mapping.isFwdStrand();

            // count only non t/rRNA mappingResults
            if (region2Exclude.get(chromNo - 1)[start] == 0) {
                this.uniqueCounts++;
            }
            // count the bases in total
            if (region2Exclude.get(chromNo - 1)[start] == 0 || region2Exclude.get(chromNo - 1)[stop] == 0) {
                this.basetotal += stop - start + 1;
            }

            //	# sum up the total coverage at each positions
            //	# (this is needed for extending genes later)
            if (isFwd) {
                fwdReadStarts[chromNo - 1][start]++;
                for (int i = start; i < stop; i++) {// map {$_++} @{$coverage{fwd}}[$sstart..$sstop];
                    fwdCoverage[chromNo - 1][i]++;
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
                revReadStarts[chromNo - 1][stop]++;
                for (int i = start; i < stop; i++) {// map {$_++} @{$coverage{rev}}[$sstart..$sstop];
                    revCoverage[chromNo - 1][i]++;
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


    /**
     * Calculates the inverse of the normal distribution. The mean is count of
     * unique mappingResults divided by the length of the reference * 2. The
     * variance is equals mean.
     *
     * @param fraction Fraction needed for calculation of allowed false
     * pasitives.
     * @return a threshold.
     */
    public double calculateBackgroundCutoff(double fraction) {
//        int length = refSeqLength * 2;
        int wholeGenomeLength = PersistantReference.calcWholeGenomeLength(refGenome.getChromosomes());
        double mean = (double) this.uniqueCounts / wholeGenomeLength;
        double standardDiviation = Math.sqrt(mean);
        double inverseCdf = 0;
        jsc.distributions.Normal normal = new Normal(mean, standardDiviation);

        inverseCdf = normal.inverseCdf(1 - (fraction / 1000));

        return inverseCdf;
    }

    /**
     * Return the background threshold value.
     *
     * @return background threshold
     */
    public double getBgThreshold() {
        return this.bgThreshold;
    }

    /**
     * 
     * @return 
     */
    public int[][] getForwardReadStarts() {
        return fwdReadStarts;
    }

    /**
     * 
     * @return 
     */
    public int[][] getReverseReadStarts() {
        return revReadStarts;
    }

    /**
     * 
     * @return 
     */
    public int[][] getFwdCoverage() {
        return fwdCoverage;
    }

    /**
     * 
     * @return
     */
    public int[][] getRevCoverage() {
        return revCoverage;
    }

    /**
     * 
     * @return 
     */
    public HashMap<Integer, OperonAdjacency> getPutativeOperonAdjacenciesFWD() {
        return putativeOperonAdjacenciesFWD;
    }

    /**
     * 
     * @return 
     */
    public HashMap<Integer, OperonAdjacency> getPutativeOperonAdjacenciesREV() {
        return putativeOperonAdjacenciesREV;
    }

    /**
     * 
     * @return 
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * 
     * @return 
     */
    public int getUniqueCounts() {
        return uniqueCounts;
    }

    /**
     * 
     * @return 
     */
    public int getBasetotal() {
        return basetotal;
    }

    @Override
    public void update(Object args) {
        if (args instanceof MappingResultPersistant) {
            MappingResultPersistant result = (MappingResultPersistant) args;
            this.parseMappings(result);
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

    public void setBgThreshold(double bg) {
        this.bgThreshold = bg;
    }
    
    /**
     * 
     */
    public void clearMemory() {
        this.allRegionsInHash = null;
        this.forwardCDSs = null;
        this.reverseCDSs = null;
        this.fwdCoverage = null;
        this.revCoverage = null;
        this.fwdReadStarts = null;
        this.revReadStarts = null;
        this.putativeOperonAdjacenciesFWD = null;
        this.putativeOperonAdjacenciesREV = null;
        this.region2Exclude = null;
    }
}
