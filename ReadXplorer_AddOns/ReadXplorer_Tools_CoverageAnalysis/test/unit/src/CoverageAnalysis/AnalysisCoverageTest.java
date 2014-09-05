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
package CoverageAnalysis;

import de.cebitec.readXplorer.coverageAnalysis.ParameterSetCoverageAnalysis;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistent;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.util.classification.FeatureType;
import de.cebitec.readXplorer.util.classification.MappingClass;
import java.util.ArrayList;
import java.util.List;
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
        
        List<Classification> excludedClassesCommon = new ArrayList<>();
        excludedClassesCommon.add(FeatureType.MULTIPLE_MAPPED_READ);
        
        List<Classification> excludedClassesBM = new ArrayList<>();
        excludedClassesBM.add(FeatureType.MULTIPLE_MAPPED_READ);
        excludedClassesBM.add(MappingClass.COMMON_MATCH);
        
        List<Classification> excludedClassesPerfect = new ArrayList<>();
        excludedClassesPerfect.add(FeatureType.MULTIPLE_MAPPED_READ);
        excludedClassesPerfect.add(MappingClass.COMMON_MATCH);
        excludedClassesPerfect.add(MappingClass.BEST_MATCH);
        
        ParametersReadClasses readClassParamsCommon = new ParametersReadClasses(excludedClassesCommon, new Byte("0"));
        ParametersReadClasses readClassParamsBM = new ParametersReadClasses(excludedClassesBM, new Byte("0"));
        ParametersReadClasses readClassParamsPerfect = new ParametersReadClasses(excludedClassesPerfect, new Byte("0"));
        
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
            CoverageManager coverage = new CoverageManager(1, size);
            CoverageManager coverage2 = new CoverageManager(size + 1, size2);
            CoverageManager coverage4 = new CoverageManager(size + 1, size2);
            CoverageManager coverage3 = new CoverageManager(size2 + 1, size3);
            
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
            
            coverage.getCoverage(MappingClass.PERFECT_MATCH).setFwdCoverage(perfArrayFwd);
            coverage.getCoverage(MappingClass.PERFECT_MATCH).setRevCoverage(perfArrayRev);
            coverage.getCoverage(MappingClass.BEST_MATCH).setFwdCoverage(bmArrayFwd);
            coverage.getCoverage(MappingClass.BEST_MATCH).setRevCoverage(bmArrayRev);
            coverage.getCoverage(MappingClass.COMMON_MATCH).setFwdCoverage(commonArrayFwd);
            coverage.getCoverage(MappingClass.COMMON_MATCH).setRevCoverage(commonArrayRev);
            CoverageAndDiffResultPersistent coverageResult = new CoverageAndDiffResultPersistent(coverage, null, null, null);
            
            coverage2.getCoverage(MappingClass.PERFECT_MATCH).setFwdCoverage(perfArrayFwd2);
            coverage2.getCoverage(MappingClass.PERFECT_MATCH).setRevCoverage(perfArrayRev2);
            coverage2.getCoverage(MappingClass.BEST_MATCH).setFwdCoverage(bmArrayFwd2);
            coverage2.getCoverage(MappingClass.BEST_MATCH).setRevCoverage(bmArrayRev2);
            coverage2.getCoverage(MappingClass.COMMON_MATCH).setFwdCoverage(commonArrayFwd2);
            coverage2.getCoverage(MappingClass.COMMON_MATCH).setRevCoverage(commonArrayRev2);
            CoverageAndDiffResultPersistent coverageResult2 = new CoverageAndDiffResultPersistent(coverage2, null, null, null);
            
            coverage3.getCoverage(MappingClass.PERFECT_MATCH).setFwdCoverage(perfArrayFwd3);
            coverage3.getCoverage(MappingClass.PERFECT_MATCH).setRevCoverage(perfArrayRev3);
            coverage3.getCoverage(MappingClass.BEST_MATCH).setFwdCoverage(bmArrayFwd3);
            coverage3.getCoverage(MappingClass.BEST_MATCH).setRevCoverage(bmArrayRev3);
            coverage3.getCoverage(MappingClass.COMMON_MATCH).setFwdCoverage(commonArrayFwd3);
            coverage3.getCoverage(MappingClass.COMMON_MATCH).setRevCoverage(commonArrayRev3);
            CoverageAndDiffResultPersistent coverageResult3 = new CoverageAndDiffResultPersistent(coverage3, null, null, null);
            
            coverage4.getCoverage(MappingClass.PERFECT_MATCH).setFwdCoverage(perfArrayFwd4);
            coverage4.getCoverage(MappingClass.PERFECT_MATCH).setRevCoverage(perfArrayRev4);
            coverage4.getCoverage(MappingClass.BEST_MATCH).setFwdCoverage(bmArrayFwd4);
            coverage4.getCoverage(MappingClass.BEST_MATCH).setRevCoverage(bmArrayRev4);
            coverage4.getCoverage(MappingClass.COMMON_MATCH).setFwdCoverage(commonArrayFwd4);
            coverage4.getCoverage(MappingClass.COMMON_MATCH).setRevCoverage(commonArrayRev4);
            CoverageAndDiffResultPersistent coverageResult4 = new CoverageAndDiffResultPersistent(coverage4, null, null, null);
            
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