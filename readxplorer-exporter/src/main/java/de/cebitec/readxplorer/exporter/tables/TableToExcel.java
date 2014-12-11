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

package de.cebitec.readxplorer.exporter.tables;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;


/**
 * Converts a DefaultTableModel in a suitable format for export. The instance of
 * this class can be handed over to the ExcelExportFileChooser.
 * <p>
 * @author kstaderm
 */
public class TableToExcel implements ExportDataI {

    private final String tableName;
    private List<String> columnName;
    private List<List<Object>> tableContent;


    /**
     * Converts a DefaultTableModel in a suitable format for export. The
     * instance
     * of this class can be handed over to the ExcelExportFileChooser.
     * <p>
     * @param tableName  The name of the table that should be exported.
     * @param tableModel The DefaultTableModel containing the Table data.
     */
    public TableToExcel( String tableName, DefaultTableModel tableModel ) {
        this.tableName = tableName;
        setData( tableModel );
    }


    private void setData( DefaultTableModel tableModel ) {
        Vector dataVector = tableModel.getDataVector();
        int columnCount = tableModel.getColumnCount();
        columnName = new ArrayList<>();
        for( int i = 0; i < columnCount; i++ ) {
            columnName.add( tableModel.getColumnName( i ) );
        }
        tableContent = new ArrayList<>();
        for( Iterator<Vector> it = dataVector.iterator(); it.hasNext(); ) {
            Vector row = it.next();
            tableContent.add( new ArrayList<>( row ) );
        }
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> ret = new ArrayList<>();
        ret.add( tableName );
        return ret;
    }


    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> ret = new ArrayList<>();
        ret.add( columnName );
        return ret;
    }


    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> ret = new ArrayList<>();
        ret.add( tableContent );
        return ret;
    }


}
