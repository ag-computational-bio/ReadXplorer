package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.util.ReadPairType;

/**
 * Contains all general SQL statements for communicating with the ReadXplorer DB.
 *
 * @author ddoppmeier, rhilker
 */
public class SQLStatements {

    /**
     * For retrieving "NUM" result from result set of a db request.
     */
    public static final String GET_NUM = "NUM";
    
    public static String DELETE_OBJECTFROMCACHE = "DELETE FROM "
    + FieldNames.TABLE_OBJECTCACHE + " WHERE "
    + FieldNames.OBJECTCACHE_FAMILY + " = ? AND " + FieldNames.OBJECTCACHE_KEY + " = ? ";
    
    public static String DELETE_OBJECTFAMILYFROMCACHE = "DELETE FROM "
    + FieldNames.TABLE_OBJECTCACHE + " WHERE "
    + FieldNames.OBJECTCACHE_FAMILY+ " = ? ";

    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private SQLStatements() {
    }
    
    
    //////////////////  statements for table creation  /////////////////////////
    
//    public static final String SETUP_PROJECT_FOLDER =
//            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_PROJECT_FOLDER
//            + " ("
//            + FieldNames.PROJECT_FOLDER_PATH + " VARCHAR(400) NOT NULL "
//            + ") ";
    
    public final static String SETUP_STATISTICS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_STATISTICS + " "
            + "( "
            + FieldNames.STATISTICS_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.STATISTICS_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_PERFECT_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_BM_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_BM_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_PERFECT_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ + " BIGINT UNSIGNED  NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_REPEATED_SEQ + " BIGINT UNSIGNED  NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_READS + " BIGINT UNSIGNED NOT NULL,  "
            + FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_SMALL_DIST_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_UNIQ_SMALL_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_LARGE_DIST_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_UNIQ_LARGE_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_ORIENT_WRONG_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_NUM_SINGLE_MAPPINGS + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH + " BIGINT UNSIGNED, "
            + FieldNames.STATISTICS_AVERAGE_READ_LENGTH + " INT UNSIGNED "
            + ") ";
    
    /**
     *
     */
    public final static String SETUP_OBJECTCACHE = 
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_OBJECTCACHE
            + " ( "
            + FieldNames.OBJECTCACHE_FAMILY + " VARCHAR(255), "
            + FieldNames.OBJECTCACHE_KEY + " VARCHAR(255), "
            + FieldNames.OBJECTCACHE_DATA + " BLOB "
            + ") ";
    
    public static final String SETUP_DB_VERSION_TABLE = 
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_DB_VERSION 
                + " ( "
                + FieldNames.DB_VERSION_DB_VERSION_NO + " INT UNSIGNED "
                + " ) ";
    
    
   /**
    * Only needed as long as older databases are floating around and did not
    * already drop this table which was replaced by STATISTICS.
    */
    public static final String DROP_TABLE_STATICS = "DROP TABLE IF EXISTS STATICS";
    
    /**
     * Only needed as long as older databases are floating around and did not
     * already drop this table which is not necessary anymore.
     */
    public static String DROP_TABLE_SUBFEATURES = "DROP TABLE IF EXISTS SUBFEATURES";

    /**
     * Only needed as long as older databases are floating around and did not
     * already drop this table which is not necessary anymore.
     */
    public static String DROP_TABLE_COVERAGE_DISTRIBUTION = "DROP TABLE IF EXISTS COVERAGE_DISTRIBUTION";

    /** 
     * Only needed as long as older databases are floating around and did not
     * already drop this index which is not necessary anymore. 
     */
    public static String DROP_INDEX_INDEXPOS = "DROP INDEX IF EXISTS INDEXPOS";
         
         
    //////////////////  statements for data insertion  ////////////////////////
    
//    public static final String INSERT_PROJECT_FOLDER = 
//            "INSERT INTO " + FieldNames.TABLE_PROJECT_FOLDER
//            + "("
//            + FieldNames.PROJECT_FOLDER_PATH
//            + ") "
//            + " VALUES (?); ";
    
    
    static String INSERT_OBJECTINTOCACHE = "INSERT INTO " + FieldNames.TABLE_OBJECTCACHE + " "
            + "("
            + FieldNames.OBJECTCACHE_FAMILY + ", "
            + FieldNames.OBJECTCACHE_KEY + ", "
            + FieldNames.OBJECTCACHE_DATA
            + ") "
            + "VALUES (?,?,?)"
            ;
    
    
    public final static String INSERT_REFGENOME =
            "INSERT INTO " + FieldNames.TABLE_REFERENCE + " "
            + "("
            + FieldNames.REF_GEN_ID + ", "
            + FieldNames.REF_GEN_NAME + ","
            + FieldNames.REF_GEN_DESCRIPTION + ","
   //         + FieldNames.REF_GEN_SEQUENCE + ", "
            + FieldNames.REF_GEN_TIMESTAMP + " "
            + ") "
            + "VALUES (?,?,?,?)";
    
    
        public final static String INSERT_CHROMOSOME =
            "INSERT INTO " + FieldNames.TABLE_CHROMOSOME + " "
            + "("
            + FieldNames.CHROM_ID + ", "
            + FieldNames.CHROM_NUMBER + ", "
            + FieldNames.CHROM_REFERENCE_ID + ", "
            + FieldNames.CHROM_NAME + ", "
            + FieldNames.CHROM_LENGTH + ", "
            + FieldNames.CHROM_SEQUENCE + " "
            + ") "
            + "VALUES (?,?,?,?,?,?)";
    
    
    public final static String INSERT_FEATURE =
            "INSERT INTO " + FieldNames.TABLE_FEATURES + " "
            + "(" +
            FieldNames.FEATURE_ID +", " +
            FieldNames.FEATURE_CHROMOSOME_ID +", "+
            FieldNames.FEATURE_PARENT_IDS +", "+
            FieldNames.FEATURE_TYPE +", " +
            FieldNames.FEATURE_START +", " +
            FieldNames.FEATURE_STOP +", " +
            FieldNames.FEATURE_LOCUS_TAG +", " +
            FieldNames.FEATURE_PRODUCT +", " +
            FieldNames.FEATURE_EC_NUM +", " +
            FieldNames.FEATURE_STRAND +", "+
            FieldNames.FEATURE_GENE +
            " ) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?) ";
    
    
    public final static String INSERT_TRACK =
            "INSERT INTO " + FieldNames.TABLE_TRACK
            + " ("
            + FieldNames.TRACK_ID + ", "
            + FieldNames.TRACK_REFERENCE_ID + ", "
            + FieldNames.TRACK_DESCRIPTION + ", "
            + FieldNames.TRACK_TIMESTAMP + ", "
            + FieldNames.TRACK_PATH
            + ") "
            + "VALUES (?,?,?,?,?)";
    
    
    public static final String RESET_TRACK_PATH = 
            "UPDATE " + FieldNames.TABLE_TRACK 
            + " SET " + FieldNames.TRACK_PATH + " = ? " 
            + "WHERE " + FieldNames.TRACK_ID + " = ?;";
    
    
    /**
     * @param seqPairId sequence pair id to set for current track
     * @param trackId track id to set the sequence pair id for
     */
    public static final String INSERT_TRACK_SEQ_PAIR_ID =
            "UPDATE " + FieldNames.TABLE_TRACK
            + " SET " + FieldNames.TRACK_READ_PAIR_ID + " = ? "
            + " WHERE " + FieldNames.TRACK_ID + " = ? ";
    
