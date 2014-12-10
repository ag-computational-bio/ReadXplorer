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

package de.cebitec.readxplorer.tools.coverageanalysis;


import de.cebitec.readxplorer.api.objects.JobPanel;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.view.dialogmenus.ChangeListeningWizardPanel;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;


/**
 * Visual panel of the coverage analysis wizard.
 *
 * @author Tobias Zimmermann, Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class CoverageAnalysisVisualPanel extends JobPanel {

    private static final long serialVersionUID = 1L;
    private int minCoverageCount;


    /**
     * Visual panel of the coverage analysis wizard.
     */
    public CoverageAnalysisVisualPanel() {
        initComponents();
        initAdditionalComponents();
    }


    @Override
    public String getName() {
        return "Coverage Analysis Parameters";
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup2 = new javax.swing.ButtonGroup();
        minCoverageCountFieldLabel = new javax.swing.JLabel();
        minCoverageCountField = new javax.swing.JTextField();
        sumCoverageButton = new javax.swing.JRadioButton();
        countStrandsSeparatelyButton = new javax.swing.JRadioButton();
        uncoveredCheckBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tssTextArea = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(minCoverageCountFieldLabel, org.openide.util.NbBundle.getMessage(CoverageAnalysisVisualPanel.class, "CoverageAnalysisVisualPanel.minCoverageCountFieldLabel.text")); // NOI18N

        minCoverageCountField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        minCoverageCountField.setText(org.openide.util.NbBundle.getMessage(CoverageAnalysisVisualPanel.class, "CoverageAnalysisVisualPanel.minCoverageCountField.text")); // NOI18N

        buttonGroup2.add(sumCoverageButton);
        org.openide.awt.Mnemonics.setLocalizedText(sumCoverageButton, org.openide.util.NbBundle.getMessage(CoverageAnalysisVisualPanel.class, "CoverageAnalysisVisualPanel.sumCoverageButton.text")); // NOI18N

        buttonGroup2.add(countStrandsSeparatelyButton);
        countStrandsSeparatelyButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(countStrandsSeparatelyButton, org.openide.util.NbBundle.getMessage(CoverageAnalysisVisualPanel.class, "CoverageAnalysisVisualPanel.countStrandsSeparatelyButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(uncoveredCheckBox, org.openide.util.NbBundle.getMessage(CoverageAnalysisVisualPanel.class, "CoverageAnalysisVisualPanel.uncoveredCheckBox.text")); // NOI18N

        tssTextArea.setEditable(false);
        tssTextArea.setBackground(new java.awt.Color(240, 240, 240));
        tssTextArea.setColumns(20);
        tssTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        tssTextArea.setLineWrap(true);
        tssTextArea.setRows(3);
        tssTextArea.setText(org.openide.util.NbBundle.getMessage(CoverageAnalysisVisualPanel.class, "CoverageAnalysisVisualPanel.tssTextArea.text")); // NOI18N
        tssTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(tssTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(countStrandsSeparatelyButton)
                            .addComponent(sumCoverageButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(minCoverageCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minCoverageCountFieldLabel))
                            .addComponent(uncoveredCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minCoverageCountFieldLabel)
                    .addComponent(minCoverageCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sumCoverageButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(countStrandsSeparatelyButton)
                .addGap(18, 18, 18)
                .addComponent(uncoveredCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JRadioButton countStrandsSeparatelyButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField minCoverageCountField;
    private javax.swing.JLabel minCoverageCountFieldLabel;
    private javax.swing.JRadioButton sumCoverageButton;
    private javax.swing.JTextArea tssTextArea;
    private javax.swing.JCheckBox uncoveredCheckBox;
    // End of variables declaration//GEN-END:variables


    /**
     * Initializes all additional stuff and components of this panel needed at
     * startup.
     */
    private void initAdditionalComponents() {
        this.loadLastParameterSelection();
        this.minCoverageCount = Integer.parseInt( this.minCoverageCountField.getText() );
        this.minCoverageCountField.getDocument().addDocumentListener( this.createDocumentListener() );
    }


    @Override
    public boolean isRequiredInfoSet() {
        boolean isValidated = true;
        if( GeneralUtils.isValidPositiveNumberInput( minCoverageCountField.getText() ) ) {
            this.minCoverageCount = Integer.parseInt( minCoverageCountField.getText() );
        }
        else {
            isValidated = false;
        }
        firePropertyChange( ChangeListeningWizardPanel.PROP_VALIDATE, null, isValidated );
        return isValidated;
    }


    /**
     * @return The minimum coverage count at a certain genome position to count
     *         for the coverage of a genome feature.
     */
    public int getMinCoverageCount() {
        return minCoverageCount;
    }


    /**
     * @return <code>true</code> if the coverage of both strands shall be
     *         combined, <code>false</code> if the coverage of each strand shall be
     *         treated separately.
     */
    public boolean isSumCoverageOfBothStrands() {
        return this.sumCoverageButton.isSelected();
    }


    /**
     * @return <code>true</code> if covered intervals shall be detected,
     *         <code>false</code> if uncovered intervals shall be detected
     */
    public boolean isDetectCoveredIntervals() {
        return !this.uncoveredCheckBox.isSelected();
    }


    /**
     * Updates the parameters for this panel with the globally stored settings
     * for this wizard panel. If no settings were stored, the default
     * configuration is chosen.
     */
    private void loadLastParameterSelection() {
        Preferences pref = NbPreferences.forModule( Object.class );
        minCoverageCountField.setText( pref.get( CoverageAnalysisWizardPanel.MIN_COVERAGE_COUNT, "5" ) );
        String sumCov = pref.get( CoverageAnalysisWizardPanel.SUM_COVERAGE, "0" );
        countStrandsSeparatelyButton.setSelected( sumCov.equals( "0" ) );
        sumCoverageButton.setSelected( sumCov.equals( "1" ) );
        uncoveredCheckBox.setSelected( pref.get( CoverageAnalysisWizardPanel.COVERED_INTERVALS, "0" ).equals( "0" ) );
    }


}
