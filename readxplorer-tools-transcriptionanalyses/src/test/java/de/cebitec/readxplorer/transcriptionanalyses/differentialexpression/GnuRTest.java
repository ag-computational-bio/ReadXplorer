/*
 * Copyright (C) 2016 Patrick Blumenkamp <patrick.blumenkamp at computational.bio.uni-giessen.de>
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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;


/**
 *
 * @author Patrick Blumenkamp
 * <patrick.blumenkamp at computational.bio.uni-giessen.de>
 */
public class GnuRTest {

    private static GnuR instance;
    private static boolean rserveStarted = true;


    /**
     * Set up connection to Rserve.
     */
    @BeforeClass
    public static void setUpClass() {
        try {
            Constructor<GnuR> constructor;
            constructor = GnuR.class.getDeclaredConstructor( String.class, int.class, boolean.class, ProcessingLog.class );
            constructor.setAccessible( true );
            instance = constructor.newInstance( "localhost", 6311, true, new ProcessingLog() );
        } catch( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex ) {
            Exceptions.printStackTrace( ex );
            rserveStarted = false;
        }
    }
    
    @Before
    public void checkInstance(){
        assumeTrue( rserveStarted );
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
     * Test of loadPackage method, of class GnuR, with existing basic package
     * "tool".
     *
     * @throws
     * de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR.PackageNotLoadableException
     * @throws org.rosuda.REngine.Rserve.RserveException
     * @throws org.rosuda.REngine.REXPMismatchException
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
     *
     * @throws org.rosuda.REngine.Rserve.RserveException
     * @throws org.rosuda.REngine.REXPMismatchException
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
        assertTrue( "Loading unknown package should throw a PackageNotLoadableException", exceptionThrown );

        REXP packagesAfter = instance.eval( "search()" );
        assertEquals( "Package list should not increase", packagesBefore.asStrings().length, packagesAfter.asStrings().length );
    }
    
    /**
     * Test of getPackageVersion method, of class GnuR.
     *     
     * @throws org.rosuda.REngine.REXPMismatchException
     * @throws org.rosuda.REngine.Rserve.RserveException
     */
    @Test
    public void testGetPackageVersion() throws REXPMismatchException, RserveException {
        Version knownPackage = instance.getPackageVersion( "base" );
        assertNotNull("\"Base\" should be always avialable in R", knownPackage);
        Version unknownPackage = instance.getPackageVersion( "LoadUnknownPackage" );
        assertNull("Checking unknown package \"LoadUnknownPackage\" should return null", unknownPackage);
    }

    /**
     * Test of checkPackage method, of class GnuR.
     *     
     * @throws org.rosuda.REngine.REXPMismatchException
     * @throws org.rosuda.REngine.Rserve.RserveException
     */
    @Test
    public void testCheckPackage() throws REXPMismatchException, RserveException {
        assertTrue("\"Base\" should be always avialable in R", instance.checkPackage( "base" ));
        assertFalse("Checking unknown package \"LoadUnknownPackage\" should return null", instance.checkPackage( "LoadUnknownPackage" ));
    }

    /**
     * Test of eval method, of class GnuR.
     *
     * @throws org.rosuda.REngine.REXPMismatchException
     */
    @Test
    public void testEvalString() throws REXPMismatchException {
        String cmd = "a <- 10 + 20";
        REXP result = null;
        try {
            result = instance.eval( cmd );
        } catch( RserveException ex ) {
            fail( "Rserve could not evaluate \"" + cmd + "\"" );
        }
        assertEquals( "Result should be 30", 30, result.asInteger() );

        cmd = "5 <|> 1";
        boolean exceptionThrown = false;
        try {
            result = instance.eval( cmd );
        } catch( RserveException ex ) {
            exceptionThrown = true;
        }
        assertTrue( "eval() with unknown expression should throw an RserveException", exceptionThrown );
    }


    /**
     * Test of assign method, of class GnuR.
     *
     * @throws org.rosuda.REngine.Rserve.RserveException 
     * @throws org.rosuda.REngine.REXPMismatchException 
     */
    @Test
    public void testAssignStringAndREXP() throws RserveException, REXPMismatchException {
        instance.clearGnuR();
        REXP list = instance.eval( "ls()" );
        assertEquals( "R environment should be empty", 0, list.asStrings().length );
        REXP value = instance.eval( "10" );
        instance.assign( "a", value );
        list = instance.eval( "ls()" );
        assertEquals( "R environment should only contain one variable", 1, list.asStrings().length );
        REXP valueOfA = instance.eval( "a" );
        assertEquals( "a should contain the value 10", value.asInteger(), valueOfA.asInteger() );
        instance.clearGnuR();
    }


