package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.RPKMvalue;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class RPKMAnalysisResult extends ResultTrackAnalysis<ParameterSetRPKM> {

    private List<RPKMvalue> rpkmResults;
    
    public RPKMAnalysisResult(Map<Integer, PersistantTrack> trackList, List<RPKMvalue> rpkmResults) {
        super(trackList);
        this.rpkmResults = rpkmResults;
    }
    
    /**
     * @return The result list of RPKM values.
     */
    public List<RPKMvalue> getResults() {
        return rpkmResults;
    }
    
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("RPKM Calculation Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
        
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Feature");
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Start");
        dataColumnDescriptions.add("Stop");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("RPKM Value");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("RPKM Calculation Parameters and Statistics");

        allSheetDescriptions.add(statisticColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> exportData = new ArrayList<>();
        List<List<Object>> rpkmResultRows = new ArrayList<>();
        PersistantFeature feat;

        for (RPKMvalue rpkmValue : this.rpkmResults) {
            List<Object> rpkmRow = new ArrayList<>();

            feat = rpkmValue.getFeature();
            rpkmRow.add(feat);
            rpkmRow.add(this.getTrackMap().get(rpkmValue.getTrackId()));
            rpkmRow.add(feat.isFwdStrand() ? feat.getStart() : feat.getStop());
            rpkmRow.add(feat.isFwdStrand() ? feat.getStop() : feat.getStart());
            rpkmRow.add(feat.isFwdStrandString());
            rpkmRow.add(rpkmValue.getRPKM());

            rpkmResultRows.add(rpkmRow);
        }

        exportData.add(rpkmResultRows);

        //create statistics sheet
        ParameterSetRPKM rpkmCalculationParameters = (ParameterSetRPKM) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("RPKM calculation for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("RPKM calculation parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum RPKM value:", 
                rpkmCalculationParameters.getMinRPKM()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Maximum RPKM value:", 
                rpkmCalculationParameters.getMaxRPKM()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("RPKM calculation statistics:"));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelOperonDetection.OPERONS_TOTAL, 
//                this.getStatsMap().get(ResultPanelOperonDetection.OPERONS_TOTAL)));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS, 
//                this.getStatsMap().get(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS)));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS, 
//                this.getStatsMap().get(ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS)));
        //TODO: Statistics

        exportData.add(statisticsExportData);

        return exportData;
    }
    
}
