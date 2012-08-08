package de.cebitec.vamp.seqPairClassifier;

import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import junit.framework.Assert;
import org.junit.*;

/**
 * @author Rolf Hilker
 */
public class SeqPairClassifierTest {
    
    /*
     * Test is run with a distance of 500bp and an allowed deviation of 10% for a perfect pair.
     * Read length is 50bp.
     * Test your own pairs by adding just adding them to the test.
     */
    
    private List<ParsedDiff> diffs = new ArrayList<ParsedDiff>();
    private List<ParsedReferenceGap> gaps = new ArrayList<ParsedReferenceGap>();
    byte one = 1;
    byte mone = -1;    
    
    //perfect pair
    private ParsedMapping m1 = new ParsedMapping(1, 50, one, diffs, gaps, 0);
    private ParsedMapping m2 = new ParsedMapping(450, 500, mone, diffs, gaps, 0);
    
    //perfect pair for testing 2 perfect pairs, one with a replicate
    private ParsedMapping m23 = new ParsedMapping(1, 50, one, diffs, gaps, 0);
    private ParsedMapping m24 = new ParsedMapping(450, 500, mone, diffs, gaps, 0);
    private ParsedMapping m25 = new ParsedMapping(1, 50, one, diffs, gaps, 0);
    private ParsedMapping m26 = new ParsedMapping(450, 500, mone, diffs, gaps, 0);
    private ParsedMapping m3 = new ParsedMapping(500, 550, one, diffs, gaps, 0);
    private ParsedMapping m4 = new ParsedMapping(950, 1000, mone, diffs, gaps, 0);
    private ParsedMapping m27 = new ParsedMapping(500, 550, one, diffs, gaps, 0);
    private ParsedMapping m28 = new ParsedMapping(950, 1000, mone, diffs, gaps, 0);
    
    //distance too small with one mapping per read
    private ParsedMapping m5 = new ParsedMapping(100, 149, one, diffs, gaps, 0);
    private ParsedMapping m6 = new ParsedMapping(400, 449, mone, diffs, gaps, 0);
    
    //distance too large with one mapping per read
    private ParsedMapping m7 = new ParsedMapping(11, 61, one, diffs, gaps, 0);
    private ParsedMapping m8 = new ParsedMapping(711, 761, mone, diffs, gaps, 0);
    
    //orientation wrong pair
    private ParsedMapping m9 = new ParsedMapping(1, 50, one, diffs, gaps, 0);
    private ParsedMapping m10 = new ParsedMapping(450, 500, one, diffs, gaps, 0);
    
    //wrong orientation + dist too small
    private ParsedMapping m11 = new ParsedMapping(100, 149, one, diffs, gaps, 0);
    private ParsedMapping m12 = new ParsedMapping(400, 449, one, diffs, gaps, 0);
    
    //wrong orientation + dist too large
    private ParsedMapping m13 = new ParsedMapping(11, 61, one, diffs, gaps, 0);
    private ParsedMapping m14 = new ParsedMapping(711, 761, one, diffs, gaps, 0);
    
    //single mappings
    private ParsedMapping m15 = new ParsedMapping(22, 522, one, diffs, gaps, 0); //without partner 1
    private ParsedMapping m16 = new ParsedMapping(33, 533, mone, diffs, gaps, 0); //without partner 2
    
    //more single mappings for testing more than one mapping for a pair
    private ParsedMapping m17 = new ParsedMapping(860, 910, one, diffs, gaps, 0); 
    private ParsedMapping m18 = new ParsedMapping(2500, 2550, mone, diffs, gaps, 0); 
    private ParsedMapping m19 = new ParsedMapping(1050, 1100, one, diffs, gaps, 0); //perfect pair among them
    private ParsedMapping m20 = new ParsedMapping(1500, 1550, mone, diffs, gaps, 0);
    private ParsedMapping m21 = new ParsedMapping(3100, 3150, one, diffs, gaps, 0);
    private ParsedMapping m22 = new ParsedMapping(3000, 3050, mone, diffs, gaps, 0);
    private ParsedMapping m29 = new ParsedMapping(30000, 30050, mone, diffs, gaps, 0);
    