    /**
     * Test of assign method, of class GnuR.
     *
     * @throws org.rosuda.REngine.Rserve.RserveException
     * @throws org.rosuda.REngine.REXPMismatchException
     */
    @Test
    public void testAssignStringAndString() throws RserveException, REXPMismatchException {
        instance.clearGnuR();
        REXP list = instance.eval( "ls()" );
        assertEquals( "R environment should be empty", 0, list.asStrings().length );
        String value = "10";
        instance.assign( "a", value );
        list = instance.eval( "ls()" );
        assertEquals( "R environment should only contain one variable", 1, list.asStrings().length );
        REXP valueOfA = instance.eval( "a" );
        assertEquals( "a should contain the string \"10\"", value, valueOfA.asString() );
        assertTrue( "Value must be a string and no numerical", !valueOfA.isNumeric() && valueOfA.isString() );
        instance.clearGnuR();
    }


    /**
     * Test of assign method, of class GnuR.
     *
     * @throws org.rosuda.REngine.REngineException
     * @throws org.rosuda.REngine.REXPMismatchException
     */
    @Test
    public void testAssign3args() throws REngineException, REXPMismatchException {
        instance.clearGnuR();
        REXP list = instance.eval( "ls()" );
        assertEquals( "R environment should be empty", 0, list.asStrings().length );
        REXP value = instance.eval( "10" );
        instance.assign( "a", value, null );
        list = instance.eval( "ls()" );
        assertEquals( "R environment should only contain one variable", 1, list.asStrings().length );
        REXP valueOfA = instance.eval( "a" );
        assertEquals( "a should contain the value 10", value.asInteger(), valueOfA.asInteger() );
        instance.clearGnuR();
        boolean exceptionThrown = false;
        try {
            REXP newEnvironment = instance.newEnvironment( null, false );
        } catch( REngineException ex ) {
            exceptionThrown = true;
        }
        assertTrue( "RConnection does not support environments at the moment", exceptionThrown );
    }


    /**
     * Test of storePlot method, of class GnuR.
     *
     * @throws java.io.IOException
     * @throws
     * de.cebitec.readxplorer.transcriptionanalyses.differentialexpression\
     * .GnuR.PackageNotLoadableException
     * @throws org.rosuda.REngine.REngineException
     * @throws org.rosuda.REngine.REXPMismatchException
     * @throws org.rosuda.REngine.Rserve.RserveException
     */
    @Test
    public void testStorePlot() throws IOException, PackageNotLoadableException, IllegalStateException, REngineException, RserveException, REXPMismatchException {
        File tmpPlot = File.createTempFile( "GnuR-Test-StorePlot", ".svg" );
        long filesizeBefore = tmpPlot.length();
        instance.storePlot( tmpPlot, "plot(c(1,2,3,4))" );
        long filesizeAfter = tmpPlot.length();
        assertTrue( "File must be larger after writing plot into it", filesizeAfter > filesizeBefore );
        Files.delete( tmpPlot.toPath() );
    }


    /**
     * Test of saveDataToFile method, of class GnuR.
     *
     * @throws org.rosuda.REngine.Rserve.RserveException
     * @throws java.io.IOException
     * @throws org.rosuda.REngine.REXPMismatchException
     */
    @Test
    public void testSaveDataToFile() throws RserveException, IOException, REXPMismatchException {
        File tmpSave = File.createTempFile( "GnuR-Test-SaveDataToFile", ".RData" );
        long filesizeBefore = tmpSave.length();
        instance.assign( "a", "abc" );
        instance.assign( "b", "def" );
        instance.assign( "c", "ghi" );
        instance.assign( "d", "jkl" );
        instance.saveDataToFile( tmpSave );
        long filesizeAfter = tmpSave.length();
        assertTrue( "File must be larger after writing image into it", filesizeAfter > filesizeBefore );
        Files.delete( tmpSave.toPath() );
        instance.clearGnuR();
    }
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
//
//
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
