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
import de.cebitec.readxplorer.api.Classification;
import de.cebitec.readxplorer.api.enums.MappingClass;
import de.cebitec.readxplorer.api.enums.TotalCoverage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Statistics container for a track and read pair track.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class StatsContainer {

    /**
     * String to append to queries for covered bases of a mapping class.
     */
    public static final String COVERAGE_STRING = " Coverage";

    //Mapping classes not present here, they are set by MappingClass.classtype!
    //General
    public static final String NO_UNIQUE_SEQS = "Unique Sequences";
    public static final String NO_REPEATED_SEQ = "Repeated Sequences";
    public static final String NO_UNIQ_MAPPINGS = "Unique Mappings";
    public static final String NO_MAPPINGS = "Mappings";
    public static final String NO_READS = "Reads";
    public static final String AVERAGE_READ_LENGTH = "Average Read Length";

    //Read pairs
    public static final String NO_READ_PAIRS = "Read Pairs";
    public static final String NO_PERF_PAIRS = "Perfect Read Pairs";
    public static final String NO_ORIENT_WRONG_PAIRS = "Wrong Oriented Pairs";
    public static final String NO_SMALL_DIST_PAIRS = "Smaller Pairs";
    public static final String NO_LARGE_DIST_PAIRS = "Larger Pairs";
    public static final String NO_LARGE_ORIENT_WRONG_PAIRS = "Wrong Orient. Larger Pairs";
    public static final String NO_SMALL_ORIENT_WRONG_PAIRS = "Wrong Orient. Smaller Pairs";
    public static final String NO_SINGLE_MAPPIGNS = "Single Mappings";
    public static final String NO_UNIQUE_PAIRS = "Unique Read Pairs"; //TODO can we calculate these?
    public static final String NO_UNIQ_PERF_PAIRS = "Unique Perfect Read Pairs";
    public static final String NO_UNIQ_SMALL_PAIRS = "Unique Small Pairs";
    public static final String NO_UNIQ_LARGE_PAIRS = "Unique Larger Pairs";
    public static final String NO_UNIQ_ORIENT_WRONG_PAIRS = "Unique Orient Wrong Pairs";
    public static final String NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS = "Unique Wrong Orient. Smaller Pairs";
    public static final String NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS = "Unique Wrong Orient. Larger Pairs";
    public static final String AVERAGE_READ_PAIR_SIZE = "Average Read Pair Size";

    private final Map<String, Integer> statsMap;
    private DiscreteCountingDistribution readLengthDistribution;
    private DiscreteCountingDistribution readPairSizeDistribution;


    /**
     * Statistics container for a track and read pair track.
     */
    public StatsContainer() {
        statsMap = new HashMap<>();
        readLengthDistribution = new DiscreteCountingDistribution();
        readPairSizeDistribution = new DiscreteCountingDistribution();
        readLengthDistribution.setType( Distribution.ReadLengthDistribution );
        readPairSizeDistribution.setType( Distribution.ReadPairSizeDistribution );
    }


    /**
     * Sets the read length distribution for the data set.
     * <p>
     * @param readLengthDistribution The read length distribution to set
     */
    public void setReadLengthDistribution( DiscreteCountingDistribution readLengthDistribution ) {
        this.readLengthDistribution = readLengthDistribution;
    }


    /**
     * @return The read length distribution of the data set.
     */
    public DiscreteCountingDistribution getReadLengthDistribution() {
        return readLengthDistribution;
    }


    /**
     * Sets the read pair distance distribution for the data set.
     * <p>
     * @param readPairSizeDistribution The read pair distribution to set
     */
    public void setReadPairDistribution( DiscreteCountingDistribution readPairSizeDistribution ) {
        this.readPairSizeDistribution = readPairSizeDistribution;
    }


    /**
     * @return The read pair size distribution of the data set.
     */
    public DiscreteCountingDistribution getReadPairSizeDistribution() {
        return readPairSizeDistribution;
    }


    /**
     * @return The map containing all initialized statistics of the associated
     * track or read pair track.
     */
    public Map<String, Integer> getStatsMap() {
        return Collections.unmodifiableMap( statsMap );
    }


    /**
     * Add a new key-value pair to the statistics map of this StatsContainer. It
     * does not check if the key already existed, so the value to an existing
     * key is overwritten.
     *
     * @param key The key to add
     * @param value The value to add
     */
    public void addStatsValue( String key, int value ) {
        statsMap.put( key, value );
    }


    /**
     * Set the number of positions covered by mappings of the given mapping
     * class when using the stats container during import of data. Later, the
     * coverage has to be put into the statsMap!
     * <p>
     * @param classToCoveredIntervalsMap Map of Chromosomes to mapping classes
     * to the intervals covered by mappings of that classification
     */
    public void setCoveredPositionsImport( Map<String, Map<Classification, List<Pair<Integer, Integer>>>> classToCoveredIntervalsMap ) {
        for( Map<Classification, List<Pair<Integer, Integer>>> chromMap : classToCoveredIntervalsMap.values() ) {
            for( Classification mappingClass : chromMap.keySet() ) {
                List<Pair<Integer, Integer>> coveredIntervals = chromMap.get( mappingClass );
                String coverageId = mappingClass.getString() + COVERAGE_STRING;
                statsMap.put( coverageId, statsMap.get( coverageId ) + this.calcCoveredBases( coveredIntervals ) );
            }
        }
    }


    /**
     * Counts all bases, which are covered by mappings in this data set.
     * <p>
     * @param coveredIntervals the covered intervals of the data set
     * <p>
     * @return the number of bases covered in the data set
     */
    private int calcCoveredBases( List<Pair<Integer, Integer>> coveredIntervals ) {
        int coveredBases = 0;
        for( Pair<Integer, Integer> interval : coveredIntervals ) {
            coveredBases += interval.getSecond() - interval.getFirst();
        }
        return coveredBases;
    }


    /**
     * Fills the stats map with all available entries for a track.
     */
    public void prepareForTrack() {
        prepareForStats( getListOfTrackStatistics() );
    }


    /**
     * Fills the stats map with all available entries for a read pair track.
     */
    public void prepareForReadPairTrack() {
        prepareForStats( getListOfReadPairStatistics() );
    }


    /**
     * Prepares the container for a predefined list of statistics identifiers.
     *
     * @param statsIdList The list of statistics identifiers to store in this
     * container
     */
    private void prepareForStats( List<String> statsIdList ) {
        for( String statsId : statsIdList ) {
            statsMap.put( statsId, 0 );
        }
    }


    /**
     * Increases the value of the given key by the given value.
     * <p>
     * @param key the key to increase
     * @param increaseValue the value to add to the old value of the key
     */
    public void increaseValue( String key, int increaseValue ) {
        statsMap.put( key, statsMap.get( key ) + increaseValue );
    }


    /**
     * Increases the read pair stats for the given read pair type by the given
     * value.
     * <p>
     * @param type the read pair type of the stats to increase
     * @param value the value to add to the corresponding stats
     */
    public void incReadPairStats( final ReadPairType type, final int value ) {

        if( type == ReadPairType.PERFECT_PAIR || type == ReadPairType.PERFECT_UNQ_PAIR ) {
            increaseValue( NO_READ_PAIRS, value );
            increaseValue( NO_PERF_PAIRS, value );
            if( type == ReadPairType.PERFECT_UNQ_PAIR ) {
                increaseValue( NO_UNIQUE_PAIRS, value );
                increaseValue( NO_UNIQ_PERF_PAIRS, value );
            }
        } else if( type == ReadPairType.DIST_SMALL_PAIR || type == ReadPairType.DIST_SMALL_UNQ_PAIR ) {
            increaseValue( NO_READ_PAIRS, value );
            increaseValue( NO_SMALL_DIST_PAIRS, value );
            if( type == ReadPairType.DIST_SMALL_UNQ_PAIR ) {
                increaseValue( NO_UNIQUE_PAIRS, value );
                increaseValue( NO_UNIQ_SMALL_PAIRS, value );
            }
        } else if( type == ReadPairType.DIST_LARGE_PAIR || type == ReadPairType.DIST_LARGE_UNQ_PAIR ) {
            increaseValue( NO_READ_PAIRS, value );
            increaseValue( NO_LARGE_DIST_PAIRS, value );
            if( type == ReadPairType.DIST_LARGE_UNQ_PAIR ) {
                increaseValue( NO_UNIQUE_PAIRS, value );
                increaseValue( NO_UNIQ_LARGE_PAIRS, value );
            }
        } else if( type == ReadPairType.ORIENT_WRONG_PAIR || type == ReadPairType.ORIENT_WRONG_UNQ_PAIR ) {
            increaseValue( NO_READ_PAIRS, value );
            increaseValue( NO_ORIENT_WRONG_PAIRS, value );
            if( type == ReadPairType.ORIENT_WRONG_UNQ_PAIR ) {
                increaseValue( NO_UNIQUE_PAIRS, value );
                increaseValue( NO_UNIQ_ORIENT_WRONG_PAIRS, value );
            }
        } else if( type == ReadPairType.OR_DIST_SMALL_PAIR || type == ReadPairType.OR_DIST_SMALL_UNQ_PAIR ) {
            increaseValue( NO_READ_PAIRS, value );
            increaseValue( NO_SMALL_ORIENT_WRONG_PAIRS, value );
            if( type == ReadPairType.OR_DIST_SMALL_PAIR ) {
                increaseValue( NO_UNIQUE_PAIRS, value );
                increaseValue( NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS, value );
            }
        } else if( type == ReadPairType.OR_DIST_LARGE_PAIR || type == ReadPairType.OR_DIST_LARGE_UNQ_PAIR ) {
            increaseValue( NO_READ_PAIRS, value );
            increaseValue( NO_LARGE_ORIENT_WRONG_PAIRS, value );
            if( type == ReadPairType.OR_DIST_LARGE_UNQ_PAIR ) {
                increaseValue( NO_UNIQUE_PAIRS, value );
                increaseValue( NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS, value );
            }
        } else { //if (type == Properties.TYPE_UNPAIRED_PAIR) {
            increaseValue( NO_SINGLE_MAPPIGNS, value );
        }
    }


    /**
     * @return The list of track statistics identifiers stored in this
     * container.
     */
    public static List<String> getListOfTrackStatistics() {
        List<String> statsList = new ArrayList<>();

        for( MappingClass mappingClass : MappingClass.values() ) {
            statsList.add( mappingClass.getString() );
            statsList.add( mappingClass.getString() + StatsContainer.COVERAGE_STRING );
        }
        statsList.add( TotalCoverage.TOTAL_COVERAGE.getString() + StatsContainer.COVERAGE_STRING );
        statsList.add( NO_MAPPINGS );
        statsList.add( NO_UNIQUE_SEQS );
        statsList.add( NO_REPEATED_SEQ );
        statsList.add( NO_UNIQ_MAPPINGS );
        statsList.add( NO_READS );
        statsList.add( AVERAGE_READ_LENGTH );

        return statsList;
    }


    /**
     * @return The list of additional read pair track statistics identifiers
     * stored in this container.
     */
    public static List<String> getListOfReadPairStatistics() {
        List<String> statsList = new ArrayList<>();

        statsList.add( NO_READ_PAIRS );
        statsList.add( NO_PERF_PAIRS );
        statsList.add( NO_ORIENT_WRONG_PAIRS );
        statsList.add( NO_SMALL_DIST_PAIRS );
        statsList.add( NO_SMALL_ORIENT_WRONG_PAIRS );
        statsList.add( NO_LARGE_DIST_PAIRS );
        statsList.add( NO_LARGE_ORIENT_WRONG_PAIRS );
        statsList.add( NO_SINGLE_MAPPIGNS );
        statsList.add( NO_UNIQUE_PAIRS );
        statsList.add( NO_UNIQ_PERF_PAIRS );
        statsList.add( NO_UNIQ_SMALL_PAIRS );
        statsList.add( NO_UNIQ_LARGE_PAIRS );
        statsList.add( NO_UNIQ_ORIENT_WRONG_PAIRS );
        statsList.add( NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS );
        statsList.add( NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS );
        statsList.add( AVERAGE_READ_PAIR_SIZE );

        return statsList;
    }


}
