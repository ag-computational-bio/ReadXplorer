/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.databackend.connector;


import de.cebitec.common.parser.fasta.FastaLineWriter;
import de.cebitec.readxplorer.api.FileException;
import de.cebitec.readxplorer.api.enums.MappingClass;
import de.cebitec.readxplorer.databackend.FieldNames;
import de.cebitec.readxplorer.databackend.GenericSQLQueries;
import de.cebitec.readxplorer.databackend.H2SQLStatements;
import de.cebitec.readxplorer.databackend.SQLStatements;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.parser.common.ParsedChromosome;
import de.cebitec.readxplorer.parser.common.ParsedFeature;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.utils.DiscreteCountingDistribution;
import de.cebitec.readxplorer.utils.FastaUtils;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Logger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.openide.util.NbBundle;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;


/**
 * Responsible for the connection between user interface and data base. Contains
 * the methods to communicate with the data base.
 * <p>
 * @author ddoppmeier, rhilker
 */
public final class ProjectConnector extends Observable {

    private static final Logger LOG = Logger.getLogger( ProjectConnector.class.getName() );

    private static final int FEATURE_BATCH_SIZE = 100000; //TODO test larger batch sizes
    private static final int DB_VERSION_NO = 3;

    private static final String BIGINT_UNSIGNED = "BIGINT UNSIGNED";
    private static final String VARCHAR400 = "VARCHAR(400)";
    private static final String VARCHAR1000 = "VARCHAR(1000)";

    private final Map<Integer, TrackConnector> trackConnectors;
    private final Map<Integer, MultiTrackConnector> multiTrackConnectors;
    private final Map<Integer, ReferenceConnector> refConnectors;

    private static ProjectConnector dbConnector;

    private String url;
    private String dbLocation;
    private JdbcConnectionPool connectionPool;


    /**
     * Responsible for the connection between user interface and data base.
     * Contains the methods to communicate with the data base.
     */
    private ProjectConnector() {

        trackConnectors = new HashMap<>();
        multiTrackConnectors = new HashMap<>();
        refConnectors = new HashMap<>();

    }


    /**
     * @return The singleton instance of the ProjectConnector
     */
    public static ProjectConnector getInstance() {
        if( dbConnector == null ) {
            dbConnector = new ProjectConnector();
        }
        return dbConnector;
    }


    /**
     * @return True, if the project connector is currently connected to a DB,
     *         false otherwise
     * <p>
     * @throws de.cebitec.readxplorer.databackend.connector.DatabaseException
     */
    public boolean isConnected() throws DatabaseException {

        if( connectionPool != null ) {
            try( Connection con = connectionPool.getConnection() ) {
                return con.isValid( 0 );
            } catch( SQLException ex ) {
                LOG.log( WARNING, ex.getMessage(), ex );
                throw new DatabaseException( "Connection to database could not be established!", ex.getMessage(), ex );
            }
        }

        return false;

    }


    /**
     * Connects to the H2 database used for the current project.
     * <p>
     * @param projectLocation the project location
     * <p>
     * @throws de.cebitec.readxplorer.databackend.connector.DatabaseException
     * @throws SQLException
     * @throws JdbcSQLException
     */
    public void connect( String projectLocation ) throws DatabaseException {

        LOG.info( "Connecting to database" );
        try {

            this.dbLocation = projectLocation;
            this.url = "jdbc:h2:" + projectLocation + ";AUTO_SERVER=TRUE;MULTI_THREADED=1;CACHE_SIZE=200000";

            connectionPool = JdbcConnectionPool.create( url, "", "" );

            try( Connection con = connectionPool.getConnection(); ) {
                con.setAutoCommit( false );
                LOG.info( "Successfully connected to database" );
                try {
                    setupDatabase( con );
                    con.commit();
                } catch( SQLException ex ) {
                    try {
                        con.rollback();
                    } catch( SQLException exRb ) {
                        LOG.log( SEVERE, "database setup failed! rollback failed!", exRb );
                        throw new DatabaseException( "Could not setup the database! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                    }
                    LOG.log( SEVERE, "database setup failed! rolledbacked!", ex );
                    throw new DatabaseException( "Could not setup the database!", ex.getMessage(), ex );
                }
                con.setAutoCommit( true );
            }

            // notify observers about the change of the database
            notifyObserversAbout( "connect" );

        } catch( SQLException ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            throw new DatabaseException( "Could not connect to the database!", ex.getMessage(), ex );
        }

    }


    /**
     * Makes sure that an H2 DB is in a correct up-to-date state. Either creates
     * all tables necessary for a ReadXplorer DB or updates them, if anything is
     * missing/different. If no changes are necessary nothing is altered.
     */
    private void setupDatabase( Connection con ) throws SQLException {

        LOG.info( "Setting up tables and indices if not existent" );
        try( Statement stmt = con.createStatement() ) {

            //create tables if not exist yet
            stmt.execute( H2SQLStatements.SETUP_REFERENCE_GENOME );

            stmt.execute( H2SQLStatements.SETUP_CHROMOSOME );
            stmt.execute( H2SQLStatements.INDEX_CHROMOSOME );

            stmt.execute( H2SQLStatements.SETUP_FEATURES );
            stmt.execute( H2SQLStatements.INDEX_FEATURES );

            stmt.execute( H2SQLStatements.SETUP_TRACKS );
            stmt.execute( H2SQLStatements.INDEX_TRACK_REFID );
            stmt.execute( H2SQLStatements.INDEX_TRACK_READ_PAIR_ID );

            stmt.execute( SQLStatements.SETUP_STATISTICS );

            stmt.execute( H2SQLStatements.SETUP_COUNT_DISTRIBUTION );
            stmt.execute( H2SQLStatements.INDEX_COUNT_DIST );

            stmt.execute( SQLStatements.SETUP_DB_VERSION_TABLE );

            checkDBStructure( con );

            LOG.info( "Finished creating tables and indices if not existent before" );

        }

    }


    /**
     * Any additional columns which were added to existing tables in newer
     * ReadXplorer versions should be checked by this method to ensure correct
     * database structure and avoiding errors when SQL statements request one of
     * these columns, which are not existent in older databases.
     */
    private void checkDBStructure( Connection con ) throws SQLException {

        LOG.info( "Checking DB structure..." );
        try( Statement stmt = con.createStatement() ) {

            //remove all old tables not used anymore
            stmt.execute( SQLStatements.DROP_TABLE + "STATICS" );
            stmt.execute( SQLStatements.DROP_TABLE + "SUBFEATURES" );
            stmt.execute( SQLStatements.DROP_TABLE + "COVERAGE_DISTRIBUTION" );
            stmt.execute( SQLStatements.DROP_TABLE + "POSITIONS" );

            //add read pair id column in tracks if not existent
            stmt.execute( GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_TRACK, FieldNames.TRACK_READ_PAIR_ID, BIGINT_UNSIGNED ) );

            stmt.execute( GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_FEATURES, FieldNames.FEATURE_GENE, "VARCHAR (20)" ) );

            stmt.execute( GenericSQLQueries.genAddColumnString(
                    FieldNames.TABLE_TRACK, FieldNames.TRACK_PATH, VARCHAR400 ) );


            /**
             * delete old "RUN_ID" field from the database to avoid problems
             * with null values in insert statement an error will be raised by
             * the query, if the field does not exist (simply ignore the error)
             */
            stmt.execute( GenericSQLQueries.genRemoveColumnString(
                    FieldNames.TABLE_TRACK, "RUN_ID" ) );

            //Add column parent id to feature table
            stmt.execute( GenericSQLQueries.genAddColumnString( FieldNames.TABLE_FEATURES, FieldNames.FEATURE_PARENT_IDS, VARCHAR1000 ) );
            stmt.execute( SQLStatements.INIT_FEATURE_PARENT_ID );
            stmt.execute( SQLStatements.NOT_NULL_FEATURE_PARENT_ID );
            //Drop old PARENT_ID column
            stmt.execute( GenericSQLQueries.genRemoveColumnString(
                    FieldNames.TABLE_FEATURES, "PARENT_ID" ) );

            //Drop unneeded indexes
            stmt.execute( SQLStatements.DROP_INDEX + "INDEXPOS" );

            checkDBVersion( con );

            LOG.info( "Finished checking DB structure." );

        }

    }


