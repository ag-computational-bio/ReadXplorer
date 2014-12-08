/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;


/**
 *
 * @author jritter
 */
public class SequenceLengthSelectionForMotifAnalysis extends javax.swing.JPanel {

    private String wizardName;


    /**
     * Creates new form SequenceLengthSelectionForMotifAnalysis
     */
    public SequenceLengthSelectionForMotifAnalysis( String wizardName ) {
        this.wizardName = wizardName;
        initComponents();
        updateFields();
    }


    public Integer getLength() {
        return Integer.parseInt( this.lengthTF.getText() );
    }


    private void updateFields() {
        Preferences pref = NbPreferences.forModule( Object.class );
        this.lengthTF.setText( pref.get( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS, "20" ) );

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lengthTF = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SequenceLengthSelectionForMotifAnalysis.class, "SequenceLengthSelectionForMotifAnalysis.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SequenceLengthSelectionForMotifAnalysis.class, "SequenceLengthSelectionForMotifAnalysis.jLabel1.text")); // NOI18N

        lengthTF.setText(org.openide.util.NbBundle.getMessage(SequenceLengthSelectionForMotifAnalysis.class, "SequenceLengthSelectionForMotifAnalysis.lengthTF.text")); // NOI18N
        lengthTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lengthTFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(lengthTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lengthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lengthTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lengthTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lengthTFActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField lengthTF;
    // End of variables declaration//GEN-END:variables
}
