package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.util.Observer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jsc.distributions.Normal;

/**
 * This class contains all of the statistics calculations and simulations.
 *
 *
 * @author jritter
 */
public class Statistics implements Observer {

    private double bg;
    List<MappingResultPersistant> mappingResults;
    HashMap<Integer, List<PersistantMapping>> mappingResultsForChromosomes;
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

    public Statistics(PersistantReference refGenome, double fraction, HashMap<Integer, List<Integer>> forwardCDSs,
            HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, PersistantFeature> allRegionsInHash, List<int[]> region2Exclude) {

        this.mappingResultsForChromosomes = new HashMap<>();
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
        this.mappingResults = new ArrayList<>();
        this.putativeOperonAdjacenciesFWD = new HashMap<>();
        this.putativeOperonAdjacenciesREV = new HashMap<>();
    }

    public Statistics(PersistantReference refGenome, double mml, double mm, double mc, double bg) {
        this.refGenome = refGenome;
        this.mml = mml;
        this.mm = mm;
        this.mc = mc;
        this.bg = bg;
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
    public void parseMappings(List<MappingResultPersistant> mappingResults) {

        /*
         * Das Problem das ich hier habe ist folgendermaßen: Ich benötige eigentlich
         * alle Mappings von dem Chromosome. Das Problem ist nur, dass wenn die
         * Iteration über die mappingResults läuft, dann werden immer nur die 
         * Buckets von Mappings gefatcht. Dadurch habe ich nicht die gesamte Länge
         * des Chromosoms, das ich aber benötige, um die Arrays zu initialisieren.
         * Da wir das ja jetzt Chromosom basiert machen wollen muss ich ja noch eine
         * Chromosom id mit angeben. 
         */



        // Sorting all mappingResults
        for (MappingResultPersistant result : mappingResults) {
            int chromId = result.getRequest().getChromId();
            int chromNo = refGenome.getChromosome(chromId).getChromNumber();
//            int chromLength = refGenome.getChromosome(chromId).getLength();
            List<PersistantMapping> mappings = result.getMappings();
            System.out.println("MappingsSize in Method parseMappings: " + mappings.size());
            Collections.sort(mappings);

            for (PersistantMapping mapping : mappings) {
                this.totalCount++;
                int start = mapping.getStart();
                int stop = mapping.getStop();

                boolean directionFWD = mapping.isFwdStrand();

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
                if (directionFWD) {
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
//            setArrays(chromNo, chromFwd, chromRev, chromFwdCov, chromRevCov);
        }
    }

//    private void setArrays(int chromNo, int[] chromFwd, int[] chromRev, int[] chromFwdCov, int[] chromRevCov) {
//        fwdReadStarts[chromNo - 1] = chromFwd;
//        revReadStarts[chromNo - 1] = chromRev;
//        fwdCoverage[chromNo - 1] = chromFwdCov;
//        revCoverage[chromNo - 1] = chromRevCov;
//    }
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
     * unique mappingResults divided by the length of the reference * 2. The
     * variance is equals mean.
     *
     * @param fraction Fraction needed for calculation of allowed false
     * pasitives.
     * @return
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
     * Calculates the mean value of a given array with values of type double.
     *
     * @param m double array.
     * @return mean value.
     */
    private double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    /**
     * Calculates the median of a given array with values of type double.
     * 
     * @param m Array of values from type double. The array must be sorted!
     * @return median value.
     */
    private double median(double[] m) {
        int middle = m.length / 2;
        if (m.length % 2 == 1) {
            return m[middle];
        } else {
            return (m[middle - 1] + m[middle]) / 2.0;
        }
    }

    /**
     * Return the background threshold value.
     * 
     * @return background threshold
     */
    public double getBg() {
        return this.bg;
    }

    public int[][] getForward() {
        return fwdReadStarts;
    }

    public int[][] getReverse() {
        return revReadStarts;
    }

    public int[][] getFwdCoverage() {
        return fwdCoverage;
    }

    public int[][] getRevCoverage() {
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

    public List<MappingResultPersistant> getMappingResults() {
        return mappingResults;
    }

    public void addMappingResult(MappingResultPersistant result) {
        this.mappingResults.add(result);
//        this.mappingCount += result.getMappings().size();
//        System.out.println("MappingCount:" + this.mappingCount);
    }

    @Override
    public void update(Object args) {
        if (args instanceof MappingResultPersistant) {
            addMappingResult((MappingResultPersistant) args);
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

    public void setBg(double bg) {
        this.bg = bg;
    }
}
