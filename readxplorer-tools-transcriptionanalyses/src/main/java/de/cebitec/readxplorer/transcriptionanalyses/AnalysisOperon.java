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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.api.objects.AnalysisI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataObjects.Mapping;
import de.cebitec.readxplorer.databackend.dataObjects.MappingResult;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.dataStructures.Operon;
import de.cebitec.readxplorer.transcriptionanalyses.dataStructures.OperonAdjacency;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Carries out the analysis of a data set for operons.
 *
 * @author MKD, rhilker
 */
public class AnalysisOperon implements Observer, AnalysisI<List<Operon>> {

    private final TrackConnector trackConnector;
    private final List<Operon> operonList; //final result list of OperonAdjacencies
    private final Map<Integer, OperonAdjacency> featureToPutativeOperonMap; //feature id of mappings to count for features
    private final List<OperonAdjacency> operonAdjacencies;
    private int averageReadLength = 0;
//    private int averageReadPairLength = 0;
    private int lastMappingIdx;
    private int readsFeature1 = 0;
    private int spanningReads = 0;
    private int readsFeature2 = 0;
    private int internalReads = 0;
    private final ParameterSetOperonDet operonDetParameters;
    private ReferenceConnector refConnector;


    /**
     * Carries out the analysis of a data set for operons.
     * <p>
     * @param trackConnector      the trackConnector whose data is to be
     *                            analyzed
     * @param operonDetParameters contains the parameters to use for the operon
     *                            detection
     */
    public AnalysisOperon( TrackConnector trackConnector, ParameterSetOperonDet operonDetParameters ) {
        this.trackConnector = trackConnector;
        this.operonDetParameters = operonDetParameters;
        this.operonList = new ArrayList<>();
        this.featureToPutativeOperonMap = new HashMap<>();
        this.operonAdjacencies = new ArrayList<>();

        this.initDatastructures();
    }


    /**
     * Initializes the initial data structures needed for an operon detection
     * analysis. This includes the detection of all neighboring features before
     * the actual analysis.
     */
    private void initDatastructures() {
        Map<String, Integer> statsMap = trackConnector.getTrackStats().getStatsMap();
        averageReadLength = statsMap.get( StatsContainer.AVERAGE_READ_LENGTH );
//        averageReadPairLength = statsMap.get( StatsContainer.AVERAGE_READ_PAIR_SIZE );
        refConnector = ProjectConnector.getInstance().getRefGenomeConnector( trackConnector.getRefGenome().getId() );

        for( PersistentChromosome chrom : refConnector.getChromosomesForGenome().values() ) {

            List<PersistentFeature> chromFeatures = refConnector.getFeaturesForClosedInterval( 0, chrom.getLength(), chrom.getId() );

            ////////////////////////////////////////////////////////////////////////////
            // detecting all neighboring features which are not overlapping more than 20bp and
            // have a distance smaller than 1000 bp from stop of 1 to start of 2 as putative operons
            ////////////////////////////////////////////////////////////////////////////

            for( int i = 0; i < chromFeatures.size() - 1; i++ ) {

                PersistentFeature feature1 = chromFeatures.get( i );
                boolean reachedEnd = false;

                if( operonDetParameters.getSelFeatureTypes().contains( feature1.getType() ) ) {

                    int featureIndex = i + 1; //find feature 2
                    while( feature1.isFwdStrand() != chromFeatures.get( featureIndex ).isFwdStrand()
                           || !feature1.getType().equals( chromFeatures.get( featureIndex ).getType() ) ) {
                        if( featureIndex < chromFeatures.size() - 1 ) {
                            featureIndex++;
                        }
                        else {
                            reachedEnd = true;
                            break;
                        }
                    }

                    if( !reachedEnd ) {
                        PersistentFeature feature2 = chromFeatures.get( featureIndex );

                        if( feature2.getStart() + 20 > feature1.getStop() && //features may overlap at the ends, happens quite often
                                feature2.getStart() - feature1.getStop() < 1000 ) { //TODO: parameter for this
                            this.featureToPutativeOperonMap.put( feature1.getId(), new OperonAdjacency( feature1, feature2, chrom.getId() ) );
                        }
                    }
                }
            }
        }
    }


    /**
     * Sums the read counts for a new list of mappings or calls the finish
     * method.
     * <p>
     * @param data the data to handle: Either a list of mappings or "2" =
     *             mapping querries are done.
     */
    @Override
    public void update( Object data ) {
        //the mappings are sorted by their start position!
        MappingResult mappingResult = new MappingResult( null, null );
        if( data.getClass() == mappingResult.getClass() ) {

            MappingResult mappings = ((MappingResult) data);
            this.sumReadCounts( mappings );
        }
        if( data instanceof Byte && ((Byte) data) == 2 ) {
            this.finish();
        }
    }


    /**
     * Method to be called when the analysis is finished. Starts the detection
     * of
     * operons after the read counts for all mappings have been stored.
     */
    public void finish() {
        Date currentTimestamp = new java.sql.Timestamp( Calendar.getInstance().getTime().getTime() );
        Logger.getLogger( this.getClass().getName() ).log( Level.INFO, "{0}: Detecting operons", currentTimestamp );
        this.findOperons();
    }


