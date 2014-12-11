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

package de.cebitec.readxplorer.parser.common;


import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


/**
 * Contains all data (description, mappings and coverageContainer) belonging
 * to a track, which can be stored into a database now.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class ParsedTrack {

    private final TrackJob trackJob;
    private Map<String, Integer> readNameToSeqIDMap1;
    private Map<String, Integer> readNameToSeqIDMap2;
    private boolean isFirstTrack;
    private int batchPos;
    /**
     * Stop position of the current batch in the ref. genome.
     */
    private StatsContainer statsContainer;


    /**
     * Contains all data (description, mappings and coverageContainer) belonging
     * to a track, which can be stored into a database now.
     * <p>
     * @param trackJob the track job for which the track is created
     */
    public ParsedTrack( TrackJob trackJob ) {
        this.trackJob = trackJob;
        this.readNameToSeqIDMap1 = new HashMap<>();
        this.readNameToSeqIDMap2 = new HashMap<>();
    }


    /**
     * @return the description of the track
     */
    public String getDescription() {
        return trackJob.getDescription();
    }


    /**
     * @return the timestamp of the creation time of this track
     */
    public Timestamp getTimestamp() {
        return trackJob.getTimestamp();
    }


    /**
     * @return the track id of this track
     */
    public int getID() {
        return trackJob.getID();
    }


    /**
     * @return the id of the reference genome in the db
     */
    public int getRefId() {
        return trackJob.getRefGen().getID();
    }


    /**
     * @return the track name
     */
    public String getTrackName() {
        return trackJob.getName();
    }


    /**
     * @return true, if this is the first track of a stepwise import (which
     *         then consists of many tracks. One for each chunk of data).
     */
    public boolean isFirstTrack() {
        return this.isFirstTrack;
    }


    /**
     * @param isFirstTrack true, if this is the first track of a stepwise import
     *                     (which
     *                     then consists of many tracks. One for each chunk of data).
     */
    public void setIsFirstTrack( boolean isFirstTrack ) {
        this.isFirstTrack = isFirstTrack;
    }


    /**
     * Needed additional information from sequence pair parsers.
     * <p>
     * @param seqToIdMap mapping of readname to sequence id for read 1 of the
     *                   pair
     */
    public void setReadnameToSeqIdMap1( HashMap<String, Integer> seqToIdMap ) {
        this.readNameToSeqIDMap1 = seqToIdMap;
    }


    /**
     * Needed additional information from sequence pair parsers.
     * <p>
     * @param seqToIdMap mapping of readname to sequence id for read 2 of the
     *                   pair
     */
    public void setReadnameToSeqIdMap2( HashMap<String, Integer> seqToIdMap ) {
        this.readNameToSeqIDMap2 = seqToIdMap;
    }


    /**
     * Clears the coverage container and ReadnameToseqIDMap.
     * All other information persists!
     */
    public void clear() {
        this.readNameToSeqIDMap1.clear();
    }


    /**
     * @return the file from which this track was created.
     */
    public File getFile() {
        return this.trackJob.getFile();
    }


    /**
     * @return Stop position of the current batch in the ref. genome.
     */
    public int getBatchPos() {
        return batchPos;
    }


    /**
     * @param batchPos Stop position of the current batch in the ref. genome.
     */
    public void setBatchPos( int batchPos ) {
        this.batchPos = batchPos;
    }


    /**
     * Sets the statistics container for this track.
     * <p>
     * @param statsContainer The statistics container to set
     */
    public void setStatsContainer( StatsContainer statsContainer ) {
        this.statsContainer = statsContainer;
    }


    /**
     * @return The statistics container for this track. If it it currently null
     *         a brand new StatsContainer is created and prepared for a standard track.
     */
    public StatsContainer getStatsContainer() {
        if( statsContainer == null ) {
            this.statsContainer = new StatsContainer();
            this.statsContainer.prepareForTrack();
        }
        return statsContainer;
    }


}