    /**
     * Checks the DB version and executes appropriate handling according to the
     * given version number. If an update was performed, the current DB version
     * number will be set after a successful update.
     */
    private void checkDBVersion( Connection con ) throws SQLException {

        LOG.info( "Checking DB version..." );
        try( final Statement stmt = con.createStatement();
             final ResultSet rs = stmt.executeQuery( SQLStatements.FETCH_DB_VERSION ); ) {

            boolean updateNeeded = false;
            int dbVersion = 0;
            if( rs.next() ) {
                dbVersion = rs.getInt( FieldNames.DB_VERSION_DB_VERSION_NO );
            }

            //restructure statistics table
            if( dbVersion < 3 ) {
                updateNeeded = true;
                restructureStatisticsTable( con );
            }

            ResultSet fileColumn = con.getMetaData().getColumns( con.getCatalog(), "PUBLIC", FieldNames.TABLE_REFERENCE, FieldNames.REF_GEN_FASTA_FILE );
            boolean columnFastafileMissing = !fileColumn.next();

            //move references to chromosome table
            if( dbVersion < 2 || columnFastafileMissing ) {
                updateNeeded = true;
                createChromsFromRefs( con );
            }

            if( updateNeeded ) {
                if( dbVersion == 0 ) {
                    PreparedStatement setDBVersion = con.prepareStatement( SQLStatements.INSERT_DB_VERSION_NO );
                    setDBVersion.setInt( 1, DB_VERSION_NO );
                    setDBVersion.executeUpdate();
                } else { //the entry already exists and has to be replaced
                    PreparedStatement updateDBVersion = con.prepareStatement( SQLStatements.UPDATE_DB_VERSION_NO );
                    updateDBVersion.setInt( 1, DB_VERSION_NO );
                    updateDBVersion.executeUpdate();
                }
            }
            LOG.info( "Done checking DB version and updated to latest version" );

        }

    }


    /**
     * Restructures the statistics table:<br>
     * 1. Reads stats content into one StatsContainer per track<br>
     * 2. Drops old table<br>
     * 3. Creates new table with 4 columns<br>
     * 4. Inserts the data of all StatsContainers in the new table This method
     * is intended for DBs < version 3.
     */
    @NbBundle.Messages( { "TITLE_FileReset=Reset track file path",
                          "MSG_FileReset=If you do not reset the track file location, it cannot be opened" } )
    private static void restructureStatisticsTable( Connection con ) throws SQLException {

        Map<Integer, StatsContainer> trackIdToStatsMap = new HashMap<>();
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        List<PersistentTrack> tracks = projectConnector.getTracks();
        for( PersistentTrack track : tracks ) {
            trackIdToStatsMap.put( track.getId(), projectConnector.getTrackStats( con, track.getId() ) );
        }

        try( Statement stmt = con.createStatement() ) {
            stmt.execute( SQLStatements.DROP_TABLE + "STATISTICS" );
            stmt.execute( SQLStatements.SETUP_STATISTICS );
        }

        for( PersistentTrack track : tracks ) {
            if( trackIdToStatsMap.containsKey( track.getId() ) ) {
                StatsContainer statsContainer = trackIdToStatsMap.get( track.getId() );
                projectConnector.storeTrackStatistics( statsContainer, track.getId() );
            }
        }

    }


