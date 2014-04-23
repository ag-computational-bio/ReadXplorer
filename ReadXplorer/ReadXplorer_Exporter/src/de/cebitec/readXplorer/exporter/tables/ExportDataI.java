package de.cebitec.readXplorer.exporter.tables;

import java.util.List;

/**
 * By implementing this interface, a data structure is able to provide its data
 * in the correct format to be received by a {@link TableExporterI}.
 * 
 * @author -Rolf Hilker-
 */
public interface ExportDataI {
    
    /**
     * @return creates and returns the list of sheet names which can be used for
     * multiple sheets or files belonging together.
     */
    public List<String> dataSheetNames();
    
    /**
     * @return creates and returns the list of descriptions for the columns.
     */
    public List<List<String>> dataColumnDescriptions();
    
    /**
     * @return creates and returns the list of rows belonging to the table.
     */
    public List<List<List<Object>>> dataToExcelExportList();
}
