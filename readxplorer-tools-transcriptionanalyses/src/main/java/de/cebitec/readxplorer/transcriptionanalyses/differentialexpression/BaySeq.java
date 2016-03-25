/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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


import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.PackageNotLoadableException;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPVector;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author kstaderm
 */
public class BaySeq {

    private GnuR gnuR;
    /*
     * The maximum number baySeq will use. This should prevent the programm from
     * using insanely mutch cores on big machines in the CeBiTec Grid
     * infrastructure.
     */
    private static final int MAX_PROCESSORS = 6;

    private static final Logger LOG = LoggerFactory.getLogger( BaySeq.class.getName() );


    public BaySeq() {
    }


    /**
     * Processes data from a differential expression analysis experiment using
     * the baySeq package.
     * <p>
     * @param bseqData         The prepared experiment data set.
     * @param numberOfFeatures The number of underlying features.
     * @param numberOfTracks   The number of underlying tracks.
     * @param saveFile         The Gnu R dataset will be saved to this file. If
     *                         no saving should be done just pass null here.
     * <p>
     * @return a List of RVector. Each RVector represents the results for one
     *         Group. The number of RVectors is always two times the number of
     *         committed groups because there is always one normalized and one
     *         not normalized result. Example: If you committed two groups. You
     *         will get four RVectors as an result. The first RVector will
     *         represent the not normalized result for the first committed
     *         group. The second will represent the not normalized result for
     *         the second group. The third result will then represent the
     *         normalized result for group one and the fourth result will
     *         represent the normalized result for group two. So you will first
     *         get all not normalized results and then all the normalized ones.
     */
    public List<ResultDeAnalysis> process( BaySeqAnalysisData bseqData,
                                           int numberOfFeatures, int numberOfTracks, File saveFile )
            throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        gnuR = GnuR.startRServe( bseqData.getProcessingLog() );
        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.info( "{0}: GNU R is processing data.", currentTimestamp );
        gnuR.loadPackage( "baySeq" );
        //Gnu R is configured to use all your processor cores aside from one up to a maximum of eight. So the
        //computation will speed up a little bit but still leave you at least one core
        //for your other work.
        if( gnuR.runningLocal ) {
            gnuR.loadPackage( "snow" );
            gnuR.loadPackage( "parallel" );
            int processors = Runtime.getRuntime().availableProcessors();
            if( processors > MAX_PROCESSORS ) {
                processors = MAX_PROCESSORS;
            }
            if( processors > 1 ) {
                processors--;
            }
            currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.info( "{0}: Gnu R running on " + processors + " cores.", currentTimestamp );
            gnuR.eval( "cl <- makeCluster(" + processors + ", \"SOCK\")" );
        } else {
            gnuR.eval( "cl <- NULL" );
        }
        List<ResultDeAnalysis> results = new ArrayList<>();
        //A lot of bad things can happen during the data processing by Gnu R.
        //So we need to prepare for this.
        try {
            int i = 1;
            StringBuilder concatenate = new StringBuilder( "c(" );
            while( bseqData.hasCountData() ) {
                gnuR.assign( "inputData" + i, bseqData.pollFirstCountData() );
                concatenate.append( "inputData" ).append( i++ ).append( ',' );
            }
            concatenate.deleteCharAt( concatenate.length() - 1 );
            concatenate.append( ")" );
            gnuR.eval( "inputData <- matrix(" + concatenate.toString() + ',' + numberOfFeatures + ')' );
            gnuR.assign( "inputFeaturesStart", bseqData.getStart() );
            gnuR.assign( "inputFeaturesStop", bseqData.getStop() );
            gnuR.assign( "inputFeaturesID", bseqData.getFeatureNames() );
            gnuR.eval( "features <- data.frame(inputFeaturesID,inputFeaturesStart,inputFeaturesStop)" );
            gnuR.eval( "colnames(features) <- c(\"locus\", \"start\", \"stop\")" );
            gnuR.eval( "seglens <- features$stop - features$start + 1" );
            gnuR.eval( "cD <- new(\"countData\", data = inputData, seglens = seglens, annotation = features)" );
            gnuR.eval( "libsizes(cD) <- getLibsizes(cD)" );
            gnuR.assign( "replicates", bseqData.getReplicateStructure() );
            gnuR.eval( "replicates(cD) <- as.factor(c(replicates))" );
            concatenate = new StringBuilder( 1000 );
            int numberofGroups = 0;
            while( bseqData.hasGroups() ) {
                numberofGroups++;
                gnuR.assign( "group" + numberofGroups, bseqData.getNextGroup() );
                concatenate.append( "group" ).append( numberofGroups ).append( "=" ).append( "group" ).append( numberofGroups ).append( ',' );
            }
            concatenate.deleteCharAt( concatenate.length() - 1 );
            gnuR.eval( "groups(cD) <- list(" + concatenate.toString() + ')' );
            //parameter samplesize could be added.
            gnuR.eval( "cD <- getPriors.NB(cD, cl = cl)" );
            gnuR.eval( "cD <- getLikelihoods.NB(cD, nullData = TRUE, cl = cl)" );
            int resultIndex = 0;
            for( int j = 1; j <= numberofGroups; j++ ) {
                gnuR.eval( "tCounts" + resultIndex + " <- topCounts(cD , group = " + j + " , number = " + numberOfFeatures + ')' );
                REXP result = gnuR.eval( "tCounts" + resultIndex );
                List<REXPVector> rvec = result.asList();
                REXP colNames = gnuR.eval( "colnames(tCounts" + resultIndex + ")" );
                REXP rowNames = gnuR.eval( "rownames(tCounts" + resultIndex + ")" );
                results.add( new ResultDeAnalysis( rvec, colNames, rowNames, "Result of model " + j, bseqData ) );
                resultIndex++;
            }
            for( int j = 1; j <= numberofGroups; j++ ) {
                gnuR.eval( "tCounts" + resultIndex + " <- topCounts(cD , group = " + j + " , number = " + numberOfFeatures + " , normaliseData=TRUE)" );
                REXP result = gnuR.eval( "tCounts" + resultIndex );
                List<REXPVector> rvec = result.asList();
                REXP colNames = gnuR.eval( "colnames(tCounts" + resultIndex + ')' );
                REXP rowNames = gnuR.eval( "rownames(tCounts" + resultIndex + ')' );
                results.add( new ResultDeAnalysis( rvec, colNames, rowNames, "Normalized result of model " + j, bseqData ) );
                resultIndex++;
            }
            if( saveFile != null ) {
                gnuR.saveDataToFile( saveFile );
            }
        } catch( Exception e ) { //We don't know what errors Gnu R might cause, so we have to catch all.
            //The new generated exception can than be caught an handelt by the DeAnalysisHandler
            //If something goes wrong try to shutdown Rserve so that no instance keeps running
            this.shutdown();
            throw new UnknownGnuRException( e );
        }
        currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.info( "{0}: GNU R finished processing data.", currentTimestamp );
        return results;
    }


    /**
     * Creates an MACD plot of the current data. The process(...) method must be
     * called before. If this is not done there will be no data in the Gnu R
     * memory which can be plotted. So this method will also not work after
     * calling clearGnuR() at least not until you have called process(...)
     * again.
     * <p>
     * @param file     a File the created SVG image should be saved to.
     * @param samplesA an int array representing the first sample group that
     *                 should be plotted.
     * @param samplesB an int array representing the secound sample group that
     *                 should be plotted. SamplesA and samplesB must not be the
     *                 same!
     * <p>
     * @throws SamplesNotValidException if SamplesA and samplesB are the same
     */
    public void plotMACD( File file, int[] samplesA, int[] samplesB ) throws SamplesNotValidException,
                                                                             IllegalStateException, PackageNotLoadableException,
                                                                             RserveException, REngineException, REXPMismatchException,
                                                                             IOException {
        if( !validateSamples( samplesA, samplesB ) ) {
            throw new SamplesNotValidException();
        }
        StringBuilder samplesABuilder = new StringBuilder( 1000 );
        samplesABuilder.append( (samplesA[0] + 1) ).append( ':' ).append( (samplesA[samplesA.length - 1] + 1) );
        StringBuilder samplesBBuilder = new StringBuilder( 1000 );
        samplesBBuilder.append( (samplesB[0] + 1) ).append( ':' ).append( (samplesB[samplesB.length - 1] + 1) );
        gnuR.storePlot( file, "plotMA.CD(cD, samplesA = " + samplesABuilder.toString() + ", " +
                              "samplesB = " + samplesBBuilder.toString() + ')' );
    }


    /**
     * Plots the posterior values of the current data. The process(...) method
     * must be called before. If this is not done there will be no data in the
     * Gnu R memory which can be plotted. So this method will also not work
     * after calling clearGnuR() at least not until you have called process(...)
     * again.
     * <p>
     * @param file     a File the created SVG image should be saved to.
     * @param group    the underlying group for the plot.
     * @param samplesA an int array representing the first sample group that
     *                 should be plotted.
     * @param samplesB an int array representing the secound sample group that
     *                 should be plotted. SamplesA and samplesB must not be the
     *                 same!
     * <p>
     * @throws SamplesNotValidException if SamplesA and samplesB are the same
     */
    public void plotPosteriors( File file, Group group, int[] samplesA, int[] samplesB ) throws SamplesNotValidException,
                                                                                                IllegalStateException, PackageNotLoadableException,
                                                                                                RserveException, REngineException, REXPMismatchException,
                                                                                                IOException {
        if( !validateSamples( samplesA, samplesB ) ) {
            throw new SamplesNotValidException();
        }
        StringBuilder samplesABuilder = new StringBuilder( 1000 );
        samplesABuilder.append( (samplesA[0] + 1) ).append( ':' ).append( (samplesA[samplesA.length - 1] + 1) );
        StringBuilder samplesBBuilder = new StringBuilder( 1000 );
        samplesBBuilder.append( (samplesB[0] + 1) ).append( ':' ).append( (samplesB[samplesB.length - 1] + 1) );
        gnuR.storePlot( file, "plotPosteriors(cD, group = " + group.getGnuRID() +
                              ", samplesA = " + samplesABuilder.toString() +
                              ", samplesB = " + samplesBBuilder.toString() +
                              ", col = c(rep(\"blue\", 100), rep(\"black\", 900))" +
                              ')' );
    }


    /**
     * Plots the prior values of the current data. The process(...) method must
     * be called before. If this is not done there will be no data in the Gnu R
     * memory which can be plotted. So this method will also not work after
     * calling clearGnuR() at least not until you have called process(...)
     * again.
     * <p>
     * @param file  a File the created SVG image should be saved to.
     * @param group the underlying group for the plot.
     */
    public void plotPriors( File file, Group group ) throws IllegalStateException, PackageNotLoadableException,
                                                            RserveException, REngineException, REXPMismatchException,
                                                            IOException {
        gnuR.storePlot( file, "plotPriors(cD, group = " + group.getGnuRID() + ')' );
    }


    public void saveResultsAsCSV( int index, File saveFile ) throws RserveException {
        String path = saveFile.getAbsolutePath();
        path = path.replace( "\\", "/" );
        gnuR.eval( "write.csv(tCounts" + index + ",file=\"" + path + "\")" );
    }


    /**
     * Validates if the samples A and B are not the same. For the MACD and
     * Posteriors plot samplesA and samplesB must not be the same.
     * <p>
     * @param samplA int array representing samplesA.
     * @param samplB int array representing samplesB.
     * <p>
     * @return true if samplA and samplB are not the same or else false
     */
    private boolean validateSamples( int[] samplA, int[] samplB ) {
        boolean inputValid = false;
        if( samplA.length != samplB.length ) {
            inputValid = true;
        } else {
            for( int i = 0; i < samplA.length; i++ ) {
                if( samplA[i] != samplB[i] ) {
                    inputValid = true;
                    break;
                }
            }
        }
        return inputValid;
    }


    /**
     * Releases the Gnu R instance and removes the reference to it.
     */
    public void shutdown() throws RserveException {
        //Might happen that gnuR is null if something went wrong during Rserve
        //startup or connection process.
        if( gnuR != null ) {
            gnuR.shutdown();
        }
    }


    /**
     * The SamplesNotValidException is thrown by the plotting methods.
     */
    public static class SamplesNotValidException extends Exception {

        public SamplesNotValidException() {
            super();
        }


    }

}
