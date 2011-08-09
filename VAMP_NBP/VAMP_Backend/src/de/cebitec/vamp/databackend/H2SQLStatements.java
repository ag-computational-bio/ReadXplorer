package de.cebitec.vamp.databackend;

/**
 * Contains H2SQL statements needed for data base connection and fetching of data
 * especially for h2 data bases.
 *
 * @author jstraube, rhilker
 */
public class H2SQLStatements {

    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private H2SQLStatements() {
    }
    /**
     * All commands belonging to the RUN domain have been commented out,
     * because the run domain has been excluded from VAMP!!!!
     * This includes the Run, Unique_Sequence and Readname tables!
     */
    //////////////////  statements for table creation  /////////////////////////
    public final static String SETUP_SNPS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_SNP + " "
            + "("
            + FieldNames.SNP_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.SNP_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.SNP_COVERAGE + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.SNP_FREQUENCY + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.SNP_TYPE + " VARCHAR(1) NOT NULL"
            + ")";
    public final static String INDEX_SNP =
            "CREATE INDEX IF NOT EXISTS INDEXDIFF ON " + FieldNames.TABLE_SNP + "(" + FieldNames.SNP_ID + ", " + FieldNames.SNP_TRACK_ID + ") ";
    public final static String SETUP_REFERENCE_GENOME =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_REF_GEN
            + " ("
            + FieldNames.REF_GEN_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.REF_GEN_NAME + " VARCHAR(200) NOT NULL, "
            + FieldNames.REF_GEN_DESCRIPTION + " VARCHAR(200) NOT NULL,"
            + FieldNames.REF_GEN_SEQUENCE + " CLOB NOT NULL, "
            + FieldNames.REF_GEN_TIMESTAMP + " DATETIME NOT NULL"
            + ") ";
    public final static String SETUP_DIFFS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_DIFF
            + " ("
            + FieldNames.DIFF_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.DIFF_MAPPING_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.DIFF_CHAR + " VARCHAR (1) NOT NULL, "
            + FieldNames.DIFF_POSITION + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.DIFF_TYPE + " TINYINT UNSIGNED NOT NULL, "
            + FieldNames.DIFF_ORDER + " BIGINT UNSIGNED, "
            + FieldNames.DIFF_SNP_ID + " BIGINT UNSIGNED NOT NULL "
            + ") ";
    //in h2 you can ask if the index exists in mysql this did not work
    public final static String INDEX_DIFF =
            "CREATE INDEX IF NOT EXISTS INDEXDIFF ON " + FieldNames.TABLE_DIFF + "(" + FieldNames.DIFF_POSITION + ", " + FieldNames.DIFF_MAPPING_ID + ") ";
    public final static String SETUP_COVERAGE =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_COVERAGE
            + " ("
            + FieldNames.COVERAGE_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.COVERAGE_TRACK + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_POSITION + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_BM_FW_MULT + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_BM_FW_NUM + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_BM_RV_MULT + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_BM_RV_NUM + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_ZERO_FW_MULT + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_ZERO_FW_NUM + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_ZERO_RV_MULT + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_ZERO_RV_NUM + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_N_FW_MULT + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_N_FW_NUM + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_N_RV_MULT + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_N_RV_NUM + " MEDIUMINT UNSIGNED NOT NULL"
            + ") ";
    public final static String INDEX_COVERAGE =
            "CREATE INDEX IF NOT EXISTS INDEXCOVERAGE ON " + FieldNames.TABLE_COVERAGE + "(" + FieldNames.COVERAGE_POSITION + ", " + FieldNames.COVERAGE_TRACK + ") ";
    public final static String SETUP_FEATURES =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_FEATURES
            + " ("
            + FieldNames.FEATURE_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.FEATURE_REFGEN + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_TYPE + " TINYINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_START + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_STOP + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_LOCUS + " VARCHAR (1000), "
            + FieldNames.FEATURE_PRODUCT + " VARCHAR (2000), "
            + FieldNames.FEATURE_ECNUM + " VARCHAR (20), "
            + FieldNames.FEATURE_STRAND + " TINYINT NOT NULL "
            + ") ";
    public final static String INDEX_FEATURES =
            "CREATE INDEX IF NOT EXISTS INDEXFEATURES ON " + FieldNames.TABLE_FEATURES + " (" + FieldNames.FEATURE_REFGEN + ") ";
    public final static String SETUP_MAPPINGS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_MAPPINGS
            + " ("
            + FieldNames.MAPPING_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.MAPPING_SEQUENCE_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.MAPPING_TRACK + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.MAPPING_START + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.MAPPING_STOP + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.MAPPING_DIRECTION + " TINYINT NOT NULL, "
            + FieldNames.MAPPING_COUNT + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.MAPPING_NUM_OF_ERRORS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.MAPPING_BEST_MAPPING + " TINYINT UNSIGNED NOT NULL "
            + ") ";
    public final static String INDEX_MAPPINGS =
            "CREATE INDEX IF NOT EXISTS INDEXMAPPINGS ON " + FieldNames.TABLE_MAPPINGS + " "
            + "(" + FieldNames.MAPPING_START + ", " + FieldNames.MAPPING_STOP + "," + FieldNames.MAPPING_SEQUENCE_ID + " ) ";
    public final static String SETUP_TRACKS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_TRACKS
            + " ( "
            + FieldNames.TRACK_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.TRACK_REFERENCE_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.TRACK_SEQUENCE_PAIR_ID + " BIGINT UNSIGNED, " //only for paired sequences
            + FieldNames.TRACK_DESCRIPTION + " VARCHAR (200) NOT NULL, "
            + FieldNames.TRACK_TIMESTAMP + " DATETIME NOT NULL "//, "
            //+ FieldNames.TRACK_RUN + " BIGINT UNSIGNED NOT NULL "
            + ") ";
    public final static String INDEX_TRACKS =
            "CREATE INDEX IF NOT EXISTS INDEXTRACK ON " + FieldNames.TABLE_TRACKS + " ("
            + FieldNames.TRACK_REFERENCE_ID + ", " + FieldNames.TRACK_SEQUENCE_PAIR_ID + ") ";
    public static final String SETUP_SEQ_PAIRS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_SEQ_PAIRS
            + " ("
            + FieldNames.SEQ_PAIR_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.SEQ_PAIR_PAIR_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.SEQ_PAIR_MAPPING1_ID + " BIGINT UNSIGNED, "
            + FieldNames.SEQ_PAIR_MAPPING2_ID + " BIGINT UNSIGNED, "
            + FieldNames.SEQ_PAIR_TYPE + " TINYINT NOT NULL, "
            + ") ";
    public final static String INDEX_SEQ_PAIRS =
            "CREATE INDEX IF NOT EXISTS INDEXSEQ_PAIRS ON " + FieldNames.TABLE_SEQ_PAIRS
            + "(" + FieldNames.SEQ_PAIR_PAIR_ID + ", " + FieldNames.SEQ_PAIR_MAPPING1_ID + ", " + FieldNames.SEQ_PAIR_MAPPING2_ID + " ) ";
    public static final String SETUP_SEQ_PAIR_REPLICATES =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_SEQ_PAIR_REPLICATES
            + " ("
            + FieldNames.SEQ_PAIR_MAPPING_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES + " SMALLINT UNSIGNED NOT NULL, "
            + ") ";
    public final static String INDEX_SEQ_PAIR_REPLICATES =
            "CREATE INDEX IF NOT EXISTS INDEXSEQ_PAIR_REPLICATES ON " + FieldNames.TABLE_SEQ_PAIR_REPLICATES
            + "(" + FieldNames.SEQ_PAIR_MAPPING_ID + " ) ";
    public static final String SETUP_SEQ_PAIR_PIVOT =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_SEQ_PAIR_PIVOT + " "
            + "("
            + FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID + " BIGINT UNSIGNED NOT NULL "
            + ") ";
    public final static String INDEX_SEQ_PAIRS_PIVOT =
            "CREATE INDEX IF NOT EXISTS INDEXMAPPING_TO_SEQ_PAIRS ON " + FieldNames.TABLE_SEQ_PAIR_PIVOT + " "
            + "(" + FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID + ", " + FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID + " ) ";
//    public final static String SETUP_RUN =
//            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_RUN+" " +
//            "( " +
//            FieldNames.RUN_ID+" BIGINT PRIMARY KEY, " +
//            FieldNames.RUN_DESCRIPTION+" VARCHAR (100) NOT NULL, " +
//            FieldNames.RUN_TIMESTAMP+" DATETIME NOT NULL "+
//            ")";
//    public final static String SETUP_SEQUENCE =
//            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_SEQUENCE+" " +
//            "(" +
//            FieldNames.SEQUENCE_ID+" BIGINT PRIMARY KEY, " +
//            FieldNames.SEQUENCE_RUN+" BIGINT UNSIGNED NOT NULL " +
//            ") ";
//
//        public final static String INDEX_SEQUENCE=
//             "CREATE INDEX IF NOT EXISTS INDEXSEQUENCE ON "+FieldNames.TABLE_SEQUENCE+" ("+FieldNames.SEQUENCE_RUN+") ";
//    public final static String SETUP_READS =
//            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_READS+" " +
//            "( " +
//            FieldNames.READ_ID+" BIGINT PRIMARY KEY, " +
//            FieldNames.READ_NAME+" VARCHAR (100) NOT NULL, " +
//            FieldNames.READ_SEQUENCE+" BIGINT UNSIGNED NOT NULL " +
//            ")";
//
//            public final static String INDEX_READS=
//             "CREATE INDEX IF NOT EXISTS INDEXREADS ON "+FieldNames.TABLE_READS+" ("+FieldNames.READ_SEQUENCE+") ";
    //////////////////  statements for data insertion  /////////////////////////  

    /*     public final static String ADD_COLUMN_TO_TABLE_STATISTICS_NUMBER_OF_UNIQUE_SEQ =
    "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = "
    + FieldNames.TABLE_STATISTICS
    + " AND COLUMN_NAME =" + FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ + ")"
    + " BEGIN "
    + "ALTER TABLE "
    + FieldNames.TABLE_STATISTICS
    + " ADD COLUMN "
    + FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ + " BIGINT UNSIGNED "+
    " END";*/
    //////// statements to fetch data from database ////////////////////////////
}
