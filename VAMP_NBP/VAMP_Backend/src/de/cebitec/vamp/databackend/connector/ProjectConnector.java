package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.dataObjects.Snp;
import de.cebitec.vamp.databackend.FieldNames;
import de.cebitec.vamp.databackend.GenericSQLQueries;
import de.cebitec.vamp.databackend.H2SQLStatements;
import de.cebitec.vamp.databackend.MySQLStatements;
import de.cebitec.vamp.databackend.SQLStatements;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.dataObjects.SnpI;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedFeature;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsedSeqPairMapping;
import de.cebitec.vamp.parser.common.ParsedSubfeature;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.PositionUtils;
import de.cebitec.vamp.util.SequenceComparison;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private HashMap<Integer, ReferenceConnector> refConnectors;
    private static final int BATCH_SIZE = 100000; //TODO: test larger batch sizes
    private final static int FEATURE_BATCH_SIZE = BATCH_SIZE;
    private final static int COVERAGE_BATCH_SIZE = BATCH_SIZE * 3;
    private final static int MAPPING_BATCH_SIZE = BATCH_SIZE;
    private static final int SEQPAIR_BATCH_SIZE = BATCH_SIZE;
    private static final int SEQPAIR_PIVOT_BATCH_SIZE = BATCH_SIZE + 25000;
    private final static int DIFF_BATCH_SIZE = BATCH_SIZE;
    private final static String VARCHAR_20 = "VARCHAR(20)";
    private final static String BIGINT_UNSIGNED = "BIGINT UNSIGNED";
    private final static String INT_UNSIGNED = "INT UNSIGNED";
    
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
    private int mappings= 0;
    private int perfectMappings=0;
    private int bmMappings=0;
    private int currentTrackID=-1;
    private boolean isLastTrack=false;
    
    private ProjectConnector() {
        trackConnectors = new HashMap<Integer, TrackConnector>();
        refConnectors = new HashMap<Integer, ReferenceConnector>();
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

    public List<PersistantTrack> getTracks() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading track data from database");
        ArrayList<PersistantTrack> tracks = new ArrayList<PersistantTrack>();

        try {
            PreparedStatement fetchTracks = con.prepareStatement(SQLStatements.FETCH_TRACKS);
            ResultSet rs = fetchTracks.executeQuery();

            while (rs.next()) {
                int id = rs.getInt(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                int refGenID = rs.getInt(FieldNames.TRACK_REFERENCE_ID);
                tracks.add(new PersistantTrack(id, description, date, refGenID));
            }

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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

    public void connect(String adapter, String hostname, String database, String user, String password) throws SQLException, JdbcSQLException {
        if (adapter.equalsIgnoreCase("mysql")) {
            this.adapter = adapter;
            this.url = "jdbc:" + adapter + "://" + hostname + "/" + database;
            this.user = user;
            this.password = password;
            this.connect(url, user, password);
            this.setupMySQLDatabase();
        } else {
            this.adapter = adapter;
            this.url = "jdbc:" + adapter + ":" + database + ";AUTO_SERVER=TRUE;MULTI_THREADED=1";
            //;FILE_LOCK=SERIALIZED"; that works temporary but now using AUTO_SERVER

            this.connectH2DataBase(url);
            this.setupDatabaseH2();

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

    private void setupDatabaseH2() {

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Setting up tables and indices if not existent");

            con.setAutoCommit(false);
            //create tables if not exist yet
            con.prepareStatement(H2SQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_POSITIONS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_POSITIONS).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_DIFFS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_DIFF).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_COVERAGE).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_COVERAGE).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_FEATURES).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_FEATURES).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_SUBFEATURES).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SUBFEATURE_PARENT_ID).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SUBFEATURE_REF_ID).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_MAPPINGS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_MAPPINGS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_MAPPING_START).executeUpdate();
