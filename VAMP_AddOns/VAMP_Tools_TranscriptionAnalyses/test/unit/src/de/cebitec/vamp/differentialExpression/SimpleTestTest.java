//package de.cebitec.vamp.differentialExpression;
//
//import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
//import de.cebitec.vamp.differentialExpression.GnuR.JRILibraryNotInPathException;
//import de.cebitec.vamp.differentialExpression.GnuR.UnknownGnuRException;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Iterator;
//import java.util.List;
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
//public class SimpleTestTest {
//
//    private static SimpleTest instance = new SimpleTest();
//    private static int numberOfFeatures;
//    private static List<String> loci;
//    private static List<String> tracks;
//    private static List<List<Integer>> allCountData;
//    private static List<PersistantTrack> selectedTraks;
//    private static int[] groupA;
//    private static int[] groupB;
//    private static File saveFile = new File("C:\\Users\\Kai\\Desktop\\newTest.rdata");
//
//    public SimpleTestTest() {
//    }
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
//            fr = new FileReader("C:/Users/Kai/Documents/NetBeansProjects/VAMP/VAMP_AddOns/VAMP_Tools_TranscriptionAnalyses/test/unit/src/de/cebitec/vamp/differentialExpression/pasilla_gene_counts.tsv");
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
//        selectedTraks = new ArrayList<>();
//        for (int i = 0; i < tracks.size(); i++) {
//            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//            selectedTraks.add(new PersistantTrack(i, "", tracks.get(i), currentTimestamp, 0, 0));
//        }
//
//        numberOfFeatures = loci.size();
//
//        groupA = new int[4];
//        groupA[0] = 1;
//        groupA[1] = 2;
//        groupA[2] = 3;
//        groupA[3] = 4;
//        groupB = new int[3];
//        groupB[0] = 5;
//        groupB[1] = 6;
//        groupB[2] = 7;
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
//     * Test of process method, of class SimpleTest.
//     */
//    @Test
//    public void testProcess() {
//        System.out.println("process");
//        SimpleTestAnalysisData analysisData = new SimpleTestAnalysisData(7, groupA, groupB, false);
//        for (Iterator<List<Integer>> it = allCountData.iterator(); it.hasNext();) {
//            List<Integer> current = it.next();
//            analysisData.addCountDataForTrack(current.toArray(new Integer[current.size()]));
//        }
//        analysisData.setLoci(loci.toArray(new String[loci.size()]));
//        analysisData.setSelectedTraks(new ArrayList<>(selectedTraks));
//        List expResult = null;
//        List result = null;
//        try {
//            result = instance.process(analysisData, numberOfFeatures, saveFile);
//        } catch (JRILibraryNotInPathException | IllegalStateException | UnknownGnuRException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//}