    //test perfect pairs with best and no best mappings
    //R1: 1C, 1C, 1B (1c & 2b = perf pair, 3 mappings = 3 mids!)
    //R2: 2C, 2B, 2C
    private ParsedMapping m30 = new ParsedMapping(2, 52, one, diffs, gaps, 2);
    private ParsedMapping m31 = new ParsedMapping(750, 800, one, diffs, gaps, 3);
    private ParsedMapping m32 = new ParsedMapping(500, 550, one, diffs, gaps, 3);
    private ParsedMapping m33 = new ParsedMapping(450, 500, mone, diffs, gaps, 0);
    private ParsedMapping m34 = new ParsedMapping(1, 50, one, diffs, gaps, 0);
    private ParsedMapping m35 = new ParsedMapping(3000, 3050, mone, diffs, gaps, 3);
    
    //R1: 1C, 1C (1b & 2a = perf)
    //R2: 2C, 2C
    private ParsedMapping m36 = new ParsedMapping(200, 250, one, diffs, gaps, 3);
    private ParsedMapping m37 = new ParsedMapping(450, 500, mone, diffs, gaps, 3);
    private ParsedMapping m38 = new ParsedMapping(1, 50, one, diffs, gaps, 3);
    private ParsedMapping m39 = new ParsedMapping(950, 1000, mone, diffs, gaps, 3);
    
    //R1: 1B, 1B, 1C (1a & 2a small pair, no perf pair)
    //R2: 2B, 2C
    private ParsedMapping m40 = new ParsedMapping(300, 350, one, diffs, gaps, 0);
    private ParsedMapping m41 = new ParsedMapping(380, 430, mone, diffs, gaps, 0);
    private ParsedMapping m42 = new ParsedMapping(1, 50, one, diffs, gaps, 0);
    private ParsedMapping m43 = new ParsedMapping(600, 650, mone, diffs, gaps, 2);
    private ParsedMapping m44 = new ParsedMapping(4500, 4550, mone, diffs, gaps, 2);
    
    private ParsedMappingContainer mappings = new ParsedMappingContainer();
    private ParsedMappingContainer mappings2 = new ParsedMappingContainer();
    private CoverageContainer coverageContainer = new CoverageContainer();
    private CoverageContainer coverageContainer2 = new CoverageContainer();
    private ParsedTrack fwdTrack = new ParsedTrack(1, "fwd track", mappings, coverageContainer, 1);
    private ParsedTrack revTrack = new ParsedTrack(2, "rev track", mappings2, coverageContainer2, 1);
    
    private List<HashMap<String,Integer>> readnameToSeqIDMap = new ArrayList<HashMap<String,Integer>>();
    HashMap<String,Integer> map1 = new HashMap<String,Integer>();
    HashMap<String,Integer> map2 = new HashMap<String,Integer>();
    
