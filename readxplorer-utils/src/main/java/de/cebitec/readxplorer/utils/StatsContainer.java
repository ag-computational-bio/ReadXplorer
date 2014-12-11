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


import de.cebitec.readxplorer.utils.classification.Classification;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import de.cebitec.readxplorer.utils.classification.TotalCoverage;
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
    public static final String NO_UNIQUE_PAIRS = "Unique Read Pairs"; //TODO: can we calculate these?
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
        readLengthDistribution.setType( Properties.READ_LENGTH_DISTRIBUTION );
        readPairSizeDistribution.setType( Properties.READ_PAIR_SIZE_DISTRIBUTION );
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
     *         track or read pair track.
     */
    public Map<String, Integer> getStatsMap() {
        return statsMap;
    }


    /**
     * Set the number of positions covered by mappings of the given mapping
     * class when using the stats container during import of data. Later, the
     * coverage has to be put into the statsMap!
     * <p>
     * @param classToCoveredIntervalsMap Map of Chromosomes to mapping classes
     *                                   to the intervals covered by mappings of that classification
     */
    public void setCoveredPositionsImport( Map<String, Map<Classification, List<Pair<Integer, Integer>>>> classToCoveredIntervalsMap ) {
        for( Map<Classification, List<Pair<Integer, Integer>>> chromMap : classToCoveredIntervalsMap.values() ) {
            for( Classification mappingClass : chromMap.keySet() ) {
                List<Pair<Integer, Integer>> coveredIntervals = chromMap.get( mappingClass );
                String coverageId = mappingClass.getTypeString() + COVERAGE_STRING;
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
        for( MappingClass mappingClass : MappingClass.values() ) {
            this.statsMap.put( mappingClass.getTypeString(), 0 );
            this.statsMap.put( mappingClass.getTypeString() + StatsContainer.COVERAGE_STRING, 0 );
        }
        this.statsMap.put( TotalCoverage.TOTAL_COVERAGE.getTypeString() + StatsContainer.COVERAGE_STRING, 0 );
        this.statsMap.put( NO_MAPPINGS, 0 );
        this.statsMap.put( NO_UNIQUE_SEQS, 0 );
        this.statsMap.put( NO_REPEATED_SEQ, 0 );
        this.statsMap.put( NO_UNIQ_MAPPINGS, 0 );
        this.statsMap.put( NO_READS, 0 );
        this.statsMap.put( AVERAGE_READ_LENGTH, 0 );
    }


    /**
     * Fills the stats map with all available entries for a read pair track.
     */
    public void prepareForReadPairTrack() {
        this.statsMap.put( NO_READ_PAIRS, 0 );
        this.statsMap.put( NO_PERF_PAIRS, 0 );
        this.statsMap.put( NO_ORIENT_WRONG_PAIRS, 0 );
        this.statsMap.put( NO_SMALL_DIST_PAIRS, 0 );
        this.statsMap.put( NO_SMALL_ORIENT_WRONG_PAIRS, 0 );
        this.statsMap.put( NO_LARGE_DIST_PAIRS, 0 );
        this.statsMap.put( NO_LARGE_ORIENT_WRONG_PAIRS, 0 );
        this.statsMap.put( NO_SINGLE_MAPPIGNS, 0 );
        this.statsMap.put( NO_UNIQUE_PAIRS, 0 );
        this.statsMap.put( NO_UNIQ_PERF_PAIRS, 0 );
        this.statsMap.put( NO_UNIQ_SMALL_PAIRS, 0 );
        this.statsMap.put( NO_UNIQ_LARGE_PAIRS, 0 );
        this.statsMap.put( NO_UNIQ_ORIENT_WRONG_PAIRS, 0 );
        this.statsMap.put( NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS, 0 );
        this.statsMap.put( NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS, 0 );
        this.statsMap.put( AVERAGE_READ_PAIR_SIZE, 0 );
    }


    /**
     * Increases the value of the given key by the given value.
     * <p>
     * @param key           the key to increase
     * @param increaseValue the value to add to the old value of the key
     */
    public void increaseValue( String key, int increaseValue ) {
        this.statsMap.put( key, this.statsMap.get( key ) + increaseValue );
    }


    /**
     * Increases the read pair stats for the given read pair type by
     * the given value.
     * <p>
     * @param type  the read pair type of the stats to increase
     * @param value the value to add to the corresponding stats
     */
    public void incReadPairStats( ReadPairType type, int value ) {

        if( type == ReadPairType.PERFECT_PAIR || type == ReadPairType.PERFECT_UNQ_PAIR ) {
            this.increaseValue( NO_READ_PAIRS, value );
            this.increaseValue( NO_PERF_PAIRS, value );
            if( type == ReadPairType.PERFECT_UNQ_PAIR ) {
                this.increaseValue( NO_UNIQUE_PAIRS, value );
                this.increaseValue( NO_UNIQ_PERF_PAIRS, value );
            }
        }
        else if( type == ReadPairType.DIST_SMALL_PAIR || type == ReadPairType.DIST_SMALL_UNQ_PAIR ) {
            this.increaseValue( NO_READ_PAIRS, value );
            this.increaseValue( NO_SMALL_DIST_PAIRS, value );
            if( type == ReadPairType.DIST_SMALL_UNQ_PAIR ) {
                this.increaseValue( NO_UNIQUE_PAIRS, value );
                this.increaseValue( NO_UNIQ_SMALL_PAIRS, value );
            }
        }
        else if( type == ReadPairType.DIST_LARGE_PAIR || type == ReadPairType.DIST_LARGE_UNQ_PAIR ) {
            this.increaseValue( NO_READ_PAIRS, value );
            this.increaseValue( NO_LARGE_DIST_PAIRS, value );
            if( type == ReadPairType.DIST_LARGE_UNQ_PAIR ) {
                this.increaseValue( NO_UNIQUE_PAIRS, value );
                this.increaseValue( NO_UNIQ_LARGE_PAIRS, value );
            }
        }
        else if( type == ReadPairType.ORIENT_WRONG_PAIR || type == ReadPairType.ORIENT_WRONG_UNQ_PAIR ) {
            this.increaseValue( NO_READ_PAIRS, value );
            this.increaseValue( NO_ORIENT_WRONG_PAIRS, value );
            if( type == ReadPairType.ORIENT_WRONG_UNQ_PAIR ) {
                this.increaseValue( NO_UNIQUE_PAIRS, value );
                this.increaseValue( NO_UNIQ_ORIENT_WRONG_PAIRS, value );
            }
        }
        else if( type == ReadPairType.OR_DIST_SMALL_PAIR || type == ReadPairType.OR_DIST_SMALL_UNQ_PAIR ) {
            this.increaseValue( NO_READ_PAIRS, value );
            this.increaseValue( NO_SMALL_ORIENT_WRONG_PAIRS, value );
            if( type == ReadPairType.OR_DIST_SMALL_PAIR ) {
                this.increaseValue( NO_UNIQUE_PAIRS, value );
                this.increaseValue( NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS, value );
            }
        }
        else if( type == ReadPairType.OR_DIST_LARGE_PAIR || type == ReadPairType.OR_DIST_LARGE_UNQ_PAIR ) {
            this.increaseValue( NO_READ_PAIRS, value );
            this.increaseValue( NO_LARGE_ORIENT_WRONG_PAIRS, value );
            if( type == ReadPairType.OR_DIST_LARGE_UNQ_PAIR ) {
                this.increaseValue( NO_UNIQUE_PAIRS, value );
                this.increaseValue( NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS, value );
            }
        }
        else { //if (type == Properties.TYPE_UNPAIRED_PAIR) {
            this.increaseValue( NO_SINGLE_MAPPIGNS, value );
        }
    }


}
