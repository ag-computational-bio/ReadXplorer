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
package de.cebitec.readXplorer.parser.common;

import de.cebitec.readXplorer.parser.common.ParsedClassification;
import java.util.List;
import net.sf.samtools.SAMFileHeader;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the calculating methods of the ParsedClassification.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParsedClassificationTest {
    
    public ParsedClassificationTest() {
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

//    /**
//     * Test of getMinMismatches method, of class ParsedClassification.
//     */
//    @Test
//    public void testGetMinMismatches() {
//        System.out.println("getMinMismatches");
//        ParsedClassification instance = null;
//        int expResult = 0;
//        int result = instance.getMinMismatches();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of updateMinMismatches method, of class ParsedClassification.
//     */
//    @Test
//    public void testUpdateMinMismatches() {
//        System.out.println("updateMinMismatches");
//        int mismatches = 0;
//        ParsedClassification instance = null;
//        instance.updateMinMismatches(mismatches);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of getReadStarts method, of class ParsedClassification.
//     */
//    @Test
//    public void testGetReadStarts() {
//        System.out.println("getReadStarts");
//        ParsedClassification instance = null;
//        List expResult = null;
//        List result = instance.getReadStarts();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getNextMappingStart method, of class ParsedClassification.
     */
    @Test
    public void testGetNextMappingStart() {
        System.out.println("getNextMappingStart");
        int start1 = 717;
        int start2 = 818;
        int start3 = 850;
        int start4 = 950;
        int start5 = 951;
        ParsedClassification classificationSorted = new ParsedClassification(SAMFileHeader.SortOrder.coordinate);
        ParsedClassification classificationUnsorted = new ParsedClassification(SAMFileHeader.SortOrder.unsorted);
        
        assertTrue(classificationSorted.getNextMappingStart(start1) == 0);
        assertTrue(classificationUnsorted.getNextMappingStart(start1) == 0);
        
        classificationSorted.addReadStart(start1);
        classificationUnsorted.addReadStart(start5);
        
        assertTrue(classificationSorted.getNextMappingStart(start1) == 0);
        assertTrue(classificationUnsorted.getNextMappingStart(start1) == 0);
        
        classificationSorted.addReadStart(start2);
        classificationSorted.addReadStart(start3);
        classificationSorted.addReadStart(start4);
        classificationSorted.addReadStart(start5);
        
        classificationUnsorted.addReadStart(start4);
        classificationUnsorted.addReadStart(start3);
        classificationUnsorted.addReadStart(start2);
        classificationUnsorted.addReadStart(start1);
        
        assertTrue(classificationSorted.getNextMappingStart(start1) == start2);
        assertTrue(classificationUnsorted.getNextMappingStart(start1) == start2);
        assertTrue(classificationSorted.getNextMappingStart(start2) == start3);
        assertTrue(classificationUnsorted.getNextMappingStart(start2) == start3);
        assertTrue(classificationSorted.getNextMappingStart(start3) == start4);
        assertTrue(classificationUnsorted.getNextMappingStart(start3) == start4);
        assertTrue(classificationSorted.getNextMappingStart(start4) == start5);
        assertTrue(classificationUnsorted.getNextMappingStart(start4) == start5);
        assertTrue(classificationSorted.getNextMappingStart(start5) == start1);
        assertTrue(classificationUnsorted.getNextMappingStart(start5) == start1);
    }

//    /**
//     * Test of addReadStart method, of class ParsedClassification.
//     */
//    @Test
//    public void testAddReadStart() {
//        System.out.println("addReadStart");
//        int mappingStart = 0;
//        ParsedClassification instance = null;
//        instance.addReadStart(mappingStart);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of getNumberOccurrences method, of class ParsedClassification.
//     */
//    @Test
//    public void testGetNumberOccurrences() {
//        System.out.println("getNumberOccurrences");
//        ParsedClassification instance = null;
//        int expResult = 0;
//        int result = instance.getNumberOccurrences();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
