package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.*;
import de.cebitec.vamp.databackend.dataObjects.*;
import de.cebitec.vamp.util.DiscreteCountingDistribution;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.util.StatsContainer;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.RuntimeIOException;

/**
 * A track connector for a single track. It handles all data requests for this track.
 *
 * @author ddoppmeier, rhilker
 */
public class TrackConnector {

    private List<PersistantTrack> associatedTracks;
    private int trackID;
    private int refSeqLength;
    private String adapter;
    private CoverageThread coverageThread;
    private CoverageThread diffThread;
    private MappingThread mappingThread;
    private CoverageThreadAnalyses coverageThreadAnalyses;
    private MappingThreadAnalyses mappingThreadAnalyses; 
    private Connection con;
    public static int FIXED_INTERVAL_LENGTH = 1000;
    private final PersistantReference refGenome;
    private SamBamFileReader externalDataReader;

    /**
     * A track connector for a single track. It handles all data requests for this track.
     * @param track the track for which this connector is created
     * @param adapter the database adapter type (mysql or h2)
     * @throws FileNotFoundException  
     */
    protected TrackConnector(PersistantTrack track, String adapter) throws FileNotFoundException {
        this.associatedTracks = new ArrayList<>();
        this.associatedTracks.add(track);
        this.trackID = track.getId();
        this.adapter = adapter;
        this.con = ProjectConnector.getInstance().getConnection();
        
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(
                this.associatedTracks.get(0).getRefGenID());
        this.refGenome = refConnector.getRefGenome();
        this.refSeqLength = this.refGenome.getRefLength();
        if (!this.associatedTracks.get(0).getFilePath().isEmpty()) {
            openBAM();
        }

        this.startDataThreads(false);
    }

    /**
     * A track connector for a list of tracks. It handles all data requests for these tracks.
     * @param id id of the track
     * @param tracks the list of tracks for which this connector is created
     * @param adapter the database adapter type (mysql or h2)
     * @param combineTracks true, if the data of these tracks is to be combined, false if 
     *      it should be kept separated
     * @throws FileNotFoundException  
     */
    protected TrackConnector(int id, List<PersistantTrack> tracks, String adapter, boolean combineTracks) throws FileNotFoundException {
        if (tracks.size() > 2 && !combineTracks) {
            throw new UnsupportedOperationException("More than two tracks not supported yet.");
        }
        //TODO: create MultipleTrackConnector with some special methods, but extending the track connector
        this.trackID = id; //TODO: trackabhängig gucken ob db benutzt oder nicht (double/multitrack)
        this.associatedTracks = tracks;
        this.adapter = adapter;
        this.con = ProjectConnector.getInstance().getConnection();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(
                this.associatedTracks.get(0).getRefGenID());
        this.refGenome = refConnector.getRefGenome();
        this.refSeqLength = this.refGenome.getRefLength();
        if (!this.associatedTracks.get(0).getFilePath().isEmpty()) {
            openBAM();
        }
        
        this.startDataThreads(combineTracks);
    }

    /**
     * Starts a thread for retrieving coverage information for a list of tracks.
     * @param tracks the tracks whose coverage can be querried from the thread
     * @param combineTracks true, if the coverage of both tracks should be combined
     */
    private void startDataThreads(boolean combineTracks) {
        this.coverageThread = new CoverageThread(this.associatedTracks, combineTracks);
        this.diffThread = new CoverageThread(this.associatedTracks, combineTracks);
        this.coverageThreadAnalyses = new CoverageThreadAnalyses(this.associatedTracks, combineTracks);
        this.mappingThread = new MappingThread(this.associatedTracks.get(0));
        this.mappingThreadAnalyses = new MappingThreadAnalyses(this.associatedTracks.get(0));
        this.coverageThread.start();
        this.diffThread.start();
        this.coverageThreadAnalyses.start();
        this.mappingThread.start();
        this.mappingThreadAnalyses.start();
    }

    /**
     * Handles a coverage request. This means the request containig the sender
     * of the request (the object that wants to receive the coverage) is handed
     * over to the CoverageThread, who will carry out the request as soon as
     * possible. Afterwards the coverage result is handed over to the receiver.
     * (CAUTION: Only the latest request is carried out completely by the
     * thread. This means when scrolling while a request is in progress the
     * current data is depleted and only data for the new request for the
     * currently visible interval is carried out)
     * @param request the coverage request including the receiving object
     */
    public void addCoverageRequest(IntervalRequest request) {
        coverageThread.addRequest(request);        
        //Currently we can only catch the diffs for one track, but not, if this is a multiple track connector
    }
    
