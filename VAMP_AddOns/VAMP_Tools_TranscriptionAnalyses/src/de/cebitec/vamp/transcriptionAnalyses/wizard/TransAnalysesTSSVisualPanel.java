package de.cebitec.vamp.transcriptionAnalyses.wizard;

import de.cebitec.vamp.api.objects.JobPanel;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.view.dialogMenus.ChangeListeningWizardPanel;

/**
 * Panel for showing all available options for the transcription start site 
 * detection.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public final class TransAnalysesTSSVisualPanel extends JobPanel {
    
    private static final long serialVersionUID = 1L;
    
    private int minTotalIncrease;
    private int minTotalPercentIncrease;
    private int maxLowCovInitialCount;
    private int minLowCovIncrease;
    private int minTranscriptExtensionCov;
    private boolean detectUnannotatedTranscripts = false;
    private boolean tssAutomatic = false;

    /**
     * Panel for showing all available options for the transcription start site
     * detection.
     */
    public TransAnalysesTSSVisualPanel() {
        this.initComponents();
        this.initAdditionalComponents();
    }

    @Override
    public String getName() {
        return "TSS Analysis Parameters";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        transcriptionStartAutomaticBox = new javax.swing.JCheckBox();
        minTotalIncreaseField = new javax.swing.JTextField();
        minTotalIncreaseLabel = new javax.swing.JLabel();
        minPercentIncreaseField = new javax.swing.JTextField();
        minPercentIncreaseLabel = new javax.swing.JLabel();
        additionalOptionPanel = new javax.swing.JPanel();
        maxInitialCountField = new javax.swing.JTextField();
        maxInitialCountLabel = new javax.swing.JLabel();
        minLowCovCountField = new javax.swing.JTextField();
        minLowCovCountLabel = new javax.swing.JLabel();
        addRestrictionLabel = new javax.swing.JLabel();
        unannotatedTranscriptsBox = new javax.swing.JCheckBox();
        transcriptExtensionField = new javax.swing.JTextField();
        transcriptExtensionLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(transcriptionStartAutomaticBox, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.transcriptionStartAutomaticBox.text")); // NOI18N
        transcriptionStartAutomaticBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transcriptionStartAutomaticBoxActionPerformed(evt);
            }
        });

        minTotalIncreaseField.setText(org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.minTotalIncreaseField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(minTotalIncreaseLabel, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.minTotalIncreaseLabel.text")); // NOI18N

        minPercentIncreaseField.setText(org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.minPercentIncreaseField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(minPercentIncreaseLabel, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.minPercentIncreaseLabel.text")); // NOI18N

        additionalOptionPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        maxInitialCountField.setText(org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.maxInitialCountField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(maxInitialCountLabel, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.maxInitialCountLabel.text")); // NOI18N

        minLowCovCountField.setText(org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.minLowCovCountField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(minLowCovCountLabel, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.minLowCovCountLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addRestrictionLabel, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.addRestrictionLabel.text")); // NOI18N

        javax.swing.GroupLayout additionalOptionPanelLayout = new javax.swing.GroupLayout(additionalOptionPanel);
        additionalOptionPanel.setLayout(additionalOptionPanelLayout);
        additionalOptionPanelLayout.setHorizontalGroup(
            additionalOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(additionalOptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(additionalOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addRestrictionLabel)
                    .addGroup(additionalOptionPanelLayout.createSequentialGroup()
                        .addComponent(maxInitialCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxInitialCountLabel))
                    .addGroup(additionalOptionPanelLayout.createSequentialGroup()
                        .addComponent(minLowCovCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minLowCovCountLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        additionalOptionPanelLayout.setVerticalGroup(
            additionalOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, additionalOptionPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addRestrictionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(additionalOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxInitialCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxInitialCountLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(additionalOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minLowCovCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minLowCovCountLabel))
                .addGap(7, 7, 7))
        );

        unannotatedTranscriptsBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(unannotatedTranscriptsBox, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.unannotatedTranscriptsBox.text")); // NOI18N
        unannotatedTranscriptsBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unannotatedTranscriptsBoxActionPerformed(evt);
            }
        });

        transcriptExtensionField.setText(org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.transcriptExtensionField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(transcriptExtensionLabel, org.openide.util.NbBundle.getMessage(TransAnalysesTSSVisualPanel.class, "TransAnalysesTSSVisualPanel.transcriptExtensionLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(transcriptExtensionField, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(transcriptExtensionLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(minPercentIncreaseField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPercentIncreaseLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(minTotalIncreaseField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minTotalIncreaseLabel))
                    .addComponent(unannotatedTranscriptsBox)
                    .addComponent(transcriptionStartAutomaticBox)
                    .addComponent(additionalOptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(transcriptionStartAutomaticBox)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minTotalIncreaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minTotalIncreaseLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPercentIncreaseLabel)
                    .addComponent(minPercentIncreaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalOptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(unannotatedTranscriptsBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transcriptExtensionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transcriptExtensionLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void transcriptionStartAutomaticBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transcriptionStartAutomaticBoxActionPerformed
        this.tssAutomatic = this.transcriptionStartAutomaticBox.isSelected();
        if (this.tssAutomatic) {
            this.minPercentIncreaseField.setEnabled(false);
            this.minTotalIncreaseField.setEnabled(false);
            this.maxInitialCountField.setEnabled(false);
            this.minLowCovCountField.setEnabled(false);
        } else {
            this.minPercentIncreaseField.setEnabled(true);
            this.minTotalIncreaseField.setEnabled(true);
            this.maxInitialCountField.setEnabled(true);
            this.minLowCovCountField.setEnabled(true);
        }
    }//GEN-LAST:event_transcriptionStartAutomaticBoxActionPerformed

    private void unannotatedTranscriptsBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unannotatedTranscriptsBoxActionPerformed
        this.detectUnannotatedTranscripts = this.unannotatedTranscriptsBox.isSelected();
        if (this.detectUnannotatedTranscripts) {
            this.transcriptExtensionField.setEnabled(true);
        } else {
            this.transcriptExtensionField.setEnabled(false);
        }
    }//GEN-LAST:event_unannotatedTranscriptsBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addRestrictionLabel;
    private javax.swing.JPanel additionalOptionPanel;
    private javax.swing.JTextField maxInitialCountField;
    private javax.swing.JLabel maxInitialCountLabel;
    private javax.swing.JTextField minLowCovCountField;
    private javax.swing.JLabel minLowCovCountLabel;
    private javax.swing.JTextField minPercentIncreaseField;
    private javax.swing.JLabel minPercentIncreaseLabel;
    private javax.swing.JTextField minTotalIncreaseField;
    private javax.swing.JLabel minTotalIncreaseLabel;
    private javax.swing.JTextField transcriptExtensionField;
    private javax.swing.JLabel transcriptExtensionLabel;
    private javax.swing.JCheckBox transcriptionStartAutomaticBox;
    private javax.swing.JCheckBox unannotatedTranscriptsBox;
    // End of variables declaration//GEN-END:variables


    /**
     * Initializes all additional stuff and components of this panel needed at startup.
     */
    private void initAdditionalComponents() {
        
        this.minTotalIncrease = Integer.parseInt(this.minTotalIncreaseField.getText());
        this.minTotalPercentIncrease = Integer.parseInt(this.minPercentIncreaseField.getText());
        this.maxLowCovInitialCount = Integer.parseInt(this.maxInitialCountField.getText());
        this.minLowCovIncrease = Integer.parseInt(this.minLowCovCountField.getText());
        this.minTranscriptExtensionCov = Integer.parseInt(this.transcriptExtensionField.getText());
        
        this.minTotalIncreaseField.getDocument().addDocumentListener(this.createDocumentListener());
        this.minPercentIncreaseField.getDocument().addDocumentListener(this.createDocumentListener());
        this.maxInitialCountField.getDocument().addDocumentListener(this.createDocumentListener());
        this.minLowCovCountField.getDocument().addDocumentListener(this.createDocumentListener());
        this.transcriptExtensionField.getDocument().addDocumentListener(this.createDocumentListener());
    }
    
    /**
     * Checks if all required information to start the transcription start analysis is set.
     */
    @Override
    public boolean isRequiredInfoSet() {
        boolean isValidated = true;
        if (GeneralUtils.isValidPositiveNumberInput(minTotalIncreaseField.getText())) {
            this.minTotalIncrease = Integer.parseInt(minTotalIncreaseField.getText());
        } else {
            isValidated = false;
        }
        if (GeneralUtils.isValidPositiveNumberInput(minPercentIncreaseField.getText())) {
            this.minTotalPercentIncrease = Integer.parseInt(minPercentIncreaseField.getText());
        } else {
            isValidated = false;
        }
        if (GeneralUtils.isValidNumberInput(maxInitialCountField.getText())) {
            this.maxLowCovInitialCount = Integer.parseInt(maxInitialCountField.getText());
        } else {
            isValidated = false;
        }
        if (GeneralUtils.isValidPositiveNumberInput(minLowCovCountField.getText())) {
            this.minLowCovIncrease = Integer.parseInt(minLowCovCountField.getText());
        } else {
            isValidated = false;
        }
        if (GeneralUtils.isValidPositiveNumberInput(transcriptExtensionField.getText())) {
            this.minTranscriptExtensionCov = Integer.parseInt(transcriptExtensionField.getText());
        } else {
            isValidated = false;
        }
        
        firePropertyChange(ChangeListeningWizardPanel.PROP_VALIDATE, null, isValidated);
        return isValidated;
    }

    public int getMinTotalIncrease() {
        return minTotalIncrease;
    }

    public int getMinTotalPercentIncrease() {
        return minTotalPercentIncrease;
    }

    public int getMaxLowCovInitialCount() {
        return maxLowCovInitialCount;
    }

    public int getMinLowCovIncrease() {
        return minLowCovIncrease;
    }

    public int getMinTranscriptExtensionCov() {
        return minTranscriptExtensionCov;
    }  

    /**
     * @return true, if unannotated transcripts should be detected, false otherwise.
     */
    public boolean getDetectUnannotatedTranscripts() {
        return detectUnannotatedTranscripts;
    }

    /**
     * @return true, if the transcription start site detection's autmatic
     * parameter estimation should be used
     */
    public boolean isTssAutomatic() {
        return tssAutomatic;
    }

}
