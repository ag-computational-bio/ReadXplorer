
package de.cebitec.vamp.databackend;

/**
 * This class contains the statements which are only used by the MySQL Database
 * 
 * @author jstraube, rhilker
 */
public class MySQLStatements {
    
    //////////////////  statements for table creation  /////////////////////////
    
    public final static String SETUP_REFERENCE_GENOME =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_REFERENCE+" " +
            "(" +
            FieldNames.REF_GEN_ID+" BIGINT PRIMARY KEY, " +
            FieldNames.REF_GEN_NAME+" VARCHAR(200) NOT NULL, " +
            FieldNames.REF_GEN_DESCRIPTION+" VARCHAR(200) NOT NULL," +
            FieldNames.REF_GEN_SEQUENCE+" LONGTEXT NOT NULL, " +
            FieldNames.REF_GEN_TIMESTAMP+" DATETIME NOT NULL" +
            ") ";

    public final static String SETUP_POSITIONS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_POSITIONS + " "
            + "("
            + FieldNames.POSITIONS_SNP_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.POSITIONS_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_POSITION + " VARCHAR(200) NOT NULL, "
            + FieldNames.POSITIONS_BASE + " VARCHAR(1) NOT NULL, "
            + FieldNames.POSITIONS_REFERENCE_BASE + " VARCHAR(1) NOT NULL, "
            + FieldNames.POSITIONS_A + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_C + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_G + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_T + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_N + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_GAP + " MEDIUMINT UNSIGNED, "
            + FieldNames.POSITIONS_COVERAGE + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_FREQUENCY + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.POSITIONS_TYPE + " VARCHAR(1) NOT NULL , "
            + "INDEX (" + FieldNames.POSITIONS_SNP_ID + "), "
            + "INDEX (" + FieldNames.POSITIONS_TRACK_ID + ") "
            + ")";
    
    public final static String SETUP_DIFFS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_DIFF+" " +
            "(" +
            FieldNames.DIFF_ID+" BIGINT PRIMARY KEY, "+
            FieldNames.DIFF_MAPPING_ID+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.DIFF_BASE+ " VARCHAR (1) NOT NULL, "+
            FieldNames.DIFF_POSITION+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.DIFF_TYPE+" TINYINT UNSIGNED NOT NULL, " +
            FieldNames.DIFF_GAP_ORDER+" BIGINT UNSIGNED , " +
            "INDEX ("+FieldNames.DIFF_POSITION+"), " +
            "INDEX ("+FieldNames.DIFF_MAPPING_ID+") " +
            ") ";

    
    public final static String SETUP_COVERAGE =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_COVERAGE+" "+
            "(" +
            FieldNames.COVERAGE_ID+" BIGINT PRIMARY KEY, " +
            FieldNames.COVERAGE_TRACK+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.COVERAGE_POSITION+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_BM_FW_MULT+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_BM_FW_NUM +" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_BM_RV_MULT+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_BM_RV_NUM+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_ZERO_FW_MULT+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_ZERO_FW_NUM+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_ZERO_RV_MULT+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_ZERO_RV_NUM+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_N_FW_MULT+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_N_FW_NUM+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_N_RV_MULT+" MEDIUMINT UNSIGNED NOT NULL, " +
            FieldNames.COVERAGE_N_RV_NUM+" MEDIUMINT UNSIGNED NOT NULL," +
            "INDEX ("+FieldNames.COVERAGE_POSITION+", "+FieldNames.COVERAGE_TRACK+") " +
            ") ";

    
       public final static String SETUP_FEATURES =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_FEATURES +
            " (" +
            FieldNames.FEATURE_ID+" BIGINT PRIMARY KEY, " +
            FieldNames.FEATURE_REFGEN_ID+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.FEATURE_PARENT_IDS + " VARCHAR (1000) NOT NULL, " +
            FieldNames.FEATURE_TYPE+" TINYINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_START+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_STOP+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_LOCUS_TAG+" VARCHAR(1000) , " +
            FieldNames.FEATURE_PRODUCT+" VARCHAR(1000), " +
            FieldNames.FEATURE_EC_NUM+" VARCHAR (20), " +
            FieldNames.FEATURE_STRAND+" TINYINT NOT NULL, " +
            FieldNames.FEATURE_GENE+" VARCHAR (20), " +
            "INDEX ("+FieldNames.FEATURE_REFGEN_ID+") " +
            ") ";

    
    public final static String SETUP_MAPPINGS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_MAPPING+" " +
            "(" +
            FieldNames.MAPPING_ID+" BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.MAPPING_SEQUENCE_ID+ " BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_TRACK+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_START+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_STOP+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_DIRECTION+" TINYINT NOT NULL, " +
            FieldNames.MAPPING_NUM_OF_REPLICATES+" SMALLINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_NUM_OF_ERRORS+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_IS_BEST_MAPPING+" TINYINT UNSIGNED NOT NULL, " +
            " INDEX ("+FieldNames.MAPPING_START+"), " +
            " INDEX ("+FieldNames.MAPPING_STOP+"), " +
            " INDEX ("+FieldNames.MAPPING_SEQUENCE_ID+") " +
            ") ";

    
    public static final String SETUP_TRACKS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_TRACK+" " +
            "( " +
            FieldNames.TRACK_ID+ " BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.TRACK_REFERENCE_ID+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.TRACK_SEQUENCE_PAIR_ID+" BIGINT UNSIGNED, " + //only for paired sequences
            FieldNames.TRACK_DESCRIPTION+" VARCHAR (1000) NOT NULL, " +
            FieldNames.TRACK_TIMESTAMP+" DATETIME NOT NULL, " +
            //FieldNames.TRACK_RUN+" BIGINT UNSIGNED NOT NULL, "+
            "INDEX ("+FieldNames.TRACK_REFERENCE_ID+") " +
            ") ";
    