    /**
     * Insert statistics data of one track into statistics table.
     */
    public final static String INSERT_STATISTICS =
            "INSERT INTO " + FieldNames.TABLE_STATISTICS + " "
            + "("
            + FieldNames.STATISTICS_ID + ", "
            + FieldNames.STATISTICS_TRACK_ID + ", "
            + FieldNames.STATISTICS_NUMBER_OF_MAPPINGS + ", "
            + FieldNames.STATISTICS_NUMBER_OF_PERFECT_MAPPINGS + ", "
            + FieldNames.STATISTICS_NUMBER_OF_BM_MAPPINGS + ", "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS + ", "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_BM_MAPPINGS + ", "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_PERFECT_MAPPINGS + ", "
            + FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME + ", "
            + FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME + ", "
            + FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME + ", "
            + FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ + ", "
            + FieldNames.STATISTICS_NUMBER_OF_REPEATED_SEQ + ", "
            + FieldNames.STATISTICS_NUMBER_READS + ", "
            + FieldNames.STATISTICS_AVERAGE_READ_LENGTH
            + " ) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    
    public final static String UPDATE_STATISTICS =
            "UPDATE " + FieldNames.TABLE_STATISTICS + " "
            + "SET "
            + FieldNames.STATISTICS_NUMBER_OF_MAPPINGS + "= ?, "
            + FieldNames.STATISTICS_NUMBER_OF_PERFECT_MAPPINGS + " = ?, "
            + FieldNames.STATISTICS_NUMBER_OF_BM_MAPPINGS + " = ?, "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS + " = ?, "
            + FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME + " = ?, "
            + FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME + " = ?, "
            + FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME + " = ?, "
            + FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ + " = ?, "
            + FieldNames.STATISTICS_NUMBER_READS + " = ? "
            + " WHERE  "
            + FieldNames.STATISTICS_TRACK_ID + " = ? ";
    
    /**
     * Update exisiting row of track statistics with sequence pair statistics
     */
    public static String INSERT_READPAIR_STATISTICS =
            "UPDATE " + FieldNames.TABLE_STATISTICS
            + " SET "
            + FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_SMALL_DIST_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_UNIQ_SMALL_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_LARGE_DIST_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_UNIQ_LARGE_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_ORIENT_WRONG_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS + " = ?, "
            + FieldNames.STATISTICS_NUM_SINGLE_MAPPINGS + " = ?, "
            + FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH + " = ? "
            + " WHERE "
            + FieldNames.STATISTICS_TRACK_ID + " = ? ";
    
    /**
     * Insert a new coverage distribution for a track with all 35 distribution
     * fields.
     */
    public static String INSERT_COUNT_DISTRIBUTION =
            "INSERT INTO " + FieldNames.TABLE_COUNT_DISTRIBUTION
            + " ("
            + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + ", "
            + FieldNames.COUNT_DISTRIBUTION_DISTRIBUTION_TYPE + ", "
            + FieldNames.COUNT_DISTRIBUTION_COV_INTERVAL_ID + ", "
            + FieldNames.COUNT_DISTRIBUTION_BIN_COUNT
            + " ) "
            + "VALUES (?,?,?,?)";
    
    public static String INSERT_DB_VERSION_NO = 
            "INSERT INTO " + FieldNames.TABLE_DB_VERSION
                + " ("
                + FieldNames.DB_VERSION_DB_VERSION_NO
                + " )"
                + " VALUES (?)";
    
    /**
     * Delete the track data.
     */
    public final static String DELETE_DIFFS_FROM_TRACK =
            "DELETE FROM "
            + FieldNames.TABLE_DIFF
            + " WHERE "
            + FieldNames.DIFF_MAPPING_ID
            + " IN "
            + "( "
            + "SELECT "
            + FieldNames.MAPPING_ID + " "
            + "FROM " + FieldNames.TABLE_MAPPING
            + " WHERE " + FieldNames.MAPPING_TRACK + " = ? "
            + ")";
    
    
    public final static String DELETE_MAPPINGS_FROM_TRACK =
            "DELETE FROM "
            + FieldNames.TABLE_MAPPING + " "
            + "WHERE "
            + FieldNames.MAPPING_TRACK + " = ? ";
    
    
    public final static String DELETE_COVERAGE_FROM_TRACK =
            "DELETE FROM "
            + FieldNames.TABLE_COVERAGE + " "
            + "WHERE "
            + FieldNames.COVERAGE_TRACK + " = ? ";
    
    
    public final static String DELETE_TRACK =
            "DELETE FROM "
            + FieldNames.TABLE_TRACK
            + " WHERE "
            + FieldNames.TRACK_ID + " = ? ";
    
    
    public final static String DELETE_STATISTIC_FROM_TRACK =
            "DELETE FROM "
            + FieldNames.TABLE_STATISTICS
            + " WHERE "
            + FieldNames.STATISTICS_TRACK_ID + " = ? ";
    
    public static final String DELETE_COUNT_DISTRIBUTIONS_FROM_TRACK = 
            "DELETE FROM "
            + FieldNames.TABLE_COUNT_DISTRIBUTION
            + " WHERE "
            + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + " = ? ";
            
    public static String DELETE_POS_TABLE_FROM_TRACK =
            "DELETE FROM "
            + FieldNames.TABLE_POSITIONS 
            + " WHERE "
            + FieldNames.POSITIONS_TRACK_ID + " = ? ";
            
    
    public final static String DELETE_SEQUENCE_PAIRS = //TODO: test delete seqpairs
            "DELETE FROM "
            + FieldNames.TABLE_SEQ_PAIRS + " AND "
            + " WHERE "
            + FieldNames.SEQ_PAIR_MAPPING1_ID
            + " OR "
            + FieldNames.SEQ_PAIR_MAPPING2_ID
            + " IN "
            + "( "
            + "SELECT "
            + FieldNames.MAPPING_ID
            + " FROM " + FieldNames.TABLE_MAPPING
            + " WHERE " + FieldNames.MAPPING_TRACK + " = ? "
            + ")";
    
    
    public final static String DELETE_SEQUENCE_PAIR_PIVOT =
            "DELETE FROM "
            + FieldNames.TABLE_SEQ_PAIR_PIVOT
            + " WHERE "
            + FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID
            + " IN "
            + "( "
            + "SELECT " + FieldNames.MAPPING_ID
            + " FROM " + FieldNames.TABLE_MAPPING
            + " WHERE " + FieldNames.MAPPING_TRACK + " = ? "
            + ")";
    
    
    public final static String DELETE_SEQUENCE_PAIR_REPLICATE =
            "DELETE FROM "
            + FieldNames.TABLE_SEQ_PAIR_REPLICATES
            + " WHERE "
            + FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID
            + " IN "
            + "( "
            + "SELECT "
            + FieldNames.SEQ_PAIR_PAIR_ID + " AS ORIG_PAIR_ID "
            + " FROM "
            + FieldNames.TABLE_MAPPING + ", "
            + FieldNames.TABLE_SEQ_PAIRS
            + " WHERE "
            + FieldNames.MAPPING_TRACK + " = ? AND "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + " = " + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID
            + //                        " OR " +
            //                    FieldNames.SEQ_PAIR_MAPPING2_ID + ") " +
            ")";
    
    
    /**
     * Delete all genomic features of a chromosome from the feature table.
     */
    public final static String DELETE_FEATURES_FROM_CHROMOSOME =
            "DELETE FROM "
            + FieldNames.TABLE_FEATURES
            + " WHERE " +
                FieldNames.FEATURE_CHROMOSOME_ID+" = ?";
    
    /**
     * Delete a chromosome from the chromosome table.
     */
    public static String DELETE_CHROMOSOME =
            "DELETE FROM "
            + FieldNames.TABLE_CHROMOSOME
            + " WHERE "
            + FieldNames.CHROM_ID + " = ?";
    
