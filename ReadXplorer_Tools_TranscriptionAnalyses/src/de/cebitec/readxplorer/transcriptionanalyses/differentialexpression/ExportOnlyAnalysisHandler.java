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
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;


/**
 *
 * @author kstaderm
 */
public class ExportOnlyAnalysisHandler extends DeAnalysisHandler {

    private DeAnalysisData data;
    private List<ResultDeAnalysis> results;


    public ExportOnlyAnalysisHandler( List<PersistentTrack> selectedTracks, int refGenomeID, File saveFile, Set<FeatureType> selectedFeatureTypes, int startOffset, int stopOffset, ParametersReadClasses readClassParams ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatureTypes, startOffset, stopOffset, readClassParams );
        data = new DeAnalysisData( selectedTracks.size() );
        data.setSelectedTracks( selectedTracks );
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() throws GnuR.PackageNotLoadableException, GnuR.JRILibraryNotInPathException, IllegalStateException, GnuR.UnknownGnuRException {
        prepareFeatures( data );
        prepareCountData( data, getAllCountData() );

        final PersistentFeature[] feature = data.getFeatures();

        ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Creating Count Data Table" );
        progressHandle.start( feature.length );
        String[] trackDescriptions = data.getTrackDescriptions();
        final int[][] countData = new int[data.getSelectedTracks().size()][];
        final List<Object> regionNamesList = new ArrayList<>();
        int i = 0;
        while( data.hasCountData() ) {
            countData[i++] = data.pollFirstCountData();
        }
        List<List<Object>> tableContents = new ArrayList<>();
        for( i = 0; i < feature.length; i++ ) {
            boolean allZero = true;
            Integer[] tmp = new Integer[data.getSelectedTracks().size() + 3];
            tmp[0] = feature[i].getChromId();
            tmp[1] = feature[i].getStart();
            tmp[2] = feature[i].getStop();
            for( int j = 3; j < data.getSelectedTracks().size() + 3; j++ ) {
                int value = countData[j - 3][i];
                if( value != 0 ) {
                    allZero = false;
                }
                tmp[j] = value;
            }
            if( !allZero ) {
                tableContents.add( new Vector( Arrays.asList( tmp ) ) );
                regionNamesList.add( feature[i] );
            }
            progressHandle.progress( i );
        }
        List<Object> colNames = new ArrayList<>( trackDescriptions.length + 3 );
        colNames.add( "Chromosome" );
        colNames.add( "Start" );
        colNames.add( "Stop" );
        colNames.addAll( Arrays.asList( trackDescriptions ) );
//        Vector rowNames = new Vector( regionNamesList );

        results = new ArrayList<>();
        results.add( new ResultDeAnalysis( tableContents, colNames, regionNamesList, "Count Data Table" ) );
        progressHandle.finish();

        return results;

    }


    @Override
    public void endAnalysis() {
        data = null;
        results = null;
    }


}
