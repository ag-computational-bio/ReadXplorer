
package de.cebitec.readxplorer.transcriptomeanalyses.main;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.RPKMvalue;
import de.cebitec.readxplorer.transcriptomeanalyses.enums.TableType;
import de.cebitec.readxplorer.transcriptomeanalyses.mainwizard.WizardPropertyStrings;
import de.cebitec.readxplorer.utils.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Container for all data belonging to an RPKM and read count analysis result.
 * Also converts the list of returned features into the format readable for the
 * ExcelExporter. Generates all three, the sheet names, headers and data to
 * write.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class RPKMAnalysisResult extends ResultTrackAnalysis<ParameterSetWholeTranscriptAnalyses> {

    private final List<RPKMvalue> rpkmResults;
    private int noGenomeFeatures;
    private HashMap<String, Object> rpkmStatsMap;
    private static final TableType TABLE_TYPE = TableType.RPKM_TABLE;


    /**
     * Container for all data belonging to an RPKM and read count analysis
     * result. Also converts the list of returned features into the format
     * readable for the ExcelExporter. Generates all three, the sheet names,
     * headers and data to write.
     *
     * @param trackMap    the map of track ids to the Track used for this
     *                    analysis
     * @param rpkmResults The result list of RPKM values and read counts
     *                    otherwise
     */
    public RPKMAnalysisResult( Map<Integer, PersistentTrack> trackMap, List<RPKMvalue> rpkmResults, PersistentReference reference ) {
        super( reference, trackMap, false, 2, 0 );
        this.rpkmResults = rpkmResults;
    }


    /**
     * @return The result list of RPKM values and read counts.
     */
    public List<RPKMvalue> getResults() {
        return rpkmResults;
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add( "RPKM and Read Count Calculation Table" );
        sheetNames.add( "Parameters and Statistics" );
        return sheetNames;

    }


    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add( "Feature" );
        dataColumnDescriptions.add( "Feature Type" );
        dataColumnDescriptions.add( "Start" );
        dataColumnDescriptions.add( "Stop" );
        dataColumnDescriptions.add( "Feature Length" );
        dataColumnDescriptions.add( "Strand" );
        dataColumnDescriptions.add( "Longest Detected 5'-UTR Length" );
        dataColumnDescriptions.add( "RPKM" );
        dataColumnDescriptions.add( "Log RPKM" );
        dataColumnDescriptions.add( "Mapped Total" );
        dataColumnDescriptions.add( "Chromosome" );
        dataColumnDescriptions.add( "Chromosome ID" );
        dataColumnDescriptions.add( "Track" );
        dataColumnDescriptions.add( "Track ID" );

        allSheetDescriptions.add( dataColumnDescriptions );

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( "RPKM and Read Count Calculation Parameters and Statistics" );

        allSheetDescriptions.add( statisticColumnDescriptions );

        return allSheetDescriptions;
    }


    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> exportData = new ArrayList<>();
        List<List<Object>> rpkmResultRows = new ArrayList<>();
        PersistentFeature feat;

        for( RPKMvalue rpkmValue : this.rpkmResults ) {
            List<Object> rpkmRow = new ArrayList<>();

            feat = rpkmValue.getFeature();
            rpkmRow.add( feat.getLocus() );
            rpkmRow.add( feat.getType() );
            rpkmRow.add( feat.isFwdStrand() ? feat.getStart() : feat.getStop() );
            rpkmRow.add( feat.isFwdStrand() ? feat.getStop() : feat.getStart() );
            rpkmRow.add( ((feat.getStop() + 1) - (feat.getStart() + 1)) + 1 );
            rpkmRow.add( feat.isFwdStrandString() );
            rpkmRow.add( rpkmValue.getLongestKnownUtrLength() );
            rpkmRow.add( rpkmValue.getRPKM() );
            rpkmRow.add( rpkmValue.getLogRpkm() );
            rpkmRow.add( rpkmValue.getReadCount() );
            rpkmRow.add( this.getChromosomeMap().get( feat.getChromId() ) );
            rpkmRow.add( rpkmValue.getChromId() );
            rpkmRow.add( this.getTrackEntry( rpkmValue.getTrackId(), true ) );
            rpkmRow.add( rpkmValue.getTrackId() );

            rpkmResultRows.add( rpkmRow );
        }

        exportData.add( rpkmResultRows );

        //create statistics sheet
        ParameterSetWholeTranscriptAnalyses rpkmCalculationParameters = (ParameterSetWholeTranscriptAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "RPKM and raw read count calculation for tracks:",
                                                                      GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "RPKM and read count calculation parameters:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_RPKM, rpkmCalculationParameters.isIncludeBestMatchedReadsRpkm() ? "yes" : "no" ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "RPKM and read count calculation statistics:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Total number of returned features", rpkmResultRows.size() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Table Type", TABLE_TYPE.toString(), "" ) );

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
     * @return The statistics map associated with this analysis
     */
    public Map<String, Object> getStatsAndParametersMap() {
        return this.rpkmStatsMap;
    }


    /**
     * Sets the statistics map associated with this analysis.
     *
     * @param statsMap the statistics map associated with this analysis
     */
    public void setStatsAndParametersMap( Map<String, Object> statsMap ) {
        this.rpkmStatsMap = (HashMap<String, Object>) statsMap;
    }


    /**
     * @return The number of features of the reference genome.
     */
    public int getNoGenomeFeatures() {
        return this.noGenomeFeatures;
    }


}
