
package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;


import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.DoubleVerifier;
import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.IntegerVerifier;
import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;


public final class WholeTranscriptTracksVisualPanel extends JPanel {

    private final String wizardName;
    private File referenceFile;
    private final String fractionText = "<html><b>Fraction</b> (used for background threshold calculation, #FP)</html>";
    private final String ratioText = "<html>Include <b>Ratio</b> as additional threshold</html>";
    private final String includeBestMatchedReadsText = "<html>Include <b>best matched</b> reads into analysis</html>";


    /**
     * Creates new form WholeTranscriptTracksVisualPanel
     */
    public WholeTranscriptTracksVisualPanel( String wizardName ) {
        this.wizardName = wizardName;
        initComponents();
        this.fractionNovelRegionTF.setInputVerifier( new DoubleVerifier( this.fractionNovelRegionTF ) );
        this.fractionOperonDetectionTF.setInputVerifier( new DoubleVerifier( this.fractionOperonDetectionTF ) );
        this.increaseRatioValueTF.setInputVerifier( new IntegerVerifier( this.increaseRatioValueTF ) );
        this.increaseRatioValueTF.setEnabled( false );
        this.referenceFile = null;
        this.fractionForNrLabel.setText( fractionText );
        this.fractionForOpLabel.setText( fractionText );
        this.includeRatioValueCB.setText( ratioText );
        this.includeBestMatchedReadsIntoOPAnalysis.setText( includeBestMatchedReadsText );
        this.includeBestMatchedReadsIntoNRAnalysis.setText( includeBestMatchedReadsText );
        this.includeBestMatchedReadsIntoRpkmAnalysis.setText( includeBestMatchedReadsText );
        bgThresholdSetManually_novelTrans.setEnabled( false );
        bgThresholdSetManually_novelTrans.setInputVerifier( new IntegerVerifier( bgThresholdSetManually_novelTrans ) );
        bgThresholdSetManually_op.setEnabled( false );
        bgThresholdSetManually_op.setInputVerifier( new IntegerVerifier( bgThresholdSetManually_op ) );
        updateCheckFields();
    }


    @Override
    public String getName() {
        return "Analysis of whole transcript RNA-seq data set";
    }


    public boolean isNewRegions() {
        return this.newRegionsCheckBox.isSelected();
    }


    public boolean isOperonDetection() {
        return this.operonsCheckBox.isSelected();
    }


    public boolean isRPKM() {
        return this.rpkmCheckBox.isSelected();
    }


    public boolean isBgThresholdSetManually_NT() {
        return isBgThresholdSetManually_novelTrans.isSelected();
    }


    public boolean isBgThresholdSetManually_OP() {
        return isBgThresholdSetManually_op.isSelected();
    }


    public Integer getBgThresholdSetManually_NT() {
        return Integer.parseInt( bgThresholdSetManually_novelTrans.getText() );
    }


    public Integer getBgThresholdSetManually_OP() {
        return Integer.parseInt( bgThresholdSetManually_op.getText() );
    }


    public double getFractionForOperonDetection() {
        return Double.valueOf( this.fractionOperonDetectionTF.getText() ) / 100;
    }


    public Double getFractionForNewRegionDetection() {
        return Double.valueOf( this.fractionNovelRegionTF.getText() ) / 100;
    }


    public Integer getMinBoundaryForNovelRegionDetection() {
        return Integer.valueOf( this.minBoundaryNovelRegionTF.getText() );
    }


    public boolean isInclusionOfRatioValueSelected() {
        return this.includeRatioValueCB.isSelected();
    }


    public boolean isIncludeBestMatchedReadsNR() {
        return this.includeBestMatchedReadsIntoNRAnalysis.isSelected();
    }


    public boolean isIncludeBestMatchedReadsOP() {
        return this.includeBestMatchedReadsIntoOPAnalysis.isSelected();
    }


    public boolean isIncludeBestMatchedReadsRpkm() {
        return this.includeBestMatchedReadsIntoRpkmAnalysis.isSelected();
    }


