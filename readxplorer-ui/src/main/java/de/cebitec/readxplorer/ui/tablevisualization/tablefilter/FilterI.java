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


import javax.swing.table.DefaultTableModel;


/**
 * Interface providing functionality for filtering a table.
 *
 * @param <E> some default table model implementation
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface FilterI<E extends DefaultTableModel> {

    /**
     * Filters the given table model by the given column and filter value.
     * <p>
     * @param tableModel  the table model to filter
     * @param column      the column to check for the filterValue
     * @param filterValue the filterValue to use for the given column
     * <p>
     * @return the filtered table model
     */
    E filterTable( E tableModel, int column, Object filterValue );


}
