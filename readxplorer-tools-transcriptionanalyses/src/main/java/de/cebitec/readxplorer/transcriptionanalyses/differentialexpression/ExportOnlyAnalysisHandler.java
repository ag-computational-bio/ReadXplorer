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
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.utils.polytree.Node;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author kstaderm
 */
public class ExportOnlyAnalysisHandler extends DeAnalysisHandler {

    private static final Logger LOG = LoggerFactory.getLogger( ExportOnlyAnalysisHandler.class.getName() );

    private DeAnalysisData data;
    private List<ResultDeAnalysis> results;


    public ExportOnlyAnalysisHandler( List<PersistentTrack> selectedTracks, int refGenomeID,
                                      File saveFile, Set<FeatureType> selectedFeatureTypes, int startOffset,
                                      int stopOffset, ParametersReadClasses readClassParams, ProcessingLog processingLog ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatureTypes, startOffset, stopOffset, readClassParams, processingLog );
        data = new DeAnalysisData( selectedTracks.size(), processingLog );
        data.setSelectedTracks( selectedTracks );
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() throws GnuR.PackageNotLoadableException, IllegalStateException, GnuR.UnknownGnuRException {

        prepareFeatures( data );
        prepareCountData( data, getAllCountData() );

        final PersistentFeature[] feature = data.getFeatures();

        final ProgressHandle progressHandle = ProgressHandle.createHandle( "Creating Count Data Table" );
        progressHandle.start( feature.length );
        final int[][] countData = new int[data.getSelectedTracks().size()][];
        final List<Object> regionNamesList = new ArrayList<>();
        int i = 0;
        while( data.hasCountData() ) {
            countData[i++] = data.pollFirstCountData();
        }
        List<List<Object>> tableContents = new ArrayList<>();

        final ReferenceConnector referenceConnector = getReferenceConnector();
        Map<Integer, PersistentChromosome> chromosomesForGenome = referenceConnector.getChromosomesForGenome();

        for( i = 0; i < data.getFeatures().length; i++ ) {

            boolean allZero = true;
            final List<Object> tmp = new ArrayList<>();
            // Here the additional fields are added. If something is added don't
            // forget to also enter a additional colum name further down.
            tmp.add( feature[i].getLocus() );
            tmp.add( chromosomesForGenome.get( feature[i].getChromId() ) );
            if( feature[i].isFwdStrand() ) {
                tmp.add( "fw" );
            } else {
                tmp.add( "rv" );
            }
            tmp.add( feature[i].getStart() );
            tmp.add( feature[i].getStop() );
            tmp.add( calculateFeatureTypeLength( feature[i], FeatureType.EXON ) );
            tmp.add( calculateFeatureTypeLength( feature[i], FeatureType.INTRON ) );
            tmp.add( feature[i].getLength() );
            tmp.add( feature[i].getType() );
            for( int j = 0; j < data.getSelectedTracks().size(); j++ ) {
                int value = countData[j][i];
                if( value != 0 ) {
                    allZero = false;
                }
                tmp.add( value );
            }
            if( !allZero ) {
                tableContents.add( tmp );
                regionNamesList.add( feature[i] );
            }
            progressHandle.progress( i );
        }

        String[] trackDescriptions = data.getTrackDescriptions();
        List<Object> colNames = new ArrayList<>( trackDescriptions.length + 10 );
        colNames.add( "Locus" );
        colNames.add( "Chromosome" );
        colNames.add( "Strand" );
        colNames.add( "Start" );
        colNames.add( "Stop" );
        colNames.add( "Exon length" );
        colNames.add( "Intron length" );
        colNames.add( "Feature length" );
        colNames.add( "Feature type" );
        colNames.addAll( Arrays.asList( trackDescriptions ) );

        results = Collections.singletonList( new ResultDeAnalysis( tableContents, colNames, regionNamesList, "Count Data Table" ) );
        progressHandle.finish();

        return results;

    }


    private static int calculateFeatureTypeLength( PersistentFeature feature, FeatureType type ) {

        int length = 0;
        for( Node n : feature.getNodeChildren() ) {

            FeatureType nodeType = n.getNodeType();
            if( nodeType == type ) {
                PersistentFeature current = (PersistentFeature) n;
                length += current.getLength();
            } else {
                PersistentFeature current = (PersistentFeature) n;
                length += calculateFeatureTypeLength( current, type );
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