    public Integer getIncreaseRatioValue() {
        return Integer.valueOf( this.increaseRatioValueTF.getText() );
    }


    public File getRefFile() {
        return this.referenceFile;
    }


    /**
     * Updates the checkboxes for the read classes with the globally stored
     * settings for this wizard. If no settings were stored, the default
     * configuration is chosen.
     */
    private void updateCheckFields() {
        Preferences pref = NbPreferences.forModule( Object.class );

        if( pref.getBoolean( wizardName + WizardPropertyStrings.PROP_NOVEL_ANALYSIS, false ) ) {
            String fractionValueNR = pref.get( wizardName + WizardPropertyStrings.PROP_Fraction, "5" );
            int fractionNR;
            if( fractionValueNR.equals( "5" ) ) {
                fractionNR = 5;
            }
            else {
                double iNR = Double.valueOf( fractionValueNR );
                fractionNR = (int) (iNR * 100);
            }
            this.fractionNovelRegionTF.setText( "" + fractionNR );
        }

        this.minBoundaryNovelRegionTF.setText( pref.get( wizardName + WizardPropertyStrings.PROP_MIN_LENGTH_OF_NOVEL_TRANSCRIPT, "100" ) );
        if( pref.getBoolean( wizardName + WizardPropertyStrings.PROP_OPERON_ANALYSIS, false ) ) {
            int fractionOP;
            String fractionValueOP = pref.get( wizardName + WizardPropertyStrings.PROP_Fraction, "5" );
            if( fractionValueOP.equals( "5" ) ) {
                fractionOP = 5;
            }
            else {
                double iOP = Double.valueOf( pref.get( wizardName + WizardPropertyStrings.PROP_Fraction, "5" ) );
                fractionOP = (int) (iOP * 100);
            }
            this.fractionOperonDetectionTF.setText( "" + fractionOP );
        }
        this.increaseRatioValueTF.setText( pref.get( wizardName + WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION, "5" ) );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        newRegionsCheckBox = new javax.swing.JCheckBox();
        fractionForNrLabel = new javax.swing.JLabel();
        fractionNovelRegionTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        minBoundaryNovelRegionTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        includeRatioValueCB = new javax.swing.JCheckBox();
        includeBestMatchedReadsIntoNRAnalysis = new javax.swing.JCheckBox();
        increaseRatioValueTF = new javax.swing.JTextField();
        fractionOperonDetectionTF = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        fractionForOpLabel = new javax.swing.JLabel();
        includeBestMatchedReadsIntoOPAnalysis = new javax.swing.JCheckBox();
        includeBestMatchedReadsIntoRpkmAnalysis = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        operonsCheckBox = new javax.swing.JCheckBox();
        rpkmCheckBox = new javax.swing.JCheckBox();
        loadRefFile = new javax.swing.JButton();
        referenceLocation = new javax.swing.JLabel();
        isBgThresholdSetManually_novelTrans = new javax.swing.JCheckBox();
        bgThresholdSetManually_novelTrans = new javax.swing.JTextField();
        isBgThresholdSetManually_op = new javax.swing.JCheckBox();
        bgThresholdSetManually_op = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(513, 322));

