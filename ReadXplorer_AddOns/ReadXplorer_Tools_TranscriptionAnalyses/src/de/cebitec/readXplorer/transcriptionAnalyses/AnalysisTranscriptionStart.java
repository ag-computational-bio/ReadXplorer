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
package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.Coverage;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResult;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.DetectedFeatures;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.TranscriptionStart;
import de.cebitec.readXplorer.util.DiscreteCountingDistribution;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carries out the logic behind the transcription start site (TSS) anaylsis.
 * When executing the transcription start site detection increaseReadCount is always active
 * and maxInitialReadCount + increaseReadCount2 are optional parameters. They can
 * further constrain the search space (e.g. inc = 100, max = 10, inc2 = 50 means 
 * that read starts above 50 with an initial read count of 0-10 are detected
 * as transcription start sites, but also all increases of 100 and bigger. When the parameters are
 * switched, e.g. inc = 50, max = 10, inc2 = 100, then all coverage increases above 100 
 * with an initial read count of 0-10 are detected as transcription start sites, but for all positions
 * with an initial read count > 10 an increase of 50 read counts is enough to be detected.
 * 
 * 1. Nach Coverage: a) More read starts than threshold of 99,75% of read starts in data set
		     b) Coverage Increase in percent larger than 99,75% of the increase percentages
 2. Nach Mappingstarts: a) Nach Chernoff-Formel
			b) Nach Wahrscheinlichkeitsformel (Binomialverteilung)

 * @author -Rolf Hilker-
 * 
 */
public class AnalysisTranscriptionStart implements Observer, AnalysisI<List<TranscriptionStart>> {

    private TrackConnector trackConnector;
    private final ParameterSetTSS parametersTSS;
    private boolean isStrandBothOption;
    private boolean isBothFwdDirection;
    private boolean isFeatureStrand;
    private boolean isFeatureStrandAnalysis; //TODO: is this still needed?
    protected List<TranscriptionStart> detectedStarts;
    private DiscreteCountingDistribution readStartDistribution;
    private DiscreteCountingDistribution covIncPercentDistribution;
    private boolean calcCoverageDistributions;
    
    //varibles for transcription start site detection
    private int totalCovLastFwdPos;
    private int totalCovLastRevPos;
    private int totalReadStartsLastRevPos;
    private int lastFeatureIdxGenStartsFwd;
    private int lastFeatureIdxGenStartsRev;
    private ReferenceConnector refConnector;
    
    private Map<Integer, Integer> exactReadStartDist = new HashMap<>(); //exact read start distribution
    private Map<Integer, Integer> exactCovIncPercDist = new HashMap<>(); //exact coverage increase percent distribution
    
    protected CoverageManager currentCoverage;
    private Map<Integer, PersistentChromosome> chromosomes;

    /**
     * Carries out the logic behind the transcription start site analysis.
     * When executing the transcription start site detection increaseReadCount is always active
     * and maxInitialReadCount + increaseReadCount2 are optional parameters. They can
     * further constrain the search space (e.g. inc = 100, max = 10, inc2 = 50 means 
     * that coverage increases above 50 with an initial read count of 0-10 are detected
     * as transcription start sites, but also all increases of 100 and bigger. When the parameters are
     * switched, e.g. inc = 50, max = 10, inc2 = 100, then all coverage increases above 100 
     * with an initial read count of 0-10 are detected as transcription start sites, but for all positions
     * with an initial read count > 10 an increase of 50 read counts is enough to be detected.
     * 
     * @param trackConnector the track viewer for which the analyses should be carried out
     * @param parametersTSS the parameter set for this TSS analysis
     */
    public AnalysisTranscriptionStart(TrackConnector trackConnector, ParameterSetTSS parametersTSS) {
        this.trackConnector = trackConnector;
        this.parametersTSS = parametersTSS;
        
        this.detectedStarts = new ArrayList<>();
        this.totalCovLastFwdPos = 0;
        this.totalCovLastRevPos = 0;
        this.totalReadStartsLastRevPos = 0;
        this.lastFeatureIdxGenStartsFwd = 0;
        this.lastFeatureIdxGenStartsRev = 0;
        
        this.initDatastructures();
    }

