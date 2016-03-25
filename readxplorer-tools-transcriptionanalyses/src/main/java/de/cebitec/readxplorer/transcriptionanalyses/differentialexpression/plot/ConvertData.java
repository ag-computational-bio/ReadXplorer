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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.ResultDeAnalysis;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool.BaySeq;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool.DeSeq;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool.ExpressTest;


/**
 *
 * @author kstaderm
 */
public final class ConvertData {

    private static final Logger LOG = LoggerFactory.getLogger( ConvertData.class.getName() );

    private static final int BAY_SEQ_OFFSET = 3;
    private static final int CUT_OFF = 30;


    /**
     * Instantiation not allowed.
     */
    private ConvertData() {
    }


    public static Map<PersistentFeature, Pair<Double, Double>> ratioABagainstConfidence( ResultDeAnalysis result ) {
        return createDataPairForFeature( result.getTableContents(), 0, 7, 9 );
    }


    public static Map<PersistentFeature, Pair<Double, Double>> ratioBAagainstConfidence( ResultDeAnalysis result ) {
        return createDataPairForFeature( result.getTableContents(), 0, 8, 9 );
    }


    public static Map<PersistentFeature, Pair<Double, Double>> createMAvalues( ResultDeAnalysis result, DeAnalysisHandler.Tool usedTool, Integer[] sampleA, Integer[] sampleB ) {
        Map<PersistentFeature, Pair<Double, Double>> input = new HashMap<>();
        switch( usedTool ) {
            case BaySeq:
                input = convertBaySeqResults( result.getTableContents(), sampleA, sampleB );
                break;
            case DeSeq:
                input = createDataPairForFeature( result.getTableContents(), 0, 2, 3 );
                break;
            case ExpressTest:
                input = createDataPairForFeature( result.getTableContents(), 0, 3, 5 );
                break;
            default:
                LOG.error( "Encountered unknown DGE tool value." );
        }
        Map<PersistentFeature, Pair<Double, Double>> ret = new HashMap<>();
        for( PersistentFeature key : input.keySet() ) {
            Pair<Double, Double> pair = input.get( key );
            Double r = pair.getFirst();
            Double g = pair.getSecond();
            if( (r > CUT_OFF) || (g > CUT_OFF) ) {

                Double m = (Math.log( r ) / Math.log( 2 )) - (Math.log( g ) / Math.log( 2 ));
                Double a;
                if( r == 0 ) {
                    a = (Math.log( g ) / Math.log( 2 ));
                } else if( g == 0 ) {
                    a = (Math.log( r ) / Math.log( 2 ));
                } else {
                    a = ((Math.log( r ) / Math.log( 2 )) + (Math.log( g ) / Math.log( 2 ))) / 2;
                }
                //Values have to be added in other order than one would think, because
                //the A value is shown on the X-Axis and the M value on the Y-Axis. So at
                //this point the values are in correct order for plotting, meaning that
                //the value corresponding to the X-Axis is the first and the one corresponding
                //to the Y-Axis is the second one.
                ret.put( key, new Pair<>( a, m ) );
            }
        }
        return ret;
    }


    private static Map<PersistentFeature, Pair<Double, Double>> convertBaySeqResults( List<List<Object>> resultTable, Integer[] sampleA, Integer[] sampleB ) {
        Map<PersistentFeature, Pair<Double, Double>> ret = new HashMap<>();
        for( List<Object> row : resultTable ) {
            PersistentFeature key = (PersistentFeature) row.get( 0 );
            double x = 0d;
            for( int idx : sampleA ) {
                int index = idx + BAY_SEQ_OFFSET;
                x += GeneralUtils.convertNumber( Double.class, (Number) row.get( index ) );
            }
            x /= sampleA.length;
            double y = 0d;
            for( int idx : sampleB ) {
                int index = idx + BAY_SEQ_OFFSET;
                y += GeneralUtils.convertNumber( Double.class, (Number) row.get( index ) );
            }
            y /= sampleB.length;

            Pair<Double, Double> values = new Pair<>( x, y );
            ret.put( key, values );

        }

        return ret;

    }


    /**
     * Creates a map of the PersistentFeatures in the resultTable to a pair of
     * Double values. E.g. this method can be used to get a data point
     * associated to the genomic features.
     * <p>
     * @param resultTable   the table to iterate through
     * @param columnFeature the column, in which the PersistentFeatures are
     *                      stored
     * @param column1       the column of the x value of the data point
     *                      associated with a PersistentFeature
     * @param column2       the column of the y value of the data point
     *                      associated with a PersistentFeature
     * <p>
     * @return
     */
    private static Map<PersistentFeature, Pair<Double, Double>> createDataPairForFeature( List<List<Object>> resultTable, int columnFeature, int column1, int column2 ) {
        Map<PersistentFeature, Pair<Double, Double>> ret = new HashMap<>();
        for( List<Object> row : resultTable ) {
            PersistentFeature key = (PersistentFeature) row.get( columnFeature );
            Double x = (Double) row.get( column1 );
            Double y = (Double) row.get( column2 );
            ret.put( key, new Pair<>( x, y ) );
        }
        return ret;
    }


}