        org.openide.awt.Mnemonics.setLocalizedText(newRegionsCheckBox, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.newRegionsCheckBox.text")); // NOI18N
        newRegionsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newRegionsCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fractionForNrLabel, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.fractionForNrLabel.text")); // NOI18N

        fractionNovelRegionTF.setText(org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.fractionNovelRegionTF.text")); // NOI18N
        fractionNovelRegionTF.setPreferredSize(new java.awt.Dimension(30, 20));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.jLabel4.text")); // NOI18N

        minBoundaryNovelRegionTF.setText(org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.minBoundaryNovelRegionTF.text")); // NOI18N
        minBoundaryNovelRegionTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minBoundaryNovelRegionTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(includeRatioValueCB, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.includeRatioValueCB.text")); // NOI18N
        includeRatioValueCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeRatioValueCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(includeBestMatchedReadsIntoNRAnalysis, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.includeBestMatchedReadsIntoNRAnalysis.text")); // NOI18N

        increaseRatioValueTF.setText(org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.increaseRatioValueTF.text")); // NOI18N

        fractionOperonDetectionTF.setText(org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.fractionOperonDetectionTF.text")); // NOI18N
        fractionOperonDetectionTF.setPreferredSize(new java.awt.Dimension(30, 20));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fractionForOpLabel, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.fractionForOpLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(includeBestMatchedReadsIntoOPAnalysis, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.includeBestMatchedReadsIntoOPAnalysis.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(includeBestMatchedReadsIntoRpkmAnalysis, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.includeBestMatchedReadsIntoRpkmAnalysis.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operonsCheckBox, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.operonsCheckBox.text")); // NOI18N
        operonsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operonsCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(rpkmCheckBox, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.rpkmCheckBox.text")); // NOI18N
        rpkmCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rpkmCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(loadRefFile, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.loadRefFile.text")); // NOI18N
        loadRefFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadRefFileActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(referenceLocation, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.referenceLocation.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(isBgThresholdSetManually_novelTrans, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.isBgThresholdSetManually_novelTrans.text")); // NOI18N
        isBgThresholdSetManually_novelTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isBgThresholdSetManually_novelTransActionPerformed(evt);
            }
        });

        bgThresholdSetManually_novelTrans.setText(org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.bgThresholdSetManually_novelTrans.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(isBgThresholdSetManually_op, org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.isBgThresholdSetManually_op.text")); // NOI18N
        isBgThresholdSetManually_op.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isBgThresholdSetManually_opActionPerformed(evt);
            }
        });

        bgThresholdSetManually_op.setText(org.openide.util.NbBundle.getMessage(WholeTranscriptTracksVisualPanel.class, "WholeTranscriptTracksVisualPanel.bgThresholdSetManually_op.text")); // NOI18N
        bgThresholdSetManually_op.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgThresholdSetManually_opActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jSeparator5))
                    .addComponent(jSeparator6)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(newRegionsCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(24, 24, 24))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(operonsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(rpkmCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, Short.MAX_VALUE)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(loadRefFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(referenceLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel5)
                                    .addComponent(includeRatioValueCB)
                                    .addComponent(fractionForOpLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(includeBestMatchedReadsIntoOPAnalysis)
                                    .addComponent(includeBestMatchedReadsIntoRpkmAnalysis)
                                    .addComponent(fractionForNrLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(includeBestMatchedReadsIntoNRAnalysis)
                                    .addComponent(isBgThresholdSetManually_novelTrans, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(isBgThresholdSetManually_op, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bgThresholdSetManually_novelTrans, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(increaseRatioValueTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(minBoundaryNovelRegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel4))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(fractionNovelRegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel2))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(fractionOperonDetectionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel6))
                                    .addComponent(bgThresholdSetManually_op, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isBgThresholdSetManually_novelTrans)
                    .addComponent(bgThresholdSetManually_novelTrans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fractionNovelRegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fractionForNrLabel)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minBoundaryNovelRegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(newRegionsCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includeRatioValueCB)
                    .addComponent(increaseRatioValueTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(includeBestMatchedReadsIntoNRAnalysis)
                .addGap(18, 18, 18)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isBgThresholdSetManually_op)
                    .addComponent(bgThresholdSetManually_op, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fractionForOpLabel)
                    .addComponent(fractionOperonDetectionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(operonsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(includeBestMatchedReadsIntoOPAnalysis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includeBestMatchedReadsIntoRpkmAnalysis)
                    .addComponent(rpkmCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(loadRefFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(referenceLocation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void includeRatioValueCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeRatioValueCBActionPerformed
        if( includeRatioValueCB.isSelected() ) {
            increaseRatioValueTF.setEnabled( true );
        }
        else {
            increaseRatioValueTF.setEnabled( false );
        }
    }//GEN-LAST:event_includeRatioValueCBActionPerformed

    private void minBoundaryNovelRegionTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minBoundaryNovelRegionTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_minBoundaryNovelRegionTFActionPerformed

    private void loadRefFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadRefFileActionPerformed
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( new String[]{ "txt" }, "Text file" ) {
            @Override
            public void save( String fileLocation ) {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.

            }


            @Override
            public void open( String fileLocation ) {
                referenceFile = new File( fileLocation );
                referenceLocation.setText( referenceFile.getName() );
            }


        };
        fileChooser.openFileChooser( ReadXplorerFileChooser.OPEN_DIALOG );
    }//GEN-LAST:event_loadRefFileActionPerformed

    private void newRegionsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRegionsCheckBoxActionPerformed
        if( newRegionsCheckBox.isSelected() ) {
            operonsCheckBox.setSelected( false );
            rpkmCheckBox.setSelected( false );
        }
    }//GEN-LAST:event_newRegionsCheckBoxActionPerformed

    private void bgThresholdSetManually_opActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgThresholdSetManually_opActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bgThresholdSetManually_opActionPerformed

    private void isBgThresholdSetManually_novelTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isBgThresholdSetManually_novelTransActionPerformed
        if( isBgThresholdSetManually_novelTrans.isSelected() ) {
            bgThresholdSetManually_novelTrans.setEnabled( true );
            fractionForNrLabel.setEnabled( false );
            fractionNovelRegionTF.setEnabled( false );
        }
        else {
            bgThresholdSetManually_novelTrans.setEnabled( false );
            fractionForNrLabel.setEnabled( true );
            fractionNovelRegionTF.setEnabled( true );
        }
    }//GEN-LAST:event_isBgThresholdSetManually_novelTransActionPerformed

    private void operonsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_operonsCheckBoxActionPerformed
        if( operonsCheckBox.isSelected() ) {
            newRegionsCheckBox.setSelected( false );
            rpkmCheckBox.setSelected( false );
        }
    }//GEN-LAST:event_operonsCheckBoxActionPerformed

    private void rpkmCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rpkmCheckBoxActionPerformed
        if( rpkmCheckBox.isSelected() ) {
            operonsCheckBox.setSelected( false );
            newRegionsCheckBox.setSelected( false );
        }
    }//GEN-LAST:event_rpkmCheckBoxActionPerformed

    private void isBgThresholdSetManually_opActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isBgThresholdSetManually_opActionPerformed
        if( isBgThresholdSetManually_op.isSelected() ) {
            bgThresholdSetManually_op.setEnabled( true );
            fractionForOpLabel.setEnabled( false );
            fractionOperonDetectionTF.setEnabled( false );
        }
        else {
            bgThresholdSetManually_op.setEnabled( false );
            fractionForOpLabel.setEnabled( true );
            fractionOperonDetectionTF.setEnabled( true );
        }
    }//GEN-LAST:event_isBgThresholdSetManually_opActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bgThresholdSetManually_novelTrans;
    private javax.swing.JTextField bgThresholdSetManually_op;
    private javax.swing.JLabel fractionForNrLabel;
    private javax.swing.JLabel fractionForOpLabel;
    private javax.swing.JTextField fractionNovelRegionTF;
    private javax.swing.JTextField fractionOperonDetectionTF;
    private javax.swing.JCheckBox includeBestMatchedReadsIntoNRAnalysis;
    private javax.swing.JCheckBox includeBestMatchedReadsIntoOPAnalysis;
    private javax.swing.JCheckBox includeBestMatchedReadsIntoRpkmAnalysis;
    private javax.swing.JCheckBox includeRatioValueCB;
    private javax.swing.JTextField increaseRatioValueTF;
    private javax.swing.JCheckBox isBgThresholdSetManually_novelTrans;
    private javax.swing.JCheckBox isBgThresholdSetManually_op;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JButton loadRefFile;
    private javax.swing.JTextField minBoundaryNovelRegionTF;
    private javax.swing.JCheckBox newRegionsCheckBox;
    private javax.swing.JCheckBox operonsCheckBox;
    private javax.swing.JLabel referenceLocation;
    private javax.swing.JCheckBox rpkmCheckBox;
    // End of variables declaration//GEN-END:variables
}
