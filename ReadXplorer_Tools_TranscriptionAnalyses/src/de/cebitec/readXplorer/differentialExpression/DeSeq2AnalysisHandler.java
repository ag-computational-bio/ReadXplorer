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


import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 *
 * @author kstaderm
 */
public class DeSeq2AnalysisHandler extends DeAnalysisHandler {

    private DeSeq2 deSeq2;
    private final DeSeqAnalysisData deSeqAnalysisData;
    private final UUID key;


    public static enum Plot {

        DispEsts( "Per gene estimates against normalized mean expression" ),
        HIST( "Histogram of p values" );
        String representation;


        Plot( String representation ) {
            this.representation = representation;
        }


        @Override
        public String toString() {
            return representation;
        }


        public static Plot[] getValues() {
            return new Plot[]{ DispEsts, HIST };
        }


    }


    public DeSeq2AnalysisHandler( List<PersistentTrack> selectedTracks, Map<String, String[]> design,
                                  List<String> fittingGroupOne, List<String> fittingGroupTwo, Integer refGenomeID, boolean workingWithoutReplicates,
                                  File saveFile, Set<FeatureType> selectedFeatures, int startOffset, int stopOffset, ParametersReadClasses readClassParams, UUID key ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams );
        deSeq2 = new DeSeq2( this.getRefGenomeID() );
        this.key = key;
        deSeqAnalysisData = new DeSeqAnalysisData( selectedTracks.size(),
                                                   design, false, fittingGroupOne, fittingGroupTwo,
                                                   workingWithoutReplicates );
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        List<ResultDeAnalysis> results;
        prepareFeatures( deSeqAnalysisData );
        prepareCountData( deSeqAnalysisData, getAllCountData() );
        results = deSeq2.process( deSeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile(), key );
        return results;

    }


    public boolean moreThanTwoCondsForDeSeq() {
        return deSeqAnalysisData.moreThanTwoConditions();
    }


    @Override
    public void endAnalysis() {
        deSeq2.shutdown( key );
        deSeq2 = null;
    }


    public File plot( Plot plot ) throws IOException, IllegalStateException, PackageNotLoadableException {
        File file = File.createTempFile( "ReadXplorer_Plot_", ".svg" );
        file.deleteOnExit();
        if( plot == Plot.DispEsts ) {
            deSeq2.plotDispEsts( file );
        }
        if( plot == Plot.HIST ) {
            deSeq2.plotHist( file );
        }
        return file;
    }


}
