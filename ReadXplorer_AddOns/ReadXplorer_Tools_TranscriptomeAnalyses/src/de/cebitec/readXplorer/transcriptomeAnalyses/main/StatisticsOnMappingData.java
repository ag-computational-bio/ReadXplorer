package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.Observer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import jsc.distributions.Normal;

/**
 * This class calculates and includes all statistical values ​​from a mapping
 * dataset.
 *
 * @author jritter
 */
public class StatisticsOnMappingData implements Observer {

    private double bgThreshold;
    private double totalCount, uniqueCounts, baseInTotal;
    private double meanMappingLength, mappingsPerMillion, mappingCount;
    private int[][] fwdReadStarts, revReadStarts;
    private int[][] fwdCoverage, revCoverage;
    private List<int[]> region2Exclude;
    protected HashMap<Integer, List<Integer>> fwdFeatures, revFeatures;
    protected List<List<List<Integer>>> fwdFeatureIds, revFeatureIds;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allFeatures;
    /**
     * Key, FeatureID of the first Feature,
     */
    private TreeMap<Integer, OperonAdjacency> putativeOperonAdjacenciesFWD, putativeOperonAdjacenciesREV;
    private final PersistantReference refGenome;

    /**
     * Constructor for this class.
     *
     * @param refGenome
     * @param fraction
     * @param forwardFeatures
     * @param reverseFeatures
     * @param allFeatures
     * @param region2Exclude
     */
    public StatisticsOnMappingData(PersistantReference refGenome, double fraction, HashMap<Integer, List<Integer>> forwardFeatures,
            HashMap<Integer, List<Integer>> reverseFeatures, HashMap<Integer, PersistantFeature> allFeatures, List<int[]> region2Exclude) {

        this.refGenome = refGenome;
        this.totalCount = 0;
        this.uniqueCounts = 0;
        this.baseInTotal = 0;
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

        this.fwdFeatures = forwardFeatures;
        this.revFeatures = reverseFeatures;
        this.allFeatures = allFeatures;
        this.region2Exclude = region2Exclude;
        this.putativeOperonAdjacenciesFWD = new TreeMap<>();
        this.putativeOperonAdjacenciesREV = new TreeMap<>();
    }

    /**
     * This Constructor needed for import of existing analysis tables.
     *
     * @param refGenome Persistant Reference.
     * @param mml Mean mapping length.
     * @param mm Mean per million.
     * @param mc Mapping count.
     * @param bg Backgrount threshold.
     */
    public StatisticsOnMappingData(PersistantReference refGenome, double mml, double mm, double mc, double bg) {
        this.refGenome = refGenome;
        this.meanMappingLength = mml;
        this.mappingsPerMillion = mm;
        this.mappingCount = mc;
        this.bgThreshold = bg;
    }

    /**
     * Initializes the three statistic values mean-mapping-length, mean-mappings
     * count and mapping count.
     */
    public void initMappingsStatistics() {
        this.meanMappingLength = baseInTotal / totalCount;
        this.mappingsPerMillion = uniqueCounts / 1000000.0;
        this.mappingCount = baseInTotal / 1000000.0;
    }

