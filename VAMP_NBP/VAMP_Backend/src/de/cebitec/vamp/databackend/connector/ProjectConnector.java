package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.*;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.dataObjects.Snp;
import de.cebitec.vamp.databackend.dataObjects.SnpI;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.PositionUtils;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceComparison;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.h2.jdbc.JdbcSQLException;
import org.openide.util.NbBundle;

/**
 * Responsible for the connection between user interface and data base.
 * Contains the methods to communicate with the data base.
 *
 * @author ddoppmeier, rhilker
 */
public class ProjectConnector {

    private static ProjectConnector dbConnector;
    private Connection con;
    private String url;
    private String user;
    private String password;
    private String adapter;
    private HashMap<Integer, TrackConnector> trackConnectors;
//    private HashMap<Integer, MultiTrackConnector> multiTrackConnectors;
    private List<MultiTrackConnector> multiTrackConnectors;
    private HashMap<Integer, ReferenceConnector> refConnectors;
    private static final int BATCH_SIZE = 100000; //TODO: test larger batch sizes
    private final static int ANNOTATION_BATCH_SIZE = BATCH_SIZE;
    private final static int COVERAGE_BATCH_SIZE = BATCH_SIZE * 3;
    private final static int MAPPING_BATCH_SIZE = BATCH_SIZE;
    private static final int SEQPAIR_BATCH_SIZE = BATCH_SIZE;
    private static final int SEQPAIR_PIVOT_BATCH_SIZE = BATCH_SIZE + 25000;
    private final static int DIFF_BATCH_SIZE = BATCH_SIZE;
    private final static String BIGINT_UNSIGNED = "BIGINT UNSIGNED";
    private final static String INT_UNSIGNED = "INT UNSIGNED";
    private static final String VARCHAR400 = "VARCHAR(400)";
    
    private static final int BASE_A = 0;
    private static final int BASE_C = 1;
    private static final int BASE_G = 2;
    private static final int BASE_T = 3;
    private static final int BASE_N = 4;
    private static final int BASE_GAP = 5;
    private static final int GAP_A = 6;
    private static final int GAP_C = 7;
    private static final int GAP_G = 8;
    private static final int GAP_T = 9;
    private static final int GAP_N = 10;
    private static final int DIFFS = 11;
    private int numReads = 0;
    private int noUniqueSeq = 0;
    private int noUniqueMappings = 0;
    private int numMappings = 0;
    private int numPerfectMappings = 0;
    private int numBmMappings = 0;
    private int sumReadLength = 0;
    private int currentTrackID = -1;
    private boolean isLastTrack = false;
    
    private ProjectConnector() {
        trackConnectors = new HashMap<>();
//        multiTrackConnectors = new HashMap<Integer, MultiTrackConnector>();
        multiTrackConnectors = new ArrayList<>();
        refConnectors = new HashMap<>();
    }

    private void cleanUp() {
        trackConnectors.clear();
        refConnectors.clear();
    }

    public static synchronized ProjectConnector getInstance() {
        if (dbConnector == null) {
            dbConnector = new ProjectConnector();
        }
        return dbConnector;
    }

    /**
     * @return All tracks stored in the database with all their information.
     */
    public List<PersistantTrack> getTracks() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading track data from database");
        List<PersistantTrack> tracks = new ArrayList<>();

        try {
            PreparedStatement fetchTracks = con.prepareStatement(SQLStatements.FETCH_TRACKS);
            ResultSet rs = fetchTracks.executeQuery();

            while (rs.next()) {
                int id = rs.getInt(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                int refGenID = rs.getInt(FieldNames.TRACK_REFERENCE_ID);
                String filePath = rs.getString(FieldNames.TRACK_PATH);
                int seqPairId = rs.getInt(FieldNames.TRACK_SEQUENCE_PAIR_ID);
                tracks.add(new PersistantTrack(id, filePath, description, date, refGenID, seqPairId));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tracks;
    }

    public boolean isConnected() {
        if (con != null) {
            try {
                return con.isValid(0);
            } catch (SQLException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    public void disconnect() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Closing database connection");
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            con = null;
            this.cleanUp();
        }
    }

    /**
     * Connects to the adapter used for the current project. Can either be a database
     * adapter for h2 or mysql or an adapter for direct file access.
     * @param adapter the adapter type to use for the current project
     * @param projectLocation the project location
     * @param hostname the hostname, if we connect to a mysql database
     * @param user the user name, if we connect to a mysql database
     * @param password the password, if we connect to a mysql database
     * @throws SQLException
     * @throws JdbcSQLException 
     */
    public void connect(String adapter, String projectLocation, String hostname, String user, String password) throws SQLException, JdbcSQLException {
        this.adapter = adapter;
        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.url = "jdbc:" + adapter + "://" + hostname + "/" + projectLocation;
            this.user = user;
            this.password = password;
            this.connect(url, user, password);
            this.setupMySQLDatabase();
        } else if (adapter.equalsIgnoreCase(Properties.ADAPTER_H2)) {
            this.url = "jdbc:" + adapter + ":" + projectLocation + ";AUTO_SERVER=TRUE;MULTI_THREADED=1";
            //;FILE_LOCK=SERIALIZED"; that works temporary but now using AUTO_SERVER

            this.connectH2DataBase(url);
            this.setupDatabaseH2();
//        } else { //means: if (adapter.equalsIgnoreCase(Properties.ADAPTER_DIRECT_ACCESS)) {
//            this.connectToProject(projectLocation);
        }
    }

    
    private void connect(String url, String user, String password) throws SQLException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Connecting to database");
        con = DriverManager.getConnection(url, user, password);
        con.setAutoCommit(true);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Successfully connected to database");
    }

    
    private void connectH2DataBase(String url) throws SQLException, JdbcSQLException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Connecting to database");
        con = DriverManager.getConnection(url);
        con.setAutoCommit(true);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Successfully connected to database");
    }
    
//    private void connectToProject(String projectLocation) {
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Connecting to project");
//        //TODO: write code for connecting to a project...
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Successfully connected to project");
//    }

    
    private void setupDatabaseH2() {

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Setting up tables and indices if not existent");

            con.setAutoCommit(false);
            //create tables if not exist yet
//            con.prepareStatement(SQLStatements.SETUP_PROJECT_FOLDER).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_POSITIONS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_POSITIONS).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_DIFFS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_DIFF).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_COVERAGE).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_COVERAGE).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_ANNOTATIONS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_ANNOTATIONS).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_SUBANNOTATIONS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SUBANNOTATION_PARENT_ID).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SUBANNOTATION_REF_ID).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_MAPPINGS).executeUpdate();
//            con.prepareStatement(H2SQLStatements.INDEX_MAPPINGS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_MAPPING_START).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_MAPPING_STOP).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_MAPPING_SEQ_ID).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_TRACKS).execute();
            con.prepareStatement(H2SQLStatements.INDEX_TRACK_REFID).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_TRACK_SEQ_PAIR_ID).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_SEQ_PAIRS).execute();
            con.prepareStatement(H2SQLStatements.INDEX_SEQ_PAIR_PAIR_ID).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SEQ_PAIR_MAPPING1_ID).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SEQ_PAIR_MAPPING2_ID).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_SEQ_PAIR_REPLICATES).execute();
            con.prepareStatement(H2SQLStatements.INDEX_SEQ_PAIR_REPLICATES).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_SEQ_PAIR_PIVOT).execute();
            con.prepareStatement(H2SQLStatements.INDEX_SEQ_PAIR_PIVOT_MID).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SEQ_PAIR_PIVOT_SID).executeUpdate();
            
            con.prepareStatement(SQLStatements.SETUP_STATISTICS).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_COVERAGE_DISTRIBUTION).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_COVERAGE_DIST).executeUpdate();

            this.checkDBStructure();

            con.commit();
            con.setAutoCommit(true);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished creating tables and indices if not existent before");

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
    }

    private void setupMySQLDatabase() {

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Setting up tables and indices if not existent");

            con.setAutoCommit(false);
            //create tables if not exist yet
//            con.prepareStatement(SQLStatements.SETUP_PROJECT_FOLDER).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_POSITIONS).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_DIFFS).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_COVERAGE).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_ANNOTATIONS).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_SUBANNOTATIONS).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_MAPPINGS).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_TRACKS).execute();
            con.prepareStatement(MySQLStatements.SETUP_SEQ_PAIRS).execute();
            con.prepareStatement(MySQLStatements.SETUP_SEQ_PAIR_REPLICATES).execute();
            con.prepareStatement(MySQLStatements.SETUP_SEQ_PAIR_PIVOT).execute();
            con.prepareStatement(SQLStatements.SETUP_STATISTICS).execute();
            con.prepareStatement(MySQLStatements.SETUP_COVERAGE_DISTRIBUTION).executeUpdate();

            this.checkDBStructure();

            con.commit();
            con.setAutoCommit(true);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished creating tables and indices if not existent before");

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
    }

    /**
     * Any additional columns which were added to existing tables in newer 
     * VAMP versions should be checked by this method to ensure correct database 
     * structure and avoiding errors when SQL statements request one of these
     * columns, which are not existent in older databases.
     */
    private void checkDBStructure() {
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Checking DB structure...");

        //remove statics table (replaced by STATISTICS table)
        this.runSqlStatement(SQLStatements.DROP_TABLE_STATICS);

        //stats table
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUMBER_READS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_SINGLE_MAPPINGS, BIGINT_UNSIGNED));

        //add sequence pair id column in tracks if not existent
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_TRACK, FieldNames.TRACK_SEQUENCE_PAIR_ID, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_FEATURES, FieldNames.ANNOTATION_GENE, "VARCHAR (20)"));
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_AVERAGE_READ_LENGTH, INT_UNSIGNED));
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH, INT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_TRACK, FieldNames.TRACK_PATH, VARCHAR400));
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished checking DB structure.");
        
    }
    
    /**
     * Runs a single sql statement.
     * @param statement sql statement to run
     */
    private void runSqlStatement(String statement) {

        try {
            con.prepareStatement(statement).executeUpdate();
        } catch (SQLException ex) {
            this.checkRollback(ex);
        }
    }

    /**
     * Checks if a rollback is needed or if the SQLException originated from a
     * duplicate column name error.
     * @param ex SQL exception to check
     */
    private void checkRollback(SQLException ex) {
        if (!ex.getMessage().contains("Duplicate column name")) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
    }

    /**
     * If the current transaction tried to make changes in the DB, these changes are
     * rolled back.
     * @param className name of the class in which the error occured
     * @param ex the exception, which was thrown
     */
    public void rollbackOnError(String className, Exception ex) {

        Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, "Error occured. Trying to recover", ex);
        try {
            if (!con.isClosed()) {
                //connection is still open. try rollback
                con.rollback();
                Logger.getLogger(ProjectConnector.class.getName()).log(Level.INFO, "Successfully rolled back");
            } else {
                //connection was closed before, open a new one
                this.connect(url, user, password);
            }

        } catch (SQLException ex1) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.INFO, "Rollback failed", ex1);
        }
    }

    /*
     * Method only for development of database and testing of import functionality.
     * Never use this in productive environment.
     */
    private void deleteAllTables() {
        try {
            con.setAutoCommit(false);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting all database tables");
            ResultSet rs = con.prepareStatement("show tables").executeQuery();
            while (rs.next()) {
                String table = rs.getString(1);
                con.prepareStatement("drop table " + table).executeUpdate();
            }
            con.commit();
            con.setAutoCommit(true);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Successfully deleted all data");
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, "Deletion of data failed", ex);
        }
    }


    /**
     * Unlocks tables in mysql fashion.
     */
    private void unlockTables() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start unlocking tables");
        try {
            con.setAutoCommit(false);
            Statement unlock = con.createStatement();
            unlock.execute(MySQLStatements.UNLOCK_TABLES);
            unlock.close();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done unlocking tables");
    }

    
    private void disableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start disabling reference data domain indexing...");
        this.disableDomainIndices(MySQLStatements.DISABLE_REFERENCE_INDICES, null);
        this.disableDomainIndices(MySQLStatements.DISABLE_ANNOTATION_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done disabling reference data domain indexing");
    }

    private void enableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start enabling reference data domain indexing...");
        this.enableDomainIndices(MySQLStatements.ENABLE_REFERENCE_INDICES, null);
        this.enableDomainIndices(MySQLStatements.ENABLE_ANNOTATION_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done enabling reference data domain indexing");
    }
    
