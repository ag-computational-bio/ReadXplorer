
package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.GnuR;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.PackageNotLoadableException;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.ProcessingLog;
import de.cebitec.readxplorer.transcriptionanalyses.gnur.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.rosuda.REngine.Rserve.RserveException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 * @author patrick
 */
@Ignore
public class BaySeqTest {

    private static GnuR instance;
//    private static Process process;
//
//    private static Thread err = null;
//    private static Thread out = null;


    /**
     * Set up connection to Rserve.
     *
     * @throws org.rosuda.REngine.Rserve.RserveException
     */
    @BeforeClass
    public static void setUpClass() throws RserveException {
        instance = new GnuR( "localhost", 6311, false, new ProcessingLog() );
    }


    @AfterClass
    public static void tearDownClass() throws RserveException, IOException {
//        instance.shutdown();
    }


    /**
     * Test of process method, of class BaySeq.
     *
     * @throws
     * de.cebitec.readxplorer.transcriptionanalyses.gnur.PackageNotLoadableException
     * @throws
     * de.cebitec.readxplorer.transcriptionanalyses.gnur.UnknownGnuRException
     * @throws org.rosuda.REngine.Rserve.RserveException
     * @throws java.io.IOException
     */
    @Test
    public void testProcess() throws PackageNotLoadableException, IllegalStateException, UnknownGnuRException, RserveException, IOException {
        int numberOfTracks = 6;
        File saveFile = null;
        List<Group> groups = new ArrayList<>();
        groups.add( new Group( new int[]{ 1, 1, 1, 1, 1, 1 }, "A,A,A,A,A,A" ) );
        groups.add( new Group( new int[]{ 1, 1, 1, 2, 2, 2 }, "A,A,A,B,B,B" ) );
        groups.add( new Group( new int[]{ 1, 1, 2, 2, 3, 3 }, "A,A,B,B,C,C" ) );
        groups.add( new Group( new int[]{ 1, 2, 3, 4, 5, 6 }, "A,B,C,D,E,F" ) );
        BaySeqAnalysisData analysisData = Mockito.spy( new BaySeqAnalysisData( numberOfTracks, groups, new int[]{ 1, 1, 1, 2, 2, 2 }, new ProcessingLog() ) );
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
        BaySeq baySeq = new BaySeq();
        List<ResultDeAnalysis> result = baySeq.process( analysisData, numberOfFeatures, numberOfTracks, saveFile, instance );

        assertEquals( "Should only be one result table", groups.size() * 2, result.size() );
        ResultDeAnalysis resultTable = result.get( 0 );
        List<Object> columns = resultTable.getColnames();
        int likelihoodColumn = 0;
        while( likelihoodColumn < columns.size() ) {
            if( columns.get( likelihoodColumn ).equals( "Likelihood" ) ) {
                break;
            }
            likelihoodColumn++;
        }
        assertTrue( "Result table should contain Likelihood column", likelihoodColumn < columns.size() );

        int locusColumn = 0;
        while( locusColumn < columns.size() ) {
            if( columns.get( locusColumn ).equals( "locus" ) ) {
                break;
            }
            locusColumn++;
        }
        Set<Object> resultLocusSet = new HashSet<>();
        for( List<Object> line : resultTable.getTableContents() ) {
            resultLocusSet.add(((PersistentFeature) line.get( locusColumn )).getLocus());
        }
        for( PersistentFeature feature : featureList ) {
            if( !resultLocusSet.contains( feature.getLocus() ) ) {
                fail( "Not all Loci were found!" );
            }
        }
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
