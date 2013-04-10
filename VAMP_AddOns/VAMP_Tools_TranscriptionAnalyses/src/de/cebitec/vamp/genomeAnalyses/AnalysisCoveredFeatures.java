package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.databackend.AnalysesHandler;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.util.Observer;
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
    private int minCoveragePercent;
    private int minCountedCoverage;
    private boolean whateverStrand;
    private int refSeqLength;
    private List<PersistantFeature> genomeFeatures;
    private HashMap<Integer, CoveredFeature> coveredFeatureCount; //feature id to count of covered positions for feature
    private List<CoveredFeature> coveredFeatures;
    
    private int lastFeatureIdx;

    /**
     * Carries out the logic behind the covered features analysis. Both
     * parameters are mandatory.
     * @param trackViewer the track viewer for which the analyses should be carried out
     * @param minCoveragePercent minimum percentage of an feature which has
     * to be classified as covered, in order to detect it as 'present' in the 
     * analysis
     * @param minCountedCoverage minimum coverage at a certain position to be
     * taken into account for the analysis
     * @param whateverStrand <tt>true</tt>, if the strand does not matter for 
     *      this analysis, false, if only mappings on the strand of the 
     *      respective feature should be considered.
     */
    public AnalysisCoveredFeatures(TrackConnector trackConnector, int minCoveragePercent, int minCountedCoverage, boolean whateverStrand) {
        this.trackConnector = trackConnector;
        this.minCoveragePercent = minCoveragePercent;
        this.minCountedCoverage = minCountedCoverage;
        this.whateverStrand = whateverStrand;
        
        this.coveredFeatures = new ArrayList<>();
        this.coveredFeatureCount = new HashMap<>();
        this.lastFeatureIdx = 0;
        
        this.initDatastructures();
    }
    
    /**
     * Initializes the initial data structures needed for filtering features by read count.
     */
    private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.refSeqLength = trackConnector.getRefSequenceLength();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, refSeqLength);
        
        for (PersistantFeature feature : this.genomeFeatures) {
            this.coveredFeatureCount.put(feature.getId(), new CoveredFeature(feature, trackConnector.getTrackID()));
        }
    }
    
    /**
     * Updates the read count for a new list of mappings or calls the findCoveredFeatures method.
     * @param data the data to handle: Either a list of mappings or "2" = mapping querries are done.
     */
    @Override
    public void update(Object data) {
        CoverageAndDiffResultPersistant coverageAndDiffResult = new CoverageAndDiffResultPersistant(null, null, null, true, 0, 0);
        
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

        PersistantCoverage coverage = coverageAndDiffResult.getCoverage();
        int rightBound = coverage.getRightBound();

        PersistantFeature feature;
        int noCoveredBases;
        int featureStart;
        int featureStop;
        int coveredBases;

          //coverage identified within an feature
        for (int i = this.lastFeatureIdx; i < this.genomeFeatures.size() - 1; ++i) {
            noCoveredBases = 0;
            feature = this.genomeFeatures.get(i);
            featureStart = feature.getStart();
            featureStop = feature.getStop();
            
            if (featureStart < rightBound) {
                if (featureStop >= rightBound) {
                    this.lastFeatureIdx = i;
                }
                
                if (whateverStrand) {
                    for (int j = featureStart; j < featureStop; ++j) {
                        if (    coverage.getBestMatchFwdMult(j) >= this.minCountedCoverage ||
                                coverage.getBestMatchRevMult(j) >= this.minCountedCoverage) {
                            ++noCoveredBases;
                        }
                    }                    
                } else {

                    if (feature.isFwdStrand()) {
                        for (int j = featureStart; j < featureStop; ++j) {
                            if (coverage.getBestMatchFwdMult(j) >= this.minCountedCoverage) {
                                ++noCoveredBases;
                            }
                        }
                    } else { //reverse strand
                        for (int j = featureStart; j < featureStop; ++j) {
                            if (coverage.getBestMatchRevMult(j) >= this.minCountedCoverage) {
                                ++noCoveredBases;
                            }
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

    /**
     * Detects all features, which satisfy the given minimum coverage value
     * at at least the given minimum percentage of bases of the feature.
     */
    private void findCoveredFeatures() {
        int percentCovered;
        for (Integer id : this.coveredFeatureCount.keySet()) {
            percentCovered = this.coveredFeatureCount.get(id).getPercentCovered();
            if (percentCovered > this.minCoveragePercent) {
                this.coveredFeatures.add(this.coveredFeatureCount.get(id));
            }
        }
    }
    
    @Override
    public List<CoveredFeature> getResults() {
        return this.coveredFeatures;
    }
    
    public int getNoGenomeFeatures() {
        return this.genomeFeatures.size();
    }
}
