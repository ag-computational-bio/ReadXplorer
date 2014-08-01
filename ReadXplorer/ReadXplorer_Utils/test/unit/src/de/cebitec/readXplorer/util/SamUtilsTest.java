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
package de.cebitec.readXplorer.util;

import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class SamUtilsTest {
    
    public SamUtilsTest() {
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
     * Test of createIndex method, of class SamUtils.
     */
    @Test
    public void testCreateIndex() {
//        System.out.println("createIndex");
//        SAMFileReader reader = null;
//        File output = null;
//        SamUtils instance = new SamUtils();
//        boolean expResult = false;
//        boolean result = instance.createIndex(reader, output);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of createSamBamWriter method, of class SamUtils.
     */
    @Test
    public void testCreateSamBamWriter() {
//        System.out.println("createSamBamWriter");
//        File oldFile = null;
//        SAMFileHeader header = null;
//        boolean presorted = false;
//        String newEnding = "";
//        Pair<SAMFileWriter, File> expResult = null;
//        Pair<SAMFileWriter, File> result = SamUtils.createSamBamWriter(oldFile, header, presorted, newEnding);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileWithBamExtension method, of class SamUtils.
     */
    @Test
    public void testGetFileWithBamExtension() {
//        System.out.println("getFileWithBamExtension");
//        File inputFile = null;
//        String newEnding = "";
//        File expResult = null;
//        File result = SamUtils.getFileWithBamExtension(inputFile, newEnding);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of isSortedBy method, of class SamUtils.
     */
    @Test
    public void testIsSortedBy() {
//        System.out.println("isSortedBy");
//        File fileToCheck = null;
//        SAMFileHeader.SortOrder sortOrderToCheck = null;
//        boolean expResult = false;
//        boolean result = SamUtils.isSortedBy(fileToCheck, sortOrderToCheck);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getAlignmentBlocks method, of class SamUtils.
     */
    @Test
    public void testGetAlignmentBlocks() {
        System.out.println("getAlignmentBlocks");
        //Testing N in middle and at the end and others without an effect: S, H, I, P
        List<CigarElement> cigarElements1 = new ArrayList<>();
        cigarElements1.add(new CigarElement(1, CigarOperator.EQ));
        cigarElements1.add(new CigarElement(3, CigarOperator.N));
        cigarElements1.add(new CigarElement(2, CigarOperator.S));
        cigarElements1.add(new CigarElement(2, CigarOperator.H));
        cigarElements1.add(new CigarElement(2, CigarOperator.I));
        cigarElements1.add(new CigarElement(2, CigarOperator.P));
        cigarElements1.add(new CigarElement(50, CigarOperator.EQ));
        cigarElements1.add(new CigarElement(2, CigarOperator.D));
        cigarElements1.add(new CigarElement(3, CigarOperator.N));
        
        List<SamAlignmentBlock> expResult1 = new ArrayList<>();
        expResult1.add(new SamAlignmentBlock(1, 1));
        expResult1.add(new SamAlignmentBlock(5, 56));
        
        List<SamAlignmentBlock> expResult4 = new ArrayList<>();
        expResult4.add(new SamAlignmentBlock(100, 100));
        expResult4.add(new SamAlignmentBlock(104, 155));
        
        //N at beginning
        List<CigarElement> cigarElements2 = new ArrayList<>();
        cigarElements2.add(new CigarElement(3, CigarOperator.N));
        cigarElements2.add(new CigarElement(50, CigarOperator.EQ));
        
        List<SamAlignmentBlock> expResult2 = new ArrayList<>();
        expResult2.add(new SamAlignmentBlock(4, 53));
        
        List<SamAlignmentBlock> expResult5 = new ArrayList<>();
        expResult5.add(new SamAlignmentBlock(103, 152));
        
        //multiple N regions and multiple consecutive mapped regions
        List<CigarElement> cigarElements3 = new ArrayList<>();
        cigarElements3.add(new CigarElement(1, CigarOperator.EQ));
        cigarElements3.add(new CigarElement(3, CigarOperator.N));
        cigarElements3.add(new CigarElement(50, CigarOperator.EQ));
        cigarElements3.add(new CigarElement(10, CigarOperator.M));
        cigarElements3.add(new CigarElement(20, CigarOperator.X));
        cigarElements3.add(new CigarElement(2, CigarOperator.N));
        cigarElements3.add(new CigarElement(10, CigarOperator.EQ));
        
        List<SamAlignmentBlock> expResult3 = new ArrayList<>();
        expResult3.add(new SamAlignmentBlock(1, 1));
        expResult3.add(new SamAlignmentBlock(5, 84));
        expResult3.add(new SamAlignmentBlock(87, 96));
        
        List<SamAlignmentBlock> expResult6 = new ArrayList<>();
        expResult6.add(new SamAlignmentBlock(100, 100));
        expResult6.add(new SamAlignmentBlock(104, 183));
        expResult6.add(new SamAlignmentBlock(186, 195));
        
        Cigar cigar1 = new Cigar(cigarElements1);
        Cigar cigar2 = new Cigar(cigarElements2);
        Cigar cigar3 = new Cigar(cigarElements3);
        
        int refStartPos1 = 1;
        int refStartPos2 = 100;
        
        SamUtils instance = new SamUtils();
        List<SamAlignmentBlock> result1 = instance.getAlignmentBlocks(cigar1, refStartPos1);
        List<SamAlignmentBlock> result2 = instance.getAlignmentBlocks(cigar2, refStartPos1);
        List<SamAlignmentBlock> result3 = instance.getAlignmentBlocks(cigar3, refStartPos1);
        
        List<SamAlignmentBlock> result4 = instance.getAlignmentBlocks(cigar1, refStartPos2);
        List<SamAlignmentBlock> result5 = instance.getAlignmentBlocks(cigar2, refStartPos2);
        List<SamAlignmentBlock> result6 = instance.getAlignmentBlocks(cigar3, refStartPos2);
        
        assertThat(expResult1.get(0), equalTo(result1.get(0)));
        assertThat(expResult1.get(1), equalTo(result1.get(1)));
        assertThat(expResult2.get(0), equalTo(result2.get(0)));
        assertThat(expResult3.get(0), equalTo(result3.get(0)));
        assertThat(expResult3.get(1), equalTo(result3.get(1)));
        assertThat(expResult3.get(2), equalTo(result3.get(2)));
        assertThat(expResult4.get(0), equalTo(result4.get(0)));
        assertThat(expResult4.get(1), equalTo(result4.get(1)));
        assertThat(expResult5.get(0), equalTo(result5.get(0)));
        assertThat(expResult6.get(0), equalTo(result6.get(0)));
        assertThat(expResult6.get(1), equalTo(result6.get(1)));
        assertThat(expResult6.get(2), equalTo(result6.get(2)));
    }
    
}
