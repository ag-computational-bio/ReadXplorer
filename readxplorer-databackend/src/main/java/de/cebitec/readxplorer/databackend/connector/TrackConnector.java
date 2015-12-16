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


import de.cebitec.readxplorer.api.enums.Distribution;
import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.CoverageThread;
import de.cebitec.readxplorer.databackend.CoverageThreadAnalyses;
import de.cebitec.readxplorer.databackend.FieldNames;
import de.cebitec.readxplorer.databackend.GenericSQLQueries;
import de.cebitec.readxplorer.databackend.IntervalRequest;
import de.cebitec.readxplorer.databackend.MappingThread;
import de.cebitec.readxplorer.databackend.MappingThreadAnalyses;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.SQLStatements;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.utils.DiscreteCountingDistribution;
import de.cebitec.readxplorer.utils.StatsContainer;
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A track connector for a single track. It handles all data requests for this
 * track.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class TrackConnector {

    private static final Logger LOG = LoggerFactory.getLogger( TrackConnector.class.getName() );

    public static final int FIXED_INTERVAL_LENGTH = 1000;

    private int trackId;
    private CoverageThread coverageThread;
    private MappingThread mappingThread;
    private CoverageThreadAnalyses coverageThreadAnalyses;
    private MappingThreadAnalyses mappingThreadAnalyses;
    private PersistentReference refGenome;
    private List<PersistentTrack> associatedTracks;


    /**
     * A track connector for a single track. It handles all data requests for
     * this track.
     * <p>
     * @param track the track for which this connector is created
     * <p>
     * @throws FileNotFoundException
     * @throws DatabaseException     An exception during data queries
     */
    protected TrackConnector( final PersistentTrack track ) throws FileNotFoundException, DatabaseException {

        associatedTracks = new ArrayList<>();
        associatedTracks.add( track );
        initTrackConnector( track.getId(), false );

    }


    /**
     * A track connector for a list of tracks. It handles all data requests for
     * these tracks.
     * <p>
     * @param id            id of the track
     * @param tracks        the list of tracks for which this connector is
     *                      created
     * @param combineTracks true, if the data of these tracks is to be combined,
     *                      false if it should be kept separated
     * <p>
     * @throws FileNotFoundException
     * @throws DatabaseException     An exception during data queries
     */
    protected TrackConnector( final int id, final List<PersistentTrack> tracks, final boolean combineTracks ) throws FileNotFoundException, DatabaseException {

        if( tracks.size() > 2 && !combineTracks ) {
            throw new UnsupportedOperationException( "More than two tracks not supported yet." );
        }
        associatedTracks = tracks;
        initTrackConnector( id, combineTracks );

    }


    /**
     * Initializes all essential fields of the TrackConnector.
     * <p>
     * @param trackId       the track id to use
     * @param combineTracks true, if the data of these tracks is to be combined,
     *                      false if it should be kept separated
     *
     * @throws FileNotFoundException
     * @throws DatabaseException     An exception during data queries
     */
    private void initTrackConnector( final int trackId, final boolean combineTracks ) throws FileNotFoundException, DatabaseException {

        for( final PersistentTrack track : associatedTracks ) {
            if( !new File( track.getFilePath() ).exists() ) {
                throw new FileNotFoundException( track.getFilePath() );
            }
        }

        this.trackId = trackId;

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(
                associatedTracks.get( 0 ).getRefGenID() );
        refGenome = refConnector.getRefGenome();

        startDataThreads( combineTracks );

    }


    /**
     * Starts a thread for retrieving coverage information for a list of tracks.
     * <p>
     * @param tracks        the tracks whose coverage can be querried from the
     *                      thread
     * @param combineTracks true, if the coverage of both tracks should be
     *                      combined
     */
    private void startDataThreads( final boolean combineTracks ) {

        mappingThread = new MappingThread( associatedTracks, refGenome );
        coverageThread = new CoverageThread( associatedTracks, refGenome, combineTracks );
        mappingThreadAnalyses = new MappingThreadAnalyses( associatedTracks, refGenome );
        coverageThreadAnalyses = new CoverageThreadAnalyses( associatedTracks, refGenome, combineTracks );
        coverageThread.start();
        coverageThreadAnalyses.start();
        mappingThread.start();
        mappingThreadAnalyses.start();

    }


    /**
     * Handles a coverage request. This means the request containig the sender
     * of the request (the object that wants to receive the coverage) is handed
     * over to the CoverageThread, who will carry out the request as soon as
     * possible. Afterwards the coverage result is handed over to the receiver.
     * (CAUTION:
     * <br>1. Only the latest request is carried out completely by the thread.
     * This means when scrolling while a request is in progress the current data
     * is depleted and only data for the new request for the currently visible
     * interval is carried out)
     * <p>
     * @param request the coverage request including the receiving object
     */
    public void addCoverageRequest( IntervalRequest request ) {

        coverageThread.addRequest( request );
        //Currently we can only catch the diffs for one track, but not, if this is a multiple track connector

    }


    /**
     * Handles a coverage request. This means the request containig the sender
     * of the request (the object that wants to receive the coverage) is handed
     * over to the CoverageThread, who will carry out the request as soon as
     * possible. Afterwards the coverage result is handed over to the receiver.
     * <p>
     * @param request the coverage request including the receiving object
     */
    public void addCoverageAnalysisRequest( IntervalRequest request ) {

        coverageThreadAnalyses.addRequest( request );
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
     * <p>
     * @param request the mapping request including the receiving object
     */
    public void addMappingRequest( IntervalRequest request ) {
        mappingThread.addRequest( request );
    }


    /**
     * Handles a mapping request for analysis functions. This means the request
     * containig the sender of the request (the object that wants to receive the
     * mappings) is handed over to the MappingThreadanalyses, who will carry out
     * the request as soon as possible. Afterwards the mapping result is handed
     * over to the receiver.
     * <p>
     * @param request the mapping request including the receiving object
     */
    public void addMappingAnalysisRequest( IntervalRequest request ) {
        mappingThreadAnalyses.addRequest( request );
    }


    /**
     * Convenience method for getTrackStats(wantedTrackId).
     * <p>
     * @return The complete statistics for the main track of this connector.
     */
    public StatsContainer getTrackStats() {
        return getTrackStats( trackId );
    }


    /**
     *
     * @param wantedTrackId the id of the track, whose statistics are needed.
     * <p>
     * @return The complete statistics for the track specified by the given id.
     */
    public StatsContainer getTrackStats( final int wantedTrackId ) {

        StatsContainer statsContainer = new StatsContainer();
        statsContainer.prepareForTrack();
        statsContainer.prepareForReadPairTrack();

        try( Connection con = ProjectConnector.getInstance().getConnection();
             PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_STATS_FOR_TRACK ) ) {

            fetch.setInt( 1, wantedTrackId );
            try( final ResultSet rs = fetch.executeQuery() ) {
                while( rs.next() ) {
                    String statsKey = rs.getString( FieldNames.STATISTICS_KEY );
                    int statsValue = rs.getInt( FieldNames.STATISTICS_VALUE );
                    statsContainer.increaseValue( statsKey, statsValue );
                }
            }

        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not get track stats from the database!" );
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( ex );
        }
        return statsContainer;
    }


    /**
     * @return The unique database id of the track.
     */
    public int getTrackID() {
        return trackId;
    }


    /**
     * @return The description of the first track stored in this connector.
     */
    public String getAssociatedTrackName() {
        return associatedTracks.get( 0 ).getDescription();
    }


    /**
     * @return The list of descriptions of all tracks stored in this connector.
     */
    public List<String> getAssociatedTrackNames() {

        List<String> trackNames = new ArrayList<>( associatedTracks.size() );
        for( PersistentTrack track : associatedTracks ) {
            trackNames.add( track.getDescription() );
        }
        return trackNames;

    }


    /**
     * @return The list of unique database ids assigned to the tracks stored in
     *         this connector.
     */
    public List<Integer> getTrackIds() {

        List<Integer> trackIds = new ArrayList<>( associatedTracks.size() );
        for( PersistentTrack track : associatedTracks ) {
            trackIds.add( track.getId() );
        }
        return trackIds;

    }


    /**
     * @return TODO remove this method for encapsulation: hand data from here to
     *         thread
     */
    public CoverageThread getCoverageThread() {
        return coverageThread;
    }


    public int getNumOfReadPairsCalculate() {
        return -1; //TODO implement read pair stats calculate
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
     * @return The read pair id belonging to the track connectors track id or
     *         <code>0</code> if this track is not a read pair track or
     *         <code>-1</code> if this the track id is not found in the DB
     *         (which is the case for combined tracks).
     */
    public Integer getReadPairToTrackID() {
        int id = -1;

        try( Connection con = ProjectConnector.getInstance().getConnection() ) {
            id = GenericSQLQueries.getIntegerFromDB( SQLStatements.FETCH_READ_PAIR_TO_TRACK_ID, SQLStatements.GET_NUM, con, trackId );
        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not fetch read pair id from the database!" );
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( ex );
        }
        return id;
    }


    /**
     * @return True, if this is a read pair track, false otherwise.
     */
    public boolean isReadPairTrack() {
        return getReadPairToTrackID() > 0;
    }


    /**
     * @param readPairId the read pair id to get the second track id for
     * <p>
     * @return the second track id of a read pair beyond this track connectors
     *         track id
     */
    public int getTrackIdToReadPairId( final int readPairId ) {

        try( Connection con = ProjectConnector.getInstance().getConnection();
             PreparedStatement pStmtFetch = con.prepareStatement( SQLStatements.FETCH_TRACK_ID_TO_READ_PAIR_ID ) ) {

            pStmtFetch.setLong( 1, readPairId );
            pStmtFetch.setLong( 2, trackId );
            try( ResultSet rs = pStmtFetch.executeQuery() ) {
                if( rs.next() ) {
                    return rs.getInt( FieldNames.TRACK_ID );
                }
            }
        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not fetch track id to read pair id from the database!" );
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( ex );
        }

        return 0;
    }


    /**
     * Fetches a {@link DiscreteCountingDistribution} for this track.
     * <p>
     * @param type the type of distribution either
     *             Properties.READ_START_DISTRIBUTION or
     *             Properties.COVERAGE_INC_PERCENT_DISTRIBUTION
     * <p>
     * @return a {@link DiscreteCountingDistribution} for this track. If the
     *         desired distribution is not yet stored in the DB, the returned
     *         distribution is empty.
     */
    public DiscreteCountingDistribution getCountDistribution( final Distribution type ) {

        DiscreteCountingDistribution countDistribution = new DiscreteCountingDistribution();
        countDistribution.setType( type );

        try( Connection con = ProjectConnector.getInstance().getConnection();
             PreparedStatement pStmtFetch = con.prepareStatement( SQLStatements.FETCH_COUNT_DISTRIBUTION ) ) {

            for( PersistentTrack track : associatedTracks ) {
                pStmtFetch.setInt( 1, track.getId() );
                pStmtFetch.setByte( 2, (byte) type.getType() );
                try( ResultSet rs = pStmtFetch.executeQuery() ) {
                    while( rs.next() ) {
                        int count = rs.getInt( FieldNames.COUNT_DISTRIBUTION_BIN_COUNT );
                        int intervalId = rs.getInt( FieldNames.COUNT_DISTRIBUTION_COV_INTERVAL_ID );
                        countDistribution.setCountForIndex( intervalId, 
                                                            countDistribution.getDiscreteCountingDistribution()[intervalId] + count );
                    }
                }
            }

            if( associatedTracks.size() > 1 ) {
                int[] distribution = countDistribution.getDiscreteCountingDistribution();
                for( int i = 0; i < distribution.length; ++i ) {
                    distribution[i] = (int) Math.ceil( (double) distribution[i] / associatedTracks.size() );
                }
            }

        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not fetch count distribution from the database!" );
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( ex );
        }

        return countDistribution;
    }


    /**
     * @return the reference genome associated to this connector
     */
    public PersistentReference getRefGenome() {
        return refGenome;
    }


    /**
     * @return the length of the reference sequence belonging to this track
     *         connector
     */
    public int getActiveChromeLength() {
        return refGenome.getActiveChromLength();
    }


    /**
     * Creates an analysis handler for this track connector, which can handle
     * coverage and mapping requests for analysis functions.
     * <p>
     * @param visualizer      the DataVisualizationI implementation to treat the
     *                        analysis results
     * @param handlerTitle    title of the analysis handler
     * @param readClassParams The parameter set which contains all parameters
     *                        concerning the usage of ReadXplorer's coverage
     *                        classes and if only uniquely mapped reads shall be
     *                        used, or all reads.
     * <p>
     * @return the configurable analysis handler
     */
    public AnalysesHandler createAnalysisHandler( DataVisualisationI visualizer, String handlerTitle,
                                                  ParametersReadClasses readClassParams ) {
        return new AnalysesHandler( this, visualizer, handlerTitle, readClassParams );
    }


    /**
     * @return The path of the main (first) file associated with this
     *         TrackConnector.
     */
    public File getTrackFile() {
        return new File( associatedTracks.get( 0 ).getFilePath() );
    }


}
