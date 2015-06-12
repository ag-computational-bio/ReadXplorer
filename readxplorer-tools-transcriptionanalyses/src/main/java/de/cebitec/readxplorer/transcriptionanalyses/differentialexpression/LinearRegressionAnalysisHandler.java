/*
 * Copyright (C) 2015 Agne Matuseviciute
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

import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.enums.IntervalRequestData;
import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import org.apache.commons.collections4.map.MultiValueMap;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import static java.util.logging.Level.INFO;


/**
 *
 * @author Agne Matuseviciute
 */
public class LinearRegressionAnalysisHandler extends DeAnalysisHandler {

    private static final Logger LOG = Logger.getLogger( LinearRegressionAnalysisHandler.class.getName() );
    private final int refGenomeID;
    private final int startOffset;
    private final int stopOffset;
    private final int[] groupAindex;
    private final int[] groupBindex;
    private final int[] groupA;
    private final int[] groupB;
    private LinearRegression linRegForConditions;
    private LinearRegression linRegForReplicates;
    private File saveFile = null;
    private final ParametersReadClasses readClassParams;
    private final List<PersistentTrack> selectedTracks;
    private final List<de.cebitec.readxplorer.utils.Observer> observerList = new ArrayList<>();

    private int resultsReceivedBack = 0;
    private ReferenceConnector referenceConnector;
    private final List<PersistentFeature> genomeAnnos;
    private final Set<FeatureType> selectedFeatureTypes;
    private final Map<Integer, Map<PersistentFeature, int[]>> allContinuousCoverageData;
    private List<ResultDeAnalysis> results;
    Integer indexForConditionA = 0;
    Integer indexForConditionB = 1;
    boolean workingWithoutReplicates;


    /**
     * Constructor of the class.
     */
    public LinearRegressionAnalysisHandler( List<PersistentTrack> selectedTracks, int[] groupAindex, int[] groupBindex,
                                            int refGenomeID, boolean workingWithoutReplicates, File saveFile, Set<FeatureType> selectedFeatureTypes,
                                            int startOffset, int stopOffset, ParametersReadClasses readClassParams, ProcessingLog log ) {

        super( selectedTracks, refGenomeID, saveFile, selectedFeatureTypes, startOffset, stopOffset, readClassParams, log );
        this.selectedTracks = selectedTracks;
        this.refGenomeID = refGenomeID;
        this.saveFile = saveFile;
        this.startOffset = startOffset;
        this.stopOffset = stopOffset;
        this.groupAindex = groupAindex;
        this.groupBindex = groupBindex;
        this.groupA = new int[groupAindex.length];
        this.groupB = new int[groupBindex.length];
        this.workingWithoutReplicates = workingWithoutReplicates;
        this.readClassParams = readClassParams;
        this.selectedFeatureTypes = selectedFeatureTypes;
        genomeAnnos = new ArrayList<>();
        allContinuousCoverageData = new HashMap<>();
        results = new ArrayList<>();
        linRegForConditions = new LinearRegression( log );
        linRegForReplicates = new LinearRegression( log );
    }


