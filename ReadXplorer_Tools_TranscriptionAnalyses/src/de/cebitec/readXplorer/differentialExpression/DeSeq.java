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

package de.cebitec.readXplorer.differentialExpression;


import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;


/**
 *
 * @author kstaderm
 */
public class DeSeq {

    private GnuR gnuR;


    public DeSeq() {
    }


    public List<ResultDeAnalysis> process( DeSeqAnalysisData analysisData,
                                           int numberOfFeatures, int numberOfTracks, File saveFile, UUID key )
            throws PackageNotLoadableException, JRILibraryNotInPathException,
                   IllegalStateException, UnknownGnuRException {
        gnuR = GnuR.SecureGnuRInitiliser.getGnuRinstance( key );
        gnuR.clearGnuR();
        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        Logger.getLogger( this.getClass().getName() ).log( Level.INFO, "{0}: GNU R is processing data.", currentTimestamp );
        gnuR.loadPackage( "DESeq" );
        List<ResultDeAnalysis> results = new ArrayList<>();
        //A lot of bad things can happen during the data processing by Gnu R.
        //So we need to prepare for this.
        try {
            //Load an R image containing the plotting functions
            try( InputStream jarPath = DeSeq.class.getResourceAsStream( "/de/cebitec/readXplorer/differentialExpression/DeSeqPlot.rdata" ) ) {
                File to = File.createTempFile( "ReadXplorer_", ".rdata" );
                to.deleteOnExit();
                Files.copy( jarPath, to.toPath(), StandardCopyOption.REPLACE_EXISTING );
                String tmpPath = to.getAbsolutePath();
                tmpPath = tmpPath.replace( "\\", "\\\\" );
                gnuR.eval( "load(file=\"" + tmpPath + "\")" );
            }
            catch( IOException ex ) {
                currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "{0}: Unable to load plotting functions. You woun't be able to plot your results!", currentTimestamp );
            }

            //Handing over the count data to Gnu R.
            int i = 1;
            StringBuilder concatenate = new StringBuilder( "c(" );
            //First the count data for each track is handed over seperatly.
            while( analysisData.hasCountData() ) {
                gnuR.assign( "inputData" + i, analysisData.pollFirstCountData() );
                concatenate.append( "inputData" ).append( i++ ).append( "," );
            }
            concatenate.deleteCharAt( concatenate.length() - 1 );
            concatenate.append( ")" );
            //Then the big count data matrix is created from the single track data handed over.
            gnuR.eval( "inputData <- matrix(" + concatenate.toString() + "," + numberOfFeatures + ")" );
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
            concatenate = new StringBuilder();
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
                gnuR.eval( "design <- data.frame(row.names = colnames(inputData)," + concatenate.toString() + ")" );
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval( "cD <- newCountDataSet(inputData, design)" );
            }
            else {
                //If this is just a two conditons experiment we only create the conds array
                gnuR.eval( "conds <- factor(" + concatenate.toString() + ")" );
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval( "cD <- newCountDataSet(inputData, conds)" );
            }

            //We estimate the size factor
            gnuR.eval( "cD <- estimateSizeFactors(cD)" );

