package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 * Data structure for storing an feature (gene), which is detected as 
 * covered and its corresponding data.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeature {
    
    private PersistantFeature coveredFeature;
    private int annoLength;
    private int noCoveredBases;
    private int percentCovered;

    /**
     * Data structure for storing an feature (gene), which is detected as 
     * covered and its corresponding data.
     * @param coveredFeature the feature which is detected as covered
     * @param noCoveredBases the number of covered bases of this feature
     */
    public CoveredFeature(PersistantFeature coveredFeature) {
        this.coveredFeature = coveredFeature;
        this.annoLength = Math.abs(coveredFeature.getStop() - coveredFeature.getStart());
    }

    /**
     * Sets the number of covered bases of this feature.
     * @param noCoveredBases the number of covered bases of this feature
     */
    public void setNoCoveredBases(int noCoveredBases) {
        this.noCoveredBases = noCoveredBases;
        this.percentCovered = this.noCoveredBases / this.annoLength * 100;
    }

    /**
     * @return the feature which is detected as covered
     */
    public PersistantFeature getCoveredFeature() {
        return this.coveredFeature;
    }

    /**
     * @return the number of covered bases of this feature
     */
    public int getNoCoveredBases() {
        return this.noCoveredBases;
    }

    /**
     * @return the percentage of this feature, which is covered
     */
    public int getPercentCovered() {
        return this.percentCovered;
    }
}
