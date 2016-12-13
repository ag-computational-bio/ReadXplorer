/*
 * Copyright (C) 2016 Patrick Blumenkamp
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.GnuR;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.PackageNotLoadableException;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.ProcessingLog;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openide.util.Exceptions;
import org.rosuda.REngine.Rserve.RserveException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 *
 * @author patrick
 */
public class DeSeqTest {

    private static GnuR instance;


    /**
     * Set up connection to Rserve.
     *
     * @throws org.rosuda.REngine.Rserve.RserveException
     */
    @BeforeClass
    public static void setUpClass() throws RserveException  {
            instance = new GnuR( "localhost", 6311, false, new ProcessingLog() );
    }


    @AfterClass
    public static void tearDownClass() throws RserveException, IOException {
//        instance.shutdown();
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of process method, of class DeSeq2.
     *
     * @throws de.cebitec.readxplorer.transcriptionanalyses.gnur.PackageNotLoadableException
     * @throws de.cebitec.readxplorer.transcriptionanalyses.gnur.UnknownGnuRException
     * @throws org.rosuda.REngine.Rserve.RserveException
     * @throws java.io.IOException
     */
    @Test
    public void testProcess() throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        int numberOfTracks = 6;
        File saveFile = null;
        Map<String, String[]> design = new HashMap<>();
        design.put( "twoConds", new String[]{ "A", "A", "A", "B", "B", "B" } );
        DeSeqAnalysisData analysisData = Mockito.spy( new DeSeqAnalysisData( numberOfTracks, design, false, null, null, false, new ProcessingLog() ) );
        Mockito.doReturn( new String[]{ "1", "2", "3", "4", "5", "6" } ).when( analysisData ).getTrackDescriptions();
        analysisData.addCountDataForTrack( new int[]{ 500, 2000, 300, 600, 1000, 25000 } );
        analysisData.addCountDataForTrack( new int[]{ 499, 2100, 280, 550, 1100, 26000 } );
        analysisData.addCountDataForTrack( new int[]{ 550, 1920, 350, 633, 1050, 26200 } );
        analysisData.addCountDataForTrack( new int[]{ 400, 3500, 350, 400, 50000, 35000 } );
        analysisData.addCountDataForTrack( new int[]{ 350, 3200, 360, 600, 55000, 33000 } );
        analysisData.addCountDataForTrack( new int[]{ 410, 2500, 330, 500, 49000, 40000 } );
        List<PersistentFeature> featureList = new ArrayList<>( 6 );
        featureList.add( new PersistentFeature( 1, 1, "", "", "locusA", "", 10, 110, true, FeatureType.CDS, "GeneA" ) );
        featureList.add( new PersistentFeature( 2, 1, "", "", "locusB", "", 300, 500, true, FeatureType.CDS, "GeneB" ) );
        featureList.add( new PersistentFeature( 3, 1, "", "", "locusC", "", 650, 950, true, FeatureType.CDS, "GeneC" ) );
        featureList.add( new PersistentFeature( 4, 1, "", "", "locusD", "", 1001, 1500, true, FeatureType.CDS, "GeneD" ) );
        featureList.add( new PersistentFeature( 5, 1, "", "", "locusE", "", 2225, 3000, true, FeatureType.CDS, "GeneE" ) );
        featureList.add( new PersistentFeature( 6, 1, "", "", "locusF", "", 4500, 5000, true, FeatureType.CDS, "GeneF" ) );
        analysisData.setFeatures( featureList );
        int numberOfFeatures = featureList.size();
        DeSeq deseq = new DeSeq();
        List<ResultDeAnalysis> result = deseq.process( analysisData, numberOfFeatures, numberOfTracks, saveFile, instance );

        assertEquals( "Should be one result table", 1, result.size() );
        ResultDeAnalysis resultTable = result.get( 0 );     //all tables contain the same results
        List<Object> columns = resultTable.getColnames();
        int baseMeanColumn = 0;
        while( baseMeanColumn < columns.size() ) {
            if( columns.get( baseMeanColumn ).equals( "baseMean" ) ) {
                break;
            }
            baseMeanColumn++;
        }
        assertTrue( "Result table should contain baseMean column", baseMeanColumn < columns.size() );
        boolean baseMeanNowhereZero = true;
        for( List<Object> col : resultTable.getTableContents() ) {
            if( (Double) col.get( baseMeanColumn ) == 0 ) {
                baseMeanNowhereZero = false;
                break;
            }
        }
        assertTrue( "BaseMean should be unequal zero in every row", baseMeanNowhereZero );
    }


//    private static volatile boolean exiting = false;
//
//
//    private final static class RStreamReader implements Runnable {
//
//        private final InputStream err;
//
//
//        RStreamReader( InputStream in ) {
//            err = in;
//        }
//
//
//        @Override
//        public void run() {
//            try( BufferedReader in = new BufferedReader( new InputStreamReader( err ) ) ) {
//                String line;
//                while( !exiting && in.ready() && ((line = in.readLine()) != null) ) {
//                    if( !line.trim().isEmpty() ) {
//                        Logger.getLogger( GnuR.class.getName() ).log( Level.INFO, "R: {0}", line );
//                    } else {
//                        try {
//                            Thread.sleep( 50 );
//                        } catch( InterruptedException ex ) {
//                        }
//                    }
//                }
//            } catch( IOException ex ) {
//                if( !exiting ) {
//                    Logger.getLogger( GnuR.class.getName() ).log( Level.SEVERE, null, ex );
//                }
//            }
//        }
//
//
//    }

}