    /**
     * Initializes the initial data structures needed for a transcription start site analysis.
     */
    private void initDatastructures() {
        
        refConnector = ProjectConnector.getInstance().getRefGenomeConnector(trackConnector.getRefGenome().getId());
        chromosomes = refConnector.getChromosomesForGenome();

        this.readStartDistribution = trackConnector.getCountDistribution(Properties.READ_START_DISTRIBUTION);
        this.covIncPercentDistribution = trackConnector.getCountDistribution(Properties.COVERAGE_INC_PERCENT_DISTRIBUTION);
        this.calcCoverageDistributions = this.readStartDistribution.isEmpty();

        if (this.parametersTSS.isAutoTssParamEstimation()) {
            this.parametersTSS.setMaxLowCovInitCount(0); //set these values as default for the transcription start site automatic
            this.parametersTSS.setMinLowCovIncrease(0); //avoids loosing smaller, low coverage increases, can only be set by the user
            this.parametersTSS.setMinNoReadStarts(0);
            this.parametersTSS.setMinPercentIncrease(0);
            if (!this.calcCoverageDistributions) {
                int genomeLength = PersistentReference.calcWholeGenomeLength(chromosomes);
                parametersTSS.setMinNoReadStarts(this.estimateCutoff(genomeLength, readStartDistribution, 0)); //+ 0,05%
                parametersTSS.setMinPercentIncrease(this.estimateCutoff(genomeLength, covIncPercentDistribution, 0));// (int) (this.genomeSize / 1000)); //0,1%
            } else {
                this.parametersTSS.setMinNoReadStarts(10); //lowest default values for new data sets without an inital distribution
                this.parametersTSS.setMinPercentIncrease(30); //in the database
            }
        }

        //the minimal increase is initially set to 10%, if the coverage distributions were not calculated yet
        parametersTSS.setMinPercentIncrease(calcCoverageDistributions ? 10 : parametersTSS.getMinPercentIncrease());
    }
    
    /**
     * Detects TSSs for a new CoverageManager object or calls the finish method.
     * @param data the data to handle: Either CoverageManager or "1" = coverage querries are done.
     */
    @Override
    public void update(Object data) {
        if (data instanceof CoverageAndDiffResult) {
            CoverageAndDiffResult result = ((CoverageAndDiffResult) data);
            this.detectTSSs(result);
        } else 
        if (data instanceof Byte && ((Byte) data) == 1) {
            this.finish();
        } 
    }
    
    /**
     * Method to be called when the analysis is finished. Stores the distributions and
     * corrects the results for automatic mode.
     */
    public void finish() {
        //when the last request is finished signalize the parent to collect the data
        this.storeDistributions();
        if (parametersTSS.isAutoTssParamEstimation()) {
            this.correctResult();
        }
    }

    /**
     * Carries out the detection of predicted transcription start sites.
     * @param result the coverage and diff result for predicting the 
     * transcription start sites.
     */
    public void detectTSSs(CoverageAndDiffResult result) {
        
        CoverageManager coverage = result.getCovManager();
        CoverageManager readStarts = result.getReadStarts();
        int chromId = result.getRequest().getChromId();
        int chromLength = chromosomes.get(chromId).getLength();
        List<PersistentFeature> chromFeatures = refConnector.getFeaturesForClosedInterval(0, chromLength, chromId);
        this.currentCoverage = coverage;
        isStrandBothOption = parametersTSS.getReadClassParams().isStrandBothOption();
        isBothFwdDirection = parametersTSS.getReadClassParams().isStrandBothFwdOption();
        isFeatureStrand = parametersTSS.getReadClassParams().isStrandFeatureOption();
        isFeatureStrandAnalysis = isFeatureStrand || isStrandBothOption && isBothFwdDirection;
        
        int leftBound = coverage.getLeftBound();
        int fixedLeftBound = leftBound <= 0 ? 0 : leftBound - 1;
        int rightBound = coverage.getRightBound();
        
        coverage.setLeftBound(fixedLeftBound); //add left coverage value from last request (or 0) to left
        readStarts.setLeftBound(fixedLeftBound); //of all coverage arrays.
        
        Coverage totalCoverage = coverage.getTotalCoverage(this.parametersTSS.getReadClassParams().getExcludedClasses());
        Coverage totalStarts = readStarts.getTotalCoverage(this.parametersTSS.getReadClassParams().getExcludedClasses());
        
        totalCoverage.setFwdCoverage(this.fixLeftCoverageBound(totalCoverage.getFwdCov(), totalCovLastFwdPos));
        totalCoverage.setRevCoverage(this.fixLeftCoverageBound(totalCoverage.getRevCov(), totalCovLastRevPos));
        totalStarts.setFwdCoverage(this.fixLeftCoverageBound(totalStarts.getFwdCov(), 0)); //for read starts the left pos is not important
        totalStarts.setRevCoverage(this.fixLeftCoverageBound(totalStarts.getRevCov(), totalReadStartsLastRevPos)); //on fwd strand

        int idx;
        for (int i = fixedLeftBound; i < rightBound; ++i) {
            idx = coverage.getInternalPos(i);
            this.gatherDataAndDetect(chromId, chromLength, chromFeatures, totalCoverage.getFwdCov(), 
                        totalCoverage.getRevCov(), totalStarts.getFwdCov(), totalStarts.getRevCov(), idx);
        }

        totalCovLastFwdPos = totalCoverage.getFwdCov(rightBound);
        totalCovLastRevPos = totalCoverage.getRevCov(rightBound);
        totalReadStartsLastRevPos = totalStarts.getRevCov(rightBound);
    }
    