    /**
     * Starts the analysis and collects data for all selected tracks.
     */
    @Override
    public void startAnalysis() {
        allContinuousCoverageData.clear();
        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.log( INFO, "{0}: Starting to collect the necessary data for the differential gene expression analysis.",
                 currentTimestamp );
        referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector( refGenomeID );
        List<AnalysesHandler> allHandler = new ArrayList<>();
        genomeAnnos.clear();

        for( PersistentChromosome chrom : referenceConnector.getRefGenome().getChromosomes().values() ) {
            genomeAnnos.addAll( referenceConnector.getFeaturesForRegionInclParents( 1, chrom.getLength(),
                                                                                    selectedFeatureTypes, chrom.getId() ) );
        }

        for( PersistentTrack currentTrack : selectedTracks ) {
            try {
                TrackConnector tc = (new SaveFileFetcherForGUI()).getTrackConnector( currentTrack );

                CollectContinuousCoverageData collCovData = new CollectContinuousCoverageData(
                        genomeAnnos, startOffset, stopOffset, readClassParams );
                allContinuousCoverageData.put( currentTrack.getId(), collCovData.getContinuousCountData() );
                AnalysesHandler handler = new AnalysesHandler( tc, (DataVisualisationI) this,
                                                               "Collecting coverage data for track " + currentTrack.getDescription() + ".", readClassParams );
                handler.setMappingsNeeded( true );
                handler.setDesiredData( IntervalRequestData.ReducedMappings );
                handler.registerObserver( collCovData );
                allHandler.add( handler );
            } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                getProcessingLog().addProperty( "Unresolved track", currentTrack );
                notifyObservers( DeAnalysisHandler.AnalysisStatus.ERROR );
                this.interrupt();
                return;
            }
        }
        for( AnalysesHandler handler : allHandler ) {
            handler.startAnalysis();
        }
    }


    /**
     * Determines which tracks belong to which condition.
     */
    private void determineGroupsForConditions() {
        int idxA = 0;
        for( int i : groupAindex ) {
            groupA[idxA] = selectedTracks.get( i - 1 ).getId();
            idxA++;
        }

        int idxB = 0;
        for( int i : groupBindex ) {
            groupB[idxB] = selectedTracks.get( i - 1 ).getId();
            idxB++;
        }
    }


    /**
     * Determines in which of two possible conditions belongs collected data.
     * <p>
     * @param allData Raw data as it was collected, with trackID as key.
     */
    private Map<Integer, MultiValueMap<PersistentFeature, int[]>> sortDataForConditions(
            Map<Integer, Map<PersistentFeature, int[]>> allData ) {
        Map<Integer, MultiValueMap<PersistentFeature, int[]>> preparedDataForConditions = new HashMap<>();
        MultiValueMap<PersistentFeature, int[]> innerMultiMapA;
        MultiValueMap<PersistentFeature, int[]> innerMultiMapB;

        innerMultiMapA = formInnerMultiMap( allData, groupA );
        innerMultiMapB = formInnerMultiMap( allData, groupB );
        preparedDataForConditions.put( indexForConditionA, innerMultiMapA );
        preparedDataForConditions.put( indexForConditionB, innerMultiMapB );
        return preparedDataForConditions;
    }


    /**
     * Prepares inner Map for condition, consisting of gene names and count
     * data.
     * <p>
     * @param allData        data with trackID as key.
     * <p>
     * @param conditionGroup array with trackIDs for the same condition.
     */
    private MultiValueMap<PersistentFeature, int[]> formInnerMultiMap(
            Map<Integer, Map<PersistentFeature, int[]>> allData, int[] conditionGroup ) {
        MultiValueMap<PersistentFeature, int[]> innerMultiMap = new MultiValueMap<>();

        for( int track : conditionGroup ) {
            Map<PersistentFeature, int[]> innerMap = allData.get( track );
            for( Map.Entry<PersistentFeature, int[]> gene
                 : innerMap.entrySet() ) {
                innerMultiMap.put( gene.getKey(), gene.getValue() );
            }
        }
        return innerMultiMap;
    }


    /**
     * Calculates mean value of countData for replicates.
     * <p>
     * @param allData data with condition as key.
     */
    private Map<Integer, Map<PersistentFeature, int[]>> findMeanOfReplicatesDataForEachCondition(
            Map<Integer, MultiValueMap<PersistentFeature, int[]>> allData ) {
        Map<Integer, Map<PersistentFeature, int[]>> preparedDataForConditions = new HashMap<>();
        Integer[] conditions = { indexForConditionA, indexForConditionB };

        for( Integer condition : conditions ) {
            Map<PersistentFeature, int[]> preparedInnerMap = new HashMap<>();
            MultiValueMap<PersistentFeature, int[]> featureSet = allData.get( condition );
            for( PersistentFeature feature : featureSet.keySet() ) {
                Collection<int[]> countData = featureSet.getCollection( feature );
                int[] means = findMeanBetweenArraysInCollection( countData );
                preparedInnerMap.put( feature, means );
            }
            preparedDataForConditions.put( condition, preparedInnerMap );
        }
        return preparedDataForConditions;
    }


    /**
     * Calculates mean value of countData for replicates.
     * <p>
     * @param allData data with condition as key.
     */
    private int[] findMeanBetweenArraysInCollection( Collection<int[]> countData ) {
        Iterator<int[]> countDataIterator = countData.iterator();
        int[] dataSum = new int[countDataIterator.next().length];
        int[] means = new int[countDataIterator.next().length];
        int k = 0;
        for( int[] data : countData ) {
            for( int i = 0; i < data.length; i++ ) {
                if( k == 0 ) {
                    dataSum[i] = data[i];
                } else {
                    dataSum[i] += data[i];
                }
            }
            k++;
        }
        int i = 0;
        for( int sum : dataSum ) {
            means[i] = (int) ((sum / countData.size()) + 0.5);
            i++;
        }
        return means;
    }


    /**
     * Calculates mean value of r square for each pair of replicates in
     * condition.
     * <p>
     * @param allData data with condition as key.
     */
    protected Map<Integer, Map<PersistentFeature, Double>> calculateAverageRSquareOfReplicates(
            Map<Integer, MultiValueMap<PersistentFeature, int[]>> countData ) {
        Map<Integer, Map<PersistentFeature, Double>> preparedDataForConditions = new HashMap<>();
        Map<PersistentFeature, Double> preparedData;
        for( Integer condition : countData.keySet() ) {
            MultiValueMap<PersistentFeature, int[]> featureSetForCondition = countData.get( condition );
            Map<PersistentFeature, Double> averagesOfReplicatesForCondition = new HashMap<>();
            Map<Integer, Map<PersistentFeature, int[]>> replicatePairs = new HashMap<>();
            for( PersistentFeature feature : featureSetForCondition.keySet() ) {
                ArrayList<Double> rCoefMeans = new ArrayList<>();
                ArrayList<int[]> data = (ArrayList<int[]>) featureSetForCondition.getCollection( feature );
                for( int i = 0; i < data.size(); i++ ) {
                    for( int j = i + 1; j < data.size(); j++ ) {
                        int[] firstReplicateData = data.get( i );
                        int[] secondReplicateData = data.get( j );
                        LinearRegression calculation = new LinearRegression( super.getProcessingLog() );
                        double[] regCalculation = calculation.runFilteredRegressionCalculation(
                                firstReplicateData, secondReplicateData );
                        double rCoef = regCalculation[2];
                        rCoefMeans.add( rCoef );
                    }
                }
                double rSquareAverage = averageOfArray( rCoefMeans );
                averagesOfReplicatesForCondition.put( feature, rSquareAverage );
            }
            preparedDataForConditions.put( condition, averagesOfReplicatesForCondition );

        }
        return preparedDataForConditions;
    }


    /**
     * Finds average value of double list elements.
     * <p>
     * @param array list with data
     */
    protected double averageOfArray( ArrayList<Double> array ) {
        double sum = 0;
        for( Double data : array ) {
            sum += data;
        }
        double average = sum / array.size();
        return average;
    }


    /**
     * Runs linear regression tool after data are collected, determines if data
     * set contains replicates data.
     * <p>
     * <p>
     */
    @Override
    protected List<ResultDeAnalysis> processWithTool() {
        Map<Integer, Map<PersistentFeature, int[]>> preparedDataForConditions;
        Map<Integer, Map<PersistentFeature, Double>> calculatedForReplicates = new HashMap<>();
        determineGroupsForConditions();
        if( workingWithoutReplicates ) {
            preparedDataForConditions = allContinuousCoverageData;
        } else {
            Map<Integer, MultiValueMap<PersistentFeature, int[]>> sortedDataForConditions
                    = sortDataForConditions( allContinuousCoverageData );
            preparedDataForConditions = findMeanOfReplicatesDataForEachCondition( sortedDataForConditions );
            calculatedForReplicates = calculateAverageRSquareOfReplicates( sortedDataForConditions );
        }
        linRegForConditions.process( preparedDataForConditions );
        Map<PersistentFeature, double[]> calculatedForConditions = linRegForConditions.getResults();
        results = prepareResults( calculatedForConditions, calculatedForReplicates );
        return results;
    }


    /**
     * Forms table of results.
     * <p>
     * @param calculated data calculated using linear regression
     */
    private List<ResultDeAnalysis> prepareResults( Map<PersistentFeature, double[]> calculated,
                                                   Map<Integer, Map<PersistentFeature, Double>> replicatesData ) {
        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Creating Continuous Count Data Table" );
        progressHandle.start( calculated.size() );

        final List<Object> regionNamesList = new ArrayList<>();
        List<List<Object>> tableContents = new ArrayList<>();

        int k = 0;
        for( Map.Entry<PersistentFeature, double[]> feature : calculated.entrySet() ) {
            k++;
            int tableSize = feature.getValue().length + 3; // Plus feature name and  r square of replicates(2 cond)
            final Object[] tmp = new Object[tableSize];
            double[] rSqrt = feature.getValue();
            boolean allZero = true;

            tmp[0] = feature.getKey();
            for( int i = 1; i < tableSize - 2; i++ ) {
                if( rSqrt[i - 1] != 0 ) {
                    tmp[i] = rSqrt[i - 1];
                    allZero = false;
                }
            }
            if( replicatesData.isEmpty() ) {
                tmp[tableSize - 2] = "There are no replicates";
                tmp[tableSize - 1] = "There are no replicates";
            } else {
                tmp[tableSize - 2] = replicatesData.get( indexForConditionA ).get( feature.getKey() );
                tmp[tableSize - 1] = replicatesData.get( indexForConditionB ).get( feature.getKey() );
            }

            if( !allZero ) {
                tableContents.add( new Vector( Arrays.asList( tmp ) ) );
                regionNamesList.add( feature.getKey() );
            }
            progressHandle.progress( k );
        }

        List<Object> colNames = new ArrayList<>();
        colNames.add( "Feature" );
        colNames.add( "Intercept" );
        colNames.add( "Slope" );
        colNames.add( "Normalized Slope" );
        colNames.add( "Apache Pearson" );
        colNames.add( "Apache Spearman" );
        colNames.add( "R for Conditions" );
        colNames.add( "R for Replicates (Cond. 1)" );
        colNames.add( "R for Replicates (Cond. 2)" );

        List<ResultDeAnalysis> result = Collections.singletonList( new ResultDeAnalysis(
                tableContents, colNames, regionNamesList, "Regression Data Table" ) );
        progressHandle.finish();
        return result;
    }


    @Override
    public synchronized void showData( Object data ) {
        if( ++resultsReceivedBack == allContinuousCoverageData.size() ) {
            results.clear();
            results.addAll( processWithTool() );
            notifyObservers( AnalysisStatus.FINISHED );
        }
    }


    @Override
    public List<ResultDeAnalysis> getResults() {
        return Collections.unmodifiableList( results );
    }


    @Override
    public void endAnalysis() {
        results = null;
    }


}
