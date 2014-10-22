package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.PositionUtils;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;

/**
 * Genome-feature parser. Produces different genomic data structures needed for 
 * further analyses.
 *
 * @author jritter, rhilker
 */
public class GenomeFeatureParser {

    private final TrackConnector trackConnector;
    /** A list of intervals (inner list) for each chromosome (outer list). */
    private final List<List<Pair<Integer, Integer>>> regions2Exclude; 
    private List<Set<Integer>> excludedGenomePos;
    private final Map<Integer, List<Integer>> fwdFeatures;
    private final Map<Integer, List<Integer>> revFeatures;
    private final ReferenceConnector refConnector;
    private final List<PersistentFeature> genomeFeatures;
    private final ProgressHandle progressHandle;
    private Integer referenceLength;
    private final PersistentReference refGenome;
    private final int noOfChromosomes;

    /**
     * Genome-feature parser, parses the needed information from all Features in
     * a Genome. Produces different needed data structures for further analyses.
     * @param trackConnector TrackConnector.
     * @param progressHandle current ProgressHandle, shows the progress for the
     * running analysis.
     */
    public GenomeFeatureParser(TrackConnector trackConnector, ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        this.trackConnector = trackConnector;
        this.fwdFeatures = new HashMap<>();
        this.revFeatures = new HashMap<>();
        this.genomeFeatures = new ArrayList<>();
        this.referenceLength = 0;
        this.refGenome = trackConnector.getRefGenome();
        this.noOfChromosomes = refGenome.getNoChromosomes();
        this.regions2Exclude = new ArrayList<>(this.noOfChromosomes);
        this.refConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenome.getId());
        Map<Integer, PersistentChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistentChromosome chrom : chroms.values()) {
            this.genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
            int chromNo = refGenome.getChromosome(chrom.getId()).getChromNumber();
            List<Pair<Integer, Integer>> intervals = new ArrayList<>(1);
            intervals.add(new Pair<>(0, 0));
            this.regions2Exclude.add(chromNo - 1, intervals);
            this.referenceLength += chrom.getLength();
        }
        this.excludedGenomePos = this.convertRegions2Exclude(this.regions2Exclude);
    }

    /**
     * Converts the regions to exclude to a set of positions for each
     * chromosome to exclude.
     * @param regions2Exclude The list of regions to exclude for each chromosome
     * @return a set of positions for each chromosome to exclude
     */
    private List<Set<Integer>> convertRegions2Exclude(List<List<Pair<Integer, Integer>>> regions2Exclude) {
        List<Set<Integer>> excludedPosGenome = new ArrayList<>(regions2Exclude.size());
        for (List<Pair<Integer, Integer>> regions2ExcludeChrom : regions2Exclude) {
            Set<Integer> excludedPos = new HashSet<>();
            for (Pair<Integer, Integer> region2Exclude : regions2ExcludeChrom) {
                for (int i = region2Exclude.getFirst(); i < region2Exclude.getSecond(); ++i) {
                    excludedPos.add(i);
                }
            }
            excludedPosGenome.add(excludedPos);
        }
        return excludedPosGenome;
    }

    /**
     * @return A set of positions for each chromosome to exclude.
     */
    public List<Set<Integer>> getPositions2Exclude() {
        return excludedGenomePos;
    }

    /**
     * Returns CDS information of forward features.
     * @return Map<Position in Genome, List<FeatureID>>
     */
    public Map<Integer, List<Integer>> getForwardCDSs() {
        return fwdFeatures;
    }

    /**
     * Reterns CDS information of reverse features.
     *
     * @return HashMap<Position in Genome, List<FeatureID>>
     */
    public Map<Integer, List<Integer>> getRevFeatures() {
        return revFeatures;
    }

    /**
     * Parse the genome features for further procedures in tanscriptome
     * analyses. This method creates an array with the length of the genome and
     * contains entries 1 => region (Feature) to exclude. All rRNA's and tRNA's
     * will get an 1 an the position from start to stop belonging to the
     * feature.
     *
     * @param genomeFeatures
     */
    public void parseFeatureInformation(List<PersistentFeature> genomeFeatures) {
        //at first we need connection to the reference (Projectconnector->ReferenceConnector)
        // ReadXplorer already has all information we need here

        this.progressHandle.progress("Parsing Feature Information", 30);

        int size = genomeFeatures.size();
        double interval = size / 7;
        int progress = 40;
        int start, stop, featureID;
        int count = 0;
        boolean isFwd;
        FeatureType type;

        for (PersistentFeature feature : genomeFeatures) {

            count++;
            if (count >= interval) {
                this.progressHandle.progress(progress);
                interval += interval;
                progress += 10;
            }

            start = feature.getStart();
            stop = feature.getStop();
            isFwd = feature.isFwdStrand();
            type = feature.getType();
            featureID = feature.getId();

            // create a blocked region (sense & antisense) masking stable (tRNA, rRNA) RNAs
            // tRNA and rRNA regions are entered into the "mask array"
            if (type.equals(FeatureType.RRNA) || type.equals(FeatureType.TRNA)) {
                int chromNo = refGenome.getChromosome(feature.getChromId()).getChromNumber();
                maskingRegions(this.regions2Exclude.get(chromNo - 1), type, isFwd, start, stop);
            }

            if (!type.equals(FeatureType.SOURCE)) {
                // store the regions in arrays of arrays (allows for overlapping regions)
                if (isFwd) {
                    createCDSsStrandInformation(this.fwdFeatures, featureID, start, stop);
                } else {
                    createCDSsStrandInformation(this.revFeatures, featureID, start, stop);
                }
            }
        }
        this.progressHandle.progress("Parsing Feature Information", 100);
    }

    /**
     * This method fills a Map of Lists. If there is a feature on Position i,
     * then the list is mapped to that position. The List contains the feature
     * ids corresponding to that Position. Each list can contain max. three
     * different feature ids because of the three reading frames for the forward
     * and reverse direction.
     * @param featureID Persistent feature id.
     * @param start Start position of feature.
     * @param stop Stop position of feature.
     * @param isFwd Feature direction is forward if true, otherwise false.
     */
    private void createCDSsStrandInformation(Map<Integer, List<Integer>> list, int featureID, int start, int stop) {

        for (int i = start; i <= stop; i++) {
            if (list.get(i) == null) {
                ArrayList<Integer> tmp = new ArrayList<>();
                list.put(i, tmp);
            }
            list.get(i).add(featureID);
        }
    }

    /**
     * This method creates an exclusion array for r/tRNA, which has a length of
     * the representive genome. Is the features type a rRNA, than 520 fields
     * upstream, the fields from start to stop of the feature and 5 fields
     * downstream to the start are marked with a 1. For a feature element of
     * tRNA type, 21 field upstream, the fields from start to stop and 20 fields
     * downstream from stop position of this feature are marked with a 1. This
     * regions are going to be excluded in further analysis. analyses.
     * @param feature Persistent Feature list.
     * @param startFeature Startposition of feature.
     * @param stopFeature Stortposition of feature.
     * @param isFwdDirection Direction of feature is forward if true, false
     * otherwise.
     */
    private void maskingRegions(List<Pair<Integer, Integer>> regions2Exclude, FeatureType type, boolean isFwd, int startFeature, int stopFeature) {

        if (type.equals(FeatureType.TRNA)) {
            if (isFwd) {
                startFeature -= 21;
                stopFeature += 21;
            }
        } else if (type.equals(FeatureType.RRNA)) {
            if (isFwd) {
                startFeature -= 520;
                stopFeature += 5;
            } else {
                startFeature -= 5;
                stopFeature += 520;
            }
        }
        PositionUtils.updateIntervals(regions2Exclude, startFeature, stopFeature);
    }

    /**
     * Fetch all genome features and load them in a HashMap<FeatureID, Feature>.
     * @param genomeFeatures List of Persistent Features.
     * @return a HashMap<FeatureID, Feature> with all genome features.
     */
    public Map<Integer, PersistentFeature> getGenomeFeaturesInHash(List<PersistentFeature> genomeFeatures) {
        this.progressHandle.progress("Hashing of Features", 10);
        Map<Integer, PersistentFeature> regions = new HashMap<>();

        for (PersistentFeature gf : genomeFeatures) {
            regions.put(gf.getId(), gf);
        }
        this.progressHandle.progress("Hashing of Features", 20);
        return regions;
    }

    /**
     * Returns the TrackConnector.
     * @return TrackConnector
     */
    public TrackConnector getTrackConnector() {
        return trackConnector;
    }

    /**
     * Returns the ReferenceConnector.
     * @return ReferenceConnector
     */
    public ReferenceConnector getRefConnector() {
        return refConnector;
    }

    /**
     * Returns a list with persistent features.
     * @return List with persistent features
     */
    public List<PersistentFeature> getGenomeFeatures() {
        return genomeFeatures;
    }

    /**
     * Returns the whole length of all Chromosomes lenght summed up.
     * @return Reference length.
     */
    public Integer getReferenceLength() {
        return referenceLength;
    }
}
