package de.cebitec.vamp.databackend;

/**
 *
 * @author ddoppmeier
 */
public class FieldNames {

    // names for various database tables
    public final static String TABLE_REF_GEN = "REFERENCE";
    public final static String TABLE_DIFF = "DIFF";
    public final static String TABLE_SEQUENCE = "UNIQUE_SEQUENCE";
    public final static String TABLE_COVERAGE = "COVERAGE";
    public final static String TABLE_FEATURES = "FEATURE";
    public final static String TABLE_MAPPINGS = "MAPPING";
    public final static String TABLE_TRACKS = "TRACK";
    public final static String TABLE_RUN = "RUN";
    public final static String TABLE_READS = "READNAME";
    public final static String TABLE_STATICS = "STATICS";


    ////////////////////////  fields of tables  ////////////////////////////////

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
    public final static String DIFF_CHAR = "BASE";
    public final static String DIFF_TYPE = "TYPE";
    public final static String DIFF_ORDER = "GAP_ORDER";

    // source table fields
    public final static String SEQUENCE_ID = "ID";
    public final static String SEQUENCE_RUN = "RUN_ID";

    // coverage table fields
    public final static String COVERAGE_ID = "ID";
    public final static String COVERAGE_TRACK = "TRACK_ID";
    public final static String COVERAGE_POSITION = "POSITION";
    public final static String COVERAGE_BM_FW_MULT =    "BEST_MATCH_FORWARD_REDUNDANT";
    public final static String COVERAGE_BM_FW_NUM =     "BEST_MATCH_FORWARD_NON_REDUNDANT";
    public final static String COVERAGE_BM_RV_MULT =    "BEST_MATCH_REVERSE_REDUNDANT";
    public final static String COVERAGE_BM_RV_NUM =     "BEST_MATCH_REVERSE_NON_REDUNDANT";
    public final static String COVERAGE_ZERO_FW_MULT =  "PERFECT_MATCH_FORWARD_REDUNDANT";
    public final static String COVERAGE_ZERO_FW_NUM =   "PERFECT_MATCH_FORWARD_NON_REDUNDANT";
    public final static String COVERAGE_ZERO_RV_MULT =  "PERFECT_MATCH_REVERSE_REDUNDANT";
    public final static String COVERAGE_ZERO_RV_NUM =   "PERFECT_MATCH_REVERSE_NON_REDUNDANT";
    public final static String COVERAGE_N_FW_MULT =     "COMPLETE_FORWARD_REDUNDANT";
    public final static String COVERAGE_N_FW_NUM =      "COMPLETE_FORWARD_NON_REDUNDANT";
    public final static String COVERAGE_N_RV_MULT =     "COMPLETE_REVERSE_REDUNDANT";
    public final static String COVERAGE_N_RV_NUM =      "COMPLETE_REVERSE_NON_REDUNDANT";

    // feature table fields
    public final static String FEATURE_ID = "ID";
    public final static String FEATURE_REFGEN = "REFERENCE_ID";
    public final static String FEATURE_TYPE = "TYPE";
    public final static String FEATURE_START = "START";
    public final static String FEATURE_STOP = "STOP";
    public final static String FEATURE_LOCUS = "LOCUS_TAG";
    public final static String FEATURE_PRODUCT = "PRODUCT";
    public final static String FEATURE_ECNUM = "EC_NUM";
    public final static String FEATURE_STRAND = "STRAND";

    // mapping table fields
    public final static String MAPPING_ID = "ID";
    public final static String MAPPING_SEQUENCE = "SEQUENCE_ID";
    public final static String MAPPING_START = "START";
    public final static String MAPPING_STOP = "STOP";
    public final static String MAPPING_NUM_OF_ERRORS = "NUM_OF_ERRORS";
    public final static String MAPPING_BEST_MAPPING = "IS_BEST_MAPPING";
    public final static String MAPPING_DIRECTION = "DIRECTION";
    public final static String MAPPING_COUNT = "NUM_OF_REPLICATES";
    public final static String MAPPING_TRACK = "TRACK_ID";

    // track table fields
    public final static String TRACK_ID = "ID";
    public final static String TRACK_REFGEN = "REFERENCE_ID";
    public final static String TRACK_DESCRIPTION = "DESCRIPTION";
    public final static String TRACK_TIMESTAMP = "CREATIONTIME";
    public final static String TRACK_RUN = "RUN_ID";

    
    // run table fields
    public final static String RUN_ID = "ID";
    public final static String RUN_DESCRIPTION = "DESCRIPTION";
    public final static String RUN_TIMESTAMP = "CREATIONTIME";

    // read table fields
    public final static String READ_ID = "ID";
    public final static String READ_NAME = "NAME";
    public final static String READ_SEQUENCE = "SEQUENCE_ID";

   // statics table fields
    public final static String STATICS_ID = "ID";
    public final static String STATICS_TRACK = "TRACK_ID";
    public final static String STATICS_NUMBER_OF_READS = "NUMBER_OF_READS";
    public final static String STATICS_NUMBER_OF_UNIQUE_SEQ = "NUMBER_OF_UNIQUE_SEQ";
    public final static String STATICS_NUMBER_OF_MAPPINGS = "NUMBER_OF_MAPPINGS";
    public final static String STATICS_NUMBER_OF_MAPPED_SEQ = "NUMBER_OF_MAPPED_SEQ";
    public final static String STATICS_NUMBER_OF_PERFECT_MAPPINGS = "NUMBER_OF_PERFECT_MAPPINGS";
    public final static String STATICS_NUMBER_OF_BM_MAPPINGS = "NUMBER_OF_BM_MAPPINGS";
    public final static String STATICS_PERFECT_COVERAGE_OF_GENOME = "PERFECT_COVERAGE_OF_GENOME";
    public final static String STATICS_BM_COVERAGE_OF_GENOME = "BM_COVERAGE_OF_GENOME";
    public final static String STATICS_COMPLETE_COVERAGE_OF_GENOME = "COVERAGE_OF_GENOME";
}