    /**
     * Gathers the necessary data from a given set of coverage arrays. 
     * Afterwards, the TSS detection is performed.
     * @param chromId the chromosome to analyze
     * @param chromLength the length of this chromosome
     * @param chromFeatures all features of the chromosome
     * @param covArrayFwd the fwd coverage array of the selected mapping class
     * @param covArrayRev the rev coverage array of the selected mapping class
     * @param readStartArrayFwd the fwd read start array of the selected mapping 
     * class
     * @param readStartArrayRev the rev read start array of the selected mapping 
     * class
     * @param pos the currently investigated position
     */
    private void gatherDataAndDetect(int chromId, int chromLength, List<PersistentFeature> chromFeatures, 
            int[] covArrayFwd, int[] covArrayRev, int[] readStartArrayFwd, int[] readStartArrayRev, int pos) {
        int fwdCov1;
        int revCov1;
        int fwdCov2;
        int revCov2;
        int increaseFwd;
        int increaseRev;
        int readStartsFwd;
        int readStartsRev;
        int percentIncFwd;
        int percentIncRev;
        int readStartIntermd;
        
        fwdCov1 = covArrayFwd[pos];
        revCov1 = covArrayRev[pos];
        fwdCov2 = covArrayFwd[pos + 1];
        revCov2 = covArrayRev[pos + 1];
        if (!isFeatureStrand && !isStrandBothOption) {
            readStartsFwd = readStartArrayFwd[pos];
            readStartsRev = readStartArrayRev[pos + 1];
        } else {
            readStartsFwd = readStartArrayFwd[pos + 1];
            readStartsRev = readStartArrayRev[pos];
        }
        
        if (isStrandBothOption) {
                if (isBothFwdDirection) {
                    increaseFwd = fwdCov2 - fwdCov1 + revCov2 - revCov1;
                    increaseRev = 0;
                    percentIncFwd = GeneralUtils.calculatePercentageIncrease(fwdCov1 + revCov1, fwdCov2 + revCov2);
                    percentIncRev = GeneralUtils.calculatePercentageIncrease(0, 0);
                } else {
                    increaseFwd = 0;
                    increaseRev = revCov1 - revCov2 + fwdCov1 - fwdCov2;
                    percentIncFwd = GeneralUtils.calculatePercentageIncrease(0, 0);
                    percentIncRev = GeneralUtils.calculatePercentageIncrease(revCov2 + fwdCov2, revCov1 + fwdCov1);
                }
            } else {
                //default increases for correctly stranded libraries
                if (isFeatureStrand) {
                    increaseFwd = fwdCov2 - fwdCov1;
                    increaseRev = revCov1 - revCov2;
                    percentIncFwd = GeneralUtils.calculatePercentageIncrease(fwdCov1, fwdCov2);
                    percentIncRev = GeneralUtils.calculatePercentageIncrease(revCov2, revCov1);

                //extra increases for invertedly stranded libraries
                } else {
                    increaseRev = fwdCov1 - fwdCov2;
                    increaseFwd = revCov2 - revCov1;
                    percentIncRev = GeneralUtils.calculatePercentageIncrease(fwdCov2, fwdCov1);
                    percentIncFwd = GeneralUtils.calculatePercentageIncrease(revCov1, revCov2);
                    readStartIntermd = readStartsFwd;
                    readStartsFwd = readStartsRev;
                    readStartsRev = readStartIntermd;
                }
            }
        
        if (this.calcCoverageDistributions) {
            this.readStartDistribution.increaseDistribution(readStartsFwd);
            this.readStartDistribution.increaseDistribution(readStartsRev);
            this.covIncPercentDistribution.increaseDistribution(percentIncFwd);
            this.covIncPercentDistribution.increaseDistribution(percentIncRev);
        }

        this.detectStart(pos, chromId, chromLength, chromFeatures, readStartsFwd, readStartsRev, increaseFwd, increaseRev, percentIncFwd, percentIncRev);
    }
    
