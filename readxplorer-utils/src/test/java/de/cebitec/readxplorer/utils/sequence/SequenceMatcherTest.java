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


import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import de.cebitec.readxplorer.api.enums.RegionType;
import de.cebitec.readxplorer.utils.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SequenceMatcherTest {

    private static Pattern[] startCodonPatterns;
    private static Pattern[] stopCodonPatterns;


    public SequenceMatcherTest() {

    }


    @BeforeClass
    public static void setUpClass() {
        GeneticCode geneticCode = GeneticCodeFactory.getDefault().getGeneticCodeById( 1 );
        List<String> startCodons = geneticCode.getStartCodons();
        List<String> stopCodons = geneticCode.getStopCodons();
        startCodonPatterns = new Pattern[6];
        stopCodonPatterns = new Pattern[6];
        int index = 0;
        for( String codon : startCodons ) {
            startCodonPatterns[index++] = Pattern.compile( codon );
            startCodonPatterns[index++] = Pattern.compile( SequenceUtils.getReverseComplement( codon ) );
        }
        index = 0;
        for( String codon : stopCodons ) {
            stopCodonPatterns[index++] = Pattern.compile( codon );
            stopCodonPatterns[index++] = Pattern.compile( SequenceUtils.getReverseComplement( codon ) );
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
     * Test of matchPattern method, of class SequenceMatcher.
     */
    @Test
    public void testMatchPattern() {
        System.out.println( "matchPattern" );
        String sequence = "GTGAGCCAGAACTCATCTTCTTTGCTCGAAACCTGGCGCCAAGTTGTTGCCGATCTCACAACTTTGAGCCAGCAAGCGGACAGTGGATTCGACCCATTGA";

        //////// Test search all codons in area ///////////////////////////////
        ArrayList<Region> result;
        RegionType type = RegionType.Start;
        SequenceMatcher startMatcher = new SequenceMatcher();

        for( int i = 0; i < startCodonPatterns.length; i++ ) {
            startMatcher.matchPattern( sequence, startCodonPatterns[i++], true, type );
            startMatcher.matchPattern( sequence, startCodonPatterns[i], false, type );
        }
        result = (ArrayList<Region>) startMatcher.getRegions();

        List<Region> expStarts = new ArrayList<>();
        expStarts.add( new Region( 22, 24, true, type ) );
        expStarts.add( new Region( 33, 35, true, type ) );
        expStarts.add( new Region( 44, 46, true, type ) );
        expStarts.add( new Region( 47, 49, true, type ) );
        expStarts.add( new Region( 64, 66, true, type ) );
        expStarts.add( new Region( 97, 99, true, type ) );
        expStarts.add( new Region( 7, 9, false, type ) );
        expStarts.add( new Region( 14, 16, false, type ) );
        expStarts.add( new Region( 40, 42, false, type ) );
        expStarts.add( new Region( 59, 61, false, type ) );
        expStarts.add( new Region( 70, 72, false, type ) );
        expStarts.add( new Region( 73, 75, false, type ) );
        expStarts.add( new Region( 81, 83, false, type ) );
        expStarts.add( new Region( 95, 97, false, type ) );

        for( Region startCodon : expStarts ) {
            assertTrue( result.contains( startCodon ) );
        }
        assertTrue( result.size() == 14 );

        //////// Test stop codons with absolute position shifted by 9bp ///////
        type = RegionType.Stop;
        SequenceMatcher stopMatcher = new SequenceMatcher();
        stopMatcher.setAbsoluteStart( 10 );
        for( int i = 0; i < stopCodonPatterns.length; i++ ) {
            stopMatcher.matchPattern( sequence, stopCodonPatterns[i++], true, type );
            stopMatcher.matchPattern( sequence, stopCodonPatterns[i], false, type );
        }
        result = (ArrayList<Region>) stopMatcher.getRegions();

        List<Region> expStops = new ArrayList<>();
        expStops.add( new Region( 11, 13, true, type ) );
        expStops.add( new Region( 74, 76, true, type ) );
        expStops.add( new Region( 107, 109, true, type ) );
        expStops.add( new Region( 22, 24, false, type ) );
        expStops.add( new Region( 65, 67, false, type ) );

        for( Region stopCodon : expStops ) {
            assertTrue( result.contains( stopCodon ) );
        }
        assertTrue( result.size() == 5 );


        //////// Test start codons with max result limit = 1 per codon ////////
        startMatcher.clearRegions();
        startMatcher.setMaxNoResults( 1 );

        for( int i = 0; i < startCodonPatterns.length; i++ ) {
            startMatcher.matchPattern( sequence, startCodonPatterns[i++], true, type );
            startMatcher.matchPattern( sequence, startCodonPatterns[i], false, type );
        }
        result = (ArrayList<Region>) startMatcher.getRegions();

        List<Region> expLimitedStarts = new ArrayList<>();
        expLimitedStarts.add( new Region( 22, 24, true, type ) );
        expLimitedStarts.add( new Region( 33, 35, true, type ) );
        expLimitedStarts.add( new Region( 7, 9, false, type ) );
        expLimitedStarts.add( new Region( 14, 16, false, type ) );
        expLimitedStarts.add( new Region( 40, 42, false, type ) );

        for( Region startCodon : expLimitedStarts ) {
            assertTrue( result.contains( startCodon ) );
        }
        assertTrue( result.size() == 5 );


        //////// Test start codons in reading frame -2 ////////////////////////
        startMatcher.clearRegions();
        startMatcher.setMaxNoResults( 0 );
        startMatcher.setRequireSameFrame( true );
        startMatcher.setTargetFrame( -2 );

        for( int i = 0; i < startCodonPatterns.length; i++ ) {
            startMatcher.matchPattern( sequence, startCodonPatterns[i++], true, type );
            startMatcher.matchPattern( sequence, startCodonPatterns[i], false, type );
        }
        result = (ArrayList<Region>) startMatcher.getRegions();

        List<Region> expFrameStarts = new ArrayList<>();
        expFrameStarts.add( new Region( 81, 83, false, type ) );

        for( Region startCodon : expFrameStarts ) {
            assertTrue( result.contains( startCodon ) );
        }
        assertTrue( result.size() == 1 );


        //////// Test start codons in reading frame -1 ////////////////////////
        startMatcher.clearRegions();
        startMatcher.setTargetFrame( -1 );

        for( int i = 0; i < startCodonPatterns.length; i++ ) {
            startMatcher.matchPattern( sequence, startCodonPatterns[i++], true, type );
            startMatcher.matchPattern( sequence, startCodonPatterns[i], false, type );
        }
        result = (ArrayList<Region>) startMatcher.getRegions();

        List<Region> expFrame3Starts = new ArrayList<>();
        expStarts.add( new Region( 7, 9, false, type ) );
        expStarts.add( new Region( 40, 42, false, type ) );
        expStarts.add( new Region( 70, 72, false, type ) );
        expStarts.add( new Region( 73, 75, false, type ) );

        for( Region startCodon : expFrame3Starts ) {
            assertTrue( result.contains( startCodon ) );
        }
        assertTrue( result.size() == 4 );

    }


}
