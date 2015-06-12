/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;


/**
 * This is an implementation of <code>TableModel</code> that uses a
 * <code>List</code> of <code>List</code> to store the cell value objects.
 * <p>
 * @author kstaderm
 * <p>
 * @see TableModel
 */
public class ListTableModel extends AbstractTableModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The <code>List</code> of <code>List</code> of <code>Object</code> values.
     */
    protected List<List<Object>> dataList;

    /**
     * The <code>List</code> of column identifiers.
     */
    protected List<Object> columnIdentifiers;


    /**
     * Constructs a default <code>ListTableModel</code> which is a table of
     * zero columns and zero rows.
     */
    public ListTableModel() {
        this( 0, 0 );
    }


    /**
     * Constructs a <code>ListTableModel</code> with <code>rowCount</code>
     * and <code>columnCount</code> of <code>null</code> object values.
     * <p>
     * @param rowCount    the number of rows the table holds
     * @param columnCount the number of columns the table holds
     * <p>
     * @see #setValueAt
     */
    public ListTableModel( int rowCount, int columnCount ) {
        this( new ArrayList<>( columnCount ), rowCount );
    }


    /**
     * Constructs a <code>ListTableModel</code> with as many columns as there
     * are elements in <code>columnNames</code> and <code>rowCount</code> of
     * <code>null</code> object values. Each column's name will be taken from
     * the <code>columnNames</code> list.
     * <p>
     * @param columnNames <code>list</code> containing the names of the new
     *                    columns; if this is <code>null</code> then the model
     *                    has no columns
     * @param rowCount    the number of rows the table holds
     * <p>
     * @see #setDataList
     * @see #setValueAt
     */
    public ListTableModel( List<Object> columnNames, int rowCount ) {
        ListTableModel.this.setDataList( new ArrayList<>( rowCount ), columnNames );
    }


    /**
     * Constructs a <code>ListTableModel</code> with as many columns as there
     * are elements in <code>columnNames</code> and <code>rowCount</code> of
     * <code>null</code> object values. Each column's name will be taken from
     * the <code>columnNames</code> array.
     * <p>
     * @param columnNames <code>array</code> containing the names of the new
     *                    columns; if this is <code>null</code> then the model
     *                    has no columns
     * @param rowCount    the number of rows the table holds
     * <p>
     * @see #setDataList
     * @see #setValueAt
     */
    public ListTableModel( Object[] columnNames, int rowCount ) {
        this( ListTableModel.convertToList( columnNames ), rowCount );
    }


    /**
     * Constructs a <code>ListTableModel</code> and initializes the table by
     * passing <code>data</code> and <code>columnNames</code> to the
     * <code>setDataList</code> method.
     * <p>
     * @param data        the data of the table, a <code>List</code> of
     *                    <code>List</code>s of <code>Object</code> values
     * @param columnNames <code>List</code> containing the names of the new
     *                    columns
     * <p>
     * @see #getDataList
     * @see #setDataList
     */
    public ListTableModel( List<List<Object>> data, List<Object> columnNames ) {
        ListTableModel.this.setDataList( data, columnNames );
    }


    /**
     * Constructs a <code>ListTableModel</code> and initializes the table by
     * passing <code>data</code> and <code>columnNames</code> to the
     * <code>setDataList</code> method. The first index in the
     * <code>Object[][]</code> array is the row index and the second is the
     * column index.
     * <p>
     * @param data        the data of the table
     * @param columnNames the names of the columns
     * <p>
     * @see #getDataList
     * @see #setDataList
     */
    public ListTableModel( Object[][] data, Object[] columnNames ) {
        setDataList( data, columnNames );
    }


    /**
     * Returns the <code>List</code> of <code>Lists</code> that contains the
     * table's data values. The lists contained in the outer list are each a
     * single row of values. In other words, to get to the cell at row 1, column
     * 5:
     * <p>
     *
     * <code>((List)getDataList().get(1)).get(5);</code>
     * <p>
     * @return the list of lists containing the tables data values
     * <p>
     * @see #newDataAvailable
     * @see #newRowsAdded
     * @see #setDataList
     */
    public List<List<Object>> getDataList() {
        return dataList;
    }


    /**
     * Replaces the current <code>dataList</code> instance variable with the new
     * <code>List</code> of rows, <code>dataList</code>. Each row is represented
     * in <code>dataList</code> as a <code>List</code> of <code>Object</code>
     * values. <code>columnIdentifiers</code> are the names of the new columns.
     * The first name in <code>columnIdentifiers</code> is mapped to column 0 in
     * <code>dataList</code>. Each row in <code>dataList</code> is adjusted to
     * match the number of columns in <code>columnIdentifiers</code> either by
     * truncating the <code>List</code> if it is too long, or adding
     * <code>null</code> values if it is too short.
     * <p>
     * Note that passing in a <code>null</code> value for <code>dataList</code>
     * results in unspecified behavior, an possibly an exception.
     * <p>
     * @param dataList          the new data list
     * @param columnIdentifiers the names of the columns
     * <p>
     * @see #getDataList
     */
    public void setDataList( List<List<Object>> dataList, List<Object> columnIdentifiers ) {
        if( dataList == null ) {
            dataList = new ArrayList<>();
        }
        this.dataList = dataList;
        if( columnIdentifiers == null ) {
            columnIdentifiers = new ArrayList<>();
        }
        this.columnIdentifiers = columnIdentifiers;
        justifyRows( 0, getRowCount() );
        fireTableStructureChanged();
    }


    /**
     * Replaces the value in the <code>dataList</code> instance variable with
     * the values in the array <code>dataList</code>. The first index in the
     * <code>Object[][]</code> array is the row index and the second is the
     * column index. <code>columnIdentifiers</code> are the names of the new
     * columns.
     * <p>
     * @param dataList          the new data list
     * @param columnIdentifiers the names of the columns
     * <p>
     * @see #setDataList(List, List)
     */
    public final void setDataList( Object[][] dataList, Object[] columnIdentifiers ) {
        ListTableModel.this.setDataList( convertToList( dataList ), ListTableModel.convertToList( columnIdentifiers ) );
    }


    /**
     * Equivalent to <code>fireTableChanged</code>.
     * <p>
     * @param event the change event
     * <p>
     */
    public void newDataAvailable( TableModelEvent event ) {
        fireTableChanged( event );
    }

