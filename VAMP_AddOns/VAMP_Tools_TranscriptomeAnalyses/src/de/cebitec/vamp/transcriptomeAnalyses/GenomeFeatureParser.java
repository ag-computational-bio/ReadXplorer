/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.util.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jritter
 */
public class GenomeFeatureParser {

    private TrackConnector trackConnector;
    private final int[] region2Exclude;
    private final HashMap<Integer, List<Integer>> forwardCDSs;
    private final HashMap<Integer, List<Integer>> reverseCDSs;
    private final HashMap<Integer, PersistantFeature> allRegionsInHash;
    private ReferenceConnector refConnector;
    private int refSeqLength;
    private List<PersistantFeature> genomeFeatures;

    /**
     * Constructor for Genome-feature parser. Produces different needed data
     * structures for further analyses.
     *
     * @param trackConnector
     */
    public GenomeFeatureParser(TrackConnector trackConnector) {
        this.trackConnector = trackConnector;
        this.refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.refSeqLength = trackConnector.getRefSequenceLength();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, this.refSeqLength);

        this.region2Exclude = new int[this.refSeqLength];
        this.forwardCDSs = new HashMap<>();
        this.reverseCDSs = new HashMap<>();
        this.allRegionsInHash = getGenomeFeaturesInHash(this.genomeFeatures);

        parseFeatureInformation(this.genomeFeatures);
    }

    /**
     * Returns an array with rigions to exclude.
     *
     * @return int array, 1 at position means that this region does not have to
     * be used in further analyses.
     */
    public int[] getRegion2Exclude() {
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
    private void parseFeatureInformation(List<PersistantFeature> genomeFeatures) {
        //at first we need connection to the reference (Projectconnector->ReferenceConnector)
        // Vamp has already all information we need here

        int start, stop, id;
        boolean isFwd;
        String featureName;
        FeatureType type;

        for (PersistantFeature feature : genomeFeatures) {

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
                maskingRegions(type, isFwd, start, stop);
            }

            // store the regions in arrays of arrays (allows for overlapping regions)
            if (!type.equals(FeatureType.RRNA) && !type.equals(FeatureType.TRNA)) {
                createCDSsStrandInformation(id, start, stop, isFwd);
            }
        }
    }

    /**
     * This method fills a Map of Lists. If there is a feature on Position i,
     * than the list is mapped to that position. The List contains the feature
     * ids corrisponding to that Position. Each list can containing max. three
     * different feature ids because of the three reading frames for the forward
     * and reverse direction.
     *
     * @param featureID Persistant feature id.
     * @param start Startposition of feature.
     * @param stop Stopposition of feature.
     * @param isFwd Feature direction is forward if true, otherwise false.
     */
    private void createCDSsStrandInformation(int featureID, int start, int stop, boolean isFwd) {

        for (int i = 0; (i + start - 1) < stop; i++) {
            if (isFwd) {
                if (this.forwardCDSs.get(i + start - 1) != null) {
                    this.forwardCDSs.get(i + start - 1).add(featureID);
                } else {
                    ArrayList<Integer> tmp = new ArrayList<>();
                    tmp.add(featureID);
                    this.forwardCDSs.put(i + start - 1, tmp);
                }
            } else {
                if (this.reverseCDSs.get(i + start - 1) != null) {
                    this.reverseCDSs.get(i + start - 1).add(featureID);
                } else {
                    ArrayList<Integer> tmp = new ArrayList<>();
                    tmp.add(featureID);
                    this.reverseCDSs.put(i + start - 1, tmp);
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
    private void maskingRegions(FeatureType type, boolean isFwd, int startFeature, int stopFeature) {

        if (type.equals(FeatureType.TRNA)) {
            if (isFwd) {
                for (startFeature -= 21; startFeature < (stopFeature + 20); startFeature++) {
                    this.region2Exclude[startFeature] = 1;
                }
            } else {
                for (startFeature -= 20; startFeature < (stopFeature + 21); stopFeature++) {
                    this.region2Exclude[startFeature] = 1;
                }
            }
        } else if (type.equals(FeatureType.RRNA)) {

            if (isFwd) {
                for (startFeature -= 520; startFeature > (stopFeature + 5); startFeature++) {
                    this.region2Exclude[startFeature] = 1;
                }
            } else {
                for (startFeature -= 5; startFeature > (stopFeature + 520); stopFeature++) {
                    this.region2Exclude[startFeature] = 1;
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
    private HashMap<Integer, PersistantFeature> getGenomeFeaturesInHash(List<PersistantFeature> genomeFeatures) {
        HashMap<Integer, PersistantFeature> regions = new HashMap<>();

        for (PersistantFeature gf : genomeFeatures) {
            regions.put(gf.getId(), gf);
            System.out.println("FeatureID: "+ gf.getId() + "\tFeatureName: "+gf.getFeatureName());
        }

        return regions;
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
     * Returns the length of the reference.
     *
     * @return length of reference
     */
    public int getRefSeqLength() {
        return refSeqLength;
    }

    /**
     * Returns a list with persistant features.
     *
     * @return List with persistant features
     */
    public List<PersistantFeature> getGenomeFeatures() {
        return genomeFeatures;
    }
}
