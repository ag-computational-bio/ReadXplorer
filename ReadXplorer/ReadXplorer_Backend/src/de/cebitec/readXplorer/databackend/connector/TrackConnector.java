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

import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.CoverageThread;
import de.cebitec.readXplorer.databackend.CoverageThreadAnalyses;
import de.cebitec.readXplorer.databackend.FieldNames;
import de.cebitec.readXplorer.databackend.GenericSQLQueries;
import de.cebitec.readXplorer.databackend.IntervalRequest;
import de.cebitec.readXplorer.databackend.MappingThread;
import de.cebitec.readXplorer.databackend.MappingThreadAnalyses;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.SQLStatements;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.DiscreteCountingDistribution;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A track connector for a single track. It handles all data requests for this track.
 *
 * @author ddoppmeier, rhilker
 */
public class TrackConnector {

    private List<PersistantTrack> associatedTracks;
    private int trackID;
    private CoverageThread coverageThread;
    private MappingThread mappingThread;
    private CoverageThreadAnalyses coverageThreadAnalyses;
    private MappingThreadAnalyses mappingThreadAnalyses; 
    private Connection con;
    public static int FIXED_INTERVAL_LENGTH = 1000;
    private PersistantReference refGenome;

    /**
     * A track connector for a single track. It handles all data requests for this track.
     * @param track the track for which this connector is created
     * @throws FileNotFoundException  
     */
    protected TrackConnector(PersistantTrack track) throws FileNotFoundException {
        this.associatedTracks = new ArrayList<>();
        this.associatedTracks.add(track);
        this.initTrackConnector(track.getId(), false);
    }

    /**
     * A track connector for a list of tracks. It handles all data requests for these tracks.
     * @param id id of the track
     * @param tracks the list of tracks for which this connector is created
     * @param combineTracks true, if the data of these tracks is to be combined, false if 
     *      it should be kept separated
     * @throws FileNotFoundException  
     */
    protected TrackConnector(int id, List<PersistantTrack> tracks, boolean combineTracks) throws FileNotFoundException {
        if (tracks.size() > 2 && !combineTracks) {
            throw new UnsupportedOperationException("More than two tracks not supported yet.");
        }
        this.associatedTracks = tracks;
        this.initTrackConnector(id, combineTracks);
    }
    
    /**
     * Initializes all essential fields of the TrackConnector.
     * @param trackId the track id to use
     * @param combineTracks true, if the data of these tracks is to be combined,
     * false if it should be kept separated
     */
    private void initTrackConnector(int trackId, boolean combineTracks) throws FileNotFoundException {
        for (PersistantTrack track : associatedTracks) {
            if (!new File(track.getFilePath()).exists()) {
                throw new FileNotFoundException(track.getFilePath());
            }
        }
        
        this.trackID = trackId;
        this.con = ProjectConnector.getInstance().getConnection();

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(
                this.associatedTracks.get(0).getRefGenID());
        this.refGenome = refConnector.getRefGenome();

        this.startDataThreads(combineTracks);
    }
    
//    /**
//     * Just for JUnit test purposes, after testing, comment it out!
//     * @param trackId
//     * @throws FileNotFoundException 
//     */
//    public TrackConnector(int trackId) throws FileNotFoundException {
//        this.trackID = trackId;
//        this.refGenome = null;
//    }

    /**
     * Starts a thread for retrieving coverage information for a list of tracks.
     * @param tracks the tracks whose coverage can be querried from the thread
     * @param combineTracks true, if the coverage of both tracks should be combined
     */
    private void startDataThreads(boolean combineTracks) {
        this.coverageThread = new CoverageThread(this.associatedTracks, combineTracks);
        this.coverageThreadAnalyses = new CoverageThreadAnalyses(this.associatedTracks, combineTracks);
        this.mappingThread = new MappingThread(this.associatedTracks);
        this.mappingThreadAnalyses = new MappingThreadAnalyses(this.associatedTracks);
        this.coverageThread.start();
        this.coverageThreadAnalyses.start();
        this.mappingThread.start();
        this.mappingThreadAnalyses.start();
    }

