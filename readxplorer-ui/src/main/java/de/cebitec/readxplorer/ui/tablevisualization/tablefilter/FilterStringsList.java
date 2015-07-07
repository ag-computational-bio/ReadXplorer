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

package de.cebitec.readxplorer.ui.tablevisualization.tablefilter;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.utils.ListTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Class for filtering a table for a given pattern/string.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>, kstaderm
 */
public class FilterStringsList<T extends ListTableModel> implements FilterListI<T> {

    private final Class<T> classType;


    public FilterStringsList( Class<T> classType ) {
        this.classType = classType;
    }


    /**
     * Only keeps entries in the table, which contain the given filterValue in
     * the given column.
     * <p>
     * @param tableModel  the table model to filter
     * @param column      the column to filter
     * @param filterValue the value to search for
     * <p>
     * @return The new table only containing rows, which contain the given
     *         filterValue in the given column.
     */
    @Override
    public T filterTable( T tableModel, int column, Object filterValue ) {
        String pattern = filterValue.toString();
        TableFilterUtilsList<T> utils = new TableFilterUtilsList( classType );
        T filteredTableModel = utils.prepareNewTableModel( tableModel );
        List<List<Object>> dataVector = tableModel.getDataList();
        for( Iterator<List<Object>> it = dataVector.iterator(); it.hasNext(); ) {
            List<Object> row = it.next();
            Object value = row.get( column );
            if( value instanceof String ) {
                if( ((String) value).contains( pattern ) ) {
                    filteredTableModel.addRow( row );
                }
            } else {
                if( value instanceof PersistentFeature ) {
                    PersistentFeature f = (PersistentFeature) value;
                    if( f.toString().contains( pattern ) ) {
                        filteredTableModel.addRow( row );
                    }
                }
            }
        }
        if( filteredTableModel.getRowCount() < 1 ) {
            filteredTableModel.addRow( new ArrayList<Object>() );
        }
        return filteredTableModel;
    }


}
