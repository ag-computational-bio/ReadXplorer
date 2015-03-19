/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.NormalizedReadCount;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.Map;



/**
 * Utility class containing formulas for the normalization of read counts.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class NormalizationFormulas {

    /**
     * Instantiation not allowed!
     */
    private NormalizationFormulas() {
    }


    /**
     * Calculates <b>RPKM = Reads per kilobase per million mapped reads</b>
     * according to the formula presented in the paper:
     * <br/><b>Mortazavi et al. 2008</b>: Mapping and Quantifying mammalian
     * transcriptomes by RNA-Seq.
     * <br>
     * <br><b>RPKM = 10^9 * C / (N * L)</b> where
     * <br>C = number of mappable reads for gene
     * <br>N = total number of mappable reads for experiment/data set
     * <br>L = sum of gene base pairs
     * <p>
     * @param noFeatureReads C
     * @param totalMappedReads N
     * @param geneExonLength L
     * @return The calculated RPKM value.
     */
    public static double calculateRpkm(double noFeatureReads, double totalMappedReads, double geneExonLength) {
        return noFeatureReads * 1000000000 / (totalMappedReads * geneExonLength);
    }


    /**
     * Calculates <b>TPM = Transcripts per million</b> according to the formula
     * presented in the paper:
     * <br><b>Li et al. 2010</b>: RNA-Seq gene expression estimation with read
     * mapping uncertainty.
     * <br>
     * <br><b>TPM = 10^6 * (c / l) * (1 / (sum_i (c_i / l_i)))</b> where
     * <br>c = number of mappable reads for gene (or genomic feature)
     * <br>l = effective length (or length) of gene (or genomic feature)
     * <br>i = 1 - #genes (or genomic features of the same type)
     * @param noFeatureReads c
     * @param effectiveFeatureLength l
     * @param normalizationSum precalculated sum of all (c_i / l_i)
     * @return The calculated TPM value.
     */
    public static double calculateTpm(double noFeatureReads, double effectiveFeatureLength, double normalizationSum) {
        double tpm = 0;
        if( effectiveFeatureLength > 0 ) {
            tpm = 1000000 * (noFeatureReads / effectiveFeatureLength) / normalizationSum;
        }
        return tpm;
    }


    /**
     * According to Li et al. 2010: RNA-Seq gene expression estimation with read
     * mapping uncertainty, the normalization sum is
     * <br/><b>1 / (sum_i (v_i/l_i))</b>
     * <br/>were
     * <br/><b>v_i = c_i/N</b><br/>
     * with c_i = #reads of feature and N = #compatible reads in the whole
     * sample (~ total read count/mapping count) which after reducing the
     * complete TPM formula by N becomes:
     * <br/><b>1 / (sum_i (c_i/l_i))</b>.
     * <br/>Thus here the sum is created for all features, summing the feature
     * read count divided by the feature length.
     * <p>
     * @param featureReadCount    Map containing the read counts for all
     *                            features
     * @param normalizationSumMap Map to modify and store the sum for
     *                            normalization
     */
    public static void calculateNormalizationSums( Map<Integer, NormalizedReadCount> featureReadCount,
                                            Map<FeatureType, Double> normalizationSumMap ) {
        for( int featId : featureReadCount.keySet() ) {
            NormalizedReadCount countObject = featureReadCount.get( featId );
            PersistentFeature feature = countObject.getFeature();
            double readCount = countObject.getReadCount();
            double effectiveFeatLength = countObject.getEffectiveFeatureLength();
            normalizationSumMap.put( feature.getType(), normalizationSumMap.get( feature.getType() ) + readCount / effectiveFeatLength );
        }
    }

}