    /**
     * Moves all reference sequences, still stored in the reference table into
     * the chromosome table and creates corresponding ids and chromosome names.
     * It also sets the corresponding chromosome id for all genomic features in
     * the feature table and removes the reference sequence column from the
     * reference table. This method is intended for DBs < version 2.
     */
    private void createChromsFromRefs( Connection con ) throws SQLException {

        try( final Statement stmt = con.createStatement();
             final PreparedStatement pStmtFetchRefSeq = con.prepareStatement( SQLStatements.FETCH_REF_SEQ ) ) {

            //Add column fastafile to reference table
            stmt.execute( GenericSQLQueries.genAddColumnString( FieldNames.TABLE_REFERENCE, FieldNames.REF_GEN_FASTA_FILE, "VARCHAR(600)" ) );
            //add column chromosome id to features
            stmt.execute( GenericSQLQueries.genAddColumnString( FieldNames.TABLE_FEATURES, FieldNames.FEATURE_CHROMOSOME_ID, BIGINT_UNSIGNED ) );

            for( final PersistentReference ref : getReferences() ) {
                pStmtFetchRefSeq.setInt( 1, ref.getId() );
                try( final ResultSet rs = pStmtFetchRefSeq.executeQuery() ) {

                    if( rs.next() ) {
                        String refSeq = rs.getString( FieldNames.REF_GEN_SEQUENCE );
                        String chromName = rs.getString( FieldNames.REF_GEN_NAME );

                        String preparedRefName = ref.getName()
                                .replace( ':', '-' )
                                .replace( '/', '-' )
                                .replace( '\\', '-' )
                                .replace( '*', '-' )
                                .replace( '?', '-' )
                                .replace( '|', '-' )
                                .replace( '<', '-' )
                                .replace( '>', '-' )
                                .replace( '"', '_' )
                                .replace( ' ', '_' );
                        String pathString = new File( dbLocation ).getParent().concat( "\\" + preparedRefName.concat( ".fasta" ) );
                        Path fastaPath = new File( pathString ).toPath();
                        try( final FastaLineWriter fastaWriter = FastaLineWriter.fileWriter( fastaPath ) ) {
                            fastaWriter.writeHeader( ref.getName() );
                            fastaWriter.appendSequence( refSeq );
                            try( PreparedStatement pStmtUpdateRefFile = con.prepareStatement( SQLStatements.UPDATE_REF_FILE ) ) {
                                pStmtUpdateRefFile.setString( 1, pathString );
                                pStmtUpdateRefFile.setInt( 2, ref.getId() );
                                pStmtUpdateRefFile.execute();
                            }
                            FastaUtils fastaUtils = new FastaUtils();
                            fastaUtils.getIndexedFasta( fastaPath.toFile() );
                        } catch( IOException ex ) {
                            LOG.log( SEVERE, "Reference fasta file cannot be written to disk! Change the permissions in the DB folder!", ex );
                            throw new FileException( "Reference fasta file cannot be written to disk! Change the permissions in the DB folder!", ex.getMessage(), ex );
                        }

                        ParsedChromosome newChrom = new ParsedChromosome();
                        newChrom.setName( chromName );
                        newChrom.setChromLength( refSeq.length() );

                        storeChromosome( con, newChrom, 1, ref.getId() );

                        //Update chromosome ids of the features for this reference
                        //Since there is exactly one chrom for the current genome, we can query it as follows:
                        PersistentChromosome chrom = getRefGenomeConnector( ref.getId() ).getChromosomesForGenome().values().iterator().next();

                        PreparedStatement updateFeatureTable = con.prepareStatement( SQLStatements.UPDATE_FEATURE_TABLE );
                        updateFeatureTable.setInt( 1, chrom.getId() );
                        updateFeatureTable.setInt( 2, ref.getId() );
                        updateFeatureTable.executeUpdate();
                    }

                }

                //set default value for references without file and set column not null
                stmt.execute( SQLStatements.INIT_FASTAFILE );
                stmt.execute( SQLStatements.NOT_NULL_FASTAFILE );

                //after setting all chromosome ids of the features, now we can delete the REFERENCE_ID
                stmt.execute( SQLStatements.NOT_NULL_CHROMOSOME_ID );
                //Drop old REFERENCE_ID column
                stmt.execute( GenericSQLQueries.genRemoveColumnString( FieldNames.TABLE_FEATURES, FieldNames.FEATURE_REFGEN_ID ) );
                //Drop old ref seq column for this DB
                stmt.execute( GenericSQLQueries.genRemoveColumnString( FieldNames.TABLE_REFERENCE, FieldNames.REF_GEN_SEQUENCE ) );

            }

        } catch( SQLException ex ) {
            String msg = "reading reference genome data failed!";
            LOG.log( SEVERE, msg, ex );
            throw new SQLException( msg, ex );
        }

    }


