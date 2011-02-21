package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.FieldNames;
import de.cebitec.vamp.databackend.H2SQLStatements;
import de.cebitec.vamp.databackend.SQLStatements;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.dataObjects.PersistentRun;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedFeature;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedReadname;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsedTrack;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier
 */
public class ProjectConnector {

    private static ProjectConnector dbConnector;
    private Connection con;
    private String url;
    private String user;
    private String password;
    private String adapter;
    private HashMap<Long, TrackConnector> trackConnectors;
    private HashMap<Long, RunConnector> runConnectors;
    private HashMap<Long, ReferenceConnector> refConnectors;
    private final static int SEQUENCE_BATCH_SIZE = 100000;
    private final static int READNAME_BATCH_SIZE = 100000;
    private final static int FEATURE_BATCH_SIZE = 100000;
    private final static int COVERAGE_BATCH_SIZE = 100000;
    private final static int MAPPING_BATCH_SIZE = 100000;
    private final static int DIFF_BATCH_SIZE = 100000;
    private int coveragePerf = 0;
    private int coverageBM = 0;
     private int coverageComplete = 0;

    private ProjectConnector() {
        trackConnectors = new HashMap<Long, TrackConnector>();
        runConnectors = new HashMap<Long, RunConnector>();
        refConnectors = new HashMap<Long, ReferenceConnector>();
    }

    private void cleanUp() {
        trackConnectors.clear();
        runConnectors.clear();
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
                Long id = rs.getLong(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                Long refGenID = rs.getLong(FieldNames.TRACK_REFGEN);
                Long runID = rs.getLong(FieldNames.TRACK_RUN);
                tracks.add(new PersistantTrack(id, description, date, refGenID, runID));
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

    public void connect(String adapter, String hostname, String database, String user, String password) throws SQLException {
        if (adapter.equalsIgnoreCase("mysql")) {
            this.adapter = adapter;
            this.url = "jdbc:" + adapter + "://" + hostname + "/" + database;
            this.user = user;
            this.password = password;
            this.connect(url, user, password);
            this.setupDatabase();
        } else {
            this.adapter = adapter;
            this.url = "jdbc:" + adapter + ":" + database+";FILE_LOCK=SERIALIZED;MULTI_THREADED=1";

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

    private void connectH2DataBase(String url) throws SQLException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Connecting to database");
        con = DriverManager.getConnection(url);
        con.setAutoCommit(true);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Successfully connected to database");
    }

    private void setupDatabaseH2() {

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Setting up tables and indices if not existant");

            con.setAutoCommit(false);
            //create tables if not exist yet
            con.prepareStatement(H2SQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            con.prepareStatement(H2SQLStatements.SETUP_DIFFS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_DIFF).executeUpdate();
            con.prepareStatement(H2SQLStatements.SETUP_COVERAGE).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_COVERAGE).executeUpdate();
            con.prepareStatement(H2SQLStatements.SETUP_FEATURES).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_FEATURES).executeUpdate();
            con.prepareStatement(H2SQLStatements.SETUP_MAPPINGS).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_MAPPINGS).executeUpdate();
            con.prepareStatement(H2SQLStatements.SETUP_TRACKS).execute();
            con.prepareStatement(H2SQLStatements.INDEX_TRACKS).executeUpdate();
            con.prepareStatement(H2SQLStatements.SETUP_STATICS).executeUpdate();
            try{
            con.prepareStatement(H2SQLStatements.ADD_COLUMN_TO_TABLE_STATICS_NUMBER_OF_READS).execute();
            con.prepareStatement(H2SQLStatements.ADD_COLUMN_TO_TABLE_STATICS_NUMBER_OF_UNIQUE_SEQ).execute();
            }catch(Exception ex){
                Logger.getLogger(this.getClass().getName()).log(Level.INFO,"Columns already exist");
            }
            con.prepareStatement(H2SQLStatements.SETUP_RUN).execute();
            con.prepareStatement(H2SQLStatements.SETUP_SEQUENCE).executeUpdate();
            con.prepareStatement(H2SQLStatements.INDEX_SEQUENCE).executeUpdate();
            con.prepareStatement(H2SQLStatements.SETUP_READS).execute();
            con.prepareStatement(H2SQLStatements.INDEX_READS).executeUpdate();


            con.commit();
            con.setAutoCommit(true);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished creating tables and indices if not existant before");

        } catch (SQLException ex) {
            this.rollbackOnError(this.getClass().getName(), ex);
        }
    }

