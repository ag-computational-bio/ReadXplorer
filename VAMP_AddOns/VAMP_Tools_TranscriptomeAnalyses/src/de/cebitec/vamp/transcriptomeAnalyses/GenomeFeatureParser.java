/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

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
    private final int[] region2Exclude;
    private final HashMap<Integer, List<Integer>> forwardCDSs;
    private final HashMap<Integer, List<Integer>> reverseCDSs;
    private final HashMap<Integer, PersistantFeature> allRegionsInHash;

    public GenomeFeatureParser(List<PersistantFeature> features, int length) {
        this.region2Exclude = new int[length];
        this.forwardCDSs = new HashMap<Integer, List<Integer>>();
        this.reverseCDSs = new HashMap<Integer, List<Integer>>();
        this.allRegionsInHash = getGenomeFeaturesInHash(features);
        
        parseFeatureInformation(features);
    }

    public int[] getRegion2Exclude() {
        return region2Exclude;
    }

    public HashMap<Integer, List<Integer>> getForwardCDSs() {
        return forwardCDSs;
    }

    public HashMap<Integer, List<Integer>> getReverseCDSs() {
        return reverseCDSs;
    }

    public HashMap<Integer, PersistantFeature> getAllRegionsInHash() {
        return allRegionsInHash;
    }
    
    
    
    /*
     * This method creates an array with the length of the genome. All rRNA's and tRNA's 
     * will get an 1 an the position from start to stop belonging to the feature.
     * 
     */
    private void parseFeatureInformation(List<PersistantFeature> genomeFeatures) {
        //at first we need connection to the reference (Projectconnector->ReferenceConnector)
        // Vamp has already all information we need here

        int start, stop;
        boolean isFwd;
        String featureName;

        for (PersistantFeature feature : genomeFeatures) {

            // get the feature-name
            featureName = feature.getFeatureName();

            if (featureName.startsWith("scg")) { //next if ($featureName =~ /^scg/);
                continue;
            }

            start = feature.getStart();
            stop = feature.getStop();
            isFwd = feature.isFwdStrand();


            // create a blocked region (sense & antisense) masking stable (tRNA, rRNA) RNAs
            // tRNA and rRNA regions are entered into the "mask array"
            maskingRegions(feature, start, stop);

            // store the regions in arrays of arrays (allows for overlapping regions)
            createCDSsStrandInformation(feature, start, stop, isFwd);
        }
    }

    /**
     * This method fills a List of Lists. If there is a feature on Position i,
     * than the list on this position adds the feature id.
     *
     * @param feature Persistant feature.
     * @param start Startposition of feature.
     * @param stop Stopposition of feature.
     * @param isFwd Feature direction is forward if true, otherwise false.
     */
    private void createCDSsStrandInformation(PersistantFeature feature, int start, int stop, boolean isFwd) {
        if (isFwd) {
            for (int i = 0; (i + stop - 1) < stop; i++) {
                if (this.forwardCDSs.get(i + start - 1) != null) {
                    this.forwardCDSs.get(i + start - 1).add(feature.getId());
                } else {
                    ArrayList<Integer> tmp = new ArrayList<Integer>();
                    tmp.add(feature.getId());
                    this.forwardCDSs.put(i + start - 1, tmp);
                }
            }
        } else {
            for (int i = 0; (i + stop - 1) < start; i++) {
                if (this.reverseCDSs.get(i + stop - 1) != null) {
                    this.reverseCDSs.get(i + stop - 1).add(feature.getId());
                } else {
                    ArrayList<Integer> tmp = new ArrayList<Integer>();
                    tmp.add(feature.getId());
                    this.reverseCDSs.put(i + stop - 1, tmp);
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
    private void maskingRegions(PersistantFeature feature, int startFeature, int stopFeature) {


        if (!feature.getType().equals(FeatureType.CDS)) { // unless feature is not CDS


            if (feature.getType().equals(FeatureType.TRNA)) {
                if (feature.isFwdStrand()) {
                    for (startFeature -= 21; startFeature < (stopFeature + 20); startFeature++) {
                        this.region2Exclude[startFeature] = 1;
                    }
                } else {
                    for (startFeature -= 20; startFeature < (stopFeature + 21); stopFeature++) {
                        this.region2Exclude[startFeature] = 1;
                    }
                }
            } else if (feature.getType().equals(FeatureType.RRNA)) {

                if (feature.isFwdStrand()) {
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
    }

    /**
     * Fetch all genome features and load them in a HashMap<FeatureID, Feature>.
     *
     * @param genomeFeatures List of Persistant Features.
     * @return a HashMap<FeatureID, Feature> with all genome features in it.
     */
    private HashMap<Integer, PersistantFeature> getGenomeFeaturesInHash(List<PersistantFeature> genomeFeatures) {
        HashMap<Integer, PersistantFeature> regions = new HashMap<Integer, PersistantFeature>();

        for (PersistantFeature gf : genomeFeatures) {
            regions.put(gf.getId(), gf);
        }

        return regions;
    }
}
