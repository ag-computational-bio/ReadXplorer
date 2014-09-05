package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistent;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.Mapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.util.classification.FeatureType;
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
     * Key: featureID , Value: PersistentFeature
     */
    private HashMap<Integer, PersistentFeature> allFeatures;
    /**
     * Key, FeatureID of the first Feature,
     */
    private TreeMap<Integer, OperonAdjacency> putativeOperonAdjacenciesFWD, putativeOperonAdjacenciesREV;
    private final PersistentReference refGenome;

    /**
     * Constructor for test cases.
     */
    public StatisticsOnMappingData() {
        this.refGenome = null;
    }

    /**
     * Constructor for this class.
     *
     * @param refGenome PersistentReference.
     * @param fraction
     * @param forwardFeatures all reference features in forward direction. <key>
     * startposition on the reference, <value> list of different (overlapping)
     * feature ids on that position
     * @param reverseFeatures all reference features in reverse direction. <key>
     * startposition on the reference, <value> list of different (overlapping)
     * feature ids on that position
     * @param allFeatures all reference features with feature id as <key> and a
 PersistentFeature as <value>
     * @param region2Exclude
     */
    public StatisticsOnMappingData(PersistentReference refGenome, double fraction, HashMap<Integer, List<Integer>> forwardFeatures,
            HashMap<Integer, List<Integer>> reverseFeatures, HashMap<Integer, PersistentFeature> allFeatures, List<int[]> region2Exclude) {

        this.refGenome = refGenome;
        this.totalCount = 0;
        this.uniqueCounts = 0;
        this.baseInTotal = 0;
        int chromCount = refGenome.getNoChromosomes();
        this.fwdReadStarts = new int[chromCount][];
        this.revReadStarts = new int[chromCount][];
        this.fwdCoverage = new int[chromCount][];
        this.revCoverage = new int[chromCount][];

        Map<Integer, PersistentChromosome> chroms = refGenome.getChromosomes();
        for (PersistentChromosome chrom : chroms.values()) {
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
     * @param refGenome Persistent Reference.
     * @param mml Mean mapping length.
     * @param mm Mean per million.
     * @param mc Mapping count.
     * @param bg Backgrount threshold.
     */
    public StatisticsOnMappingData(PersistentReference refGenome, double mml, double mm, double mc, double bg) {
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
     * @param result MappingResultPersistent contains List of PersistenMappings.
     */
    private void parseMappings(MappingResultPersistent result) {

        // Sorting all mappingResults
        int chromId = result.getRequest().getChromId();
        int chromNo = refGenome.getChromosome(chromId).getChromNumber();
        List<Mapping> mappings = result.getMappings();
        Collections.sort(mappings);
        int length = region2Exclude.get(chromNo - 1).length;
        for (Mapping mapping : mappings) {
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
     * Check two features for putative adjacency.
     *
     * @param putativeOperonAdjacencies Tree of putative operon adjacencies
     * @param features list of PersistentFeature Ids <value> on a reference
     * position <key>
     * @param stop stop of certain region
     * @param start start of certain region
     */
    private void checkOperonAdjacency(TreeMap<Integer, OperonAdjacency> putativeOperonAdjacencies, HashMap<Integer, List<Integer>> features, int stop, int start) {
        PersistentFeature feat1;
        PersistentFeature feat2;
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
        int wholeGenomeLength = PersistentReference.calcWholeGenomeLength(refGenome.getChromosomes());
        double mean = (double) this.uniqueCounts / (wholeGenomeLength * 2);
        double standardDiviation = Math.sqrt(mean);
        double inverseCdf = 0;
        jsc.distributions.Normal normal = new Normal(mean, standardDiviation);
        inverseCdf = normal.inverseCdf(1 - (fraction / 1000));
        return inverseCdf;
    }

    /**
     * Simalation for a background threshold. We expect ~ one start per kb, and
     * we want less than $fraction false positives
     *
     * @param fraction quantil for false positive mappings.
     * @return the simmulated background threshold
     */
    public int simulateBackgroundThreshold(double fraction) {
        int genomeSize = PersistentReference.calcWholeGenomeLength(refGenome.getChromosomes());
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
     * Get array of readstarts in forward orientation.
     *
     * @return int[chromosome id][reference position]
     */
    public int[][] getForwardReadStarts() {
        return fwdReadStarts;
    }

    /**
     * Get array of readstarts in reverse orientation.
     *
     * @return int[chromosome id][reference position]
     */
    public int[][] getReverseReadStarts() {
        return revReadStarts;
    }

    /**
     *
     * @return
     */
    public int[][] getFwdCov() {
        return fwdCoverage;
    }

    /**
     *
     * @return
     */
    public int[][] getRevCov() {
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
        if (args instanceof MappingResultPersistent) {
            MappingResultPersistent result = (MappingResultPersistent) args;
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
     * Set all memory inefficient structures to null.
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
