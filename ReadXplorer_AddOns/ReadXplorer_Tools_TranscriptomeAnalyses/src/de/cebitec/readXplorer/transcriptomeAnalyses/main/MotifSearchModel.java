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
    private File bioProspectorOutRbsFile;
    private File logoMinus10, logoMinus35, logoRbs;
    HashMap<Integer, PersistantChromosome> chromosomes;
    HashMap<String, Integer> bioProsOutShifts;
    private List<String> upstreamRegions;
    private int meanMinus10SpacerToTSS, meanMinus35SpacerToMinus10, meanSpacerLengthOfRBSMotif;
    private final ProgressHandle progressHandlePromotorAnalysis, progressHandleRbsAnalysis;
    private String handlerTitlePromotorAnalysis, handlerTitleRBSAnalysis;

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
     * @param params
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
//        File backgroundSequencesFile = writeBackgroundSequencesInFile(workingDir);
        // #####################################################################
        // 1. write all upstream subregions for -10 analysis
        File minusTenFile = writeSubRegionFor5UTRInFile(
                workingDir, "putativeMinus10Region", this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                null, regionOfIntrestMinus10, regionOfIntrestMinus35, null);
        promotorMotifSearchPanel.setStyledDocumentToRegionOfIntrestMinusTen(regionOfIntrestMinus10);

        // ####Analysze -10#####################################################
        String posixPath = "/cygdrive/c";