//            con.prepareStatement(H2SQLStatements.INDEX_MAPPING_STOP).executeUpdate();
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
            con.prepareStatement(MySQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_POSITIONS).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_DIFFS).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_COVERAGE).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_FEATURES).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_SUBFEATURES).executeUpdate();
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
                    FieldNames.TABLE_TRACKS, FieldNames.TRACK_SEQUENCE_PAIR_ID, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_FEATURES, FieldNames.FEATURE_GENE, "VARCHAR (20)"));
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_AVERAGE_READ_LENGTH, INT_UNSIGNED));
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH, INT_UNSIGNED));
        
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
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done unlocking tables");
    }

    
    private void disableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start disabling reference data domain indexing...");
        this.disableDomainIndices(MySQLStatements.DISABLE_REFERENCE_INDICES, null);
        this.disableDomainIndices(MySQLStatements.DISABLE_FEATURE_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done disabling reference data domain indexing");
    }

    private void enableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start enabling reference data domain indexing...");
        this.enableDomainIndices(MySQLStatements.ENABLE_REFERENCE_INDICES, null);
        this.enableDomainIndices(MySQLStatements.ENABLE_FEATURE_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done enabling reference data domain indexing");
    }

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
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting reference sequence data");
    }

    private void storeFeatures(ParsedReference reference) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start inserting features...");
        try {
            long id = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_FEATURE_ID, con);
            PreparedStatement insertFeature = con.prepareStatement(SQLStatements.INSERT_FEATURE);
            PreparedStatement insertSubfeature = con.prepareStatement(SQLStatements.INSERT_SUBFEATURE);

            int batchCounter = 1;
            int batchCountSubfeat = 1;
            int referenceId = reference.getID();
            Iterator<ParsedFeature> featIt = reference.getFeatures().iterator();
            while (featIt.hasNext()) {
                
                batchCounter++;
                ParsedFeature feature = featIt.next();
                insertFeature.setLong(1, id);
                insertFeature.setLong(2, referenceId);
                insertFeature.setInt(3, feature.getType().getTypeInt());
                insertFeature.setInt(4, feature.getStart());
                insertFeature.setInt(5, feature.getStop());
                insertFeature.setString(6, feature.getLocusTag());
                insertFeature.setString(7, feature.getProduct());
                insertFeature.setString(8, feature.getEcNumber());
                insertFeature.setInt(9, feature.getStrand());
                insertFeature.setString(10, feature.getGeneName());
                insertFeature.addBatch();

                for (ParsedSubfeature subfeature : feature.getSubfeatures()) {
                    batchCountSubfeat++;
                    insertSubfeature.setLong(1, id);
                    insertSubfeature.setLong(2, referenceId);
                    insertSubfeature.setInt(3, subfeature.getType().getTypeInt());
                    insertSubfeature.setInt(4, subfeature.getStart());
                    insertSubfeature.setInt(5, subfeature.getStop());
                    insertSubfeature.addBatch();
                    
                    if (batchCountSubfeat == FEATURE_BATCH_SIZE) {
                        batchCountSubfeat = 1;
                        insertSubfeature.executeBatch();
                    }
                }
                
                if (batchCounter == FEATURE_BATCH_SIZE) {
                    batchCounter = 1;
                    insertFeature.executeBatch();
                }
                ++id;
                //  it.remove();
            }

            insertFeature.executeBatch();
            insertSubfeature.executeBatch();
            insertFeature.close();
            insertSubfeature.close();


        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting features");
    }

    private void lockReferenceDomainTables() {
        this.lockDomainTables(MySQLStatements.LOCK_TABLE_REFERENCE_DOMAIN, "reference");
    }

    public int addRefGenome(ParsedReference reference) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing reference sequence  \"{0}\"", reference.getName());

        try {
            con.setAutoCommit(false);

            if (adapter.equalsIgnoreCase("mysql")) {
                this.lockReferenceDomainTables();
                this.disableReferenceIndices();
            }

            this.storeGenome(reference);
            this.storeFeatures(reference);

            if (adapter.equalsIgnoreCase("mysql")) {
                this.enableReferenceIndices();
                this.unlockTables();
            }

            con.setAutoCommit(true);
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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
                int id = -1;
                id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_COVERAGE_ID, con);

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
                ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
            }
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing coverage information");
        }
    }

    private int storeTrack(ParsedTrack track, long refGenID) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track data...");
        int id = -1;
        try {
            if (track.isFirstTrack() | !track.isStepwise()) {
                id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_TRACK_ID, con);
                PreparedStatement insertTrack = con.prepareStatement(SQLStatements.INSERT_TRACK);

                // store track in table
                insertTrack.setLong(1, id);
                insertTrack.setLong(2, refGenID);
                insertTrack.setString(3, track.getDescription());
                insertTrack.setTimestamp(4, track.getTimestamp());
                //insertTrack.setLong(5, runID);
                insertTrack.execute();

                insertTrack.close();
                currentTrackID = id;
                track.setID(id);
            } else {
                track.setID(currentTrackID);
            }
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track data");
        return id;
    }


    private void storeTrackStatistics(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track statistics data...");
        int numReads = 0;
        int noUniqueSeq = 0;
        int noUniqueMappings = 0;
        int coveragePerf = 0;
        int coverageBM = 0;
        int coverageComplete = 0;
        long trackID = track.getID();
        int averageRead=0;
        try {
            HashMap<Integer, Integer> mappingInfos = track.getParsedMappingContainer().getMappingInformations();
            mappings += mappingInfos.get(1);
            perfectMappings += mappingInfos.get(2);
            bmMappings += mappingInfos.get(3);
            averageRead=mappingInfos.get(7);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "...can't get the statistics list");
        }
        try {
            if (!track.isStepwise() | isLastTrack) {
            // get latest id for track
            long id = -1;
            id = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_STATISTICS_ID, con);

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
                
                noUniqueMappings = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_SINGLETON_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
                noUniqueSeq = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
                numReads = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_READS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
                PreparedStatement insertStatistics = con.prepareStatement(SQLStatements.INSERT_STATISTICS);
                // store track in table
                insertStatistics.setLong(1, id);
                insertStatistics.setLong(2, trackID);
                insertStatistics.setInt(3, mappings);
                insertStatistics.setInt(4, perfectMappings);
                insertStatistics.setInt(5, bmMappings);
                insertStatistics.setInt(6, noUniqueMappings);
                insertStatistics.setInt(7, coveragePerf);
                insertStatistics.setInt(8, coverageBM);
                insertStatistics.setInt(9, coverageComplete);
                insertStatistics.setInt(10, noUniqueSeq);
                insertStatistics.setInt(11, numReads);
                insertStatistics.setInt(12,averageRead);
                insertStatistics.execute();
                insertStatistics.close();
                mappings = 0;
                perfectMappings = 0;
                bmMappings = 0;
                numReads = 0;
            }

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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
                ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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
                    insertMapping.setInt(5, m.getCount()); //count der gleichen seq (seq id) in reads
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
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done locking {0} domain tables...", domainName);
    }

    public int addTrack(ParsedTrack track, long refGenID, boolean seqPairs, boolean onlyPosTable) throws StorageException {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Preparing statements for storing track data");

        if (adapter.equalsIgnoreCase("mysql")) {
            this.lockTrackDomainTables();
            this.disableTrackDomainIndices();
        }
        isLastTrack = track.getParsedMappingContainer().isLastMappingContainer();
        if (!onlyPosTable) {
            this.storeTrack(track, refGenID);
            this.storeCoverage(track);
            this.storeMappings(track);
            this.storeDiffs(track);
            this.storeTrackStatistics(track); //needs to be called after storeCoverage
        }
            this.storePositionTable(track);

        if (adapter.equalsIgnoreCase("mysql")) {
            this.enableTrackDomainIndices();
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
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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
            trackConnectors.put(trackID, new TrackConnector(track));
        }
        return trackConnectors.get(trackID);
    }

    public TrackConnector getTrackConnector(List<PersistantTrack> tracks) {
        // makes sure the track id is not already used
        int id = 9999;
        for (PersistantTrack track : tracks) {
            id += track.getId();
        }
        // only return new object, if no suitable connector was created before
        trackConnectors.put(id, new TrackConnector(id, tracks));
        return trackConnectors.get(id);
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

    /**
     * Calculates and returns the names of all currently opened tracks hashed to their
     * track id.
     * @return the names of all currently opened tracks hashed to their track id.
     */
    public HashMap<Integer, String> getOpenedTrackNames() {
        HashMap<Integer, String> namesList = new HashMap<Integer, String>();
        Iterator<Integer> it = this.trackConnectors.keySet().iterator();
        int nextId = -1;
        while (it.hasNext()) {
            nextId = it.next();
            namesList.put(nextId, this.trackConnectors.get(nextId).getAssociatedTrackName());
        }
        return namesList;
    }
    
    
    public Connection getConnection() {
        return con;
    }

    
    public List<PersistantReference> getGenomes() {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading reference genome data from database");
        ArrayList<PersistantReference> refGens = new ArrayList<PersistantReference>();

        try {
            PreparedStatement fetch;

            fetch = con.prepareStatement(SQLStatements.FETCH_GENOMES);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.REF_GEN_ID);
                String description = rs.getString(FieldNames.REF_GEN_DESCRIPTION);
                String name = rs.getString(FieldNames.REF_GEN_NAME);
                String sequence = null;
                sequence = rs.getString(FieldNames.REF_GEN_SEQUENCE);
                Timestamp timestamp = rs.getTimestamp(FieldNames.REF_GEN_TIMESTAMP);
                refGens.add(new PersistantReference(id, name, description, sequence, timestamp));
            }

        } catch (SQLException e) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), e);
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
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Track...");
            deleteTrack.execute();

            con.commit();

            deleteDiffs.close();
            deleteMappings.close();
            deleteCoverage.close();
            deleteStatistics.close();
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

            PreparedStatement deleteFeatures = con.prepareStatement(SQLStatements.DELETE_FEATURES_FROM_GENOME);
            deleteFeatures.setLong(1, refGenID);
            PreparedStatement deleteSubeatures = con.prepareStatement(SQLStatements.DELETE_SUBFEATURES_FROM_GENOME);
            deleteSubeatures.setLong(1, refGenID);
            PreparedStatement deleteGenome = con.prepareStatement(SQLStatements.DELETE_GENOME);
            deleteGenome.setLong(1, refGenID);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Features...");
            deleteFeatures.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Subfeatures...");
            deleteSubeatures.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Genome...");
            deleteGenome.execute();

            con.commit();

            deleteFeatures.close();
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

        if (adapter.equalsIgnoreCase("mysql")) {
            this.lockSeqPairDomainTables();
            this.disableSeqPairDomainIndices();
        }

        this.storeSeqPairTrackStatistics(seqPairData);
        this.storeSeqPairData(seqPairData);

        if (adapter.equalsIgnoreCase("mysql")) {
            this.enableSeqPairDomainIndices();
            this.unlockTables();
        }

        seqPairData.clear();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Sequence pair data for tracks \"{0}\" has been stored successfully");

    }

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
            long pivotId = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_SEQUENCE_PAIR_PIVOT_ID, con);
            PreparedStatement insertSeqPairPivot = con.prepareStatement(SQLStatements.INSERT_SEQ_PAIR_PIVOT);
            batchCounter = 1;
            long correctSeqPairId;
            List<Pair<Long, Long>> mappingToPairIdList = seqPairData.getMappingToPairIdList();
            Iterator<Pair<Long, Long>> mappingToPairIdIterator = mappingToPairIdList.iterator();

            while (mappingToPairIdIterator.hasNext()) {
                Pair<Long, Long> pair = mappingToPairIdIterator.next();
                interimPairId = pair.getSecond();
                correctSeqPairId = interimPairId + seqPairId;

                insertSeqPairPivot.setLong(1, pivotId++);
                insertSeqPairPivot.setLong(2, pair.getFirst()); //mapping id
                insertSeqPairPivot.setLong(3, correctSeqPairId); //sequence pair id

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
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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

            PreparedStatement setSeqPairIds = con.prepareStatement(SQLStatements.INSERT_TRACK_SEQ_PAIR_ID);

            setSeqPairIds.setInt(1, seqPairId);
            setSeqPairIds.setLong(2, track1Id);
            setSeqPairIds.execute();

            setSeqPairIds.setInt(1, seqPairId);
            setSeqPairIds.setLong(2, track2Id);
            setSeqPairIds.execute();

            setSeqPairIds.close();

        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    //TODO: delete seqpairs
    //TODO: seqpair queries
                
    /**
     * Since Coverage container only stores the data, here we have to calculate which base is the
     * one with the most occurrences at a certain position.
     * @param track 
     */
    private void storePositionTable(ParsedTrack track) {

        if (!track.isStepwise() | isLastTrack) {

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start inserting snp data...");
            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.valueOf(currentTimestamp));
            try {
                long snpID = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_SNP_ID, con);
                PreparedStatement getGenomeID = con.prepareStatement(SQLStatements.FETCH_GENOMEID_FOR_TRACK);
                PreparedStatement getRefSeq = con.prepareStatement(SQLStatements.FETCH_SINGLE_GENOME);
                PreparedStatement insertPosition = con.prepareStatement(SQLStatements.INSERT_POSITION);
                // get latest snpID used

                String refSeq = "";

                //get reference sequence
                getGenomeID.setLong(1, track.getID());
                long genomeID = 0;
                ResultSet qs = getGenomeID.executeQuery();
                if (qs.next()) {
                    genomeID = qs.getLong(FieldNames.TRACK_REFERENCE_ID);
                }
                getRefSeq.setLong(1, genomeID);
                refSeq = "";
                ResultSet os = getRefSeq.executeQuery();
                if (os.next()) {
                    refSeq = os.getString(FieldNames.REF_GEN_SEQUENCE);
                }

                int batchCounter = 0;
                int counterUncoveredDiffs = 0;
                int counterUncoveredGaps = 0;

                // go through positionTable
                HashMap<String, Integer[]> positionTable = track.getCoverageContainer().getPositionTable();
                Iterator<String> positionIterator = positionTable.keySet().iterator();

                while (positionIterator.hasNext()) {

                    String posString = positionIterator.next();
                    Integer[] coverageValues = positionTable.get(posString);

                    // i=0..5 is ACGTN_GAP (DIFFS) ...
                    int maxCount = 0;
                    int typeInt = 0;
                    for (int i = 0; i <= BASE_GAP; i++) {
                        if (maxCount < coverageValues[i]) {
                            maxCount = coverageValues[i];
                            typeInt = i;
                        }
                    }

                    int position = -1;
                    if (maxCount != 0) {

                        position = PositionUtils.convertPosition(posString);
                        double cov = 0;
                        // get coverage
                        if (track.getCoverageContainer().positionCovered(position)) {
                            double forwCov = track.getCoverageContainer().getBestMappingForwardCoverage(position);
                            double revCov = track.getCoverageContainer().getBestMappingReverseCoverage(position);
                            cov = forwCov + revCov;

                            double frequency = 0;
                            char type = ' ';
                            char base = ' ';
                            String refBase = "";
                            // get consensus base
                            refBase = String.valueOf(refSeq.charAt(position - 1)).toUpperCase();
                            int baseInt = this.getBaseInt(refBase);
                            coverageValues[baseInt] = (int) cov - coverageValues[11];
                            coverageValues[baseInt] = coverageValues[baseInt] < 0 ? 0 : coverageValues[baseInt]; //check if negative
                            if (maxCount < coverageValues[baseInt]) {
                                type = SequenceComparison.MATCH.getType();
                                base = refBase.charAt(0);
                                frequency = coverageValues[baseInt] / cov * 100;
                            } else {
                                type = this.getType(typeInt);
                                base = this.getBase(typeInt);
                                frequency = coverageValues[11] / cov * 100;
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
                        if (track.getCoverageContainer().positionCovered(position)) {
                            forwCov1 = track.getCoverageContainer().getBestMappingForwardCoverage(position);
                            revCov1 = track.getCoverageContainer().getBestMappingReverseCoverage(position);
                        } else {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found " + ++counterUncoveredGaps + " uncovered position in gaps: {0}", position);
                        }

                        if (track.getCoverageContainer().positionCovered(position + 1)) {
                            forwCov2 = track.getCoverageContainer().getBestMappingForwardCoverage(position + 1);
                            revCov2 = track.getCoverageContainer().getBestMappingReverseCoverage(position + 1);
                        }

                        double forwCov = (forwCov1 + forwCov2) / 2;
                        double revCov = (revCov1 + revCov2) / 2;
                        double cov = forwCov + revCov;
                        double coverage = forwCov1 + revCov1;

                        double frequency = coverageValues[11] / coverage * 100;
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

            } catch (SQLException ex) {
                ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
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

        if (base.toUpperCase().equals("A")) {
            baseInt = BASE_A;
        } else if (base.toUpperCase().equals("C")) {
            baseInt = BASE_C;
        } else if (base.toUpperCase().equals("G")) {
            baseInt = BASE_G;
        } else if (base.toUpperCase().equals("T")) {
            baseInt = BASE_T;
        } else if (base.toUpperCase().equals("N")) {
            baseInt = BASE_N;
        } else if (base.toUpperCase().equals("_")) {
            baseInt = BASE_GAP;
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
        ArrayList<SnpI> snps = new ArrayList<SnpI>();
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

            ResultSet rs = fetchSNP.executeQuery();
            while (rs.next()) {
                String position = rs.getString(FieldNames.POSITIONS_POSITION);
                int trackId = rs.getInt(FieldNames.POSITIONS_TRACK_ID);
                char base = rs.getString(FieldNames.POSITIONS_BASE).charAt(0);
                char refBase = rs.getString(FieldNames.POSITIONS_REF_BASE).charAt(0);
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
                    if (coverage == 0){
                        int a=0; //TODO: remove this
                    }
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return snps;
    }
    
}
