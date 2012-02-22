package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.DiscreteCountingDistribution;
import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.api.objects.JobI;
import de.cebitec.vamp.databackend.CoverageThreadAnalyses;
import de.cebitec.vamp.databackend.GenomeRequest;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.view.dataVisualisation.DataVisualisationI;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * @author -Rolf Hilker-
 * 
 * Carries out the logic behind the gene start anaylsis.
 * When executing the gene start detection increaseReadCount is always active
 * and maxInitialReadCount + increaseReadCount2 are optional parameters. They can
 * further constrain the search space (e.g. inc = 100, max = 10, inc2 = 50 means 
 * that coverage increases above 50 with an initial read count of 0-10 are detected
 * as gene starts, but also all increases of 100 and bigger. When the parameters are
 * switched, e.g. inc = 50, max = 10, inc2 = 100, then all coverage increases above 100 
 * with an initial read count of 0-10 are detected as gene starts, but for all positions
 * with an initial read count > 10 an increase of 50 read counts is enough to be detected.
 */
public class AnalysisGeneStart implements ThreadListener, AnalysisI<List<GeneStart>>, JobI {

    private final ProgressHandle progressHandle;
    private DataVisualisationI parent;
    private TrackViewer trackViewer;
    private int genomeSize;
    private int increaseReadCount;
    private int increaseReadPercent;
    private int maxInitialReadCount;
    private int increaseReadCount2;
    private List<PersistantFeature> genomeFeatures;
    private List<GeneStart> detectedStarts; //stores position and true for fwd, false for rev strand
    private DiscreteCountingDistribution covIncreaseDistribution;
    private DiscreteCountingDistribution covIncreasePercentDistr;
    private boolean calcCoverageIncreaseDistribution;
    private boolean geneStartAutomatic;
    
    //varibles for gene start detection
    private int nbRequests;
    private int nbCarriedOutRequests;
    private int covLastFwdPos;
    private int covLastRevPos;
    private int lastFeatureIdxGenStartsFwd;
    private int lastFeatureIdxGenStartsRev;

    /**
     * Carries out the logic behind the gene start analysis.
     * When executing the gene start detection increaseReadCount is always active
     * and maxInitialReadCount + increaseReadCount2 are optional parameters. They can
     * further constrain the search space (e.g. inc = 100, max = 10, inc2 = 50 means 
     * that coverage increases above 50 with an initial read count of 0-10 are detected
     * as gene starts, but also all increases of 100 and bigger. When the parameters are
     * switched, e.g. inc = 50, max = 10, inc2 = 100, then all coverage increases above 100 
     * with an initial read count of 0-10 are detected as gene starts, but for all positions
     * with an initial read count > 10 an increase of 50 read counts is enough to be detected.
     * 
     * @param trackViewer the track viewer for which the analyses should be carried out
     * @param increaseReadCount minimum increase of read counts for two neighboring
     *          positions. Only when the increase is bigger, a gene start is predicted
     * @param maxInitialReadCount maximum number of reads at the left position in 
     *          a pair of tho neighboring positions. Gene starts are only predicted,
     *          if this maximum is not exceeded AND increaseReadCount2 is satisfied
     * @param increaseReadCount2 minimum increase of read counts for two neighboring
     *          positions. Only when the increase is bigger, a gene start is predicted
     */
    public AnalysisGeneStart(DataVisualisationI parent, TrackViewer trackViewer, int increaseReadCount,
            int increaseReadPercent, int maxInitialReadCount, int increaseReadCount2, boolean geneStartAutomatic) {
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AnalysisGeneStart.class, "MSG_AnalysesWorker.progress.name"));
        this.parent = parent;
        this.trackViewer = trackViewer;
        this.increaseReadCount = increaseReadCount;
        this.increaseReadPercent = increaseReadPercent;
        this.maxInitialReadCount = maxInitialReadCount;
        this.increaseReadCount2 = increaseReadCount2;
        this.geneStartAutomatic = geneStartAutomatic;
        
        this.nbCarriedOutRequests = 0;
        this.detectedStarts = new ArrayList<GeneStart>();
        this.covLastFwdPos = 0;
        this.covLastRevPos = 0;
        this.lastFeatureIdxGenStartsFwd = 0;
        this.lastFeatureIdxGenStartsRev = 0;
    }

    /**
     * Needs to be called in order to start the gene start analysis. Creates the
     * needed database requests and carries them out. The parent has to be a
     * ThreadListener in order to receive the coverage
     * Afterwards the results are returned by {@link getResults()}
     */
    @Override
    public void startAnalysis() {

        this.progressHandle.start();
        TrackConnector trackCon = trackViewer.getTrackCon();
        List<Integer> trackIds = new ArrayList<Integer>();
        trackIds.add(trackCon.getTrackID());
        CoverageThreadAnalyses coverageThread = new CoverageThreadAnalyses(trackIds);
        coverageThread.start();
        
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackViewer.getReference().getId());
        this.genomeSize = refConnector.getRefGen().getSequence().length();
        this.genomeFeatures = refConnector.getFeaturesForClosedInterval(0, genomeSize);   
        this.covIncreaseDistribution = trackCon.getCoverageIncreaseDistribution();
        this.covIncreasePercentDistr = new DiscreteCountingDistribution();
        this.calcCoverageIncreaseDistribution = this.covIncreaseDistribution.isEmpty();
        if (this.geneStartAutomatic && !this.calcCoverageIncreaseDistribution) {
            this.estimateCutoff();
        }

        int stepSize = 200000;
        int from = 1;
        int to = genomeSize > stepSize ? stepSize : genomeSize;
        int additionalRequest = genomeSize % stepSize == 0 ? 0 : 1;
        this.nbRequests = genomeSize / stepSize + additionalRequest;
        this.progressHandle.switchToDeterminate(this.nbRequests);

        while (to < genomeSize) {
            GenomeRequest coverageRequest = new GenomeRequest(from, to, this, Properties.BEST_MATCH_COVERAGE);
            coverageThread.addRequest(coverageRequest);

            from = to + 1;
            to += stepSize;
        }

        //calc last interval until genomeSize
        to = genomeSize;
        GenomeRequest coverageRequest = new GenomeRequest(from, to, this, Properties.BEST_MATCH_COVERAGE);
        coverageThread.addRequest(coverageRequest);
    }

    
    @Override
    public List<GeneStart> getResults() {
        return this.detectedStarts;
    }

    
    @Override
    public void receiveData(Object data) {
        this.progressHandle.progress("Request " + (++nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests);

        if (data instanceof PersistantCoverage) {
            PersistantCoverage coverage = (PersistantCoverage) data;
            this.detectGeneStarts(coverage);

            //TODO: prozentualer increase
            //annotation finden/ändern

        }

        //when the last request is finished signalize the parent to collect the data
        if (nbCarriedOutRequests >= nbRequests) {
            if (this.calcCoverageIncreaseDistribution) { //if it was calculated, also store it
                this.trackViewer.getTrackCon().insertCoverageIncreaseDistribution(covIncreaseDistribution);
                if (this.geneStartAutomatic) {
                    this.estimateCutoff();
                    this.correctGeneStartList();
                }
            }
            //TODO: correctly incorporate this
            this.estimateCutoffPercent();
            
            this.parent.showData(true);
            this.nbCarriedOutRequests = 0;
            this.progressHandle.finish();
        }
    }

    /**
     * Carries out the detection of predicted gene starts.
     * @param coverage the coverage for predicting the gene starts.
     */
    public void detectGeneStarts(PersistantCoverage coverage) {

        int leftBound = coverage.getLeftBound();
        int fixedLeftBound = leftBound - 1;
        int rightBound = coverage.getRightBound();
        int fwdCov1;
        int revCov1;
        int fwdCov2;
        int revCov2;
        int diffFwd;
        int diffRev;
        int percentDiffFwd;
        int percentDiffRev;

        coverage.setBestMatchFwdMult(fixedLeftBound, covLastFwdPos);
        coverage.setBestMatchRevMult(fixedLeftBound, covLastRevPos);

        if (this.calcCoverageIncreaseDistribution) { //this way code is duplicated, but if clause only evaluated once
            for (int i = fixedLeftBound; i < rightBound; ++i) {
                
                fwdCov1 = coverage.getPerfectFwdMult(i) + coverage.getBestMatchFwdMult(i);
                revCov1 = coverage.getPerfectRevMult(i) + coverage.getBestMatchRevMult(i);
                fwdCov2 = coverage.getPerfectFwdMult(i + 1) + coverage.getBestMatchFwdMult(i + 1);
                revCov2 = coverage.getPerfectRevMult(i + 1) + coverage.getBestMatchRevMult(i + 1);
                diffFwd = fwdCov2 - fwdCov1;
                diffRev = revCov1 - revCov2;
                
                percentDiffFwd = (int) (((double) fwdCov2 / (double) fwdCov1) * 100.0) - 100;
                percentDiffRev = (int) (((double) revCov1 / (double) revCov2) * 100.0) - 100;
                
                this.covIncreaseDistribution.increaseDistribution(diffFwd);
                this.covIncreaseDistribution.increaseDistribution(diffRev);
                this.detectStart(i, fwdCov1, fwdCov2, revCov1, revCov2, diffFwd, diffRev, percentDiffFwd, percentDiffRev);
            }
        } else {
            for (int i = fixedLeftBound; i < rightBound; ++i) {
                fwdCov1 = coverage.getPerfectFwdMult(i) + coverage.getBestMatchFwdMult(i);
                revCov1 = coverage.getPerfectRevMult(i) + coverage.getBestMatchRevMult(i);
                fwdCov2 = coverage.getPerfectFwdMult(i + 1) + coverage.getBestMatchFwdMult(i + 1);
                revCov2 = coverage.getPerfectRevMult(i + 1) + coverage.getBestMatchRevMult(i + 1);
                diffFwd = fwdCov2 - fwdCov1;
                diffRev = revCov1 - revCov2;
                
                percentDiffFwd = (int) (((double) fwdCov2 / (double) fwdCov1) * 100.0) - 100;
                percentDiffRev = (int) (((double) revCov1 / (double) revCov2) * 100.0) - 100;
                this.covIncreasePercentDistr.increaseDistribution(percentDiffFwd);
                this.covIncreasePercentDistr.increaseDistribution(percentDiffRev);
                
                this.detectStart(i, fwdCov1, fwdCov2, revCov1, revCov2, diffFwd, diffRev, percentDiffFwd, percentDiffRev);
            }
        }

        covLastFwdPos = coverage.getPerfectFwdMult(rightBound) + coverage.getBestMatchFwdMult(rightBound);
        covLastRevPos = coverage.getPerfectRevMult(rightBound) + coverage.getBestMatchRevMult(rightBound);
    }
    
    /**
     * Method for analysing the coverage of one pair of neighboring positions and
     * detecting a gene start, if the parameters are satisfied.
     * @param coverage the PersistantCoverage container
     * @param pos the position defining the pair to analyse: pos and (pos + 1)
     * @param fwdCov1 
     * @param fwdCov2 
     * @param revCov1 
     * @param revCov2 
     * @param diffFwd 
     * @param diffRev 
     */
    private void detectStart(int pos, int fwdCov1, int fwdCov2, int revCov1, int revCov2, 
                             int diffFwd, int diffRev, int percentDiffFwd, int percentDiffRev) {
        

        if (this.increaseReadCount2 > 0) { //if low coverage read count is calculated separately
//TODO: percentage einbeziehen!!!
            if (diffFwd > this.increaseReadCount && fwdCov1 > this.maxInitialReadCount
                    || fwdCov1 <= this.maxInitialReadCount && diffFwd > this.increaseReadCount2) {

                DetectedFeatures detFeatures = this.findNextFeature(pos + 1, SequenceUtils.STRAND_FWD);
                this.detectedStarts.add(new GeneStart(pos + 1, SequenceUtils.STRAND_FWD, fwdCov1, fwdCov2, detFeatures));
            }
            if (diffRev > this.increaseReadCount && revCov2 > this.maxInitialReadCount
                    || revCov2 <= this.maxInitialReadCount && diffRev > this.increaseReadCount2) {

                DetectedFeatures detFeatures = this.findNextFeature(pos, SequenceUtils.STRAND_REV);
                this.detectedStarts.add(new GeneStart(pos, SequenceUtils.STRAND_REV, revCov2, revCov1, detFeatures));
            }

        } else {
            if (diffFwd > this.increaseReadCount && percentDiffFwd > this.increaseReadPercent) {
                DetectedFeatures detFeatures = this.findNextFeature(pos + 1, SequenceUtils.STRAND_FWD);
                this.detectedStarts.add(new GeneStart(pos + 1, SequenceUtils.STRAND_FWD, fwdCov1, fwdCov2, detFeatures));
            }
            if (diffRev > this.increaseReadCount && percentDiffRev > this.increaseReadPercent) {
                DetectedFeatures detFeatures = this.findNextFeature(pos, SequenceUtils.STRAND_REV);
                this.detectedStarts.add(new GeneStart(pos, SequenceUtils.STRAND_REV, revCov2, revCov1, detFeatures));
            }
        }
    }

    /**
     * Detects and returns the genomic features, which can be associated to the
     * given gene start and strand. This can be eiter a feature starting at the
     * predicted gene start, which would be a correct start, or it will contain
     * the maximal two closest features found in a vicinity of 500bp up- or 
     * downstream of the gene start.
     * @param geneStartPos the predicted gene start position
     * @param strand the strand, on which the gene start is located
     * @return the genomic features, which can be associated to the
     * given gene start and strand.
     */
    private DetectedFeatures findNextFeature(int geneStartPos, byte strand) {
        final int maxDeviation = 500;
        int minStartPos = geneStartPos - maxDeviation < 0 ? 0 : geneStartPos - maxDeviation;
        int maxStartPos = geneStartPos + maxDeviation > this.genomeSize ? genomeSize : geneStartPos + maxDeviation;
        PersistantFeature feature;
        DetectedFeatures detectedFeatures = new DetectedFeatures();
        int start;
        boolean fstFittingFeature = true;
        if (strand == SequenceUtils.STRAND_FWD) {
            for (int i = this.lastFeatureIdxGenStartsFwd; i < this.genomeFeatures.size(); ++i) {
                feature = this.genomeFeatures.get(i);
                start = feature.getStart();

                if (start >= minStartPos && feature.getStrand() == strand && start <= maxStartPos) {

                    if (fstFittingFeature) {
                        this.lastFeatureIdxGenStartsFwd = i; //this is the first feature in the interval
                        fstFittingFeature = false;
                    }

                    if (start < geneStartPos && feature.getStop() > geneStartPos) {
                        //store feature as next smaller feature, but search for closer
                        //smaller feature & correctly annotated gene start
                        detectedFeatures.setupstreamFeature(feature);
                    } else if (start == geneStartPos) {
                        //store correctly annotated gene start
                        detectedFeatures.setCorrectStartFeature(feature);
                        detectedFeatures.setupstreamFeature(null);
                        break;
                    } else if (start > geneStartPos) {
                        //store next bigger feature, translation start is earlier
                        detectedFeatures.setDownstreamFeature(feature);
                        break;
                    }

                } else if (start >= maxStartPos) {
                    if (fstFittingFeature) {
                        this.lastFeatureIdxGenStartsFwd = i; //this is the first feature in the interval
                    }
                    break;
                }
            }
        } else { //means: strand == SequenceUtils.STRAND_REV

            for (int i = this.lastFeatureIdxGenStartsRev; i < this.genomeFeatures.size(); ++i) {
                feature = this.genomeFeatures.get(i);
                start = feature.getStop();

                if (start >= minStartPos && feature.getStrand() == strand && start <= maxStartPos) {

                    if (fstFittingFeature) {
                        this.lastFeatureIdxGenStartsRev = i; //this is the first feature in the interval
                        fstFittingFeature = false;
                    }

                    if (start < geneStartPos) {
                        //store feature as next bigger feature, but search for closer
                        //bigger feature & correctly annotated gene start
                        detectedFeatures.setDownstreamFeature(feature);
                    } else if (start == geneStartPos) {
                        //store correctly annotated gene start
                        detectedFeatures.setCorrectStartFeature(feature);
                        detectedFeatures.setDownstreamFeature(null);
                        break;
                    } else if (start > geneStartPos && feature.getStart() < geneStartPos) {
                        //store next smaller feature, translation start is further in gene
                        detectedFeatures.setupstreamFeature(feature);
                        break;

                    }

                } else if (start >= maxStartPos) {
                    if (fstFittingFeature) {
                        this.lastFeatureIdxGenStartsRev = i;
                    }
                    break;
                }
            }
        }
        return detectedFeatures;
    }

    @Override
    public int getNbCarriedOutRequests() {
        return this.nbCarriedOutRequests;
    }

    @Override
    public int getNbTotalRequests() {
        return this.nbRequests;
    }

    /**
     * Used to computationally estimate the optimal cutoff = minimum increase of 
     * read counts from one position to the next.
     * 2 Parameters take care of this task: 
     * At first calculate the index of the coverage increase distribution, which exceeds 
     * the threshold of more than 1 gene per 1KB genome size for the first time for this track.
     * Second, if the number of possible gene starts is still larger than 0,05%
     * + a 10% correction term, use the 0,05% of largest coverage increases
     * as threshold. 
     * These thresholds return an index from the coverage increase distribution
     * and the increaseReadCount variable is set according to the smallest 
     * coverage increase value for the calculated index.
     */
    private void estimateCutoff() {
        long totalCount = this.covIncreaseDistribution.getTotalCount();
        int fstReferenceValue = this.genomeSize / 1000; // 1 Gene per 1KB Genome size
        long scndReferenceValue = totalCount / 2000; //threshold of 0,05% of largest coverage increases
        int[] covIncDistribution = this.covIncreaseDistribution.getDiscreteCountingDistribution();
        
        int nbGeneStarts = 0;
        int selectedIndex = 0;
        for (int i = covIncDistribution.length - 1; i > 0; --i) {
            if (nbGeneStarts < fstReferenceValue) {
                nbGeneStarts += covIncDistribution[i];
                selectedIndex = i;
            } else {
                break;
            }
        }
        if (nbGeneStarts > scndReferenceValue + (scndReferenceValue / 10)) {
            for (int i = covIncDistribution.length; i > 0; --i) {
                if (nbGeneStarts < scndReferenceValue) {
                    nbGeneStarts += covIncDistribution[i];
                    selectedIndex = i;
                } else {
                    break;
                }
            }
        }
        
        this.increaseReadCount = covIncreaseDistribution.getMinCountForIndex(selectedIndex);
    }
    
    private void estimateCutoffPercent() {
        long totalCount = this.covIncreasePercentDistr.getTotalCount();
        long referenceValue = totalCount / 1000; //threshold of 1% of largest coverage increases in percent
        int[] covIncDistribution = this.covIncreasePercentDistr.getDiscreteCountingDistribution();
        
        for (int i : covIncDistribution) {
            System.out.println(i);
        }
        
        int nbCoverageIncreases = 0;
        int selectedIndex = 0;
        for (int i = covIncDistribution.length - 1; i > 0; --i) {
            if (nbCoverageIncreases < referenceValue) {
                nbCoverageIncreases += covIncDistribution[i];
                selectedIndex = i;
            } else {
                break;
            }
        }
        
        this.increaseReadPercent = covIncreasePercentDistr.getMinCountForIndex(selectedIndex);
    }

    /**
     * Removes all detected gene starts with a too small coverage increase, in case 
     * the increaseReadCount was changed after calculating the list of detectedGenes.
     */
    private void correctGeneStartList() {
        for (GeneStart geneStart : this.detectedStarts) {
            if (geneStart.getStartCoverage() - geneStart.getInitialCoverage() < this.increaseReadCount) {
                this.detectedStarts.remove(geneStart);
            }
        }
    }
}