    public static final String SETUP_SEQ_PAIRS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_SEQ_PAIRS+" " +
            "(" +
            FieldNames.SEQ_PAIR_ID+" BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.SEQ_PAIR_PAIR_ID+ " BIGINT UNSIGNED NOT NULL, "+
            FieldNames.SEQ_PAIR_MAPPING1_ID+" BIGINT UNSIGNED NOT NULL,"+
            FieldNames.SEQ_PAIR_MAPPING2_ID+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.SEQ_PAIR_TYPE+" TINYINT NOT NULL, " +
            " INDEX ("+FieldNames.SEQ_PAIR_PAIR_ID+"), " + 
            " INDEX ("+FieldNames.SEQ_PAIR_MAPPING1_ID+"), " +
            " INDEX ("+FieldNames.SEQ_PAIR_MAPPING2_ID+") " +
            ") ";
    
    public static final String SETUP_SEQ_PAIR_REPLICATES = 
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_SEQ_PAIR_REPLICATES+" " +
            "(" +
            FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID+" BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.SEQ_PAIR_NUM_OF_REPLICATES+" SMALLINT UNSIGNED NOT NULL, " +
            " INDEX ("+FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID+") " + 
            ") ";
    
    
    public static final String SETUP_SEQ_PAIR_PIVOT = 
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_SEQ_PAIR_PIVOT+" " +
            "(" +
            FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID+ " BIGINT UNSIGNED NOT NULL, "+
            " INDEX ("+FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID+"), " + 
            " INDEX ("+FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID+") " +
            ") ";

//    public final static String SETUP_RUN =
//            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_RUN+" " +
//            "( " +
//            FieldNames.RUN_ID+" BIGINT PRIMARY KEY, " +
//            FieldNames.RUN_DESCRIPTION+" VARCHAR (100) NOT NULL, " +
//            FieldNames.RUN_TIMESTAMP+" DATETIME NOT NULL"+
//            ")";

//    public final static String SETUP_SEQUENCE =
//            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_SEQUENCE+" " +
//            "(" +
//            FieldNames.SEQUENCE_ID+" BIGINT PRIMARY KEY, " +
//            FieldNames.SEQUENCE_RUN+" BIGINT UNSIGNED NOT NULL, " +
//            "INDEX ("+FieldNames.SEQUENCE_RUN+") " +
//            ") ";

//    public final static String SETUP_READS =
//            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_READS+" " +
//            "( " +
//            FieldNames.READ_ID+" BIGINT PRIMARY KEY, " +
//            FieldNames.READ_NAME+" VARCHAR (100) NOT NULL, " +
//            FieldNames.READ_SEQUENCE+" BIGINT UNSIGNED NOT NULL, " +
//            "INDEX ("+FieldNames.READ_SEQUENCE+") " +
//            ")";
    
    // Removes a constraint or a primary key from a table. This command commits an open and faster transaction.
    // Enable KEYS and DISABLE KEYS are functions only use for mysql not for h2
    // for an open transaction in h2 use the methode connectH2DataBaseforImport

