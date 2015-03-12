/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.transcriptionanalyses.datastructures;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.TrackResultEntry;


/**
 * Data storage for read count and normalized read count values (RPKM and TPM)
 * of a reference feature.
 * <br/>
 * <br/><b>RPKM = Reads per kilobase per million mapped reads -> Mortazavi et
 * al. 2008</b>: Mapping and Quantifying mammalian transcriptomes by RNA-Seq
 * <br/>
 * <br/><b>TPM = Transcripts per million -> Li et al. 2010</b>: RNA-Seq gene
 * expression estimation with read mapping uncertainty
 * <br/>
 *
 * @author Martin Tötsches, Rolf Hilker
 */
public class NormalizedReadCount extends TrackResultEntry {

    private PersistentFeature feature;
    private double effectiveFeatureLength;
    private int readLengthSum;
    private double rpkm;
    private double tpm;
    private double readCount;


    /**
     * Data storage for read count and normalized read count values (RPKM and
     * TPM) of a reference feature.
     * <br/>
     * <br/><b>RPKM = Reads per kilobase per million mapped reads -> Mortazavi et
     * al. 2008</b>: Mapping and Quantifying mammalian transcriptomes by
     * RNA-Seq
     * <br/>
     * <br/><b>TPM = Transcripts per million -> Li et al. 2010</b>: RNA-Seq gene
     * expression estimation with read mapping uncertainty
     * <br/>
     * <p>
     * @param feature   feature for which the values shall be stored
     * @param rpkm      the RPKM value for this feature
     * @param readCount the raw read count for this feature
     * @param trackId   the trackId for which these result values where
     *                  calculated
     */
    public NormalizedReadCount( PersistentFeature feature, double rpkm, double tpm, double readCount, int trackId ) {
        super( trackId );
        this.feature = feature;
        this.rpkm = rpkm;
        this.tpm = tpm;
        this.readCount = readCount;
        readLengthSum = 0;
        effectiveFeatureLength = 0;
    }


    /**
     * Data storage for read count and normalized read count values (RPKM and
     * TPM) of a reference feature.
     * <br/>
     * <br/><b>RPKM = Reads per kilobase per million mapped reads -> Mortazavi
     * et al. 2008</b>: Mapping and Quantifying mammalian transcriptomes by
     * RNA-Seq
     * <br/>
     * <br/><b>TPM = Transcripts per million -> Li et al. 2010</b>: RNA-Seq gene
     * expression estimation with read mapping uncertainty
     * <br/>
     * <p>
     * @param feature                Feature for which the values shall be
     *                               stored
     * @param rpkm                   The RPKM value for this feature
     * @param tpm                    The TPM value for this feature
     * @param readCount              The raw read count for this feature
     * @param effectiveFeatureLength The effective length of the feature = the
     *                               number of bases within the feature at which
     *                               reads can start
     * @param trackId                The trackId for which these result values
     *                               where calculated
     */
    public NormalizedReadCount( PersistentFeature feature, double rpkm, double tpm, double readCount, double effectiveFeatureLength, int trackID ) {
        this(feature, rpkm, tpm, readCount, trackID );
        this.effectiveFeatureLength = effectiveFeatureLength;
    }


    /**
     * @return The feature for which the values shall be stored.
     */
    public PersistentFeature getFeature() {
        return feature;
    }


    /**
     * @param feature Feature for which the values shall be stored.
     */
    public void setFeature( PersistentFeature feature ) {
        this.feature = feature;
    }


    /**
     * @return The raw read count for this feature.
     */
    public double getReadCount() {
        return readCount;
    }


    /**
     * @param readCount The raw read count for this feature
     */
    public void setReadCount( double readCount ) {
        this.readCount = readCount;
    }


    /**
     * @return The RPKM value for this feature.
     */
    public double getRPKM() {
        return rpkm;
    }


    /**
     * @param rpkm The RPKM value for this feature.
     */
    public void setRpkm( double rpkm ) {
        this.rpkm = rpkm;
    }


    /**
     * @return The TPM value for this feature.
     */
    public double getTPM() {
        return tpm;
    }


    /**
     * @param tpm The TPM value for this feature.
     */
    public void setTpm( double tpm ) {
        this.tpm = tpm;
    }


    /**
     * @param readLengthSum The value to add to the current read length sum.
     */
    public void addReadLength( int readLengthSum ) {
        this.readLengthSum += readLengthSum;
    }

    /**
     * @return The mean read length of all reads counted for the corresponding
     *         feature.
     */
    public double getReadLengthMean() {
        double readLengthMean = 0;
        if( readCount > 0 ) {
            readLengthMean = readLengthSum / readCount;
        }
        return readLengthMean;
    }


    /**
     * @return The sum of all lengths of all reads belonging to this transcript.
     */
    public int getReadLengthSum() {
        return readLengthSum;
    }


    /**
     * @param readLengthSum The sum of all lengths of all reads belonging to
     *                      this transcript.
     */
    public void setReadLengthSum( int readLengthSum ) {
        this.readLengthSum = readLengthSum;
    }

    /**
     * Either the effective length is calculated or it has to be set beforehand
     * explicitly by using {@link #storeEffectiveFeatureLength(double)}.
     * @return The effective feature length according to <b>Li and Dewey 2011:
     *         RSEM: accurate transcript quantification from RNA-Seq data with
     *         or without a reference genome</b>.
     */
    public double getEffectiveFeatureLength() {
        double realFeatLength = feature.getLength();
        if( effectiveFeatureLength > 0 ) {
            realFeatLength = effectiveFeatureLength;
        } else {
            realFeatLength = Utils.calcEffectiveFeatureLength( realFeatLength, getReadLengthMean() );
        }
        return realFeatLength;
    }


    /**
     * When the effective feature length is calculated externally, it can be
     * assigned to this NormalizedReadCount by this method.
     * @param effectiveFeatureLength The effective feature length according to
     *                               <b>Li and Dewey 2011: RSEM: accurate
     *                               transcript quantification from RNA-Seq data
     *                               with or without a reference genome</b>.
     */
    public void storeEffectiveFeatureLength( double effectiveFeatureLength ) {
        this.effectiveFeatureLength = effectiveFeatureLength;
    }


    /**
     * Utility class providing special methods for NormalizedReadCount objects.
     */
    public static final class Utils {

        /**
         * Instantiation not allowed.
         */
        private Utils() {
        }


        /**
         * Calculates the effective feature length according to
         * <b>Li and Dewey 2011: RSEM: accurate transcript quantification from
         * RNA-Seq data with or without a reference genome</b>.
         * <p>
         * @param featureLength The total length of the feature.
         * @param meanReadLength The mean read length of all reads asssociated
         * with the feature transcript.
         * @return The effective feature length. Minimum length is 1.
         */
        public static double calcEffectiveFeatureLength( double featureLength, double meanReadLength ) {
            double effLength = featureLength - meanReadLength + 1;
            effLength = effLength <= 0 ? 1 : effLength; //in operons reads can be longer than one gene!
            return effLength;//+1 because e.g.: 100 - 100 + 1 = 1 -> that's what we want
        }

    }


}
