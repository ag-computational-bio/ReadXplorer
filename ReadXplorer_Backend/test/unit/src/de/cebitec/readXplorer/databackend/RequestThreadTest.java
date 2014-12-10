/*
 * Copyright (C) 2014 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readXplorer.databackend;


import de.cebitec.readxplorer.databackend.RequestThread;
import de.cebitec.readxplorer.databackend.IntervalRequest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class RequestThreadTest {

    public RequestThreadTest() {
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
     * Test of readClassParamsFulfilled method, of class RequestThread.
     */
    @Test
    public void testReadClassParamsFulfilled() {
        System.out.println( "readClassParamsFulfilled" );
        IntervalRequest request = null;
        IntervalRequest lastRequest = null; //TODO: finish test
        IntervalRequest request2 = null;
        IntervalRequest lastRequest2 = null;
        RequestThread instance = new RequestThreadImpl();
        boolean expResult1 = true;
        boolean expResult2 = false;
        boolean result = instance.readClassParamsFulfilled( request );
        boolean result2 = instance.readClassParamsFulfilled( request2 );
        assertEquals( expResult1, result );
        fail( "The test case is a prototype." );
    }


    /**
     * Test of doesNotMatchLatestRequestBounds method, of class RequestThread.
     */
    @Test
    public void testDoesNotMatchLatestRequestBounds() {
//        System.out.println("doesNotMatchLatestRequestBounds");
//        IntervalRequest request = null;
//        RequestThread instance = new RequestThreadImpl();
//        boolean expResult = false;
//        boolean result = instance.doesNotMatchLatestRequestBounds(request);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }


    /**
     * Test of calcCenterMiddle method, of class RequestThread.
     */
    @Test
    public void testCalcCenterMiddle() {
//        System.out.println("calcCenterMiddle");
//        IntervalRequest request = null;
//        RequestThread instance = new RequestThreadImpl();
//        int expResult = 0;
//        int result = instance.calcCenterMiddle(request);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }


    public class RequestThreadImpl extends RequestThread {

        public void addRequest( IntervalRequest request ) {
        }


    }

}
