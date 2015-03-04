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

package de.cebitec.readxplorer.utils.sequence;


import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.PositionUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class PositionUtilsTest {

    public PositionUtilsTest() {
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
     * Test of convertPosition method, of class PositionUtils.
     */
    @Test
    public void testConvertPosition() {
        System.out.println( "convertPosition" );

        String posString = "102456"; //ordinary number
        int expResult = 102456;
        int result = PositionUtils.convertPosition( posString );
        assertEquals( expResult, result );

        String posString2 = "-34654"; //negative number
        int expResult2 = -34654;
        int result2 = PositionUtils.convertPosition( posString2 );
        assertEquals( expResult2, result2 );

        String posString3 = "102456_3"; //number containing '_'
        int expResult3 = 102456;
        int result3 = PositionUtils.convertPosition( posString3 );
        assertEquals( expResult3, result3 );
    }


    /**
     * Test of determineFrame method, of class PositionUtils.
     */
    @Test
    public void testDetermineFrame() {
        System.out.println( "determineFrame" );
        List<GenomicRange> ranges = new ArrayList<>();
        List<Integer> expResults = new ArrayList<>();
        List<Integer> results = new ArrayList<>();

        ranges.add( new GenomicRangeImpl( 1, 6, true ) ); //frame 1
        expResults.add( 1 );
        results.add( PositionUtils.determineFrame( ranges.get( ranges.size() - 1 ) ) );

        ranges.add( new GenomicRangeImpl( 2, 6, true ) ); //frame 2
        expResults.add( 2 );
        results.add( PositionUtils.determineFrame( ranges.get( ranges.size() - 1 ) ) );

        ranges.add( new GenomicRangeImpl( 3, 6, true ) ); //frame 3
        expResults.add( 3 );
        results.add( PositionUtils.determineFrame( ranges.get( ranges.size() - 1 ) ) );

        ranges.add( new GenomicRangeImpl( 1, 6, false ) ); //frame -1
        expResults.add( -1 );
        results.add( PositionUtils.determineFrame( ranges.get( ranges.size() - 1 ) ) );

        ranges.add( new GenomicRangeImpl( 1, 5, false ) ); //frame -2
        expResults.add( -2 );
        results.add( PositionUtils.determineFrame( ranges.get( ranges.size() - 1 ) ) );

        ranges.add( new GenomicRangeImpl( 1, 4, false ) ); //frame -3
        expResults.add( -3 );
        results.add( PositionUtils.determineFrame( ranges.get( ranges.size() - 1 ) ) );

        for( int i = 0; i < results.size(); i++ ) {
            assertEquals( expResults.get( i ), results.get( i ) );
        }
    }


    /**
     * Test of determineFwdFrame method, of class PositionUtils.
     */
    @Test
    public void testDetermineFwdFrame() {
        System.out.println( "determineFwdFrame" );
        List<Integer> positions = new ArrayList<>();
        List<Integer> expResults = new ArrayList<>();
        List<Integer> results = new ArrayList<>();

        positions.add( 1 );
        expResults.add( 1 );
        results.add( PositionUtils.determineFwdFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 2 );
        expResults.add( 2 );
        results.add( PositionUtils.determineFwdFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 3 );
        expResults.add( 3 );
        results.add( PositionUtils.determineFwdFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 4 );
        expResults.add( 1 );
        results.add( PositionUtils.determineFwdFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 5 );
        expResults.add( 2 );
        results.add( PositionUtils.determineFwdFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 6 );
        expResults.add( 3 );
        results.add( PositionUtils.determineFwdFrame( positions.get( positions.size() - 1 ) ) );

        for( int i = 0; i < results.size(); i++ ) {
            assertEquals( expResults.get( i ), results.get( i ) );
        }
    }


    /**
     * Test of determineRevFrame method, of class PositionUtils.
     */
    @Test
    public void testDetermineRevFrame() {
        System.out.println( "determineRevFrame" );
        List<Integer> positions = new ArrayList<>();
        List<Integer> expResults = new ArrayList<>();
        List<Integer> results = new ArrayList<>();

        positions.add( 1 );
        expResults.add( -3 );
        results.add( PositionUtils.determineRevFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 2 );
        expResults.add( -2 );
        results.add( PositionUtils.determineRevFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 3 );
        expResults.add( -1 );
        results.add( PositionUtils.determineRevFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 4 );
        expResults.add( -3 );
        results.add( PositionUtils.determineRevFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 5 );
        expResults.add( -2 );
        results.add( PositionUtils.determineRevFrame( positions.get( positions.size() - 1 ) ) );

        positions.add( 6 );
        expResults.add( -1 );
        results.add( PositionUtils.determineRevFrame( positions.get( positions.size() - 1 ) ) );

        for( int i = 0; i < results.size(); i++ ) {
            assertEquals( expResults.get( i ), results.get( i ) );
        }
    }


    /**
     * Test of updateIntervals method, of class PositionUtils.
     */
    @Test
    public void testUpdateIntervals() {
        System.out.println( "updateIntervals" );
        List<Pair<Integer, Integer>> intervals = null;
        int start = 0;
        int stop = 0;
//        PositionUtils.updateIntervals( intervals, start, stop );
        // TODO write test for this method
    }


    private class GenomicRangeImpl implements GenomicRange {

        private int start;
        private int stop;
        private boolean isFwdStrand;


        GenomicRangeImpl( int start, int stop, boolean isFwdStrand ) {
            this.start = start;
            this.stop = stop;
            this.isFwdStrand = isFwdStrand;
        }


        @Override
        public int compareTo( GenomicRange o ) {
            return 0;
        }


        @Override
        public int getStart() {
            return start;
        }


        public void setStart( int start ) {
            this.start = start;
        }


        @Override
        public int getStop() {
            return stop;
        }


        public void setStop( int stop ) {
            this.stop = stop;
        }


        @Override
        public boolean isFwdStrand() {
            return isFwdStrand;
        }


        public void setIsFwdStrand( boolean isFwdStrand ) {
            this.isFwdStrand = isFwdStrand;
        }


    }

}
