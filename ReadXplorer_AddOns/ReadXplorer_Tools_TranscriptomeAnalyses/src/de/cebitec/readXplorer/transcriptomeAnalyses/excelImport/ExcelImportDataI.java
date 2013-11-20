/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jritter
 */
public interface ExcelImportDataI {
    
        /**
     * @return creates and returns the list of sheet names
     */
    public List<String> dataSheetNames();
    
    /**
     * @return creates and returns the list of descriptions for the columns.
     */
    public List<List<String>> dataColumnDescriptions();
    
    /**
     * @return creates and returns the list of rows belonging to the excel file.
     */
    public DefaultTableModel dataToDataTableImport();
}
