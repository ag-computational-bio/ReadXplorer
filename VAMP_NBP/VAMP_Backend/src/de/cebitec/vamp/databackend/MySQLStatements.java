
package de.cebitec.vamp.databackend;

/**
 *
 * @author jstraube, rhilker
 */
/*
 * This class contains the statements which are only used by the MySQL Database
 */
public class MySQLStatements {
    
    //////////////////  statements for table creation  /////////////////////////
    
    public final static String SETUP_REFERENCE_GENOME =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_REF_GEN+" " +
            "(" +
            FieldNames.REF_GEN_ID+" BIGINT PRIMARY KEY, " +
            FieldNames.REF_GEN_NAME+" VARCHAR(200) NOT NULL, " +
            FieldNames.REF_GEN_DESCRIPTION+" VARCHAR(200) NOT NULL," +
            FieldNames.REF_GEN_SEQUENCE+" LONGTEXT NOT NULL, " +
            FieldNames.REF_GEN_TIMESTAMP+" DATETIME NOT NULL" +
            ") ";

    public final static String SETUP_SNPS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_SNP + " "
            + "("
            + FieldNames.SNP_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.SNP_COVERAGE + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.SNP_FREQUENCY + " MEDIUMINT UNSIGNED NOT NULL, "
            + FieldNames.SNP_TYPE + " VARCHAR(1) NOT NULL , "
            + "INDEX (" + FieldNames.SNP_ID + "), "
            + "INDEX (" + FieldNames.SNP_TRACK_ID + ") "
            + ")";
    
    public final static String SETUP_DIFFS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_DIFF+" " +
            "(" +
            FieldNames.DIFF_ID+" BIGINT PRIMARY KEY, "+
            FieldNames.DIFF_MAPPING_ID+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.DIFF_CHAR+ " VARCHAR (1) NOT NULL, "+
            FieldNames.DIFF_POSITION+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.DIFF_TYPE+" TINYINT UNSIGNED NOT NULL, " +
            FieldNames.DIFF_ORDER+" BIGINT UNSIGNED , " +
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
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_FEATURES+" " +
            "(" +
            FieldNames.FEATURE_ID+" BIGINT PRIMARY KEY, " +
            FieldNames.FEATURE_REFGEN+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.FEATURE_TYPE+" TINYINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_START+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_STOP+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_LOCUS+" VARCHAR(1000) , " +
            FieldNames.FEATURE_PRODUCT+" VARCHAR(1000), " +
            FieldNames.FEATURE_ECNUM+" VARCHAR (20), " +
            FieldNames.FEATURE_STRAND+" TINYINT NOT NULL, " +
            "INDEX ("+FieldNames.FEATURE_REFGEN+") " +
            ") ";

    
    public final static String SETUP_MAPPINGS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_MAPPINGS+" " +
            "(" +
            FieldNames.MAPPING_ID+" BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.MAPPING_SEQUENCE_ID+ " BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_TRACK+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_START+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_STOP+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_DIRECTION+" TINYINT NOT NULL, " +
            FieldNames.MAPPING_COUNT+" SMALLINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_NUM_OF_ERRORS+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_BEST_MAPPING+" TINYINT UNSIGNED NOT NULL, " +
            " INDEX ("+FieldNames.MAPPING_START+"), " +
            " INDEX ("+FieldNames.MAPPING_STOP+"), " +
            " INDEX ("+FieldNames.MAPPING_SEQUENCE_ID+") " +
            ") ";

    
    public static final String SETUP_TRACKS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_TRACKS+" " +
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
            FieldNames.SEQ_PAIR_MAPPING_ID+" BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.SEQ_PAIR_NUM_OF_REPLICATES+" SMALLINT UNSIGNED NOT NULL, " +
            " INDEX ("+FieldNames.SEQ_PAIR_MAPPING_ID+"), " + 
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
            "ALTER TABLE "+FieldNames.TABLE_MAPPINGS+" DISABLE KEYS";

    public final static String ENABLE_MAPPING_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_MAPPINGS+" ENABLE KEYS";

    public final static String DISABLE_DIFF_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_DIFF+" DISABLE KEYS";

    public final static String ENABLE_DIFF_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_DIFF+" ENABLE KEYS";

    public final static String DISABLE_TRACK_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_TRACKS+" DISABLE KEYS";

