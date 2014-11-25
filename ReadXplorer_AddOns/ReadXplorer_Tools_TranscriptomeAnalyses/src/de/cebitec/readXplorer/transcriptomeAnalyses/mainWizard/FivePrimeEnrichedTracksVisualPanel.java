package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.DoubleVerifier;
import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.IntegerVerifier;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.openide.WizardValidationException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

public final class FivePrimeEnrichedTracksVisualPanel extends JPanel {
    
    private String tssDetectionText = "For the determination of TSS for each a position i, the number of\n"
            + " read starts on that strand are considered. A putative TSS is displayed, if the read starts\n"
            + " at this position exceeded the background threshold (parameter: fraction) and the ratio xi/xi-1 at\n"
            + " this position exceeds the threshold (parameter: ratio). Although the default values are based\n"
            + " on experience, it is recommend to try different combinations of parameters to find out which \n"
            + "deliver the best results. Additionally, it is crucial to understand the difference between \n"
            + "coverage and read starts as latter one are used for calculation.";
    private final String wizardName;
    private ReferenceConnector refGenConnector;
    private int nbOfFeatures;
    private int referenceLength;

    /**
     * Creates new form FivePrimeEnrichedTracksVisualPanel
     */
    public FivePrimeEnrichedTracksVisualPanel(String wizardName, int referenceID) {
        initComponents();
        setInputVerifier();
        updateFields();
        this.leaderlessDetectionPanel.setBorder(BorderFactory.createTitledBorder("Detection of leaderless transcripts"));
        this.tlsShiftPanel.setBorder(BorderFactory.createTitledBorder("Detection of TLS shifts"));
        this.wizardName = wizardName;
        this.jScrollPane1.setBorder(BorderFactory.createTitledBorder("TSS-detection"));
        this.descriptionTextArea.setEditable(false);
        this.descriptionTextArea.setText(tssDetectionText);
        this.refGenConnector = ProjectConnector.getInstance().getRefGenomeConnector(referenceID);
        for (PersistantChromosome persistantChromosome : refGenConnector.getRefGenome().getChromosomes().values()) {
            this.referenceLength += persistantChromosome.getLength();
            this.nbOfFeatures += refGenConnector.getFeaturesForClosedInterval(0, persistantChromosome.getLength(), persistantChromosome.getId()).size();
        }
    }
    
    private void setInputVerifier() {
        this.fractionTF.setInputVerifier(new DoubleVerifier(this.fractionTF));
        this.excludeTSSDistanceTF.setInputVerifier(new IntegerVerifier(this.excludeTSSDistanceTF));
        this.keepingDistanceForInternalTssTF.setInputVerifier(new IntegerVerifier(this.keepingDistanceForInternalTssTF));
        this.leaderlessDistanveTF.setInputVerifier(new IntegerVerifier(this.leaderlessDistanveTF));
        this.ratioTF.setInputVerifier(new IntegerVerifier(this.ratioTF));
    }
    
    @Override
    public String getName() {
        return "Identification of transcription start sites using primary 5′-end data";
    }

    
    public Double getFraction() {
        return Double.valueOf(fractionTF.getText());
    }
    
    public Integer getRatio() {
        return Integer.valueOf(ratioTF.getText());
    }
    
    public boolean isExcludeInternalTSS() {
        return exclusionOfAllInternalTSSCB.isSelected();
    }
    
    public Integer getExcludeTssDistance() {
        return Integer.valueOf(excludeTSSDistanceTF.getText());
    }
    
    public Integer getKeepingInternalTssDistance() {
        return Integer.valueOf(keepingDistanceForInternalTssTF.getText());
    }
    
    public Integer getLeaderlessDistance() {
        return Integer.valueOf(this.leaderlessDistanveTF.getText());
    }
    
    public Integer getPercentageForCdsShiftAnalysis() {
        return Integer.valueOf(this.percentageTF.getText());
    }

