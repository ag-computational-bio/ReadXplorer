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


import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.exporter.tables.ExportDataI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Data container for a result of an analysis for a list of tracks.
 * <p>
 * @param <T> class type of the parameter set
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public abstract class ResultTrackAnalysis<T> implements ExportDataI {

    private final Map<Integer, PersistentTrack> trackMap;
    private final PersistentReference reference;
    private final Map<String, Integer> statsMap;
    private final boolean combineTracks;
    private final int trackColumn;
    private final int filterColumn;
    private List<String> trackNameList;
    private ParameterSetI<T> parameters;


    /**
     * A result of an analysis for a list of tracks. It also fetches and stores
     * the map of chromosomes for which the analysis was carried out hashed to
     * their respective chromosomes id.
     * <p>
     * @param reference     reference for which the analysis result was
     *                      generated
     * @param trackMap      the map of track ids to the tracks for which the
     *                      analysis
     * @param combineTracks <code>true</code>, if the tracks in the list are
     *                      combined, <code>false</code> otherwise
     *                      generated
     * @param trackColumn   column of the track id in result tables
     * @param filterColumn  column of the position or genomic feature in result
     *                      tables
     */
    public ResultTrackAnalysis( PersistentReference reference, Map<Integer, PersistentTrack> trackMap, boolean combineTracks,
                                int trackColumn, int filterColumn ) {
        this.reference = reference;
        this.trackMap = trackMap;
        trackNameList = PersistentTrack.generateTrackDescriptionList( trackMap.values() );
        statsMap = new HashMap<>();
        this.combineTracks = combineTracks;
        this.trackColumn = trackColumn;
        this.filterColumn = filterColumn;
    }


    /**
     * @return trackColumn column of the track id in result tables
     */
    public int getTrackColumn() {
        return trackColumn;
    }


    /**
     * @return filterColumn column of the position or genomic feature in result
     *         tables
     */
    public int getFilterColumn() {
        return filterColumn;
    }


    /**
     * @return <code>true</code>, if the tracks in the list are combined,
     *         <code>false</code> otherwise
     */
    public boolean isCombineTracks() {
        return combineTracks;
    }


    /**
     * Concatenates all track names either in full length or each name trimmed
     * to 20 characters.
     * <p>
     * @param fullLength <code>true</code>, if the track names shall appear in
     *                   full
     *                   length, <code>false</code> otherwise
     * <p>
     * @return The concatenated String containing all track names.
     */
    private String getCombinedTrackNames( boolean fullLength ) {
        String concatTrackNames = "";
        String description;
        for( PersistentTrack track : trackMap.values() ) {
            if( fullLength || track.getDescription().length() <= 20 ) {
                description = track.getDescription();
            }
            else {
                description = track.getDescription().substring( 0, 20 ) + "...";
            }
            concatTrackNames += description + ", ";
        }
        if( !concatTrackNames.isEmpty() ) {
            concatTrackNames = concatTrackNames.substring( 0, concatTrackNames.length() - 2 );
        }
        return concatTrackNames;
    }

//    private String getCombinedTrackIds() {
//        String concatTrackIds = "";
//        for (PersistentTrack track : trackMap.values()) {
//            concatTrackIds += track.getId() + ", ";
//        }
//        if (!concatTrackIds.isEmpty()) {
//            concatTrackIds = concatTrackIds.substring(0, concatTrackIds.length() - 2);
//        }
//        return concatTrackIds;
//    }

    /**
     * @param trackId          the track id of the track whose entry is needed
     * @param getFullengthName true, if the concated names shall be returned for
     *                         combined tracks, false, if shortened concated names shall be returned for
     *                         combined tracks. For single tracks, this option does not have an
     *                         influence.
     * <p>
     * @return Either a Track entry for a single track or a String of the track
     *         names or ids for a combined list of tracks
     */
    public Object getTrackEntry( int trackId, boolean getFullengthName ) {
        Object trackEntry;
        if( isCombineTracks() ) {
            trackEntry = getCombinedTrackNames( getFullengthName );
        }
        else {
            trackEntry = getTrackMap().get( trackId );
        }
        return trackEntry;
    }


    /**
     * Sets the map of tracks for which the analysis was carried out hashed to
     * their respective track id
     *
     * @param trackMap the map of tracks for which the analysis was carried out
     */
    public void setTrackMap( Map<Integer, PersistentTrack> trackMap ) {
        this.trackMap.clear();
        this.trackMap.putAll( trackMap );
        trackNameList = PersistentTrack.generateTrackDescriptionList( trackMap.values() );
    }


    /**
     * @return the map of tracks for which the analysis was carried out hashed
     *         to their respective track id
     */
    public Map<Integer, PersistentTrack> getTrackMap() {
        return Collections.unmodifiableMap( trackMap );
    }


    /**
     * @return The map of chromosomes for which the analysis was carried out
     *         hashed to their respective chromosome id.
     */
    public Map<Integer, PersistentChromosome> getChromosomeMap() {
        return reference.getChromosomes();
    }


    /**
     * @return Reference genome for which the analysis was carried out
     */
    public PersistentReference getReference() {
        return reference;
    }


    /**
     * @return the parameter set which was used for the analysis
     */
    public ParameterSetI<T> getParameters() {
        return parameters;
    }


    /**
     * Sets the parameter set which was used for the analysis
     * <p>
     * @param parameters the parameter set which was used for the analysis
     */
    public void setParameters( ParameterSetI<T> parameters ) {
        this.parameters = parameters;
    }


    /**
     * @return the list of track names associated with the overall analysis, of
     *         which this analysis result is one part
     */
    public List<String> getTrackNameList() {
        return Collections.unmodifiableList( trackNameList );
    }


    /**
     * @return The statistics map associated with this analysis
     */
    public Map<String, Integer> getStatsMap() {
        return Collections.unmodifiableMap( statsMap );
    }


    /**
     * Sets the statistics map associated with this analysis.
     * <p>
     * @param statsMap the statistics map associated with this analysis
     */
    public void setStatsMap( Map<String, Integer> statsMap ) {
        this.statsMap.clear();
        this.statsMap.putAll( statsMap );
    }


    /**
     * Adds a key value pair to the stats map.
     * <p>
     * @param key   the key of the pair
     * @param value the value of the pair
     */
    public void addStatsToMap( String key, int value ) {
        statsMap.put( key, value );
    }


    /**
     * Update the stats map of this track analysis result with the given data.
     * <p>
     * @param newStatsMap A new stats map, whose content shall be merged /
     *                    summed with the content of the stats map contained in this result object.
     */
    public void updateStatsMap( Map<String, Integer> newStatsMap ) {
        Map<String, Integer> internalStatsMap = getStatsMap();
        for( Map.Entry<String, Integer> entrySet : newStatsMap.entrySet() ) {
            String key = entrySet.getKey();
            Integer value = entrySet.getValue();
            if( internalStatsMap.containsKey( key ) ) {
                internalStatsMap.put( key, internalStatsMap.get( key ) + value );
            }
            else {
                internalStatsMap.put( key, value );
            }
        }
    }


    /**
     * Creates a table row for statistic entries by the given identifier
     * <p>
     * @param identifier the identifier of the statistic value
     * <p>
     * @return the list containing the identifier and its statistic value
     */
    public List<Object> createStatisticTableRow( String identifier ) {
        List<Object> row = new ArrayList<>();
        if( statsMap.containsKey( identifier ) ) {
            row.add( identifier );
            row.add( getStatsMap().get( identifier ) );
        }
        return row;
    }


    /**
     * Adds the complete content of the internal statistics map to the given
     * table content (list of object list)
     * <p>
     * @param statisticsExportData the table content, to which the content of
     *                             the internal statistics map shall be added
     */
    public void createStatisticTableRows( List<List<Object>> statisticsExportData ) {
        for( String id : statsMap.keySet() ) {
            statisticsExportData.add( createStatisticTableRow( id ) );
        }
    }


    /**
     * Creates a table row (= object list) with two elemets.
     * <p>
     * @param content entries to add to the table row
     * <p>
     * @return the new table row (= object list)
     */
    public static List<Object> createTableRow( Object... content ) {
        List<Object> row = new ArrayList<>();
        row.addAll( Arrays.asList( content ) );
        return row;
    }


}
