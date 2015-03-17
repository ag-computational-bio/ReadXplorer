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
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Handles all steps necessary for calling GASV to predict genome
 * rearrangements.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class GASVCaller implements Runnable {

    private final File bamFile;
    private final ParametersBamToGASV bamToGASVParams;
    private final ParametersGASVMain gasvMainParams;

    private final PersistentReference reference;
    private String chromNamesFileName;
    private ProgressHandle progressHandle;


    /**
     * Handles all steps necessary for calling GASV to predict genome
     * rearrangements.
     * <p>
     * @param reference       The reference whose tracks are analyzed here.
     * @param bamFile         The bam file to analyze
     * @param bamToGASVParams BamToGASV parameter set to apply.
     * @param gasvMainParams  GASVMain parameter set to apply.
     */
    @NbBundle.Messages( { "ProgressName=Storing chromosome names in file..." } )
    public GASVCaller( PersistentReference reference, File bamFile, ParametersBamToGASV bamToGASVParams, ParametersGASVMain gasvMainParams ) {
        this.reference = reference;
        this.bamFile = bamFile;
        this.bamToGASVParams = bamToGASVParams;
        this.gasvMainParams = gasvMainParams;
        progressHandle = ProgressHandleFactory.createHandle( Bundle.ProgressName() );
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
    public void callGASV() {
//        Algorithm:
//        1. create chromosome naming file as references will most certainly contain other names than numbers
//        2. Call GASV
        createChromosomeNamingFile( reference );
        runBamToGASV( bamFile );
        runGASVMain( bamFile.getAbsolutePath() + ".gasv.in" );

    }


    /**
     * Creates a chromosome naming file of the form "chr-name chr-id". E.g.:
     * <br/>
     * <br/>PAO1 1
     * <br/>plasmidX 2
     * <p>
     * @param reference The reference whose tracks are analyzed here.
     */
    @NbBundle.Messages( { "Error=An error occured during the file saving process.",
                          "SuccessMsg=Chromosome names successfully stored in ",
                          "SuccessHeader=Success" } )
    private void createChromosomeNamingFile( PersistentReference reference ) {
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        String dbLocation = projectConnector.getDBLocation();
        String refName = reference.getName();
        chromNamesFileName = new File( dbLocation ).getParent().concat( "\\" + refName.concat( "-seqNames-gasv.txt" ) );

        final String chromNamesString = createChromNamesString( reference );
        progressHandle.start();

        //Note that file is overwritten every time!
        try( final BufferedWriter outputWriter = new BufferedWriter( new FileWriter( chromNamesFileName ) ); ) {
            outputWriter.write( chromNamesString );
            NotificationDisplayer.getDefault().notify( Bundle.SuccessHeader(), new ImageIcon(),
                                                       Bundle.SuccessMsg() + chromNamesFileName, null );
        } catch( IOException | MissingResourceException | HeadlessException e ) {
            JOptionPane.showMessageDialog( new JPanel(), Bundle.Error() + e.getMessage() );
        }
        progressHandle.finish();
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
    private void runBamToGASV( File bamFile ) {
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
        BAMToGASV.main( gasvArgs ); //TODO: sysos have to be redirected!
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
    private void runGASVMain( String gasvInputFileName ) {
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
        } catch( IOException ex ) { //TODO: Correct error handling
            Exceptions.printStackTrace( ex );
        } catch( CloneNotSupportedException ex ) {
            Exceptions.printStackTrace( ex );
        } catch( NullPointerException ex ) {
            Exceptions.printStackTrace( ex );
        }
    }


}
