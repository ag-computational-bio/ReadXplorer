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


import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;


/**
 *
 * @author kstaderm
 */
public final class GenerateRowSorter {


    private GenerateRowSorter() {
    }


    /**
     * Creates a row sorter for the underlying table model and table contents.
     * <p>
     * @param tm the table model of the table used to display the tableContents
     * <p>
     * @return row sorter for the table model
     */
    public static synchronized TableRowSorter<DefaultTableModel> createRowSorter( DefaultTableModel tm ) {
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>( tm );
        if( tm.getRowCount() > 1 ) {
            Vector<Vector> tableContents = tm.getDataVector();
            Vector<?> firstRow = tableContents.get( 0 );
            int columnCounter = 0;
            for( Iterator<?> it1 = firstRow.iterator(); it1.hasNext(); columnCounter++ ) {
                Object object = it1.next();
                if( object instanceof Double ) {
                    rowSorter.setComparator( columnCounter, new Comparator<Double>() {

                        @Override
                        public int compare( Double o1, Double o2 ) {
                            return o1.compareTo( o2 );
                        }


                    } );
                }
                if( object instanceof Integer ) {
                    rowSorter.setComparator( columnCounter, new Comparator<Integer>() {

                        @Override
                        public int compare( Integer o1, Integer o2 ) {
                            return o1.compareTo( o2 );
                        }


                    } );
                }

            }
        }
        return rowSorter;
    }


}
