/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * An assigned mapping is a mapping knowing to which genomic features it has
 * been added to determine their read count. Features have to be added in sorted
 * order from the first to the last position of a reference sequence. Since
 * features on the reverse strand appear in the opposite order, the class also
 * offers methods to remove assigned features from the reverse strand again.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public abstract class AssignedMapping implements MappingAssignment {

    private final Mapping mapping;
    Set<PersistentFeature> assignedFeatures;
    List<PersistentFeature> revRemoveList;
    Map<FeatureType, Set<PersistentFeature>> fractionMap;


    /**
     * An assigned mapping is a mapping knowing to which genomic features it has
     * been added to determine their read count. Features have to be added in
     * sorted order from the first to the last position of a reference sequence.
     * Since features on the reverse strand appear in the opposite order, the
     * class also offers methods to remove assigned features from the reverse
     * strand again.
     * @param mapping The mapping for which features shall be assigned
     */
    public AssignedMapping( Mapping mapping ) {
        this.mapping = mapping;
        assignedFeatures = new HashSet<>();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean checkAssignment( int featStart, int featStop, PersistentFeature feature, boolean isStrandBothOption );


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void fractionAssignmentCheck( Map<Integer, NormalizedReadCount> featureReadCount );


    /**
     * @return The set of assigned features to which this mapping has to be
     * counted.
     */
    public Set<PersistentFeature> getAssignedFeatures() {
        return Collections.unmodifiableSet( assignedFeatures );
    }


    /**
     * @return The mapping which is assigned to features here.
     */
    public Mapping getMapping() {
        return mapping;
    }


    /**
     * @return <code>true</code> if the remove features list contains features
     *         to remove, <code>false</code> otherwise.
     */
    public boolean isRemoveFeatures() {
        return revRemoveList != null;
    }


    /**
     * @return The list of reverse strand features to remove from this mapping.
     * May be <code>null</code>!
     */
    public List<PersistentFeature> getRevRemoveList() {
        if ( revRemoveList != null ) {
            return Collections.unmodifiableList( revRemoveList );
        }
        return null;
    }


    /**
     * When the remove reverse features list has been treated accordingly, this
     * method should be invoced to set the list in this AssignedMapping to null.
     */
    public void notifyRemovedFeatures() {
        revRemoveList = null;
    }


    /**
     * Checks if the read count has to be decreased for any features. It has to
     * be decreased for all features in the reverse remove list. By default,
     * the list is null. If a feature has been removed from the association to
     * the mapping, its read count is decreased by 1 and its read length sum is
     * decreased by the length of this mapping.
     * @param featureReadCount The map containing the read counts for each
     * genomic feature id.
     */
    public void checkCountDecrease( Map<Integer, NormalizedReadCount> featureReadCount ) {
        if( isRemoveFeatures() ) {
            for( PersistentFeature removeFeat : getRevRemoveList() ) {
                NormalizedReadCount decreaseCount = featureReadCount.get( removeFeat.getId() );
                decreaseCount.setReadCount( decreaseCount.getReadCount() - 1 );
                decreaseCount.setReadLengthSum( decreaseCount.getReadLengthSum() - mapping.getLength() );
            }
            notifyRemovedFeatures();
        }
    }


}
