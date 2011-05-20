
package de.cebitec.vamp.databackend;

/**
 * Contains H2SQL statements needed for dat base connection and fetching of data.
 *
 * @author jstraube
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

    public final static String SETUP_REFERENCE_GENOME =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_REF_GEN+" " +
            "(" +
            FieldNames.REF_GEN_ID+" BIGINT PRIMARY KEY, " +
            FieldNames.REF_GEN_NAME+" VARCHAR(200) NOT NULL, " +
            FieldNames.REF_GEN_DESCRIPTION+" VARCHAR(200) NOT NULL," +
            FieldNames.REF_GEN_SEQUENCE+" CLOB NOT NULL, " +
            FieldNames.REF_GEN_TIMESTAMP+" DATETIME NOT NULL" +
            ") ";

    public final static String SETUP_DIFFS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_DIFF+" " +
            "(" +
            FieldNames.DIFF_ID+" BIGINT PRIMARY KEY, "+
            FieldNames.DIFF_MAPPING_ID+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.DIFF_CHAR+ " VARCHAR (1) NOT NULL, "+
            FieldNames.DIFF_POSITION+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.DIFF_TYPE+" TINYINT UNSIGNED NOT NULL, " +
            FieldNames.DIFF_ORDER+" BIGINT UNSIGNED " +
            ") ";
//in h2 you can ask if the index exists in mysql this did not work
    public final static String INDEX_DIFF =
            "CREATE INDEX IF NOT EXISTS INDEXDIFF ON " +FieldNames.TABLE_DIFF+ "("+FieldNames.DIFF_POSITION+", "+FieldNames.DIFF_MAPPING_ID+") " ;

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
            FieldNames.COVERAGE_N_RV_NUM+" MEDIUMINT UNSIGNED NOT NULL" +
            ") ";

    public final static String INDEX_COVERAGE =
            "CREATE INDEX IF NOT EXISTS INDEXCOVERAGE ON " +FieldNames.TABLE_COVERAGE+ "("+FieldNames.COVERAGE_POSITION+", "+FieldNames.COVERAGE_TRACK+") " ;

    public final static String SETUP_FEATURES =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_FEATURES+" " +
            "(" +
            FieldNames.FEATURE_ID+" BIGINT PRIMARY KEY, " +
            FieldNames.FEATURE_REFGEN+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.FEATURE_TYPE+" TINYINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_START+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_STOP+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.FEATURE_LOCUS+" VARCHAR (1000), " +
            FieldNames.FEATURE_PRODUCT+" VARCHAR (2000), " +
            FieldNames.FEATURE_ECNUM+" VARCHAR (20), " +
            FieldNames.FEATURE_STRAND+" TINYINT NOT NULL " +
            ") ";

    public final static String INDEX_FEATURES =
             "CREATE INDEX IF NOT EXISTS INDEXFEATURES ON "+FieldNames.TABLE_FEATURES+" ("+FieldNames.FEATURE_REFGEN+") ";

    public final static String SETUP_MAPPINGS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_MAPPINGS+" " +
            "(" +
            FieldNames.MAPPING_ID+" BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.MAPPING_SEQUENCE+ " BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_TRACK+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_START+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_STOP+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_DIRECTION+" TINYINT NOT NULL, " +
            FieldNames.MAPPING_COUNT+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_NUM_OF_ERRORS+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_BEST_MAPPING+" TINYINT UNSIGNED NOT NULL " +
            ") ";

     public final static String UPDATE_MAPPINGS_DATATYPE =
            "ALTER TABLE "+FieldNames.TABLE_MAPPINGS+" " +
            " ALTER COLUMN " +
            FieldNames.MAPPING_COUNT+" BIGINT UNSIGNED NOT NULL " ;

        public final static String INDEX_MAPPINGS =
             "CREATE INDEX IF NOT EXISTS INDEXMAPPINGS ON "+FieldNames.TABLE_MAPPINGS+" "
             + "("+FieldNames.MAPPING_START+", "+FieldNames.MAPPING_STOP+","+FieldNames.MAPPING_SEQUENCE+" ) ";

    public final static String SETUP_TRACKS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_TRACKS + " "
            + "( "
            + FieldNames.TRACK_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.TRACK_REFGEN + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.TRACK_DESCRIPTION + " VARCHAR (200) NOT NULL, "
            + FieldNames.TRACK_TIMESTAMP + " DATETIME NOT NULL "//, "
            //+ FieldNames.TRACK_RUN + " BIGINT UNSIGNED NOT NULL "
            + ") ";

     public final static String SETUP_STATICS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_STATICS + " "
            + "( "
            + FieldNames.STATICS_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.STATICS_TRACK + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_PERFECT_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_BM_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_MAPPED_SEQ + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_PERFECT_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_BM_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_COMPLETE_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL "
            + ") ";

    public final static String INDEX_TRACKS =
             "CREATE INDEX IF NOT EXISTS INDEXTRACK ON "+FieldNames.TABLE_TRACKS+" ("+FieldNames.TRACK_REFGEN+") ";

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

        public final static String INSERT_STATICS =
            "INSERT INTO "+FieldNames.TABLE_STATICS+" " +
            "(" +
            FieldNames.STATICS_ID+", " +
            FieldNames.STATICS_TRACK+", " +
            FieldNames.STATICS_NUMBER_OF_MAPPINGS +", " +
            FieldNames.STATICS_NUMBER_OF_PERFECT_MAPPINGS +", " +
            FieldNames.STATICS_NUMBER_OF_BM_MAPPINGS +", " +
            FieldNames.STATICS_NUMBER_OF_MAPPED_SEQ+ ", "+
            FieldNames.STATICS_PERFECT_COVERAGE_OF_GENOME+", " +
            FieldNames.STATICS_BM_COVERAGE_OF_GENOME+", " +
            FieldNames.STATICS_COMPLETE_COVERAGE_OF_GENOME+", " +
            FieldNames.STATICS_NUMBER_OF_READS+", " +
            FieldNames.STATICS_NUMBER_OF_UNIQUE_SEQ+" " +

            ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)";


  

//    public final static String INSERT_RUN =
//            "INSERT INTO "+FieldNames.TABLE_RUN+" " +
//            "( " +
//            FieldNames.RUN_ID+", "+
//            FieldNames.RUN_DESCRIPTION+", " +
//            FieldNames.RUN_TIMESTAMP+" " +
//            ") " +
//            "VALUES (?,?,?)";

        public final static String UPDATE_STATIC_VALUES =
            "UPDATE "+FieldNames.TABLE_STATICS+" " +
            "SET " +
            FieldNames.STATICS_NUMBER_OF_READS+" = ? , " +
            FieldNames.STATICS_NUMBER_OF_UNIQUE_SEQ+"  = ?" +
            "WHERE " +
                FieldNames.TRACK_ID+" = ? " ;

//    public final static String INSERT_SEQUENCE =
//            "INSERT INTO "+FieldNames.TABLE_SEQUENCE+" " +
//            "(" +
//            FieldNames.SEQUENCE_ID+", "+
//            FieldNames.SEQUENCE_RUN+" " +
//            ")" +
//            "VALUES (?,?)";

//    public final static String INSERT_READ =
//            "INSERT INTO "+FieldNames.TABLE_READS+" " +
//            "( " +
//            FieldNames.READ_ID+", "+
//            FieldNames.READ_NAME+", " +
//            FieldNames.READ_SEQUENCE+" " +
//            ") " +
//            "VALUES (?,?,?)";



    public final static String ADD_COLUMN_TO_TABLE_STATICS_NUMBER_OF_READS =
            "ALTER TABLE "+
                FieldNames.TABLE_STATICS+" " +
            "ADD COLUMN "+
                FieldNames.STATICS_NUMBER_OF_READS+" BIGINT UNSIGNED " ;

     /*     public final static String ADD_COLUMN_TO_TABLE_STATICS_NUMBER_OF_UNIQUE_SEQ =
            "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = "
            + FieldNames.TABLE_STATICS
            + " AND COLUMN_NAME =" + FieldNames.STATICS_NUMBER_OF_UNIQUE_SEQ + ")"
            + " BEGIN "
            + "ALTER TABLE "
            + FieldNames.TABLE_STATICS
            + " ADD COLUMN "
            + FieldNames.STATICS_NUMBER_OF_UNIQUE_SEQ + " BIGINT UNSIGNED "+
            " END";*/

          public final static String ADD_COLUMN_TO_TABLE_STATICS_NUMBER_OF_UNIQUE_SEQ =
            "ALTER TABLE "+
                FieldNames.TABLE_STATICS+" " +
            "ADD COLUMN "+
                FieldNames.STATICS_NUMBER_OF_UNIQUE_SEQ+" BIGINT UNSIGNED  ";


    // statements to fetch data from database