            //For multi condition testing estimateDispersions does not converge most of the
            //times. So we relax the settings in every step a little more.
            if( analysisData.moreThanTwoConditions() ) {
                REXP res = gnuR.eval( "cD <- estimateDispersions(cD)" );
                if( res == null ) {
                    res = gnuR.eval( "cD <- estimateDispersions(cD,fitType=\"local\")" );
                    if( res == null ) {
                        res = gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\")" );
                        if( res == null ) {
                            gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\",fitType=\"local\")" );
                        }
                    }
                }
            }
            else {
                if( analysisData.isWorkingWithoutReplicates() ) {
                    // If there are no replicates for each condition we need to tell
                    // the function to ignore this fact.
                    REXP res = gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\")" );
                    //For some reasons the above computation fails on some data sets.
                    //In those cases the following computation should do the trick.
                    if( res == null ) {
                        gnuR.eval( "cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\",fitType=\"local\")" );
                    }
                }
                else {
                    //The dispersion is estimated
                    REXP res = gnuR.eval( "cD <- estimateDispersions(cD)" );
                    //For some reasons the above computation fails on some data sets.
                    //In those cases the following computation should do the trick.
                    if( res == null ) {
                        gnuR.eval( "cD <- estimateDispersions(cD,fitType=\"local\")" );
                    }
                }
            }

            if( analysisData.moreThanTwoConditions() ) {
                //Handing over the first fitting group to Gnu R...
                concatenate = new StringBuilder();
                List<String> fittingGroupOne = analysisData.getFittingGroupOne();
                for( String current : fittingGroupOne ) {
                    concatenate.append( current ).append( "+" );
                }
                concatenate.deleteCharAt( concatenate.length() - 1 );
                gnuR.eval( "fit1 <- fitNbinomGLMs( cD, count ~ " + concatenate.toString() + " )" );

                //..and then the secound one.
                concatenate = new StringBuilder();
                List<String> fittingGroupTwo = analysisData.getFittingGroupTwo();
                for( String current : fittingGroupTwo ) {
                    concatenate.append( current ).append( "+" );
                }
                concatenate.deleteCharAt( concatenate.length() - 1 );
                gnuR.eval( "fit0 <- fitNbinomGLMs( cD, count ~ " + concatenate.toString() + " )" );

                gnuR.eval( "pvalsGLM <- nbinomGLMTest( fit1, fit0 )" );
                gnuR.eval( "padjGLM <- p.adjust( pvalsGLM, method=\"BH\" )" );

            }
            else {
                //Perform the normal test.
                String[] levels = analysisData.getLevels();
                gnuR.eval( "res <- nbinomTest( cD,\"" + levels[0] + "\",\"" + levels[1] + "\")" );
            }
            if( analysisData.moreThanTwoConditions() ) {
                gnuR.eval( "tmp0 <- data.frame(fit1,pvalsGLM,padjGLM)" );
                gnuR.eval( "res0 <- data.frame(rownames(tmp0),tmp0)" );
                REXP currentResult1 = gnuR.eval( "res0" );
                RVector tableContents1 = currentResult1.asVector();
                REXP colNames1 = gnuR.eval( "colnames(res0)" );
                REXP rowNames1 = gnuR.eval( "rownames(res0)" );
                results.add( new ResultDeAnalysis( tableContents1, colNames1, rowNames1, "Fitting Group One", analysisData ) );

                gnuR.eval( "tmp1 <- data.frame(fit0,pvalsGLM,padjGLM)" );
                gnuR.eval( "res1 <- data.frame(rownames(tmp1),tmp1)" );
                REXP currentResult0 = gnuR.eval( "res1" );
                RVector tableContents0 = currentResult0.asVector();
                REXP colNames0 = gnuR.eval( "colnames(res1)" );
                REXP rowNames0 = gnuR.eval( "rownames(res1)" );
                results.add( new ResultDeAnalysis( tableContents0, colNames0, rowNames0, "Fitting Group Two", analysisData ) );

            }
            else {
                //Significant results sorted by the most significantly differentially expressed genes
                gnuR.eval( "res0 <- res[order(res$pval), ]" );
                REXP result = gnuR.eval( "res0" );
                RVector rvec = result.asVector();
                REXP colNames = gnuR.eval( "colnames(res0)" );
                REXP rowNames = gnuR.eval( "rownames(res0)" );
                results.add( new ResultDeAnalysis( rvec, colNames, rowNames,
                                                   "Significant results sorted by the most significantly differentially expressed genes", analysisData ) );

                //Significant results sorted by the most strongly down regulated genes
                gnuR.eval( "res1 <- res[order(res$foldChange, -res$baseMean), ]" );
                result = gnuR.eval( "res1" );
                rvec = result.asVector();
                colNames = gnuR.eval( "colnames(res1)" );
                rowNames = gnuR.eval( "rownames(res1)" );
                results.add( new ResultDeAnalysis( rvec, colNames, rowNames,
                                                   "Significant results sorted by the most strongly down regulated genes", analysisData ) );

                //Significant results sorted by the most strongly up regulated genes
                gnuR.eval( "res2 <- res[order(-res$foldChange, -res$baseMean), ]" );
                result = gnuR.eval( "res2" );
                rvec = result.asVector();
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
        } //We don't know what errors Gnu R might cause, so we have to catch all.
        //The new generated exception can than be caught an handelt by the DeAnalysisHandler
        catch( Exception e ) {
            throw new UnknownGnuRException( e );
        }
        currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        Logger.getLogger( this.getClass().getName() ).log( Level.INFO, "{0}: GNU R finished processing data.", currentTimestamp );
        return results;
    }


    /**
     * Releases the Gnu R instance and removes the reference to it.
     */
    public void shutdown( UUID key ) {
        if( gnuR != null ) {
            gnuR.releaseGnuRInstance( key );
            gnuR = null;
        }
    }


    public void saveResultsAsCSV( int index, File saveFile ) {
        String path = saveFile.getAbsolutePath();
        path = path.replace( "\\", "/" );
        gnuR.eval( "write.csv(res" + index + ",file=\"" + path + "\")" );
    }


    public void plotDispEsts( File file ) throws IllegalStateException, PackageNotLoadableException {
        gnuR.storePlot( file, "plotDispEsts(cD)" );
    }


    public void plotDE( File file ) throws IllegalStateException, PackageNotLoadableException {
        gnuR.storePlot( file, "plotDE(res)" );
    }


    public void plotHist( File file ) throws IllegalStateException, PackageNotLoadableException {
        gnuR.storePlot( file, "hist(res$pval, breaks=100, col=\"skyblue\", border=\"slateblue\", main=\"\")" );
    }


}