//
// Manipulating rows
//

    private void justifyRows( int from, int to ) {
        for( int i = from; i < to; i++ ) {
            if( dataList.get( i ) == null ) {
                dataList.set( i, new ArrayList<>( getColumnCount() ) );
            }
        }
    }


    /**
     * Ensures that the new rows have the correct number of columns. This is
     * accomplished by using the <code>setSize</code> method in
     * <code>List</code> which truncates lists which are too long, and appends
     * <code>null</code>s if they are too short. This method also sends out a
     * <code>tableChanged</code> notification message to all the listeners.
     * <p>
     * @param e this <code>TableModelEvent</code> describes where the rows were
     *          added. If <code>null</code> it assumes all the rows were newly
     *          added
     * <p>
     * @see #getDataList
     */
    public void newRowsAdded( TableModelEvent e ) {
        justifyRows( e.getFirstRow(), e.getLastRow() + 1 );
        fireTableChanged( e );
    }


    /**
     * Equivalent to <code>fireTableChanged</code>.
     * <p>
     * @param event the change event
     * <p>
     */
    public void rowsRemoved( TableModelEvent event ) {
        fireTableChanged( event );
    }


    /**
     * Adds a row to the end of the model. The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     * <p>
     * @param rowData optional data of the row being added
     */
    public void addRow( List<Object> rowData ) {
        insertRow( getRowCount(), rowData );
    }


    /**
     * Adds a row to the end of the model. The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     * <p>
     * @param rowData optional data of the row being added
     */
    public void addRow( Object[] rowData ) {
        addRow( ListTableModel.convertToList( rowData ) );
    }


    /**
     * Inserts a row at <code>row</code> in the model. The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     * <p>
     * @param row     the row index of the row to be inserted
     * @param rowData optional data of the row being added
     * <p>
     * @exception ArrayIndexOutOfBoundsException if the row was invalid
     */
    public void insertRow( int row, List<Object> rowData ) {
        dataList.add( row, rowData );
        justifyRows( row, row + 1 );
        fireTableRowsInserted( row, row );
    }


    /**
     * Inserts a row at <code>row</code> in the model. The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     * <p>
     * @param row     the row index of the row to be inserted
     * @param rowData optional data of the row being added
     * <p>
     * @exception ArrayIndexOutOfBoundsException if the row was invalid
     */
    public void insertRow( int row, Object[] rowData ) {
        insertRow( row, ListTableModel.convertToList( rowData ) );
    }


    private static int gcd( int i, int j ) {
        return (j == 0) ? i : gcd( j, i % j );
    }


    private static void rotate( List<List<Object>> l, int a, int b, int shift ) {
        int size = b - a;
        int r = size - shift;
        int g = gcd( size, r );
        for( int i = 0; i < g; i++ ) {
            int to = i;
            List<Object> tmp = l.get( a + to );
            for( int from = (to + r) % size; from != i; from = (to + r) % size ) {
                l.set( a + to, l.get( a + from ) );
                to = from;
            }
            l.set( a + to, tmp );
        }
    }


    /**
     * Moves one or more rows from the inclusive range <code>start</code> to
     * <code>end</code> to the <code>to</code> position in the model. After the
     * move, the row that was at index <code>start</code> will be at index
     * <code>to</code>. This method will send a <code>tableChanged</code>
     * notification message to all the listeners.
     * <p>
     * <
     * pre>
     *  Examples of moves:
     *
     *  1. moveRow(1,3,5);
     *          a|B|C|D|e|f|g|h|i|j|k   - before
     *          a|e|f|g|h|B|C|D|i|j|k   - after
     *
     *  2. moveRow(6,7,1);
     *          a|b|c|d|e|f|G|H|i|j|k   - before
     *          a|G|H|b|c|d|e|f|i|j|k   - after
     * </pre>
     * <p>
     * @param start the starting row index to be moved
     * @param end   the ending row index to be moved
     * @param to    the destination of the rows to be moved
     * <p>
     * @exception ArrayIndexOutOfBoundsException if any of the elements would be
     *                                           moved out of the table's range
     * <p>
     */
    public void moveRow( int start, int end, int to ) {
        int shift = to - start;
        int first, last;
        if( shift < 0 ) {
            first = to;
            last = end;
        } else {
            first = start;
            last = to + end - start;
        }
        rotate( dataList, first, last + 1, shift );

        fireTableRowsUpdated( first, last );
    }


    /**
     * Removes the row at <code>row</code> from the model. Notification of the
     * row being removed will be sent to all the listeners.
     * <p>
     * @param row the row index of the row to be removed
     * <p>
     * @exception ArrayIndexOutOfBoundsException if the row was invalid
     */
    public void removeRow( int row ) {
        dataList.remove( row );
        fireTableRowsDeleted( row, row );
    }

