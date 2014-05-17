/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.transcriptionAnalyses.wizard;

import de.cebitec.readXplorer.api.objects.JobPanel;
import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;

/**
 * Panel showing all different kinds of transcription analyses functions and
 * allowing for selection of these.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public final class TransAnalysesSelectionVisualPanel extends JobPanel {
    
    private static final long serialVersionUID = 1L;

    /**
     * Panel showing all different kinds of transcription analyses functions and
     * allowing for selection of these.
     */
    public TransAnalysesSelectionVisualPanel() {
        initComponents();
    }

    @Override
    public String getName() {
        return "Transcription Analyses Selection";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        transcriptionStartBox = new javax.swing.JCheckBox();
        rpkmValuesBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tssTextArea = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        rpkmTextArea = new javax.swing.JTextArea();
        operonDetectionBox = new javax.swing.JCheckBox();
        jScrollPane5 = new javax.swing.JScrollPane();
        operonTextArea = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(transcriptionStartBox, org.openide.util.NbBundle.getMessage(TransAnalysesSelectionVisualPanel.class, "TransAnalysesSelectionVisualPanel.transcriptionStartBox.text")); // NOI18N
        transcriptionStartBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transcriptionStartBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(rpkmValuesBox, org.openide.util.NbBundle.getMessage(TransAnalysesSelectionVisualPanel.class, "TransAnalysesSelectionVisualPanel.rpkmValuesBox.text")); // NOI18N
        rpkmValuesBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rpkmValuesBoxActionPerformed(evt);
            }
        });

        tssTextArea.setEditable(false);
        tssTextArea.setBackground(new java.awt.Color(240, 240, 240));
        tssTextArea.setColumns(20);
        tssTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        tssTextArea.setLineWrap(true);
        tssTextArea.setRows(3);
        tssTextArea.setText(org.openide.util.NbBundle.getMessage(TransAnalysesSelectionVisualPanel.class, "TransAnalysesSelectionVisualPanel.tssTextArea.text")); // NOI18N
        tssTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(tssTextArea);

        rpkmTextArea.setEditable(false);
        rpkmTextArea.setBackground(new java.awt.Color(240, 240, 240));
        rpkmTextArea.setColumns(20);
        rpkmTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        rpkmTextArea.setLineWrap(true);
        rpkmTextArea.setRows(2);
        rpkmTextArea.setText(org.openide.util.NbBundle.getMessage(TransAnalysesSelectionVisualPanel.class, "TransAnalysesSelectionVisualPanel.rpkmTextArea.text")); // NOI18N
        rpkmTextArea.setWrapStyleWord(true);
        jScrollPane3.setViewportView(rpkmTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(operonDetectionBox, org.openide.util.NbBundle.getMessage(TransAnalysesSelectionVisualPanel.class, "TransAnalysesSelectionVisualPanel.operonDetectionBox.text")); // NOI18N
        operonDetectionBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operonDetectionBoxActionPerformed(evt);
            }
        });

        operonTextArea.setEditable(false);
        operonTextArea.setBackground(new java.awt.Color(240, 240, 240));
        operonTextArea.setColumns(20);
        operonTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        operonTextArea.setLineWrap(true);
        operonTextArea.setRows(2);
        operonTextArea.setText(org.openide.util.NbBundle.getMessage(TransAnalysesSelectionVisualPanel.class, "TransAnalysesSelectionVisualPanel.operonTextArea.text")); // NOI18N
        operonTextArea.setWrapStyleWord(true);
        jScrollPane5.setViewportView(operonTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(transcriptionStartBox)
                            .addComponent(rpkmValuesBox)
                            .addComponent(operonDetectionBox))
                        .addGap(0, 179, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane3)
                            .addComponent(jScrollPane5))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(transcriptionStartBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rpkmValuesBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(operonDetectionBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void transcriptionStartBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transcriptionStartBoxActionPerformed
        firePropertyChange(ChangeListeningWizardPanel.PROP_VALIDATE, null, isRequiredInfoSet());
    }//GEN-LAST:event_transcriptionStartBoxActionPerformed

    private void rpkmValuesBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rpkmValuesBoxActionPerformed
        firePropertyChange(ChangeListeningWizardPanel.PROP_VALIDATE, null, isRequiredInfoSet());
    }//GEN-LAST:event_rpkmValuesBoxActionPerformed

    private void operonDetectionBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_operonDetectionBoxActionPerformed
        firePropertyChange(ChangeListeningWizardPanel.PROP_VALIDATE, null, isRequiredInfoSet());
    }//GEN-LAST:event_operonDetectionBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JCheckBox operonDetectionBox;
    private javax.swing.JTextArea operonTextArea;
    private javax.swing.JTextArea rpkmTextArea;
    private javax.swing.JCheckBox rpkmValuesBox;
    private javax.swing.JCheckBox transcriptionStartBox;
    private javax.swing.JTextArea tssTextArea;
    // End of variables declaration//GEN-END:variables

    /**
     * @return true, if the transcription start analysis is selected, false otherwise
     */
    public boolean isTSSAnalysisSelected() {
        return this.transcriptionStartBox.isSelected();
    }
    
    /**
     * @return true, if the operon analysis is selected, false
     * otherwise
     */
    public boolean isOperonAnalysisSelected() {
        return this.operonDetectionBox.isSelected();
    }
    
    /**
     * @return true, if the rpkm analysis is selected, false
     * otherwise
     */
    public boolean isRPKMAnalysisSelected() {
        return this.rpkmValuesBox.isSelected();
    }

    /**
     * @return true, if the panel contains valid information, false otherwise
     */
    @Override
    public boolean isRequiredInfoSet() {
        return this.transcriptionStartBox.isSelected()
               || this.operonDetectionBox.isSelected()
                || this.rpkmValuesBox.isSelected();
    }
}