    /**
     * Delete a reference genome from the reference table.
     */
    public final static String DELETE_GENOME =
            "DELETE FROM "
            + FieldNames.TABLE_REFERENCE
            + " WHERE "
            + FieldNames.REF_GEN_ID + " = ?";
    
    
    // statements to fetch data from database
    
//    public static final String FETCH_PROJECT_FOLDER = 
//            "SELECT "
//                + FieldNames.PROJECT_FOLDER_PATH
//            + " FROM "
//                + FieldNames.TABLE_PROJECT_FOLDER;
    
    public static final String FETCH_GENOMES =
            "SELECT "
                + "R." + FieldNames.REF_GEN_ID + ", "
                + "R." + FieldNames.REF_GEN_NAME + ", "
                + "R." + FieldNames.REF_GEN_DESCRIPTION + ", "
                + "R." + FieldNames.REF_GEN_TIMESTAMP
            + " FROM "
                + FieldNames.TABLE_REFERENCE + " AS R ";
    
    
    public static final String FETCH_DB_VERSION =
            "SELECT "
                + FieldNames.DB_VERSION_DB_VERSION_NO
            + " FROM "
                + FieldNames.TABLE_DB_VERSION;
            
    public static final String FETCH_CHROMOSOMES =
            "SELECT "
                + FieldNames.CHROM_ID + ", "
                + FieldNames.CHROM_NUMBER + ", "
                + FieldNames.CHROM_NAME + ", "
                + FieldNames.CHROM_LENGTH
            + " FROM "
                + FieldNames.TABLE_CHROMOSOME
            + " WHERE "
                + FieldNames.CHROM_REFERENCE_ID + " = ? ";
    
    public static final String FETCH_CHROMOSOME = 
            "SELECT "
                + FieldNames.CHROM_NAME + ", "
                + FieldNames.CHROM_NUMBER + ", "
                + FieldNames.CHROM_LENGTH
            + " FROM "
                + FieldNames.TABLE_CHROMOSOME
            + " WHERE "
                + FieldNames.CHROM_ID + " = ? ";
    
    /** Fetch the chromosome sequence for a given chromosome id. */
    public static final String FETCH_CHROM_SEQ =
            "SELECT "
                + FieldNames.CHROM_SEQUENCE
            + " FROM "
                + FieldNames.TABLE_CHROMOSOME
            + " WHERE "
                + FieldNames.CHROM_ID + " = ?";
    
    /** Fetch the number of chromosomes for a reference. */
    public static String FETCH_NUMBER_CHROMS_FOR_REF =
            "SELECT "
                + "COUNT(" + FieldNames.TABLE_CHROMOSOME + "." + FieldNames.CHROM_ID + ") as NUM "
            + " FROM "
                + FieldNames.TABLE_CHROMOSOME
            + " WHERE "
                + FieldNames.CHROM_REFERENCE_ID + " = ? ";
    
    /**
     * Updates the reference genome sequence.
     */
    public static final String UPDATE_REF_GENOME =
            "UPDATE " + FieldNames.TABLE_REFERENCE
            + " SET "
            + FieldNames.REF_GEN_SEQUENCE + " = ? "
            + " WHERE "
            + FieldNames.REF_GEN_ID + " = ? ";
    
    
    public static final String UPDATE_FEATURE_TABLE =
            "UPDATE " + FieldNames.TABLE_FEATURES
            + " SET "
            + FieldNames.FEATURE_CHROMOSOME_ID + " = ? "
            + " WHERE "
            + FieldNames.FEATURE_REFGEN_ID + " = ? ";
    
    /**
     * Statement for old DBs to fetch the reference sequence, still stored here.
     * @deprecated for newer DB Versions, since the sequences are stored in the
     * chromosome table!
     */
    public static final String FETCH_REF_SEQ =
            " SELECT "
            + FieldNames.REF_GEN_SEQUENCE + ", "
            + FieldNames.REF_GEN_NAME
            + " FROM "
            + FieldNames.TABLE_REFERENCE
            + " WHERE "
            + FieldNames.REF_GEN_ID + " = ? ";
    
    
    public final static String FETCH_TRACKS =
            "SELECT "
                + " * "
            + "FROM "
                + FieldNames.TABLE_TRACK;
    
    public static final String FETCH_TRACK = 
            " SELECT * FROM " 
            + FieldNames.TABLE_TRACK
            + " WHERE " 
            + FieldNames.TRACK_ID + " = ? ";
    
    
    public final static String FETCH_SINGLE_GENOME =
            "SELECT "
            + "R." + FieldNames.REF_GEN_ID + ", "
            + "R." + FieldNames.REF_GEN_NAME + ", "
            + "R." + FieldNames.REF_GEN_DESCRIPTION + ", "
            + "R." + FieldNames.REF_GEN_TIMESTAMP
            + " FROM "
            + FieldNames.TABLE_REFERENCE + " AS R"
            + " WHERE "
            + FieldNames.REF_GEN_ID + " = ?";
    
    //Select ID from first feature belonging to the referece genome
    public final static String CHECK_IF_FEATURES_EXIST =
            "SELECT " +
                FieldNames.FEATURE_ID +
            " FROM " +
                FieldNames.TABLE_FEATURES +
            " WHERE " +
                FieldNames.FEATURE_CHROMOSOME_ID+" = ? LIMIT 1";
    
    public final static String CHECK_IF_FEATURES_OF_TYPE_EXIST =
            "SELECT " +
                FieldNames.FEATURE_ID +
            " FROM " +
                FieldNames.TABLE_FEATURES +
            " WHERE " +
                FieldNames.FEATURE_CHROMOSOME_ID+" = ? and " +
                FieldNames.FEATURE_TYPE+" = ?";
    
    public final static String FETCH_FEATURES_FOR_GENOME_INTERVAL =
            "SELECT " +
                FieldNames.FEATURE_ID+", "+
                FieldNames.FEATURE_PARENT_IDS+", "+
                FieldNames.FEATURE_TYPE+", " +
                FieldNames.FEATURE_START+", "+
                FieldNames.FEATURE_STOP+", "+
                FieldNames.FEATURE_EC_NUM+", "+
                FieldNames.FEATURE_LOCUS_TAG+", "+
                FieldNames.FEATURE_PRODUCT+", "+
                FieldNames.FEATURE_STRAND+", "+
                FieldNames.FEATURE_GENE +
            " FROM " 
            + FieldNames.TABLE_FEATURES
            + " WHERE " +
                FieldNames.FEATURE_CHROMOSOME_ID+" = ? and " +
                FieldNames.FEATURE_STOP+" >= ? and " +
                FieldNames.FEATURE_START+" <= ? " + 
            " ORDER BY " + FieldNames.FEATURE_START;
    
    public final static String FETCH_SPECIFIED_FEATURES_FOR_GENOME_INTERVAL =
            "SELECT " +
                FieldNames.FEATURE_ID+", "+
                FieldNames.FEATURE_PARENT_IDS+", "+
                FieldNames.FEATURE_TYPE+", " +
                FieldNames.FEATURE_START+", "+
                FieldNames.FEATURE_STOP+", "+
                FieldNames.FEATURE_EC_NUM+", "+
                FieldNames.FEATURE_LOCUS_TAG+", "+
                FieldNames.FEATURE_PRODUCT+", "+
                FieldNames.FEATURE_STRAND+", "+
                FieldNames.FEATURE_GENE +
            " FROM " 
            + FieldNames.TABLE_FEATURES
            + " WHERE " +
                FieldNames.FEATURE_CHROMOSOME_ID+" = ? and " +
                FieldNames.FEATURE_STOP+" >= ? and " +
                FieldNames.FEATURE_START+" <= ? and " + 
                FieldNames.FEATURE_TYPE+" = ? " + 
            " ORDER BY " + FieldNames.FEATURE_START;
    