//
// Manipulating columns
//

    /**
     * Replaces the column identifiers in the model. If the number of
     * <code>newIdentifier</code>s is greater than the current number of
     * columns, new columns are added to the end of each row in the model. If
     * the number of <code>newIdentifier</code>s is less than the current number
     * of columns, all the extra columns at the end of a row are discarded.
     * <p>
     * @param columnIdentifiers list of column identifiers. If
     *                          <code>null</code>, set the model to zero columns
     * <p>
     * @see #setNumRows
     */
    public void setColumnIdentifiers( List<Object> columnIdentifiers ) {
        ListTableModel.this.setDataList( dataList, columnIdentifiers );
    }


    /**
     * Replaces the column identifiers in the model. If the number of
     * <code>newIdentifier</code>s is greater than the current number of
     * columns, new columns are added to the end of each row in the model. If
     * the number of <code>newIdentifier</code>s is less than the current number
     * of columns, all the extra columns at the end of a row are discarded.
     * <p>
     * @param newIdentifiers array of column identifiers. If <code>null</code>,
     *                       set the model to zero columns
     * <p>
     * @see #setNumRows
     */
    public void setColumnIdentifiers( Object[] newIdentifiers ) {
        setColumnIdentifiers( ListTableModel.convertToList( newIdentifiers ) );
    }


    /**
     * Sets the number of columns in the model. If the new size is greater than
     * the current size, new columns are added to the end of the model with
     * <code>null</code> cell values. If the new size is less than the current
     * size, all columns at index <code>columnCount</code> and greater are
     * discarded.
     * <p>
     * @param columnCount the new number of columns in the model
     * <p>
     * @see #setColumnCount
     * @since 1.3
     */
    public void setColumnCount( int columnCount ) {
        justifyRows( 0, getRowCount() );
        fireTableStructureChanged();
    }


    /**
     * Adds a column to the model. The new column will have the identifier
     * <code>columnName</code>, which may be null. This method will send a
     * <code>tableChanged</code> notification message to all the listeners. This
     * method is a cover for <code>addColumn(Object, List)</code> which uses
     * <code>ArrayList</code> as the data list.
     * <p>
     * @param columnName the identifier of the column being added
     */
    public void addColumn( Object columnName ) {
        addColumn( columnName, new ArrayList<>() );
    }


    /**
     * Adds a column to the model. The new column will have the identifier
     * <code>columnName</code>, which may be null. <code>columnData</code> is
     * the optional list of data for the column. If it is <code>null</code> the
     * column is filled with <code>null</code> values. Otherwise, the new data
     * will be added to model starting with the first element going to row 0,
     * etc. This method will send a <code>tableChanged</code> notification
     * message to all the listeners.
     * <p>
     * @param columnName the identifier of the column being added
     * @param columnData optional data of the column being added
     */
    public void addColumn( Object columnName, List<Object> columnData ) {
        columnIdentifiers.add( columnName );
        if( columnData != null ) {
            int columnSize = columnData.size();
            justifyRows( 0, getRowCount() );
            int newColumn = getColumnCount() - 1;
            for( int i = 0; i < columnSize; i++ ) {
                List<Object> row = dataList.get( i );
                row.set(newColumn, columnData.get( i ) );
            }
        } else {
            justifyRows( 0, getRowCount() );
        }

        fireTableStructureChanged();
    }


    /**
     * Adds a column to the model. The new column will have the identifier
     * <code>columnName</code>.  <code>columnData</code> is the optional array of
     * data for the column. If it is <code>null</code> the column is filled with
     * <code>null</code> values. Otherwise, the new data will be added to model
     * starting with the first element going to row 0, etc. This method will
     * send a <code>tableChanged</code> notification message to all the
     * listeners.
     * <p>
     * @param columnName the identifier of the column being added
     * @param columnData optional data of the column being added
     * <p>
     * @see #addColumn(Object, List)
     */
    public void addColumn( Object columnName, Object[] columnData ) {
        addColumn( columnName, ListTableModel.convertToList( columnData ) );
    }