//        String substring = backgroundSequencesFile.getAbsolutePath().toString().substring(2);
//        String backgroundFilePath = "/cygdrive/c";
//        backgroundFilePath += substring.replaceAll("\\\\", "/");
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
        HashMap<String, Integer> alignmentShifts10Regions = new HashMap<>();
        alignmentShifts10Regions.putAll(this.bioProsOutShifts);

        // 4. All sequences, which did not contain a motif in the first run for-10 motif serch
        // will be descard in the next step of the analysis of the -35 motif
        
        File minusThirtyFiveFile = writeSubRegionFor5UTRInFile(
                workingDir, "putativeMinus35", this.upstreamRegions,
                params.getMinSpacer1(), params.getSequenceWidthToAnalyzeMinus10(),
                params.getMinSpacer2(), params.getSequenceWidthToAnalyzeMinus35(),
                alignmentshiftsOFMinusTenArray, regionOfIntrestMinus10, regionOfIntrestMinus35, alignmentShifts10Regions);

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
        HashMap<String, Integer> alignmentShifts35Regions = new HashMap<>();
        alignmentShifts35Regions.putAll(this.bioProsOutShifts);

        List<Integer[]> motifStarts = calculateMotifStartsAndMeanSpacerLength(this.upstreamRegions, alignmentshiftsOFMinusTenArray, alignmentshiftsOf35InArray, params);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 70);

        StyledDocument coloredPromotorRegions = colorMotifRegion(this.upstreamRegions, motifStarts, params);


        promotorMotifSearchPanel.setStyledDocToPromotorsFastaPane(coloredPromotorRegions);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 80);
        // generating Sequence Logos and adding them into Tabbedpane
        this.logoMinus10 = new File(workingDir.getAbsolutePath() + "\\minusTenLogo");
        this.setLogoMinus10(logoMinus10);
        int logoStart10 = this.meanMinus10SpacerToTSS + params.getMinusTenMotifWidth();
        promotorMotifSearchPanel.setMinSpacer1LengthToLabel(this.meanMinus10SpacerToTSS);

        makeSeqLogo(2.0, bioProspectorOutMinus10File, getLogoMinus10(),
                "PNG", 8.0, -logoStart10, 15, true, true);

        this.logoMinus35 = new File(workingDir.getAbsolutePath() + "\\minus35Logo");
        this.setLogoMinus35(logoMinus35);
        int logoStart35 = this.meanMinus35SpacerToMinus10 + params.getMinus35MotifWidth() + logoStart10;
        promotorMotifSearchPanel.setMinSpacer2LengthToLabel(this.meanMinus35SpacerToMinus10);
        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 90);

        makeSeqLogo(2.0, bioProspectorOutMinus35File, this.getLogoMinus35(),
                "PNG", 8.0, -logoStart35, 15, true, true);

        JLabel logoLabel1 = new JLabel();
        Icon icon1 = new ImageIcon(getLogoMinus10().getAbsolutePath() + ".png");
        logoLabel1.setIcon(icon1);
        promotorMotifSearchPanel.setMinus10LogoToPanel(logoLabel1);

        JLabel logoLabel2 = new JLabel();
        Icon icon2 = new ImageIcon(getLogoMinus35().getAbsolutePath() + ".png");
        logoLabel2.setIcon(icon2);
        promotorMotifSearchPanel.setMinus35LogoToPanel(logoLabel2);

        progressHandlePromotorAnalysis.progress("Starting promotor analysis ...", 100);
        progressHandlePromotorAnalysis.finish();

        return promotorMotifSearchPanel;
    }

    public void rbsMotifAnalysis(RbsAnalysisParameters rbsParams) {

        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 20);
        File workingDir = rbsParams.getWorkingDir();
        File rbsBioProspectorInput = new File(workingDir.getAbsolutePath() + "\\rbsSequencesOfIntrest.fna");

        File rbsBioProsFirstHit = new File(workingDir.getAbsolutePath() + "\\rbsBioProspectorFirstHit.fna");
        StyledDocument bioProspectorOut = null;
        StyledDocument regionsToAnalyze = new DefaultStyledDocument();
        StyledDocument regionsOfIntrest = new DefaultStyledDocument();


        this.rbsMotifSearchPanel = new RbsMotifSearchPanel();

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(rbsBioProspectorInput.getAbsolutePath()), "utf-8"));

            for (String region : this.upstreamRegions) {
                if (region.startsWith(">")) {
                    writer.write(region);
                    regionsOfIntrest.insertString(regionsOfIntrest.getLength(), region, null);
//                    regionsToAnalyze.insertString(regionsToAnalyze.getLength(), region, null);
                } else {
                    String subregion = region.substring(0, region.length() - rbsParams.getMinSpacer() - 1);
                    writer.write(subregion + "\n");
                    regionsOfIntrest.insertString(regionsOfIntrest.getLength(), subregion + "\n", null);
//                    regionsToAnalyze.insertString(regionsToAnalyze.getLength(), region, null);
                }
            }
            writer.close();

        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 60);

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
        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 80);
        try {
            List<Integer> motifStarts = calculateMotifStartsAndMeanSpacerInRbsAnalysis(upstreamRegions, regionsToAnalyze, bioProspectorOutArray, rbsParams);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        this.rbsMotifSearchPanel.setRegionsToAnalyzeToPane(regionsToAnalyze);

        this.rbsMotifSearchPanel.setRegionOfIntrestToPane(regionsOfIntrest);
        // generating Sequence Logos and adding them into Tabbedpane
        this.logoRbs = new File(workingDir.getAbsolutePath() + "\\RBSLogo");
        this.setLogoRbs(logoRbs);
        int logoStart = this.meanSpacerLengthOfRBSMotif + rbsParams.getMotifWidth();

        this.rbsMotifSearchPanel.setRegionLengthForBioProspector(rbsParams.getSeqLengthToAnalyze());
        this.rbsMotifSearchPanel.setMotifWidth(rbsParams.getMotifWidth());
        this.rbsMotifSearchPanel.setMeanSpacerLength(this.meanSpacerLengthOfRBSMotif);

        makeSeqLogo(2.0, rbsBioProsFirstHit, getLogoRbs(),
                "PNG", 7.0, -logoStart, 15, true, true);
        //        this.motifSearch = new MotifSearchPanel(seqs, downRegion);
        //        this.motifSearch.writePromotorsInTextPane();
        JLabel logoLabel1 = new JLabel();
        Icon icon1 = new ImageIcon(getLogoRbs().getAbsolutePath() + ".png");
        logoLabel1.setIcon(icon1);
        this.rbsMotifSearchPanel.setLogo(logoLabel1);
        progressHandleRbsAnalysis.progress("processing rbs analysis ...", 100);
        progressHandleRbsAnalysis.finish();
        this.setRbsMotifSearchPanel(this.rbsMotifSearchPanel);
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
        this.bioProsOutShifts = new HashMap<>();

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
                    this.bioProsOutShifts.put(id, Integer.valueOf(start));
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
            StyledDocument minus10, StyledDocument minus35, HashMap<String, Integer> alignmentShifts) {

        Writer writer = null;
        File outFile = new File(workDir.getAbsolutePath() + "\\" + nameOfRegion + ".fna");
        int cnt = 1;
        int shift = 0;
        boolean isShifts = false;
//        if (alignmentShiftsInArray != null) {
//            isShifts = true;
//        }

        if (alignmentShifts != null) {
            isShifts = true;
        }

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile.getAbsolutePath()), "utf-8"));
            String header = "";
            for (String string : seqs) {
                if (cnt == 1) {
                    header = string.substring(1, string.length() - 1);
//                    if (isShifts && alignmentShifts.containsKey(header)) {
                    if (isShifts) {
//                            shift = (Integer) alignmentShiftsInArray.get(0).get(1);
                        shift = alignmentShifts.get(header);
                        writer.append(string);
                        minus35.insertString(minus35.getLength(), string, null);
                    } else {
                        minus10.insertString(minus10.getLength(), string, null);
                        writer.append(string);
                    }
                    cnt = 0;
                } else {
//                    if (isShifts && alignmentShifts.containsKey(header)) {
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
     *
     * @param bioProspectorOut
     * @param file
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

    private List<Integer> calculateMotifStartsAndMeanSpacerInRbsAnalysis(List<String> promotors, StyledDocument doc, List<List<Object>> shifts, RbsAnalysisParameters params) throws BadLocationException {
        int sumOfMinsSpacer = 0;
        Integer shift = 0;
        List<Integer> motifStarts = new ArrayList<>();
        int index = 0;
        String header = "";

        for (String string : promotors) {
            if (string.startsWith(">")) {
                System.out.println(string);
                header = string.substring(1, string.length() - 1); // remove '>' and '\n'
                shift = (Integer) shifts.get(index).get(1);
                doc.insertString(doc.getLength(), string, null);
                index++;
            } else {
                int length = string.length() - 1;
                sumOfMinsSpacer += params.getMinSpacer() + (params.getSeqLengthToAnalyze() - params.getMotifWidth() - (shift - 1));
                int motifStart = params.getSeqLengthToAnalyze() - params.getMinSpacer() - params.getMotifWidth() + shift;
                int colorStart = doc.getLength() - 1 - params.getMinSpacer() - params.getMotifWidth() + shift;
                doc.insertString(doc.getLength(), string, null);
                colorSubstringsInPromotorSites(doc, colorStart, params.getMotifWidth(), Color.RED);
                motifStarts.add(motifStart);
            }
        }

        this.meanSpacerLengthOfRBSMotif = sumOfMinsSpacer / (promotors.size() / 2);
        return motifStarts;
    }

    /**
     *
     * @param promotors
     * @param shifts10
     * @param shifts35
     * @param params
     * @return
     */
    private List<Integer[]> calculateMotifStartsAndMeanSpacerLength(List<String> promotors, List<List<Object>> shifts10,
            List<List<Object>> shifts35, PromotorSearchParameters params) {
        int sumOfMinus10Spacer = 0;
        int sumOfMinus35Spacer = 0;
        String header = "";
        Integer shiftPosMinus10 = 0;
        Integer shiftPosMinus35 = 0;
        List<Integer[]> motifStarts = new ArrayList<>();
        int index = 0;
        int length = params.getLengthOfPromotorRegion();
        int spacer1 = params.getMinSpacer1();
        int spacer2 = params.getMinSpacer2();
        int seqWidthToAnalyzeMinus10 = params.getSequenceWidthToAnalyzeMinus10();
        int seqWidthToAnalyzeMinus35 = params.getSequenceWidthToAnalyzeMinus35();
        System.out.println("==========================================");
        for (String string : promotors) {
            if (string.startsWith(">")) {
                System.out.println(string);
                header = string.substring(1, string.length() - 1); // remove '>' and '\n'
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

        this.meanMinus10SpacerToTSS = sumOfMinus10Spacer / (promotors.size() / 2);
        this.meanMinus35SpacerToMinus10 = sumOfMinus35Spacer / (promotors.size() / 2);

        System.out.println("Durchschnitts Spacer1: " + this.meanMinus10SpacerToTSS);
        System.out.println("Durchschnitts Spacer2: " + this.meanMinus35SpacerToMinus10);

        return motifStarts;

    }

    /**
     *
     * @param promotors
     * @param minusTenAlignmentShifts
     * @param minus35AlignmentShifts
     * @param downStreamRegion
     * @param paramsBioProspector
     * @return
     */
    private StyledDocument colorMotifRegion(List<String> promotors, List<Integer[]> motifStarts, PromotorSearchParameters params) {
        String header = "";
        StyledDocument styledDoc = new DefaultStyledDocument();
        Integer alignmentStartMinus10 = 0;
        Integer alignmentStartMinus35 = 0;
//        StyledDocument styledDoc = this.pane.getStyledDocument();
        int index = 0;
        for (String string : promotors) {
            if (string.startsWith(">")) {
                header = string.substring(1, string.length() - 1); // remove '>' and '\n'
                alignmentStartMinus10 = motifStarts.get(index)[1];
                alignmentStartMinus35 = motifStarts.get(index)[0];
                index++;
                int stingLength = string.length();
                try {
                    styledDoc.insertString(styledDoc.getLength(), string, null);
                    int docLength1 = styledDoc.getLength();
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                try {
                    styledDoc.insertString(styledDoc.getLength(), string, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                int length = styledDoc.getLength();
                int colorStartMinus10 = length - (params.getLengthOfPromotorRegion() - alignmentStartMinus10);
                int colorStartMinus35 = length - (params.getLengthOfPromotorRegion() - alignmentStartMinus35);
                colorSubstringsInPromotorSites(styledDoc, colorStartMinus35 - 2, params.getMinus35MotifWidth(), Color.blue);
                colorSubstringsInPromotorSites(styledDoc, colorStartMinus10 - 2, params.getMinusTenMotifWidth(), Color.red);
            }
        }

        return styledDoc;
    }

    /**
     *
     * @param start
     * @param length
     * @param color
     */
    private void colorSubstringsInPromotorSites(StyledDocument styledDoc, int start, int length, Color color) {

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(
                SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, color);
        styledDoc.setCharacterAttributes(start, length, aset, false);
    }

    /**
     *
     * @param noOfBitsInBar
     * @param noOfBitsBetweenTicMarcs
     * @param noOfCharactersPerLine
     * @param shrinkFactor
     * @param errorBarFraction
     * @param inputFile
     * @param outputFile
     * @param outputFormat
     * @param hieghtOfLogo
     * @param kindOfData
     * @param lowerBound
     * @param upperBound
     * @param sequenceStart
     * @param title
     * @param logwidth
     * @param xAxisLabel
     * @param yAxisLabel
     * @param isAntialiasin
     * @param isBarEnds
     * @param isColor
     * @param isSerrorBars
     * @param isSmallSampleCorrection
     * @param isOutliningOfCharacters
     * @param isFineprint
     * @param isNumberingOfXAxis
     * @param isStretching
     * @param isBoxingCahrs
     * @param isYAxis
     */
    private void makeSeqLogo(Double noOfBitsInBar, File inputFile, File outputFile, String outputFormat, Double hieghtOfLogo,
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
        commandArguments.add(outputFile.getAbsolutePath());
        commandArguments.add("-s");
        commandArguments.add(sequenceStart.toString());
//        commandArguments.add("-t");
//        commandArguments.add(title);
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

    }

    public void setBioProspectorOutRbsFile(File bioProspectorOutRbsFile) {
        this.bioProspectorOutRbsFile = bioProspectorOutRbsFile;
    }

    public File getLogoRbs() {
        return logoRbs;
    }

    public void setLogoRbs(File logoRbs) {
        this.logoRbs = logoRbs;
    }

    /**
     *
     * @return
     */
    public File getLogoMinus10() {
        return logoMinus10;
    }

    /**
     * Set PNG file which is a logo representing a minus ten region.
     *
     * @param logo
     */
    public void setLogoMinus10(File logo) {
        this.logoMinus10 = logo;
    }

    /**
     * Return a PNG File which is a logo that represents a minus 35 region.
     *
     * @return PNG file.
     */
    public File getLogoMinus35() {
        return logoMinus35;
    }

    /**
     *
     * @param logo
     */
    public void setLogoMinus35(File logo) {
        this.logoMinus35 = logo;
    }

    /**
     * This method gets upstream regions from elements of a spesific type of a
     * detected transcription start sites.
     *
     * @param type ElementsOfInterest specifies the typ of transcription start
     * site.
     * @param starts list of detected transcriptional start site objects.
     * @param length region upstream relative to a TSS
     * @param isRbsAnalysis
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

    public RbsMotifSearchPanel getRbsMotifSearchPanel() {
        return rbsMotifSearchPanel;
    }

    public void setRbsMotifSearchPanel(RbsMotifSearchPanel rbsMotifSearchPanel) {
        this.rbsMotifSearchPanel = rbsMotifSearchPanel;
    }

    @Override
    public void update(Object args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private File writeBackgroundSequencesInFile(File workingDir) {
        // ##########KAPSELN############
        File backgroundSequencesFile = new File(workingDir.getAbsolutePath() + "\\" + "backgroundSequences" + ".fna");
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(backgroundSequencesFile.getAbsolutePath()), "utf-8"));

            for (String region : this.upstreamRegions) {
                writer.write(region);
            }
            writer.close();

        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return backgroundSequencesFile;
    }
}
