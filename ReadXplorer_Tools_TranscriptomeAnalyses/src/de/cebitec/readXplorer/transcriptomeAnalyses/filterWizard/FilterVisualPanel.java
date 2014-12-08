/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readXplorer.transcriptomeAnalyses.filterWizard;


import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.IntegerVerifier;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;


public final class FilterVisualPanel extends JPanel {

    private final String wizardName;


    /**
     * Creates new form FilterVisualPanel1
     */
    public FilterVisualPanel( String wizardName ) {
        initComponents();
        this.wizardName = wizardName;
        updateFields();
        this.atLeastReadStartsTF.setInputVerifier( new IntegerVerifier( this.atLeastReadStartsTF ) );
    }


    @Override
    public String getName() {
        return "Choose Filter Options For Sub Tables";
    }


    public boolean isSingleSelected() {
        return this.singleCB.isSelected();
    }


    public boolean isMultipleSelected() {
        return this.multipleCB.isSelected();
    }


    public boolean isExtractionOfTSSWithAtLeastRSSelected() {
        return this.atLeastReadStartsCB.isSelected();
    }


    public boolean isOnlyIntergenic() {
        return this.intergenicCB.isSelected();
    }


    public boolean isOnlyIntragenic() {
        return this.intragenicCB.isSelected();
    }


    public boolean isOnlyLeaderless() {
        return this.leaderlessCB.isSelected();
    }


    public boolean isOnlyPutativeAntisense() {
        return this.putativeAntisenseCB.isSelected();
    }


    public boolean isOnlyFinished() {
        return this.onlyFinishedCB.isSelected();
    }


    public boolean isOnlyUpstreamRegions() {
        return this.onlyTagedUpstreamAnalysisCB.isSelected();
    }


    public boolean isFalsePositive() {
        return this.falsePositiveCB.isSelected();
    }


    public Integer getAtLeastReadStarts() {
        return Integer.valueOf( this.atLeastReadStartsTF.getText() );
    }


    public boolean isStableRnaSelected() {
        return this.stableRnaCB.isSelected();
    }


    public boolean isOnlyNonStableRnaSelected() {
        return this.onlyNonStableRNACB.isSelected();
    }


    public boolean isIntragenicAntisenseSelected() {
        return this.intragenicAntisenseCB.isSelected();
    }


    public boolean isFivePrimeUtrAntisenseSelected() {
        return this.fivePrimeUtrAntisenseCB.isSelected();
    }


    public boolean isThreePrimeAntisenseSelected() {
        return this.threePrimeUtrAntisense.isSelected();
    }


