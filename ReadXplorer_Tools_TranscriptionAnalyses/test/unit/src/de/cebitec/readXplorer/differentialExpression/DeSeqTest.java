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
//import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
//import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
//import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
//import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import org.junit.After;
//import org.junit.AfterClass;
//import static org.junit.Assert.*;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.openide.util.Exceptions;
//
///**
// *
// * @author kstaderm
// */
//public class DeSeqTest {
//
//    public DeSeqTest() {
//    }
//    private static DeSeq instance = new DeSeq();
//    private static int numberOfFeatures;
//    private static int numberOfTracks;
//    private static List<PersistentTrack> selectedTracks;
//    private static List<String> loci;
//    private static List<String> tracks;
//    private static List<List<Integer>> allCountData;
//    private static Map<String, String[]> MultiFactorDesign;
//    private static Map<String, String[]> TwoFactorDesign;
//    private static List<String> fit1;
//    private static List<String> fit2;
//    private static File saveFile = new File("C:\\Users\\Kai\\Desktop\\newTest.rdata");
//
//    @BeforeClass
//    public static void setUpClass() {
//        DeAnalysisHandler.TESTING_MODE = false;
//        FileReader fr = null;
//        tracks = new ArrayList<>();
//        loci = new ArrayList<>();
//        allCountData = new ArrayList<>();
//        List<Integer> countData1 = new ArrayList<>();
//        List<Integer> countData2 = new ArrayList<>();
//        List<Integer> countData3 = new ArrayList<>();
//        List<Integer> countData4 = new ArrayList<>();
//        List<Integer> countData5 = new ArrayList<>();
//        List<Integer> countData6 = new ArrayList<>();
//        List<Integer> countData7 = new ArrayList<>();
//        try {
//            fr = new FileReader("C:/Users/Kai/Documents/NetBeansProjects/ReadXplorer/ReadXplorer_AddOns/ReadXplorer_Tools_TranscriptionAnalyses/test/unit/src/de/cebitec/readXplorer/differentialExpression/pasilla_gene_counts.tsv");
//            BufferedReader br = new BufferedReader(fr);
//            String[] headLine = br.readLine().split("\t");
//            for (int i = 1; i < headLine.length; i++) {
//                tracks.add(headLine[i]);
//            }
//            String line;
//            while (true) {
//                line = br.readLine();
//                if (line == null) {
//                    break;
//                }
//                String[] splittedLine = line.split("\t");
//                loci.add(splittedLine[0]);
//                countData1.add(Integer.parseInt(splittedLine[1]));
//                countData2.add(Integer.parseInt(splittedLine[2]));
//                countData3.add(Integer.parseInt(splittedLine[3]));
//                countData4.add(Integer.parseInt(splittedLine[4]));
//                countData5.add(Integer.parseInt(splittedLine[5]));
//                countData6.add(Integer.parseInt(splittedLine[6]));
//                countData7.add(Integer.parseInt(splittedLine[7]));
//            }
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } finally {
//            try {
//                fr.close();
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//
//        allCountData.add(countData1);
//        allCountData.add(countData2);
//        allCountData.add(countData3);
//        allCountData.add(countData4);
//        allCountData.add(countData5);
//        allCountData.add(countData6);
//        allCountData.add(countData7);
//
//        MultiFactorDesign = new HashMap<>();
//        String[] value1 = {"untreated", "untreated", "untreated", "untreated", "treated", "treated", "treated"};
//        MultiFactorDesign.put("condition", value1);
//        String[] value2 = {"single-end", "single-end", "paired-end", "paired-end", "single-end", "paired-end", "paired-end"};
//        MultiFactorDesign.put("libType", value2);
//
//        TwoFactorDesign = new HashMap<>();
//        String[] value = {"untreated", "untreated", "treated", "treated"};
//        TwoFactorDesign.put("condition", value);
//
//
//        fit1 = new ArrayList<>();
//        fit1.add("libType");
//        fit1.add("condition");
//
//        fit2 = new ArrayList<>();
//        fit2.add("libType");
//
//
//        selectedTracks = new ArrayList<>();
//        for (int i = 0; i < tracks.size(); i++) {
//            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//            selectedTracks.add(new PersistentTrack(i, "", tracks.get(i), currentTimestamp, 0, 0));
//        }
//
//        numberOfFeatures = loci.size();
//        numberOfTracks = tracks.size();
//
//        System.out.println("Setup ready");
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
//     * Test of process method, of class DeSeq.
//     */
//    @Test
//    public void testProcess() {
//        System.out.println("Testing multi and two factor design experiments");
//        int expResult = 4;
//        DeSeqAnalysisData analysisData = new DeSeqAnalysisData(tracks.size(), new HashMap<>(MultiFactorDesign), true, new ArrayList<>(fit1), new ArrayList<>(fit2), false);
//        analysisData.setLoci(loci.toArray(new String[loci.size()]));
//        analysisData.setSelectedTracks(new ArrayList<>(selectedTracks));
//        for (Iterator<List<Integer>> it = allCountData.iterator(); it.hasNext();) {
//            List<Integer> current = it.next();
//            analysisData.addCountDataForTrack(current.toArray(new Integer[current.size()]));
//        }
//
//        List result=new ArrayList();
//        try {
//            result = instance.process(analysisData, numberOfFeatures, numberOfTracks, saveFile);
//        } catch (PackageNotLoadableException | JRILibraryNotInPathException | UnknownGnuRException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        assertEquals(expResult, result.size());
//
//        expResult = 3;
//        analysisData = new DeSeqAnalysisData(tracks.size(), new HashMap<>(TwoFactorDesign), false, new ArrayList<>(fit1), new ArrayList<>(fit2), false);
//        analysisData.setLoci(loci.toArray(new String[loci.size()]));
//
//        List<PersistentTrack> selectedTracksTwoFactor = new ArrayList<>();
//
//        List<Integer> current = allCountData.get(2);
//        analysisData.addCountDataForTrack(current.toArray(new Integer[current.size()]));
//        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//        selectedTracksTwoFactor.add(new PersistentTrack(2, "", tracks.get(2), currentTimestamp, 0, 0));
//
//        current = allCountData.get(3);
//        analysisData.addCountDataForTrack(current.toArray(new Integer[current.size()]));
//        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//        selectedTracksTwoFactor.add(new PersistentTrack(3, "", tracks.get(3), currentTimestamp, 0, 0));
//
//        current = allCountData.get(5);
//        analysisData.addCountDataForTrack(current.toArray(new Integer[current.size()]));
//        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//        selectedTracksTwoFactor.add(new PersistentTrack(5, "", tracks.get(5), currentTimestamp, 0, 0));
//
//        current = allCountData.get(6);
//        analysisData.addCountDataForTrack(current.toArray(new Integer[current.size()]));
//        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//        selectedTracksTwoFactor.add(new PersistentTrack(6, "", tracks.get(6), currentTimestamp, 0, 0));
//
//        analysisData.setSelectedTracks(selectedTracksTwoFactor);
//        try {
//            result = instance.process(analysisData, numberOfFeatures, numberOfTracks, null);
//        } catch (PackageNotLoadableException | JRILibraryNotInPathException | UnknownGnuRException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        assertEquals(expResult, result.size());
//    }
//}