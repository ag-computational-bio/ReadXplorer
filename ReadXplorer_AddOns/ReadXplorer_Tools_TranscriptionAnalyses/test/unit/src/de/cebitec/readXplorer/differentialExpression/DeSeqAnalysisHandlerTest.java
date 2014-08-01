/* 
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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
package de.cebitec.readXplorer.differentialExpression;

//package de.cebitec.readXplorer.differentialExpression;
//
//import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
//import de.cebitec.readXplorer.util.FeatureType;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// *
// * @author Kai
// */
//public class DeSeqAnalysisHandlerTest implements de.cebitec.readXplorer.util.Observer {
//
//    private DeSeqAnalysisHandler instance;
//    private static final int numberOfTracks = 4;
//    private static List<PersistantTrack> selectedTracks;
//    private static Map<String, String[]> MultiFactorDesign;
//    private static Map<String, String[]> TwoFactorDesign;
//
//    public DeSeqAnalysisHandlerTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() {
//        DeAnalysisHandler.TESTING_MODE = true;
//        selectedTracks = new ArrayList<>();
//        for (int i = 0; i < numberOfTracks; i++) {
//            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//            selectedTracks.add(new PersistantTrack(i, "", "track" + i, currentTimestamp, 0, 0));
//        }
//
//        MultiFactorDesign = new HashMap<>();
//        String[] value1 = {"a", "a", "b", "b"};
//        MultiFactorDesign.put("Eins", value1);
//        String[] value2 = {"c", "c", "d", "d"};
//        MultiFactorDesign.put("Zwei", value2);
//        String[] value3 = {"e", "e", "f", "f"};
//        MultiFactorDesign.put("Drei", value3);
//
//        TwoFactorDesign = new HashMap<>();
//        String[] value = {"untreated", "untreated", "treated", "treated"};
//        TwoFactorDesign.put("condition", value);
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    /**
//     * Test of performAnalysis method, of class DeSeqAnalysisHandler.
//     */
//    @Test
//    public void testPerformAnalysis() {
//        System.out.println("perform single analysis");
//        instance = new DeSeqAnalysisHandler(selectedTracks, TwoFactorDesign, false, null, null, 0, true, null, FeatureType.ANY, 300, 0);
//        instance.registerObserver(this);
//        instance.start();
//        instance.endAnalysis();
//
//        System.out.println("perform multi analysis");
//        instance = new DeSeqAnalysisHandler(selectedTracks, MultiFactorDesign, true, null, null, 0, true, null, FeatureType.ANY, 300, 0);
//        instance.registerObserver(this);
//        instance.start();
//        instance.endAnalysis();
//    }
//
//    @Override
//    public void update(Object args) {
//        List<DeSeqAnalysisHandler.Result> res = instance.getResults();
//        for (Iterator<DeAnalysisHandler.Result> it = res.iterator(); it.hasNext();) {
//            DeAnalysisHandler.Result result = it.next();
//            result.getTableContents();
//            result.getColnames();
//            result.getRownames();
//            System.out.println("Ergebnis");
//        }
//    }
//}
