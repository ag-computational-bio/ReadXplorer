/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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
package de.cebitec.readXplorer.featureCoverageAnalysis;

import de.cebitec.readXplorer.api.objects.JobPanel;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public final class CoveredFeaturesVisualPanel extends JobPanel {
    
    private static final long serialVersionUID = 1L;

    private int minCoveragePercent;
    private int minCoverageCount;
    
    /**
     * Creates new form CoveredFeaturesVisualPanel
     */
    public CoveredFeaturesVisualPanel() {
        initComponents();
        this.initAdditionalComponents();
    }

    @Override
    public String getName() {
        return "Feature Coverage Analysis Parameters";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        minCoveragePercentField = new javax.swing.JTextField();
        minCoveragePercentLabel = new javax.swing.JLabel();
        minCoverageCountLabel = new javax.swing.JLabel();
        minCoverageCountField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tssTextArea = new javax.swing.JTextArea();
        detectUncoveredBox = new javax.swing.JCheckBox();

        minCoveragePercentField.setText(org.openide.util.NbBundle.getMessage(CoveredFeaturesVisualPanel.class, "CoveredFeaturesVisualPanel.minCoveragePercentField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(minCoveragePercentLabel, org.openide.util.NbBundle.getMessage(CoveredFeaturesVisualPanel.class, "CoveredFeaturesVisualPanel.minCoveragePercentLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(minCoverageCountLabel, org.openide.util.NbBundle.getMessage(CoveredFeaturesVisualPanel.class, "CoveredFeaturesVisualPanel.minCoverageCountLabel.text")); // NOI18N

        minCoverageCountField.setText(org.openide.util.NbBundle.getMessage(CoveredFeaturesVisualPanel.class, "CoveredFeaturesVisualPanel.minCoverageCountField.text")); // NOI18N

        tssTextArea.setEditable(false);
        tssTextArea.setBackground(new java.awt.Color(240, 240, 240));
        tssTextArea.setColumns(20);
        tssTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        tssTextArea.setLineWrap(true);
        tssTextArea.setRows(3);
        tssTextArea.setText(org.openide.util.NbBundle.getMessage(CoveredFeaturesVisualPanel.class, "CoveredFeaturesVisualPanel.tssTextArea.text")); // NOI18N
        tssTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(tssTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(detectUncoveredBox, org.openide.util.NbBundle.getMessage(CoveredFeaturesVisualPanel.class, "CoveredFeaturesVisualPanel.detectUncoveredBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(detectUncoveredBox)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(minCoveragePercentField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minCoveragePercentLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(minCoverageCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minCoverageCountLabel))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minCoveragePercentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minCoveragePercentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minCoverageCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minCoverageCountLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(detectUncoveredBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox detectUncoveredBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField minCoverageCountField;
    private javax.swing.JLabel minCoverageCountLabel;
    private javax.swing.JTextField minCoveragePercentField;
    private javax.swing.JLabel minCoveragePercentLabel;
    private javax.swing.JTextArea tssTextArea;
    // End of variables declaration//GEN-END:variables

    /**
     * Initializes all additional stuff and components of this panel needed at
     * startup.
     */
    private void initAdditionalComponents() {
        this.loadLastParameterSelection();
        this.minCoveragePercent = Integer.parseInt(this.minCoveragePercentField.getText());
        this.minCoverageCount = Integer.parseInt(this.minCoverageCountField.getText());

        this.minCoveragePercentField.getDocument().addDocumentListener(this.createDocumentListener());
        this.minCoverageCountField.getDocument().addDocumentListener(this.createDocumentListener());
    }

    /**
     * Checks if all required information to start the transcription start
     * analysis is set.
     */
    @Override
    public boolean isRequiredInfoSet() {
        boolean isValidated = true;
        if (GeneralUtils.isValidNumberInput(minCoveragePercentField.getText())) {
            this.minCoveragePercent = Integer.parseInt(minCoveragePercentField.getText());
        } else {
            isValidated = false;
        }
        if (GeneralUtils.isValidPositiveNumberInput(minCoverageCountField.getText())) {
            this.minCoverageCount = Integer.parseInt(minCoverageCountField.getText());
        } else {
            isValidated = false;
        }

        firePropertyChange(ChangeListeningWizardPanel.PROP_VALIDATE, null, isValidated);
        return isValidated;
    }
    
    /**
     * @return <code>true</code> if the covered features should be returned, 
     * <code>false</code> if the uncovered features should be returned
     */
    public boolean isGetCoveredFeatures() {
        return !this.detectUncoveredBox.isSelected();
    }

    /**
     * @return The minimum covered percent of a genome feature to be considered
     *      in this analysis.
     */
    public int getMinCoveredPercent() {
        return minCoveragePercent;
    }

    /**
     * @return The minimum coverage count at a certain genome position to count
     *      for the coverage of a genome feature.
     */
    public int getMinCoverageCount() {
        return minCoverageCount;
    }
    
    /**
     * Updates the parameters for this panel with the globally stored settings
     * for this wizard panel. If no settings were stored, the default
     * configuration is chosen.
     */
    private void loadLastParameterSelection() {
        Preferences pref = NbPreferences.forModule(Object.class);
        minCoverageCountField.setText(pref.get(CoveredFeaturesWizardPanel.PROP_MIN_COVERAGE_COUNT, "10"));
        minCoveragePercentField.setText(pref.get(CoveredFeaturesWizardPanel.PROP_MIN_COVERED_PERCENT, "90"));
        detectUncoveredBox.setSelected(pref.get(CoveredFeaturesWizardPanel.PROP_GET_COVERED_FEATURES, "1").equals("0"));
    }
}
