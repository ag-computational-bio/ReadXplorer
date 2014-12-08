/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.IntegerVerifier;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;


public final class FivePrimeUTRPromotorSettingsVisualPanel extends JPanel {

    private final String wizardName;


    /**
     * Creates new form FivePrimeUTRPromotorSettingsVisualPanel
     */
    public FivePrimeUTRPromotorSettingsVisualPanel( String wizardName ) {
        initComponents();
        setIntegerVerifierOnTextFields();
        this.wizardName = wizardName;
        updateFields();
    }


    /**
     * Set IntegerVerifier to all JTextFields.
     */
    private void setIntegerVerifierOnTextFields() {
        this.putativeMinus35RegionTF.setInputVerifier( new IntegerVerifier( this.putativeMinus35RegionTF ) );
        this.putativeMinus10RegionTF.setInputVerifier( new IntegerVerifier( this.putativeMinus10RegionTF ) );
        this.alternativeSpacerTF.setInputVerifier( new IntegerVerifier( this.alternativeSpacerTF ) );
        this.minSpacer1TF.setInputVerifier( new IntegerVerifier( this.minSpacer1TF ) );
        this.minSpacer2TF.setInputVerifier( new IntegerVerifier( this.minSpacer2TF ) );
        this.minus10MotifWidthTF.setInputVerifier( new IntegerVerifier( this.minus10MotifWidthTF ) );
        this.minus35MotifWidthTF.setInputVerifier( new IntegerVerifier( this.minus35MotifWidthTF ) );
        this.noTimesTryingTF.setInputVerifier( new IntegerVerifier( this.noTimesTryingTF ) );
    }


    /**
     * Updates the checkboxes for the read classes with the globally stored
     * settings for this wizard. If no settings were stored, the default
     * configuration is chosen.
     */
    private void updateFields() {
        Preferences pref = NbPreferences.forModule( Object.class );
        this.minSpacer1TF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER1_LENGTH, "6" ) );
        this.minSpacer2TF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER2_LENGTH, "17" ) );
        this.putativeMinus10RegionTF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_10_REGION, "9" ) );
        this.putativeMinus35RegionTF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_35_REGION, "9" ) );
        minus10MotifWidthTF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS10_MOTIF_LENGTH, "6" ) );
        minus35MotifWidthTF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS35_MOTIF_LENGTH, "6" ) );
        noTimesTryingTF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_NUMBER_OF_TRYING, "40" ) );
        alternativeSpacerTF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ALTERNATIVE_SPACER, "29" ) );
    }


    @Override
    public String getName() {
        return "Parameters for Promotor detection using BioProspector";
    }


    public Integer getAlternativeSpacer() {
        return Integer.valueOf( this.alternativeSpacerTF.getText() );
    }


    public Integer getSpacer1Length() {
        return Integer.valueOf( this.minSpacer1TF.getText() );
    }


    public Integer getSpacer2Length() {
        return Integer.valueOf( this.minSpacer2TF.getText() );
    }


    public Integer getPutativeMinusTenLength() {
        return Integer.valueOf( this.putativeMinus10RegionTF.getText() );
    }


    public Integer getPutativeMinusThirtyFiveLength() {
        return Integer.valueOf( this.putativeMinus35RegionTF.getText() );
    }


    public Integer getMinus10MotifWidth() {
        return Integer.valueOf( this.minus10MotifWidthTF.getText() );
    }


    public Integer getMinus35MotifWidth() {
        return Integer.valueOf( this.minus35MotifWidthTF.getText() );
    }


    /**
     *
     * @return
     */
    public Integer getNoTrying() {
        return Integer.valueOf( this.noTimesTryingTF.getText() );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        minSpacer1TF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        putativeMinus10RegionTF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        minSpacer2TF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        putativeMinus35RegionTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        minus10MotifWidthTF = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        minus35MotifWidthTF = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        noTimesTryingTF = new javax.swing.JTextField();
        setToDefaultValuesButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        alternativeSpacerTF = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/readXplorer/transcriptomeAnalyses/resources/promotorSearchWizardFig.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel1.text")); // NOI18N

        minSpacer1TF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.minSpacer1TF.text")); // NOI18N
        minSpacer1TF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minSpacer1TFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel2.text")); // NOI18N

        putativeMinus10RegionTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.putativeMinus10RegionTF.text")); // NOI18N
        putativeMinus10RegionTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                putativeMinus10RegionTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel3.text")); // NOI18N

        minSpacer2TF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.minSpacer2TF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel4.text")); // NOI18N

        putativeMinus35RegionTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.putativeMinus35RegionTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel5.text")); // NOI18N

        minus10MotifWidthTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.minus10MotifWidthTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel7.text")); // NOI18N

        minus35MotifWidthTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.minus35MotifWidthTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel9.text")); // NOI18N

        noTimesTryingTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.noTimesTryingTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(setToDefaultValuesButton, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.setToDefaultValuesButton.text")); // NOI18N
        setToDefaultValuesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setToDefaultValuesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.jLabel10.text")); // NOI18N

        alternativeSpacerTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeUTRPromotorSettingsVisualPanel.class, "FivePrimeUTRPromotorSettingsVisualPanel.alternativeSpacerTF.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(noTimesTryingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(setToDefaultValuesButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(minus35MotifWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(minSpacer2TF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(18, 18, 18)
                                        .addComponent(putativeMinus35RegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(67, 67, 67)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(alternativeSpacerTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(minus10MotifWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(minSpacer1TF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(18, 18, 18)
                                        .addComponent(putativeMinus10RegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(minSpacer2TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(minSpacer1TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(putativeMinus35RegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(putativeMinus10RegionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(minus35MotifWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(minus10MotifWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(alternativeSpacerTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(noTimesTryingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setToDefaultValuesButton))
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void putativeMinus10RegionTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_putativeMinus10RegionTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_putativeMinus10RegionTFActionPerformed

    private void minSpacer1TFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minSpacer1TFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_minSpacer1TFActionPerformed


    /**
     * Set all text fields to default values.
     *
     * @param evt
     */
    private void setToDefaultValuesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setToDefaultValuesButtonActionPerformed
        this.minSpacer1TF.setText( "5" );
        this.minSpacer2TF.setText( "16" );
        this.minus10MotifWidthTF.setText( "6" );
        this.minus35MotifWidthTF.setText( "6" );
        this.noTimesTryingTF.setText( "40" );
        this.putativeMinus10RegionTF.setText( "9" );
        this.putativeMinus35RegionTF.setText( "9" );
        this.alternativeSpacerTF.setText( "29" );
    }//GEN-LAST:event_setToDefaultValuesButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alternativeSpacerTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField minSpacer1TF;
    private javax.swing.JTextField minSpacer2TF;
    private javax.swing.JTextField minus10MotifWidthTF;
    private javax.swing.JTextField minus35MotifWidthTF;
    private javax.swing.JTextField noTimesTryingTF;
    private javax.swing.JTextField putativeMinus10RegionTF;
    private javax.swing.JTextField putativeMinus35RegionTF;
    private javax.swing.JButton setToDefaultValuesButton;
    // End of variables declaration//GEN-END:variables
}