    public final static String DISABLE_COVERAGE_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_COVERAGE+" DISABLE KEYS";

    public final static String ENABLE_COVERAGE_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_COVERAGE+" ENABLE KEYS";

    public final static String DISABLE_MAPPING_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_MAPPING+" DISABLE KEYS";

    public final static String ENABLE_MAPPING_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_MAPPING+" ENABLE KEYS";

    public final static String DISABLE_DIFF_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_DIFF+" DISABLE KEYS";

    public final static String ENABLE_DIFF_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_DIFF+" ENABLE KEYS";

    public final static String DISABLE_TRACK_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_TRACK+" DISABLE KEYS";

    public final static String ENABLE_TRACK_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_TRACK+" ENABLE KEYS";
    
    public final static String DISABLE_SEQUENCE_PAIR_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_SEQ_PAIRS+" DISABLE KEYS";

    public final static String ENABLE_SEQUENCE_PAIR_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_SEQ_PAIRS+" ENABLE KEYS";

//    public final static String DISABLE_RUN_INDICES =
//            "ALTER TABLE "+FieldNames.TABLE_RUN+" DISABLE KEYS";
//
//    public final static String ENABLE_RUN_INDICES =
//            "ALTER TABLE "+FieldNames.TABLE_RUN+" ENABLE KEYS";

//    public final static String DISABLE_SEQUENCE_INDICES =
//            "ALTER TABLE "+FieldNames.TABLE_SEQUENCE+" DISABLE KEYS";
//
//    public final static String ENABLE_SEQUENCE_INDICES =
//            "ALTER TABLE "+FieldNames.TABLE_SEQUENCE+" ENABLE KEYS";

//    public final static String DISABLE_READNAMES_INDICES =
//            "ALTER TABLE "+FieldNames.TABLE_READS+" DISABLE KEYS";
//
//    public final static String ENABLE_READNAMES_INDICES =
//            "ALTER TABLE "+FieldNames.TABLE_READS+" ENABLE KEYS";

    
    public final static String ENABLE_REFERENCE_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_REFERENCE+" ENABLE KEYS";

    
    public final static String DISABLE_REFERENCE_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_REFERENCE+" DISABLE KEYS";

    
    public final static String ENABLE_FEATURE_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_FEATURES+" ENABLE KEYS";

    
    public final static String DISABLE_FEATURE_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_FEATURES+" DISABLE KEYS";

    
    public final static String UNLOCK_TABLES =
            "UNLOCK TABLES";

//    public final static String LOCK_TABLE_RUN_DOMAIN =
//            "LOCK TABLE " +
//            FieldNames.TABLE_RUN+" WRITE, " +
//            FieldNames.TABLE_SEQUENCE+" WRITE, " +
//            FieldNames.TABLE_READS+" WRITE";

    
    public final static String LOCK_TABLE_REFERENCE_DOMAIN =
            "LOCK TABLE " +
            FieldNames.TABLE_REFERENCE+ " WRITE, " +
            FieldNames.TABLE_FEATURES+ " WRITE,";

    
    public final static String LOCK_TABLE_TRACK_DOMAIN =
            "LOCK TABLE "
            + FieldNames.TABLE_COVERAGE + " WRITE, "
            + FieldNames.TABLE_TRACK + " WRITE, "
            + FieldNames.TABLE_MAPPING + " WRITE, "
            + FieldNames.TABLE_STATISTICS + " WRITE, "
            + FieldNames.TABLE_POSITIONS + " WRITE, "
            + FieldNames.TABLE_DIFF + " WRITE ";
    
    
    public static final String LOCK_TABLE_SEQUENCE_PAIRS_DOMAIN = 
            "LOCK TABLE " + 
            FieldNames.TABLE_SEQ_PAIRS + " WRITE, " +
            FieldNames.TABLE_SEQ_PAIR_REPLICATES + " WRITE, " + 
            FieldNames.TABLE_SEQ_PAIR_PIVOT + " WRITE ";
    
    public static final String LOCK_TABLES_ALL =
            "LOCK TABLE "
            + FieldNames.TABLE_REFERENCE+ " WRITE, " 
            + FieldNames.TABLE_FEATURES+ " WRITE,"
            + FieldNames.TABLE_COVERAGE + " WRITE, "
            + FieldNames.TABLE_TRACK + " WRITE, "
            + FieldNames.TABLE_MAPPING + " WRITE, "
            + FieldNames.TABLE_STATISTICS + " WRITE, "
            + FieldNames.TABLE_POSITIONS + " WRITE, "
            + FieldNames.TABLE_DIFF + " WRITE, "
            + FieldNames.TABLE_SEQ_PAIRS + " WRITE, " +
            FieldNames.TABLE_SEQ_PAIR_REPLICATES + " WRITE, " + 
            FieldNames.TABLE_SEQ_PAIR_PIVOT + " WRITE ";