    private void setupDatabase() {

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Setting up tables and indices if not existant");

            con.setAutoCommit(false);
            //create tables if not exist yet
            con.prepareStatement(SQLStatements.SETUP_REFERENCE_GENOME).executeUpdate();
            con.prepareStatement(SQLStatements.SETUP_DIFFS).executeUpdate();
            con.prepareStatement(SQLStatements.SETUP_COVERAGE).executeUpdate();
            con.prepareStatement(SQLStatements.SETUP_FEATURES).executeUpdate();
            con.prepareStatement(SQLStatements.SETUP_MAPPINGS).executeUpdate();
            con.prepareStatement(SQLStatements.SETUP_TRACKS).execute();
            con.prepareStatement(SQLStatements.SETUP_RUN).execute();
            con.prepareStatement(SQLStatements.SETUP_STATICS).execute();
            con.prepareStatement(SQLStatements.SETUP_SEQUENCE).executeUpdate();
            con.prepareStatement(SQLStatements.SETUP_READS).execute();


            con.commit();
            con.setAutoCommit(true);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished creating tables and indices if not existant before");

        } catch (SQLException ex) {
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

    private void lockRunDomainTables() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start locking run domain tables");
        try {
            Statement lock = con.createStatement();
            lock.execute(SQLStatements.LOCK_TABLE_RUN_DOMAIN);
            con.commit();
            lock.close();
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done locking run domain tables");
    }

    private void unlockTables() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start unlocking tables");
        try {
            con.setAutoCommit(false);
            Statement unlock = con.createStatement();
            unlock.execute(SQLStatements.UNLOCK_TABLES);
            unlock.close();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done unlocking tables");
    }

    private void storeRun(ParsedRun run) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing run data");

        try {
            PreparedStatement insertRun = con.prepareStatement(SQLStatements.INSERT_RUN);
            PreparedStatement fetchRunID = con.prepareStatement(SQLStatements.GET_LATEST_RUN_ID);

            long runID = 0;
            ResultSet rs = fetchRunID.executeQuery();
            if (rs.next()) {
                runID = rs.getLong("LATEST_ID");
            }

            runID++;
            run.setID(runID);

            insertRun.setLong(1, runID);
            insertRun.setString(2, run.getDescription());
            insertRun.setTimestamp(3, run.getTimestamp());
            insertRun.execute();

            insertRun.close();
            fetchRunID.close();

            con.commit();
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done storing run data");
    }

    private void storeRunH2(ParsedRun run) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing run data");

        try {
            PreparedStatement insertRun = con.prepareStatement(H2SQLStatements.INSERT_RUN);
            PreparedStatement fetchRunID = con.prepareStatement(H2SQLStatements.GET_LATEST_RUN_ID);

            long runID = 0;
            ResultSet rs = fetchRunID.executeQuery();
            if (rs.next()) {
                runID = rs.getLong("LATEST_ID");
            }

            runID++;
            run.setID(runID);

            insertRun.setLong(1, runID);
            insertRun.setString(2, run.getDescription());
            insertRun.setTimestamp(3, run.getTimestamp());
            insertRun.execute();

            insertRun.close();
            fetchRunID.close();

            con.commit();
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done storing run data");
    }

    private void storeSequences(ParsedRun run) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing unique sequences");

        try {
            PreparedStatement insertSequence = con.prepareStatement(SQLStatements.INSERT_SEQUENCE);
            PreparedStatement latestSeqID = con.prepareStatement(SQLStatements.GET_LATEST_SEQUENCE_ID);

            long seqID = 0;
            ResultSet rs = latestSeqID.executeQuery();
            if (rs.next()) {
                seqID = rs.getLong("LATEST_ID");
            }

            // store sequences
            Collection<ParsedReadname> reads = run.getReads();
            int batchCounter = 1;
            Iterator<ParsedReadname> it = reads.iterator();
            while( it.hasNext()) {
                batchCounter++;
                ParsedReadname read = it.next();
                seqID++;
                read.setID(seqID);
                insertSequence.setLong(1, seqID);
                insertSequence.setLong(2, run.getID());
                insertSequence.addBatch();

                if (batchCounter == SEQUENCE_BATCH_SIZE) {
                    insertSequence.executeBatch();
                   batchCounter = 1;
                }
              //  it.remove();
            }

            insertSequence.executeBatch();

            con.commit();
            insertSequence.close();
            latestSeqID.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done storing unique sequences");
    }

    private void storeReads(ParsedRun run) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing readnames");
        try {
            PreparedStatement insertRead = con.prepareStatement(SQLStatements.INSERT_READ);
            PreparedStatement getLatestID = con.prepareStatement(SQLStatements.GET_LATEST_READ_ID);

            ResultSet rs = getLatestID.executeQuery();
            long readID = 0;
            if (rs.next()) {
                readID = rs.getLong("LATEST_ID");
            }
            
            int batchCounter = 1;
            Iterator<ParsedReadname> readMapIt = run.getReads().iterator();
            while (readMapIt.hasNext()) {
                ParsedReadname readMap = readMapIt.next();
                Iterator<String> readIt = readMap.getReads().iterator();
                while( readIt.hasNext()) {
                    batchCounter++;
                    readID++;
                    insertRead.setLong(1, readID);
                    insertRead.setString(2, readIt.next());
                    insertRead.setLong(3, readMap.getID());
                    insertRead.addBatch();

                    if (batchCounter == READNAME_BATCH_SIZE) {
                          batchCounter = 1;
                        insertRead.executeBatch();
                    }
                   // readIt.remove();
                }
              //  readMapIt.remove();
            }

            insertRead.executeBatch();
            con.commit();
            insertRead.close();
            getLatestID.close();

        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "done storing readnames");
    }

