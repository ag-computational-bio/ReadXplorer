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
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.polytree.Node;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    protected List<ResultDeAnalysis> processWithTool() throws GnuR.PackageNotLoadableException, IllegalStateException, GnuR.UnknownGnuRException {

        prepareFeatures( data );
        prepareCountData( data, getAllCountData() );

        final PersistentFeature[] feature = data.getFeatures();

        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Creating Count Data Table" );
        progressHandle.start( feature.length );
        final int[][] countData = new int[data.getSelectedTracks().size()][];
        final List<Object> regionNamesList = new ArrayList<>();
        int i = 0;
        while( data.hasCountData() ) {
            countData[i++] = data.pollFirstCountData();
        }
        List<List<Object>> tableContents = new ArrayList<>();

        final ReferenceConnector referenceConnector = getReferenceConnector();
        //This offset must correspond to the additional fields added by hand
        final int offset = 6;
        for( i = 0; i < data.getFeatures().length; i++ ) {

            boolean allZero = true;
            final Object[] tmp = new Object[data.getSelectedTracks().size() + offset];
            /*
            * Here the additional fields are added. If one field is added or
            * remove the "offset" value must be changed accordingly.
            */
            tmp[0] = referenceConnector.getChromosomeForGenome( feature[i].getChromId() );
            if( feature[i].isFwdStrand() ) {
                tmp[1] = "fw";
            }
            else {
                tmp[1] = "rv";
            }
            tmp[2] = feature[i].getStart();
            tmp[3] = feature[i].getStop();
            tmp[4] = calculateExonLength( feature[i] );
            tmp[5] = feature[i].getLength();
            for( int j = offset; j < data.getSelectedTracks().size() + offset; j++ ) {
                int value = countData[j - offset][i];
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

        String[] trackDescriptions = data.getTrackDescriptions();
        List<Object> colNames = new ArrayList<>( trackDescriptions.length + 10 );
        colNames.add( "Chromosome" );
        colNames.add( "Strand" );
        colNames.add( "Start" );
        colNames.add( "Stop" );
        colNames.add( "Exon length" );
        colNames.add( "Gene length" );
        colNames.addAll( Arrays.asList( trackDescriptions ) );

        results = Collections.singletonList( new ResultDeAnalysis( tableContents, colNames, regionNamesList, "Count Data Table" ) );
        progressHandle.finish();

        return results;

    }


    private static int calculateExonLength( PersistentFeature feature ) {

        int length = 0;
        for( Node n : feature.getNodeChildren() ) {

            FeatureType nodeType = n.getNodeType();
            if( nodeType == FeatureType.EXON ) {
                PersistentFeature current = (PersistentFeature) n;
                length += current.getLength();
            }
            else {
                PersistentFeature current = (PersistentFeature) n;
                length += calculateExonLength( current );
            }

        }

        return length;

    }


    @Override
    public void endAnalysis() {
        data = null;
        results = null;
    }


}
