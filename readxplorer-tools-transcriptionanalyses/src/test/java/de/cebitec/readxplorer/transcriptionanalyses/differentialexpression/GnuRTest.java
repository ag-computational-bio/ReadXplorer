/*
 * Copyright (C) 2016 pblumenk
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

import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.PackageNotLoadableException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import static org.junit.Assert.*;


/**
 *
 * @author pblumenk
 */
public class GnuRTest {

    private static GnuR instance;
    private static ProcessingLog processingLog;


    public GnuRTest() {
    }


    @BeforeClass
    public static void setUpClass() {
        try {
            Constructor<GnuR> constructor;
            constructor = GnuR.class.getDeclaredConstructor( String.class, int.class, boolean.class, ProcessingLog.class );
            constructor.setAccessible( true );
            instance = constructor.newInstance( "localhost", 6311, false, new ProcessingLog() );
        } catch( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex ) {
            Exceptions.printStackTrace( ex );
        }
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
     * Test of clearGnuR method, of class GnuR.
     *
     * @throws org.rosuda.REngine.Rserve.RserveException
     * @throws org.rosuda.REngine.REXPMismatchException
     */
    @Test
    public void testClearGnuR() throws RserveException, REXPMismatchException {
        instance.clearGnuR();
        REXP list = instance.eval( "ls()" );
        assertEquals( "R environment should be empty", 0, list.asStrings().length );
        instance.eval( "a <- 10" );
        list = instance.eval( "ls()" );
        assertEquals( "First variable should be \"a\"", "a", list.asString() );
        assertEquals( "Only one variable should exist in the R environment", 1, list.asStrings().length );
        instance.clearGnuR();
        list = instance.eval( "ls()" );
        assertEquals( "R environment should be empty", 0, list.asStrings().length );
    }


    /**
     * Test of loadPackage method, of class GnuR, with existing basic package "tool".
     */
    @Test
    public void testLoadExististingPackage() throws PackageNotLoadableException, RserveException, REXPMismatchException {
        REXP packagesBefore = instance.eval( "search()" );
        instance.loadPackage( "tools" );
        REXP packagesAfter = instance.eval( "search()" );
        assertEquals( "Package list should increase by one", packagesBefore.asStrings().length + 1, packagesAfter.asStrings().length );
        instance.eval( "detach(package:tools)" );
        REXP packagesAfterDetached = instance.eval( "search()" );
        assertEquals( "Package should be detached again", packagesBefore.asStrings().length, packagesAfterDetached.asStrings().length );
    }


    /**
     * Test of loadPackage method, of class GnuR, with non-existing package.
     */
    @Test
    public void testLoadNonExististingPackage() throws RserveException, REXPMismatchException {
        boolean exceptionThrown = false;
        REXP packagesBefore = instance.eval( "search()" );
        try {
            instance.loadPackage( "LoadUnknownPackage" );
        } catch( PackageNotLoadableException ex ) {
            exceptionThrown = true;
        }
        REXP packagesAfter = instance.eval( "search()" );
        assertEquals( "Package list shouldn't increase", packagesBefore.asStrings().length, packagesAfter.asStrings().length );
        assertTrue( exceptionThrown );
    }

//
//    /**
//     * Test of saveDataToFile method, of class GnuR.
//     */
//    @Test
//    public void testSaveDataToFile() throws Exception {
//        System.out.println( "saveDataToFile" );
//        File saveFile = null;
//        GnuR instance = null;
//        instance.saveDataToFile( saveFile );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//
//
//    /**
//     * Test of shutdown method, of class GnuR.
//     */
//    @Test
//    public void testShutdown() throws Exception {
//        System.out.println( "shutdown" );
//        GnuR instance = null;
//        instance.shutdown();
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of eval method, of class GnuR.
//     */
//    @Test
//    public void testEval_String() throws Exception {
//        System.out.println( "eval" );
//        String cmd = "";
//        GnuR instance = null;
//        REXP expResult = null;
//        REXP result = instance.eval( cmd );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of eval method, of class GnuR.
//     */
//    @Test
//    public void testEval_3args() throws Exception {
//        System.out.println( "eval" );
//        REXP what = null;
//        REXP where = null;
//        boolean resolve = false;
//        GnuR instance = null;
//        REXP expResult = null;
//        REXP result = instance.eval( what, where, resolve );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of assign method, of class GnuR.
//     */
//    @Test
//    public void testAssign_String_REXP() throws Exception {
//        System.out.println( "assign" );
//        String sym = "";
//        REXP rexp = null;
//        GnuR instance = null;
//        instance.assign( sym, rexp );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of assign method, of class GnuR.
//     */
//    @Test
//    public void testAssign_String_String() throws Exception {
//        System.out.println( "assign" );
//        String sym = "";
//        String ct = "";
//        GnuR instance = null;
//        instance.assign( sym, ct );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of assign method, of class GnuR.
//     */
//    @Test
//    public void testAssign_3args() throws Exception {
//        System.out.println( "assign" );
//        String symbol = "";
//        REXP value = null;
//        REXP env = null;
//        GnuR instance = null;
//        instance.assign( symbol, value, env );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of storePlot method, of class GnuR.
//     */
//    @Test
//    public void testStorePlot() throws Exception {
//        System.out.println( "storePlot" );
//        File file = null;
//        String plotIdentifier = "";
//        GnuR instance = null;
//        instance.storePlot( file, plotIdentifier );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of startRServe method, of class GnuR.
//     */
//    @Test
//    public void testStartRServe() throws Exception {
//        System.out.println( "startRServe" );
//        ProcessingLog processingLog = null;
//        GnuR expResult = null;
//        GnuR result = GnuR.startRServe( processingLog );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//
//
//    /**
//     * Test of gnuRSetupCorrect method, of class GnuR.
//     */
//    @Test
//    public void testGnuRSetupCorrect() {
//        System.out.println( "gnuRSetupCorrect" );
//        boolean expResult = false;
//        boolean result = GnuR.gnuRSetupCorrect();
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }

}
