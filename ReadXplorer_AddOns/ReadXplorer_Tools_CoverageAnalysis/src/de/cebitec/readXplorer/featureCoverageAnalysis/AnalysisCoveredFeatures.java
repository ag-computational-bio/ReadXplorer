package de.cebitec.readXplorer.featureCoverageAnalysis;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.util.Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Carries out the logic behind the covered features analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisCoveredFeatures implements Observer, AnalysisI<List<CoveredFeature>> {

    private TrackConnector trackConnector;
    private ParameterSetCoveredFeatures analysisParams;
    private List<PersistantFeature> genomeFeatures;
    private HashMap<Integer, CoveredFeature> coveredFeatureCount; //feature id to count of covered positions for feature
    private List<CoveredFeature> detectedFeatures;
    
    private int lastFeatureIdx;

    /**
     * Carries out the logic behind the covered features analysis. Both
     * parameters are mandatory.
     * @param trackViewer the track viewer for which the analyses should be carried out
     * @param getCoveredFeatures <code>true</code> if the covered features should be
     * returned, <code>false</code> if the uncovered features should be returned
     * @param minCoveragePercent minimum percentage of an feature which has
     * to be classified as covered, in order to detect it as 'present' in the 
     * analysis
     * @param minCountedCoverage minimum coverage at a certain position to be
     * taken into account for the analysis
     * @param whateverStrand <tt>true</tt>, if the strand does not matter for 
     *      this analysis, false, if only mappings on the strand of the 
     *      respective feature should be considered.
     */
    public AnalysisCoveredFeatures(TrackConnector trackConnector, ParameterSetCoveredFeatures featureCoverageParameters) {
        this.trackConnector = trackConnector;
        this.analysisParams = featureCoverageParameters;
        this.detectedFeatures = new ArrayList<>();
        this.coveredFeatureCount = new HashMap<>();
        this.genomeFeatures = new ArrayList<>();
        this.lastFeatureIdx = 0;
        
        this.initDatastructures();
    }
    
    /**
     * Initializes the initial data structures needed for filtering features by read count.
     */
    private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        for (PersistantChromosome chrom : refConnector.getChromosomesForGenome().values()) {
            this.genomeFeatures.addAll(refConnector.getFeaturesForRegionInclParents(0, chrom.getLength(), 
                    analysisParams.getSelFeatureTypes(), chrom.getId()));
        }

        PersistantFeature feature;
        for (int i = 0; i < this.genomeFeatures.size(); ++i) {
            feature = this.genomeFeatures.get(i);
            this.coveredFeatureCount.put(feature.getId(), new CoveredFeature(feature, trackConnector.getTrackID()));
        }
    }
    
    /**
     * Updates the read count for a new list of mappings or calls the findCoveredFeatures method.
     * @param data the data to handle: Either a list of mappings or "2" = mapping querries are done.
     */
    @Override
    public void update(Object data) {
        CoverageAndDiffResultPersistant coverageAndDiffResult = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0), null, null, null);
        
        if (data.getClass() == coverageAndDiffResult.getClass()) {
            coverageAndDiffResult = (CoverageAndDiffResultPersistant) data;
            this.updateCoverageCountForFeatures(coverageAndDiffResult);
        } else
        if (data instanceof Byte && ((Byte) data) == AnalysesHandler.COVERAGE_QUERRIES_FINISHED) { //1 means coverage analysis is finished
            this.findCoveredFeatures();
        }
    }
    
    /**
     * Updates the coverage count for the features with the coverage positions.
     * @param coverageAndDiffResult the coverage and diff result
     */
    public void updateCoverageCountForFeatures(CoverageAndDiffResultPersistant coverageAndDiffResult) {

        int chromId = coverageAndDiffResult.getRequest().getChromId();
        PersistantCoverage coverage = coverageAndDiffResult.getCoverage();
        int rightBound = coverage.getRightBound();

        PersistantFeature feature;
        int noCoveredBases;
        int featureStart;
        int featureStop;
        int coveredBases;

        //coverage identified within an feature
        for (int i = 0; i < this.genomeFeatures.size(); ++i) {
            noCoveredBases = 0;
            feature = this.genomeFeatures.get(i);
            
            if (feature.getChromId() == coverageAndDiffResult.getRequest().getChromId()) {
                featureStart = feature.getStart();
                featureStop = feature.getStop();

                if (featureStart < rightBound) {
//                    if (featureStop >= rightBound) {
//                        this.lastFeatureIdx = i; //still works, since one result only contains results for one chromosome
//                    }

                    if (analysisParams.isWhateverStrand()) {
                        for (int j = featureStart; j < featureStop; ++j) {
                            if (this.checkCanIncreaseBothStrands(coverage, j)) {
                                ++noCoveredBases;
                            }
                        }
                    } else {
                        for (int j = featureStart; j < featureStop; ++j) {
                            if (this.checkCanIncreaseOneStrand(coverage, j, feature.isFwdStrand())) {
                                ++noCoveredBases;
                            }
                        }
                    }

                    //store covered features
                    coveredBases = this.coveredFeatureCount.get(feature.getId()).getNoCoveredBases();
                    this.coveredFeatureCount.get(feature.getId()).setNoCoveredBases(coveredBases + noCoveredBases);
                } else {
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
        if (analysisParams.isGetCoveredFeatures()) {
            for (Integer id : this.coveredFeatureCount.keySet()) {
                percentCovered = this.coveredFeatureCount.get(id).getPercentCovered();
                if (percentCovered > analysisParams.getMinCoveredPercent()) {
                    this.detectedFeatures.add(this.coveredFeatureCount.get(id));
                }
            }
        } else {
            for (Integer id : this.coveredFeatureCount.keySet()) {
                percentCovered = this.coveredFeatureCount.get(id).getPercentCovered();
                if (percentCovered <= analysisParams.getMinCoveredPercent()) {
                    this.detectedFeatures.add(this.coveredFeatureCount.get(id));
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
     * @param coverage The coverage, whose position shall be increased, if valid
     * @param j the position to check
     * @return true, if the position can be increased, false otherwise
     */
    private boolean checkCanIncreaseBothStrands(PersistantCoverage coverage, int j) {
        boolean canIncrease = false;
        ParametersReadClasses readClassParams = analysisParams.getReadClassParams();
        if (readClassParams.isCommonMatchUsed() &&
            coverage.getCommonFwdMult(j) + coverage.getCommonRevMult(j) >= analysisParams.getMinCoverageCount()) {
                canIncrease = true;
        } else if (readClassParams.isBestMatchUsed() &&
            coverage.getBestMatchFwdMult(j) + coverage.getBestMatchRevMult(j) >= analysisParams.getMinCoverageCount()) {
                canIncrease = true;
        } else if (readClassParams.isPerfectMatchUsed() &&
            coverage.getPerfectFwdMult(j) + coverage.getPerfectRevMult(j) >= analysisParams.getMinCoverageCount()) {
                canIncrease = true;
        }
        return canIncrease;
    }
    
    /**
     * Checks if the coverage can be increased for the given position.
     * @param coverage The coverage, whose position shall be increased, if valid
     * @param j the position to check
     * @return true, if the position can be increased, false otherwise
     */
    private boolean checkCanIncreaseOneStrand(PersistantCoverage coverage, int j, boolean isFwdStrand) {
        boolean canIncrease = false;
        ParametersReadClasses readClassParams = analysisParams.getReadClassParams();
        if (readClassParams.isCommonMatchUsed() &&
            (isFwdStrand && coverage.getCommonFwdMult(j) >= analysisParams.getMinCoverageCount()
                || !isFwdStrand && coverage.getCommonRevMult(j) >= analysisParams.getMinCoverageCount())) {
                canIncrease = true;
                
        } else if (readClassParams.isBestMatchUsed() &&
            (isFwdStrand && coverage.getBestMatchFwdMult(j) >= analysisParams.getMinCoverageCount()
                || !isFwdStrand && coverage.getBestMatchRevMult(j) >= analysisParams.getMinCoverageCount())) {
                canIncrease = true;
                
        } else if (readClassParams.isPerfectMatchUsed() &&
            (isFwdStrand && coverage.getPerfectFwdMult(j) >= analysisParams.getMinCoverageCount()
                || !isFwdStrand && coverage.getPerfectRevMult(j) >= analysisParams.getMinCoverageCount())) {
                canIncrease = true;
        }
        return canIncrease;
    }
}
