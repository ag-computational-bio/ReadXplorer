package de.cebitec.vamp.exporter.excel;

import java.util.List;

/**
 * @author -Rolf Hilker-
 * 
 * By implementing this interface, a data structure is able to provide its data
 * in the correct format to be received by an {@link ExcelExporter}.
 */
public interface ExcelExportDataI {
    
    public List<String> dataColumnDescriptions();
    
    public List<List<Object>> dataToExcelExportList();
}
