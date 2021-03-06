/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.datastructures;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * An assigned mapping implementation. It is a mapping knowing to which genomic
 * features it has been added to determine their read count. Based on a union
 * and fraction model.
 * <br>Features have to be added in sorted order from the first to the last
 * position of a reference sequence. Since features on the reverse strand appear
 * in the opposite order, the class also offers methods to remove assigned
 * features from the reverse strand again.
 * <br>Model explanation:
 * <br>Mapping: ____(--------)________
 * <br>CDS1: ___[---------------]_____
 * <br>CDS1 is added
 * <br>CDS2: ____[--------------]_____
 * <br>CDS2 is added -> fraction is calculated later: RC - 1/2
 * <br>CDS3: ___________[-----------]_
 * <br>CDS3 is not added
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class UnionFractionMapping extends AssignedMapping {


    /**
     * An assigned mapping implementation. It is a mapping knowing to which
     * genomic features it has been added to determine their read count. Based
     * on a union and fraction model.
     * <br>Features have to be added in sorted order from the first to the last
     * position of a reference sequence. Since features on the reverse strand
     * appear in the opposite order, the class also offers methods to remove
     * assigned features from the reverse strand again.
     * <br>Model explanation:
     * <br>Mapping: ____(--------)________
     * <br>CDS1: ___[---------------]_____
     * <br>CDS1 is added
     * <br>CDS2: ____[--------------]_____
     * <br>CDS2 is added -> fraction is calculated later: RC - 1/2
     * <br>CDS3: ___________[-----------]_
     * <br>CDS3 is not added
     * <p>
     * @param mapping The mapping to which features are assigned
     */
    public UnionFractionMapping( Mapping mapping ) {
        super( mapping );
    }


    /**
     * Checks if the mapping should be counted for the given feature. Mainly
     * important when a mapping overlaps multiple genomic features of the same
     * type.
     * <br>This implementation adds the feature to this mappings list of
     * assigned features if it is the first feature, if the mapping is
     * completely contained in multiple features (then the features are added to
     * the fraction map), if it is a feature from the reverse strand. For
     * reverse strand features the remove list receives the previously added
     * feature(s), if the current feature is the only feature containing the
     * whole mapping (not just overlapping it).
     * <p>
     * @param featStart          The start of the feature, always smaller than
     *                           featStop
     * @param featStop           The stop of the feature, always larger than
     *                           featStart
     * @param feature            The feature to check
     * <p>
     * @param isStrandBothOption <code>true</code> if both strands are counted
     *                           for a feature, <code>false</code> otherwise.
     *
     * @return <code>true</code> if the mapping should be counted for the
     *         current feature, <code>false</code> otherwise.
     */
    @Override
    public boolean checkAssignment( int featStart, int featStop, PersistentFeature feature, boolean isStrandBothOption ) {
        boolean countIt = true;
        List<PersistentFeature> removeList = new ArrayList<>();
        if( !assignedFeatures.isEmpty() ) {
            //fwd strand: only do not count when encounter second feature of same type which is not totally covering the mapping
            //rev strand: only do not count when encounter second feature of same type where first feature is not totally covering the mapping
            //if two features of the same type start at the same position, then the fraction of the mapping is counted for both
            for( PersistentFeature assignedFeature : assignedFeatures ) {
                if( assignedFeature.getType().equals( feature.getType() ) ) {

                    if( !isStrandBothOption ) {
                        if( feature.isFwdStrand() ) {
                            if( getMapping().getStart() < featStart && featStart != assignedFeature.getStart() ) {
                                countIt = false;
                            } else {
                                addToFractionMap( feature, assignedFeature );
                            }
                        } else if( !feature.isFwdStrand() ) {  //since they arrive in sorted order!
                            if( getMapping().getStop() > assignedFeature.getStop() && feature.getStop() != assignedFeature.getStop() ) {
                                //means delete read count of other feature!
                                removeList.add( assignedFeature );
                                //the remove list can be retrieved via getter -> then analysis decreases rc
                            } else {
                                addToFractionMap( feature, assignedFeature );
                            }
                        }

                        //combine strands option is on and features on opposite strands
                    } else if( feature.isFwdStrand() != assignedFeature.isFwdStrand() ) {
                        if( assignedFeatures.size() == 1 ) { //means only the two compared features are assigned until now
                            addToFractionMap( feature, assignedFeature );
                        }

                    } else //combine strands option is on and features on same strand
                    //Not counted for current (SECOND) fwd feature when:
                    //Mapping:  ______(-------)________
                    //CDS1 fwd: ___[--------]_____
                    //CDS2 fwd: _________[---------]_____
                    //Not counted for current (FIRST) rev feature when:
                    //Mapping:  ______(-------)________
                    //CDS1 rev: ___[--------]_____
                    //CDS2 rev: _________[---------]_____   
                    if( feature.isFwdStrand() && getMapping().getStart() < featStart && featStart != assignedFeature.getStart() ) {
                        countIt = false;
                    } else if( !feature.isFwdStrand() && getMapping().getStop() > assignedFeature.getStop() && feature.getStop() != assignedFeature.getStop() ) {
                        //means delete read count of other feature!
                        removeList.add( assignedFeature );
                        replaceInFractionMap( assignedFeature, feature );
                        //the remove list can be retrieved via getter -> then analysis decreases rc
                    } else {
                        addToFractionMap( feature, assignedFeature );
                    }
                }
            }
        }

        if( countIt ) {
            assignedFeatures.add( feature );
        }
        if( removeList.size() > 0 ) {
            revRemoveList = removeList;
            for( PersistentFeature removeFeat : removeList ) {
                assignedFeatures.remove( removeFeat );
            }
        }
        return countIt;
    }


    /**
     * The map is created if it does not exist and the feature type key is
     * created, if it does not exist yet.
     *
     * @param type Ensures that the feature type exists in the map
     */
    private void initFractionMap( FeatureType type ) {
        if( fractionMap == null ) {
            fractionMap = new HashMap<>();
        }
        if( !fractionMap.containsKey( type ) ) {
            fractionMap.put( type, new HashSet<>() );
        }
    }


    /**
     * Adds both featues to the fraction map stored in this object. Makes sure
     * that the fraction map is initialized with the needed feature key.
     * <p>
     * @param feature         The new feature to add to the fraction map
     * @param assignedFeature The feature from the asssignedFeatures map to add
     *                        to the fraction map
     */
    private void addToFractionMap( PersistentFeature feature, PersistentFeature assignedFeature ) {
        initFractionMap( feature.getType() );
        fractionMap.get( feature.getType() ).add( assignedFeature );
        fractionMap.get( feature.getType() ).add( feature );
    }


    /**
     * Replace the already assigned feature by the new feature in the fraction
     * map. Makes sure that the fraction map is initialized with the needed
     * feature key.
     *
     * @param assignedFeature The feature from the asssignedFeatures map to add
     *                        to the fraction map
     * @param feature         The new feature to add to the fraction map
     */
    private void replaceInFractionMap( PersistentFeature assignedFeature, PersistentFeature feature ) {
        initFractionMap( feature.getType() );
        fractionMap.get( assignedFeature.getType() ).remove( assignedFeature ); //also delete from fraction map
        fractionMap.get( feature.getType() ).add( feature );
    }


    /**
     * Decreases the read count of all features contained in the fraction map of
     * this object for a certain feature type key by its fraction (1 / number of
     * features in the list).
     * <p>
     * @param featureReadCount A map of feature id to normalized read count in
     *                         which the read count should be adapted according
     *                         to the fractions
     */
    @Override
    public void fractionAssignmentCheck( Map<Integer, NormalizedReadCount> featureReadCount ) {
        if( fractionMap != null ) {
            for( Map.Entry<FeatureType, Set<PersistentFeature>> entry : fractionMap.entrySet() ) {
                Set<PersistentFeature> fractionList = entry.getValue();
                double fractionSum = 1.0 - (1.0 / fractionList.size());
                for( PersistentFeature feature : fractionList ) {
                    NormalizedReadCount decreaseCount = featureReadCount.get( feature.getId() );
                    decreaseCount.setReadCount( decreaseCount.getReadCount() - fractionSum );
                }
            }
        }
    }


}
