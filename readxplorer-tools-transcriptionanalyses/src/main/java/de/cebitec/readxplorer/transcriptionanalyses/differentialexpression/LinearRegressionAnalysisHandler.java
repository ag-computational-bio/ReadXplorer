/*
 * Copyright (C) 2015 Agne Matuseviciute
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

import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import static java.util.logging.Level.INFO;



/**
 *
 * @author Agne Matuseviciute
 */
public class LinearRegressionAnalysisHandler extends DeAnalysisHandler{
    
    private static final Logger LOG = Logger.getLogger( LinearRegressionAnalysisHandler.class.getName() );
    private final int refGenomeID;
    private final int startOffset;
    private final int stopOffset;
    private LinearRegression linReg;
    private File saveFile = null;
    private final ParametersReadClasses readClassParams;
    private final List<PersistentTrack> selectedTracks;
    private final List<de.cebitec.readxplorer.utils.Observer> observerList = new ArrayList<>();

    private int resultsReceivedBack = 0;
    private ReferenceConnector referenceConnector;
    private final List<PersistentFeature> genomeAnnos;
    private final Set<FeatureType> selectedFeatureTypes;
    private final Map<Integer, Map<PersistentFeature, int[]>> allContinuousCoverageData;
    private List<ResultDeAnalysis> results;

    
    public LinearRegressionAnalysisHandler( List<PersistentTrack> selectedTracks,
                                     int refGenomeID, File saveFile, Set<FeatureType> selectedFeatureTypes, int startOffset, int stopOffset,
                                     ParametersReadClasses readClassParams ) {
         
        super( selectedTracks, refGenomeID, saveFile, selectedFeatureTypes, startOffset, stopOffset, readClassParams );
        ProcessingLog.getInstance().resetLog();
        this.selectedTracks = selectedTracks;
        this.refGenomeID = refGenomeID;
        this.saveFile = saveFile;
        this.startOffset = startOffset;
        this.stopOffset = stopOffset;
        this.readClassParams = readClassParams;
        this.selectedFeatureTypes = selectedFeatureTypes;
        genomeAnnos = new ArrayList<>();
        allContinuousCoverageData = new HashMap<>();
        results = new ArrayList<>();
        linReg = new LinearRegression();
    }
    
    @Override
    public void startAnalysis() {
        allContinuousCoverageData.clear();
        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        LOG.log( INFO, "{0}: Starting to collect the necessary data for the differential gene expression analysis.", currentTimestamp );
        referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector( refGenomeID );
        List<AnalysesHandler> allHandler = new ArrayList<>();
        genomeAnnos.clear();

        for( PersistentChromosome chrom : referenceConnector.getRefGenome().getChromosomes().values() ) {
            genomeAnnos.addAll( referenceConnector.getFeaturesForRegionInclParents( 1, chrom.getLength(), selectedFeatureTypes, chrom.getId() ) );
        }

        for( PersistentTrack currentTrack : selectedTracks ) {
            try {
                TrackConnector tc = (new SaveFileFetcherForGUI()).getTrackConnector( currentTrack );

                CollectContinuousCoverageData collCovData = new CollectContinuousCoverageData( 
                    genomeAnnos, startOffset, stopOffset, readClassParams );
                allContinuousCoverageData.put(currentTrack.getId(), collCovData.getContinuousCountData() );
                AnalysesHandler handler = new AnalysesHandler( tc, (DataVisualisationI) this, "Collecting coverage data for track " +
                                                                         currentTrack.getDescription() + ".", readClassParams );
                handler.setMappingsNeeded( true );
                handler.setDesiredData( Properties.REDUCED_MAPPINGS );
                handler.registerObserver( collCovData );
                allHandler.add( handler );
            } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                ProcessingLog.getInstance().addProperty( "Unresolved track", currentTrack );
                notifyObservers( DeAnalysisHandler.AnalysisStatus.ERROR );
                this.interrupt();
                return;
            }
        }
        for( AnalysesHandler handler : allHandler ) {
            handler.startAnalysis();
        }
    }
    
    @Override
    protected List<ResultDeAnalysis> processWithTool() {
        linReg.process( allContinuousCoverageData ); 
        Map<PersistentFeature, double[]> calculated = linReg.getResults();
        results = prepareResults(calculated);
        return results;
    } 
   
    private List<ResultDeAnalysis> prepareResults(Map<PersistentFeature, double[]> calculated) {
        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Creating Continuous Count Data Table" );
        progressHandle.start( calculated.size() );
       
        final List<Object> regionNamesList = new ArrayList<>();

        List<List<Object>> tableContents = new ArrayList<>();
        
        int k = 0;
        for (Map.Entry<PersistentFeature, double[]> feature : calculated.entrySet() ) {
            k++;
            int tableSize = feature.getValue().length+1;
            boolean allZero = true;
            final Object[] tmp = new Object[tableSize];
            double[] rSqrt = feature.getValue();
            
            tmp[0] = feature.getKey();
            for(int i = 1; i<tableSize; i++ ){
                if( ! ( Double.isNaN( rSqrt[i-1] ) ) && 
                  ( Double.isFinite( rSqrt[i-1] ) ) ) {
                    allZero = false;
                    tmp[i] = rSqrt[i-1];
                }
            }
            if(!allZero){
                tableContents.add( new Vector( Arrays.asList( tmp ) ) );
                regionNamesList.add( feature.getKey() );
            }
            progressHandle.progress( k );
        }

        List<Object> colNames = new ArrayList<>();
        colNames.add( "Feature" );
        colNames.add( "Intercept" );
        colNames.add( "Slope" );
        colNames.add( "R^2" );

        List<ResultDeAnalysis> result = Collections.singletonList( new ResultDeAnalysis(
            tableContents, colNames, regionNamesList, "Count Data Table" ) );
        progressHandle.finish();
        return result;
    }

    @Override
    public synchronized void showData( Object data ) {
      //Integer <- trackID String <- queryType{{coverageNeeded ? DATA_TYPE_COVERAGE : DATA_TYPE_MAPPINGS}}
      //Pair<Integer, String> res = (Pair<Integer, String>) data; 
      //allContinuousCoverageData.put( res.getFirst(), allContinuousCoverageData.get( res.getFirst() ) ); // <- Why to do this????
        if( ++resultsReceivedBack == allContinuousCoverageData.size() ) {
            results.clear();
            results.addAll( processWithTool() );
            notifyObservers( AnalysisStatus.FINISHED );
        }
    }
    
    @Override
    public List<ResultDeAnalysis> getResults() {
        return Collections.unmodifiableList( results );
    }
    
    @Override
    public void endAnalysis() {
         results = null;
    }
}
