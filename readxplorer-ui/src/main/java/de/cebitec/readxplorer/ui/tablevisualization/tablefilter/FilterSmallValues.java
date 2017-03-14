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


import java.util.Vector;
import javax.swing.table.DefaultTableModel;


/**
 * Class for filtering values which are lower than a given cutoff.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilterSmallValues implements FilterValuesI {

    /**
     * Filters values which are smaller than a given cutoff from the given
     * column of the table. The filtered table model is updated and only
     * contains rows whose values in the selected column exceed the given
     * double filterValue.
     * <p>
     * @param filteredTableModel the table whose content shall be filtered
     * @param row                the row, which shall be filtered
     * @param cutoff             the minimum double value in the given column to
     *                           keep
     *                           the current row in the table
     * @param currentEntryValue  filter value associated with the given row
     */
    @Override
    public void filterTable( DefaultTableModel filteredTableModel, Vector row, double cutoff, double currentEntryValue ) {
        if( currentEntryValue >= cutoff ) {
            filteredTableModel.addRow( row );
        }
    }


}
