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


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.GnuRAccess;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.PackageNotLoadableException;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.ProcessingLog;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;


/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisHandler extends DeAnalysisHandler {

    private DeSeq deSeq;
    private final DeSeqAnalysisData deSeqAnalysisData;


    public static enum Plot {

        DispEsts( "Gene dispersion vs. normalized mean expression" ),
        DE( "Log2 fold change vs. base means" ),
        HIST( "Histogram of p-values" ),
        MAplot( "MA Plot" );
        private final String representation;


        Plot( String representation ) {
            this.representation = representation;
        }


        @Override
        public String toString() {
            return representation;
        }


        public static Plot[] getValues( boolean moreThanTwoConditions ) {
            if( moreThanTwoConditions ) {
                return new Plot[]{ DispEsts };
            } else {
                return Plot.values();
            }
        }


    }


    public DeSeqAnalysisHandler( List<PersistentTrack> selectedTracks, Map<String, String[]> design, boolean moreThanTwoConditions,
                                 List<String> fittingGroupOne, List<String> fittingGroupTwo, Integer refGenomeID,
                                 boolean workingWithoutReplicates, File saveFile, Set<FeatureType> selectedFeatures, int startOffset,
                                 int stopOffset, ParametersReadClasses readClassParams, ProcessingLog processingLog ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams, processingLog );
        deSeq = new DeSeq();
        deSeqAnalysisData = new DeSeqAnalysisData( selectedTracks.size(), design, moreThanTwoConditions, fittingGroupOne,
                                                   fittingGroupTwo, workingWithoutReplicates, processingLog );
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        List<ResultDeAnalysis> results;
        prepareFeatures( deSeqAnalysisData );
        prepareCountData( deSeqAnalysisData, getAllCountData() );
        results = deSeq.process( deSeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile(),
                                 GnuRAccess.startRServe( deSeqAnalysisData.getProcessingLog() ) );
        return results;

    }


    public boolean moreThanTwoCondsForDeSeq() {
        return deSeqAnalysisData.moreThanTwoConditions();
    }


    @Override
    public void endAnalysis() throws RserveException {
        deSeq.shutdown();
        deSeq = null;
    }


    public File plot( Plot plot ) throws IOException, IllegalStateException, PackageNotLoadableException,
                                         RserveException, REngineException, REXPMismatchException {
        File file = File.createTempFile( "ReadXplorer_Plot_", ".svg" );
        file.deleteOnExit();
        if( plot == Plot.DE ) {
            deSeq.plotDE( file );
        }
        if( plot == Plot.DispEsts ) {
            deSeq.plotDispEsts( file );
        }
        if( plot == Plot.HIST ) {
            deSeq.plotHist( file );
        }
        return file;
    }


}
