/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses;

import java.awt.Color;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import org.openide.util.NbPreferences;

/**
 *
 * @author jritter
 */
public class SequenceLogoParametersPanel extends javax.swing.JPanel {

    private File inputFile;
    private File outputFile;
    private static final String SEQLOGO_WIDTH_OF_LOGO = "width";
    private static final String SEQLOGO_NO_OF_BITS = "bits";
    private static final String SEQLOGO_LABEL_X = "xAxisLabel";
    private static final String SEQLOGO_LABEL_Y = "yAxisLabel";
    private static final String SEQLOGO_FRACTION_OF_ERROR = "fractionOfErrorBars";
    private static final String SEQLOGO_HEIGHT_OF_LOGO = "height";
    private static final String SEQLOGO_TITLE = "title";
    private static final String SEQLOGO_FORMAT = "format";
    private static final String SEQLOGO_SEQUENCE_START = "seqStart";
    private static final String SEQLOGO_NO_OF_BITS_BETWEEN_TIC_MARKS = "bitsBetweenTicMarks";
    private static final String SEQLOGO_UPPERBOUND = "upperBound";
    private static final String SEQLOGO_LOWERBOUND = "lowerBound";
    private static final String SEQLOGO_NO_OF_CHARS = "numberOfCharacters";
    private static final String SEQLOGO_SHRINK_FACTOR = "shrinkFactor";

    /**
     * Creates new form SequenceLogoParametersPanel
     */
    public SequenceLogoParametersPanel() {
        initComponents();
        this.dataButtonGroup.add(aminoCB);
        this.dataButtonGroup.add(nucleicCB);
        this.nucleicCB.setSelected(true);
        this.shringFactorOfCaracters.setEnabled(false);
        this.colorCB.setSelected(true);
        this.yAxisCB.setSelected(true);
        this.numberingOfXaxisCB.setSelected(true);
        update();
    }

    private void update() {
        Preferences pref = NbPreferences.forModule(Object.class);
        this.noOfBitsInBar.setText(pref.get(SEQLOGO_NO_OF_BITS, "2"));
        this.noOfBitsBetweenTicMarks.setText(pref.get(SEQLOGO_NO_OF_BITS_BETWEEN_TIC_MARKS, "0"));
        this.noOfCharacters.setText(pref.get(SEQLOGO_NO_OF_CHARS, "0"));
        this.widthOfOutLogo.setText(pref.get(SEQLOGO_WIDTH_OF_LOGO, "0"));
        this.heightOfOutLogo.setText(pref.get(SEQLOGO_HEIGHT_OF_LOGO, "5"));
        this.labelOfXAxis.setText(pref.get(SEQLOGO_LABEL_X, "X"));
        this.labelOfYAxis.setText(pref.get(SEQLOGO_LABEL_Y, "Y"));
        this.textOfTitle.setText(pref.get(SEQLOGO_TITLE, "TITLE"));
        this.outputFormat.setText(pref.get(SEQLOGO_FORMAT, "PNG"));
        this.upperBoundOfSeq.setText(pref.get(SEQLOGO_UPPERBOUND, "0"));
        this.lowerBoundOfSeq.setText(pref.get(SEQLOGO_LOWERBOUND, "0"));
        this.fractionOfErrorBar.setText(pref.get(SEQLOGO_FRACTION_OF_ERROR, "0"));
        this.seqStartNumber.setText(pref.get(SEQLOGO_SEQUENCE_START, "1"));
        this.shringFactorOfCaracters.setText(pref.get(SEQLOGO_SHRINK_FACTOR, "0"));
        
    }
    
