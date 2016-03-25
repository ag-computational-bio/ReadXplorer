/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.tools.coverageanalysis.featurecoverageanalysis;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.exporter.tables.ExportDataI;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.UrlWithTitle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Container for all data belonging to a covered feature detection result. Also
 * converts a all data into the format readable for the ExcelExporter. Generates
 * all three, the sheet names, headers and data to write.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeatureResult extends ResultTrackAnalysis<ParameterSetCoveredFeatures>
        implements ExportDataI {

    private final List<CoveredFeature> results;


    /**
     * Container for all data belonging to a covered feature detection result.
     * Also converts a all data into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * <p>
     * @param results      the results of the covered feature detection
     * @param trackMap     the map of track ids to the tracks, for which the
     *                     covered feature detection was carried out
     * @param currentTrack the track on which this analysis result was generated
     */
    public CoveredFeatureResult( List<CoveredFeature> results, Map<Integer, PersistentTrack> trackMap, PersistentReference reference,
                                 boolean combineTracks, int trackColumn, int filterColumn ) {
        super( reference, trackMap, combineTracks, trackColumn, filterColumn );
        this.results = new ArrayList<>( results );

    }


    /**
     * @return The current content of the result list.
     */
    public List<CoveredFeature> getResults() {
        return Collections.unmodifiableList( results );
    }


    /**
     * Use this method when adding new results to the current results. It
     * synchronizes the list and prevents making changes during the adding
     * process.
     * <p>
     * @param coveredFeatures The new list of covered features to add
     */
    public void addAllToResult( List<CoveredFeature> coveredFeatures ) {
        this.results.addAll( coveredFeatures );
    }


    @Override
    public List<List<String>> dataColumnDescriptions() {

        ParameterSetCoveredFeatures parameters = (ParameterSetCoveredFeatures) this.getParameters();
        String coveredString = parameters.isGetCoveredFeatures() ? "Covered" : "Uncovered";

        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add( coveredString + " Feature" );
        resultDescriptions.add( "Track" );
        resultDescriptions.add( "Chromosome" );
        resultDescriptions.add( "Strand" );
        resultDescriptions.add( "Start" );
        resultDescriptions.add( "Stop" );
        resultDescriptions.add( "Length" );
        resultDescriptions.add( "Mean Coverage" );
        resultDescriptions.add( "Covered Percent" );
        resultDescriptions.add( "Covered Bases Count" );
        resultDescriptions.add( "Locus" );
        resultDescriptions.add( "EC-Number" );
        resultDescriptions.add( "Product" );

        dataColumnDescriptions.add( resultDescriptions );

        //add covered features detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( coveredString + " Features Detection Parameter and Statistics Table" );

        dataColumnDescriptions.add( statisticColumnDescriptions );

        return dataColumnDescriptions;
    }


    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> coveredFeaturesExport = new ArrayList<>();
        List<List<Object>> coveredFeaturesResultList = new ArrayList<>();

        PersistentFeature feature;
        for( CoveredFeature coveredFeature : this.results ) {
            List<Object> coveredFeatureRow = new ArrayList<>();
            feature = coveredFeature.getCoveredFeature();
            coveredFeatureRow.add( feature.toString() );
            coveredFeatureRow.add( this.getTrackEntry( coveredFeature.getTrackId(), true ) );
            coveredFeatureRow.add( this.getChromosomeMap().get( feature.getChromId() ) );
            coveredFeatureRow.add( feature.getStrandString() );
            coveredFeatureRow.add( feature.getStartOnStrand() );
            coveredFeatureRow.add( feature.getStopOnStrand() );
            coveredFeatureRow.add( feature.getLength() );
            coveredFeatureRow.add( coveredFeature.getMeanCoverage() );
            coveredFeatureRow.add( coveredFeature.getPercentCovered() );
            coveredFeatureRow.add( coveredFeature.getNoCoveredBases() );
            coveredFeatureRow.add( feature.getLocus() );
            UrlWithTitle url = GeneralUtils.createEcUrl( feature.getEcNumber() );
            coveredFeatureRow.add( url != null ? url : "" );
            coveredFeatureRow.add( feature.getProduct() );

            coveredFeaturesResultList.add( coveredFeatureRow );
        }

        coveredFeaturesExport.add( coveredFeaturesResultList );



        //create statistics sheet
        ParameterSetCoveredFeatures parameters = (ParameterSetCoveredFeatures) this.getParameters();
        String coveredString = parameters.isGetCoveredFeatures() ? "Covered" : "Uncovered";

        List<List<Object>> statisticsExportData = new ArrayList<>();
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( coveredString + " feature detection statistics for tracks:",
                                                                      GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createRxVersionRow() );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( coveredString + " feature detection parameters:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum covered percent:", parameters.getMinCoveredPercent() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum counted coverage:", parameters.getMinCoverageCount() ) );
        String uncoveredFeatures = parameters.isGetCoveredFeatures() ? "no" : "yes";
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Detect uncovered instead of covered features:", uncoveredFeatures ) );
        parameters.getReadClassParams().addReadClassParamsToStats( statisticsExportData );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( coveredString + " feature statistics:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelCoveredFeatures.FEATURES_COVERED, coveredFeaturesResultList.size() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow(
                ResultPanelCoveredFeatures.FEATURES_TOTAL, this.getStatsMap().get( ResultPanelCoveredFeatures.FEATURES_TOTAL ) ) );

        coveredFeaturesExport.add( statisticsExportData );

        return coveredFeaturesExport;
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();

        ParameterSetCoveredFeatures parameters = (ParameterSetCoveredFeatures) this.getParameters();
        String tableHeader;
        if( parameters.isGetCoveredFeatures() ) {
            tableHeader = "Covered Features Table";
        } else {
            tableHeader = "Uncovered Features Table";
        }
        sheetNames.add( tableHeader );
        sheetNames.add( "Parameters and Statistics" );
        return sheetNames;
    }


}
