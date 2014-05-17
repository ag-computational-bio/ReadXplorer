/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.view.tableVisualization.tableFilter;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Class for filtering a table for a given pattern/string.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilterStrings<T extends DefaultTableModel> implements FilterI<T>{
    
    private final Class<T> classType;

    public FilterStrings(Class<T> classType) {
        this.classType = classType;
    }
    
    /**
     * Only keeps entries in the table, which contain the given filterValue
     * in the given column.
     * @param tableModel the table model to filter
     * @param column the column to filter
     * @param filterValue the value to search for
     * @return The new table only containing rows, which contain the given
     * filterValue in the given column.
     */
    @Override
    public T filterTable(T tableModel, int column, Object filterValue) {
        String pattern = filterValue.toString();
        TableFilterUtils<T> utils = new TableFilterUtils(classType);
        T filteredTableModel = utils.prepareNewTableModel(tableModel);
        Vector dataVector = tableModel.getDataVector();
        for (Iterator<Vector> it = dataVector.iterator(); it.hasNext();) {
            Vector row = it.next();
            if (((String) row.get(column)).contains(pattern)) {
                filteredTableModel.addRow(row);
            }
        }
        if (filteredTableModel.getRowCount() < 1) {
            filteredTableModel.addRow(new Vector());
        }
        return filteredTableModel;
    }
    
}