    /**
     * Handles a coverage request. This means the request containig the sender
     * of the request (the object that wants to receive the coverage) is handed
     * over to the CoverageThread, who will carry out the request as soon as
     * possible. Afterwards the coverage result is handed over to the receiver.
     * @param request the coverage request including the receiving object
     */
    public void addCoverageAnalysisRequest(IntervalRequest request) {
        coverageThreadAnalyses.addRequest(request);
        //Currently we can only catch the diffs for one track, but not, if this is a multiple track connector
    }
    
    /**
     * Handles a diff request. This means the request is carried out by the TrackConnector
     * if the track is stored in the db and afterwards the diff result is handed 
     * over to the receiver.
     * @param request the diff request including the receiving object
     * @return request unneeded: true, if this is a direct access track, false, if the request had to be carried out
     */
    public boolean addDiffRequest(IntervalRequest request) {
        if (request instanceof CoverageAndDiffRequest && this.associatedTracks.get(0).isDbUsed()) {
            this.diffThread.addRequest(request);
            return false;
        }
        return true;
    }
    
    /**
     * Handles a coverage and diff request. This means the request containig the
     * sender of the request (the object that wants to receive the coverage) is
     * handed over to the CoverageThread, who will carry out the request as soon
     * as possible. Afterwards the result is handed over to the receiver. For
     * database tracks the diffs are fetched from the db in a second statement.
     * @param request the coverage and diff request including the receiving object
     */
    public void addCoverageAndDiffRequest(IntervalRequest request) {
        this.coverageThread.addRequest(request);

    }
    
    /**
     * Handles a mapping request. This means the request containig the sender of
     * the request (the object that wants to receive the mappings) is handed
     * over to the MappingThread, who will carry out the request as soon as
     * possible. Afterwards the mapping result is handed over to the receiver.
     * (CAUTION: Only the latest request is carried out completely by the
     * thread. This means when scrolling while a request is in progress the
     * current data is depleted and only data for the new request for the
     * currently visible interval is carried out)
     *
     * @param request the mapping request including the receiving object
     */
    public void addMappingRequest(IntervalRequest request) {
        this.mappingThread.addRequest(request);
    } 
    
    /**
     * Handles a mapping request for analysis functions. This means the request
     * containig the sender of the request (the object that wants to receive the
     * mappings) is handed over to the MappingThreadanalyses, who will carry out
     * the request as soon as possible. Afterwards the mapping result is handed
     * over to the receiver.
     * @param request the mapping request including the receiving object
     */
    public void addMappingAnalysisRequest(IntervalRequest request) {
        this.mappingThreadAnalyses.addRequest(request);
    }
    
