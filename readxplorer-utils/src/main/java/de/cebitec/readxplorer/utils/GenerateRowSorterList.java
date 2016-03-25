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


import java.util.Iterator;
import java.util.List;
import javax.swing.table.TableRowSorter;


/**
 *
 * @author kstaderm
 */
public final class GenerateRowSorterList {


    private GenerateRowSorterList() {
    }


    /**
     * Creates a row sorter for the underlying table model and table contents.
     * <p>
     * @param tm the table model of the table used to display the tableContents
     * <p>
     * @return row sorter for the table model
     */
    public static synchronized TableRowSorter<ListTableModel> createRowSorter( ListTableModel tm ) {
        TableRowSorter<ListTableModel> rowSorter = new TableRowSorter<>( tm );
        if( tm.getRowCount() > 1 ) {
            List<List<Object>> tableContents = tm.getDataList();
            List<Object> firstRow = tableContents.get( 0 );
            int columnCounter = 0;
            for( Iterator<Object> it1 = firstRow.iterator(); it1.hasNext(); columnCounter++ ) {
                Object object = it1.next();
                if( object instanceof Double ) {
                    rowSorter.setComparator( columnCounter, (Double o1, Double o2) -> o1.compareTo( o2 ) );
                }
                if( object instanceof Integer ) {
                    rowSorter.setComparator( columnCounter, (Integer o1, Integer o2) -> o1.compareTo( o2 ) );
                }
            }
        }
        return rowSorter;
    }


}
