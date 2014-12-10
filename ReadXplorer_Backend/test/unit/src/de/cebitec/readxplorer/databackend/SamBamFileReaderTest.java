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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readxplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamFileReaderTest {

    public SamBamFileReaderTest() {
    }


    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of getMappingsFromBam method, of class SamBamFileReader.
     */
    @Test
    public void testGetMappingsFromBam() {
//        System.out.println("getMappingsFromBam");
//        PersistentReference refGenome = new PersistentReference(1, "PAO1", "PAO1", null, new File(""));
//        PersistentChromosome chrom = new PersistentChromosome(0, 1, 1, "gi|292657124|ref|NC_013968.1|", 7000000);
//        int from = 2000;
//        int to = 3000;
//        boolean diffsAndGapsNeeded = true;
//        byte trackNeeded = 0;
//        ParametersReadClasses parametersReadClasses = new ParametersReadClasses(true, true, false, false, new Byte("30"));
//        IntervalRequest request = new IntervalRequest(from, to, from, to, 1, null, diffsAndGapsNeeded, Properties.NORMAL, trackNeeded, parametersReadClasses);
//        SamBamFileReader samBamFileReader = new SamBamFileReader(new File(), 1, refGenome);

    }


    /**
     * Test of getCoverageFromBam method, of class SamBamFileReader.
     */
    @Test
    public void testGetCoverageFromBam() {
        System.out.println( "getCoverageFromBam" );
        PersistentReference refGenome = new PersistentReference( 1, "PAO1", "PAO1", null, new File( "" ) );
        int from = 10000;
        int to = 21100;
        boolean diffsAndGapsNeeded = false;
        byte trackNeeded = 0;
        IntervalRequest request = new IntervalRequest( from, to, from, to, 1, null, diffsAndGapsNeeded, Properties.NORMAL, trackNeeded, new ParametersReadClasses() );
        SamBamFileReader samBamFileReader = new SamBamFileReader( new File( "D:\\Programmieren & Studieren\\Pseudomonas aeruginosa Projekt\\SequenceData\\NG-5516_2_2_read_1-F469-with-PAO1.jok_sort.bam" ), 1, refGenome );
        CoverageManager coverage = samBamFileReader.getCoverageFromBam( request ).getCovManager();
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getFwdCov( 10000 ) == 129 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getFwdCov( 15000 ) == 178 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getFwdCov( 15001 ) == 179 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getFwdCov( 15002 ) == 178 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getFwdCov( 15003 ) == 178 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getFwdCov( 21100 ) == 208 );

        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getRevCov( 10000 ) == 152 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getRevCov( 15000 ) == 65 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getRevCov( 15001 ) == 65 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getRevCov( 15002 ) == 63 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getRevCov( 15003 ) == 59 );
        assertTrue( coverage.getCoverage( MappingClass.PERFECT_MATCH ).getRevCov( 21100 ) == 168 );
    }


}
