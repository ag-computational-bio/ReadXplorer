package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
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
    private List<List<Object>> bioProspectorOutArray;
//    private File bioProspectorOutRbsFile;
    private File logoMinus10, logoMinus35, logoRbs;
    HashMap<Integer, PersistantChromosome> chromosomes;
    private List<String> upstreamRegions;
    private int meanMinus10SpacerToTSS, meanMinus35SpacerToMinus10;
    private float meanSpacerLengthOfRBSMotif;
    private final ProgressHandle progressHandlePromotorAnalysis, progressHandleRbsAnalysis;
    private String handlerTitlePromotorAnalysis, handlerTitleRBSAnalysis;

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
    public MotifSearchPanel utrPromotorAnalysis(PromotorSearchParameters params) {

        MotifSearchPanel promotorMotifSearchPanel = new MotifSearchPanel();
        promotorMotifSearchPanel.setMinus35MotifWidth(params.getMinus35MotifWidth());
        promotorMotifSearchPanel.setMinus10MotifWidth(params.getMinusTenMotifWidth());
        this.progressHandlePromotorAnalysis.progress("processing promotor analysis ...", 20);

        DefaultStyledDocument regionOfIntrestMinus10 = new DefaultStyledDocument();
        DefaultStyledDocument regionOfIntrestMinus35 = new DefaultStyledDocument();
        StyledDocument bioProspectorOut = null;
        File workingDir = params.getWorkingDirectory();
        File bioProspectorOutMinus10File = new File(workingDir.getAbsolutePath() + "\\bioProspectorOutMinus10.fna");
        File bioProspectorOutMinus35File = new File(workingDir.getAbsolutePath() + "\\bioProspectorOutMinus35.fna");
        // #####################################################################
        // 1. write all upstream subregions for -10 analysis
        File minusTenFile = writeSubRegionFor5UTRInFile(
                workingDir, "putativeMinus10Region", this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                null, regionOfIntrestMinus10, regionOfIntrestMinus35);
        promotorMotifSearchPanel.setStyledDocumentToRegionOfIntrestMinusTen(regionOfIntrestMinus10);

        // ####Analysze -10#####################################################
        String posixPath = "/cygdrive/c";
        String sub = minusTenFile.getAbsolutePath().toString().substring(2);
        posixPath += sub.replaceAll("\\\\", "/");
        // #####################################################################
        try {
            // 2. executing bioprospector and parse the best scored Hits and write 
            // them in a stypeddocument for printing this into a file
            bioProspectorOut = this.executeBioProspector(
                    posixPath, params.getMinusTenMotifWidth(), params.getNoOfTimesTrying(),
                    1, 1, 1);// last param 0 => not ervery Seq contains motif
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        try {
            // 3. parse the contetn of bioProspectorOut and write it into 
            // bioProspectorOutMinus10File
            parseBioProspOutput(bioProspectorOut, bioProspectorOutMinus10File);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        this.progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 40);
        // #####################################################################


        List<List<Object>> alignmentshiftsOFMinusTenArray = new ArrayList<>();
        alignmentshiftsOFMinusTenArray.addAll(this.bioProspectorOutArray);
        // 4. All sequences, which did not contain a motif in the first run for-10 motif serch
        // will be descard in the next step of the analysis of the -35 motif

        File minusThirtyFiveFile = writeSubRegionFor5UTRInFile(
                workingDir, "putativeMinus35", this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                alignmentshiftsOFMinusTenArray, regionOfIntrestMinus10, regionOfIntrestMinus35);

        promotorMotifSearchPanel.setStyledDocumentToRegionOfIntrestMinus35(regionOfIntrestMinus35);

        posixPath = "/cygdrive/c";
        sub = minusThirtyFiveFile.getAbsolutePath().toString().substring(2);
        posixPath += sub.replaceAll("\\\\", "/");

        try {
            // 5. So in this step we just processing subregions which had a hit 
            // in the -10 region.
            bioProspectorOut = this.executeBioProspector(
                    posixPath, params.getMinus35MotifWidth(),
                    params.getNoOfTimesTrying(),
                    1, 1, 1); // last param 0 => not ervery Seq contains motif
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 60);

        try {
            // 6. bioprospector output will be parsed and written into a file
            parseBioProspOutput(bioProspectorOut, bioProspectorOutMinus35File);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        List<List<Object>> alignmentshiftsOf35InArray = new ArrayList<>();
        alignmentshiftsOf35InArray.addAll(this.bioProspectorOutArray);

        List<Integer[]> motifStarts = calculateMotifStartsAndMeanSpacerLength(this.upstreamRegions, alignmentshiftsOFMinusTenArray, alignmentshiftsOf35InArray, params);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 70);

        StyledDocument coloredPromotorRegions = colorPromotorMotifRegions(this.upstreamRegions, motifStarts, params);


        promotorMotifSearchPanel.setStyledDocToPromotorsFastaPane(coloredPromotorRegions);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 80);
        // generating Sequence Logos and adding them into Tabbedpane
        int logoStart10 = this.meanMinus10SpacerToTSS + params.getMinusTenMotifWidth();
        promotorMotifSearchPanel.setMinSpacer1LengthToLabel(this.meanMinus10SpacerToTSS);

        this.logoMinus10 = makeSeqLogo(2.0, bioProspectorOutMinus10File, workingDir.getAbsolutePath() + "\\minusTenLogo",
                "PNG", 8.0, -logoStart10, 15, true, true);

        JLabel logoLabel1 = new JLabel();
        Icon icon1 = new ImageIcon(this.logoMinus10.getAbsolutePath() + ".png");
        logoLabel1.setIcon(icon1);
        promotorMotifSearchPanel.setMinus10LogoToPanel(logoLabel1);


        int logoStart35 = this.meanMinus35SpacerToMinus10 + params.getMinus35MotifWidth() + logoStart10;
        promotorMotifSearchPanel.setMinSpacer2LengthToLabel(this.meanMinus35SpacerToMinus10);
        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 90);

        this.logoMinus35 = makeSeqLogo(2.0, bioProspectorOutMinus35File, workingDir.getAbsolutePath() + "\\minus35Logo",
                "PNG", 8.0, -logoStart35, 15, true, true);

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
    public void rbsMotifAnalysis(RbsAnalysisParameters rbsParams) {

        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 20);
        File workingDir = rbsParams.getWorkingDir();
        File rbsBioProspectorInput = new File(workingDir.getAbsolutePath() + "\\rbsSequencesOfIntrest.fna");
        File rbsBioProsFirstHit = new File(workingDir.getAbsolutePath() + "\\rbsBioProspectorFirstHit.fna");

        StyledDocument bioProspectorOut = null;
        StyledDocument regionsForMotifSearch = new DefaultStyledDocument();
        StyledDocument regionsRelToTLS = new DefaultStyledDocument();

        writeBioProspInputFromRbsAalysis(rbsBioProspectorInput, regionsForMotifSearch, rbsParams);

        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 60);

        runBioProspForRbsAnalysis(workingDir, rbsBioProspectorInput, bioProspectorOut, rbsParams, rbsBioProsFirstHit);

        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 80);
        try {
            meanSpacerLengthOfRBSMotif = calculateMotifStartsAndMeanSpacerInRbsAnalysis(this.upstreamRegions, regionsRelToTLS, this.bioProspectorOutArray, rbsParams);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        this.rbsMotifSearchPanel.setRegionsToAnalyzeToPane(regionsRelToTLS);
        this.rbsMotifSearchPanel.setRegionOfIntrestToPane(regionsForMotifSearch);

        // generating Sequence Logos and adding them into Tabbedpane
        int logoStart = Math.round(this.meanSpacerLengthOfRBSMotif);

        this.rbsMotifSearchPanel.setRegionLengthForBioProspector(rbsParams.getSeqLengthToAnalyze());
        this.rbsMotifSearchPanel.setMotifWidth(rbsParams.getMotifWidth());
        this.rbsMotifSearchPanel.setMeanSpacerLength(logoStart);

        this.logoRbs = makeSeqLogo(2.0, rbsBioProsFirstHit, workingDir.getAbsolutePath() + "\\RBSLogo",
                "PNG", 8.0, -logoStart, 15, true, true);

        JLabel logoLabel = new JLabel();
        Icon icon = new ImageIcon(this.logoRbs.getAbsolutePath() + ".png");
        logoLabel.setIcon(icon);
        this.rbsMotifSearchPanel.setLogo(logoLabel);
        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 100);
        progressHandleRbsAnalysis.finish();
    }

    /**
     *
     * @param down
     * @param tp
     * @param flag true for extracting the -35 region
     */
    public void writeRegionInTextPane(List<String> upstreamRegions, JTextPane tp, HashMap<String, Integer> bioProspectorOut, PromotorSearchParameters params, boolean flag) {
        StyledDocument styledDocument = tp.getStyledDocument();
        String header = "";
        for (int i = 0; i < upstreamRegions.size(); i++) {
            String original = upstreamRegions.get(i);
            if (original.startsWith(">")) {

                header = original.substring(1, original.length() - 1);
                try {
                    styledDocument.insertString(styledDocument.getLength(), original, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                if (flag) {
                    int length = original.length();
                    int beginnOfMinusTen = length - params.getMinSpacer1() - params.getSequenceWidthToAnalyzeMinus10() + bioProspectorOut.get(header);
                    int begin = beginnOfMinusTen - params.getMinSpacer2() - params.getSequenceWidthToAnalyzeMinus35();
                    int end = begin + params.getSequenceWidthToAnalyzeMinus35();
                    String region = original.substring(begin, end);
                    try {
                        styledDocument.insertString(styledDocument.getLength(), region + "\n", null);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    int length = original.length();
                    int begin = length - params.getMinSpacer1() - params.getSequenceWidthToAnalyzeMinus10();
                    int end = length - params.getMinSpacer1();
                    String region = original.substring(begin, end);
                    try {
                        styledDocument.insertString(styledDocument.getLength(), region + "\n", null);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        tp.setStyledDocument(styledDocument);

    }

    /**
     * This method execute the BioProspector binary with the following
     * parameters.
     *
     * @param firstMotifWidth
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
    private StyledDocument executeBioProspector(String inputFilePath,
            int firstMotifWidth, int noOfCycles, int noOfTopMotifs,
            int justExamineFwd, int everySeqHasMotif) throws IOException {
        StyledDocument doc = new DefaultStyledDocument();
        this.bioProspectorOutArray = new ArrayList<>();

        String cmd = "C:\\Users\\jritter\\Documents\\MA-Thesis\\BioProspector.2004\\BioProspector.exe";
        List<String> commandArguments = new ArrayList<>();
        commandArguments.add(cmd);
        commandArguments.add("-i");
        commandArguments.add(inputFilePath);
        commandArguments.add("-W");
        commandArguments.add("" + firstMotifWidth);
        commandArguments.add("-n");
        commandArguments.add("" + noOfCycles);
        commandArguments.add("-r");
        commandArguments.add("" + noOfTopMotifs);
        commandArguments.add("-a");
        commandArguments.add("" + everySeqHasMotif);
        commandArguments.add("-d");
        commandArguments.add("" + justExamineFwd);
//        commandArguments.add("-b");
//        commandArguments.add("" + backgroundSequenceFilePath);

        ProcessBuilder ps = new ProcessBuilder(commandArguments);

        //From the DOC:  Initially, this property is false, meaning that the 
        //standard output and error output of a subprocess are sent to two 
        //separate streams
        ps.redirectErrorStream(true);

        Process pr = ps.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            try {
                //            System.out.println(line);
                doc.insertString(doc.getLength(), line + "\n", null);
                if (line.startsWith(">")) {
                    String[] splitted = line.split("\t");
                    String start = splitted[splitted.length - 1].substring(2);
                    String id = splitted[0].substring(1);
                    List<Object> tmp = new ArrayList<>();
                    tmp.add(id);
                    tmp.add(Integer.valueOf(start));
                    this.bioProspectorOutArray.add(tmp);
                }

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        try {
            pr.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(MotifSearchPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        in.close();
        return doc;

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
    private File writeSubRegionFor5UTRInFile(File workDir, String nameOfRegion,
            List<String> seqs, int spacer, int seqLengthForMotifSearch,
            int spacer2, int seqLengthForMotifSearch2, List<List<Object>> alignmentShiftsInArray,
            StyledDocument minus10, StyledDocument minus35) {

        Writer writer = null;
        File outFile = new File(workDir.getAbsolutePath() + "\\" + nameOfRegion + ".fna");
        int cnt = 1;
        int shift = 0;
        boolean isShifts = false;
        if (alignmentShiftsInArray != null) {
            isShifts = true;
        }

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile.getAbsolutePath()), "utf-8"));
            for (String string : seqs) {
                if (cnt == 1) {
                    if (isShifts) {
                        shift = (Integer) alignmentShiftsInArray.get(0).get(1);
                        writer.append(string);
                        minus35.insertString(minus35.getLength(), string, null);
                    } else {
                        minus10.insertString(minus10.getLength(), string, null);
                        writer.append(string);
                    }
                    cnt = 0;
                } else {
                    if (isShifts) {
                        int stringLength = string.length();
                        int offset = stringLength - (spacer + 1) - (seqLengthForMotifSearch - (shift - 1)) - spacer2 - seqLengthForMotifSearch2;
                        int end = (offset - 1) + (seqLengthForMotifSearch2 + 1);
                        String substring = string.substring(offset, end);
                        writer.append(substring + "\n");
                        minus35.insertString(minus35.getLength(), substring + "\n", null);
                    } else {
                        int stringLength = string.length();
                        int offset = stringLength - (spacer + 1) - seqLengthForMotifSearch;
                        int end = (offset - 1) + (seqLengthForMotifSearch + 1);
                        String substring = string.substring(offset, end);
                        writer.append(substring + "\n");
                        minus10.insertString(minus10.getLength(), substring + "\n", null);
                    }
                    cnt = 1;
                }
            }

        } catch (IOException ex) {
            // report
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
        return outFile;
    }

    /**
     * Parses the BioProspector output and save the first Hit alignment in the
     * passed over file.
     *
     * @param bioProspectorOut Output from BioProspector run.
     * @param file Destination file for saving the parsed BioProspector output.
     * @throws BadLocationException
     */
    private void parseBioProspOutput(StyledDocument bioProspectorOut, File file) throws BadLocationException {

        String[] bioProspectorOutInArr = bioProspectorOut.getText(0, bioProspectorOut.getLength()).toString().split("\n");
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file.getAbsolutePath()), "utf-8"));

            for (int i = 0; i < bioProspectorOutInArr.length; i++) {
                String string = bioProspectorOutInArr[i];
                if (string.startsWith("Motif#2")) {
                    break;
                }
                if (string.startsWith(">")) {
                    writer.write(string + "\n");
                    writer.write(bioProspectorOutInArr[i + 1] + "\n");
                    i++;
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
    }

    /**
     * This method calculaes the mean spacer betwean the TLS ant the beginning
     * of a RBS-Motif. It also determine the starts of the motifs and presents
     * the values to the colorSubstringsInStyledDocument.
     *
     * @param upstreamRegions List of upstream regions for promotor motif.
     * @param doc
     * @param shifts
     * @param params instance of PromotorSearchParameters.
     * @return the mean spacer distance.
     * @throws BadLocationException
     */
    private float calculateMotifStartsAndMeanSpacerInRbsAnalysis(List<String> upstreamRegions, StyledDocument upstreamRegionDoc, List<List<Object>> shifts, RbsAnalysisParameters params) throws BadLocationException {
        int sumOfMinsSpacer = 0;
        Integer shift = 0;
        int index = 0;
        int minSpacer = params.getMinSpacer();
        int motifWidth = params.getMotifWidth();

        for (String string : upstreamRegions) {
            if (string.startsWith(">")) {
                shift = (Integer) shifts.get(index).get(1);
                upstreamRegionDoc.insertString(upstreamRegionDoc.getLength(), string, null);
                index++;
            } else {
                int length = string.length() - 1;
                int documentLenght = upstreamRegionDoc.getLength();
                upstreamRegionDoc.insertString(documentLenght, string, null);
                sumOfMinsSpacer += (length - ((shift - 1) + motifWidth)) + minSpacer;
                int colorStart = documentLenght + (shift - 1);
                colorSubstringsInStyledDocument(upstreamRegionDoc, colorStart, motifWidth, Color.RED);
            }
        }

        return sumOfMinsSpacer / (upstreamRegions.size() / 2);
    }

    /**
     *
     * @param upstreamRegions List of upstream regions for promotor motif.
     * @param shifts10
     * @param shifts35
     * @param params instance of PromotorSearchParameters.
     * @return
     */
    private List<Integer[]> calculateMotifStartsAndMeanSpacerLength(List<String> upstreamRegions, List<List<Object>> shifts10,
            List<List<Object>> shifts35, PromotorSearchParameters params) {
        int sumOfMinus10Spacer = 0;
        int sumOfMinus35Spacer = 0;
        Integer shiftPosMinus10 = 0;
        Integer shiftPosMinus35 = 0;
        List<Integer[]> motifStarts = new ArrayList<>();
        int index = 0;
        int length = params.getLengthOfPromotorRegion();
        int spacer1 = params.getMinSpacer1();
        int spacer2 = params.getMinSpacer2();
        int seqWidthToAnalyzeMinus10 = params.getSequenceWidthToAnalyzeMinus10();
        int seqWidthToAnalyzeMinus35 = params.getSequenceWidthToAnalyzeMinus35();
        for (String string : upstreamRegions) {
            if (string.startsWith(">")) {
                System.out.println(string);
                shiftPosMinus10 = (Integer) shifts10.get(index).get(1);
                shiftPosMinus35 = (Integer) shifts35.get(index).get(1);
                index++;
            } else {
//                System.out.println(string);
                sumOfMinus10Spacer += spacer1 + (params.getSequenceWidthToAnalyzeMinus10() - params.getMinusTenMotifWidth() - (shiftPosMinus10 - 1));
                int motifStartMinus10 = length - spacer1 - seqWidthToAnalyzeMinus10 + shiftPosMinus10;
                int motifStartMinus35 = motifStartMinus10 - spacer2 - seqWidthToAnalyzeMinus35 + shiftPosMinus35 - 1;
                sumOfMinus35Spacer += spacer2 + (params.getSequenceWidthToAnalyzeMinus35() - params.getMinus35MotifWidth() - (shiftPosMinus35 - 1));
                System.out.println("-35 start:" + motifStartMinus35 + "\t" + "-10 start:" + motifStartMinus10);
                Integer[] tmp = new Integer[]{motifStartMinus35, motifStartMinus10};
                motifStarts.add(tmp);
            }
        }

        this.meanMinus10SpacerToTSS = sumOfMinus10Spacer / (upstreamRegions.size() / 2);
        this.meanMinus35SpacerToMinus10 = sumOfMinus35Spacer / (upstreamRegions.size() / 2);

        System.out.println("Durchschnitts Spacer1: " + this.meanMinus10SpacerToTSS);
        System.out.println("Durchschnitts Spacer2: " + this.meanMinus35SpacerToMinus10);

        return motifStarts;

    }

    /**
     * This method tone all promotor motifs in a upstream region of a TSS.
     *
     * @param upstreamRegions List of upstream regions for promotor motif.
     * analysis.
     * @param motifStarts List with motif start positions.
     * @param params instance of PromotorSearchParameters.
     * @return
     */
    private StyledDocument colorPromotorMotifRegions(List<String> upstreamRegions, List<Integer[]> motifStarts, PromotorSearchParameters params) {
        StyledDocument styledDoc = new DefaultStyledDocument();
        Integer alignmentStartMinus10 = 0;
        Integer alignmentStartMinus35 = 0;
//        StyledDocument styledDoc = this.pane.getStyledDocument();
        int index = 0;
        for (String string : upstreamRegions) {
            if (string.startsWith(">")) {
                alignmentStartMinus10 = motifStarts.get(index)[1];
                alignmentStartMinus35 = motifStarts.get(index)[0];
                index++;
                try {
                    styledDoc.insertString(styledDoc.getLength(), string, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                int length = styledDoc.getLength();
                int colorStartMinus35 = length + (alignmentStartMinus35 - 1);

                try {
                    styledDoc.insertString(styledDoc.getLength(), string, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                length = styledDoc.getLength();
                int colorStartMinus10 = length - (params.getLengthOfPromotorRegion() - alignmentStartMinus10);
                colorSubstringsInStyledDocument(styledDoc, colorStartMinus35, params.getMinus35MotifWidth(), Color.blue);
                colorSubstringsInStyledDocument(styledDoc, colorStartMinus10 - 2, params.getMinusTenMotifWidth(), Color.red);
            }
        }

        return styledDoc;
    }

    /**
     * Colorize the defined range between start and stop with the given color.
     *
     * @param styledDoc StyledDocument in which the subregion have to be toned.
     * @param start Start position of coloring.
     * @param length Length of subregion to be toned.
     * @param color Color.
     */
    private void colorSubstringsInStyledDocument(StyledDocument styledDoc, int start, int length, Color color) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(
                SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, color);
        styledDoc.setCharacterAttributes(start, length, aset, false);
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
        commandArguments.add("Bits");
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
        PersistantFeature currentFeature = null;
        String substr = "";
        int tssStart;
        int featureStart;
        int uniqueIdx = 1;

        if (type == ElementsOfInterest.ALL) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                if (isRbsAnalysis) {
                    // add header in array
                    this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                    if (tss.isFwdStrand()) {
                        if (tss.isLeaderless()) {
                            tssStart = tss.getStartPosition();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                        }
                    } else {
                        if (tss.isLeaderless()) {
                            tssStart = tss.getStartPosition();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                        }
                    }
                } else {
                    tssStart = tss.getStartPosition();
                    this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                    if (tss.isFwdStrand()) {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                        upstreamRegions.add(substr + "\n");
                    } else {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                        upstreamRegions.add(substr + "\n");
                    }
                }
                uniqueIdx++;
            }
        } else if (type == ElementsOfInterest.ONLY_ANTISENSE) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                if (isRbsAnalysis) {
                    // add header in array
                    if (tss.isPutativeAntisense()) {
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (tss.isPutativeAntisense()) {
                        tssStart = tss.getStartPosition();
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
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
                tssStart = tss.getStartPosition();
                if (tss.isLeaderless()) {
                    this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                    if (tss.isFwdStrand()) {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                        upstreamRegions.add(substr + "\n");
                    } else {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                        upstreamRegions.add(substr + "\n");
                    }
                } else {
                    continue;
                }
                uniqueIdx++;
            }
        } else if (type == ElementsOfInterest.ONLY_NONE_LEADERLESS) {
            for (TranscriptionStart tss : starts) {
                currentFeature = tss.getAssignedFeature();
                if (isRbsAnalysis) {
                    // add header in array
                    if (!tss.isLeaderless()) {
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (tss.isLeaderless()) {
                        tssStart = tss.getStartPosition();
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
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
                if (isRbsAnalysis) {
                    // add header in array
                    if (!tss.isPutativeAntisense() && !tss.isLeaderless() && !tss.isInternalTSS()) {
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (!tss.isPutativeAntisense() && !tss.isLeaderless() && !tss.isInternalTSS()) {
                        tssStart = tss.getStartPosition();
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
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
                tssStart = tss.getStartPosition();
                if (isRbsAnalysis) {
                    // add header in array
                    if (tss.isLeaderless() && tss.isSelected()) {
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                        }
                    } else if (tss.isSelected()) {
                        this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                        if (tss.isFwdStrand()) {
                            featureStart = currentFeature.getStart();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart - (length + 1), featureStart - 1);
                            upstreamRegions.add(substr + "\n");
                        } else {
                            featureStart = currentFeature.getStop();
                            substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(featureStart + 1, featureStart + (length + 1));
                            upstreamRegions.add(substr + "\n");
                        }
                    }
                } else if (tss.isSelected()) {
                    this.upstreamRegions.add(">" + currentFeature.getLocus() + "_" + uniqueIdx + "\n");
                    System.out.println(">" + currentFeature.getLocus());
                    if (tss.isFwdStrand()) {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart - (length + 1), tssStart - 1);
                        upstreamRegions.add(substr + "\n");
                        System.out.println(substr);
                    } else {
                        substr = this.chromosomes.get(tss.getChromId()).getSequence(this).substring(tssStart + 1, tssStart + (length + 1));
                        upstreamRegions.add(substr + "\n");
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
     * @param workingDir
     * @param rbsBioProspectorInput
     * @param bioProspectorOut
     * @param rbsParams
     * @param rbsBioProsFirstHit
     */
    private void runBioProspForRbsAnalysis(File workingDir, File rbsBioProspectorInput, StyledDocument bioProspectorOut, RbsAnalysisParameters rbsParams, File rbsBioProsFirstHit) {
        if (workingDir.isDirectory()) {
            String posixPath = "/cygdrive/c";
            String sub = rbsBioProspectorInput.getAbsolutePath().toString().substring(2);
            posixPath += sub.replaceAll("\\\\", "/");

            try {
                bioProspectorOut = this.executeBioProspector(
                        posixPath, rbsParams.getMotifWidth(),
                        rbsParams.getNumberOfCyclesForBioProspector(),
                        1, 1, 1);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                parseBioProspOutput(bioProspectorOut, rbsBioProsFirstHit);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