    public final static String FETCH_FEATURES_FOR_CLOSED_GENOME_INTERVAL =
            "SELECT " +
                FieldNames.FEATURE_ID+", "+
                FieldNames.FEATURE_PARENT_IDS+", "+
                FieldNames.FEATURE_TYPE+", " +
                FieldNames.FEATURE_START+", "+
                FieldNames.FEATURE_STOP+", "+
                FieldNames.FEATURE_EC_NUM+", "+
                FieldNames.FEATURE_LOCUS_TAG+", "+
                FieldNames.FEATURE_PRODUCT+", "+
                FieldNames.FEATURE_STRAND+", "+
                FieldNames.FEATURE_GENE +
            " FROM "
            + FieldNames.TABLE_FEATURES
            + " WHERE " +
                FieldNames.FEATURE_CHROMOSOME_ID + " = ? and " +
                FieldNames.FEATURE_START + " between ? and ? and " +
                FieldNames.FEATURE_STOP + " between ? and ? " +
                " ORDER BY " + FieldNames.FEATURE_START;
    
                
    public final static String FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK =
            "SELECT "
            + FieldNames.COVERAGE_POSITION + ", "
            + FieldNames.COVERAGE_BM_FW_MULT + ", "
            + FieldNames.COVERAGE_BM_FW_NUM + ", " + //TODO: was ist mit num? wird nie benutzt!!!
            FieldNames.COVERAGE_BM_RV_MULT + ", "
            + FieldNames.COVERAGE_BM_RV_NUM + ", "
            + FieldNames.COVERAGE_N_FW_MULT + ", "
            + FieldNames.COVERAGE_N_FW_NUM + ", "
            + FieldNames.COVERAGE_N_RV_MULT + ", "
            + FieldNames.COVERAGE_N_RV_NUM + ", "
            + FieldNames.COVERAGE_ZERO_FW_MULT + ", "
            + FieldNames.COVERAGE_ZERO_FW_NUM + ", "
            + FieldNames.COVERAGE_ZERO_RV_MULT + ", "
            + FieldNames.COVERAGE_ZERO_RV_NUM + " "
            + "FROM "
            + FieldNames.TABLE_COVERAGE + " "
            + "WHERE ("
            + FieldNames.COVERAGE_TRACK + " = ?) and ("
            + FieldNames.COVERAGE_POSITION + " between ? and ?) "
            ;
    
    
    public final static String FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART1 =
            "SELECT "+
                FieldNames.COVERAGE_POSITION+", "+
                FieldNames.COVERAGE_BM_FW_MULT+", " +
                FieldNames.COVERAGE_BM_FW_NUM+", " + 
                FieldNames.COVERAGE_BM_RV_MULT+", " +
                FieldNames.COVERAGE_BM_RV_NUM+", " +
                FieldNames.COVERAGE_N_FW_MULT+", " +
                FieldNames.COVERAGE_N_FW_NUM+", " +
                FieldNames.COVERAGE_N_RV_MULT+", " +
                FieldNames.COVERAGE_N_RV_NUM+", " +
                FieldNames.COVERAGE_ZERO_FW_MULT+", " +
                FieldNames.COVERAGE_ZERO_FW_NUM+", " +
                FieldNames.COVERAGE_ZERO_RV_MULT+", " +
                FieldNames.COVERAGE_ZERO_RV_NUM+", " +
                FieldNames.COVERAGE_TRACK + 
            " FROM " +
                FieldNames.TABLE_COVERAGE+
            " WHERE "+
                FieldNames.COVERAGE_POSITION+ " between ? and ? and ";
        
    public final static String FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART2 =
            FieldNames.COVERAGE_TRACK + " = ? ";

            
    public final static String FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2 =
            "SELECT "
            + FieldNames.COVERAGE_POSITION + ", "
            + FieldNames.COVERAGE_N_FW_MULT + ", "
            + FieldNames.COVERAGE_N_RV_MULT
            + " FROM "
            + FieldNames.TABLE_COVERAGE
            + " WHERE "
            + FieldNames.COVERAGE_POSITION + " BETWEEN ? AND ? and "
            + FieldNames.COVERAGE_TRACK + " = ? ";
    
    
    public final static String FETCH_COVERAGE_FOR_TRACK =
            "SELECT "
            + FieldNames.COVERAGE_POSITION + ", "
            + FieldNames.COVERAGE_N_FW_MULT + " + "
            + FieldNames.COVERAGE_N_RV_MULT + " as " + FieldNames.COVERAGE_N_MULT
            + " FROM "
            + FieldNames.TABLE_COVERAGE
            + " WHERE "
            + FieldNames.COVERAGE_TRACK + " = ?  and " + FieldNames.COVERAGE_POSITION + " between ? and ?";
    
    
    public static final String FETCH_COVERAGE_BEST_FOR_INTERVAL =
            "SELECT "
            + FieldNames.COVERAGE_POSITION + ", "
            + FieldNames.COVERAGE_ZERO_FW_MULT + ", "
            + FieldNames.COVERAGE_ZERO_RV_MULT + ", "
            + FieldNames.COVERAGE_BM_FW_MULT + ", "
            + FieldNames.COVERAGE_BM_RV_MULT
            + " FROM "
            + FieldNames.TABLE_COVERAGE
            + " WHERE "
            + FieldNames.COVERAGE_TRACK + " = ? AND "
            + FieldNames.COVERAGE_POSITION + " BETWEEN ? AND ?";
         
         
   public final static String FETCH_TRACKS_FOR_GENOME =
            "SELECT * "
            + " FROM "
            + FieldNames.TABLE_TRACK
            + " WHERE "
            + FieldNames.TRACK_REFERENCE_ID + " = ? ";
    
    
    public final static String GET_LATEST_STATISTICS_ID =
            "SELECT "
            + "MAX(" + FieldNames.STATISTICS_ID + ") AS LATEST_ID "
            + "FROM "
            + FieldNames.TABLE_STATISTICS;
    
    
    public final static String FETCH_OBJECTFROMCACHE = 
            "SELECT " + FieldNames.OBJECTCACHE_DATA 
            + " FROM " + FieldNames.TABLE_OBJECTCACHE
            + " WHERE " + FieldNames.OBJECTCACHE_FAMILY + " = ? "
            + " AND " + FieldNames.OBJECTCACHE_KEY + " = ? "
            ;
    