    /**
     * Method for analyzing the coverage of one pair of neighboring positions and
     * detecting a transcription start site, if the parameters are satisfied.
     * @param coverage the CoverageManager container
     * @param pos the position defining the pair to analyse: pos and (pos + 1)
     * on the fwd strand the TSS pos is "pos+1" and on the reverse strand the TSS
     * position is "pos"
     * @param readStartsFwd 
     * @param readStartsRev 
     * @param revCov1 
     * @param revCov2 
     * @param diffFwd 
     * @param diffRev 
     */
    private void detectStart(int pos, int chromId, int chromLength, List<PersistentFeature> chromFeatures, int readStartsFwd, 
                    int readStartsRev, int increaseFwd, int increaseRev, int percentIncreaseFwd, int percentIncreaseRev) {
        
        
        if ( ((readStartsFwd <= parametersTSS.getMaxLowCovReadStarts() && readStartsFwd >= parametersTSS.getMinLowCovReadStarts())
            || readStartsFwd >  parametersTSS.getMaxLowCovReadStarts() && readStartsFwd >= parametersTSS.getMinNoReadStarts())
                && percentIncreaseFwd > parametersTSS.getMinPercentIncrease()) {
            
            DetectedFeatures detFeatures = this.findNextFeatures(pos + 1, chromLength, chromFeatures, true);
            this.checkAndAddDetectedStart(new TranscriptionStart(pos + 1, true,
                    readStartsFwd, percentIncreaseFwd, increaseFwd, detFeatures, trackConnector.getTrackID(), chromId));
        }
        if ( ((readStartsRev <= parametersTSS.getMaxLowCovReadStarts() && readStartsRev >= parametersTSS.getMinLowCovReadStarts())
            || readStartsRev >  parametersTSS.getMaxLowCovReadStarts() && readStartsRev >= parametersTSS.getMinNoReadStarts())
                 && percentIncreaseRev > parametersTSS.getMinPercentIncrease()) {
            
            DetectedFeatures detFeatures = this.findNextFeatures(pos, chromLength, chromFeatures, false);
            this.checkAndAddDetectedStart(new TranscriptionStart(pos, false,
                    readStartsRev, percentIncreaseRev, increaseRev, detFeatures, trackConnector.getTrackID(), chromId));
        }

        if (this.parametersTSS.isAutoTssParamEstimation()) {
            //add values to exact counting data structures to refine threshold
            this.increaseDistribution(this.exactReadStartDist, readStartsFwd, parametersTSS.getMinNoReadStarts());
            this.increaseDistribution(this.exactReadStartDist, readStartsRev, parametersTSS.getMinNoReadStarts());
            this.increaseDistribution(this.exactCovIncPercDist, percentIncreaseFwd, parametersTSS.getMinPercentIncrease());
            this.increaseDistribution(this.exactCovIncPercDist, percentIncreaseRev, parametersTSS.getMinPercentIncrease());
        }
    }

