/*
 * Copyright (C) 2015 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

import java.util.List;


/**
 *
 * @author kstaderm
 */
public class UneditableListTableModel extends ListTableModel {

    private static final long serialVersionUID = 1L;


    public UneditableListTableModel() {
        super();
    }


    public UneditableListTableModel( int rowCount, int columnCount ) {
        super( rowCount, columnCount );
    }


    public UneditableListTableModel( List<Object> columnNames, int rowCount ) {
        super( columnNames, rowCount );
    }


    public UneditableListTableModel( Object[] columnNames, int rowCount ) {
        super( columnNames, rowCount );
    }


    public UneditableListTableModel( List<List<Object>> data, List<Object> columnNames ) {
        super( data, columnNames );
    }


    public UneditableListTableModel( Object[][] data, Object[] columnNames ) {
        super( data, columnNames );
    }


    @Override
    public boolean isCellEditable( int row, int column ) {
        return false;
    }


}
