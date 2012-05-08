package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.DetectedAnnotations;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TranscriptionStart;
import de.cebitec.vamp.databackend.dataObjects.DiscreteCountingDistribution;
import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author -Rolf Hilker-
 * 
 * Carries out the logic behind the transcription start site (TSS) anaylsis.
 * When executing the transcription start site detection increaseReadCount is always active
 * and maxInitialReadCount + increaseReadCount2 are optional parameters. They can
 * further constrain the search space (e.g. inc = 100, max = 10, inc2 = 50 means 
 * that coverage increases above 50 with an initial read count of 0-10 are detected
 * as transcription start sites, but also all increases of 100 and bigger. When the parameters are
 * switched, e.g. inc = 50, max = 10, inc2 = 100, then all coverage increases above 100 
 * with an initial read count of 0-10 are detected as transcription start sites, but for all positions
 * with an initial read count > 10 an increase of 50 read counts is enough to be detected.
 * 
 * 1. Nach Coverage: a) Coverage Increase larger than threshold of 99,75% increases in data set
		     b) Coverage Increase in percent larger than 99,75% of the increase percentages
 2. Nach Mappingstarts: a) Nach Chernoff-Formel
			b) Nach Wahrscheinlichkeitsformel (Binomialverteilung)

 */
public class AnalysisTranscriptionStart implements Observer, AnalysisI<List<TranscriptionStart>> {

    private TrackViewer trackViewer;
    private int genomeSize;
    private int increaseReadCount;
    private int increaseReadPercent;
    private int maxInitialReadCount;
    private int increaseReadCount2;
    private List<PersistantAnnotation> genomeAnnotations;
    private List<TranscriptionStart> detectedStarts; //stores position and true for fwd, false for rev strand
    private DiscreteCountingDistribution covIncreaseDistribution;
    private DiscreteCountingDistribution covIncPercentDistribution;
    private boolean calcCoverageDistributions;
    private boolean tssAutomatic;
    
    //varibles for transcription start site detection
    private int covLastFwdPos;
    private int covLastRevPos;
    private int lastAnnotationIdxGenStartsFwd;
    private int lastAnnotationIdxGenStartsRev;
    
    private HashMap<Integer, Integer> exactCovIncreaseDist = new HashMap<Integer, Integer>(); //exact coverage increase distribution
    private HashMap<Integer, Integer> exactCovIncPercDist = new HashMap<Integer, Integer>(); //exact coverage increase percent distribution

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
     * @param trackViewer the track viewer for which the analyses should be carried out
     * @param increaseReadCount minimum increase of read counts for two neighboring
     *          positions. Only when the increase is bigger, a transcription start site is predicted
     * @param maxInitialReadCount maximum number of reads at the left position in 
     *          a pair of tho neighboring positions. Gene starts are only predicted,
     *          if this maximum is not exceeded AND increaseReadCount2 is satisfied
     * @param increaseReadCount2 minimum increase of read counts for two neighboring
     *          positions. Only when the increase is bigger, a transcription start site is predicted
     */
    public AnalysisTranscriptionStart(TrackViewer trackViewer, int increaseReadCount, int increaseReadPercent, 
            int maxInitialReadCount, int increaseReadCount2, boolean tssAutomatic) {
        this.trackViewer = trackViewer;
        this.increaseReadCount = increaseReadCount;
        this.increaseReadPercent = increaseReadPercent;
        this.maxInitialReadCount = maxInitialReadCount;
        this.increaseReadCount2 = increaseReadCount2;
        this.tssAutomatic = tssAutomatic;
        
        this.detectedStarts = new ArrayList<TranscriptionStart>();
        this.covLastFwdPos = 0;
        this.covLastRevPos = 0;
        this.lastAnnotationIdxGenStartsFwd = 0;
        this.lastAnnotationIdxGenStartsRev = 0;
        
        this.initDatastructures();
    }

