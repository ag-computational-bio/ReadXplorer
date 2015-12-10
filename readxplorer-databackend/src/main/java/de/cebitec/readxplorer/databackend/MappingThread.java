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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readxplorer.api.enums.IntervalRequestData;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.MappingResult;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.databackend.dataobjects.ReadPairGroup;
import de.cebitec.readxplorer.databackend.dataobjects.ReadPairResultPersistent;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This mapping thread should be used for analyses, but not for visualizing
 * data. The thread carries out the database querries to receive the mappings
 * for a certain interval.
 *
 * @author -Rolf Hilker-
 */
public class MappingThread extends RequestThread {

    private static final Logger LOG = LoggerFactory.getLogger( MappingThread.class.getName() );


    private static final int FIXED_INTERVAL_LENGTH = 1000;
    private final List<PersistentTrack> tracks;
    private List<Mapping> currentMappings;
    private Collection<ReadPairGroup> currentReadPairs;
    private PersistentReference refGenome;
    protected ConcurrentLinkedQueue<IntervalRequest> requestQueue;


    /**
     * Creates a new mapping thread for carrying out mapping request either to a
     * database or a file.
     * <p>
     * @param tracks the track for which this mapping thread is created
     */
    public MappingThread( List<PersistentTrack> tracks ) {
        super();
        // do general stuff
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.tracks = tracks;
        if( this.canQueryData() ) {
            ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector( tracks.get( 0 ).getRefGenID() );
            this.refGenome = refConnector.getRefGenome();
        }
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void addRequest( IntervalRequest request ) {
        this.setLatestRequest( request );
        this.requestQueue.add( request );
    }


    /**
     * Collects all mappings of the associated tracks for the interval described
     * by the request parameters.
     * <p>
     * @param request the interval request containing the requested reference
     *                interval
     * <p>
     * @return the collection of mappings for the given interval
     */
    List<Mapping> loadMappings( final IntervalRequest request ) {
        ArrayList<Mapping> mappingList = new ArrayList<>();
        if( request.getFrom() < request.getTo() && request.getFrom() > 0 && request.getTo() > 0 ) {

            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.info( "{0}: Reading mapping data from file...", currentTimestamp );

            mappingList.ensureCapacity( tracks.size() );
            for( final PersistentTrack track : tracks ) {
                SamBamFileReader externalDataReader = new SamBamFileReader( new File( track.getFilePath() ), track.getId(), refGenome );
                Collection<Mapping> intermedRes = externalDataReader.getMappingsFromBam( request );
                externalDataReader.close();
                mappingList.addAll( intermedRes );
            }
            if( tracks.size() > 1 ) {
                Collections.sort( mappingList );
            }

            currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.info( "{0}: Done reading mapping data from file...", currentTimestamp );

        }
        return mappingList;
    }


    /**
     * Receives all reduced mappings belonging to the associated tracks. In
     * order to save memory only start, stop and strand are received by this
     * method. Diffs and gaps are never included.
     * <p>
     * @param request the request to carry out
     * <p>
     * @return list of reduced mappings. Diffs and gaps are never included.
     */
    public List<Mapping> loadReducedMappings( final IntervalRequest request ) {

        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.info( "{0}: Reading mapping data from file...", currentTimestamp );

        List<Mapping> mappings = new ArrayList<>( tracks.size() );
        for( final PersistentTrack track : tracks ) {
            SamBamFileReader externalDataReader = new SamBamFileReader( new File( track.getFilePath() ), track.getId(), refGenome );
            Collection<Mapping> intermedRes = externalDataReader.getReducedMappingsFromBam( request );
            externalDataReader.close();
            mappings.addAll( intermedRes );
        }
        if( tracks.size() > 1 ) {
            Collections.sort( mappings );
        }

        currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.info( "{0}: Done reading mapping data from file...", currentTimestamp );

        return mappings;
    }


    /**
     * Fetches all read pair mappings for the given interval and typeFlag.
     * <p>
     * @param request The request for which the data shall be gathered
     * <p>
     * @return the collection of read pair mappings for the given interval and
     *         typeFlag
     */
    public Collection<ReadPairGroup> getReadPairMappings( final IntervalRequest request ) {
        ArrayList<ReadPairGroup> readPairs = new ArrayList<>();
        int from = request.getFrom();
        int to = request.getTo();

        if( from > 0 && to > 0 && from < to ) {
            readPairs.ensureCapacity( tracks.size() );
            for( PersistentTrack track : tracks ) {
                SamBamFileReader reader = new SamBamFileReader( new File( track.getFilePath() ), track.getId(), refGenome );
                Collection<ReadPairGroup> intermedRes = reader.getReadPairMappingsFromBam( request );
                readPairs.addAll( intermedRes );
            }
        }
        return readPairs;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void run() {

        while( !interrupted() ) {

            IntervalRequest request = requestQueue.poll();
            if( request != null ) {
                if( doesNotMatchLatestRequestBounds( request ) ) {
                    if( request.getDesiredData() == IntervalRequestData.ReadPairs ) {
                        this.currentReadPairs = this.getReadPairMappings( request );
                    } else if( request.getDesiredData() == IntervalRequestData.ReducedMappings ) {
                        currentMappings = this.loadReducedMappings( request );
                    } else {
                        currentMappings = this.loadMappings( request );
                    }
                    //switch between ordinary mappings and read pairs
                    if( request.getDesiredData() != IntervalRequestData.ReadPairs ) {
                        request.getSender().receiveData( new MappingResult( currentMappings, request ) );
                    } else {
                        request.getSender().receiveData( new ReadPairResultPersistent( currentReadPairs, request ) );
                    }
                }

            } else {
                try {
                    Thread.sleep( 10 );
                } catch( InterruptedException ex ) {
                    LOG.error( null, ex );
                }
            }

        }
    }


    /**
     * @return true, if the tracklist is not null or empty, false otherwise
     */
    protected boolean canQueryData() {
        return this.tracks != null && !this.tracks.isEmpty();
    }


}
