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
package de.cebitec.readXplorer.parser.common;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Contains all data (description, mappings and coverageContainer) belonging
 * to a track, which can be stored into a database now.
 * 
 * @author ddoppmeier, rhilker
 */
public class ParsedTrack {

    private TrackJob trackJob;
    private HashMap<String, Integer> readNameToSeqIDMap1;
    private HashMap<String, Integer> readNameToSeqIDMap2;
    private CoverageContainer coverageContainer;
    private boolean isFirstTrack;
    private int batchPos; /** Stop position of the current batch in the ref. genome. */
    private StatsContainer statsContainer;

    /**
     * Contains all data (description, mappings and coverageContainer) belonging
     * to a track, which can be stored into a database now.
     * @param trackJob the track job for which the track is created
     * @param coverageContainer coverage container of the track
     */
    public ParsedTrack(TrackJob trackJob, CoverageContainer coverageContainer){
        this.trackJob = trackJob;
        this.readNameToSeqIDMap1 = new HashMap<>();
        this.readNameToSeqIDMap2 = new HashMap<>();
        this.coverageContainer = coverageContainer;
    }

    /**
     * @return the coverage container of this track with all coverage information
     */
    public CoverageContainer getCoverageContainer(){
        return this.coverageContainer;
    }

    /**
     * @return the description of the track
     */
    public String getDescription(){
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
     * then consists of many tracks. One for each chunk of data).
     */
    public boolean isFirstTrack() {
        return this.isFirstTrack;
    }

    /**
     * @param isFirstTrack true, if this is the first track of a stepwise import (which
     * then consists of many tracks. One for each chunk of data).
     */
    public void setIsFirstTrack(boolean isFirstTrack) {
        this.isFirstTrack = isFirstTrack;
    }
    
    /**
     * @return the readname to sequence id map for read 1 of the pair
     */
    public HashMap<String, Integer> getReadnameToSeqIdMap1(){
        return this.readNameToSeqIDMap1;
    }

    /**
     * @return the readname to sequence id map for read 2 of the pair
     */
    public HashMap<String, Integer> getReadnameToSeqIdMap2() {
        return this.readNameToSeqIDMap2;
    }
    
    /**
     * Needed additional information from sequence pair parsers.
     * @param seqToIdMap mapping of readname to sequence id for read 1 of the pair
     */
    public void setReadnameToSeqIdMap1(HashMap<String, Integer> seqToIdMap){
        this.readNameToSeqIDMap1 = seqToIdMap;
    }
    
    /**
     * Needed additional information from sequence pair parsers.
     * @param seqToIdMap mapping of readname to sequence id for read 2 of the pair
     */
    public void setReadnameToSeqIdMap2(HashMap<String, Integer> seqToIdMap) {
        this.readNameToSeqIDMap2 = seqToIdMap;
    }
    
    /**
     * Clears the coverage container and ReadnameToseqIDMap.
     * All other information persists!
     */
    public void clear(){
        this.readNameToSeqIDMap1.clear();
        this.coverageContainer.clearCoverageContainer();
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
    public void setBatchPos(int batchPos) {
        this.batchPos = batchPos;
    }

    /**
     * Sets the statistics container for this track.
     * @param statsContainer The statistics container to set
     */
    public void setStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }

    /**
     * @return The statistics container for this track. If it it currently null
     * a brand new StatsContainer is created and prepared for a standard track.
     */
    public StatsContainer getStatsContainer() {
        if (statsContainer == null) {
            this.statsContainer = new StatsContainer();
            this.statsContainer.prepareForTrack();
        }
        return statsContainer;
    }
    
}