    /**
     * Handles a coverage request. This means the request containig the sender
     * of the request (the object that wants to receive the coverage) is handed
     * over to the CoverageThread, who will carry out the request as soon as
     * possible. Afterwards the coverage result is handed over to the receiver.
     * (CAUTION: 
     * <br>1. Only the latest request is carried out completely by the
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
     * Convenience method for getTrackStats(wantedTrackId).
     * @return The complete statistics for the main track of this connector.
     */
    public StatsContainer getTrackStats() {
        return this.getTrackStats(trackID);
    }
    
    /**
     * 
     * @param wantedTrackId the id of the track, whose statistics are needed.
     * @return The complete statistics for the track specified by the given id.
     */
    public StatsContainer getTrackStats(int wantedTrackId) {
        StatsContainer statsContainer = new StatsContainer();
        statsContainer.prepareForTrack();
        statsContainer.prepareForReadPairTrack();

        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_STATS_FOR_TRACK)) {
            fetch.setInt(1, wantedTrackId);
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
                statsContainer.increaseValue(StatsContainer.AVERAGE_READ_PAIR_SIZE, rs.getInt(FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH));
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

    /**
     * @return TODO: remove this method for encapsulation: hand data from here to thread
     */
    public CoverageThread getCoverageThread() {
        return this.coverageThread;
    }

    public int getNumOfReadPairsCalculate() {
        return -1; //TODO: implement read pair stats calculate
    }

    public int getNumOfPerfectReadPairsCalculate() {
        return -1;
    }

    public int getNumOfUniqueReadPairsCalculate() {
        return -1;
    }

    public int getNumOfUniquePerfectReadPairsCalculate() {
        return -1;
    }

    public int getNumOfSingleMappingsCalculate() {
        return -1;
    }

    public int getAverageReadPairLengthCalculate() {
        return -1;
    }

    /**
     * @return The read pair id belonging to the track connectors track id
     * or <code>0</code> if this track is not a read pair track.
     */
    public Integer getReadPairToTrackID() {
        int value = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_READ_PAIR_TO_TRACK_ID, SQLStatements.GET_NUM, con, trackID);
        return value;
    }

    /**
     * @return True, if this is a sequence pair track, false otherwise.
     */
    public boolean isReadPairTrack() {
        return this.getReadPairToTrackID() != 0;
    }

    /**
     * @param readPairId the read pair id to get the second track id for
     * @return the second track id of a read pair beyond this track
     * connectors track id
     */
    public int getTrackIdToReadPairId(int readPairId) {
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_TRACK_ID_TO_READ_PAIR_ID);
            fetch.setLong(1, readPairId);
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
     * Fetches a {@link DiscreteCountingDistribution} for this track.
     * @param type the type of distribution either
     * Properties.READ_START_DISTRIBUTION or
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
     * @return the reference genome associated to this connector
     */
    public PersistantReference getRefGenome() {
        return this.refGenome;
    }

    /**
     * @return the length of the reference sequence belonging to this track
     * connector
     */
    public int getActiveChromeLength() {
        return this.refGenome.getActiveChromLength();
    }    
    
    /**
     * Creates an analysis handler for this track connector, which can handle
     * coverage and mapping requests for analysis functions.
     * @param visualizer the DataVisualizationI implementation to treat the
     * analysis results
     * @param handlerTitle title of the analysis handler
     * @param readClassParams The parameter set which contains all parameters
     * concerning the usage of ReadXplorer's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads.
     * @return the configurable analysis handler
     */
    public AnalysesHandler createAnalysisHandler(DataVisualisationI visualizer, String handlerTitle, 
            ParametersReadClasses readClassParams) {
        return new AnalysesHandler(this, visualizer, handlerTitle, readClassParams);
    }

    public File getTrackPath() {
        return new File(this.associatedTracks.get(0).getFilePath());
    }
}
