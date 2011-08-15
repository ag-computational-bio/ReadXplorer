package de.cebitec.vamp.databackend;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class SQLStatements {

    /** For retrieving "NUM" result from result set of a db request. */
    public static final String GET_NUM = "NUM";
    
    /** For retrieving "COVERED" result from result set of a db request. */
    public static final String GET_COVERED = "COVERED";
    
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
    
     public final static String SETUP_STATISTICS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_STATISTICS + " "
            + "( "
            + FieldNames.STATISTICS_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.STATISTICS_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_PERFECT_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_BM_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ + " BIGINT UNSIGNED  NOT NULL, "
            + FieldNames.STATISTICS_NUMBER_READS +" BIGINT UNSIGNED NOT NULL,  "
            + FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS +" BIGINT UNSIGNED NOT NULL,  "
            + FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS +" BIGINT UNSIGNED NOT NULL,  "
            + FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS +" BIGINT UNSIGNED NOT NULL,  "
            + FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS +" BIGINT UNSIGNED NOT NULL  "
            + ") ";

    /**
     * Only needed as long as older databases are floating around and did not
     * already drop this table which was replaced by STATISTICS.
     */
    public static final String DROP_TABLE_STATICS = "DROP TABLE IF EXISTS STATICS";

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
            FieldNames.TRACK_REFERENCE_ID+", " +
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
            FieldNames.MAPPING_SEQUENCE_ID+", " +
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
    
    
        public static final String INSERT_SEQ_PAIR =
            "INSERT INTO "+FieldNames.TABLE_SEQ_PAIRS+
            " (" +
            FieldNames.SEQ_PAIR_ID+", " +
            FieldNames.SEQ_PAIR_PAIR_ID+", " +
            FieldNames.SEQ_PAIR_MAPPING1_ID+", " +
            FieldNames.SEQ_PAIR_MAPPING2_ID+", " +
            FieldNames.SEQ_PAIR_TYPE+", " +
            ") " +
            "VALUES (?,?,?,?,?) ";
        
        
        public static final String INSERT_SEQ_PAIR_REPLICATE = 
            "INSERT INTO " + FieldNames.TABLE_SEQ_PAIR_REPLICATES +
            " (" +
            FieldNames.SEQ_PAIR_MAPPING_ID+", "+
            FieldNames.SEQ_PAIR_NUM_OF_REPLICATES+", " +
            ") "+
            "VALUES (?,?) ";
        
        
        public static final String INSERT_SEQ_PAIR_PIVOT = 
            "INSERT INTO "+FieldNames.TABLE_SEQ_PAIR_PIVOT +
            " (" +
            FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID+", " +
            FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID+" " +
            ") " +
            "VALUES (?,?) ";
        

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

    
    /**
     * Insert statistics data of one track into statistics table.
     */
    public final static String INSERT_STATISTICS =
            "INSERT INTO "+FieldNames.TABLE_STATISTICS+" " +
            "(" +
                FieldNames.STATISTICS_ID+", " +
                FieldNames.STATISTICS_TRACK_ID+", " +
                FieldNames.STATISTICS_NUMBER_OF_MAPPINGS +", " +
                FieldNames.STATISTICS_NUMBER_OF_PERFECT_MAPPINGS +", " +
                FieldNames.STATISTICS_NUMBER_OF_BM_MAPPINGS +", " +
                FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS+", " +
                FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME+", " +
                FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME+", " +
                FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME+", " +
                FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ+ ", "+
                FieldNames.STATISTICS_NUMBER_READS+ ", "+
                FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS+ ", "+
                FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS +",  " +
                FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS +",  " +
                FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS + " " +
            ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * Delete the track data.
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
    
    
    public final static String DELETE_SEQUENCE_PAIRS = //TODO: test delete matepairs
            "DELETE FROM "+
                FieldNames.TABLE_SEQ_PAIRS +" " +
            "WHERE " +
                FieldNames.SEQ_PAIR_MAPPING1_ID + 
                " OR " + 
                FieldNames.SEQ_PAIR_MAPPING2_ID+ 
            " IN " +
            "( " +
                "SELECT "+
                    FieldNames.MAPPING_ID+ " " +
                "FROM "+FieldNames.TABLE_MAPPINGS+" " +
                "WHERE "+FieldNames.MAPPING_TRACK+" = ? " +
            ")";
    
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
                "T."+FieldNames.TRACK_REFERENCE_ID+" "+//", " +
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
                FieldNames.TRACK_REFERENCE_ID+" "+//", "+
                //FieldNames.TRACK_RUN+" " +
            "FROM "+
                FieldNames.TABLE_TRACKS+" " +
            "WHERE "+
                FieldNames.TRACK_REFERENCE_ID+" = ? ";

    
    public final static String FETCH_SNP_DATA_FOR_TRACK_FOR_INTERVAL =
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
    
    
    public final static String GET_LATEST_STATISTICS_ID =
        "SELECT " +
            "MAX("+FieldNames.STATISTICS_ID +") AS LATEST_ID " +
        "FROM " +
            FieldNames.TABLE_STATISTICS;
             

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

//    public final static String FETCH_NUM_UNIQUE_SEQUENCES_FOR_RUN =
//            "SELECT " +
//                "COUNT(S."+FieldNames.SEQUENCE_ID+") as NUM " +
//            "FROM "+
//                FieldNames.TABLE_SEQUENCE+" as S " +
//            "WHERE "+
//                "S."+FieldNames.SEQUENCE_RUN+" = ?";


//    public final static String FETCH_NUM_OF_MAPPINGS_FOR_TRACK_CALCULATE =
//            "SELECT " +
//                "SUM(M." + FieldNames.MAPPING_COUNT + ") as NUM " +
//            "FROM " +
//                FieldNames.TABLE_MAPPINGS + " as M " +
//            "WHERE " +
//                "M." + FieldNames.MAPPING_TRACK + " = ?";
    
    
    public static final String FETCH_NUM_OF_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "SUM(R." + FieldNames.MAPPING_COUNT + ") as NUM " +
            "FROM " +
                "(SELECT " +
                    "M." + FieldNames.MAPPING_SEQUENCE_ID+ " , " + FieldNames.MAPPING_COUNT + //DISTINCT returns mapping count for unique sequences
                " FROM " +
                    FieldNames.TABLE_MAPPINGS + " as M " +
                "WHERE " +
                    "M." + FieldNames.MAPPING_TRACK + " = ? " +
                ") as R;";
    

    public static final String FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK =
            "SELECT " +
                FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS + " as NUM " +
            "FROM "+
                FieldNames.TABLE_STATISTICS+ " as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID + " = ?";
    

    public static final String FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(M."+FieldNames.MAPPING_ID+") as NUM " +
            "FROM "+
                FieldNames.TABLE_MAPPINGS + " as M " +
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK+" = ?";

    
   public final static String FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK =
        "SELECT " +
            "S."+FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ +" as NUM " +
        "FROM "+
            FieldNames.TABLE_STATISTICS+" as S " +
        "WHERE "+
            "S."+FieldNames.TRACK_ID+" = ?" ;

    /**
     * Number of unique sequences is the same as the number of distinct mapped
     * sequences. The number of all sequences would be the number of reads.
     */
    public final static String FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(DISTINCT M."+FieldNames.MAPPING_SEQUENCE_ID+") as NUM "+
            "FROM "+ 
                FieldNames.TABLE_MAPPINGS+" as M " +
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK+" = ?";
    
    
    public final static String FETCH_NUM_SEQ_PAIRS_FOR_TRACK =
        "SELECT " +
            "S."+FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS +" as NUM " +
        "FROM "+
            FieldNames.TABLE_STATISTICS+" as S " +
        "WHERE "+
            "S."+FieldNames.TRACK_ID+" = ?" ;


    /**
     * Don't use yet, not working!
     */
    public final static String FETCH_NUM_SEQ_PAIRS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(DISTINCT S."+FieldNames.SEQ_PAIR_ID+") as NUM "+
            "FROM "+ 
                FieldNames.TABLE_SEQ_PAIR_PIVOT+" as S " +
            "WHERE "+ //TODO:mappingid holen & track abgleichen...
                "S."+FieldNames.MAPPING_TRACK+" = ?";
    
    //TODO: add calculate method
    public final static String FETCH_NUM_PERFECT_SEQ_PAIRS_FOR_TRACK =
        "SELECT " +
            "S."+FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS +" as NUM " +
        "FROM "+
            FieldNames.TABLE_STATISTICS+" as S " +
        "WHERE "+
            "S."+FieldNames.TRACK_ID+" = ?" ;
    
    
    public final static String FETCH_NUM_UNIQUE_SEQ_PAIRS_FOR_TRACK =
        "SELECT " +
            "S."+FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS +" as NUM " +
        "FROM "+
            FieldNames.TABLE_STATISTICS+" as S " +
        "WHERE "+
            "S."+FieldNames.TRACK_ID+" = ?" ;
        
    
    public final static String FETCH_NUM_UNIQUE_PERFECT_SEQ_PAIRS_FOR_TRACK =
        "SELECT " +
            "S."+FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS +" as NUM " +
        "FROM "+
            FieldNames.TABLE_STATISTICS+" as S " +
        "WHERE "+
            "S."+FieldNames.TRACK_ID+" = ?" ;


    public final static String FETCH_NUM_BM_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(M."+FieldNames.MAPPING_ID+") as NUM " +
            " FROM "+
                FieldNames.TABLE_MAPPINGS+" as M " +
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK+" = ? AND "+
                "M."+FieldNames.MAPPING_BEST_MAPPING+" = 1 ";
    
    /*
     * eine seq id = mehrere reads & mehrere mappings -> anzahl mappings/seq id z.b. 10 = 10 versch. pos. = müssen immer dieselben (max. X) reads sein
     * z.b. können 20 reads auf selbe seq id kommen + 10 versch pos abdecken = 10 mappings mit je 20 replicates
     * 20 reads auf 10pos = 10 unique mappings + je 20 replicates
     * nimm 1 der unique mapping mit gleicher seq id & zähle replicates = 20 hieße 20 reads, kann die 20 noch anderweitig zustande kommen?
     */
    //public final static String FETCH_NUM_READS_FOR_TRACK_CALCULATE = //TODO: implement
    //        "SELECT " + "";


//    public final static String FETCH_RUNID_FOR_TRACK =
//            "SELECT "+
//                "T."+FieldNames.TRACK_RUN+" " +
//            "FROM "+
//                FieldNames.TABLE_TRACKS+" as T " +
//            "WHERE "+
//                "T."+FieldNames.TRACK_ID+" = ?";
    

         //Get values from table STATISTICS
        public final static String FETCH_PERFECT_COVERAGE_OF_GENOME =
            "SELECT " +
            FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME+" as COVERED "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";

         public final static String FETCH_BM_COVERAGE_OF_GENOME =
            "SELECT " +
            FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME+" as COVERED "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";

         
         public final static String FETCH_COMPLETE_COVERAGE_OF_GENOME =
            "SELECT " +
                FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME+" as COVERED "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";
    
    
    public static final String FETCH_NUM_MAPPED_SEQUENCES_FOR_TRACK_CALCULATE =
            "SELECT " +
                "SUM(R." + FieldNames.MAPPING_COUNT + ") as NUM " +
            "FROM " +
                "(SELECT " +
                    "M." + FieldNames.MAPPING_SEQUENCE_ID+ " , " + FieldNames.MAPPING_COUNT + //DISTINCT returns mapping count for unique sequences
                " FROM " +
                    FieldNames.TABLE_MAPPINGS + " as M " +
                "WHERE " +
                    "M." + FieldNames.MAPPING_TRACK + " = ? " +
                ") as R;";
    
    
    public final static String FETCH_NUM_MAPPINGS_FOR_TRACK =
            "SELECT " +
                FieldNames.STATISTICS_NUMBER_OF_MAPPINGS+" as Num "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";
    
   public final static String FETCH_NUM_MAPPINGS_FOR_TRACK_CALCULATE =
        "SELECT " +
            "COUNT(M."+FieldNames.MAPPING_ID+") as NUM " +
        " FROM "+
            FieldNames.TABLE_MAPPINGS+" as M " +
        "WHERE "+
            "M."+FieldNames.MAPPING_TRACK+" = ? ";

    
    public final static String FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK =
            "SELECT " +
                FieldNames.STATISTICS_NUMBER_OF_PERFECT_MAPPINGS+" as Num "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";
    
    
    public final static String FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK_CALCULATE =
            "SELECT " +
                "COUNT(M."+FieldNames.MAPPING_ID+") as NUM " +
            " FROM "+
                FieldNames.TABLE_MAPPINGS+ " as M "+
            "WHERE "+
                "M."+FieldNames.MAPPING_TRACK +" = ? and "+
                "M."+FieldNames.MAPPING_NUM_OF_ERRORS+" = 0 ";


    public final static String FETCH_NUM_BM_MAPPINGS_FOR_TRACK =
            "SELECT " +
                FieldNames.STATISTICS_NUMBER_OF_BM_MAPPINGS+" as Num "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";
    

    public final static String FETCH_NUM_UNIQUE_SEQ_FOR_TRACK =
            "SELECT " +
                FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ+" as Num "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";
    
    
    public final static String FETCH_NUM_READS_FOR_TRACK =
            "SELECT " +
                FieldNames.STATISTICS_NUMBER_READS+" as Num "+
            " FROM "+
                FieldNames.TABLE_STATISTICS+" as S " +
            "WHERE "+
                "S."+FieldNames.STATISTICS_TRACK_ID+" = ?";


    public final static String FETCH_GENOMEID_FOR_TRACK =
            "SELECT "+
                FieldNames.TRACK_REFERENCE_ID+" " +
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
//           FieldNames.READ_NAME + "= ?  AND M."+FieldNames.MAPPING_SEQUENCE_ID + " = R." +FieldNames.READ_SEQUENCE+" AND M." +FieldNames.MAPPING_TRACK+" = ? " ;


    
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
    
   public static final String GET_LATEST_SEQUENCE_PAIR_ID =
            "SELECT MAX("+FieldNames.SEQ_PAIR_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_SEQ_PAIRS;
   
   public static final String GET_LATEST_SEQUENCE_PAIR_PAIR_ID =
            "SELECT MAX("+FieldNames.SEQ_PAIR_PAIR_ID+") AS LATEST_ID FROM "+FieldNames.TABLE_SEQ_PAIRS;
   
}
