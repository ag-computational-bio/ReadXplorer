package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
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
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

/**
 * This Class represents the whole model behind the motif search in
 * transcriptomeAnalyses module.
 *
 * @author jritter
 */
public class MotifSearchModel implements Observer {

//    private RbsMotifSearchPanel rbsMotifSearchPanel;
    private TreeMap<String, String> upstreamRegionsInHash;
    private TreeMap<String, Integer> contributedSequencesWithShift;
    private File logoMinus10, logoMinus35, logoRbs;
    private final HashMap<Integer, PersistantChromosome> chromosomes;
    private List<String> upstreamRegions;
    private float meanMinus10SpacerToTSS, meanMinus35SpacerToMinus10;
    private float meanSpacerLengthOfRBSMotif;
    private final ProgressHandle progressHandlePromotorAnalysis, progressHandleRbsAnalysis;
    private final String handlerTitlePromotorAnalysis, handlerTitleRBSAnalysis;
    private List<String> minus10AnalysisStrings, minus35AnalysisStrings;
    private float contributingCitesForMinus10Motif, contributingCitesForMinus35Motif, contributingCitesForRbsMotif;
    private int alternativeSpacer;
    private TreeMap<String, Integer> minus10MotifStarts, minus35MotifStarts, rbsStarts;
    private File rbsBioProspectorInput, rbsBioProsFirstHit;
    private JTextPane regionsRelToTLSTextPane;
    private JTextPane regionsForMotifSearch;
    private JLabel rbsLogoLabel;
    private File minus10Input, minus35Input, bioProspOutMinus10, bioProspOutMinus35, info;
    private JTextPane regionOfIntrestMinus10, regionOfIntrestMinus35;
    private TreeMap<String, Integer> idsToMinus10Shifts;
    private TreeMap<String, Integer> idsToMinus35Shifts;
    private StyledDocument coloredPromotorRegions;
    private JLabel minus10logoLabel;
    private JLabel minus35LogoLabel;

    /**
     *
     * @param refViewer
     */
    public MotifSearchModel(ReferenceViewer refViewer) {
        this.chromosomes = (HashMap<Integer, PersistantChromosome>) refViewer.getReference().getChromosomes();
        this.handlerTitlePromotorAnalysis = "Processing promotor analysis";
        this.handlerTitleRBSAnalysis = "Processing rbs analysis";
        this.progressHandlePromotorAnalysis = ProgressHandleFactory.createHandle(handlerTitlePromotorAnalysis);
        this.progressHandleRbsAnalysis = ProgressHandleFactory.createHandle(handlerTitleRBSAnalysis);
    }

