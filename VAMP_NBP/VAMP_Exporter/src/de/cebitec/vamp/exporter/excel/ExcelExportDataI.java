package de.cebitec.vamp.exporter.excel;

import java.util.List;

/**
 * By implementing this interface, a data structure is able to provide its data
 * in the correct format to be received by an {@link ExcelExporter}.
 * 
 * @author -Rolf Hilker-
 */
public interface ExcelExportDataI {
    
    /**
     * @return creates and returns the list of descriptions for the columns.
     */
    public List<String> dataColumnDescriptions();
    
    /**
     * @return creates and returns the list of rows belonging to the table.
     */
    public List<List<Object>> dataToExcelExportList();
}