//    public final static String FETCH_READNAME_SEQUENCEID_MAPPING =
//            "SELECT " +
//                "S."+FieldNames.SEQUENCE_ID+" as seqID, " +
//                "R."+FieldNames.READ_NAME+" as readname "+
//            "FROM "+
//                FieldNames.TABLE_READS+" AS R, " +
//                FieldNames.TABLE_SEQUENCE+" AS S "+
//            "WHERE "+
//                FieldNames.SEQUENCE_RUN+" = ? and " +
//                "S."+FieldNames.SEQUENCE_ID+" = R."+FieldNames.READ_SEQUENCE;

//    public final static String FETCH_RUNS =
//            "SELECT " +
//                "R."+FieldNames.RUN_ID+", " +
//                "R."+FieldNames.RUN_DESCRIPTION+", " +
//                "R."+FieldNames.RUN_TIMESTAMP +" "+
//            "FROM "+
//                FieldNames.TABLE_RUN+" AS R ";

    public final static String FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK =
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
                FieldNames.COVERAGE_ZERO_RV_NUM+" " +
            "FROM " +
                FieldNames.TABLE_COVERAGE+" " +
            "WHERE "+
                FieldNames.COVERAGE_POSITION+ " between ? and ? and "+
                FieldNames.COVERAGE_TRACK+" = ? ";





