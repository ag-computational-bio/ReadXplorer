package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredFeature;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Converts a List of FilteredGenes into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class FilteredFeaturesColumns implements ExcelExportDataI {

    FilteredFeaturesResult filteredFeaturesResult;
    private final HashMap<String, Integer> filterStatisticsMap;
    
    /** 
     * Converts a List of FilteredGenes into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * @param filteredFeaturesResult the result holding the list of filtered genes
     * @param filterStatisticsMap statistics for the filter result
     */
    public FilteredFeaturesColumns(FilteredFeaturesResult filteredFeaturesResult, HashMap<String, Integer> filterStatisticsMap) {
        this.filteredFeaturesResult = filteredFeaturesResult;
        this.filterStatisticsMap = filterStatisticsMap;
    }
    
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add("Filtered Feature");
        resultDescriptions.add("Start");
        resultDescriptions.add("Stop");
        resultDescriptions.add("Strand");
        resultDescriptions.add("Total Read Count");
        
        dataColumnDescriptions.add(resultDescriptions);
        
        //add feature filering statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Feature Filtering Parameter and Statistics Table");

        dataColumnDescriptions.add(statisticColumnDescriptions);
        
        return dataColumnDescriptions;
    }

    
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> filteredGenesExport = new ArrayList<>();
        List<List<Object>> filteredGenesResult = new ArrayList<>();
        
        for (FilteredFeature filteredFeature : this.filteredFeaturesResult.getResults()) {      
            List<Object> filteredGeneRow = new ArrayList<>();
            
            filteredGeneRow.add(PersistantFeature.getFeatureName(filteredFeature.getFilteredFeature()));
            filteredGeneRow.add(filteredFeature.getFilteredFeature().getStart());
            filteredGeneRow.add(filteredFeature.getFilteredFeature().getStop());
            filteredGeneRow.add(filteredFeature.getFilteredFeature().isFwdStrand() ? "Fwd" : "Rev");
            filteredGeneRow.add(filteredFeature.getReadCount());
            
            filteredGenesResult.add(filteredGeneRow);
        }
        
        filteredGenesExport.add(filteredGenesResult);
        
        //create statistics sheet
        ParameterSetFilteredFeatures filterParameters = (ParameterSetFilteredFeatures) this.filteredFeaturesResult.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();
        List<Object> statisticsExport = new ArrayList<>();

        statisticsExport.add("Feature filtering statistics for tracks:");
        statisticsExport.add(GeneralUtils.generateConcatenatedString(filteredFeaturesResult.getTrackList()));
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between title and parameters
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Feature Filtering parameters:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum number of reads in a feature:");
        statisticsExport.add(filterParameters.getMinNumberReads());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Maximum number of reads in a feature:");
        statisticsExport.add(filterParameters.getMaxNumberReads());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between parameters and statistics
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Feature filtering statistics:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelFilteredFeatures.FEATURES_TOTAL);
        statisticsExport.add(filterStatisticsMap.get(ResultPanelFilteredFeatures.FEATURES_TOTAL));
        statisticsExportData.add(statisticsExport);

        filteredGenesExport.add(statisticsExportData);
        
        return filteredGenesExport;
    }
    
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Filtered Features Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }
}
