package de.cebitec.readXplorer.databackend;

/**
 * Contains all field names for data base requests.
 *
 * @author ddoppmeier, rhilker
 */
public class FieldNames {
    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private FieldNames() {
    }

    // names for various database tables
    public static final String TABLE_PROJECT_FOLDER = "PROJECT_FOLDER";
    public final static String TABLE_REFERENCE = "REFERENCE";
    public final static String TABLE_CHROMOSOME = "CHROMOSOME";
    public final static String TABLE_DIFF = "DIFF";
    public final static String TABLE_COVERAGE = "COVERAGE";
    public final static String TABLE_FEATURES = "FEATURE";
    public final static String TABLE_MAPPING = "MAPPING";
    public final static String TABLE_TRACK = "TRACK";
    public static final String TABLE_SEQ_PAIRS = "SEQ_PAIRS";
    public static final String TABLE_SEQ_PAIR_PIVOT = "SEQ_PAIR_PIVOT";
    public static final String TABLE_SEQ_PAIR_REPLICATES = "SEQ_PAIR_REPLICATES";
    public final static String TABLE_POSITIONS = "POSITIONS";
    public final static String TABLE_STATISTICS = "STATISTICS";  
    public final static String TABLE_COUNT_DISTRIBUTION = "COUNT_DISTRIBUTION";
    public final static String TABLE_OBJECTCACHE = "OBJECT_CACHE"; 
    public final static String TABLE_DB_VERSION = "DB_VERSION"; 
    
    
    ////////////////////////  tables fields  //////////////////////////////// 
        
    // project folder field
//    public static final String PROJECT_FOLDER_PATH = "PATH";
    
    // position table fields
    public final static String POSITIONS_SNP_ID = "ID";
    public final static String POSITIONS_TRACK_ID = "TRACK_ID";
    public final static String POSITIONS_POSITION = "POSITION";
    public final static String POSITIONS_BASE = "BASE";
    public final static String POSITIONS_REFERENCE_BASE = "REFERENCE_BASE";
    public final static String POSITIONS_A = "A";
    public final static String POSITIONS_C = "C";
    public final static String POSITIONS_G = "G";
    public final static String POSITIONS_T = "T";
    public final static String POSITIONS_N = "N";
    public final static String POSITIONS_GAP = "_";
    public final static String POSITIONS_COVERAGE = "COVERAGE";
    public final static String POSITIONS_FREQUENCY = "FREQUENCY";
    public final static String POSITIONS_TYPE = "TYPE";
    
    // reference genome table fields
    public final static String REF_GEN_ID ="ID";
    public final static String REF_GEN_NAME = "NAME";
    public final static String REF_GEN_DESCRIPTION = "DESCRIPTION";
    public final static String REF_GEN_SEQUENCE = "SEQUENCE"; //still needed for old DBs...
    public final static String REF_GEN_TIMESTAMP = "CREATIONTIME";

    // chromosome table fields
    public final static String CHROM_ID = "ID";
    public final static String CHROM_NUMBER = "NUMBER";
    public final static String CHROM_REFERENCE_ID = "REFERENCE_ID";
    public final static String CHROM_NAME = "NAME";
    public final static String CHROM_LENGTH = "LENGTH";
    public final static String CHROM_SEQUENCE = "SEQUENCE";

    // diff table fields
    public final static String DIFF_ID = "ID";
    public final static String DIFF_MAPPING_ID = "MAPPING_ID";
    public final static String DIFF_POSITION = "POSITION";
    public final static String DIFF_BASE = "BASE";
    public final static String DIFF_TYPE = "TYPE";
    public final static String DIFF_GAP_ORDER = "GAP_ORDER";
    public final static String DIFF_SNP_ID = "SNP_ID";

    
    // objectcache table fields
    public final static String OBJECTCACHE_ID = "ID";
    public final static String OBJECTCACHE_FAMILY = "FAMILY";
    public final static String OBJECTCACHE_KEY = "KEY";
    public final static String OBJECTCACHE_DATA = "DATA";
    
    // coverage table fields
    public final static String COVERAGE_ID = "ID";
    public final static String COVERAGE_TRACK = "TRACK_ID";
    public final static String COVERAGE_POSITION = "POSITION";
    public final static String COVERAGE_BM_FW_MULT =    "BEST_MATCH_FORWARD_REDUNDANT";
    public final static String COVERAGE_BM_FW_NUM =     "BEST_MATCH_FORWARD_NON_REDUNDANT"; //NON_REDUNDANT is what you want!
    public final static String COVERAGE_BM_RV_MULT =    "BEST_MATCH_REVERSE_REDUNDANT"; //check if this is needed anyway
    public final static String COVERAGE_BM_RV_NUM =     "BEST_MATCH_REVERSE_NON_REDUNDANT";
    public final static String COVERAGE_ZERO_FW_MULT =  "PERFECT_MATCH_FORWARD_REDUNDANT";
    public final static String COVERAGE_ZERO_FW_NUM =   "PERFECT_MATCH_FORWARD_NON_REDUNDANT";
    public final static String COVERAGE_ZERO_RV_MULT =  "PERFECT_MATCH_REVERSE_REDUNDANT";
    public final static String COVERAGE_ZERO_RV_NUM =   "PERFECT_MATCH_REVERSE_NON_REDUNDANT";
    public final static String COVERAGE_N_FW_MULT =     "COMPLETE_FORWARD_REDUNDANT";
    public final static String COVERAGE_N_FW_NUM =      "COMPLETE_FORWARD_NON_REDUNDANT";
    public final static String COVERAGE_N_RV_MULT =     "COMPLETE_REVERSE_REDUNDANT";
    public final static String COVERAGE_N_RV_NUM =      "COMPLETE_REVERSE_NON_REDUNDANT";

