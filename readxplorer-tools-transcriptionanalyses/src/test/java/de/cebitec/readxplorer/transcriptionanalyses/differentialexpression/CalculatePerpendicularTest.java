/*
 * Copyright (C) 2015 Kai
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.lang.Double.NaN;
import static org.junit.Assert.assertArrayEquals;


/**
 *
 * @author Kai
 */
public class CalculatePerpendicularTest {

    public CalculatePerpendicularTest() {
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
     * Test of calculate method, of class CalculatePerpendicular.
     */
    @Test
    public void testCalculate1() {
        System.out.println( "calculate" );
        int[] conditionA = new int[21];
        int[] conditionB = new int[21];

        for( int i = 0; i < 10; i++ ) {
            conditionA[i] = i;
        }
        for( int i = 10; i < conditionA.length; i++ ) {
            conditionA[i] = 10;
        }


        for( int i = 0; i < 11; i++ ) {
            conditionB[i] = 10;
        }
        int j = 9;
        for( int i = 11; i < conditionA.length; i++ ) {

            conditionB[i] = j--;
        }


        CalculatePerpendicular instance = new CalculatePerpendicular();
        double[] result = instance.calculate( conditionA, conditionB );
        //We only want to test slope here, so we copy the r^2 result.
        double[] expResult = { result[0], -1d, result[2], result[3], result[4] };

        assertArrayEquals( "Wrong result for CalculatePerpendicular", expResult, result, 0 );
    }


    /**
     * Test of calculate method, of class CalculatePerpendicular.
     */
    @Test
    public void testCalculate2() {
        System.out.println( "calculate" );
        int[] conditionA = new int[21];
        int[] conditionB = new int[21];

        for( int i = 0; i < 10; i++ ) {
            conditionA[i] = i;
        }
        for( int i = 10; i < conditionA.length; i++ ) {
            conditionA[i] = 10;
        }


        for( int i = 0; i < 11; i++ ) {
            conditionB[i] = 10;
        }
        int j = 9;
        for( int i = 11; i < conditionA.length; i++ ) {

            conditionB[i] = j--;
        }


        CalculatePerpendicular instance = new CalculatePerpendicular();
        double[] result = instance.calculate( conditionB, conditionA );
        //We only want to test slope here, so we copy the r^2 result.
        double[] expResult = { result[0], -1d, result[2], result[3], result[4] };

        assertArrayEquals( "Wrong result for CalculatePerpendicular", expResult, result, 0 );
    }


    /**
     * Test of calculate method, of class CalculatePerpendicular.
     */
    @Test
    public void testCalculate3() {
        System.out.println( "calculate" );
        int[] conditionA = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] conditionB = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };


        CalculatePerpendicular instance = new CalculatePerpendicular();
        double[] result = instance.calculate( conditionB, conditionA );
        //We only want to test slope and intercept here, so we copy the r^2 result.
        double[] expResult = { 11d, -1d, result[2], result[3], result[4] };

        assertArrayEquals( "Wrong result for CalculatePerpendicular", expResult, result, 0 );
    }
    
    /**
     * Test of calculate method, of class CalculatePerpendicular.
     */
    @Test
    public void testCalculate4() {
        System.out.println( "calculate" );
        int[] conditionA = { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 };
        int[] conditionB = { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 };


        CalculatePerpendicular instance = new CalculatePerpendicular();
        double[] result = instance.calculate( conditionB, conditionA );
        //All points are equal, hence no line can be drawn.
        double[] expResult = { NaN, NaN, NaN, NaN, NaN };

        assertArrayEquals( "Wrong result for CalculatePerpendicular", expResult, result, 0 );
    }

    /**
     * Test of calculate method, of class CalculatePerpendicular.
     */
    @Test
    public void testCalculate5() {
        System.out.println( "calculate" );
        int[] conditionA = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] conditionB = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };


        CalculatePerpendicular instance = new CalculatePerpendicular();
        double[] result = instance.calculate( conditionB, conditionA );
        //Perfect linear correlation
        double[] expResult = { 0d, 1d, 1d, 1d, 1d };

        assertArrayEquals( "Wrong result for CalculatePerpendicular", expResult, result, 0 );
    }

    /**
     * Test of computePearsonsCorrelationCoefficient method, of class
     * CalculatePerpendicular.
     */
//    @Test
//    public void testComputePearsonsCorrelationCoefficient_intArr_intArr() {
//        System.out.println( "computePearsonsCorrelationCoefficient" );
//        int[] x = null;
//        int[] y = null;
//        double expResult = 0.0;
//        double result = CalculatePerpendicular.computePearsonsCorrelationCoefficient( x, y );
//        assertEquals( expResult, result, 0.0 );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
    /**
     * Test of computePearsonsCorrelationCoefficient method, of class
     * CalculatePerpendicular.
     */
//    @Test
//    public void testComputePearsonsCorrelationCoefficient_4args() {
//        System.out.println( "computePearsonsCorrelationCoefficient" );
//        int[] x = null;
//        int[] y = null;
//        double meanx = 0.0;
//        double meany = 0.0;
//        double expResult = 0.0;
//        double result = CalculatePerpendicular.computePearsonsCorrelationCoefficient( x, y, meanx, meany );
//        assertEquals( expResult, result, 0.0 );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
    /**
     * Test of computeMean method, of class CalculatePerpendicular.
     */
//    @Test
//    public void testComputeMean() {
//        System.out.println( "computeMean" );
//        int[] values = null;
//        double expResult = 0.0;
//        double result = CalculatePerpendicular.computeMean( values );
//        assertEquals( expResult, result, 0.0 );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
    /**
     * Test of computeSquaredSum method, of class CalculatePerpendicular.
     */
//    @Test
//    public void testComputeSquaredSum() {
//        System.out.println( "computeSquaredSum" );
//        int[] values = null;
//        double expResult = 0.0;
//        double result = CalculatePerpendicular.computeSquaredSum( values );
//        assertEquals( expResult, result, 0.0 );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
    /**
     * Test of computeSumOfProducts method, of class CalculatePerpendicular.
     */
//    @Test
//    public void testComputeSumOfProducts() {
//        System.out.println( "computeSumOfProducts" );
//        int[] values1 = null;
//        int[] values2 = null;
//        double expResult = 0.0;
//        double result = CalculatePerpendicular.computeSumOfProducts( values1, values2 );
//        assertEquals( expResult, result, 0.0 );
//         TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
}
