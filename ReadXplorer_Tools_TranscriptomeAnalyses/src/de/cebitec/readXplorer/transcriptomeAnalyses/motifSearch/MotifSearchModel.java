
package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;


/**
 * This Class represents the whole model behind the motif search in
 * transcriptomeAnalyses module.
 *
 * @author jritter
 */
public class MotifSearchModel implements Observer {

    private TreeMap<String, String> upstreamRegionsInHash;
    private TreeMap<String, Integer> contributedSequencesWithShift;
    private File logoMinus10, logoMinus35, logoRbs, minus10Input, minus35Input,
            bioProspOutMinus10, bioProspOutMinus35, info, rbsBioProspectorInput,
            rbsBioProsFirstHit;
    private final PersistentReference ref;
    private List<String> upstreamRegions;
    private float meanMinus10SpacerToTSS, meanMinus35SpacerToMinus10,
            meanSpacerLengthOfRBSMotif;
    private final ProgressHandle progressHandlePromotorAnalysis,
            progressHandleRbsAnalysis;
    private final String handlerTitlePromotorAnalysis, handlerTitleRBSAnalysis;
    private List<String> minus10AnalysisStrings, minus35AnalysisStrings;
    private float contributingCitesForMinus10Motif, contributingCitesForMinus35Motif,
            contributingCitesForRbsMotif;
    private int alternativeSpacer;
    private TreeMap<String, Integer> minus10MotifStarts, minus35MotifStarts, rbsStarts;
    private JTextPane regionsRelToTLSTextPane, regionsForMotifSearch,
            regionOfIntrestMinus10, regionOfIntrestMinus35;
    private JLabel rbsLogoLabel, minus10logoLabel, minus35LogoLabel;
    private TreeMap<String, Integer> idsToMinus10Shifts, idsToMinus35Shifts;
    private StyledDocument coloredPromotorRegions;
    private TreeMap<String, TranscriptionStart> locusToTSSs;


    /**
     * Provides all methods for the automated motif search analysis.
     *
     * @param refViewer
     */
    public MotifSearchModel( PersistentReference reference ) {
        this.ref = reference;
        this.handlerTitlePromotorAnalysis = "Processing promotor analysis";
        this.handlerTitleRBSAnalysis = "Processing rbs analysis";
        this.progressHandlePromotorAnalysis = ProgressHandleFactory.createHandle( handlerTitlePromotorAnalysis );
        this.progressHandleRbsAnalysis = ProgressHandleFactory.createHandle( handlerTitleRBSAnalysis );
    }


