package de.cebitec.vamp.databackend;

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
    public final static String TABLE_REF_GEN = "REFERENCE";
    public final static String TABLE_DIFF = "DIFF";
    public final static String TABLE_COVERAGE = "COVERAGE";
    public final static String TABLE_FEATURES = "FEATURE";
    public static final String TABLE_SUBFEATURES = "SUBFEATURES";
    public final static String TABLE_MAPPINGS = "MAPPING";
    public final static String TABLE_TRACKS = "TRACK";
    public static final String TABLE_SEQ_PAIRS = "SEQ_PAIRS";
    public static final String TABLE_SEQ_PAIR_PIVOT = "SEQ_PAIR_PIVOT";
    public static final String TABLE_SEQ_PAIR_REPLICATES = "SEQ_PAIR_REPLICATES";
    public final static String TABLE_POSITIONS = "POSITIONS";
    public final static String TABLE_STATISTICS = "STATISTICS";  
    public final static String TABLE_COVERAGE_DISTRIBUTION = "COVERAGE_DISTRIBUTION";
   
    
    
    ////////////////////////  tables fields  //////////////////////////////// 
        
    // position table fields
    public final static String POSITIONS_SNP_ID = "ID";
    public final static String POSITIONS_TRACK_ID = "TRACK_ID";
    public final static String POSITIONS_POSITION = "POSITION";
    public final static String POSITIONS_BASE = "BASE";
    public final static String POSITIONS_REF_BASE = "REFERENCE_BASE";
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
    public final static String REF_GEN_SEQUENCE = "SEQUENCE";
    public final static String REF_GEN_TIMESTAMP = "CREATIONTIME";

    // diff table fields
    public final static String DIFF_ID = "ID";
    public final static String DIFF_MAPPING_ID = "MAPPING_ID";
    public final static String DIFF_POSITION = "POSITION";
    public final static String DIFF_BASE = "BASE";
    public final static String DIFF_TYPE = "TYPE";
    public final static String DIFF_ORDER = "GAP_ORDER";
    public final static String DIFF_SNP_ID = "SNP_ID";

    
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


    //all annotation table fields (table still called Feature table)
    public final static String ANNOTATION_ID = "ID";
    public final static String ANNOTATION_REFGEN_ID = "REFERENCE_ID";
    public final static String ANNOTATION_TYPE = "TYPE";
    public final static String ANNOTATION_START = "START";
    public final static String ANNOTATION_STOP = "STOP";
    public final static String ANNOTATION_LOCUS_TAG = "LOCUS_TAG";
    public final static String ANNOTATION_PRODUCT = "PRODUCT";
    public final static String ANNOTATION_EC_NUM = "EC_NUM";
    public final static String ANNOTATION_STRAND = "STRAND";
    public static final String ANNOTATION_GENE = "GENE";
    
    
    //all sub annotation table fields (table still called Subfeature table)
    public final static String SUBANNOTATION_PARENT_ID = "PARENT_ID";
    public final static String SUBANNOTATION_REFERENCE_ID = "REFERENCE_ID";
    public final static String SUBANNOTATION_TYPE = "TYPE";
    public final static String SUBANNOTATION_START = "START";
    public final static String SUBANNOTATION_STOP = "STOP";

    
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
    public static final String TRACK_SEQUENCE_PAIR_ID = "SEQUENCE_PAIR_ID";
    public final static String TRACK_DESCRIPTION = "DESCRIPTION";
    public final static String TRACK_TIMESTAMP = "CREATIONTIME";
    
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
    public final static String STATISTICS_ID = "ID";
    public final static String STATISTICS_TRACK_ID = "TRACK_ID";
    public final static String STATISTICS_NUMBER_UNIQUE_MAPPINGS = "NUMBER_UNIQUE_MAPPINGS";
    public final static String STATISTICS_NUMBER_OF_UNIQUE_SEQ = "NUMBER_OF_UNIQUE_SEQ";
    public final static String STATISTICS_NUMBER_OF_MAPPINGS = "NUMBER_OF_MAPPINGS";
//    public final static String STATISTICS_NUMBER_OF_MAPPED_SEQ = "NUMBER_OF_MAPPED_SEQ";
    public final static String STATISTICS_NUMBER_OF_PERFECT_MAPPINGS = "NUMBER_OF_PERFECT_MAPPINGS";
    public final static String STATISTICS_NUMBER_OF_BM_MAPPINGS = "NUMBER_OF_BM_MAPPINGS";
    public final static String STATISTICS_PERFECT_COVERAGE_OF_GENOME = "PERFECT_COVERAGE_OF_GENOME";
    public final static String STATISTICS_BM_COVERAGE_OF_GENOME = "BM_COVERAGE_OF_GENOME";
    public final static String STATISTICS_COMPLETE_COVERAGE_OF_GENOME = "COVERAGE_OF_GENOME";
    public final static String STATISTICS_NUMBER_READS = "NUMBER_OF_READS";
    public final static String STATISTICS_NUM_SEQUENCE_PAIRS = "NUM_SEQPAIRS";
    public final static String STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS = "NUM_PERFECT_SEQPAIRS";
    public final static String STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS = "NUM_UNIQUE_SEQPAIRS";
    public final static String STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS = "NUM_UNIQUE_PERFECT_SEQPAIRS";
    public static final String STATISTICS_NUM_SINGLE_MAPPINGS = "NUM_SINGLE_MAPPINGS";
    public static final String STATISTICS_AVERAGE_READ_LENGTH = "AVERAGE_READ_LENGTH";
    public static final String STATISTICS_AVERAGE_SEQ_PAIR_LENGTH= "AVERAGE_SEQPAIR_LENGTH";
    
    // unique mappings = count all distinct mapping ids
    // unique sequences = num mapped seq = count all distinct seq ids
    // num mappings = count ALL mapping ids
    // num reads = extra calculation: count all reads during import process, also possible later
    
    public static final String COVERAGE_DISTRIBUTION_TRACK_ID = "TRACK_ID";
    public static final String COVERAGE_DISTRIBUTION_DISTRIBUTION_TYPE = "DISTRIBUTION_TYPE";
    public static final String COVERAGE_DISTRIBUTION_COV_INTERVAL_ID = "COV_INTERVAL_ID";
    public static final String COVERAGE_DISTRIBUTION_INC_COUNT = "INC_COUNT";
    
}
