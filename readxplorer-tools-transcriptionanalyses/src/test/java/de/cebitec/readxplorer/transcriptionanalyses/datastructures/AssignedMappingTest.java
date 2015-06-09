/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
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
 * Tests the utility methods of the {@link AssignedMapping} class.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class AssignedMappingTest {

    private static Mapping mapping2;

    private static PersistentFeature feature4;
    private static PersistentFeature feature5;
    private static PersistentFeature feature6;
    private static PersistentFeature feature9;

    private static NormalizedReadCount normaReadCount2;
    private static NormalizedReadCount normaReadCount3;
    private static NormalizedReadCount normaReadCount4;
    private static NormalizedReadCount normaReadCount5;

    private static Map<Integer, NormalizedReadCount> featureReadCount;


    /**
     * Tests the utility methods of the {@link AssignedMapping} class.
     */
    public AssignedMappingTest() {
    }


    @BeforeClass
    public static void setUpClass() {
        mapping2 = new Mapping( 1500, 1600, false );

        feature4 = new PersistentFeature( 4, 1, "", "", "", "", 1200, 1550, false, FeatureType.CDS, "feat4" );
        feature5 = new PersistentFeature( 5, 1, "", "", "", "", 1450, 1850, false, FeatureType.CDS, "feat5" );
        feature6 = new PersistentFeature( 6, 1, "", "", "", "", 1490, 1650, false, FeatureType.CDS, "feat6" );
        feature9 = new PersistentFeature( 9, 1, "", "", "", "", 1499, 1850, false, FeatureType.CDS, "feat9" );

        normaReadCount2 = new NormalizedReadCount( feature4, 0, 0, 200, 1 );
        normaReadCount3 = new NormalizedReadCount( feature5, 0, 0, 800, 1 );
        normaReadCount4 = new NormalizedReadCount( feature6, 0, 0, 400, 1 );
        normaReadCount5 = new NormalizedReadCount( feature9, 0, 0, 900, 1 );

        featureReadCount = new HashMap<>();

        featureReadCount.put( normaReadCount2.getFeature().getId(), normaReadCount2 );
        featureReadCount.put( normaReadCount3.getFeature().getId(), normaReadCount3 );
        featureReadCount.put( normaReadCount4.getFeature().getId(), normaReadCount4 );
        featureReadCount.put( normaReadCount5.getFeature().getId(), normaReadCount5 );

        normaReadCount2.setReadLengthSum( 30000 );
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
     * Test of checkCountDecrease method, of class AssignedMapping.
     */
    @Test
    public void testCheckCountDecrease() {
        System.out.println( "checkCountDecrease" );
        AssignedMapping assignedMapping2 = new UnionFractionMapping( mapping2 );

        // - rev strand
        // 1. Fst added mapping - does it work, is it added?
        boolean countIt = assignedMapping2.checkAssignment( feature4.getStart(), feature4.getStop(), feature4 );
        // 2. Second feature of same type added:
        // 1. Mapping is overlapping
        boolean countIt2 = assignedMapping2.checkAssignment( feature5.getStart(), feature5.getStop(), feature5 );
        // 2. Mapping is contained in both
        boolean countIt3 = assignedMapping2.checkAssignment( feature6.getStart(), feature6.getStop(), feature6 );

        assertTrue( assignedMapping2.getRevRemoveList().size() == 1 );

        assignedMapping2.checkCountDecrease( featureReadCount );

        assertEquals( 199, featureReadCount.get( feature4.getId() ).getReadCount(), 0.0 );
        assertEquals( 29899, featureReadCount.get( feature4.getId() ).getReadLengthSum(), 0.0 );
    }


}
