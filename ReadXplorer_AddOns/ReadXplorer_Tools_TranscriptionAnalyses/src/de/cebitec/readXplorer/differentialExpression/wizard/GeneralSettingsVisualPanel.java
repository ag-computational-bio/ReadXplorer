/* 
 * Copyright (C) 2014 Kai Bernd Stadermann
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
package de.cebitec.readXplorer.differentialExpression.wizard;

import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import java.io.File;
import java.util.List;
import javax.swing.JPanel;

/**
 * Panel displaying general options for the differential expression wizard, such
 * as the annotation selection, start and stop offset selection and if the R
 * data should be saved to disk.
 *
 * @author kstaderm
 */
public final class GeneralSettingsVisualPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private boolean saveBoxChecked;

    /**
     * Panel displaying general options for the differential expression wizard,
     * such as the annotation selection, start and stop offset selection and if
     * the R data should be saved to disk.
     */
    public GeneralSettingsVisualPanel() {
        initComponents();
        this.saveBoxChecked = this.saveCheckBox.isSelected();
        fileNameField.setText(System.getProperty("user.home") + File.separator + "DiffExpResult.rdata");
        int[] selectedIndex = {0,1};
        usedAnnotationsList.setSelectedIndices(selectedIndex);
    }

    public void enableSaveOptions(boolean bool) {
        saveCheckBox.setEnabled(bool);
    }

    public boolean verifyInput() {
        return GeneralUtils.isValidNumberInput(startOffset.getText()) && GeneralUtils.isValidNumberInput(stopOffset.getText());
    }

    public Integer getStartOffset() {
        return Integer.parseInt(startOffset.getText());
    }

    public Integer getStopOffset() {
        return Integer.parseInt(stopOffset.getText());
    }

    @Override
    public String getName() {
        return "General Setup";
    }

    public List<FeatureType> getSelectedFeatureTypes() {
        return usedAnnotationsList.getSelectedValuesList();
    }

    public String getSavePath() {
        return fileNameField.getText();
    }

    public boolean isSaveBoxChecked() {
        return saveBoxChecked;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        startOffset = new javax.swing.JTextField();
        stopOffset = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        fileNameField = new javax.swing.JTextField();
        saveCheckBox = new javax.swing.JCheckBox();
        fileChooserButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        usedAnnotationsList = new javax.swing.JList<>(FeatureType.SELECTABLE_FEATURE_TYPES);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.jLabel2.text")); // NOI18N

        startOffset.setText(org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.startOffset.text")); // NOI18N

        stopOffset.setText(org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.stopOffset.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.jLabel3.text")); // NOI18N

        fileNameField.setText(org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.fileNameField.text")); // NOI18N
        fileNameField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(saveCheckBox, org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.saveCheckBox.text")); // NOI18N
        saveCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fileChooserButton, org.openide.util.NbBundle.getMessage(GeneralSettingsVisualPanel.class, "GeneralSettingsVisualPanel.fileChooserButton.text")); // NOI18N
        fileChooserButton.setEnabled(false);
        fileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(usedAnnotationsList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileNameField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileChooserButton))
                    .addComponent(saveCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startOffset, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(stopOffset, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 277, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(saveCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileNameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileChooserButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void saveCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCheckBoxActionPerformed
        saveBoxChecked = this.saveCheckBox.isSelected();
        fileChooserButton.setEnabled(saveBoxChecked);
        fileNameField.setEnabled(saveBoxChecked);
    }//GEN-LAST:event_saveCheckBoxActionPerformed

    private void fileChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserButtonActionPerformed
        ReadXplorerFileChooser fc = new ReadXplorerFileChooser(new String[]{"rdata"}, "rdata") {
            private static final long serialVersionUID = 1L;

            @Override
            public void save(String fileLocation) {
                fileNameField.setText(fileLocation);
            }

            @Override
            public void open(String fileLocation) {
            }
        };
        fc.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
    }//GEN-LAST:event_fileChooserButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JTextField fileNameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox saveCheckBox;
    private javax.swing.JTextField startOffset;
    private javax.swing.JTextField stopOffset;
    private javax.swing.JList<FeatureType> usedAnnotationsList;
    // End of variables declaration//GEN-END:variables
}
