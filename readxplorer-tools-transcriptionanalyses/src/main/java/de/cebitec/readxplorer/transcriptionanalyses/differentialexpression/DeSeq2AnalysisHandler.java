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
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.PackageNotLoadableException;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.UnknownGnuRException;
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
public class DeSeq2AnalysisHandler extends DeAnalysisHandler {

    private DeSeq2 deSeq2;
    private final DeSeqAnalysisData deSeqAnalysisData;


    public static enum Plot {

        DispEsts( "Per gene estimates against normalized mean expression" ),
        HIST( "Histogram of p-values" ),
        PADJ_HIST( "Histogram of adjusted p-values" ),
        MAplot( "MA Plot" );
        private final String representation;


        Plot( String representation ) {
            this.representation = representation;
        }


        @Override
        public String toString() {
            return representation;
        }


        public static Plot[] getValues() {
            return Plot.values();
        }


    }


    public DeSeq2AnalysisHandler( List<PersistentTrack> selectedTracks, Map<String, String[]> design,
                                  List<String> fittingGroupOne, List<String> fittingGroupTwo, Integer refGenomeID,
                                  boolean workingWithoutReplicates, File saveFile, Set<FeatureType> selectedFeatures,
                                  int startOffset, int stopOffset, ParametersReadClasses readClassParams, ProcessingLog processingLog ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams, processingLog );
        deSeq2 = new DeSeq2( this.getRefGenomeID() );
        deSeqAnalysisData = new DeSeqAnalysisData( selectedTracks.size(), design, false, fittingGroupOne,
                                                   fittingGroupTwo, workingWithoutReplicates, processingLog );
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        List<ResultDeAnalysis> results;
        prepareFeatures( deSeqAnalysisData );
        prepareCountData( deSeqAnalysisData, getAllCountData() );
        results = deSeq2.process( deSeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile() );
        return results;

    }


    public boolean moreThanTwoCondsForDeSeq() {
        return deSeqAnalysisData.moreThanTwoConditions();
    }


    @Override
    public void endAnalysis() throws RserveException {
        if( deSeq2 != null ) {
            deSeq2.shutdown();
            deSeq2 = null;
        }
    }


    public File plot( Plot plot ) throws IOException, IllegalStateException, PackageNotLoadableException,
                                         RserveException, REngineException, REXPMismatchException {
        File file = File.createTempFile( "ReadXplorer_Plot_", ".svg" );
        file.deleteOnExit();
        if( plot == Plot.DispEsts ) {
            deSeq2.plotDispEsts( file );
        }
        if( plot == Plot.HIST ) {
            deSeq2.plotHist( file );
        }
        if( plot == Plot.PADJ_HIST ) {
            deSeq2.plotPadjHist( file );
        }
        if( plot == Plot.MAplot ) {
            deSeq2.plotMA( file );
        }
        return file;
    }


}
