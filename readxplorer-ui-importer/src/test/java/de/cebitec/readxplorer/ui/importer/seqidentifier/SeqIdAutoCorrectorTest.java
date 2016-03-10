/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.ui.importer.seqidentifier;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdAutoCorrectorTest {

    public SeqIdAutoCorrectorTest() {
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
     * Test of isFixed method, of class SeqIdAutoCorrector.
     */
    @Test
    public void correctSeqIds() {
        String pao1 = "PAO1";
        String pao11 = "PAO11";
        String chrUscore1 = "chr_1";
        String chrUscore3 = "chr_3";
        String chr1 = "chr1";
        String chr2 = "chr2";
        String chr4 = "chr4";
        String chromosome1 = "chromosome1";
        String chromosomeUscore2 = "chromosome_2";
        String id1 = "1";
        String id2 = "2";
        String id3 = "3";
        String id4 = "4";

        Set<String> pao1Ids = new HashSet<>();
        pao1Ids.add( pao1 );
        Set<String> pao11Ids = new HashSet<>();
        pao11Ids.add( pao11 );

        SAMSequenceDictionary pao1Dictionary = new SAMSequenceDictionary();
        pao1Dictionary.addSequence( new SAMSequenceRecord( pao1, 6000000 ) );

        //Nothing to correct, always needs to work
        SeqIdAutoCorrector pao1Corrector = new SeqIdAutoCorrector( pao1Dictionary, pao1Ids );
        assertTrue( pao1Corrector.isFixed() );

        SAMSequenceDictionary pao11Dictionary = new SAMSequenceDictionary();
        pao11Dictionary.addSequence( new SAMSequenceRecord( pao11, 6000000 ) );

        //Auto correction of PAO11 to PAO1
        SeqIdAutoCorrector pao11Corrector = new SeqIdAutoCorrector( pao11Dictionary, pao1Ids );
        assertTrue( pao11Corrector.isFixed() );

        SAMSequenceDictionary multiDictionary = new SAMSequenceDictionary();
        multiDictionary.addSequence( new SAMSequenceRecord( pao11, 6000000 ) );
        multiDictionary.addSequence( new SAMSequenceRecord( chrUscore1, 6000000 ) );
        multiDictionary.addSequence( new SAMSequenceRecord( chr1, 6000000 ) );
        multiDictionary.addSequence( new SAMSequenceRecord( chr2, 6000000 ) );

        //Keeping PAO11 while ignoring the other sequences
        SeqIdAutoCorrector multiCorrector = new SeqIdAutoCorrector( multiDictionary, pao11Ids );
        assertFalse( multiCorrector.isFixed() );
        assertTrue( multiCorrector.getMissingSeqIds().size() == 3 );
        assertTrue( multiDictionary.size() == 4 );

        SAMSequenceDictionary multiDictionaryWorkx = new SAMSequenceDictionary();
        multiDictionaryWorkx.addSequence( new SAMSequenceRecord( pao11, 6000000 ) );
        multiDictionaryWorkx.addSequence( new SAMSequenceRecord( chrUscore1, 6000000 ) );
        multiDictionaryWorkx.addSequence( new SAMSequenceRecord( chr1, 6000000 ) );
        multiDictionaryWorkx.addSequence( new SAMSequenceRecord( chr2, 6000000 ) );
        
        //Failing because PAO1 and PAO11 are different
        SeqIdAutoCorrector multiCorrectorFail = new SeqIdAutoCorrector( multiDictionaryWorkx, pao11Ids );
        assertFalse( multiCorrectorFail.isFixed() );

        Set<String> multiIds = new HashSet<>();
        multiIds.add( pao11 );
        multiIds.add( chr1 );
        multiIds.add( chr2 );
        multiIds.add( chrUscore1 );

        //Auto correction of multiple sequences
        SeqIdAutoCorrector multiCorrector2 = new SeqIdAutoCorrector( multiDictionary, multiIds );
        assertTrue( multiCorrector2.isFixed() );

        SAMSequenceDictionary multiDictionary2 = new SAMSequenceDictionary();
        multiDictionary2.addSequence( new SAMSequenceRecord( chromosome1, 6000000 ) );
        multiDictionary2.addSequence( new SAMSequenceRecord( chromosomeUscore2, 6000000 ) );
        multiDictionary2.addSequence( new SAMSequenceRecord( chrUscore3, 6000000 ) );
        multiDictionary2.addSequence( new SAMSequenceRecord( chr4, 6000000 ) );

        Set<String> multiIds2 = new HashSet<>();
        multiIds2.add( id1 );
        multiIds2.add( id3 );
        multiIds2.add( id4 );
        multiIds2.add( id2 );

        //Auto correction of multiple sequences 2 (replace chrx by x)
        SeqIdAutoCorrector multiCorrector3 = new SeqIdAutoCorrector( multiDictionary2, multiIds2 );
        assertTrue( multiCorrector3.isFixed() );
        
        SAMSequenceDictionary multiDictionary3 = new SAMSequenceDictionary();
        multiDictionary3.addSequence( new SAMSequenceRecord( id1, 6000000 ) );
        multiDictionary3.addSequence( new SAMSequenceRecord( id2, 6000000 ) );
        multiDictionary3.addSequence( new SAMSequenceRecord( id3, 6000000 ) );
        multiDictionary3.addSequence( new SAMSequenceRecord( id4, 6000000 ) );
        
        Set<String> multiIds3 = new HashSet<>();
        multiIds3.add( chromosome1 );
        multiIds3.add( chromosomeUscore2 );
        multiIds3.add( chrUscore3 );
        multiIds3.add( chr4 );

        //Auto correction of multiple sequences 2 (replace chrx by x)
        SeqIdAutoCorrector multiCorrector4 = new SeqIdAutoCorrector( multiDictionary3, multiIds3 );
        assertTrue( multiCorrector4.isFixed() );
        
        
    }


}