    public long addRun(ParsedRun run) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing run \"{0}to {1}", new Object[]{run.getDescription(), adapter});

        if (adapter.equalsIgnoreCase("mysql")) {
            try {
                con.setAutoCommit(false);
                this.lockRunDomainTables();
                this.disableRunIndices();
                storeRun(run);
                storeSequences(run);
                storeReads(run);
                run.deleteMap();
                this.enableRunIndices();
                this.unlockTables();
                con.setAutoCommit(true);

            } catch (SQLException ex) {
                ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
                throw new StorageException(ex);
            } catch (Exception ex) {
                throw new StorageException(ex);
            }
        } else {
            try {
                con.setAutoCommit(false);
                storeRunH2(run);
                storeSequences(run);
                storeReads(run);
                run.deleteMap();
                con.setAutoCommit(true);

            } catch (SQLException ex) {
                ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
                throw new StorageException(ex);
            } catch (Exception ex) {
                throw new StorageException(ex);
            }

        }

        return run.getID();
    }

    private void disableRunIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start disabling run domain indexing");
        try {

            PreparedStatement run = con.prepareStatement(SQLStatements.DISABLE_RUN_INDICES);
            PreparedStatement sequence = con.prepareStatement(SQLStatements.DISABLE_SEQUENCE_INDICES);
            PreparedStatement read = con.prepareStatement(SQLStatements.DISABLE_READNAMES_INDICES);

            run.execute();
            sequence.execute();
            read.execute();

            con.commit();

            run.close();
            sequence.close();
            read.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "disabled run data domain indexing");
    }

    private void enableRunIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start enabling run data domain indexing");
        try {

            PreparedStatement run = con.prepareStatement(SQLStatements.ENABLE_RUN_INDICES);
            PreparedStatement sequence = con.prepareStatement(SQLStatements.ENABLE_SEQUENCE_INDICES);
            PreparedStatement read = con.prepareStatement(SQLStatements.ENABLE_READNAMES_INDICES);

            run.execute();
            sequence.execute();
            read.execute();

            con.commit();

            run.close();
            sequence.close();
            read.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "enabled run data domain indexing");
    }

    private void disableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start disabling reference data domain indexing...");
        try {
            PreparedStatement disableSeq = con.prepareStatement(SQLStatements.DISABLE_REFERENCE_INDICES);
            PreparedStatement disableFeatures = con.prepareStatement(SQLStatements.DISABLE_FEATURE_INDICES);

            disableSeq.execute();
            disableFeatures.execute();

            con.commit();
            disableSeq.close();
            disableFeatures.close();
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done disabling reference data domain indexing");
    }

    private void enableReferenceIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start enabling reference data domain indexing...");
        try {
            PreparedStatement enableSeq = con.prepareStatement(SQLStatements.ENABLE_REFERENCE_INDICES);
            PreparedStatement enableFeature = con.prepareStatement(SQLStatements.ENABLE_FEATURE_INDICES);

            enableSeq.execute();
            enableFeature.execute();
            con.commit();
            enableSeq.close();
            enableFeature.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done enabling reference data domain indexing");
    }

    private void storeGenome(ParsedReference reference) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing reference sequence data...");
        try {
            PreparedStatement getLatestID = con.prepareStatement(SQLStatements.GET_LATEST_REFERENCE_ID);
            PreparedStatement insertGenome = con.prepareStatement(SQLStatements.INSERT_REFGENOME);

            // get the latest id assigned for a reference
            ResultSet rs = getLatestID.executeQuery();
            long id = 0;
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;
            reference.setID(id);

            // store reference data
            insertGenome.setLong(1, reference.getID());
            insertGenome.setString(2, reference.getName());
            insertGenome.setString(3, reference.getDescription());
            insertGenome.setString(4, reference.getSequence());
            insertGenome.setTimestamp(5, reference.getTimestamp());
            insertGenome.execute();

            con.commit();

            insertGenome.close();
            getLatestID.close();

        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }



        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting reference sequence data");
    }

    private void storeFeatures(ParsedReference reference) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start inserting features...");
        try {
            PreparedStatement latestId = con.prepareStatement(SQLStatements.GET_LATEST_FEATURE_ID);
            PreparedStatement insertFeature = con.prepareStatement(SQLStatements.INSERT_FEATURE);

            long id = 0;
            ResultSet rs = latestId.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;

            int batchCounter = 1;
            Iterator<ParsedFeature> it = reference.getFeatures().iterator();
            while (it.hasNext()) {
                batchCounter++;
                ParsedFeature f = it.next();
                insertFeature.setLong(1, id);
                insertFeature.setLong(2, reference.getID());
                insertFeature.setInt(3, f.getType());
                insertFeature.setInt(4, f.getStart());
                insertFeature.setInt(5, f.getStop());
                insertFeature.setString(6, f.getLocusTag());
                insertFeature.setString(7, f.getProduct());
                insertFeature.setString(8, f.getEcNumber());
                insertFeature.setInt(9, f.getStrand());
                insertFeature.addBatch();
                id++;
                if (batchCounter == FEATURE_BATCH_SIZE) {
                    batchCounter = 1;
                    insertFeature.executeBatch();
                }
              //  it.remove();
            }

            insertFeature.executeBatch();
            insertFeature.close();


        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting features");
    }

    private void lockReferenceDomainIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start locking reference domain tables...");
        try {
            PreparedStatement lock = con.prepareStatement(SQLStatements.LOCK_TABLE_REFERENCE_DOMAIN);
            lock.execute();
            con.commit();
            lock.close();
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done locking reference domain tables");
    }

    public long addRefGenome(ParsedReference reference) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing reference sequence  \"{0}\"", reference.getName());

        if (adapter.equalsIgnoreCase("mysql")) {

            try {
                con.setAutoCommit(false);
                this.lockReferenceDomainIndices();
                this.disableReferenceIndices();

                this.storeGenome(reference);
                this.storeFeatures(reference);

                this.enableReferenceIndices();
                this.unlockTables();
                con.setAutoCommit(true);
            } catch (SQLException ex) {
                ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
            }
        } else {
            try {

                con.setAutoCommit(false);
                this.storeGenome(reference);
                this.storeFeatures(reference);
                con.setAutoCommit(true);
            } catch (SQLException ex) {
                ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
            }
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished storing reference sequence \"{0}\"", reference.getName());
        return reference.getID();
    }

    private void storeCoverage(ParsedTrack track) {
         coveragePerf = 0 ;
         coverageBM = 0;
         coverageComplete=0;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing coverage information...");
        try {
            PreparedStatement insertCoverage = con.prepareStatement(SQLStatements.INSERT_COVERAGE);
            PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_COVERAGE_ID);

            // get the latest used coverage id
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;

            // insert coverage for track
            int batchCounter = 1;
            CoverageContainer cov = track.getCoverageContainer();
            Iterator<Integer> covsIt = cov.getCoveredPositions().iterator();
            while (covsIt.hasNext()) {
                id++;
                batchCounter++;
                int pos = covsIt.next();
                if(cov.getZeroErrorMappingsForwardCoverage(pos)+ cov.getZeroErrorMappingsReverseCoverage(pos)!= 0){
                coveragePerf ++;
                }
                if(cov.getBestMappingForwardCoverage(pos)+ cov.getBestMappingReverseCoverage(pos)!= 0){
                  coverageBM ++ ;
                }
                if(cov.getNErrorMappingsReverseCoverage(pos)+ cov.getNErrorMappingsForwardCoverage(pos)!= 0){
                  coverageComplete++;
                }
                insertCoverage.setLong(1, id);
                insertCoverage.setLong(2, track.getID());
                insertCoverage.setInt(3, pos);
                insertCoverage.setInt(4, cov.getBestMappingForwardCoverage(pos));
                insertCoverage.setInt(5, cov.getNumberOfBestMapppingsForward(pos));
                insertCoverage.setInt(6, cov.getBestMappingReverseCoverage(pos));
                insertCoverage.setInt(7, cov.getNumberOfBestMapppingsReverse(pos));
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
            }
           //System.out.println("perf " + coveragePerf + "covBM "+ coverageBM + "covCompl "+ coverageComplete);

            insertCoverage.executeBatch();

            insertCoverage.close();
            latestID.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing coverage information");
    }

    private void storeTrack(ParsedTrack track, long refGenID, long runID) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track data...");
        try {
            PreparedStatement insertTrack = con.prepareStatement(SQLStatements.INSERT_TRACK);
            PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_TRACK_ID);

            // get latest id for track
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;
            track.setID(id);

            // store track in table
            insertTrack.setLong(1, id);
            insertTrack.setLong(2, refGenID);
            insertTrack.setString(3, track.getDescription());
            insertTrack.setTimestamp(4, track.getTimestamp());
            insertTrack.setLong(5, runID);
            insertTrack.execute();

            insertTrack.close();
            latestID.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track data");
    }


    private void storeTrackH2(ParsedTrack track, long refGenID, long runID) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track data...");
        
        try {
            PreparedStatement insertTrack = con.prepareStatement(H2SQLStatements.INSERT_TRACK);
            PreparedStatement latestID = con.prepareStatement(H2SQLStatements.GET_LATEST_TRACK_ID);

            // get latest id for track
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;
            track.setID(id);

            // store track in table
            insertTrack.setLong(1, id);
            insertTrack.setLong(2, refGenID);
            insertTrack.setString(3, track.getDescription());
            insertTrack.setTimestamp(4, track.getTimestamp());
            insertTrack.setLong(5, runID);
            insertTrack.execute();

            insertTrack.close();
            latestID.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track data");
    }

        private void storeStatics(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track data...");
        int mappings = 0;
        int perfectmappings = 0;
        int bmmappings = 0;
        int mappedSeq = 0;
        int noOfReads= 0;
        int noOfUniqueSeq = 0;
        try {
            HashMap<Integer, Integer> mappingInfos = track.getParsedMappingContainer().getMappingInformations();
            mappings = mappingInfos.get(1);
            perfectmappings = mappingInfos.get(2);
            bmmappings = mappingInfos.get(3);
            mappedSeq = mappingInfos.get(4);
            noOfReads =  mappingInfos.get(5);
            noOfUniqueSeq =  mappingInfos.get(6);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "...can't get the list");
        }
        try {
            PreparedStatement insertStatics = con.prepareStatement(H2SQLStatements.INSERT_STATICS);
            PreparedStatement latestID = con.prepareStatement(H2SQLStatements.GET_LATEST_STATICS_ID);

            // get latest id for track
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;

            // store track in table
            insertStatics.setLong(1, id);
            insertStatics.setLong(2, track.getID());
            insertStatics.setInt(3, mappings);
            insertStatics.setInt(4, perfectmappings);
            insertStatics.setInt(5, bmmappings);
            insertStatics.setInt(6, mappedSeq);
            insertStatics.setInt(7,coveragePerf);
            insertStatics.setInt(8,coverageBM);
            insertStatics.setInt(9,coverageComplete);
            insertStatics.setInt(10,noOfReads);
            insertStatics.setInt(11,noOfUniqueSeq);
            insertStatics.execute();

            insertStatics.close();
            latestID.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track data");
    }



    private void storeMappings(ParsedTrack track) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing mapping data...");
        try {
            PreparedStatement insertMapping = con.prepareStatement(SQLStatements.INSERT_MAPPING);
            PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_MAPPING_ID);

            // get the latest assigned mapping id
            long mappingID = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                mappingID = rs.getLong("LATEST_ID");
            }
            mappingID++;

            // start storing the mappings
            int batchCounter = 1;
            Iterator<Integer> sequenceIDIterator = track.getParsedMappingContainer().getMappedSequenceIDs().iterator();
            while (sequenceIDIterator.hasNext()) {
                int sequenceID = sequenceIDIterator.next();
                List<ParsedMapping> c = track.getParsedMappingContainer().getParsedMappingGroupBySeqID(sequenceID).getMappings();
                Iterator<ParsedMapping> mappingsIt = c.iterator();
                while (mappingsIt.hasNext()) {
                    ParsedMapping m = mappingsIt.next();
                    m.setID(mappingID);

                    insertMapping.setLong(1, mappingID);
                    insertMapping.setInt(2, m.getStart());
                    insertMapping.setInt(3, m.getStop());
                    insertMapping.setInt(4, (m.isBestMapping() ? 1 : 0));
                    insertMapping.setInt(5, m.getCount());
                    insertMapping.setByte(6, m.getDirection());
                    insertMapping.setInt(7, m.getErrors());
                    insertMapping.setInt(8, sequenceID);
                    insertMapping.setLong(9, track.getID());

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
            PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_DIFF_ID);
            PreparedStatement insertDiff = con.prepareStatement(SQLStatements.INSERT_DIFF);
            PreparedStatement insertGap = con.prepareStatement(SQLStatements.INSERT_GAP);

            // get the latest diff id used
            long diffID = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                diffID = rs.getLong("LATEST_ID");
            }
            diffID++;

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
                    while(diffsIt.hasNext()) {

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
                    while( gapIt.hasNext()) {
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
            latestID.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done inserting diff data");
    }

    private void lockTrackDomainTables() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start locking track domain tables...");
        try {
            PreparedStatement lock = con.prepareStatement(SQLStatements.LOCK_TABLE_TRACK_DOMAIN);
            lock.execute();
            lock.close();
        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done locking track domain tables");
    }

    public Long addTrack(ParsedTrack track, long runID, long refGenID) throws StorageException {


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Preparing statements for storing track data");
        if (adapter.equalsIgnoreCase("mysql")) {

            this.lockTrackDomainTables();
            this.disableTrackDomainIndices();

            this.storeTrack(track, refGenID, runID);
            this.storeCoverage(track);
            this.storeStatics(track);
            this.storeMappings(track);
            this.storeDiffs(track);

            this.enableTrackDomainIndices();
            this.unlockTables();
        } else {

            this.storeTrackH2(track, refGenID, runID);
            this.storeCoverage(track);
            this.storeStatics(track);
            this.storeMappings(track);
            this.storeDiffs(track);
            track.clear();

        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Track \"{0}\" has been stored successfully", track.getDescription());

        return track.getID();
    }

    private void disableTrackDomainIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started disabling track data domain indices");

        try {

            PreparedStatement disableCoverage = con.prepareStatement(SQLStatements.DISABLE_COVERAGE_INDICES);
            PreparedStatement disableTracks = con.prepareStatement(SQLStatements.DISABLE_TRACK_INDICES);
            PreparedStatement disableMappings = con.prepareStatement(SQLStatements.DISABLE_MAPPING_INDICES);
            PreparedStatement disableDiffs = con.prepareStatement(SQLStatements.DISABLE_DIFF_INDICES);

            disableCoverage.execute();
            disableTracks.execute();
            disableMappings.execute();
            disableDiffs.execute();

            disableCoverage.close();
            disableTracks.close();
            disableMappings.close();
            disableDiffs.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished disabling track data domain indices");
    }

    private void enableTrackDomainIndices() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "started enabling track data domain indices");
        try {

            PreparedStatement enableCoverage = con.prepareStatement(SQLStatements.ENABLE_COVERAGE_INDICES);
            PreparedStatement enableTracks = con.prepareStatement(SQLStatements.ENABLE_TRACK_INDICES);
            PreparedStatement enableMappings = con.prepareStatement(SQLStatements.ENABLE_MAPPING_INDICES);
            PreparedStatement enableDiffs = con.prepareStatement(SQLStatements.ENABLE_DIFF_INDICES);

            enableCoverage.execute();
            enableTracks.execute();
            enableMappings.execute();
            enableDiffs.execute();

            enableCoverage.close();
            enableTracks.close();
            enableMappings.close();
            enableDiffs.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "finished enabling track data domain indices");
    }

    public ReferenceConnector getRefGenomeConnector(long refGenID) {
        // only return new object, if no suitable connector was created before
        if (!refConnectors.containsKey(refGenID)) {
            refConnectors.put(refGenID, new ReferenceConnector(refGenID));
        }
        return refConnectors.get(refGenID);
    }

    public RunConnector getRunConnector(long runID,long trackID) {
        // only return new object, if no suitable connector was created before
        if (!runConnectors.containsKey(runID)) {
            runConnectors.put(runID, new RunConnector(runID,trackID));
        }
        return runConnectors.get(runID);
    }

    public TrackConnector getTrackConnector(long trackID) {
        // only return new object, if no suitable connector was created before
        if (!trackConnectors.containsKey(trackID)) {
            trackConnectors.put(trackID, new TrackConnector(trackID));
        }
        return trackConnectors.get(trackID);
    }

        public TrackConnector getTrackConnector(long trackID,long trackID2) {
        // only return new object, if no suitable connector was created before
            trackConnectors.put(trackID+trackID2+100, new TrackConnector(trackID,trackID2));
        if (!trackConnectors.containsKey(trackID)) {
            
        }
        return trackConnectors.get(trackID+trackID2+100);
    }

    public Connection getConnection() {
        return con;
    }

    public List<PersistentRun> getRuns() {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading run data from database");
        ArrayList<PersistentRun> runs = new ArrayList<PersistentRun>();

        try {
            PreparedStatement fetchRuns = con.prepareStatement(SQLStatements.FETCH_RUNS);
            ResultSet rs = fetchRuns.executeQuery();

            while (rs.next()) {
                int id = rs.getInt(FieldNames.RUN_ID);
                String description = rs.getString(FieldNames.RUN_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.RUN_TIMESTAMP);
                runs.add(new PersistentRun(id, description, date));
            }

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        return runs;
    }

    public List<PersistantReference> getGenomes() {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading reference genome data from database");
        ArrayList<PersistantReference> refGens = new ArrayList<PersistantReference>();

        try {
            PreparedStatement fetch;

            fetch = con.prepareStatement(SQLStatements.FETCH_GENOMES);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(FieldNames.REF_GEN_ID);
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

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Reading reference genome data from database");

        return refGens;
    }

    public void deleteTrack(long trackID) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of track with id \"{0}\"", trackID);
        try {
            con.setAutoCommit(false);

            PreparedStatement deleteDiffs = con.prepareStatement(SQLStatements.DELETE_DIFFS_FROM_TRACK);
            deleteDiffs.setLong(1, trackID);
            PreparedStatement deleteMappings = con.prepareStatement(SQLStatements.DELETE_MAPPINGS_FROM_TRACK);
            deleteMappings.setLong(1, trackID);
            PreparedStatement deleteCoverage = con.prepareStatement(SQLStatements.DELETE_COVERAGE_FROM_TRACK);
            deleteCoverage.setLong(1, trackID);
            PreparedStatement deleteTrack = con.prepareStatement(SQLStatements.DELETE_TRACK);
            deleteTrack.setLong(1, trackID);

            deleteDiffs.execute();
            deleteMappings.execute();
            deleteCoverage.execute();
            deleteTrack.execute();

            con.commit();

            deleteDiffs.close();
            deleteMappings.close();
            deleteCoverage.close();
            deleteTrack.close();

            con.setAutoCommit(true);

        } catch (SQLException ex) {
            throw new StorageException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished deletion of track \"{0}\"", trackID);
    }

    public void deleteRun(long runID) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of run with id \"{0}\"", runID);
        try {
            con.setAutoCommit(false);

            PreparedStatement deleteReads = con.prepareStatement(SQLStatements.DELETE_READS_FROM_RUN);
            deleteReads.setLong(1, runID);
            PreparedStatement deleteSeqs = con.prepareStatement(SQLStatements.DELETE_SEQUENCE_FROM_RUN);
            deleteSeqs.setLong(1, runID);
            PreparedStatement deleteRun = con.prepareStatement(SQLStatements.DELETE_RUN);
            deleteRun.setLong(1, runID);

            deleteReads.execute();
            deleteSeqs.execute();
            deleteRun.execute();

            con.commit();

            deleteReads.close();
            deleteSeqs.close();
            deleteRun.close();

            con.setAutoCommit(true);

        } catch (SQLException ex) {
            throw new StorageException(ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished deletion of run with id \"{0}\"", runID);
    }

    public void deleteGenome(long refGenID) throws StorageException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of reference genome with id \"{0}\"", refGenID);
        try {
            con.setAutoCommit(false);

            PreparedStatement deleteFeatures = con.prepareStatement(SQLStatements.DELETE_FEATURES_FROM_GENOME);
            deleteFeatures.setLong(1, refGenID);
            PreparedStatement deleteGenome = con.prepareStatement(SQLStatements.DELETE_GENOME);
            deleteGenome.setLong(1, refGenID);

            deleteFeatures.execute();
            deleteGenome.execute();

            con.commit();

            deleteFeatures.close();
            deleteGenome.close();

            con.setAutoCommit(true);

        } catch (SQLException ex) {
            throw new StorageException(ex);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished deletion of reference genome with id \"{0}\"", refGenID);
    }

    public List<String> getReadNamesForSequenceID(int id) {
        List<String> names = new ArrayList<String>();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_READNAMES_FOR_SEQUENCE_ID);
            fetch.setInt(1, id);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(FieldNames.READ_NAME));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ProjectConnector.class.getName()).log(Level.SEVERE, null, ex);
        }


        return names;
    }
}
