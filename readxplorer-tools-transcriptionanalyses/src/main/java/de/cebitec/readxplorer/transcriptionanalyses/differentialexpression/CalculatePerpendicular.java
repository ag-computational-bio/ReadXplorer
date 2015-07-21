/*
 * Copyright (C) 2015 Agne Matuseviciute
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

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.regression.SimpleRegression;


/**
 *
 * @author AM
 */
public class CalculatePerpendicular implements CoreCalculationI {
    
    
    @Override
    public LinearRegressionResult calculate( int[] conditionA, int[] conditionB )
            throws IllegalArgumentException {
        LinearRegressionResult result = new LinearRegressionResult();
        
        if( conditionA.length != conditionB.length ) {
            throw new IllegalArgumentException();
        }

        // sum(x^2)
        double sumConditionA2 = computeSquaredSum( conditionA );

        //mean(x)
        double meanConditionA = computeMean( conditionA );

        // sum(y^2)
        double sumConditionB2 = computeSquaredSum( conditionB );

        // mean(y)
        double meanConditionB = computeMean( conditionB );

        // sum(y*x)
        double sumConditionsAB = computeSumOfProducts( conditionA, conditionB );
        
        int n = conditionA.length;

        // b <- 0.5*((sum(x^2) - n*mean(x)^2) - (sum(y^2) - n*mean(y)^2)) / (n*mean(y)*mean(x) - sum(y*x))
        double b = 0.5 * (((sumConditionA2 - (n * meanConditionA * meanConditionA)) -
                           (sumConditionB2 - (n * meanConditionB * meanConditionB))) /
                          (n * meanConditionB * meanConditionA - sumConditionsAB));

        //r <- cor(y,x)
        double r = computePearsonsCorrelationCoefficient( conditionA, conditionB, meanConditionA, meanConditionB );
        
        double[] conditionADouble = new double[conditionA.length];
        double[] conditionBDouble = new double[conditionB.length];
        
        for( int i = 0; i < conditionA.length; i++ ) {
            conditionADouble[i] = conditionA[i];
            conditionBDouble[i] = conditionB[i];
        }
        
        double correlationApacheSpearman = new SpearmansCorrelation().correlation( conditionADouble, conditionBDouble );
        
        SimpleRegression regression = new SimpleRegression();
        for( int i = 0; i < conditionADouble.length; i++ ) {
            regression.addData( conditionADouble[i], conditionBDouble[i] );
        }

        //slope <- (-b + sqrt(b^2 + 1))
        double slope;
        if( r >= 0 ) {
            slope = (-1.0 * b) + Math.sqrt( (b * b) + 1 );
        } else {
            slope = (-1.0 * b) - Math.sqrt( (b * b) + 1 );
        }

        //inter <- mean(x) - slope*mean(y)
        double inter = meanConditionA - (slope * meanConditionB);

        //double rsq = r; // * r;
        //r *= r;

        result.setIntercept( inter );
        result.setPearsonCorrelation( regression.getR() );
        result.setPearsonSlope( regression.getSlope() );
        result.setPearsonIntercept( regression.getIntercept() );
        result.setR( r );
        result.setSlope( slope );
        result.setSpearmanCorrelation( correlationApacheSpearman );
        
        return result;
    }


    /**
     * Calculate pearsons correlation coefficient
     * <p>
     * @param x
     * @param y <p>
     * @return r
     */
    protected static final double computePearsonsCorrelationCoefficient(
            final int[] x,
            final int[] y ) {
        return computePearsonsCorrelationCoefficient( x, y, computeMean( x ), computeMean( y ) );
    }


    /**
     * Calculate pearsons correlation coefficient
     * <p>
     * @param x
     * @param y
     * @param meanx
     * @param meany <p>
     * @return r
     */
    protected static final double computePearsonsCorrelationCoefficient(
            final int[] x,
            final int[] y,
            final double meanx,
            final double meany ) {
        
        if( x.length != y.length ) {
            return Double.NaN;
        }
        int n = x.length;
        
        double covxy = 0.0;
        double varx = 0.0;
        double vary = 0.0;
        
        for( int i = 0; i < n; i++ ) {
            covxy += (x[i] - meanx) * (y[i] - meany);
            
            double xm = x[i] - meanx;
            varx += xm * xm;
            
            double ym = y[i] - meany;
            vary += ym * ym;
        }
        
        return covxy / Math.sqrt( varx * vary );
    }


    /**
     * Computes the arithmetic mean of the given values
     * <p>
     * @param values <p>
     * @return mean
     */
    protected static final double computeMean( int[] values ) {
        double mean = 0.0;
        for( int i = 0; i < values.length; i++ ) {
            mean += values[i];
        }
        return mean / values.length;
    }


    /**
     * Computes the sum of all squared values (sum overall values^2)
     * <p>
     * @param values <p>
     * @return squaredSum
     */
    protected static final double computeSquaredSum( int[] values ) {
        double sqrsum = 0.0;
        for( int i = 0; i < values.length; i++ ) {
            sqrsum += values[i] * values[i];
        }
        return sqrsum;
    }


    /**
     * Given two vectors this function calculates the sum of the products of
     * each element of the first vector with its corresponding element in the
     * second one.
     * <p>
     * Both vectors must have the same length, otherwise NaN is returned.
     * <p>
     * @param values1
     * @param values2 <p>
     * @return sum
     */
    protected static final double computeSumOfProducts( int[] values1, int[] values2 ) {
        if( values1.length != values2.length ) {
            return Double.NaN;
        }
        
        double sum = 0.0;
        for( int i = 0; i < values1.length; i++ ) {
            sum += values1[i] * values2[i];
        }
        return sum;
    }
    
    
}