    public final static String FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK =
            "SELECT "
            + "M." + FieldNames.MAPPING_ID + ", "
            + "M." + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + "M." + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + "M." + FieldNames.MAPPING_DIRECTION + ", "
            + "M." + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + "M." + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + "M." + FieldNames.MAPPING_START + ", "
            + "M." + FieldNames.MAPPING_STOP + ", "
            + "M." + FieldNames.MAPPING_TRACK + ", "
            + "D." + FieldNames.DIFF_BASE + ", "
            + "D." + FieldNames.DIFF_GAP_ORDER + ", "
            + "D." + FieldNames.DIFF_POSITION + ", "
            + "D." + FieldNames.DIFF_TYPE + " "
            + "FROM ("
            + "SELECT "
            + FieldNames.MAPPING_ID + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + " "
            + "FROM ( "
            + "SELECT "
            + FieldNames.MAPPING_ID + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + " "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " "
            + "WHERE "
            + FieldNames.MAPPING_START + " BETWEEN ? AND ? and "
            + FieldNames.MAPPING_STOP + " BETWEEN ? AND ? "
            + ") AS MM "
            + "WHERE "
            + FieldNames.MAPPING_TRACK + " = ? "
            + ") AS M "
            + "LEFT JOIN ("
            + "SELECT "
            + FieldNames.DIFF_BASE + ", "
            + FieldNames.DIFF_GAP_ORDER + ", "
            + FieldNames.DIFF_POSITION + ", "
            + FieldNames.DIFF_TYPE + ", "
            + FieldNames.DIFF_MAPPING_ID + " "
            + "FROM "
            + FieldNames.TABLE_DIFF + " "
            + "WHERE "
            + FieldNames.DIFF_POSITION + " BETWEEN ? AND ? "
            + ") AS D "
            + "on "
            + "M." + FieldNames.MAPPING_ID + " = D." + FieldNames.DIFF_MAPPING_ID;

            
    public final static String FETCH_MAPPINGS_WITHOUT_DIFFS =
            "SELECT "
            + FieldNames.MAPPING_ID + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + " "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " "
            + "WHERE "
            + FieldNames.MAPPING_START + " BETWEEN ? AND ? and "
            //            + FieldNames.MAPPING_STOP + " BETWEEN ? AND ? and "
            + FieldNames.MAPPING_TRACK + " = ? "
            + "ORDER BY " + FieldNames.MAPPING_START;
    
    
    /**
     * Fetches mappins without diffs where the mapping id is between 2 given values.
     * The result is sorted by mapping start.
     */
    public final static String FETCH_MAPPINGS_BY_ID_WITHOUT_DIFFS =
            "SELECT "
            + FieldNames.MAPPING_ID + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + " "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " "
            + "WHERE "
            + FieldNames.MAPPING_ID + " BETWEEN ? AND ? "
            //            + FieldNames.MAPPING_STOP + " BETWEEN ? AND ? and "
            //            + FieldNames.MAPPING_TRACK + " = ? "
            + "ORDER BY " + FieldNames.MAPPING_START;
    
    /**
     * kstaderm: Return all the Mappings belonging to a given track.
     */
    public static final String LOAD_MAPPINGS_BY_TRACK_ID =
            "SELECT "
            + FieldNames.MAPPING_ID + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + " "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " "
            + "WHERE "
            + FieldNames.MAPPING_TRACK + " "
            + "=?";
    
    /**
     * kstaderm: Return all the Mappings belonging to a given track.
     */
    public static final String FETCH_REDUCED_MAPPINGS_BY_TRACK_ID_AND_MAP_ID_INTERVAL =
            "SELECT "
                + FieldNames.MAPPING_DIRECTION + ", "
                + FieldNames.MAPPING_START + ", "
                + FieldNames.MAPPING_STOP + ", "
                + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
                + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
                + FieldNames.MAPPING_NUM_OF_REPLICATES
            + " FROM "
                + FieldNames.TABLE_MAPPING
            + " WHERE "
                + FieldNames.MAPPING_TRACK + " = ? AND "
                + FieldNames.MAPPING_ID + " BETWEEN ? AND ? ";
    
    public static final String FETCH_READ_STARTS_BY_TRACK_ID_AND_REF_INTERVAL =
            "SELECT "
                + FieldNames.MAPPING_DIRECTION + ", "
                + FieldNames.MAPPING_START + ", "
                + FieldNames.MAPPING_STOP + ", "
                + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
                + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
                + FieldNames.MAPPING_NUM_OF_REPLICATES
            + " FROM "
                + FieldNames.TABLE_MAPPING
            + " WHERE "
                + FieldNames.MAPPING_TRACK + " = ? and "
                + FieldNames.MAPPING_START + " BETWEEN ? AND ? ";
    
    /*
     * <1min variante mit start between ? and ? 3min: variante mit start < ? &
     * start < ? 7min: variante mit start < ? & stop > ?
     */  
    public final static String FETCH_DIFFS_AND_GAPS_FOR_INTERVAL =
            "SELECT "
            + "D." + FieldNames.DIFF_POSITION + ", "
            + "D." + FieldNames.DIFF_BASE + ", "
            + "D." + FieldNames.DIFF_GAP_ORDER + ", "
            + "D." + FieldNames.DIFF_TYPE + ", "
            + "M." + FieldNames.MAPPING_DIRECTION + ", "
            + "M." + FieldNames.MAPPING_NUM_OF_REPLICATES + " "
            + "FROM "
            + FieldNames.TABLE_DIFF + " AS D , "
            + FieldNames.TABLE_MAPPING + " AS M "
            + "WHERE "
            + "D." + FieldNames.DIFF_POSITION + " between ? and ? and "
//            + "D." + FieldNames.DIFF_TYPE + " = 1 and "
            + "D." + FieldNames.DIFF_MAPPING_ID + " = M." + FieldNames.MAPPING_ID + " and "
            + "M." + FieldNames.MAPPING_TRACK + " = ?";
    
         
         public static final String FETCH_SEQ_PAIRS_W_REPLICATES_FOR_INTERVAL_PERFECT =
            "SELECT "
            + " MAPPING_ID, "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + " MAPPING_REP, "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + ", "
            + " ORIG_PAIR_ID, "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + ", "
            + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES + " "
            + " FROM ("
            + "SELECT "
            + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID + " AS MAPPING_ID, "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_NUM_OF_REPLICATES + " AS MAPPING_REP, "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + ", "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + " AS ORIG_PAIR_ID, "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + " "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " , "
            + FieldNames.TABLE_SEQ_PAIRS + " "
            + " WHERE "
            + FieldNames.MAPPING_START + "  BETWEEN ? AND ? AND "
            + FieldNames.MAPPING_STOP + " BETWEEN ? AND ? AND "
            + " ( " + FieldNames.MAPPING_TRACK + " = ? OR " + FieldNames.MAPPING_TRACK + " = ?) AND "
            + FieldNames.SEQ_PAIR_TYPE + " = " + ReadPairType.PERFECT_PAIR.getTypeString() + " AND "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + " = " + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID
            + " ) LEFT OUTER JOIN "
            + FieldNames.TABLE_SEQ_PAIR_REPLICATES
            + " ON "
            + " ORIG_PAIR_ID = " + FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID;
         
         
         public static final String FETCH_SEQ_PAIRS_W_REPLICATES_FOR_INTERVAL_PERFECT2 =
            "SELECT "
            + " MAPPING_ID, "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + " MAPPING_REP, "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + ", "
            + " ORIG_PAIR_ID, "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + ", "
            + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES + " "
            + " FROM ("
            + "SELECT "
            + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID + " AS MAPPING_ID, "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_NUM_OF_REPLICATES + " AS MAPPING_REP, "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + ", "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + " AS ORIG_PAIR_ID, "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + " "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " , "
            + FieldNames.TABLE_SEQ_PAIRS + " "
            + " WHERE "
            + FieldNames.MAPPING_START + "  BETWEEN ? AND ? AND "
            + FieldNames.MAPPING_STOP + " BETWEEN ? AND ? AND "
            + " ( " + FieldNames.MAPPING_TRACK + " = ? OR " + FieldNames.MAPPING_TRACK + " = ?) AND "
            + FieldNames.SEQ_PAIR_TYPE + " = " + ReadPairType.PERFECT_PAIR.getTypeString() + " AND "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + " = " + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID
            + " ) LEFT OUTER JOIN "
            + FieldNames.TABLE_SEQ_PAIR_REPLICATES
            + " ON "
            + " ORIG_PAIR_ID = " + FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID;
         
         
         public static final String FETCH_SEQ_PAIRS_PIVOT_DATA_FOR_INTERVAL =
            "SELECT "
            + "MAPPING_ORIG_ID, "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + ", "
            + FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID + " "
            + "FROM ("
            + "SELECT "
            + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID + " as MAPPING_ORIG_ID, "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.MAPPING_SEQUENCE_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_TRACK + " "
            + "FROM "
            + " MAPPING "
            + " WHERE "
            + FieldNames.MAPPING_START + "  BETWEEN ? AND ? AND "
            + FieldNames.MAPPING_STOP + " BETWEEN ? AND ? AND "
            + "(" + FieldNames.MAPPING_TRACK + " = ? OR " + FieldNames.MAPPING_TRACK + " = ?) "
            + ") AS MM "
            + " LEFT OUTER JOIN "
            + FieldNames.TABLE_SEQ_PAIR_PIVOT
            + " ON "
            + FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID + " = " + " MAPPING_ORIG_ID "
            + " WHERE "
            + FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID + " != -1;";
         
