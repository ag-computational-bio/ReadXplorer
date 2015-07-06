/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.expresstest;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.ProcessingLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;


/**
 *
 * @author kstaderm
 */
public class ExpressTest implements ExpressTestI {

    private final List<ExpressTestObserver> observers;
    private List<List<Object>> results;
    private List<List<Object>> resultsNormalized;
    private List<Object> rowNames;
    private final List<Object> colNames;
//    private final Map<double[], Double> meanCache;
    private int[] normalizationFeatures;
    private final ProcessingLog processingLog;
    private boolean useHousekeepingGenesForNormalization = false;


    public ExpressTest( ProcessingLog processingLog ) {
        this.processingLog = processingLog;
//        this.meanCache = new HashMap<>( 1024 );
        this.observers = new LinkedList<>();
        this.colNames = Arrays.asList( new Object[]{ "Region", "Start",
                                                     "Stop", "MeanA", "VarA", "MeanB", "VarB", "RatioAB", "RatioBA", "Confidence" } );

    }


    @Override
    public void performAnalysis( PersistentFeature[] regionNames, int[] start, int[] stop, int[][] groupA,
                                 int[][] groupB, double cutOff ) throws IllegalArgumentException {

        notifyObservers( ExpressTestStatus.RUNNING );

        final int regionLength = regionNames.length;
        for( int[] groupA1 : groupA ) {
            if( regionLength != groupA1.length ) {
                notifyObservers( ExpressTestStatus.FAILED );
                throw new IllegalArgumentException( "There must be an entry in groupA and groupB for each region!" );
            }
        }
        for( int[] groupB1 : groupB ) {
            if( regionLength != groupB1.length ) {
                notifyObservers( ExpressTestStatus.FAILED );
                throw new IllegalArgumentException( "There must be an entry in groupA and groupB for each region!" );
            }
        }
        if( regionLength != start.length || regionLength != stop.length ) {
            notifyObservers( ExpressTestStatus.FAILED );
            throw new IllegalArgumentException( "There must be an entry in groupA and groupB for each region!" );
        }

        //Compute mean and variance between the replicates of each group
        double[] meanCountsA;
        double[] meanCountsB;
        if( useHousekeepingGenesForNormalization ) {
            final int[][] houseKeepingA = new int[groupA.length][normalizationFeatures.length];
            final int[][] houseKeepingB = new int[groupB.length][normalizationFeatures.length];
            Arrays.parallelSort( normalizationFeatures ); // necessary for later binary search
            for( int i = 0, j = 0; i < regionLength; i++ ) {
                if( Arrays.binarySearch( normalizationFeatures, regionNames[i].getId() ) >= 0 ) {
                    for( int k = 0; k < groupA.length; k++ ) {
                        houseKeepingA[k][j] = groupA[k][i];
                    }
                    for( int k = 0; k < groupB.length; k++ ) {
                        houseKeepingB[k][j] = groupB[k][i];
                    }
                    j++;
                }
            }
            boolean meanCountContainsZero = false;
            meanCountsA = calculateMeanCountsForEachReplicate( houseKeepingA );
            meanCountsB = calculateMeanCountsForEachReplicate( houseKeepingB );
            if( !zeroFreeValues( meanCountsA ) ) {
                meanCountContainsZero = true;
                String msg = "One of the selected house keeping genes has no mapping read under condition A." +
                             " The default normalization method will be used.";
                String title = "Unable to normalize using house keeping genes.";
                JOptionPane.showMessageDialog( null, msg, title, JOptionPane.INFORMATION_MESSAGE );
            }
            if( !zeroFreeValues( meanCountsB ) ) {
                meanCountContainsZero = true;
                String msg = "One of the selected house keeping genes has no mapping read under condition B." +
                             " The default normalization method will be used.";
                String title = "Unable to normalize using house keeping genes.";
                JOptionPane.showMessageDialog( null, msg, title, JOptionPane.INFORMATION_MESSAGE );
            }
            if( meanCountContainsZero ) {
                meanCountsA = calculateMeanCountsForEachReplicate( groupA );
                meanCountsB = calculateMeanCountsForEachReplicate( groupB );
            }
        } else {
            meanCountsA = calculateMeanCountsForEachReplicate( groupA );
            meanCountsB = calculateMeanCountsForEachReplicate( groupB );
        }
        final double averageMeanCounts = calculateTotalMeanCount( meanCountsA, meanCountsB );
        final double[] normalizationRatiosA = calculateNormalizationRatios( meanCountsA, averageMeanCounts );
        final double[] normalizationRatiosB = calculateNormalizationRatios( meanCountsB, averageMeanCounts );

        ExpressTest.MeanVarianceGroup meanVarGroupA = computeMeanAndVar( groupA, normalizationRatiosA );
        ExpressTest.MeanVarianceGroup meanVarGroupB = computeMeanAndVar( groupB, normalizationRatiosB );
        final double[] varA = meanVarGroupA.getVar();
        final double[] varB = meanVarGroupB.getVar();
        final double[] meanA = meanVarGroupA.getMean();
        final double[] meanB = meanVarGroupB.getMean();

        final double[] varANormalized = meanVarGroupA.getVarNormalized();
        final double[] varBNormalized = meanVarGroupB.getVarNormalized();
        final double[] meanANormalized = meanVarGroupA.getMeanNormalized();
        final double[] meanBNormalized = meanVarGroupB.getMeanNormalized();

        final List<Object> regionNamesList = new ArrayList<>( regionLength );

        results = new ArrayList<>( regionLength );
        resultsNormalized = new ArrayList<>( regionLength );

        for( int i = 0; i < regionLength; i++ ) {
            //Filter out regions with low mean values
            if( meanA[i] > cutOff || meanB[i] > cutOff ) {
                regionNamesList.add( regionNames[i] );

                List<Object> currentResult = new ArrayList<>( 20 );
                List<Object> currentResultNormalized = new ArrayList<>( 20 );

                currentResult.add( regionNames[i] );
                currentResultNormalized.add( regionNames[i] );

                currentResult.add( start[i] );
                currentResultNormalized.add( start[i] );

                currentResult.add( stop[i] );
                currentResultNormalized.add( stop[i] );

                currentResult.add( meanA[i] );
                currentResultNormalized.add( meanANormalized[i] );

                currentResult.add( varA[i] );
                currentResultNormalized.add( varANormalized[i] );

                currentResult.add( meanB[i] );
                currentResultNormalized.add( meanBNormalized[i] );

                currentResult.add( varB[i] );
                currentResultNormalized.add( varBNormalized[i] );

                currentResult.add( computeRatio( meanA[i], meanB[i] ) );
                currentResultNormalized.add( computeRatio( meanANormalized[i], meanBNormalized[i] ) );

                currentResult.add( computeRatio( meanB[i], meanA[i] ) );
                currentResultNormalized.add( computeRatio( meanBNormalized[i], meanANormalized[i] ) );

                if( groupA.length < 2 && groupB.length < 2 ) {
                    currentResult.add( -1.0d );
                } else {
                    currentResult.add( computeConfidence( meanA[i], meanB[i], varA[i], varB[i] ) );
                }
                if( groupA.length < 2 && groupB.length < 2 ) {
                    currentResultNormalized.add( -1.0d );
                } else {
                    currentResultNormalized.add( computeConfidence( meanANormalized[i], meanBNormalized[i], varANormalized[i], varBNormalized[i] ) );
                }
                results.add( currentResult );
                resultsNormalized.add( currentResultNormalized );
            }
        }

        rowNames = regionNamesList;

        processingLog.addProperty( "Average mean counts", averageMeanCounts );
        processingLog.addProperty( "Use house keeping genes for normalization", useHousekeepingGenesForNormalization );
        if( useHousekeepingGenesForNormalization ) {
            processingLog.addProperty( "Used house keeping genes", normalizationFeatures );
        }
        processingLog.addProperty( "Normalization ratios for group A", normalizationRatiosA );
        processingLog.addProperty( "Normalization ratios for group B", normalizationRatiosB );
        notifyObservers( ExpressTestStatus.FINISHED );

    }