    /**
     * Stores the selected read classes for this specific wizard for later use,
     * also after restarting the software.
     * @param readClassParams The parameters to store
     */
    public void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        
        pref.put(SEQLOGO_NO_OF_BITS, getNoOfBitsInBar().toString());
        pref.put(SEQLOGO_NO_OF_BITS_BETWEEN_TIC_MARKS, getNoOfBarsBetweenTicMarks().toString());
        pref.put(SEQLOGO_NO_OF_CHARS, getNoOfCharacters().toString());
        pref.put(SEQLOGO_WIDTH_OF_LOGO, getWidthOfOutputLogo().toString());
        pref.put(SEQLOGO_HEIGHT_OF_LOGO, getHightOfOutputLogo().toString());
        pref.put(SEQLOGO_LABEL_X, getLabelForXAxis().toString());
        pref.put(SEQLOGO_LABEL_Y, getLabelForYAxis().toString());
        pref.put(SEQLOGO_TITLE, getTitle().toString());
        pref.put(SEQLOGO_FORMAT, getOutputFormat().toString());
        pref.put(SEQLOGO_UPPERBOUND, getUpperboundOfSequence().toString());
        pref.put(SEQLOGO_LOWERBOUND, getLowerboundIfSequence().toString());
        pref.put(SEQLOGO_FRACTION_OF_ERROR, getFractionOfErrorBars().toString());
        pref.put(SEQLOGO_SEQUENCE_START, getSequenceStartNumber().toString());
        pref.put(SEQLOGO_SHRINK_FACTOR, getShrinkFactorOfCharacters().toString());
        // TODO store workingDirectoryPath!
    }
    
    public Double getNoOfBitsInBar() {
        return Double.valueOf(this.noOfBitsInBar.getText());
    }

    public Integer getWidthOfOutputLogo() {
        return Integer.valueOf(widthOfOutLogo.getText());
    }

    public Double getHightOfOutputLogo() {
        return Double.valueOf(this.heightOfOutLogo.getText());
    }

    public String getLabelForXAxis() {
        return labelOfXAxis.getText();
    }

    public String getLabelForYAxis() {
        return labelOfYAxis.getText();
    }

    public int getKindOfData() {
        if (this.aminoCB.isSelected()) {
            return 0;
        } else {
            return 1;
        }
    }

    public String getTitle() {
        return this.textOfTitle.getText();
    }

    public String getOutputFormat() {
        return this.outputFormat.getText();
    }

    public Double getFractionOfErrorBars() {
        return Double.valueOf(this.fractionOfErrorBar.getText());
    }

    public Integer getSequenceStartNumber() {
        return Integer.valueOf(this.seqStartNumber.getText());
    }

    public Integer getNoOfBarsBetweenTicMarks() {
        return Integer.valueOf(this.noOfBitsBetweenTicMarks.getText());
    }

    public Integer getUpperboundOfSequence() {
        return Integer.valueOf(this.upperBoundOfSeq.getText());
    }

    public Integer getLowerboundIfSequence() {
        return Integer.valueOf(this.lowerBoundOfSeq.getText());
    }

    public Integer getShrinkFactorOfCharacters() {
        return Integer.valueOf(this.shringFactorOfCaracters.getText());
    }

    public Integer getNoOfCharacters() {
        return Integer.valueOf(this.noOfCharacters.getText());
    }

    public boolean isAntialiasin() {
        return antialiasinCB.isSelected();
    }

    public boolean isBarEnds() {
        return barEndsCB.isSelected();
    }

    public boolean isColor() {
        return colorCB.isSelected();
    }

    public boolean isErrorBars() {
        return errorBarsCB.isSelected();
    }

    public boolean isSmallSampleCorrection() {
        return smallSampleCorrectionCB.isSelected();
    }

    public boolean isOutliningOfCharacters() {
        return outliningOfCharactersCB.isSelected();
    }

    public boolean isFineprint() {
        return fineprintCB.isSelected();
    }

    public boolean isNumberingOfXAxis() {
        return numberingOfXaxisCB.isSelected();
    }

    public boolean isStretching() {
        return stretchingOfLogosToEntireLengthCB.isSelected();
    }

    public boolean isBoxingCahrs() {
        return boxingOfCharactersCB.isSelected();
    }

    public boolean isYAxis() {
        return yAxisCB.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        getInputFileButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        getOutputFileButton = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        noOfBitsInBar = new javax.swing.JTextField();
        widthOfOutLogo = new javax.swing.JTextField();
        labelOfXAxis = new javax.swing.JTextField();
        labelOfYAxis = new javax.swing.JTextField();
        fractionOfErrorBar = new javax.swing.JTextField();
        inputFilePathTF = new javax.swing.JTextField();
        outoutFilePathTF = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        heightOfOutLogo = new javax.swing.JTextField();
        seqStartNumber = new javax.swing.JTextField();
        outputFormat = new javax.swing.JTextField();
        textOfTitle = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        antialiasinCB = new javax.swing.JCheckBox();
        barEndsCB = new javax.swing.JCheckBox();
        colorCB = new javax.swing.JCheckBox();
        errorBarsCB = new javax.swing.JCheckBox();
        smallSampleCorrectionCB = new javax.swing.JCheckBox();
        outliningOfCharactersCB = new javax.swing.JCheckBox();
        fineprintCB = new javax.swing.JCheckBox();
        numberingOfXaxisCB = new javax.swing.JCheckBox();
        yAxisCB = new javax.swing.JCheckBox();
        stretchingOfLogosToEntireLengthCB = new javax.swing.JCheckBox();
        boxingOfCharactersCB = new javax.swing.JCheckBox();
        noOfBitsBetweenTicMarks = new javax.swing.JTextField();
        upperBoundOfSeq = new javax.swing.JTextField();
        lowerBoundOfSeq = new javax.swing.JTextField();
        noOfCharacters = new javax.swing.JTextField();
        shringFactorOfCaracters = new javax.swing.JTextField();
        aminoCB = new javax.swing.JRadioButton();
        nucleicCB = new javax.swing.JRadioButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(getInputFileButton, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.getInputFileButton.text")); // NOI18N
        getInputFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getInputFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel10.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(getOutputFileButton, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.getOutputFileButton.text")); // NOI18N
        getOutputFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getOutputFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel13.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel14.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel15.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel16.text")); // NOI18N

        noOfBitsInBar.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.noOfBitsInBar.text")); // NOI18N

        widthOfOutLogo.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.widthOfOutLogo.text")); // NOI18N

        labelOfXAxis.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.labelOfXAxis.text")); // NOI18N

        labelOfYAxis.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.labelOfYAxis.text")); // NOI18N
        labelOfYAxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelOfYAxisActionPerformed(evt);
            }
        });

        fractionOfErrorBar.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.fractionOfErrorBar.text")); // NOI18N

        inputFilePathTF.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.inputFilePathTF.text")); // NOI18N

        outoutFilePathTF.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.outoutFilePathTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.jLabel17.text")); // NOI18N

        heightOfOutLogo.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.heightOfOutLogo.text")); // NOI18N

        seqStartNumber.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.seqStartNumber.text")); // NOI18N

        outputFormat.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.outputFormat.text")); // NOI18N

        textOfTitle.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.textOfTitle.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(antialiasinCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.antialiasinCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(barEndsCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.barEndsCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(colorCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.colorCB.text")); // NOI18N
        colorCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(errorBarsCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.errorBarsCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(smallSampleCorrectionCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.smallSampleCorrectionCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(outliningOfCharactersCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.outliningOfCharactersCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fineprintCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.fineprintCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(numberingOfXaxisCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.numberingOfXaxisCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(yAxisCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.yAxisCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(stretchingOfLogosToEntireLengthCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.stretchingOfLogosToEntireLengthCB.text")); // NOI18N
        stretchingOfLogosToEntireLengthCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stretchingOfLogosToEntireLengthCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(boxingOfCharactersCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.boxingOfCharactersCB.text")); // NOI18N

        noOfBitsBetweenTicMarks.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.noOfBitsBetweenTicMarks.text")); // NOI18N

        upperBoundOfSeq.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.upperBoundOfSeq.text")); // NOI18N
        upperBoundOfSeq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upperBoundOfSeqActionPerformed(evt);
            }
        });

        lowerBoundOfSeq.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.lowerBoundOfSeq.text")); // NOI18N

        noOfCharacters.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.noOfCharacters.text")); // NOI18N

        shringFactorOfCaracters.setText(org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.shringFactorOfCaracters.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(aminoCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.aminoCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nucleicCB, org.openide.util.NbBundle.getMessage(SequenceLogoParametersPanel.class, "SequenceLogoParametersPanel.nucleicCB.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(antialiasinCB)
                            .addComponent(barEndsCB))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(colorCB)
                                .addGap(34, 34, 34)
                                .addComponent(smallSampleCorrectionCB)
                                .addGap(18, 18, 18)
                                .addComponent(fineprintCB))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(outliningOfCharactersCB)
                                .addGap(18, 18, 18)
                                .addComponent(stretchingOfLogosToEntireLengthCB))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(numberingOfXaxisCB)
                        .addGap(18, 18, 18)
                        .addComponent(yAxisCB)
                        .addGap(18, 18, 18)
                        .addComponent(boxingOfCharactersCB)
                        .addGap(18, 18, 18)
                        .addComponent(errorBarsCB))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel11))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(upperBoundOfSeq, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(noOfBitsBetweenTicMarks))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(aminoCB)
                                .addGap(18, 18, 18)
                                .addComponent(nucleicCB))
                            .addComponent(heightOfOutLogo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(inputFilePathTF)
                                    .addComponent(outoutFilePathTF, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel17))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelOfYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fractionOfErrorBar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelOfXAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(widthOfOutLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(noOfBitsInBar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(47, 47, 47)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel8)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(33, 33, 33)
                                .addComponent(shringFactorOfCaracters, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel10))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(noOfCharacters)
                            .addComponent(getOutputFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(getInputFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(seqStartNumber)
                            .addComponent(outputFormat)
                            .addComponent(textOfTitle)
                            .addComponent(lowerBoundOfSeq))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(noOfBitsInBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(heightOfOutLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthOfOutLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel15)
                    .addComponent(aminoCB)
                    .addComponent(nucleicCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelOfXAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel14)
                    .addComponent(textOfTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelOfYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel7)
                    .addComponent(outputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(fractionOfErrorBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(seqStartNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(getInputFileButton)
                    .addComponent(inputFilePathTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getOutputFileButton)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outoutFilePathTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(noOfBitsBetweenTicMarks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lowerBoundOfSeq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel11)
                        .addComponent(upperBoundOfSeq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(shringFactorOfCaracters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(noOfCharacters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(antialiasinCB)
                    .addComponent(colorCB)
                    .addComponent(smallSampleCorrectionCB)
                    .addComponent(fineprintCB))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(barEndsCB)
                    .addComponent(outliningOfCharactersCB)
                    .addComponent(stretchingOfLogosToEntireLengthCB))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberingOfXaxisCB)
                    .addComponent(yAxisCB)
                    .addComponent(boxingOfCharactersCB)
                    .addComponent(errorBarsCB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void colorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorCBActionPerformed

        if (colorCB.isSelected()) {
            this.shringFactorOfCaracters.setEnabled(true);
        } else {
            this.shringFactorOfCaracters.setEnabled(false);
        }

    }//GEN-LAST:event_colorCBActionPerformed

    private void labelOfYAxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelOfYAxisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_labelOfYAxisActionPerformed

    private void upperBoundOfSeqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upperBoundOfSeqActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_upperBoundOfSeqActionPerformed

    private void getInputFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getInputFileButtonActionPerformed
        Preferences prefs = NbPreferences.forModule(Object.class);
        String currentDirPath = prefs.get(de.cebitec.readXplorer.transcriptomeAnalyses.enums.Preferences.CURRENT_DIR.toString(), null);
        JFileChooser fc = new JFileChooser(currentDirPath);
        int returnValue = fc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.inputFilePathTF.setText(fc.getSelectedFile().getAbsolutePath().toString());
            this.setInputFile(fc.getSelectedFile());
            prefs.put(de.cebitec.readXplorer.transcriptomeAnalyses.enums.Preferences.CURRENT_DIR.toString(), fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_getInputFileButtonActionPerformed

    private void getOutputFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getOutputFileButtonActionPerformed
        Preferences prefs = NbPreferences.forModule(Object.class);
        String currentDirPath = prefs.get(de.cebitec.readXplorer.transcriptomeAnalyses.enums.Preferences.CURRENT_DIR.toString(), null);
        JFileChooser fc = new JFileChooser();
        int returnValue = fc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.outoutFilePathTF.setText(fc.getSelectedFile().getAbsolutePath().toString());
            this.setOutputFile(fc.getSelectedFile());
            prefs.put(de.cebitec.readXplorer.transcriptomeAnalyses.enums.Preferences.CURRENT_DIR.toString(), fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_getOutputFileButtonActionPerformed

    private void stretchingOfLogosToEntireLengthCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stretchingOfLogosToEntireLengthCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stretchingOfLogosToEntireLengthCBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton aminoCB;
    private javax.swing.JCheckBox antialiasinCB;
    private javax.swing.JCheckBox barEndsCB;
    private javax.swing.JCheckBox boxingOfCharactersCB;
    private javax.swing.JCheckBox colorCB;
    private javax.swing.ButtonGroup dataButtonGroup;
    private javax.swing.JCheckBox errorBarsCB;
    private javax.swing.JCheckBox fineprintCB;
    private javax.swing.JTextField fractionOfErrorBar;
    private javax.swing.JButton getInputFileButton;
    private javax.swing.JButton getOutputFileButton;
    private javax.swing.JTextField heightOfOutLogo;
    private javax.swing.JTextField inputFilePathTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField labelOfXAxis;
    private javax.swing.JTextField labelOfYAxis;
    private javax.swing.JTextField lowerBoundOfSeq;
    private javax.swing.JTextField noOfBitsBetweenTicMarks;
    private javax.swing.JTextField noOfBitsInBar;
    private javax.swing.JTextField noOfCharacters;
    private javax.swing.JRadioButton nucleicCB;
    private javax.swing.JCheckBox numberingOfXaxisCB;
    private javax.swing.JCheckBox outliningOfCharactersCB;
    private javax.swing.JTextField outoutFilePathTF;
    private javax.swing.JTextField outputFormat;
    private javax.swing.JTextField seqStartNumber;
    private javax.swing.JTextField shringFactorOfCaracters;
    private javax.swing.JCheckBox smallSampleCorrectionCB;
    private javax.swing.JCheckBox stretchingOfLogosToEntireLengthCB;
    private javax.swing.JTextField textOfTitle;
    private javax.swing.JTextField upperBoundOfSeq;
    private javax.swing.JTextField widthOfOutLogo;
    private javax.swing.JCheckBox yAxisCB;
    // End of variables declaration//GEN-END:variables

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputFile() {
        return this.outputFile;
    }

    private void setOutputFile(File selectedFile) {
        this.outputFile = selectedFile;
    }

    void colorTextfieldOfFileInput() {
        this.inputFilePathTF.setBorder(BorderFactory.createLineBorder(Color.red));
        this.outoutFilePathTF.setBorder(BorderFactory.createLineBorder(Color.red));
    }
}
