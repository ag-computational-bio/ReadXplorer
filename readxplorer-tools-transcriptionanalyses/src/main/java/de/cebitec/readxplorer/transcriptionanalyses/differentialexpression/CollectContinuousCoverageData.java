/*
 * Copyright (C) 2015 AM
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import de.cebitec.readxplorer.databackend.ParametersFeatureTypes;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.MappingResult;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Collect the coverage data for a given track.
 * <p>
 * @author AM
 */
public class CollectContinuousCoverageData implements Observer{

  /**
     * The whole set of features for the current genome.
     */
    private final List<PersistentFeature> genomeFeatures;
    /**
     * The storage holding the collected coverage data, also named count data.
     * The Key value of this HashMap is the ID of the feature. The value value
     * represents the corresponding number of counted coverage data.
     */
    private final Map<PersistentFeature, int[]> continuousCountData = new HashMap<>();
    /**
     * Adjusts how many bases downstream from the start position of a feature a
     * mapping should still be considered a hit. The features in the database
     * are sometimes CDS positions. So it is normal that a lot of mappings will
     * start in an are downstream of the start position of the feature.
     */
    private final int startOffset;
    /**
     * Adjusts how many bases upstream from the stop position of a feature a
     * mapping should still be considered a hit. The features in the database
     * are sometimes CDS positions. So it is normales that some mappings are not
     * located exactly indside the feature positions.
     */
    private final int stopOffset;
    private final ParametersReadClasses readClassParams;


    /**
     * Constructor of the class.
     * <p>
     * @param trackID      The ID of the track the instance of this class should
     *                     collect the coverage data for
     * @param perfAnalysis Instance of the calling instance of
     *                     DeAnalysisHandler.
     */
    public CollectContinuousCoverageData( List<PersistentFeature> genomeFeatures, int startOffset, int stopOffset, ParametersReadClasses readClassParams ) {
        this.genomeFeatures = genomeFeatures;
        this.startOffset = startOffset;
        this.stopOffset = stopOffset;
        this.readClassParams = readClassParams;
        Collections.sort( genomeFeatures );
    }


    /**
     * Updates the read count for the features with the given mappings.
     * <p>
     * @param mappings the mappings
     */
    private void updateReadCountForFeatures( MappingResult result ) {
        List<Mapping> mappings = new ArrayList<>( result.getMappings() );
        Collections.sort( mappings );
        int lastMappingIdx = 0;
        boolean isStrandBothOption = readClassParams.isStrandBothOption();
        boolean isFeatureStrand = readClassParams.isStrandFeatureOption();

        for( PersistentFeature feature : genomeFeatures ) {

            if( feature.getChromId() == result.getRequest().getChromId() ) {

                ParametersFeatureTypes featTypeParams = new ParametersFeatureTypes( EnumSet.allOf( FeatureType.class ),
                    startOffset, stopOffset );
                int featStart = featTypeParams.calcFeatureStartOffset( feature );
                int featStop = featTypeParams.calcFeatureStopOffset( feature );
                boolean analysisStrand = isFeatureStrand ? feature.isFwdStrand() :
                    !feature.isFwdStrand(); //only use this if Properties.STRAND_BOTH is not selected
                boolean fstFittingMapping = true;
                //If no matching mapping is found, we still need to know that by
                //writing down a count of zero for this feature.
                int[] featurePositions = new int[featStop-featStart];
                if( !continuousCountData.containsKey( feature ) ) {                                    
                    Arrays.fill(featurePositions, 0);
                    continuousCountData.put( feature,  featurePositions);
                }
                for( int j = lastMappingIdx; j < mappings.size(); j++ ) {
                    Mapping mapping = mappings.get( j );
                    //If the orientation of the read does not matter this one is always true.
                    //mappings identified within a feature
                    if( mapping.getStop() >= featStart && mapping.getStart() <= featStop ) {

                        if( fstFittingMapping ) {
                            lastMappingIdx = j;
                            fstFittingMapping = false;
                        }
                        if( isStrandBothOption || analysisStrand == mapping.isFwdStrand() ) {
                            featurePositions = continuousCountData.get( feature );
                            for( int i = mapping.getStart()-feature.getStart();
                               ((i <= mapping.getStop()-feature.getStart()) && 
                                (i < featurePositions.length)); i++ ) {
                                if(i<0){
                                    i = 0;
                                }
                                featurePositions[i]++;
                            }
                            continuousCountData.put( feature, featurePositions );
                        }
                        //still mappings left, but need next feature
                    } else if( mapping.getStart() > featStop ) {
                        break;
                    }
                }
            }
        }
    }


    @Override
    public void update( Object args ) {
        if( args instanceof MappingResult ) {
            MappingResult result = (MappingResult) args;
            updateReadCountForFeatures( result );
        }
    }


    public Map<PersistentFeature, int[]> getContinuousCountData() {
        return Collections.unmodifiableMap( continuousCountData );
    }
    
}
