/*
 * Copyright (C) 2015 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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
import de.cebitec.readxplorer.api.enums.IntervalRequestData;
import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author kstaderm
 */
public class ExportOnlyContinuousAnalysisHandler extends DeAnalysisHandler {

    private static final Logger LOG = LoggerFactory.getLogger( ExportOnlyContinuousAnalysisHandler.class.getName() );
    private ReferenceConnector referenceConnector;
    private final List<PersistentFeature> genomeAnnos;
    private List<ResultDeAnalysis> results;
    private Map<PersistentTrack, Map<PersistentFeature, int[]>> allContinuousCoverageData;
    private int resultsReceivedBack = 0;


    public ExportOnlyContinuousAnalysisHandler( List<PersistentTrack> selectedTracks, int refGenomeID, File saveFile, Set<FeatureType> selectedFeatureTypes, int startOffset, int stopOffset, ParametersReadClasses readClassParams, ProcessingLog processingLog ) {
        super( selectedTracks, refGenomeID, saveFile, selectedFeatureTypes, startOffset, stopOffset, readClassParams, processingLog );
        genomeAnnos = new ArrayList<>();
        allContinuousCoverageData = new HashMap<>();
        results = new ArrayList<>();
    }


    /**
     * Starts the analysis and collects data for all selected tracks.
     */
    @Override
    public void startAnalysis() {
        try {
            allContinuousCoverageData.clear();
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.info( currentTimestamp + ": Starting to collect the necessary data for the differential gene expression analysis." );
            referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector( super.getRefGenomeID() );
            List<AnalysesHandler> allHandler = new ArrayList<>();
            genomeAnnos.clear();

            for( PersistentChromosome chrom : referenceConnector.getRefGenome().getChromosomes().values() ) {
                genomeAnnos.addAll( referenceConnector.getFeaturesForRegionInclParents( 1, chrom.getLength(),
                                                                                        super.getSelectedFeatureTypes(), chrom.getId() ) );
            }

            for( PersistentTrack currentTrack : super.getSelectedTracks() ) {
                try {
                    TrackConnector tc = (new SaveFileFetcherForGUI()).getTrackConnector( currentTrack );

                    CollectContinuousCoverageData collCovData = new CollectContinuousCoverageData(
                            genomeAnnos, super.getStartOffset(), super.getStopOffset(), super.getReadClassParams() );
                    allContinuousCoverageData.put( currentTrack, collCovData.getContinuousCountData() );
                    AnalysesHandler handler = new AnalysesHandler( tc, (DataVisualisationI) this,
                                                                   "Collecting coverage data for track " + currentTrack.getDescription() + ".", super.getReadClassParams() );
                    handler.setMappingsNeeded( true );
                    handler.setDesiredData( IntervalRequestData.ReducedMappings );
                    handler.registerObserver( collCovData );
                    allHandler.add( handler );
                } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                    SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                    getProcessingLog().addProperty( "Unresolved track", currentTrack );
                    notifyObservers( DeAnalysisHandler.AnalysisStatus.ERROR );
                    this.interrupt();
                    return;
                }
            }
            for( AnalysesHandler handler : allHandler ) {
                handler.startAnalysis();
            }
        } catch( DatabaseException e ) {
            LOG.error( e.getMessage(), e );
            notifyObservers( AnalysisStatus.ERROR );
            interrupt();
        }
    }


    @Override
    protected List<ResultDeAnalysis> processWithTool() {

        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Creating Count Data Table" );
        progressHandle.start( genomeAnnos.size() );
        final List<Object> regionNamesList = new ArrayList<>();
        List<List<Object>> tableContents = new ArrayList<>();
        StringBuilder sb;

        for( int i = 0; i < genomeAnnos.size(); i++ ) {

            try {
                final List<Object> tmp = new ArrayList<>();
                PersistentFeature currentFeature = genomeAnnos.get( i );
                /*
                 * Here the additional fields are added. If one field is added
                 * or remove the "offset" value must be changed accordingly.
                 */
                tmp.add( referenceConnector.getChromosomeForGenome( currentFeature.getChromId() ) );
                if( currentFeature.isFwdStrand() ) {
                    tmp.add( "fw" );
                } else {
                    tmp.add( "rv" );
                }
                tmp.add( currentFeature.getStart() );
                tmp.add( currentFeature.getStop() );
                tmp.add( ExportOnlyAnalysisHandler.calculateFeatureTypeLength( currentFeature, FeatureType.EXON ) );
                tmp.add( ExportOnlyAnalysisHandler.calculateFeatureTypeLength( currentFeature, FeatureType.INTRON ) );
                tmp.add( currentFeature.getLength() );
                tmp.add( currentFeature.getType() );

                boolean allZero = true;
                for( PersistentTrack currentTrack : super.getSelectedTracks() ) {
                    Map<PersistentFeature, int[]> allFeatures = allContinuousCoverageData.get( currentTrack );
                    int[] valuesForCurrentFeature = allFeatures.get( currentFeature );
                    sb = new StringBuilder( valuesForCurrentFeature.length * 2 );
                    for( int j = 0; j < valuesForCurrentFeature.length; j++ ) {
                        int currentValue = valuesForCurrentFeature[j];
                        if( currentValue > 0 ) {
                            allZero = false;
                        }
                        sb.append( currentValue ).append( "," );
                    }
                    sb.deleteCharAt( sb.length() - 1 );
                    tmp.add( sb.toString() );
                }
                if( !allZero ) {
                    tableContents.add( tmp );
                    regionNamesList.add( currentFeature );
                }
                progressHandle.progress( i );
            } catch( DatabaseException e ) {
                LOG.error( e.getMessage(), e );
                notifyObservers( AnalysisStatus.ERROR );
                interrupt();
            }
        }

        List<Object> colNames = new ArrayList<>();
        colNames.add( "Chromosome" );
        colNames.add( "Strand" );
        colNames.add( "Start" );
        colNames.add( "Stop" );
        colNames.add( "Exon length" );
        colNames.add( "Intron length" );
        colNames.add( "Feature length" );
        colNames.add( "Feature type" );
        colNames.addAll( super.getSelectedTracks() );

        results = Collections.singletonList( new ResultDeAnalysis( tableContents, colNames, regionNamesList, "Count Data Table" ) );
        progressHandle.finish();

        return results;
    }


    @Override
    public void endAnalysis() throws RserveException {
        allContinuousCoverageData = null;
    }


    @Override
    public List<ResultDeAnalysis> getResults() {
        return Collections.unmodifiableList( results );
    }


    @Override
    public synchronized void showData( Object data ) {
        if( ++resultsReceivedBack == allContinuousCoverageData.size() ) {
            results.clear();
            results.addAll( processWithTool() );
            notifyObservers( AnalysisStatus.FINISHED );
        }
    }


}
