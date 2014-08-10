package de.cebitec.readXplorer.ui.importer;

import de.cebitec.readXplorer.parser.tables.TableType;
import java.util.List;

/**
 *
 * @author jritter
 */
public interface TranscriptomeTableViewI {

    /**
     * Process the table information given by the List of List for a specified
     * table type.
     *
     * @param tableData List<List<?>> first sheet containing the main table data
     * @param tableData2 List<List<?>> second sheet containing the parameters
     * and statistics of the analysis
     * @param type TableType
     */
    public void process(List<List<?>> tableData, List<List<?>> tableData2, TableType type);

}
