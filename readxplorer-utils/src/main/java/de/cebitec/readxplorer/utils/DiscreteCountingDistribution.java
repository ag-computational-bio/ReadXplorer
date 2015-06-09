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

package de.cebitec.readxplorer.utils;

import de.cebitec.readxplorer.api.enums.Distribution;



/**
 * Contains the data structure for storing a distribution of count values. The
 * counts of the input are assigned into bins. The bins get more voluminous with
 * growing count size.
 *
 * @author -Rolf Hilker-
 */
public class DiscreteCountingDistribution {

    private static double[] standardBinSteps = {0.5, 1, 1, 1, 1.5, 1.5, 1.5, 1.5, 2, 2}; //= 135% of the adjustmentValue in total
    private static final int STANDARD_ADJUSTMENT_VALUE = 1000;
    private static final int NO_DISTRIBUTION_BINS = 101;

    private int[] discreteCountingDistribution;
    private double[] binSteps;
    private Distribution type;
    private int minValue;
    private long maxValue;
    private long totalCount;
    private int[] lowerBinBorders;
    private int[] upperBinBorders;
    private int adjustmentValue;
    private long sumValue;


    /**
     * Contains the data structure for storing a distribution of count values.
     * The counts of the input are assigned into bins. The bins get more
     * voluminous with growing count size. This constructor uses the standard
     * bin step array with the following values: <br>{0.5, 1, 1, 1, 1.5, 1.5,
     * 1.5, 1.5, 2, 2} = 135% of the adjustmentValue in total. This constructor
     * also uses the standard adjustmentValue = 1000.
     */
    public DiscreteCountingDistribution() {
        this( standardBinSteps, STANDARD_ADJUSTMENT_VALUE );
    }


    /**
     * Contains the data structure for storing a distribution of count values.
     * The counts of the input are assigned into bins. The bins get more
     * voluminous with growing count size. This constructor uses the standard
     * bin step array with the following values:
     * <br>{0.5, 1, 1, 1, 1.5, 1.5, 1.5, 1.5, 2, 2} = 135% of the
     * adjustmentValue in total
     * <p>
     * @param adjustmentValue The value to serve as basis for the count
     * distribution to create. It is used as 100% value for the percentage
     * values in the binStepsPercent array.
     */
    public DiscreteCountingDistribution( int adjustmentValue ) {
        this( standardBinSteps, adjustmentValue );
    }


    /**
     * Contains the data structure for storing a distribution of count values.
     * The counts of the input are assigned into bins. The bins get more
     * voluminous with growing count size. This constructor uses the standard
     * adjustmentValue = 1000.
     * <p>
     * @param binStepsPercent array of bin steps in percent. It has to contain
     * 10 elements. Each of the ten elements is used to create ten bins from
     * index 0 to index 10. This means, the percent values should be sorted for
     * the increasing bin values. In the end 100 bins are created.
     */
    public DiscreteCountingDistribution( double[] binStepsPercent ) {
        this( binStepsPercent, STANDARD_ADJUSTMENT_VALUE );
    }


    /**
     * Contains the data structure for storing a distribution of count values.
     * The counts of the input are assigned into bins. The bins get more
     * voluminous with growing count size.
     * <p>
     * @param binStepsPercent array of bin steps in percent. It has to contain
     * 10 elements. Each of the ten elements is used to create ten bins from
     * index 0 to index 10. This means, the percent values should be sorted for
     * the increasing bin values. In the end 100 bins are created.
     * @param adjustmentValue The value to serve as basis for the count
     * distribution to create. It is used as 100% value for the percentage
     * values in the binStepsPercent array.
     */
    public DiscreteCountingDistribution( double[] binStepsPercent, int adjustmentValue ) {
        this.discreteCountingDistribution = new int[NO_DISTRIBUTION_BINS];
        this.lowerBinBorders = new int[NO_DISTRIBUTION_BINS];
        this.upperBinBorders = new int[NO_DISTRIBUTION_BINS];
        this.binSteps = binStepsPercent;
        this.adjustmentValue = adjustmentValue;
        this.createBins();
        this.totalCount = 0;
        this.minValue = 0;
        this.maxValue = 0;
    }