    /**
     * This method provides a motif search for cosensus regions in 5'-UTR,
     * usually the -35 and -10 region.
     *
     * @param params instance of PromotorSearchParameters.
     * @param starts list of transcriptions start sites from which the 5'-UTR
     *               have to be analysed
     */
    public boolean utrPromotorAnalysis( PromotorSearchParameters params, List<TranscriptionStart> starts ) {
        boolean success = true;
        this.alternativeSpacer = params.getAlternativeSpacer();
        minus10MotifStarts = new TreeMap<>();
        minus35MotifStarts = new TreeMap<>();
        this.minus10AnalysisStrings = new ArrayList<>();
        this.minus35AnalysisStrings = new ArrayList<>();
        this.progressHandlePromotorAnalysis.progress( "processing promotor analysis ...", 20 );

        regionOfIntrestMinus10 = new JTextPane();
        regionOfIntrestMinus35 = new JTextPane();

        Path workingDirPath = null;
        Path bioProspectorOutMinus10Path = null;
        Path bioProspectorOutMinus35Path = null;
        Path minus10InputPath = null;
        Path minus35InputPath = null;
        Path infoFilePath = null;
        try {
            workingDirPath = Files.createTempDirectory( "promotorAnalysis_" );
            bioProspectorOutMinus10Path = Files.createTempFile( workingDirPath, "promotorAnalysis_", "bioProspectorOutMinus10.fna" );
            bioProspectorOutMinus35Path = Files.createTempFile( workingDirPath, "promotorAnalysis_", "bioProspectorOutMinus35.fna" );
            minus10InputPath = Files.createTempFile( workingDirPath, "promotorAnalysis_", "inputBioProspectorMinus10.fna" );
            minus35InputPath = Files.createTempFile( workingDirPath, "promotorAnalysis_", "inputBioProspectorMinus35.fna" );
            infoFilePath = Files.createTempFile( workingDirPath, "promotorAnalysis_", "info.txt" );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }

        File workingDir = workingDirPath.toFile();
        minus10Input = minus10InputPath.toFile();
        minus35Input = minus35InputPath.toFile();
        bioProspOutMinus10 = bioProspectorOutMinus10Path.toFile();
        bioProspOutMinus35 = bioProspectorOutMinus35Path.toFile();
//        logoMinus10 = new File(workingDir.getAbsolutePath() + "\\minusTenLogo");
//        logoMinus35 = new File(workingDir.getAbsolutePath() + "\\minus35Logo");
        info = infoFilePath.toFile();

        // 1. write all upstream subregions for -10 analysis
        writeSubRegionFor5UTRInFile(
                minus10Input, this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                null );

        // BioProspector search for -10 motif
        String posixPath = "/cygdrive/c";
        String sub = minus10Input.getAbsolutePath().substring( 2 );
        posixPath += sub.replaceAll( "\\\\", "/" );
        boolean successfulyBioProspRun = false;
        try {
            // 2. executing bioprospector and parse the best scored (first listed) Hits and write
            successfulyBioProspRun = this.executeBioProspector(
                    posixPath, bioProspOutMinus10, params.getMinusTenMotifWidth(), params.getNoOfTimesTrying(),
                    1, 1 );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
        if( successfulyBioProspRun ) {
            this.progressHandlePromotorAnalysis.progress( "Starting promotor analysis ...", 40 );

            idsToMinus10Shifts = new TreeMap<>();
            idsToMinus10Shifts.putAll( this.contributedSequencesWithShift );

            // 4. All sequences, which did not contain a motif in the first run for-10 motif serch
            // will be descard in the next step of the analysis of the -35 motif search
            writeSubRegionFor5UTRInFile(
                    minus35Input, this.upstreamRegions,
                    params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                    params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                    idsToMinus10Shifts );
//                alignmentshiftsOFMinusTenArray, regionOfIntrestMinus10, regionOfIntrestMinus35);

            posixPath = "/cygdrive/c";
            sub = minus35Input.getAbsolutePath().substring( 2 );
            posixPath += sub.replaceAll( "\\\\", "/" );

            try {
                // 5. So in this step we just processing subregions which had a hit
                // in the -10 region.
                this.executeBioProspector(
                        posixPath, bioProspOutMinus35, params.getMinus35MotifWidth(),
                        params.getNoOfTimesTrying(),
                        1, 1 );
            }
            catch( IOException ex ) {
                Exceptions.printStackTrace( ex );
            }

            progressHandlePromotorAnalysis.progress( "Starting promotor analysis ...", 60 );

            idsToMinus35Shifts = new TreeMap<>();
            idsToMinus35Shifts.putAll( this.contributedSequencesWithShift );

            int motifWidth10 = params.getMinusTenMotifWidth();
            int motifWidth35 = params.getMinus35MotifWidth();
            int seqWidthToAnalyzeMinus10 = params.getSequenceWidthToAnalyzeMinus10();
            int seqWidthToAnalyzeMinus35 = params.getSequenceWidthToAnalyzeMinus35();
            int shiftPosMinus10 = 0;
            int shiftPosMinus35 = 0;
            int index = 0;
            String header = "";
            Font font = new Font( Font.MONOSPACED, Font.PLAIN, 16 );

            for( String string : upstreamRegions ) {
                if( string.startsWith( ">" ) ) {
                    header = string.substring( 1, string.length() - 1 );
                    if( idsToMinus10Shifts.containsKey( header ) ) {
                        shiftPosMinus10 = idsToMinus10Shifts.get( header );
                    }
                    if( idsToMinus35Shifts.containsKey( header ) ) {
                        shiftPosMinus35 = idsToMinus35Shifts.get( header );
                    }
                    try {
                        regionOfIntrestMinus10.getStyledDocument().insertString( regionOfIntrestMinus10.getStyledDocument().getLength(), string, null );
                        regionOfIntrestMinus35.getStyledDocument().insertString( regionOfIntrestMinus35.getStyledDocument().getLength(), string, null );
                    }
                    catch( BadLocationException ex ) {
                        Exceptions.printStackTrace( ex );
                    }
                    index++;
                }
                else {
                    try {
                        regionOfIntrestMinus10.getStyledDocument().insertString( regionOfIntrestMinus10.getStyledDocument().getLength(), this.minus10AnalysisStrings.get( index - 1 ).toLowerCase(), null );
                        regionOfIntrestMinus35.getStyledDocument().insertString( regionOfIntrestMinus35.getStyledDocument().getLength(), this.minus35AnalysisStrings.get( index - 1 ).toLowerCase(), null );
                        if( idsToMinus10Shifts.containsKey( header ) ) {
                            colorSubstringsInStyledDocument( regionOfIntrestMinus10, font, regionOfIntrestMinus10.getStyledDocument().getLength() - 1 - seqWidthToAnalyzeMinus10 + shiftPosMinus10 - 1, motifWidth10, Color.RED );
                        }
                        if( idsToMinus35Shifts.containsKey( header ) ) {
                            colorSubstringsInStyledDocument( regionOfIntrestMinus35, font, regionOfIntrestMinus35.getStyledDocument().getLength() - 1 - seqWidthToAnalyzeMinus35 + shiftPosMinus35 - 1, motifWidth35, Color.BLUE );
                        }
                    }
                    catch( BadLocationException ex ) {
                        Exceptions.printStackTrace( ex );
                    }

                }
            }

            calcMotifStartsAndMeanSpacerLength( this.upstreamRegions, idsToMinus10Shifts, idsToMinus35Shifts, params );
            setMotifSearchResults( starts, this.minus10MotifStarts, this.minus35MotifStarts, params );

            progressHandlePromotorAnalysis.progress( "Starting promotor analysis ...", 70 );

            // Color -35 and -10 motifs in the pane with the whole urt region rel. to TSS
            coloredPromotorRegions = colorPromotorMotifRegions( this.upstreamRegions, this.minus10MotifStarts, this.minus35MotifStarts, params );

            progressHandlePromotorAnalysis.progress( "Starting promotor analysis ...", 80 );
            // generating Sequence Logos and adding them into Tabbedpane
            int logoStart10 = Math.round( this.meanMinus10SpacerToTSS + params.getMinusTenMotifWidth() );

            this.logoMinus10 = generateSeqLogo( 2.0, bioProspOutMinus10, workingDir.getAbsolutePath() + "\\minusTenLogo",
                                                "EPS", 8.0, -logoStart10, 15, true, true );
//            File logoMinus10_show_in_Panel = generateSeqLogo(2.0, bioProspOutMinus10, workingDir.getAbsolutePath() + "\\minusTenLogoPanel",
//                    "PNG", 8.0, -logoStart10, 15, true, true);
            File logoMinus10_show_in_Panel = convertEpsToPng( logoMinus10.getAbsolutePath() + ".eps", 15, 8 );

            if( logoMinus10_show_in_Panel != null ) {
                minus10logoLabel = new JLabel();
                Icon icon1 = new ImageIcon( logoMinus10_show_in_Panel.getAbsolutePath() );
                minus10logoLabel.setIcon( icon1 );

                int logoStart35 = Math.round( this.meanMinus35SpacerToMinus10 + params.getMinus35MotifWidth() + logoStart10 );
                progressHandlePromotorAnalysis.progress( "Starting promotor analysis ...", 90 );

                this.logoMinus35 = generateSeqLogo( 2.0, bioProspOutMinus35, workingDir.getAbsolutePath() + "\\minus35Logo",
                                                    "EPS", 8.0, -logoStart35, 15, true, true );
//                File logoMinus35_show_in_Panel = generateSeqLogo(2.0, bioProspOutMinus35, workingDir.getAbsolutePath() + "\\minus35LogoPanel",
//                        "PNG", 8.0, -logoStart35, 15, true, true);
                File logoMinus35_show_in_Panel = convertEpsToPng( logoMinus35.getAbsolutePath() + ".eps", 15, 8 );

                minus35LogoLabel = new JLabel();
                Icon icon2 = new ImageIcon( logoMinus35_show_in_Panel.getAbsolutePath() );
                minus35LogoLabel.setIcon( icon2 );

                writeInfoFile( info, false, meanMinus10SpacerToTSS, meanMinus35SpacerToMinus10, (int) contributingCitesForMinus10Motif, (int) contributingCitesForMinus35Motif, upstreamRegions.size() / 2, params );
                progressHandlePromotorAnalysis.progress( "Starting promotor analysis ...", 100 );
                progressHandlePromotorAnalysis.finish();
            }
            else {
                progressHandlePromotorAnalysis.finish();
                success = false;
            }
        }
        else {
            progressHandlePromotorAnalysis.finish();
            success = false;
        }

        return success;
    }


    /**
     *
     * @param rbsParams
     * @param starts
     * @param operons
     *                  <p>
     * @return
     */
    public boolean rbsMotifAnalysis( RbsAnalysisParameters rbsParams, List<TranscriptionStart> starts, List<Operon> operons ) {
        boolean success = true;
        progressHandleRbsAnalysis.progress( "processing rbs analysis ...", 20 );
        Path workingDir = null;
        try {
            workingDir = Files.createTempDirectory( "rbsAnalysis_" );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
        Path rbsBioProspectorInputPath = null;
        Path rbsBioProsFirstHitPath = null;
        Path infoFilePath = null;
        try {
            rbsBioProspectorInputPath = Files.createTempFile( workingDir, "rbsAnalysis_", "SequencesOfIntrest.fna" );
            System.out.println( rbsBioProspectorInputPath );
            rbsBioProsFirstHitPath = Files.createTempFile( workingDir, "rbsAnalysis_", "BioProspectorBestHit.fna" );
            infoFilePath = Files.createTempFile( workingDir, "rbsAnalysis_", "info.txt" );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }

        File parentDir = workingDir.toFile();
        rbsBioProspectorInput = rbsBioProspectorInputPath.toFile();
        rbsBioProsFirstHit = rbsBioProsFirstHitPath.toFile();
//        logoRbs = new File(parentDir.getAbsolutePath() + "\\RBSLogo");
        info = infoFilePath.toFile();

        this.rbsStarts = new TreeMap<>();

        // Make a text pane, set its font and color, then add it to the frame
        regionsRelToTLSTextPane = new JTextPane();
        regionsForMotifSearch = new JTextPane();

        writeSeqForRbsAnalysisInFile( rbsBioProspectorInput, rbsParams );
        progressHandleRbsAnalysis.progress( "processing rbs analysis ...", 60 );

        boolean successfulyRun = runBioProspForRbsAnalysis( parentDir, rbsBioProspectorInput, rbsParams, rbsBioProsFirstHit );
        if( successfulyRun ) {
            Font font = new Font( Font.MONOSPACED, Font.PLAIN, 16 );
            String header = "";
            int shift = 0;
            for( String string : this.upstreamRegions ) {
                if( string.startsWith( ">" ) ) {
                    header = string.substring( 1, string.length() - 1 );
                    if( contributedSequencesWithShift.containsKey( header ) ) {
                        shift = contributedSequencesWithShift.get( header );
                    }
                    try {
                        regionsForMotifSearch.getStyledDocument().insertString( regionsForMotifSearch.getStyledDocument().getLength(), string.toLowerCase(), null );
                    }
                    catch( BadLocationException ex ) {
                        Exceptions.printStackTrace( ex );
                    }
                }
                else {
                    String subregionForMotifSearch = string.substring( 0, string.length() - rbsParams.getMinSpacer() - 1 );
                    try {
                        regionsForMotifSearch.getStyledDocument().insertString( regionsForMotifSearch.getStyledDocument().getLength(), subregionForMotifSearch.toLowerCase() + "\n", null );
                    }
                    catch( BadLocationException ex ) {
                        Exceptions.printStackTrace( ex );
                    }
                    if( contributedSequencesWithShift.containsKey( header ) ) {
                        colorSubstringsInStyledDocument( regionsForMotifSearch, font, regionsForMotifSearch.getStyledDocument().getLength() - 1 - subregionForMotifSearch.length() + shift - 1, rbsParams.getMotifWidth(), Color.BLUE );
                    }
                }
            }

            progressHandleRbsAnalysis.progress( "processing rbs analysis ...", 80 );
            try {
                meanSpacerLengthOfRBSMotif = calculateMotifStartsAndMeanSpacerInRbsAnalysis( this.upstreamRegions, this.regionsRelToTLSTextPane, this.contributedSequencesWithShift, rbsParams, starts, operons );
            }
            catch( BadLocationException ex ) {
                Exceptions.printStackTrace( ex );
            }

            // generating Sequence Logos and adding them into Tabbedpane
            int logoStart = Math.round( this.meanSpacerLengthOfRBSMotif );
            logoStart += rbsParams.getMotifWidth();

            this.logoRbs = generateSeqLogo( 2.0, rbsBioProsFirstHit, parentDir.getAbsolutePath() + "\\RBSLogo",
                                            "EPS", 8.0, -logoStart, 15, true, true );
//            File logoRbs_inPanel = generateSeqLogo(2.0, rbsBioProsFirstHit, parentDir.getAbsolutePath() + "\\RBSLogo_show_in_panel",
//                    "PNG", 8.0, -logoStart, 15, true, true);
            File logoRbs_inPanel = convertEpsToPng( logoRbs.getAbsolutePath() + ".eps", 15.0, 8.0 );
            if( logoRbs_inPanel != null && logoRbs_inPanel.exists() ) {
                rbsLogoLabel = new JLabel();
                Icon icon = new ImageIcon( logoRbs_inPanel.getAbsolutePath() );
                rbsLogoLabel.setIcon( icon );

                writeInfoFile( info, true, meanSpacerLengthOfRBSMotif, 0, (int) contributingCitesForRbsMotif, 0, upstreamRegions.size() / 2, rbsParams );
                progressHandleRbsAnalysis.progress( "processing rbs analysis ...", 100 );
                progressHandleRbsAnalysis.finish();
            }
            else {
                progressHandleRbsAnalysis.finish();
                success = false;
            }
        }
        else {
            progressHandleRbsAnalysis.finish();
            success = false;
        }

        return success;
    }


    /**
     * This method execute the BioProspector binary with the following
     * parameters.
     *
     * @param inputFilePath  absolute path to the input file
     * @param outputFile     absolute path to the output file
     * @param motifWidth     width of the motif
     * @param noOfCycles     number of cycles
     * @param noOfTopMotifs  number of top motifs
     * @param justExamineFwd
     *                       <p>
     * @throws IOException
     */
    private boolean executeBioProspector( String inputFilePath, File outputFile,
                                          int motifWidth, int noOfCycles, int noOfTopMotifs,
                                          int justExamineFwd ) throws IOException {
        this.contributedSequencesWithShift = new TreeMap<>();

        String cmd = NbPreferences.forModule( Object.class ).get( "bioprospector location", "" );
        File file = new File( cmd );
        if( file.exists() && file.canExecute() && file.isDirectory() == false ) {
            List<String> commandArguments = new ArrayList<>();
            commandArguments.add( cmd );
            commandArguments.add( "-i" );
            commandArguments.add( inputFilePath );
            commandArguments.add( "-W" );
            commandArguments.add( "" + motifWidth );
            commandArguments.add( "-n" );
            commandArguments.add( "" + noOfCycles );
            commandArguments.add( "-r" );
            commandArguments.add( "" + noOfTopMotifs );
            commandArguments.add( "-d" );
            commandArguments.add( "" + justExamineFwd );

            ProcessBuilder ps = new ProcessBuilder( commandArguments );

            //From the DOC:  Initially, this property is false, meaning that the
            //standard output and error output of a subprocess are sent to two
            //separate streams
            ps.redirectErrorStream( true );

            Process pr = ps.start();

            BufferedReader in = new BufferedReader( new InputStreamReader( pr.getInputStream() ) );
            String line;
            String id = null;
            String start = "";
            boolean skip = false;

            Writer writer = new BufferedWriter( new OutputStreamWriter(
                    new FileOutputStream( outputFile ), "utf-8" ) );
            while( (line = in.readLine()) != null ) {
                if( line.startsWith( "Motif#2" ) ) {
                    break;
                }
                if( line.startsWith( ">" ) ) {
                    String[] splitted = line.split( "\t" );
                    start = splitted[splitted.length - 1].substring( 2 ); // shift where the motif starts in input seq.
                    id = splitted[0].substring( 1 ); // header
                    writer.write( line + "\n" );
                    skip = true;
                }
                else if( skip ) {
                    this.contributedSequencesWithShift.put( id, Integer.valueOf( start ) );
                    writer.write( line + "\n" );
                    skip = false;
                }
            }

            try {
                pr.waitFor();
            }
            catch( InterruptedException ex ) {
                Logger.getLogger( MotifSearchPanel.class.getName() ).log( Level.SEVERE, null, ex );
                in.close();
            }
            finally {
                writer.close();
            }
            return true;
        }
        else {
            JOptionPane.showMessageDialog( rbsLogoLabel, "BioProspector could not be executed. Check the location settings \n"
                                                         + "for BioProspector or check the permissions for BioProspector.", "Error during running BioProspector", JOptionPane.ERROR_MESSAGE );
            return false;
        }
    }


    /**
     * This method writes all regions of interest for the Motif detection into a
     * file.
     *
     * @param outFile                  output file
     * @param seqs
     * @param noOfBasesToTSS
     * @param spacer
     * @param seqLengthForMotifSearch
     * @param spacer2
     * @param seqLengthForMotifSearch2
     * @param shifts
     *                                 <p>
     * @return the output file containing all subregions needet for motif search
     *         as input.
     */
    private void writeSubRegionFor5UTRInFile( File outFile, List<String> seqs,
                                              int spacer, int seqLengthForMotifSearch, int spacer2,
                                              int seqLengthForMotifSearch2, TreeMap<String, Integer> alignmentShifts ) {

        int cnt = 1;
        int shift = 0;
        boolean isShifts = false;
        String header = "";
        if( alignmentShifts != null ) {
            isShifts = true;
        }

        try( Writer writer = new BufferedWriter( new OutputStreamWriter(
                new FileOutputStream( outFile.getAbsolutePath() ), "utf-8" ) ) ) {
            for( String string : seqs ) {
                if( cnt == 1 ) {
                    if( isShifts ) {
                        header = string.substring( 1, string.length() - 1 );
                        if( alignmentShifts.containsKey( header ) ) {
                            shift = alignmentShifts.get( header );
                        }
                        writer.append( string );
                    }
                    else {
                        writer.append( string );
                    }
                    cnt = 0;
                }
                else {
                    if( isShifts ) {
                        String substring;
                        if( alignmentShifts.containsKey( header ) ) {
                            int stringLength = string.length();
                            int offset = stringLength - (spacer + 1) - (seqLengthForMotifSearch - (shift - 1)) - spacer2 - seqLengthForMotifSearch2;
                            int end = (offset - 1) + (seqLengthForMotifSearch2 + 1);
                            substring = string.substring( offset, end );
                            writer.append( substring + "\n" );
                        }
                        else {
                            int stringLength = string.length();
                            int offset = stringLength - this.alternativeSpacer - seqLengthForMotifSearch2;
                            int end = (offset - 1) + (seqLengthForMotifSearch2 + 1);
                            substring = string.substring( offset, end );
                            writer.append( substring + "\n" );
                        }
                        minus35AnalysisStrings.add( substring + "\n" );
                    }
                    else {
                        int stringLength = string.length();
                        int offset = stringLength - (spacer + 1) - seqLengthForMotifSearch;
                        int end = (offset - 1) + (seqLengthForMotifSearch + 1);
                        String substring = string.substring( offset, end );
                        writer.append( substring + "\n" );
                        minus10AnalysisStrings.add( substring + "\n" );
                    }
                    cnt = 1;
                }
            }

        }
        catch( IOException ex ) {
            // report
        }
    }


    /**
     * This method calculaes the mean spacer betwean the TLS ant the beginning
     * of a RBS-Motif. It also determine the starts of the motifs and presents
     * the values to the colorSubstringsInStyledDocument.
     *
     * @param upstreamRegions        List of upstream regions for promotor motif
     *                               search.
     * @param upstreamRegionTextPane JTextPane where the sequences and motifs
     *                               are going to be written.
     * @param rbsShifts              Shifts of ribosomal binding sites motifs.
     * @param params                 instance of PromotorSearchParameters.
     * @param tss                    List of Transcription start sites.
     * <p>
     * @return the mean spacer distance.
     * <p>
     * @throws BadLocationException
     */
    private float calculateMotifStartsAndMeanSpacerInRbsAnalysis( List<String> upstreamRegions, JTextPane upstreamRegionTextPane, TreeMap<String, Integer> rbsShifts, RbsAnalysisParameters params, List<TranscriptionStart> tss, List<Operon> operons ) throws BadLocationException {
        Font font = new Font( Font.MONOSPACED, Font.PLAIN, 16 );
        int sumOfMinsSpacer = 0;
        Integer shift = 0;
        int minSpacer = params.getMinSpacer();
        int motifWidth = params.getMotifWidth();
        int sequenceOfInterestLength = params.getSeqLengthToAnalyze();

        String header = "";
        for( String string : upstreamRegions ) {
            if( string.startsWith( ">" ) ) {
                header = string.substring( 1, string.length() - 1 );
                if( rbsShifts.containsKey( header ) ) {
                    shift = rbsShifts.get( header );
                    this.contributingCitesForRbsMotif++;
                }
                upstreamRegionTextPane.getStyledDocument().insertString( upstreamRegionTextPane.getStyledDocument().getLength(), string.toLowerCase(), null );
            }
            else {
                int length = string.length() - 1;
                int documentLenght = upstreamRegionTextPane.getStyledDocument().getLength();
                upstreamRegionTextPane.getStyledDocument().insertString( documentLenght, string.toLowerCase(), null );
                if( rbsShifts.containsKey( header ) ) {
                    sumOfMinsSpacer += (length - minSpacer - (shift - 1) - motifWidth) + minSpacer;
                    // shift means the actually position in a string, so if 4 is passed
                    // aat S tartg the motif starts at S. => shift minus 1 in the 0 based system
                    // 123 4 56789
                    int colorStart = documentLenght + (shift - 1);
                    int motifStart = sequenceOfInterestLength - (shift - 1);
                    this.rbsStarts.put( header, motifStart );
                    // this method is 0-based so the coloring starts at the 4th.
                    // Position if you pass 3 as a start position
                    colorSubstringsInStyledDocument( upstreamRegionTextPane, font, colorStart, motifWidth, Color.BLUE );
                }
            }
        }
        return sumOfMinsSpacer / contributingCitesForRbsMotif;
    }


    /**
     * This method calculates an the mean spacer-lentght to the -10 and -35
     * region and also generates two TreeMaps which saves the actually start
     * positions of the -10 and -35 Motifs in context of the whole
     * upstreamregion, which is passed to the promotor analysis.
     *
     * @param upstreamRegions List of upstream regions for promotor motif.
     * @param shifts10        shifts to the -10 motif start.
     * @param shifts35        shifts to the -35 motif start.
     * @param params          instance of PromotorSearchParameters.
     */
    private void calcMotifStartsAndMeanSpacerLength( List<String> upstreamRegions, TreeMap<String, Integer> shifts10,
                                                     TreeMap<String, Integer> shifts35, PromotorSearchParameters params ) {
        int sumOfMinus10Spacer = 0;
        int sumOfMinus35Spacer = 0;
        int shiftPosMinus10 = 0;
        int shiftPosMinus35 = 0;
        final int length = params.getLengthOfPromotorRegion();
        final int spacer1 = params.getMinSpacer1();
        final int spacer2 = params.getMinSpacer2();
        final int seqWidthToAnalyzeMinus10 = params.getSequenceWidthToAnalyzeMinus10();
        final int seqWidthToAnalyzeMinus35 = params.getSequenceWidthToAnalyzeMinus35();

        String header = "";
        for( String string : upstreamRegions ) {
            if( string.startsWith( ">" ) ) {
                header = string.substring( 1, string.length() - 1 );
                if( shifts10.containsKey( header ) ) {
                    shiftPosMinus10 = shifts10.get( header );
                    this.contributingCitesForMinus10Motif++;
                }
                else {
                    shiftPosMinus10 = 0;
                }
                if( shifts35.containsKey( header ) ) {
                    shiftPosMinus35 = shifts35.get( header );
                    this.contributingCitesForMinus35Motif++;
                }
                else {
                    shiftPosMinus35 = 0;
                }
            }
            else {
                int motifStartMinus35;
                if( shifts10.containsKey( header ) ) {
                    sumOfMinus10Spacer += spacer1 + (params.getSequenceWidthToAnalyzeMinus10() - params.getMinusTenMotifWidth() - (shiftPosMinus10 - 1));
                    int motifStartMinus10 = length - spacer1 - seqWidthToAnalyzeMinus10 + shiftPosMinus10;
                    this.minus10MotifStarts.put( header, motifStartMinus10 );
                    motifStartMinus35 = motifStartMinus10 - spacer2 - seqWidthToAnalyzeMinus35 + shiftPosMinus35 - 1;
                }
                else {
                    motifStartMinus35 = length - this.alternativeSpacer - seqWidthToAnalyzeMinus35 + shiftPosMinus35 - 1;
                }
                if( shifts35.containsKey( header ) ) {
                    this.minus35MotifStarts.put( header, motifStartMinus35 );
                    sumOfMinus35Spacer += spacer2 + (params.getSequenceWidthToAnalyzeMinus35() - params.getMinus35MotifWidth() - (shiftPosMinus35 - 1));
                }
            }
        }

        this.meanMinus10SpacerToTSS = sumOfMinus10Spacer / contributingCitesForMinus10Motif;
        this.meanMinus35SpacerToMinus10 = sumOfMinus35Spacer / contributingCitesForMinus35Motif;
    }


    /**
     * This method tone all promotor motifs in an upstream region of a passed
     * Transcriptional start site.
     *
     * @param upstreamRegions List of upstream regions for promotor motif.
     *                        analysis.
     * @param minus10Starts
     * @param minus35Starts
     * @param params          instance of PromotorSearchParameters. Contains all
     *                        passed
     *                        parameters by startign the rbs motif analysis.
     * <p>
     * @return a StyledDocument filled with the upstream regions and toned
     *         motifs.
     */
    private StyledDocument colorPromotorMotifRegions( List<String> upstreamRegions, TreeMap<String, Integer> minus10Starts, TreeMap<String, Integer> minus35Starts, PromotorSearchParameters params ) {

        JTextPane text = new JTextPane();
        Font font = new Font( Font.MONOSPACED, Font.PLAIN, 16 );

        int alignmentStartMinus10 = 0;
        boolean minus10StartExists = false;
        boolean minus35StartExists = false;
        for( String string : upstreamRegions ) {
            if( string.startsWith( ">" ) ) {
                String locus = string.substring( 1, string.length() - 1 );
                if( minus10Starts.containsKey( locus ) ) {
                    alignmentStartMinus10 = minus10Starts.get( locus );
                    minus10StartExists = true;
                }

                if( minus35Starts.containsKey( locus ) ) {
                    minus35StartExists = true;
                }
                try {
                    text.getStyledDocument().insertString( text.getStyledDocument().getLength(), string, null );
                }
                catch( BadLocationException ex ) {
                    Exceptions.printStackTrace( ex );
                }
            }
            else {
                try {
                    text.getStyledDocument().insertString( text.getStyledDocument().getLength(), string.toLowerCase(), null );
                }
                catch( BadLocationException ex ) {
                    Exceptions.printStackTrace( ex );
                }

                int length = text.getStyledDocument().getLength();
                if( minus35StartExists ) {
//                    int colorStartMinus35 = length - (params.getLengthOfPromotorRegion() - alignmentStartMinus35);
//                    colorSubstringsInStyledDocument(text, font, colorStartMinus35 - 2, params.getMinus35MotifWidth(), Color.BLUE);
                    minus35StartExists = false;
                }
                if( minus10StartExists ) {
                    int colorStartMinus10 = length - (params.getLengthOfPromotorRegion() - alignmentStartMinus10);
                    colorSubstringsInStyledDocument( text, font, colorStartMinus10 - 2, params.getMinusTenMotifWidth(), Color.RED );
                    minus10StartExists = false;
                }
            }
        }
        return text.getStyledDocument();
    }


    /**
     * Colorize the defined range between start and stop with the given color.
     *
     * @param styledDoc StyledDocument in which the subregion have to be toned.
     * @param start     Start position of coloring
     * @param length    length of subregion to be toned.
     * @param color     tone color
     */
    private void colorSubstringsInStyledDocument( JTextPane textPane, Font font, int start, int length, Color color ) {
        // Start with the current input attributes for the JTextPane. This
        // should ensure that we do not wipe out any existing attributes
        // (such as alignment or other paragraph attributes) currently
        // set on the text area.
        MutableAttributeSet attrs = textPane.getInputAttributes();

        // Set the font family, size, and style, based on properties of
        // the Font object. Note that JTextPane supports a number of
        // character attributes beyond those supported by the Font class.
        // For example, underline, strike-through, super- and sub-script.
        StyleConstants.setFontFamily( attrs, font.getFamily() );
        StyleConstants.setFontSize( attrs, font.getSize() );
        StyleConstants.setBold( attrs, (font.getStyle() & Font.BOLD) != 0 );

        // Set the font color
        StyleConstants.setForeground( attrs, color );

        // Retrieve the pane's document object
        StyledDocument doc = textPane.getStyledDocument();
        try {
            // Replace the style for the entire document. We exceed the length
            // of the document by 1 so that text entered at the end of the
            // document uses the attributes.
            String upperCasedString = doc.getText( start, length ).toUpperCase();
            doc.remove( start, length );
            doc.insertString( start, upperCasedString, attrs );
        }
        catch( BadLocationException ex ) {
            Exceptions.printStackTrace( ex );
        }
    }


    /**
     * Generates a sequence logo from a given multiple fasta file in fasta
     * format.
     *
     * @param noOfBitsInBar      Number of Bits the Logo will be presented.
     * @param inputFile          Multiple FASTA file.
     * @param outputFilePath     Absolute file path to the destination of the
     *                           generated Logo.
     * @param outputFormat       Possible formats are: "PNG", "JPG" or "SVG".
     * @param hieghtOfLogo       Hight of the Logo.
     * @param sequenceStart      Start of the X-axis labeling.
     * @param logoWidth          Logo width.
     * @param isNumberingOfXAxis Numbering of X-axis if true.
     * @param isYAxis            If true, than Y-axis is visible.
     */
    private File generateSeqLogo( Double noOfBitsInBar, File inputFile, String outputFilePath, String outputFormat, Double hieghtOfLogo,
                                  Integer sequenceStart, Integer logoWidth, boolean isNumberingOfXAxis, boolean isYAxis ) {

        String perl = "perl";
        String cmd = NbPreferences.forModule( Object.class ).get( "seqlogo location", "" );

        File file = new File( cmd );
        if( file.exists() && file.canExecute() && file.isDirectory() == false ) {
            List<String> commandArguments = new ArrayList<>();
            commandArguments.add( perl );
            commandArguments.add( cmd );
            commandArguments.add( "-f" );
            commandArguments.add( inputFile.getAbsolutePath() );
            commandArguments.add( "-B" );
            commandArguments.add( noOfBitsInBar.toString() );
            commandArguments.add( "-F" );
            commandArguments.add( outputFormat );
            commandArguments.add( "-h" );
            commandArguments.add( hieghtOfLogo.toString() );
            commandArguments.add( "-o" );
            commandArguments.add( outputFilePath );
            commandArguments.add( "-s" );
            commandArguments.add( sequenceStart.toString() );
            commandArguments.add( "-w" );
            commandArguments.add( logoWidth.toString() );
            commandArguments.add( "-x" );
            commandArguments.add( "average positions upstream of TSS" );
            commandArguments.add( "-y" );
            commandArguments.add( "bits" );
            commandArguments.add( "-c" );
            if( isNumberingOfXAxis ) {
                commandArguments.add( "-n" );
            }
            if( isYAxis ) {
                commandArguments.add( "-Y" );
            }

            ProcessBuilder ps = new ProcessBuilder( commandArguments );
            ps.redirectErrorStream( true );
            try {
                Process pr = ps.start();
                pr.waitFor();
            }
            catch( IOException | InterruptedException ex ) {
                Exceptions.printStackTrace( ex );
            }

            return new File( outputFilePath );
        }
        else {
            Logger.getLogger( this.getClass().getName() ).log( Level.WARNING, "Please check the setting for Motif-Search, maybe the location\n"
                                                                              + "to the program seqlogo is wrong. Please read the help section for more information." );
            JOptionPane.showMessageDialog( null, "Please check the setting for Motif-Search, maybe the location\n"
                                                 + "to the program seqlogo is wrong. Please read the help section for more information.", "Program SeqLogo", JOptionPane.ERROR_MESSAGE );
            return null;
        }

    }


    /**
     * This method extracts upstream regions from elements of a spesific
     * detected transcription start sites type.
     *
     * @param type          ElementsOfInterest specifies the typ of
     *                      transcription start
     *                      site.
     * @param allStarts     List<TranscriptionStart> of detected transcription
     *                      start
     *                      site instances.
     * @param length        of the region upstream to the transcription start or
     *                      translation start
     * @param isRbsAnalysis <true> for the rbs motif analysis, <false>
     * for promotor motif analysis.
     */
    public void takeUpstreamRegions( ElementsOfInterest type, List<TranscriptionStart> allStarts, int length, boolean isRbsAnalysis ) {

        if( isRbsAnalysis ) {
            progressHandleRbsAnalysis.start( 100 );
            progressHandleRbsAnalysis.progress( 10 );
        }
        else {
            progressHandlePromotorAnalysis.start( 100 );
            progressHandlePromotorAnalysis.progress( 10 );
        }

        this.upstreamRegions = new ArrayList<>();
        this.upstreamRegionsInHash = new TreeMap<>();
        this.locusToTSSs = new TreeMap<>();

        PersistentFeature currentFeature;
        String newLocus;
        String locus;
        int tssStart;
        int uniqueIdx = 1;

        if( isRbsAnalysis ) {
            if( type == ElementsOfInterest.ELEMENTS_FOR_RBS_ANALYSIS ) {
                for( TranscriptionStart tss : allStarts ) {
                    // take only tss with an assigned feature, and only non leaderless
                    // if intragenic tss, than the tss.getNextGene()
                    currentFeature = tss.getAssignedFeature();

                    if( currentFeature != null && !tss.isLeaderless() ) {
                        if( tss.isIntergenicTSS() && tss.getNextGene() != null ) {
                            currentFeature = tss.getNextGene();
                        }

                        locus = currentFeature.getLocus();

                        tss.setRbsSequenceLength( length );
                        tss.setAdditionalIdentyfier( locus );

                        if( !locusToTSSs.containsKey( locus ) ) {
                            // add header in array
                            this.upstreamRegions.add( ">" + locus + "\n" );
                            getUpstreamRegionRelToFeatureStart( currentFeature, length );
                            locusToTSSs.put( locus, tss );
                        }
                    }
                }
            }
        }

        if( type == ElementsOfInterest.ALL ) {
            for( TranscriptionStart tss : allStarts ) {
                currentFeature = tss.getAssignedFeature();
                if( currentFeature != null ) {
                    newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                }
                else {
                    newLocus = "putative_novel_transcript_" + tss.getStartPosition() + "_" + uniqueIdx;
                }
                tss.setPromotorSequenceLength( length );
                tss.setAdditionalIdentyfier( newLocus );
                tssStart = tss.getStartPosition();

                this.upstreamRegions.add( ">" + newLocus + "\n" );
                getPromotorSubstring( tss, tssStart, length, newLocus );
                uniqueIdx++;
            }
        }
        else if( type == ElementsOfInterest.ONLY_ANTISENSE_TSS ) {
            for( TranscriptionStart tss : allStarts ) {
                currentFeature = tss.getAssignedFeature();

                if( tss.isPutativeAntisense() ) {
                    if( currentFeature != null ) {
                        newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                    }
                    else {
                        newLocus = "novelTranscript_" + tss.getStartPosition() + "_" + uniqueIdx;
                    }

                    tss.setPromotorSequenceLength( length );
                    tss.setAdditionalIdentyfier( newLocus );
                    tssStart = tss.getStartPosition();
                    this.upstreamRegions.add( ">" + newLocus + "\n" );
                    getPromotorSubstring( tss, tssStart, length, newLocus );
                    uniqueIdx++;
                }
            }
        }
        else if( type == ElementsOfInterest.ONLY_LEADERLESS_TRANSCRIPTS ) {
            for( TranscriptionStart tss : allStarts ) {
                currentFeature = tss.getAssignedFeature();
                tssStart = tss.getStartPosition();

                if( tss.isLeaderless() ) {
                    newLocus = currentFeature.getLocus() + "_" + uniqueIdx;

                    tss.setAdditionalIdentyfier( newLocus );
                    tss.setPromotorSequenceLength( length );
                    this.upstreamRegions.add( ">" + newLocus + "\n" );
                    getPromotorSubstring( tss, tssStart, length, newLocus );
                    uniqueIdx++;
                }
            }
        }
        else if( type == ElementsOfInterest.ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS ) {
            for( TranscriptionStart tss : allStarts ) {
                currentFeature = tss.getAssignedFeature();

                if( !tss.isPutativeAntisense() && !tss.isLeaderless()
                    || (tss.isIntragenicTSS() && tss.getOffsetToNextDownstrFeature() > 0) ) {
                    if( currentFeature != null ) {
                        newLocus = currentFeature.getLocus() + "_" + uniqueIdx;

                        tss.setPromotorSequenceLength( length );
                        tss.setAdditionalIdentyfier( newLocus );
                        tssStart = tss.getStartPosition();
                        this.upstreamRegions.add( ">" + newLocus + "\n" );
                        getPromotorSubstring( tss, tssStart, length, newLocus );
                        uniqueIdx++;
                    }
                }
            }
        }
        else if( type == ElementsOfInterest.ONLY_SELECTED_FOR_UPSTREAM_ANALYSES ) {
            for( TranscriptionStart tss : allStarts ) {
                currentFeature = tss.getAssignedFeature();
                tssStart = tss.getStartPosition();

                if( tss.isSelected() ) {
                    if( currentFeature != null ) {
                        newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                    }
                    else {
                        newLocus = "novelTranscript_" + tss.getStartPosition() + "_" + uniqueIdx;
                    }

                    tss.setPromotorSequenceLength( length );
                    tss.setAdditionalIdentyfier( newLocus );
                    this.upstreamRegions.add( ">" + newLocus + "\n" );
                    getPromotorSubstring( tss, tssStart, length, newLocus );
                    uniqueIdx++;
                }
            }
        }
    }


    /**
     * Gets the promotor region upstream to the tss and adds it to the list of
     * upstream promotor regions.
     *
     * @param tss      the TranscriptionStart instance
     * @param tssStart the transcriptions start site
     * @param length   of the upstream region to extract
     * @param newLocus the new generated locus name, if no locus exists for
     *                 given tss
     */
    private void getPromotorSubstring( TranscriptionStart tss, int tssStart, int length, String newLocus ) {
        String substr = "";
        int chromLength = ref.getChromosome( tss.getChromId() ).getLength();
        if( tss.isFwdStrand() ) {
            if( tssStart < length ) {
                int a = length - tssStart;
                String substr1 = ref.getChromSequence( tss.getChromId(), chromLength - a, chromLength );
                String substr2 = ref.getChromSequence( tss.getChromId(), 0, tssStart - 1 );
                substr = substr1 + substr2;
            }
            else {
                substr = ref.getChromSequence( tss.getChromId(), tssStart - length, tssStart - 1 );
            }
            upstreamRegions.add( substr + "\n" );
            this.upstreamRegionsInHash.put( newLocus, substr + "\n" );
        }
        else {
            if( tssStart + length > chromLength ) {
                String substr1 = ref.getChromSequence( tss.getChromId(), tssStart + 1, chromLength );
                String substr2 = ref.getChromSequence( tss.getChromId(), 0, length - (chromLength - tssStart) );
                substr = substr1 + substr2;
            }
            else {
                substr = SequenceUtils.getReverseComplement( ref.getChromSequence( tss.getChromId(), tssStart + 1, tssStart + length ) );
            }
            upstreamRegions.add( substr + "\n" );
            this.upstreamRegionsInHash.put( newLocus, substr + "\n" );
        }
    }


    /**
     * Gets the upstream region relative to the translation start site of the
     * feature assigned to the given tss and adds it to the list of upstream
     * regions.
     *
     * @param tss            TranscriptionStart
     * @param currentFeature PersistentFeature which is assigned to the tss
     * @param length         of the upstream region to extract
     */
    private void getUpstreamRegionRelToFeatureStart( PersistentFeature currentFeature, int length ) {
        int featureStart;
        String substr = "";
        String locus = currentFeature.getLocus();
        int chromId = currentFeature.getChromId();
        int chromLength = ref.getChromosome( chromId ).getLength();
        if( currentFeature.isFwdStrand() ) {
            featureStart = currentFeature.getStart();
            if( featureStart < length ) {
                // TODO
                int a = length - featureStart;
                String substr1 = ref.getChromSequence( chromId, chromLength - a, chromLength );
                String substr2 = ref.getChromSequence( chromId, 0, featureStart - 1 );
                substr = substr1 + substr2;
            }
            else {
                substr = ref.getChromSequence( chromId, featureStart - length, featureStart - 1 );
            }
            upstreamRegions.add( substr + "\n" );
            this.upstreamRegionsInHash.put( locus, substr + "\n" );
        }
        else {
            featureStart = currentFeature.getStop();
            if( featureStart + length > chromLength ) {
                // TODO
                String substr1 = ref.getChromSequence( chromId, featureStart + 1, chromLength );
                String substr2 = ref.getChromSequence( chromId, 0, length - (chromLength - featureStart) );
                substr = substr1 + substr2;
            }
            else {
                substr = SequenceUtils.getReverseComplement( ref.getChromSequence( chromId, featureStart + 1, featureStart + length ) );
            }
            upstreamRegions.add( substr + "\n" );
            this.upstreamRegionsInHash.put( locus, substr + "\n" );
        }
    }


    /**
     * Writes all sequences used for the RBS-analysis into a file. This file
     * will be the input file for the motif search tool BioProspector.
     *
     * @param rbsBioProspectorInput Output file for the BioProspector input.
     * @param rbsParams             RbsAnalysisParameters
     */
    private void writeSeqForRbsAnalysisInFile( File rbsBioProspectorInput, RbsAnalysisParameters rbsParams ) {

        try( Writer writer = new BufferedWriter( new OutputStreamWriter(
                new FileOutputStream( rbsBioProspectorInput.getAbsolutePath() ), "utf-8" ) ) ) {
            for( String region : this.upstreamRegions ) {
                if( region.startsWith( ">" ) ) {
                    writer.write( region );
                }
                else {
                    String subregionForMotifSearch = region.substring( 0, region.length() - rbsParams.getMinSpacer() - 1 );
                    writer.write( subregionForMotifSearch + "\n" );
                }
            }

        }
        catch( UnsupportedEncodingException ex ) {
            Exceptions.printStackTrace( ex );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
    }


    /**
     * Wrapper method to start the executeBioProspector method.
     *
     * @param workingDir            Current working directory File instance.
     * @param rbsBioProspectorInput Input file for motif analysis tool
     *                              BioProspector
     * @param bioProspectorOut      Output file of the motif search tool
     *                              BioProspector
     * @param rbsParams             RbsAnalysisParameters
     * @param rbsBioProsFirstHit    File instance, contains the parsed output
     *                              (best
     *                              hit from BioProspector).
     * <p>
     * @return <true> if excecution wass seccessfully, else <false>
     */
    private boolean runBioProspForRbsAnalysis( File workingDir, File rbsBioProspectorInput, RbsAnalysisParameters rbsParams, File rbsBioProsFirstHit ) {
        boolean success = false;
        if( workingDir.isDirectory() ) {
            String posixPath = "/cygdrive/c";
            String sub = rbsBioProspectorInput.getAbsolutePath().substring( 2 );
            posixPath += sub.replaceAll( "\\\\", "/" );

            try {
                success = this.executeBioProspector(
                        posixPath, rbsBioProsFirstHit, rbsParams.getMotifWidth(),
                        rbsParams.getNumberOfCyclesForBioProspector(),
                        1, 1 );
            }
            catch( IOException ex ) {
                Exceptions.printStackTrace( ex );
            }
        }
        return success;
    }


    /**
     * Each TSS-element on which the promotor analysis was performed gets
     * positions at which the -10 and -35 motif starts and which length the
     * motifs have.
     *
     * @param starts        List of Transcription start sites
     * @param minus10Starts all detected -10 start sites
     * @param minus35Starts all detected -35 start sites
     * @param params        PromotorSearchParameters instance
     */
    private void setMotifSearchResults( List<TranscriptionStart> starts, TreeMap<String, Integer> minus10Starts, TreeMap<String, Integer> minus35Starts, PromotorSearchParameters params ) {

        if( starts != null ) {
            // Setting start positions of Promotorelements (-35 -10) to TSS objects
            // needed, when exporting to feature tables.
            HashMap<String, TranscriptionStart> startsInTreeMap = new HashMap<>();
            for( TranscriptionStart tss : starts ) {
                if( tss.getAdditionalIdentyfier() != null ) {
                    startsInTreeMap.put( tss.getAdditionalIdentyfier(), tss );
                }
            }
            for( String str : this.upstreamRegions ) {
                String locus;
                if( str.startsWith( ">" ) ) {
                    locus = str.substring( 1, str.length() - 1 );
                    if( startsInTreeMap.containsKey( locus ) ) {
                        TranscriptionStart start = startsInTreeMap.get( locus );
                        if( minus10Starts.containsKey( locus ) ) {
                            start.setPromotorFeaturesAssigned( true );
                            start.setStartMinus10Motif( minus10Starts.get( locus ) );
                            start.setMinus10MotifWidth( params.getMinusTenMotifWidth() );
                        }
                        if( minus35Starts.containsKey( locus ) ) {
                            start.setPromotorFeaturesAssigned( true );
                            start.setStartMinus35Motif( minus35Starts.get( locus ) );
                            start.setMinus35MotifWidth( params.getMinus35MotifWidth() );
                        }
                        startsInTreeMap.put( locus, start );
                    }
                }
            }
        }
    }


    /**
     * Stores all results of the promotor analysis into the transcription start
     * site instances.
     *
     * @param upstreamRegions all for the analysis used upstream regions.
     * @param minus10Starts   TreeMap<String, Integer>
     * @param minus35Starts   TreeMap<String, Integer>
     * @param minus10Shifts   TreeMap<String, Integer>
     * @param minus35Shifts   TreeMap<String, Integer>
     * @param params          PromotorSearchParameters
     * @param tss             all for the motif search used transcriptions start
     *                        site
     *                        instances
     */
    public void storePromoterAnalysisResults( List<String> upstreamRegions,
                                              TreeMap<String, Integer> minus10Starts, TreeMap<String, Integer> minus35Starts,
                                              TreeMap<String, Integer> minus10Shifts, TreeMap<String, Integer> minus35Shifts,
                                              PromotorSearchParameters params, List<TranscriptionStart> tss ) {
        if( tss != null ) {
            HashMap<String, TranscriptionStart> tssInTreeMap = new HashMap<>();
            for( TranscriptionStart ts : tss ) {
                if( ts.getAdditionalIdentyfier() != null ) {
                    tssInTreeMap.put( ts.getAdditionalIdentyfier(), ts );
                    // reset
                    ts.setPromotorFeaturesAssigned( false );
                }
            }

            for( String str : upstreamRegions ) {
                String locus;
                if( str.startsWith( ">" ) ) {
                    locus = str.substring( 1, str.length() - 1 );
                    if( tssInTreeMap.containsKey( locus ) ) {
                        TranscriptionStart start = tssInTreeMap.get( locus );
                        if( minus10Shifts.containsKey( locus ) ) {
                            start.setPromotorFeaturesAssigned( true );
                            start.setStartMinus10Motif( minus10Starts.get( locus ) );
                            start.setMinus10MotifWidth( params.getMinusTenMotifWidth() );
                        }
                        if( minus35Shifts.containsKey( locus ) ) {
                            start.setPromotorFeaturesAssigned( true );
                            start.setStartMinus10Motif( minus35Starts.get( locus ) );
                            start.setMinus10MotifWidth( params.getMinus35MotifWidth() );
                        }
                        tssInTreeMap.put( locus, start );
                    }
                }
            }
        }
    }


    /**
     * Stores the results from rbs analysis.
     *
     * @param upstreamRegions all for the analysis used upstream regions.
     * @param rbsStarts       start of ribosomal binding site
     * @param rbsShifts       shift in upstream region to start of ribosomal
     *                        binding
     *                        site
     * @param params          RbsAnalysisParameters
     * @param tss             all transcription start site instaces used for
     *                        upstream
     *                        analysis
     */
    public void storeRbsAnalysisResults( List<String> upstreamRegions, TreeMap<String, Integer> rbsStarts, TreeMap<String, Integer> rbsShifts, RbsAnalysisParameters params, List<TranscriptionStart> tss ) {
        if( tss != null ) {
            HashMap<String, TranscriptionStart> tssInTreeMap = new HashMap<>();
            for( TranscriptionStart ts : tss ) {
                if( ts.getAdditionalIdentyfier() != null ) {
                    tssInTreeMap.put( ts.getAdditionalIdentyfier(), ts );
                    // reset
                    ts.setRbsFeatureAssigned( false );
                }
            }

            for( String str : upstreamRegions ) {
                String locus;
                if( str.startsWith( ">" ) ) {
                    locus = str.substring( 1, str.length() - 1 );
                    if( tssInTreeMap.containsKey( locus ) ) {
                        TranscriptionStart start = tssInTreeMap.get( locus );
                        if( rbsShifts.containsKey( locus ) ) {
                            start.setRbsFeatureAssigned( true );
                            start.setStartRbsMotif( rbsStarts.get( locus ) );
                            start.setRbsMotifWidth( params.getMotifWidth() );
                        }
                        tssInTreeMap.put( locus, start );
                    }
                }
            }
        }
    }


    @Override
    public void update( Object args ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


    public TreeMap<String, Integer> getContributedSequencesWithShift() {
        return contributedSequencesWithShift;
    }


    public void setContributedSequencesWithShift( TreeMap<String, Integer> contributedSequencesWithShift ) {
        this.contributedSequencesWithShift = contributedSequencesWithShift;
    }


    public File getLogoMinus10() {
        return logoMinus10;
    }


    public JLabel getMinus35LogoLabel() {
        return minus35LogoLabel;
    }


    public void setMinus35LogoLabel( JLabel minus35LogoLabel ) {
        this.minus35LogoLabel = minus35LogoLabel;
    }


    public void setLogoMinus10( File logoMinus10 ) {
        this.logoMinus10 = logoMinus10;
    }


    public File getLogoMinus35() {
        return logoMinus35;
    }


    public void setLogoMinus35( File logoMinus35 ) {
        this.logoMinus35 = logoMinus35;
    }


    public JLabel getMinus10logoLabel() {
        return minus10logoLabel;
    }


    public void setMinus10logoLabel( JLabel minus10logoLabel ) {
        this.minus10logoLabel = minus10logoLabel;
    }


    public File getLogoRbs() {
        return logoRbs;
    }


    public void setLogoRbs( File logoRbs ) {
        this.logoRbs = logoRbs;
    }


    public List<String> getUpstreamRegions() {
        return upstreamRegions;
    }


    public void setUpstreamRegions( List<String> upstreamRegions ) {
        this.upstreamRegions = upstreamRegions;
    }


    public List<String> getMinus10AnalysisStrings() {
        return minus10AnalysisStrings;
    }


    public void setMinus10AnalysisStrings( List<String> minus10AnalysisStrings ) {
        this.minus10AnalysisStrings = minus10AnalysisStrings;
    }


    public List<String> getMinus35AnalysisStrings() {
        return minus35AnalysisStrings;
    }


    public void setMinus35AnalysisStrings( List<String> minus35AnalysisStrings ) {
        this.minus35AnalysisStrings = minus35AnalysisStrings;
    }


    public float getContributingCitesForMinus10Motif() {
        return contributingCitesForMinus10Motif;
    }


    public void setContributingCitesForMinus10Motif( float contributingCitesForMinus10Motif ) {
        this.contributingCitesForMinus10Motif = contributingCitesForMinus10Motif;
    }


    public float getContributingCitesForMinus35Motif() {
        return contributingCitesForMinus35Motif;
    }


    public void setContributingCitesForMinus35Motif( float contributingCitesForMinus35Motif ) {
        this.contributingCitesForMinus35Motif = contributingCitesForMinus35Motif;
    }


    public float getContributingCitesForRbsMotif() {
        return contributingCitesForRbsMotif;
    }


    public void setContributingCitesForRbsMotif( float contributingCitesForRbsMotif ) {
        this.contributingCitesForRbsMotif = contributingCitesForRbsMotif;
    }


    public int getAlternativeSpacer() {
        return alternativeSpacer;
    }


    public void setAlternativeSpacer( int alternativeSpacer ) {
        this.alternativeSpacer = alternativeSpacer;
    }


    public TreeMap<String, Integer> getMinus10MotifStarts() {
        return minus10MotifStarts;
    }


    public void setMinus10MotifStarts( TreeMap<String, Integer> minus10MotifStarts ) {
        this.minus10MotifStarts = minus10MotifStarts;
    }


    public TreeMap<String, Integer> getMinus35MotifStarts() {
        return minus35MotifStarts;
    }


    public void setMinus35MotifStarts( TreeMap<String, Integer> minus35MotifStarts ) {
        this.minus35MotifStarts = minus35MotifStarts;
    }


    public TreeMap<String, Integer> getRbsStarts() {
        return rbsStarts;
    }


    public void setRbsStarts( TreeMap<String, Integer> rbsStarts ) {
        this.rbsStarts = rbsStarts;
    }


    public File getRbsBioProspectorInput() {
        return rbsBioProspectorInput;
    }


    public void setRbsBioProspectorInput( File rbsBioProspectorInput ) {
        this.rbsBioProspectorInput = rbsBioProspectorInput;
    }


    public File getRbsBioProsFirstHit() {
        return rbsBioProsFirstHit;
    }


    public void setRbsBioProsFirstHit( File rbsBioProsFirstHit ) {
        this.rbsBioProsFirstHit = rbsBioProsFirstHit;
    }


    public TreeMap<String, String> getUpstreamRegionsInHash() {
        return upstreamRegionsInHash;
    }


    public void setUpstreamRegionsInHash( TreeMap<String, String> upstreamRegionsInHash ) {
        this.upstreamRegionsInHash = upstreamRegionsInHash;
    }


    public float getMeanMinus10SpacerToTSS() {
        return meanMinus10SpacerToTSS;
    }


    public void setMeanMinus10SpacerToTSS( float meanMinus10SpacerToTSS ) {
        this.meanMinus10SpacerToTSS = meanMinus10SpacerToTSS;
    }


    public float getMeanMinus35SpacerToMinus10() {
        return meanMinus35SpacerToMinus10;
    }


    public void setMeanMinus35SpacerToMinus10( float meanMinus35SpacerToMinus10 ) {
        this.meanMinus35SpacerToMinus10 = meanMinus35SpacerToMinus10;
    }


    public float getMeanSpacerLengthOfRBSMotif() {
        return meanSpacerLengthOfRBSMotif;
    }


    public void setMeanSpacerLengthOfRBSMotif( float meanSpacerLengthOfRBSMotif ) {
        this.meanSpacerLengthOfRBSMotif = meanSpacerLengthOfRBSMotif;
    }


    public JTextPane getRegionsRelToTLSTextPane() {
        return regionsRelToTLSTextPane;
    }


    public void setRegionsRelToTLSTextPane( JTextPane regionsRelToTLSTextPane ) {
        this.regionsRelToTLSTextPane = regionsRelToTLSTextPane;
    }


    public JTextPane getRegionsForMotifSearch() {
        return regionsForMotifSearch;
    }


    public void setRegionsForMotifSearch( JTextPane regionsForMotifSearch ) {
        this.regionsForMotifSearch = regionsForMotifSearch;
    }


    public JLabel getRbsLogoLabel() {
        return rbsLogoLabel;
    }


    public void setRbsLogoLabel( JLabel rbsLogoLabel ) {
        this.rbsLogoLabel = rbsLogoLabel;
    }


    public File getMinus10Input() {
        return minus10Input;
    }


    public void setMinus10Input( File minus10Input ) {
        this.minus10Input = minus10Input;
    }


    public File getMinus35Input() {
        return minus35Input;
    }


    public void setMinus35Input( File minus35Input ) {
        this.minus35Input = minus35Input;
    }


    public File getBioProspOutMinus10() {
        return bioProspOutMinus10;
    }


    public void setBioProspOutMinus10( File bioProspOutMinus10 ) {
        this.bioProspOutMinus10 = bioProspOutMinus10;
    }


    public File getBioProspOutMinus35() {
        return bioProspOutMinus35;
    }


    public void setBioProspOutMinus35( File bioProspOutMinus35 ) {
        this.bioProspOutMinus35 = bioProspOutMinus35;
    }


    public JTextPane getRegionOfIntrestMinus10() {
        return regionOfIntrestMinus10;
    }


    public JTextPane getRegionOfIntrestMinus35() {
        return regionOfIntrestMinus35;
    }


    public TreeMap<String, Integer> getIdsToMinus10Shifts() {
        return idsToMinus10Shifts;
    }


    public TreeMap<String, Integer> getIdsToMinus35Shifts() {
        return idsToMinus35Shifts;
    }


    public StyledDocument getColoredPromotorRegions() {
        return coloredPromotorRegions;
    }


    /**
     * Writes the info file. It contains all information about the motif search
     * analysis.
     *
     * @param file                          destination file
     * @param isRbs                         <true> if the info file is about the
     *                                      RBS-analysis
     * @param meanSpacer1                   if RBS analysis was performed, it is
     *                                      the mean spacer
     *                                      between translation start site and the RBS-motif. If the promotor
     *                                      analysis was performed, meanspacer1 ist the mean spacer between the tss
     *                                      and the -10 motif
     * @param meanspacer2                   If the promotor analysis was
     *                                      performed, meanspacer2
     *                                      ist the mean spacer from the -10 motif to the -35 motif
     * @param contributedSegmentsToFstMotif number of sequences that
     *                                      contdributed to the -10/RBS motif
     * @param contributedSegmentsToSndMotif number of sequences that
     *                                      contdributed to the -35 motif
     * @param noOfSequences                 number of all for the motif search
     *                                      used sequences
     * @param params                        ParameterSetIs
     */
    private void writeInfoFile( File file, boolean isRbs, float meanSpacer1,
                                float meanspacer2, int contributedSegmentsToFstMotif,
                                int contributedSegmentsToSndMotif, int noOfSequences,
                                ParameterSetI<Object> params ) {
        PromotorSearchParameters promotorParams = null;
        RbsAnalysisParameters rbsParams = null;
        if( isRbs ) {
            rbsParams = (RbsAnalysisParameters) params;
        }
        else {
            promotorParams = (PromotorSearchParameters) params;
        }

        try( Writer writer = new BufferedWriter( new OutputStreamWriter(
                new FileOutputStream( file.getAbsolutePath() ), "utf-8" ) ) ) {
            if( isRbs ) {
                writer.write( "Infos to RBS analysis\n" );
                writer.write( "Length of upstream regions relative to TLS taken for analysis: " + rbsParams.getSeqLengthToAnalyze() + "\n" );
                writer.write( "Min. Spacer: " + rbsParams.getMinSpacer() + "\n" );
                writer.write( "Motif width: " + rbsParams.getMotifWidth() + "\n" );
                writer.write( "Mean spacer width relative to TLS: " + meanSpacer1 + "\n" );
                writer.write( "Number of contributed Segments to Motif: " + contributedSegmentsToFstMotif + "/" + noOfSequences + "\n" );
            }
            else {
                writer.write( "Infos to Promotor analysis\n" );
                writer.write( "Length of upstream regions relative to TLS taken for analysis: " + promotorParams.getLengthOfPromotorRegion() + "\n" );
                writer.write( "Min. spacer 1: " + promotorParams.getMinSpacer1() + "\n" );
                writer.write( "Mean spacer 1: " + meanSpacer1 + "\n" );
                writer.write( "Min. spacer 2: " + promotorParams.getMinSpacer2() + "\n" );
                writer.write( "Mean spacer 2: " + meanspacer2 + "\n" );
                writer.write( "-10 motif width: " + promotorParams.getMinusTenMotifWidth() + "\n" );
                writer.write( "-35 motif width: " + promotorParams.getMinus35MotifWidth() + "\n" );
                writer.write( "Number of contributed Segments to -10 Motif: " + contributedSegmentsToFstMotif + "/" + noOfSequences + "\n" );
                writer.write( "Number of contributed Segments to -35 Motif: " + contributedSegmentsToSndMotif + "/" + noOfSequences + "\n" );
            }

        }
        catch( UnsupportedEncodingException ex ) {
            Exceptions.printStackTrace( ex );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
    }


    /**
     *
     * @return info file
     */
    public File getInfo() {
        return info;
    }


    /**
     * Converts the EPS into PNG using ghostscript.
     *
     * @param epsPath    absolute path to the EPS-file (Encapsulated Post
     *                   Script)
     * @param logoWidth  logo width
     * @param logoHeight logo heigth
     * <p>
     * @return the converted .png file.
     */
    private File convertEpsToPng( String epsPath, double logoWidth, double logoHeight ) {
        int height = (int) (logoHeight * (72 / 2.54));  // user specifies height of logo line
        int width = (int) (logoWidth * (72 / 2.54));
        String pngFilePath = epsPath.substring( 0, epsPath.length() - 4 ) + ".png";
        File returnFile = new File( pngFilePath );
        String cmd = "gs";

        List<String> commandArguments = new ArrayList<>();
        commandArguments.add( cmd );
        commandArguments.add( "-o" );
        commandArguments.add( pngFilePath );
        commandArguments.add( "-sDEVICE=png16m" );
        commandArguments.add( "-q" );
        commandArguments.add( "-r96" );
        commandArguments.add( "-dDEVICEWIDTHPOINTS=" + width );
        commandArguments.add( "-dDEVICEHEIGHTPOINTS=" + height );
        commandArguments.add( "-dTextAlphaBits=4" ); // $antialias = (defined $input->{ANTIALIAS} && $input->{ANTIALIAS}) ? "-dTextAlphaBits=4" : "";
        commandArguments.add( "-dSAFER" );
        commandArguments.add( epsPath );

        String commandsString = commandArguments.toString();
        System.out.println( commandsString );
        ProcessBuilder ps = new ProcessBuilder( commandArguments );
        try {
            Process pr = ps.start();
            pr.waitFor();
            return returnFile;
        }
        catch( IOException | InterruptedException ex ) {
            return null;
        }
    }


}
