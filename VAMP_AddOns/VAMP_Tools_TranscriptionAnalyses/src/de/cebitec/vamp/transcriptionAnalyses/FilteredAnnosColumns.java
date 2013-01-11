package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredAnnotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a List of FilteredGenes into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 *
 * @author -Rolf Hilker-
 */
class FilteredAnnosColumns implements ExcelExportDataI {

    List<FilteredAnnotation> filteredGenes;
    
    /** 
     * Converts a List of FilteredGenes into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * @param filteredGenes the list of filtered genes
     */
    public FilteredAnnosColumns(List<FilteredAnnotation> filteredGenes) {
        this.filteredGenes = filteredGenes;
    }
    
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add("Expressed Gene");
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
        
        for (FilteredAnnotation filteredGene : this.filteredGenes) {      
            List<Object> filteredGeneRow = new ArrayList<>();
            
            filteredGeneRow.add(PersistantAnnotation.getAnnotationName(filteredGene.getFilteredAnnotation()));
            filteredGeneRow.add(filteredGene.getFilteredAnnotation().getStart());
            filteredGeneRow.add(filteredGene.getFilteredAnnotation().getStop());
            filteredGeneRow.add(filteredGene.getFilteredAnnotation().isFwdStrand() ? "Fwd" : "Rev");
            filteredGeneRow.add(filteredGene.getReadCount());
            
            filteredGenesResult.add(filteredGeneRow);
        }
        
        filteredGenesExport.add(filteredGenesResult);
        
        return filteredGenesExport;
    }
    
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Filtered Annotations Table");
        sheetNames.add("Filtered Annotation Parameters/Stats");
        return sheetNames;
    }
}
