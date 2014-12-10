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

package de.cebitec.readxplorer.databackend.dataObjects;


import de.cebitec.readxplorer.utils.ReadPairType;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.ArrayList;
import java.util.List;


/**
 * Holds all ReadPairs which belong to one read pair id.
 * Since a pair might have more than one mapping in the visible interval
 * and a pair id might not have an ordinary read pair, but several mappings
 * of both reads along the genome we need this data structure.
 *
 * @author Rolf Hilker
 */
public class ReadPairGroup implements ObjectWithId {

    private long readPairId;
    private final List<ReadPair> readPairs;
    private final List<Mapping> singleMappings;
//    private boolean hasNewRead; //set true when new read was added until this variable was send to the observers
//    private ArrayList<Observer> observers;
    private List<FeatureType> excludedFeatureTypes;


    public ReadPairGroup() {
//        observers = new ArrayList<Observer>();
        this.readPairs = new ArrayList<>();
        this.singleMappings = new ArrayList<>();
    }


    /**
     * Adds a new mapping to the group and creates a new ReadPair, if necessary.
     * <p>
     * @param mapping    the mapping to add to the group
     * @param type       type of the read pair this mapping is belonging to
     *                   (@see de.cebitec.readXplorer.util.Properties)
     * @param mapping1Id id of the first mapping of the read pair to create, or
     *                   -1 in case of a single mapping
     * @param mapping2Id id of the second mapping of the read pair to create, or
     *                   -1 in case of a single mapping
     * @param replicates number of replicates of the read pair to create, or -1
     *                   in case of a single mapping
     */
    public void addPersistentMapping( Mapping mapping, ReadPairType type, long mapping1Id, long mapping2Id, int replicates ) {

        boolean stored = false;
        if( type != ReadPairType.UNPAIRED_PAIR ) {
            for( ReadPair readPair : this.readPairs ) { //TODO: exponential!!! reduce complexity by hash or else...

                if( mapping.getId() == readPair.getVisibleMapping().getId()
                    || mapping.getId() == readPair.getMapping2Id() && readPair.hasVisibleMapping2() ) {

                    //second mapping of this read pair = second mappingid will deviate = create a new pair
                    this.readPairs.add( new ReadPair( this.readPairId, mapping1Id, mapping2Id, type, replicates, mapping ) );
                    stored = true;
                    break;

                }
                else if( mapping.getId() == readPair.getMapping2Id() ) {

                    // pair already exists, this is the second mapping of that pair = add it
                    readPair.setVisiblemapping2( mapping );
                    stored = true;
                    break;
                }
            }
            if( !stored ) {
                // this mapping defines a new read pair for this pair id
                this.readPairs.add( new ReadPair( this.readPairId, mapping1Id, mapping2Id, type, replicates, mapping ) );
            }
        }
        else {
            //this is a single mapping, just add id to the list
            this.singleMappings.add( mapping );
        }

//            this.hasNewRead = true;
//            this.notifyObservers();
    }


    /**
     * Adds a new direct access mapping to the group and creates a new
     * ReadPair, if necessary.
     * <p>
     * @param mapping     the mapping to add to the group
     * @param mate
     * @param type        type of the read pair this mapping is belonging to (
     * @param bothVisible true, if both mappings of the pair are visible
     * <p>
     * @see de.cebitec.readxplorer.utils.Properties     */
    public void addPersistentDirectAccessMapping( Mapping mapping, Mapping mate, ReadPairType type, boolean bothVisible ) {

        boolean stored = false;
        if( type != ReadPairType.UNPAIRED_PAIR ) {
            for( ReadPair readPair : this.readPairs ) {

                if( readPair.getVisibleMapping().getStart() == mate.getStart()
                    && readPair.getVisibleMapping2().getStart() == mapping.getStart()
                    && readPair.getVisibleMapping().isFwdStrand() == mate.isFwdStrand()
                    && readPair.getVisibleMapping2().isFwdStrand() == mapping.isFwdStrand() ) {
                    readPair.setVisiblemapping2( mapping );
                    stored = true;
                    break;
                }
            }
            if( !stored ) {
                // this mapping defines a new read pair for this pair id
                this.readPairs.add( new ReadPair( this.readPairId, mapping.getId(), -1, type, 1, mapping, mate ) );
            }
        }
        else {
            //this is a single mapping, just add id to the list
            this.singleMappings.add( mapping );
        }

//            this.hasNewRead = true;
//            this.notifyObservers();
    }


    public void setReadPairId( long readPairId ) {
        this.readPairId = readPairId;
    }


    /**
     * @return List of all read pairs belonging to this pair id.
     */
    public List<ReadPair> getReadPairs() {
        return this.readPairs;
    }


    /**
     * @return List of all single mappings belonging to this pair id.
     */
    public List<Mapping> getSingleMappings() {
        return this.singleMappings;
    }

//    @Override
//    public void registerObserver(Observer observer) {
//        this.observers.add(observer);
//    }
//
//    @Override
//    public void removeObserver(Observer observer) {
//        this.observers.remove(observer);
//    }
//
//    @Override
//    public void notifyObservers() {
//        for (Observer observer : observers) {
//            observer.update(this.hasNewRead);
//        }
//        this.hasNewRead = false;
//    }

    public void setExcludedFeatureTypes( List<FeatureType> excludedFeatureTypes ) {
        this.excludedFeatureTypes = excludedFeatureTypes;
    }


    @Override
    public long getId() {
        return this.readPairId;
    }


}
