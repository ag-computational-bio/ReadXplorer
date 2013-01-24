package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author MKD, rhilker
 */
public class OperonColumns implements ExcelExportDataI {

    private OperonDetectionResult operonDetectionResult;
    private HashMap<String, Integer> operonResultStatsMap;

    public OperonColumns(OperonDetectionResult operonResults, HashMap<String, Integer> operonResultStatsMap) {
        this.operonDetectionResult = operonResults;
        this.operonResultStatsMap = operonResultStatsMap;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Feature 1");
        dataColumnDescriptions.add("Feature 2");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Start Anno 1");
        dataColumnDescriptions.add("Start Anno 2");
        dataColumnDescriptions.add("Reads Overlap Stop 1");
        dataColumnDescriptions.add("Reads Overlap Start 2");
        dataColumnDescriptions.add("Internal Reads");
        dataColumnDescriptions.add("Spanning Reads");
        
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

        for (Operon operon : operonDetectionResult.getResults()) {
            String annoName1 = "";
            String annoName2 = "";
            String strand = (operon.getOperonAdjacencies().get(0).getFeature1().isFwdStrand() ? "Fwd" : "Rev") + "\n";
            String startAnno1 = "";
            String startAnno2 = "";
            String readsAnno1 = "";
            String readsAnno2 = "";
            String internalReads = "";
            String spanningReads = "";
            
            for (OperonAdjacency opAdj : operon.getOperonAdjacencies()) {
                annoName1 += opAdj.getFeature1().getLocus() + "\n";
                annoName2 += opAdj.getFeature2().getLocus() + "\n";
                startAnno1 += opAdj.getFeature1().getStart() + "\n";
                startAnno2 += opAdj.getFeature2().getStart() + "\n";
                readsAnno1 += opAdj.getReadsFeature1() + "\n";
                readsAnno2 += opAdj.getReadsFeature2() + "\n";
                internalReads += opAdj.getInternalReads() + "\n";
                spanningReads += opAdj.getSpanningReads() + "\n";

            }
            List<Object> operonsRow = new ArrayList<>();
            operonsRow.add(annoName1);
            operonsRow.add(annoName2);
            operonsRow.add(strand);
            operonsRow.add(startAnno1);
            operonsRow.add(startAnno2);
            operonsRow.add(readsAnno1);
            operonsRow.add(readsAnno2);
            operonsRow.add(internalReads);
            operonsRow.add(spanningReads);

            operonResults.add(operonsRow);
        }
        
        exportData.add(operonResults);
        
        //create statistics sheet
        ParameterSetOperonDet operonDetectionParameters = (ParameterSetOperonDet) this.operonDetectionResult.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();
        List<Object> statisticsExport = new ArrayList<>();

        statisticsExport.add("Operon detection statistics for tracks:");
        statisticsExport.add(GeneralUtils.generateConcatenatedString(this.operonDetectionResult.getTrackList()));
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between title and parameters
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Operon detection parameters:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum number of spanning reads:");
        statisticsExport.add(operonDetectionParameters.getMinSpanningReads());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between parameters and statistics
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Operon detection statistics:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelOperonDetection.OPERONS_TOTAL);
        statisticsExport.add(operonResultStatsMap.get(ResultPanelOperonDetection.OPERONS_TOTAL));
        statisticsExportData.add(statisticsExport);
                
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS);
        statisticsExport.add(operonResultStatsMap.get(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS));
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS);
        statisticsExport.add(operonResultStatsMap.get(ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS));
        statisticsExportData.add(statisticsExport);

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