    private static boolean zeroFreeValues( double[] values ) {

        for( double val : values ) {
            if( val == 0d ) {
                return false;
            }
        }

        return true;

    }


    private static double[] calculateNormalizationRatios( final double[] meanCounts, final double totalMeanCount ) {

        final int l = meanCounts.length;
        final double[] ret = new double[l];
        for( int i = 0; i < l; i++ ) {
            ret[i] = totalMeanCount / meanCounts[i];
        }

        return ret;

    }


    private static double calculateTotalMeanCount( final double[] groupA, final double[] groupB ) {

        double sum = 0d;
        for( double val : groupA ) {
            sum += val;
        }
        for( double val : groupB ) {
            sum += val;
        }

        int numberOfReplicates = groupA.length + groupB.length;

        return sum / numberOfReplicates;

    }


    private static double calculateMeanCountForReplicate( final int[] replicate ) {

        double sum = 0d;
        for( int count : replicate ) {
            sum += count;
        }

        return sum / replicate.length;

    }


    private static double[] calculateMeanCountsForEachReplicate( final int[][] group ) {

        final double[] meanCounts = new double[group.length];
        for( int i = 0; i < group.length; i++ ) {
            meanCounts[i] = calculateMeanCountForReplicate( group[i] );
        }

        return meanCounts;

    }


