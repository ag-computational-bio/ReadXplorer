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
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.BaySeq.SamplesNotValidException;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.PackageNotLoadableException;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;


/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisHandler extends DeAnalysisHandler {

    private final List<Group> groups;
    private BaySeq baySeq;
    private final BaySeqAnalysisData baySeqAnalysisData;


    public static enum Plot {

        Priors( "Priors" ),
        MACD( "\"MA\"-Plot for the count data" ),
        Posteriors( "Posterior likelihoods of differential expression against log-ratio" );
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


    public BaySeqAnalysisHandler( List<PersistentTrack> selectedTracks, List<Group> groups, Integer refGenomeID,
                                  int[] replicateStructure, File saveFile, Set<FeatureType> selectedFeatures, int startOffset,
                                  int stopOffset, ParametersReadClasses readClassParams, ProcessingLog processingLog ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams, processingLog );
        baySeq = new BaySeq();
        baySeqAnalysisData = new BaySeqAnalysisData( getSelectedTracks().size(), groups, replicateStructure, processingLog );
        this.groups = groups;
    }


    @Override
    public void endAnalysis() throws RserveException {
        baySeq.shutdown();
        baySeq = null;
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        prepareFeatures( baySeqAnalysisData );
        prepareCountData( baySeqAnalysisData, getAllCountData() );
        List<ResultDeAnalysis> results = baySeq.process( baySeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile() );
        return results;
    }


    public File plot( Plot plot, Group group, int[] samplesA, int[] samplesB ) throws IOException, SamplesNotValidException,
                                                                                      IllegalStateException, PackageNotLoadableException,
                                                                                      RserveException, REngineException, REXPMismatchException {
        File file = File.createTempFile( "ReadXplorer_Plot_", ".svg" );
        file.deleteOnExit();
        if( plot == Plot.MACD ) {
            baySeq.plotMACD( file, samplesA, samplesB );
        }
        if( plot == Plot.Posteriors ) {
            baySeq.plotPosteriors( file, group, samplesA, samplesB );
        }
        if( plot == Plot.Priors ) {
            baySeq.plotPriors( file, group );
        }
        return file;
    }


    public List<Group> getGroups() {
        return Collections.unmodifiableList( groups );
    }


}