    /**
     * Sums up the read counts for the features the mappings are located in.
     * <p>
     * @param mappingResult the result containing the mappings to be
     *                      investigated
     */
    public void sumReadCounts( MappingResult mappingResult ) {

        final List<Mapping> mappings = mappingResult.getMappings();
        final int noMappings = mappings.size();
        PersistentChromosome chrom = refConnector.getChromosomeForGenome( mappingResult.getRequest().getChromId() );
        final boolean isStrandBothOption = operonDetParameters.getReadClassParams().isStrandBothOption();
        final boolean isFeatureStrand = operonDetParameters.getReadClassParams().isStrandFeatureOption();

        List<PersistentFeature> chromFeatures = refConnector.getFeaturesForClosedInterval( 0, chrom.getLength(), chrom.getId() );
        for( PersistentFeature feature1 : chromFeatures ) {
            int id1 = feature1.getId();
            boolean analysisStrand = isFeatureStrand ? feature1.isFwdStrand() : !feature1.isFwdStrand();
            boolean fstFittingMapping = true;

            //we can already neglect all features not forming a putative operon
            if( this.featureToPutativeOperonMap.containsKey( id1 ) ) {
                PersistentFeature feature2 = this.featureToPutativeOperonMap.get( id1 ).getFeature2();

                this.readsFeature1 = 0;
                this.readsFeature2 = 0;
                this.spanningReads = 0;
                this.internalReads = 0;

                int feature1Stop = feature1.getStop();
                int feature2Start = feature2.getStart();
                int feature2Stop = feature2.getStop();

                for( int j = this.lastMappingIdx; j < noMappings; j++ ) {
                    Mapping mapping = mappings.get( j );

                    if( mapping.getStart() > feature2Stop ) {
                        break; //since the mappings are sorted by start position
                    }
                    else if( !isStrandBothOption && (mapping.isFwdStrand() != analysisStrand || mapping.getStop() < feature1Stop) ) {
                        continue;
                    }

                    //mappings identified between both features
                    if( mapping.getStart() <= feature1Stop && mapping.getStop() > feature1Stop && mapping.getStop() < feature2Start ) {
                        readsFeature1++;
                    }
                    else if( mapping.getStart() > feature1Stop && mapping.getStart() < feature2Start && mapping.getStop() >= feature2Start ) {
                        readsFeature2++;
                    }
                    else if( mapping.getStart() <= feature1Stop && mapping.getStop() >= feature2Start ) {
                        spanningReads++;
                    }
                    else if( mapping.getStart() > feature1Stop && mapping.getStop() < feature2Start ) {
                        internalReads++;
                    }

                    if( fstFittingMapping ) { //TODO: either add to each if clause above or add surrounding if clause!
                        this.lastMappingIdx = j;
                        fstFittingMapping = false;
                    }
                }

                OperonAdjacency putativeOperon = featureToPutativeOperonMap.get( id1 );
                putativeOperon.setReadsFeature1( putativeOperon.getReadsFeature1() + readsFeature1 );
                putativeOperon.setReadsFeature2( putativeOperon.getReadsFeature2() + readsFeature2 );
                putativeOperon.setSpanningReads( putativeOperon.getSpanningReads() + spanningReads );
                putativeOperon.setInternalReads( putativeOperon.getInternalReads() + internalReads );
            }
        }
        this.lastMappingIdx = 0;
    }


    /**
     * Method for identifying operons after all read counts were summed up for
     * each
     * genome feature.
     */
    public void findOperons() {
        Integer[] featureIds = featureToPutativeOperonMap.keySet().toArray( new Integer[featureToPutativeOperonMap.size()] );
        Arrays.sort( featureIds );

        /*
         * If we have read pairs, we calculate the average read pair length
         * and if we only have single reads, we use the average read length.
         */
        //TODO: incorporate read pair handling in the detection. currently only reads are used
        int minimumSpanningReads;
//        if (trackCon.getNumOfSeqPairs() > 0 && operonDetectionAutomatic) {
//            minimumSpanningReads = (numUniqueBmMappings * averageSeqPairLength) / transcritomeLength;
//        } else if (operonDetectionAutomatic) {
//            minimumSpanningReads = (numUniqueBmMappings * averageReadLength) / transcritomeLength;
//        } else {
        minimumSpanningReads = operonDetParameters.getMinSpanningReads();
//        }
//        System.out.println("Threshold: " + minimumSpanningReads + ", = uniqBMM = " + numUniqueBmMappings + ", avReadLength = "
//                + averageReadLength + ", transcriptome length = " + transcritomeLength + "Result: " + numUniqueBmMappings * 280 / transcritomeLength);

        int count = 0;
        int lastAnnoId = 0;
        for( Integer featureId : featureIds ) {
            OperonAdjacency putativeOperon = featureToPutativeOperonMap.get( featureId );
            spanningReads = putativeOperon.getSpanningReads();
            internalReads = putativeOperon.getInternalReads();
            PersistentFeature feature1 = putativeOperon.getFeature1();
            PersistentFeature feature2 = putativeOperon.getFeature2();
            /* Detect an operon only, if the number of spanning reads is larger than
             * the threshold. */
            if( spanningReads >= minimumSpanningReads ) {

                //only in this case a new operon starts:
                if( lastAnnoId != feature1.getId() && lastAnnoId != 0 ) {

                    Operon operon = new Operon( trackConnector.getTrackID() );
                    operon.addAllOperonAdjacencies( operonAdjacencies );
                    operonList.add( operon ); //only here the operons are added to final list
                    operonAdjacencies.clear();
                }
                operonAdjacencies.add( putativeOperon );
                lastAnnoId = feature2.getId();

                // TODO: check if parameter ok or new parameter
            }
            else if( feature2.getStart() - feature1.getStop() > averageReadLength
                     && internalReads > operonDetParameters.getMinSpanningReads() ) {
                //TODO: think about creating an operon
                System.out.println( "found case " + ++count );
            }
        }
        if( !operonAdjacencies.isEmpty() ) {
            Operon operon = new Operon( trackConnector.getTrackID() );
            operon.addAllOperonAdjacencies( operonAdjacencies );
            operonList.add( operon ); //only here the operons are added to final list
        }
    }


    @Override
    public List<Operon> getResults() {
        return this.operonList;

    }


}
