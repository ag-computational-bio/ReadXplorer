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

package de.cebitec.readxplorer.utils;


import java.util.Vector;
import javax.swing.table.DefaultTableModel;


/**
 * Default table model, which does not allow editing of any table cell.
 * <p>
 * @author kstaderm
 */
public class UneditableTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;


    /**
     * Default table model, which does not allow editing of any table cell.
     */
    public UneditableTableModel() {
        super();
    }


    public UneditableTableModel( Object[][] data, Object[] columnNames ) {
        super( data, columnNames );
    }


    public UneditableTableModel( Object[] columnNames, int rowCount ) {
        super( columnNames, rowCount );
    }


    public UneditableTableModel( Vector data, Vector columnNames ) {
        super( data, columnNames );
    }


    public UneditableTableModel( Vector columnNames, int rowCount ) {
        super( columnNames, rowCount );
    }


    public UneditableTableModel( int rowCount, int columnCount ) {
        super( rowCount, columnCount );
    }


    @Override
    public boolean isCellEditable( int row, int column ) {
        return false;
    }


}
