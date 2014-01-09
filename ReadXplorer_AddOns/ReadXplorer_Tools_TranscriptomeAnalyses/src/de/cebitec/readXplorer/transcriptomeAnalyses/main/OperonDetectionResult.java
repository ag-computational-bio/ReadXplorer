package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.TableType;
import de.cebitec.readXplorer.util.GeneralUtils;
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

    private final List<Operon> detectedOperons;
    private final StatisticsOnMappingData stats;
    private HashMap<String, Object> operonStatsMap;
    private static final TableType TABLE_TYPE = TableType.OPETON_TABLE;

    public OperonDetectionResult(StatisticsOnMappingData stats, Map<Integer, PersistantTrack> trackList, List<Operon> detectedOperons, int refId) {//, PersistantTrack currentTrack) {
        super(trackList, refId, false, 2, 1);
        this.detectedOperons = detectedOperons;
        this.stats = stats;
    }

    public StatisticsOnMappingData getStats() {
        return stats;
    }

    public void setStatsAndParametersMap(HashMap<String, Object> statsMap) {
        this.operonStatsMap = statsMap;
    }

    public HashMap<String, Object> getOperonStatsMap() {
        return operonStatsMap;
    }

    public List<Operon> getResults() {
        return detectedOperons;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Feature 1");
        dataColumnDescriptions.add("Feature 2");
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Start Anno 1");
        dataColumnDescriptions.add("Start Anno 2");
//        dataColumnDescriptions.add("Reads Overlap Stop 1");
//        dataColumnDescriptions.add("Reads Overlap Start 2");
//        dataColumnDescriptions.add("Internal Reads");
        dataColumnDescriptions.add("Spanning Reads");
        dataColumnDescriptions.add("Operon-String");
        dataColumnDescriptions.add("Chromosome ID");
        dataColumnDescriptions.add("Track ID");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Operon Detection Parameters and Statistics");

        allSheetDescriptions.add(statisticColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> exportData = new ArrayList<>();
        List<List<Object>> operonResults = new ArrayList<>();

        for (Operon operon : this.detectedOperons) {
            String annoName1 = "";
            String annoName2 = "";
            String strand = (operon.getOperonAdjacencies().get(0).getFeature1().isFwdStrandString()) + "\n";
            String startAnno1 = "";
            String startAnno2 = "";
//            String readsAnno1 = "";
//            String readsAnno2 = "";
//            String internalReads = "";
            String spanningReads = "";
            int chromId = operon.getOperonAdjacencies().get(0).getFeature1().getChromId();

            for (OperonAdjacency opAdj : operon.getOperonAdjacencies()) {
                annoName1 += opAdj.getFeature1().getLocus() + "\n";
                annoName2 += opAdj.getFeature2().getLocus() + "\n";
                startAnno1 += opAdj.getFeature1().getStart() + "\n";
                startAnno2 += opAdj.getFeature2().getStart() + "\n";
//                readsAnno1 += opAdj.getReadsFeature1() + "\n";
//                readsAnno2 += opAdj.getReadsFeature2() + "\n";
//                internalReads += opAdj.getInternalReads() + "\n";
                spanningReads += opAdj.getSpanningReads() + "\n";

            }
            List<Object> operonsRow = new ArrayList<>();
            operonsRow.add(annoName1);
            operonsRow.add(annoName2);
            operonsRow.add(this.getTrackEntry(operon.getTrackId(), true));
            operonsRow.add(this.getChromosomeMap().get(operon.getOperonAdjacencies().get(0).getFeature1().getChromId()));
            operonsRow.add(strand);
            operonsRow.add(startAnno1);
            operonsRow.add(startAnno2);
//            operonsRow.add(readsAnno1);
//            operonsRow.add(readsAnno2);
//            operonsRow.add(internalReads);
            operonsRow.add(spanningReads);
            operonsRow.add(operon.toOperonString());
            operonsRow.add(chromId);
            operonsRow.add(operon.getTrackId());
            operonResults.add(operonsRow);
        }

        exportData.add(operonResults);

        //create statistics sheet
        ParameterSetWholeTranscriptAnalyses operonDetectionParameters = (ParameterSetWholeTranscriptAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Operon detection statistics for tracks:",
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Operon detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelOperonDetection.OPERONS_BACKGROUND_THRESHOLD,
                this.getOperonStatsMap().get(ResultPanelOperonDetection.OPERONS_BACKGROUND_THRESHOLD)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Fraction for Background threshold calculation:",
                operonDetectionParameters.getFraction()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Operon detection statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelOperonDetection.OPERONS_TOTAL,
                this.getOperonStatsMap().get(ResultPanelOperonDetection.OPERONS_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS,
                this.getOperonStatsMap().get(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_COUNT,
                this.getOperonStatsMap().get(ResultPanelTranscriptionStart.MAPPINGS_COUNT)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH,
                this.getOperonStatsMap().get(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_MILLION,
                this.getOperonStatsMap().get(ResultPanelTranscriptionStart.MAPPINGS_MILLION)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD,
                this.getOperonStatsMap().get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow(""));
        
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Table Type", TABLE_TYPE.toString()));
        
        exportData.add(statisticsExportData);

        return exportData;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Operon Detection Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }
}