    /**
     * Initializes the initial data structures needed for a transcription start site analysis.
     */
    private void initDatastructures() {
        
        TrackConnector trackCon = this.trackViewer.getTrackCon();
        int refId = this.trackViewer.getReference().getId();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(refId);
        this.genomeSize = refConnector.getRefGen().getSequence().length();
        this.genomeAnnotations = refConnector.getAnnotationsForClosedInterval(0, this.genomeSize);   
        
        this.covIncreaseDistribution = trackCon.getCoverageIncreaseDistribution(Properties.COVERAGE_INCREASE_DISTRIBUTION);
        this.covIncPercentDistribution = trackCon.getCoverageIncreaseDistribution(Properties.COVERAGE_INC_PERCENT_DISTRIBUTION);
        this.calcCoverageDistributions = this.covIncreaseDistribution.isEmpty();
        
        if (this.tssAutomatic) {
            this.maxInitialReadCount = 0; //set these values as default for the transcription start site automatic
            this.increaseReadCount2 = 0; //avoids loosing smaller, low coverage increases
            if (!this.calcCoverageDistributions) {
                this.increaseReadCount = this.estimateCutoff(this.covIncreaseDistribution, 0); //+ 0,05%
                this.increaseReadPercent = this.estimateCutoff(this.covIncPercentDistribution, 0);// (int) (this.genomeSize / 1000)); //0,1%
            } else {
                this.increaseReadCount = 10; //lowest default values for new data sets without an inital distribution
                this.increaseReadPercent = 30; //in the database
            }
        }
        
        //the minimal increase is initially set to 10%, if the coverage distributions were not calculated yet
        this.increaseReadPercent = this.calcCoverageDistributions ? 10 : this.increaseReadPercent;
    }
    