    /**
     * Detects and returns the genomic features, which can be associated to the
     * given transcription start site and strand. This can be eiter a feature
     * starting at the predicted transcription start site, which would be a
     * correct start, or it will contain the maximal two closest features found
     * in a vicinity of 1000bp up- or downstream of the transcription start site.
     * If more than one feature start at the detected TSS position, only the
     * last fitting feature is returned as correct start.
     * @param tssPos the predicted transcription start site position
     * @param isFwdStrand the strand, on which the transcription start site is located
     * @return the genomic features, which can be associated to the
     * given transcription start site and strand.
     */
    private DetectedFeatures findNextFeatures(int tssPos, int chromLength, List<PersistentFeature> chromFeatures, boolean isFwdStrand) {
        final int maxDeviation = 1000;
        int minStartPos = tssPos - maxDeviation < 0 ? 0 : tssPos - maxDeviation;
        int maxStartPos = tssPos + maxDeviation > chromLength ? chromLength : tssPos + maxDeviation;
        PersistentFeature feature;
        DetectedFeatures detectedFeatures = new DetectedFeatures();
        int start;
        boolean fstFittingFeature = true;
        if (isFwdStrand) {
            for (int i = this.lastFeatureIdxGenStartsFwd; i < chromFeatures.size(); ++i) {
                feature = chromFeatures.get(i);
                start = feature.getStart();

                /*
                 * We use all features, because also mRNA or rRNA features can contribute to TSS detection,
                 * as they also depict expressed sequences from the reference
                 */
                if (start >= minStartPos && feature.isFwdStrand() && start <= maxStartPos) {

                    if (fstFittingFeature) {
                        this.lastFeatureIdxGenStartsFwd = i; //this is the first feature in the interval
                        fstFittingFeature = false;
                    }

                    if (start < tssPos && feature.getStop() > tssPos) {
                        //store feature as next upstream feature, but search for closer
                        //upstream feature & correctly annotated transcription start site
                        
                        /* 
                         * Also check, if gene and CDS feature are available and covering each other.
                         * Handle this case by not storing the current feature, if it is 
                         * a CDS feature completely covered by a gene feature. In all other
                         * cases the feature can be stored, since we also use CDS features for TSS detection,
                         * if no gene feature is available.
                         */
                        PersistentFeature upstreamAnno = detectedFeatures.getUpstreamFeature();
                        if (    upstreamAnno != null && 
                                feature.getType() == FeatureType.CDS && 
                                upstreamAnno.getType() == FeatureType.GENE &&
                                upstreamAnno.getStop() >= feature.getStop()) {
//                            System.out.println("CDS covered by gene feature Fwd");
                            continue;
                        }
                        
                        detectedFeatures.setUpstreamFeature(feature);
                        
                    } else if (start == tssPos) {
                        //store correctly annotated transcription start site
                        detectedFeatures.setCorrectStartFeature(feature);
                        detectedFeatures.setUpstreamFeature(null);
                        break;
                    } else if (start > tssPos) {
                        /*
                         * Store next downstream feature, transcription start is earlier than annotated,
                         * except the current feature is a CDS feature and no gene feature is present for
                         * that gene, starting earlier.
                         */
                        if (feature.getType() == FeatureType.CDS && i+1 < chromFeatures.size() &&
                                feature.getStart() == chromFeatures.get(i+1).getStart() &&
                                chromFeatures.get(i+1).getType() == FeatureType.GENE) {
                            detectedFeatures.setDownstreamFeature(chromFeatures.get(i+1));
//                            System.out.println("Gene covers CDS with same annotated TSS Fwd");
                        } else {
                            detectedFeatures.setDownstreamFeature(feature);
                        }
                        
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

            for (int i = this.lastFeatureIdxGenStartsRev; i < chromFeatures.size(); ++i) {
                feature = chromFeatures.get(i);
                start = feature.getStop();

                if (start >= minStartPos && feature.isFwdStrand() == isFwdStrand && start <= maxStartPos) {

                    if (fstFittingFeature) {
                        this.lastFeatureIdxGenStartsRev = i; //this is the first feature in the interval
                        fstFittingFeature = false;
                    }

                    if (start < tssPos) {
                        //store feature as next bigger feature, but search for closer
                        //bigger feature & correctly annotated transcription start site
                        
                        /*
                         * Store next upstream feature. transcription start is earlier than annotated,
                         * except the current feature is a CDS feature and no gene feature is present for
                         * that gene, starting earlier.
                         */
                        PersistentFeature upstreamAnno = detectedFeatures.getUpstreamFeature();
                        if (    upstreamAnno != null && 
                                feature.getType() == FeatureType.CDS && 
                                start == upstreamAnno.getStop() &&
                                upstreamAnno.getType() == FeatureType.GENE) {
                            //TODO: this does not work if features start at the same position on rev and fwd strand!
//                            System.out.println("CDS covered by gene feature Rev");
                            continue; // we want to keep the gene instead the CDS feature
                        }
                        
                        detectedFeatures.setDownstreamFeature(feature);
                        
                    } else if (start == tssPos) {
                        //store correctly annotated transcription start site
                        detectedFeatures.setCorrectStartFeature(feature);
                        detectedFeatures.setDownstreamFeature(null);
                        break;
                    } else if (start > tssPos && feature.getStart() < tssPos) {
                        //store next upstream feature, translation start is further in gene
                        
                        if (    feature.getType() == FeatureType.CDS && i+1 < chromFeatures.size() && 
                                chromFeatures.get(i+1).getType() == FeatureType.GENE &&
                                chromFeatures.get(i+1).getStart() <= feature.getStart()) {
                            detectedFeatures.setUpstreamFeature(chromFeatures.get(i+1));
//                            System.out.println("Gene covers CDS with same annotated TSS Rev");
                        } else {                      
                            detectedFeatures.setUpstreamFeature(feature);
                        }
                        break;
                    }

                } else if (start >= tssPos) {
                    if (fstFittingFeature) {
                        this.lastFeatureIdxGenStartsRev = i; //TODO: features should be sorted by stop pos for rev strand
                        fstFittingFeature = false;
                    }
                } 
                if (start >= maxStartPos && feature.getStart() > maxStartPos) {
                    break;
                }
            }
        }
        return detectedFeatures;
    }
    
    /**
     * Before adding a new detected transcription start site to the detected
     * transcription start sites list the method checks, if the last detected
     * transcription start site is located within 19bp (sRNAs can be short) of
     * the current transcription start site on the same strand. If that's the
     * case, only the transcription start site with the higher number of TOTAL
     * coverage increase is kept. This method prevents detecting two
     * transcription start sites for the same gene, in case the transcription
     * already starts at a low rate a few bases before the actual transcription
     * start site. This seems to happen, when the end of the -10 region is
     * further away from the actual transcription start site than 7 bases in 
     * procaryotes. There might exist more reasons, of course.
     * @param tss the currently detected transcription start site 
     */
    private void checkAndAddDetectedStart(TranscriptionStart tss) {        
        
        if (this.detectedStarts.size() > 0) {
            int index = this.detectedStarts.size() - 1;
            TranscriptionStart lastDetectedStart = this.detectedStarts.get(index);
            
            while (lastDetectedStart.isFwdStrand() != tss.isFwdStrand() && index > 0 ) {
                lastDetectedStart = this.detectedStarts.get(--index);
            }
            
            if (lastDetectedStart.getPos() + 1 >= tss.getPos() && lastDetectedStart.isFwdStrand() == tss.isFwdStrand()) {
                int noReadStartsLastStart = lastDetectedStart.getReadStartsAtPos();
                int noReadStartsTSS = tss.getReadStartsAtPos();
                
                if (noReadStartsLastStart < noReadStartsTSS) {
                    this.detectedStarts.remove(index);
                    this.addDetectStart(tss);
                }
                //else, we keep the lastDetectedStart, but discard the current transcription start site
            } else {
                this.addDetectStart(tss);
            }
        } else {
            this.addDetectStart(tss);
        }
    }
    
    /**
     * Acutally adds the detected TSS to the list of detected TSSs.
     * @param tss the transcription start site to add to the list
     */
    protected void addDetectStart(TranscriptionStart tss) {
        this.detectedStarts.add(tss);
    }

    /**
     * Used to computationally estimate the optimal cutoff = minimum increase of 
     * read counts from one position to the next in total or in percent.
     * 2 Parameters take care of this task: 
     * At first the index of the coverage increase distribution, which exceeds the threshold
     * of more than 2 genes per 1KB genome size for the first time for this track is calculated. 
     * In prokaryotic genomes gene density is approx 1 per 1kb and max 1,16 in Sulfolobus solfataricus according to
     * I. B. Rogozin, et al., “Congruent evolution of different classes of non-coding DNA in prokaryotic genomes,” 
     * Nucleic Acids Res, vol. 30, no. 19, pp. 4264–4271, Oct. 2002.
     * In this case we allow for 2 genes per kb, because we use two distributions, each leading to one threshold.
     * Since only positions exceeding both thresholds are reported later, the result set is shrinked again.
     * In the optimal case we gain very low numbers of false positives, but still find all true positives.
     * @param distribution the underlying distribution
     * @param thresholdEnlarger Absolute value 
     *                      if 0,2% is to tight as a threshold, you can set a value to be added to the standard 0,2% threshold.
     *                      if 0,3% of the whole distribution should be used as threshold use "this.genomeSize * 3 / 1000"
     * @return The calculated threshold returns an index from the coverage increase distribution
     * and the smallest total coverage increase (in total or percent) value for the calculated index is returned by this method.
     */
    private int estimateCutoff(int genomeLength, DiscreteCountingDistribution distribution, int thresholdEnlarger) {
        //genomeSize = total number of positions contributing to the increase distribution
        int maxEstimatedNbOfActiveGenes = (int) (genomeLength * 0.0025) + thresholdEnlarger; // 0,25% = 2,5 Genes per 1KB Genome size. This allows for variability.
        int[] distributionValues = distribution.getDiscreteCountingDistribution();
        
        int nbTSSs = 0;
        int selectedIndex = 0;
        for (int i = distributionValues.length - 1; i > 0; --i) {
            // we use the index which first exceeds maxEstimatedNbOfActiveGenes
            if (nbTSSs < maxEstimatedNbOfActiveGenes) {
                nbTSSs += distributionValues[i];
                selectedIndex = i;
            } else {
                break;
            }
        }
        /*
         * number of active genes in the current genome
         * in prokaryotic genomes gene density approx 1 per 1000bp, max 1,16 in Sulfolobus solfataricus according to
         * I. B. Rogozin, et al., “Congruent evolution of different classes of non-coding DNA in prokaryotic genomes,” 
         * Nucleic Acids Res, vol. 30, no. 19, pp. 4264–4271, Oct. 2002.
         */
        
        return distribution.getMinValueForIndex(selectedIndex - 1);
    }

    /**
     * Receives a Hashmap, checks if the current value is already present in the map
     * and increases it by one. If it is not present yet, it is created with a value of 0.
     * @param map the map whose data should be increased
     * @param value the value (key) which should be increased
     * @param threshold the threshold the value has to exceed, to be added to the map
     */
    private void increaseDistribution(Map<Integer, Integer> map, int value, int threshold) {
        if (value > threshold) {
            if (!map.containsKey(value)) {
                map.put(value, 0);
            }
            map.put(value, map.get(value) + 1);
        }
    }

    /**
     * If a new distribution was calculated, this method stores it in the DB and
     * corrects the result list with the new estimated parameters, if the tssAutomatic
     * was chosen.
     */
    private void storeDistributions() {
        if (this.calcCoverageDistributions) { //if it was calculated, also store it
            ProjectConnector.getInstance().insertCountDistribution(readStartDistribution, this.trackConnector.getTrackID());
            ProjectConnector.getInstance().insertCountDistribution(covIncPercentDistribution, this.trackConnector.getTrackID());
            if (this.parametersTSS.isAutoTssParamEstimation()) {
                for (PersistentChromosome chrom : chromosomes.values()) {
                    parametersTSS.setMinNoReadStarts(parametersTSS.getMinNoReadStarts()
                            + this.estimateCutoff(chrom.getLength(), readStartDistribution, 0)); //+ 0,05%
                    parametersTSS.setMinPercentIncrease(parametersTSS.getMinPercentIncrease()
                            + this.estimateCutoff(chrom.getLength(), covIncPercentDistribution, 0));// (int) (this.genomeSize / 1000)); //0,1%
                }
                parametersTSS.setMinNoReadStarts(parametersTSS.getMinNoReadStarts() / chromosomes.values().size());
                parametersTSS.setMinPercentIncrease(parametersTSS.getMinPercentIncrease() / chromosomes.values().size());
                this.correctTSSList();
            }
        }
    }

    /**
     * Removes all detected transcription start sites with a too small coverage increase, in case 
     * the increaseReadCount was changed after calculating the list of detectedGenes.
     */
    private void correctTSSList() {
        TranscriptionStart tss;
        for (int i = 0; i < this.detectedStarts.size(); ++i) {
            tss = this.detectedStarts.get(i);
            if (tss.getReadStartsAtPos() < parametersTSS.getMinNoReadStarts()
                    || tss.getPercentIncrease() < parametersTSS.getMinPercentIncrease()) {
                this.detectedStarts.remove(tss);
            }
        }
    }

    /**
     * After detecting the transcription start sites the exact distribution of coverage increases
     * is known and the threshold can be adapted for the automatic parameter estimation
     * mode. This method calculates the more stringent thresholds first and then removes
     * all transcription start sites from the detected starts list, which cannot satisfy the new 
     * thresholds. Prevents false positives.
     */
    private void correctResult() {
        
        //estimate exact cutoff for readcount increase of 0,25%
        System.out.println("old threshold read count: " + parametersTSS.getMinNoReadStarts());
        System.out.println("old threshold percent: " + parametersTSS.getMinPercentIncrease());
        parametersTSS.setMinNoReadStarts(this.getNewThreshold(this.exactReadStartDist, 0));//(int) (this.genomeSize * 0.0005));
        parametersTSS.setMinPercentIncrease(this.getNewThreshold(this.exactCovIncPercDist, 0));//(int) (this.genomeSize * 0.0005));
        
        //remove detected starts with too low coverage increases
        List<TranscriptionStart> copiedDetectedStarts = new ArrayList<>(this.detectedStarts);
        for (TranscriptionStart tss : this.detectedStarts) {
            
            if ((   tss.getReadStartsAtPos() < parametersTSS.getMinNoReadStarts() ||
                    tss.getPercentIncrease() < parametersTSS.getMinPercentIncrease()) 
//                    && tss.getReadStartsAtPos() > parametersTSS.getMaxLowCovReadStarts()
                    ) {
                copiedDetectedStarts.remove(tss);
            }
        }
        this.detectedStarts = copiedDetectedStarts;
    }
    
    /**
     * Calculates the exact threshold for a map containing a coverage increase distribution.
     * The threshold is  set exactly to 0,25% and can be enlarged by setting the threshold enlarger.
     * @param distribution the exact distribution of coverage increases or cov increases in percent
     * @param thresholdEnlarger absolute value to be added to the new threshold
     * @return 
     */
    private int getNewThreshold(Map<Integer, Integer> distribution, int thresholdEnlarger) {
        int maxValue = (int) (PersistentReference.calcWholeGenomeLength(chromosomes) * 0.0025 + thresholdEnlarger);
        maxValue /= chromosomes.values().size();
        int nbValues = 0;
        List<Integer> keyList = new ArrayList<>(distribution.keySet());
        Collections.sort(keyList);
        
        for (int i = keyList.size()-1; i >= 0; --i) {
            if (nbValues < maxValue) {
                nbValues += distribution.get(keyList.get(i));
            } else {
                return keyList.get(i);
            }
        }
        
        if (keyList.isEmpty()) {
            return 4;
        } else {
            return keyList.get(0);
        }
    }

    /**
     * @return An updated set of the current parameters. It can be updated,
     * because this analysis contains an automatic parameter erstimation.
     */
    public ParameterSetTSS getParametersTSS() {
        return this.parametersTSS;
    }

    @Override
    public List<TranscriptionStart> getResults() {
        return this.detectedStarts;
    }

    /**
     * Adds the coverage of the "lastFwdPos" to the beginning of the given
     * coverage array - resulting array length = oldLength + 1.
     * @param covArray the coverage array to which a new left bound shall be added
     * @param lastCov the last coverage value of the previous request or 0
     * @return the new coverage array including the added coverage value
     */
    private int[] fixLeftCoverageBound(int[] covArray, int lastCov) {
        int[] newCovArray = new int[covArray.length + 1];
        newCovArray[0] = lastCov;
        System.arraycopy(covArray, 0, newCovArray, 1, covArray.length);
        return newCovArray;
    }
}