//    /**
//     * Stores the project folder location in the database.
//     * @param projectFolder the path to the project folder associated to this database
//     */
//    public boolean storeProjectFolder(String projectFolder) {
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing project folder...");
//        
//        try {
//            PreparedStatement insertProjectFolder = con.prepareStatement(SQLStatements.INSERT_PROJECT_FOLDER);
//
//            // store project folder
//            insertProjectFolder.setString(1, projectFolder);
//            insertProjectFolder.execute();
//
//            con.commit();
//
//            insertProjectFolder.close();
//
//        } catch (SQLException ex) {
//            this.rollbackOnError(this.getClass().getName(), ex);
//            return false;
//        }
//
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting project folder"); 
//        return true;        
//    }

    private void storeGenome(ParsedReference reference) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing reference sequence data...");
        try {
            int id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_REFERENCE_ID, con);
            reference.setID(id);
            PreparedStatement insertGenome = con.prepareStatement(SQLStatements.INSERT_REFGENOME);

            // store reference data
            insertGenome.setLong(1, reference.getID());
            insertGenome.setString(2, reference.getName());
            insertGenome.setString(3, reference.getDescription());
            insertGenome.setString(4, reference.getSequence());
            insertGenome.setTimestamp(5, reference.getTimestamp());
            insertGenome.execute();

            con.commit();

            insertGenome.close();

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting reference sequence data");
    }

    private void storeAnnotations(ParsedReference reference) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start inserting annotations...");
        try {
            long id = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_ANNOTATION_ID, con);
            PreparedStatement insertAnnotation = con.prepareStatement(SQLStatements.INSERT_ANNOTATION);
            PreparedStatement insertSubAnnotation = con.prepareStatement(SQLStatements.INSERT_SUBANNOTATION);

            int batchCounter = 1;
            int batchCountSubfeat = 1;
            int referenceId = reference.getID();
            Iterator<ParsedAnnotation> featIt = reference.getAnnotations().iterator();
            while (featIt.hasNext()) {
                
                batchCounter++;
                ParsedAnnotation annotation = featIt.next();
                insertAnnotation.setLong(1, id);
                insertAnnotation.setLong(2, referenceId);
                insertAnnotation.setInt(3, annotation.getType().getTypeInt());
                insertAnnotation.setInt(4, annotation.getStart());
                insertAnnotation.setInt(5, annotation.getStop());
                insertAnnotation.setString(6, annotation.getLocusTag());
                insertAnnotation.setString(7, annotation.getProduct());
                insertAnnotation.setString(8, annotation.getEcNumber());
                insertAnnotation.setInt(9, annotation.getStrand());
                insertAnnotation.setString(10, annotation.getGeneName());
                insertAnnotation.addBatch();

                for (ParsedSubAnnotation subAnnotation : annotation.getSubAnnotations()) {
                    batchCountSubfeat++;
                    insertSubAnnotation.setLong(1, id);
                    insertSubAnnotation.setLong(2, referenceId);
                    insertSubAnnotation.setInt(3, subAnnotation.getType().getTypeInt());
                    insertSubAnnotation.setInt(4, subAnnotation.getStart());
                    insertSubAnnotation.setInt(5, subAnnotation.getStop());
                    insertSubAnnotation.addBatch();
                    
                    if (batchCountSubfeat == ANNOTATION_BATCH_SIZE) {
                        batchCountSubfeat = 1;
                        insertSubAnnotation.executeBatch();
                    }
                }
                
                if (batchCounter == ANNOTATION_BATCH_SIZE) {
                    batchCounter = 1;
                    insertAnnotation.executeBatch();
                }
                ++id;
                //  it.remove();
            }

            insertAnnotation.executeBatch();
            insertSubAnnotation.executeBatch();
            insertAnnotation.close();
            insertSubAnnotation.close();


        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting annotations");
    }

    private void lockReferenceDomainTables() {
        this.lockDomainTables(MySQLStatements.LOCK_TABLE_REFERENCE_DOMAIN, "reference");
    }

    public int addRefGenome(ParsedReference reference) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing reference sequence  \"{0}\"", reference.getName());

        try {
            con.setAutoCommit(false);

            if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
                this.lockReferenceDomainTables();
                this.disableReferenceIndices();
            }

            this.storeGenome(reference);
            this.storeAnnotations(reference);

            if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
                this.enableReferenceIndices();
                this.unlockTables();
            }

            con.setAutoCommit(true);
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished storing reference sequence \"{0}\"", reference.getName());
        return reference.getID();
    }

    private void storeCoverage(ParsedTrack track) {
        if (!track.isStepwise() | isLastTrack) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing coverage information...");
            try {
                PreparedStatement insertCoverage = con.prepareStatement(SQLStatements.INSERT_COVERAGE);
                // get the latest used coverage id
                int id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_COVERAGE_ID, con);

                // insert coverage for track
                int batchCounter = 1;
                int coveredPerfectPos = 0;
                int coveredBestMatchPos = 0;
                int coveredCommonMatchPos = 0;
                CoverageContainer cov = track.getCoverageContainer();
                Iterator<Integer> covsIt = cov.getCoveredPositions().iterator();
                while (covsIt.hasNext()) {
                    batchCounter++;
                    int pos = covsIt.next();
               
                        insertCoverage.setLong(1, id++);
                        insertCoverage.setLong(2, track.getID());
                        insertCoverage.setInt(3, pos);
                        insertCoverage.setInt(4, cov.getBestMappingForwardCoverage(pos));
                        insertCoverage.setInt(5, cov.getNumberOfBestMappingsForward(pos));
                        insertCoverage.setInt(6, cov.getBestMappingReverseCoverage(pos));
                        insertCoverage.setInt(7, cov.getNumberOfBestMappingsReverse(pos));
                        insertCoverage.setInt(8, cov.getZeroErrorMappingsForwardCoverage(pos));
                        insertCoverage.setInt(9, cov.getNumberOfZeroErrorMappingsForward(pos));
                        insertCoverage.setInt(10, cov.getZeroErrorMappingsReverseCoverage(pos));
                        insertCoverage.setInt(11, cov.getNumberOfZeroErrorMappingsReverse(pos));
                        insertCoverage.setInt(12, cov.getNErrorMappingsForwardCoverage(pos));
                        insertCoverage.setInt(13, cov.getNumberOfNErrorMappingsForward(pos));
                        insertCoverage.setInt(14, cov.getNErrorMappingsReverseCoverage(pos));
                        insertCoverage.setInt(15, cov.getNumberOfNErrorMappingsReverse(pos));
                        insertCoverage.addBatch();
                        if (batchCounter == COVERAGE_BATCH_SIZE) {
                            batchCounter = 0;
                            insertCoverage.executeBatch();
                        }
                        if (cov.getNErrorMappingsForwardCoverage(pos) > 0) {
                            ++coveredCommonMatchPos;
                            if (cov.getBestMappingForwardCoverage(pos) > 0) {
                                ++coveredBestMatchPos;
                                if (cov.getZeroErrorMappingsForwardCoverage(pos) > 0) {
                                    ++coveredPerfectPos;
                                }
                            }
                        }
                        if (cov.getNErrorMappingsReverseCoverage(pos) > 0) {
                            ++coveredCommonMatchPos;
                            if (cov.getBestMappingReverseCoverage(pos) > 0) {
                                ++coveredBestMatchPos;
                                if (cov.getZeroErrorMappingsReverseCoverage(pos) > 0) {
                                    ++coveredPerfectPos;
                                }
                            }
                        }
                }
                insertCoverage.executeBatch();
                insertCoverage.close();
                
                //here we get the calculations for free, so we store it in this step
                cov.setCoveredPerfectPositions(coveredPerfectPos);
                cov.setCoveredBestMatchPositions(coveredBestMatchPos);
                cov.setCoveredCommonMatchPositions(coveredCommonMatchPos);
                
            } catch (SQLException ex) {
                this.rollbackOnError(this.getClass().getName(), ex);
            }
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing coverage information");
        }
    }

    /**
     * Adds a track to the database with its file path. This means, it is stored
     * as a track for direct file access and adds the persistant track id to the 
     * track job.
     * @param trackJob the track job containing the track information to store
     */
    public void storeDirectAccessTrack(TrackJob trackJob) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing direct access track data...");       
        
        try (PreparedStatement insertTrack = con.prepareStatement(SQLStatements.INSERT_TRACK)) {
            insertTrack.setLong(1, trackJob.getID());
            insertTrack.setLong(2, trackJob.getRefGen().getID());
            insertTrack.setString(3, trackJob.getDescription());
            insertTrack.setTimestamp(4, trackJob.getTimestamp());
            insertTrack.setString(5, trackJob.getFile().getAbsolutePath());
            insertTrack.execute();

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing direct access track data");        
    }
    
    /**
     * Method explicitly storing a track in the database. This means all basic
     * information of a track, which is stored in the track table of the db.
     * @param track the track whose data is to be stored
     */
    private void storeTrack(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track data...");
        try {
            if (track.isFirstTrack() || !track.isStepwise()) {
                try (PreparedStatement insertTrack = con.prepareStatement(SQLStatements.INSERT_TRACK)) {
                    insertTrack.setLong(1, track.getID());
                    insertTrack.setLong(2, track.getRefId());
                    insertTrack.setString(3, track.getDescription());
                    insertTrack.setTimestamp(4, track.getTimestamp());
                    if (!track.isDbUsed()) {
                        insertTrack.setString(5, track.getFile().getAbsolutePath());
                    } else {
                        insertTrack.setString(5, "");
                    }
                    insertTrack.execute();
                }
            }
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track data");
    }


    private void storeTrackStatistics(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track statistics data...");

        int coveragePerf;
        int coverageBM;
        int coverageComplete;
        long trackID = track.getID();
        try {
            HashMap<Integer, Integer> mappingInfos = track.getParsedMappingContainer().getMappingInformations();
            numMappings += mappingInfos.get(1);
            numPerfectMappings += mappingInfos.get(2);
            numBmMappings += mappingInfos.get(3);
            noUniqueMappings += mappingInfos.get(4); 
            noUniqueSeq += mappingInfos.get(5);
            numReads += mappingInfos.get(6);
            sumReadLength += mappingInfos.get(7);
        } catch (NullPointerException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "...can't get the statistics list (MappingInfos are null)");
        }
        try {
            if (!track.isStepwise() || this.isLastTrack) {
            // get latest id for track
            long id = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_STATISTICS_ID, con);

                CoverageContainer cov = track.getCoverageContainer();
                if (cov.getCoveredPerfectPositions() > 0) {
                    coveragePerf = cov.getCoveredPerfectPositions();
                } else {
                    coveragePerf = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_OF_PERFECT_POSITIONS_FOR_TRACK, 
                            SQLStatements.GET_NUM, con, trackID);
                }
                
                if (cov.getCoveredBestMatchPositions() > 0) {
                    coverageBM = cov.getCoveredBestMatchPositions();
                } else {
                    coverageBM = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_BM_COVERAGE_OF_GENOME_CALCULATE, 
                            SQLStatements.GET_NUM, con, trackID);
                }
                
                if (cov.getCoveredCommonMatchPositions() > 0) {
                    coverageComplete = cov.getCoveredCommonMatchPositions();
                } else {
                    coverageComplete = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_COVERED_POSITIONS, 
                            SQLStatements.GET_NUM, con, trackID);
                }
                
                //calculate average read length
                int averageReadLength = this.numMappings != 0 ? this.sumReadLength / this.numMappings : 0;
                
                PreparedStatement insertStatistics = con.prepareStatement(SQLStatements.INSERT_STATISTICS);
                // store track in table
                insertStatistics.setLong(1, id);
                insertStatistics.setLong(2, trackID);
                insertStatistics.setInt(3, this.numMappings);
                insertStatistics.setInt(4, this.numPerfectMappings);
                insertStatistics.setInt(5, this.numBmMappings);
                insertStatistics.setInt(6, this.noUniqueMappings);
                insertStatistics.setInt(7, coveragePerf);
                insertStatistics.setInt(8, coverageBM);
                insertStatistics.setInt(9, coverageComplete);
                insertStatistics.setInt(10, this.noUniqueSeq);
                insertStatistics.setInt(11, this.numReads);
                insertStatistics.setInt(12, averageReadLength);
                insertStatistics.execute();
                insertStatistics.close();
                this.numMappings = 0;
                this.numPerfectMappings = 0;
                this.numBmMappings = 0;
                this.noUniqueSeq = 0;
                this.noUniqueMappings = 0;
                this.numReads = 0;
                this.sumReadLength = 0;
            }

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track statistics data");
    }

    
    private void storeSeqPairTrackStatistics(ParsedSeqPairContainer seqPairContainer) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing sequence pair statistics...");

        long numSeqPairs = seqPairContainer.getNumOfSeqPairs();
        long numPerfSeqPairs = seqPairContainer.getNumOfPerfectSPs();
        long numUniqueSeqPairs = seqPairContainer.getNumOfUniqueSPs();
        long numUniquePerfSeqPairs = seqPairContainer.getNumUniquePerfectSPs();
        long numSingleMappings = seqPairContainer.getNumOfSingleMappings();

        for (int i = 0; i < 2; ++i) {
            try {
                PreparedStatement addStatistics = con.prepareStatement(SQLStatements.ADD_SEQPAIR_STATISTICS);
                long id = i == 0 ? seqPairContainer.getTrackId1() : seqPairContainer.getTrackId2();

                // store track in table
                addStatistics.setLong(1, numSeqPairs);
                addStatistics.setLong(2, numPerfSeqPairs);
                addStatistics.setLong(3, numUniqueSeqPairs);
                addStatistics.setLong(4, numUniquePerfSeqPairs);
                addStatistics.setLong(5, numSingleMappings);
                addStatistics.setLong(6, id);
                addStatistics.setLong(7, seqPairContainer.getAverage_Seq_Pair_length());
                addStatistics.execute();

                addStatistics.close();

            } catch (SQLException ex) {
                this.rollbackOnError(this.getClass().getName(), ex);
            }
        }
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing sequence pair statistics");
    }

    private void storeMappings(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing mapping data...");
        try {
            long mappingID = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_MAPPING_ID, con);
            //TODO: test cache or increase cache temporarily to improve performance: 
            //SELECT * FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'info.CACHE_MAX_SIZE'
            //jdbc:h2:~/test;CACHE_SIZE=65536;
            PreparedStatement insertMapping = con.prepareStatement(SQLStatements.INSERT_MAPPING);

            // start storing the mappings
            int batchCounter = 1;
            //sequence ids can be the same in different tracks, track id has to be checked then
            Iterator<Integer> sequenceIDIterator = track.getParsedMappingContainer().getMappedSequenceIDs().iterator();
            while (sequenceIDIterator.hasNext()) { //sequence ids aller unterschiedlichen readsequenzen = unique reads
                int sequenceID = sequenceIDIterator.next();
                List<ParsedMapping> c = track.getParsedMappingContainer().getParsedMappingGroupBySeqID(sequenceID).getMappings();
                Iterator<ParsedMapping> mappingsIt = c.iterator(); //eine mapping group, alle gleiche seq id, untersch. pos
                while (mappingsIt.hasNext()) {
                    ParsedMapping m = mappingsIt.next(); //einzelnes mapping
                    m.setID(mappingID); //from now on id is set and can be used for sequence pairs for example!

                    insertMapping.setLong(1, mappingID); //einzigartig
                    insertMapping.setInt(2, m.getStart()); //einzigartig im zusammenhang
                    insertMapping.setInt(3, m.getStop()); //einzigartig im zusammenhang
                    insertMapping.setInt(4, (m.isBestMapping() ? 1 : 0));
                    insertMapping.setInt(5, m.getNumReplicates()); //count der gleichen seq (seq id) in reads
                    insertMapping.setByte(6, m.getDirection());
                    insertMapping.setInt(7, m.getErrors());
                    insertMapping.setInt(8, sequenceID); // mappings der gleichen seq an anderer stelle enthalten sie auch +
                    insertMapping.setLong(9, track.getID()); //gleichen count

                    insertMapping.addBatch();

                    if (batchCounter == MAPPING_BATCH_SIZE) {
                        insertMapping.executeBatch();
                        batchCounter = 0;
                    }

                    mappingID++;
                    batchCounter++;
                }
            }
            insertMapping.executeBatch();

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing mapping data");
    }

    private void storeDiffs(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start inserting diff data...");

        try {
            long diffID = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_DIFF_ID, con);
            PreparedStatement insertDiff = con.prepareStatement(SQLStatements.INSERT_DIFF);
            PreparedStatement insertGap = con.prepareStatement(SQLStatements.INSERT_GAP);

            // byte flags for diffs
            byte gap = 0;
            byte diff = 1;

            // insert data
            int batchCounter = 0;
            Iterator<Integer> sequenceIDIterator = track.getParsedMappingContainer().getMappedSequenceIDs().iterator();
            while (sequenceIDIterator.hasNext()) {
                int sequenceID = sequenceIDIterator.next();
                List<ParsedMapping> c = track.getParsedMappingContainer().getParsedMappingGroupBySeqID(sequenceID).getMappings();
                Iterator<ParsedMapping> mappingsIt = c.iterator();

                // iterate mappings
                while (mappingsIt.hasNext()) {
                    ParsedMapping m = mappingsIt.next();

                    // iterate diffs (non-gap variation)
                    Iterator<ParsedDiff> diffsIt = m.getDiffs().iterator();
                    while (diffsIt.hasNext()) {

                        batchCounter++;

                        ParsedDiff d = diffsIt.next();
                        insertDiff.setLong(1, diffID++);
                        insertDiff.setLong(2, m.getID());
                        insertDiff.setString(3, Character.toString(d.getBase()));
                        insertDiff.setLong(4, d.getPosition());
                        insertDiff.setByte(5, diff);

                        insertDiff.addBatch();

                        if (batchCounter == DIFF_BATCH_SIZE) {
                            insertDiff.executeBatch();
                            batchCounter = 0;
                        }
                    }

                    // iterate gaps
                    Iterator<ParsedReferenceGap> gapIt = m.getGenomeGaps().iterator();
                    while (gapIt.hasNext()) {
                        batchCounter++;

                        ParsedReferenceGap g = gapIt.next();
                        insertGap.setLong(1, diffID++);
                        insertGap.setLong(2, m.getID());
                        insertGap.setString(3, Character.toString(g.getBase()));
                        insertGap.setLong(4, g.getAbsPos());
                        insertGap.setByte(5, gap);
                        insertGap.setInt(6, g.getOrder());

                        insertGap.addBatch();

                        if (batchCounter == DIFF_BATCH_SIZE) {
                            insertGap.executeBatch();
                            batchCounter = 0;
                        }
                    }
                }
            }

            insertDiff.executeBatch();
            insertGap.executeBatch();

            insertDiff.close();
            insertGap.close();

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting diff data");
    }

    /**
     * Locks all tables involved when adding a track in mysql fashion.
     */
    private void lockTrackDomainTables() {
        this.lockDomainTables(MySQLStatements.LOCK_TABLE_TRACK_DOMAIN, "track");
    }

    /**
     * Locks all tables involved when adding seq pair data in mysql fashion.
     */
    private void lockSeqPairDomainTables() {
        this.lockDomainTables(MySQLStatements.LOCK_TABLE_SEQUENCE_PAIRS_DOMAIN, "seq pair");
    }

    /**
     * Locks all tables declared by the lock sql statement.
     * @param lockStatement sql statement to lock some tables
     * @param domainName name of the domain to lock for logging
     */
    private void lockDomainTables(String lockStatement, String domainName) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start locking {0} domain tables...", domainName);
        try {
            PreparedStatement lock = con.prepareStatement(lockStatement);
            lock.execute();
            lock.close();
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done locking {0} domain tables...", domainName);
    }

    /**
     * Adds all track data to the database which should be stored.
     * @param track track to add
     * @param seqPairs true, if this is a sequence pair track, false otherwise
     * @param onlyPosTable true, if only the position table is to be stored, false in the ordinary "import track" scenario
     * @return the track id used in the database
     * @throws StorageException 
     */
    public int addTrack(ParsedTrack track, boolean seqPairs, boolean onlyPosTable) throws StorageException {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Preparing statements for storing track data");

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.lockTrackDomainTables();
            this.disableTrackDomainIndices();
        }
        isLastTrack = track.getParsedMappingContainer().isLastMappingContainer();
        if (!onlyPosTable) {
            this.storeTrack(track);
            if (track.isDbUsed()) {
                this.storeCoverage(track);
                this.storeMappings(track);
                this.storeDiffs(track);
            }
            this.storeTrackStatistics(track); //needs to be called after storeCoverage
        }
        if (track.isDbUsed()) {
            this.storePositionTable(track);
        }

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.enableTrackDomainIndices();
            this.enableReferenceIndices();
            this.enableSeqPairDomainIndices();
            this.unlockTables();
        }

        if (!seqPairs && isLastTrack) {
            track.clear();
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Track \"{0}\" has been stored successfully", track.getDescription());

        return track.getID();
    }

    private void disableTrackDomainIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started disabling track data domain indices");
        this.disableDomainIndices(MySQLStatements.DISABLE_COVERAGE_INDICES, null);
        this.disableDomainIndices(MySQLStatements.DISABLE_TRACK_INDICES, null);
        this.disableDomainIndices(MySQLStatements.DISABLE_MAPPING_INDICES, null);
        this.disableDomainIndices(MySQLStatements.DISABLE_DIFF_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished disabling track data domain indices");
    }

    private void enableTrackDomainIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started enabling track data domain indices");
        this.enableDomainIndices(MySQLStatements.ENABLE_COVERAGE_INDICES, null);
        this.enableDomainIndices(MySQLStatements.ENABLE_TRACK_INDICES, null);
        this.enableDomainIndices(MySQLStatements.ENABLE_MAPPING_INDICES, null);
        this.enableDomainIndices(MySQLStatements.ENABLE_DIFF_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished enabling track data domain indices");
    }

    /**
     * Disables domain indices in mysql fashion.
     * @param sqlStatement mysql statement to disable domain indices
     * @param domainName name of the domain to disable, if not needed here, pass <code>null</code>
     */
    private void disableDomainIndices(String sqlStatement, String domainName) {
        if (domainName != null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started disabling {0} data domain indices", domainName);
        }
        try {
            PreparedStatement disableDomainIndices = con.prepareStatement(sqlStatement);
            disableDomainIndices.execute();
            disableDomainIndices.close();
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        if (domainName != null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished disabling {0} data domain indices", domainName);
        }
    }

    /**
     * Enables domain indices in mysql fashion.
     * @param sqlStatement mysql statement to enable domain indices
     * @param domainName name of the domain to enable, if not needed here, pass <code>null</code>
     */
    private void enableDomainIndices(String sqlStatement, String domainName) {
        if (domainName != null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started enabling {0} data domain indices", domainName);
        }
        try {
            PreparedStatement enableDomainIndices = con.prepareStatement(sqlStatement);
            enableDomainIndices.execute();
            enableDomainIndices.close();
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        if (domainName != null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished enabling {0} data domain indices", domainName);
        }
    }

    private void disableSeqPairDomainIndices() {
        this.disableDomainIndices(MySQLStatements.DISABLE_SEQUENCE_PAIR_INDICES, "seq pair");
    }

    private void enableSeqPairDomainIndices() {
        this.enableDomainIndices(MySQLStatements.ENABLE_SEQUENCE_PAIR_INDICES, "seq pair");
    }

    
    public ReferenceConnector getRefGenomeConnector(int refGenID) {

        // only return new object, if no suitable connector was created before
        if (!refConnectors.containsKey(refGenID)) {
            refConnectors.put(refGenID, new ReferenceConnector(refGenID));
        }
        return refConnectors.get(refGenID);
    }

    
    public TrackConnector getTrackConnector(PersistantTrack track) {
        // only return new object, if no suitable connector was created before
        int trackID = track.getId();
        if (!trackConnectors.containsKey(trackID)) {
            trackConnectors.put(trackID, new TrackConnector(track, adapter));
        }
        return trackConnectors.get(trackID);
    }

    
    public TrackConnector getTrackConnector(List<PersistantTrack> tracks, boolean combineTracks) {
        // makes sure the track id is not already used
        int id = 9999;
        for (PersistantTrack track : tracks) {
            id += track.getId();
        }
        // only return new object, if no suitable connector was created before
        trackConnectors.put(id, new TrackConnector(id, tracks, adapter, combineTracks));
        return trackConnectors.get(id);
    }
    

    public MultiTrackConnector getMultiTrackConnector(PersistantTrack track) {
        // only return new object, if no suitable connector was created before
//        int trackID = track.getId();
//        if (!multiTrackConnectors.containsKey(trackID)) { //old solution, which does not work anymore
//            multiTrackConnectors.put(trackID, new MultiTrackConnector(track, adapter));
//        }
//        return multiTrackConnectors.get(trackID);
        return new MultiTrackConnector(track, adapter);
    }
    
    
    public MultiTrackConnector getMultiTrackConnector(List<PersistantTrack> tracks) {
        // makes sure the track id is not already used
        int id = 9999;
        for (PersistantTrack track : tracks) {
            id += track.getId();
        }
        // only return new object, if no suitable connector was created before
//        multiTrackConnectors.put(id, new MultiTrackConnector(tracks, adapter));
        return multiTrackConnectors.get(id);
    }
    
    /**
     * Removes the track connector for the given trackId.
     * @param trackId track id of the track connector to remove
     */
    public void removeTrackConnector(int trackId){
        if (trackConnectors.containsKey(trackId)){
            trackConnectors.remove(trackId);
        }
    }
    
//    /**
//     * Removes the multi track connector for the given trackId.
//     * @param trackId track id of the multi track connector to remove
//     */
//    public void removeMultiTrackConnector(int trackId){
//        if (multiTrackConnectors.containsKey(trackId)){
//            multiTrackConnectors.remove(trackId);
//        }
//    }
    
    /**
     * Removes the multi track connector for the given trackId.
     * @param trackCon track connector of the multi track connector to remove
     */
    public void removeMultiTrackConnector(MultiTrackConnector trackCon) {
        multiTrackConnectors.remove(trackCon);
    }

    /**
     * Calculates and returns the names of all currently opened tracks hashed to their
     * track id.
     * @return the names of all currently opened tracks hashed to their track id.
     */
    public HashMap<Integer, String> getOpenedTrackNames() {
        HashMap<Integer, String> namesList = new HashMap<>();
        Iterator<Integer> it = this.trackConnectors.keySet().iterator();
        int nextId;
        while (it.hasNext()) {
            nextId = it.next();
            namesList.put(nextId, this.trackConnectors.get(nextId).getAssociatedTrackName());
        }
        return namesList;
    }
    
    
    public Connection getConnection() {
        return con;
    }
    
//    /**
//     * Fetches the project folder associated to this project. Not every project
//     * needs to have a project folder. If everything is imported into the database
//     * this folder is never set.
//     * @return the project folder associated to this project or an empty string,
//     * if it was not set yet.
//     */
//    public String getProjectFolder() {
//        
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading project folder from database");
//        String projectFolder = "";
//        try {
//            PreparedStatement fetchProjectFolder = con.prepareStatement(SQLStatements.FETCH_PROJECT_FOLDER);
//            ResultSet results = fetchProjectFolder.executeQuery();
//            if (results.next()) {
//                projectFolder = results.getString(FieldNames.PROJECT_FOLDER_PATH);
//            }
//        } catch (SQLException e) {
//            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, e);
//        }
//        
//        return projectFolder;
//    }

    
    public List<PersistantReference> getGenomes() throws OutOfMemoryError {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading reference genome data from database");
        ArrayList<PersistantReference> refGens = new ArrayList<>();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_GENOMES);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.REF_GEN_ID);
                String description = rs.getString(FieldNames.REF_GEN_DESCRIPTION);
                String name = rs.getString(FieldNames.REF_GEN_NAME);
                String sequence = rs.getString(FieldNames.REF_GEN_SEQUENCE);
                Timestamp timestamp = rs.getTimestamp(FieldNames.REF_GEN_TIMESTAMP);
                refGens.add(new PersistantReference(id, name, description, sequence, timestamp));
            }

        } catch (SQLException e) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, e);
        }

        return refGens;
    }

    public void deleteTrack(long trackID) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of track with id \"{0}\"", trackID);
        try {
            con.setAutoCommit(false);

            PreparedStatement deleteDiffs = con.prepareStatement(SQLStatements.DELETE_DIFFS_FROM_TRACK);
            deleteDiffs.setLong(1, trackID);
            int seqPairTrack = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_SEQ_PAIR_TO_TRACK_ID, SQLStatements.GET_NUM, con, trackID);
            if (seqPairTrack > 0) {
                PreparedStatement deleteSeqPairPivot = con.prepareStatement(SQLStatements.DELETE_SEQUENCE_PAIR_PIVOT);
                deleteSeqPairPivot.setLong(1, trackID);
                PreparedStatement deleteSeqPairReplicates = con.prepareStatement(SQLStatements.DELETE_SEQUENCE_PAIR_REPLICATE);
                deleteSeqPairReplicates.setLong(1, trackID);
                PreparedStatement deleteSeqPairs = con.prepareStatement(SQLStatements.DELETE_SEQUENCE_PAIRS);
                deleteSeqPairs.setLong(1, trackID);
                
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Seq Pair Pivot data...");
                deleteSeqPairPivot.execute();
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Seq Pair Replicate data...");
                deleteSeqPairReplicates.execute();
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Seq Pair Main data...");
                deleteSeqPairs.execute();
                
                con.commit();
                
                deleteSeqPairPivot.close();
                deleteSeqPairReplicates.close();
                deleteSeqPairs.close();
            }
            PreparedStatement deleteMappings = con.prepareStatement(SQLStatements.DELETE_MAPPINGS_FROM_TRACK);
            deleteMappings.setLong(1, trackID);
            PreparedStatement deleteCoverage = con.prepareStatement(SQLStatements.DELETE_COVERAGE_FROM_TRACK);
            deleteCoverage.setLong(1, trackID);
            PreparedStatement deleteStatistics = con.prepareStatement(SQLStatements.DELETE_STATISTIC_FROM_TRACK);
            deleteStatistics.setLong(1, trackID);
            PreparedStatement deletePosTable = con.prepareStatement(SQLStatements.DELETE_POS_TABLE_FROM_TRACK);
            deletePosTable.setLong(1, trackID);
            PreparedStatement deleteTrack = con.prepareStatement(SQLStatements.DELETE_TRACK);
            deleteTrack.setLong(1, trackID);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Diffs...");
            deleteDiffs.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Mappings...");
            deleteMappings.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Coverage...");
            deleteCoverage.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Statistics...");
            deleteStatistics.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Position Table...");
            deletePosTable.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Track...");
            deleteTrack.execute();

            con.commit();

            deleteDiffs.close();
            deleteMappings.close();
            deleteCoverage.close();
            deleteStatistics.close();
            deletePosTable.close();
            deleteTrack.close();

            con.setAutoCommit(true);

        } catch (SQLException ex) {
            throw new StorageException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished deletion of track \"{0}\"", trackID);
    }

    
    public void deleteGenome(long refGenID) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of reference genome with id \"{0}\"", refGenID);
        try {
            con.setAutoCommit(false);

            PreparedStatement deleteAnnotations = con.prepareStatement(SQLStatements.DELETE_ANNOTATIONS_FROM_GENOME);
            deleteAnnotations.setLong(1, refGenID);
            PreparedStatement deleteSubeatures = con.prepareStatement(SQLStatements.DELETE_SUBANNOTATIONS_FROM_GENOME);
            deleteSubeatures.setLong(1, refGenID);
            PreparedStatement deleteGenome = con.prepareStatement(SQLStatements.DELETE_GENOME);
            deleteGenome.setLong(1, refGenID);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting annotations...");
            deleteAnnotations.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Sub annotations...");
            deleteSubeatures.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Genome...");
            deleteGenome.execute();

            con.commit();

            deleteAnnotations.close();
            deleteSubeatures.close();
            deleteGenome.close();

            con.setAutoCommit(true);

        } catch (SQLException ex) {
            throw new StorageException(ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished deletion of reference genome with id \"{0}\"", refGenID);
    }

    
    /**
     * Adds the sequence pair data and statistics to the database.
     * @param seqPairData sequence pair data container holding the data to store
     */
    public void addSeqPairData(ParsedSeqPairContainer seqPairData) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Preparing statements for storing sequence pair data for track data");

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.lockSeqPairDomainTables();
            this.disableSeqPairDomainIndices();
            this.disableTrackDomainIndices();
        }

        this.storeSeqPairTrackStatistics(seqPairData);
        this.storeSeqPairData(seqPairData);

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.enableSeqPairDomainIndices();
            this.unlockTables();
        }

        seqPairData.clear();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Sequence pair data for tracks \"{0}\" has been stored successfully");

    }

    /**
     * Helper method for storing the sequence pair data to the database.
     * @param seqPairData sequence pair data container holding the data to store
     */
    private void storeSeqPairData(ParsedSeqPairContainer seqPairData) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing sequence pair data...");
        try {
            long id = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_SEQUENCE_PAIR_ID, con);
            long seqPairId = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_SEQUENCE_PAIR_PAIR_ID, con);
            PreparedStatement insertSeqPair = con.prepareStatement(SQLStatements.INSERT_SEQ_PAIR);
            PreparedStatement insertReplicate = con.prepareStatement(SQLStatements.INSERT_SEQ_PAIR_REPLICATE);
            long interimPairId;

            // start storing the sequence pair data
            int batchCounter = 1;
            int replicateCounter = 1;
            HashMap<Long, Integer> replicateMap = new HashMap<Long, Integer>();
            HashMap<Pair<Long, Long>, ParsedSeqPairMapping> seqPairMap = seqPairData.getParsedSeqPairs();
            Iterator<Pair<Long, Long>> seqPairIterator = seqPairMap.keySet().iterator();
            while (seqPairIterator.hasNext()) {
                ParsedSeqPairMapping seqPair = seqPairMap.get(seqPairIterator.next());
                interimPairId = seqPair.getSequencePairID();
                seqPair.setSequencePairID(interimPairId + seqPairId);
                //if seq pairs are needed later on we have to set:
                //seqPair.setID(id);

                insertSeqPair.setLong(1, id++); //table index, unique for pos of seq pair
                insertSeqPair.setLong(2, seqPair.getSequencePairID()); //same for all positions of this sequence pair
                insertSeqPair.setLong(3, seqPair.getMappingId1()); //id of fst mapping
                insertSeqPair.setLong(4, seqPair.getMappingId2()); // id of scnd mapping
                insertSeqPair.setByte(5, seqPair.getType()); //type of the sequence pair

                insertSeqPair.addBatch();

                if (batchCounter == SEQPAIR_BATCH_SIZE) {
                    insertSeqPair.executeBatch();
                    batchCounter = 0;
                }
                batchCounter++;

                //insert replicates in map
                if (seqPair.getReplicates() > 1
                        && !replicateMap.containsKey(seqPair.getSequencePairID())) {
                    replicateMap.put(seqPair.getSequencePairID(), seqPair.getReplicates());
                }

            }
            
            //store replicates in db
            Iterator<Long> idIter = replicateMap.keySet().iterator();
            while (idIter.hasNext()) {
                seqPairId = idIter.next();

                insertReplicate.setLong(1, seqPairId);
                insertReplicate.setLong(2, replicateMap.get(seqPairId));
                insertReplicate.addBatch();

                if (replicateCounter++ == SEQPAIR_BATCH_SIZE) {
                    insertReplicate.executeBatch();
                    replicateCounter = 0;
                }
            }

            insertSeqPair.executeBatch();
            insertReplicate.executeBatch();


            //storing mapping to pair id data
            PreparedStatement insertSeqPairPivot = con.prepareStatement(SQLStatements.INSERT_SEQ_PAIR_PIVOT);
            batchCounter = 1;
            long correctSeqPairId;
            List<Pair<Long, Long>> mappingToPairIdList = seqPairData.getMappingToPairIdList();
            Iterator<Pair<Long, Long>> mappingToPairIdIterator = mappingToPairIdList.iterator();

            while (mappingToPairIdIterator.hasNext()) {
                Pair<Long, Long> pair = mappingToPairIdIterator.next();
                interimPairId = pair.getSecond();
                correctSeqPairId = interimPairId + seqPairId;
                
                insertSeqPairPivot.setLong(1, pair.getFirst()); //mapping id
                insertSeqPairPivot.setLong(2, correctSeqPairId); //sequence pair id

                insertSeqPairPivot.addBatch();

                if (batchCounter == SEQPAIR_PIVOT_BATCH_SIZE) {
                    insertSeqPairPivot.executeBatch();
                    batchCounter = 0;
                }
                batchCounter++;
            }
            insertSeqPairPivot.executeBatch();

            insertSeqPair.close();
            insertSeqPairPivot.close();
            insertReplicate.close();

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing sequence pair data");
    }

    /**
     * Sets the sequence pair id for both tracks belonging to one sequence pair.
     * @param track1Id track id of first track of the pair
     * @param track2Id track id of second track of the pair
     */
    public void setSeqPairIdsForTrackIds(long track1Id, long track2Id) {

        try {
            Integer seqPairId = 1; //not 0, because 0 is the value when a track is not a sequence pair track!
            PreparedStatement getLatestSeqPairId = con.prepareStatement(SQLStatements.GET_LATEST_TRACK_SEQUENCE_PAIR_ID);
            
            ResultSet rs = getLatestSeqPairId.executeQuery();
            if (rs.next()) {
                seqPairId = rs.getInt("LATEST_ID");
                if (seqPairId == null || seqPairId == 0) {
                    seqPairId = 1;
                }
            }
            try (PreparedStatement setSeqPairIds = con.prepareStatement(SQLStatements.INSERT_TRACK_SEQ_PAIR_ID)) {
                setSeqPairIds.setInt(1, seqPairId);
                setSeqPairIds.setLong(2, track1Id);
                setSeqPairIds.execute();

                setSeqPairIds.setInt(1, seqPairId);
                setSeqPairIds.setLong(2, track2Id);
                setSeqPairIds.execute();
            }

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
    }
        
    //TODO: delete seqpairs
                
    /**
     * Since Coverage container only stores the data, here we have to calculate which base is the
     * one with the most occurrences at a certain position.
     * @param track 
     */
    public void storePositionTable(ParsedTrack track) {

        if (!track.isStepwise() || isLastTrack) {

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start inserting snp data...");
            try (PreparedStatement insertPosition = con.prepareStatement(SQLStatements.INSERT_POSITION)) {
                // get latest snpID used
                long snpID = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_SNP_ID, con);
                //get reference sequence
                String refSeq = this.getRefGenomeConnector(track.getRefId()).getRefGenome().getSequence();

                int batchCounter = 0;
                int counterUncoveredDiffs = 0;
                int counterUncoveredGaps = 0;

                // go through positionTable
                CoverageContainer coverageContainer = track.getCoverageContainer();
                HashMap<String, Integer[]> positionTable = coverageContainer.getPositionTable();
                Iterator<String> positionIterator = positionTable.keySet().iterator();

                //all parameters needed in while loop (efficiency)
                String posString;
                Integer[] coverageValues;
                int maxCount;
                int typeInt;
                int position;
                double cov;
                double coverage;
                double forwCov;
                double revCov;
                double frequency;
                char type;
                char base;
                String refBase;
                int baseInt;
                
                while (positionIterator.hasNext()) {

                    posString = positionIterator.next();
                    coverageValues = positionTable.get(posString);

                    // i=0..5 is ACGTN_GAP (DIFFS) ...
                    maxCount = 0;
                    typeInt = 0;
                    for (int i = 0; i <= BASE_GAP; i++) {
                        if (maxCount < coverageValues[i]) {
                            maxCount = coverageValues[i];
                            typeInt = i;
                        }
                    }
                    
                    if (maxCount != 0) {

                        position = PositionUtils.convertPosition(posString);
                        // get coverage
                        if (coverageContainer.positionCovered(position)) {
                            forwCov = coverageContainer.getBestMappingForwardCoverage(position);
                            revCov = coverageContainer.getBestMappingReverseCoverage(position);
                            cov = forwCov + revCov;
                            cov = cov == 0 ? 1 : cov;

                            // get consensus base
                            refBase = String.valueOf(refSeq.charAt(position - 1)).toUpperCase();
                            baseInt = this.getBaseInt(refBase);
                            coverageValues[baseInt] = (int) cov - coverageValues[DIFFS];
                            coverageValues[baseInt] = coverageValues[baseInt] < 0 ? 0 : coverageValues[baseInt]; //check if negative
                            if (maxCount < coverageValues[baseInt]) {
                                type = SequenceComparison.MATCH.getType();
                                base = refBase.charAt(0);
                                frequency = coverageValues[baseInt] / cov * 100;
                            } else {
                                type = this.getType(typeInt);
                                base = this.getBase(typeInt);
                                frequency = coverageValues[DIFFS] / cov * 100;
                            }
                            if (!refBase.equals("N")) {
                                frequency = frequency > 100 ? 100 : frequency; //Todo: correct freq calculation

                                insertPosition.setLong(1, snpID);
                                insertPosition.setLong(2, track.getID());
                                insertPosition.setString(3, posString);
                                insertPosition.setString(4, String.valueOf(base));
                                insertPosition.setString(5, String.valueOf(refBase));
                                insertPosition.setInt(6, coverageValues[BASE_A]);
                                insertPosition.setInt(7, coverageValues[BASE_C]);
                                insertPosition.setInt(8, coverageValues[BASE_G]);
                                insertPosition.setInt(9, coverageValues[BASE_T]);
                                insertPosition.setInt(10, coverageValues[BASE_N]);
                                insertPosition.setInt(11, coverageValues[BASE_GAP]);
                                insertPosition.setInt(12, (int) cov);
                                insertPosition.setInt(13, (int) frequency);
                                insertPosition.setString(14, String.valueOf(type));

                                insertPosition.addBatch();

                                // save SnpID for position for insert in diff table
                                //tmpSnpID.put(position, snpID);

                                batchCounter++;
                                snpID++;
                            }
                        } else {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found " + ++counterUncoveredDiffs + " uncovered position in diffs {0}", position);
                        }
                    }

                    // ... i=6..10 is ACGTN (GAP); i=11 #diffs
                    maxCount = 0;
                    typeInt = 0;
                    for (int i = GAP_A; i <= GAP_N; i++) {
                        if (maxCount < coverageValues[i]) {
                            maxCount = coverageValues[i];
                            typeInt = i;
                        }
                    }

                    if (maxCount != 0) {

                        String absPosition = posString.substring(0, posString.length() - 2);
                        position = Integer.parseInt(absPosition);

                        // get coverage from adjacent positions
                        double forwCov1 = 0;
                        double revCov1 = 0;
                        double forwCov2 = 0;
                        double revCov2 = 0;
                        if (coverageContainer.positionCovered(position)) {
                            forwCov1 = coverageContainer.getBestMappingForwardCoverage(position);
                            revCov1 = coverageContainer.getBestMappingReverseCoverage(position);
                        } else {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found " + ++counterUncoveredGaps + " uncovered position in gaps: {0}", position);
                        }

                        if (coverageContainer.positionCovered(position + 1)) {
                            forwCov2 = coverageContainer.getBestMappingForwardCoverage(position + 1);
                            revCov2 = coverageContainer.getBestMappingReverseCoverage(position + 1);
                        }

                        forwCov = (forwCov1 + forwCov2) / 2;
                        revCov = (revCov1 + revCov2) / 2;
                        cov = forwCov + revCov;
                        coverage = forwCov1 + revCov1;
                        coverage = coverage == 0 ? 1 : coverage;

                        frequency = coverageValues[DIFFS] / coverage * 100;
                        frequency = frequency > 100 ? 100 : frequency; //Todo: correct freq calculation

                        insertPosition.setLong(1, snpID);
                        insertPosition.setLong(2, track.getID());
                        insertPosition.setString(3, posString);
                        insertPosition.setString(4, String.valueOf(getBase(typeInt)));
                        insertPosition.setString(5, String.valueOf('_'));
                        insertPosition.setInt(6, coverageValues[GAP_A]);
                        insertPosition.setInt(7, coverageValues[GAP_C]);
                        insertPosition.setInt(8, coverageValues[GAP_G]);
                        insertPosition.setInt(9, coverageValues[GAP_T]);
                        insertPosition.setInt(10, coverageValues[GAP_N]);
                        insertPosition.setInt(11, 0);
                        insertPosition.setInt(12, (int) cov);
                        insertPosition.setInt(13, (int) frequency);
                        insertPosition.setString(14, String.valueOf(SequenceComparison.INSERTION.getType()));

                        insertPosition.addBatch();

                        batchCounter++;
                        snpID++;
                    }


                    if (batchCounter == DIFF_BATCH_SIZE) {
                        insertPosition.executeBatch();
                        batchCounter = 0;
                    }

                }

                insertPosition.executeBatch();
                insertPosition.close();
                
                if (adapter.equalsIgnoreCase("mysql")) {
                    this.unlockTables();
                }

            } catch (SQLException ex) {
                this.rollbackOnError(this.getClass().getName(), ex);
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting snp data");
        }
    }

    /**
     * @param typeInt value between 0 and 4
     * @return the type of a sequence deviation (only subs and del) as character
     */
    private char getType(int typeInt) {

        char type = ' ';

        if (typeInt >= 0 && typeInt < 5) {
            type = SequenceComparison.SUBSTITUTION.getType();
        } else if (typeInt == 5) {
            type = SequenceComparison.DELETION.getType();
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown diff type");
        }

        return type;

    }

    private char getBase(int index) {

        char base = ' ';

        if (index == BASE_A || index == GAP_A) {
            base = 'A';
        } else if (index == BASE_C || index == GAP_C) {
            base = 'C';
        } else if (index == BASE_G || index == GAP_G) {
            base = 'G';
        } else if (index == BASE_T || index == GAP_T) {
            base = 'T';
        } else if (index == BASE_N || index == GAP_N) {
            base = 'N';
        } else if (index == BASE_GAP) {
            base = '_';
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown snp type");
        }

        return base;

    }

    /**
     * @param base the base whose integer value is needed
     * @return the integer value for the given base type
     */
    private int getBaseInt(String base) {

        int baseInt = 0;
        switch (base.toUpperCase()) {
            case "A": baseInt = BASE_A; break;
            case "C": baseInt = BASE_C; break;
            case "G": baseInt = BASE_G; break;
            case "T": baseInt = BASE_T; break;
            case "N": baseInt = BASE_N; break;
            case "_": baseInt = BASE_GAP; break;
        }

        return baseInt;

    }

    /**
     * Identifies SNPs with given criteria in all opened trackConnectors.
     * @param percentageThreshold minimum percentage of deviation from the reference base
     * @param absThreshold minimum number of total deviating bases from the reference base
     * @param trackIds the list of track ids for which the snp detection should be carried out
     * @return list of snps found in the opened tracks for the given criteria
     */
    public List<SnpI> findSNPs(int percentageThreshold, int absThreshold, List<Integer> trackIds) {
        List<SnpI> snps = new ArrayList<>();
        if (trackIds.isEmpty()){
            String msg = NbBundle.getMessage(ProjectConnector.class, "ProjectConnector.NoTracksMsg", 
                        "When no track are opened/chosen, no result can be returned!");
            String header = NbBundle.getMessage(ProjectConnector.class, "ProjectConnector.NoTracksHeader", 
                        "Missing Information");
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
            return snps;
        }
        // currently opened tracks
        try {
            PreparedStatement fetchSNP = con.prepareStatement(SQLStatements.FETCH_SNPS);
            fetchSNP.setInt(1, percentageThreshold);
            fetchSNP.setInt(2, absThreshold);
            fetchSNP.setInt(3, absThreshold);
            fetchSNP.setInt(4, absThreshold);
            fetchSNP.setInt(5, absThreshold);
            fetchSNP.setInt(6, absThreshold);
            
            ResultSet rs = fetchSNP.executeQuery();
            while (rs.next()) {
                String position = rs.getString(FieldNames.POSITIONS_POSITION);
                int trackId = rs.getInt(FieldNames.POSITIONS_TRACK_ID);
                char base = rs.getString(FieldNames.POSITIONS_BASE).charAt(0);
                char refBase = rs.getString(FieldNames.POSITIONS_REFERENCE_BASE).charAt(0);
                int aRate = rs.getInt(FieldNames.POSITIONS_A);
                int cRate = rs.getInt(FieldNames.POSITIONS_C);
                int gRate = rs.getInt(FieldNames.POSITIONS_G);
                int tRate = rs.getInt(FieldNames.POSITIONS_T);
                int nRate = rs.getInt(FieldNames.POSITIONS_N);
                int gapRate = rs.getInt(FieldNames.POSITIONS_GAP);
                int coverage = rs.getInt(FieldNames.POSITIONS_COVERAGE);
                int frequency = rs.getInt(FieldNames.POSITIONS_FREQUENCY);
                SequenceComparison type = SequenceComparison.getSequenceComparison(rs.getString(FieldNames.POSITIONS_TYPE).charAt(0));
                if (trackIds.contains(trackId)) {
                    snps.add(new Snp(position, trackId, base, refBase, aRate, cRate, gRate,
                            tRate, nRate, gapRate, coverage, frequency, type));
                    if (coverage == 0) {
                        System.out.println("Coverage is zero"); //TODO: remove this
                    }
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return snps;
    }
    
    /**
     * @return The database adapter string for this project
     */
    public String getAdapter() {
        return this.adapter;
    }

    /**
     * Resets the file path of a direct access track.
     * @param track track whose file path has to be resetted.
     * @throws StorageException 
     */
    public void resetTrackPath(PersistantTrack track) throws StorageException {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Preparing statements for storing track data");

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.lockTrackDomainTables();
            this.disableTrackDomainIndices();
        }
        
        try (PreparedStatement resetTrackPath = con.prepareStatement(SQLStatements.RESET_TRACK)) {
                resetTrackPath.setString(1, track.getFilePath());
                resetTrackPath.setLong(2, track.getId());
                resetTrackPath.execute();
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.enableTrackDomainIndices();
            this.unlockTables();
        }
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Track \"{0}\" has been updated successfully", track.getDescription());
    }
    
    /**
     * @return the latest track id used in the database + 1 = the next id to use.
     */
    public int getLatestTrackId() {
        return (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_TRACK_ID, con);
    }
    
}