    /**
     * Returns all paired mappings belonging to the sequence pair with the given
     * sequence pair id.
     */
    public static String FETCH_MAPPINGS_FOR_SEQ_PAIR_ID =
            "SELECT "
            + FieldNames.SEQ_PAIR_PAIR_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + ", "
            + "RES." + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_NUM_OF_REPLICATES + " as MAPPING_REPLICATES "
            + " FROM "
            + "(SELECT "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + ", "
            + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES + " "
            + " FROM "
            + FieldNames.TABLE_SEQ_PAIRS
            + " LEFT JOIN "
            + FieldNames.TABLE_SEQ_PAIR_REPLICATES
            + " ON "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + " = "
            + FieldNames.TABLE_SEQ_PAIR_REPLICATES + "." + FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID
            + " WHERE "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + " = ? "
            + ") as RES "
            + " LEFT JOIN "
            + FieldNames.TABLE_MAPPING
            + " ON "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + " = " + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID;
    
    /**
     * Returns all paired mappings belonging to the sequence pair with the given
     * sequence pair id.
     */
    public static String FETCH_MAPPINGS_FOR_SEQ_PAIR_ID2 =
            "SELECT "
            + FieldNames.SEQ_PAIR_PAIR_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + ", "
            + "RES." + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_DIRECTION + ", "
            + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_NUM_OF_REPLICATES + " as MAPPING_REPLICATES "
            + " FROM "
            + "(SELECT "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
            + FieldNames.SEQ_PAIR_TYPE + ", "
            + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES + " "
            + " FROM "
            + FieldNames.TABLE_SEQ_PAIRS
            + " LEFT JOIN "
            + FieldNames.TABLE_SEQ_PAIR_REPLICATES
            + " ON "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + " = "
            + FieldNames.TABLE_SEQ_PAIR_REPLICATES + "." + FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID
            + " WHERE "
            + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + " = ? "
            + ") as RES "
            + " LEFT JOIN "
            + FieldNames.TABLE_MAPPING
            + " ON "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + " = " + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID;
    
    /**
     * Returns all single mappings belonging to the sequence pair with the given
     * sequence pair id.
     */
    public static String FETCH_SINGLE_MAPPINGS_FOR_SEQ_PAIR_ID =
            "SELECT "
            + FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID + ", "
            + FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID + ", "
            + FieldNames.MAPPING_START + ", "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_NUM_OF_REPLICATES + ", "
            + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
            + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
            + FieldNames.MAPPING_DIRECTION + " "
            + " FROM "
            + "(SELECT "
            + FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID + ", "
            + FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID
            + " FROM "
            + FieldNames.TABLE_SEQ_PAIR_PIVOT
            + " WHERE "
            + FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID + " = ? "
            + ") as RES "
            + " LEFT JOIN "
            + FieldNames.TABLE_MAPPING
            + " ON "
            + FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID + " = " + FieldNames.MAPPING_ID;
    
    
    /////////////////// statistics calculations and querries //////////////////////////
    
    public final static String CHECK_FOR_TRACK_IN_STATS_CALCULATE =
            "SELECT "
            + "COUNT(S." + FieldNames.STATISTICS_TRACK_ID + ") as NUM "
            + " FROM "
            + FieldNames.TABLE_STATISTICS + " as S "
            + "WHERE "
            + "S." + FieldNames.STATISTICS_TRACK_ID + " = ?";    
    
    /**
     * +n for the nb of replicates (n) of each sequence id (also, when sequence
     * id occurs more than once.
     */
    public static final String FETCH_NUM_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT "
            + "SUM(R." + FieldNames.MAPPING_NUM_OF_REPLICATES + ") as NUM "
            + "FROM "
            + "(SELECT "
            + "M." + FieldNames.MAPPING_SEQUENCE_ID + " , " + FieldNames.MAPPING_NUM_OF_REPLICATES + //DISTINCT returns mapping count for unique sequences
            " FROM "
            + FieldNames.TABLE_MAPPING + " as M "
            + "WHERE "
            + "M." + FieldNames.MAPPING_TRACK + " = ? "
            + ") as R;";
         
    /**
     * +1 for each mapping id = mapping with different positions
     */
     public static final String FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT "
            + "COUNT(M." + FieldNames.MAPPING_ID + ") as NUM "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " as M "
            + "WHERE "
            + "M." + FieldNames.MAPPING_TRACK + " = ?";
    
    
    /**
     * Number of real different sequence numbers of unique sequences is the same
     * as the number of distinct mapped sequences. The number of all sequences
     * would be the number of reads.
     */
    public final static String FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE =
            "SELECT "
                + "COUNT(DISTINCT M." + FieldNames.MAPPING_SEQUENCE_ID + ") as NUM "
            + "FROM "
                + FieldNames.TABLE_MAPPING + " as M "
            + "WHERE "
                + "M." + FieldNames.MAPPING_TRACK + " = ?";
    
    /**
     * Don't use yet, not working!
     */
    public final static String FETCH_NUM_SEQ_PAIRS_FOR_TRACK_CALCULATE =
            "SELECT "
            + "COUNT(DISTINCT S." + FieldNames.SEQ_PAIR_ID + ") as NUM "
            + "FROM "
            + FieldNames.TABLE_SEQ_PAIR_PIVOT + " as S "
            + "WHERE " + //TODO:mappingid holen & track abgleichen...
            "S." + FieldNames.MAPPING_TRACK + " = ?";    
    
    public static final String FETCH_STATS_FOR_TRACK = 
            "SELECT * FROM "
            + FieldNames.TABLE_STATISTICS
            + " WHERE "
            + FieldNames.STATISTICS_TRACK_ID + " = ?";    
    
    public final static String FETCH_NUM_BM_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT "
            + "COUNT(M." + FieldNames.MAPPING_ID + ") as NUM "
            + " FROM "
            + FieldNames.TABLE_MAPPING + " as M "
            + "WHERE "
            + "M." + FieldNames.MAPPING_TRACK + " = ? AND "
            + "M." + FieldNames.MAPPING_IS_BEST_MAPPING + " = 1 ";
    
    public final static String FETCH_BM_COVERAGE_OF_GENOME_CALCULATE =
            "SELECT "
            + "COUNT(" + FieldNames.COVERAGE_ID + ") as NUM "
            + "FROM "
            + FieldNames.TABLE_COVERAGE + " "
            + "WHERE "
            + "(" + FieldNames.COVERAGE_BM_FW_MULT + " + " + FieldNames.COVERAGE_BM_RV_MULT + ") != 0 AND "
            + FieldNames.COVERAGE_TRACK + " = ?";    
    