    /**
     * Creates the bins of the distribution for the given binStepsPercent and
     * adjustmentValue.
     */
    private void createBins() {
        int border = 0;
        int lastBorder = 0;
        int stepsCounter = 0;
        int stepIdx = -1;
        for( int i = 0; i < NO_DISTRIBUTION_BINS - 1; ++i ) {
            if( stepsCounter % 10 == 0 ) {
                ++stepIdx;
            }
            border += (int) (binSteps[stepIdx] * adjustmentValue * 0.01);
            lowerBinBorders[i] = lastBorder + 1; //inclusive values
            upperBinBorders[i] = border;
            lastBorder = border;
            ++stepsCounter;
        }
        lowerBinBorders[lowerBinBorders.length - 1] = lastBorder + 1;
        upperBinBorders[upperBinBorders.length - 1] = Integer.MAX_VALUE;
    }


    /**
     *
     * @param valueToAdd The value to add to the distribution
     */
    public void increaseDistribution( int valueToAdd ) {
        for( int i = 1; i < NO_DISTRIBUTION_BINS; ++i ) {
            if( valueToAdd < lowerBinBorders[i] ) {
                ++discreteCountingDistribution[i - 1];
                break;
            }
        }
        if( valueToAdd > lowerBinBorders[lowerBinBorders.length - 1] ) { //add values to last bin
            ++discreteCountingDistribution[lowerBinBorders.length - 1];
        }
        if( valueToAdd < minValue ) {
            minValue = valueToAdd;
        }
        if( valueToAdd > maxValue ) {
            maxValue = valueToAdd;
        }
        ++this.totalCount;
        this.sumValue += valueToAdd;
    }


    /**
     * Sets the total counts for a given index.
     * <p>
     * @param index the index for which the count should be set
     * @param count the total count for a given index of the distribution
     * <p>
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setCountForIndex( int index, int count ) {
        this.totalCount += count - this.discreteCountingDistribution[index];
        this.discreteCountingDistribution[index] = count;
    }


    /**
     * @return the array containing the discrete counting distribution.
     */
    public int[] getDiscreteCountingDistribution() {
        return this.discreteCountingDistribution;
    }


    /**
     * @return true, if the distribution only contains 0 entries, false
     * otherwise
     */
    public boolean isEmpty() {
        for( int i : this.discreteCountingDistribution ) {
            if( i > 0 ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @return The total count of entries for this data set.
     */
    public long getTotalCount() {
        return this.totalCount;
    }


    /**
     * @return the type of this distribution: Either
     * Properties.COVERAGE_INCREASE_DISTRIBUTION or
     * Properties.COVERAGE_INC_PERCENT_DISTRIBUTION
     */
    public Distribution getType() {
        return this.type;
    }


    /**
     * Set the type of this distribution.
     * <p>
     * @param type of this distribution
     */
    public void setType( Distribution type ) {
        this.type = type;
    }


    /**
     * @param count the count value for which the index is needed.
     * <p>
     * @return the index of the distribution used for a count like the given one
     */
    public int getIndexForCountValue( int count ) {
        int index = -1;
        for( int i = 0; i < NO_DISTRIBUTION_BINS; ++i ) {
            if( count > lowerBinBorders[i] ) {
                index = i;
                break;
            }
        }
        return index;
    }


    /**
     *
     * @param index the bin index, for which the lower border value is needed
     * <p>
     * @return the lower border value for the given index
     */
    public int getMinValueForIndex( int index ) {
        return lowerBinBorders[index];
    }


    /**
     * @return The average value of the distribution, which is the sum of all
     * added values divided by the total count of values.
     */
    public int getAverageValue() {
        int value;
        if( totalCount == 0 ) {
            value = 0;
        } else {
            value = (int) (this.sumValue / this.totalCount);
        }
        return value;
    }


}
