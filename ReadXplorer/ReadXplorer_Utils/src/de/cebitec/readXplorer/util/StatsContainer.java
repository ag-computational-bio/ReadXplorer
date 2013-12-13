package de.cebitec.readXplorer.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Statistics container for a track and sequence pair track.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class StatsContainer {
    
    public static final String NO_UNIQUE_SEQS = "Unique Sequences";
    public static final String NO_REPEATED_SEQ = "Repeated Sequences";
    public static final String NO_UNIQ_MAPPINGS = "Unique Mappings";
    public static final String NO_UNIQ_BM_MAPPINGS = "Unique best Match Mappings";
    public static final String NO_UNIQ_PERF_MAPPINGS = "Unique Perfect Mappings";
    public static final String NO_PERFECT_MAPPINGS = "Perfect Mappings";
    public static final String NO_BESTMATCH_MAPPINGS = "Best Match Mappings";
    public static final String NO_COMMON_MAPPINGS = "Common Mappings";
    public static final String NO_READS = "Reads";
    
    public static final String NO_SEQ_PAIRS = "Seq. Pairs";
    public static final String NO_PERF_PAIRS = "Perfect Seq. Pairs";
    public static final String NO_ORIENT_WRONG_PAIRS = "Wrong Oriented Pairs";
    public static final String NO_SMALL_DIST_PAIRS = "Smaller Pairs";
    public static final String NO_LARGE_DIST_PAIRS = "Larger Pairs";
    public static final String NO_LARGE_ORIENT_WRONG_PAIRS = "Wrong Orient. Larger Pairs";
    public static final String NO_SMALL_ORIENT_WRONG_PAIRS = "Wrong Orient. Smaller Pairs";
    public static final String NO_SINGLE_MAPPIGNS = "Single Mappings";
    public static final String NO_UNIQUE_PAIRS = "Unique Seq. Pairs"; //TODO: can we calculate these?
    public static final String NO_UNIQ_PERF_PAIRS = "Unique Perfect Seq. Pairs";
    public static final String NO_UNIQ_SMALL_PAIRS = "Unique Small Pairs";
    public static final String NO_UNIQ_LARGE_PAIRS = "Unique Larger Pairs";
    public static final String NO_UNIQ_ORIENT_WRONG_PAIRS = "Unique Orient Wrong Pairs";
    public static final String NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS = "Unique Wrong Orient. Smaller Pairs";
    public static final String NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS = "Unique Wrong Orient. Larger Pairs";
    public static final String AVERAGE_READ_LENGTH = "Average Read Length";
    public static final String AVERAGE_READ_PAIR_SIZE = "Average Seq. Pair Size";
    public static final String COVERAGE_BM_GENOME = "Best Match Coverage";
    public static final String COVERAGE_COMPLETE_GENOME = "Complete Coverage";
    public static final String COVERAGE_PERFECT_GENOME = "Perfect Coverage";
    
    private Map<String, Integer> statsMap;
    private DiscreteCountingDistribution readLengthDistribution;
    private DiscreteCountingDistribution readPairSizeDistribution;
    
    /**
     * Statistics container for a track and sequence pair track.
     */
    public StatsContainer() {
        statsMap = new HashMap<>();
        readLengthDistribution = new DiscreteCountingDistribution();
        readPairSizeDistribution = new DiscreteCountingDistribution();
        readLengthDistribution.setType(Properties.READ_LENGTH_DISTRIBUTION);
        readPairSizeDistribution.setType(Properties.READ_PAIR_SIZE_DISTRIBUTION);
    }

    /**
     * Sets the read length distribution for the data set.
     * @param readLengthDistribution  The read length distribution to set
     */
    public void setReadLengthDistribution(DiscreteCountingDistribution readLengthDistribution) {
        this.readLengthDistribution = readLengthDistribution;
    }

    /**
     * @return The read length distribution of the data set.
     */
    public DiscreteCountingDistribution getReadLengthDistribution() {
        return readLengthDistribution;
    }

    /**
     * Sets the sequence pair distance distribution for the data set.
     * @param readPairSizeDistribution The sequence pair distribution to set
     */
    public void setReadPairDistribution(DiscreteCountingDistribution readPairSizeDistribution) {
        this.readPairSizeDistribution = readPairSizeDistribution;
    }

    /**
     * @return The sequence pair size distribution of the data set.
     */
    public DiscreteCountingDistribution getReadPairSizeDistribution() {
        return readPairSizeDistribution;
    }

    /**
     * @return The map containing all initialized statistics of the associated
     * track or sequence pair track.
     */
    public Map<String, Integer> getStatsMap() {
        return statsMap;
    }

    /**
     * Fills the stats map with all available entries for a track.
     */
    public void prepareForTrack() {
        statsMap.put(NO_UNIQUE_SEQS, 0);
        statsMap.put(NO_REPEATED_SEQ, 0);
        statsMap.put(NO_UNIQ_MAPPINGS, 0);
        statsMap.put(NO_UNIQ_BM_MAPPINGS, 0);
        statsMap.put(NO_UNIQ_PERF_MAPPINGS, 0);
        statsMap.put(NO_PERFECT_MAPPINGS, 0);
        statsMap.put(NO_BESTMATCH_MAPPINGS, 0);
        statsMap.put(NO_COMMON_MAPPINGS, 0);
        statsMap.put(NO_READS, 0);
        statsMap.put(AVERAGE_READ_LENGTH, 0);
        statsMap.put(COVERAGE_COMPLETE_GENOME, 0);
        statsMap.put(COVERAGE_BM_GENOME, 0);
        statsMap.put(COVERAGE_PERFECT_GENOME, 0);
    }
    
    /**
     * Fills the stats map with all available entries for a sequence pair track.
     */
    public void prepareForReadPairTrack() {
        statsMap.put(NO_SEQ_PAIRS, 0);
        statsMap.put(NO_PERF_PAIRS, 0);
        statsMap.put(NO_ORIENT_WRONG_PAIRS, 0);
        statsMap.put(NO_SMALL_DIST_PAIRS, 0);
        statsMap.put(NO_SMALL_ORIENT_WRONG_PAIRS, 0);
        statsMap.put(NO_LARGE_DIST_PAIRS, 0);
        statsMap.put(NO_LARGE_ORIENT_WRONG_PAIRS, 0);
        statsMap.put(NO_SINGLE_MAPPIGNS, 0);
        statsMap.put(NO_UNIQUE_PAIRS, 0);
        statsMap.put(NO_UNIQ_PERF_PAIRS, 0);
        statsMap.put(NO_UNIQ_SMALL_PAIRS, 0);
        statsMap.put(NO_UNIQ_LARGE_PAIRS, 0);
        statsMap.put(NO_UNIQ_ORIENT_WRONG_PAIRS, 0);
        statsMap.put(NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS, 0);
        statsMap.put(NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS, 0);
        statsMap.put(AVERAGE_READ_PAIR_SIZE, 0);
    }

    /**
     * Increases the value of the given key by the given value.
     * @param key the key to increase
     * @param increaseValue the value to add to the old value of the key
     */
    public void increaseValue(String key, int increaseValue) {
        statsMap.put(key, statsMap.get(key) + increaseValue);
    }
    
    /**
     * Increases the sequence pair stats for the given sequence pair type by
     * the given value.
     * @param type the sequence pair type of the stats to increase
     * @param value the value to add to the corresponding stats
     */
    public void incReadPairStats(ReadPairType type, int value) {
        
        if (type == ReadPairType.PERFECT_PAIR || type == ReadPairType.PERFECT_UNQ_PAIR) {
            this.increaseValue(NO_SEQ_PAIRS, value);
            this.increaseValue(NO_PERF_PAIRS, value);
            if (type == ReadPairType.PERFECT_UNQ_PAIR) {
                this.increaseValue(NO_UNIQUE_PAIRS, value);
                this.increaseValue(NO_UNIQ_PERF_PAIRS, value);
            }
        } else if (type == ReadPairType.DIST_SMALL_PAIR || type == ReadPairType.DIST_SMALL_UNQ_PAIR) {
            this.increaseValue(NO_SEQ_PAIRS, value);
            this.increaseValue(NO_SMALL_DIST_PAIRS, value);
            if (type == ReadPairType.DIST_SMALL_UNQ_PAIR) {
                this.increaseValue(NO_UNIQUE_PAIRS, value);
                this.increaseValue(NO_UNIQ_SMALL_PAIRS, value);
            }
        } else if (type == ReadPairType.DIST_LARGE_PAIR || type == ReadPairType.DIST_LARGE_UNQ_PAIR) {
            this.increaseValue(NO_SEQ_PAIRS, value);
            this.increaseValue(NO_LARGE_DIST_PAIRS, value);
            if (type == ReadPairType.DIST_LARGE_UNQ_PAIR) {
                this.increaseValue(NO_UNIQUE_PAIRS, value);
                this.increaseValue(NO_UNIQ_LARGE_PAIRS, value);
            }
        } else if (type == ReadPairType.ORIENT_WRONG_PAIR || type == ReadPairType.ORIENT_WRONG_UNQ_PAIR) {
            this.increaseValue(NO_SEQ_PAIRS, value);
            this.increaseValue(NO_ORIENT_WRONG_PAIRS, value);
            if (type == ReadPairType.ORIENT_WRONG_UNQ_PAIR) {
                this.increaseValue(NO_UNIQUE_PAIRS, value);
                this.increaseValue(NO_UNIQ_ORIENT_WRONG_PAIRS, value);
            }
        } else if (type == ReadPairType.OR_DIST_SMALL_PAIR || type == ReadPairType.OR_DIST_SMALL_UNQ_PAIR) {
            this.increaseValue(NO_SEQ_PAIRS, value);
            this.increaseValue(NO_SMALL_ORIENT_WRONG_PAIRS, value);
            if (type == ReadPairType.OR_DIST_SMALL_PAIR) {
                this.increaseValue(NO_UNIQUE_PAIRS, value);
                this.increaseValue(NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS, value);
            }
        } else if (type == ReadPairType.OR_DIST_LARGE_PAIR || type == ReadPairType.OR_DIST_LARGE_UNQ_PAIR) {
            this.increaseValue(NO_SEQ_PAIRS, value);
            this.increaseValue(NO_LARGE_ORIENT_WRONG_PAIRS, value);
            if (type == ReadPairType.OR_DIST_LARGE_UNQ_PAIR) {
                this.increaseValue(NO_UNIQUE_PAIRS, value);
                this.increaseValue(NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS, value);
            }
        } else { //if (type == Properties.TYPE_UNPAIRED_PAIR) {
            this.increaseValue(NO_SINGLE_MAPPIGNS, value);
        }
    }
    
}
