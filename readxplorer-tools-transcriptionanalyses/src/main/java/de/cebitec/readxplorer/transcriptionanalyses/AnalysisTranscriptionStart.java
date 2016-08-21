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


import de.cebitec.readxplorer.api.enums.Distribution;
import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.objects.AnalysisI;
import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.Coverage;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageAndDiffResult;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageManager;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.DetectedFeatures;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TranscriptionStart;
import de.cebitec.readxplorer.transcriptionanalyses.logic.PrimaryTssFlagger;
import de.cebitec.readxplorer.transcriptionanalyses.logic.TssAssociater;
import de.cebitec.readxplorer.transcriptionanalyses.logic.TssLinker;
import de.cebitec.readxplorer.utils.DiscreteCountingDistribution;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.StatsContainer;
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Carries out the logic behind the transcription start site (TSS) anaylsis.
 * When executing the transcription start site detection increaseReadCount is
 * always active and maxInitialReadCount + increaseReadCount2 are optional
 * parameters. They can further constrain the search space (e.g. inc = 100, max
 * = 10, inc2 = 50 means that read starts above 50 with an initial read count of
 * 0-10 are detected as transcription start sites, but also all increases of 100
 * and bigger. When the parameters are switched, e.g. inc = 50, max = 10, inc2 =
 * 100, then all coverage increases above 100 with an initial read count of 0-10
 * are detected as transcription start sites, but for all positions with an
 * initial read count > 10 an increase of 50 read counts is enough to be
 * detected.
 * <p>
 * 1. Nach Coverage: a) More read starts than threshold of 99,75% of read starts
 * in data set b) Coverage Increase in percent larger than 99,75% of the
 * increase percentages 2. Nach Mappingstarts: a) Nach Chernoff-Formel b) Nach
 * Wahrscheinlichkeitsformel (Binomialverteilung)
 * <p>
 * @author -Rolf Hilker-
 */
