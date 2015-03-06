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


import de.cebitec.readxplorer.parser.reference.filter.FeatureFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Data holder for a chromosome.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParsedChromosome {

    public static final String FINISHED = "ParsingFinished";
    private List<ParsedFeature> features;
    private List<ParsedFeature> finalFeatures;
    private Map<String, Integer> featureIdMap;
    private FeatureFilter filter;
    private int id;
    private int featId;
    private String name;
    private boolean hasSubFeatures;
    private long chromLength;


    /**
     * Data holder for a chromosome.
     * <p>
     * @param name Name of the chromosome
     * @param chromLength
     * @param hasSubfeatures <tt>true</tt>, if it has subfeatures,
     * <tt>false</tt>, if it already relies on parent ids.
     */
    public ParsedChromosome( String name, long chromLength, boolean hasSubfeatures ) {
        this.features = new ArrayList<>();
        this.finalFeatures = new ArrayList<>();
        this.featureIdMap = new HashMap<>();
        this.filter = new FeatureFilter();
        this.hasSubFeatures = hasSubfeatures;
        this.name = name;
        this.chromLength = chromLength;
    }


    /**
     * Data holder for a chromosome.
     */
    public ParsedChromosome() {
        this( "", 0, true );
    }


    /**
     * Sets the unique id of this chromosome, which will be used in the db.
     * <p>
     * @param id The unique id of this chromosome, which will be used in the db.
     */
    public void setID( int id ) {
        this.id = id;
    }


    /**
     * @return The unique id of this chromosome, which will be used in the db.
     */
    public int getID() {
        return id;
    }


    /**
     * @return The name of this chromosome.
     */
    public String getName() {
        return name;
    }


    /**
     * @param name The name of this chromosome.
     */
    public void setName( String name ) {
        this.name = name;
    }


    /**
     * @return Length of the chromosome.
     */
    public long getChromLength() {
        return chromLength;
    }


    /**
     * @param chromLength Length of the chromosome.
     */
    public void setChromLength( long chromLength ) {
        this.chromLength = chromLength;
    }


    /**
     * @return The name of this chromosome.
     */
    @Override
    public String toString() {
        return this.getName();
    }


    /**
     * Adds a feature to this reference either in the root level feature list or
     * the sub feature list (features, which have a parent id).
     * <p>
     * @param feature The feature to add
     */
    public void addFeature( ParsedFeature feature ) {
        // only add valid features according to specified filterrules
        if( filter.isValidFeature( feature ) ) {
            features.add( feature );
        }
    }


    /**
     * Adds all features in the <tt>featureList</tt> to the list of features of
     * this reference.
     * <p>
     * @param featureList The list of features to add to the list of features of
     * this reference
     */
    public void addAllFeatures( List<ParsedFeature> featureList ) {
        for( ParsedFeature feature : featureList ) {
            if( filter.isValidFeature( feature ) ) {
                features.add( feature );
            }
        }
    }


    /**
     * @return The list of root level features.
     */
    public List<ParsedFeature> getFeatures() {
        return Collections.unmodifiableList( features );
    }


    /**
     * @return The list of subfeatures (features, which have a parent id).
     */
    public List<ParsedFeature> getSubFeatures() {
        return Collections.unmodifiableList( finalFeatures );
    }


    /**
     * @param featId The latest feature id used in the db. Set it before
     * distributing feature ids with {@link distributeFeatureIds()}.
     */
    public void setFeatId( int featId ) {
        this.featId = featId;
    }


    /**
     * Set the unique feature ids used in the db for each feature. Also stores a
     * mapping between feature identifier (name) and id, if the feature has an
     * identifier. Further replaces the parent names in the feature by the newly
     * assigned parent ids.
     */
    public void distributeFeatureIds() {
        Collections.sort( features ); //sort features by position
        distributeFeatureIds( features ); //this way only the feature ids of this reference features can be distributed!

        if( hasSubFeatures ) {
            this.mutateSubFeatureToParentIds( features );
            this.features = finalFeatures;
            finalFeatures = new ArrayList<>();
        } else {
            this.replaceParentNamesByIds();
        }
    }


    /**
     * Set the unique feature ids used in the db for each feature. Also stores a
     * mapping between feature identifier (name) and id, if the feature has an
     * identifier. Further replaces the parent names in the feature by the newly
     * assigned parent ids.
     * <p>
     * @param id The unique feature id to start with (will be increased by one
     * for each feature in the reference
     * @param featureList list of features for which the ids need to be
     * distributed
     */
    private void distributeFeatureIds( List<ParsedFeature> featureList ) {

        for( ParsedFeature feature : featureList ) {
            feature.setId( featId );
            if( feature.hasIdentifier() ) {
                featureIdMap.put( feature.getIdentifier(), featId );
            }
            ++featId;
            this.distributeFeatureIds( feature.getSubFeatures() );
        }
    }


    /**
     * Visits all features in the given list and sets their parentids from the
     * subfeature list contained within each feature. Also all features are
     * added to the finalFeatures list to guarantee the readiness for storing
     * the features in the db.
     * <p>
     * @param featureList The list of features whose parents have to be set
     */
    private void mutateSubFeatureToParentIds( List<ParsedFeature> featureList ) {
        List<String> parentIds;
        for( ParsedFeature feature : featureList ) {
            parentIds = new ArrayList<>();
            parentIds.add( String.valueOf( feature.getId() ) );
            finalFeatures.add( feature );
            for( ParsedFeature subFeature : feature.getSubFeatures() ) {
                subFeature.setParentIds( parentIds );
                finalFeatures.add( subFeature );
                this.mutateSubFeatureToParentIds( subFeature.getSubFeatures() );
            }
        }
    }


    /**
     * Set the unique feature ids used in the db for each feature. Also stores a
     * mapping between feature identifier (name) and id, if the feature has an
     * identifier. Further replaces the parent names in the feature by the newly
     * assigned parent ids.
     * <p>
     * @param id The unique feature id to start with (will be increased by one
     * for each feature in the reference
     * @param featureList list of features for which the ids need to be
     * distributed
     */
    private void replaceParentNamesByIds() {
        //replace parent names by parent ids
        List<String> newParentIds;
        for( ParsedFeature feature : this.features ) {
            newParentIds = new ArrayList<>();
            for( String parentName : feature.getParentIds() ) {
                if( featureIdMap.containsKey( parentName ) ) {
                    newParentIds.add( String.valueOf( featureIdMap.get( parentName ) ) );
                }
            }
            feature.setParentIds( newParentIds );
        }
    }


    /**
     * @return <tt>true</tt>, if it has subfeatures, <tt>false</tt>, if it
     * already relies on parent ids.
     */
    public boolean hasSubFeatures() {
        return hasSubFeatures;
    }


    /**
     * Set if the hierarchy of the featueres is based on subfeatures or parent
     * ids.
     * <p>
     * @param hasSubFeatures True, if it has subfeatures, false, if it already
     * relies on parent ids.
     */
    public void setHasSubFeatures( boolean hasSubFeatures ) {
        this.hasSubFeatures = hasSubFeatures;
    }


}