    public final static String FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK =
            "SELECT " +
                "M."+FieldNames.MAPPING_ID+", "+
                "M."+FieldNames.MAPPING_IS_BEST_MAPPING+", "+
                "M."+FieldNames.MAPPING_NUM_OF_REPLICATES+", "+
                "M."+FieldNames.MAPPING_DIRECTION+", "+
                "M."+FieldNames.MAPPING_NUM_OF_ERRORS+", "+
                "M."+FieldNames.MAPPING_SEQUENCE_ID+", "+
                "M."+FieldNames.MAPPING_START+", "+
                "M."+FieldNames.MAPPING_STOP+", "+
                "M."+FieldNames.MAPPING_TRACK+", "+
                "D."+FieldNames.DIFF_BASE+", "+
                "D."+FieldNames.DIFF_GAP_ORDER+", "+
                "D."+FieldNames.DIFF_POSITION+", "+
                "D."+FieldNames.DIFF_TYPE+" "+
            "FROM " +
                "(" +
                "SELECT " +
                    FieldNames.MAPPING_ID+", "+
                    FieldNames.MAPPING_IS_BEST_MAPPING+", "+
                    FieldNames.MAPPING_NUM_OF_REPLICATES+", "+
                    FieldNames.MAPPING_DIRECTION+", "+
                    FieldNames.MAPPING_NUM_OF_ERRORS+", "+
                    FieldNames.MAPPING_SEQUENCE_ID+", "+
                    FieldNames.MAPPING_START+", "+
                    FieldNames.MAPPING_STOP+", "+
                    FieldNames.MAPPING_TRACK+" "+
                "FROM "+
                    FieldNames.TABLE_MAPPING +" "+
                "WHERE " +
                    FieldNames.MAPPING_TRACK+" = ? and  " +
                    FieldNames.MAPPING_STOP+" >= ? and " +
                    FieldNames.MAPPING_START+" <= ? " +
                ") AS M " +
            "LEFT JOIN " +
                FieldNames.TABLE_DIFF+" AS D " +
            "on " +
                "M."+FieldNames.MAPPING_ID+" = D."+FieldNames.DIFF_MAPPING_ID;


    public final static String FETCH_SNP_DATA_FOR_TRACK =
            "SELECT A."+FieldNames.DIFF_POSITION+", " +
                    "A."+FieldNames.DIFF_BASE+", " +
                    "A."+FieldNames.MAPPING_DIRECTION+", " +
                    "A."+FieldNames.DIFF_TYPE+", " +
                    "A.mult_count, " +
                    "C."+FieldNames.COVERAGE_BM_FW_MULT+", " +
                    "C."+FieldNames.COVERAGE_BM_RV_MULT+" " +
            "FROM "+
		"(SELECT " +
                    FieldNames.DIFF_POSITION+", "+
                    FieldNames.DIFF_BASE+", "+
                    FieldNames.DIFF_TYPE+", "+
                    FieldNames.MAPPING_DIRECTION+", " +
                    "SUM("+FieldNames.MAPPING_NUM_OF_REPLICATES+") as mult_count  "+
		"FROM "+
                    FieldNames.TABLE_MAPPING+" M " +
                    "left join "+FieldNames.TABLE_DIFF+" D " +
                    "on D."+FieldNames.DIFF_MAPPING_ID+" = M."+FieldNames.MAPPING_ID+" " +
		"WHERE " +
                    "M."+FieldNames.MAPPING_TRACK+" = ? and M."+FieldNames.MAPPING_IS_BEST_MAPPING+" = 1 "+
		"GROUP BY " +
                    "D."+FieldNames.DIFF_POSITION+", "+
                    "D."+FieldNames.DIFF_BASE+", " +
                    "M."+FieldNames.MAPPING_DIRECTION+" ,"+
                    "D."+FieldNames.DIFF_TYPE+"" +
                ") as A , "+
		FieldNames.TABLE_COVERAGE+" C "+
            "WHERE " +
                "C."+FieldNames.COVERAGE_TRACK+" = ? AND " +
                "C."+FieldNames.COVERAGE_POSITION+" = A."+FieldNames.DIFF_POSITION;
    
