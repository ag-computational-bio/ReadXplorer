/*
 * Copyright (C) 2015 AM
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author Agne Matuseviciute
 */
public class LinearRegression implements LinearRegressionI {

    private Map<PersistentFeature, double[]> genesToScoreMap = new HashMap<>();
    private double numberOfResults = 0;
    private double addedUpSlopeValues = 0;
    private ProcessingLog log;


    public LinearRegression( ProcessingLog log ) {
        this.log = log;
    }


    @Override
    public Map<PersistentFeature, double[]> getResults() throws IllegalStateException {
        return genesToScoreMap;
    }


    @Override
    public void process( Map<Integer, Map<PersistentFeature, int[]>> countData ) {
        Set<Integer> conditions = countData.keySet();
        Integer[] conditionsArray = conditions.toArray(
                new Integer[conditions.size()] );
        Map<PersistentFeature, int[]> geneSet1
                = countData.get( conditionsArray[0] );
        Map<PersistentFeature, int[]> geneSet2
                = countData.get( conditionsArray[1] );

        genesToScoreMap = this.findSimilarity( geneSet1, geneSet2 );
        genesToScoreMap = this.calculateMeanSlope( genesToScoreMap );
    }


    /**
     * Runs calculation of differential expression for each gene in two
     * conditions.
     * <p>
     * @param geneSet1
     * @param geneSet2
     */
    private Map<PersistentFeature, double[]> findSimilarity( Map<PersistentFeature, int[]> geneSet1,
                                                             Map<PersistentFeature, int[]> geneSet2 ) {
        Map<PersistentFeature, double[]> result = new HashMap<>();
        for( Map.Entry<PersistentFeature, int[]> gene1
             : geneSet1.entrySet() ) {
            for( Map.Entry<PersistentFeature, int[]> gene2
                 : geneSet2.entrySet() ) {
                if( gene1.getKey() == null ? gene2.getKey() ==
                                             null : gene1.getKey().equals( gene2.getKey() ) ) {
                    int[] gene1Values = gene1.getValue();
                    int[] gene2Values = gene2.getValue();
                    double[] calculated = runFilteredRegressionCalculation(
                            gene1Values, gene2Values );
                    Double slopeValue = calculated[1];
                    if( !(slopeValue.isInfinite() || slopeValue.isNaN()) ) {
                        numberOfResults++;
                        addedUpSlopeValues += slopeValue;
                    }
                    result.put( gene1.getKey(), calculated );
                }
            }
        }
        return result;
    }


    private Map<PersistentFeature, double[]> calculateMeanSlope( Map<PersistentFeature, double[]> genesMap ) {
        double meanSlopeValue = Math.abs( addedUpSlopeValues / numberOfResults );
        log.addProperty( "Mean Slope Value", meanSlopeValue );
        Map<PersistentFeature, double[]> ret = new HashMap<>();
        for( PersistentFeature key : genesMap.keySet() ) {
            double[] values = genesMap.get( key );
            double[] enhancedValues = new double[values.length + 1];
            enhancedValues[0] = values[0];
            enhancedValues[1] = values[1];
            enhancedValues[2] = values[1] / meanSlopeValue;
            enhancedValues[3] = values[2];
            enhancedValues[4] = values[3];
            enhancedValues[5] = values[4];
            ret.put( key, enhancedValues );
        }

        return ret;
    }


    /**
     * Returns true if all coverage values in gene are larger than limit.
     * <p>
     * @param geneValues
     * @param limit
     */
    private boolean coverageFilter( int[] geneValues, int limit ) {

        for( int k = 0; k < geneValues.length; k++ ) {
            if( geneValues[k] >= limit ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Filters out not proper values.If all values in both conditions are
     * smaller than 5, samples will be discarded. If one sample has no
     * expression and another has
     * <p>
     * @param gene1Values
     * @param gene2Values
     */
    public double[] runFilteredRegressionCalculation( int[] gene1Values, int[] gene2Values ) {
        double[] calculated = new double[5];
        int limitLow = 1;
        int limitNormal = 7;

        if( coverageFilter( gene1Values, limitNormal ) &&
            coverageFilter( gene2Values, limitNormal ) ) {
            CalculatePerpendicular rSqrt = new CalculatePerpendicular();
            calculated = rSqrt.calculate( gene1Values, gene2Values );
        } else if( coverageFilter( gene1Values, limitNormal ) &&
                   !(coverageFilter( gene2Values, limitLow )) ) {
            calculated = new double[]{ Double.POSITIVE_INFINITY,
                                       Double.POSITIVE_INFINITY,
                                       Double.POSITIVE_INFINITY,
                                       Double.POSITIVE_INFINITY,
                                       Double.POSITIVE_INFINITY };
        } else if( !(coverageFilter( gene1Values, limitLow )) &&
                   coverageFilter( gene2Values, limitNormal ) ) {
            calculated = new double[]{ Double.NEGATIVE_INFINITY,
                                       Double.NEGATIVE_INFINITY,
                                       Double.NEGATIVE_INFINITY,
                                       Double.NEGATIVE_INFINITY,
                                       Double.NEGATIVE_INFINITY };
        }
        return calculated;
    }


}