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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.Operon;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.OperonAdjacency;
import de.cebitec.readxplorer.utils.GeneralUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Container for all data belonging to an operon detection result.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class OperonDetectionResult extends ResultTrackAnalysis<ParameterSetOperonDet> {

    private final List<Operon> detectedOperons;


    /**
     * Container for all data belonging to an operon detection result.
     * @param trackList
     * @param detectedOperons
     * @param reference
     * @param combineTracks
     * @param trackColumn
     * @param filterColumn
     */
    public OperonDetectionResult( Map<Integer, PersistentTrack> trackList, List<Operon> detectedOperons,
                                  PersistentReference reference, boolean combineTracks, int trackColumn, int filterColumn ) {
        super( reference, trackList, combineTracks, trackColumn, filterColumn );
        this.detectedOperons = new ArrayList<>( detectedOperons );
    }


    /**
     * @return The list of detected operons.
     */
    public List<Operon> getResults() {
        return Collections.unmodifiableList( detectedOperons );
    }

    /**
     * Use this method when adding new results to the current results. It
     * synchronizes the list and prevents making changes during the adding
     * process.
     * <p>
     * @param newOperons Operons to add to the current result
     */
    public void addAllToResult(List<Operon> newOperons) {
        detectedOperons.addAll( newOperons );
    }


    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add( "Feature 1" );
        dataColumnDescriptions.add( "Feature 2" );
        dataColumnDescriptions.add( "Track" );
        dataColumnDescriptions.add( "Chromosome" );
        dataColumnDescriptions.add( "Strand" );
        dataColumnDescriptions.add( "Start Anno 1" );
        dataColumnDescriptions.add( "Start Anno 2" );
        dataColumnDescriptions.add( "Reads Overlap Stop 1" );
        dataColumnDescriptions.add( "Reads Overlap Start 2" );
        dataColumnDescriptions.add( "Internal Reads" );
        dataColumnDescriptions.add( "Spanning Reads" );
        dataColumnDescriptions.add( "Feature 1 Locus" );
        dataColumnDescriptions.add( "Feature 1 Product" );
        dataColumnDescriptions.add( "Feature 1 EC Number" );
        dataColumnDescriptions.add( "Feature 2 Locus" );
        dataColumnDescriptions.add( "Feature 2 Product" );
        dataColumnDescriptions.add( "Feature 2 EC Number" );

        allSheetDescriptions.add( dataColumnDescriptions );

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( "Operon Detection Parameters and Statistics" );

        allSheetDescriptions.add( statisticColumnDescriptions );

        return allSheetDescriptions;
    }


    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> exportData = new ArrayList<>();
        List<List<Object>> operonResults = new ArrayList<>();

        for( Operon operon : this.detectedOperons ) {
            PersistentFeature feat1 = operon.getOperonAdjacencies().get( 0 ).getFeature1();
            String annoName1 = "";
            String annoName2 = "";
            String strand = feat1.isFwdStrandString() + "\n";
            String startAnno1 = "";
            String startAnno2 = "";
            String readsAnno1 = "";
            String readsAnno2 = "";
            String internalReads = "";
            String spanningReads = "";
            String anno1Locus = "";
            String anno1Product = "";
            String anno1EcNumber = "";
            String anno2Locus = "";
            String anno2Product = "";
            String anno2EcNumber = "";

            for( OperonAdjacency opAdj : operon.getOperonAdjacencies() ) {
                annoName1 += opAdj.getFeature1().toString() + "\n";
                annoName2 += opAdj.getFeature2().toString() + "\n";
                startAnno1 += opAdj.getFeature1().getStart() + "\n";
                startAnno2 += opAdj.getFeature2().getStart() + "\n";
                readsAnno1 += opAdj.getReadsFeature1() + "\n";
                readsAnno2 += opAdj.getReadsFeature2() + "\n";
                internalReads += opAdj.getInternalReads() + "\n";
                spanningReads += opAdj.getSpanningReads() + "\n";
                anno1Locus += opAdj.getFeature1().getLocus();
                anno1Product += opAdj.getFeature1().getProduct();
                anno1EcNumber += opAdj.getFeature1().getEcNumber();
                anno2Locus += opAdj.getFeature2().getLocus();
                anno2Product += opAdj.getFeature2().getProduct();
                anno2EcNumber += opAdj.getFeature2().getEcNumber();

            }
            List<Object> operonsRow = new ArrayList<>();
            operonsRow.add( annoName1 );
            operonsRow.add( annoName2 );
            operonsRow.add( this.getTrackEntry( operon.getTrackId(), true ) );
            operonsRow.add( this.getChromosomeMap().get( feat1.getChromId() ) );
            operonsRow.add( strand );
            operonsRow.add( startAnno1 );
            operonsRow.add( startAnno2 );
            operonsRow.add( readsAnno1 );
            operonsRow.add( readsAnno2 );
            operonsRow.add( internalReads );
            operonsRow.add( spanningReads );
            operonsRow.add( anno1Locus );
            operonsRow.add( anno1Product );
            operonsRow.add( anno1EcNumber );
            operonsRow.add( anno2Locus );
            operonsRow.add( anno2Product );
            operonsRow.add( anno2EcNumber );

            operonResults.add( operonsRow );
        }

        exportData.add( operonResults );

        //create statistics sheet
        ParameterSetOperonDet operonDetectionParameters = (ParameterSetOperonDet) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Operon detection statistics for tracks:",
                                                                      GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Operon detection parameters:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum number of spanning reads:",
                                                                      operonDetectionParameters.getMinSpanningReads() ) );
        operonDetectionParameters.getReadClassParams().addReadClassParamsToStats( statisticsExportData );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Operon detection statistics:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelOperonDetection.OPERONS_TOTAL,
                                                                      this.getStatsMap().get( ResultPanelOperonDetection.OPERONS_TOTAL ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS,
                                                                      this.getStatsMap().get( ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS,
                                                                      this.getStatsMap().get( ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS ) ) );

        exportData.add( statisticsExportData );

        return exportData;
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add( "Operon Detection Table" );
        sheetNames.add( "Parameters and Statistics" );
        return sheetNames;
    }


}
