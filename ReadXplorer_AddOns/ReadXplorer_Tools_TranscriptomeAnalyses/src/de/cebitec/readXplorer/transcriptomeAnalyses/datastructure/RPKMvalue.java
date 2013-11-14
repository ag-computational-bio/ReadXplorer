package de.cebitec.readXplorer.transcriptomeAnalyses.datastructure;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.TrackResultEntry;

/**
 * Data storage for RPKM and read count values of a reference feature.
 *
 * @author Martin Tötsches, Rolf Hilker
 */
public class RPKMvalue extends TrackResultEntry {
    
    private PersistantFeature feature;
    private int readCount, coverage;
    private double rpkm, logRpkm, coverageRpkm, coverageLogRpkm;
    
    
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
     * Data storage for RPKM and read count values of a reference feature.
     * 
     * @param feature PersistantFeature for which the RPKM values are.
     * @param rpkm Reads per kilobase of exon per million mapped reads.
     * @param logRpkm
     * @param coverageRpkm
     * @param trackId Track id.
     */
    public RPKMvalue(PersistantFeature feature, double rpkm, double logRpkm, double coverageRpkm, double coverageLogRpkm, int readstarts, int coverage, int trackId) {
        super(trackId);
        this.feature = feature;
        this.rpkm = rpkm;
        this.logRpkm = logRpkm;
        this.coverageRpkm = coverageRpkm;
        this.coverageLogRpkm = coverageLogRpkm;
        this.coverage = coverage;
        this.readCount = readstarts;
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

    public double getLogRpkm() {
        return logRpkm;
    }

    public void setLogRpkm(double logRpkm) {
        this.logRpkm = logRpkm;
    }

    public double getCoverageRpkm() {
        return coverageRpkm;
    }

    public void setCoverageRpkm(double coverageRpkm) {
        this.coverageRpkm = coverageRpkm;
    }

    public double getCoverageLogRpkm() {
        return coverageLogRpkm;
    }

    public void setCoverageLogRpkm(double coverageLogRpkm) {
        this.coverageLogRpkm = coverageLogRpkm;
    }

    public int getCoverage() {
        return coverage;
    }

    public void setCoverage(int coverage) {
        this.coverage = coverage;
    }
    
    
}