    /**
     * Number of singletons = sequences, which were only mapped once, but can have several replicates.
     * The replicates are summed in this case.
     */
    public static final String FETCH_NUM_SINGLETON_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT "
                + "sum(S." + FieldNames.MAPPING_NUM_OF_REPLICATES + ") as NUM " +//DISTINCT SEQUENCE_ID  returns  singleton count for unique sequences
            "FROM ( SELECT "
                + FieldNames.MAPPING_NUM_OF_REPLICATES
                + " FROM "
                    + FieldNames.TABLE_MAPPING + " as M "
                + "WHERE "
                    + "M." + FieldNames.MAPPING_TRACK + " = ? "
                + "GROUP BY M."
                    + FieldNames.MAPPING_SEQUENCE_ID + ",M." + FieldNames.MAPPING_NUM_OF_REPLICATES
                + " HAVING COUNT(M." + FieldNames.MAPPING_SEQUENCE_ID + ") = 1) AS S";
    
    public final static String FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT "
                + "COUNT(M." + FieldNames.MAPPING_ID + ") as NUM "
            + " FROM "
                + FieldNames.TABLE_MAPPING + " as M "
            + "WHERE "
                + "M." + FieldNames.MAPPING_TRACK + " = ? and "
                + "M." + FieldNames.MAPPING_NUM_OF_ERRORS + " = 0 ";
    
    
//    public final static String FETCH_NUM_READS_FOR_TRACK =
//            "SELECT "
//            + FieldNames.STATISTICS_NUMBER_READS + " as Num "
//            + " FROM "
//            + FieldNames.TABLE_STATISTICS + " as S "
//            + "WHERE "
//            + "S." + FieldNames.STATISTICS_TRACK_ID + " = ?";
    
    /*
     * eine seq id = mehrere reads & mehrere mappings -> anzahl mappings/seq id
     * z.b. 10 = 10 versch. pos. = mÃ¼ssen immer dieselben (max. X) reads sein.
     * z.b. kÃ¶nnen 20 reads auf selbe seq id kommen + 10 versch pos abdecken =
     * 10 mappings mit je 20 replicates. 20 reads auf 10pos = 10 unique mappings
     * + je 20 replicates. nimm 1 der unique mapping mit gleicher seq id & zÃ¤hle
     * replicates = 20 hieÃŸe 20 reads, kann die 20 noch anderweitig zustande
     * kommen?
     * 
     * Ein read wird mehrmals aufs genom gemappt, aber nicht mehrmals auf die selbe
     * position. D.h. Auswahl eines der mappings aus den 10 mappings mit der selben
     * sequence id und summieren der replikate.
     */
    public final static String FETCH_NUM_READS_FOR_TRACK_CALCULATE =
            "SELECT "
            + " sum(M." + FieldNames.MAPPING_NUM_OF_REPLICATES + ") AS NUM "
                + " FROM(SELECT "
                    + "distinct(" + FieldNames.MAPPING_SEQUENCE_ID + ")," + FieldNames.MAPPING_NUM_OF_REPLICATES
                + " FROM "
                    + FieldNames.TABLE_MAPPING
                + " WHERE "
                    + FieldNames.MAPPING_TRACK + "=?) AS M";
    
    
//    public final static String FETCH_GENOMEID_FOR_TRACK =
//            "SELECT "
//            + FieldNames.TRACK_REFERENCE_ID + " "
//            + "FROM "
//            + FieldNames.TABLE_TRACK + " "
//            + "WHERE "
//            + FieldNames.TRACK_ID + " = ?";
    
    
    public final static String FETCH_NUM_PERFECT_COVERED_POSITIONS_FOR_TRACK =
            "SELECT "
            + "COUNT(" + FieldNames.COVERAGE_ID + ") as NUM "
            + "FROM "
            + FieldNames.TABLE_COVERAGE + " "
            + "WHERE "
            + "(" + FieldNames.COVERAGE_ZERO_FW_MULT + " + " + FieldNames.COVERAGE_ZERO_RV_MULT + ") != 0 AND "
            + FieldNames.COVERAGE_TRACK + " = ?";
    
    public final static String FETCH_NUM_COVERED_POSITIONS =
            "SELECT "
            + "COUNT(" + FieldNames.COVERAGE_ID + ") as NUM "
            + "FROM "
            + FieldNames.TABLE_COVERAGE + " "
            + "WHERE "
            + "(" + FieldNames.COVERAGE_N_FW_MULT + " + " + FieldNames.COVERAGE_N_RV_MULT + ") != 0 AND "
            + FieldNames.COVERAGE_TRACK + " = ?";
    
    public final static String FETCH_NUM_OF_PERFECT_POSITIONS_FOR_TRACK =
            "SELECT "
            + "count(" + FieldNames.COVERAGE_POSITION + ") as NUM "
            + " FROM "
            + FieldNames.TABLE_COVERAGE + " as C "
            + "WHERE "
            + "(C." + FieldNames.COVERAGE_ZERO_FW_MULT + " + " + FieldNames.COVERAGE_ZERO_RV_MULT + " ) !=0 and " + FieldNames.COVERAGE_TRACK + "= ?";
    
    /**
     * @param trackId track id of one track of a sequence pair
     */
    public static String FETCH_READ_PAIR_TO_TRACK_ID =
            "SELECT "
            + FieldNames.TRACK_READ_PAIR_ID + " AS NUM "
            + "FROM "
            + FieldNames.TABLE_TRACK + " "
            + "WHERE "
            + FieldNames.TRACK_ID + " = ? ";
    