    public SeqPairClassifierTest() {
        
        /*
         * Readnames in map1 and map2 have to be identical to be recogized as pair!
         */
        
        m1.setID(1);
        m2.setID(2);
        mappings.addParsedMapping(m1, 1); //perf pair
        mappings2.addParsedMapping(m2, 2);
        map1.put("a", 1); 
        map2.put("a", 2);
        
        m23.setID(3);
        m24.setID(4);
        m25.setID(3);
        m26.setID(4);
        m3.setID(19);
        m4.setID(20);
        m27.setID(19);
        m28.setID(20);
        mappings.addParsedMapping(m23, 3); //triple perf pair
        mappings2.addParsedMapping(m24, 4);
        mappings.addParsedMapping(m25, 3);
        mappings2.addParsedMapping(m26, 4);
        mappings.addParsedMapping(m3, 3);
        mappings2.addParsedMapping(m4, 4);
        mappings.addParsedMapping(m27, 3);
        mappings2.addParsedMapping(m28, 4);
        map1.put("b", 3); 
        map2.put("b", 4);
        map1.put("k", 3);
        map2.put("k", 4);
                
        m5.setID(5);
        m6.setID(6);
        mappings.addParsedMapping(m5, 5); //distance too small pair
        mappings2.addParsedMapping(m6, 6);
        map1.put("c", 5);
        map2.put("c", 6);
        
        m7.setID(7);
        m8.setID(8);
        mappings.addParsedMapping(m7, 7); //distance too large pair
        mappings2.addParsedMapping(m8, 8);
        map1.put("d", 7);
        map2.put("d", 8);
        
        m9.setID(9);
        m10.setID(10);
        mappings.addParsedMapping(m9, 9); //orientation wrong pair
        mappings2.addParsedMapping(m10, 10);
        map1.put("e", 9);
        map2.put("e", 10);
        
        m11.setID(11);
        m12.setID(12);
        mappings.addParsedMapping(m11, 11); //orient wrong + dist too small
        mappings2.addParsedMapping(m12, 12);
        map1.put("f", 11); 
        map2.put("f", 12);
        
        m13.setID(13);
        m14.setID(14);
        mappings.addParsedMapping(m13, 13); //orient wrong + dist too large
        mappings2.addParsedMapping(m14, 14);
        map1.put("g", 13); 
        map2.put("g", 14);
        
        m15.setID(15);
        m16.setID(16);
        mappings.addParsedMapping(m15, 15); //2 single mappings
        mappings2.addParsedMapping(m16, 16);   
        map1.put("h", 15);
        map2.put("i", 16);
        
        m17.setID(17);
        m18.setID(18);
        m19.setID(21);
        m20.setID(22);
        m21.setID(23);
        m22.setID(24);
        m29.setID(25);
        mappings.addParsedMapping(m17, 17); //more than one mapping per read
        mappings2.addParsedMapping(m18, 18);
        mappings.addParsedMapping(m19, 17); //perfect pair among them
        mappings2.addParsedMapping(m20, 18);
        mappings.addParsedMapping(m21, 17);
        mappings2.addParsedMapping(m22, 18);
        mappings.addParsedMapping(m29, 17);
        map1.put("j", 17);
        map2.put("j", 18);


        //R1: 1C, 1C, 1B (3 mappings = 3 mids!)
        //R2: 2C, 2B, 2C
        m30.setID(30);
        m31.setID(31);
        m32.setID(32);
        m33.setID(33);
        m34.setID(34);
        m35.setID(35);
        mappings.addParsedMapping(m30, 30); //more than one mapping per read
        mappings2.addParsedMapping(m31, 31);
        mappings.addParsedMapping(m32, 30); //perfect pair among them
        mappings2.addParsedMapping(m33, 31);
        mappings.addParsedMapping(m34, 30);
        mappings2.addParsedMapping(m35, 31);
        map1.put("l", 30);
        map2.put("l", 31);
        
        //R1: 1C, 1C (1b & 2a = perf)
        //R2: 2C, 2C
        m36.setID(36);
        m37.setID(37);
        m38.setID(38);
        m39.setID(39);
        mappings.addParsedMapping(m36, 36);
        mappings2.addParsedMapping(m37, 37);
        mappings.addParsedMapping(m38, 36);
        mappings2.addParsedMapping(m39, 37);
        map1.put("m", 36);
        map2.put("m", 37);
        
        //R1: 1B, 1B, 1C (1b & 2b = perf, but no pair should be stored)
        //R2: 2B, 2C
        m40.setID(40);
        m41.setID(41);
        m42.setID(42);
        m43.setID(43);
        m44.setID(44);
        mappings.addParsedMapping(m40, 38);
        mappings2.addParsedMapping(m41, 39);
        mappings.addParsedMapping(m42, 38);
        mappings2.addParsedMapping(m43, 39);
        mappings.addParsedMapping(m44, 38);
        map1.put("n", 38);
        map2.put("n", 39); 
        
        readnameToSeqIDMap.add(map1);
        readnameToSeqIDMap.add(map2);
        
        fwdTrack.setReadnameToSeqIdMap(map1);
        revTrack.setReadnameToSeqIdMap(map2);
        
        coverageContainer.computeCoverage(mappings);
        coverageContainer2.computeCoverage(mappings2);
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
     * Test of classifySeqPairs method, of class SeqPairClassifier.
     * @throws Exception 
     */
    @Test
    public void testClassifySeqPairs() throws Exception {
        System.out.println("calculateSeqPairs");
        
        short orientation = 0; // 0 = fr, 1 = rf, 2 = ff/rr
        SeqPairClassifier seqPaircalc = new SeqPairClassifier(fwdTrack, revTrack, 500, 10, orientation);
        ParsedSeqPairContainer result = seqPaircalc.classifySeqPairs();
        HashMap<Pair<Long, Long>, ParsedSeqPairMapping> pairs = result.getParsedSeqPairs();
        List<Pair<Long,Long>> list = result.getMappingToPairIdList();
        
        //perfect pair
        ParsedSeqPairMapping pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("1"), Long.valueOf("2")));
        Assert.assertTrue(pair1.getMappingId1() == 1 && pair1.getMappingId2() == 2 && pair1.getType() == 0
                            && pair1.getReplicates() == 1);
        
