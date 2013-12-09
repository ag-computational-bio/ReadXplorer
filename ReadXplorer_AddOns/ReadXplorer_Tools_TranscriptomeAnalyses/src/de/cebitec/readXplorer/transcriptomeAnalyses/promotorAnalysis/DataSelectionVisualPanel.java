/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.promotorAnalysis;

import de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis.*;
import javax.swing.JPanel;

public final class DataSelectionVisualPanel extends JPanel {

    /**
     * Creates new form DataSelectionVisualPanel
     */
    public DataSelectionVisualPanel() {
        initComponents();
        this.buttonSelectionGroup.add(onlyLeaderlessElementsCB);
        this.buttonSelectionGroup.add(onlyNonLeaderlessElementsCB);
        this.buttonSelectionGroup.add(onlyPutAntisenseElementsCB);
        this.buttonSelectionGroup.add(onlyRealTssCB);
        this.buttonSelectionGroup.add(onlySelectedCB);

//        this.lengthRelativeToTSS.setEnabled(false);
    }

    @Override
    public String getName() {
        return "Data type selection";
    }

    public boolean isAllElements() {
        return allElementsCB.isSelected();
    }

    public boolean isOnlyLeaderlessElements() {
        return onlyLeaderlessElementsCB.isSelected();
    }

    public boolean isOnlyAntisenseElements() {
        return onlyPutAntisenseElementsCB.isSelected();
    }

    public boolean isOnlyNonLeaderlessElements() {
        return onlyNonLeaderlessElementsCB.isSelected();
    }

    public boolean isOnlyRealTSS() {
        return onlyRealTssCB.isSelected();
    }

    public Integer getLengthRelativeToTss() {
        return Integer.valueOf(this.lengthRelativeToTSS.getText());
    }

    public boolean isOnlySelected() {
        return onlySelectedCB.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonSelectionGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        allElementsCB = new javax.swing.JCheckBox();
        onlyLeaderlessElementsCB = new javax.swing.JCheckBox();
        onlyPutAntisenseElementsCB = new javax.swing.JCheckBox();
        onlyNonLeaderlessElementsCB = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        onlyRealTssCB = new javax.swing.JCheckBox();
        lengthRelativeToTSS = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        onlySelectedCB = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(allElementsCB, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.allElementsCB.text")); // NOI18N
        allElementsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allElementsCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(onlyLeaderlessElementsCB, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.onlyLeaderlessElementsCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(onlyPutAntisenseElementsCB, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.onlyPutAntisenseElementsCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(onlyNonLeaderlessElementsCB, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.onlyNonLeaderlessElementsCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(onlyRealTssCB, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.onlyRealTssCB.text")); // NOI18N

        lengthRelativeToTSS.setText(org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.lengthRelativeToTSS.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(onlySelectedCB, org.openide.util.NbBundle.getMessage(DataSelectionVisualPanel.class, "DataSelectionVisualPanel.onlySelectedCB.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(allElementsCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lengthRelativeToTSS, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(onlySelectedCB)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(onlyRealTssCB)
                                        .addComponent(onlyPutAntisenseElementsCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(onlyLeaderlessElementsCB)
                                        .addComponent(onlyNonLeaderlessElementsCB)
                                        .addComponent(jLabel2)
                                        .addComponent(jSeparator1)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(allElementsCB)
                    .addComponent(lengthRelativeToTSS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlyLeaderlessElementsCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlyPutAntisenseElementsCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlyNonLeaderlessElementsCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlyRealTssCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlySelectedCB)
                .addContainerGap(46, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void allElementsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allElementsCBActionPerformed

        if (allElementsCB.isSelected()) {
            this.onlyLeaderlessElementsCB.setEnabled(false);
            this.onlyNonLeaderlessElementsCB.setEnabled(false);
            this.onlyPutAntisenseElementsCB.setEnabled(false);
            this.onlyRealTssCB.setEnabled(false);
            this.onlySelectedCB.setEnabled(false);
//            this.lengthRelativeToTSS.setEnabled(true);
        } else {
            this.onlyLeaderlessElementsCB.setEnabled(true);
            this.onlyNonLeaderlessElementsCB.setEnabled(true);
            this.onlyPutAntisenseElementsCB.setEnabled(true);
            this.onlyRealTssCB.setEnabled(true);
            this.onlySelectedCB.setEnabled(true);
//            this.lengthRelativeToTSS.setEnabled(false);
        }

    }//GEN-LAST:event_allElementsCBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allElementsCB;
    private javax.swing.ButtonGroup buttonSelectionGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField lengthRelativeToTSS;
    private javax.swing.JCheckBox onlyLeaderlessElementsCB;
    private javax.swing.JCheckBox onlyNonLeaderlessElementsCB;
    private javax.swing.JCheckBox onlyPutAntisenseElementsCB;
    private javax.swing.JCheckBox onlyRealTssCB;
    private javax.swing.JCheckBox onlySelectedCB;
    // End of variables declaration//GEN-END:variables
}