    /**
     * Fetches second track id for sequence pair tracks.
     *
     * @param seqPairId sequence pair id
     * @param trackId track id of one of the two tracks of the pair
     */
    public static String FETCH_TRACK_ID_TO_READ_PAIR_ID =
            "SELECT "
            + FieldNames.TRACK_ID + " "
            + "FROM "
            + FieldNames.TABLE_TRACK + " "
            + "WHERE "
            + FieldNames.TRACK_READ_PAIR_ID + " = ? AND "
            + FieldNames.TRACK_ID + " != ? ";
    
    
    public final static String FETCH_SNP_IDS_FOR_TRACK =
            "SELECT * FROM " + FieldNames.TABLE_POSITIONS + " WHERE " + FieldNames.POSITIONS_TRACK_ID + " = ?";
    
    
    public final static String FETCH_DIFFS_HAVING_SNP_ID =
            "SELECT "
            + FieldNames.DIFF_MAPPING_ID + ","
            + FieldNames.DIFF_BASE + ","
            + FieldNames.DIFF_POSITION + ","
            + FieldNames.DIFF_TYPE + ","
            + FieldNames.DIFF_GAP_ORDER + " "
            + "FROM "
            + FieldNames.TABLE_DIFF + " "
            + "WHERE "
            + FieldNames.DIFF_SNP_ID + " = ?";
    
    
    //track id query could be included if desired and performance can be improved
    public final static String FETCH_SNPS =
            "SELECT * "
            + "FROM "
            + FieldNames.TABLE_POSITIONS
            + " WHERE ("
            + FieldNames.POSITIONS_TRACK_ID + " = ? AND "
            + FieldNames.POSITIONS_TYPE + " != 'M' AND "
            + FieldNames.POSITIONS_FREQUENCY + " >= ? AND "
            + "SELECT CASE WHEN "
            + FieldNames.POSITIONS_REFERENCE_BASE + " = 'A' "
            + "THEN GREATEST("
            + FieldNames.POSITIONS_C + "," + FieldNames.POSITIONS_G + "," + FieldNames.POSITIONS_T + "," 
            + FieldNames.POSITIONS_N + "," + FieldNames.POSITIONS_GAP
            + ") >= ? "
            + "WHEN "
            + FieldNames.POSITIONS_REFERENCE_BASE + " = 'C' "
            + "THEN GREATEST("
            + FieldNames.POSITIONS_A + "," + FieldNames.POSITIONS_G + "," + FieldNames.POSITIONS_T + "," 
            + FieldNames.POSITIONS_N + "," + FieldNames.POSITIONS_GAP
            + ") >= ? "
            + "WHEN "
            + FieldNames.POSITIONS_REFERENCE_BASE + " = 'G' "
            + "THEN GREATEST("
            + FieldNames.POSITIONS_A + "," + FieldNames.POSITIONS_C + "," + FieldNames.POSITIONS_T + "," 
            + FieldNames.POSITIONS_N + "," + FieldNames.POSITIONS_GAP
            + ") >= ? "
            + "WHEN "
            + FieldNames.POSITIONS_REFERENCE_BASE + " = 'T' "
            + "THEN GREATEST("
            + FieldNames.POSITIONS_A + "," + FieldNames.POSITIONS_C + "," + FieldNames.POSITIONS_G + "," 
            + FieldNames.POSITIONS_N + "," + FieldNames.POSITIONS_GAP
            + ") >= ? "
            + "ELSE GREATEST("
            + FieldNames.POSITIONS_A + "," + FieldNames.POSITIONS_C + "," + FieldNames.POSITIONS_G + "," 
            + FieldNames.POSITIONS_T + "," + FieldNames.POSITIONS_N + "," + FieldNames.POSITIONS_GAP
            + ") >= ? END) "
            + "ORDER BY " + FieldNames.POSITIONS_POSITION;
    
    
    public final static String GET_DIRECTION_OF_MAPPING =
            "SELECT "
            + FieldNames.MAPPING_DIRECTION + " "
            + "FROM "
            + FieldNames.TABLE_MAPPING + " "
            + "WHERE "
            + FieldNames.MAPPING_ID + " = ?";
   
    
    public final static String GET_LATEST_COVERAGE_ID =
            "SELECT MAX(" + FieldNames.COVERAGE_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_COVERAGE;
    
    
    public final static String GET_LATEST_REFERENCE_ID =
            "SELECT MAX(" + FieldNames.REF_GEN_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_REFERENCE;
    
    
    public final static String GET_LATEST_CHROMOSOME_ID =
             "SELECT MAX(" + FieldNames.CHROM_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_CHROMOSOME;
    
    
    public final static String GET_LATEST_FEATURE_ID =
            "SELECT MAX("+FieldNames.FEATURE_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_FEATURES;
    
    
    public final static String GET_LATEST_TRACK_ID =
            "SELECT MAX(" + FieldNames.TRACK_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_TRACK;
    
    
    public final static String GET_LATEST_MAPPING_ID =
            "SELECT MAX(" + FieldNames.MAPPING_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_MAPPING;
    
    
    public final static String GET_LATEST_DIFF_ID =
            "SELECT MAX(" + FieldNames.DIFF_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_DIFF;
         
    
    public static final String GET_LATEST_SEQUENCE_PAIR_ID =
            "SELECT MAX(" + FieldNames.SEQ_PAIR_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_SEQ_PAIRS;
         
    public static final String GET_LATEST_SEQUENCE_PAIR_PAIR_ID =
            "SELECT MAX(" + FieldNames.SEQ_PAIR_PAIR_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_SEQ_PAIRS;
    
    
    public final static String GET_LATEST_SNP_ID =
            "SELECT MAX(" + FieldNames.POSITIONS_SNP_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_POSITIONS;
        
    
    public static final String GET_LATEST_TRACK_SEQUENCE_PAIR_ID =
            "SELECT MAX(" + FieldNames.TRACK_READ_PAIR_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_TRACK;
        
    
    public static final String GET_CURRENT_READLENGTH =
            "SELECT "
            + FieldNames.MAPPING_STOP + ", "
            + FieldNames.MAPPING_START
            + " FROM "
            + FieldNames.TABLE_MAPPING
            + " WHERE "
            + FieldNames.MAPPING_TRACK + " = ? "
            + " LIMIT 1 ";
    
    
    public static final String FETCH_COUNT_DISTRIBUTION =
            "SELECT "
            + FieldNames.COUNT_DISTRIBUTION_COV_INTERVAL_ID + ", "
            + FieldNames.COUNT_DISTRIBUTION_BIN_COUNT
            + " FROM "
            + FieldNames.TABLE_COUNT_DISTRIBUTION
            + " WHERE "
            + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + " = ? AND "
            + FieldNames.COUNT_DISTRIBUTION_DISTRIBUTION_TYPE + " = ? ";
    
    
    public static String INIT_FEATURE_PARENT_ID =
            "UPDATE "
            + FieldNames.TABLE_FEATURES
            + " SET "
            + FieldNames.FEATURE_PARENT_IDS
            + " = 0 "
            + " WHERE "
            + FieldNames.FEATURE_PARENT_IDS + " IS NULL ";
    
    
    public static final String NOT_NULL_FEATURE_PARENT_ID =
            "ALTER TABLE "
            + FieldNames.TABLE_FEATURES
            + " ALTER COLUMN "
            + FieldNames.FEATURE_PARENT_IDS
            + " SET NOT NULL";
    
    
    public static final String NOT_NULL_CHROMOSOME_ID =
            "ALTER TABLE "
            + FieldNames.TABLE_FEATURES
            + " ALTER COLUMN "
            + FieldNames.FEATURE_CHROMOSOME_ID
            + " SET NOT NULL";
    
//             public static final String COPY_TO_FEATURE_DETAILS_TABLE =
//                " INSERT INTO " + FieldNames.TABLE_FEATURE_DETAILS + " ("
//                    + FieldNames.FEATURE_ID + ", "
//                    + FieldNames.FEATURE_EC_NUM + ", " 
//                    + FieldNames.FEATURE_LOCUS_TAG + ", " 
//                    + FieldNames.FEATURE_PRODUCT + ", " 
//                    + FieldNames.FEATURE_STRAND + ", " 
//                    + FieldNames.FEATURE_GENE + ") "
//                + " SELECT "
//                    + FieldNames.FEATURE_ID + ", "
//                    + FieldNames.FEATURE_EC_NUM + ", " 
//                    + FieldNames.FEATURE_LOCUS_TAG + ", " 
//                    + FieldNames.FEATURE_PRODUCT + ", " 
//                    + FieldNames.FEATURE_STRAND + ", " 
//                    + FieldNames.FEATURE_GENE
//                + " FROM " 
//                    + FieldNames.TABLE_FEATURES +
//                " WHERE EXISTS ("
//                    + "SELECT * "
//                    + "FROM INFORMATION_SCHEMA.COLUMNS "
//                    + "WHERE TABLE_NAME = '" + FieldNames.TABLE_FEATURES + "' "
//                    + " AND COLUMN_NAME = '" + FieldNames.FEATURE_PRODUCT + "' ) ";
//        
//        
//   
//     
//             public static final String CHECK_FEATURE_TABLE = 
//                "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" 
//                    + FieldNames.TABLE_FEATURES + "' AND COLUMN_NAME = '"
//                    + FieldNames.FEATURE_PRODUCT + "' ";
//   
//        
//    
//    
//             public static final String ALTER_FEATURE_TABLE = 
//                "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_EC_NUM + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_GENE + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_LOCUS_TAG + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_PRODUCT + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_STRAND + "; ";
}
