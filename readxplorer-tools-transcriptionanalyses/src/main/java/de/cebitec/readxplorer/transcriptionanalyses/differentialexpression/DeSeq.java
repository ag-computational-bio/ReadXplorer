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
public class DeSeq {

    private GnuR gnuR;

    private static final Logger LOG = LoggerFactory.getLogger( DeSeq.class.getName() );


    public DeSeq() {
    }


    public List<ResultDeAnalysis> process( DeSeqAnalysisData analysisData,
                                           int numberOfFeatures, int numberOfTracks, File saveFile )
            throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        gnuR = GnuR.startRServe( analysisData.getProcessingLog() );
        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.info( "{0}: GNU R is processing data.", currentTimestamp );
        gnuR.loadPackage( "DESeq" );
        final List<ResultDeAnalysis> results = new ArrayList<>();
        //A lot of bad things can happen during the data processing by Gnu R.
        //So we need to prepare for this.
        try {
            //Create the plotting functions as they are not part of the DESeq package
            createPlotFunctions();

            //Handing over the count data to Gnu R.
            int i = 1;
            StringBuilder concatenate = new StringBuilder( "c(" );
            //First the count data for each track is handed over seperatly.
            while( analysisData.hasCountData() ) {
                gnuR.assign( "inputData" + i, analysisData.pollFirstCountData() );
                concatenate.append( "inputData" ).append( i++ ).append( ',' );
            }
            concatenate.deleteCharAt( concatenate.length() - 1 );
            concatenate.append( ')' );
            //Then the big count data matrix is created from the single track data handed over.
            gnuR.eval( "inputData <- matrix(" + concatenate.toString() + ',' + numberOfFeatures + ')' );
            //The colum names are handed over to Gnu R...
            gnuR.assign( "columNames", analysisData.getTrackDescriptions() );
            //...and assigned to the count data matrix.
            gnuR.eval( "colnames(inputData) <- columNames" );
            //Now we need to name the rows. First hand over the row names to Gnu R...
            gnuR.assign( "rowNames", analysisData.getFeatureNames() );
            //...and then assign them to the count data matrix.
            gnuR.eval( "rownames(inputData) <- rowNames" );
            //Remove all the sides that don't appear under any condition because
            //those rows produce "NA" rows in the results table.
            gnuR.eval( "inputData <- inputData[rowSums(inputData) > 0,]" );

            //Now we need to hand over the experimental design behind the data.
            concatenate = new StringBuilder( 1000 );
            //First all sub designs are assigned to an individual variable.
            while( analysisData.hasNextSubDesign() ) {
                DeSeqAnalysisData.ReturnTupel subDesign = analysisData.getNextSubDesign();
                gnuR.assign( subDesign.getKey(), subDesign.getValue() );
                concatenate.append( subDesign.getKey() ).append( "," );
            }
            concatenate.deleteCharAt( concatenate.length() - 1 );

            if( saveFile != null ) {
                String path = saveFile.getAbsolutePath();
                path = path.replace( "\\", "\\\\" );
                gnuR.eval( "save.image(\"" + path + "\")" );
            }

            if( analysisData.moreThanTwoConditions() ) {
                //The individual variables are then used to create the design element
                gnuR.eval( "design <- data.frame(row.names = colnames(inputData)," + concatenate.toString() + ')' );
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval( "cD <- newCountDataSet(inputData, design)" );
            } else {
                //If this is just a two conditons experiment we only create the conds array
                gnuR.eval( "conds <- factor(" + concatenate.toString() + ')' );
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval( "cD <- newCountDataSet(inputData, conds)" );
            }

            //We estimate the size factor
            gnuR.eval( "cD <- estimateSizeFactors(cD)" );

            //For multi condition testing estimateDispersions does not converge most of the
            //times. So we relax the settings in every step a little more.
            if( analysisData.moreThanTwoConditions() ) {
                try {
                    gnuR.eval( "cD <- estimateDispersions(cD)" );
                } catch( RserveException e ) {
                    try {
                        gnuR.eval( "cD <- estimateDispersions(cD,fitType=\"local\")" );
                    } catch( RserveException e2 ) {
                        try {
                            gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\")" );
                        } catch( RserveException e3 ) {
                            gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\",fitType=\"local\")" );
                        }
                    }
                }
            } else if( analysisData.isWorkingWithoutReplicates() ) {
                // If there are no replicates for each condition we need to tell
                // the function to ignore this fact.
                try {
                    gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\")" );
                } catch( RserveException e ) {
                    //For some reasons the above computation fails on some data sets.
                    //In those cases the following computation should do the trick.
                    gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\",fitType=\"local\")" );
                }
            } else {
                //The dispersion is estimated
                try {
                    gnuR.eval( "cD <- estimateDispersions(cD)" );
                } catch( RserveException e ) {
                    //For some reasons the above computation fails on some data sets.
                    //In those cases the following computation should do the trick.
                    gnuR.eval( "cD <- estimateDispersions(cD,fitType=\"local\")" );
                }
            }

            if( analysisData.moreThanTwoConditions() ) {
                //Handing over the first fitting group to Gnu R...
                concatenate = new StringBuilder( 1000 );
                List<String> fittingGroupOne = analysisData.getFittingGroupOne();
                for( String current : fittingGroupOne ) {
                    concatenate.append( current ).append( '+' );
                }
                concatenate.deleteCharAt( concatenate.length() - 1 );
                gnuR.eval( "fit1 <- fitNbinomGLMs( cD, count ~ " + concatenate.toString() + " )" );

                //..and then the secound one.
                concatenate = new StringBuilder();
                List<String> fittingGroupTwo = analysisData.getFittingGroupTwo();
                for( String current : fittingGroupTwo ) {
                    concatenate.append( current ).append( '+' );
                }
                concatenate.deleteCharAt( concatenate.length() - 1 );
                gnuR.eval( "fit0 <- fitNbinomGLMs( cD, count ~ " + concatenate.toString() + " )" );

                gnuR.eval( "pvalsGLM <- nbinomGLMTest( fit1, fit0 )" );
                gnuR.eval( "padjGLM <- p.adjust( pvalsGLM, method=\"BH\" )" );

            } else {
                //Perform the normal test.
                String[] levels = analysisData.getLevels();
                gnuR.eval( "res <- nbinomTest( cD,\"" + levels[0] + "\",\"" + levels[1] + "\")" );
            }
            if( analysisData.moreThanTwoConditions() ) {
                gnuR.eval( "tmp0 <- data.frame(fit1,pvalsGLM,padjGLM)" );
                gnuR.eval( "res0 <- data.frame(rownames(tmp0),tmp0)" );
                REXP currentResult1 = gnuR.eval( "res0" );
                List<REXPVector> tableContents1 = currentResult1.asList();
                REXP colNames1 = gnuR.eval( "colnames(res0)" );
                REXP rowNames1 = gnuR.eval( "rownames(res0)" );
                results.add( new ResultDeAnalysis( tableContents1, colNames1, rowNames1, "Fitting Group One", analysisData ) );

                gnuR.eval( "tmp1 <- data.frame(fit0,pvalsGLM,padjGLM)" );
                gnuR.eval( "res1 <- data.frame(rownames(tmp1),tmp1)" );
                REXP currentResult0 = gnuR.eval( "res1" );
                List<REXPVector> tableContents0 = currentResult0.asList();
                REXP colNames0 = gnuR.eval( "colnames(res1)" );
                REXP rowNames0 = gnuR.eval( "rownames(res1)" );
                results.add( new ResultDeAnalysis( tableContents0, colNames0, rowNames0, "Fitting Group Two", analysisData ) );

            } else {
                //Significant results sorted by the most significantly differentially expressed genes
                gnuR.eval( "res0 <- res[order(res$pval), ]" );
                REXP result = gnuR.eval( "res0" );
                List<REXPVector> rvec = result.asList();
                REXP colNames = gnuR.eval( "colnames(res0)" );
                REXP rowNames = gnuR.eval( "rownames(res0)" );
                results.add( new ResultDeAnalysis( rvec, colNames, rowNames,
                                                   "Significant results sorted by the most significantly differentially expressed genes", analysisData ) );

                //Significant results sorted by the most strongly down regulated genes
                gnuR.eval( "res1 <- res[order(res$foldChange, -res$baseMean), ]" );
                result = gnuR.eval( "res1" );
                rvec = result.asList();
                colNames = gnuR.eval( "colnames(res1)" );
                rowNames = gnuR.eval( "rownames(res1)" );
                results.add( new ResultDeAnalysis( rvec, colNames, rowNames,
                                                   "Significant results sorted by the most strongly down regulated genes", analysisData ) );

                //Significant results sorted by the most strongly up regulated genes
                gnuR.eval( "res2 <- res[order(-res$foldChange, -res$baseMean), ]" );
                result = gnuR.eval( "res2" );
                rvec = result.asList();
                colNames = gnuR.eval( "colnames(res2)" );
                rowNames = gnuR.eval( "rownames(res2)" );
                results.add( new ResultDeAnalysis( rvec, colNames, rowNames,
                                                   "Significant results sorted by the most strongly up regulated genes", analysisData ) );
            }
            if( saveFile != null ) {
                String path = saveFile.getAbsolutePath();
                path = path.replace( "\\", "\\\\" );
                gnuR.eval( "save.image(\"" + path + "\")" );
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
     * Releases the Gnu R instance and removes the reference to it.
     */
    public void shutdown() throws RserveException {
        //Might happen that gnuR is null if something went wrong during Rserve
        //startup or connection process.
        if( gnuR != null ) {
            gnuR.shutdown();
        }
    }


    public void saveResultsAsCSV( int index, File saveFile ) throws RserveException {
        String path = saveFile.getAbsolutePath();
        path = path.replace( "\\", "/" );
        gnuR.eval( "write.csv(res" + index + ",file=\"" + path + "\")" );
    }


    public void plotDispEsts( File file ) throws IllegalStateException, PackageNotLoadableException,
                                                 RserveException, REngineException, REXPMismatchException,
                                                 IOException {
        gnuR.storePlot( file, "plotDispEsts(cD)" );
    }


    public void plotDE( File file ) throws IllegalStateException, PackageNotLoadableException,
                                           RserveException, REngineException, REXPMismatchException,
                                           IOException {
        gnuR.storePlot( file, "plotDE(res)" );
    }


    public void plotHist( File file ) throws IllegalStateException, PackageNotLoadableException,
                                             RserveException, REngineException, REXPMismatchException,
                                             IOException {
        gnuR.storePlot( file, "hist(res$pval, breaks=100, col=\"skyblue\", border=\"slateblue\", main=\"\")" );
    }


    private void createPlotFunctions() throws RserveException {
        gnuR.eval( "plotDispEsts <- function( cds )\n" +
                   "{\n" +
                   "plot(\n" +
                   "rowMeans( counts( cds, normalized=TRUE ) ),\n" +
                   "fitInfo(cds)$perGeneDispEsts,\n" +
                   "pch = '.', log=\"xy\" )\n" +
                   "xg <- 10^seq( -.5, 5, length.out=300 )\n" +
                   "lines( xg, fitInfo(cds)$dispFun( xg ), col=\"red\" )\n" +
                   "}" );
        gnuR.eval( "plotDE <- function( res )\n" +
                   "plot(\n" +
                   "res$baseMean,\n" +
                   "res$log2FoldChange,\n" +
                   "log=\"x\", pch=20, cex=.3,\n" +
                   "col = ifelse( res$padj < .1, \"red\", \"black\" ) )" );
    }


}