    private void updateFields() {
        Preferences pref = NbPreferences.forModule( Object.class );
        this.atLeastReadStartsTF.setText( pref.get( wizardName + FilterWizardPanel.PROP_FILTER_READSTARTS, "10" ) );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        singleCB = new javax.swing.JCheckBox();
        multipleCB = new javax.swing.JCheckBox();
        atLeastReadStartsCB = new javax.swing.JCheckBox();
        atLeastReadStartsTF = new javax.swing.JTextField();
        leaderlessCB = new javax.swing.JCheckBox();
        intergenicCB = new javax.swing.JCheckBox();
        putativeAntisenseCB = new javax.swing.JCheckBox();
        onlyFinishedCB = new javax.swing.JCheckBox();
        onlyTagedUpstreamAnalysisCB = new javax.swing.JCheckBox();
        intragenicCB = new javax.swing.JCheckBox();
        falsePositiveCB = new javax.swing.JCheckBox();
        fivePrimeUtrAntisenseCB = new javax.swing.JCheckBox();
        threePrimeUtrAntisense = new javax.swing.JCheckBox();
        intragenicAntisenseCB = new javax.swing.JCheckBox();
        stableRnaCB = new javax.swing.JCheckBox();
        onlyNonStableRNACB = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(singleCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.singleCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(multipleCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.multipleCB.text")); // NOI18N
        multipleCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multipleCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(atLeastReadStartsCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.atLeastReadStartsCB.text")); // NOI18N
        atLeastReadStartsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                atLeastReadStartsCBActionPerformed(evt);
            }
        });

        atLeastReadStartsTF.setText(org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.atLeastReadStartsTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(leaderlessCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.leaderlessCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(intergenicCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.intergenicCB.text")); // NOI18N
        intergenicCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intergenicCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(putativeAntisenseCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.putativeAntisenseCB.text")); // NOI18N
        putativeAntisenseCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                putativeAntisenseCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(onlyFinishedCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.onlyFinishedCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(onlyTagedUpstreamAnalysisCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.onlyTagedUpstreamAnalysisCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(intragenicCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.intragenicCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(falsePositiveCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.falsePositiveCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fivePrimeUtrAntisenseCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.fivePrimeUtrAntisenseCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(threePrimeUtrAntisense, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.threePrimeUtrAntisense.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(intragenicAntisenseCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.intragenicAntisenseCB.text")); // NOI18N
        intragenicAntisenseCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intragenicAntisenseCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(stableRnaCB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.stableRnaCB.text")); // NOI18N
        stableRnaCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stableRnaCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(onlyNonStableRNACB, org.openide.util.NbBundle.getMessage(FilterVisualPanel.class, "FilterVisualPanel.onlyNonStableRNACB.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(singleCB)
                            .addComponent(multipleCB)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(atLeastReadStartsCB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(atLeastReadStartsTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(leaderlessCB)
                            .addComponent(intergenicCB)
                            .addComponent(putativeAntisenseCB)
                            .addComponent(onlyFinishedCB)
                            .addComponent(onlyTagedUpstreamAnalysisCB)
                            .addComponent(intragenicCB)
                            .addComponent(falsePositiveCB))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stableRnaCB)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(threePrimeUtrAntisense)
                                    .addComponent(fivePrimeUtrAntisenseCB)
                                    .addComponent(intragenicAntisenseCB)))
                            .addComponent(onlyNonStableRNACB))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(singleCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(multipleCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(leaderlessCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(intragenicCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(intergenicCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(putativeAntisenseCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(fivePrimeUtrAntisenseCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(threePrimeUtrAntisense)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(intragenicAntisenseCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlyFinishedCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlyTagedUpstreamAnalysisCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(falsePositiveCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stableRnaCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlyNonStableRNACB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(atLeastReadStartsTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(atLeastReadStartsCB)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void atLeastReadStartsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atLeastReadStartsCBActionPerformed
        if( atLeastReadStartsCB.isSelected() ) {
            this.atLeastReadStartsTF.setEditable( true );
        }
        else {
            this.atLeastReadStartsTF.setEnabled( false );
        }
    }//GEN-LAST:event_atLeastReadStartsCBActionPerformed

    private void putativeAntisenseCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_putativeAntisenseCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_putativeAntisenseCBActionPerformed

    private void intragenicAntisenseCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intragenicAntisenseCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_intragenicAntisenseCBActionPerformed

    private void multipleCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multipleCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_multipleCBActionPerformed

    private void intergenicCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intergenicCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_intergenicCBActionPerformed

    private void stableRnaCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stableRnaCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stableRnaCBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox atLeastReadStartsCB;
    private javax.swing.JTextField atLeastReadStartsTF;
    private javax.swing.JCheckBox falsePositiveCB;
    private javax.swing.JCheckBox fivePrimeUtrAntisenseCB;
    private javax.swing.JCheckBox intergenicCB;
    private javax.swing.JCheckBox intragenicAntisenseCB;
    private javax.swing.JCheckBox intragenicCB;
    private javax.swing.JCheckBox leaderlessCB;
    private javax.swing.JCheckBox multipleCB;
    private javax.swing.JCheckBox onlyFinishedCB;
    private javax.swing.JCheckBox onlyNonStableRNACB;
    private javax.swing.JCheckBox onlyTagedUpstreamAnalysisCB;
    private javax.swing.JCheckBox putativeAntisenseCB;
    private javax.swing.JCheckBox singleCB;
    private javax.swing.JCheckBox stableRnaCB;
    private javax.swing.JCheckBox threePrimeUtrAntisense;
    // End of variables declaration//GEN-END:variables
}