    private ExpressTest.MeanVarianceGroup computeMeanAndVar( final int[][] group, final double[] normalizationRatios ) {

        final double[] mean = new double[group[0].length];
        final double[] var = new double[group[0].length];
        final double[] meanNormalized = new double[group[0].length];
        final double[] varNormalized = new double[group[0].length];

        for( int j = 0; j < group[0].length; j++ ) {

            double[] rowValues = new double[group.length];
            double[] rowValuesNormalized = new double[group.length];
            for( int i = 0; i < group.length; i++ ) {
                rowValues[i] = group[i][j];
                rowValuesNormalized[i] = (group[i][j] * normalizationRatios[i]);
            }

            mean[j] = mean( rowValues );
            var[j] = Math.round( variance( rowValues ) );
            meanNormalized[j] = mean( rowValuesNormalized );
            varNormalized[j] = Math.round( variance( rowValuesNormalized ) );

        }

        return new ExpressTest.MeanVarianceGroup( mean, var, meanNormalized, varNormalized );

    }


    private static double mean( final double[] values ) {

        /**
         * Deactivated as computations of List hashcodes are even more expensive
         * than actual computations of the means! For arrays, it doesn't even
         * make sense, as by default no deep hashcode computation is used but
         * the object reference. Please, have a look at the standard java
         * hashcode implementations of List/Array to double check.
         */

//        if( meanCache.containsKey( values ) ) {
//            return meanCache.get( values );
//        }

        double mean = 0d;
        for( double value : values ) {
            mean += value;
        }
        mean /= values.length;
//        meanCache.put( values, mean );

        return mean;

    }


    private double variance( final double[] values ) {

        double var = 0d;
        for( double value : values ) {
            var += Math.pow( value - mean( values ), 2 );
        }

        return var / (values.length - 1);

    }


    @Override
    public void addObserver( ExpressTestObserver o ) {
        observers.add( o );
    }


    @Override
    public void removeObserver( ExpressTestObserver o ) {
        observers.remove( o );
    }


    private void notifyObservers( ExpressTestStatus status ) {
        for( ExpressTestObserver observer : observers ) {
            observer.update( this, status );
        }
    }


    @Override
    public List<List<Object>> getResults() {
        return Collections.unmodifiableList( results );
    }


    @Override
    public List<List<Object>> getResultsNormalized() {
        return Collections.unmodifiableList( resultsNormalized );
    }


    @Override
    public List<Object> getColumnNames() {
        return Collections.unmodifiableList( colNames );
    }


    @Override
    public List<Object> getRowNames() {
        return Collections.unmodifiableList( rowNames );
    }


    private static double computeRatio( double one, double two ) {

        if( one == 0 ) {
            one = 1d;
        }

        if( two == 0 ) {
            two = 1d;
        }

        return one / two;

    }


    private static double computeConfidence( double meanA, double meanB, double varA, double varB ) {

        if( meanA == 0 ) {
            meanA = 1d;
        }

        if( meanB == 0 ) {
            meanB = 1d;
        }

        return -(Math.log10( (((varA / meanA) + (varB / meanB)) / 2) ));

    }


    @Override
    public void setNormalizationFeatures( int[] normalizationFeatures ) {
        this.normalizationFeatures = normalizationFeatures;
        useHousekeepingGenesForNormalization = true;
    }


    private static class MeanVarianceGroup {

        private final double[] mean;
        private final double[] var;
        private final double[] meanNormalized;
        private final double[] varNormalized;


        MeanVarianceGroup( double[] mean, double[] var, double[] meanNormalized, double[] varNormalized ) {
            this.mean = mean;
            this.var = var;
            this.meanNormalized = meanNormalized;
            this.varNormalized = varNormalized;
        }


        public double[] getMean() {
            return mean;
        }


        public double[] getVar() {
            return var;
        }


        public double[] getMeanNormalized() {
            return meanNormalized;
        }


        public double[] getVarNormalized() {
            return varNormalized;
        }


    }

}