    /**
     * @return The complete statistics for a track.
     */
    public StatsContainer getTrackStats() {
        StatsContainer statsContainer = new StatsContainer();
        statsContainer.prepareForTrack();
        statsContainer.prepareForSeqPairTrack();
        
        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_STATS_FOR_TRACK)) {
            fetch.setInt(1, trackID);
            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                statsContainer.increaseValue(StatsContainer.NO_BESTMATCH_MAPPINGS, rs.getInt(FieldNames.STATISTICS_NUMBER_OF_BM_MAPPINGS));
                statsContainer.increaseValue(StatsContainer.NO_COMMON_MAPPINGS, rs.getInt(FieldNames.STATISTICS_NUMBER_OF_MAPPINGS));
                statsContainer.increaseValue(StatsContainer.NO_LARGE_DIST_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_LARGE_DIST_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_LARGE_ORIENT_WRONG_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_ORIENT_WRONG_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_ORIENT_WRONG_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_PERFECT_MAPPINGS, rs.getInt(FieldNames.STATISTICS_NUMBER_OF_PERFECT_MAPPINGS));
                statsContainer.increaseValue(StatsContainer.NO_PERF_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_READS, rs.getInt(FieldNames.STATISTICS_NUMBER_READS));
                statsContainer.increaseValue(StatsContainer.NO_REPEATED_SEQ, rs.getInt(FieldNames.STATISTICS_NUMBER_OF_REPEATED_SEQ));
                statsContainer.increaseValue(StatsContainer.NO_SEQ_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_SINGLE_MAPPIGNS, rs.getInt(FieldNames.STATISTICS_NUM_SINGLE_MAPPINGS));
                statsContainer.increaseValue(StatsContainer.NO_SMALL_DIST_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_SMALL_DIST_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_SMALL_ORIENT_WRONG_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQUE_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQUE_SEQS, rs.getInt(FieldNames.STATISTICS_NUMBER_OF_UNIQUE_SEQ));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_LARGE_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_UNIQ_LARGE_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_MAPPINGS, rs.getInt(FieldNames.STATISTICS_NUMBER_UNIQUE_MAPPINGS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_ORIENT_WRONG_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_PERF_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_SMALL_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_UNIQ_SMALL_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_WRNG_ORIENT_LARGE_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_WRNG_ORIENT_SMALL_PAIRS, rs.getInt(FieldNames.STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS));
                statsContainer.increaseValue(StatsContainer.AVERAGE_READ_LENGTH, rs.getInt(FieldNames.STATISTICS_AVERAGE_READ_LENGTH));
                statsContainer.increaseValue(StatsContainer.AVERAGE_SEQ_PAIR_SIZE, rs.getInt(FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH));
                statsContainer.increaseValue(StatsContainer.COVERAGE_BM_GENOME, rs.getInt(FieldNames.STATISTICS_BM_COVERAGE_OF_GENOME));
                statsContainer.increaseValue(StatsContainer.COVERAGE_COMPLETE_GENOME, rs.getInt(FieldNames.STATISTICS_COMPLETE_COVERAGE_OF_GENOME));
                statsContainer.increaseValue(StatsContainer.COVERAGE_PERFECT_GENOME, rs.getInt(FieldNames.STATISTICS_PERFECT_COVERAGE_OF_GENOME));
            }                
            rs.close();
            
        } catch (SQLException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        return statsContainer;
    }
    
    
    public int getNumOfReadsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_READS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }


    public int getNumOfUniqueSequencesCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }


    public int getNumOfUniqueBmMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_BM_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }


    public int getNumOfMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }


    public int getNumOfUniqueMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_SINGLETON_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }


    public int getNumOfPerfectUniqueMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }

    
    public double getPercentRefGenPerfectCoveredCalculate() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_OF_PERFECT_POSITIONS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
        return absValue / refSeqLength * 100;

    }

    
    public double getPercentRefGenBmCoveredCalculate() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_BM_COVERAGE_OF_GENOME_CALCULATE, SQLStatements.GET_NUM, con, trackID);
        return absValue / refSeqLength * 100;
    }


    public double getPercentRefGenNErrorCoveredCalculate() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_COVERED_POSITIONS, SQLStatements.GET_NUM, con, trackID);
        return absValue / refSeqLength * 100;
    }

    /**
     * Fetches the common coverage information from the db for a given interval.
     * @param request request containing the needed interval
     * @return a map of the position to the common coverage value at that position
     */
    public HashMap<Integer, Integer> getCoverageInfosOfTrack(IntervalRequest request) {
        PreparedStatement fetch;
        HashMap<Integer, Integer> positionMap = new HashMap<>();
        int coverage;
        int position;
        int from = request.getFrom();
        int to = request.getTo();
        if (from < to && from > 0 && to > 0) {
            try {

                fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_TRACK);

                fetch.setLong(1, trackID);
                fetch.setLong(2, from);
                fetch.setLong(3, to);
                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {
                    position = rs.getInt(FieldNames.COVERAGE_POSITION);
                    coverage = rs.getInt(FieldNames.COVERAGE_N_MULT);
                    positionMap.put(position, coverage);
                }
            } catch (SQLException ex) {
                Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return positionMap;
    }

//    public void setStatistics(int numMappings, int numUniqueMappings, int numUniqueSeq,
//            int numPerfectMappings, int numBestMatchMappings, double coveragePerf, double coverageBM,
//            double coverageComplete, int numReads) {
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track statistics");
//        
//        try (PreparedStatement insertStatistics = con.prepareStatement(SQLStatements.INSERT_STATISTICS);
//             PreparedStatement updateStatistics = con.prepareStatement(SQLStatements.UPDATE_STATISTICS)) {
//            
//            int hasTrack = GenericSQLQueries.getIntegerFromDB(SQLStatements.CHECK_FOR_TRACK_IN_STATS_CALCULATE, SQLStatements.GET_NUM, con, trackID);
//            if (hasTrack == 0) {
//                
//                int id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_STATISTICS_ID, con);
//                id++;
//                int covPerf = (int) (coveragePerf / 100 * refSeqLength);
//                int covBM = (int) (coverageBM / 100 * refSeqLength);
//                int covComplete = (int) (coverageComplete / 100 * refSeqLength);
//                
//                //calculate average read length
////                int averageReadLength = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_AVERAGE_READ_LENGTH, SQLStatements.GET_NUM, con, trackID);
//                
//                // store track in table
//                insertStatistics.setLong(1, id);
//                insertStatistics.setLong(2, trackID);
//                insertStatistics.setInt(3, numMappings);
//                insertStatistics.setInt(4, numPerfectMappings);
//                insertStatistics.setInt(5, numBestMatchMappings);
//                insertStatistics.setInt(6, numUniqueMappings);
//                insertStatistics.setInt(7, covPerf);
//                insertStatistics.setInt(8, covBM);
//                insertStatistics.setInt(9, covComplete);
//                insertStatistics.setInt(10, numUniqueSeq);
//                insertStatistics.setInt(11, numReads);
//                insertStatistics.execute();
//            } else {
//                int id = (int) GenericSQLQueries.getLatestIDFromDB(SQLStatements.GET_LATEST_STATISTICS_ID, con);
//                id++;
//                int covPerf = (int) (coveragePerf / 100 * refSeqLength);
//                int covBM = (int) (coverageBM / 100 * refSeqLength);
//                int covComplete = (int) (coverageComplete / 100 * refSeqLength);
//                // store track in table
//                updateStatistics.setInt(1, numMappings);
//                updateStatistics.setInt(2, numPerfectMappings);
//                updateStatistics.setInt(3, numBestMatchMappings);
//                updateStatistics.setInt(4, numUniqueMappings);
//                updateStatistics.setInt(5, covPerf);
//                updateStatistics.setInt(6, covBM);
//                updateStatistics.setInt(7, covComplete);
//                updateStatistics.setInt(8, numUniqueSeq);
//                updateStatistics.setInt(9, numReads);
//                updateStatistics.setLong(10, trackID);
//                updateStatistics.executeUpdate();
//            }
//
//        } catch (SQLException ex) {
//            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
//        }
//
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track statistics");
//    }

//    /**
//     * Store the sequence pair statistics for a sequence pair data set.
//     * @param numSeqPairs
//     * @param numPerfectSeqPairs
//     * @param numUniqueSeqPairs
//     * @param numUniquePerfectSeqPairs
//     * @param numSingleMappings 
//     */
//    public void addSeqPairStatistics(int numSeqPairs, int numPerfectSeqPairs, int numUniqueSeqPairs,
//            int numUniquePerfectSeqPairs, int numSingleMappings) {
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing sequence pair statistics");
//        
//        try (PreparedStatement addSeqPairStats = con.prepareStatement(SQLStatements.ADD_SEQPAIR_STATISTICS);
//             PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_STATISTICS_ID)) {
//            
//            // get latest id for track
//            long id = 0;
//            ResultSet rs = latestID.executeQuery();
//            if (rs.next()) {
//                id = rs.getLong("LATEST_ID");
//            }
//            id++;
//                
//            addSeqPairStats.setLong(1, id);
//            addSeqPairStats.setInt(2, numSeqPairs);
//            addSeqPairStats.setInt(3, numPerfectSeqPairs);
//            addSeqPairStats.setInt(4, numUniqueSeqPairs);
//            addSeqPairStats.setInt(5, numUniquePerfectSeqPairs);
//            addSeqPairStats.setInt(6, numSingleMappings);
//            addSeqPairStats.execute();
//
//        } catch (SQLException ex) {
//            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
//        }
//
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing sequence pair statistics");
//
//    }

    public int getTrackID() {
        return trackID;
    }    
    

    public String getAssociatedTrackName() {
        return associatedTracks.get(0).getDescription();
    }

    public List<String> getAssociatedTrackNames() {
        List<String> trackNames = new ArrayList<>();
        for (PersistantTrack track : this.associatedTracks) {
            trackNames.add(track.getDescription());
        }
        return trackNames;
    }

    public List<Integer> getTrackIds() {
        List<Integer> trackIds = new ArrayList<>();
        for (PersistantTrack track : this.associatedTracks) {
            trackIds.add(track.getId());
        }
        return trackIds;
    }


    /*
     * pruefe coverage links und rechts des Diffs -> soll nicht abfallen,
     * stetige Readabdeckung TODO: try to incorporate continuous coverage as
     * optional in snp detection
     */
    private boolean isCoverageContinuous(int position, double coverage) {
        boolean isContinous = true;
        try {
            //Die Coverage der naechsten 5 Basen links und rechts des SNPs wird verglichen
            int fromPos = ((position - 5) < 0) ? 0 : (position - 5);
            int toPos = ((position + 5) > this.refSeqLength) ? this.refSeqLength : (position + 5);
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK);
            fetch.setLong(3, trackID);
            fetch.setLong(1, fromPos);
            fetch.setLong(2, toPos);

            ResultSet rs = fetch.executeQuery();


            while (rs.next()) {
                int covPosition = rs.getInt(FieldNames.COVERAGE_POSITION);
                int forwardCov = rs.getInt(FieldNames.COVERAGE_BM_FW_MULT);
                int reverseCov = rs.getInt(FieldNames.COVERAGE_BM_RV_MULT);
                int neighbourCov = forwardCov + reverseCov;
                double deviation = (Math.abs(neighbourCov - coverage)) / coverage;
                if (deviation >= 0.5) {
                    isContinous = false;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return isContinous;
    }

    /**
     * @return TODO: remove this method for encapsulation: hand data from here to thread
     */
    public CoverageThread getCoverageThread() {
        return this.coverageThread;
    }

    public int getNumOfSeqPairsCalculate() {
        return -1; //TODO: implement seq pair stats calculate
    }

    public int getNumOfPerfectSeqPairsCalculate() {
        return -1;
    }

    public int getNumOfUniqueSeqPairsCalculate() {
        return -1;
    }

    public int getNumOfUniquePerfectSeqPairsCalculate() {
        return -1;
    }

    public int getNumOfSingleMappingsCalculate() {
        return -1;
    }

    public int getAverageSeqPairLengthCalculate() {
        return -1;
    }

    /**
     * Fetches all sequence pair mappings for the given interval and typeFlag.
     * @param from start position of the currently viewed interval
     * @param to stop position of the currently viewed interval
     * @param trackID2 the track id of the second track to which the currently
     * viewed sequence paris belong
     * @param typeFlag flagging which data to retrieve using
     * Properties.SEQ_PAIRS, Properties.SINGLE_MAPPINGS, Properties.BOTH
     * @return the collection of sequence pair mappings for the given interval
     * and typeFlag
     */
    public Collection<PersistantSeqPairGroup> getSeqPairMappings(int from, int to, int trackID2, byte typeFlag) {
        HashMap<Long, PersistantSeqPairGroup> seqPairs = new HashMap<>();
        if (from > 0 && to > 0 && from < to) {

            if (this.associatedTracks.get(0).isDbUsed()) {
                try {

                //sequence pair processing

                    PreparedStatement fetch;
                    if (typeFlag != Properties.SINGLE_MAPPINGS && typeFlag != Properties.NONE) {

                        //the statements are a little different for performance issues
                        if (adapter.equalsIgnoreCase("mysql")) {
                            fetch = con.prepareStatement(MySQLStatements.FETCH_SEQ_PAIRS_W_REPLICATES_FOR_INTERVAL);
                        } else {
                            //we need both statements, because an "OR" query for mapping1_id OR mapping2_id is incredibly slow...
                            fetch = con.prepareStatement(H2SQLStatements.FETCH_SEQ_PAIRS_W_REPLICATES_FOR_INTERVAL);
                            PreparedStatement fetch2 = con.prepareStatement(H2SQLStatements.FETCH_SEQ_PAIRS_W_REPLICATES_FOR_INTERVAL2);

                            fetch2.setLong(1, from - FIXED_INTERVAL_LENGTH);
                            fetch2.setLong(2, to);
                            fetch2.setLong(3, from);
                            fetch2.setLong(4, to + FIXED_INTERVAL_LENGTH);
                            fetch2.setLong(5, trackID);
                            fetch2.setLong(6, trackID2);

                            ResultSet rs3 = fetch2.executeQuery();

                            while (rs3.next()) {

                                // mapping data
                                int mappingID = rs3.getInt("MAPPING_ID");
                                int sequenceID = rs3.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                                int mappingTrack = rs3.getInt(FieldNames.MAPPING_TRACK);
                                int start = rs3.getInt(FieldNames.MAPPING_START);
                                int stop = rs3.getInt(FieldNames.MAPPING_STOP);
                                byte direction = rs3.getByte(FieldNames.MAPPING_DIRECTION);
                                boolean isFwdStrand = direction == SequenceUtils.STRAND_FWD;
                                int count = rs3.getInt("MAPPING_REP");
                                int errors = rs3.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                                int bestMapping = rs3.getInt(FieldNames.MAPPING_IS_BEST_MAPPING);
                                boolean isBestMapping = (bestMapping == 1 ? true : false);
                                long seqPairID = rs3.getLong("ORIG_PAIR_ID");
                                long mapping1Id = rs3.getLong(FieldNames.SEQ_PAIR_MAPPING1_ID);
                                long mapping2Id = rs3.getLong(FieldNames.SEQ_PAIR_MAPPING2_ID);
                                byte seqPairType = rs3.getByte(FieldNames.SEQ_PAIR_TYPE);
                                int seqPairReplicates = rs3.getInt(FieldNames.SEQ_PAIR_NUM_OF_REPLICATES);
                                PersistantMapping mapping = new PersistantMapping(mappingID, start, stop, mappingTrack, isFwdStrand, count, errors, sequenceID, isBestMapping);

                                // add new seqPair if not exists
                                if (!seqPairs.containsKey(seqPairID)) {
                                    PersistantSeqPairGroup newGroup = new PersistantSeqPairGroup();
                                    newGroup.setSeqPairId(seqPairID);
                                    seqPairs.put(seqPairID, newGroup);
                                }
                                seqPairs.get(seqPairID).addPersistantMapping(mapping, seqPairType, mapping1Id, mapping2Id, seqPairReplicates);

                            }
                            fetch2.close();
                        }

                        fetch.setLong(1, from - FIXED_INTERVAL_LENGTH); // 101 000 - 1000 = 100 000
                        fetch.setLong(2, to); // 101 100
                        fetch.setLong(3, from); // 101 000
                        fetch.setLong(4, to + FIXED_INTERVAL_LENGTH); // 101 100 + 1000 = 102 100
                        fetch.setLong(5, trackID);
                        fetch.setLong(6, trackID2);


                        ResultSet rs = fetch.executeQuery();                       
                        while (rs.next()) {

                            // mapping data
                            int mappingID = rs.getInt("MAPPING_ID");
                            int sequenceID = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                            int mappingTrack = rs.getInt(FieldNames.MAPPING_TRACK);
                            int start = rs.getInt(FieldNames.MAPPING_START);
                            int stop = rs.getInt(FieldNames.MAPPING_STOP);
                            byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                            boolean isFwdStrand = direction == SequenceUtils.STRAND_FWD;
                            int count = rs.getInt("MAPPING_REP");
                            int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                            int bestMapping = rs.getInt(FieldNames.MAPPING_IS_BEST_MAPPING);
                            boolean isBestMapping = (bestMapping == 1 ? true : false);
                            long seqPairID = rs.getLong("ORIG_PAIR_ID");
                            long mapping1Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING1_ID);
                            long mapping2Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING2_ID);
                            byte seqPairType = rs.getByte(FieldNames.SEQ_PAIR_TYPE);
                            int seqPairReplicates = rs.getInt(FieldNames.SEQ_PAIR_NUM_OF_REPLICATES);
                            PersistantMapping mapping = new PersistantMapping(mappingID, start, stop, mappingTrack, isFwdStrand, count, errors, sequenceID, isBestMapping);

                            // add new seqPair if not exists
                            if (!seqPairs.containsKey(seqPairID)) {
                                PersistantSeqPairGroup newGroup = new PersistantSeqPairGroup();
                                newGroup.setSeqPairId(seqPairID);
                                seqPairs.put(seqPairID, newGroup);
                            }
                            seqPairs.get(seqPairID).addPersistantMapping(mapping, seqPairType, mapping1Id, mapping2Id, seqPairReplicates);

                        }

                        fetch.close();
                    }

                    //single mapping processing
                    if (typeFlag != Properties.SEQ_PAIRS && typeFlag != Properties.NONE) {

                        PreparedStatement fetchSingleReads = con.prepareStatement(SQLStatements.FETCH_SEQ_PAIRS_PIVOT_DATA_FOR_INTERVAL);
                        fetchSingleReads.setLong(1, from - FIXED_INTERVAL_LENGTH);
                        fetchSingleReads.setLong(2, to);
                        fetchSingleReads.setLong(3, from);
                        fetchSingleReads.setLong(4, to + FIXED_INTERVAL_LENGTH);
                        fetchSingleReads.setLong(5, trackID);
                        fetchSingleReads.setLong(6, trackID2);

                        ResultSet rs2 = fetchSingleReads.executeQuery();
                        while (rs2.next()) {

                            // mapping data
                            int mappingID = rs2.getInt("MAPPING_ORIG_ID");
                            int sequenceID = rs2.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                            int mappingTrack = rs2.getInt(FieldNames.MAPPING_TRACK);
                            int start = rs2.getInt(FieldNames.MAPPING_START);
                            int stop = rs2.getInt(FieldNames.MAPPING_STOP);
                            byte direction = rs2.getByte(FieldNames.MAPPING_DIRECTION);
                            boolean isFwdStrand = direction == SequenceUtils.STRAND_FWD;
                            int count = rs2.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                            int errors = rs2.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                            int bestMapping = rs2.getInt(FieldNames.MAPPING_IS_BEST_MAPPING);
                            boolean isBestMapping = (bestMapping == 1 ? true : false);
                            long seqPairID = rs2.getLong(FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID);
                            PersistantMapping mapping = new PersistantMapping(mappingID, start, stop, mappingTrack, isFwdStrand, count, errors, sequenceID, isBestMapping);

                            // add to seqPair container
                            if (!seqPairs.containsKey(seqPairID)) {
                                PersistantSeqPairGroup newGroup = new PersistantSeqPairGroup();
                                newGroup.setSeqPairId(seqPairID);
                                seqPairs.put(seqPairID, newGroup);
                            }
                            seqPairs.get(seqPairID).addPersistantMapping(mapping, Properties.TYPE_UNPAIRED_PAIR, -1, -1, -1);

                        }

                        fetchSingleReads.close();

                    }
                } catch (SQLException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                IntervalRequest request = new IntervalRequest(from, to, null, new ParametersReadClasses());
                return externalDataReader.getSeqPairMappingsFromBam(this.refGenome, request, true);
            }
        }


        return seqPairs.values();
    }

    /**
     * @return The sequence pair id belonging to the track connectors track id
     * or <code>0</code> if this track is not a sequence pair track.
     */
    public Integer getSeqPairToTrackID() {
        int value = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_SEQ_PAIR_TO_TRACK_ID, SQLStatements.GET_NUM, con, trackID);
        return value;
    }

    /**
     * @return True, if this is a sequence pair track, false otherwise.
     */
    public boolean isSeqPairTrack() {
        return this.getSeqPairToTrackID() != 0;
    }

    /**
     * @param seqPairId the sequence pair id to get the second track id for
     * @return the second track id of a sequence pair beyond this track
     * connectors track id
     */
    public int getTrackIdToSeqPairId(int seqPairId) {
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_TRACK_ID_TO_SEQ_PAIR_ID);
            fetch.setLong(1, seqPairId);
            fetch.setLong(2, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt(FieldNames.TRACK_ID);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLStatements.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    /**
     * Returns all mappings belonging to a sequence pair
     *
     * @param seqPairId sequence id to search for
     * @return all data belonging to this sequence pair id (all mappings and
     * pair replicates)
     */
    public PersistantSeqPairGroup getMappingsForSeqPairId(long seqPairId) {

        PersistantSeqPairGroup seqPairData = new PersistantSeqPairGroup();
        seqPairData.setSeqPairId(seqPairId);
        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_FOR_SEQ_PAIR_ID);
             PreparedStatement fetch2 = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_FOR_SEQ_PAIR_ID2);
             PreparedStatement fetchSingleReads = con.prepareStatement(SQLStatements.FETCH_SINGLE_MAPPINGS_FOR_SEQ_PAIR_ID);) {

            //sequence pair processing
            fetch.setLong(1, seqPairId);
            fetch2.setLong(1, seqPairId);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {

                // mapping data
                long mappingId = rs.getLong(FieldNames.MAPPING_ID);
                int start = rs.getInt(FieldNames.MAPPING_START);
                int stop = rs.getInt(FieldNames.MAPPING_STOP);
                byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                boolean isFwdStrand = direction == SequenceUtils.STRAND_FWD;
                int count = rs.getInt("MAPPING_REPLICATES");
                int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                int bestMapping = rs.getInt(FieldNames.MAPPING_IS_BEST_MAPPING);
                boolean isBestMapping = bestMapping == 1;
                long mapping1Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING1_ID);
                long mapping2Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING2_ID);
                byte seqPairType = rs.getByte(FieldNames.SEQ_PAIR_TYPE);
                int seqPairReplicates = rs.getInt(FieldNames.SEQ_PAIR_NUM_OF_REPLICATES);
                
                PersistantMapping mapping = new PersistantMapping((int) mappingId, start, stop, -1, isFwdStrand, count, errors, -1, isBestMapping);
                seqPairData.addPersistantMapping(mapping, seqPairType, mapping1Id, mapping2Id, seqPairReplicates);

            }

            ResultSet rs2 = fetch2.executeQuery();
            while (rs2.next()) {

                // mapping data
                long mappingId = rs2.getLong(FieldNames.MAPPING_ID);
                int start = rs2.getInt(FieldNames.MAPPING_START);
                int stop = rs2.getInt(FieldNames.MAPPING_STOP);
                byte direction = rs2.getByte(FieldNames.MAPPING_DIRECTION);
                boolean isFwdStrand = direction == SequenceUtils.STRAND_FWD;
                int count = rs2.getInt("MAPPING_REPLICATES");
                int errors = rs2.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                int bestMapping = rs2.getInt(FieldNames.MAPPING_IS_BEST_MAPPING);
                boolean isBestMapping = bestMapping == 1;
                long mapping1Id = rs2.getLong(FieldNames.SEQ_PAIR_MAPPING1_ID);
                long mapping2Id = rs2.getLong(FieldNames.SEQ_PAIR_MAPPING2_ID);
                byte seqPairType = rs2.getByte(FieldNames.SEQ_PAIR_TYPE);
                int seqPairReplicates = rs2.getInt(FieldNames.SEQ_PAIR_NUM_OF_REPLICATES);
                
                rs2.close();

                PersistantMapping mapping = new PersistantMapping((int) mappingId, start, stop, -1, isFwdStrand, count, errors, -1, isBestMapping);
                seqPairData.addPersistantMapping(mapping, seqPairType, mapping1Id, mapping2Id, seqPairReplicates);

            }

            //single mapping processing
            fetchSingleReads.setLong(1, seqPairId);

            rs = fetchSingleReads.executeQuery();
            while (rs.next()) {

                // mapping data
                int start = rs.getInt(FieldNames.MAPPING_START);
                int stop = rs.getInt(FieldNames.MAPPING_STOP);
                byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                boolean isFwdStrand = direction == SequenceUtils.STRAND_FWD;
                int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                int bestMapping = rs.getInt(FieldNames.MAPPING_IS_BEST_MAPPING);
                boolean isBestMapping = bestMapping == 1;
                long mapping1Id = rs.getLong(FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID);
                PersistantMapping mapping = new PersistantMapping((int) mapping1Id, start, stop, -1, isFwdStrand, count, errors, -1, isBestMapping);

                seqPairData.addPersistantMapping(mapping, Properties.TYPE_UNPAIRED_PAIR, -1, -1, -1);

            }
            rs.close();

            fetchSingleReads.close();
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        return seqPairData;

    }

    /**
     * Fetches a {@link DiscreteCountingDistribution} for this track.
     * @param type the type of distribution either
     * Properties.COVERAGE_INCREASE_DISTRIBUTION or
     * Properties.COVERAGE_INC_PERCENT_DISTRIBUTION
     * @return a {@link DiscreteCountingDistribution} for this track.
     */
    public DiscreteCountingDistribution getCountDistribution(byte type) {
        DiscreteCountingDistribution countDistribution = new DiscreteCountingDistribution();
        countDistribution.setType(type);

        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COUNT_DISTRIBUTION)) {
            
            fetch.setInt(1, this.trackID);
            fetch.setByte(2, type);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int coverageIntervalId = rs.getInt(FieldNames.COUNT_DISTRIBUTION_COV_INTERVAL_ID);
                int count = rs.getInt(FieldNames.COUNT_DISTRIBUTION_BIN_COUNT);
                countDistribution.setCountForIndex(coverageIntervalId, count);
            }
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        return countDistribution;
    }
    
    /**
     * @return true, if this track's data is completely stored in the db, false
     * if this is a direct access track
     */
    public boolean isDbUsed() {
        return this.associatedTracks.get(0).isDbUsed();
    }

    /**
     * @return the reference genome associated to this connector
     */
    public PersistantReference getRefGenome() {
        return this.refGenome;
    }

    /**
     * @return the length of the reference sequence belonging to this track
     * connector
     */
    public int getRefSequenceLength() {
        return this.refSeqLength;
    }    
    
    /**
     * Creates an analysis handler for this track connector, which can handle
     * coverage and mapping requests for analysis functions.
     * @param visualizer the DataVisualizationI implementation to treat the
     * analysis results
     * @param handlerTitle title of the analysis handler
     * @param readClassParams The parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads.
     * @return the configurable analysis handler
     */
    public AnalysesHandler createAnalysisHandler(DataVisualisationI visualizer, String handlerTitle, 
            ParametersReadClasses readClassParams) {
        return new AnalysesHandler(this, visualizer, handlerTitle, readClassParams);
    }

    private void openBAM() throws FileNotFoundException {
        try {
            File file = new File(this.associatedTracks.get(0).getFilePath());
            this.externalDataReader = new SamBamFileReader(file, this.trackID);
        } catch (RuntimeIOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }
    
    /**
     * Releases resources which are not needed after closing the track connector.
     */
    public void close() {
        this.mappingThread.close();
        this.mappingThreadAnalyses.close();
        this.externalDataReader.close();
    }
}
