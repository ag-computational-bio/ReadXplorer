/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.parser.mappings;

import de.cebitec.readxplorer.api.enums.ReadPairExtensions;
import de.cebitec.readxplorer.utils.Pair;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CommonsMappingParserTest {

    private String readName1;
    private String readName4;
    private SAMFileHeader samFileHeader;
    private SAMRecord record;
    private SAMRecord record2;
    private SAMRecord record3;
    private SAMRecord record4;
    private SAMRecord record5;
    private SAMRecord record6;


    public CommonsMappingParserTest() {
    }


    @BeforeClass
    public static void setUpClass() {
    }


    @AfterClass
    public static void tearDownClass() {
    }


    @Before
    public void setUp() {
        readName1 = "HWI-ST143_0357:3:1101:1124:2075#CAGATC";
        readName4 = "@EAS139:136:FC706VJ:2:2104:15343:197393 1:Y:18:ATCACG";
        samFileHeader = new SAMFileHeader();
        record = new SAMRecord( samFileHeader );
        record2 = new SAMRecord( samFileHeader );
        record3 = new SAMRecord( samFileHeader );
        record4 = new SAMRecord( samFileHeader );
        record5 = new SAMRecord( samFileHeader );
        record6 = new SAMRecord( samFileHeader );
        record.setReadName( readName1 );
        record2.setReadName( readName1 + "/1" );
        record3.setReadName( readName1 + "/2" );
        record4.setReadName( readName4 );
        record5.setReadName( readName1 );
        record6.setReadName( readName1 );
        record5.setReadPairedFlag( true );
        record5.setFirstOfPairFlag( true );
        record6.setReadPairedFlag( true );
        record6.setSecondOfPairFlag( true );
    }


    @After
    public void tearDown() {
    }


//    /**
//     * Test of countDiffsAndGaps method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCountDiffsAndGaps() {
//        System.out.println( "countDiffsAndGaps" );
//        String cigar = "";
//        String readSeq = "";
//        String refSeq = "";
//        boolean isRevStrand = false;
//        int expResult = 0;
//        int result = CommonsMappingParser.countDiffsAndGaps( cigar, readSeq, refSeq, isRevStrand );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of createDiffsAndGaps method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCreateDiffsAndGaps_5args() {
//        System.out.println( "createDiffsAndGaps" );
//        String cigar = "";
//        String readSeq = "";
//        String refSeq = "";
//        boolean isRevStrand = false;
//        int start = 0;
//        DiffAndGapResult expResult = null;
//        DiffAndGapResult result = CommonsMappingParser.createDiffsAndGaps( cigar, readSeq, refSeq, isRevStrand, start );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of createDiffsAndGaps method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCreateDiffsAndGaps_4args() {
//        System.out.println( "createDiffsAndGaps" );
//        String readSeq = "";
//        String refSeq = "";
//        int start = 0;
//        byte direction = 0;
//        DiffAndGapResult expResult = null;
//        DiffAndGapResult result = CommonsMappingParser.createDiffsAndGaps( readSeq, refSeq, start, direction );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of getOrderForGap method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testGetOrderForGap() {
//        System.out.println( "getOrderForGap" );
//        int gapPos = 0;
//        Map<Integer, Integer> gapOrderIndex = null;
//        int expResult = 0;
//        int result = CommonsMappingParser.getOrderForGap( gapPos, gapOrderIndex );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of createMappingOfRefAndRead method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCreateMappingOfRefAndRead() {
//        System.out.println( "createMappingOfRefAndRead" );
//        String cigar = "";
//        String refSeq = "";
//        String readSeq = "";
//        String[] expResult = null;
//        String[] result = CommonsMappingParser.createMappingOfRefAndRead( cigar, refSeq, readSeq );
//        assertArrayEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of checkRead method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCheckRead() {
//        System.out.println( "checkRead" );
//        MessageSenderI parent = null;
//        String readSeq = "";
//        int refSeqLength = 0;
//        int start = 0;
//        int stop = 0;
//        String filename = "";
//        int lineNo = 0;
//        boolean expResult = false;
//        boolean result = CommonsMappingParser.checkRead( parent, readSeq, refSeqLength, start, stop, filename, lineNo );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of checkReadSam method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCheckReadSam() {
//        System.out.println( "checkReadSam" );
//        MessageSenderI parent = null;
//        String readSeq = "";
//        int refSeqLength = 0;
//        String cigar = "";
//        int start = 0;
//        int stop = 0;
//        String filename = "";
//        int lineno = 0;
//        boolean expResult = false;
//        boolean result = CommonsMappingParser.checkReadSam( parent, readSeq, refSeqLength, cigar, start, stop, filename, lineno );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of checkReadJok method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCheckReadJok() {
//        System.out.println( "checkReadJok" );
//        MessageSenderI parent = null;
//        String readSeq = "";
//        String readname = "";
//        String refSeq = "";
//        int refSeqLength = 0;
//        int start = 0;
//        int stop = 0;
//        int direction = 0;
//        String filename = "";
//        int lineno = 0;
//        boolean expResult = false;
//        boolean result = CommonsMappingParser.checkReadJok( parent, readSeq, readname, refSeq, refSeqLength, start, stop, direction, filename, lineno );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of countStopPosition method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testCountStopPosition() {
//        System.out.println( "countStopPosition" );
//        String cigar = "";
//        Integer startPosition = null;
//        Integer readLength = null;
//        int expResult = 0;
//        int result = CommonsMappingParser.countStopPosition( cigar, startPosition, readLength );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }


    /**
     * Test of getReadNameWithoutPairTag method, of class CommonsMappingParser.
     */
    @Test
    public void testGetReadNameWithoutPairTag() {
        System.out.println( "getReadNameWithoutPairTag" );
        Pair<Boolean, String> namePair = CommonsMappingParser.getReadNameWithoutPairTag( readName1 );
        Pair<Boolean, String> namePair2 = CommonsMappingParser.getReadNameWithoutPairTag( readName4 );
        Pair<Boolean, String> namePair3 = CommonsMappingParser.getReadNameWithoutPairTag( record2.getReadName() );
        Pair<Boolean, String> namePair4 = CommonsMappingParser.getReadNameWithoutPairTag( record3.getReadName() );
        assertEquals( false, namePair.getFirst() );
        assertEquals( true, namePair2.getFirst() );
        assertEquals( true, namePair3.getFirst() );
        assertEquals( true, namePair4.getFirst() );
        assertEquals( readName1, namePair.getSecond() );
        assertEquals( "@EAS139:136:FC706VJ:2:2104:15343:197393", namePair2.getSecond() );
        assertEquals( readName1, namePair3.getSecond() );
        assertEquals( readName1, namePair4.getSecond() );
    }


    /**
     * Test of getReadPairTag method, of class CommonsMappingParser.
     */
    @Test
    public void testGetReadPairTag() {
        System.out.println( "getReadPairTag" );
        ReadPairExtensions readPairTag1 = CommonsMappingParser.getReadPairTag( record );
        ReadPairExtensions readPairTag2 = CommonsMappingParser.getReadPairTag( record2 );
        ReadPairExtensions readPairTag3 = CommonsMappingParser.getReadPairTag( record3 );
        ReadPairExtensions readPairTag4 = CommonsMappingParser.getReadPairTag( record4 );
        ReadPairExtensions readPairTag5 = CommonsMappingParser.getReadPairTag( record5 );
        ReadPairExtensions readPairTag6 = CommonsMappingParser.getReadPairTag( record6 );
        assertEquals( ReadPairExtensions.Undefined, readPairTag1 );
        assertEquals( ReadPairExtensions.A1, readPairTag2 );
        assertEquals( ReadPairExtensions.A2, readPairTag3 );
        assertEquals( ReadPairExtensions.A1, readPairTag4 );
        assertEquals( ReadPairExtensions.A1, readPairTag5 );
        assertEquals( ReadPairExtensions.A2, readPairTag6 );
    }


//    /**
//     * Test of isCasavaLarger1Dot8Format method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testIsCasavaLarger1Dot8Format() {
//        System.out.println( "isCasavaLarger1Dot8Format" );
//        String readName = "";
//        boolean expResult = false;
//        boolean result = CommonsMappingParser.isCasavaLarger1Dot8Format( readName );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of elongatePairedReadName method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testElongatePairedReadName() {
//        System.out.println( "elongatePairedReadName" );
//        SAMRecord record = null;
//        String expResult = "";
//        String result = CommonsMappingParser.elongatePairedReadName( record );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
    /**
     * Test of checkOrRemovePairTag method, of class CommonsMappingParser.
     */
    @Test
    public void testCheckOrRemovePairTag() {
        System.out.println( "checkOrRemovePairTag" );
        CommonsMappingParser.checkOrRemovePairTag( record );
        CommonsMappingParser.checkOrRemovePairTag( record2 );
        CommonsMappingParser.checkOrRemovePairTag( record3 );
        CommonsMappingParser.checkOrRemovePairTag( record4 );
        assertEquals( readName1, record.getReadName() );
        assertEquals( readName1, record2.getReadName() );
        assertEquals( readName1, record3.getReadName() );
        assertEquals( "@EAS139:136:FC706VJ:2:2104:15343:197393", record4.getReadName() );
    }


//    /**
//     * Test of isMappedSequence method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testIsMappedSequence() {
//        System.out.println( "isMappedSequence" );
//        int flag = 0;
//        int startPosition = 0;
//        boolean expResult = false;
//        boolean result = CommonsMappingParser.isMappedSequence( flag, startPosition );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of addClassificationData method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testAddClassificationData() throws Exception {
//        System.out.println( "addClassificationData" );
//        Map<SAMRecord, Integer> recordToDiffMap = null;
//        ParsedClassification classification = null;
//        CommonsMappingParser.addClassificationData( recordToDiffMap, classification );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of writeSamRecord method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testWriteSamRecord() {
//        System.out.println( "writeSamRecord" );
//        Map<SAMRecord, Integer> diffMap = null;
//        ParsedClassification classificationData = null;
//        SAMFileWriter samBamWriter = null;
//        CommonsMappingParser.writeSamRecord( diffMap, classificationData, samBamWriter );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
//    /**
//     * Test of classifyRead method, of class CommonsMappingParser.
//     */
//    @Test
//    public void testClassifyRead() {
//        System.out.println( "classifyRead" );
//        SAMRecord record = null;
//        MessageSenderI messageSender = null;
//        Map<String, Integer> chromLengthMap = null;
//        String fileName = "";
//        int lineNo = 0;
//        RefSeqFetcher refSeqFetcher = null;
//        Map<SAMRecord, Integer> diffMap = null;
//        ParsedClassification classificationData = null;
//        boolean expResult = false;
//        boolean result = CommonsMappingParser.classifyRead( record, messageSender, chromLengthMap, fileName, lineNo, refSeqFetcher, diffMap, classificationData );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
}
