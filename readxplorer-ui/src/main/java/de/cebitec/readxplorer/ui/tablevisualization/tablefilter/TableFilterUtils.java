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
import org.openide.util.Exceptions;


/**
 * Provides some standard table filter util methods.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 * @param <E> A DefaultTableModel implementation
 */
public class TableFilterUtils<E extends DefaultTableModel> {

    private final Class<E> classType;


    /**
     * Provides some standard table filter util methods.
     * <p>
     * @param classType The class type of this table
     */
    public TableFilterUtils( Class<E> classType ) {
        this.classType = classType;
    }


    /**
     * Prepares a new table model of the same type and with the same headers, as
     * the given model.
     * <p>
     * @param tableModel the tabel model to recreate with headers, but without
     *                   content.
     * <p>
     * @return the new table model
     */
    public E prepareNewTableModel( E tableModel ) {
        E newTableModel = null;
        try {
            newTableModel = classType.newInstance();
            String[] columnNames = new String[tableModel.getColumnCount()];
            for( int i = 0; i < tableModel.getColumnCount(); i++ ) {
                columnNames[i] = tableModel.getColumnName( i );
            }
            newTableModel.setColumnIdentifiers( columnNames );
        } catch( InstantiationException | IllegalAccessException ex ) {
            Exceptions.printStackTrace( ex );
        }
        return newTableModel;
    }


}