    /**
     * Method for downward compatibility of old databases with old statistics
     * tables. DO NOT USE FOR OTHER PURPOSES!
     * <p>
     * @param wantedTrackId the id of the track, whose statistics are needed.
     * <p>
     * @return The complete statistics for the track specified by the given id.
     */
    private static StatsContainer getTrackStats( Connection con, int wantedTrackId ) throws SQLException {

        try( PreparedStatement pStmtFetch = con.prepareStatement( SQLStatements.FETCH_STATS_FOR_TRACK ) ) {
             pStmtFetch.setInt( 1, wantedTrackId );

            StatsContainer statsContainer = new StatsContainer();
            statsContainer.prepareForTrack();
            statsContainer.prepareForReadPairTrack();
            try( ResultSet rs = pStmtFetch.executeQuery() ) {
                while( rs.next() ) {
                    //General data
                    statsContainer.increaseValue( MappingClass.PERFECT_MATCH.toString(), rs.getInt( FieldNames.STATISTICS_NUMBER_PERFECT_MAPPINGS ) );
                    statsContainer.increaseValue( MappingClass.BEST_MATCH.toString(), rs.getInt( FieldNames.STATISTICS_NUMBER_BM_MAPPINGS ) );
                    statsContainer.increaseValue( MappingClass.COMMON_MATCH.toString(), rs.getInt( FieldNames.STATISTICS_NUMBER_OF_MAPPINGS ) );
                    statsContainer.increaseValue( MappingClass.PERFECT_MATCH + StatsContainer.COVERAGE_STRING, rs.getInt( FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME ) );
                    statsContainer.increaseValue( MappingClass.BEST_MATCH + StatsContainer.COVERAGE_STRING, rs.getInt( FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME ) );
                    statsContainer.increaseValue( MappingClass.COMMON_MATCH + StatsContainer.COVERAGE_STRING, rs.getInt( FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME ) );
                    statsContainer.increaseValue( StatsContainer.NO_READS, rs.getInt( FieldNames.STATISTICS_NUMBER_READS ) );
                    statsContainer.increaseValue( StatsContainer.NO_REPEATED_SEQ, rs.getInt( FieldNames.STATISTICS_NUMBER_OF_REPEATED_SEQ ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQUE_SEQS, rs.getInt( FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQ_MAPPINGS, rs.getInt( FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS ) );
                    statsContainer.increaseValue( StatsContainer.AVERAGE_READ_LENGTH, rs.getInt( FieldNames.STATISTICS_AVERAGE_READ_LENGTH ) );
                    //Read pair data
                    statsContainer.increaseValue( StatsContainer.NO_LARGE_DIST_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_LARGE_DIST_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_LARGE_ORIENT_WRONG_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_ORIENT_WRONG_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_ORIENT_WRONG_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_PERF_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_READ_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_SINGLE_MAPPIGNS, rs.getInt( FieldNames.STATISTICS_NUM_SINGLE_MAPPINGS ) );
                    statsContainer.increaseValue( StatsContainer.NO_SMALL_DIST_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_SMALL_DIST_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_SMALL_ORIENT_WRONG_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQUE_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQ_LARGE_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_UNIQ_LARGE_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQ_ORIENT_WRONG_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQ_PERF_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQ_SMALL_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_UNIQ_SMALL_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS, rs.getInt( FieldNames.STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS ) );
                    statsContainer.increaseValue( StatsContainer.AVERAGE_READ_PAIR_SIZE, rs.getInt( FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH ) );
                }
            }
            return statsContainer;

        } catch( SQLException ex ) {
            String msg = "Unfortunately, the Statistics table seems to be broken. In this case the DB has to be re-created" +
                     " to get the correct statistics entries for each track again." +
                     " Statistics table format error";
            LOG.log( SEVERE, msg, ex );
            throw new SQLException( msg, ex );
        }

    }


    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }


    /**
     * @return The location of the database
     */
    public String getDbLocation() {
        return dbLocation;
    }


    /**
     * Disconnects the current DB connection.
     * <p>
     * @throws de.cebitec.readxplorer.databackend.connector.DatabaseException
     */
    public void disconnect() {

        LOG.info( "Closing database connection" );
        connectionPool.dispose();
        connectionPool = null;
        trackConnectors.clear();
        refConnectors.clear();

        // notify observers about the change of the database
        notifyObserversAbout( "disconnect" );

    }


    /**
     * Adds all data belonging to a reference genome to the database.
     * <p>
     * @param reference the reference to store
     * <p>
     * @return the reference id
     * <p>
     * @throws DatabaseException
     */
    public int addRefGenome( final ParsedReference reference ) throws DatabaseException {

        LOG.log( INFO, "Start storing reference sequence  \"{0}\"", reference.getName() );
        try( Connection con = connectionPool.getConnection() ) {
            con.setAutoCommit( false );
            try {

                storeGenome( con, reference );
                storeFeatures( con, reference );
                con.commit();
                con.setAutoCommit( true );

                LOG.log( INFO, "finished storing reference sequence \"{0}\"", reference.getName() );
                // notify observers about the change of the database
                notifyObserversAbout( "addRefGenome" );

                return reference.getID();

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( SQLException exRb ) {
                    LOG.log( SEVERE, "adding reference genome failed! rollback failed!", exRb );
                    throw new DatabaseException( "Adding reference genome failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "adding reference genome failed! rolledbacked!", ex );
                throw new DatabaseException( "Adding reference genome failed!", ex.getMessage(), ex );
            }
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Stores a reference genome in the reference genome table of the db.
     * <p>
     * @param reference the reference data to store
     */
    private static void storeGenome( Connection con, ParsedReference reference ) throws SQLException {

        LOG.info( "start storing reference sequence data..." );
        try( PreparedStatement insertGenome = con.prepareStatement( SQLStatements.INSERT_REFGENOME ) ) {

            int id = (int) GenericSQLQueries.getLatestIDFromDB( SQLStatements.GET_LATEST_REFERENCE_ID, con );
            reference.setID( id );

            // store reference data
            insertGenome.setLong( 1, reference.getID() );
            insertGenome.setString( 2, reference.getName() );
            insertGenome.setString( 3, reference.getDescription() );
            insertGenome.setTimestamp( 4, reference.getTimestamp() );
            insertGenome.setString( 5, reference.getFastaFile().toString() );
            insertGenome.execute();

            List<ParsedChromosome> chromosomes = reference.getChromosomes();
            for( int i = 0; i < chromosomes.size(); i++ ) {
                storeChromosome( con, chromosomes.get( i ), i + 1, reference.getID() );
            }

        } catch( SQLException ex ) {
            String msg = "storing genome failed!";
            LOG.log( SEVERE, msg, ex );
            throw new SQLException( msg, ex );
        }

        LOG.info( "...done inserting reference sequence data" );

    }


    /**
     * Stores a chromosome in the db.
     * <p>
     * @param chromosome  the chromosome to store
     * @param chromNumber the chromosome number of the new chromosome
     * @param refID       the reference id of the chromosome
     */
    private static void storeChromosome( Connection con, ParsedChromosome chromosome, int chromNumber, int refID ) throws SQLException {

        LOG.info( "start storing chromosome data..." );
        try( PreparedStatement insertChromosome = con.prepareStatement( SQLStatements.INSERT_CHROMOSOME ) ) {

            int id = (int) GenericSQLQueries.getLatestIDFromDB( SQLStatements.GET_LATEST_CHROMOSOME_ID, con );
            chromosome.setID( id );

            //store chromosome data
            insertChromosome.setLong( 1, chromosome.getID() );
            insertChromosome.setLong( 2, chromNumber );
            insertChromosome.setLong( 3, refID );
            insertChromosome.setString( 4, chromosome.getName() );
            insertChromosome.setLong( 5, chromosome.getChromLength() );
            insertChromosome.execute();

        } catch( SQLException ex ) {
            String msg = "storing chromosome failed!";
            LOG.log( SEVERE, msg, ex );
            throw new SQLException( msg, ex );
        }

        LOG.info( "...done inserting chromosome data" );

    }


    /**
     * Stores the features of a reference genome in the feature table of the db.
     * <p>
     * @param reference the reference containing the features to store
     */
    private static void storeFeatures( Connection con, ParsedReference reference ) throws SQLException {

        LOG.info( "start inserting features..." );
        try( final PreparedStatement insertFeature = con.prepareStatement( SQLStatements.INSERT_FEATURE ) ) {

            for( final ParsedChromosome chrom : reference.getChromosomes() ) {

                int latestId = (int) GenericSQLQueries.getLatestIDFromDB( SQLStatements.GET_LATEST_FEATURE_ID, con );

                chrom.setFeatId( latestId );
                chrom.distributeFeatureIds();

                int batchCounter = 0;
                insertFeature.setLong( 2, chrom.getID() );
                for( ParsedFeature feature : chrom.getFeatures() ) {
                    batchCounter++;
                    insertFeature.setLong( 1, feature.getId() );
                    insertFeature.setString( 3, feature.getParentIdsConcat() );
                    insertFeature.setInt( 4, feature.getType().getType() );
                    insertFeature.setInt( 5, feature.getStart() );
                    insertFeature.setInt( 6, feature.getStop() );
                    insertFeature.setString( 7, feature.getLocusTag() );
                    insertFeature.setString( 8, feature.getProduct() );
                    insertFeature.setString( 9, feature.getEcNumber() );
                    insertFeature.setInt( 10, feature.getStrand().getType() );
                    insertFeature.setString( 11, feature.getGeneName() );
                    insertFeature.addBatch();

                    if( batchCounter == FEATURE_BATCH_SIZE ) {
                        batchCounter = 0;
                        insertFeature.executeBatch();
                    }
                }
                insertFeature.executeBatch();

            }

        } catch( SQLException ex ) {
            String msg = "storing features failed!";
            LOG.log( SEVERE, msg, ex );
            throw new SQLException( msg, ex );
        }

        LOG.info( "...done inserting features" );

    }


    /**
     * Adds a track to the database with its file path. This means, it is stored
     * as a track for direct file access and adds the persistent track id to the
     * track job.
     * <p>
     * @param track the track job containing the track information to store
     */
    public void storeBamTrack( final ParsedTrack track ) throws DatabaseException {

        LOG.info( "start storing bam track data..." );
        try( Connection con = connectionPool.getConnection() ) {
            try( PreparedStatement pStmtInsertTrack = con.prepareStatement( SQLStatements.INSERT_TRACK ) ) {

                pStmtInsertTrack.setLong( 1, track.getID() );
                pStmtInsertTrack.setLong( 2, track.getRefId() );
                pStmtInsertTrack.setString( 3, track.getDescription() );
                pStmtInsertTrack.setTimestamp( 4, track.getTimestamp() );
                pStmtInsertTrack.setString( 5, track.getFile().getAbsolutePath() );
                pStmtInsertTrack.execute();

                // notify observers about the change of the database
                notifyObserversAbout( "storeTrack" );
                LOG.info( "...done storing bam track data" );

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "adding track failed! rollback failed!", exRb );
                    throw new DatabaseException( "Adding track failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "adding track failed! rolledbacked!", ex );
                throw new DatabaseException( "Adding track failed!", ex.getMessage(), ex );
            }
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Stores the statistics for a track in the db.
     * <p>
     * @param statsContainer The container with all statistics for the given
     *                       track ID
     * @param trackID        the track id whose data shall be stored
     */
    public void storeTrackStatistics( final StatsContainer statsContainer, final int trackID ) throws DatabaseException {

        LOG.info( "Start storing track statistics..." );

        DiscreteCountingDistribution readLengthDistribution = statsContainer.getReadLengthDistribution();
        if( !readLengthDistribution.isEmpty() ) {
            insertCountDistribution( readLengthDistribution, trackID );
            statsContainer.addStatsValue( StatsContainer.AVERAGE_READ_LENGTH, readLengthDistribution.getAverageValue() );
        }
        DiscreteCountingDistribution readPairLengthDistribution = statsContainer.getReadPairSizeDistribution();
        if( !readPairLengthDistribution.isEmpty() ) {
            insertCountDistribution( readPairLengthDistribution, trackID );
            statsContainer.addStatsValue( StatsContainer.AVERAGE_READ_PAIR_SIZE, readPairLengthDistribution.getAverageValue() );
        }

        try( Connection con = connectionPool.getConnection() ) {
            con.setAutoCommit( false );
            // get latest id for statistic
            long latestID = GenericSQLQueries.getLatestIDFromDB( SQLStatements.GET_LATEST_STATISTICS_ID, con );
            try( PreparedStatement pStmtInsertStats = con.prepareStatement( SQLStatements.INSERT_STATISTICS ) ) {

                int batchCounter = 0;
                pStmtInsertStats.setInt( 2, trackID );
                for( Map.Entry<String, Integer> entry : statsContainer.getStatsMap().entrySet() ) {
                    batchCounter++;
                    pStmtInsertStats.setLong( 1, latestID++ );
                    pStmtInsertStats.setString( 3, entry.getKey() );
                    pStmtInsertStats.setInt( 4, entry.getValue() );
                    pStmtInsertStats.addBatch();
                    if( batchCounter == FEATURE_BATCH_SIZE ) {
                        batchCounter = 0;
                        pStmtInsertStats.executeBatch();
                    }
                }
                con.commit();
                LOG.info( "...done storing track statistics data" );

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "storing track statistics failed! rollback failed!", exRb );
                    throw new DatabaseException( "Storing track statistics failed failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "storing track statistics failed! rolledbacked!", ex );
                throw new DatabaseException( "Storing track statistics failed failed!", ex.getMessage(), ex );
            }
            con.setAutoCommit( true );
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Stores the statistics for a track in the db.
     * <p>
     * @param keys    the list of statistics key whose values shall be deleted
     *                for the given track
     * @param trackId the track id whose data shall be stored
     */
    public void deleteSpecificTrackStatistics( List<String> keys, int trackId ) throws DatabaseException {

        LOG.info( "Start deleting specific track statistics..." );
        try( Connection con = connectionPool.getConnection() ) {
            con.setAutoCommit( false );
            try( PreparedStatement pStmtDeleteStats = con.prepareStatement( SQLStatements.DELETE_SPECIFIC_TRACK_STATISTIC ) ) {

                pStmtDeleteStats.setLong( 1, trackId );
                for( String key : keys ) {
                    pStmtDeleteStats.setString( 2, key );
                    pStmtDeleteStats.execute();
                }
                con.commit();
                LOG.info( "...done deleting specific track statistics." );

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "deleting specific track statistics failed! rollback failed!", exRb );
                    throw new DatabaseException( "Deleting specific track statistics failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "deleting specific track statistics failed! rolledbacked!", ex );
                throw new DatabaseException( "Deleting specific track statistics failed!", ex.getMessage(), ex );
            }
            con.setAutoCommit( true );
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Sets a count distribution {@link DiscreteCountingDistribution} for this
     * track.
     * <p>
     * @param distribution the count distribution to store
     * @param trackId      track id of this track
     */
    public void insertCountDistribution( DiscreteCountingDistribution distribution, final int trackId ) throws DatabaseException {

        int[] countDistribution = distribution.getDiscreteCountingDistribution();
        try( Connection con = connectionPool.getConnection() ) {
            con.setAutoCommit( false );
            try( PreparedStatement pStmtInsert = con.prepareStatement( SQLStatements.INSERT_COUNT_DISTRIBUTION ) ) {

                pStmtInsert.setInt( 1, trackId );
                pStmtInsert.setByte( 2, (byte) distribution.getType().getType() );
                for( int i = 0; i < countDistribution.length; ++i ) {
                    pStmtInsert.setInt( 3, i );
                    pStmtInsert.setInt( 4, countDistribution[i] );
                    pStmtInsert.addBatch();
                }
                pStmtInsert.executeBatch();
                con.commit();

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "inserting count distribution failed! rollback failed!", exRb );
                    throw new DatabaseException( "Inserting count distribution failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "inserting count distribution failed! rolledbacked!", ex );
                throw new DatabaseException( "Inserting count distribution failed!", ex.getMessage(), ex );
            }
            con.setAutoCommit( true );
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Sets the read pair id for both tracks belonging to one read pair.
     * <p>
     * @param track1Id track id of first track of the pair
     * @param track2Id track id of second track of the pair
     */
    public void setReadPairIdsForTrackIds( long track1Id, long track2Id ) throws DatabaseException {

        try( Connection con = connectionPool.getConnection() ) {
            con.setAutoCommit( false );
            try {
                //not 0, because 0 is the value when a track is not a sequence pair track!
                int readPairId = (int) GenericSQLQueries.getLatestIDFromDB( SQLStatements.GET_LATEST_TRACK_READ_PAIR_ID, con );
                try( PreparedStatement pStmtSetReadPairIds = con.prepareStatement( SQLStatements.INSERT_TRACK_READ_PAIR_ID ) ) {
                    pStmtSetReadPairIds.setInt( 1, readPairId );
                    pStmtSetReadPairIds.setLong( 2, track1Id );
                    pStmtSetReadPairIds.execute();

                    pStmtSetReadPairIds.setInt( 1, readPairId );
                    pStmtSetReadPairIds.setLong( 2, track2Id );
                    pStmtSetReadPairIds.execute();
                    con.commit();
                }

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "setting read pair ids failed! rollback failed!", exRb );
                    throw new DatabaseException( "Setting read pair ids failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "setting read pair ids failed! rolledbacked!", ex );
                throw new DatabaseException( "Setting read pair ids failed!", ex.getMessage(), ex );
            }
            con.setAutoCommit( true );
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * @param refId the reference id
     * <p>
     * @return The reference genome connector for the given reference id
     */
    public ReferenceConnector getRefGenomeConnector( int refId ) {

        // only return new object, if no suitable connector was created before
        if( !refConnectors.containsKey( refId ) ) {
            refConnectors.put( refId, new ReferenceConnector( refId ) );
        }

        return refConnectors.get( refId );

    }


    public TrackConnector getTrackConnector( PersistentTrack track ) throws FileNotFoundException {

        // only return new object, if no suitable connector was created before
        int trackId = track.getId();
        if( !trackConnectors.containsKey( trackId ) ) {
            trackConnectors.put( trackId, new TrackConnector( track ) );
        }

        return trackConnectors.get( trackId );

    }


    public TrackConnector getTrackConnector( List<PersistentTrack> tracks, boolean combineTracks ) throws FileNotFoundException {

        // makes sure the track id is not already used
        int id = 9999;
        for( PersistentTrack track : tracks ) {
            id += track.getId();
        }
        // only return new object, if no suitable connector was created before
        trackConnectors.put( id, new TrackConnector( id, tracks, combineTracks ) );

        return trackConnectors.get( id );

    }


    public MultiTrackConnector getMultiTrackConnector( PersistentTrack track ) throws FileNotFoundException {

        // only return new object, if no suitable connector was created before
        int trackID = track.getId();
        if( !multiTrackConnectors.containsKey( trackID ) ) { //old solution, which does not work anymore
            multiTrackConnectors.put( trackID, new MultiTrackConnector( track ) );
        }

        return multiTrackConnectors.get( trackID );

    }


    public MultiTrackConnector getMultiTrackConnector( List<PersistentTrack> tracks ) throws FileNotFoundException {

        // makes sure the track id is not already used
        int id = 9999;
        for( PersistentTrack track : tracks ) {
            id += track.getId();
        }
        // only return new object, if no suitable connector was created before
        multiTrackConnectors.put( id, new MultiTrackConnector( tracks ) );

        return multiTrackConnectors.get( id );

    }


    /**
     * Removes the track connector for the given trackId.
     * <p>
     * @param trackId track id of the track connector to remove
     */
    public void removeTrackConnector( int trackId ) {
        if( trackConnectors.containsKey( trackId ) ) {
            trackConnectors.remove( trackId );
        }
    }


    /**
     * Removes the multi track connector for the given trackId.
     * <p>
     * @param trackId track id of the multi track connector to remove
     */
    public void removeMultiTrackConnector( int trackId ) {
        if( multiTrackConnectors.containsKey( trackId ) ) {
            multiTrackConnectors.remove( trackId );
        }
    }


    /**
     * @return All references stored in the db with their associated data. All
     *         references are re-queried from the DB and returned in new,
     *         independent objects each time the method is called.
     * <p>
     * @throws OutOfMemoryError
     */
    public List<PersistentReference> getReferences() throws DatabaseException {

        LOG.info( "Reading reference genome data from database" );
        try( Connection con = connectionPool.getConnection() ) {
            try( final Statement stmtFetchRefs = con.createStatement();
                 final ResultSet rs = stmtFetchRefs.executeQuery( SQLStatements.FETCH_GENOMES ) ) {

                List<PersistentReference> refGens = new ArrayList<>();
                while( rs.next() ) {
                    int     id = rs.getInt( FieldNames.REF_GEN_ID );
                    String  description = rs.getString( FieldNames.REF_GEN_DESCRIPTION );
                    String  name = rs.getString( FieldNames.REF_GEN_NAME );
                    String  fileName = rs.getString( FieldNames.REF_GEN_FASTA_FILE ); //special handling for backwards compatibility with old DBs
                    fileName = fileName == null ? "" : fileName;
                    Timestamp timestamp = rs.getTimestamp( FieldNames.REF_GEN_TIMESTAMP );
                    File fastaFile = new File( fileName );
                    refGens.add( new PersistentReference( id, name, description, timestamp, fastaFile ) );
                }
                return refGens;

            } catch( SQLException ex ) {
                LOG.log( SEVERE, "reading reference genome data failed! rolledbacked!", ex );
                throw new DatabaseException( "Reading reference genome data failed!", ex.getMessage(), ex );
            }
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Resets the fasta file path of a direct access reference.
     * <p>
     * @param fastaFile fasta file to reset for the current reference.
     * @param ref       The reference genome, whose file shall be updated
     * <p>
     * @throws DatabaseException
     */
    public void resetReferencePath( final File fastaFile, final PersistentReference ref ) throws DatabaseException {

        LOG.info( "Preparing statements for storing track data" );
        try( Connection con = connectionPool.getConnection() ) {
            try( PreparedStatement resetRefPath = con.prepareStatement( SQLStatements.RESET_REF_PATH ) ) {

                resetRefPath.setString( 1, fastaFile.getAbsolutePath() );
                resetRefPath.setLong( 2, ref.getId() );
                resetRefPath.execute();
                ref.resetFastaPath( fastaFile );
                LOG.log( INFO, "Reference file for \"{0}\" has been updated successfully", ref.getName() );

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "setting reference path failed! rollback failed!", exRb );
                    throw new DatabaseException( "Setting reference path failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "setting reference path failed! rolledbacked!", ex );
                throw new DatabaseException( "Setting reference path failed!", ex.getMessage(), ex );
            }
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Deletes all data associated with the given reference id.
     * <p>
     * @param refId the reference id whose data shall be delete from the DB
     * <p>
     * @throws DatabaseException
     */
    public void deleteReference( final int refId ) throws DatabaseException {

        LOG.log( INFO, "Starting deletion of reference with id \"{0}\"", refId );
        ReferenceConnector refCon = getRefGenomeConnector( refId );
        try( Connection con = connectionPool.getConnection() ) {
            con.setAutoCommit( false );
            try( PreparedStatement pStmtDeleteFeatures = con.prepareStatement( SQLStatements.DELETE_FEATURES_FROM_CHROMOSOME );
                 PreparedStatement pStmtDeleteChrom = con.prepareStatement( SQLStatements.DELETE_CHROMOSOME );
                 PreparedStatement pStmtDeleteGenome = con.prepareStatement( SQLStatements.DELETE_GENOME ); ) {

                for( PersistentChromosome chrom : refCon.getChromosomesForGenome().values() ) {
                    LOG.log( INFO, "Deleting features for chromosome {0}...", chrom.getName() );
                    pStmtDeleteFeatures.setLong( 1, chrom.getId() );
                    pStmtDeleteFeatures.execute();
                    LOG.log( INFO, "Deleting chromosome {0}...", chrom.getName() );
                    pStmtDeleteChrom.setInt( 1, chrom.getId() );
                    pStmtDeleteChrom.execute();
                }

                LOG.log( INFO, "Deleting Genome {0}...", refCon.getRefGenome().getName() );
                pStmtDeleteGenome.setLong( 1, refId );
                pStmtDeleteGenome.execute();

                con.commit();
                refConnectors.remove( refId );

                // notify observers about the change of the database
                notifyObserversAbout( "deleteGenomes" );
                LOG.log( INFO, "Finished deletion of reference genome with id \"{0}\"", refId );

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "deleting reference failed! rollback failed!", exRb );
                    throw new DatabaseException( "Deleting reference failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "deleting reference failed! rolledbacked!", ex );
                throw new DatabaseException( "Deleting reference failed!", ex.getMessage(), ex );
            }
            con.setAutoCommit( true );
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * @return A map of all tracks in the connected DB mapped on their
     *         respective reference.
     */
    public Map<PersistentReference, List<PersistentTrack>> getReferencesAndTracks() throws DatabaseException {

        final List<PersistentReference> genomes = getReferences();
        final List<PersistentTrack> tracks = getTracks();
        final Map<Integer, List<PersistentTrack>> tracksByReferenceId = new HashMap<>( tracks.size() );
        for( PersistentTrack pt : tracks ) {
            List<PersistentTrack> list = tracksByReferenceId.get( pt.getRefGenID() );
            if( list == null ) {
                list = new ArrayList<>();
                tracksByReferenceId.put( pt.getRefGenID(), list );
            }
            list.add( pt );
        }

        Map<PersistentReference, List<PersistentTrack>> tracksByReference = new HashMap<>( genomes.size() );
        for( PersistentReference reference : genomes ) {
            List<PersistentTrack> currentTrackList = tracksByReferenceId.get( reference.getId() );
            //if the current reference genome does not have any tracks,
            //just create an empty list
            if( currentTrackList == null ) {
                currentTrackList = new ArrayList<>();
            }
            tracksByReference.put( reference, currentTrackList );
        }

        return tracksByReference;

    }


    /**
     * Calculates and returns the names of all currently opened tracks hashed to
     * their track id.
     * <p>
     * @return the names of all currently opened tracks hashed to their track
     *         id.
     */
    public Map<Integer, String> getOpenedTrackNames() {

        Map<Integer, String> namesList = new HashMap<>( trackConnectors.size() );
        for( int nextId : trackConnectors.keySet() ) {
            namesList.put( nextId, trackConnectors.get( nextId ).getAssociatedTrackName() );
        }
        return namesList;

    }


    /**
     * @return All tracks stored in the database with all their information. All
     *         tracks are re-queried from the DB and returned in new,
     *         independent objects each time the method is called.
     */
    public List<PersistentTrack> getTracks() throws DatabaseException {

        LOG.info( "Reading track data from database" );
        try( Connection con = connectionPool.getConnection() ) {
            try( Statement stmtFetch = con.createStatement();
                 ResultSet rs = stmtFetch.executeQuery( SQLStatements.FETCH_TRACKS ); ) {

                List<PersistentTrack> tracks = new ArrayList<>();
                while( rs.next() ) {
                    int id         = rs.getInt( FieldNames.TRACK_ID );
                    int refGenID   = rs.getInt( FieldNames.TRACK_REFERENCE_ID );
                    int readPairId = rs.getInt( FieldNames.TRACK_READ_PAIR_ID );
                    String filePath    = rs.getString( FieldNames.TRACK_PATH );
                    String description = rs.getString( FieldNames.TRACK_DESCRIPTION );
                    Timestamp date = rs.getTimestamp( FieldNames.TRACK_TIMESTAMP );
                    tracks.add( new PersistentTrack( id, filePath, description, date, refGenID, -1, readPairId ) );
                }
                return tracks;

            } catch( SQLException ex ) {
                LOG.log( SEVERE, "reading tracks data failed! rolledbacked!", ex );
                throw new DatabaseException( "Reading tracks data failed!", ex.getMessage(), ex );
            }
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * @param trackID
     * <p>
     * @return The track for the given track id in a fresh track object
     */
    public PersistentTrack getTrack( final int trackID ) throws DatabaseException {

        LOG.info( "Reading track data from database" );
        try( Connection con = connectionPool.getConnection() ) {
            try( final PreparedStatement pStmtFetchTracks = con.prepareStatement( SQLStatements.FETCH_TRACK ); ) {
                pStmtFetchTracks.setInt( 1, trackID );
                try( final ResultSet rs = pStmtFetchTracks.executeQuery(); ) {

                    if( rs.getFetchSize() == 1 ) {
                        rs.next();
                        int id         = rs.getInt( FieldNames.TRACK_ID );
                        int refId      = rs.getInt( FieldNames.TRACK_REFERENCE_ID );
                        int readPairId = rs.getInt( FieldNames.TRACK_READ_PAIR_ID );
                        String filePath    = rs.getString( FieldNames.TRACK_PATH );
                        String description = rs.getString( FieldNames.TRACK_DESCRIPTION );
                        Timestamp date = rs.getTimestamp( FieldNames.TRACK_TIMESTAMP );
                        return new PersistentTrack( id, filePath, description, date, refId, readPairId );
                    } else {
                        return null;
                    }

                }

            } catch( SQLException ex ) {
                LOG.log( SEVERE, "reading tracks data failed! rolledbacked!", ex );
                throw new DatabaseException( "Reading tracks data failed!", ex.getMessage(), ex );
            }
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * @return the latest track id used in the database + 1 = the next id to
     *         use.
     */
    public int getLatestTrackId() throws DatabaseException {

        try( Connection con = connectionPool.getConnection() ) {
            return (int) GenericSQLQueries.getLatestIDFromDB( SQLStatements.GET_LATEST_TRACK_ID, con );
        } catch( Exception ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Resets the file path of a direct access reference.
     * <p>
     * @param track track whose file path has to be resetted.
     * <p>
     * @throws DatabaseException
     */
    public void resetTrackPath( final PersistentTrack track ) throws DatabaseException {

        LOG.info( "Preparing statements for storing track data" );
        try( Connection con = connectionPool.getConnection() ) {
            try( PreparedStatement resetTrackPath = con.prepareStatement( SQLStatements.RESET_TRACK_PATH ) ) {

                resetTrackPath.setString( 1, track.getFilePath() );
                resetTrackPath.setLong( 2, track.getId() );
                resetTrackPath.execute();
                LOG.log( INFO, "Track \"{0}\" has been updated successfully", track.getDescription() );

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "setting track path failed! rollback failed!", exRb );
                    throw new DatabaseException( "Setting track path failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "setting track path failed! rolledbacked!", ex );
                throw new DatabaseException( "Setting track path failed!", ex.getMessage(), ex );
            }
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    /**
     * Deletes all data associated with the given track id.
     * <p>
     * @param trackId the track id whose data shall be delete from the DB
     * <p>
     * @throws DatabaseException
     */
    public void deleteTrack( final int trackId ) throws DatabaseException {

        LOG.log( INFO, "Starting deletion of track with id \"{0}\"", trackId );
        try( Connection con = connectionPool.getConnection() ) {
            con.setAutoCommit( false );
            try( PreparedStatement pStmtDeleteStats = con.prepareStatement( SQLStatements.DELETE_STATISTIC_FROM_TRACK );
                 PreparedStatement pStmtDeleteCountDistributions = con.prepareStatement( SQLStatements.DELETE_COUNT_DISTRIBUTIONS_FROM_TRACK );
                 PreparedStatement pStmtDeleteTrack = con.prepareStatement( SQLStatements.DELETE_TRACK ); ) {

                LOG.info( "Deleting Statistics..." );
                pStmtDeleteStats.setInt( 1, trackId );
                pStmtDeleteStats.execute();

                LOG.info( "Deleting Count Distributions..." );
                pStmtDeleteCountDistributions.setInt( 1, trackId );
                pStmtDeleteCountDistributions.execute();

                LOG.info( "Deleting Track..." );
                pStmtDeleteTrack.setInt( 1, trackId );
                pStmtDeleteTrack.execute();

                con.commit();

                trackConnectors.remove( trackId );
                // notify observers about the change of the database
                notifyObserversAbout( "deleteTrack" );
                LOG.log( INFO, "Finished deletion of track \"{0}\"", trackId );

            } catch( SQLException ex ) {
                try {
                    con.rollback();
                } catch( Exception exRb ) {
                    LOG.log( SEVERE, "deleting track failed! rollback failed!", exRb );
                    throw new DatabaseException( "Deleting track failed! No rolledback! Database could be compromised!", exRb.getMessage(), exRb );
                }
                LOG.log( SEVERE, "deleting track failed! rolledbacked!", ex );
                throw new DatabaseException( "Deleting track failed!", ex.getMessage(), ex );
            }
            con.setAutoCommit( true );
        } catch( SQLException ex ) {
            LOG.log( SEVERE, "no connection to db!", ex );
            throw new DatabaseException( "Couldn't connect to database!", "db connection error!", ex );
        }

    }


    private void notifyObserversAbout( final String message ) {
        setChanged();
        notifyObservers( message );
    }


    /**
     * @param tracks The list of tracks to convert to a map
     * <p>
     * @return Converts the given track list into a map of tracks to their track
     *         id.
     */
    public static Map<Integer, PersistentTrack> getTrackMap( final List<PersistentTrack> tracks ) {

        Map<Integer, PersistentTrack> trackMap = new HashMap<>( tracks.size() );
        for( PersistentTrack track : tracks ) {
            trackMap.put( track.getId(), track );
        }
        return trackMap;

    }




    /*
     * Legacy Code
     *
     * Just for temporary archiving and debugging
     */


    /**
     * Checks if a rollback is needed or if the SQLException originated from a
     * duplicate column name error.
     * <p>
     * @param ex SQL exception to check
     */
//    private void checkRollback( SQLException ex ) throws SQLException {
//        if( !ex.getMessage().contains( "Duplicate column name" ) ) {
//            rollbackOnError( ex );
//        }
//    }

}
