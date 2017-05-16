/*
 * Copyright (C) 2016 Rolf.Hilker
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

package de.cebitec.readxplorer.databackend.dataobjects;

import de.cebitec.readxplorer.api.enums.ReadPairType;
import de.cebitec.readxplorer.utils.SamAlignmentBlock;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 *
 * @author Rolf.Hilker
 */
public class ReadPairTest {

    private static final int START = 500;
    private static final int STOP = 600;
    private static final int START2 = 700;
    private static final int STOP2 = 850;
    private static final int START_OVERLAP = 550;
    private static final int STOP_OVERLAP = 750;
    private static final int STARTA_BLOCK = START;
    private static final int STOPA_BLOCK = 530;
    private static final int STARTA_BLOCK2 = START_OVERLAP;
    private static final int STOPA_BLOCK2 = STOP;
    private static final int STARTB_BLOCK = START_OVERLAP;
    private static final int STOPB_BLOCK = 650;
    private static final int STARTB_BLOCK2 = 700;
    private static final int STOPB_BLOCK2 = STOP_OVERLAP;
    private static Mapping mapping;
    private static Mapping mapping2;
    private static Mapping mappingOverlaps;
    private static Mapping mappingBlocks;
    private static Mapping mappingBlocks2;
    private static ReadPair readPair;
    private static ReadPair readPair2;
    private static ReadPair readPair3;
    private static ReadPair readPair4;
    private static ReadPair readPair5;
    private static ReadPair readPair6;
    private static ReadPair readPair7;
    private static ReadPair readPair8;
    private static List<SamAlignmentBlock> alignmentBlocksA;
    private static List<SamAlignmentBlock> alignmentBlocksB;

    public ReadPairTest() {
    }


    @BeforeClass
    public static void setUpClass() {
        mapping = new Mapping( START, STOP, true );
        mapping2 = new Mapping( START2, STOP2, false );
        mappingOverlaps = new Mapping( START_OVERLAP, STOP_OVERLAP, false );
        mappingBlocks = new Mapping( STARTA_BLOCK, STOPA_BLOCK2, true );
        mappingBlocks2 = new Mapping( STARTB_BLOCK, STOPB_BLOCK2, false );
        
        alignmentBlocksA = new ArrayList<>();
        alignmentBlocksA.add( new SamAlignmentBlock( STARTA_BLOCK, STOPA_BLOCK ) );
        alignmentBlocksA.add( new SamAlignmentBlock( STARTA_BLOCK2, STOPA_BLOCK2 ) );
        
        alignmentBlocksB = new ArrayList<>();
        alignmentBlocksB.add( new SamAlignmentBlock( STARTB_BLOCK, STOPB_BLOCK ) );
        alignmentBlocksB.add( new SamAlignmentBlock( STARTB_BLOCK2, STOPB_BLOCK2 ) );

        readPair = new ReadPair( 1, 12, 13, ReadPairType.PERFECT_UNQ_PAIR, 1, mapping );
        readPair2 = new ReadPair( 2, 12, 13, ReadPairType.PERFECT_UNQ_PAIR, 1, mapping2, mapping );
        readPair3 = new ReadPair( 3, 12, 13, ReadPairType.PERFECT_UNQ_PAIR, 1, mapping, mapping2 );
        readPair4 = new ReadPair( 4, 12, 14, ReadPairType.DIST_LARGE_UNQ_PAIR, 1, mapping, mappingOverlaps );
        readPair5 = new ReadPair( 5, 12, 14, ReadPairType.DIST_LARGE_UNQ_PAIR, 1, mappingOverlaps, mapping );
        readPair6 = new ReadPair( 6, 12, 14, ReadPairType.DIST_LARGE_UNQ_PAIR, 1, mappingBlocks, mappingBlocks2 );
        readPair7 = new ReadPair( 7, 12, 14, ReadPairType.DIST_LARGE_UNQ_PAIR, 1, mappingBlocks, mappingBlocks2 );
        readPair8 = new ReadPair( 8, 12, 14, ReadPairType.DIST_LARGE_UNQ_PAIR, 1, mappingBlocks2, mappingBlocks );
        
        
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
     * Test of getStart method, of class ReadPair.
     */
    @Test
    public void testGetStart() {
        System.out.println( "getStart" );
        assertEquals( START, readPair.getStart() );
        assertEquals( START, readPair2.getStart() );
        assertEquals( START, readPair3.getStart() );
    }


    /**
     * Test of getStop method, of class ReadPair.
     */
    @Test
    public void testGetStop() {
        System.out.println( "getStop" );
        assertEquals( STOP, readPair.getStop() );
        assertEquals( STOP2, readPair2.getStop() );
        assertEquals( STOP2, readPair3.getStop() );
    }


    /**
     * Test of getFragmentLength method, of class ReadPair.
     */
    @Test
    public void testGetFragmentLength() {
        System.out.println( "getFragmentLength" );
        //only 1 visible mapping
        assertEquals( STOP - START + 1, readPair.getFragmentLength() );
        //2 mappings without blocks, second mapping as first argument
        assertEquals( STOP - START + 1 + STOP2 - START2 + 1, readPair2.getFragmentLength() );
        //2 mappings without blocks, first mapping as first argument
        assertEquals( STOP - START + 1 + STOP2 - START2 + 1, readPair3.getFragmentLength() );
        //2 mappings without blocks, they overlap
        assertEquals( STOP_OVERLAP - START + 1, readPair4.getFragmentLength() );
        //2 mappings without blocks, they overlap, second mapping as first argument
        assertEquals( STOP_OVERLAP - START + 1, readPair5.getFragmentLength() );
        
        //mapping 1 has blocks, mapping 2 not
        mappingBlocks.setAlignmentBlocks( alignmentBlocksA );
        assertEquals( STOPA_BLOCK - STARTA_BLOCK + 1 + STOPA_BLOCK2 - STARTA_BLOCK2 + 1 + STOPB_BLOCK2 - STOPA_BLOCK2, readPair6.getFragmentLength() );
        
        //mapping 2 has blocks, mapping 1 not
        mappingBlocks = new Mapping( STARTA_BLOCK, STOPA_BLOCK2, true );
        readPair7.setVisibleMapping( mappingBlocks );
        mappingBlocks2.setAlignmentBlocks( alignmentBlocksB );
        assertEquals( STOPA_BLOCK2 - STARTA_BLOCK + 1 + STOPB_BLOCK - STOPA_BLOCK2 + STOPB_BLOCK2 - STARTB_BLOCK2 + 1, readPair7.getFragmentLength() );
        
        //both mappings have blocks
        mappingBlocks.setAlignmentBlocks( alignmentBlocksA );
        assertEquals( 183, readPair8.getFragmentLength() );
    }


}