    //public final static String FETCH_SNPS_FROM_SNP_TABLE =
    //        "SELECT "
    
        public static final String SETUP_COVERAGE_DISTRIBUTION = 
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_COVERAGE_DISTRIBUTION + " ( "
            + FieldNames.COVERAGE_DISTRIBUTION_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_DISTRIBUTION_DISTRIBUTION_TYPE + " TINYINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_DISTRIBUTION_COV_INTERVAL_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.COVERAGE_DISTRIBUTION_INC_COUNT + " BIGINT UNSIGNED NOT NULL,"
            + " INDEX (" + FieldNames.COVERAGE_DISTRIBUTION_TRACK_ID + ")) ";
        
        
        /** Fetches all sequence pair data including replicates for the given interval. */
        public static final String FETCH_SEQ_PAIRS_W_REPLICATES_FOR_INTERVAL =
                 "SELECT " +
                    "MAPPING . ID  AS MAPPING_ID, " +
                    "IS_BEST_MAPPING , " +
                    "MAPPING . NUM_OF_REPLICATES  AS MAPPING_REP, " +
                    "NUM_OF_ERRORS , " +
                    "DIRECTION , " +
                    "SEQUENCE_ID , " +
                    "START , " +
                    "STOP , " +
                    "track_id, " +
                    "SEQ_PAIRS . PAIR_ID AS ORIG_PAIR_ID, " +
                    "MAPPING1_ID , " +
                    "MAPPING2_ID , " +
                    "TYPE,  " +
                    FieldNames.TABLE_SEQ_PAIR_REPLICATES + "." + FieldNames.SEQ_PAIR_NUM_OF_REPLICATES +
                " FROM " +
                    "MAPPING  , " +
                    "SEQ_PAIRS " +
                "LEFT OUTER JOIN " +
                    "SEQ_PAIR_REPLICATES " +
                "ON " +
                    "SEQ_PAIRS . PAIR_ID =  SEQ_PAIR_REPLICATES.PAIR_ID " +
                 "WHERE " +
                    "START   BETWEEN ? AND ? AND " +
                    "STOP  BETWEEN ? AND ? AND " +
                     "(  track_id = ? OR  track_id = ?) AND " +
                    "(MAPPING1_ID  =  MAPPING . ID or MAPPING2_ID  =  MAPPING . ID)";
        
        //         "SELECT "
//                    + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID + " AS MAPPING_ID, "
//                    + FieldNames.MAPPING_IS_BEST_MAPPING + ", "
//                    + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_NUM_OF_REPLICATES + " AS MAPPING_REP, "
//                    + FieldNames.MAPPING_NUM_OF_ERRORS + ", "
//                    + FieldNames.MAPPING_DIRECTION + ", "
//                    + FieldNames.MAPPING_SEQUENCE_ID + ", "
//                    + FieldNames.MAPPING_START + ", "
//                    + FieldNames.MAPPING_STOP + ", "
//                    + FieldNames.MAPPING_TRACK + ", "
//                    + FieldNames.TABLE_SEQ_PAIRS + "." + FieldNames.SEQ_PAIR_PAIR_ID + " AS ORIG_PAIR_ID, "
//                    + FieldNames.SEQ_PAIR_MAPPING1_ID + ", "
//                    + FieldNames.SEQ_PAIR_MAPPING2_ID + ", "
//                    + FieldNames.SEQ_PAIR_TYPE + " "
//                + "FROM "
//                    + FieldNames.TABLE_MAPPING + " , "
//                    + FieldNames.TABLE_SEQ_PAIRS + " "
//                + " WHERE "
//                    + FieldNames.MAPPING_START + "  BETWEEN ? AND ? AND "
//                    + FieldNames.MAPPING_STOP + " BETWEEN ? AND ? AND "
//                    + " ( " + FieldNames.MAPPING_TRACK + " = ? OR " + FieldNames.MAPPING_TRACK + " = ?) AND "
//                    + FieldNames.SEQ_PAIR_MAPPING1_ID + " = " + FieldNames.TABLE_MAPPING + "." + FieldNames.MAPPING_ID
//            + " LEFT OUTER JOIN "
//                + FieldNames.TABLE_SEQ_PAIR_REPLICATES
//            + " ON "
//                + " ORIG_PAIR_ID = " + FieldNames.SEQ_PAIR_REPLICATE_PAIR_ID;
                 
}
