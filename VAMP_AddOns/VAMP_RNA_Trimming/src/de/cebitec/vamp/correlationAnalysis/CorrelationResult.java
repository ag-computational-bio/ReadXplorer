package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.dataObjects.Snp;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains all data belonging to a correlation analysis data set. Also has the
 * capabilities of transforming the result data into the format readable by
 * ExcelExporters.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class CorrelationResult extends ResultTrackAnalysis<CorrelationResult> {
    
    private List<CorrelatedInterval> correlationsList;
    private int num;
    private int percent;
    private Map<String, Object> params;

    
    /**
     * New CorrelationResult data object.
     * @param correlationsList list of found correlations
     * @param trackNames hashmap of track ids to track names used in the analysis
     */
    public CorrelationResult(List<CorrelatedInterval> correlationsList, Map<Integer, PersistantTrack> trackNames) {
        super(trackNames);
        this.correlationsList = correlationsList;
    }
    
    /**
     * @return the list of correlations found during the analysis step
     */
    public List<CorrelatedInterval> getCorrelationsList() {
        return this.correlationsList;
    }    
    
    /**
     * @return the snp data ready to export with an {@link ExcelExporter}
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> allData = new ArrayList<>();
        List<List<Object>> exportData = new ArrayList<>();
        List<Object> exportLine;
        Snp snp;
        
        
        for (CorrelatedInterval correlation : this.correlationsList) {
            exportLine = new ArrayList<>(); 
            
            exportLine.add(correlation.getDirection());
            exportLine.add(correlation.getFrom());
            exportLine.add(correlation.getTo());
            exportLine.add(correlation.getCorrelation());
            exportLine.add(correlation.getMinPeakCoverage());
   
            exportData.add(exportLine);
        }
        
        allData.add(exportData);
        
        //create statistics sheet
        List<List<Object>> statisticsExportData = new ArrayList<>();
        List<Object> statisticsExport;
        
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Correlation analysis for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Analysis parameters:"));
        for(Entry<String,Object> entry : this.params.entrySet()) {
            statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(entry.getKey()+":", entry.getValue()));
        }
        allData.add(statisticsExportData);
        
        return allData;
    }

    /**
     * @return the snp data column descriptions to export with an 
     * {@link ExcelExporter}
     */
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptionsList = new ArrayList<>();
        
        List<String> dataColumnDescriptions = new ArrayList<>();
        dataColumnDescriptions.add("Strang Direction");
        dataColumnDescriptions.add("Pos From");
        dataColumnDescriptions.add("Pos To");
        dataColumnDescriptions.add("Correlation");
        dataColumnDescriptions.add("Minimum Peak Coverage");
        
        dataColumnDescriptionsList.add(dataColumnDescriptions);
        
        //add snp statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Analysis parameter and statistics table");
        dataColumnDescriptionsList.add(statisticColumnDescriptions);
        
        return dataColumnDescriptionsList;
    }
    
    /**
     * @return the snp data sheet names ready to export with an 
     * {@link ExcelExporter}
     */
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Correlations");
        sheetNames.add("Analysis Statistics");
        return sheetNames;
    }
    

    /**
     * Sets used analysis parameters to have them connected with the search
     * results.
     * @param params 
     */
    public void setAnalysisParameters(Map<String, Object> params) {
        this.params = params;
    }
    
    /**
     * returns the used analysis parameters
     * @return 
     */
    public Map<String, Object> getAnalysisParameters() {
        return this.params;
    }

}
