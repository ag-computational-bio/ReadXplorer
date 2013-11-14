package de.cebitec.readXplorer.transcriptionAnalyses.dataStructures;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.TrackResultEntry;

/**
 * Data storage for RPKM and read count values of a reference feature.
 *
 * @author Martin Tötsches, Rolf Hilker
 */
public class RPKMvalue extends TrackResultEntry {
    
    private PersistantFeature feature;
    private double rpkm;
    private int readCount;
    
    /**
     * Data storage for RPKM and read count values of a reference feature.
     * @param feature feature for which the values shall be stored
     * @param rpkm the RPKM value for this feature
     * @param readCount the raw read count for this feature
     * @param trackId the trackId for which these result values where calculated
     */
    public RPKMvalue(PersistantFeature feature, double rpkm, int readCount, int trackId) {
        super(trackId);
        this.feature = feature;
        this.rpkm = rpkm;
        this.readCount = readCount;
    }

    /**
     * @return the RPKM value for this feature.
     */
    public double getRPKM() {
        return rpkm;
    }

    /**
     * @param rpkm the RPKM value for this feature
     */
    public void setRpkm(double rpkm) {
        this.rpkm = rpkm;
    }

    /**
     * @return the feature for which the values shall be stored.
     */
    public PersistantFeature getFeature() {
        return feature;
    }

    /**
     * @param feature feature for which the values shall be stored
     */
    public void setFeature(PersistantFeature feature) {
        this.feature = feature;
    }

    /**
     * @return the raw read count for this feature.
     */
    public int getReadCount() {
        return readCount;
    }

    /**
     * @param readCount the raw read count for this feature
     */
    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }
}
