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

package de.cebitec.readxplorer.tools.coverageanalysis;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.exporter.tables.ExportDataI;
import de.cebitec.readxplorer.utils.GeneralUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Container for all data belonging to a coverage analysis result. Also converts
 * a all data into the format readable for the ExcelExporter. Generates all
 * three, the sheet names, headers and data to write.
 * <p>
 * @author Tobias Zimmermann, Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CoverageAnalysisResult extends ResultTrackAnalysis<ParameterSetCoverageAnalysis>
        implements ExportDataI {

    private final CoverageIntervalContainer results;


    public CoverageAnalysisResult( CoverageIntervalContainer results, Map<Integer, PersistentTrack> trackMap,
                                   PersistentReference reference, boolean combineTracks ) {
        super( reference, trackMap, combineTracks, 0, 3 );
        this.results = results;
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>( 2 );

        ParameterSetCoverageAnalysis parameters = (ParameterSetCoverageAnalysis) this.getParameters();
        String tableHeader;
        if( parameters.isDetectCoveredIntervals() ) {
            tableHeader = "Covered Intervals Table";
        } else {
            tableHeader = "Uncovered Intervals Table";
        }
        sheetNames.add( tableHeader );
        sheetNames.add( "Parameters and Statistics" );
        return sheetNames;

    }


    @Override
    public List<List<String>> dataColumnDescriptions() {

        ParameterSetCoverageAnalysis parameters = (ParameterSetCoverageAnalysis) this.getParameters();
        String coveredString = parameters.isDetectCoveredIntervals() ? "Covered" : "Uncovered";

        List<List<String>> dataColumnDescriptions = new ArrayList<>( 2 );
        List<String> resultDescriptions = new ArrayList<>( 10 );

        resultDescriptions.add( "Start" );
        resultDescriptions.add( "Stop" );
        resultDescriptions.add( "Track" );
        resultDescriptions.add( "Chromosome" );
        resultDescriptions.add( "Strand" );
        resultDescriptions.add( "Length" );
        resultDescriptions.add( "Mean Coverage" );


        dataColumnDescriptions.add( resultDescriptions );

        //add covered interval detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( coveredString + " Interval Analysis Parameter and Statistics Table" );

        dataColumnDescriptions.add( statisticColumnDescriptions );

        return dataColumnDescriptions;
    }


    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> coveredIntervalsExport = new ArrayList<>( 1 );
        List<List<Object>> coveredIntervalsResultList = new ArrayList<>( 2 );

        fillTableRow( this.results.getCoverageIntervals(), coveredIntervalsResultList );
        fillTableRow( this.results.getCoverageIntervalsRev(), coveredIntervalsResultList );

        coveredIntervalsExport.add( coveredIntervalsResultList );

        //create statistics sheet
        ParameterSetCoverageAnalysis parameters = (ParameterSetCoverageAnalysis) this.getParameters();
        String coveredString = parameters.isDetectCoveredIntervals() ? "Covered" : "Uncovered";

        List<List<Object>> statisticsExportData = new ArrayList<>( 10 );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( coveredString + " interval analysis statistics for tracks:",
                                                                      GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createRxVersionRow() );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( coveredString + " interval analysis detection parameters:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum counted coverage:", parameters.getMinCoverageCount() ) );

        String coverageCount = parameters.isSumCoverageOfBothStrands() ? "sum coverage of both strands" : "treat each strand separately";
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Count coverage for:", coverageCount ) );
        parameters.getReadClassParams().addReadClassParamsToStats( statisticsExportData );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( coveredString + " interval analysis statistics:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow(
                ResultPanelCoverageAnalysis.NUMBER_INTERVALS, getStatsMap().get( ResultPanelCoverageAnalysis.NUMBER_INTERVALS ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow(
                ResultPanelCoverageAnalysis.MEAN_INTERVAL_LENGTH, getStatsMap().get( ResultPanelCoverageAnalysis.MEAN_INTERVAL_LENGTH ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow(
                ResultPanelCoverageAnalysis.MEAN_INTERVAL_COVERAGE, getStatsMap().get( ResultPanelCoverageAnalysis.MEAN_INTERVAL_COVERAGE ) ) );


        coveredIntervalsExport.add( statisticsExportData );


        return coveredIntervalsExport;
    }


    public CoverageIntervalContainer getResults() {
        return results;
    }


    private void fillTableRow( List<CoverageInterval> coverageList, List<List<Object>> coveredFeaturesResultList ) {
        for( CoverageInterval interval : coverageList ) {
            List<Object> coveredIntervalRow = new ArrayList<>( 10 );
            coveredIntervalRow.add( interval.isFwdStrand() ? interval.getStart() : interval.getStop() );
            coveredIntervalRow.add( interval.isFwdStrand() ? interval.getStop() : interval.getStart() );
            coveredIntervalRow.add( this.getTrackEntry( interval.getTrackId(), true ) );
            coveredIntervalRow.add( this.getChromosomeMap().get( interval.getChromId() ) );
            coveredIntervalRow.add( interval.getStrandString() );
            coveredIntervalRow.add( interval.getLength() );
            coveredIntervalRow.add( interval.getMeanCoverage() );

            coveredFeaturesResultList.add( coveredIntervalRow );
        }
    }


}
