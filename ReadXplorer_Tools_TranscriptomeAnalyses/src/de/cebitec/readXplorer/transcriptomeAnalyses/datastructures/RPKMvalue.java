package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;


/**
 * Data storage for RPKM and read count values of a reference feature.
 *
 * @author Martin Tötsches, Rolf Hilker, extended by jritter
 */
public class RPKMvalue extends TrackChromResultEntry {

    private PersistentFeature feature;
    private int readCount, longestKnownUtrLength;
    private double rpkm, logRpkm;


    /**
     * Data storage for RPKM and read count values of a reference feature.
     *
     * @param feature   feature for which the values shall be stored
     * @param rpkm      the RPKM value for this feature
     * @param readCount the raw read count for this feature
     * @param trackId   the trackId for which these result values where
     *                  calculated
     */
    public RPKMvalue( PersistentFeature feature, double rpkm, int readCount, int trackId, int chromId ) {
        super( trackId, chromId );
        this.feature = feature;
        this.rpkm = rpkm;
        this.readCount = readCount;
    }


    /**
     * Data storage for RPKM and read count values of a reference feature.
     *
     * @param feature    PersistentFeature for which the RPKM values are.
     * @param rpkm       Reads per kilobase of exon per million mapped reads.
     * @param logRpkm
     * @param readstarts
     * @param trackId    Track ID.
     * @param chromId    Chrom ID.
     */
    public RPKMvalue( PersistentFeature feature, double rpkm, double logRpkm,
                      int readstarts, int trackId, int chromId ) {
        super( trackId, chromId );
        this.feature = feature;
        this.rpkm = rpkm;
        this.logRpkm = logRpkm;
        this.readCount = readstarts;
    }


    /**
     * @return the RPKM value for this feature.
     */
    public double getRPKM() {
        return rpkm;
    }


    /**
     * Sets the RPKM value.
     *
     * @param rpkm the RPKM value
     */
    public void setRpkm( double rpkm ) {
        this.rpkm = rpkm;
    }


    /**
     * @return the feature for which the values shall be stored.
     */
    public PersistentFeature getFeature() {
        return feature;
    }


    /**
     * @param feature for which the values was determined
     */
    public void setFeature( PersistentFeature feature ) {
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
    public void setReadCount( int readCount ) {
        this.readCount = readCount;
    }


    /**
     *
     * @return
     */
    public double getLogRpkm() {
        return logRpkm;
    }


    /**
     *
     * @param logRpkm
     */
    public void setLogRpkm( double logRpkm ) {
        this.logRpkm = logRpkm;
    }


    /**
     *
     * @return
     */
    public int getLongestKnownUtrLength() {
        return longestKnownUtrLength;
    }


    /**
     *
     * @param longestKnownUtrLength
     */
    public void setLongestKnownUtrLength( int longestKnownUtrLength ) {
        this.longestKnownUtrLength = longestKnownUtrLength;
    }


}
