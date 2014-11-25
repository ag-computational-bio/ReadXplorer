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
 * Container for all data belonging to an RPKM and read count analysis result.
 * Also converts the list of returned features into the format readable for the
 * ExcelExporter. Generates all three, the sheet names, headers and data to
 * write.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class RPKMAnalysisResult extends ResultTrackAnalysis<ParameterSetRPKM> {

    private List<RPKMvalue> rpkmResults;
    private int noGenomeFeatures;
    
    /**
     * Container for all data belonging to an RPKM and read count analysis
     * result. Also converts the list of returned features into the format
     * readable for the ExcelExporter. Generates all three, the sheet names,
     * headers and data to write.
     * @param trackMap the map of track ids to the PersistantTrack used for this
     * analysis
     * @param rpkmResults The result list of RPKM values and read counts
     * @param combineTracks true, if the tracks in the list are combined, false
     * otherwise 
     */
    public RPKMAnalysisResult(Map<Integer, PersistantTrack> trackMap, List<RPKMvalue> rpkmResults, boolean combineTracks) {
        super(trackMap, combineTracks);
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
        sheetNames.add("RPKM and Read Count Calculation Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
        
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Feature");
        dataColumnDescriptions.add("Feature Type");
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Start");
        dataColumnDescriptions.add("Stop");
        dataColumnDescriptions.add("Length");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("RPKM Value");
        dataColumnDescriptions.add("Raw Read Count");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("RPKM and Read Count Calculation Parameters and Statistics");

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
            rpkmRow.add(feat.getType());
            rpkmRow.add(this.getTrackEntry(rpkmValue.getTrackId(), true));
            rpkmRow.add(feat.isFwdStrand() ? feat.getStart() : feat.getStop());
            rpkmRow.add(feat.isFwdStrand() ? feat.getStop() : feat.getStart());
            rpkmRow.add(feat.getStop() - feat.getStart());
            rpkmRow.add(feat.isFwdStrandString());
            rpkmRow.add(rpkmValue.getRPKM());
            rpkmRow.add(rpkmValue.getReadCount());

            rpkmResultRows.add(rpkmRow);
        }

        exportData.add(rpkmResultRows);

        //create statistics sheet
        ParameterSetRPKM rpkmCalculationParameters = (ParameterSetRPKM) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("RPKM and raw read count calculation for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("RPKM and read count calculation parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Min read count value of a feature to be shown in the results:", 
                rpkmCalculationParameters.getMinReadCount()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Max read count value of a feature to be shown in the results:", 
                rpkmCalculationParameters.getMaxReadCount()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("RPKM and read count calculation statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelRPKM.RETURNED_FEATURES,
                this.getStatsMap().get(ResultPanelRPKM.RETURNED_FEATURES)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelRPKM.FEATURES_TOTAL, 
                this.getStatsMap().get(ResultPanelRPKM.FEATURES_TOTAL)));

        exportData.add(statisticsExportData);

        return exportData;
    }

    /**
     * @param noGenomeFeatures The number of features of the reference genome.
     */
    public void setNoGenomeFeatures(int noGenomeFeatures) {
        this.noGenomeFeatures = noGenomeFeatures;
    }

    /**
     * @return The number of features of the reference genome.
     */
    public int getNoGenomeFeatures() {
        return this.noGenomeFeatures;
    }
    
}
