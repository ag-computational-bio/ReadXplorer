package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Carries out the logic behind the covered annotations analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisCoveredAnnotations implements Observer, AnalysisI<List<CoveredAnnotation>> {

    private TrackConnector trackConnector;
    private int minCoveragePercent;
    private int minCountedCoverage;
    private int refSeqLength;
    private List<PersistantAnnotation> genomeAnnotations;
    private HashMap<Integer, CoveredAnnotation> coveredAnnoCount; //annotation id to count of covered positions for annotation
    private List<CoveredAnnotation> coveredAnnos;
    
    private int lastAnnotationIdx;

    /**
     * Carries out the logic behind the covered annotations analysis. Both
     * parameters are mandatory.
     * @param trackViewer the track viewer for which the analyses should be carried out
     * @param minCoveragePercent minimum percentage of an annotation which has
     * to be classified as covered, in order to detect it as 'present' in the 
     * analysis
     * @param minCountedCoverage minimum coverage at a certain position to be
     * taken into account for the analysis
     */
    public AnalysisCoveredAnnotations(TrackConnector trackConnector, int minCoveragePercent, int minCountedCoverage) {
        this.trackConnector = trackConnector;
        this.minCoveragePercent = minCoveragePercent;
        this.minCountedCoverage = minCountedCoverage;
        
        this.coveredAnnos = new ArrayList<>();
        this.coveredAnnoCount = new HashMap<>();
        this.lastAnnotationIdx = 0;
        
        this.initDatastructures();
    }
    
    /**
     * Initializes the initial data structures needed for filtering annotations by read count.
     */
    private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        this.refSeqLength = trackConnector.getRefSequenceLength();
        this.genomeAnnotations = refConnector.getAnnotationsForClosedInterval(0, refSeqLength);
        
        for (PersistantAnnotation annotation : this.genomeAnnotations) {
            this.coveredAnnoCount.put(annotation.getId(), new CoveredAnnotation(annotation));
        }
    }
    
    /**
     * Updates the read count for a new list of mappings or calls the findCoveredAnnotations method.
     * @param data the data to handle: Either a list of mappings or "2" = mapping querries are done.
     */
    @Override
    public void update(Object data) {
        CoverageAndDiffResultPersistant coverageAndDiffResult = new CoverageAndDiffResultPersistant(null, null, null, true, 0, 0);
        
        if (data.getClass() == coverageAndDiffResult.getClass()) {
            coverageAndDiffResult = (CoverageAndDiffResultPersistant) data;
            this.updateCoverageCountForAnnotations(coverageAndDiffResult);
        } else
        if (data instanceof Byte && ((Byte) data) == 2) { //2 means coverage analysis is finished
            this.findCoveredAnnotations();
        }
    }
    
    /**
     * Updates the coverage count for the annotations with the coverage positions.
     * @param coverageAndDiffResult the coverage and diff result
     */
    public void updateCoverageCountForAnnotations(CoverageAndDiffResultPersistant coverageAndDiffResult) {

        PersistantCoverage coverage = coverageAndDiffResult.getCoverage();
        int rightBound = coverage.getRightBound();

        /*
         * algorithm:
         * get bunch of data
         * extract for each position the total coverage value
         * (unneeded, because SNPs are exprected - extract for each position the total diff count)
         * (substract coverage - diff for each position)
         * after done for all positions of genome: count for each anno the number of covered bases
         */

        PersistantAnnotation annotation;
        int noCoveredBases = 0;
        int annoStart;
        int annoStop;
        int coveredBases;

          //coverage identified within an annotation
        for (int i = this.lastAnnotationIdx; i < this.genomeAnnotations.size() - 1; ++i) {
            annotation = this.genomeAnnotations.get(i);
            annoStart = annotation.getStart();
            annoStop = annotation.getStop();
            
            if (annoStart < rightBound) {
                if (annoStop >= rightBound) {
                    this.lastAnnotationIdx = i;
                }

                if (annotation.isFwdStrand()) {
                    for (int j = annoStart; j < annoStop; ++j) {
                        if (coverage.getBestMatchFwdMult(j) >= this.minCountedCoverage) {
                            ++noCoveredBases;
                        }
                    }
                } else { //reverse strand
                    for (int j = annoStart; j < annoStop; ++j) {
                        if (coverage.getBestMatchRevMult(j) >= this.minCountedCoverage) {
                            ++noCoveredBases;
                        }
                    }
                }

                //store covered annotations
                coveredBases = this.coveredAnnoCount.get(annotation.getId()).getNoCoveredBases();
                this.coveredAnnoCount.get(annotation.getId()).setNoCoveredBases(coveredBases + noCoveredBases);
            } else {
                break;
            }
    }
}

    /**
     * Detects all annotations, which satisfy the given minimum coverage value
     * at at least the given minimum percentage of bases of the annotation.
     */
    private void findCoveredAnnotations() {
        int percentCovered;
        for (Integer id : this.coveredAnnoCount.keySet()) {
            percentCovered = this.coveredAnnoCount.get(id).getPercentCovered();
            if (percentCovered > this.minCoveragePercent) {
                this.coveredAnnos.add(this.coveredAnnoCount.get(id));
            }
        }
    }
    
    @Override
    public List<CoveredAnnotation> getResults() {
        return this.coveredAnnos;
    }
}
