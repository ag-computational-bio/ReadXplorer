/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.databackend.connector;

import de.cebitec.common.parser.fasta.FastaLineWriter;
import de.cebitec.readXplorer.databackend.FieldNames;
import de.cebitec.readXplorer.databackend.GenericSQLQueries;
import de.cebitec.readXplorer.databackend.H2SQLStatements;
import de.cebitec.readXplorer.databackend.MySQLStatements;
import de.cebitec.readXplorer.databackend.ObjectCache;
import de.cebitec.readXplorer.databackend.SQLStatements;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.parser.common.CoverageContainer;
import de.cebitec.readXplorer.parser.common.ParsedChromosome;
import de.cebitec.readXplorer.parser.common.ParsedFeature;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsedTrack;
import de.cebitec.readXplorer.util.DiscreteCountingDistribution;
import de.cebitec.readXplorer.util.FastaUtils;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.h2.jdbc.JdbcSQLException;
import org.openide.util.Exceptions;

/**
 * Responsible for the connection between user interface and data base.
 * Contains the methods to communicate with the data base.
 *
 * @author ddoppmeier, rhilker
 */
public class ProjectConnector extends Observable {

    private static ProjectConnector dbConnector;
    private static final int DB_VERSION_NO = 2;
    private Connection con;
    private String url;
    private String user;
    private String password;
    private String adapter;
    private HashMap<Integer, TrackConnector> trackConnectors;
    private HashMap<Integer, MultiTrackConnector> multiTrackConnectors;
//    private List<MultiTrackConnector> multiTrackConnectors;
    private HashMap<Integer, ReferenceConnector> refConnectors;
    private static final int BATCH_SIZE = 100000; //TODO: test larger batch sizes
    private final static int FEATURE_BATCH_SIZE = BATCH_SIZE;
    private final static String BIGINT_UNSIGNED = "BIGINT UNSIGNED";
    private final static String INT_UNSIGNED = "INT UNSIGNED";
    private static final String VARCHAR400 = "VARCHAR(400)";
    private static final String VARCHAR1000 = "VARCHAR(1000)";
    
    private int numReads = 0;
    private int noUniqueSeq = 0;
    private int noRepeatedSeq = 0;
    private int noUniqueMappings = 0;
    private int noUniqueBMMappings = 0;
    private int noUniquePerfectMappings = 0;
    private int numMappings = 0;
    private int numPerfectMappings = 0;
    private int numBmMappings = 0;
    private int averageReadLengthPart = 0;
    private int containerCount = 0;
    private DiscreteCountingDistribution readLengthDistribution;
    private DiscreteCountingDistribution readPairLengthDistribution;
    private String dbLocation;
    
    /**
     * Responsible for the connection between user interface and data base.
     * Contains the methods to communicate with the data base.
     */
    private ProjectConnector() {
        trackConnectors = new HashMap<>();
        multiTrackConnectors = new HashMap<>();
//        multiTrackConnectors = new ArrayList<>();
        refConnectors = new HashMap<>();
    }

    /**
     * Clears all track an reference connector lists of this ProjectConnector.
     */
    private void cleanUp() {
        trackConnectors.clear();
        refConnectors.clear();
    }

    /**
     * @return The singleton instance of the ProjectConnector
     */
    public static synchronized ProjectConnector getInstance() {
        if (dbConnector == null) {
            dbConnector = new ProjectConnector();
        }
        return dbConnector;
    }

    /**
     * @return True, if the project connector is currently connected to a DB, 
     * false otherwise
     */
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

    /**
     * Disconnects the current DB connection.
     */
    public void disconnect() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Closing database connection");
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            con = null;
            this.cleanUp();
            
