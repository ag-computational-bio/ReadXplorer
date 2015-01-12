/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.transcriptomeanalyses.motifsearch;


import de.cebitec.readxplorer.transcriptomeanalyses.verifier.IntegerVerifier;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;


public final class RbsAnalysisVisualPanel extends JPanel {

    private final String wizardName;
    private File workingDir;


    /**
     * Creates new form RbsAnalysisVisualPanel
     */
    public RbsAnalysisVisualPanel( String wizardName ) {
        this.wizardName = wizardName;
        initComponents();
        additionalSettings();
        updateFields();
    }


    @Override
    public String getName() {
        return "Parameters for RBS detection using BioProspector";
    }


    private void additionalSettings() {
        this.expectedMotifWidth.setInputVerifier( new IntegerVerifier( this.expectedMotifWidth ) );
        this.noOfTryingTF.setInputVerifier( new IntegerVerifier( this.noOfTryingTF ) );
        this.minSpacerTF.setInputVerifier( new IntegerVerifier( this.minSpacerTF ) );
        this.lengthForAnalysis.setInputVerifier( new IntegerVerifier( this.lengthForAnalysis ) );
    }


    private void updateFields() {
        Preferences pref = NbPreferences.forModule( Object.class );
        this.expectedMotifWidth.setText( pref.get( wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH, "6" ) );
        this.noOfTryingTF.setText( pref.get( wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR, "40" ) );
        this.minSpacerTF.setText( pref.get( wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER, "6" ) );
        this.lengthForAnalysis.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS, "20" ) );

    }


    public void setWorkingDir( File inputFile ) {
        this.workingDir = inputFile;
    }


    public File getWorkingDir() {
        return workingDir;
    }


    public Integer getNoOfTrying() {
        return Integer.valueOf( this.noOfTryingTF.getText() );
    }


    public Integer getLengthForAnalysis() {
        return Integer.valueOf( this.lengthForAnalysis.getText() );
    }


    public Integer getExpectedMotifWidth() {
        return Integer.valueOf( this.expectedMotifWidth.getText() );
    }


    public Integer getMinSpacer() {
        return Integer.valueOf( this.minSpacerTF.getText() );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        expectedMotifWidth = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        noOfTryingTF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        minSpacerTF = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lengthForAnalysis = new javax.swing.JTextField();

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/readxplorer/transcriptomeanalyses/rbsMotifSearchWizardFig.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.jLabel5.text")); // NOI18N

        expectedMotifWidth.setText(org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.expectedMotifWidth.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.jLabel7.text")); // NOI18N

        noOfTryingTF.setText(org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.noOfTryingTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.jLabel4.text")); // NOI18N

        minSpacerTF.setText(org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.minSpacerTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.jLabel3.text")); // NOI18N

        lengthForAnalysis.setText(org.openide.util.NbBundle.getMessage(RbsAnalysisVisualPanel.class, "RbsAnalysisVisualPanel.lengthForAnalysis.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel5))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(expectedMotifWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel4)
                                        .addGap(18, 18, 18)
                                        .addComponent(minSpacerTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(noOfTryingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton1)
                                        .addGap(11, 11, 11)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(lengthForAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(expectedMotifWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(minSpacerTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(noOfTryingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(lengthForAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.minSpacerTF.setText( "6" );
        this.noOfTryingTF.setText( "40" );
        this.expectedMotifWidth.setText( "6" );
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField expectedMotifWidth;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField lengthForAnalysis;
    private javax.swing.JTextField minSpacerTF;
    private javax.swing.JTextField noOfTryingTF;
    // End of variables declaration//GEN-END:variables
}
