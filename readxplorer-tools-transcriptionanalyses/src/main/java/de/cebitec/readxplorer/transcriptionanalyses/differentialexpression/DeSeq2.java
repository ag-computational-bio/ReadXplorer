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
public class DeSeq2 {

    private GnuR gnuR;

    private static final Logger LOG = LoggerFactory.getLogger( DeSeq2.class.getName() );


    public DeSeq2( int referenceId ) {
    }


    public List<ResultDeAnalysis> process( DeSeqAnalysisData analysisData,
                                           int numberOfFeatures, int numberOfTracks, File saveFile )
            throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        gnuR = GnuR.startRServe( analysisData.getProcessingLog() );
        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.info( "{0}: GNU R is processing data.", currentTimestamp );
        List<ResultDeAnalysis> results = new ArrayList<>();
        //A lot of bad things can happen during the data processing by Gnu R.
        //So we need to prepare for this.
        try {
            gnuR.loadPackage( "DESeq2" );
            gnuR.loadPackage( "Biobase" );
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
                concatenate.append( subDesign.getKey() ).append( ',' );
            }
            concatenate.deleteCharAt( concatenate.length() - 1 );

            if( saveFile != null ) {
                String path = saveFile.getAbsolutePath();
                path = path.replace( "\\", "\\\\" );
                gnuR.eval( "save.image(\"" + path + "\")" );
            }

            if( analysisData.moreThanTwoConditions() ) {
                LOG.info( "moreThanTwoConditions is still a TODO" );
                //TODO
            } else {
                //If this is just a two conditons experiment we only create the conds array
                gnuR.eval( "conds <- factor(" + concatenate.toString() + ')' );
                gnuR.eval( "design <- data.frame(row.names = colnames(inputData),conds)" );
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval( "dds <- DESeqDataSetFromMatrix(countData = inputData, colData = design, design = ~ conds)" );
            }

            gnuR.eval( "dds <- DESeq(dds)" );
            gnuR.eval( "res <- results(dds)" );
            gnuR.eval( "res <- res[order(res$padj),]" );

            REXP currentResult1 = gnuR.eval( "as.data.frame(res)" );
            List<REXPVector> tableContents1 = currentResult1.asList();
            REXP colNames1 = gnuR.eval( "colnames(res)" );
            REXP rowNames1 = gnuR.eval( "rownames(res)" );
            results.add( new ResultDeAnalysis( tableContents1, colNames1, rowNames1, "Results", analysisData ) );

            if( saveFile != null ) {
                String path = saveFile.getAbsolutePath();
                path = path.replace( "\\", "\\\\" );
                gnuR.eval( "save.image(\"" + path + "\")" );
            }
        } catch( Exception e ) {
            //We don't know what errors Gnu R might cause, so we have to catch all.
            //The newly generated exception can than be caught and handelt by the DeAnalysisHandler
            //If something goes wrong tries to shutdown Rserve so that no instance keeps running
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
                                                 RserveException, REngineException, REXPMismatchException, IOException {
        gnuR.storePlot( file, "plotDispEsts(dds)" );
    }


    public void plotHist( File file ) throws IllegalStateException, PackageNotLoadableException,
                                             RserveException, REngineException, REXPMismatchException, IOException {
        gnuR.storePlot( file, "hist(res$pval, breaks=100, col=\"skyblue\", border=\"slateblue\", main=\"\")" );
    }
    
    public void plotMA( File file ) throws IllegalStateException, PackageNotLoadableException,
                                             RserveException, REngineException, REXPMismatchException, IOException {
        gnuR.storePlot( file, "plotMA(res, main=\"\")" );
    }
    
}
