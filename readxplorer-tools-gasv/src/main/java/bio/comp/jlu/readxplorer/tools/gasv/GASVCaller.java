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

package bio.comp.jlu.readxplorer.tools.gasv;

import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import gasv.bamtogasv.BAMToGASV;
import gasv.main.GASVMain;
import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;


/**
 * Handles all steps necessary for calling GASV to predict genome
 * rearrangements.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@NbBundle.Messages( "GASV.output.name=GASV output" )
public class GASVCaller implements Runnable {

    public static final InputOutput IO = IOProvider.getDefault().getIO( Bundle.GASV_output_name(), false );

    private final PersistentReference reference;
    private final TrackConnector trackConnector;
    private final ParametersBamToGASV bamToGASVParams;
    private final ParametersGASVMain gasvMainParams;
    private final DataVisualisationI parent;
    private final ProgressHandle analysisProgressHandle;
    private final ProgressHandle storeChromsProgressHandle;

    private String chromNamesFileName;


    /**
     * Handles all steps necessary for calling GASV to predict genome
     * rearrangements.
     * <p>
     * @param reference       The reference whose tracks are analyzed here.
     * @param trackConnector  The track connector for the bam file to analyze
     * @param bamToGASVParams BamToGASV parameter set to apply.
     * @param gasvMainParams  GASVMain parameter set to apply.
     * @param parent          The parent to notify when the calculation has
     *                        finished that it can visualize the results.
     * @param progressHandle  The progress handle of the analysis to update when
     *                        intermediate steps have finished.
     */
    @NbBundle.Messages( { "CallerProgressName=Storing chromosome names in file..." } )
    public GASVCaller( PersistentReference reference, TrackConnector trackConnector, ParametersBamToGASV bamToGASVParams,
                       ParametersGASVMain gasvMainParams,
                       DataVisualisationI parent,
                       ProgressHandle progressHandle ) {
        this.reference = reference;
        this.trackConnector = trackConnector;
        this.bamToGASVParams = bamToGASVParams;
        this.gasvMainParams = gasvMainParams;
        this.parent = parent;
        this.storeChromsProgressHandle = ProgressHandleFactory.createHandle( Bundle.CallerProgressName() );
        this.analysisProgressHandle = progressHandle;
    }


    /**
     * Calls GASV.
     */
    @Override
    public void run() {
        callGASV();
    }


    /**
     * Executes all necessary steps to prepare and run GASV for the detection of
     * genome rearrangements within read mapping data.
     */
    @NbBundle.Messages( { "# {0} - Track File Name",
                          "ErrorPairedTrack=The selected track {0} is not a read pair track and cannot be analyzed with GASV!" } )
    public void callGASV() {

        IO.getOut().flush(); //delete data from previous GASV runs
        IO.select();
        //check if a paired file has been passed:
        if( trackConnector.isReadPairTrack() ) {
            createChromosomeNamingFile( reference );
            runBamToGASV( trackConnector.getTrackFile() );
            runGASVMain( trackConnector.getTrackFile().getAbsolutePath() + ".gasv.in" );
        } else {
            IO.getOut().println( Bundle.ErrorPairedTrack( trackConnector.getTrackFile().getName() ) );
            parent.showData( "noReadPairTrack" );
        }

    }


    /**
     * Creates a chromosome naming file of the form "chr-name chr-id". E.g.:
     * <br/>
     * <br/>PAO1 1
     * <br/>plasmidX 2
     * <p>
     * @param reference The reference whose tracks are analyzed here.
     */
    @NbBundle.Messages( { "Error=An error occurred during the file saving process.",
                          "SuccessMsg=Chromosome names successfully stored in ",
                          "SuccessHeader=Success",
                          "ChromFileWritingStart=Starting to write chromosome naming file for GASV...",
                          "ChromFileWritingFinished=Finished writing chromosome naming file for GASV." } )
    private void createChromosomeNamingFile( PersistentReference reference ) {
        IO.getOut().println( Bundle.ChromFileWritingStart() );
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        String dbLocation = projectConnector.getDbLocation();
        String refName = reference.getName();
        chromNamesFileName = new File( dbLocation ).getParent().concat( "\\" + refName.concat( "-seqNames-gasv.txt" ) );

        final String chromNamesString = createChromNamesString( reference );
        storeChromsProgressHandle.start();

        //Note that file is overwritten every time!
        try( final BufferedWriter outputWriter = new BufferedWriter( new FileWriter( chromNamesFileName ) ); ) {
            outputWriter.write( chromNamesString );
            NotificationDisplayer.getDefault().notify( Bundle.SuccessHeader(), new ImageIcon(),
                                                       Bundle.SuccessMsg() + chromNamesFileName, null );
        } catch( IOException | MissingResourceException | HeadlessException e ) {
            JOptionPane.showMessageDialog( new JPanel(), Bundle.Error() + e.getMessage() );
        }
        storeChromsProgressHandle.finish();
        IO.getOut().println( Bundle.ChromFileWritingFinished() );
    }


    /**
     * Creates a chromosome names string with one chromosome per line of the
     * form "chr-name chr-id". E.g.:
     * <br/>
     * <br/>PAO1 1
     * <br/>plasmidX 2
     * <p>
     * @param reference The reference connector of the reference whose tracks
     *                  are analyzed here.
     * <p>
     * @return A String with one chromosome name and a unique id per line for
     *         all chromosomes of the given reference genome.
     */
    private String createChromNamesString( PersistentReference reference ) {
        StringBuilder chromNamesBuilder = new StringBuilder( 20 );
        Map<Integer, PersistentChromosome> chromosomes = reference.getChromosomes();
        int chromNo = 1;
        for( PersistentChromosome chrom : chromosomes.values() ) {
            chromNamesBuilder.append( chrom.getName() ).append( " " ).append( chromNo++ ).append( "\n" );
        }

        return chromNamesBuilder.toString();
    }


    /**
     * Runs BamToGASV, the first step of the GASV pipeline.
     * <p>
     * @param bamFile The bam file to analyze
     */
    @NbBundle.Messages( { "ProgressBamToGASV=Running BamToGASV step (1. of 2 steps)" } )
    private void runBamToGASV( File bamFile ) {
        analysisProgressHandle.progress( Bundle.ProgressBamToGASV() );
        String[] gasvArgs = { bamFile.getAbsolutePath(),
                              "-LIBRARY_SEPARATED",
                              bamToGASVParams.isLibrarySeparated() ? "sep" : "all",
                              "-MAPPING_QUALITY",
                              String.valueOf( bamToGASVParams.getMinMappingQuality() ),
                              "-CUTOFF_LMINLMAX",
                              calcFragmentBoundsMethod( bamToGASVParams.getFragmentBoundsMethod() ),
                              "-CHROMOSOME_NAMING",
                              chromNamesFileName,
                              "-USE_NUMBER_READS",
                              "1000000",
                              "-PROPER_LENGTH",
                              String.valueOf( bamToGASVParams.getMaxPairLength() ),
                              "-PLATFORM",
                              bamToGASVParams.getPlatform() ? "SOLiD" : "Illumina",
                              "-WRITE_CONCORDANT",
                              bamToGASVParams.isWriteConcordantPairs() ? "true" : "false",
                              "-WRITE_LOWQ",
                              bamToGASVParams.isWriteLowQualityPairs() ? "true" : "false",
                              "-VALIDATION_STRINGENCY",
                              bamToGASVParams.getSamValidationStringency() };
        BAMToGASV.main( gasvArgs );
    }


    /**
     * Constructs the correctly formatted input string for the fragment bounds
     * method parameter of GASV.
     * <p>
     * @param fragmentBoundsMethod The fragment bounds method enumeration
     *                             parameter
     * <p>
     * @return The correctly formatted input string for the fragment bounds
     *         method parameter of GASV.
     */
    private String calcFragmentBoundsMethod( String fragmentBoundsMethod ) {
        String methodString = fragmentBoundsMethod + "=";
        switch( fragmentBoundsMethod ) {
            case ParametersBamToGASV.FB_METHOD_SD:
                methodString += String.valueOf( bamToGASVParams.getDistSDValue() );
                break;
            case ParametersBamToGASV.FB_METHOD_EXACT:
                methodString += bamToGASVParams.getDistExactValue();
                break;
            case ParametersBamToGASV.FB_METHOD_FILE:
                methodString += bamToGASVParams.getDistFile();
                break;
            case ParametersBamToGASV.FB_METHOD_PCT: //fallthrough to default
            default:
                methodString += String.valueOf( bamToGASVParams.getDistPCTValue() ) + "%";
        }

        return methodString;
    }


    /**
     * Runs GASVMain, the second and final step of the GASV pipeline.
     * <p>
     * @param gasvInputFileName The input file for GASVMain generated with
     *                          BamToGASV.
     */
    @NbBundle.Messages( { "ProgressGASVMain=Running GASVMain step (2. of 2 steps)" } )
    private void runGASVMain( String gasvInputFileName ) {
        analysisProgressHandle.progress( Bundle.ProgressGASVMain() );
        List<String> gasvArgsList = new ArrayList<>();
        gasvArgsList.add( "--batch" );
        if( gasvMainParams.isHeaderless() ) {
            gasvArgsList.add( "--nohead" );
        }
        if( gasvMainParams.isVerbose() ) {
            gasvArgsList.add( "--verbose" );
        }
        if( gasvMainParams.isDebug() ) {
            gasvArgsList.add( "--debug" );
        }
        if( gasvMainParams.isFast() ) {
            gasvArgsList.add( "--fast" );
        }
        if( gasvMainParams.getMinClusterSize() > 0 ) {
            gasvArgsList.add( "--minClusterSize" );
            gasvArgsList.add( String.valueOf( gasvMainParams.getMinClusterSize() ) );
        }
        if( gasvMainParams.getMaxClusterSize() > 0 ) {
            gasvArgsList.add( "--maxClusterSize" );
            gasvArgsList.add( String.valueOf( gasvMainParams.getMaxClusterSize() ) );
        }
        if( gasvMainParams.getMaxCliqueSize() > 0 ) {
            gasvArgsList.add( "--maxCliqueSize" );
            gasvArgsList.add( String.valueOf( gasvMainParams.getMaxCliqueSize() ) );
        }
        if( gasvMainParams.getMaxReadPairs() > 0 ) {
            gasvArgsList.add( "--maxPairedEndsPerWin" );
            gasvArgsList.add( String.valueOf( gasvMainParams.getMaxReadPairs() ) );
        }
        if( gasvMainParams.isMaxSubClusters() ) {
            gasvArgsList.add( "--maximal" );
        }
        gasvArgsList.add( "--numChrom" );
        gasvArgsList.add( String.valueOf( reference.getNoChromosomes() ) );
        gasvArgsList.add( "--output" );
        gasvArgsList.add( gasvMainParams.getOutputType() );
        if( gasvMainParams.isNonreciprocal() ) {
            gasvArgsList.add( "--nonreciprocal" );
        }
        gasvArgsList.add( gasvInputFileName );

        String[] gasvArgs = new String[0];
        try {
            GASVMain.main( gasvArgsList.toArray( gasvArgs ) );
        } catch( IOException | CloneNotSupportedException | NullPointerException ex ) {
            IO.getOut().println( ex.getMessage() );
        }

        parent.showData( "done" );
    }


}