        //tripled perfect pair
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("3"), Long.valueOf("4")));
        Assert.assertTrue(pair1.getMappingId1() == 3 && pair1.getMappingId2() == 4 && pair1.getType() == 0
                            && pair1.getReplicates() == 2);
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("19"), Long.valueOf("20")));
        Assert.assertTrue(pair1.getMappingId1() == 19 && pair1.getMappingId2() == 20 && pair1.getType() == 0
                            && pair1.getReplicates() == 2);
        
        //dist too small pair
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("5"), Long.valueOf("6")));
        Assert.assertTrue(pair1.getMappingId1() == 5 && pair1.getMappingId2() == 6 && pair1.getType() == 2
                            && pair1.getReplicates() == 1);
        
        //dist too large pair
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("7"), Long.valueOf("8")));
        Assert.assertTrue(pair1.getMappingId1() == 7 && pair1.getMappingId2() == 8 && pair1.getType() == 1
                            && pair1.getReplicates() == 1);
        
        //orientation wrong pair
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("9"), Long.valueOf("10")));
        Assert.assertTrue(pair1.getMappingId1() == 9 && pair1.getMappingId2() == 10 && pair1.getType() == 3
                            && pair1.getReplicates() == 1);
        
        //dist too small + orient wrong pair
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("11"), Long.valueOf("12")));
        Assert.assertTrue(pair1.getMappingId1() == 11 && pair1.getMappingId2() == 12 && pair1.getType() == 5
                            && pair1.getReplicates() == 1);
        
        //dist too large + orient wrong pair
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("13"), Long.valueOf("14")));
        Assert.assertTrue(pair1.getMappingId1() == 13 && pair1.getMappingId2() == 14 && pair1.getType() == 4
                            && pair1.getReplicates() == 1);
        
        //single reads
        Assert.assertTrue(pairs.containsKey(new Pair<Long, Long>(Long.valueOf("15"), Long.valueOf("16"))) == false);
        
        //multiple mappings per read perf pair
        pair1 = pairs.get(new Pair<Long, Long>(Long.valueOf("21"), Long.valueOf("22")));
        Assert.assertTrue(pair1.getMappingId1() == 21 && pair1.getMappingId2() == 22 && pair1.getType() == 0
                            && pair1.getReplicates() == 1);
        
        Iterator<Pair<Long, Long>> keys = pairs.keySet().iterator();
        while (keys.hasNext()){
            ParsedSeqPairMapping mp = pairs.get(keys.next());
            System.out.println(mp.toString());
        }
        for (Pair<Long,Long> pair : list){
            System.out.println("MID: "+pair.getFirst()+", PairID: "+pair.getSecond());
        }
    }
}
