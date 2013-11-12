/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.transcriptomeAnalyses.wizard.PromotorDetectionParamVisualPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class MotifSearchPanel extends javax.swing.JPanel {

    public static final String CURRENT_DIR = "currentDirectory";
    private List<String> promotors;
    private int downStreamRegion;
    private HashMap<String, Integer> bioProspectorOut;
    private int spacer1, spacer2, sequenceToAnalyzeLength, sequenceToAnaylzeLength2, motifLength1, motifLength2;

    /**
     * Creates new form MotifSearchPanel
     */
    public MotifSearchPanel(List<String> promotors, int downStream) {
        this.downStreamRegion = downStream;
        this.promotors = promotors;
        initComponents();
        additionalInits();
    }

    public int getSpacer1() {
        return spacer1;
    }

    public void setSpacer1(int spacer1) {
        this.spacer1 = spacer1;
    }

    public int getSequenceToAnalyzeLength() {
        return sequenceToAnalyzeLength;
    }

    public void setSequenceToAnalyzeLength(int sequenceToAnalyzeLength) {
        this.sequenceToAnalyzeLength = sequenceToAnalyzeLength;
    }

    /**
     * Some additional settings on components like setting borders.
     */
    private void additionalInits() {
        promotorsPanel.setBorder(BorderFactory.createTitledBorder("Promotor region in Fasta format"));
        promotorsInFastaTextPane.setEditable(false);
        promotorsInFastaTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        regionsPanel.setBorder(BorderFactory.createTitledBorder("Region of intrest"));
        regionOfIntrestMinusTenTP.setEditable(false);
        regionOfIntrestMinusTenTP.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        regionOfIntrestMinusThirtyfiveTP.setEditable(false);
        regionOfIntrestMinusThirtyfiveTP.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bioProspectorPanel.setBorder(BorderFactory.createTitledBorder("Best scored BioProspector Output"));
        bioProspectoerOutTP1.setEditable(false);
        bioProspectoerOutTP1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bioProspectoerOutTP2.setEditable(false);
        bioProspectoerOutTP2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
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
    private void makeSeqLogo(Double noOfBitsInBar, Integer noOfBitsBetweenTicMarcs,
            Integer noOfCharactersPerLine, Integer shrinkFactor, Double errorBarFraction,
            File inputFile, File outputFile, String outputFormat, Double hieghtOfLogo,
            int kindOfData, Integer lowerBound, Integer upperBound, Integer sequenceStart,
            String title, Integer logwidth, String xAxisLabel, String yAxisLabel,
            boolean isAntialiasin, boolean isBarEnds, boolean isColor, boolean isErrorBars,
            boolean isSmallSampleCorrection, boolean isOutliningOfCharacters, boolean isFineprint,
            boolean isNumberingOfXAxis, boolean isStretching, boolean isBoxingCahrs, boolean isYAxis) {

        String perl = "perl";
        String cmd = "C:\\Users\\jritter\\Documents\\MA-Thesis\\weblogo\\seqlogo";
        List<String> commandArguments = new ArrayList<>();
        commandArguments.add(perl);
        commandArguments.add(cmd);
        commandArguments.add("-f");
        commandArguments.add(inputFile.getAbsolutePath());
        commandArguments.add("-B");
        commandArguments.add(noOfBitsInBar.toString());
//        commandArguments.add("-T");
//        commandArguments.add(noOfBitsBetweenTicMarcs.toString());
//        commandArguments.add("-C");
//        commandArguments.add(noOfCharactersPerLine.toString());
//        if (isColor) {
//            commandArguments.add("-d");
//            commandArguments.add(shrinkFactor.toString());
//        }
//        commandArguments.add("-E");
//        commandArguments.add(errorBarFraction.toString());
        commandArguments.add("-F");
        commandArguments.add(outputFormat);
//        commandArguments.add("-h");
//        commandArguments.add(hieghtOfLogo.toString());
//        commandArguments.add("-k");
//        commandArguments.add("" + kindOfData);
//        commandArguments.add("-l");
//        commandArguments.add(lowerBound.toString());
//        commandArguments.add("-m");
//        commandArguments.add(upperBound.toString());
        commandArguments.add("-o");
        commandArguments.add(outputFile.getAbsolutePath());
//        commandArguments.add("-s");
//        commandArguments.add(sequenceStart.toString());
        commandArguments.add("-t");
        commandArguments.add(title);
//        commandArguments.add("-w");
//        commandArguments.add(logwidth.toString());
        commandArguments.add("-x");
        commandArguments.add(xAxisLabel);
        commandArguments.add("-y");
        commandArguments.add(yAxisLabel);
//        if (isAntialiasin) {
//            commandArguments.add("-a");
//        }
//        if (isBarEnds) {
//            commandArguments.add("-b");
//        }
//
        if (isColor) {
            commandArguments.add("-c");
        }
//        if (isErrorBars) {
//            commandArguments.add("-e");
//        }
//        if (isSmallSampleCorrection) {
//            commandArguments.add("-M");
//        }
//        if (isOutliningOfCharacters) {
//            commandArguments.add("-O");
//        }
//        if (isFineprint) {
//            commandArguments.add("-p");
//        }
        if (isNumberingOfXAxis) {
            commandArguments.add("-n");
        }
//        if (isStretching) {
//            commandArguments.add("-S");
//        }
//        if (isBoxingCahrs) {
//            commandArguments.add("-X");
//        }
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

    /**
     * 
     * @param start
     * @param length
     * @param color 
     */
    private void colorSubstringsInPromotorSites(int start, int length, Color color) {

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(
                SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, color);
        this.promotorsInFastaTextPane.getStyledDocument().setCharacterAttributes(start, length, aset, false);
    }

    /**
     * 
     * @param down
     * @param tp
     * @param flag 
     */
    public void writeRegionInTextPane(int down, JTextPane tp, boolean flag) {
        StyledDocument styledDocument = tp.getStyledDocument();
        String header = "";
        for (int i = 0; i < this.promotors.size(); i++) {
            String original = this.promotors.get(i);
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
                    int beginnOfMinusTen = length - down - this.getSpacer1() - this.getSequenceToAnalyzeLength() + this.bioProspectorOut.get(header);
                    int begin = beginnOfMinusTen - this.getSpacer2() - this.getSequenceToAnalyzeLength2();
                    int end = begin + this.getSequenceToAnalyzeLength2();
                    String region = original.substring(begin, end);
                    try {
                        styledDocument.insertString(styledDocument.getLength(), region + "\n", null);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    int length = original.length();
                    int begin = length - down - this.getSpacer1() - this.getSequenceToAnalyzeLength();
                    int end = length - down - this.getSpacer1();
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
     *
     * @param firstMotifWidth
     * @param noOfTimes
     * @param noOfTopMotifs
     * @param sndMotifBlockWidth
     * @param isMotifBlockPalindrome
     * @param minGap
     * @param maxGap
     * @param justExamineFwd
     * @param everySeqHasMotif
     * @return
     */
    private StyledDocument executeBioProspector(String inputFilePath, int firstMotifWidth, int noOfTimes, int noOfTopMotifs, boolean isSndMotifExpected, int sndMotifBlockWidth, int isMotifBlockPalindrome, int minGap, int maxGap, int justExamineFwd, int everySeqHasMotif) throws IOException {
        StyledDocument doc = new DefaultStyledDocument();
        this.bioProspectorOut = new HashMap<>();

        String cmd = "C:\\Users\\jritter\\Documents\\MA-Thesis\\BioProspector.2004\\BioProspector.exe";
        List<String> commandArguments = new ArrayList<>();
        commandArguments.add(cmd);
        commandArguments.add("-i");
        commandArguments.add(inputFilePath);
//        commandArguments.add("/cygdrive/c/Users/jritter/Documents/MA-Thesis/testdata/test.fna");
        commandArguments.add("-W");
        commandArguments.add("" + firstMotifWidth);
        commandArguments.add("-n");
        commandArguments.add("" + noOfTimes);
        commandArguments.add("-r");
        commandArguments.add("" + noOfTopMotifs);
        commandArguments.add("-a");
        commandArguments.add("" + everySeqHasMotif);
        commandArguments.add("-d");
        commandArguments.add("" + justExamineFwd);
        if (isSndMotifExpected) {
            commandArguments.add("-w");
            commandArguments.add("" + sndMotifBlockWidth);
            commandArguments.add("-p");
            commandArguments.add("" + isMotifBlockPalindrome);
            commandArguments.add("-g");
            commandArguments.add("" + minGap);
            commandArguments.add("-G");
            commandArguments.add("" + maxGap);
        }

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
                    this.bioProspectorOut.put(id, Integer.valueOf(start));
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

    public void writePromotorsInTextPane() {
        StyledDocument styleDocSequences = this.promotorsInFastaTextPane.getStyledDocument();
        for (int i = 0; i < this.promotors.size(); i++) {
            try {
                styleDocSequences.insertString(styleDocSequences.getLength(), this.promotors.get(i), null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        this.promotorsInFastaTextPane.setStyledDocument(styleDocSequences);
    }

    private void colorMotifRegion(HashMap<String, Integer> minusTenAlignmentShifts, HashMap<String, Integer> minus35AlignmentShifts) {
        // TODO hier muss der geparste bioprospector output in der styledDocument des fasta textpane 
        // blau markiert werden.
        String header = "";
        Integer alignmentStartMinus10 = 0;
        Integer alignmentStartMinus35 = 0;
        this.promotorsInFastaTextPane.setText("");
        StyledDocument styledDoc = this.promotorsInFastaTextPane.getStyledDocument();
        for (String string : promotors) {
            if (string.startsWith(">")) {
                header = string.substring(1, string.length() - 1);
                alignmentStartMinus10 = minusTenAlignmentShifts.get(header);
                alignmentStartMinus35 = minus35AlignmentShifts.get(header);

                try {
                    styledDoc.insertString(styledDoc.getLength(), string, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                try {
                    styledDoc.insertString(styledDoc.getLength(), string, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }


                int startMinus10 = (styledDoc.getLength() - this.downStreamRegion - this.getSpacer1() - this.getSequenceToAnalyzeLength() - 1);
                int motifStartMinus10 = startMinus10 + alignmentStartMinus10;
                int startMinus35 = (styledDoc.getLength() - this.downStreamRegion - this.getSpacer1() - this.getSequenceToAnalyzeLength() - 1 - this.getSpacer2() - this.getSequenceToAnalyzeLength2());
                int motifStartMinus35 = startMinus35 + alignmentStartMinus35;
                colorSubstringsInPromotorSites(motifStartMinus35, this.getMotifMinus35length(), Color.blue);
                colorSubstringsInPromotorSites(motifStartMinus10, this.getMotifMinus10length(), Color.red);
            }
        }
    }

    private String parseBestScoredBioProspectorOut() {
        String out = new String();

        String content = this.bioProspectoerOutTP1.getText();
        String[] splittedByNewLine = content.split("\n");
        boolean needSeq = false;
        for (String line : splittedByNewLine) {
            if (line.startsWith("Motif#2")) {
                break;
            }

            if (line.startsWith(">")) {
                needSeq = true;
                out += line + "\n";
            } else {
                if (needSeq) {
                    out += line + "\n";
                    needSeq = false;
                }
            }

        }
        return out;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        showPutativeMinusTenRegion = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        startBioProspector = new javax.swing.JButton();
        startSeqLogo = new javax.swing.JButton();
        regionsPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        regionOfIntrestMinusThirtyfiveTP = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        regionOfIntrestMinusTenTP = new javax.swing.JTextPane();
        bioProspectorPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        bioProspectoerOutTP1 = new javax.swing.JTextPane();
        jScrollPane8 = new javax.swing.JScrollPane();
        bioProspectoerOutTP2 = new javax.swing.JTextPane();
        promotorsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        promotorsInFastaTextPane = new javax.swing.JTextPane();
        logoTabbedPane = new javax.swing.JTabbedPane();

        org.openide.awt.Mnemonics.setLocalizedText(showPutativeMinusTenRegion, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.showPutativeMinusTenRegion.text")); // NOI18N
        showPutativeMinusTenRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPutativeMinusTenRegionActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(startBioProspector, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.startBioProspector.text")); // NOI18N
        startBioProspector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBioProspectorActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(startSeqLogo, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.startSeqLogo.text")); // NOI18N
        startSeqLogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startSeqLogoActionPerformed(evt);
            }
        });

        jScrollPane4.setViewportView(regionOfIntrestMinusThirtyfiveTP);

        jSplitPane1.setRightComponent(jScrollPane4);

        jScrollPane6.setViewportView(regionOfIntrestMinusTenTP);

        jSplitPane1.setLeftComponent(jScrollPane6);

        jScrollPane2.setViewportView(jSplitPane1);

        javax.swing.GroupLayout regionsPanelLayout = new javax.swing.GroupLayout(regionsPanel);
        regionsPanel.setLayout(regionsPanelLayout);
        regionsPanelLayout.setHorizontalGroup(
            regionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
        );
        regionsPanelLayout.setVerticalGroup(
            regionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
        );

        jScrollPane7.setViewportView(bioProspectoerOutTP1);

        jSplitPane2.setLeftComponent(jScrollPane7);

        jScrollPane8.setViewportView(bioProspectoerOutTP2);

        jSplitPane2.setRightComponent(jScrollPane8);

        jScrollPane3.setViewportView(jSplitPane2);

        javax.swing.GroupLayout bioProspectorPanelLayout = new javax.swing.GroupLayout(bioProspectorPanel);
        bioProspectorPanel.setLayout(bioProspectorPanelLayout);
        bioProspectorPanelLayout.setHorizontalGroup(
            bioProspectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
        );
        bioProspectorPanelLayout.setVerticalGroup(
            bioProspectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(promotorsInFastaTextPane);

        javax.swing.GroupLayout promotorsPanelLayout = new javax.swing.GroupLayout(promotorsPanel);
        promotorsPanel.setLayout(promotorsPanelLayout);
        promotorsPanelLayout.setHorizontalGroup(
            promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 512, Short.MAX_VALUE)
            .addGroup(promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE))
        );
        promotorsPanelLayout.setVerticalGroup(
            promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 305, Short.MAX_VALUE)
            .addGroup(promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(logoTabbedPane)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(411, 411, 411)
                                .addComponent(showPutativeMinusTenRegion)
                                .addGap(18, 18, 18)
                                .addComponent(startBioProspector)
                                .addGap(18, 18, 18)
                                .addComponent(startSeqLogo)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(promotorsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(regionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(bioProspectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(promotorsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(regionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bioProspectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startBioProspector)
                    .addComponent(showPutativeMinusTenRegion)
                    .addComponent(startSeqLogo))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(logoTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane5.setViewportView(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void startSeqLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSeqLogoActionPerformed
        SequenceLogoParametersPanel seqlogo = new SequenceLogoParametersPanel();

        NotifyDescriptor nd = new NotifyDescriptor(
                seqlogo, // instance of your panel
                "BioProspector Settings", // title of the dialog
                NotifyDescriptor.OK_CANCEL_OPTION, // it is Yes/No dialog ...
                NotifyDescriptor.QUESTION_MESSAGE, // ... of a question type => a question mark icon
                null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                // otherwise specify options as:
                //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                NotifyDescriptor.OK_CANCEL_OPTION // default option is "Yes"
                );

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            seqlogo.storePrefs();

            if (seqlogo.getInputFile() != null && seqlogo.getOutputFile() != null) {
                makeSeqLogo(seqlogo.getNoOfBitsInBar(), seqlogo.getNoOfBarsBetweenTicMarks(),
                        seqlogo.getNoOfCharacters(), seqlogo.getShrinkFactorOfCharacters(), seqlogo.getFractionOfErrorBars(),
                        seqlogo.getInputFile(), seqlogo.getOutputFile(), seqlogo.getOutputFormat(), seqlogo.getHightOfOutputLogo(),
                        seqlogo.getKindOfData(), seqlogo.getLowerboundIfSequence(), seqlogo.getUpperboundOfSequence(),
                        seqlogo.getSequenceStartNumber(), seqlogo.getTitle(), seqlogo.getWidthOfOutputLogo(), seqlogo.getLabelForXAxis(),
                        seqlogo.getLabelForYAxis(), seqlogo.isAntialiasin(), seqlogo.isBarEnds(), seqlogo.isColor(), seqlogo.isErrorBars(),
                        seqlogo.isSmallSampleCorrection(), seqlogo.isOutliningOfCharacters(), seqlogo.isFineprint(),
                        seqlogo.isNumberingOfXAxis(), seqlogo.isStretching(), seqlogo.isBoxingCahrs(), seqlogo.isYAxis());
            } else {
                NotifyDescriptor attention = new NotifyDescriptor(
                        new JLabel("Please choose an input and an output file!"), // instance of your panel
                        "WARNING", // title of the dialog
                        NotifyDescriptor.WARNING_MESSAGE,
                        NotifyDescriptor.WARNING_MESSAGE, // ... of a question type => a question mark icon
                        null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                        // otherwise specify options as:
                        //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                        NotifyDescriptor.OK_OPTION // default option is "Yes"
                        );
            }
            
            JPanel panel = new JPanel(new BorderLayout());
        JLabel logoLabel = new JLabel();
        panel.add(logoLabel, BorderLayout.CENTER);

        if (seqlogo.getOutputFile() != null) {
                if (seqlogo.getOutputFormat().equals("PNG")) {
                    Icon icon = new ImageIcon(seqlogo.getOutputFile().getAbsolutePath() + ".png");
                    logoLabel.setIcon(icon);
                    this.logoTabbedPane.addTab(seqlogo.getTitle(), panel);
                } else if (seqlogo.getOutputFormat().equals("GIF")) {
                    Icon icon = new ImageIcon(seqlogo.getOutputFile().getAbsolutePath() + ".gif");
                    logoLabel.setIcon(icon);
                    this.logoTabbedPane.addTab(seqlogo.getTitle(), panel);
                }
        } else {
            System.out.println("Die Datai: " + seqlogo.getOutputFile().getAbsolutePath() + " does not exist.");
        }
        }

        
    }//GEN-LAST:event_startSeqLogoActionPerformed

    private void startBioProspectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBioProspectorActionPerformed
        StyledDocument bioProspectorOut = null;
        BioProspectorParameters bp = new BioProspectorParameters();


        NotifyDescriptor nd = new NotifyDescriptor(
                bp, // instance of your panel
                "BioProspector Settings", // title of the dialog
                NotifyDescriptor.OK_CANCEL_OPTION, // it is Yes/No dialog ...
                NotifyDescriptor.INFORMATION_MESSAGE, // ... of a question type => a question mark icon
                null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                // otherwise specify options as:
                //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                NotifyDescriptor.OK_CANCEL_OPTION // default option is "Yes"
                );

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            this.setMotifMinus10Length(bp.getMinusTenMotifWidth());
            this.setMotifMinus35Length(bp.getThirtyFiveMotifWidth());
            
            // #####Write and SAVE putative -10 region##########################
            File workingDir = bp.getWorkingDir();

            Writer writer = null;
            File minusTenFile = new File(workingDir.getAbsolutePath() + "\\minusTenRegion.fna");
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(minusTenFile.getAbsolutePath()), "utf-8"));
                writer.write(this.regionOfIntrestMinusTenTP.getText());
            } catch (IOException ex) {
                // report
            } finally {
                try {
                    writer.close();
                } catch (Exception ex) {
                }
            }

            // ####Analysze -10#################################################
            String posixPath = "/cygdrive/c";
            
            if (workingDir.isDirectory()) {
                String sub = minusTenFile.getAbsolutePath().toString().substring(2);
                posixPath += sub.replaceAll("\\\\", "/");

                try {
                    bioProspectorOut = this.executeBioProspector(posixPath, bp.getMinusTenMotifWidth(),
                            bp.getNoOfTimesTrying(), bp.getNoOfTopMotifs(), bp.expectingTwoMitifBlocks(), bp.getSecondMotifWidth(),
                            bp.isTwoMotifBlockPalindrome(), bp.getMinGap(), bp.getMaxGap(),
                            bp.isOnlyFwdExamination(), bp.everySeqHasMotif());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                this.bioProspectoerOutTP1.setStyledDocument(bioProspectorOut);
                File bioProspectorOutMinus10File = new File(bp.getWorkingDir().getAbsolutePath() + "\\bioProspectorOutMinus10.fna");

                String[] bioProspectorOutInArr = bioProspectoerOutTP1.getText().split("\\n");


                try {
                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(bioProspectorOutMinus10File.getAbsolutePath()), "utf-8"));

                    for (int i = 0; i < bioProspectorOutInArr.length; i++) {
                        String string = bioProspectorOutInArr[i];
                        if(string.startsWith("Motif#2")){
                            break;
                        }
                        if (string.startsWith(">")) {
                            writer.write(string);
                            writer.write(bioProspectorOutInArr[i + 1]);
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

            HashMap<String, Integer> alignmentshiftsOFMinusTen = new HashMap<>();
            alignmentshiftsOFMinusTen.putAll(this.bioProspectorOut);

            // Now i have to extract and save the putative -35 region
            // hier habe ich nun die Länge des zweiten spacers und die länge der zweiten zu analysierenden sequenz
            this.writeRegionInTextPane(this.downStreamRegion, this.regionOfIntrestMinusThirtyfiveTP, true);
//            int start = (styledDoc.getLength() - this.downStreamRegion - this.getSpacer1() - this.getSequenceToAnalyzeLength() - 1);

            // write and save -35 region

            File minusThirtyFiveFile = new File(workingDir.getAbsolutePath() + "\\putativeMinus35.fna");
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(minusThirtyFiveFile.getAbsolutePath()), "utf-8"));
                writer.write(this.regionOfIntrestMinusThirtyfiveTP.getText());
            } catch (IOException ex) {
                // report
            } finally {
                try {
                    writer.close();
                } catch (Exception ex) {
                }
            }

            if (workingDir.isDirectory()) {
                posixPath = "/cygdrive/c";
                String sub = minusThirtyFiveFile.getAbsolutePath().toString().substring(2);
                posixPath += sub.replaceAll("\\\\", "/");

                try {
                    bioProspectorOut = this.executeBioProspector(posixPath, bp.getThirtyFiveMotifWidth(),
                            bp.getNoOfTimesTrying(), bp.getNoOfTopMotifs(), bp.expectingTwoMitifBlocks(), bp.getSecondMotifWidth(),
                            bp.isTwoMotifBlockPalindrome(), bp.getMinGap(), bp.getMaxGap(),
                            bp.isOnlyFwdExamination(), bp.everySeqHasMotif());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                this.bioProspectoerOutTP2.setStyledDocument(bioProspectorOut);
                File bioProspectorOutMinus35File = new File(bp.getWorkingDir().getAbsolutePath() + "\\bioProspectorOutMinus35.fna");
                String[] bioProspectorOutInArr = bioProspectoerOutTP2.getText().split("\\n");


                try {
                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(bioProspectorOutMinus35File.getAbsolutePath()), "utf-8"));

                    for (int i = 0; i < bioProspectorOutInArr.length; i++) {
                        String string = bioProspectorOutInArr[i];
                        if(string.startsWith("Motif#2")){
                            break;
                        }
                        if (string.startsWith(">")) {
                            writer.write(string);
                            writer.write(bioProspectorOutInArr[i + 1]);
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

            HashMap<String, Integer> alignmentshiftsOf35 = new HashMap<>();
            alignmentshiftsOf35.putAll(this.bioProspectorOut);


            // Hier geht es nur noch um das FÄRBEN!
            colorMotifRegion(alignmentshiftsOFMinusTen, alignmentshiftsOf35);
        }
    }//GEN-LAST:event_startBioProspectorActionPerformed

    private void showPutativeMinusTenRegionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPutativeMinusTenRegionActionPerformed
//        ExtractingMinusTenRegionParams tenRegion = new ExtractingMinusTenRegionParams();
        PromotorDetectionParamVisualPanel promotorSettings = new PromotorDetectionParamVisualPanel("");

        NotifyDescriptor nd = new NotifyDescriptor(
                promotorSettings, // instance of your panel
                "Parameters for extracting sequence for -10 motif analysis", // title of the dialog
                NotifyDescriptor.OK_CANCEL_OPTION, // it is Yes/No dialog ...
                NotifyDescriptor.INFORMATION_MESSAGE, // ... of a question type => a question mark icon
                null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                // otherwise specify options as:
                //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                NotifyDescriptor.OK_CANCEL_OPTION // default option is "Yes"
                );
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            this.setSequenceToAnalyzeLength(promotorSettings.getPutativeMinusTenLength());
            this.setSquenceToAnaylzeLength2(promotorSettings.getPutativeMinusThirtyFiveLength());
            this.setSpacer1(promotorSettings.getSpacer1Length());
            this.setSpacer2(promotorSettings.getSpacer2Length());
            this.writeRegionInTextPane(this.downStreamRegion, this.regionOfIntrestMinusTenTP, false);
        }


    }//GEN-LAST:event_showPutativeMinusTenRegionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane bioProspectoerOutTP1;
    private javax.swing.JTextPane bioProspectoerOutTP2;
    private javax.swing.JPanel bioProspectorPanel;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane logoTabbedPane;
    private javax.swing.JTextPane promotorsInFastaTextPane;
    private javax.swing.JPanel promotorsPanel;
    private javax.swing.JTextPane regionOfIntrestMinusTenTP;
    private javax.swing.JTextPane regionOfIntrestMinusThirtyfiveTP;
    private javax.swing.JPanel regionsPanel;
    private javax.swing.JButton showPutativeMinusTenRegion;
    private javax.swing.JButton startBioProspector;
    private javax.swing.JButton startSeqLogo;
    // End of variables declaration//GEN-END:variables

    private int getMotifMinus10length() {
        return this.motifLength1;
    }

    private void setMotifMinus10Length(int length) {
        this.motifLength1 = length;
    }

    private int getMotifMinus35length() {
        return this.motifLength2;
    }

    private void setMotifMinus35Length(int length) {
        this.motifLength2 = length;
    }

    private void setSquenceToAnaylzeLength2(Integer putativeMinusThirtyFiveLength) {
        this.sequenceToAnaylzeLength2 = putativeMinusThirtyFiveLength;
    }

    private int getSequenceToAnalyzeLength2() {
        return this.sequenceToAnaylzeLength2;
    }

    private void setSpacer2(Integer spacer2Length) {
        this.spacer2 = spacer2Length;
    }

    private int getSpacer2() {
        return this.spacer2;
    }
}
