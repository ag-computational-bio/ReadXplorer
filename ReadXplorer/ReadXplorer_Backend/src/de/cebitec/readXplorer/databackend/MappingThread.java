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
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReadPairGroup;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.databackend.dataObjects.ReadPairResultPersistant;
import de.cebitec.readXplorer.util.Properties;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This mapping thread should be used for analyses, but not for visualizing
 * data. The thread carries out the database querries to receive the mappings
 * for a certain interval.
 *
 * @author -Rolf Hilker-
 */
public class MappingThread extends RequestThread {

    public static int FIXED_INTERVAL_LENGTH = 1000;
    private List<PersistantTrack> tracks;
    ConcurrentLinkedQueue<IntervalRequest> requestQueue;
    private List<PersistantMapping> currentMappings;
    private Collection<PersistantReadPairGroup> currentReadPairs;
    private PersistantReference refGenome;

    /**
     * Creates a new mapping thread for carrying out mapping request either to a
     * database or a file.
     * @param tracks the track for which this mapping thread is created
     */
    public MappingThread(List<PersistantTrack> tracks) {
        super();
        // do general stuff
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.tracks = tracks;
        if (this.canQueryData()) {
            ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(tracks.get(0).getRefGenID());
            this.refGenome = refConnector.getRefGenome();
        }
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public void addRequest(IntervalRequest request) {
        this.setLatestRequest(request);
        this.requestQueue.add(request);
    }
    
    /**
     * Collects all mappings of the associated tracks for the interval described
     * by the request parameters.
     * @param request the interval request containing the requested reference
     * interval
     * @return the collection of mappings for the given interval
     */
    List<PersistantMapping> loadMappings(IntervalRequest request) {
        List<PersistantMapping> mappingList = new ArrayList<>();
        if (request.getFrom() < request.getTo() && request.getFrom() > 0 && request.getTo() > 0) {

            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from file...", currentTimestamp);

            for (PersistantTrack track : tracks) {
                SamBamFileReader externalDataReader = new SamBamFileReader(new File(track.getFilePath()), track.getId(), refGenome);
                Collection<PersistantMapping> intermedRes = externalDataReader.getMappingsFromBam(request);
                externalDataReader.close();
                mappingList.addAll(intermedRes);
            }
            if (tracks.size() > 1) {
                Collections.sort(mappingList);
            }

            currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from file...", currentTimestamp);

        }
        return mappingList;
    }

    /**
     * Receives all reduced mappings belonging to the associated tracks. In 
     * order to save memory only start, stop and strand are received by this
     * method. Diffs and gaps are never included.
     * @param request the request to carry out
     * @return list of reduced mappings. Diffs and gaps are never included.
     */
    public List<PersistantMapping> loadReducedMappings(IntervalRequest request) {
        
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from file...", currentTimestamp);

        List<PersistantMapping> mappings = new ArrayList<>();
        for (PersistantTrack track : tracks) {
            SamBamFileReader externalDataReader = new SamBamFileReader(new File(track.getFilePath()), track.getId(), refGenome);
            Collection<PersistantMapping> intermedRes = externalDataReader.getReducedMappingsFromBam(request);
            externalDataReader.close();
            mappings.addAll(intermedRes);
        }
        if (tracks.size() > 1) {
            Collections.sort(mappings);
        }

        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from file...", currentTimestamp);

        return mappings;
    }
    
    /**
     * Fetches all read pair mappings for the given interval and typeFlag.
     * @param request The request for which the data shall be gathered
     * @return the collection of read pair mappings for the given interval
     * and typeFlag
     */
    public Collection<PersistantReadPairGroup> getReadPairMappings(IntervalRequest request) {
        Collection<PersistantReadPairGroup> readPairs = new ArrayList<>();
        int from = request.getFrom();
        int to = request.getTo();
        
        if (from > 0 && to > 0 && from < to) {
            for (PersistantTrack track : tracks) {
                SamBamFileReader reader = new SamBamFileReader(new File(track.getFilePath()), track.getId(), refGenome);
                Collection<PersistantReadPairGroup> intermedRes = reader.getReadPairMappingsFromBam(request);
                readPairs.addAll(intermedRes);
            }
        }
        return readPairs;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public void run() {

        while (!interrupted()) {

            IntervalRequest request = requestQueue.poll();
            if (request != null) {
                if (doesNotMatchLatestRequestBounds(request)) {
                    if (request.getDesiredData() == Properties.READ_PAIRS) {
                        this.currentReadPairs = this.getReadPairMappings(request);
                    } else if (request.getDesiredData() == Properties.REDUCED_MAPPINGS) {
                        currentMappings = this.loadReducedMappings(request);
                    } else {
                        currentMappings = this.loadMappings(request);
                    }
                    //switch between ordinary mappings and read pairs
                    if (request.getDesiredData() != Properties.READ_PAIRS) {
                        request.getSender().receiveData(new MappingResultPersistant(currentMappings, request));
                    } else {
                        request.getSender().receiveData(new ReadPairResultPersistant(currentReadPairs, request));
                    }
                }

            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CoverageThreadAnalyses.class.getName()).log(Level.SEVERE, null, ex);
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
