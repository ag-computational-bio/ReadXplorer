/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.datastructures;


import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests the utility methods of the {@link UnionFractionMapping} class.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class UnionFractionMappingTest {

    private static Mapping mapping1;
    private static Mapping mapping2;

    private static PersistentFeature feature1;
    private static PersistentFeature feature2;
    private static PersistentFeature feature3;
    private static PersistentFeature feature4;
    private static PersistentFeature feature5;
    private static PersistentFeature feature6;
    private static PersistentFeature feature7;
    private static PersistentFeature feature8;
    private static PersistentFeature feature9;

    private static NormalizedReadCount normaReadCount;
    private static NormalizedReadCount normaReadCount2;
    private static NormalizedReadCount normaReadCount3;
    private static NormalizedReadCount normaReadCount4;
    private static NormalizedReadCount normaReadCount5;

    private static Map<Integer, NormalizedReadCount> featureReadCount;

    /**
     * Tests the utility methods of the {@link UnionFractionMapping} class.
     */
    public UnionFractionMappingTest() {
    }


    @BeforeClass
    public static void setUpClass() {
        mapping1 = new Mapping( 500, 600, true );
        mapping2 = new Mapping( 1500, 1600, false );

        feature1 = new PersistentFeature( 1, 1, "", "", "", "", 400, 650, true, FeatureType.CDS, "feat1" );
        feature2 = new PersistentFeature( 2, 1, "", "", "", "", 550, 950, true, FeatureType.CDS, "feat2" );
        feature3 = new PersistentFeature( 3, 1, "", "", "", "", 450, 900, true, FeatureType.CDS, "feat3" );
        feature4 = new PersistentFeature( 4, 1, "", "", "", "", 1200, 1550, false, FeatureType.CDS, "feat4" );
        feature5 = new PersistentFeature( 5, 1, "", "", "", "", 1450, 1850, false, FeatureType.CDS, "feat5" );
        feature6 = new PersistentFeature( 6, 1, "", "", "", "", 1490, 1650, false, FeatureType.CDS, "feat6" );
        feature7 = new PersistentFeature( 7, 1, "", "", "", "", 400, 650, true, FeatureType.GENE, "feat7" );
        feature8 = new PersistentFeature( 8, 1, "", "", "", "", 1400, 1800, false, FeatureType.GENE, "feat8" );
        feature9 = new PersistentFeature( 9, 1, "", "", "", "", 1499, 1850, false, FeatureType.CDS, "feat9" );

        normaReadCount = new NormalizedReadCount( feature1, 0, 0, 500, 1 );
        normaReadCount2 = new NormalizedReadCount( feature3, 0, 0, 700, 1 );
        normaReadCount3 = new NormalizedReadCount( feature5, 0, 0, 800, 1 );
        normaReadCount4 = new NormalizedReadCount( feature6, 0, 0, 400, 1 );
        normaReadCount5 = new NormalizedReadCount( feature9, 0, 0, 900, 1 );

        featureReadCount = new HashMap<>();

        featureReadCount.put( normaReadCount.getFeature().getId(), normaReadCount );
        featureReadCount.put( normaReadCount2.getFeature().getId(), normaReadCount2 );
        featureReadCount.put( normaReadCount3.getFeature().getId(), normaReadCount3 );
        featureReadCount.put( normaReadCount4.getFeature().getId(), normaReadCount4 );
        featureReadCount.put( normaReadCount5.getFeature().getId(), normaReadCount5 );
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
     * Test of checkAssignment method, of class AssignedMapping.
     */
    @Test
    public void testCheckAssignment() {
        System.out.println( "checkAssignment" );

        AssignedMapping assignedMapping = new UnionFractionMapping( mapping1 );
        AssignedMapping assignedMapping2 = new UnionFractionMapping( mapping2 );
        // Test cases:
        // 1. Fst added mapping - does it work, is it added?
        boolean countIt = assignedMapping.checkAssignment( feature1.getStart(), feature1.getStop(), feature1 );
        assertEquals( true, countIt );
        assertTrue( assignedMapping.getAssignedFeatures().size() == 1 );
        assertTrue( assignedMapping.getAssignedFeatures().contains( feature1 ) );
        // 2. Second feature of same type added:
        // - fwd strand:
        // 1. Mapping is overlapping
        boolean countIt2 = assignedMapping.checkAssignment( feature2.getStart(), feature2.getStop(), feature2 );
        assertEquals( false, countIt2 );
        assertTrue( assignedMapping.getAssignedFeatures().size() == 1 );
        assertTrue( assignedMapping.getAssignedFeatures().contains( feature1 ) );
        // 2. Mapping is contained in both
        boolean countIt3 = assignedMapping.checkAssignment( feature3.getStart(), feature3.getStop(), feature3 );
        assertEquals( true, countIt3 );
        assertTrue( assignedMapping.getAssignedFeatures().size() == 2 );
        assertTrue( assignedMapping.getAssignedFeatures().contains( feature1 ) );
        assertTrue( assignedMapping.getAssignedFeatures().contains( feature3 ) );
        //3. a feature of different type is added
        boolean countIt4 = assignedMapping.checkAssignment( feature7.getStart(), feature7.getStop(), feature7 );
        assertEquals( true, countIt4 );
        assertTrue( assignedMapping.getAssignedFeatures().size() == 3 );
        assertTrue( assignedMapping.getAssignedFeatures().contains( feature1 ) );
        assertTrue( assignedMapping.getAssignedFeatures().contains( feature3 ) );
        assertTrue( assignedMapping.getAssignedFeatures().contains( feature7 ) );
        // - rev strand
        // 1. Fst added mapping - does it work, is it added?
        boolean countIt5 = assignedMapping2.checkAssignment( feature4.getStart(), feature4.getStop(), feature4 );
        assertEquals( true, countIt5 );
        assertTrue( assignedMapping2.getAssignedFeatures().size() == 1 );
        assertTrue( assignedMapping2.getAssignedFeatures().contains( feature4 ) );
        // 2. Second feature of same type added:
        // 1. Mapping is overlapping
        boolean countIt6 = assignedMapping2.checkAssignment( feature5.getStart(), feature5.getStop(), feature5 );
        assertEquals( true, countIt6 );
        assertTrue( assignedMapping2.getAssignedFeatures().size() == 1 );
        assertTrue( assignedMapping2.getAssignedFeatures().contains( feature5 ) );
        assertTrue( assignedMapping2.isRemoveFeatures() );
        assertTrue( assignedMapping2.getRevRemoveList().size() == 1 );
        assertTrue( assignedMapping2.getRevRemoveList().get( 0 ).equals( feature4 ) );
        // 2. Mapping is contained in both
        boolean countIt7 = assignedMapping2.checkAssignment( feature6.getStart(), feature6.getStop(), feature6 );
        assertEquals( true, countIt7 );
        assertTrue( assignedMapping2.getAssignedFeatures().size() == 2 );
        assertTrue( assignedMapping2.getAssignedFeatures().contains( feature5 ) );
        assertTrue( assignedMapping2.getAssignedFeatures().contains( feature6 ) );
        //3. a feature of different type is added
        boolean countIt8 = assignedMapping2.checkAssignment( feature8.getStart(), feature8.getStop(), feature8 );
        assertEquals( true, countIt8 );
        assertTrue( assignedMapping2.getAssignedFeatures().size() == 3 );
        assertTrue( assignedMapping2.getAssignedFeatures().contains( feature5 ) );
        assertTrue( assignedMapping2.getAssignedFeatures().contains( feature6 ) );
        assertTrue( assignedMapping2.getAssignedFeatures().contains( feature8 ) );

        assignedMapping2.notifyRemovedFeatures();
        assertTrue( null == assignedMapping2.getRevRemoveList() );

    }


    /**
     * Test of fractionAssignmentCheck method, of class AssignedMapping.
     */
    @Test
    public void testFractionAssignmentCheck() {
        System.out.println( "fractionAssignmentCheck" );
        AssignedMapping assignedMapping = new UnionFractionMapping( mapping1 );
        AssignedMapping assignedMapping2 = new UnionFractionMapping( mapping2 );
        // Test cases:
        // 1. Fst added mapping - does it work, is it added?
        boolean countIt = assignedMapping.checkAssignment( feature1.getStart(), feature1.getStop(), feature1 );
        boolean countIt2 = assignedMapping.checkAssignment( feature3.getStart(), feature3.getStop(), feature3 );
        assignedMapping.fractionAssignmentCheck( featureReadCount );
        assertEquals ( 499.5, normaReadCount.getReadCount(), 0.0 );
        assertEquals ( 699.5, normaReadCount2.getReadCount(), 0.0 );
        // - rev strand
        // 1. Fst added mapping - does it work, is it added?
        boolean countIt3 = assignedMapping2.checkAssignment( feature5.getStart(), feature5.getStop(), feature5 );
        boolean countIt4 = assignedMapping2.checkAssignment( feature6.getStart(), feature6.getStop(), feature6 );
        boolean countIt5 = assignedMapping2.checkAssignment( feature9.getStart(), feature9.getStop(), feature9 );
        assignedMapping2.fractionAssignmentCheck( featureReadCount );
        assertEquals( 799.333, normaReadCount3.getReadCount(), 0.001 );
        assertEquals( 399.333, normaReadCount4.getReadCount(), 0.001 );
        assertEquals( 899.333, normaReadCount5.getReadCount(), 0.001 );
    }


}
