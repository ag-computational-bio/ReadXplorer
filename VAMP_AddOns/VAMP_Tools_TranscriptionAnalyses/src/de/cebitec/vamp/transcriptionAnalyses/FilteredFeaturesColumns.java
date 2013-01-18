package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredFeature;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a List of FilteredGenes into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class FilteredFeaturesColumns implements ExcelExportDataI {

    List<FilteredFeature> filteredFeatures;
    
    /** 
     * Converts a List of FilteredGenes into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * @param filteredFeatures the list of filtered genes
     */
    public FilteredFeaturesColumns(List<FilteredFeature> filteredGenes) {
        this.filteredFeatures = filteredGenes;
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
        
        return dataColumnDescriptions;
    }

    
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> filteredGenesExport = new ArrayList<>();
        List<List<Object>> filteredGenesResult = new ArrayList<>();
        
        for (FilteredFeature filteredFeature : this.filteredFeatures) {      
            List<Object> filteredGeneRow = new ArrayList<>();
            
            filteredGeneRow.add(PersistantFeature.getFeatureName(filteredFeature.getFilteredFeature()));
            filteredGeneRow.add(filteredFeature.getFilteredFeature().getStart());
            filteredGeneRow.add(filteredFeature.getFilteredFeature().getStop());
            filteredGeneRow.add(filteredFeature.getFilteredFeature().isFwdStrand() ? "Fwd" : "Rev");
            filteredGeneRow.add(filteredFeature.getReadCount());
            
            filteredGenesResult.add(filteredGeneRow);
        }
        
        filteredGenesExport.add(filteredGenesResult);
        
        return filteredGenesExport;
    }
    
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Filtered Features Table");
        sheetNames.add("Filtered Feature Parameters/Stats");
        return sheetNames;
    }
}
