package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.TrackResultEntry;

/**
 *
 * @author Martin Tötsches
 */
public class RPKMvalue extends TrackResultEntry {
    
    private PersistantFeature feature;
    private double rpkm;
    
    public RPKMvalue(PersistantFeature feature, double rpkm, int trackId) {
        super(trackId);
        this.feature = feature;
        this.rpkm = rpkm;
    }

    /**
     * @return the rpkm
     */
    public double getRPKM() {
        return rpkm;
    }

    /**
     * @param rpkm the rpkm to set
     */
    public void setRpkm(double rpkm) {
        this.rpkm = rpkm;
    }

    /**
     * @return the feature
     */
    public PersistantFeature getFeature() {
        return feature;
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(PersistantFeature feature) {
        this.feature = feature;
    }
}
