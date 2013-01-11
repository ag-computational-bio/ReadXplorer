package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;

/**
 * Data structure for storing an annotation (gene), which is detected as 
 * covered and its corresponding data.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredAnnotation {
    
    private PersistantAnnotation coveredAnnotation;
    private int annoLength;
    private int noCoveredBases;
    private int percentCovered;

    /**
     * Data structure for storing an annotation (gene), which is detected as 
     * covered and its corresponding data.
     * @param coveredAnnotation the annotation which is detected as covered
     * @param noCoveredBases the number of covered bases of this annotation
     */
    public CoveredAnnotation(PersistantAnnotation coveredAnnotation) {
        this.coveredAnnotation = coveredAnnotation;
        this.annoLength = Math.abs(coveredAnnotation.getStop() - coveredAnnotation.getStart());
    }

    /**
     * Sets the number of covered bases of this annotation.
     * @param noCoveredBases the number of covered bases of this annotation
     */
    public void setNoCoveredBases(int noCoveredBases) {
        this.noCoveredBases = noCoveredBases;
        this.percentCovered = this.noCoveredBases / this.annoLength * 100;
    }

    /**
     * @return the annotation which is detected as covered
     */
    public PersistantAnnotation getCoveredAnnotation() {
        return this.coveredAnnotation;
    }

    /**
     * @return the number of covered bases of this annotation
     */
    public int getNoCoveredBases() {
        return this.noCoveredBases;
    }

    /**
     * @return the percentage of this annotation, which is covered
     */
    public int getPercentCovered() {
        return this.percentCovered;
    }
}
