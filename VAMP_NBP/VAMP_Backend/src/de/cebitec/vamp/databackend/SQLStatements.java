package de.cebitec.vamp.databackend;

/**
 *
 * @author ddoppmeier
 */
public class SQLStatements {

    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private SQLStatements() {
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
            FieldNames.REF_GEN_SEQUENCE+" LONGTEXT NOT NULL, " +
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
            FieldNames.MAPPING_SEQUENCE+ " BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_TRACK+" BIGINT UNSIGNED NOT NULL, "+
            FieldNames.MAPPING_START+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_STOP+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_DIRECTION+" TINYINT NOT NULL, " +
            FieldNames.MAPPING_COUNT+" SMALLINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_NUM_OF_ERRORS+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.MAPPING_BEST_MAPPING+" TINYINT UNSIGNED NOT NULL, " +
            " INDEX ("+FieldNames.MAPPING_START+"), " +
            " INDEX ("+FieldNames.MAPPING_STOP+"), " +
            " INDEX ("+FieldNames.MAPPING_SEQUENCE+") " +
            ") ";

    public final static String SETUP_TRACKS =
            "CREATE TABLE IF NOT EXISTS "+FieldNames.TABLE_TRACKS+" " +
            "( " +
            FieldNames.TRACK_ID+ " BIGINT UNSIGNED PRIMARY KEY, " +
            FieldNames.TRACK_REFGEN+" BIGINT UNSIGNED NOT NULL, " +
            FieldNames.TRACK_DESCRIPTION+" VARCHAR (1000) NOT NULL, " +
            FieldNames.TRACK_TIMESTAMP+" DATETIME NOT NULL, " +
            //FieldNames.TRACK_RUN+" BIGINT UNSIGNED NOT NULL, "+
            "INDEX ("+FieldNames.TRACK_REFGEN+") " +
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

     public final static String SETUP_STATICS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_STATICS + " "
            + "( "
            + FieldNames.STATICS_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.STATICS_TRACK + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_PERFECT_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_BM_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_UNIQUE_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_PERFECT_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_BM_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_COMPLETE_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATICS_NUMBER_OF_UNIQUE_SEQ + " BIGINT UNSIGNED  "
            + ") ";


    //////////////////  statements for data insertion  /////////////////////////

    public final static String INSERT_REFGENOME =
            "INSERT INTO "+FieldNames.TABLE_REF_GEN+" " +
            "(" +
            FieldNames.REF_GEN_ID+", " +
            FieldNames.REF_GEN_NAME+"," +
            FieldNames.REF_GEN_DESCRIPTION+"," +
            FieldNames.REF_GEN_SEQUENCE+", " +
            FieldNames.REF_GEN_TIMESTAMP+" " +
            ") " +
            "VALUES (?,?,?,?,?)";

    public final static String INSERT_FEATURE =
            "INSERT INTO "+FieldNames.TABLE_FEATURES+" " +
            "(" +
            FieldNames.FEATURE_ID+", " +
            FieldNames.FEATURE_REFGEN+", "+
            FieldNames.FEATURE_TYPE+", " +
            FieldNames.FEATURE_START+", " +
            FieldNames.FEATURE_STOP+", " +
            FieldNames.FEATURE_LOCUS+", " +
            FieldNames.FEATURE_PRODUCT+", " +
            FieldNames.FEATURE_ECNUM+", " +
            FieldNames.FEATURE_STRAND+" "+
            ") " +
            "VALUES (?,?,?,?,?,?,?,?,?)";

    public final static String INSERT_TRACK =
            "INSERT INTO "+FieldNames.TABLE_TRACKS+" " +
            "(" +
            FieldNames.TRACK_ID+", " +
            FieldNames.TRACK_REFGEN+", " +
            FieldNames.TRACK_DESCRIPTION+", " +
            FieldNames.TRACK_TIMESTAMP+" "+//", " +
            //FieldNames.TRACK_RUN+" " +
            ") " +
            "VALUES (?,?,?,?)";//,?)";

    public final static String INSERT_MAPPING =
            "INSERT INTO "+FieldNames.TABLE_MAPPINGS+" " +
            "(" +
            FieldNames.MAPPING_ID+", " +
            FieldNames.MAPPING_START+", " +
            FieldNames.MAPPING_STOP+", " +
            FieldNames.MAPPING_BEST_MAPPING+", " +
            FieldNames.MAPPING_COUNT+", " +
            FieldNames.MAPPING_DIRECTION+", " +
            FieldNames.MAPPING_NUM_OF_ERRORS+", " +
            FieldNames.MAPPING_SEQUENCE+", " +
            FieldNames.MAPPING_TRACK+" " +
            ") " +
            "VALUES (?,?,?,?,?,?,?,?,?)";

    public final static String INSERT_DIFF =
            "INSERT INTO "+FieldNames.TABLE_DIFF+" " +
            "(" +
            FieldNames.DIFF_ID+", "+
            FieldNames.DIFF_MAPPING_ID+", " +
            FieldNames.DIFF_CHAR+", " +
            FieldNames.DIFF_POSITION+", " +
            FieldNames.DIFF_TYPE+", " +
            FieldNames.DIFF_ORDER+" " +
            ") " +
            "VALUES (?,?,?,?,?, null)";

    public final static String INSERT_GAP =
            "INSERT INTO "+FieldNames.TABLE_DIFF+" " +
            "(" +
            FieldNames.DIFF_ID+", " +
            FieldNames.DIFF_MAPPING_ID+", " +
            FieldNames.DIFF_CHAR+", " +
            FieldNames.DIFF_POSITION+", " +
            FieldNames.DIFF_TYPE+", " +
            FieldNames.DIFF_ORDER+" "+
            ") " +
            "VALUES (?,?,?,?,?,?)";

    public final static String INSERT_COVERAGE =
            "INSERT INTO "+FieldNames.TABLE_COVERAGE+" " +
            "(" +
            FieldNames.COVERAGE_ID+", " +
            FieldNames.COVERAGE_TRACK+", " +
            FieldNames.COVERAGE_POSITION+", " +
            FieldNames.COVERAGE_BM_FW_MULT+", " +
            FieldNames.COVERAGE_BM_FW_NUM+", " +
            FieldNames.COVERAGE_BM_RV_MULT+", " +
            FieldNames.COVERAGE_BM_RV_NUM+", " +
            FieldNames.COVERAGE_ZERO_FW_MULT+", " +
            FieldNames.COVERAGE_ZERO_FW_NUM+", " +
            FieldNames.COVERAGE_ZERO_RV_MULT+", " +
            FieldNames.COVERAGE_ZERO_RV_NUM+", " +
            FieldNames.COVERAGE_N_FW_MULT+", " +
            FieldNames.COVERAGE_N_FW_NUM+", " +
            FieldNames.COVERAGE_N_RV_MULT+", " +
            FieldNames.COVERAGE_N_RV_NUM+" " +
            ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

//    public final static String INSERT_RUN =
//            "INSERT INTO "+FieldNames.TABLE_RUN+" " +
//            "( " +
//            FieldNames.RUN_ID+", "+
//            FieldNames.RUN_DESCRIPTION+", " +
//            FieldNames.RUN_TIMESTAMP+" " +
//            ") " +
//            "VALUES (?,?,?)";

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



//            public final static String INSERT_STATICS =
//            "INSERT INTO "+FieldNames.TABLE_STATICS+" " +
//            "(" +
//            FieldNames.STATICS_ID+", " +
//            FieldNames.STATICS_TRACK+", " +
//            FieldNames.STATICS_NUMBER_OF_MAPPINGS +", " +
//            FieldNames.STATICS_NUMBER_OF_PERFECT_MAPPINGS +", " +
//            FieldNames.STATICS_NUMBER_OF_BM_MAPPINGS +", " +
//            FieldNames.STATICS_NUMBER_OF_MAPPED_SEQ+ ", "+
//            FieldNames.STATICS_PERFECT_COVERAGE_OF_GENOME+", " +
//            FieldNames.STATICS_BM_COVERAGE_OF_GENOME+", " +
//            FieldNames.STATICS_COMPLETE_COVERAGE_OF_GENOME+" " +
//            ") " +
//            "VALUES (?,?,?,?,?,?,?,?,?)";

    /*
     * Delete the track data
     */
    public final static String DELETE_DIFFS_FROM_TRACK =
            "DELETE FROM "+
                FieldNames.TABLE_DIFF +" " +
            "WHERE " +
                FieldNames.DIFF_MAPPING_ID+
            " IN " +
            "( " +
                "SELECT "+
                    FieldNames.MAPPING_ID+ " " +
                "FROM "+FieldNames.TABLE_MAPPINGS+" " +
                "WHERE "+FieldNames.MAPPING_TRACK+" = ? " +
            ")";

    public final static String DELETE_MAPPINGS_FROM_TRACK =
            "DELETE FROM "+
                FieldNames.TABLE_MAPPINGS+" " +
            "WHERE "+
                FieldNames.MAPPING_TRACK+" = ? ";

    public final static String DELETE_COVERAGE_FROM_TRACK =
            "DELETE FROM "+
                FieldNames.TABLE_COVERAGE+" " +
            "WHERE "+
                FieldNames.COVERAGE_TRACK+" = ?";

    public final static String DELETE_TRACK =
            "DELETE FROM "+
                FieldNames.TABLE_TRACKS+" " +
            "WHERE "+
                FieldNames.TRACK_ID+" = ? ";
//    /*
//     * Delete the run data
//     */
//    public final static String DELETE_READS_FROM_RUN =
//            "DELETE FROM "+
//                FieldNames.TABLE_READS+" " +
//            "WHERE "+
//                FieldNames.READ_SEQUENCE+" " +
//            "IN " +
//            "(" +
//                "SELECT "+
//                    FieldNames.SEQUENCE_ID+" " +
//                "FROM "+
//                FieldNames.TABLE_SEQUENCE+" " +
//                "WHERE "+
//                    FieldNames.SEQUENCE_RUN+" = ?" +
//            ")";

//    public final static String DELETE_SEQUENCE_FROM_RUN =
//            "DELETE FROM "+
//                FieldNames.TABLE_SEQUENCE+" " +
//            "WHERE "+
//                FieldNames.SEQUENCE_RUN+" = ?";
//
//    public final static String DELETE_RUN =
//            "DELETE FROM "+
//                FieldNames.TABLE_RUN+" " +
//            "WHERE "+
//                FieldNames.RUN_ID+" = ?";
/*
 * Delete the genome data
 */
    public final static String DELETE_FEATURES_FROM_GENOME =
            "DELETE FROM "+
                FieldNames.TABLE_FEATURES+" " +
            "WHERE "+
                FieldNames.FEATURE_REFGEN+" = ?";

    public final static String DELETE_GENOME =
            "DELETE FROM "+
                FieldNames.TABLE_REF_GEN+" " +
            "WHERE "+
                FieldNames.REF_GEN_ID+" = ?";


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
//
//    public final static String FETCH_RUNS =
//            "SELECT " +
//                "R."+FieldNames.RUN_ID+", " +
//                "R."+FieldNames.RUN_DESCRIPTION+", " +
//                "R."+FieldNames.RUN_TIMESTAMP +" "+
//            "FROM "+
//                FieldNames.TABLE_RUN+" AS R ";

    public final static String FETCH_GENOMES =
            "SELECT " +
                "R."+FieldNames.REF_GEN_ID+", " +
                "R."+FieldNames.REF_GEN_NAME+", " +
                "R."+FieldNames.REF_GEN_DESCRIPTION+", " +
                "R."+FieldNames.REF_GEN_SEQUENCE+",  " +
                "R."+FieldNames.REF_GEN_TIMESTAMP+" " +
            "FROM "+
                FieldNames.TABLE_REF_GEN+" AS R ";

    public final static String FETCH_TRACKS =
            "SELECT " +
                "T."+FieldNames.TRACK_ID+", " +
                "T."+FieldNames.TRACK_DESCRIPTION+", " +
                "T."+FieldNames.TRACK_TIMESTAMP+", " +
                "T."+FieldNames.TRACK_REFGEN+" "+//", " +
                //"T."+FieldNames.TRACK_RUN+" " +
            "FROM "+
                FieldNames.TABLE_TRACKS+" AS T ";

    public final static String FETCH_SINGLE_GENOME =
            "SELECT "+
                FieldNames.REF_GEN_ID+", "+
                FieldNames.REF_GEN_NAME+", "+
                FieldNames.REF_GEN_DESCRIPTION+", "+
                FieldNames.REF_GEN_SEQUENCE+", "+
                FieldNames.REF_GEN_TIMESTAMP+" " +
            "FROM "+
                FieldNames.TABLE_REF_GEN+" "+
            "WHERE "+
                FieldNames.REF_GEN_ID+" = ?";

    public final static String FETCH_FEATURES_FOR_INTERVAL_FROM_GENOME =
            "SELECT " +
                FieldNames.FEATURE_ECNUM+", "+
                FieldNames.FEATURE_ID+", "+
                FieldNames.FEATURE_LOCUS+", "+
                FieldNames.FEATURE_PRODUCT+", "+
                FieldNames.FEATURE_START+", "+
                FieldNames.FEATURE_STOP+", "+
                FieldNames.FEATURE_STRAND+", "+
                FieldNames.FEATURE_TYPE+" " +
            "FROM "+
                FieldNames.TABLE_FEATURES+" " +
            "WHERE "+
                FieldNames.FEATURE_REFGEN+" = ? and " +
                FieldNames.FEATURE_STOP+" >= ? and " +
                FieldNames.FEATURE_START+" <= ? ";


    public final static String FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK =
            "(SELECT "+
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
                FieldNames.COVERAGE_TRACK+" = ? )";

    public final static String FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2 =
            "SELECT "+
                FieldNames.COVERAGE_POSITION+", "+
                FieldNames.COVERAGE_N_FW_MULT+", " +
                FieldNames.COVERAGE_N_RV_MULT+" " +
            "FROM " +
                FieldNames.TABLE_COVERAGE+" " +
            "WHERE "+
                FieldNames.COVERAGE_POSITION+ " between ? and ? and "+
                FieldNames.COVERAGE_TRACK+" = ? ";

        public final static String FETCH_COVERAGE_FOR_TRACK =
            "SELECT "+
                FieldNames.COVERAGE_POSITION+", "+

                FieldNames.COVERAGE_N_FW_MULT+" + " +FieldNames.COVERAGE_N_RV_MULT+
               " as " + FieldNames.COVERAGE_N_MULT+

            " FROM " +
                FieldNames.TABLE_COVERAGE+" " +
            "WHERE "+
                FieldNames.COVERAGE_TRACK+" = ?  and " + FieldNames.COVERAGE_POSITION + " between ? and ?";


    public final static String FETCH_TRACKS_FOR_GENOME =
            "SELECT "+
                FieldNames.TRACK_ID+", "+
                FieldNames.TRACK_DESCRIPTION+", "+
                FieldNames.TRACK_TIMESTAMP+", " +
                FieldNames.TRACK_REFGEN+" "+//", "+
                //FieldNames.TRACK_RUN+" " +
            "FROM "+
                FieldNames.TABLE_TRACKS+" " +
            "WHERE "+
                FieldNames.TRACK_REFGEN+" = ? ";


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
                FieldNames.TABLE_DIFF+" AS D " +
            "on " +
                "M."+FieldNames.MAPPING_ID+" = D."+FieldNames.DIFF_MAPPING_ID;


//    public final static String FETCH_READNAMES_FOR_SEQUENCE_ID =
//            "SELECT "+
//                FieldNames.READ_NAME+" " +
//            "FROM "+
//                FieldNames.TABLE_READS+" " +
//            "WHERE "+
//                FieldNames.READ_SEQUENCE+" = ?";


    public final static String FETCH_GENOME_GAPS_IN_TRACK_FOR_INTERVAL =
            "SELECT "+
                "D."+FieldNames.DIFF_CHAR+", "+
                "D."+FieldNames.DIFF_POSITION+", " +
                "D."+FieldNames.DIFF_ORDER+", "+
                "M."+FieldNames.MAPPING_DIRECTION+ ", " +
                "M."+FieldNames.MAPPING_COUNT+" "+
            "FROM "+
                FieldNames.TABLE_DIFF+" as D, "+
                FieldNames.TABLE_MAPPINGS+" as M "+
            "WHERE " +
                "D."+FieldNames.DIFF_POSITION+" between ? and ? and "+
                "D."+FieldNames.DIFF_TYPE+" = 0 and "+
                "M."+FieldNames.MAPPING_ID+" = D."+FieldNames.DIFF_MAPPING_ID+" and "+
                "M."+FieldNames.MAPPING_TRACK+" = ?";


    public final static String FETCH_DIFFS_IN_TRACK_FOR_INTERVAL =
            "SELECT " +
                "D."+FieldNames.DIFF_POSITION+", " +
                "D."+FieldNames.DIFF_CHAR+", " +
                "M."+FieldNames.MAPPING_DIRECTION+", " +
                "M."+FieldNames.MAPPING_COUNT+" "+
            "FROM "+
                FieldNames.TABLE_DIFF+" AS D , "+
                FieldNames.TABLE_MAPPINGS+" AS M "+
            "WHERE " +
                "D."+FieldNames.DIFF_POSITION+" between ? and ? and " +
                "D."+FieldNames.DIFF_TYPE+" = 1 and " +
                "D."+FieldNames.DIFF_MAPPING_ID+" = M."+FieldNames.MAPPING_ID+" and " +
                "M."+FieldNames.MAPPING_TRACK+" = ?";

//    public final static String FETCH_NUM_OF_READS_FOR_RUN =
//            "SELECT " +
//                "COUNT(R."+FieldNames.READ_ID+") as NUM " +
//            "FROM "+
//                FieldNames.TABLE_READS+" as R , "+
//                FieldNames.TABLE_SEQUENCE+" as S " +
//            "WHERE "+
//                "S."+FieldNames.SEQUENCE_RUN+" = ? and " +
//                "R."+FieldNames.READ_SEQUENCE+" = S."+FieldNames.SEQUENCE_ID;

//    public final static String FETCH_NUM_UNIQUE_SEQUENCES_FOR_RUN =
//            "SELECT " +
//                "COUNT(S."+FieldNames.SEQUENCE_ID+") as NUM " +
//            "FROM "+
//                FieldNames.TABLE_SEQUENCE+" as S " +
//            "WHERE "+
//                "S."+FieldNames.SEQUENCE_RUN+" = ?";


//    public final static String FETCH_NUM_OF_READS_FOR_TRACK_CALCULATE =
//            "SELECT " +
//                "SUM(M." + FieldNames.MAPPING_COUNT + ") as NUM " +
//            "FROM " +
//                FieldNames.TABLE_MAPPINGS + " as M " +
//            "WHERE " +
//                "M." + FieldNames.MAPPING_TRACK + " = ?";
    
    public static final String FETCH_NUM_OF_READS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "SUM(R." + FieldNames.MAPPING_COUNT + ") as NUM " +
            "FROM " +
                "(SELECT " +
                    "M." + FieldNames.MAPPING_SEQUENCE+ " , " + FieldNames.MAPPING_COUNT + //DISTINCT returns mapping count for unique sequences
                " FROM " +
                    FieldNames.TABLE_MAPPINGS + " as M " +
                "WHERE " +
                    "M." + FieldNames.MAPPING_TRACK + " = ? " +
                ") as R;";

    public static final String FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK =
            "SELECT " +
                FieldNames.STATICS_NUMBER_UNIQUE_MAPPINGS + " as NUM " +
            "FROM "+
                FieldNames.TABLE_STATICS+ " as S " +
            "WHERE "+
                "S."+FieldNames.STATICS_TRACK + " = ?";

    public static final String FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(DISTINCT M."+FieldNames.MAPPING_ID+") as NUM " +
            "FROM "+
                FieldNames.TABLE_MAPPINGS + " as M " +
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK+" = ?";


    public final static String FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(DISTINCT M."+FieldNames.MAPPING_SEQUENCE+") as NUM "+
            "FROM "+
                FieldNames.TABLE_MAPPINGS+" as M " +
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK+" = ?";


    public final static String FETCH_NUM_BM_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(M."+FieldNames.MAPPING_ID+") as NUM " +
            " FROM "+
                FieldNames.TABLE_MAPPINGS+" as M " +
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK+" = ? AND "+
                "M."+FieldNames.MAPPING_BEST_MAPPING+" = 1 ";


    public final static String FETCH_NUM_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(M."+FieldNames.MAPPING_ID+") as NUM " +
            " FROM "+
                FieldNames.TABLE_MAPPINGS+" as M " +
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK+" = ? ";

    public final static String FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(M."+FieldNames.MAPPING_ID+") as NUM " +
            " FROM "+
                FieldNames.TABLE_MAPPINGS+ " as M "+
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK +" = ? and "+
                "M."+FieldNames.MAPPING_NUM_OF_ERRORS+" = 0 ";

//    public final static String FETCH_RUNID_FOR_TRACK =
//            "SELECT "+
//                "T."+FieldNames.TRACK_RUN+" " +
//            "FROM "+
//                FieldNames.TABLE_TRACKS+" as T " +
//            "WHERE "+
//                "T."+FieldNames.TRACK_ID+" = ?";



         //Get values from table STATICS
        public final static String FETCH_PERFECT_COVERAGE_OF_GENOME =
            "SELECT " +
            FieldNames.STATICS_PERFECT_COVERAGE_OF_GENOME+" as COVERED "+
            " FROM "+
                FieldNames.TABLE_STATICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATICS_TRACK+" = ?";

         public final static String FETCH_BM_COVERAGE_OF_GENOME =
            "SELECT " +
            FieldNames.STATICS_BM_COVERAGE_OF_GENOME+" as COVERED "+
            " FROM "+
                FieldNames.TABLE_STATICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATICS_TRACK+" = ?";

         public final static String FETCH_COMPLETE_COVERAGE_OF_GENOME =
            "SELECT " +
            FieldNames.STATICS_COMPLETE_COVERAGE_OF_GENOME+" as COVERED "+
            " FROM "+
                FieldNames.TABLE_STATICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATICS_TRACK+" = ?";

//    public final static String FETCH_NUM_MAPPED_SEQUENCES_FOR_TRACK =
//            "SELECT " +
//            FieldNames.STATICS_NUMBER_OF_MAPPED_SEQ +" as Num "+
//            " FROM "+
//                FieldNames.TABLE_STATICS+" as S " +
//            "WHERE "+
//                "S."+FieldNames.STATICS_TRACK+" = ?";

    public final static String FETCH_NUM_MAPPINGS_FOR_TRACK =
            "SELECT " +
            FieldNames.STATICS_NUMBER_OF_MAPPINGS+" as Num "+
            " FROM "+
                FieldNames.TABLE_STATICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATICS_TRACK+" = ?";

    public final static String FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK =
            "SELECT " +
            FieldNames.STATICS_NUMBER_OF_PERFECT_MAPPINGS+" as Num "+
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


    public final static String FETCH_GENOMEID_FOR_TRACK =
            "SELECT "+
                FieldNames.TRACK_REFGEN+" " +
            "FROM "+
                FieldNames.TABLE_TRACKS+" " +
            "WHERE "+
                FieldNames.TRACK_ID+" = ?";

    public final static String FETCH_GENOME_LENGTH =
            "SELECT " +
                "LENGTH("+FieldNames.REF_GEN_SEQUENCE+") as LENGTH " +
            "FROM "+
                FieldNames.TABLE_REF_GEN+" " +
            "WHERE "+
                FieldNames.REF_GEN_ID+" = ?";

    public final static String FETCH_NUM_BM_COVERED_POSITION_FOR_TRACK =
            "SELECT " +
                "COUNT("+FieldNames.COVERAGE_ID+") as COVERED " +
            "FROM "+
                FieldNames.TABLE_COVERAGE+" " +
            "WHERE " +
                "("+FieldNames.COVERAGE_BM_FW_MULT+" + "+FieldNames.COVERAGE_BM_RV_MULT+") != 0 AND "+
                FieldNames.COVERAGE_TRACK+" = ?";

    public final static String FETCH_NUM_PERFECT_COVERED_POSITIONS_FOR_TRACK =
            "SELECT " +
                "COUNT("+FieldNames.COVERAGE_ID+") as COVERED " +
            "FROM "+
                FieldNames.TABLE_COVERAGE+" " +
            "WHERE " +
                "("+FieldNames.COVERAGE_ZERO_FW_MULT+" + "+FieldNames.COVERAGE_ZERO_RV_MULT+") != 0 AND "+
                FieldNames.COVERAGE_TRACK+" = ?";

    public final static String FETCH_NUM_COVERED_POSITIONS =
            "SELECT " +
                "COUNT("+FieldNames.COVERAGE_ID+") as COVERED " +
            "FROM "+
                FieldNames.TABLE_COVERAGE+" " +
            "WHERE " +
                "("+FieldNames.COVERAGE_N_FW_MULT+" + "+FieldNames.COVERAGE_N_RV_MULT+") != 0 AND "+
                FieldNames.COVERAGE_TRACK+" = ?";


//    public final static String FETCH_READ_POSITION_BY_READNAME =
//            "SELECT "+
//            "M."+ FieldNames.MAPPING_START+", M."+FieldNames.MAPPING_STOP+
//            " FROM " +
//            FieldNames.TABLE_MAPPINGS+ " AS M , " + FieldNames.TABLE_READS+ " AS R " +
//           "WHERE  R." +
//           FieldNames.READ_NAME + "= ?  AND M."+FieldNames.MAPPING_SEQUENCE + " = R." +FieldNames.READ_SEQUENCE+" AND M." +FieldNames.MAPPING_TRACK+" = ? " ;


    // Removes a constraint or a primary key from a table. This command commits an open and faster transaction.
    //Enable KEYS and DISABLE KEYS are functions only use for mysql not for h2
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
            "LOCK TABLE " +
            FieldNames.TABLE_COVERAGE + " WRITE, " +
            FieldNames.TABLE_TRACKS + " WRITE, " +
            FieldNames.TABLE_MAPPINGS + " WRITE, " +
            FieldNames.TABLE_STATICS + " WRITE, " +
            FieldNames.TABLE_DIFF + " WRITE";

    public final static String GET_LATEST_COVERAGE_ID =
            "SELECT MAX("+FieldNames.COVERAGE_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_COVERAGE;

//    public final static String GET_LATEST_SEQUENCE_ID =
//            "SELECT MAX("+FieldNames.SEQUENCE_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_SEQUENCE;
//
//    public final static String GET_LATEST_READ_ID =
//            "SELECT MAX("+FieldNames.READ_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_READS;
//
//    public final static String GET_LATEST_RUN_ID =
//            "SELECT MAX("+FieldNames.RUN_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_RUN;

    public final static String GET_LATEST_REFERENCE_ID =
            "SELECT MAX("+FieldNames.REF_GEN_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_REF_GEN;

    public final static String GET_LATEST_FEATURE_ID =
            "SELECT MAX("+FieldNames.FEATURE_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_FEATURES;

    public final static String GET_LATEST_TRACK_ID =
            "SELECT MAX("+FieldNames.TRACK_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_TRACKS;

    public final static String GET_LATEST_MAPPING_ID =
            "SELECT MAX("+FieldNames.MAPPING_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_MAPPINGS;

    public final static String GET_LATEST_DIFF_ID =
            "SELECT MAX("+FieldNames.DIFF_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_DIFF;

}
