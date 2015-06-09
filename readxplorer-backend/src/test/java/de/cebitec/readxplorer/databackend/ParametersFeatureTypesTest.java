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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.EnumSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Test of utility methods from the {@link ParametersFeatureTypes}.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersFeatureTypesTest {

    private static PersistentFeature feature;
    private static PersistentFeature feature2;
    private static ParametersFeatureTypes paramsFeatTypes;

    /**
     * Test of utility methods from the {@link ParametersFeatureTypes}.
     */
    public ParametersFeatureTypesTest() {
        feature = new PersistentFeature( 1, 1, "", "", "", "", 1000, 2000, true, FeatureType.CDS, "feat1" );
        feature2 = new PersistentFeature( 2, 1, "", "", "", "", 3000, 4000, false, FeatureType.CDS, "feat2" );
        paramsFeatTypes = new ParametersFeatureTypes( EnumSet.allOf( FeatureType.class ), 10, 20 );
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
     * Test of calcFeatureStartOffset method, of class ParametersFeatureTypes.
     */
    @Test
    public void testCalcFeatureStartOffset() {
        System.out.println( "calcFeatureStartOffset" );
        int expResult = 990;
        int expResult2 = 2980;
        int result = paramsFeatTypes.calcFeatureStartOffset( feature );
        int result2 = paramsFeatTypes.calcFeatureStartOffset( feature2 );
        assertEquals( expResult, result );
        assertEquals( expResult2, result2 );
    }


    /**
     * Test of calcFeatureStopOffset method, of class ParametersFeatureTypes.
     */
    @Test
    public void testCalcFeatureStopOffset() {
        System.out.println( "calcFeatureStopOffset" );
        int expResult = 2020;
        int expResult2 = 4010;
        int result = paramsFeatTypes.calcFeatureStopOffset( feature );
        int result2 = paramsFeatTypes.calcFeatureStopOffset( feature2 );
        assertEquals( expResult, result );
        assertEquals( expResult2, result2 );
    }


}
