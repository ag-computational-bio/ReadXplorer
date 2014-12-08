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
package de.cebitec.readXplorer.featureCoverageAnalysis;


import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResult;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.classification.Classification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Carries out the logic behind the covered features analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisCoveredFeatures implements Observer,
                                                AnalysisI<List<CoveredFeature>> {

    private TrackConnector trackConnector;
    private ParameterSetCoveredFeatures analysisParams;
    private List<PersistentFeature> genomeFeatures;
    private HashMap<Integer, CoveredFeature> coveredFeatureCount; //feature id to count of covered positions for feature
    private List<CoveredFeature> detectedFeatures;
    private int summedCov = 0;

    private int lastFeatureIdx;


    /**
     * Carries out the logic behind the covered features analysis. Both
     * parameters are mandatory.
     * <p>
     * @param trackViewer        the track viewer for which the analyses should
     *                           be carried out
     * @param getCoveredFeatures <code>true</code> if the covered features
     *                           should be
     *                           returned, <code>false</code> if the uncovered features should be returned
     * @param minCoveragePercent minimum percentage of an feature which has
     *                           to be classified as covered, in order to detect it as 'present' in the
     *                           analysis
     * @param minCountedCoverage minimum coverage at a certain position to be
     *                           taken into account for the analysis
     * @param whateverStrand     <tt>true</tt>, if the strand does not matter
     *                           for
     *                           this analysis, false, if only mappings on the strand of the
     *                           respective feature should be considered.
     */
    public AnalysisCoveredFeatures( TrackConnector trackConnector, ParameterSetCoveredFeatures featureCoverageParameters ) {
        this.trackConnector = trackConnector;
        this.analysisParams = featureCoverageParameters;
        this.detectedFeatures = new ArrayList<>();
        this.coveredFeatureCount = new HashMap<>();
        this.genomeFeatures = new ArrayList<>();
        this.lastFeatureIdx = 0;

        this.initDatastructures();
    }


    /**
     * Initializes the initial data structures needed for filtering features by
     * read count.
     */
    private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector( trackConnector.getRefGenome().getId() );
        for( PersistentChromosome chrom : refConnector.getChromosomesForGenome().values() ) {
            this.genomeFeatures.addAll( refConnector.getFeaturesForRegionInclParents( 0, chrom.getLength(),
                                                                                      analysisParams.getSelFeatureTypes(), chrom.getId() ) );
        }

        PersistentFeature feature;
        for( int i = 0; i < this.genomeFeatures.size(); ++i ) {
            feature = this.genomeFeatures.get( i );
            this.coveredFeatureCount.put( feature.getId(), new CoveredFeature( feature, trackConnector.getTrackID() ) );
        }
    }


    /**
     * Updates the read count for a new list of mappings or calls the
     * findCoveredFeatures method.
     * <p>
     * @param data the data to handle: Either a list of mappings or "2" =
     *             mapping querries are done.
     */
    @Override
    public void update( Object data ) {
        CoverageAndDiffResult coverageAndDiffResult = new CoverageAndDiffResult( new CoverageManager( 0, 0 ), null, null, null );

        if( data.getClass() == coverageAndDiffResult.getClass() ) {
            coverageAndDiffResult = (CoverageAndDiffResult) data;
            this.updateCoverageCountForFeatures( coverageAndDiffResult );
        }
        else if( data instanceof Byte && ((byte) data) == AnalysesHandler.COVERAGE_QUERRIES_FINISHED ) { //1 means coverage analysis is finished
            this.findCoveredFeatures();
        }
    }


    /**
     * Updates the coverage count for the features with the coverage positions.
     * <p>
     * @param coverageAndDiffResult the coverage and diff result
     */
    public void updateCoverageCountForFeatures( CoverageAndDiffResult coverageAndDiffResult ) {

        CoverageManager covManager = coverageAndDiffResult.getCovManager();
        int rightBound = covManager.getRightBound();
        boolean isStrandBothOption = analysisParams.getReadClassParams().isStrandBothOption();
        boolean isFeatureStrand = analysisParams.getReadClassParams().isStrandFeatureOption();
        boolean analysisStrand;

        PersistentFeature feature;
        int noCoveredBases;
        int featureStart;
        int featureStop;
        int coveredBases;
        int meanCov = 0;

        //coverage identified within an feature
        for( int i = 0; i < this.genomeFeatures.size(); ++i ) {
            noCoveredBases = 0;
            feature = this.genomeFeatures.get( i );
            summedCov = 0;

            if( feature.getChromId() == coverageAndDiffResult.getRequest().getChromId() ) {
                featureStart = feature.getStart();
                featureStop = feature.getStop();

                if( featureStart < rightBound ) {
//                    if (featureStop >= rightBound) {
//                        this.lastFeatureIdx = i; //still works, since one result only contains results for one chromosome
//                    }

                    if( isStrandBothOption ) {
                        for( int j = featureStart; j <= featureStop; ++j ) {
                            if( this.checkCanIncreaseAndSumBothStrands( covManager, j ) ) {
                                ++noCoveredBases;
                            }
                        }
                    }
                    else {
                        analysisStrand = isFeatureStrand ? feature.isFwdStrand() : !feature.isFwdStrand(); //only use this if Properties.STRAND_BOTH is not selected
                        for( int j = featureStart; j <= featureStop; ++j ) {
                            if( this.checkCanIncreaseAndSumOneStrand( covManager, j, analysisStrand ) ) {
                                ++noCoveredBases;
                            }
                        }
                    }

                    //store covered features
                    CoveredFeature coveredFeature = coveredFeatureCount.get( feature.getId() );
                    coveredBases = this.coveredFeatureCount.get( feature.getId() ).getNoCoveredBases();
                    if( noCoveredBases > 0 ) {
                        meanCov = (coveredFeature.getMeanCoverage() + (summedCov / noCoveredBases));
                        if( coveredFeature.getMeanCoverage() > 0 ) {
                            meanCov /= 2;
                        }
                        coveredFeature.setMeanCoverage( meanCov );
                    }
                    coveredFeature.setNoCoveredBases( coveredBases + noCoveredBases );
                }
                else {
                    break;
                }
            }
        }
    }


    /**
     * Detects all features, which satisfy the given minimum coverage value
     * at at least the given minimum percentage of bases of the feature.
     */
    private void findCoveredFeatures() {
        int percentCovered;
        if( analysisParams.isGetCoveredFeatures() ) {
            for( Integer id : this.coveredFeatureCount.keySet() ) {
                percentCovered = this.coveredFeatureCount.get( id ).getPercentCovered();
                if( percentCovered > analysisParams.getMinCoveredPercent() ) {
                    this.detectedFeatures.add( this.coveredFeatureCount.get( id ) );
                }
            }
        }
        else {
            for( Integer id : this.coveredFeatureCount.keySet() ) {
                percentCovered = this.coveredFeatureCount.get( id ).getPercentCovered();
                if( percentCovered <= analysisParams.getMinCoveredPercent() ) {
                    this.detectedFeatures.add( this.coveredFeatureCount.get( id ) );
                }
            }
        }
    }


    @Override
    public List<CoveredFeature> getResults() {
        return this.detectedFeatures;
    }


    public int getNoGenomeFeatures() {
        return this.genomeFeatures.size();
    }


    /**
     * Checks if the coverage can be increased for the given position.
     * <p>
     * @param coverage The coverage, whose position shall be increased, if valid
     * @param j        the position to check
     * <p>
     * @return true, if the position can be increased, false otherwise
     */
    private boolean checkCanIncreaseAndSumBothStrands( CoverageManager coverage, int j ) {
        List<Classification> excludedClasses = analysisParams.getReadClassParams().getExcludedClasses();
        int cov = coverage.getTotalCoverage( excludedClasses, j, true )
                  + coverage.getTotalCoverage( excludedClasses, j, false );
        return this.increaseSumIfCanIncrease( cov );
    }


    /**
     * Checks if the coverage can be increased for the given position.
     * <p>
     * @param coverage The coverage, whose position shall be increased, if valid
     * @param j        the position to check
     * <p>
     * @return true, if the position can be increased, false otherwise
     */
    private boolean checkCanIncreaseAndSumOneStrand( CoverageManager coverage, int j, boolean isFwdStrand ) {
        List<Classification> excludedClasses = analysisParams.getReadClassParams().getExcludedClasses();
        return this.increaseSumIfCanIncrease( coverage.getTotalCoverage( excludedClasses, j, isFwdStrand ) );
    }


    /**
     * Checks if the coverage can be increased for the given position and if so,
     * increases the summed coverage for the current feature.
     * <p>
     * @param cov The coverage value
     * <p>
     * @return true, if the position can be increased, false otherwise
     */
    private boolean increaseSumIfCanIncrease( int cov ) {
        boolean canIncrease = cov >= analysisParams.getMinCoverageCount();
        if( canIncrease ) {
            this.summedCov += cov;
        }
        return canIncrease;
    }


}