package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredGene;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author -Rolf Hilker-
 * 
 * Converts a List of ExpressedGenes into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 */
class FilteredGenesColumns implements ExcelExportDataI {

    List<FilteredGene> expressedGenes;
    
    /** 
     * Converts a List of ExpressedGenes into the format readable for the ExcelExporter.
     * Generates both, the header and the data to write.
     * @param expressedGenes the list of expressed genes
     */
    public FilteredGenesColumns(List<FilteredGene> expressedGenes) {
        this.expressedGenes = expressedGenes;
    }
    
    @Override
    public List<String> dataColumnDescriptions() {
     List<String> dataColumnDescriptions = new ArrayList();
        
        dataColumnDescriptions.add("Expressed Gene");
        dataColumnDescriptions.add("Start");
        dataColumnDescriptions.add("Stop");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Total Read Count");
        
        return dataColumnDescriptions;
    }

    
    @Override
    public List<List<Object>> dataToExcelExportList() {
        List<List<Object>> expressedGenesExport = new ArrayList<List<Object>>();
        
        for (FilteredGene expressedGene : this.expressedGenes) {      
            List<Object> expressedGeneRow = new ArrayList<Object>();
            
            expressedGeneRow.add(PersistantAnnotation.getAnnotationName(expressedGene.getFilteredAnnotation()));
            expressedGeneRow.add(expressedGene.getFilteredAnnotation().getStart());
            expressedGeneRow.add(expressedGene.getFilteredAnnotation().getStop());
            expressedGeneRow.add(expressedGene.getFilteredAnnotation().getStrand() == SequenceUtils.STRAND_FWD ? "Fwd" : "Rev");
            expressedGeneRow.add(expressedGene.getReadCount());
            
            expressedGenesExport.add(expressedGeneRow);
        }
        
        return expressedGenesExport;
    }
    
}
