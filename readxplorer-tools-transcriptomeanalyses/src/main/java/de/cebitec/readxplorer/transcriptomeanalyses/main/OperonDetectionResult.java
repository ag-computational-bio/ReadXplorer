
package de.cebitec.readxplorer.transcriptomeanalyses.main;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.Operon;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.OperonAdjacency;
import de.cebitec.readxplorer.transcriptomeanalyses.enums.TableType;
import de.cebitec.readxplorer.transcriptomeanalyses.mainwizard.WizardPropertyStrings;
import de.cebitec.readxplorer.utils.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Container for all data belonging to an operon detection result.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class OperonDetectionResult extends ResultTrackAnalysis<ParameterSetWholeTranscriptAnalyses> {

    private List<Operon> detectedOperons;
    private final StatisticsOnMappingData stats;
    private HashMap<String, Object> operonStatsMap;
    private static final TableType TABLE_TYPE = TableType.OPERON_TABLE;


    public OperonDetectionResult( StatisticsOnMappingData stats, Map<Integer, PersistentTrack> trackList, List<Operon> detectedOperons, PersistentReference reference ) {//, Track currentTrack) {
        super( reference, trackList, false, 2, 1 );
        this.detectedOperons = detectedOperons;
        this.stats = stats;
    }


    public StatisticsOnMappingData getStats() {
        return stats;
    }


    public void setStatsAndParametersMap( HashMap<String, Object> statsMap ) {
        this.operonStatsMap = statsMap;
    }


    public HashMap<String, Object> getOperonStatsMap() {
        return operonStatsMap;
    }


    public List<Operon> getResults() {
        return detectedOperons;
    }


    public void setResults( List<Operon> results ) {
        this.detectedOperons = results;
    }


    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add( "Putative Operon Transcript Begin" );
        dataColumnDescriptions.add( "Feature 1" );
        dataColumnDescriptions.add( "Feature 2" );
        dataColumnDescriptions.add( "Strand" );
        dataColumnDescriptions.add( "Start Anno 1" );
        dataColumnDescriptions.add( "Start Anno 2" );
        dataColumnDescriptions.add( "False Positive" );
        dataColumnDescriptions.add( "Finished" );
        dataColumnDescriptions.add( "Spanning Reads" );
        dataColumnDescriptions.add( "Operon String" );
        dataColumnDescriptions.add( "Number Of Genes" );
        dataColumnDescriptions.add( "Chromosome" );
        dataColumnDescriptions.add( "Chromosome ID" );
        dataColumnDescriptions.add( "Track" );
        dataColumnDescriptions.add( "Track ID" );

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
            String annoName1 = "";
            String annoName2 = "";
            String strand = (operon.getOperonAdjacencies().get( 0 ).getFeature1().isFwdStrandString());
            String startAnno1 = "";
            String startAnno2 = "";
            String spanningReads = "";
            int chromId = operon.getOperonAdjacencies().get( 0 ).getFeature1().getChromId();

            for( OperonAdjacency opAdj : operon.getOperonAdjacencies() ) {
                annoName1 += opAdj.getFeature1().getLocus() + "\n";
                annoName2 += opAdj.getFeature2().getLocus() + "\n";
                startAnno1 += opAdj.getFeature1().getStart() + "\n";
                startAnno2 += opAdj.getFeature2().getStart() + "\n";
                spanningReads += opAdj.getSpanningReads() + "\n";

            }
            List<Object> operonsRow = new ArrayList<>();
            operonsRow.add( operon.getOperonAdjacencies().get( 0 ).getFeature1().isFwdStrand()
                            ? operon.getOperonAdjacencies().get( 0 ).getFeature1().getStart()
                            : operon.getOperonAdjacencies().get( 0 ).getFeature1().getStop() );
            operonsRow.add( annoName1 );
            operonsRow.add( annoName2 );

            operonsRow.add( strand );
            operonsRow.add( startAnno1 );
            operonsRow.add( startAnno2 );
            operonsRow.add( operon.isFalsPositive() );
            operonsRow.add( operon.isConsidered() );
            operonsRow.add( spanningReads );
            operonsRow.add( operon.toOperonString() );
            operonsRow.add( operon.getNbOfGenes() );

            operonsRow.add( this.getChromosomeMap().get( chromId ) );
            operonsRow.add( chromId );
            operonsRow.add( this.getTrackEntry( operon.getTrackId(), true ) );
            operonsRow.add( operon.getTrackId() );
            operonResults.add( operonsRow );
        }

        exportData.add( operonResults );

        double mappingCount = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.MAPPINGS_COUNT );
        double meanMappingLength = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH );
        double mappingsPerMio = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.MAPPINGS_MILLION );
        double backgroundThreshold = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS );

        //create statistics sheet
        ParameterSetWholeTranscriptAnalyses operonDetectionParameters = (ParameterSetWholeTranscriptAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Operon detection statistics for tracks:",
                                                                      GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Parameters:" ) );
//        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelOperonDetection.OPERONS_BACKGROUND_THRESHOLD,
//                this.getOperonStatsMap().get(ResultPanelOperonDetection.OPERONS_BACKGROUND_THRESHOLD)));
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP,
                                                                      operonDetectionParameters.isIncludeBestMatchedReadsOP() ? "yes" : "no" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelTranscriptionStart.TSS_FRACTION,
                                                                      operonDetectionParameters.getFraction() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD,
                                                                      operonDetectionParameters.isThresholdManuallySet() ? "yes" : "no" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS,
                                                                      String.valueOf( String.format( "%2.2f", backgroundThreshold ) ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Operon detection statistics:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelOperonDetection.OPERONS_TOTAL,
                                                                      this.getOperonStatsMap().get( ResultPanelOperonDetection.OPERONS_TOTAL ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelOperonDetection.OPERONS_TWO_GENES,
                                                                      this.getOperonStatsMap().get( ResultPanelOperonDetection.OPERONS_TWO_GENES ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelOperonDetection.OPERONS_BIGGEST,
                                                                      this.getOperonStatsMap().get( ResultPanelOperonDetection.OPERONS_BIGGEST ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelTranscriptionStart.MAPPINGS_COUNT,
                                                                      String.valueOf( String.format( "%2.2f", mappingCount ) ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH,
                                                                      String.valueOf( String.format( "%2.2f", meanMappingLength ) ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( ResultPanelTranscriptionStart.MAPPINGS_MILLION,
                                                                      String.valueOf( String.format( "%2.2f", mappingsPerMio ) ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Table Type", TABLE_TYPE.toString(), "" ) );

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