    /**
     * Updates the checkboxes for the read classes with the globally stored
     * settings for this wizard. If no settings were stored, the default
     * configuration is chosen.
     */
    private void updateFields() {
        Preferences pref = NbPreferences.forModule(Object.class);
        fractionTF.setText(pref.get(wizardName + TranscriptomeAnalysisWizardIterator.PROP_Fraction, "0.05"));
        ratioTF.setText(pref.get(wizardName + TranscriptomeAnalysisWizardIterator.PROP_RATIO, "5"));
        excludeTSSDistanceTF.setText(pref.get(wizardName + TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_TSS_DISTANCE, "500"));
        keepingDistanceForInternalTssTF.setText(pref.get(wizardName + TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_TSS_DISTANCE, "100"));
        percentageTF.setText(pref.get(wizardName + TranscriptomeAnalysisWizardIterator.PROP_PERCENTAGE_FOR_CDS_ANALYSIS, "10"));
        leaderlessDistanveTF.setText(pref.get(wizardName + TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_LIMIT, "0"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        settingsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        fractionTF = new javax.swing.JTextField();
        resultLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        ratioTF = new javax.swing.JTextField();
        exclusionOfAllInternalTSSCB = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        excludeTSSDistanceTF = new javax.swing.JTextField();
        keepInternalLabel = new javax.swing.JLabel();
        keepingDistanceForInternalTssTF = new javax.swing.JTextField();
        separator1 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();
        leaderlessDetectionPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        leaderlessDistanveTF = new javax.swing.JTextField();
        tlsShiftPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        percentageTF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        jScrollPane1.setViewportView(descriptionTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel2.text")); // NOI18N

        fractionTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.fractionTF.text")); // NOI18N
        fractionTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fractionTFActionPerformed(evt);
            }
        });
        fractionTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fractionTFKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.resultLabel.text")); // NOI18N

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/readXplorer/transcriptomeAnalyses/resources/fractionDescription.PNG"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel6.text")); // NOI18N

        ratioTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.ratioTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(exclusionOfAllInternalTSSCB, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.exclusionOfAllInternalTSSCB.text")); // NOI18N
        exclusionOfAllInternalTSSCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exclusionOfAllInternalTSSCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel7.text")); // NOI18N

        excludeTSSDistanceTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.excludeTSSDistanceTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepInternalLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.keepInternalLabel.text")); // NOI18N

        keepingDistanceForInternalTssTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.keepingDistanceForInternalTssTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel10.text")); // NOI18N

        leaderlessDistanveTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.leaderlessDistanveTF.text")); // NOI18N
        leaderlessDistanveTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaderlessDistanveTFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leaderlessDetectionPanelLayout = new javax.swing.GroupLayout(leaderlessDetectionPanel);
        leaderlessDetectionPanel.setLayout(leaderlessDetectionPanelLayout);
        leaderlessDetectionPanelLayout.setHorizontalGroup(
            leaderlessDetectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leaderlessDetectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(leaderlessDistanveTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        leaderlessDetectionPanelLayout.setVerticalGroup(
            leaderlessDetectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leaderlessDetectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leaderlessDetectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(leaderlessDistanveTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel11.text")); // NOI18N

        percentageTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.percentageTF.text")); // NOI18N
        percentageTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                percentageTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout tlsShiftPanelLayout = new javax.swing.GroupLayout(tlsShiftPanel);
        tlsShiftPanel.setLayout(tlsShiftPanelLayout);
        tlsShiftPanelLayout.setHorizontalGroup(
            tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tlsShiftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(percentageTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tlsShiftPanelLayout.setVerticalGroup(
            tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tlsShiftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(percentageTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator1)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(keepInternalLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(excludeTSSDistanceTF)
                            .addComponent(keepingDistanceForInternalTssTF, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(exclusionOfAllInternalTSSCB))
                    .addComponent(leaderlessDetectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tlsShiftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ratioTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(fractionTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(resultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(fractionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(resultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(ratioTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keepInternalLabel)
                    .addComponent(keepingDistanceForInternalTssTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exclusionOfAllInternalTSSCB))
                .addGap(18, 18, 18)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(excludeTSSDistanceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(separator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leaderlessDetectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tlsShiftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void exclusionOfAllInternalTSSCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exclusionOfAllInternalTSSCBActionPerformed
        if (keepInternalLabel.isEnabled() && keepingDistanceForInternalTssTF.isEnabled()) {
            keepingDistanceForInternalTssTF.setEnabled(false);
            keepInternalLabel.setEnabled(false);
        } else {
            keepingDistanceForInternalTssTF.setEnabled(true);
            keepInternalLabel.setEnabled(true);
        }
    }//GEN-LAST:event_exclusionOfAllInternalTSSCBActionPerformed
    
    private void fractionTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fractionTFActionPerformed
    }//GEN-LAST:event_fractionTFActionPerformed
    
    private void fractionTFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fractionTFKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (Double.valueOf(fractionTF.getText()) > 0.0 && Double.valueOf(fractionTF.getText()) < 1.0) {
                double fraction = Double.parseDouble(fractionTF.getText());
                double result = (this.referenceLength / this.nbOfFeatures) * fraction;
                this.resultLabel.setText("" + result);
            } else {
                try {
                    throw new WizardValidationException(null, "Please give a fraction biggen than 0.0.", null);
                } catch (WizardValidationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_fractionTFKeyReleased

    private void percentageTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_percentageTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_percentageTFActionPerformed

    private void leaderlessDistanveTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaderlessDistanveTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_leaderlessDistanveTFActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JTextField excludeTSSDistanceTF;
    private javax.swing.JCheckBox exclusionOfAllInternalTSSCB;
    private javax.swing.JTextField fractionTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel keepInternalLabel;
    private javax.swing.JTextField keepingDistanceForInternalTssTF;
    private javax.swing.JPanel leaderlessDetectionPanel;
    private javax.swing.JTextField leaderlessDistanveTF;
    private javax.swing.JTextField percentageTF;
    private javax.swing.JTextField ratioTF;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JSeparator separator1;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JPanel tlsShiftPanel;
    // End of variables declaration//GEN-END:variables
}
