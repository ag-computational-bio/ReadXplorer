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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.NormalizedReadCount;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Tests the normalization formulars for RPKM and TPM with the examples from
 * https://haroldpimentel.wordpress.com/2014/05/08/what-the-fpkm-a-review-rna-seq-expression-units/
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class NormalizationFormulasTest {


    private static final List<Double> FEATURE_READ_LIST = new ArrayList<>();
    private static final List<Integer> FEAT_LENGTH_LIST = new ArrayList<>();
    private static final List<Double> EFF_FEAT_LENGTH_LIST = new ArrayList<>();
    private static final List<PersistentFeature> FEAT_LIST = new ArrayList<>();
    private static final List<NormalizedReadCount> NORMALIZED_RC_LIST = new ArrayList<>();


    public NormalizationFormulasTest() {
    }


    @BeforeClass
    public static void setUpClass() {
        double meanReadLength = 203.7;
        FEATURE_READ_LIST.add( 4250.0 );
        FEATURE_READ_LIST.add( 3300.0 );
        FEATURE_READ_LIST.add( 200.0 );
        FEATURE_READ_LIST.add( 1750.0 );
        FEATURE_READ_LIST.add( 50.0 );
        FEATURE_READ_LIST.add( 0.0 );
        FEAT_LENGTH_LIST.add( 900 );
        FEAT_LENGTH_LIST.add( 1020 );
        FEAT_LENGTH_LIST.add( 2000 );
        FEAT_LENGTH_LIST.add( 770 );
        FEAT_LENGTH_LIST.add( 3000 );
        FEAT_LENGTH_LIST.add( 1777 );
        int start = 1;
        int featSpacing = 500;
        for( int i = 0; i < FEATURE_READ_LIST.size(); i++ ) {
            double readCount = FEATURE_READ_LIST.get( i );
            int length = FEAT_LENGTH_LIST.get( i );
            EFF_FEAT_LENGTH_LIST.add( length - meanReadLength );
            int stop = start + length - 1;
            FEAT_LIST.add( new PersistentFeature( i, 1, "", "", "", "", start, stop, true, FeatureType.CDS, "feat" + i ) );
            NORMALIZED_RC_LIST.add( new NormalizedReadCount( FEAT_LIST.get( i ), 0, 0, (int) readCount, 1 ) );
            NORMALIZED_RC_LIST.get( i ).storeEffectiveFeatureLength( EFF_FEAT_LENGTH_LIST.get( i ) );
            start += stop + featSpacing;
        }
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
     * Test of calculateRpkm method, of class NormalizationFormulas.
     */
    @Test
    public void testCalculateRpkm() {
        System.out.println( "calculateRpkm" );
        List<Double> resultList = new ArrayList<>();
        double totalMappedReads = 9550;

        resultList.add( 639129.9411323728 ); //with logs: 638213.363
        resultList.add( 423312.1869654481 ); //422794.247
        resultList.add( 11658.636295141876 ); //11652.150
        resultList.add( 323584.8018690258 ); //323014.407
        resultList.add( 1872.3320438582548 ); //1871.663
        resultList.add( 0.0 );

        for( int i = 0; i < FEATURE_READ_LIST.size(); i++ ) {
            double result = NormalizationFormulas.calculateRpkm( FEATURE_READ_LIST.get( i ), totalMappedReads, EFF_FEAT_LENGTH_LIST.get( i ) );
            assertEquals( resultList.get( i ), result, 0.0001 );
        }
    }


    /**
     * Test of calculateTpm method, of class NormalizationFormulas.
     */
    @Test
    public void testCalculateTpm() {
        System.out.println( "calculateTpm" );
        List<Double> resultList = new ArrayList<>();
        double normalizationSum = 13.36577793;

        resultList.add( 456665.5955 );
        resultList.add( 302461.3612 );
        resultList.add( 8330.227931 );
        resultList.add( 231205.0129 );
        resultList.add( 1337.802492 );
        resultList.add( 0.0 );

        for( int i = 0; i < FEATURE_READ_LIST.size(); i++ ) {
            double result = NormalizationFormulas.calculateTpm( FEATURE_READ_LIST.get( i ), EFF_FEAT_LENGTH_LIST.get( i ), normalizationSum );
            assertEquals( resultList.get( i ), result, 0.0001 );
        }
    }


    /**
     * Test of calculateNormalizationSums method, of class NormalizationFormulas.
     */
    @Test
    public void testCalculateNormalizationSums() {
        System.out.println( "calculateNormalizationSums" );
        Map<Integer, NormalizedReadCount> featureReadCount = new HashMap<>();
        for( NormalizedReadCount normRC : NORMALIZED_RC_LIST ) {
            featureReadCount.put( normRC.getFeature().getId(), normRC );
        }
        Map<FeatureType, Double> normalizationSumMap = new HashMap<>();
        normalizationSumMap.put( FeatureType.CDS, 0.0 );

        double expNormalizationSum = 13.36577793;
        NormalizationFormulas.calculateNormalizationSums( featureReadCount, normalizationSumMap );

        assertEquals( expNormalizationSum, normalizationSumMap.get( FeatureType.CDS), 0.0001);
    }

}
