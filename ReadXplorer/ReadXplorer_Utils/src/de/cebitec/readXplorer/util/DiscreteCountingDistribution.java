package de.cebitec.readXplorer.util;

/**
 * Contains the data structure for storing a distribution of count values.
 * The counts of the input are assigned into bins. The bins get more voluminous
 * with growing count size.
 *
 * @author -Rolf Hilker-
 */
public class DiscreteCountingDistribution {
    
    private static double[] standardBinSteps = {0.5, 1, 1, 1, 1.5, 1.5, 1.5, 1.5, 2, 2}; //= 135% of the adjustmentValue in total
    private static final int standardAdjustmentValue = 1000;
    private static final int noDistributionBins = 101;
    
    private int[] discreteCountingDistribution;
    private double[] binSteps;
    private byte type;
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
     * 1.5, 1.5, 2, 2} = 135% of the adjustmentValue in total.
     * This constructor also uses the standard adjustmentValue = 1000.
     */
    public DiscreteCountingDistribution() {
        this(standardBinSteps, standardAdjustmentValue);
    }
    
    /**
     * Contains the data structure for storing a distribution of count values.
     * The counts of the input are assigned into bins. The bins get more
     * voluminous with growing count size. This constructor uses the standard
     * bin step array with the following values: 
     * <br>{0.5, 1, 1, 1, 1.5, 1.5, 1.5, 1.5, 2, 2} = 135% of the 
     * adjustmentValue in total
     * @param adjustmentValue The value to serve as basis for the count
     * distribution to create. It is used as 100% value for the percentage
     * values in the binStepsPercent array.
     */
    public DiscreteCountingDistribution(int adjustmentValue) {
        this(standardBinSteps, adjustmentValue);
    }
    
    /**
     * Contains the data structure for storing a distribution of count values.
     * The counts of the input are assigned into bins. The bins get more
     * voluminous with growing count size. This constructor uses the standard
     * adjustmentValue = 1000.
     * @param binStepsPercent array of bin steps in percent. It has to contain
     * 10 elements. Each of the ten elements is used to create ten bins from
     * index 0 to index 10. This means, the percent values should be sorted for
     * the increasing bin values. In the end 100 bins are created.
     */
    public DiscreteCountingDistribution(double[] binStepsPercent) {
        this(binStepsPercent, standardAdjustmentValue);
    }
    
    /**
     * Contains the data structure for storing a distribution of count values.
     * The counts of the input are assigned into bins. The bins get more
     * voluminous with growing count size.
     * @param binStepsPercent array of bin steps in percent. It has to contain
     * 10 elements. Each of the ten elements is used to create ten bins from
     * index 0 to index 10. This means, the percent values should be sorted for
     * the increasing bin values. In the end 100 bins are created.
     * @param adjustmentValue The value to serve as basis for the count
     * distribution to create. It is used as 100% value for the percentage 
     * values in the binStepsPercent array.
     */
    public DiscreteCountingDistribution(double[] binStepsPercent, int adjustmentValue) {
        this.discreteCountingDistribution = new int[noDistributionBins];
        this.lowerBinBorders = new int[noDistributionBins];
        this.upperBinBorders = new int[noDistributionBins];
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
        for (int i = 0; i < noDistributionBins - 1; ++i) {
            if (stepsCounter % 10 == 0) { ++stepIdx; }
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
    public void increaseDistribution(int valueToAdd) {
        for (int i = 1; i < noDistributionBins; ++i) {
            if (valueToAdd < lowerBinBorders[i]) {
                ++discreteCountingDistribution[i - 1];
                break;
            }
        }
        if (valueToAdd > lowerBinBorders[lowerBinBorders.length - 1]) { //add values to last bin
            ++discreteCountingDistribution[lowerBinBorders.length - 1];
        }
        if (valueToAdd < minValue) {
            minValue = valueToAdd;
        }
        if (valueToAdd > maxValue) {
            maxValue = valueToAdd;
        }
        ++this.totalCount;
        this.sumValue += valueToAdd;
    }
    
    /**
     * Sets the total counts for a given index.
     * @param index the index for which the count should be set
     * @param count the total count for a given index of the distribution
     * @throws ArrayIndexOutOfBoundsException  
     */
    public void setCountForIndex(int index, int count) throws ArrayIndexOutOfBoundsException {
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
     * @return true, if the distribution only contains 0 entries, false otherwise
     */
    public boolean isEmpty() {
        for (int i : this.discreteCountingDistribution) {
            if (i > 0 ) {
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
     * @return the type of this distribution: Either Properties.COVERAGE_INCREASE_DISTRIBUTION
     * or Properties.COVERAGE_INC_PERCENT_DISTRIBUTION
     */
    public byte getType() {
        return this.type;
    }

    /**
     * Set the type of this distribution.
     * @param type of this distribution: Either Properties.COVERAGE_INCREASE_DISTRIBUTION
     * or Properties.COVERAGE_INC_PERCENT_DISTRIBUTION
     */
    public void setType(byte type) {
        this.type = type;
    }
    
    /**
     * @param count the count value for which the index is needed.
     * @return the index of the distribution used for a count like the given one 
     */
    public int getIndexForCountValue(int count) {
        int index = -1;
        for (int i = 0; i < noDistributionBins; ++i) {
            if (count > lowerBinBorders[i]) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    /**
     * 
     * @param index the bin index, for which the lower border value is needed
     * @return the lower border value for the given index
     */
    public int getMinValueForIndex(int index) {
        return lowerBinBorders[index];
    }
    
    /**
     * @return The average value of the distribution, which is the sum of all
     * added values divided by the total count of values.
     */
    public int getAverageValue() {
        int value;
        if (totalCount == 0)  {
            value = 0;
        } else {
            value = (int) (this.sumValue/this.totalCount);
        }
        return value;
    }
    
}
