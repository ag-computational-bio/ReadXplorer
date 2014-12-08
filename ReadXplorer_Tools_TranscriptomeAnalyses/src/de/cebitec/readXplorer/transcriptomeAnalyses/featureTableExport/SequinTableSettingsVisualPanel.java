/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport;


import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.IntegerVerifier;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;

import static de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableSettingsWizardPanel.SEQUIN_EXPORT_FEATURE_NAME;


public final class SequinTableSettingsVisualPanel extends JPanel {

    private final String wizardName;
    private final String descriptionText = "<html><p align='justify'>The parsing "
                                           + "of the loci_tags is needed, if you want to assign the qualifiers "
                                           + "to each locus_tag in sequin table. </p></html>";
    private String separator = "";


    /**
     * Creates new form SequinTableSettingsVisualPanel
     */
    public SequinTableSettingsVisualPanel( String wizardName ) {
        this.wizardName = wizardName;
        initComponents();
        strainTF.setInputVerifier( new IntegerVerifier( this.strainTF ) );
        this.separatorTF.setEnabled( false );
        this.descriptionLabel.setText( descriptionText );
        this.descriptionLabel.setBorder( BorderFactory.createTitledBorder( "Description -parsing-" ) );
        this.separatorCB.setEnabled( false );
        this.strainLabel.setEnabled( false );
        this.seperatorLabel.setEnabled( false );
        this.strainTF.setEnabled( false );
        updateFields();
    }


    @Override
    public String getName() {
        return "Sequin Table Export";
    }


    /**
     * Getter for the taped feature.
     *
     * @return featurename
     */
    public String getFeatureName() {
        return this.featureTF.getText();
    }


    private void updateFields() {
        Preferences pref = NbPreferences.forModule( Object.class );
        this.featureTF.setText( pref.get( wizardName + SEQUIN_EXPORT_FEATURE_NAME, "" ) );
        this.strainTF.setInputVerifier( new IntegerVerifier( this.strainTF ) );
    }


    public Integer getStrainLength() {
        return Integer.valueOf( this.strainTF.getText() );
    }


    public boolean isLocusTagParsingSelected() {
        return this.parseLocusTagsCB.isSelected();
    }


    public String getSeparator() {
        return this.separatorTF.getText();
    }


    public boolean isSeparatorChoosen() {
        return this.separatorCB.isSelected();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        featureTF = new javax.swing.JTextField();
        dataSelectionContainer = new javax.swing.JPanel();
        strainLabel = new javax.swing.JLabel();
        strainTF = new javax.swing.JTextField();
        seperatorLabel = new javax.swing.JLabel();
        separatorCB = new javax.swing.JCheckBox();
        separatorTF = new javax.swing.JTextField();
        parseLocusTagsCB = new javax.swing.JCheckBox();
        descriptionLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.jLabel1.text")); // NOI18N

        featureTF.setText(org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.featureTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(strainLabel, org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.strainLabel.text")); // NOI18N

        strainTF.setText(org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.strainTF.text")); // NOI18N
        strainTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strainTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(seperatorLabel, org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.seperatorLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(separatorCB, org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.separatorCB.text")); // NOI18N
        separatorCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separatorCBActionPerformed(evt);
            }
        });

        separatorTF.setText(org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.separatorTF.text")); // NOI18N
        separatorTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separatorTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(parseLocusTagsCB, org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.parseLocusTagsCB.text")); // NOI18N
        parseLocusTagsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parseLocusTagsCBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dataSelectionContainerLayout = new javax.swing.GroupLayout(dataSelectionContainer);
        dataSelectionContainer.setLayout(dataSelectionContainerLayout);
        dataSelectionContainerLayout.setHorizontalGroup(
            dataSelectionContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataSelectionContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataSelectionContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parseLocusTagsCB)
                    .addGroup(dataSelectionContainerLayout.createSequentialGroup()
                        .addGroup(dataSelectionContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(strainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(strainTF))
                        .addGap(18, 18, 18)
                        .addGroup(dataSelectionContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(seperatorLabel)
                            .addGroup(dataSelectionContainerLayout.createSequentialGroup()
                                .addComponent(separatorCB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separatorTF)))))
                .addContainerGap(79, Short.MAX_VALUE))
        );
        dataSelectionContainerLayout.setVerticalGroup(
            dataSelectionContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataSelectionContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parseLocusTagsCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dataSelectionContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(seperatorLabel)
                    .addComponent(strainLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dataSelectionContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(strainTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorCB)
                    .addComponent(separatorTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(SequinTableSettingsVisualPanel.class, "SequinTableSettingsVisualPanel.descriptionLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(featureTF, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addComponent(dataSelectionContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(featureTF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dataSelectionContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void strainTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strainTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_strainTFActionPerformed

    private void separatorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separatorCBActionPerformed
        if( separatorCB.isSelected() ) {
            separatorTF.setEnabled( true );
            strainTF.setEnabled( false );
        }
        else {
            separatorTF.setEditable( false );
            strainTF.setEnabled( true );
        }
    }//GEN-LAST:event_separatorCBActionPerformed

    private void separatorTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separatorTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_separatorTFActionPerformed

    private void parseLocusTagsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parseLocusTagsCBActionPerformed
        if( parseLocusTagsCB.isSelected() ) {
            this.separatorCB.setEnabled( true );
            this.strainLabel.setEnabled( true );
            this.seperatorLabel.setEnabled( true );
            this.strainTF.setEnabled( true );
        }
        else {
            this.separatorCB.setEnabled( false );
            this.strainLabel.setEnabled( false );
            this.seperatorLabel.setEnabled( false );
            this.strainTF.setEnabled( false );
        }
    }//GEN-LAST:event_parseLocusTagsCBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dataSelectionContainer;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField featureTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox parseLocusTagsCB;
    private javax.swing.JCheckBox separatorCB;
    private javax.swing.JTextField separatorTF;
    private javax.swing.JLabel seperatorLabel;
    private javax.swing.JLabel strainLabel;
    private javax.swing.JTextField strainTF;
    // End of variables declaration//GEN-END:variables
}
