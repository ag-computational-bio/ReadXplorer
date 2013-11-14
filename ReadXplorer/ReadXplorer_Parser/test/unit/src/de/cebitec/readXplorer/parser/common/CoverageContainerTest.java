package de.cebitec.readXplorer.parser.common;

import de.cebitec.readXplorer.parser.common.ParsedReferenceGap;
import de.cebitec.readXplorer.parser.common.CoverageContainer;
import de.cebitec.readXplorer.parser.common.ParsedDiff;
import de.cebitec.readXplorer.parser.common.ParsedMapping;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
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
        List<ParsedDiff> diffs1 = new ArrayList<ParsedDiff>();
        List<ParsedDiff> diffs2 = new ArrayList<ParsedDiff>();
        List<ParsedDiff> diffs3 = new ArrayList<ParsedDiff>();
        List<ParsedDiff> diffs4 = new ArrayList<ParsedDiff>();
        List<ParsedDiff> diffs5 = new ArrayList<ParsedDiff>();
        
        long diff1 = 3;
        long diff2 = 7;
        long diff3 = 22;
        long diff4 = 30;
        long diff5 = 9;
        long diff6 = 10;
        diffs1.add(new ParsedDiff(diff1, 'a'));
        diffs1.add(new ParsedDiff(diff6, 'a'));
        diffs2.add(new ParsedDiff(diff2, 'a'));
        diffs3.add(new ParsedDiff(diff3, 'a'));
        diffs4.add(new ParsedDiff(diff4, 'a'));
        diffs5.add(new ParsedDiff(diff5, 'a'));
        
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
