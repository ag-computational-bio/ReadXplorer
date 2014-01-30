package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;

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

    private RbsMotifSearchPanel rbsMotifSearchPanel;
    private TreeMap<String, String> upstreamRegionsInHash;
    private TreeMap<String, Integer> contributedSequencesWithShift;
    private File logoMinus10, logoMinus35, logoRbs;
    private HashMap<Integer, PersistantChromosome> chromosomes;
    private List<String> upstreamRegions;
    private float meanMinus10SpacerToTSS, meanMinus35SpacerToMinus10;
    private float meanSpacerLengthOfRBSMotif;
    private final ProgressHandle progressHandlePromotorAnalysis, progressHandleRbsAnalysis;
    private String handlerTitlePromotorAnalysis, handlerTitleRBSAnalysis;
    private List<String> minus10AnalysisStrings, minus35AnalysisStrings;
    private float contributingCitesForMinus10Motif, contributingCitesForMinus35Motif, contributingCitesForRbsMotif;
    private int alternativeSpacer;
    private TreeMap<String, Integer> minus10MotifStarts, minus35MotifStarts, rbsStarts;

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
    public MotifSearchPanel utrPromotorAnalysis(PromotorSearchParameters params, List<TranscriptionStart> starts, List<Operon> operons) {

        this.alternativeSpacer = params.getAlternativeSpacer();
        minus10MotifStarts = new TreeMap<>();
        minus35MotifStarts = new TreeMap<>();
        MotifSearchPanel promotorMotifSearchPanel = new MotifSearchPanel();
        promotorMotifSearchPanel.setMinus35MotifWidth(params.getMinus35MotifWidth());
        promotorMotifSearchPanel.setMinus10MotifWidth(params.getMinusTenMotifWidth());
        this.minus10AnalysisStrings = new ArrayList<>();
        this.minus35AnalysisStrings = new ArrayList<>();
        this.progressHandlePromotorAnalysis.progress("processing promotor analysis ...", 20);

        JTextPane regionOfIntrestMinus10 = new JTextPane();
        JTextPane regionOfIntrestMinus35 = new JTextPane();

        Path workingDirPath = null;
        Path bioProspectorOutMinus10Path = null;
        Path bioProspectorOutMinus35Path = null;
        Path minus10InputPath = null;
        Path minus35InputPath = null;
        try {
            workingDirPath = Files.createTempDirectory("promotorAnalysis_");
            bioProspectorOutMinus10Path = Files.createTempFile(workingDirPath, "promotorAnalysis_", "bioProspectorOutMinus10.fna");
            bioProspectorOutMinus35Path = Files.createTempFile(workingDirPath, "promotorAnalysis_", "bioProspectorOutMinus35.fna");
            minus10InputPath = Files.createTempFile(workingDirPath, "promotorAnalysis_", "inputBioProspectorMinus10.fna");
            minus35InputPath = Files.createTempFile(workingDirPath, "promotorAnalysis_", "inputBioProspectorMinus35.fna");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        File workingDir = workingDirPath.toFile();
        File minus10Input = minus10InputPath.toFile();
        File minus35Input = minus35InputPath.toFile();
        File bioProspOutMinus10 = bioProspectorOutMinus10Path.toFile();
        File bioProspOutMinus35 = bioProspectorOutMinus35Path.toFile();

        promotorMotifSearchPanel.setMinus10Input(minus10Input);
        promotorMotifSearchPanel.setMinus35Input(minus35Input);
        promotorMotifSearchPanel.setBioProspOutMinus10(bioProspOutMinus10);
        promotorMotifSearchPanel.setBioProspOutMinus35(bioProspOutMinus35);

        // 1. write all upstream subregions for -10 analysis
        File minusTenFile = writeSubRegionFor5UTRInFile(
                workingDir, minus10Input, this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                null);

        // BioProspector search for -10 motif
        String posixPath = "/cygdrive/c";
        String sub = minusTenFile.getAbsolutePath().toString().substring(2);
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

//        List<List<Object>> alignmentshiftsOFMinusTenArray = new ArrayList<>();
//        alignmentshiftsOFMinusTenArray.addAll(this.bioProspectorOutArray);
        TreeMap<String, Integer> idsToMinus10Shifts = new TreeMap<>();
        idsToMinus10Shifts.putAll(this.contributedSequencesWithShift);

        // 4. All sequences, which did not contain a motif in the first run for-10 motif serch
        // will be descard in the next step of the analysis of the -35 motif search

        File minusThirtyFiveFile = writeSubRegionFor5UTRInFile(
                workingDir, minus35Input, this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                idsToMinus10Shifts);
//                alignmentshiftsOFMinusTenArray, regionOfIntrestMinus10, regionOfIntrestMinus35);

        posixPath = "/cygdrive/c";
        sub = minusThirtyFiveFile.getAbsolutePath().toString().substring(2);
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

        TreeMap<String, Integer> idsToMinus35Shifts = new TreeMap<>();
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

        promotorMotifSearchPanel.setStyledDocumentToRegionOfIntrestMinusTen(regionOfIntrestMinus10.getStyledDocument());
        promotorMotifSearchPanel.setStyledDocumentToRegionOfIntrestMinus35(regionOfIntrestMinus35.getStyledDocument());

        calcMotifStartsAndMeanSpacerLength(this.upstreamRegions, idsToMinus10Shifts, idsToMinus35Shifts, params);
        setMotifSearchResults(starts, operons, this.minus10MotifStarts, this.minus35MotifStarts, params);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 70);

        // Color -35 and -10 motifs in the pane with the whole urt region rel. to TSS
        StyledDocument coloredPromotorRegions = colorPromotorMotifRegions(this.upstreamRegions, this.minus10MotifStarts, this.minus35MotifStarts, params);

        promotorMotifSearchPanel.setStyledDocToPromotorsFastaPane(coloredPromotorRegions);

        promotorMotifSearchPanel.setContributionMinus10Label("Number of segments contributes to the motif: "
                + (int) this.contributingCitesForMinus10Motif + "/" + this.upstreamRegions.size() / 2);
        promotorMotifSearchPanel.setContributionMinus35Label("Number of segments contributes to the motif: "
                + (int) this.contributingCitesForMinus35Motif + "/" + this.upstreamRegions.size() / 2);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 80);
        // generating Sequence Logos and adding them into Tabbedpane
        int logoStart10 = Math.round(this.meanMinus10SpacerToTSS + params.getMinusTenMotifWidth());
        String roundedMean = String.format("%.1f", this.meanMinus10SpacerToTSS);
        promotorMotifSearchPanel.setMinSpacer1LengthToLabel(roundedMean);

        this.logoMinus10 = makeSeqLogo(2.0, bioProspOutMinus10, workingDir.getAbsolutePath() + "\\minusTenLogo",
                "PNG", 8.0, -logoStart10, 15, true, true);

        promotorMotifSearchPanel.setLogoMinus10(new File(this.logoMinus10.getAbsolutePath() + ".png"));
        JLabel logoLabel1 = new JLabel();
        Icon icon1 = new ImageIcon(this.logoMinus10.getAbsolutePath() + ".png");
        logoLabel1.setIcon(icon1);
        promotorMotifSearchPanel.setMinus10LogoToPanel(logoLabel1);


        int logoStart35 = Math.round(this.meanMinus35SpacerToMinus10 + params.getMinus35MotifWidth() + logoStart10);
        roundedMean = String.format("%.1f", this.meanMinus35SpacerToMinus10);
        promotorMotifSearchPanel.setMinSpacer2LengthToLabel(roundedMean);
        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 90);

        this.logoMinus35 = makeSeqLogo(2.0, bioProspOutMinus35, workingDir.getAbsolutePath() + "\\minus35Logo",
                "PNG", 8.0, -logoStart35, 15, true, true);


        promotorMotifSearchPanel.setLogoMinus35(new File(this.logoMinus35.getAbsolutePath() + ".png"));
        JLabel logoLabel2 = new JLabel();
        Icon icon2 = new ImageIcon(this.logoMinus35.getAbsolutePath() + ".png");
        logoLabel2.setIcon(icon2);
        promotorMotifSearchPanel.setMinus35LogoToPanel(logoLabel2);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 100);
        progressHandlePromotorAnalysis.finish();
        return promotorMotifSearchPanel;
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
        try {
            rbsBioProspectorInputPath = Files.createTempFile(workingDir, "rbsAnalysis_", "SequencesOfIntrest.fna");
            rbsBioProsFirstHitPath = Files.createTempFile(workingDir, "rbsAnalysis_", "BioProspectorBestHit.fna");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        File parentDir = workingDir.toFile();
        File rbsBioProspectorInput = rbsBioProspectorInputPath.toFile();
        File rbsBioProsFirstHit = rbsBioProsFirstHitPath.toFile();



        System.out.println("WOrkingDir: " + parentDir.getAbsolutePath());
        System.out.println("rbsProspectorInput: " + rbsBioProspectorInput.getAbsolutePath());
        System.out.println("BestHit: " + rbsBioProsFirstHit.getAbsolutePath());

        this.rbsMotifSearchPanel = new RbsMotifSearchPanel();


        rbsMotifSearchPanel.setBioProspInput(rbsBioProspectorInput);
        rbsMotifSearchPanel.setBioProspOut(rbsBioProsFirstHit);
        this.rbsStarts = new TreeMap<>();

        // Make a text pane, set its font and color, then add it to the frame
        JTextPane regionsRelToTLSTextPane = new JTextPane();
        JTextPane regionsForMotifSearch = new JTextPane();

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
            meanSpacerLengthOfRBSMotif = calculateMotifStartsAndMeanSpacerInRbsAnalysis(this.upstreamRegions, regionsRelToTLSTextPane, this.contributedSequencesWithShift, rbsParams, starts, operons);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        this.rbsMotifSearchPanel.setContributedSequencesToMotif("" + this.contributingCitesForRbsMotif + "/" + upstreamRegions.size() / 2);
        this.rbsMotifSearchPanel.setRegionsToAnalyzeToPane(regionsRelToTLSTextPane.getStyledDocument());
        this.rbsMotifSearchPanel.setRegionOfIntrestToPane(regionsForMotifSearch.getStyledDocument());

        // generating Sequence Logos and adding them into Tabbedpane
        int logoStart = Math.round(this.meanSpacerLengthOfRBSMotif);
        logoStart += rbsParams.getMotifWidth();
        String roundedMean = String.format("%.1f", this.meanSpacerLengthOfRBSMotif);

        this.rbsMotifSearchPanel.setRegionLengthForBioProspector(rbsParams.getSeqLengthToAnalyze());
        this.rbsMotifSearchPanel.setMotifWidth(rbsParams.getMotifWidth());
        this.rbsMotifSearchPanel.setMeanSpacerLength(roundedMean);

        this.logoRbs = makeSeqLogo(2.0, rbsBioProsFirstHit, parentDir.getAbsolutePath() + "\\RBSLogo",
                "PNG", 8.0, -logoStart, 15, true, true);
        rbsMotifSearchPanel.setSequenceLogo(new File(this.logoRbs.getAbsolutePath() + ".png"));
        JLabel logoLabel = new JLabel();
        Icon icon = new ImageIcon(this.logoRbs.getAbsolutePath() + ".png");
        logoLabel.setIcon(icon);
        this.rbsMotifSearchPanel.setLogo(logoLabel);
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
    private File writeSubRegionFor5UTRInFile(File workDir, File outFile,
            List<String> seqs, int spacer, int seqLengthForMotifSearch,
            int spacer2, int seqLengthForMotifSearch2, TreeMap<String, Integer> alignmentShifts) {

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
//                        minus35TP.getStyledDocument().insertString(minus35TP.getStyledDocument().getLength(), string, null);
                    } else {
//                        minus10TP.getStyledDocument().insertString(minus10TP.getStyledDocument().getLength(), string, null);
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
//                        minus35TP.getStyledDocument().insertString(minus35TP.getStyledDocument().getLength(), substring + "\n", null);
//                        colorSubstringsInStyledDocument(minus35TP, font, shift - 1, cnt, Color.blue);
                    } else {
                        int stringLength = string.length();
                        int offset = stringLength - (spacer + 1) - seqLengthForMotifSearch;
                        int end = (offset - 1) + (seqLengthForMotifSearch + 1);
                        substring = string.substring(offset, end);
                        writer.append(substring + "\n");
                        minus10AnalysisStrings.add(substring + "\n");
//                        minus10TP.getStyledDocument().insertString(minus10TP.getStyledDocument().getLength(), substring + "\n", null);
//                        colorSubstringsInStyledDocument(minus10TP, font, shift - 1, cnt, Color.blue);
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
        return outFile;
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
        storeRbsAnalysisResults(this.rbsStarts, tss, rbsShifts, params, operons);
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
     * Getter for RbsMotifSearchPanel.
     *
     * @return an instance of RbsMotifSearchPanel.
     */
    public RbsMotifSearchPanel getRbsMotifSearchPanel() {
        return rbsMotifSearchPanel;
    }

    /**
     * Setter for RbsMotifSearchPanel.
     *
     * @param rbsMotifSearchPanel an instance of RbsMotifSearchPanel.
     */
    public void setRbsMotifSearchPanel(RbsMotifSearchPanel rbsMotifSearchPanel) {
        this.rbsMotifSearchPanel = rbsMotifSearchPanel;
    }

    @Override
    public void update(Object args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param rbsBioProspectorInput
     * @param regionsForMotifSearch
     * @param rbsParams
     */
    private void writeBioProspInputFromRbsAalysis(File rbsBioProspectorInput, StyledDocument regionsForMotifSearch, RbsAnalysisParameters rbsParams) {
        this.rbsMotifSearchPanel = new RbsMotifSearchPanel();

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

    private void storeRbsAnalysisResults(TreeMap<String, Integer> rbsStarts, List<TranscriptionStart> tss, TreeMap<String, Integer> rbsShifts, RbsAnalysisParameters params, List<Operon> operons) {
        if (tss != null) {
            HashMap<String, TranscriptionStart> startsInTreeMap = new HashMap<>();
            for (TranscriptionStart ts : tss) {
                if (ts.getAdditionalIdentyfier() != null) {
                    startsInTreeMap.put(ts.getAdditionalIdentyfier(), ts);
                }
            }

            for (int i = 0; i < this.upstreamRegions.size(); i++) {
                String str = this.upstreamRegions.get(i);
                String locus;
                if (str.startsWith(">")) {
                    locus = str.substring(1, str.length() - 1);
                    if (startsInTreeMap.containsKey(locus.toString())) {
                        TranscriptionStart start = startsInTreeMap.get(locus);
                        if (rbsShifts.containsKey(locus)) {
                            start.setRbsFeatureAssigned(true);
                            start.setStartRbsMotif(rbsStarts.get(locus));
                            start.setRbsMotifWidth(params.getMotifWidth());
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
                        Operon op = operonsInTreeMap.get(locus);
                        if (rbsShifts.containsKey(locus)) {
                            op.setRbsFeatureAssigned(true);
                            op.setStartRbsMotif(rbsStarts.get(locus));
                            op.setRbsMotifWidth(params.getMotifWidth());
                        }
                        operonsInTreeMap.put(locus, op);
                    }
                } else {
                    continue;
                }
            }
        }
    }
}
