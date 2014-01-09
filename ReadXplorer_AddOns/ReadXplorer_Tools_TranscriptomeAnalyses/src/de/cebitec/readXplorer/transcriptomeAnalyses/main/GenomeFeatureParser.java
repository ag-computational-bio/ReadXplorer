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

    private TrackConnector trackConnector;
    private List<int[]> region2Exclude;
    private HashMap<Integer, List<Integer>> forwardCDSs;
    private HashMap<Integer, List<Integer>> reverseCDSs;
    private HashMap<Integer, PersistantFeature> allRegionsInHash;
    private ReferenceConnector refConnector;
    private List<PersistantFeature> genomeFeatures;
    private final ProgressHandle progressHandle;
    private HashMap<Integer, List<Integer>> allFwdFeatures;
    private HashMap<Integer, List<Integer>> allRevFeatures;
    private Integer referenceLength;
    private PersistantReference refGenome;

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
        this.forwardCDSs = new HashMap<>();
        this.reverseCDSs = new HashMap<>();
        this.region2Exclude = new ArrayList<>();
        this.genomeFeatures = new ArrayList<>();
        this.referenceLength = 0;
        this.refGenome = trackConnector.getRefGenome();
        this.refConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenome.getId());
        Map<Integer, PersistantChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistantChromosome chrom : chroms.values()) {
            this.genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
            this.region2Exclude.add(new int[chrom.getLength()]);
            this.referenceLength += chrom.getLength();
        }
//        this.allRegionsInHash = getGenomeFeaturesInHash(this.genomeFeatures);
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
        return forwardCDSs;
    }

    /**
     * Reterns CDS information of reverse features.
     *
     * @return HashMap<Position in Genome, List<FeatureID>>
     */
    public HashMap<Integer, List<Integer>> getReverseCDSs() {
        return reverseCDSs;
    }

    /**
     * Returns a HashMap with all genome features.
     *
     * @return HashMap<FeatureID, Feature>
     */
    public HashMap<Integer, PersistantFeature> getAllRegionsInHash() {
        return allRegionsInHash;
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
        int start, stop, id;
        int count = 0;
        boolean isFwd;
        String featureName;
        FeatureType type;

        for (PersistantFeature feature : genomeFeatures) {

            count++;
            if (count >= interval) {
                this.progressHandle.progress("Parsing Feature Information", progress);
                interval = interval + interval;
                progress += 10;
            }

            // get the feature-name
            featureName = feature.getFeatureName();

            if (featureName.startsWith("scg")) { //next if ($featureName =~ /^scg/);
                continue;
            }

            start = feature.getStart();
            stop = feature.getStop();
            isFwd = feature.isFwdStrand();
            type = feature.getType();
            id = feature.getId();

            // create a blocked region (sense & antisense) masking stable (tRNA, rRNA) RNAs
            // tRNA and rRNA regions are entered into the "mask array"
            if (type.equals(FeatureType.RRNA) || type.equals(FeatureType.TRNA)) {
                maskingRegions(type, isFwd, start, stop, feature.getChromId());
            } else {
                // store the regions in arrays of arrays (allows for overlapping regions)
                if (isFwd) {
                    createCDSsStrandInformation(this.forwardCDSs, id, start, stop, isFwd);
                } else {
                    createCDSsStrandInformation(this.reverseCDSs, id, start, stop, isFwd);
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
    private void createCDSsStrandInformation(HashMap<Integer, List<Integer>> list, int featureID, int start, int stop, boolean isFwd) {

        for (int i = 0; (i + start - 1) < stop; i++) {
            if (isFwd) {
                if (list.get(i + start - 1) != null) {
                    list.get(i + start - 1).add(featureID);
                } else {
                    ArrayList<Integer> tmp = new ArrayList<>();
                    tmp.add(featureID);
                    list.put(i + start - 1, tmp);
                }
            } else {
                if (list.get(i + start - 1) != null) {
                    list.get(i + start - 1).add(featureID);
                } else {
                    ArrayList<Integer> tmp = new ArrayList<>();
                    tmp.add(featureID);
                    list.put(i + start - 1, tmp);
                }
            }
        }

    }

    /**
     * This method puts an 1 on each Postition in int[] array, where a feature
     * has the type RRNA or TRNA. This Array excludes this regions from further
     * analyses.
     *
     * @param feature Persistant Feature list.
     * @param startFeature Startposition of feature.
     * @param stopFeature Stortposition of feature.
     * @param isFwdDirection Direction of feature is forward if true, false
     * otherwise.
     */
    private void maskingRegions(FeatureType type, boolean isFwd, int startFeature, int stopFeature,
            int chromId) {

        if (type.equals(FeatureType.TRNA)) {
            if (isFwd) {
                for (startFeature -= 21; startFeature < (stopFeature + 20); startFeature++) {
                    this.region2Exclude.get(chromId - 1)[startFeature] = 1;
                }
            } else {
                for (startFeature -= 20; startFeature < (stopFeature + 21); stopFeature++) {
                    this.region2Exclude.get(chromId - 1)[startFeature] = 1;
                }
            }
        } else if (type.equals(FeatureType.RRNA)) {

            if (isFwd) {
                for (startFeature -= 520; startFeature > (stopFeature + 5); startFeature++) {
                    this.region2Exclude.get(chromId - 1)[startFeature] = 1;
                }
            } else {
                for (startFeature -= 5; startFeature > (stopFeature + 520); stopFeature++) {
                    this.region2Exclude.get(chromId - 1)[startFeature] = 1;
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
                createCDSsStrandInformation(this.allFwdFeatures, id, start, stop, isFwd);
            } else {
                createCDSsStrandInformation(this.allRevFeatures, id, start, stop, isFwd);
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
