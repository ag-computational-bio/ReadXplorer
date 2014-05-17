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

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Class for filtering values which are lower than a given cutoff.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilterSmallValues implements FilterValuesI {

    /**
     * Filters values which are smaller than a given cutoff from the given 
     * column of the table.
     * @param tableModel the table whose content shall be filtered
     * @param column the column, which shall be filtered
     * @param filterValue the minimum double value in the given column to keep 
     * the current row in the table
     * @return the filtered table model only containing rows whose values in 
     * the selected column exceed the given double filterValue
     */
    @Override
    public void filterTable(DefaultTableModel filteredTableModel, Vector row, double cutoff, double currentEntryValue) {
        if (currentEntryValue >= cutoff) {
            filteredTableModel.addRow(row);
        }
    }
    
}