    public final static String COVERAGE_N_FW_MULT_TRACK_1 =     "COMPLETE_FORWARD_REDUNDANT_TRACK_1";
    public final static String COVERAGE_N_FW_NUM_TRACK_1 =      "COMPLETE_FORWARD_NON_REDUNDANT_TRACK_1";
    public final static String COVERAGE_N_RV_MULT_TRACK_1 =     "COMPLETE_REVERSE_REDUNDANT_TRACK_1";
    public final static String COVERAGE_N_RV_NUM_TRACK_1 =      "COMPLETE_REVERSE_NON_REDUNDANT_TRACK_1";

    public final static String COVERAGE_N_FW_MULT_TRACK_2 =     "COMPLETE_FORWARD_REDUNDANT_TRACK_2";
    public final static String COVERAGE_N_FW_NUM_TRACK_2 =      "COMPLETE_FORWARD_NON_REDUNDANT_TRACK_2";
    public final static String COVERAGE_N_RV_MULT_TRACK_2 =     "COMPLETE_REVERSE_REDUNDANT_TRACK_2";
    public final static String COVERAGE_N_RV_NUM_TRACK_2 =      "COMPLETE_REVERSE_NON_REDUNDANT_TRACK_2";
    public final static String COVERAGE_N_MULT =                "COMPLETE_REDUNDANT_TRACK";
    public final static String COVERAGE_N_FW_MULT_TRACK_DIFF =     "COMPLETE_FORWARD_REDUNDANT_TRACK_DIFF";
    public final static String COVERAGE_N_FW_NUM_TRACK_DIFF =      "COMPLETE_FORWARD_NON_REDUNDANT_DIFF";
    public final static String COVERAGE_N_RV_MULT_TRACK_DIFF =     "COMPLETE_REVERSE_REDUNDANT__DIFF";
    public final static String COVERAGE_N_RV_NUM_TRACK_DIFF=      "COMPLETE_REVERSE_NON_REDUNDANT_DIFF";


    //all feature table fields
    public static final String FEATURE_ID = "ID";
    /** Dont use this in new DBs, it is not available anmymore and was replaced by FEATURE_CHROMOSOME_ID!*/
    public static final String FEATURE_REFGEN_ID = "REFERENCE_ID"; 
    public static final String FEATURE_CHROMOSOME_ID = "CHROMOSOME_ID";
    public static final String FEATURE_PARENT_IDS = "PARENT_IDS"; //should be 0, if no parent exists
    public static final String FEATURE_TYPE = "TYPE";
    public static final String FEATURE_START = "START";
    public static final String FEATURE_STOP = "STOP";
    public static final String FEATURE_LOCUS_TAG = "LOCUS_TAG";
    public static final String FEATURE_PRODUCT = "PRODUCT";
    public static final String FEATURE_EC_NUM = "EC_NUM";
    public static final String FEATURE_STRAND = "STRAND";
    public static final String FEATURE_GENE = "GENE";

    
    // mapping table fields
    public final static String MAPPING_ID = "ID";
    public final static String MAPPING_SEQUENCE_ID = "SEQUENCE_ID";
    public final static String MAPPING_START = "START";
    public final static String MAPPING_STOP = "STOP";
    public final static String MAPPING_NUM_OF_ERRORS = "NUM_OF_ERRORS";
    public final static String MAPPING_IS_BEST_MAPPING = "IS_BEST_MAPPING";
    public final static String MAPPING_DIRECTION = "DIRECTION";
    public final static String MAPPING_NUM_OF_REPLICATES = "NUM_OF_REPLICATES";
    public final static String MAPPING_TRACK = "TRACK_ID";

    // track table fields
    public final static String TRACK_ID = "ID";
    public final static String TRACK_REFERENCE_ID = "REFERENCE_ID";
    public static final String TRACK_READ_PAIR_ID = "SEQUENCE_PAIR_ID";
    public final static String TRACK_DESCRIPTION = "DESCRIPTION";
    public final static String TRACK_TIMESTAMP = "CREATIONTIME";
    public final static String TRACK_PATH = "PATH";
    
