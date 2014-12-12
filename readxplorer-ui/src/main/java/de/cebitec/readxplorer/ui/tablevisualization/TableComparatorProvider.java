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

package de.cebitec.readxplorer.ui.tablevisualization;


import java.util.Comparator;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * Provides different table row comparators for different sorting purposes.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TableComparatorProvider {


    /**
     * Creates and adds a PersistentTrack comparator to the given sorter and the
     * given table column.
     * <p>
     * @param sorter      the table row sorter to which the comparator should be
     *                    added
     * @param tableColumn the table column for which the comparator should be
     *                    added
     */
    public static void setPersistentTrackComparator( TableRowSorter<TableModel> sorter, int tableColumn ) {
        sorter.setComparator( tableColumn, new Comparator<Object>() {
            @Override
            public int compare( Object a, Object b ) {
                return a.toString().compareToIgnoreCase( b.toString() );
            }


        } );
    }


    /**
     * Creates and adds a position string comparator for positions, which might
     * contain "_*" at the end of the position string.
     * <p>
     * @param sorter      the table row sorter to which the comparator should be
     *                    added
     * @param tableColumn the table column for which the comparator should be
     *                    added
     */
    public static void setPositionComparator( TableRowSorter<TableModel> sorter, int tableColumn ) {
        sorter.setComparator( tableColumn, new Comparator<String>() {
            @Override
            public int compare( String a, String b ) {
                if( a.contains( "_" ) ) {
                    a = a.substring( 0, a.length() - 2 );
                }
                if( b.contains( "_" ) ) {
                    b = b.substring( 0, b.length() - 2 );
                }
                Integer intA = Integer.parseInt( a );
                Integer intB = Integer.parseInt( b );
                return intA.compareTo( intB );
            }


        } );
    }


    /**
     * Creates a String comparator, that cuts the string after the first line
     * break "\n" and compares it to the second string afterwards as an Integer.
     * If one of the values is not an integer, they are compared as strings.
     * <p>
     * @param sorter      the table row sorter to which the comparator should be
     *                    added
     * @param tableColumn the table column for which the comparator should be
     *                    added
     */
    public static void setStringComparator( TableRowSorter<TableModel> sorter, int tableColumn ) {

        sorter.setComparator( tableColumn, new Comparator<String>() {
            @Override
            public int compare( String a, String b ) {
                if( a.contains( "\n" ) ) {
                    a = a.substring( 0, a.indexOf( '\n' ) );
                }
                if( b.contains( "\n" ) ) {
                    b = b.substring( 0, b.indexOf( '\n' ) );
                }
                try {
                    Integer intA = Integer.parseInt( a );
                    Integer intB = Integer.parseInt( b );
                    return intA.compareTo( intB );
                }
                catch( NumberFormatException e ) {
                    return a.compareTo( b );
                }
            }


        } );
    }


}