public class AnalysisTranscriptionStart implements Observer,
                                                   AnalysisI<List<TranscriptionStart>> {

    private static final Logger LOG = LoggerFactory.getLogger( AnalysisTranscriptionStart.class.getName() );

    private final TrackConnector trackConnector;
    private ReferenceConnector refConnector;
    private Map<Integer, PersistentChromosome> chromosomes;
    private final ParameterSetTSS parametersTSS;
    private boolean isStrandBothOption;
    private boolean isBothFwdDirection;
    private boolean isFeatureStrand;
    protected List<TranscriptionStart> detectedStarts;
    private DiscreteCountingDistribution readStartDistribution;
    private DiscreteCountingDistribution covIncPercentDistribution;
    private boolean calcCoverageDistributions;

    //varibles for transcription start site detection
    protected CoverageManager currentCoverage;
    private int totalCovLastFwdPos;
    private int totalCovLastRevPos;
    private int totalReadStartsLastRevPos;
    private int lastFeatureIdxGenStartsFwd;
    private int lastFeatureIdxGenStartsRev;

    private final Map<Integer, Integer> exactReadStartDist = new HashMap<>(); //exact read start distribution
    private final Map<Integer, Integer> exactCovIncPercDist = new HashMap<>(); //exact coverage increase percent distribution


    /**
     * Carries out the logic behind the transcription start site analysis. When
     * executing the transcription start site detection increaseReadCount is
     * always active and maxInitialReadCount + increaseReadCount2 are optional
     * parameters. They can further constrain the search space (e.g. inc = 100,
     * max = 10, inc2 = 50 means that coverage increases above 50 with an
     * initial read count of 0-10 are detected as transcription start sites, but
     * also all increases of 100 and bigger. When the parameters are switched,
     * e.g. inc = 50, max = 10, inc2 = 100, then all coverage increases above
     * 100 with an initial read count of 0-10 are detected as transcription
     * start sites, but for all positions with an initial read count > 10 an
     * increase of 50 read counts is enough to be detected.
     * <p>
     * @param trackConnector the track viewer for which the analyses should be
     *                       carried out
     * @param parametersTSS  the parameter set for this TSS analysis
     */
    public AnalysisTranscriptionStart( TrackConnector trackConnector, ParameterSetTSS parametersTSS ) {
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
     * Initializes the initial data structures needed for a transcription start
     * site analysis.
     */
    private void initDatastructures() {

        refConnector = ProjectConnector.getInstance().getRefGenomeConnector( trackConnector.getRefGenome().getId() );
        chromosomes = refConnector.getChromosomesForGenome();

        isStrandBothOption = parametersTSS.getReadClassParams().isStrandBothOption();
        isBothFwdDirection = parametersTSS.getReadClassParams().isStrandBothFwdOption();
        isFeatureStrand = parametersTSS.getReadClassParams().isStrandFeatureOption();

        this.readStartDistribution = trackConnector.getCountDistribution( getReadStartDistributionType() );
        this.covIncPercentDistribution = trackConnector.getCountDistribution( getCovIncDistributionType() );
        this.calcCoverageDistributions = this.readStartDistribution.isEmpty() || this.covIncPercentDistribution.isEmpty();

        if( this.parametersTSS.isAutoTssParamEstimation() ) {
            this.parametersTSS.setMaxLowCovInitCount( 0 ); //set these values as default for the transcription start site automatic
            this.parametersTSS.setMinLowCovIncrease( 0 ); //avoids loosing smaller, low coverage increases, can only be set by the user
            this.parametersTSS.setMinNoReadStarts( 0 );
            this.parametersTSS.setMinPercentIncrease( 0 );
            int genomeLength = PersistentReference.calcWholeGenomeLength( chromosomes );
            if( !this.calcCoverageDistributions ) {
                parametersTSS.setMinNoReadStarts( this.estimateCutoff( genomeLength, readStartDistribution, 0 ) ); //+ 0,05%
                parametersTSS.setMinPercentIncrease( this.estimateCutoff( genomeLength, covIncPercentDistribution, 0 ) );// (int) (this.genomeSize / 1000)); //0,1%

            } else {
                calcInitialReadStartThreshold( genomeLength );
            }
            
            //the minimal increase is initially set to 10%, if the coverage distributions were not calculated yet
            parametersTSS.setMinPercentIncrease( calcCoverageDistributions ? 10 : parametersTSS.getMinPercentIncrease() );
        }
    }


    /**
     * Calculates the initial threshold for read starts. The largest value is
     * 10, but for small data sets the formula calculates the rounded up value
     * of number of mappings in data set / ( 0.1 * genome length ). This is
     * meant as a rough estimator for a useful initial threshold.
     * <p>
     * @param genomeLength Total length of the genome under analysis
     */
    private void calcInitialReadStartThreshold( int genomeLength ) {
        int noMappings = trackConnector.getTrackStats().getStatsMap().get( StatsContainer.NO_MAPPINGS );
        //for small read numbers calculate a lower initial threshold
        int defaultMinReadStarts = (int) Math.floor( noMappings / (0.1 * genomeLength) ); //always round down but the minimum is 1
        if( defaultMinReadStarts > 10 ) {
            defaultMinReadStarts = 10; // to ensure values smaller or equal to 10 as initial threshold
        } else if( defaultMinReadStarts < 1 ) {
            defaultMinReadStarts = 1;
        }
        this.parametersTSS.setMinNoReadStarts( defaultMinReadStarts ); //lowest default values for new data sets without an inital distribution in the database
    }


    /**
     * Detects TSSs for a new CoverageManager object or calls the finish method.
     * <p>
     * @param data the data to handle: Either CoverageManager or "1" = coverage
     *             querries are done.
     */
    @Override
    public void update( Object data ) {
        if( data instanceof CoverageAndDiffResult ) {
            CoverageAndDiffResult result = (CoverageAndDiffResult) data;
            this.detectTSSs( result );

        } else if( data instanceof Byte && ((Byte) data) == 1 ) {
            this.finish();
        }
    }


    /**
     * Method to be called when the analysis is finished. Stores the
     * distributions and corrects the results for automatic mode.
     */
    public void finish() {
        try {
            storeDistributions();
            if( parametersTSS.isAutoTssParamEstimation() ) {
                correctResult();
            }
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( new DatabaseException( ex.getMessage() +
                                                                    " TSS detection result parameters are not corrected!", ex ) );
        }
        linkTssInSameRegion();
    }


    /**
     * Carries out the detection of predicted transcription start sites.
     * <p>
     * @param result the coverage and diff result for predicting the
     *               transcription start sites.
     */
    public void detectTSSs( CoverageAndDiffResult result ) {

        CoverageManager coverage = result.getCovManager();
        CoverageManager readStarts = result.getReadStarts();
        int chromId = result.getRequest().getChromId();
        int chromLength = chromosomes.get( chromId ).getLength();
        List<PersistentFeature> chromFeatures = refConnector.getFeaturesForClosedInterval( 0, chromLength, chromId );
        currentCoverage = coverage;

        int leftBound = currentCoverage.getLeftBound();
        int fixedLeftBound = leftBound <= 0 ? 0 : leftBound - 1;
        int rightBound = currentCoverage.getRightBound();

        currentCoverage.setLeftBound( fixedLeftBound ); //add left coverage value from last request (or 0) to left
        readStarts.setLeftBound( fixedLeftBound ); //of all coverage arrays.

        Coverage totalCoverage = currentCoverage.getTotalCoverage( this.parametersTSS.getReadClassParams().getExcludedClasses() );
        Coverage totalStarts = readStarts.getTotalCoverage( this.parametersTSS.getReadClassParams().getExcludedClasses() );

        totalCoverage.setFwdCoverage( this.fixLeftCoverageBound( totalCoverage.getFwdCov(), totalCovLastFwdPos ) );
        totalCoverage.setRevCoverage( this.fixLeftCoverageBound( totalCoverage.getRevCov(), totalCovLastRevPos ) );
        totalStarts.setFwdCoverage( this.fixLeftCoverageBound( totalStarts.getFwdCov(), 0 ) ); //for read starts the left pos is not important
        totalStarts.setRevCoverage( this.fixLeftCoverageBound( totalStarts.getRevCov(), totalReadStartsLastRevPos ) ); //on fwd strand

        for( int i = fixedLeftBound; i < rightBound; i++ ) {
            this.gatherDataAndDetect( chromId, chromLength, chromFeatures, totalCoverage, totalStarts, i );
        }

        totalCovLastFwdPos = totalCoverage.getFwdCov( rightBound );
        totalCovLastRevPos = totalCoverage.getRevCov( rightBound );
        totalReadStartsLastRevPos = totalStarts.getRevCov( rightBound );
    }


    /**
     * Gathers the necessary data from a given set of coverage arrays.
     * Afterwards, the TSS detection is performed.
     * <p>
     * @param chromId       the chromosome to analyze
     * @param chromLength   the length of this chromosome
     * @param chromFeatures all features of the chromosome
     * @param coverage      the fwd coverage array of the selected mapping class
     * @param readStarts    the fwd read start array of the selected mapping
     *                      class
     * @param refPos        the currently investigated reference position
     *                      coordinate
     */
    private void gatherDataAndDetect( int chromId, int chromLength, List<PersistentFeature> chromFeatures,
                                      Coverage coverage, Coverage readStarts, int refPos ) {
        int[] covArrayFwd = coverage.getFwdCov();
        int[] covArrayRev = coverage.getRevCov();
        int[] readStartArrayFwd = readStarts.getFwdCov();
        int[] readStartArrayRev = readStarts.getRevCov();
        int pos = coverage.getInternalPos( refPos );
        int increaseFwd;
        int increaseRev;
        int readStartsFwd;
        int readStartsRev;
        int percentIncFwd;
        int percentIncRev;

        int fwdCov1 = covArrayFwd[pos];
        int revCov1 = covArrayRev[pos];
        int fwdCov2 = covArrayFwd[pos + 1];
        int revCov2 = covArrayRev[pos + 1];
        if( !isStrandBothOption ) { //calc values based on analysis strand selection (4 possibilities)
            if( isFeatureStrand ) { //default increases for correctly stranded libraries
                increaseFwd = fwdCov2 - fwdCov1;
                increaseRev = revCov1 - revCov2;
                percentIncFwd = GeneralUtils.calculatePercentageIncrease( fwdCov1, fwdCov2 );
                percentIncRev = GeneralUtils.calculatePercentageIncrease( revCov2, revCov1 );
                readStartsFwd = readStartArrayFwd[pos + 1];
                readStartsRev = readStartArrayRev[pos];

            } else { //extra increases for invertedly stranded libraries
                increaseRev = fwdCov1 - fwdCov2;
                increaseFwd = revCov2 - revCov1;
                percentIncRev = GeneralUtils.calculatePercentageIncrease( fwdCov2, fwdCov1 );
                percentIncFwd = GeneralUtils.calculatePercentageIncrease( revCov1, revCov2 );
                readStartsFwd = readStartArrayRev[pos];
                readStartsRev = readStartArrayFwd[pos + 1];
            }
        } else if( isBothFwdDirection ) { //TODO: Strand options disrupt distributions! calculate one for each strand option!
            increaseFwd = fwdCov2 - fwdCov1 + revCov2 - revCov1;
            increaseRev = 0;
            percentIncFwd = GeneralUtils.calculatePercentageIncrease( fwdCov1 + revCov1, fwdCov2 + revCov2 );
            percentIncRev = GeneralUtils.calculatePercentageIncrease( 0, 0 );
            readStartsFwd = readStartArrayFwd[pos + 1] + readStartArrayRev[pos + 1];
            readStartsRev = 0;

        } else {
            increaseFwd = 0;
            increaseRev = revCov1 - revCov2 + fwdCov1 - fwdCov2;
            percentIncFwd = GeneralUtils.calculatePercentageIncrease( 0, 0 );
            percentIncRev = GeneralUtils.calculatePercentageIncrease( revCov2 + fwdCov2, revCov1 + fwdCov1 );
            readStartsFwd = 0;
            readStartsRev = readStartArrayFwd[pos] + readStartArrayRev[pos];
        }

        if( this.calcCoverageDistributions ) {
            this.readStartDistribution.increaseDistribution( readStartsFwd );
            this.readStartDistribution.increaseDistribution( readStartsRev );
            this.covIncPercentDistribution.increaseDistribution( percentIncFwd );
            this.covIncPercentDistribution.increaseDistribution( percentIncRev );
        }

        this.detectStart( refPos, chromId, chromLength, chromFeatures, readStartsFwd, readStartsRev, increaseFwd, increaseRev, percentIncFwd, percentIncRev );
    }


    /**
     * Method for analyzing the coverage of one pair of neighboring positions
     * and detecting a transcription start site, if the parameters are
     * satisfied.
     * <p>
     * @param pos                the position defining the pair to analyse: pos
     *                           and (pos + 1) on the fwd strand the TSS pos is
     *                           "pos+1" and on the reverse strand the TSS
     *                           position is "pos"
     * @param chromId            chromosome id
     * @param chromLength        chromosome length
     * @param chromFeatures      chromosome features
     * @param readStartsFwd      readStartsFwd
     * @param readStartsRev      readStartsRev
     * @param increaseFwd        forward coverage increase
     * @param increaseRev        reverse coverage increase
     * @param percentIncreaseFwd forward coverage increase in percent
     * @param percentIncreaseRev reverse coverage increase in percent
     */
    private void detectStart( int pos, int chromId, int chromLength, List<PersistentFeature> chromFeatures, int readStartsFwd,
                              int readStartsRev, int increaseFwd, int increaseRev, int percentIncreaseFwd, int percentIncreaseRev ) {

        if( ((readStartsFwd <= parametersTSS.getMaxLowCovReadStarts() && readStartsFwd >= parametersTSS.getMinLowCovReadStarts()) ||
             readStartsFwd > parametersTSS.getMaxLowCovReadStarts() && readStartsFwd >= parametersTSS.getMinNoReadStarts()) &&
            percentIncreaseFwd > parametersTSS.getMinPercentIncrease() ) {

            DetectedFeatures detFeatures = this.findNextFeatures( pos + 1, chromLength, chromFeatures, true );
            addDetectStart( new TranscriptionStart( pos + 1, true, readStartsFwd,
                                                    percentIncreaseFwd, increaseFwd, detFeatures, trackConnector.getTrackID(), chromId ) );
        }
        if( ((readStartsRev <= parametersTSS.getMaxLowCovReadStarts() && readStartsRev >= parametersTSS.getMinLowCovReadStarts()) ||
             readStartsRev > parametersTSS.getMaxLowCovReadStarts() && readStartsRev >= parametersTSS.getMinNoReadStarts()) &&
            percentIncreaseRev > parametersTSS.getMinPercentIncrease() ) {

            DetectedFeatures detFeatures = this.findNextFeatures( pos, chromLength, chromFeatures, false );
            addDetectStart( new TranscriptionStart( pos, false, readStartsRev,
                                                    percentIncreaseRev, increaseRev, detFeatures, trackConnector.getTrackID(), chromId ) );
        }

        if( this.parametersTSS.isAutoTssParamEstimation() ) {
            //add values to exact counting data structures to refine threshold
            increaseDistribution( exactReadStartDist, readStartsFwd, parametersTSS.getMinNoReadStarts() );
            increaseDistribution( exactReadStartDist, readStartsRev, parametersTSS.getMinNoReadStarts() );
            increaseDistribution( exactCovIncPercDist, percentIncreaseFwd, parametersTSS.getMinPercentIncrease() );
            increaseDistribution( exactCovIncPercDist, percentIncreaseRev, parametersTSS.getMinPercentIncrease() );
        }
    }


    /**
     * Add the detected TSS to the result list.
     * <p>
     * @param tss The TSS to add
     */
    protected void addDetectStart( TranscriptionStart tss ) {
        this.detectedStarts.add( tss );
    }


    /**
     * Detects and returns the genomic features, which can be associated to the
     * given transcription start site and strand. This can be eiter a feature
     * starting at the predicted transcription start site, which would be a
     * correct start, or it will contain the maximal two closest features found
     * in a vicinity of 1000bp up- or downstream of the transcription start
     * site. If more than one feature start at the detected TSS position, only
     * the last fitting feature is returned as correct start.
     * <p>
     * @param tssPos      the predicted transcription start site position
     * @param isFwdStrand the strand, on which the transcription start site is
     *                    located
     * <p>
     * @return the genomic features, which can be associated to the given
     *         transcription start site and strand.
     */
    private DetectedFeatures findNextFeatures( int tssPos, int chromLength, List<PersistentFeature> chromFeatures, boolean isFwdStrand ) {
        final int maxFeatureDist = parametersTSS.getMaxFeatureDistance();
        int minStartPos = tssPos - maxFeatureDist < 0 ? 0 : tssPos - maxFeatureDist;
        int maxStartPos = tssPos + maxFeatureDist > chromLength ? chromLength : tssPos + maxFeatureDist;
        DetectedFeatures detectedFeatures = new DetectedFeatures();
        boolean fstFittingFeature = true;
        if( isFwdStrand ) {
            for( int i = this.lastFeatureIdxGenStartsFwd; i < chromFeatures.size(); i++ ) {
                PersistentFeature feature = chromFeatures.get( i );
                int start = feature.getStart();

                /*
                 * We use all features, because also mRNA or rRNA features can
                 * contribute to TSS detection, as they also depict expressed
                 * sequences from the reference
                 */
                if( start >= minStartPos && feature.isFwdStrand() && start <= maxStartPos ) {

                    if( fstFittingFeature ) {
                        this.lastFeatureIdxGenStartsFwd = i; //this is the first feature in the interval
                        fstFittingFeature = false;
                    }

                    if( start < tssPos && feature.getStop() > tssPos ) {
                        //store feature as next upstream feature, but search for closer
                        //upstream feature & correctly annotated transcription start site

                        /*
                         * Also check, if gene and CDS feature are available and
                         * covering each other. Handle this case by not storing
                         * the current feature, if it is a CDS feature
                         * completely covered by a gene feature. In all other
                         * cases the feature can be stored, since we also use
                         * CDS features for TSS detection, if no gene feature is
                         * available.
                         */
                        PersistentFeature upstreamAnno = detectedFeatures.getUpstreamFeature();
                        if( upstreamAnno != null &&
                            feature.getType() == FeatureType.CDS &&
                            upstreamAnno.getType() == FeatureType.GENE &&
                            upstreamAnno.getStop() >= feature.getStop() ) {
//                            LOG.info( null, "CDS covered by gene feature Fwd");
                            continue;
                        }

                        detectedFeatures.setUpstreamFeature( feature );

                    } else if( start == tssPos ) {
                        //store correctly annotated transcription start site
                        detectedFeatures.setCorrectStartFeature( feature );
                        detectedFeatures.setUpstreamFeature( null );
                        break;

                    } else if( start > tssPos ) {
                        /*
                         * Store next downstream feature, transcription start is
                         * earlier than annotated, except the current feature is
                         * a CDS feature and no gene feature is present for that
                         * gene, starting earlier.
                         */
                        if( feature.getType() == FeatureType.CDS && i + 1 < chromFeatures.size() &&
                            feature.getStart() == chromFeatures.get( i + 1 ).getStart() &&
                            chromFeatures.get( i + 1 ).getType() == FeatureType.GENE ) {
                            detectedFeatures.setDownstreamFeature( chromFeatures.get( i + 1 ) );
                            detectedFeatures.setIsLeaderless( isLeaderless( chromFeatures.get( i + 1 ), tssPos ) );
//                            LOG.info( null, "Gene covers CDS with same annotated TSS Fwd");

                        } else {
                            detectedFeatures.setDownstreamFeature( feature );
                            detectedFeatures.setIsLeaderless( isLeaderless( feature, tssPos ) );
                        }

                        break;
                    }

                } else if( start >= maxStartPos ) {
                    if( fstFittingFeature ) {
                        this.lastFeatureIdxGenStartsFwd = i; //this is the first feature in the interval
                    }
                    break;
                }
            }
        } else { //means: strand == SequenceUtils.STRAND_REV

            for( int i = this.lastFeatureIdxGenStartsRev; i < chromFeatures.size(); i++ ) {
                PersistentFeature feature = chromFeatures.get( i );
                int start = feature.getStop();

                if( start >= minStartPos && feature.isFwdStrand() == isFwdStrand && start <= maxStartPos ) {

                    if( fstFittingFeature ) {
                        this.lastFeatureIdxGenStartsRev = i; //this is the first feature in the interval
                        fstFittingFeature = false;
                    }

                    if( start < tssPos ) {
                        //store feature as next bigger feature, but search for closer
                        //bigger feature & correctly annotated transcription start site

                        /*
                         * Store next upstream feature. transcription start is
                         * earlier than annotated, except the current feature is
                         * a CDS feature and no gene feature is present for that
                         * gene, starting earlier.
                         */
                        PersistentFeature upstreamAnno = detectedFeatures.getUpstreamFeature();
                        if( upstreamAnno != null &&
                            feature.getType() == FeatureType.CDS &&
                            start == upstreamAnno.getStop() &&
                            upstreamAnno.getType() == FeatureType.GENE ) {
                            //TODO: this does not work if features start at the same position on rev and fwd strand!
//                            LOG.info( null, "CDS covered by gene feature Rev");
                            continue; // we want to keep the gene instead the CDS feature
                        }

                        detectedFeatures.setDownstreamFeature( feature );
                        detectedFeatures.setIsLeaderless( isLeaderless( feature, tssPos ) );

                    } else if( start == tssPos ) {
                        //store correctly annotated transcription start site
                        detectedFeatures.setCorrectStartFeature( feature );
                        detectedFeatures.setDownstreamFeature( null );
                        break;

                    } else if( start > tssPos && feature.getStart() < tssPos ) {
                        //store next upstream feature, translation start is further in gene

                        if( feature.getType() == FeatureType.CDS && i + 1 < chromFeatures.size() &&
                            chromFeatures.get( i + 1 ).getType() == FeatureType.GENE &&
                            chromFeatures.get( i + 1 ).getStart() <= feature.getStart() ) {
                            detectedFeatures.setUpstreamFeature( chromFeatures.get( i + 1 ) );
//                            LOG.info( null, "Gene covers CDS with same annotated TSS Rev");
                        } else {
                            detectedFeatures.setUpstreamFeature( feature );
                        }
                        break;
                    }

                } else if( start >= tssPos ) {
                    if( fstFittingFeature ) {
                        this.lastFeatureIdxGenStartsRev = i; //TODO: features should be sorted by stop pos for rev strand
                        fstFittingFeature = false;
                    }
                }
                if( start >= maxStartPos && feature.getStart() > maxStartPos ) {
                    break;
                }
            }
        }
        return detectedFeatures;
    }


    /**
     * @param feature feature to check
     * @param tssPos  tss position to check
     * <p>
     * @return true, if the feature is a leaderless feature, false otherwise
     */
    private boolean isLeaderless( PersistentFeature feature, int tssPos ) {
        return Math.abs( feature.getStartOnStrand() - tssPos ) <= parametersTSS.getMaxLeaderlessDistance();
    }


    /**
     * The method first checks for all neighboring transcription start site
     * pairs, if they need to be associated according to the given parameters
     * and their distance. Then it checks, if they are located within the given
     * maximum feature distance bp of the checked transcription start site on
     * the same strand. If that's the case, the transcription start site with
     * the higher number of TOTAL read starts is marked as "primary TSS". All
     * other TSS receive the "secondary TSS" flag.
     */
    private void linkTssInSameRegion() {

        if( detectedStarts.size() > 0 ) {
            detectedStarts.get( 0 ).setIsPrimary( true );
        }

        if( detectedStarts.size() > 1 ) {

            if( parametersTSS.isAssociateTss() ) {
                TssAssociater tssAssociater = new TssAssociater();
                tssAssociater.setParametersTSS( parametersTSS );
                tssAssociater.setDetectedStarts( Collections.unmodifiableList( detectedStarts ) );
                iterateTssForLinking( tssAssociater );
                detectedStarts = tssAssociater.getAssociatedTss();
            }
            PrimaryTssFlagger primaryTssFlagger = new PrimaryTssFlagger();
            primaryTssFlagger.setParametersTSS( parametersTSS );
            primaryTssFlagger.setDetectedStarts( Collections.unmodifiableList( detectedStarts ) );
            iterateTssForLinking( primaryTssFlagger );
        }
    }


    /**
     * Iterates all TSS in the result list and runs the TssLinker implementation
     * on each pair of neighboring TSS.
     * <p>
     * @param tssLinker A TssLinker implementation instance performing some
     *                  linking action for all neighboring TSS
     */
    private void iterateTssForLinking( TssLinker tssLinker ) {

        for( int tssIdx = 1; tssIdx < detectedStarts.size(); tssIdx++ ) {

            TranscriptionStart tss = detectedStarts.get( tssIdx );
            int prevTssIdx = tssIdx - 1;
            TranscriptionStart previousTss = this.detectedStarts.get( prevTssIdx );
            while( previousTss.isFwdStrand() != tss.isFwdStrand() && previousTss.getChromId() != tss.getChromId() && prevTssIdx > 0 ) {
                previousTss = this.detectedStarts.get( --prevTssIdx );
            }

            tssLinker.linkTss( previousTss, prevTssIdx, tss );
        }
    }


    /**
     * Used to computationally estimate the optimal cutoff = minimum increase of
     * read counts from one position to the next in total or in percent.<br>
     * 2 Parameters take care of this task:<br>
     * At first the index of the coverage increase distribution, which exceeds
     * the threshold of more than 2 genes per 1KB genome size for the first time
     * for this track is calculated.<br>
     * In prokaryotic genomes gene density is approx 1 per 1kb and max 1,16 in
     * Sulfolobus solfataricus according to I. B. Rogozin, et al., "Congruent
     * evolution of different classes of non-coding DNA in prokaryotic genomes",
     * Nucleic Acids Res, vol. 30, no. 19, pp. 4264–4271, Oct. 2002.
     * <p>
     * In this case we allow for 2 genes per kb, because we use two
     * distributions, each leading to one threshold.<br>
     * Since only positions exceeding both thresholds are reported later, the
     * result set is shrinked again.<br>
     * In the optimal case we gain very low numbers of false positives, but
     * still find all true positives.
     * <p>
     * @param distribution      the underlying distribution
     * @param thresholdEnlarger Absolute value if 0,2% is to tight as a
     *                          threshold, you can set a value to be added to
     *                          the standard 0,2% threshold. if 0,3% of the
     *                          whole distribution should be used as threshold
     *                          use "this.genomeSize * 3 / 1000"
     * <p>
     * @return The lower bound value associated with the index from the given
     *         <code>distribution</code> FIRST EXCEEDING the number of expected
     *         events for the analyzed genome. This is NOT yet the final cutoff,
     *         but rather an estimation!
     */
    private int estimateCutoff( int genomeLength, DiscreteCountingDistribution distribution, int thresholdEnlarger ) {
        //genomeSize = total number of positions contributing to the increase distribution
        int maxEstimatedNbOfActiveGenes = (int) (genomeLength * 0.0025) + thresholdEnlarger; // 0,25% = 2,5 Genes per 1KB Genome size. This allows for variability.
        int[] distributionValues = distribution.getDiscreteCountingDistribution();

        int nbTSSs = 0;
        int selectedIndex = 1;
        for( int i = distributionValues.length - 1; i > 0; i-- ) {
            // we use the index which first exceeds maxEstimatedNbOfActiveGenes
            if( nbTSSs < maxEstimatedNbOfActiveGenes ) {
                nbTSSs += distributionValues[i];
            } else {
                selectedIndex = i;
                break;
            }
        }
        /*
         * number of active genes in the current genome in prokaryotic genomes
         * gene density approx 1 per 1000bp, max 1,16 in Sulfolobus solfataricus
         * according to I. B. Rogozin, et al., "Congruent evolution of different
         * classes of non-coding DNA in prokaryotic genomes", Nucleic Acids Res,
         * vol. 30, no. 19, pp. 4264–4271, Oct. 2002.
         */
        return distribution.getMinValueForIndex( selectedIndex );
    }


    /**
     * Receives a Hashmap, checks if the current value is already present in the
     * map and increases it by one. If it is not present yet, it is created with
     * a value of 0.
     * <p>
     * @param map       the map whose data should be increased
     * @param value     the value (key) which should be increased
     * @param threshold the threshold the value has to exceed, to be added to
     *                  the map
     */
    private void increaseDistribution( Map<Integer, Integer> map, int value, int threshold ) {
        if( threshold > 1 ) {
            threshold--; //this value is also needed for small data sets
        }
        if( value >= threshold ) {
            if( !map.containsKey( value ) ) {
                map.put( value, 0 );
            }
            map.put( value, map.get( value ) + 1 );
        }
    }


    /**
     * If a new distribution was calculated, this method stores it in the DB and
     * corrects the result list with the new estimated parameters, if the
     * tssAutomatic was chosen.
     *
     * @throws DatabaseException An exception during data queries. It has
     *                           already been logged.
     */
    private void storeDistributions() throws DatabaseException {
        if( this.calcCoverageDistributions && this.trackConnector.getAssociatedTrackNames().size() == 1 ) {
            ProjectConnector.getInstance().insertCountDistribution( readStartDistribution, this.trackConnector.getTrackID() );
            ProjectConnector.getInstance().insertCountDistribution( covIncPercentDistribution, this.trackConnector.getTrackID() );
        }
    }


    /**
     * After detecting the transcription start sites the exact distribution of
     * coverage increases is known and the threshold can be adapted for the
     * automatic parameter estimation mode. This method calculates the more
     * stringent thresholds first and then removes all transcription start sites
     * from the detected starts list, which cannot satisfy the new thresholds.
     * Prevents false positives.
     */
    private void correctResult() {

        //estimate exact cutoff for readcount increase of 0,25%
        LOG.info( "old threshold read count: {0}", parametersTSS.getMinNoReadStarts() );
        LOG.info( "old threshold percent: {0}", parametersTSS.getMinPercentIncrease() );
        parametersTSS.setMinNoReadStarts( getNewThreshold( exactReadStartDist, 0 ) );//(int) (this.genomeSize * 0.0005));
        parametersTSS.setMinPercentIncrease( getNewThreshold( exactCovIncPercDist, 0 ) );//(int) (this.genomeSize * 0.0005));

        //remove detected starts with too low coverage increases
        List<TranscriptionStart> copiedDetectedStarts = new ArrayList<>( detectedStarts );
        for( TranscriptionStart tss : detectedStarts ) {

            if( (tss.getReadStartsAtPos() < parametersTSS.getMinNoReadStarts() ||
                 tss.getPercentIncrease() < parametersTSS.getMinPercentIncrease()) // && tss.getReadStartsAtPos() > parametersTSS.getMaxLowCovReadStarts()
                    ) {
                copiedDetectedStarts.remove( tss );
            }
        }
        this.detectedStarts = copiedDetectedStarts;
    }


    /**
     * Calculates the exact threshold for a map containing a coverage increase
     * or read start distribution. The threshold is set exactly to 0,25% and can
     * be enlarged by setting the threshold enlarger.
     * <p>
     * @param distribution      The exact distribution of coverage increases or
     *                          read starts
     * @param thresholdEnlarger Absolute value to be added to the new threshold
     * <p>
     * @return The exact read start or coverage increase cutoff threshold
     */
    private int getNewThreshold( Map<Integer, Integer> distribution, int thresholdEnlarger ) {
        int maxValue = (int) (PersistentReference.calcWholeGenomeLength( chromosomes ) * 0.0025 + thresholdEnlarger);
        maxValue /= chromosomes.values().size();
        int nbValues = 0;
        List<Integer> keyList = new ArrayList<>( distribution.keySet() );
        Collections.sort( keyList );

        for( int i = keyList.size() - 1; i > 0; --i ) {
            if( nbValues < maxValue ) {
                nbValues += distribution.get( keyList.get( i ) );
            } else {
                return keyList.get( i - 1 );
            }
        }

        if( keyList.isEmpty() ) {
            return 4;
        } else {
            return keyList.get( 0 );
        }
    }


    /**
     * @return An updated set of the current parameters. It can be updated,
     *         because this analysis contains an automatic parameter
     *         erstimation.
     */
    public ParameterSetTSS getParametersTSS() {
        return this.parametersTSS;
    }


    @Override
    public List<TranscriptionStart> getResults() {
        return Collections.unmodifiableList( detectedStarts );
    }


    /**
     * Adds the coverage of the "lastFwdPos" to the beginning of the given
     * coverage array - resulting array length = oldLength + 1.
     * <p>
     * @param covArray the coverage array to which a new left bound shall be
     *                 added
     * @param lastCov  the last coverage value of the previous request or 0
     * <p>
     * @return the new coverage array including the added coverage value
     */
    private int[] fixLeftCoverageBound( int[] covArray, int lastCov ) {
        int[] newCovArray = new int[covArray.length + 1];
        newCovArray[0] = lastCov;
        System.arraycopy( covArray, 0, newCovArray, 1, covArray.length );
        return newCovArray;
    }


    /**
     * @return The read start distribution type for the selected analysis strand
     *         option.
     */
    private Distribution getReadStartDistributionType() {
        Distribution distType;
        if( isStrandBothOption ) {
            if( isBothFwdDirection ) {
                distType = Distribution.ReadStartBothFwdStrand;
            } else {
                distType = Distribution.ReadStartBothRevStrand;
            }
        } else if( isFeatureStrand ) {
            distType = Distribution.ReadStartFeatStrand;
        } else {
            distType = Distribution.ReadStartOppStrand;
        }
        return distType;
    }


    /**
     * @return The coverage increase in percent distribution type for the
     *         selected analysis strand option.
     */
    private Distribution getCovIncDistributionType() {
        Distribution distType;
        if( isStrandBothOption ) {
            if( isBothFwdDirection ) {
                distType = Distribution.CovIncPercentBothFwdStrand;
            } else {
                distType = Distribution.CovIncPercentBothRevStrand;
            }
        } else if( isFeatureStrand ) {
            distType = Distribution.CovIncPercentFeatStrand;
        } else {
            distType = Distribution.CovIncPercentOppStrand;
        }
        return distType;
    }


}
