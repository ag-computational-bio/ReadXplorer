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


import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.ReadPairType;
import de.cebitec.readXplorer.util.StatsContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Container for all read pairs belonging to a pair of tracks.
 * <p>
 * @author Rolf Hilker
 */
public class ParsedReadPairContainer {

    private int trackId1;
    private int trackId2;
    private String description;
    private HashMap<Pair<Long, Long>, ParsedReadPairMapping> parsedReadPairs;
    private List<Pair<Long, Long>> mappingToPairIDList;
    private StatsContainer statsContainer;


    /**
     * Creates a new and empty sequence pair container.
     */
    public ParsedReadPairContainer() {
        this.parsedReadPairs = new HashMap<>();
        this.mappingToPairIDList = new ArrayList<>();
        this.statsContainer = new StatsContainer();
        this.statsContainer.prepareForTrack();
        this.statsContainer.prepareForReadPairTrack();
    }


    /**
     * Adds all mappings to a MappingGroup with same pair id,
     * but different mapping positions
     * Mapping = is unique to a sequence and position
     * <p>
     * @param mappingIDs
     * @param parsedReadPair
     */
    public void addParsedReadPair( Pair<Long, Long> mappingIDs, ParsedReadPairMapping parsedReadPair ) {
        Map<String, Integer> statsMap = statsContainer.getStatsMap();
        if( !this.parsedReadPairs.containsKey( mappingIDs ) ) {
            this.parsedReadPairs.put( mappingIDs, parsedReadPair ); //TODO: mappingIDs can be vice versa

            if( parsedReadPair.getType() == ReadPairType.PERFECT_PAIR || parsedReadPair.getType() == ReadPairType.PERFECT_UNQ_PAIR ) {
                statsMap.put( StatsContainer.NO_UNIQ_PERF_PAIRS, statsMap.get( StatsContainer.NO_UNIQ_PERF_PAIRS ) + 1 );
            }
            statsMap.put( StatsContainer.NO_UNIQUE_PAIRS, statsMap.get( StatsContainer.NO_UNIQUE_PAIRS ) + 1 );
        }
        else {
            this.parsedReadPairs.get( mappingIDs ).addReplicate();
            if( parsedReadPair.getType() == ReadPairType.PERFECT_PAIR || parsedReadPair.getType() == ReadPairType.PERFECT_UNQ_PAIR ) {
                statsMap.put( StatsContainer.NO_UNIQ_PERF_PAIRS, statsMap.get( StatsContainer.NO_UNIQ_PERF_PAIRS ) - 1 );
            }
            statsMap.put( StatsContainer.NO_UNIQUE_PAIRS, statsMap.get( StatsContainer.NO_UNIQUE_PAIRS ) - 1 );
        }
        statsContainer.incReadPairStats( parsedReadPair.getType(), 1 );
    }


    /**
     * Adds a pair containing a mapping id and a read pair id to the list.
     * <p>
     * @param mappingToPairId pair to add
     */
    public void addMappingToPairId( Pair<Long, Long> mappingToPairId ) {
        this.mappingToPairIDList.add( mappingToPairId );
    }


    public void setDescription( String description ) {
        this.description = description;
    }


    public HashMap<Pair<Long, Long>, ParsedReadPairMapping> getParsedReadPairs() {
        return parsedReadPairs;
    }


    /**
     * @return The mapping list of all mapping ids to their corresponding
     *         sequence pair ids. Only contains the mappings which don't form a proper
     *         pair.
     *         To get this mapping for paired sequences use
     *         <code>getParsedReadPairs()</code>.
     */
    public List<Pair<Long, Long>> getMappingToPairIdList() {
        return this.mappingToPairIDList;
    }


    public String getDescription() {
        return description;
    }


    /**
     * Clears only the parsed mate pairs. All other data persists.
     */
    public void clear() {
        parsedReadPairs.clear();
    }


    public int getTrackId1() {
        return trackId1;
    }


    public void setTrackId1( int trackId1 ) {
        this.trackId1 = trackId1;
    }


    public int getTrackId2() {
        return trackId2;
    }


    public void setTrackId2( int trackId2 ) {
        this.trackId2 = trackId2;
    }


    public StatsContainer getStatsContainer() {
        return statsContainer;
    }


}
