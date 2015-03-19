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
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.NormalizedReadCount;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.UrlWithTitle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Container for all data belonging to a read count and normalization (RPKM and
 * TPM) analysis result. Also converts the list of returned features into the
 * format readable for the ExcelExporter. Generates all three, the sheet names,
 * headers and data to write.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class NormalizationAnalysisResult extends ResultTrackAnalysis<ParameterSetNormalization> {

    private final List<NormalizedReadCount> normalizationResults;
    private int noGenomeFeatures;
    private double totalMappings;


    /**
     * Container for all data belonging to a read count and normalization (RPKM
     * and TPM) analysis result. Also converts the list of returned features
     * into the format readable for the ExcelExporter. Generates all three, the
     * sheet names, headers and data to write.
     * <p>
     * @param trackMap      the map of track ids to the PersistentTrack used for
     *                      this analysis
     * @param normResults   The result list of normalization values and read
     *                      counts
     * @param reference     reference genome, for which this result was
     *                      generated
     * @param combineTracks <code>true</code>, if the tracks in the list are
     *                      combined, <code>false</code> otherwise
     * @param trackColumn   column in which the track is stored
     * @param filterColumn  column which shall be used for filtering the results
     *                      among results of other tracks (e.g. the feature
     *                      column for normalization analysis)
     */
    public NormalizationAnalysisResult( Map<Integer, PersistentTrack> trackMap, List<NormalizedReadCount> normResults,
                                                                                PersistentReference reference,
                                                                                boolean combineTracks,
                                                                                int trackColumn,
                                                                                int filterColumn ) {
        super( reference, trackMap, combineTracks, trackColumn, filterColumn );
        this.normalizationResults = new ArrayList<>( normResults );
    }


    /**
     * @return The result list of raw read counts and normalized read counts.
     */
    public List<NormalizedReadCount> getResults() {
        return Collections.unmodifiableList( normalizationResults );
    }


    /**
     * Use this method when adding new results to the current results. It
     * synchronizes the list and prevents making changes during the adding
     * process.
     * <p>
     * @param newNormValues Normalization values to add to the current result
     */
    public void addAllToResult(List<NormalizedReadCount> newNormValues) {
        normalizationResults.addAll( newNormValues );
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add( "TPM, RPKM and Read Count Calculation Table" );
        sheetNames.add( "Parameters and Statistics" );
        return sheetNames;

    }


    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add( "Feature" );
        dataColumnDescriptions.add( "Locus" );
        dataColumnDescriptions.add( "EC-Number" );
        dataColumnDescriptions.add( "Product" );
        dataColumnDescriptions.add( "Feature Type" );
        dataColumnDescriptions.add( "Track" );
        dataColumnDescriptions.add( "Chromosome" );
        dataColumnDescriptions.add( "Strand" );
        dataColumnDescriptions.add( "Start" );
        dataColumnDescriptions.add( "Stop" );
        dataColumnDescriptions.add( "Length" );
        dataColumnDescriptions.add( "Effective Length" );
        dataColumnDescriptions.add( "TPM Value" );
        dataColumnDescriptions.add( "RPKM Value" );
        dataColumnDescriptions.add( "Raw Read Count" );

        allSheetDescriptions.add( dataColumnDescriptions );

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( "TPM, RPKM and Read Count Calculation Parameters and Statistics" );

        allSheetDescriptions.add( statisticColumnDescriptions );

        return allSheetDescriptions;
    }


    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> exportData = new ArrayList<>();
        List<List<Object>> normResultRows = new ArrayList<>();
        PersistentFeature feat;

        for( NormalizedReadCount normValue : normalizationResults ) {
            List<Object> normRow = new ArrayList<>();

            feat = normValue.getFeature();
            normRow.add( feat );
            normRow.add( feat.getLocus() );
            UrlWithTitle url = GeneralUtils.createEcUrl( feat.getEcNumber() );
            normRow.add( url != null ? url : "" );
            normRow.add( feat.getProduct() );
            normRow.add( feat.getType() );
            normRow.add( getTrackEntry( normValue.getTrackId(), true ) );
            normRow.add( getChromosomeMap().get( feat.getChromId() ) );
            normRow.add( feat.isFwdStrandString() );
            normRow.add( feat.getStartOnStrand() );
            normRow.add( feat.getStopOnStrand() );
            normRow.add( feat.getLength() );
            normRow.add( normValue.getEffectiveFeatureLength() );
            normRow.add( normValue.getTPM() );
            normRow.add( normValue.getRPKM() );
            normRow.add( normValue.getReadCount() );

            normResultRows.add( normRow );
        }

        exportData.add( normResultRows );

        //create statistics sheet
        ParameterSetNormalization normCalculationParameters = (ParameterSetNormalization) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "TPM, RPKM and raw read count calculation for tracks:",
                                                                      GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "TPM, RPKM and read count calculation parameters:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Min read count value of a feature to be shown in the results:",
                                                                      normCalculationParameters.getMinReadCount() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Max read count value of a feature to be shown in the results:",
                                                                      normCalculationParameters.getMaxReadCount() ) );
        normCalculationParameters.getReadClassParams().addReadClassParamsToStats( statisticsExportData );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "TPM, RPKM and read count calculation statistics:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelNormalization.TOTAL_MAPPINGS,
                                                                      getStatsMap().get( ResultPanelNormalization.TOTAL_MAPPINGS ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelNormalization.RETURNED_FEATURES,
                                                                      getStatsMap().get( ResultPanelNormalization.RETURNED_FEATURES ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelNormalization.FEATURES_TOTAL,
                                                                      getStatsMap().get( ResultPanelNormalization.FEATURES_TOTAL ) ) );

        exportData.add( statisticsExportData );

        return exportData;
    }


    /**
     * @param noGenomeFeatures The number of features of the reference genome.
     */
    public void setNoGenomeFeatures( int noGenomeFeatures ) {
        this.noGenomeFeatures = noGenomeFeatures;
    }


    /**
     * @return The number of features of the reference genome.
     */
    public int getNoGenomeFeatures() {
        return this.noGenomeFeatures;
    }


    /**
     * @param totalMappings The total number of mappings assignable to any
     *                      features during this analysis.
     */
    public void setTotalMappings( double totalMappings ) {
        this.totalMappings = totalMappings;
    }


    /**
     * @return The total number of mappings assignable to any features during
     *         this analysis.
     */
    public double getTotalMappings() {
        return totalMappings;
    }


}
