/*
 * Copyright (C) 2015 Kai Bernd Stadermann
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


/**
 *
 * @author kstaderm
 */
public class LinearRegressionResult {

    private double intercept;
    private double slope;
    private double normalizedSlope;
    private double r;
    private double pearsonCorrelation;
    private double pearsonSlope;
    private double pearsonIntercept;
    private double spearmanCorrelation;


    public LinearRegressionResult( double intercept, double slope, double normalizedSlope,
                                   double pearsonCorrelation, double pearsonSlope,
                                   double pearsonIntercept, double spearmanCorrelation ) {
        this.intercept = intercept;
        this.slope = slope;
        this.normalizedSlope = normalizedSlope;
        this.pearsonCorrelation = pearsonCorrelation;
        this.pearsonSlope = pearsonSlope;
        this.pearsonIntercept = pearsonIntercept;
        this.spearmanCorrelation = spearmanCorrelation;
    }


    public LinearRegressionResult() {
    }


    public boolean allValuesZero() {
        //Summing everything up an than comparing it one time is cheaper than
        //multiple comparision.
        double allSummedUp = intercept + slope + normalizedSlope + r +
                             pearsonCorrelation + pearsonSlope + spearmanCorrelation;
        return (allSummedUp == 0.0);
    }


    public double getIntercept() {
        return intercept;
    }


    public void setIntercept( double intercept ) {
        this.intercept = intercept;
    }


    public double getSlope() {
        return slope;
    }


    public void setSlope( double slope ) {
        this.slope = slope;
    }


    public double getNormalizedSlope() {
        return normalizedSlope;
    }


    public void setNormalizedSlope( double normalizedSlope ) {
        this.normalizedSlope = normalizedSlope;
    }


    public double getR() {
        return r;
    }


    public void setR( double r ) {
        this.r = r;
    }


    public double getPearsonCorrelation() {
        return pearsonCorrelation;
    }


    public void setPearsonCorrelation( double pearsonCorrelation ) {
        this.pearsonCorrelation = pearsonCorrelation;
    }


    public double getPearsonSlope() {
        return pearsonSlope;
    }


    public void setPearsonSlope( double pearsonSlope ) {
        this.pearsonSlope = pearsonSlope;
    }


    public double getPearsonIntercept() {
        return pearsonIntercept;
    }


    public void setPearsonIntercept( double pearsonIntercept ) {
        this.pearsonIntercept = pearsonIntercept;
    }


    public double getSpearmanCorrelation() {
        return spearmanCorrelation;
    }


    public void setSpearmanCorrelation( double spearmanCorrelation ) {
        this.spearmanCorrelation = spearmanCorrelation;
    }


}