//
// Implementing the TableModel interface
//

    /**
     * Returns the number of rows in this data table.
     * <p>
     * @return the number of rows in the model
     */
    @Override
    public int getRowCount() {
        return dataList.size();
    }


    /**
     * Returns the number of columns in this data table.
     * <p>
     * @return the number of columns in the model
     */
    @Override
    public int getColumnCount() {
        return columnIdentifiers.size();
    }


    /**
     * Returns the column name.
     * <p>
     * @return a name for this column using the string value of the appropriate
     *         member in <code>columnIdentifiers</code>. If
     *         <code>columnIdentifiers</code> does not have an entry for this
     *         index, returns the default name provided by the superclass.
     */
    @Override
    public String getColumnName( int column ) {
        Object id = null;
        // This test is to cover the case when
        // getColumnCount has been subclassed by mistake ...
        if( column < columnIdentifiers.size() && (column >= 0) ) {
            id = columnIdentifiers.get( column );
        }
        return (id == null) ? super.getColumnName( column )
               : id.toString();
    }


    /**
     * Returns true regardless of parameter values.
     * <p>
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     * <p>
     * @return true
     * <p>
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable( int row, int column ) {
        return true;
    }


    /**
     * Returns an attribute value for the cell at <code>row</code> and
     * <code>column</code>.
     * <p>
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     * <p>
     * @return the value Object at the specified cell
     * <p>
     * @exception ArrayIndexOutOfBoundsException if an invalid row or column was
     *                                           given
     */
    @Override
    public Object getValueAt( int row, int column ) {
        List<Object> rowList = dataList.get( row );
        return rowList.get( column );
    }


    /**
     * Sets the object value for the cell at <code>column</code> and
     * <code>row</code>.  <code>aValue</code> is the new value. This method will
     * generate a <code>tableChanged</code> notification.
     * <p>
     * @param aValue the new value; this can be null
     * @param row    the row whose value is to be changed
     * @param column the column whose value is to be changed
     * <p>
     * @exception ArrayIndexOutOfBoundsException if an invalid row or column was
     *                                           given
     */
    @Override
    public void setValueAt( Object aValue, int row, int column ) {
        List<Object> rowList = dataList.get( row );
        rowList.set( column, aValue );
        fireTableCellUpdated( row, column );
    }

//
// Protected Methods
//

    /**
     * Returns a list that contains the same objects as the array.
     * <p>
     * @param anArray the array to be converted
     * <p>
     * @return the new list; if <code>anArray</code> is <code>null</code>,
     *         returns <code>null</code>
     */
    protected static List<Object> convertToList( Object[] anArray ) {
        if( anArray == null ) {
            return null;
        }
        return Arrays.asList( anArray );
    }


    /**
     * Returns a list of lists that contains the same objects as the array.
     * <p>
     * @param anArray the double array to be converted
     * <p>
     * @return the new list of lists; if <code>anArray</code> is
     *         <code>null</code>, returns <code>null</code>
     */
    protected static List<List<Object>> convertToList( Object[][] anArray ) {
        if( anArray == null ) {
            return null;
        }
        List<List<Object>> l = new ArrayList<>( anArray.length );
        for( Object[] o : anArray ) {
            l.add( ListTableModel.convertToList( o ) );
        }
        return l;
    }


}
