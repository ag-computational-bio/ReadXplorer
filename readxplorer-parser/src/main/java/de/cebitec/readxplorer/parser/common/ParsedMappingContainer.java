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


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Container for all mappings belonging to one track. Contains statistics as
 * well as a all mappings.
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedMappingContainer {

    private int numberOfMappings = 0; //the number of created mappings by the mapper
    private int numberOfBM = 0;
    private int numberOfPerfect = 0;
    private int numOfMappings = 0;
    private int numUniqueSeq = 0; //mappingseq only occurs once in data set
    private int numUniqueMappings = 0; //calculate number of unique mappings (map only to one position, but have replicates)
    private int numReads = 0;
    private final HashMap<Integer, ParsedMappingGroup> mappings;
    private boolean lastMappingContainer = false;
    private boolean firstMappingContainer = false;
    private int sumReadLength;
    private boolean mappingInfosCalculated = false;
    private Map<Integer, Integer> mappingInfos;


    /**
     * Creates an empty mapping container.
     */
    public ParsedMappingContainer() {
        mappings = new HashMap<>();
        mappingInfos = new HashMap<>();
        sumReadLength = 0;
    }


    public void addParsedMapping( ParsedMapping mapping, int sequenceID ) {
        numOfMappings++;
        if( !mappings.containsKey( sequenceID ) ) {
            ParsedMappingGroup mappingGroup = new ParsedMappingGroup();
            mappings.put( sequenceID, mappingGroup );
            numUniqueSeq++;
        }
        mappings.get( sequenceID ).addParsedMapping( mapping );
        if( mappings.get( sequenceID ).getMappings().size() == 2 ) {
            numUniqueSeq--;
        }
    }


    public Collection<Integer> getMappedSequenceIDs() {
        return Collections.unmodifiableCollection( mappings.keySet() );
    }


    public ParsedMappingGroup getParsedMappingGroupBySeqID( int sequenceID ) {
        return mappings.get( sequenceID );
    }


    /**
     * Set the mapping informations, if calculated somewhere else already.
     * <p>
     * @param mappingInfos the mapping infos to set:
     * <br>(1, numberOfMappings);
     * <br>(2, numberOfPerfect);
     * <br>(3, numberOfBM);
     * <br>(4, numUniqueMappings); = reads that only map to one position in the
     * reference
     * <br>(5, numUniqueSeq); = reads whose sequence is unique in the data set
     * <br>(6, numReads);
     * <br>(7, sumReadLength);
     */
    public void setMappingInfos( Map<Integer, Integer> mappingInfos ) {
        this.mappingInfos.clear();
        this.mappingInfos.putAll( mappingInfos );
        this.mappingInfosCalculated = true;
    }


    /**
     * @return Hashmap with following entries: <br>(1, numberOfMappings);
     * <br>(2, numberOfPerfect); <br>(3, numberOfBM); <br>(4,
     * numUniqueMappings); <br>(5, numUniqueSeq); <br>(6, numReads); <br>(7,
     * sumReadLength);
     */
    public Map<Integer, Integer> getMappingInfos() {
        if( !mappingInfosCalculated ) {
            calcMappingInformations();
        }
        return Collections.unmodifiableMap( mappingInfos );
    }


    /**
     * Calculates the mapping informations by analyzing the mapping container
     * data.
     */
    private void calcMappingInformations() {

        Collection<ParsedMappingGroup> groups = mappings.values();
        Iterator<ParsedMappingGroup> groupsIt = groups.iterator();

        while( groupsIt.hasNext() ) {
            ParsedMappingGroup mappingGroup = groupsIt.next();
            List<ParsedMapping> mappingList = mappingGroup.getMappings();
            Iterator<ParsedMapping> mappingIt = mappingList.iterator();
            while( mappingIt.hasNext() ) {
                ParsedMapping mapping = mappingIt.next();
                if( mapping.isBestMapping() ) {
                    numberOfBM++;
                    if( mapping.getErrors() == 0 ) {
                        numberOfPerfect++;
                    }
                }
                numberOfMappings += mapping.getNumReplicates();
            }

            //calculate number of unique mappings (map only to one position, but have replicates)
            if( mappingList.size() == 1 ) {
                numUniqueMappings += mappingList.get( 0 ).getNumReplicates();
            }
            numReads += mappingList.get( 0 ).getNumReplicates();
        }

        mappingInfos.put( 1, numberOfMappings );
        mappingInfos.put( 2, numberOfPerfect );
        mappingInfos.put( 3, numberOfBM );
        mappingInfos.put( 4, numUniqueMappings );
        mappingInfos.put( 5, numUniqueSeq );
        mappingInfos.put( 6, numReads );
        mappingInfos.put( 7, sumReadLength );

        mappingInfosCalculated = true;
    }


    public void clear() {
        mappings.clear();
    }


    public boolean isLastMappingContainer() {
        return lastMappingContainer;
    }


    public void setLastMappingContainer( boolean lastMappingContainer ) {
        this.lastMappingContainer = lastMappingContainer;
    }


    public boolean isFirstMappingContainer() {
        return firstMappingContainer;
    }


    public void setFirstMappingContainer( boolean firstMappingContainer ) {
        this.firstMappingContainer = firstMappingContainer;
    }


    /**
     * @return the averageReadLength
     */
    public int getAverageReadLength() {
        return numOfMappings != 0 ? sumReadLength / numOfMappings : 0;
    }


    /**
     * @param sumReadLength the sumReadLength to set
     */
    public void setSumReadLength( int sumReadLength ) {
        this.sumReadLength = sumReadLength;
    }


    public int getSumReadLength() {
        return sumReadLength;
    }


}