    /**
     * Parses all mappingResults of one Track. It also counts all unique
     * mappingResults and the total base count of the mappingResults. By the way
     * it cunstructs a List with OperonAdjacencies for further analyses.
     *
     * @param result MappingResultPersistant contains List of PersistenMappings.
     */
    private void parseMappings(MappingResultPersistant result) {

        // Sorting all mappingResults
        int chromId = result.getRequest().getChromId();
        int chromNo = refGenome.getChromosome(chromId).getChromNumber();
        List<PersistantMapping> mappings = result.getMappings();
        Collections.sort(mappings);
        int length = region2Exclude.get(chromNo - 1).length;
        for (PersistantMapping mapping : mappings) {
            this.totalCount++;
            int start = mapping.getStart();
            int stop = mapping.getStop();
            if (stop == length) {
                stop -= 1;
            }

            boolean isFwd = mapping.isFwdStrand();

            // count only non t/rRNA mappingResults
            if (region2Exclude.get(chromNo - 1)[start] == 0 && region2Exclude.get(chromNo - 1)[stop] == 0) {
                this.uniqueCounts++;
            }

            // count the bases in total
            if (region2Exclude.get(chromNo - 1)[start] == 0 || region2Exclude.get(chromNo - 1)[stop] == 0) {
                this.baseInTotal += stop - start + 1;
            }

            //	sum up the total coverage at each positions
            if (isFwd) {
                fwdReadStarts[chromNo - 1][start]++;
                for (int i = start; i < stop; i++) {
                    fwdCoverage[chromNo - 1][i]++;
                }
                checkOperonAdjacency(putativeOperonAdjacenciesFWD, fwdFeatures, stop, start);
            } else {
                revReadStarts[chromNo - 1][stop]++;
                for (int i = start; i < stop; i++) {
                    revCoverage[chromNo - 1][i]++;
                }
                checkOperonAdjacency(putativeOperonAdjacenciesREV, revFeatures, stop, start);
            }
        }
    }

