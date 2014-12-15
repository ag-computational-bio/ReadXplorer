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


import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.BaySeq.SamplesNotValidException;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.PackageNotLoadableException;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.UnknownGnuRException;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisHandler extends DeAnalysisHandler {

    private final List<Group> groups;
    private BaySeq baySeq;
    private final BaySeqAnalysisData baySeqAnalysisData;
    private final UUID key;


    public static enum Plot {

        Priors( "Priors" ),
        MACD( "\"MA\"-Plot for the count data" ),
        Posteriors( "Posterior likelihoods of differential expression against log-ratio" );
        String representation;


        Plot( String representation ) {
            this.representation = representation;
        }


        @Override
        public String toString() {
            return representation;
        }


    }


    public BaySeqAnalysisHandler( List<PersistentTrack> selectedTracks, List<Group> groups, Integer refGenomeID, int[] replicateStructure,
                                  File saveFile, Set<FeatureType> selectedFeatures, int startOffset, int stopOffset, ParametersReadClasses readClassParams, UUID key ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams );
        baySeq = new BaySeq();
        baySeqAnalysisData = new BaySeqAnalysisData( getSelectedTracks().size(), groups, replicateStructure );
        this.groups = groups;
        this.key = key;
    }


    @Override
    public void endAnalysis() {
        baySeq.shutdown( key );
        baySeq = null;
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        prepareFeatures( baySeqAnalysisData );
        prepareCountData( baySeqAnalysisData, getAllCountData() );
        List<ResultDeAnalysis> results = baySeq.process( baySeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile(), key );
        return results;
    }


    public File plot( Plot plot, Group group, int[] samplesA, int[] samplesB ) throws IOException, SamplesNotValidException,
                                                                                      IllegalStateException, PackageNotLoadableException {
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
        return groups;
    }


}
