package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.util.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;

/**
 * Genome-feature parser. Produces different needed data structures for further
 * analyses.
 *
 * @author jritter
 */
public class GenomeFeatureParser {

    private final TrackConnector trackConnector;
    private final List<int[]> region2Exclude;
    private final HashMap<Integer, List<Integer>> fwdFeatures;
    private final HashMap<Integer, List<Integer>> revFeatures;
    private HashMap<Integer, PersistantFeature> allFeatures;
    private final ReferenceConnector refConnector;
    private final List<PersistantFeature> genomeFeatures;
    private final ProgressHandle progressHandle;
    private HashMap<Integer, List<Integer>> allFwdFeatures;
    private HashMap<Integer, List<Integer>> allRevFeatures;
    private Integer referenceLength;
    private final PersistantReference refGenome;
    private final int noOfChromosomes;

    /**
     * Genome-feature parser, parses the needed information from all Features in
     * a Genome. Produces different needed data structures for further analyses.
     *
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
        this.region2Exclude = new ArrayList(this.noOfChromosomes);
        this.refConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenome.getId());
        Map<Integer, PersistantChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistantChromosome chrom : chroms.values()) {
            this.genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
            int chromNo = refGenome.getChromosome(chrom.getId()).getChromNumber();
            this.region2Exclude.add(chromNo - 1, new int[chrom.getLength()]);
            this.referenceLength += chrom.getLength();
        }
    }

    /**
     * Returns an array with rigions to exclude.
     *
     * @return int array, 1 at position means that this region does not have to
     * be used in further analyses.
     */
    public List<int[]> getRegion2Exclude() {
        return region2Exclude;
    }

    /**
     * Returns CDS information of forward features.
     *
     * @return HashMap<Position in Genome, List<FeatureID>>
     */
    public HashMap<Integer, List<Integer>> getForwardCDSs() {
        return fwdFeatures;
    }

    /**
     * Reterns CDS information of reverse features.
     *
     * @return HashMap<Position in Genome, List<FeatureID>>
     */
    public HashMap<Integer, List<Integer>> getRevFeatures() {
        return revFeatures;
    }

    /**
     * Returns a HashMap with all genome features.
     *
     * @return HashMap<FeatureID, Feature>
     */
    public HashMap<Integer, PersistantFeature> getAllFeatures() {
        return allFeatures;
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
    public void parseFeatureInformation(List<PersistantFeature> genomeFeatures) {
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

        for (PersistantFeature feature : genomeFeatures) {

            count++;
            if (count >= interval) {
                this.progressHandle.progress(progress);
                interval = interval + interval;
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
                maskingRegions(this.region2Exclude.get(chromNo - 1), type, isFwd, start, stop);
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
     *
     * @param featureID Persistant feature id.
     * @param start Start position of feature.
     * @param stop Stop position of feature.
     * @param isFwd Feature direction is forward if true, otherwise false.
     */
    private void createCDSsStrandInformation(HashMap<Integer, List<Integer>> list, int featureID, int start, int stop) {

        for (int i = 0; (i + start) <= stop; i++) {
            if (list.get(i + start) != null) {
                list.get(i + start).add(featureID);
            } else {
                ArrayList<Integer> tmp = new ArrayList<>();
                tmp.add(featureID);
                list.put(i + start, tmp);
            }
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
     *
     * @param feature Persistant Feature list.
     * @param startFeature Startposition of feature.
     * @param stopFeature Stortposition of feature.
     * @param isFwdDirection Direction of feature is forward if true, false
     * otherwise.
     */
    private void maskingRegions(int[] region2Exclude, FeatureType type, boolean isFwd, int startFeature, int stopFeature) {

        if (type.equals(FeatureType.TRNA)) {
            if (isFwd) {
                for (startFeature -= 21; startFeature < (stopFeature + 20); startFeature++) {
                    region2Exclude[startFeature] = 1;
                }
            } else {
                for (startFeature -= 20; startFeature < (stopFeature + 21); startFeature++) {
                    region2Exclude[startFeature] = 1;
                }
            }
        } else if (type.equals(FeatureType.RRNA)) {

            if (isFwd) {
                for (startFeature -= 520; startFeature > (stopFeature + 5); startFeature++) {
                    region2Exclude[startFeature] = 1;
                }
            } else {
                for (startFeature -= 5; startFeature > (stopFeature + 520); startFeature++) {
                    region2Exclude[startFeature] = 1;
                }
            }
        }
    }

    /**
     * Fetch all genome features and load them in a HashMap<FeatureID, Feature>.
     *
     * @param genomeFeatures List of Persistant Features.
     * @return a HashMap<FeatureID, Feature> with all genome features.
     */
    public HashMap<Integer, PersistantFeature> getGenomeFeaturesInHash(List<PersistantFeature> genomeFeatures) {
        this.progressHandle.progress("Hashing of Features", 10);
        HashMap<Integer, PersistantFeature> regions = new HashMap<>();

        for (PersistantFeature gf : genomeFeatures) {
            regions.put(gf.getId(), gf);
        }
        this.progressHandle.progress("Hashing of Features", 20);
        return regions;
    }

    /**
     * Running trough all features in genome and divided by direction in which
     * strand the features are located and creates two hash structures.
     */
    public void generateAllFeatureStrandInformation(List<PersistantFeature> genomeFeatures) {
        this.allFwdFeatures = new HashMap<>();
        this.allRevFeatures = new HashMap<>();

        for (PersistantFeature feature : genomeFeatures) {

            int start = feature.getStart();
            int stop = feature.getStop();
            boolean isFwd = feature.isFwdStrand();
            int id = feature.getId();
            // store the regions in arrays of arrays (allows for overlapping regions)
            if (isFwd) {
                createCDSsStrandInformation(this.allFwdFeatures, id, start, stop);
            } else {
                createCDSsStrandInformation(this.allRevFeatures, id, start, stop);
            }
        }
    }

    /**
     * Returns the TrackConnector.
     *
     * @return TrackConnector
     */
    public TrackConnector getTrackConnector() {
        return trackConnector;
    }

    /**
     * Returns the ReferenceConnector.
     *
     * @return ReferenceConnector
     */
    public ReferenceConnector getRefConnector() {
        return refConnector;
    }

    /**
     * Returns a list with persistant features.
     *
     * @return List with persistant features
     */
    public List<PersistantFeature> getGenomeFeatures() {
        return genomeFeatures;
    }

    public HashMap<Integer, List<Integer>> getAllFwdFeatures() {
        return allFwdFeatures;
    }

    public void setAllFwdFeatures(HashMap<Integer, List<Integer>> allFwdFeatures) {
        this.allFwdFeatures = allFwdFeatures;
    }

    /**
     * Get the hash structure filled with all reverse feature elements.
     *
     * @return HashMap<StartPosition, List<FeatureID>> which contains start
     * position as the KEY and a list filled with features ids, that starts at
     * this position.
     */
    public HashMap<Integer, List<Integer>> getAllRevFeatures() {
        return allRevFeatures;
    }

    public void setAllRevFeatures(HashMap<Integer, List<Integer>> allRevFeatures) {
        this.allRevFeatures = allRevFeatures;
    }

    /**
     * Returns the whole length of all Chromosomes lenght summed up.
     *
     * @return Reference length.
     */
    public Integer getReferenceLength() {
        return referenceLength;
    }
}