    /**
     *
     * @param putativeOperonAdjacencies
     * @param features
     * @param stop
     * @param start
     */
    private void checkOperonAdjacency(TreeMap<Integer, OperonAdjacency> putativeOperonAdjacencies, HashMap<Integer, List<Integer>> features, int stop, int start) {
        PersistantFeature feat1;
        PersistantFeature feat2;
        if (features.containsKey(Integer.valueOf(stop)) && features.containsKey(Integer.valueOf(start))) {
            for (int featureIDrev1 : features.get(start)) {
                feat1 = allFeatures.get(featureIDrev1);

                for (int featureIDrev2 : features.get(stop)) {
                    feat2 = allFeatures.get(featureIDrev2);

                    if (feat1.getType() != FeatureType.MISC_RNA && feat2.getType() != FeatureType.MISC_RNA && featureIDrev1 != featureIDrev2) {
                        if (putativeOperonAdjacencies.get(featureIDrev1) != null) {
                            putativeOperonAdjacencies.get(featureIDrev1).setSpanningReads(putativeOperonAdjacencies.get(featureIDrev1).getSpanningReads() + 1);
                        } else {
                            OperonAdjacency operonAdj = new OperonAdjacency(feat1, feat2);
                            operonAdj.setSpanningReads(operonAdj.getSpanningReads() + 1);
                            putativeOperonAdjacencies.put(featureIDrev1, operonAdj);
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
        System.out.println("WholeGenomeLength: " + wholeGenomeLength);
        double mean = (double) this.uniqueCounts / (wholeGenomeLength * 2);
        System.out.println("uniqueMappings: " + this.uniqueCounts);
        System.out.println("Mean: " + mean);
        double standardDiviation = Math.sqrt(mean);
        System.out.println("StandardAbweichung: " + standardDiviation);
        double inverseCdf = 0;
        jsc.distributions.Normal normal = new Normal(mean, standardDiviation);
        inverseCdf = normal.inverseCdf(1 - (fraction / 1000));

        System.out.println("BG: " + inverseCdf);
        return inverseCdf;
    }

    public int simulateBackgroundThreshold(double fraction) {
        int genomeSize = PersistantReference.calcWholeGenomeLength(refGenome.getChromosomes());
        int doubleGenomeSize = genomeSize * 2;
        int maxRandomVariableValue = 0; // Das ist der Wert der größten Zufallsvariable
        int backgroundCutoff = 0;

        int bgtotal = 0;
        int bgcount;

        int[] bin = new int[doubleGenomeSize];
        HashMap<Integer, Integer> relativeCountsOfRandomVariables = new HashMap<>();

        Random generator = new Random();
        for (int j = 0; j < uniqueCounts; j++) {
            int randomInt = generator.nextInt(doubleGenomeSize);
            bin[randomInt]++;
            //System.out.println("randomInt: "+randomInt);
        }

        // dermining Maximum coverage count for a mapping
        for (int pos = 0; pos < doubleGenomeSize; pos++) {
            bgcount = bin[pos]; // Zufallsvariable an Stelle pos
            if (relativeCountsOfRandomVariables.containsKey(bgcount)) {
                relativeCountsOfRandomVariables.put(bgcount, relativeCountsOfRandomVariables.get(bgcount) + 1);
            } else {
                relativeCountsOfRandomVariables.put(bgcount, 1);
            }
            if (bgcount > maxRandomVariableValue) {
                maxRandomVariableValue = bgcount;
            }
        }

        double mean = (uniqueCounts / doubleGenomeSize); // y number of mappings, x genomesize y/x = mü and s^2 Erwartungswert und Varianz!
        System.out.println("Limit: " + (int) (genomeSize / 1000 * fraction));
        for (int num = maxRandomVariableValue; num >= 0; num--) {
            // # we expect ~ one start per kb, and we want less than $fraction false positives

            System.out.println("Num: " + num);

            if (bgtotal >= ((int) (genomeSize / 1000 * fraction))) {
                break;
            }

            if (relativeCountsOfRandomVariables.containsKey(num)) {
                bgtotal += relativeCountsOfRandomVariables.get(num);
                backgroundCutoff = num;
                System.out.println("BackgroundCutoff: " + backgroundCutoff);
                System.out.println("count of BackgroundCutoff: " + relativeCountsOfRandomVariables.get(num));
                System.out.println("BackgroundTotal: " + bgtotal);
            } else {
                System.out.println("Num nicht im Hash: " + num);
            }

        }
        return backgroundCutoff;
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

    public void setFwdReadStarts(int[][] fwdReadStarts) {
        this.fwdReadStarts = fwdReadStarts;
    }

    public void setRevReadStarts(int[][] revReadStarts) {
        this.revReadStarts = revReadStarts;
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
     * @return hash representative of putative forward operon adjacencies.
     */
    public TreeMap<Integer, OperonAdjacency> getPutativeOperonAdjacenciesFWD() {
        return putativeOperonAdjacenciesFWD;
    }

    /**
     *
     * @return hash representative of putative reverse operon adjacencies.
     */
    public TreeMap<Integer, OperonAdjacency> getPutativeOperonAdjacenciesREV() {
        return putativeOperonAdjacenciesREV;
    }

    /**
     * Returns the number of total number of mapped reads.
     *
     * @return total number of mapped reads.
     */
    public double getTotalCount() {
        return totalCount;
    }

    /**
     * Returns the number of unique mapped reads.
     *
     * @return number of unique mapped reads.
     */
    public double getUniqueCounts() {
        return uniqueCounts;
    }

    /**
     * Retursn the total number of bases of mapped reads.
     *
     * @return total number of bases of mapped reads.
     */
    public double getBaseInTotal() {
        return baseInTotal;
    }

    @Override
    public void update(Object args) {
        if (args instanceof MappingResultPersistant) {
            MappingResultPersistant result = (MappingResultPersistant) args;
            this.parseMappings(result);
        }
    }

    /**
     * Returns the mean mapping length of all mapped reads.
     *
     * @return the mean mapping length
     */
    public double getMeanMappingLength() {
        return meanMappingLength;
    }

    /**
     * Returns the number of mapped reads per million nucleotides.
     *
     * @return the number of mapped reads per million nucleotides
     */
    public double getMappingsPerMillion() {
        return mappingsPerMillion;
    }

    /**
     * Returns the mapping count.
     *
     * @return mapping count.
     */
    public double getMappingCount() {
        return mappingCount;
    }

    /**
     * Sets the background threshold.
     *
     * @param bg threshold
     */
    public void setBgThreshold(double bg) {
        this.bgThreshold = bg;
    }

    /**
     *
     */
    public void clearMemory() {
        this.allFeatures = null;
        this.fwdFeatures = null;
        this.revFeatures = null;
        this.fwdCoverage = null;
        this.revCoverage = null;
        this.fwdReadStarts = null;
        this.revReadStarts = null;
        this.putativeOperonAdjacenciesFWD = null;
        this.putativeOperonAdjacenciesREV = null;
        this.region2Exclude = null;
    }
}
