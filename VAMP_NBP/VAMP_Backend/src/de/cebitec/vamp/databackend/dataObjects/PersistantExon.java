package de.cebitec.vamp.databackend.dataObjects;

/**
 * @author rhilker
 * 
 * Represents an exon.
 */
public class PersistantExon {
    
    private int parentFeatureId;
    private int start;
    private int stop;

    /**
     * Represents and exon.
     * @param id the id of the exon.
     * @param parentFeatureId the id of the parent feature this exon belongs to
     * @param start absolute start of the exon in regard to the reference genome
     * @param stop absolute stop of the exon in regard to the reference genome
     */
    public PersistantExon(int parentFeatureId, int start, int stop) {
        this.parentFeatureId = parentFeatureId;
        this.start = start;
        this.stop = stop;
    }

    /**
     * @return the id of the parent feature this exon belongs to
     */
    public int getParentId() {
        return parentFeatureId;
    }

    /**
     * @return the absolute start of the exon in regard to the reference genome
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the absolute stop of the exon in regard to the reference genome
     */
    public int getStop() {
        return stop;
    }

    
}