public final static String FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK =
            "SELECT " +
                "M."+FieldNames.MAPPING_ID+", "+
                "M."+FieldNames.MAPPING_BEST_MAPPING+", "+
                "M."+FieldNames.MAPPING_COUNT+", "+
                "M."+FieldNames.MAPPING_DIRECTION+", "+
                "M."+FieldNames.MAPPING_NUM_OF_ERRORS+", "+
                "M."+FieldNames.MAPPING_SEQUENCE+", "+
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
                    FieldNames.MAPPING_SEQUENCE+", "+
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
               "("
               + "SELECT "+
               FieldNames.DIFF_CHAR+", "+
               FieldNames.DIFF_ORDER+", "+
               FieldNames.DIFF_POSITION+", "+
               FieldNames.DIFF_TYPE+", "+
               FieldNames.DIFF_MAPPING_ID+" "+
               "FROM "+
                FieldNames.TABLE_DIFF+" " +
               "WHERE " +
               FieldNames.DIFF_POSITION + " BETWEEN ? AND ? "+
                ") AS D " +
            "on " +
                "M."+FieldNames.MAPPING_ID+" = D."+FieldNames.DIFF_MAPPING_ID;


//    public final static String FETCH_READNAMES_FOR_SEQUENCE_ID =
//            "SELECT "+
//                FieldNames.READ_NAME+" " +
//            "FROM "+
//                FieldNames.TABLE_READS+" " +
//            "WHERE "+
//                FieldNames.READ_SEQUENCE+" = ?";


//    public final static String FETCH_NUM_OF_READS_FOR_RUN_CALCULATE =
//            "SELECT " +
//                "COUNT(R."+FieldNames.READ_ID+") as NUM " +
//            "FROM "+
//                FieldNames.TABLE_READS+" as R , "+
//                FieldNames.TABLE_SEQUENCE+" as S " +
//            "WHERE "+
//                "S."+FieldNames.SEQUENCE_RUN+" = ? and " +
//                "R."+FieldNames.READ_SEQUENCE+" = S."+FieldNames.SEQUENCE_ID;


    public final static String FETCH_NUM_OF_READS_FOR_TRACK =
        "SELECT " +
            "S."+ FieldNames.STATICS_NUMBER_OF_READS+" as NUM " +
        "FROM "+
            FieldNames.TABLE_STATICS+" as S " +
        " WHERE "+
            "S."+ FieldNames.TRACK_ID+" = ?" ;


    public final static String FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK =
        "SELECT " +
            "S."+FieldNames.STATICS_NUMBER_OF_UNIQUE_SEQ +" as NUM " +
        "FROM "+
            FieldNames.TABLE_STATICS+" as S " +
        "WHERE "+
            "S."+FieldNames.TRACK_ID+" = ?" ;



    public final static String FETCH_NUM_MAPPED_SEQUENCES_FOR_TRACK =
            "SELECT " +
            FieldNames.STATICS_NUMBER_OF_MAPPED_SEQ +" as Num "+
            " FROM "+
                FieldNames.TABLE_STATICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATICS_TRACK+" = ?";


    public final static String FETCH_NUM_BM_MAPPINGS_FOR_TRACK =
            "SELECT " +
            FieldNames.STATICS_NUMBER_OF_BM_MAPPINGS+" as Num "+
            " FROM "+
                FieldNames.TABLE_STATICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATICS_TRACK+" = ?";


    public final static String FETCH_SNP_DATA_FOR_TRACK_FOR_INTERVALL =
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
                    FieldNames.TABLE_MAPPINGS+" AS M " +
                    "left join "+FieldNames.TABLE_DIFF+" AS D " +
                    "on D."+FieldNames.DIFF_MAPPING_ID+" = M."+FieldNames.MAPPING_ID+" " +
		"WHERE " +
                    "M."+FieldNames.MAPPING_TRACK+" = ? and M."+FieldNames.MAPPING_BEST_MAPPING+" = 1 and M."+
                    FieldNames.MAPPING_START+" BETWEEN ? AND ? and D."+FieldNames.DIFF_POSITION+" BETWEEN ? AND ? " +
		"GROUP BY " +
                    "D."+FieldNames.DIFF_POSITION+", "+
                    "D."+FieldNames.DIFF_CHAR+", " +
                    "M."+FieldNames.MAPPING_DIRECTION+" ,"+
                    "D."+FieldNames.DIFF_TYPE+"" +
                ") as A , "+
		FieldNames.TABLE_COVERAGE+" AS C "+
            "WHERE " +
                "C."+FieldNames.COVERAGE_TRACK+" = ? AND " +
                "C."+FieldNames.COVERAGE_POSITION+" = A."+FieldNames.DIFF_POSITION;
    
             public final static String GET_LATEST_STATICS_ID =
            "SELECT MAX("+FieldNames.STATICS_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_STATICS;
}
