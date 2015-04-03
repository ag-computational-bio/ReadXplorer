/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

/**
 *
 * @author AM
 */
public class CalculatePerpendicular implements CoreCalculationI{
    
    public static int LEAST_SQUARES_INTERCEPT = 0;
    public static int LEAST_SQUARES_SLOPE = 1;
    public static int LEAST_SQUARES_RSQUARE = 2;
    public static int LEAST_SQUARES_STDERR = 3;
    public static int LEAST_SQUARES_SUM_SQUARED_ERROR = 4;

    @Override
    public double[] calculate(int[] conditionA, int[] conditionB) 
            throws IllegalArgumentException {
               double[] result = new double[3];

        if (conditionA.length != conditionB.length) {
            throw new IllegalArgumentException();
        }
       
        // sum(x^2)
        double sumConditionA2 = computeSquaredSum(conditionA);

        //mean(x)
        double meanConditionA = computeMean(conditionA);

        // sum(y^2)
        double sumConditionB2 = computeSquaredSum(conditionB);

        // mean(y)
        double meanConditionB = computeMean(conditionB);

        // sum(y*x)
        double sumConditionsAB = computeSumOfProducts(conditionA, conditionB);

        int n = conditionA.length;
        
        // B <- 0.5*((sum(x^2) - n*mean(x)^2) - (sum(y^2) - n*mean(y)^2)) / (n*mean(y)*mean(x) - sum(y*x))
        double B = 0.5 * (
                ((sumConditionA2 - (n * meanConditionA*meanConditionA)) - (sumConditionB2 - (n * meanConditionB * meanConditionB)))
                / (n * meanConditionB * meanConditionA - sumConditionsAB));

        //slope <- (-B + sqrt(B^2 + 1))
        double slope = (-1.0 * B) + Math.sqrt( (B * B) +1);

        //inter <- mean(x) - slope*mean(y)
        double inter = meanConditionA - (slope * meanConditionB);

        //rsq <- cor(y,x)^2
        double r = computePearsonsCorrelationCoefficient(conditionA,conditionB,meanConditionA,meanConditionB);
        //double rsq = r; // * r;

        result[LEAST_SQUARES_INTERCEPT] = inter;
        result[LEAST_SQUARES_SLOPE] = slope;
        result[LEAST_SQUARES_RSQUARE] = r;
        //result[LEAST_SQUARES_STDERR] = Double.NaN;

        return result;
    }
        
      
    /**
     * Calculate pearsons correlation coefficient
     * @param x
     * @param y
     * @return r
     */
    protected static final double computePearsonsCorrelationCoefficient(
            final int[] x,
            final int[] y) {
        return computePearsonsCorrelationCoefficient(x,y,computeMean(x),computeMean(y));
    }

    /**
     * Calculate pearsons correlation coefficient
     * @param x
     * @param y
     * @param meanx
     * @param meany
     * @return r
     */
    protected static final double computePearsonsCorrelationCoefficient(
            final int[] x,
            final int[] y,
            final double meanx,
            final double meany) {

        if (x.length != y.length) return Double.NaN;
        int n = x.length;

        double covxy = 0.0;
        double varx = 0.0;
        double vary = 0.0;

        for (int i=0; i<n; i++) {
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
     * @param values
     * @return mean
     */
    protected static final double computeMean(int[] values) {
        double mean = 0.0;
        for (int i=0; i<values.length; i++) {
            mean += values[i];
        }
        return mean / (double) values.length;
    }

    /**
     * Computes the sum of all squared values
     * (sum overall values^2)
     *
     * @param values
     * @return squaredSum
     */
    protected static final double computeSquaredSum(int[] values) {
        double sqrsum = 0.0;
        for (int i=0; i<values.length; i++) {
            sqrsum += values[i] * values[i];
        }
        return sqrsum;
    }

    /**
     * Given two vectors this function calculates the sum of the products
     * of each element of the first vector with its corresponding
     * element in the second one.
     *
     * Both vectors must have the same length, otherwise NaN is returned.
     *
     * @param values1
     * @param values2
     * @return sum
     */
    protected static final double computeSumOfProducts(int[] values1, int[] values2) {
        if (values1.length != values2.length) return Double.NaN;

        double sum = 0.0;
        for (int i=0; i<values1.length; i++) {
            sum += values1[i] * values2[i];
        }
        return sum;
    }
  
    
    
}