            // notify observers about the change of the database
            this.notifyObserversAbout("disconnect");
        }
    }

    /**
     * Connects to the adapter used for the current project. Can either be a 
     * database adapter for h2 or mysql or an adapter for direct file access.
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
        this.dbLocation = projectLocation;
        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.url = "jdbc:" + adapter + "://" + hostname + "/" + projectLocation;
            this.user = user;
            this.password = password;
            this.connectMySql(url, user, password);
            this.setupMySQLDatabase();
        } else if (adapter.equalsIgnoreCase(Properties.ADAPTER_H2)) {
            //CACHE_SIZE is measured in KB
            this.url = "jdbc:" + adapter + ":" + projectLocation + ";AUTO_SERVER=TRUE;MULTI_THREADED=1;CACHE_SIZE=200000";
            //;FILE_LOCK=SERIALIZED"; that works temporary but now using AUTO_SERVER

            this.connectH2DataBase(url);
            this.setupDatabaseH2();
//        } else { //means: if (adapter.equalsIgnoreCase(Properties.ADAPTER_DIRECT_ACCESS)) {
//            this.connectToProject(projectLocation);
        }
        // notify observers about the change of the database
        this.notifyObserversAbout("connect");
    }

    /**
     * Connects to a MySql DB.
     * @param url the DB url to connect to
     * @param user the username to use
     * @param password the password to use
     * @throws SQLException 
     */
    private void connectMySql(String url, String user, String password) throws SQLException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Connecting to database");
        con = DriverManager.getConnection(url, user, password);
        con.setAutoCommit(true);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Successfully connected to database");
    }

    /**
     * Connects to an H2 DB.
     * @param url the DB url to connect to
     * @throws SQLException
     * @throws JdbcSQLException 
     */
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

    /**
     * Makes sure that an H2 DB is in a correct up-to-date state.
     * Either creates all tables necessary for a ReadXplorer DB or updates them, if
     * anything is missing/different. If no changes are necessary nothing is 
     * altered.
     */
    private void setupDatabaseH2() {

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Setting up tables and indices if not existent");

            con.setAutoCommit(false);
            //create tables if not exist yet
//            con.prepareStatement(SQLStatements.SETUP_PROJECT_FOLDER).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_CHROMOSOME).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_CHROMOSOME).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_FEATURES).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_FEATURES).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_TRACKS).execute();
            con.prepareStatement(H2SQLStatements.INDEX_TRACK_REFID).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_TRACK_READ_PAIR_ID).executeUpdate();
            
            con.prepareStatement(SQLStatements.SETUP_STATISTICS).executeUpdate();
            
            con.prepareStatement(H2SQLStatements.SETUP_COUNT_DISTRIBUTION).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_COUNT_DIST).executeUpdate();
            
            con.prepareStatement(SQLStatements.SETUP_OBJECTCACHE).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_OBJECTCACHE).executeUpdate();
            
            con.prepareStatement(SQLStatements.SETUP_DB_VERSION_TABLE).executeUpdate();

            this.checkDBStructure();

            con.commit();
            con.setAutoCommit(true);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished creating tables and indices if not existent before");

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
    }

    /**
     * Makes sure that a MySql DB is in a correct up-to-date state. Either
     * creates all tables necessary for a ReadXplorer DB or updates them, if anything
     * is missing/different. If no changes are necessary nothing is altered.
     */
    private void setupMySQLDatabase() {

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Setting up tables and indices if not existent");

            con.setAutoCommit(false);
            //create tables if not exist yet
//            con.prepareStatement(SQLStatements.SETUP_PROJECT_FOLDER).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_FEATURES).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_TRACKS).execute();
            con.prepareStatement(SQLStatements.SETUP_STATISTICS).execute();
            con.prepareStatement(MySQLStatements.SETUP_COUNT_DISTRIBUTION).executeUpdate();
            con.prepareStatement(MySQLStatements.SETUP_CHROMOSOME).executeUpdate();

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
     * ReadXplorer versions should be checked by this method to ensure correct database 
     * structure and avoiding errors when SQL statements request one of these
     * columns, which are not existent in older databases.
     */
    private void checkDBStructure() {
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Checking DB structure...");

        //remove statics table (replaced by STATISTICS table)
        this.runSqlStatement(SQLStatements.DROP_TABLE + "STATICS");
        this.runSqlStatement(SQLStatements.DROP_TABLE + "SUBFEATURES");
        this.runSqlStatement(SQLStatements.DROP_TABLE + "COVERAGE_DISTRIBUTION");
        this.runSqlStatement(SQLStatements.DROP_TABLE + "POSITIONS");

        //stats table
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUMBER_OF_REPEATED_SEQ, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUMBER_UNIQUE_BM_MAPPINGS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUMBER_UNIQUE_PERFECT_MAPPINGS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUMBER_READS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS, BIGINT_UNSIGNED));

        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_NUM_SINGLE_MAPPINGS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_SMALL_DIST_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS,
                    FieldNames.STATISTICS_NUM_UNIQ_SMALL_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_ORIENT_WRONG_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_LARGE_DIST_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_UNIQ_LARGE_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_STATISTICS, 
                    FieldNames.STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_AVERAGE_READ_LENGTH, INT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_STATISTICS, FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH, INT_UNSIGNED));

        //add read pair id column in tracks if not existent
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_TRACK, FieldNames.TRACK_READ_PAIR_ID, BIGINT_UNSIGNED));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_FEATURES, FieldNames.FEATURE_GENE, "VARCHAR (20)"));
        
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_TRACK, FieldNames.TRACK_PATH, VARCHAR400));
        

        //delete old "RUN_ID" field from the database to avoid problems with null values in insert statement
        // an error will be raised by the query, if the field does not exist 
        // (simply ignore the error) 
        this.runSqlStatement(GenericSQLQueries.genRemoveColumnString( 
                FieldNames.TABLE_TRACK, "RUN_ID"));

        //Add column parent id to feature table
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_FEATURES, FieldNames.FEATURE_PARENT_IDS, VARCHAR1000));
        this.runSqlStatement(SQLStatements.INIT_FEATURE_PARENT_ID);
        this.runSqlStatement(SQLStatements.NOT_NULL_FEATURE_PARENT_ID);
        //Drop old PARENT_ID column
        this.runSqlStatement(GenericSQLQueries.genRemoveColumnString(
                FieldNames.TABLE_FEATURES, "PARENT_ID"));
                
        //Drop unneeded indexes
        this.runSqlStatement(SQLStatements.DROP_INDEX + "INDEXPOS");
        
        this.checkDBVersion();
        
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
     * Runs a single sql statement and ignores any errors
     * @param statement sql statement to run
     */
    private void runSqlStatementIgnoreErrors(String statement) {

        try {
            con.prepareStatement(statement).executeUpdate();
        } catch (SQLException ex) {
            //ignore
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
                this.connectMySql(url, user, password);
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
        try (Statement unlock = con.createStatement()) {
            con.setAutoCommit(false);
            unlock.execute(MySQLStatements.UNLOCK_TABLES);
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done unlocking tables");
    }

    /**
     * Disables all indices belonging to the domain of genomic references.
     */
    private void disableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start disabling reference data domain indexing...");
        this.disableDomainIndices(MySQLStatements.DISABLE_REFERENCE_INDICES, null);
        this.disableDomainIndices(MySQLStatements.DISABLE_FEATURE_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done disabling reference data domain indexing");
    }

    /**
     * Enables all indices belonging to the domain of genomic references.
     */
    private void enableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start enabling reference data domain indexing...");
        this.enableDomainIndices(MySQLStatements.ENABLE_REFERENCE_INDICES, null);
        this.enableDomainIndices(MySQLStatements.ENABLE_FEATURE_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done enabling reference data domain indexing");
    }
    
    /**
     * Adds all data belonging to a reference genome to the database.
     * @param reference the reference to store
     * @return the reference id
     * @throws StorageException
     */
    public int addRefGenome(ParsedReference reference) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing reference sequence  \"{0}\"", reference.getName());

        try {
            con.setAutoCommit(false);

            if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
                this.lockReferenceDomainTables();
                this.disableReferenceIndices();
            }

            this.storeGenome(reference);
            this.storeFeatures(reference);

            if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
                this.enableReferenceIndices();
                this.unlockTables();
            }

            con.setAutoCommit(true);
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished storing reference sequence \"{0}\"", reference.getName());

        // notify observers about the change of the database
        this.notifyObserversAbout("addRefGenome");

        return reference.getID();
    }
    
    /**
     * Stores a reference genome in the reference genome table of the db.
     * @param reference the reference data to store
     */
    private void storeGenome(ParsedReference reference) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing reference sequence data...");
        try (PreparedStatement insertGenome = con.prepareStatement(SQLStatements.INSERT_REFGENOME)) {
            
            int id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_REFERENCE_ID, con);
            reference.setID(id);

            // store reference data
            insertGenome.setLong(1, reference.getID());
            insertGenome.setString(2, reference.getName());
            insertGenome.setString(3, reference.getDescription());
            insertGenome.setTimestamp(4, reference.getTimestamp());
            insertGenome.setString(5, reference.getFastaFile().toString());
            insertGenome.execute();

            List<ParsedChromosome> chromosomes = reference.getChromosomes();
            for (int i = 0; i < chromosomes.size(); i++) {
                this.storeChromosome(chromosomes.get(i), i + 1, reference.getID());
            }

            con.commit();

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting reference sequence data");
    }

    /**
     * Stores a chromosome in the db.
     * @param chromosome the chromosome to store
     * @param chromNumber the chromosome number of the new chromosome
     * @param refID the reference id of the chromosome
     */
    private void storeChromosome(ParsedChromosome chromosome, int chromNumber, int refID) {
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing chromosome data...");
        try (PreparedStatement insertChromosome = con.prepareStatement(SQLStatements.INSERT_CHROMOSOME)) {
                
            int id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_CHROMOSOME_ID, con);
            chromosome.setID(id);
                
            //store chromosome data
            insertChromosome.setLong(1, chromosome.getID());
            insertChromosome.setLong(2, chromNumber);
            insertChromosome.setLong(3, refID);
            insertChromosome.setString(4, chromosome.getName());
            insertChromosome.setLong(5, chromosome.getChromLength());
            insertChromosome.execute();
                
            con.commit();
             
        } catch (SQLException ex) {
                this.rollbackOnError(this.getClass().getName(), ex);
        }
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting chromosome data");
    }

    /**
     * Stores the features of a reference genome in the feature table of the db.
     * @param reference the reference containing the features to store
     */
    private void storeFeatures(ParsedReference reference) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start inserting features...");
        try (PreparedStatement insertFeature = con.prepareStatement(SQLStatements.INSERT_FEATURE)) {

            for (ParsedChromosome chrom : reference.getChromosomes()) {
                
                int latestId = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_FEATURE_ID, con);
                
                Collections.sort(chrom.getFeatures()); //sort features by position
                chrom.setFeatId(latestId);
                chrom.distributeFeatureIds();

                int batchCounter = 1;
                Iterator<ParsedFeature> featIt = chrom.getFeatures().iterator();
                ParsedFeature feature;
                while (featIt.hasNext()) {

                    batchCounter++;
                    feature = featIt.next();
                    insertFeature.setLong(1, feature.getId());
                    insertFeature.setLong(2, chrom.getID());
                    insertFeature.setString(3, feature.getParentIdsConcat());
                    insertFeature.setInt(4, feature.getType().getTypeInt());
                    insertFeature.setInt(5, feature.getStart());
                    insertFeature.setInt(6, feature.getStop());
                    insertFeature.setString(7, feature.getLocusTag());
                    insertFeature.setString(8, feature.getProduct());
                    insertFeature.setString(9, feature.getEcNumber());
                    insertFeature.setInt(10, feature.getStrand());
                    insertFeature.setString(11, feature.getGeneName());
                    insertFeature.addBatch();

                    if (batchCounter == FEATURE_BATCH_SIZE) {
                        batchCounter = 1;
                        insertFeature.executeBatch();
                    }
                }
                insertFeature.executeBatch();

            }

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage());
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting features");
    }

    
    private void lockReferenceDomainTables() {
        this.lockDomainTables(MySQLStatements.LOCK_TABLE_REFERENCE_DOMAIN, "reference");
    }
    
    /**
     * Adds a track to the database with its file path. This means, it is stored
     * as a track for direct file access and adds the persistant track id to the 
     * track job.
     * @param track the track job containing the track information to store
     */
    public void storeBamTrack(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing bam track data...");       
        
        try (PreparedStatement insertTrack = con.prepareStatement(SQLStatements.INSERT_TRACK)) {
            insertTrack.setLong(1, track.getID());
            insertTrack.setLong(2, track.getRefId());
            insertTrack.setString(3, track.getDescription());
            insertTrack.setTimestamp(4, track.getTimestamp());
            insertTrack.setString(5, track.getFile().getAbsolutePath());
            insertTrack.execute();

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        // notify observers about the change of the database
        this.notifyObserversAbout("storeTrack");
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing bam track data");        
    }


    /**
     * Stores the statistics for a track in the db.
     * @param track the track to store containing a coverage container
     */
    public void storeTrackStatistics(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track statistics data...");

        int coveragePerf = 0;
        int coverageBM = 0;
        int coverageComplete = 0;
        int trackID = track.getID();
        StatsContainer statsContainer = track.getStatsContainer();
        Map<String, Integer> statsMap = statsContainer.getStatsMap();
        numMappings += statsMap.get(StatsContainer.NO_COMMON_MAPPINGS);
        numPerfectMappings += statsMap.get(StatsContainer.NO_PERFECT_MAPPINGS);
        numBmMappings += statsMap.get(StatsContainer.NO_BESTMATCH_MAPPINGS);
        noUniqueMappings += statsMap.get(StatsContainer.NO_UNIQ_MAPPINGS);
        noUniqueBMMappings += statsMap.get(StatsContainer.NO_UNIQ_BM_MAPPINGS);
        noUniquePerfectMappings += statsMap.get(StatsContainer.NO_UNIQ_PERF_MAPPINGS);
        noUniqueSeq += statsMap.get(StatsContainer.NO_UNIQUE_SEQS);
        noRepeatedSeq += statsMap.get(StatsContainer.NO_REPEATED_SEQ);
        numReads += statsMap.get(StatsContainer.NO_READS);
        averageReadLengthPart += statsContainer.getReadLengthDistribution().getAverageValue();
        ++containerCount;
//            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "...can't get the statistics list (MappingInfos are null)");

        try {
            // get latest id for statistic
            long latestID = GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_STATISTICS_ID, con);

            CoverageContainer cov = track.getCoverageContainer();
            if (cov.getCoveredPerfectPositions() > 0) {
                coveragePerf = cov.getCoveredPerfectPositions();
            } 

            if (cov.getCoveredBestMatchPositions() > 0) {
                coverageBM = cov.getCoveredBestMatchPositions();
            } 

            if (cov.getCoveredCommonMatchPositions() > 0) {
                coverageComplete = cov.getCoveredCommonMatchPositions();
            } 

            //calculate average read length
            int averageReadLength = this.containerCount > 0 ? this.averageReadLengthPart / this.containerCount : 0;
            try (PreparedStatement insertStatistics = con.prepareStatement(SQLStatements.INSERT_STATISTICS)) {
                insertStatistics.setLong(1, latestID);
                insertStatistics.setLong(2, trackID);
                insertStatistics.setInt(3, this.numMappings);
                insertStatistics.setInt(4, this.numPerfectMappings);
                insertStatistics.setInt(5, this.numBmMappings);
                insertStatistics.setInt(6, this.noUniqueMappings);
                insertStatistics.setInt(7, this.noUniqueBMMappings);
                insertStatistics.setInt(8, this.noUniquePerfectMappings);
                insertStatistics.setInt(9, coveragePerf);
                insertStatistics.setInt(10, coverageBM);
                insertStatistics.setInt(11, coverageComplete);
                insertStatistics.setInt(12, this.noUniqueSeq);
                insertStatistics.setInt(13, this.noRepeatedSeq);
                insertStatistics.setInt(14, this.numReads);
                insertStatistics.setInt(15, averageReadLength);
                insertStatistics.execute();

                readLengthDistribution = statsContainer.getReadLengthDistribution();
                if (!readLengthDistribution.isEmpty()) {
                    this.insertCountDistribution(readLengthDistribution, trackID);
                }
                readPairLengthDistribution = statsContainer.getReadPairSizeDistribution();
                if (!readPairLengthDistribution.isEmpty()) {
                    this.insertCountDistribution(readPairLengthDistribution, trackID);
                }
            }

            this.numMappings = 0;
            this.numPerfectMappings = 0;
            this.numBmMappings = 0;
            this.noUniqueSeq = 0;
            this.noRepeatedSeq = 0;
            this.noUniqueMappings = 0;
            this.numReads = 0;
            this.averageReadLengthPart = 0;

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track statistics data");
    }
    
    /**
     * Sets a count distribution
     * {@link DiscreteCountingDistribution} for this track.
     * @param distribution the count distribution to store
     * @param trackID track id of this track 
     */
    public void insertCountDistribution(DiscreteCountingDistribution distribution, int trackID) {

        int[] countDistribution = distribution.getDiscreteCountingDistribution();
        try (PreparedStatement insert = con.prepareStatement(SQLStatements.INSERT_COUNT_DISTRIBUTION)) {
            for (int i = 0; i < countDistribution.length; ++i) {
                insert.setInt(1, trackID);
                insert.setByte(2, distribution.getType());
                insert.setInt(3, i);
                insert.setInt(4, countDistribution[i]);
                insert.addBatch();
            }

            insert.executeBatch();

        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Stores read pair statistics
     * @param statsContainer container with the statistics
     * @param trackId track id 
     */
    public void storeReadPairTrackStatistics(StatsContainer statsContainer, int trackId) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing read pair statistics...");

        Map<String, Integer> statsMap = statsContainer.getStatsMap();

        try {
            long numReadPairs = statsMap.get(StatsContainer.NO_SEQ_PAIRS);
            long numUniqueReadPairs = statsMap.get(StatsContainer.NO_UNIQUE_PAIRS);
            long numPerfReadPairs = statsMap.get(StatsContainer.NO_PERF_PAIRS);
            long numUniquePerfReadPairs = statsMap.get(StatsContainer.NO_UNIQ_PERF_PAIRS);
            long numSmallDistPairs = statsMap.get(StatsContainer.NO_SMALL_DIST_PAIRS);
            long numSmallUniqPairs = statsMap.get(StatsContainer.NO_UNIQ_SMALL_PAIRS);
            long numLargeDistPairs = statsMap.get(StatsContainer.NO_LARGE_DIST_PAIRS);
            long numLargeUniqPairs = statsMap.get(StatsContainer.NO_UNIQ_LARGE_PAIRS);
            long numOrientWrongPairs = statsMap.get(StatsContainer.NO_ORIENT_WRONG_PAIRS);
            long numOrientWrongUniqPairs = statsMap.get(StatsContainer.NO_UNIQ_ORIENT_WRONG_PAIRS);
            long numSmallOrientWrongPairs = statsMap.get(StatsContainer.NO_SMALL_ORIENT_WRONG_PAIRS);
            long numSmallOrientUniqPairs = statsMap.get(StatsContainer.NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS);
            long numLargeOrientWrongPairs = statsMap.get(StatsContainer.NO_LARGE_ORIENT_WRONG_PAIRS);
            long numLargeOrientUniqPairs = statsMap.get(StatsContainer.NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS);
            long numSingleMappings = statsMap.get(StatsContainer.NO_SINGLE_MAPPIGNS);

            try (PreparedStatement addStatistics = con.prepareStatement(SQLStatements.INSERT_READPAIR_STATISTICS)) {

                // store track stats in table
                addStatistics.setLong(1, numReadPairs);
                addStatistics.setLong(2, numUniqueReadPairs);
                addStatistics.setLong(3, numPerfReadPairs);
                addStatistics.setLong(4, numUniquePerfReadPairs);
                addStatistics.setLong(5, numSmallDistPairs);
                addStatistics.setLong(6, numSmallUniqPairs);
                addStatistics.setLong(7, numLargeDistPairs);
                addStatistics.setLong(8, numLargeUniqPairs);
                addStatistics.setLong(9, numOrientWrongPairs);
                addStatistics.setLong(10, numOrientWrongUniqPairs);
                addStatistics.setLong(11, numSmallOrientWrongPairs);
                addStatistics.setLong(12, numSmallOrientUniqPairs);
                addStatistics.setLong(13, numLargeOrientWrongPairs);
                addStatistics.setLong(14, numLargeOrientUniqPairs);
                addStatistics.setLong(15, numSingleMappings);
                addStatistics.setLong(16, statsContainer.getReadPairSizeDistribution().getAverageValue());
                addStatistics.setLong(17, trackId);

                addStatistics.execute();

            } catch (SQLException ex) {
                this.rollbackOnError(this.getClass().getName(), ex);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing sequence pair statistics");
    }

    /**
     * Sets the read pair id for both tracks belonging to one read pair.
     * @param track1Id track id of first track of the pair
     * @param track2Id track id of second track of the pair
     */
    public void setReadPairIdsForTrackIds(long track1Id, long track2Id) {

        try {
            //not 0, because 0 is the value when a track is not a sequence pair track!
            int readPairId = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_TRACK_READ_PAIR_ID, con);

            try (PreparedStatement setReadPairIds = con.prepareStatement(SQLStatements.INSERT_TRACK_READ_PAIR_ID)) {
                setReadPairIds.setInt(1, readPairId);
                setReadPairIds.setLong(2, track1Id);
                setReadPairIds.execute();

                setReadPairIds.setInt(1, readPairId);
                setReadPairIds.setLong(2, track2Id);
                setReadPairIds.execute();
            }

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
    }

    /**
     * Locks all tables involved when adding a track in mysql fashion.
     */
    private void lockTrackDomainTables() {
        this.lockDomainTables(MySQLStatements.LOCK_TABLE_TRACK_DOMAIN, "track");
    }

    /**
     * Locks all tables declared by the lock sql statement.
     * @param lockStatement sql statement to lock some tables
     * @param domainName name of the domain to lock for logging
     */
    private void lockDomainTables(String lockStatement, String domainName) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start locking {0} domain tables...", domainName);
        try (PreparedStatement lock = con.prepareStatement(lockStatement)) {
            lock.execute();
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done locking {0} domain tables...", domainName);
    }

    private void disableTrackDomainIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started disabling track data domain indices");
        this.disableDomainIndices(MySQLStatements.DISABLE_TRACK_INDICES, null);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished disabling track data domain indices");
    }

    private void enableTrackDomainIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started enabling track data domain indices");
        this.enableDomainIndices(MySQLStatements.ENABLE_TRACK_INDICES, null);
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
        try (PreparedStatement disableDomainIndices = con.prepareStatement(sqlStatement)) {
            disableDomainIndices.execute();
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
        try (PreparedStatement enableDomainIndices = con.prepareStatement(sqlStatement)) {
            enableDomainIndices.execute();
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        if (domainName != null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished enabling {0} data domain indices", domainName);
        }
    }

    /**
     * @param refGenID the reference id
     * @return The reference genome connector for the given reference id
     */
    public ReferenceConnector getRefGenomeConnector(int refGenID) {

        // only return new object, if no suitable connector was created before
        if (!refConnectors.containsKey(refGenID)) {
            refConnectors.put(refGenID, new ReferenceConnector(refGenID));
        }
        return refConnectors.get(refGenID);
    }

    
    public TrackConnector getTrackConnector(PersistantTrack track) throws FileNotFoundException {
        // only return new object, if no suitable connector was created before
        int trackID = track.getId();
        if (!trackConnectors.containsKey(trackID)) {
            trackConnectors.put(trackID, new TrackConnector(track));
        }
        return trackConnectors.get(trackID);
    }

    
    public TrackConnector getTrackConnector(List<PersistantTrack> tracks, boolean combineTracks) throws FileNotFoundException {
        // makes sure the track id is not already used
        int id = 9999;
        for (PersistantTrack track : tracks) {
            id += track.getId();
        }
        // only return new object, if no suitable connector was created before
        trackConnectors.put(id, new TrackConnector(id, tracks, combineTracks));
        return trackConnectors.get(id);
    }
    

    public MultiTrackConnector getMultiTrackConnector(PersistantTrack track) throws FileNotFoundException {
        // only return new object, if no suitable connector was created before
        int trackID = track.getId();
        if (!multiTrackConnectors.containsKey(trackID)) { //old solution, which does not work anymore
            multiTrackConnectors.put(trackID, new MultiTrackConnector(track));
        }
        return multiTrackConnectors.get(trackID);
    }
    
    
    public MultiTrackConnector getMultiTrackConnector(List<PersistantTrack> tracks) throws FileNotFoundException {
        // makes sure the track id is not already used
        int id = 9999;
        for (PersistantTrack track : tracks) {
            id += track.getId();
        }
        // only return new object, if no suitable connector was created before
        multiTrackConnectors.put(id, new MultiTrackConnector(tracks));
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
    
    /**
     * Removes the multi track connector for the given trackId.
     * @param trackId track id of the multi track connector to remove
     */
    public void removeMultiTrackConnector(int trackId) {
        if (multiTrackConnectors.containsKey(trackId)) {
            multiTrackConnectors.remove(trackId);
        }
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
//     * Stores the project folder location in the database.
//     * @param projectFolder the path to the project folder associated to this database
//     */
//    public boolean storeProjectFolder(String projectFolder) {
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing project folder...");
//        
//        try (PreparedStatement insertProjectFolder = con.prepareStatement(SQLStatements.INSERT_PROJECT_FOLDER)) {
//
//            // store project folder
//            insertProjectFolder.setString(1, projectFolder);
//            insertProjectFolder.execute();
//
//            con.commit();
//
//        } catch (SQLException ex) {
//            this.rollbackOnError(this.getClass().getName(), ex);
//            return false;
//        }
//
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting project folder"); 
//        return true;        
//    }
    
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

    /**
     * @return All references stored in the db with their associated data. All
     * references are re-queried from the DB and returned in new, independent
     * objects each time the method is called.
     * @throws OutOfMemoryError 
     */
    public List<PersistantReference> getGenomes() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading reference genome data from database");
        List<PersistantReference> refGens = new ArrayList<>();

        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_GENOMES)) {
            
            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.REF_GEN_ID);
                String description = rs.getString(FieldNames.REF_GEN_DESCRIPTION);
                String name = rs.getString(FieldNames.REF_GEN_NAME);
                Timestamp timestamp = rs.getTimestamp(FieldNames.REF_GEN_TIMESTAMP);
                String fileName = rs.getString(FieldNames.REF_GEN_FASTA_FILE); //special handling for backwards compatibility with old DBs
                fileName = fileName == null ? "" : fileName;
                File fastaFile = new File(fileName);
                refGens.add(new PersistantReference(id, name, description, timestamp, fastaFile));
            }
            rs.close();

        } catch (SQLException e) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, e);
        }

        return refGens;
    }
    /**
     * Get an array of available genomes from the database. Alternative method
     * for getGenomes().
     * @return Array of genomes
     */
    public PersistantReference[] getGenomesAsArray() {
        List<PersistantReference> references = this.getGenomes();
        PersistantReference[] refArray = new PersistantReference[references.size()];
        return references.toArray(refArray);
    }

    /**
     * @return A map of all tracks in the connected DB mapped on their
     * respective reference.
     */
    public Map<PersistantReference, List<PersistantTrack>> getGenomesAndTracks() {
        List<PersistantReference> genomes = this.getGenomes();
        List<PersistantTrack> tracks = this.getTracks();
        Map<Integer, List<PersistantTrack>> tracksByReferenceId = new HashMap<>();
        for (PersistantTrack t : tracks) {
            List<PersistantTrack> list = tracksByReferenceId.get(t.getRefGenID());
            if (list == null) {
                list = new ArrayList<>();
                tracksByReferenceId.put(t.getRefGenID(), list);
            }
            list.add(t);
        }

        Map<PersistantReference, List<PersistantTrack>> tracksByReference = new HashMap<>();
        for (PersistantReference reference : genomes) {
            List<PersistantTrack> currentTrackList = tracksByReferenceId.get(reference.getId());
            //if the current reference genome does not have any tracks, 
            //just create an empty list
            if (currentTrackList == null) {
                currentTrackList = new ArrayList<>();
            }
            tracksByReference.put(reference, currentTrackList);
        }

        return tracksByReference;
    }

    /**
     * @return All tracks stored in the database with all their information. All
     * tracks are re-queried from the DB and returned in new, independent 
     * objects each time the method is called.
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
                int readPairId = rs.getInt(FieldNames.TRACK_READ_PAIR_ID);
                tracks.add(new PersistantTrack(id, filePath, description, date, refGenID, -1, readPairId));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tracks;
    }
    
        /**
         * @param trackID 
         * @return The track for the given track id in a fresh track object
     */
    public PersistantTrack getTrack(int trackID) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading track data from database");
        PersistantTrack track = null;

        try {
            PreparedStatement fetchTracks = con.prepareStatement(SQLStatements.FETCH_TRACK);
            fetchTracks.setInt(1, trackID);
            ResultSet rs = fetchTracks.executeQuery();

            while (rs.next()) {
                int id = rs.getInt(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                int refGenID = rs.getInt(FieldNames.TRACK_REFERENCE_ID);
                String filePath = rs.getString(FieldNames.TRACK_PATH);
                int readPairId = rs.getInt(FieldNames.TRACK_READ_PAIR_ID);
                track = new PersistantTrack(id, filePath, description, date, refGenID, readPairId);
            }

        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return track;
    }

    /**
     * @return the latest track id used in the database + 1 = the next id to
     * use.
     */
    public int getLatestTrackId() {
        return (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_TRACK_ID, con);
    }

    /**
     * Deletes all data associated with the given track id.
     * @param trackID the track id whose data shall be delete from the DB
     * @throws StorageException 
     */
    public void deleteTrack(int trackID) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of track with id \"{0}\"", trackID);
        try (PreparedStatement deleteStatistics = con.prepareStatement(SQLStatements.DELETE_STATISTIC_FROM_TRACK);
             PreparedStatement deleteCountDistributions = con.prepareStatement(SQLStatements.DELETE_COUNT_DISTRIBUTIONS_FROM_TRACK);
             PreparedStatement deleteTrack = con.prepareStatement(SQLStatements.DELETE_TRACK);) {
            
            con.setAutoCommit(false);
                       
            deleteStatistics.setInt(1, trackID);
            deleteCountDistributions.setInt(1, trackID);
            deleteTrack.setInt(1, trackID);
            
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Statistics...");
            deleteStatistics.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Count Distributions...");
            deleteCountDistributions.execute();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Track...");
            deleteTrack.execute();
            
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Track Cache...");
            ObjectCache.getInstance().deleteFamily("loadCoverage."+trackID);
            ObjectCache.getInstance().delete(ObjectCache.getTrackCacherFieldFamily(), "Track."+trackID);

            con.commit();

            con.setAutoCommit(true);
            this.trackConnectors.remove(trackID);

        } catch (SQLException ex) {
            throw new StorageException(ex);
        }
        
        // notify observers about the change of the database
        this.notifyObserversAbout("deleteTrack");
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished deletion of track \"{0}\"", trackID);
    }
    
    /**
     * Deletes all data associated with the given reference id.
     * @param refGenID  the reference id whose data shall be delete from the DB
     * @throws StorageException
     */
    public void deleteGenome(int refGenID) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of reference genome with id \"{0}\"", refGenID);
        ReferenceConnector refCon = this.getRefGenomeConnector(refGenID);
        try (PreparedStatement deleteFeatures = con.prepareStatement(SQLStatements.DELETE_FEATURES_FROM_CHROMOSOME);
             PreparedStatement deleteChrom = con.prepareStatement(SQLStatements.DELETE_CHROMOSOME);
             PreparedStatement deleteGenome = con.prepareStatement(SQLStatements.DELETE_GENOME)) {
            
            con.setAutoCommit(false);
            
            Map<Integer,PersistantChromosome> chroms = refCon.getChromosomesForGenome();
            for (PersistantChromosome chrom : chroms.values()) {
                
                deleteFeatures.setLong(1, chrom.getId());
                deleteChrom.setInt(1, chrom.getId());
                
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting features for chromosome {0}...", chrom.getName());
                deleteFeatures.execute();
                
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting chromosome {0}...", chrom.getName());
                deleteChrom.execute();
                
            }
            
            deleteGenome.setLong(1, refGenID);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting Genome {0}...", refCon.getRefGenome().getName());
            deleteGenome.execute();

            con.commit();
            con.setAutoCommit(true);
            this.refConnectors.remove(refGenID);

        } catch (SQLException ex) {
            throw new StorageException(ex);
        }
        
        // notify observers about the change of the database
        this.notifyObserversAbout("deleteGenomes");
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished deletion of reference genome with id \"{0}\"", refGenID);
    }

    /**
     * @return The database adapter string for this project
     */
    public String getAdapter() {
        return this.adapter;
    }

    /**
     * Resets the file path of a direct access reference.
     * @param track track whose file path has to be resetted.
     * @throws StorageException
     */
    public void resetTrackPath(PersistantTrack track) throws StorageException {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Preparing statements for storing track data");

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.lockTrackDomainTables();
            this.disableTrackDomainIndices();
        }

        try (PreparedStatement resetTrackPath = con.prepareStatement(SQLStatements.RESET_TRACK_PATH)) {
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
     * Resets the fasta file path of a direct access reference.
     * @param fastaFile fasta file to reset for the current reference.
     * @param ref The reference genome, whose file shall be updated
     * @throws StorageException
     */
    public void resetRefPath(File fastaFile, PersistantReference ref) throws StorageException {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Preparing statements for storing track data");

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.lockReferenceDomainTables();
            this.disableReferenceIndices();
        }

        try (PreparedStatement resetRefPath = con.prepareStatement(SQLStatements.RESET_REF_PATH)) {
            resetRefPath.setString(1, fastaFile.getAbsolutePath());
            resetRefPath.setLong(2, ref.getId());
            resetRefPath.execute();
            ref.resetFastaPath(fastaFile);
        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }

        if (adapter.equalsIgnoreCase(Properties.ADAPTER_MYSQL)) {
            this.enableReferenceIndices();
            this.unlockTables();
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reference file for \"{0}\" has been updated successfully", ref.getName());
    }
    

    private void notifyObserversAbout(final String message) {
        this.setChanged();
        this.notifyObservers(message);
    }

    /**
     * Checks the DB version and executes appropriate handling according to the
     * given version number. If an update was performed, the current DB version
     * number will be set after a successful update.
     */
    private void checkDBVersion() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Checking DB version...");
        
        try (PreparedStatement fetchDBVersion = con.prepareStatement(SQLStatements.FETCH_DB_VERSION)) {
            ResultSet rs = fetchDBVersion.executeQuery();
            boolean updateNeeded = false;
            int dbVersion = 0;
            if (rs.next()) {
                dbVersion = rs.getInt(FieldNames.DB_VERSION_DB_VERSION_NO);
            } 
            
            ResultSet fileColumn = con.getMetaData().getColumns(con.getCatalog(), "PUBLIC", FieldNames.TABLE_REFERENCE, FieldNames.REF_GEN_FASTA_FILE);
            boolean columnFastafileMissing = !fileColumn.next();
            
            //move references to chromosome table
            if (dbVersion < 2 || columnFastafileMissing) {
                updateNeeded = true;
                this.createChromsFromRefs();
            }
            
            if (updateNeeded) {
                if (dbVersion == 0) {
                    PreparedStatement setDBVersion = con.prepareStatement(SQLStatements.INSERT_DB_VERSION_NO);
                    setDBVersion.setInt(1, DB_VERSION_NO);
                    setDBVersion.executeUpdate();
                } else { //the entry already exists and has to be replaced
                    PreparedStatement updateDBVersion = con.prepareStatement(SQLStatements.UPDATE_DB_VERSION_NO);
                    updateDBVersion.setInt(1, DB_VERSION_NO);
                    updateDBVersion.executeUpdate();
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Done checking DB version and updated to latest version");
    }

    /**
     * Moves all reference sequences, still stored in the reference table into
     * the chromosome table and creates corresponding ids and chromosome names.
     * It also sets the corresponding chromosome id for all genomic features in 
     * the feature table and removes the reference sequence column from the
     * reference table.
     * This method is intended for DBs < version 2.
     */
    private void createChromsFromRefs() throws SQLException {
        //Add column fastafile to reference table
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_REFERENCE, FieldNames.REF_GEN_FASTA_FILE, "VARCHAR(600)"));
        //add column chromosome id to features
        this.runSqlStatement(GenericSQLQueries.genAddColumnString(FieldNames.TABLE_FEATURES, FieldNames.FEATURE_CHROMOSOME_ID, BIGINT_UNSIGNED));
        
        List<PersistantReference> refList = this.getGenomesDbUpgrade();
        
        
        for (PersistantReference ref : refList) {
            try (PreparedStatement fetchRefSeq = con.prepareStatement(SQLStatements.FETCH_REF_SEQ);) {
                
                fetchRefSeq.setInt(1, ref.getId());
                ResultSet rs = fetchRefSeq.executeQuery();
                if (rs.next()) {

                    String refSeq = rs.getString(FieldNames.REF_GEN_SEQUENCE);
                    String chromName = rs.getString(FieldNames.REF_GEN_NAME);

                    String preparedRefName = ref.getName().replace(':', '-').
                            replace('/', '-').
                            replace('\\', '-').
                            replace('*', '-').
                            replace('?', '-').
                            replace('|', '-').
                            replace('<', '-').
                            replace('>', '-').
                            replace('"', '_').
                            replace(" ", "_");
                    String pathString = new File(dbLocation).getParent().concat("\\" + preparedRefName.concat(".fasta"));
                    Path fastaPath = new File(pathString).toPath();
                    try (FastaLineWriter fastaWriter = FastaLineWriter.fileWriter(fastaPath)) {
                        fastaWriter.writeHeader(ref.getName());
                        fastaWriter.appendSequence(refSeq);
                        PreparedStatement updateRefFile = con.prepareStatement(SQLStatements.UPDATE_REF_FILE);
                        updateRefFile.setString(1, pathString);
                        updateRefFile.setInt(2, ref.getId());
                        updateRefFile.execute();
                        FastaUtils fastaUtils = new FastaUtils();
                        fastaUtils.getIndexedFasta(fastaPath.toFile());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(new JPanel(), "Reference fasta file cannot be written to disk! Change the permissions in the DB folder!",
                                "Reference fasta cannot be written to DB folder", JOptionPane.ERROR_MESSAGE);
                        throw new SQLException("Cannot update reference table, since fasta file is missing. Please retry after changing the permissions in the DB folder!");
                    }

                    ParsedChromosome newChrom = new ParsedChromosome();
                    newChrom.setName(chromName);
                    newChrom.setChromLength(refSeq.length());

                    this.storeChromosome(newChrom, 1, ref.getId());

                    //Update chromosome ids of the features for this reference
                    //Since there is exactly one chrom for the current genome, we can query it as follows:
                    PersistantChromosome chrom = getRefGenomeConnector(ref.getId()).getChromosomesForGenome().values().iterator().next();

                    PreparedStatement updateFeatureTable = con.prepareStatement(SQLStatements.UPDATE_FEATURE_TABLE);
                    updateFeatureTable.setInt(1, chrom.getId());
                    updateFeatureTable.setInt(2, ref.getId());
                    updateFeatureTable.executeUpdate();
                }
            }
        }

        //set default value for references without file and set column not null
        this.runSqlStatement(SQLStatements.INIT_FASTAFILE);
        this.runSqlStatement(SQLStatements.NOT_NULL_FASTAFILE);

        //after setting all chromosome ids of the features, now we can delete the REFERENCE_ID
        this.runSqlStatement(SQLStatements.NOT_NULL_CHROMOSOME_ID);
        //Drop old REFERENCE_ID column
        this.runSqlStatement(GenericSQLQueries.genRemoveColumnString(
                FieldNames.TABLE_FEATURES, FieldNames.FEATURE_REFGEN_ID));
        //Drop old ref seq column for this DB
        this.runSqlStatement(GenericSQLQueries.genRemoveColumnString(
                FieldNames.TABLE_REFERENCE, FieldNames.REF_GEN_SEQUENCE));
    }
    
    /**
     * @return All references stored in the db with their associated data. All
     * references are re-queried from the DB and returned in new, independent
     * objects each time the method is called. No check of fasta files is performed
     * @throws OutOfMemoryError 
     */
    public List<PersistantReference> getGenomesDbUpgrade() throws OutOfMemoryError {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading reference genome data from database");
        List<PersistantReference> refGens = new ArrayList<>();

        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_GENOMES)) {
            
            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.REF_GEN_ID);
                String description = rs.getString(FieldNames.REF_GEN_DESCRIPTION);
                String name = rs.getString(FieldNames.REF_GEN_NAME);
                Timestamp timestamp = rs.getTimestamp(FieldNames.REF_GEN_TIMESTAMP);
                String fileName = rs.getString(FieldNames.REF_GEN_FASTA_FILE); //special handling for backwards compatibility with old DBs
                fileName = fileName == null ? "" : fileName;
                File fastaFile = new File(fileName);
                refGens.add(new PersistantReference(id, 1, name, description, timestamp, fastaFile, false));
            }
            rs.close();

        } catch (SQLException e) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, e);
        }

        return refGens;
    }
    
    /**
     * @return The location of the database
     */
    public String getDBLocation() {
        return this.dbLocation;
    }
    
    /**
     * @param tracks The list of tracks to convert to a map
     * @return Converts the given track list into a map of tracks to their track
     * id.
     */
    public static Map<Integer, PersistantTrack> getTrackMap(List<PersistantTrack> tracks) {
        Map<Integer, PersistantTrack> trackMap = new HashMap<>();
        for (PersistantTrack track : tracks) {
            trackMap.put(track.getId(), track);
        }
        return trackMap;
    }
}