    public final static String ENABLE_TRACK_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_TRACKS+" ENABLE KEYS";
    
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
            "ALTER TABLE "+FieldNames.TABLE_REF_GEN+" ENABLE KEYS";

    
    public final static String DISABLE_REFERENCE_INDICES =
            "ALTER TABLE "+FieldNames.TABLE_REF_GEN+" DISABLE KEYS";

    
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
            FieldNames.TABLE_REF_GEN+ " WRITE, " +
            FieldNames.TABLE_FEATURES+ " WRITE";

    
    public final static String LOCK_TABLE_TRACK_DOMAIN =
            "LOCK TABLE "
            + FieldNames.TABLE_COVERAGE + " WRITE, "
            + FieldNames.TABLE_TRACKS + " WRITE, "
            + FieldNames.TABLE_MAPPINGS + " WRITE, "
            + FieldNames.TABLE_STATISTICS + " WRITE, "
            + FieldNames.TABLE_DIFF + " WRITE ";
    
    
    public static final String LOCK_TABLE_SEQUENCE_PAIRS_DOMAIN = 
            "LOCK TABLE " + 
            FieldNames.TABLE_SEQ_PAIRS + " WRITE, " +
            FieldNames.TABLE_SEQ_PAIR_REPLICATES + " WRITE, " + 
            FieldNames.TABLE_SEQ_PAIR_PIVOT + " WRITE ";


    public final static String FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK =
            "SELECT " +
                "M."+FieldNames.MAPPING_ID+", "+
                "M."+FieldNames.MAPPING_BEST_MAPPING+", "+
                "M."+FieldNames.MAPPING_COUNT+", "+
                "M."+FieldNames.MAPPING_DIRECTION+", "+
                "M."+FieldNames.MAPPING_NUM_OF_ERRORS+", "+
                "M."+FieldNames.MAPPING_SEQUENCE_ID+", "+
                "M."+FieldNames.MAPPING_START+", "+
                "M."+FieldNames.MAPPING_STOP+", "+
                "M."+FieldNames.MAPPING_TRACK+", "+
                "D."+FieldNames.DIFF_CHAR+", "+
                "D."+FieldNames.DIFF_ORDER+", "+
                "D."+FieldNames.DIFF_POSITION+", "+
                "D."+FieldNames.DIFF_TYPE+" "+
            "FROM " +
                "(" +
                "SELECT " +
                    FieldNames.MAPPING_ID+", "+
                    FieldNames.MAPPING_BEST_MAPPING+", "+
                    FieldNames.MAPPING_COUNT+", "+
                    FieldNames.MAPPING_DIRECTION+", "+
                    FieldNames.MAPPING_NUM_OF_ERRORS+", "+
                    FieldNames.MAPPING_SEQUENCE_ID+", "+
                    FieldNames.MAPPING_START+", "+
                    FieldNames.MAPPING_STOP+", "+
                    FieldNames.MAPPING_TRACK+" "+
                "FROM "+
                    FieldNames.TABLE_MAPPINGS +" "+
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
                    "A."+FieldNames.DIFF_CHAR+", " +
                    "A."+FieldNames.MAPPING_DIRECTION+", " +
                    "A."+FieldNames.DIFF_TYPE+", " +
                    "A.mult_count, " +
                    "C."+FieldNames.COVERAGE_BM_FW_MULT+", " +
                    "C."+FieldNames.COVERAGE_BM_RV_MULT+" " +
            "FROM "+
		"(SELECT " +
                    FieldNames.DIFF_POSITION+", "+
                    FieldNames.DIFF_CHAR+", "+
                    FieldNames.DIFF_TYPE+", "+
                    FieldNames.MAPPING_DIRECTION+", " +
                    "SUM("+FieldNames.MAPPING_COUNT+") as mult_count  "+
		"FROM "+
                    FieldNames.TABLE_MAPPINGS+" M " +
                    "left join "+FieldNames.TABLE_DIFF+" D " +
                    "on D."+FieldNames.DIFF_MAPPING_ID+" = M."+FieldNames.MAPPING_ID+" " +
		"WHERE " +
                    "M."+FieldNames.MAPPING_TRACK+" = ? and M."+FieldNames.MAPPING_BEST_MAPPING+" = 1 "+
		"GROUP BY " +
                    "D."+FieldNames.DIFF_POSITION+", "+
                    "D."+FieldNames.DIFF_CHAR+", " +
                    "M."+FieldNames.MAPPING_DIRECTION+" ,"+
                    "D."+FieldNames.DIFF_TYPE+"" +
                ") as A , "+
		FieldNames.TABLE_COVERAGE+" C "+
            "WHERE " +
                "C."+FieldNames.COVERAGE_TRACK+" = ? AND " +
                "C."+FieldNames.COVERAGE_POSITION+" = A."+FieldNames.DIFF_POSITION;
    
}