    //paired data table fields (mate pairs and paired end data)
    public static final String SEQ_PAIR_ID = "ID";
    public static final String SEQ_PAIR_PAIR_ID = "PAIR_ID"; //one pair can be seen at diff. positions
    public static final String SEQ_PAIR_MAPPING1_ID = "MAPPING1_ID";
    public static final String SEQ_PAIR_MAPPING2_ID = "MAPPING2_ID";
    public static final String SEQ_PAIR_TYPE = "TYPE";
    
    // paired data replicates table fields
    public final static String SEQ_PAIR_REPLICATE_PAIR_ID = "PAIR_ID";
    public static final String SEQ_PAIR_NUM_OF_REPLICATES = "NUM_OF_REPLICATES";
    
    //paired data to mapping connection table
    public static final String SEQ_PAIR_PIVOT_MAPPING_ID = "MAPPING_ID";
    public static final String SEQ_PAIR_PIVOT_SEQ_PAIR_ID = "SEQ_PAIR_ID";


    // statistics table fields
    public static final String STATISTICS_ID = "ID";
    public static final String STATISTICS_TRACK_ID = "TRACK_ID";
    public static final String STATISTICS_NUMBER_UNIQUE_MAPPINGS = "NUMBER_UNIQUE_MAPPINGS";
    public static final String STATISTICS_NUMBER_UNIQUE_BM_MAPPINGS = "NUMBER_UNIQUE_BM_MAPPINGS";
    public static final String STATISTICS_NUMBER_UNIQUE_PERFECT_MAPPINGS = "NUMBER_UNIQUE_PERFECT_MAPPINGS";
    public static final String STATISTICS_NUMBER_OF_UNIQUE_SEQ = "NUMBER_OF_UNIQUE_SEQ";
    public static final String STATISTICS_NUMBER_OF_REPEATED_SEQ = "NUMBER_OF_REPEATED_SEQ";
    public static final String STATISTICS_NUMBER_OF_MAPPINGS = "NUMBER_OF_MAPPINGS";
    public static final String STATISTICS_NUMBER_OF_PERFECT_MAPPINGS = "NUMBER_OF_PERFECT_MAPPINGS";
    public static final String STATISTICS_NUMBER_OF_BM_MAPPINGS = "NUMBER_OF_BM_MAPPINGS";
    public static final String STATISTICS_PERFECT_COVERAGE_OF_GENOME = "PERFECT_COVERAGE_OF_GENOME";
    public static final String STATISTICS_BM_COVERAGE_OF_GENOME = "BM_COVERAGE_OF_GENOME";
    public static final String STATISTICS_COMPLETE_COVERAGE_OF_GENOME = "COVERAGE_OF_GENOME";
    public static final String STATISTICS_NUMBER_READS = "NUMBER_OF_READS";
    public static final String STATISTICS_NUM_SEQUENCE_PAIRS = "NUM_SEQPAIRS";
    public static final String STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS = "NUM_UNIQUE_SEQPAIRS";
    public static final String STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS = "NUM_PERFECT_SEQPAIRS";
    public static final String STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS = "NUM_UNIQUE_PERFECT_SEQPAIRS";
    public static final String STATISTICS_NUM_SINGLE_MAPPINGS = "NUM_SINGLE_MAPPINGS";
    public static final String STATISTICS_NUM_SMALL_DIST_PAIRS = "NUM_SMALL_DIST_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_SMALL_PAIRS = "NUM_UNIQ_SMALL_PAIRS";
    public static final String STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS = "NUM_SMALL_ORIENT_WRONG_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS = "NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS";
    public static final String STATISTICS_NUM_ORIENT_WRONG_PAIRS = "NUM_ORIENT_WRONG_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS = "NUM_UNIQ_ORIENT_WRNG_PAIRS";
    public static final String STATISTICS_NUM_LARGE_DIST_PAIRS = "NUM_LARGE_DIST_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_LARGE_PAIRS = "NUM_UNIQ_LARGE_PAIRS";
    public static final String STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS = "NUM_LARGE_ORIENT_WRONG_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS = "NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS";
    public static final String STATISTICS_AVERAGE_READ_LENGTH = "AVERAGE_READ_LENGTH";
    public static final String STATISTICS_AVERAGE_SEQ_PAIR_LENGTH= "AVERAGE_SEQPAIR_LENGTH";
    
    
    // unique mappings = count all distinct mapping ids
    // unique sequences = num mapped seq = count all distinct seq ids
    // num mappings = count ALL mapping ids
    // num reads = extra calculation: count all reads during import process, also possible later
    
    public static final String COUNT_DISTRIBUTION_TRACK_ID = "TRACK_ID";
    public static final String COUNT_DISTRIBUTION_DISTRIBUTION_TYPE = "DISTRIBUTION_TYPE";
    public static final String COUNT_DISTRIBUTION_COV_INTERVAL_ID = "BIN_INDEX";
    public static final String COUNT_DISTRIBUTION_BIN_COUNT = "BIN_COUNT";
    
    public static final String DB_VERSION_DB_VERSION_NO = "DB_VERSION_NO";
}
