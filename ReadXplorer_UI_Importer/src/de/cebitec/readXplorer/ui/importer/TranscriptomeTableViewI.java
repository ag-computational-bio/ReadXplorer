
package de.cebitec.readXplorer.ui.importer;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.parser.tables.TableType;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author jritter
 */
public interface TranscriptomeTableViewI {

    /**
     * Process the table information given by the List of List for a specified
     * table type in csv format.
     *
     * @param tableData  List<List<?>> first sheet containing the main table
     *                   data
     * @param tableData2 List<List<?>> second sheet containing the parameters
     *                   and statistics of the analysis
     * @param reference  PersistentReference
     * @param type       TableType
     */
    public void processCsvInput( List<List<?>> tableData, List<List<?>> tableData2, TableType type, PersistentReference reference );


    /**
     * Process the table information given by the List of List for a specified
     * table type in .xls format.
     * <p>
     * @param reference              PersistentReference
     * @param model                  DefaultTableModel contains all table
     *                               entries.
     * @param secondSheetMap         Map<String, String> contains all entries of
     *                               the
     *                               parameters and statistics excel sheet.
     * @param secondSheetMapThirdCol Map<String, String> contains all entries of
     *                               the parameters and statistics excel sheet in the third column.
     */
    public void processXlsInput( PersistentReference reference, DefaultTableModel model, Map<String, String> secondSheetMap, Map<String, String> secondSheetMapThirdCol );


}