    /**
     * Detects TSSs for a new PersistantCoverage object or calls the finish method.
     * @param data the data to handle: Either PersistantCoverage or "1" = coverage querries are done.
     */
    @Override
    public void update(Object data) {
        if (data instanceof PersistantCoverage) {
            PersistantCoverage coverage = (PersistantCoverage) data;
            this.detectTSSs(coverage);

            //TODO: annotation finden/ändern
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
        if (this.tssAutomatic) {
            this.correctResult();
        }
    }

    /**
     * Carries out the detection of predicted transcription start sites.
     * @param coverage the coverage for predicting the transcription start sites.
     */
    public void detectTSSs(PersistantCoverage coverage) {

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

        if (this.calcCoverageDistributions) { //this way code is duplicated, but if clause only evaluated once
            for (int i = fixedLeftBound; i < rightBound; ++i) {
                
                fwdCov1 = coverage.getBestMatchFwdMult(i); //coverage.getPerfectFwdMult(i) + 
                revCov1 = coverage.getBestMatchRevMult(i); //coverage.getPerfectRevMult(i) + 
                fwdCov2 = coverage.getBestMatchFwdMult(i + 1); //coverage.getPerfectFwdMult(i + 1) + 
                revCov2 = coverage.getBestMatchRevMult(i + 1); //coverage.getPerfectRevMult(i + 1) + 
                diffFwd = fwdCov2 - fwdCov1;
                diffRev = revCov1 - revCov2;
                
                percentDiffFwd = GeneralUtils.calculatePercentageIncrease(fwdCov1, fwdCov2);
                percentDiffRev = GeneralUtils.calculatePercentageIncrease(revCov2, revCov1);
                
                this.covIncreaseDistribution.increaseDistribution(diffFwd);
                this.covIncreaseDistribution.increaseDistribution(diffRev);
                this.covIncPercentDistribution.increaseDistribution(percentDiffFwd);
                this.covIncPercentDistribution.increaseDistribution(percentDiffRev);
                
                this.detectStart(i, fwdCov1, fwdCov2, revCov1, revCov2, diffFwd, diffRev, percentDiffFwd, percentDiffRev);
            }
        } else {
            for (int i = fixedLeftBound; i < rightBound; ++i) {
                fwdCov1 = coverage.getBestMatchFwdMult(i); //coverage.getPerfectFwdMult(i) + 
                revCov1 = coverage.getBestMatchRevMult(i); //coverage.getPerfectRevMult(i) + 
                fwdCov2 = coverage.getBestMatchFwdMult(i + 1); //coverage.getPerfectFwdMult(i + 1) + 
                revCov2 = coverage.getBestMatchRevMult(i + 1); //coverage.getPerfectRevMult(i + 1) + 
                diffFwd = fwdCov2 - fwdCov1;
                diffRev = revCov1 - revCov2;
                
                percentDiffFwd = GeneralUtils.calculatePercentageIncrease(fwdCov1, fwdCov2);
                percentDiffRev = GeneralUtils.calculatePercentageIncrease(revCov2, revCov1);
                
                this.detectStart(i, fwdCov1, fwdCov2, revCov1, revCov2, diffFwd, diffRev, percentDiffFwd, percentDiffRev);
            }
        }

        covLastFwdPos = coverage.getBestMatchFwdMult(rightBound); //coverage.getPerfectFwdMult(rightBound) + 
        covLastRevPos = coverage.getBestMatchRevMult(rightBound); //coverage.getPerfectRevMult(rightBound) + 
    }
    
    /**
     * Method for analysing the coverage of one pair of neighboring positions and
     * detecting a transcription start site, if the parameters are satisfied.
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
            if (diffFwd > this.increaseReadCount 
                    && percentDiffFwd > this.increaseReadPercent 
                    && fwdCov1 > this.maxInitialReadCount
                    || fwdCov1 <= this.maxInitialReadCount 
                    && diffFwd > this.increaseReadCount2) {

                DetectedAnnotations detAnnotations = this.findNextAnnotation(pos + 1, SequenceUtils.STRAND_FWD);
                this.checkAndAddDetectedStart(new TranscriptionStart(pos + 1, SequenceUtils.STRAND_FWD, fwdCov1, fwdCov2, detAnnotations));
            }
            if (diffRev > this.increaseReadCount 
                    && percentDiffRev > this.increaseReadPercent 
                    && revCov2 > this.maxInitialReadCount
                    || revCov2 <= this.maxInitialReadCount 
                    && diffRev > this.increaseReadCount2) {

                DetectedAnnotations detAnnotations = this.findNextAnnotation(pos, SequenceUtils.STRAND_REV);
                this.checkAndAddDetectedStart(new TranscriptionStart(pos, SequenceUtils.STRAND_REV, revCov2, revCov1, detAnnotations));
            }

        } else {
            if (diffFwd > this.increaseReadCount && percentDiffFwd > this.increaseReadPercent) {
                DetectedAnnotations detAnnotations = this.findNextAnnotation(pos + 1, SequenceUtils.STRAND_FWD);
                this.checkAndAddDetectedStart(new TranscriptionStart(pos + 1, SequenceUtils.STRAND_FWD, fwdCov1, fwdCov2, detAnnotations));
            }
            if (diffRev > this.increaseReadCount && percentDiffRev > this.increaseReadPercent) {
                DetectedAnnotations detAnnotations = this.findNextAnnotation(pos, SequenceUtils.STRAND_REV);
                this.checkAndAddDetectedStart(new TranscriptionStart(pos, SequenceUtils.STRAND_REV, revCov2, revCov1, detAnnotations));
            }
            
        }
        
        if (this.tssAutomatic) {
            //add values to exact counting data structures to refine threshold
            this.increaseDistribution(this.exactCovIncreaseDist, diffFwd, this.increaseReadCount);
            this.increaseDistribution(this.exactCovIncreaseDist, diffRev, this.increaseReadCount);
            this.increaseDistribution(this.exactCovIncPercDist, percentDiffFwd, this.increaseReadPercent);
            this.increaseDistribution(this.exactCovIncPercDist, percentDiffRev, this.increaseReadPercent);
        }
    }

    /**
     * Detects and returns the genomic annotations, which can be associated to the
     * given transcription start site and strand. This can be eiter an annotation starting at the
     * predicted transcription start site, which would be a correct start, or it will contain
     * the maximal two closest annotations found in a vicinity of 500bp up- or 
     * downstream of the transcription start site.
     * @param tssPos the predicted transcription start site position
     * @param strand the strand, on which the transcription start site is located
     * @return the genomic annotations, which can be associated to the
     * given transcription start site and strand.
     */
    private DetectedAnnotations findNextAnnotation(int tssPos, byte strand) {
        final int maxDeviation = 1000;
        int minStartPos = tssPos - maxDeviation < 0 ? 0 : tssPos - maxDeviation;
        int maxStartPos = tssPos + maxDeviation > this.genomeSize ? genomeSize : tssPos + maxDeviation;
        PersistantAnnotation annotation;
        DetectedAnnotations detectedAnnotations = new DetectedAnnotations();
        int start;
        boolean fstFittingAnnotation = true;
        if (strand == SequenceUtils.STRAND_FWD) {
            for (int i = this.lastAnnotationIdxGenStartsFwd; i < this.genomeAnnotations.size()-1; ++i) {
                annotation = this.genomeAnnotations.get(i);
                start = annotation.getStart();

                /*
                 * We use all annotations, because also mRNA or rRNA annotations can contribute to TSS detection,
                 * as they also depict expressed sequences from the reference
                 */
                if (start >= minStartPos && annotation.getStrand() == strand && start <= maxStartPos) {

                    if (fstFittingAnnotation) {
                        this.lastAnnotationIdxGenStartsFwd = i; //this is the first annotation in the interval
                        fstFittingAnnotation = false;
                    }

                    if (start < tssPos && annotation.getStop() > tssPos) {
                        //store annotation as next upstream annotation, but search for closer
                        //upstream annotation & correctly annotated transcription start site
                        
                        /* 
                         * Also check, if gene and CDS annotation are available and covering each other.
                         * Handle this case by not storing the current annotation, if it is 
                         * a CDS annotation completely covered by a gene annotation. In all other
                         * cases the annotation can be stored, since we also use CDS annotations for TSS detection,
                         * if no gene annotation is available.
                         */
                        PersistantAnnotation upstreamAnno = detectedAnnotations.getUpstreamAnnotation();
                        if (    upstreamAnno != null && 
                                annotation.getType() == FeatureType.CDS && 
                                upstreamAnno.getType() == FeatureType.GENE &&
                                upstreamAnno.getStop() >= annotation.getStop()) {
//                            System.out.println("CDS covered by gene annotation Fwd");
                            continue;
                        }
                        
                        detectedAnnotations.setUpstreamAnnotation(annotation);
                        
                    } else if (start == tssPos) {
                        //store correctly annotated transcription start site
                        detectedAnnotations.setCorrectStartAnnotation(annotation);
                        detectedAnnotations.setUpstreamAnnotation(null);
                        break;
                    } else if (start > tssPos) {
                        /*
                         * Store next downstream annotation, transcription start is earlier than annotated,
                         * except the current annotation is a CDS annotation and no gene annotation is present for
                         * that gene, starting earlier.
                         */
                        if (annotation.getType() == FeatureType.CDS && 
                                annotation.getStart() == this.genomeAnnotations.get(i+1).getStart() &&
                                this.genomeAnnotations.get(i+1).getType() == FeatureType.GENE) {
                            detectedAnnotations.setDownstreamAnnotation(this.genomeAnnotations.get(i+1));
//                            System.out.println("Gene covers CDS with same annotated TSS Fwd");
                        } else {
                            detectedAnnotations.setDownstreamAnnotation(annotation);
                        }
                        
                        break;
                    }

                } else if (start >= maxStartPos) {
                    if (fstFittingAnnotation) {
                        this.lastAnnotationIdxGenStartsFwd = i; //this is the first annotation in the interval
                    }
                    break;
                }
            }
        } else { //means: strand == SequenceUtils.STRAND_REV

            for (int i = this.lastAnnotationIdxGenStartsRev; i < this.genomeAnnotations.size(); ++i) {
                annotation = this.genomeAnnotations.get(i);
                start = annotation.getStop();

                if (start >= minStartPos && annotation.getStrand() == strand && start <= maxStartPos) {

                    if (fstFittingAnnotation) {
                        this.lastAnnotationIdxGenStartsRev = i; //this is the first annotation in the interval
                        fstFittingAnnotation = false;
                    }

                    if (start < tssPos) {
                        //store annotation as next bigger annotation, but search for closer
                        //bigger annotation & correctly annotated transcription start site
                        
                        /*
                         * Store next upstream annotation. transcription start is earlier than annotated,
                         * except the current annotation is a CDS annotation and no gene annotation is present for
                         * that gene, starting earlier.
                         */
                        PersistantAnnotation upstreamAnno = detectedAnnotations.getUpstreamAnnotation();
                        if (    upstreamAnno != null && 
                                annotation.getType() == FeatureType.CDS && 
                                start == upstreamAnno.getStop() &&
                                upstreamAnno.getType() == FeatureType.GENE) {
                            //TODO: this does not work if annotations start at the same position on rev and fwd strand!
//                            System.out.println("CDS covered by gene annotation Rev");
                            continue; // we want to keep the gene instead the CDS annotation
                        }
                        
                        detectedAnnotations.setDownstreamAnnotation(annotation);
                        
                    } else if (start == tssPos) {
                        //store correctly annotated transcription start site
                        detectedAnnotations.setCorrectStartAnnotation(annotation);
                        detectedAnnotations.setDownstreamAnnotation(null);
                        break;
                    } else if (start > tssPos && annotation.getStart() < tssPos) {
                        //store next upstream annotation, translation start is further in gene
                        
                        /* 
                         * Also check, if gene and CDS annotation are available for current SNP.
                         * Handle this case by not storing the current annotation, if it is 
                         * a CDS annotation completely covered by a gene annotation. In all other
                         * cases the annotation can be stored.
                         */
                        if (    annotation.getType() == FeatureType.CDS && 
                                this.genomeAnnotations.get(i+1).getType() == FeatureType.GENE &&
                                this.genomeAnnotations.get(i+1).getStart() <= annotation.getStart()) {
                            detectedAnnotations.setUpstreamAnnotation(this.genomeAnnotations.get(i+1));
//                            System.out.println("Gene covers CDS with same annotated TSS Rev");
                        } else {                      
                            detectedAnnotations.setUpstreamAnnotation(annotation);
                        }
                        break;
                    }

                } else if (start >= maxStartPos) {
                    if (fstFittingAnnotation) {
                        this.lastAnnotationIdxGenStartsRev = i;
                    }
                    break;
                }
            }
        }
        return detectedAnnotations;
    }
    
    /**
     * Before adding a new detected transcription start site to the detected transcription start sites list the method
     * checks, if the last detected transcription start site is located within 19bp (sRNAs can be short) of the current transcription start site
     * on the same strand. If that's the case, only the transcription start site with the higher number of
     * TOTAL coverage increase is kept. This method prevents detecting two transcription start sites for 
     * the same gene, in case the transcription already starts at a low rate a few bases before the
     * actual transcription start site. This seems to happen, when the end of the -10 region is further away from
     * the actual transcription start site than 7 bases.
     * @param tss the currently detected transcription start site 
     */
    private void checkAndAddDetectedStart(TranscriptionStart tss) {        
        
        if (this.detectedStarts.size() > 0) {
            int index = this.detectedStarts.size() - 1;
            TranscriptionStart lastDetectedStart = this.detectedStarts.get(index);
            
            while (lastDetectedStart.getStrand() != tss.getStrand() && index > 0 ) {
                lastDetectedStart = this.detectedStarts.get(--index);
            }
            
            if (lastDetectedStart.getPos() + 19 >= tss.getPos() && lastDetectedStart.getStrand() == tss.getStrand()) {
                int covIncreaseLastStart = lastDetectedStart.getStartCoverage() - lastDetectedStart.getInitialCoverage();
                int covIncreaseTSS = tss.getStartCoverage() - tss.getInitialCoverage();
                
                if (covIncreaseLastStart < covIncreaseTSS) {
                    this.detectedStarts.remove(this.detectedStarts.size() - 1);
                    this.detectedStarts.add(tss);
                }
                //else, we keep the lastDetectedStart, but discard the current transcription start site
            } else {
                this.detectedStarts.add(tss);
            }
        } else {
            this.detectedStarts.add(tss);
        }
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
    private int estimateCutoff(DiscreteCountingDistribution distribution, int thresholdEnlarger) {
        //genomeSize = total number of positions contributing to the increase distribution
        int maxEstimatedNbOfActiveGenes = (int) (this.genomeSize * 0.0025) + thresholdEnlarger; // 0,25% = 2,5 Genes per 1KB Genome size. This allows for variability.
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
        
        return distribution.getMinCountForIndex(selectedIndex - 1);
    }

    /**
     * Receives a Hashmap, checks if the current value is already present in the map
     * and increases it by one. If it is not present yet, it is created with a value of 0.
     * @param map the map whose data should be increased
     * @param value the value (key) which should be increased
     * @param threshold the threshold the value has to exceed, to be added to the map
     */
    private void increaseDistribution(HashMap<Integer, Integer> map, int value, int threshold) {
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
            this.trackViewer.getTrackCon().insertCoverageDistribution(covIncreaseDistribution);
            this.trackViewer.getTrackCon().insertCoverageDistribution(covIncPercentDistribution);
            if (this.tssAutomatic) {
                this.increaseReadCount = this.estimateCutoff(this.covIncreaseDistribution, 0);//(int) (this.genomeSize * 0.0005));
                this.increaseReadPercent = this.estimateCutoff(this.covIncPercentDistribution, 0);//(int) (this.genomeSize * 0.0005));
                this.correctTSSList();
            }
        }
    }

    /**
     * Removes all detected transcription start sites with a too small coverage increase, in case 
     * the increaseReadCount was changed after calculating the list of detectedGenes.
     */
    private void correctTSSList() {
        int percentDiff;
        TranscriptionStart tss;
        for (int i = 0; i < this.detectedStarts.size(); ++i) {
            tss = this.detectedStarts.get(i);
            percentDiff = (int) (((double) tss.getStartCoverage() / (double) tss.getInitialCoverage()) * 100.0) - 100;
            if (tss.getStartCoverage() - tss.getInitialCoverage() < this.increaseReadCount
                    || percentDiff < this.increaseReadPercent) {
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
        System.out.println("old threshold read count: " + this.increaseReadCount);
        System.out.println("old threshold percent: " + this.increaseReadPercent);
        this.increaseReadCount = this.getNewThreshold(this.exactCovIncreaseDist, 0);//(int) (this.genomeSize * 0.0005));
        this.increaseReadPercent = this.getNewThreshold(this.exactCovIncPercDist, 0);//(int) (this.genomeSize * 0.0005));
        
        //remove detected starts with too low coverage increases
        List<TranscriptionStart> copiedDetectedStarts = new ArrayList<TranscriptionStart>(this.detectedStarts);
        int increase;
        int percentage;
        for (TranscriptionStart tss : this.detectedStarts) {
            increase = tss.getStartCoverage() - tss.getInitialCoverage();
            percentage = GeneralUtils.calculatePercentageIncrease(tss.getInitialCoverage(), tss.getStartCoverage());
            if ((   increase < this.increaseReadCount ||
                    percentage < this.increaseReadPercent) &&
                    tss.getInitialCoverage() > this.maxInitialReadCount) {
                copiedDetectedStarts.remove(tss);
                System.out.println(tss.getPos());
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
    private int getNewThreshold(HashMap<Integer, Integer> distribution, int thresholdEnlarger) {
        int maxValue = (int) (this.genomeSize * 0.0025 + thresholdEnlarger); // = 0,25% + thresholdEnlarger
        int nbValues = 0;
        List<Integer> keyList = new ArrayList<Integer>(distribution.keySet());
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

    public int getIncreaseReadCount() {
        return increaseReadCount;
    }

    public int getIncreaseReadCount2() {
        return increaseReadCount2;
    }

    public int getIncreaseReadPercent() {
        return increaseReadPercent;
    }

    public int getMaxInitialReadCount() {
        return maxInitialReadCount;
    }

    @Override
    public List<TranscriptionStart> getResults() {
        return this.detectedStarts;
    }
}
