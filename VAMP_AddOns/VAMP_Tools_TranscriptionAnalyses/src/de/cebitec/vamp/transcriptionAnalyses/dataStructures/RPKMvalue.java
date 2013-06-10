package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 *
 * @author Martin Tötsches
 */
public class RPKMvalue {
    
    private PersistantFeature feature;
    private double rpkm;
    
    public RPKMvalue(PersistantFeature feature, double rpkm) {
        this.feature = feature;
        this.rpkm = rpkm;
    }

    /**
     * @return the rpkm
     */
    public double getRpkm() {
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
