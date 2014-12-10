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


import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.TableModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class TableUtilsTest {

    public TableUtilsTest() {
    }


    @BeforeClass
    public static void setUpClass() {
    }


    @AfterClass
    public static void tearDownClass() {
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of getSelectedModelRow method, of class TableUtils.
     */
    @Test
    public void testGetSelectedModelRow() {
//        System.out.println("getSelectedModelRow");
//        JTable table = null;
//        int expResult = 0;
//        int result = TableUtils.getSelectedModelRow(table);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }


    /**
     * Test of showPosition method, of class TableUtils.
     */
    @Test
    public void testShowPosition() {
//        System.out.println("showPosition");
//        JTable table = null;
//        int posColumnIndex = 0;
//        int chromColumnIdx = 0;
//        BoundsInfoManager bim = null;
//        TableUtils.showPosition(table, posColumnIndex, chromColumnIdx, bim);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }


    /**
     * Test of showFeaturePosition method, of class TableUtils.
     */
    @Test
    public void testShowFeaturePosition() {
//        System.out.println("showFeaturePosition");
//        JTable table = null;
//        int featColumnIndex = 0;
//        BoundsInfoManager bim = null;
//        TableUtils.showFeaturePosition(table, featColumnIndex, bim);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }


    /**
     * Test of transformDataToTableModel method, of class TableUtils.
     */
    @Test
    public void testTransformDataToTableModel() {
        System.out.println( "transformDataToTableModel" );
        List<Object> headers = new ArrayList<>();
        headers.add( "Pos" );
        headers.add( "Name" );
        headers.add( "Car" );
        headers.add( "Date" );
        headers.add( "Has Paid" );
        List<Object> data1 = new ArrayList<>();
        data1.add( 1 );
        data1.add( "Luke" );
        data1.add( "X-Wing" );
        data1.add( new Date( 2013 + 1900, 04, 13 ) );
        data1.add( true );
        List<Object> data2 = new ArrayList<>();
        data2.add( 12435324 );
        data2.add( "Han" );
        data2.add( "Millenium Falcon" );
        data2.add( new Date( 2013 + 1900, 05, 13 ) );
        data2.add( false );
        List<Object> data3 = new ArrayList<>();
        data3.add( 1 );
        data3.add( "Ackbar" );
        data3.add( "Fregatte" );
        data3.add( new Date( 2013 + 1900, 04, 17 ) );
        data3.add( true );
        List<List<?>> dataToTransform = new ArrayList<>();
        dataToTransform.add( headers );
        dataToTransform.add( data1 );
        dataToTransform.add( data2 );
        dataToTransform.add( data3 );

        TableModel result = TableUtils.transformDataToTableModel( dataToTransform );
        assertEquals( result.getColumnName( 0 ), "Pos" );
        assertEquals( result.getColumnName( 2 ), "Car" );
        assertEquals( result.getValueAt( 0, 1 ), "Luke" );
        assertEquals( result.getValueAt( 0, 3 ), new Date( 2013 + 1900, 04, 13 ) );
        assertEquals( result.getValueAt( 0, 4 ), true );
        assertEquals( result.getValueAt( 1, 0 ), 12435324 );
        assertEquals( result.getValueAt( 1, 4 ), false );
        assertEquals( result.getValueAt( 2, 0 ), 1 );
        assertEquals( result.getValueAt( 2, 1 ), "Ackbar" );
    }


}
