package de.cebitec.readXplorer.parser.common;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

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
    private ParsedMappingContainer mappings;
    private CoverageContainer coverageContainer;
    private boolean isFirstTrack;
    private int batchPos; /** Stop position of the current batch in the ref. genome. */
    private StatsContainer statsContainer;

    /**
     * Contains all data (description, mappings and coverageContainer) belonging
     * to a track, which can be stored into a database now.
     * @param trackJob the track job for which the track is created
     * @param mappings mappings of the track
     * @param coverageContainer coverage container of the track
     */
    public ParsedTrack(TrackJob trackJob, ParsedMappingContainer mappings, CoverageContainer coverageContainer){
        this.trackJob = trackJob;
        this.readNameToSeqIDMap1 = new HashMap<>();
        this.readNameToSeqIDMap2 = new HashMap<>();
        this.mappings = mappings;
        this.coverageContainer = coverageContainer;
    }

    /**
     * @return true, if this track is stored in the db completely, false otherwise
     */
    public boolean isDbUsed() {
        return trackJob.isDbUsed();
    }

    /**
     * @return the coverage container of this track with all coverage information
     */
    public CoverageContainer getCoverageContainer(){
        return this.coverageContainer;
    }

    /**
     * @return the container of all mappings of this track (if they were stored,
     * if not, the container is just empty)
     */
    public ParsedMappingContainer getParsedMappingContainer() {
        return this.mappings;
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
     * Clears the mappings, coverage container and ReadnameToseqIDMap.
     * All other information persists!
     */
    public void clear(){
        this.mappings.clear();
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
     * @return The statistics container for this track
     */
    public StatsContainer getStatsContainer() {
        if (statsContainer == null) {
            this.statsContainer = new StatsContainer();
            this.statsContainer.prepareForTrack();
            if (mappings.getMappingInfos() != null) {
                Map<Integer,Integer> mappingInfos = mappings.getMappingInfos();
                statsContainer.increaseValue(StatsContainer.NO_COMMON_MAPPINGS, mappingInfos.get(1));
                statsContainer.increaseValue(StatsContainer.NO_PERFECT_MAPPINGS, mappingInfos.get(2));
                statsContainer.increaseValue(StatsContainer.NO_BESTMATCH_MAPPINGS, mappingInfos.get(3));
                statsContainer.increaseValue(StatsContainer.NO_READS, mappingInfos.get(6));
                statsContainer.increaseValue(StatsContainer.NO_UNIQUE_SEQS, mappingInfos.get(5));
                statsContainer.increaseValue(StatsContainer.NO_UNIQ_MAPPINGS, mappingInfos.get(4));
            }
        }
        return statsContainer;
    }
    

}
