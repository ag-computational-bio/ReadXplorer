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


import de.cebitec.readxplorer.utils.ListTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Class for filtering integer or double value entries from a table.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>, kstaderm
 * @param <T> Type of TableModel extending ListTableModel
 */
public class FilterDoubleValuesList<T extends ListTableModel> implements
        FilterListI<T> {

    private final FilterValuesListI valueFilter;
    private final Class<T> classType;


    public FilterDoubleValuesList( FilterValuesListI valueFilter, Class<T> classType ) {
        this.valueFilter = valueFilter;
        this.classType = classType;
    }


    /**
     * Filters values which are lower than a given cutoff from the given column
     * of the table.
     * <p>
     * @param tableModel  the table whose content shall be filtered
     * @param column      the column, which shall be filtered
     * @param filterValue the minimum double value in the given column to keep
     *                    the current row in the table
     * <p>
     * @return the filtered table model only containing rows whose values in the
     *         selected column exceed the given double filterValue
     */
    @Override
    @SuppressWarnings( "unchecked" )
    public T filterTable( T tableModel, int column, Object filterValue ) {
        double cutoff = 0;
        if( filterValue instanceof Double ) {
            cutoff = (Double) filterValue;
        }
        TableFilterUtilsList<T> utils = new TableFilterUtilsList<>( classType );
        T filteredTableModel = utils.prepareNewTableModel( tableModel );
        List<List<Object>> dataVector = tableModel.getDataList();
        for( Iterator<List<Object>> it = dataVector.iterator(); it.hasNext(); ) {
            List<Object> row = it.next();
            Object currentEntry = row.get( column );
            if( currentEntry instanceof Integer ) {
                int intValue = (Integer) currentEntry;
                valueFilter.filterTable( filteredTableModel, row, cutoff, intValue );
            }
            if( currentEntry instanceof Double ) {
                valueFilter.filterTable( filteredTableModel, row, cutoff, (double) currentEntry );
            }
        }
        if( filteredTableModel.getRowCount() < 1 ) {
            filteredTableModel.addRow( new ArrayList<Object>() );
        }
        return filteredTableModel;
    }


}