    /**
     * This method provide a motif search for cosensus regions in 5'-UTR,
     * usually the -35 and -10 region.
     *
     * @param params instance of PromotorSearchParameters.
     */
    public void utrPromotorAnalysis(PromotorSearchParameters params, List<TranscriptionStart> starts, List<Operon> operons) {

        this.alternativeSpacer = params.getAlternativeSpacer();
        minus10MotifStarts = new TreeMap<>();
        minus35MotifStarts = new TreeMap<>();
        this.minus10AnalysisStrings = new ArrayList<>();
        this.minus35AnalysisStrings = new ArrayList<>();
        this.progressHandlePromotorAnalysis.progress("processing promotor analysis ...", 20);

        regionOfIntrestMinus10 = new JTextPane();
        regionOfIntrestMinus35 = new JTextPane();

        Path workingDirPath = null;
        Path bioProspectorOutMinus10Path = null;
        Path bioProspectorOutMinus35Path = null;
        Path minus10InputPath = null;
        Path minus35InputPath = null;
        Path infoFilePath = null;
        try {
            workingDirPath = Files.createTempDirectory("promotorAnalysis_");
            bioProspectorOutMinus10Path = Files.createTempFile(workingDirPath, "promotorAnalysis_", "bioProspectorOutMinus10.fna");
            bioProspectorOutMinus35Path = Files.createTempFile(workingDirPath, "promotorAnalysis_", "bioProspectorOutMinus35.fna");
            minus10InputPath = Files.createTempFile(workingDirPath, "promotorAnalysis_", "inputBioProspectorMinus10.fna");
            minus35InputPath = Files.createTempFile(workingDirPath, "promotorAnalysis_", "inputBioProspectorMinus35.fna");
            infoFilePath = Files.createTempFile(workingDirPath, "promotorAnalysis_", "info.txt");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        File workingDir = workingDirPath.toFile();
        minus10Input = minus10InputPath.toFile();
        minus35Input = minus35InputPath.toFile();
        bioProspOutMinus10 = bioProspectorOutMinus10Path.toFile();
        bioProspOutMinus35 = bioProspectorOutMinus35Path.toFile();
        info = infoFilePath.toFile();

        // 1. write all upstream subregions for -10 analysis
        writeSubRegionFor5UTRInFile(
                minus10Input, this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                null);

        // BioProspector search for -10 motif
        String posixPath = "/cygdrive/c";
        String sub = minus10Input.getAbsolutePath().toString().substring(2);
        posixPath += sub.replaceAll("\\\\", "/");

        try {
            // 2. executing bioprospector and parse the best scored (first listed) Hits and write 
            this.executeBioProspector(
                    posixPath, bioProspOutMinus10, params.getMinusTenMotifWidth(), params.getNoOfTimesTrying(),
                    1, 1);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        this.progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 40);

        idsToMinus10Shifts = new TreeMap<>();
        idsToMinus10Shifts.putAll(this.contributedSequencesWithShift);

        // 4. All sequences, which did not contain a motif in the first run for-10 motif serch
        // will be descard in the next step of the analysis of the -35 motif search
        writeSubRegionFor5UTRInFile(
                minus35Input, this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                idsToMinus10Shifts);
//                alignmentshiftsOFMinusTenArray, regionOfIntrestMinus10, regionOfIntrestMinus35);

        posixPath = "/cygdrive/c";
        sub = minus35Input.getAbsolutePath().toString().substring(2);
        posixPath += sub.replaceAll("\\\\", "/");

        try {
            // 5. So in this step we just processing subregions which had a hit 
            // in the -10 region.
            this.executeBioProspector(
                    posixPath, bioProspOutMinus35, params.getMinus35MotifWidth(),
                    params.getNoOfTimesTrying(),
                    1, 1);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 60);

        idsToMinus35Shifts = new TreeMap<>();
        idsToMinus35Shifts.putAll(this.contributedSequencesWithShift);

        int motifWidth10 = params.getMinusTenMotifWidth();
        int motifWidth35 = params.getMinus35MotifWidth();
        int seqWidthToAnalyzeMinus10 = params.getSequenceWidthToAnalyzeMinus10();
        int seqWidthToAnalyzeMinus35 = params.getSequenceWidthToAnalyzeMinus35();
        int shiftPosMinus10 = 0;
        int shiftPosMinus35 = 0;
        int index = 0;
        String header = "";
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);

        for (String string : upstreamRegions) {
            if (string.startsWith(">")) {
                header = string.substring(1, string.length() - 1);
                if (idsToMinus10Shifts.containsKey(header)) {
                    shiftPosMinus10 = idsToMinus10Shifts.get(header);
                }
                if (idsToMinus35Shifts.containsKey(header)) {
                    shiftPosMinus35 = idsToMinus35Shifts.get(header);
                }
                try {
                    regionOfIntrestMinus10.getStyledDocument().insertString(regionOfIntrestMinus10.getStyledDocument().getLength(), string, null);
                    regionOfIntrestMinus35.getStyledDocument().insertString(regionOfIntrestMinus35.getStyledDocument().getLength(), string, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                index++;
            } else {
                try {
                    regionOfIntrestMinus10.getStyledDocument().insertString(regionOfIntrestMinus10.getStyledDocument().getLength(), this.minus10AnalysisStrings.get(index - 1).toLowerCase(), null);
                    regionOfIntrestMinus35.getStyledDocument().insertString(regionOfIntrestMinus35.getStyledDocument().getLength(), this.minus35AnalysisStrings.get(index - 1).toLowerCase(), null);
                    if (idsToMinus10Shifts.containsKey(header)) {
                        colorSubstringsInStyledDocument(regionOfIntrestMinus10, font, regionOfIntrestMinus10.getStyledDocument().getLength() - 1 - seqWidthToAnalyzeMinus10 + shiftPosMinus10 - 1, motifWidth10, Color.RED);
                    }
                    if (idsToMinus35Shifts.containsKey(header)) {
                        colorSubstringsInStyledDocument(regionOfIntrestMinus35, font, regionOfIntrestMinus35.getStyledDocument().getLength() - 1 - seqWidthToAnalyzeMinus35 + shiftPosMinus35 - 1, motifWidth35, Color.BLUE);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        }

        calcMotifStartsAndMeanSpacerLength(this.upstreamRegions, idsToMinus10Shifts, idsToMinus35Shifts, params);
        setMotifSearchResults(starts, operons, this.minus10MotifStarts, this.minus35MotifStarts, params);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 70);

        // Color -35 and -10 motifs in the pane with the whole urt region rel. to TSS
        coloredPromotorRegions = colorPromotorMotifRegions(this.upstreamRegions, this.minus10MotifStarts, this.minus35MotifStarts, params);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 80);
        // generating Sequence Logos and adding them into Tabbedpane
        int logoStart10 = Math.round(this.meanMinus10SpacerToTSS + params.getMinusTenMotifWidth());

        this.logoMinus10 = makeSeqLogo(2.0, bioProspOutMinus10, workingDir.getAbsolutePath() + "\\minusTenLogo",
                "PNG", 8.0, -logoStart10, 15, true, true);

        minus10logoLabel = new JLabel();
        Icon icon1 = new ImageIcon(this.logoMinus10.getAbsolutePath() + ".png");
        minus10logoLabel.setIcon(icon1);

        int logoStart35 = Math.round(this.meanMinus35SpacerToMinus10 + params.getMinus35MotifWidth() + logoStart10);
        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 90);

        this.logoMinus35 = makeSeqLogo(2.0, bioProspOutMinus35, workingDir.getAbsolutePath() + "\\minus35Logo",
                "PNG", 8.0, -logoStart35, 15, true, true);

        minus35LogoLabel = new JLabel();
        Icon icon2 = new ImageIcon(this.logoMinus35.getAbsolutePath() + ".png");
        minus35LogoLabel.setIcon(icon2);

        writeInfoFile(info, false, meanMinus10SpacerToTSS, meanMinus35SpacerToMinus10, (int) contributingCitesForMinus10Motif, (int) contributingCitesForMinus35Motif, upstreamRegions.size(), params);
        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 100);
        progressHandlePromotorAnalysis.finish();
    }

    /**
     *
     * @param rbsParams
     */
    public void rbsMotifAnalysis(RbsAnalysisParameters rbsParams, List<TranscriptionStart> starts, List<Operon> operons) {

        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 20);
        Path workingDir = null;
        try {
            workingDir = Files.createTempDirectory("rbsAnalysis_");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        Path rbsBioProspectorInputPath = null;
        Path rbsBioProsFirstHitPath = null;
        Path infoFilePath = null;
        try {
            rbsBioProspectorInputPath = Files.createTempFile(workingDir, "rbsAnalysis_", "SequencesOfIntrest.fna");
            rbsBioProsFirstHitPath = Files.createTempFile(workingDir, "rbsAnalysis_", "BioProspectorBestHit.fna");
            infoFilePath = Files.createTempFile(workingDir, "rbsAnalysis_", "info.txt");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        File parentDir = workingDir.toFile();
        rbsBioProspectorInput = rbsBioProspectorInputPath.toFile();
        rbsBioProsFirstHit = rbsBioProsFirstHitPath.toFile();
        info = infoFilePath.toFile();

        this.rbsStarts = new TreeMap<>();

        // Make a text pane, set its font and color, then add it to the frame
        regionsRelToTLSTextPane = new JTextPane();
        regionsForMotifSearch = new JTextPane();

        writeSeqaForRbsAnalysisInFile(rbsBioProspectorInput, rbsParams);
        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 60);

        runBioProspForRbsAnalysis(parentDir, rbsBioProspectorInput, rbsParams, rbsBioProsFirstHit);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        String header = "";
        int shift = 0;
        for (String string : this.upstreamRegions) {
            if (string.startsWith(">")) {
                header = string.substring(1, string.length() - 1);
                if (contributedSequencesWithShift.containsKey(header)) {
                    shift = contributedSequencesWithShift.get(header);
                }
                try {
                    regionsForMotifSearch.getStyledDocument().insertString(regionsForMotifSearch.getStyledDocument().getLength(), string.toLowerCase(), null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                String subregionForMotifSearch = string.substring(0, string.length() - rbsParams.getMinSpacer() - 1);
                try {
                    regionsForMotifSearch.getStyledDocument().insertString(regionsForMotifSearch.getStyledDocument().getLength(), subregionForMotifSearch.toLowerCase() + "\n", null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (contributedSequencesWithShift.containsKey(header)) {
                    colorSubstringsInStyledDocument(regionsForMotifSearch, font, regionsForMotifSearch.getStyledDocument().getLength() - 1 - subregionForMotifSearch.length() + shift - 1, rbsParams.getMotifWidth(), Color.BLUE);
                }
            }
        }

        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 80);
        try {
            meanSpacerLengthOfRBSMotif = calculateMotifStartsAndMeanSpacerInRbsAnalysis(this.upstreamRegions, this.regionsRelToTLSTextPane, this.contributedSequencesWithShift, rbsParams, starts, operons);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        // generating Sequence Logos and adding them into Tabbedpane
        int logoStart = Math.round(this.meanSpacerLengthOfRBSMotif);
        logoStart += rbsParams.getMotifWidth();

        this.logoRbs = makeSeqLogo(2.0, rbsBioProsFirstHit, parentDir.getAbsolutePath() + "\\RBSLogo",
                "PNG", 8.0, -logoStart, 15, true, true);
        rbsLogoLabel = new JLabel();
        Icon icon = new ImageIcon(this.logoRbs.getAbsolutePath() + ".png");
        rbsLogoLabel.setIcon(icon);

        writeInfoFile(info, true, meanSpacerLengthOfRBSMotif, 0, (int) contributingCitesForRbsMotif, 0, upstreamRegions.size(), rbsParams);
        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 100);
        progressHandleRbsAnalysis.finish();
    }

    /**
     * This method execute the BioProspector binary with the following
     * parameters.
     *
     * @param motifWidth
     * @param noOfCycles
     * @param noOfTopMotifs
     * @param sndMotifBlockWidth
     * @param isMotifBlockPalindrome
     * @param minGap
     * @param maxGap
     * @param justExamineFwd
     * @param everySeqHasMotif
     * @return
     */
    private void executeBioProspector(String inputFilePath, File outputFile,
            int motifWidth, int noOfCycles, int noOfTopMotifs,
            int justExamineFwd) throws IOException {
        this.contributedSequencesWithShift = new TreeMap<>();

        String cmd = "C:\\Users\\jritter\\Documents\\MA-Thesis\\BioProspector.2004\\BioProspector.exe";
//        String cmd = "C:\\BioProspector.2004\\BioProspector.exe";
        List<String> commandArguments = new ArrayList<>();
        commandArguments.add(cmd);
        commandArguments.add("-i");
        commandArguments.add(inputFilePath);
        commandArguments.add("-W");
        commandArguments.add("" + motifWidth);
        commandArguments.add("-n");
        commandArguments.add("" + noOfCycles);
        commandArguments.add("-r");
        commandArguments.add("" + noOfTopMotifs);
        commandArguments.add("-d");
        commandArguments.add("" + justExamineFwd);

        ProcessBuilder ps = new ProcessBuilder(commandArguments);

        //From the DOC:  Initially, this property is false, meaning that the 
        //standard output and error output of a subprocess are sent to two 
        //separate streams
        ps.redirectErrorStream(true);

        Process pr = ps.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
        String id = null;
        String start = "";
        boolean skip = false;

        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "utf-8"));
        while ((line = in.readLine()) != null) {
            if (line.startsWith("Motif#2")) {
                break;
            }
            if (line.startsWith(">")) {
                String[] splitted = line.split("\t");
                start = splitted[splitted.length - 1].substring(2); // shift where the motif starts in input seq.
                id = splitted[0].substring(1); // header
                writer.write(line + "\n");
                skip = true;
            } else if (skip) {
                this.contributedSequencesWithShift.put(id, Integer.valueOf(start));
                writer.write(line + "\n");
                skip = false;
            }
        }

        try {
            pr.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(MotifSearchPanel.class.getName()).log(Level.SEVERE, null, ex);
            in.close();
        } finally {
            writer.close();
        }
    }

    /**
     * This method writes all regions of interest for the Motif detection into a
     * file.
     *
     * @param workDir
     * @param nameOfRegion
     * @param seqs
     * @param noOfBasesToTSS
     * @param spacer
     * @param seqLengthForMotifSearch
     * @param spacer2
     * @param seqLengthForMotifSearch2
     * @param shifts
     * @return the output file containing all subregions needet for motif search
     * as input.
     */
    private void writeSubRegionFor5UTRInFile(File outFile, List<String> seqs,
            int spacer, int seqLengthForMotifSearch, int spacer2,
            int seqLengthForMotifSearch2, TreeMap<String, Integer> alignmentShifts) {

        Writer writer = null;
        int cnt = 1;
        int shift = 0;
        boolean isShifts = false;
        String header = "";
        String substring = "";
        if (alignmentShifts != null) {
            isShifts = true;
        }

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile.getAbsolutePath()), "utf-8"));
            for (String string : seqs) {
                if (cnt == 1) {
                    if (isShifts) {
                        header = string.substring(1, string.length() - 1);
                        if (alignmentShifts.containsKey(header)) {
                            shift = alignmentShifts.get(header);
                        }
                        writer.append(string);
                    } else {
                        writer.append(string);
                    }
                    cnt = 0;
                } else {
                    if (isShifts) {
                        if (alignmentShifts.containsKey(header)) {
                            int stringLength = string.length();
                            int offset = stringLength - (spacer + 1) - (seqLengthForMotifSearch - (shift - 1)) - spacer2 - seqLengthForMotifSearch2;
                            int end = (offset - 1) + (seqLengthForMotifSearch2 + 1);
                            substring = string.substring(offset, end);
                            writer.append(substring + "\n");
                        } else {
                            int stringLength = string.length();
                            int offset = stringLength - this.alternativeSpacer - seqLengthForMotifSearch2;
                            int end = (offset - 1) + (seqLengthForMotifSearch2 + 1);
                            substring = string.substring(offset, end);
                            writer.append(substring + "\n");
                        }
                        minus35AnalysisStrings.add(substring + "\n");
                    } else {
                        int stringLength = string.length();
                        int offset = stringLength - (spacer + 1) - seqLengthForMotifSearch;
                        int end = (offset - 1) + (seqLengthForMotifSearch + 1);
                        substring = string.substring(offset, end);
                        writer.append(substring + "\n");
                        minus10AnalysisStrings.add(substring + "\n");
                    }
                    cnt = 1;
                }
            }

        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
//        return outFile;
    }

    /**
     * This method calculaes the mean spacer betwean the TLS ant the beginning
     * of a RBS-Motif. It also determine the starts of the motifs and presents
     * the values to the colorSubstringsInStyledDocument.
     *
     * @param upstreamRegions List of upstream regions for promotor motif
     * search.
     * @param upstreamRegionTextPane JTextPane where the sequences and motifs
     * are going to be written.
     * @param rbsShifts Shifts of ribosomal binding sites motifs.
     * @param params instance of PromotorSearchParameters.
     * @param tss List of Transcription start sites.
     * @return the mean spacer distance.
     * @throws BadLocationException
     */
    private float calculateMotifStartsAndMeanSpacerInRbsAnalysis(List<String> upstreamRegions, JTextPane upstreamRegionTextPane, TreeMap<String, Integer> rbsShifts, RbsAnalysisParameters params, List<TranscriptionStart> tss, List<Operon> operons) throws BadLocationException {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        int sumOfMinsSpacer = 0;
        Integer shift = 0;
        int minSpacer = params.getMinSpacer();
        int motifWidth = params.getMotifWidth();
        int sequenceOfInterestLength = params.getSeqLengthToAnalyze();

        String header = "";
        for (String string : upstreamRegions) {
            if (string.startsWith(">")) {
                header = string.substring(1, string.length() - 1);
                if (rbsShifts.containsKey(header)) {
                    shift = rbsShifts.get(header);
                    this.contributingCitesForRbsMotif++;
                }
                upstreamRegionTextPane.getStyledDocument().insertString(upstreamRegionTextPane.getStyledDocument().getLength(), string.toLowerCase(), null);
            } else {
                int length = string.length() - 1;
                int documentLenght = upstreamRegionTextPane.getStyledDocument().getLength();
                upstreamRegionTextPane.getStyledDocument().insertString(documentLenght, string.toLowerCase(), null);
                if (rbsShifts.containsKey(header)) {
                    sumOfMinsSpacer += (length - minSpacer - (shift - 1) - motifWidth) + minSpacer;
                    // shift means the actually position in a string, so if 4 is passed
                    // aat S tartg the motif starts at S. => shift minus 1 in the 0 based system
                    // 123 4 56789
                    int colorStart = documentLenght + (shift - 1);
                    int motifStart = sequenceOfInterestLength - (shift - 1);
                    this.rbsStarts.put(header, motifStart);
                    // this method is 0-based so the coloring starts at the 4th. 
                    // Position if you pass 3 as a start position 
                    colorSubstringsInStyledDocument(upstreamRegionTextPane, font, colorStart, motifWidth, Color.BLUE);
                }
            }
        }
//        storeRbsAnalysisResults(this.rbsStarts, tss, rbsShifts, params, operons);
        return sumOfMinsSpacer / contributingCitesForRbsMotif;
    }

    /**
     * This method calculates an the mean spacer-lentght to the -10 and -35
     * region and also generates two TreeMaps which saves the actually start
     * positions of the -10 and -35 Motifs in context of the whole
     * upstreamregion, which is passed to the promotor analysis.
     *
     * @param upstreamRegions List of upstream regions for promotor motif.
     * @param shifts10 shifts to the -10 motif start.
     * @param shifts35 shifts to the -35 motif start.
     * @param params instance of PromotorSearchParameters.
     * @return
     */
    private void calcMotifStartsAndMeanSpacerLength(List<String> upstreamRegions, TreeMap<String, Integer> shifts10,
            TreeMap<String, Integer> shifts35, PromotorSearchParameters params) {
        int sumOfMinus10Spacer = 0;
        int sumOfMinus35Spacer = 0;
        int shiftPosMinus10 = 0;
        int shiftPosMinus35 = 0;
        int length = params.getLengthOfPromotorRegion();
        int spacer1 = params.getMinSpacer1();
        int spacer2 = params.getMinSpacer2();
        int seqWidthToAnalyzeMinus10 = params.getSequenceWidthToAnalyzeMinus10();
        int seqWidthToAnalyzeMinus35 = params.getSequenceWidthToAnalyzeMinus35();
        int motifStartMinus35 = 0;
        String header = "";
        for (String string : upstreamRegions) {
            if (string.startsWith(">")) {
                header = string.substring(1, string.length() - 1);
                if (shifts10.containsKey(header)) {
                    shiftPosMinus10 = shifts10.get(header);
                    this.contributingCitesForMinus10Motif++;
                } else {
                    shiftPosMinus10 = 0;
                }
                if (shifts35.containsKey(header)) {
                    shiftPosMinus35 = shifts35.get(header);
                    this.contributingCitesForMinus35Motif++;
                } else {
                    shiftPosMinus35 = 0;
                }
            } else {
                if (shifts10.containsKey(header)) {
                    sumOfMinus10Spacer += spacer1 + (params.getSequenceWidthToAnalyzeMinus10() - params.getMinusTenMotifWidth() - (shiftPosMinus10 - 1));
                    int motifStartMinus10 = length - spacer1 - seqWidthToAnalyzeMinus10 + shiftPosMinus10;
                    this.minus10MotifStarts.put(header, motifStartMinus10);
                    motifStartMinus35 = motifStartMinus10 - spacer2 - seqWidthToAnalyzeMinus35 + shiftPosMinus35 - 1;
                } else {
                    motifStartMinus35 = length - this.alternativeSpacer - seqWidthToAnalyzeMinus35 + shiftPosMinus35 - 1;
                }
                if (shifts35.containsKey(header)) {
                    this.minus35MotifStarts.put(header, motifStartMinus35);
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
     * analysis.
     * @param minus10Starts
     * @param minus35Starts
     * @param params instance of PromotorSearchParameters. Contains all passed
     * parameters by startign the rbs motif analysis.
     * @return a StyledDocument filled with the upstream regions and toned
     * motifs.
     */
    private StyledDocument colorPromotorMotifRegions(List<String> upstreamRegions, TreeMap<String, Integer> minus10Starts, TreeMap<String, Integer> minus35Starts, PromotorSearchParameters params) {

        JTextPane text = new JTextPane();
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);

        int alignmentStartMinus10 = 0;
        int alignmentStartMinus35 = 0;
        boolean minus10StartExists = false;
        boolean minus35StartExists = false;
        String locus = "";
        for (String string : upstreamRegions) {
            if (string.startsWith(">")) {
                locus = string.substring(1, string.length() - 1);
                if (minus10Starts.containsKey(locus)) {
                    alignmentStartMinus10 = minus10Starts.get(locus);
                    minus10StartExists = true;
                }

                if (minus35Starts.containsKey(locus)) {
                    alignmentStartMinus35 = minus35Starts.get(locus);
                    minus35StartExists = true;
                }
                try {
                    text.getStyledDocument().insertString(text.getStyledDocument().getLength(), string, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                try {
                    text.getStyledDocument().insertString(text.getStyledDocument().getLength(), string.toLowerCase(), null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                int length = text.getStyledDocument().getLength();
                if (minus35StartExists) {
                    int colorStartMinus35 = length - (params.getLengthOfPromotorRegion() - alignmentStartMinus35);
                    colorSubstringsInStyledDocument(text, font, colorStartMinus35 - 2, params.getMinus35MotifWidth(), Color.BLUE);
                    minus35StartExists = false;
                }
                if (minus10StartExists) {
                    int colorStartMinus10 = length - (params.getLengthOfPromotorRegion() - alignmentStartMinus10);
                    colorSubstringsInStyledDocument(text, font, colorStartMinus10 - 2, params.getMinusTenMotifWidth(), Color.RED);
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
     * @param start Start position of coloring.
     * @param length Length of subregion to be toned.
     * @param color Color.
     */
    private void colorSubstringsInStyledDocument(JTextPane textPane, Font font, int start, int length, Color color) {
        // Start with the current input attributes for the JTextPane. This
        // should ensure that we do not wipe out any existing attributes
        // (such as alignment or other paragraph attributes) currently
        // set on the text area.
        MutableAttributeSet attrs = textPane.getInputAttributes();

        // Set the font family, size, and style, based on properties of
        // the Font object. Note that JTextPane supports a number of
        // character attributes beyond those supported by the Font class.
        // For example, underline, strike-through, super- and sub-script.
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);

        // Set the font color
        StyleConstants.setForeground(attrs, color);

        // Retrieve the pane's document object
        StyledDocument doc = textPane.getStyledDocument();
        try {
            // Replace the style for the entire document. We exceed the length
            // of the document by 1 so that text entered at the end of the
            // document uses the attributes.
            String upperCasedString = doc.getText(start, length).toUpperCase();
            doc.remove(start, length);
            doc.insertString(start, upperCasedString, attrs);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Generates a sequence logo from a given input (FASTA format).
     *
     * @param noOfBitsInBar Number of Bits the Logo will be presented.
     * @param inputFile Multiple FASTA file.
     * @param outputFilePath Absolute file path to the destination of the
     * generated Logo.
     * @param outputFormat Possible formats are: "PNG", "JPG" or "SVG".
     * @param hieghtOfLogo Hight of the Logo.
     * @param sequenceStart Start of the X-axis labeling.
     * @param logoWidth Logo width.
     * @param isNumberingOfXAxis Numbering of X-axis if true.
     * @param isYAxis If true, than Y-axis is visible.
     */
    private File makeSeqLogo(Double noOfBitsInBar, File inputFile, String outputFilePath, String outputFormat, Double hieghtOfLogo,
            Integer sequenceStart, Integer logoWidth, boolean isNumberingOfXAxis, boolean isYAxis) {

        String perl = "perl";
        String cmd = "C:\\Users\\jritter\\Documents\\MA-Thesis\\weblogo\\seqlogo";
//        String cmd = "C:\\weblogo\\seqlogo";
        List<String> commandArguments = new ArrayList<>();
        commandArguments.add(perl);
        commandArguments.add(cmd);
        commandArguments.add("-f");
        commandArguments.add(inputFile.getAbsolutePath());
        commandArguments.add("-B");
        commandArguments.add(noOfBitsInBar.toString());
        commandArguments.add("-F");
        commandArguments.add(outputFormat);
        commandArguments.add("-h");
        commandArguments.add(hieghtOfLogo.toString());
        commandArguments.add("-o");
        commandArguments.add(outputFilePath);
        commandArguments.add("-s");
        commandArguments.add(sequenceStart.toString());
        commandArguments.add("-w");
        commandArguments.add(logoWidth.toString());
        commandArguments.add("-x");
        commandArguments.add("average position upstream of TSS");
        commandArguments.add("-y");
        commandArguments.add("bits");
        commandArguments.add("-c");
        if (isNumberingOfXAxis) {
            commandArguments.add("-n");
        }
        if (isYAxis) {
            commandArguments.add("-Y");
        }

        ProcessBuilder ps = new ProcessBuilder(commandArguments);
        ps.redirectErrorStream(true);
        try {
            Process pr = ps.start();
            pr.waitFor();
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        return new File(outputFilePath);

    }

    /**
     * This method gets upstream regions from elements of a spesific type of a
     * detected transcription start sites.
     *
     * @param type ElementsOfInterest specifies the typ of transcription start
     * site.
     * @param starts list of detected transcriptional start site objects.
     * @param length region upstream relative to a TSS
     * @param isRbsAnalysis true if it needs fo the rbs motif analysis, false
     * for promotor motif analysis.
     */
    public void takeUpstreamRegions(ElementsOfInterest type, List<TranscriptionStart> starts, int length, boolean isRbsAnalysis) {

        if (isRbsAnalysis) {
            progressHandleRbsAnalysis.start(100);
            progressHandleRbsAnalysis.progress(10);
        } else {
            progressHandlePromotorAnalysis.start(100);
            progressHandlePromotorAnalysis.progress(10);
        }

        this.upstreamRegions = new ArrayList<>();
        this.upstreamRegionsInHash = new TreeMap<>();
        PersistantFeature currentFeature = null;
        String substr = "";
        int tssStart;
        int featureStart;
        int uniqueIdx = 1;

        if (type == ElementsOfInterest.ALL) {
            for (TranscriptionStart tss : starts) {

                currentFeature = tss.getAssignedFeature();
                String newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                if (isRbsAnalysis) {
                    tss.setRbsSequenceLength(length);
                    tss.setAdditionalIdentyfier(newLocus);

                    // add header in array
                    this.upstreamRegions.add(">" + newLocus + "\n");
                    if (tss.isFwdStrand()) {
                        if (tss.isLeaderless()) {
                            tssStart = tss.getStartPosition();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else {
                        if (tss.isLeaderless()) {
                            tssStart = tss.getStartPosition();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    }
                } else {
                    tss.setPromotorSequenceLength(length);
                    tss.setAdditionalIdentyfier(newLocus);
                    tssStart = tss.getStartPosition();
                    this.upstreamRegions.add(">" + newLocus + "\n");
                    if (tss.isFwdStrand()) {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                        upstreamRegions.add(substr + "\n");
                        this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                    } else {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                        upstreamRegions.add(substr + "\n");
                        this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                    }
                }
                uniqueIdx++;
            }
        } else if (type == ElementsOfInterest.ONLY_ANTISENSE) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                String newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                if (isRbsAnalysis) {
                    // add header in array
                    if (tss.isPutativeAntisense()) {
                        tss.setRbsSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (tss.isPutativeAntisense()) {
                        tss.setPromotorSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        tssStart = tss.getStartPosition();
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else {
                        continue;
                    }
                }
                uniqueIdx++;
            }
        } else if (type == ElementsOfInterest.ONLY_LEADERLESS) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                String newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                tssStart = tss.getStartPosition();
                tss.setAdditionalIdentyfier(newLocus);
                if (tss.isLeaderless()) {

                    if (isRbsAnalysis) {
                        tss.setRbsSequenceLength(length);
                    } else {
                        tss.setPromotorSequenceLength(length);
                    }
                    this.upstreamRegions.add(">" + newLocus + "\n");
                    if (tss.isFwdStrand()) {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                        upstreamRegions.add(substr + "\n");
                        this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                    } else {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                        upstreamRegions.add(substr + "\n");
                        this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                    }
                } else {
                    continue;
                }
                uniqueIdx++;
            }
        } else if (type == ElementsOfInterest.ONLY_NONE_LEADERLESS) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                String newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                if (isRbsAnalysis) {
                    // add header in array
                    if (!tss.isLeaderless()) {
                        tss.setRbsSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (!tss.isLeaderless()) {
                        tss.setPromotorSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        tssStart = tss.getStartPosition();
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else {
                        continue;
                    }
                }
                uniqueIdx++;
            }
        } else if (type == ElementsOfInterest.ONLY_REAL_TSS) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                String newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                if (isRbsAnalysis) {
                    // add header in array
                    if (!tss.isPutativeAntisense() && !tss.isLeaderless() && !tss.isInternalTSS()) {
                        tss.setRbsSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (!tss.isPutativeAntisense() && !tss.isLeaderless() && !tss.isInternalTSS()) {
                        tss.setPromotorSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        tssStart = tss.getStartPosition();
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else {
                        continue;
                    }
                }
                uniqueIdx++;
            }
        } else if (type == ElementsOfInterest.ONLY_SELECTED) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                String newLocus = currentFeature.getLocus() + "_" + uniqueIdx;
                tssStart = tss.getStartPosition();
                if (isRbsAnalysis) {
                    // add header in array
                    if (tss.isLeaderless() && tss.isSelected()) {
                        tss.setRbsSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    } else if (!tss.isLeaderless() && tss.isSelected()) {
                        tss.setRbsSequenceLength(length);
                        tss.setAdditionalIdentyfier(newLocus);
                        this.upstreamRegions.add(">" + newLocus + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                            this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        }
                    }
                } else if (tss.isSelected()) {
                    tss.setPromotorSequenceLength(length);
                    tss.setAdditionalIdentyfier(newLocus);
                    this.upstreamRegions.add(">" + newLocus + "\n");
                    System.out.println(">" + currentFeature.getLocus());
                    if (tss.isFwdStrand()) {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                        upstreamRegions.add(substr + "\n");
                        this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        System.out.println(substr);
                    } else {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                        upstreamRegions.add(substr + "\n");
                        this.upstreamRegionsInHash.put(newLocus, substr + "\n");
                        System.out.println(substr);
                    }
                }
                uniqueIdx++;
            }
        }
    }

    /**
     *
     * @param rbsBioProspectorInput
     * @param regionsForMotifSearch
     * @param rbsParams
     */
    private void writeBioProspInputFromRbsAalysis(File rbsBioProspectorInput, StyledDocument regionsForMotifSearch, RbsAnalysisParameters rbsParams) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(rbsBioProspectorInput.getAbsolutePath()), "utf-8"));

            for (String region : this.upstreamRegions) {
                if (region.startsWith(">")) {
                    writer.write(region);
                    regionsForMotifSearch.insertString(regionsForMotifSearch.getLength(), region, null);
                } else {
                    String subregionForMotifSearch = region.substring(0, region.length() - rbsParams.getMinSpacer() - 1);
                    writer.write(subregionForMotifSearch + "\n");
                    regionsForMotifSearch.insertString(regionsForMotifSearch.getLength(), subregionForMotifSearch + "\n", null);
                }
            }
            writer.close();

        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException | BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     *
     * @param rbsBioProspectorInput
     * @param rbsParams
     */
    private void writeSeqaForRbsAnalysisInFile(File rbsBioProspectorInput, RbsAnalysisParameters rbsParams) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(rbsBioProspectorInput.getAbsolutePath()), "utf-8"));

            for (String region : this.upstreamRegions) {
                if (region.startsWith(">")) {
                    writer.write(region);
                } else {
                    String subregionForMotifSearch = region.substring(0, region.length() - rbsParams.getMinSpacer() - 1);
                    writer.write(subregionForMotifSearch + "\n");
                }
            }
            writer.close();

        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void takeSubRegionsForOperonAnalysis(List<Operon> operons, int length, boolean isRbsAnalysis) {
        if (isRbsAnalysis) {
            progressHandleRbsAnalysis.start(100);
            progressHandleRbsAnalysis.progress(10);
        } else {
            progressHandlePromotorAnalysis.start(100);
            progressHandlePromotorAnalysis.progress(10);
        }

        this.upstreamRegions = new ArrayList<>();
        this.upstreamRegionsInHash = new TreeMap<>();
        PersistantFeature leadingFeature = null;
        String substr = "";
        int featureStart;
        int uniqueIdx = 1;

        for (Operon operon : operons) {

            leadingFeature = operon.getOperonAdjacencies().get(0).getFeature1();
            String uniqueLocus = leadingFeature.getLocus() + "_" + uniqueIdx;
            if (isRbsAnalysis) {
                operon.setRbsSequenceLength(length);
                operon.setAdditionalLocus(uniqueLocus);

                // add header in array
                this.upstreamRegions.add(">" + uniqueLocus + "\n");
                if (operon.isFwd()) {
                    featureStart = leadingFeature.getStart();
                    substr = this.chromosomes.get(leadingFeature.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                    upstreamRegions.add(substr + "\n");
                    this.upstreamRegionsInHash.put(uniqueLocus, substr + "\n");
                } else {
                    featureStart = leadingFeature.getStop();
                    substr = this.chromosomes.get(leadingFeature.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                    upstreamRegions.add(substr + "\n");
                    this.upstreamRegionsInHash.put(uniqueLocus, substr + "\n");
                }
            } else {
                operon.setPromotorSequenceLength(length);
                operon.setAdditionalLocus(uniqueLocus);
                int transkriptStart = operon.getStartPositionOfTranscript();
                this.upstreamRegions.add(">" + uniqueLocus + "\n");
                if (operon.isFwd()) {
                    substr = this.chromosomes.get(leadingFeature.getChromId()).getSequence(this).substring(transkriptStart - (length + 1), transkriptStart - 1);
                    upstreamRegions.add(substr + "\n");
                    this.upstreamRegionsInHash.put(uniqueLocus, substr + "\n");
                } else {
                    substr = this.chromosomes.get(leadingFeature.getChromId()).getSequence(this).substring(transkriptStart + 1, transkriptStart + (length + 1));
                    upstreamRegions.add(substr + "\n");
                    this.upstreamRegionsInHash.put(uniqueLocus, substr + "\n");
                }
            }
            uniqueIdx++;
        }
    }

    /**
     * Wrapper method to start the executeBioProspector method.
     *
     * @param workingDir Current working directory File instance.
     * @param rbsBioProspectorInput
     * @param bioProspectorOut
     * @param rbsParams
     * @param rbsBioProsFirstHit File instance, contains the parsed output (best
     * hit from BioProspector).
     */
    private void runBioProspForRbsAnalysis(File workingDir, File rbsBioProspectorInput, RbsAnalysisParameters rbsParams, File rbsBioProsFirstHit) {
        if (workingDir.isDirectory()) {
            String posixPath = "/cygdrive/c";
            String sub = rbsBioProspectorInput.getAbsolutePath().toString().substring(2);
            posixPath += sub.replaceAll("\\\\", "/");

            try {
                this.executeBioProspector(
                        posixPath, rbsBioProsFirstHit, rbsParams.getMotifWidth(),
                        rbsParams.getNumberOfCyclesForBioProspector(),
                        1, 1);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Each TSS-element on which the promotor analysis was performed gets
     * positions at which the -10 and -35 motif starts and which length the
     * motifs have.
     *
     * @param starts List of Transcription start site instances.
     * @param minus10Starts
     * @param minus35Starts
     * @param params
     */
    private void setMotifSearchResults(List<TranscriptionStart> starts, List<Operon> operons, TreeMap<String, Integer> minus10Starts, TreeMap<String, Integer> minus35Starts, PromotorSearchParameters params) {

        if (starts != null) {
            // Setting start positions of Promotorelements (-35 -10) to TSS objects
            // needed, when exporting to feature tables.
            HashMap<String, TranscriptionStart> startsInTreeMap = new HashMap<>();
            for (TranscriptionStart tss : starts) {
                if (tss.getAdditionalIdentyfier() != null) {
                    startsInTreeMap.put(tss.getAdditionalIdentyfier(), tss);
                }
            }
            for (int i = 0; i < this.upstreamRegions.size(); i++) {
                String str = this.upstreamRegions.get(i);
                String locus;
                if (str.startsWith(">")) {
                    locus = str.substring(1, str.length() - 1);
                    if (startsInTreeMap.containsKey(locus.toString())) {
                        TranscriptionStart start = startsInTreeMap.get(locus);
                        if (minus10Starts.containsKey(locus)) {
                            start.setPromotorFeaturesAssigned(true);
                            start.setStartMinus10Motif(minus10Starts.get(locus));
                            start.setMinus10MotifWidth(params.getMinusTenMotifWidth());
                        }
                        if (minus35Starts.containsKey(locus)) {
                            start.setPromotorFeaturesAssigned(true);
                            start.setStartMinus35Motif(minus35Starts.get(locus));
                            start.setMinus35MotifWidth(params.getMinus35MotifWidth());
                        }
                        startsInTreeMap.put(locus, start);
                    }
                } else {
                    continue;
                }
            }
        } else if (operons != null) {
            HashMap<String, Operon> operonsInTreeMap = new HashMap<>();
            for (Operon op : operons) {
                if (op.getAdditionalLocus() != null) {
                    operonsInTreeMap.put(op.getAdditionalLocus(), op);
                }
            }
            for (int i = 0; i < this.upstreamRegions.size(); i++) {
                String str = this.upstreamRegions.get(i);
                String locus;
                if (str.startsWith(">")) {
                    locus = str.substring(1, str.length() - 1);
                    if (operonsInTreeMap.containsKey(locus.toString())) {
                        Operon start = operonsInTreeMap.get(locus);
                        if (minus10Starts.containsKey(locus)) {
                            start.setHasPromtorFeaturesAssigned(true);
                            start.setStartMinus10Motif(minus10Starts.get(locus));
                            start.setMinus10MotifWidth(params.getMinusTenMotifWidth());
                        }
                        if (minus35Starts.containsKey(locus)) {
                            start.setHasPromtorFeaturesAssigned(true);
                            start.setStartMinus35Motif(minus35Starts.get(locus));
                            start.setMinus35MotifWidth(params.getMinus35MotifWidth());
                        }
                        operonsInTreeMap.put(locus, start);
                    }
                } else {
                    continue;
                }
            }
        }

    }

    /**
     *
     * @param upstreamRegions
     * @param minus10Starts
     * @param minus35Starts
     * @param minus10Shifts
     * @param minus35Shifts
     * @param params
     * @param tss
     * @param operons
     */
    public void storePromoterAnalysisResults(List<String> upstreamRegions, TreeMap<String, Integer> minus10Starts, TreeMap<String, Integer> minus35Starts, TreeMap<String, Integer> minus10Shifts, TreeMap<String, Integer> minus35Shifts, PromotorSearchParameters params, List<TranscriptionStart> tss, List<Operon> operons) {
        if (tss != null) {
            HashMap<String, TranscriptionStart> tssInTreeMap = new HashMap<>();
            for (TranscriptionStart ts : tss) {
                if (ts.getAdditionalIdentyfier() != null) {
                    tssInTreeMap.put(ts.getAdditionalIdentyfier(), ts);
                    // reset
                    ts.setPromotorFeaturesAssigned(false);
                }
            }

            for (int i = 0; i < upstreamRegions.size(); i++) {
                String str = upstreamRegions.get(i);
                String locus;
                if (str.startsWith(">")) {
                    locus = str.substring(1, str.length() - 1);
                    if (tssInTreeMap.containsKey(locus.toString())) {
                        TranscriptionStart start = tssInTreeMap.get(locus);
                        if (minus10Shifts.containsKey(locus)) {
                            start.setPromotorFeaturesAssigned(true);
                            start.setStartMinus10Motif(minus10Starts.get(locus));
                            start.setMinus10MotifWidth(params.getMinusTenMotifWidth());
                        }
                        if (minus35Shifts.containsKey(locus)) {
                            start.setPromotorFeaturesAssigned(true);
                            start.setStartMinus10Motif(minus35Starts.get(locus));
                            start.setMinus10MotifWidth(params.getMinus35MotifWidth());
                        }
                        tssInTreeMap.put(locus, start);
                    }
                }
            }
        } else if (operons != null) {
            HashMap<String, Operon> operonsInTreeMap = new HashMap<>();
            for (Operon op : operons) {
                if (op.getAdditionalLocus() != null) {
                    operonsInTreeMap.put(op.getAdditionalLocus(), op);
                    // reset
                    op.setHasPromtorFeaturesAssigned(false);
                }
            }

            for (int i = 0; i < upstreamRegions.size(); i++) {
                String str = upstreamRegions.get(i);
                String locus;
                if (str.startsWith(">")) {
                    locus = str.substring(1, str.length() - 1);
                    if (operonsInTreeMap.containsKey(locus.toString())) {
                        Operon op = operonsInTreeMap.get(locus);
                        if (minus10Shifts.containsKey(locus)) {
                            op.setHasPromtorFeaturesAssigned(true);
                            op.setStartMinus10Motif(minus10Starts.get(locus));
                            op.setMinus10MotifWidth(params.getMinusTenMotifWidth());
                        }

                        if (minus35Shifts.containsKey(locus)) {
                            op.setHasPromtorFeaturesAssigned(true);
                            op.setStartMinus10Motif(minus35Starts.get(locus));
                            op.setMinus10MotifWidth(params.getMinus35MotifWidth());
                        }
                        operonsInTreeMap.put(locus, op);
                    }
                }
            }
        }
    }

    /**
     *
     * @param upstreamRegions
     * @param rbsStarts
     * @param rbsShifts
     * @param params
     * @param tss
     * @param operons
     */
    public void storeRbsAnalysisResults(List<String> upstreamRegions, TreeMap<String, Integer> rbsStarts, TreeMap<String, Integer> rbsShifts, RbsAnalysisParameters params, List<TranscriptionStart> tss, List<Operon> operons) {
        if (tss != null) {
            HashMap<String, TranscriptionStart> tssInTreeMap = new HashMap<>();
            for (TranscriptionStart ts : tss) {
                if (ts.getAdditionalIdentyfier() != null) {
                    tssInTreeMap.put(ts.getAdditionalIdentyfier(), ts);
                    // reset
                    ts.setRbsFeatureAssigned(false);
                }
            }

            for (int i = 0; i < upstreamRegions.size(); i++) {
                String str = upstreamRegions.get(i);
                String locus;
                if (str.startsWith(">")) {
                    locus = str.substring(1, str.length() - 1);
                    if (tssInTreeMap.containsKey(locus.toString())) {
                        TranscriptionStart start = tssInTreeMap.get(locus);
                        if (rbsShifts.containsKey(locus)) {
                            start.setRbsFeatureAssigned(true);
                            start.setStartRbsMotif(rbsStarts.get(locus));
                            start.setRbsMotifWidth(params.getMotifWidth());
                        }
                        tssInTreeMap.put(locus, start);
                    }
                }
            }
        } else if (operons != null) {
            HashMap<String, Operon> operonsInTreeMap = new HashMap<>();
            for (Operon op : operons) {
                if (op.getAdditionalLocus() != null) {
                    operonsInTreeMap.put(op.getAdditionalLocus(), op);
                    // reset
                    op.setRbsFeatureAssigned(false);
                }
            }

            for (int i = 0; i < upstreamRegions.size(); i++) {
                String str = upstreamRegions.get(i);
                String locus;
                if (str.startsWith(">")) {
                    locus = str.substring(1, str.length() - 1);
                    if (operonsInTreeMap.containsKey(locus.toString())) {
                        Operon op = operonsInTreeMap.get(locus);
                        if (rbsShifts.containsKey(locus)) {
                            op.setRbsFeatureAssigned(true);
                            op.setStartRbsMotif(rbsStarts.get(locus));
                            op.setRbsMotifWidth(params.getMotifWidth());
                        }
                        operonsInTreeMap.put(locus, op);
                    }
                }
            }
        }
    }

    @Override
    public void update(Object args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public TreeMap<String, Integer> getContributedSequencesWithShift() {
        return contributedSequencesWithShift;
    }

    public void setContributedSequencesWithShift(TreeMap<String, Integer> contributedSequencesWithShift) {
        this.contributedSequencesWithShift = contributedSequencesWithShift;
    }

    public File getLogoMinus10() {
        return logoMinus10;
    }

    public JLabel getMinus35LogoLabel() {
        return minus35LogoLabel;
    }

    public void setMinus35LogoLabel(JLabel minus35LogoLabel) {
        this.minus35LogoLabel = minus35LogoLabel;
    }

    public void setLogoMinus10(File logoMinus10) {
        this.logoMinus10 = logoMinus10;
    }

    public File getLogoMinus35() {
        return logoMinus35;
    }

    public void setLogoMinus35(File logoMinus35) {
        this.logoMinus35 = logoMinus35;
    }

    public JLabel getMinus10logoLabel() {
        return minus10logoLabel;
    }

    public void setMinus10logoLabel(JLabel minus10logoLabel) {
        this.minus10logoLabel = minus10logoLabel;
    }

    public File getLogoRbs() {
        return logoRbs;
    }

    public void setLogoRbs(File logoRbs) {
        this.logoRbs = logoRbs;
    }

    public List<String> getUpstreamRegions() {
        return upstreamRegions;
    }

    public void setUpstreamRegions(List<String> upstreamRegions) {
        this.upstreamRegions = upstreamRegions;
    }

    public List<String> getMinus10AnalysisStrings() {
        return minus10AnalysisStrings;
    }

    public void setMinus10AnalysisStrings(List<String> minus10AnalysisStrings) {
        this.minus10AnalysisStrings = minus10AnalysisStrings;
    }

    public List<String> getMinus35AnalysisStrings() {
        return minus35AnalysisStrings;
    }

    public void setMinus35AnalysisStrings(List<String> minus35AnalysisStrings) {
        this.minus35AnalysisStrings = minus35AnalysisStrings;
    }

    public float getContributingCitesForMinus10Motif() {
        return contributingCitesForMinus10Motif;
    }

    public void setContributingCitesForMinus10Motif(float contributingCitesForMinus10Motif) {
        this.contributingCitesForMinus10Motif = contributingCitesForMinus10Motif;
    }

    public float getContributingCitesForMinus35Motif() {
        return contributingCitesForMinus35Motif;
    }

    public void setContributingCitesForMinus35Motif(float contributingCitesForMinus35Motif) {
        this.contributingCitesForMinus35Motif = contributingCitesForMinus35Motif;
    }

    public float getContributingCitesForRbsMotif() {
        return contributingCitesForRbsMotif;
    }

    public void setContributingCitesForRbsMotif(float contributingCitesForRbsMotif) {
        this.contributingCitesForRbsMotif = contributingCitesForRbsMotif;
    }

    public int getAlternativeSpacer() {
        return alternativeSpacer;
    }

    public void setAlternativeSpacer(int alternativeSpacer) {
        this.alternativeSpacer = alternativeSpacer;
    }

    public TreeMap<String, Integer> getMinus10MotifStarts() {
        return minus10MotifStarts;
    }

    public void setMinus10MotifStarts(TreeMap<String, Integer> minus10MotifStarts) {
        this.minus10MotifStarts = minus10MotifStarts;
    }

    public TreeMap<String, Integer> getMinus35MotifStarts() {
        return minus35MotifStarts;
    }

    public void setMinus35MotifStarts(TreeMap<String, Integer> minus35MotifStarts) {
        this.minus35MotifStarts = minus35MotifStarts;
    }

    public TreeMap<String, Integer> getRbsStarts() {
        return rbsStarts;
    }

    public void setRbsStarts(TreeMap<String, Integer> rbsStarts) {
        this.rbsStarts = rbsStarts;
    }

    public File getRbsBioProspectorInput() {
        return rbsBioProspectorInput;
    }

    public void setRbsBioProspectorInput(File rbsBioProspectorInput) {
        this.rbsBioProspectorInput = rbsBioProspectorInput;
    }

    public File getRbsBioProsFirstHit() {
        return rbsBioProsFirstHit;
    }

    public void setRbsBioProsFirstHit(File rbsBioProsFirstHit) {
        this.rbsBioProsFirstHit = rbsBioProsFirstHit;
    }

    public TreeMap<String, String> getUpstreamRegionsInHash() {
        return upstreamRegionsInHash;
    }

    public void setUpstreamRegionsInHash(TreeMap<String, String> upstreamRegionsInHash) {
        this.upstreamRegionsInHash = upstreamRegionsInHash;
    }

    public float getMeanMinus10SpacerToTSS() {
        return meanMinus10SpacerToTSS;
    }

    public void setMeanMinus10SpacerToTSS(float meanMinus10SpacerToTSS) {
        this.meanMinus10SpacerToTSS = meanMinus10SpacerToTSS;
    }

    public float getMeanMinus35SpacerToMinus10() {
        return meanMinus35SpacerToMinus10;
    }

    public void setMeanMinus35SpacerToMinus10(float meanMinus35SpacerToMinus10) {
        this.meanMinus35SpacerToMinus10 = meanMinus35SpacerToMinus10;
    }

    public float getMeanSpacerLengthOfRBSMotif() {
        return meanSpacerLengthOfRBSMotif;
    }

    public void setMeanSpacerLengthOfRBSMotif(float meanSpacerLengthOfRBSMotif) {
        this.meanSpacerLengthOfRBSMotif = meanSpacerLengthOfRBSMotif;
    }

    public JTextPane getRegionsRelToTLSTextPane() {
        return regionsRelToTLSTextPane;
    }

    public void setRegionsRelToTLSTextPane(JTextPane regionsRelToTLSTextPane) {
        this.regionsRelToTLSTextPane = regionsRelToTLSTextPane;
    }

    public JTextPane getRegionsForMotifSearch() {
        return regionsForMotifSearch;
    }

    public void setRegionsForMotifSearch(JTextPane regionsForMotifSearch) {
        this.regionsForMotifSearch = regionsForMotifSearch;
    }

    public JLabel getRbsLogoLabel() {
        return rbsLogoLabel;
    }

    public void setRbsLogoLabel(JLabel rbsLogoLabel) {
        this.rbsLogoLabel = rbsLogoLabel;
    }

    public File getMinus10Input() {
        return minus10Input;
    }

    public void setMinus10Input(File minus10Input) {
        this.minus10Input = minus10Input;
    }

    public File getMinus35Input() {
        return minus35Input;
    }

    public void setMinus35Input(File minus35Input) {
        this.minus35Input = minus35Input;
    }

    public File getBioProspOutMinus10() {
        return bioProspOutMinus10;
    }

    public void setBioProspOutMinus10(File bioProspOutMinus10) {
        this.bioProspOutMinus10 = bioProspOutMinus10;
    }

    public File getBioProspOutMinus35() {
        return bioProspOutMinus35;
    }

    public void setBioProspOutMinus35(File bioProspOutMinus35) {
        this.bioProspOutMinus35 = bioProspOutMinus35;
    }

    public JTextPane getRegionOfIntrestMinus10() {
        return regionOfIntrestMinus10;
    }

    public void setRegionOfIntrestMinus10(JTextPane regionOfIntrestMinus10) {
        this.regionOfIntrestMinus10 = regionOfIntrestMinus10;
    }

    public JTextPane getRegionOfIntrestMinus35() {
        return regionOfIntrestMinus35;
    }

    public void setRegionOfIntrestMinus35(JTextPane regionOfIntrestMinus35) {
        this.regionOfIntrestMinus35 = regionOfIntrestMinus35;
    }

    public TreeMap<String, Integer> getIdsToMinus10Shifts() {
        return idsToMinus10Shifts;
    }

    public void setIdsToMinus10Shifts(TreeMap<String, Integer> idsToMinus10Shifts) {
        this.idsToMinus10Shifts = idsToMinus10Shifts;
    }

    public TreeMap<String, Integer> getIdsToMinus35Shifts() {
        return idsToMinus35Shifts;
    }

    public void setIdsToMinus35Shifts(TreeMap<String, Integer> idsToMinus35Shifts) {
        this.idsToMinus35Shifts = idsToMinus35Shifts;
    }

    public StyledDocument getColoredPromotorRegions() {
        return coloredPromotorRegions;
    }

    public void setColoredPromotorRegions(StyledDocument coloredPromotorRegions) {
        this.coloredPromotorRegions = coloredPromotorRegions;
    }

    private void writeInfoFile(File file, boolean isRbs, float meanSpacer1, float meanspacer2, int contributedSegmentsToFstMotif, int contributedSegmentsToSndMotif, int noOfSequences, ParameterSetI<Object> params) {
        PromotorSearchParameters promotorParams = null;
        RbsAnalysisParameters rbsParams = null;
        if (isRbs) {
            rbsParams = (RbsAnalysisParameters) params;
        } else {
            promotorParams = (PromotorSearchParameters) params;
        }

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file.getAbsolutePath()), "utf-8"));

            if (isRbs) {
                writer.write("Infos to RBS analysis\n");
                writer.write("Length of upstream regions relative to TLS taken for analysis: " + rbsParams.getSeqLengthToAnalyze() + "\n");
                writer.write("Min. Spacer: " + rbsParams.getMinSpacer() + "\n");
                writer.write("Motif width: " + rbsParams.getMotifWidth() + "\n");
                writer.write("Mean spacer width relative to TLS: " + meanSpacer1 + "\n");
                writer.write("Number of contributed Segments to Motif: " + contributedSegmentsToFstMotif + "/" + noOfSequences + "\n");
            } else {
                writer.write("Infos to Promotor analysis\n");
                writer.write("Length of upstream regions relative to TLS taken for analysis: " + promotorParams.getLengthOfPromotorRegion() + "\n");
                writer.write("Min. spacer 1: " + promotorParams.getMinSpacer1() + "\n");
                writer.write("Mean spacer 1: " + meanSpacer1 + "\n");
                writer.write("Min. spacer 2: " + promotorParams.getMinSpacer2() + "\n");
                writer.write("Mean spacer 2: " + meanspacer2 + "\n");
                writer.write("-10 motif width: " + promotorParams.getMinusTenMotifWidth() + "\n");
                writer.write("-35 motif width: " + promotorParams.getMinus35MotifWidth() + "\n");
                writer.write("Number of contributed Segments to -10 Motif: " + contributedSegmentsToFstMotif + "/" + noOfSequences + "\n");
                writer.write("Number of contributed Segments to -35 Motif: " + contributedSegmentsToSndMotif + "/" + noOfSequences + "\n");
            }
            writer.close();

        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public File getInfo() {
        return info;
    }

    public void setInfo(File info) {
        this.info = info;
    }

}
