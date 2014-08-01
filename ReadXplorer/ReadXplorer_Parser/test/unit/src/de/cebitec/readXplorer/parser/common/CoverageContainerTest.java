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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author tron1c
 */
public class CoverageContainerTest {
    
    public CoverageContainerTest() {
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
//     * Test of computeCoverage method, of class CoverageContainer.
//     */
//    @Test
//    public void testComputeCoverage() {
//        System.out.println("computeCoverage");
//        ParsedMappingContainer mappings = null;
//        CoverageContainer instance = new CoverageContainer();
////        instance.computeCoverage(mappings);
//        // TODO review the generated test code and remove the default call to fail.
////        fail("The test case is a prototype.");
//    }    

    /**
     * Test of clearCoverageContainerUpTo method, of class CoverageContainer.
     */
    @Test
    public void testClearCoverageContainerUpTo() {
        System.out.println("clearCoverageContainerUpTo");
        int clearPos = 9;
        CoverageContainer covContainer = new CoverageContainer();
        
        long diff1 = 3;
        long diff2 = 7;
        long diff3 = 22;
        long diff4 = 30;
        long diff5 = 9;
        long diff6 = 10;
        List<ParsedDiff> diffs1 = Arrays.asList(new ParsedDiff(diff1, 'a'), new ParsedDiff(diff6, 'a'));
        List<ParsedDiff> diffs2 = Arrays.asList(new ParsedDiff(diff2, 'a'));
        List<ParsedDiff> diffs3 = Arrays.asList(new ParsedDiff(diff3, 'a'));
        List<ParsedDiff> diffs4 = Arrays.asList(new ParsedDiff(diff4, 'a'));
        List<ParsedDiff> diffs5 = Arrays.asList(new ParsedDiff(diff5, 'a'));
        
        ParsedMapping mapping1 = new ParsedMapping(1, 10, Byte.valueOf("1"), diffs1, new ArrayList<ParsedReferenceGap>(), 2);
        ParsedMapping mapping2 = new ParsedMapping(2, 11, Byte.valueOf("-1"), diffs2, new ArrayList<ParsedReferenceGap>(), 1);
        ParsedMapping mapping3 = new ParsedMapping(20, 29, Byte.valueOf("1"), diffs3, new ArrayList<ParsedReferenceGap>(), 1);
        ParsedMapping mapping4 = new ParsedMapping(25, 36, Byte.valueOf("-1"), diffs4, new ArrayList<ParsedReferenceGap>(), 1);
        ParsedMapping mapping5 = new ParsedMapping(9, 19, Byte.valueOf("1"), diffs5, new ArrayList<ParsedReferenceGap>(), 1);   
        mapping1.setIsBestMapping(true);
        mapping2.setIsBestMapping(true);
        mapping3.setIsBestMapping(true);
        mapping4.setIsBestMapping(true);
        mapping5.setIsBestMapping(true);
        
        covContainer.addMapping(mapping1);
        covContainer.addMapping(mapping2);
        covContainer.addMapping(mapping3);
        covContainer.addMapping(mapping4);
        covContainer.addMapping(mapping5);
        covContainer.savePositions(mapping1);
        covContainer.savePositions(mapping2);
        covContainer.savePositions(mapping3);
        covContainer.savePositions(mapping4);
        covContainer.savePositions(mapping5);
        covContainer.clearCoverageContainerUpTo(clearPos);
        
        Iterator<String> it = covContainer.getPositionTable().keySet().iterator();
        String posString;
        int pos;
        while (it.hasNext()) {
            posString = it.next();
            if (posString.contains("_")) {
                pos = Integer.valueOf(posString.substring(0, posString.length() - 2));
            } else {
                pos = Integer.valueOf(posString);
            }
            if (pos < clearPos) {
                fail("Postition table: Position is smaller than the border value: " + posString);
            } else{
                System.out.println("Pos in pos table: " + posString);
            }
        }
        
        Iterator<Integer> covIt = covContainer.getCoverage().keySet().iterator();
        while (covIt.hasNext()) {
            pos = covIt.next();
            if (pos < clearPos) {
                fail("Coverage: Position is smaller than the border value: " + pos);
            } else{
                System.out.println("Pos in coverage: " + pos);
            }
        }
        
        if (covContainer.getPositionTable().size() != 4) {
            fail("Size of pos table is not correct");
        }
        
    }
}
