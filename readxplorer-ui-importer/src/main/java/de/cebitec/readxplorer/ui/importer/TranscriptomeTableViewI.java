/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readxplorer.ui.importer;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.parser.tables.TableType;
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
    void processCsvInput( List<List<?>> tableData, List<List<?>> tableData2, TableType type, PersistentReference reference );


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
    void processXlsInput( PersistentReference reference, DefaultTableModel model, Map<String, String> secondSheetMap, Map<String, String> secondSheetMapThirdCol );


}
