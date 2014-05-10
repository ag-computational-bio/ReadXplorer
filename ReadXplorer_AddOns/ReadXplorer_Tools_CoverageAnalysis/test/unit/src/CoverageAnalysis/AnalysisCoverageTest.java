package CoverageAnalysis;

import de.cebitec.readXplorer.coverageAnalysis.ParameterSetCoverageAnalysis;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class AnalysisCoverageTest {
    
    public AnalysisCoverageTest() {
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
     * Test of update method, of class AnalysisCoverage.
     */
    @Test
    public void testUpdate() {
        
        ParametersReadClasses readClassParamsCommon = new ParametersReadClasses(true, true, true, false, new Byte("0"));
        ParametersReadClasses readClassParamsBM = new ParametersReadClasses(true, true, false, false, new Byte("0"));
        ParametersReadClasses readClassParamsPerfect = new ParametersReadClasses(true, false, false, false, new Byte("0"));
        
        ParameterSetCoverageAnalysis sumCoverageParamsCommon = new ParameterSetCoverageAnalysis(5, true, true, readClassParamsCommon);
        ParameterSetCoverageAnalysis sumCoverageParamsBM = new ParameterSetCoverageAnalysis(5, true, false, readClassParamsBM);
        ParameterSetCoverageAnalysis sumCoverageParamsPerfect = new ParameterSetCoverageAnalysis(5, true, true, readClassParamsPerfect);
        
//        try {
//            TrackConnector connector = new TrackConnector(1);
//            AnalysisCoverage instanceSumPerfect = new AnalysisCoverage(connector, sumCoverageParamsPerfect);
//            AnalysisCoverage instanceSumBM = new AnalysisCoverage(connector, sumCoverageParamsBM);
//            AnalysisCoverage instanceSumCommon = new AnalysisCoverage(connector, sumCoverageParamsCommon);

            int size = 20000;
            int size2 = 40000;
            int size3 = 60000;
            PersistantCoverage coverage = new PersistantCoverage(1, size);
            PersistantCoverage coverage2 = new PersistantCoverage(size + 1, size2);
            PersistantCoverage coverage4 = new PersistantCoverage(size + 1, size2);
            PersistantCoverage coverage3 = new PersistantCoverage(size2 + 1, size3);
            
            int[] covValues = {0, 4, 5, 6, 100, 3, 100, 2, 100, 5};
            //Result both strands = 2000-14000 = interval1, 16-20000 = interval2
            //Result = 4000-10000 = interval1, 12000-14000 = interval2, 16-20000 = interval3
            int[] covValues2 = {5, 2, 3, 100, 2, 1, 0, 4, 10, 5};
            int[] covValues3 = {5, 2, 3, 100, 2, 1, 0, 4, 10, 5};
            int[] covValues4 = {5, 5, 5, 100, 4, 4, 4, 4, 10, 5};
            
            int[] perfArrayFwd = this.generateArray(covValues, size);
            int[] perfArrayRev = this.generateArray(covValues, size);
            int[] bmArrayFwd = this.generateArray(covValues, size);
            int[] bmArrayRev = this.generateArray(covValues, size);
            int[] commonArrayFwd = this.generateArray(covValues, size);
            int[] commonArrayRev = this.generateArray(covValues, size);
            
            int[] perfArrayFwd2 = this.generateArray(covValues2, size);
            int[] perfArrayRev2 = this.generateArray(covValues2, size);
            int[] bmArrayFwd2 = this.generateArray(covValues2, size);
            int[] bmArrayRev2 = this.generateArray(covValues2, size);
            int[] commonArrayFwd2 = this.generateArray(covValues2, size);
            int[] commonArrayRev2 = this.generateArray(covValues2, size);
            
            int[] perfArrayFwd3 = this.generateArray(covValues3, size);
            int[] perfArrayRev3 = this.generateArray(covValues3, size);
            int[] bmArrayFwd3 = this.generateArray(covValues3, size);
            int[] bmArrayRev3 = this.generateArray(covValues3, size);
            int[] commonArrayFwd3 = this.generateArray(covValues3, size);
            int[] commonArrayRev3 = this.generateArray(covValues3, size);
            
            int[] perfArrayFwd4 = this.generateArray(covValues4, size);
            int[] perfArrayRev4 = this.generateArray(covValues4, size);
            int[] bmArrayFwd4 = this.generateArray(covValues4, size);
            int[] bmArrayRev4 = this.generateArray(covValues4, size);
            int[] commonArrayFwd4 = this.generateArray(covValues4, size);
            int[] commonArrayRev4 = this.generateArray(covValues4, size);
            
            coverage.setPerfectFwdMult(perfArrayFwd);
            coverage.setPerfectRevMult(perfArrayRev);
            coverage.setBestMatchFwdMult(bmArrayFwd);
            coverage.setBestMatchRevMult(bmArrayRev);
            coverage.setCommonFwdMult(commonArrayFwd);
            coverage.setCommonRevMult(commonArrayRev);
            CoverageAndDiffResultPersistant coverageResult = new CoverageAndDiffResultPersistant(coverage, null, null, null);
            
            coverage2.setPerfectFwdMult(perfArrayFwd2);
            coverage2.setPerfectRevMult(perfArrayRev2);
            coverage2.setBestMatchFwdMult(bmArrayFwd2);
            coverage2.setBestMatchRevMult(bmArrayRev2);
            coverage2.setCommonFwdMult(commonArrayFwd2);
            coverage2.setCommonRevMult(commonArrayRev2);
            CoverageAndDiffResultPersistant coverageResult2 = new CoverageAndDiffResultPersistant(coverage2, null, null, null);
            
            coverage3.setPerfectFwdMult(perfArrayFwd3);
            coverage3.setPerfectRevMult(perfArrayRev3);
            coverage3.setBestMatchFwdMult(bmArrayFwd3);
            coverage3.setBestMatchRevMult(bmArrayRev3);
            coverage3.setCommonFwdMult(commonArrayFwd3);
            coverage3.setCommonRevMult(commonArrayRev3);
            CoverageAndDiffResultPersistant coverageResult3 = new CoverageAndDiffResultPersistant(coverage3, null, null, null);
            
            coverage4.setPerfectFwdMult(perfArrayFwd4);
            coverage4.setPerfectRevMult(perfArrayRev4);
            coverage4.setBestMatchFwdMult(bmArrayFwd4);
            coverage4.setBestMatchRevMult(bmArrayRev4);
            coverage4.setCommonFwdMult(commonArrayFwd4);
            coverage4.setCommonRevMult(commonArrayRev4);
            CoverageAndDiffResultPersistant coverageResult4 = new CoverageAndDiffResultPersistant(coverage4, null, null, null);
            
            //standard order
//            instanceSumPerfect.update(coverageResult);
//            instanceSumPerfect.update(coverageResult2);
//            instanceSumPerfect.update(coverageResult3);
            
            //reverse order
//            instanceSumPerfect.update(coverageResult);
//            instanceSumPerfect.update(coverageResult2);
//            instanceSumPerfect.update(coverageResult3);
//            
//            instanceSumPerfect.finishAnalysis();
//            List<CoverageInterval> intervals = instanceSumPerfect.getResults().getCoverageIntervals();
//            for (CoverageInterval interval : intervals) {
//                System.out.println("start: " + interval.getStart() + ", stop: " + interval.getStop());
//            }
            
            //standard order
//            Assert.assertTrue(intervals.get(0).getStart() == 2001);
//            Assert.assertTrue(intervals.get(0).getStop() == 14000);
//            Assert.assertTrue(intervals.get(1).getStart() == 16001);
//            Assert.assertTrue(intervals.get(1).getStop() == 22000);
//            Assert.assertTrue(intervals.get(0).getStart() == 2000);
//            Assert.assertTrue(intervals.get(0).getStop() == 10000);
//            Assert.assertTrue(intervals.get(1).getStart() == 12000);
//            Assert.assertTrue(intervals.get(1).getStop() == 14000);
//            Assert.assertTrue(intervals.get(2).getStart() == 16000);
//            Assert.assertTrue(intervals.get(2).getStart() == 20000);
            
            //reverse order
//            Assert.assertTrue(intervals.get(2).getStart() == 2001);
//            Assert.assertTrue(intervals.get(2).getStop() == 14000);
//            Assert.assertTrue(intervals.get(1).getStart() == 16001);
//            Assert.assertTrue(intervals.get(1).getStop() == 22000);
//            Assert.assertTrue(intervals.get(0).getStart() == 2000);
//            Assert.assertTrue(intervals.get(0).getStop() == 10000);
//            Assert.assertTrue(intervals.get(1).getStart() == 12000);
//            Assert.assertTrue(intervals.get(1).getStop() == 14000);
//            Assert.assertTrue(intervals.get(2).getStart() == 16000);
//            Assert.assertTrue(intervals.get(2).getStart() == 20000);
//            
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }
    /**
     * Test of getResults method, of class AnalysisCoverage.
     */
    @Test
    public void testGetResults() {
//        System.out.println("getResults");
//        AnalysisCoverage instance = null;
//        CoverageIntervalContainer expResult = null;
//        CoverageIntervalContainer result = instance.getResults();
//        assertEquals(expResult, result);
    }

    private int[] generateArray(int[] covValues, int size) {
        int[] covArray = new int[size];
        int length = size / covValues.length;
        for (int i = 0; i < covValues.length; ++i) {
            int currentEndPos = length * i;
            for (int j = 0; j < length; ++j) {
                covArray[currentEndPos + j] = covValues[i];
            }
        }
        return covArray;
    }
}