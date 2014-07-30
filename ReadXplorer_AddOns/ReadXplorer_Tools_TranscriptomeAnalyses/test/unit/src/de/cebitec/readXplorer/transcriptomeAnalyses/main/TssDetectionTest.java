/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.util.FeatureType;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author jritter
 */
public class TssDetectionTest {

    HashMap<Integer, PersistantFeature> allRegionsInHash;
    ParameterSetFiveEnrichedAnalyses parameters;
    StatisticsOnMappingData stats;

    public TssDetectionTest() {
        allRegionsInHash = new HashMap<>();

        parameters = new ParameterSetFiveEnrichedAnalyses();

        stats = new StatisticsOnMappingData();
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of runningTSSDetection method, of class TssDetection.
     */
    @Test
    public void testRunningTSSDetection() {
        System.out.println("runningTSSDetection");

        int[][] forwardStarts = new int[1][30000];
        int[][] reverseStarts = new int[1][30000];

        fillArraysWithStarts(forwardStarts, reverseStarts);
        HashMap<Integer, List<Integer>> forwardCDSs = setUpFwdCDSs();
        HashMap<Integer, List<Integer>> reverseCDSs = setUpRevCDSs();
        int trackId = 1;
        TssDetection instance = new TssDetection(trackId);

        // needed for running tss detection
        File persRefFile = new File("C:\\Users\\jritter\\Documents\\MA-Thesis\\AN\\Chromosome.genbank.fasta");
        PersistantReference persRef = new PersistantReference(1, "B.Methanolicus", "no description", new Timestamp(20014, 7, 21, 11, 12, 50, 0), persRefFile);
        instance.runningTSSDetection(persRef, forwardCDSs, reverseCDSs, allRegionsInHash, stats, 0, parameters);
        List<TranscriptionStart> detectedStarts = instance.getResults();
        boolean condition = detectedStarts.isEmpty();

        /**
         * Normal. Offset schould be > 0, and smaller than Limit for offsets (in
         * test case 500), on the reverse site there should be nothing.
         */
        boolean fwdCase1 = false;
        /**
         * Leaderkess. Offset should be > 0 but smaller leaderless restriction
         * length (in test case 3).
         */
        boolean fwdCase2 = false;
        /**
         * Leaderless. Offset should be = 0 but distance to start of feature
         * should be smaller than leaderless restriction (in test case 3)
         */
        boolean fwdCase3 = false;
        /**
         * Leaderless. Offset = 0, distance to start also = 0 => TSS(i) = TLS.
         */
        boolean fwdCase4 = false;
        /**
         *
         */
        boolean fwdCase5 = false;
        /**
         *
         */
        boolean fwdCase6 = false;
        /**
         *
         */
        boolean fwdCase7 = false;
        /**
         *
         */
        boolean fwdCase8 = false;
        /**
         *
         */
        boolean fwdCase9 = false;
        /**
         *
         */
        boolean fwdCase10 = false;
        /**
         *
         */
        boolean fwdCase11 = false;

//        Assert.assertTrue("The TSS-Array is Empty", condition);
    }

    private HashMap<Integer, List<Integer>> setUpFwdCDSs() {
        HashMap<Integer, List<Integer>> forwardCDSs = new HashMap<>();
        PersistantFeature feature1 = new PersistantFeature(1, 1, "", "1", "test_1", "no", 666, 1000, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature2 = new PersistantFeature(2, 1, "", "2", "test_2", "no", 1752, 3458, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature3 = new PersistantFeature(4, 1, "", "3", "test_3", "no", 6000, 8333, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature4 = new PersistantFeature(7, 1, "", "4", "test_4", "no", 12891, 13995, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature5 = new PersistantFeature(9, 1, "", "5", "test_5", "no", 15002, 16245, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature6 = new PersistantFeature(10, 1, "", "6", "test_6", "no", 16444, 17568, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature7 = new PersistantFeature(14, 1, "", "7", "test_7", "no", 24000, 25354, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature8 = new PersistantFeature(15, 1, "", "8", "test_8", "no", 25481, 25954, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature9 = new PersistantFeature(17, 1, "", "9", "test_9", "no", 27034, 28548, true, FeatureType.CDS, "Test_1");
        PersistantFeature feature10 = new PersistantFeature(18, 1, "", "10", "test_10", "no", 29000, 29999, true, FeatureType.CDS, "Test_1");
        this.allRegionsInHash.put(feature1.getId(), feature1);
        this.allRegionsInHash.put(feature2.getId(), feature2);
        this.allRegionsInHash.put(feature3.getId(), feature3);
        this.allRegionsInHash.put(feature4.getId(), feature4);
        this.allRegionsInHash.put(feature5.getId(), feature5);
        this.allRegionsInHash.put(feature6.getId(), feature6);
        this.allRegionsInHash.put(feature7.getId(), feature7);
        this.allRegionsInHash.put(feature8.getId(), feature8);
        this.allRegionsInHash.put(feature9.getId(), feature9);
        this.allRegionsInHash.put(feature10.getId(), feature10);

        createCDSsStrandInformation(forwardCDSs, feature1.getId(), feature1.getStart(), feature1.getStop());
        createCDSsStrandInformation(forwardCDSs, feature2.getId(), feature2.getStart(), feature2.getStop());
        createCDSsStrandInformation(forwardCDSs, feature3.getId(), feature3.getStart(), feature3.getStop());
        createCDSsStrandInformation(forwardCDSs, feature4.getId(), feature4.getStart(), feature4.getStop());
        createCDSsStrandInformation(forwardCDSs, feature5.getId(), feature5.getStart(), feature5.getStop());
        createCDSsStrandInformation(forwardCDSs, feature6.getId(), feature6.getStart(), feature6.getStop());
        createCDSsStrandInformation(forwardCDSs, feature7.getId(), feature7.getStart(), feature7.getStop());
        createCDSsStrandInformation(forwardCDSs, feature8.getId(), feature8.getStart(), feature8.getStop());
        createCDSsStrandInformation(forwardCDSs, feature9.getId(), feature9.getStart(), feature9.getStop());
        createCDSsStrandInformation(forwardCDSs, feature10.getId(), feature10.getStart(), feature10.getStop());

        return forwardCDSs;
    }

    private HashMap<Integer, List<Integer>> setUpRevCDSs() {
        HashMap<Integer, List<Integer>> reverseCDSs = new HashMap<>();

        PersistantFeature feature11 = new PersistantFeature(3, 1, "", "11", "test_11", "no", 5000, 5530, false, FeatureType.CDS, "Test_1");
        PersistantFeature feature12 = new PersistantFeature(5, 1, "", "12", "test_12", "no", 9003, 9450, false, FeatureType.CDS, "Test_1");
        PersistantFeature feature13 = new PersistantFeature(6, 1, "", "13", "test_13", "no", 11000, 12000, false, FeatureType.CDS, "Test_1");
        PersistantFeature feature14 = new PersistantFeature(8, 1, "", "14", "test_14", "no", 14203, 14950, false, FeatureType.CDS, "Test_1");
        PersistantFeature feature15 = new PersistantFeature(11, 1, "", "15", "test_15", "no", 18222, 19750, false, FeatureType.CDS, "Test_1");
        PersistantFeature feature16 = new PersistantFeature(12, 1, "", "16", "test_16", "no", 21000, 22000, false, FeatureType.CDS, "Test_1");
        PersistantFeature feature17 = new PersistantFeature(13, 1, "", "17", "test_17", "no", 23000, 24000, false, FeatureType.CDS, "Test_1");
        PersistantFeature feature18 = new PersistantFeature(16, 1, "", "18", "test_18", "no", 27400, 28500, false, FeatureType.CDS, "Test_1");

        this.allRegionsInHash.put(feature11.getId(), feature11);
        this.allRegionsInHash.put(feature12.getId(), feature12);
        this.allRegionsInHash.put(feature13.getId(), feature13);
        this.allRegionsInHash.put(feature14.getId(), feature14);
        this.allRegionsInHash.put(feature15.getId(), feature15);
        this.allRegionsInHash.put(feature16.getId(), feature16);
        this.allRegionsInHash.put(feature17.getId(), feature17);
        this.allRegionsInHash.put(feature18.getId(), feature18);

        createCDSsStrandInformation(reverseCDSs, feature11.getId(), feature11.getStart(), feature11.getStop());
        createCDSsStrandInformation(reverseCDSs, feature12.getId(), feature12.getStart(), feature12.getStop());
        createCDSsStrandInformation(reverseCDSs, feature13.getId(), feature13.getStart(), feature13.getStop());
        createCDSsStrandInformation(reverseCDSs, feature14.getId(), feature14.getStart(), feature14.getStop());
        createCDSsStrandInformation(reverseCDSs, feature15.getId(), feature15.getStart(), feature15.getStop());
        createCDSsStrandInformation(reverseCDSs, feature16.getId(), feature16.getStart(), feature16.getStop());
        createCDSsStrandInformation(reverseCDSs, feature17.getId(), feature17.getStart(), feature17.getStop());
        createCDSsStrandInformation(reverseCDSs, feature18.getId(), feature18.getStart(), feature18.getStop());

        return reverseCDSs;
    }

    /**
     * This method fills a Map of Lists. If there is a feature on Position i,
     * then the list is mapped to that position. The List contains the feature
     * ids corresponding to that Position. Each list can contain max. three
     * different feature ids because of the three reading frames for the forward
     * and reverse direction.
     *
     * @param featureID Persistant feature id.
     * @param start Start position of feature.
     * @param stop Stop position of feature.
     * @param isFwd Feature direction is forward if true, otherwise false.
     */
    private void createCDSsStrandInformation(HashMap<Integer, List<Integer>> list, int featureID, int start, int stop) {

        // EDIT: (i + start - 1) => (i + start - 1)
        for (int i = 0; (i + start) <= stop; i++) {
            if (list.get(i + start) != null) {
                list.get(i + start).add(featureID);
            } else {
                ArrayList<Integer> tmp = new ArrayList<>();
                tmp.add(featureID);
                list.put(i + start, tmp);
            }
        }
    }

    /**
     *
     * @param forwardStarts
     * @param reverseStarts
     */
    private void fillArraysWithStarts(int[][] forwardStarts, int[][] reverseStarts) {
        forwardStarts[0][600] = 3; // false one
        forwardStarts[0][700] = 6; // intragenic tss without associated Feature
        forwardStarts[0][1200] = 10; // intergenic tss without associated Feature
        forwardStarts[0][6002] = 8; // Leaderless, offset = 2
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;
//        forwardStarts[0][333] = 5;

        reverseStarts[0][11998] = 7; // leaderless with distance to start = 2
//        reverseStarts[0][11998] = 7;
//        reverseStarts[0][11998] = 7;
//        reverseStarts[0][11998] = 7;
//        reverseStarts[0][11998] = 7;
//        reverseStarts[0][11998] = 7;
    }
}
