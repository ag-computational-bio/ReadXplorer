package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
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
    private Statistics stats;
    private final int[] forwardStarts, reverseStarts;
    private final int[] forwardCoverage, reverseCoverage;
    private final double mm, mc;
    private int trackID;

    public RPKMValuesCalculation(HashMap<Integer, PersistantFeature> persFeatures, Statistics stats, int trackID) {
        this.allRegionsInHash = persFeatures;
        this.rpkmValues = new ArrayList<>();
        this.stats = stats;
        this.forwardStarts = this.stats.getForward();
        this.reverseStarts = this.stats.getReverse();
        this.forwardCoverage = this.stats.getFwdCoverage();
        this.reverseCoverage = this.stats.getRevCoverage();
        this.mm = this.stats.getMm();
        this.mc = this.stats.getMc();
        this.trackID = trackID;

    }

    public void calculationExpressionValues() {

//print STDERR "Calculating and writing expression levels";
//print STDERR " and writing region info" unless ($opt_E);
//print STDERR "...\n";

        Map<Integer, PersistantFeature> allRegionsSorted = new TreeMap<>(this.allRegionsInHash);
        Set<Integer> keys = allRegionsSorted.keySet();
        PersistantFeature feature = null;

        for (Integer id : keys) {
            feature = allRegionsSorted.get(id);
            if(feature.getType() == FeatureType.RRNA || feature.getType() == FeatureType.TRNA) {
                continue;
            }

            int start = feature.getStart();
            int stop = feature.getStop();
            boolean isFwd = feature.isFwdStrand();
            RPKMvalue rpkm = null;
            if (isFwd) {
//                System.out.println("Feature fwd: " + feature.getFeatureName());
                rpkm = this.calculateStatistics(start, stop, forwardStarts, forwardCoverage, this.mm, this.mc);
                rpkm.setFeature(feature);
                rpkmValues.add(rpkm);
            } else {
//                System.out.println("Feature rev: " + feature.getFeatureName());
                rpkm = this.calculateStatistics(start, stop, reverseStarts, reverseCoverage, this.mm, this.mc);
                rpkm.setFeature(feature);
                rpkmValues.add(rpkm);
            }

        }


        // TODO: Den TrueStart Datensatz beschaffen.

        //    my $offset = $all_regions{$gene}{start} - $all_regions{$gene}{truestart};
        //    $offset = -$offset if ($offset < 0);

        //    my $operon = 'OP ' . ($all_regions{$gene}{operon} ? $all_regions{$gene}{operon} : $gene);

        //    print OUT "$gene\t$dir\t$start\t$operon\t$out\t$offset\n" unless ($opt_E);
//    unless ($opt_R) {
//	print REG "$gene\t$start\t$stop\t";
//	print REG "yes" if ($static_regions{$gene}{start});
//	print REG "\t";
//	print REG "yes" if ($static_regions{$gene}{stop});
//	print REG "\n";
//    }
//}
//
//print STDERR "Calculating and writing expression levels for new regions\n";
//
//foreach my $pos (sort by_number(keys(%{$new_regs{out}}))) {
//    foreach my $entry (@{$new_regs{out}{$pos}}) {
//	print NEW "$pos\t$entry\n" unless ($opt_N);
//
//	unless ($opt_E) {
//
//	    my ($stop) = (split(/\t/, $entry))[0] - 1;
//	    my $start  = $pos - 1;
//
//	    my $dir = (($start < $stop) ? '+' : '-');
//
//	    my $out = &calculate_statistics($start, $stop);
//
//	    print OUT "\t$dir\t$pos\t\t$out\t0\n" 
//	}
//    }
//}


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
    private RPKMvalue calculateStatistics(int start, int stop, int[] starts, int[] covered, double mm, double mc) {

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
            int j = starts[i];
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
            int j = covered[i];
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

        return new RPKMvalue(null, rpkm, logRpkm, coverageRpkm, coverageLogRpkm, sum, covsum, this.trackID);
    }

    /**
     * the array double[] m MUST BE SORTED
     *
     */
    private double median(List<Double> m) {
        int length = m.size();
//        System.out.println("Length: " + length);
        int middle = length / 2;
//        System.out.println("Middle: " + middle);
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
