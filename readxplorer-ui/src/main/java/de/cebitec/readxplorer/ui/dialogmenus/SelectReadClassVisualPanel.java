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

package de.cebitec.readxplorer.ui.dialogmenus;


import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.api.objects.JobPanel;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.classification.Classification;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.ToolTipManager;
import org.openide.util.NbPreferences;


/**
 * A visual wizard job panel. It offers to select read mapping classes and
 * unique or all mapped reads for any further processing.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SelectReadClassVisualPanel extends JobPanel {

    private static final long serialVersionUID = 1L;
    private final String wizardName;
    private byte minMappingQual;


    /**
     * A visual wizard job panel. It offers to select read mapping classes and
     * unique or all mapped reads for any further processing.
     * <p>
     * @param wizardName        the name of the corresponding wizard
     * @param isFeatureAnalysis <code>true</code> means the analysis runs on
     *                          genomic features and should show appropriate
     *                          options. <code>false</code> means the analysis
     *                          generally runs on the strands and should not
     *                          show the strand option components.
     */
    public SelectReadClassVisualPanel( String wizardName, boolean isFeatureAnalysis ) {
        this.wizardName = wizardName;
        this.initComponents();
        this.updateStrandOptionLabels( isFeatureAnalysis );

        this.loadLastParameterSelection();
        this.minMappingQualityField.getDocument().addDocumentListener( this.createDocumentListener() );
    }


    /**
     * Updates the visibility of all strand option associated components
     * depending on the given boolean.
     * <p>
     * @param isFeatureAnalysis <code>true</code> means the analysis runs on
     *                          genomic features and should show appropriate
     *                          options. <code>false</code> means the analysis
     *                          generally runs on the strands and should not
     *                          show the strand option components.
     */
    private void updateStrandOptionLabels( boolean isFeatureAnalysis ) {
        jSeparator2.setVisible( isFeatureAnalysis );
        descriptionStrandScrollPane.setVisible( isFeatureAnalysis );
        decriptionStrandTextArea.setVisible( isFeatureAnalysis );
        strandLabel.setVisible( isFeatureAnalysis );
        strandFeatureRadioButton.setVisible( isFeatureAnalysis );
        strandOppositeRadioButton.setVisible( isFeatureAnalysis );
        strandBothRadioButton.setVisible( isFeatureAnalysis );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        strandButtonGroup = new javax.swing.ButtonGroup();
        descriptionStrandScrollPane = new javax.swing.JScrollPane();
        decriptionStrandTextArea = new javax.swing.JTextArea();
        checkBoxPerfect = new javax.swing.JCheckBox();
        checkBoxBestMatch = new javax.swing.JCheckBox();
        checkBoxCommon = new javax.swing.JCheckBox();
        checkBoxUnique = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        minMappingQualLabel = new javax.swing.JLabel();
        minMappingQualityField = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        strandLabel = new javax.swing.JLabel();
        strandFeatureRadioButton = new javax.swing.JRadioButton();
        strandBothRadioButton = new javax.swing.JRadioButton();
        strandOppositeRadioButton = new javax.swing.JRadioButton();
        descriptionRCScrollPane = new javax.swing.JScrollPane();
        decriptionRCTextArea = new javax.swing.JTextArea();
        checkBoxSinglePerfect = new javax.swing.JCheckBox();
        checkBoxSingleBestMatch = new javax.swing.JCheckBox();

        decriptionStrandTextArea.setEditable(false);
        decriptionStrandTextArea.setBackground(new java.awt.Color(240, 240, 240));
        decriptionStrandTextArea.setColumns(20);
        decriptionStrandTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        decriptionStrandTextArea.setLineWrap(true);
        decriptionStrandTextArea.setRows(2);
        decriptionStrandTextArea.setText(org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.decriptionStrandTextArea.text")); // NOI18N
        decriptionStrandTextArea.setToolTipText(org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.decriptionStrandTextArea.toolTipText")); // NOI18N
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        decriptionStrandTextArea.setWrapStyleWord(true);
        descriptionStrandScrollPane.setViewportView(decriptionStrandTextArea);

        checkBoxPerfect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxPerfect, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.checkBoxPerfect.text")); // NOI18N
        checkBoxPerfect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPerfectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxBestMatch, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.checkBoxBestMatch.text")); // NOI18N
        checkBoxBestMatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBestMatchActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxCommon, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.checkBoxCommon.text")); // NOI18N
        checkBoxCommon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCommonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxUnique, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.checkBoxUnique.text")); // NOI18N
        checkBoxUnique.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxUniqueActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        org.openide.awt.Mnemonics.setLocalizedText(minMappingQualLabel, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.minMappingQualLabel.text")); // NOI18N

        minMappingQualityField.setText(org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.minMappingQualityField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(strandLabel, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.strandLabel.text")); // NOI18N

        strandButtonGroup.add(strandFeatureRadioButton);
        strandFeatureRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(strandFeatureRadioButton, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.strandFeatureRadioButton.text")); // NOI18N

        strandButtonGroup.add(strandBothRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(strandBothRadioButton, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.strandBothRadioButton.text")); // NOI18N

        strandButtonGroup.add(strandOppositeRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(strandOppositeRadioButton, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.strandOppositeRadioButton.text")); // NOI18N

        decriptionRCTextArea.setEditable(false);
        decriptionRCTextArea.setBackground(new java.awt.Color(240, 240, 240));
        decriptionRCTextArea.setColumns(20);
        decriptionRCTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        decriptionRCTextArea.setLineWrap(true);
        decriptionRCTextArea.setRows(5);
        decriptionRCTextArea.setText(org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.decriptionRCTextArea.text")); // NOI18N
        decriptionRCTextArea.setWrapStyleWord(true);
        descriptionRCScrollPane.setViewportView(decriptionRCTextArea);

        checkBoxSinglePerfect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxSinglePerfect, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.checkBoxSinglePerfect.text")); // NOI18N
        checkBoxSinglePerfect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxSinglePerfectActionPerformed(evt);
            }
        });

        checkBoxSingleBestMatch.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxSingleBestMatch, org.openide.util.NbBundle.getMessage(SelectReadClassVisualPanel.class, "SelectReadClassVisualPanel.checkBoxSingleBestMatch.text")); // NOI18N
        checkBoxSingleBestMatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxSingleBestMatchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxPerfect)
                            .addComponent(checkBoxSinglePerfect)
                            .addComponent(checkBoxSingleBestMatch)
                            .addComponent(checkBoxBestMatch)
                            .addComponent(checkBoxCommon))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(minMappingQualityField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minMappingQualLabel))
                            .addComponent(checkBoxUnique))
                        .addGap(0, 160, Short.MAX_VALUE))
                    .addComponent(descriptionRCScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(strandLabel)
                            .addComponent(strandFeatureRadioButton)
                            .addComponent(strandOppositeRadioButton)
                            .addComponent(strandBothRadioButton))
                        .addGap(154, 154, 154))
                    .addComponent(descriptionStrandScrollPane))
                .addGap(4, 4, 4))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionRCScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBoxUnique)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(minMappingQualityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(minMappingQualLabel)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBoxSinglePerfect)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxPerfect)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxSingleBestMatch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxBestMatch)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxCommon))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionStrandScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(strandLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(strandFeatureRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(strandOppositeRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(strandBothRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void checkBoxCommonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCommonActionPerformed
        this.updateUniqueBox();
    }//GEN-LAST:event_checkBoxCommonActionPerformed

    private void checkBoxUniqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxUniqueActionPerformed
        checkBoxPerfect.setEnabled( !checkBoxUnique.isSelected() );
        checkBoxBestMatch.setEnabled( !checkBoxUnique.isSelected() );
        checkBoxCommon.setEnabled( !checkBoxUnique.isSelected() );
        isRequiredInfoSet();
    }//GEN-LAST:event_checkBoxUniqueActionPerformed

    private void checkBoxPerfectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPerfectActionPerformed
        this.updateUniqueBox();
    }//GEN-LAST:event_checkBoxPerfectActionPerformed

    private void checkBoxBestMatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBestMatchActionPerformed
        this.updateUniqueBox();
    }//GEN-LAST:event_checkBoxBestMatchActionPerformed

    private void checkBoxSinglePerfectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxSinglePerfectActionPerformed
        this.isRequiredInfoSet();
    }//GEN-LAST:event_checkBoxSinglePerfectActionPerformed

    private void checkBoxSingleBestMatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxSingleBestMatchActionPerformed
        this.isRequiredInfoSet();
    }//GEN-LAST:event_checkBoxSingleBestMatchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkBoxBestMatch;
    private javax.swing.JCheckBox checkBoxCommon;
    private javax.swing.JCheckBox checkBoxPerfect;
    private javax.swing.JCheckBox checkBoxSingleBestMatch;
    private javax.swing.JCheckBox checkBoxSinglePerfect;
    private javax.swing.JCheckBox checkBoxUnique;
    private javax.swing.JTextArea decriptionRCTextArea;
    private javax.swing.JTextArea decriptionStrandTextArea;
    private javax.swing.JScrollPane descriptionRCScrollPane;
    private javax.swing.JScrollPane descriptionStrandScrollPane;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel minMappingQualLabel;
    private javax.swing.JTextField minMappingQualityField;
    private javax.swing.JRadioButton strandBothRadioButton;
    private javax.swing.ButtonGroup strandButtonGroup;
    private javax.swing.JRadioButton strandFeatureRadioButton;
    private javax.swing.JLabel strandLabel;
    private javax.swing.JRadioButton strandOppositeRadioButton;
    // End of variables declaration//GEN-END:variables


    private void updateUniqueBox() {
        this.checkBoxUnique.setEnabled( !checkBoxPerfect.isSelected() && !checkBoxBestMatch.isSelected() && !checkBoxCommon.isSelected() );
        this.isRequiredInfoSet();
    }


    /**
     * @return <code>true</code>, if at least one read class is selected,
     *         <code>false</code> otherwise
     */
    @Override
    public boolean isRequiredInfoSet() {
        boolean isValidated
                = this.checkBoxPerfect.isSelected() ||
                 this.checkBoxBestMatch.isSelected() ||
                 this.checkBoxCommon.isSelected() ||
                 this.checkBoxSinglePerfect.isSelected() ||
                 this.checkBoxSingleBestMatch.isSelected();

        if( GeneralUtils.isValidByteInput( minMappingQualityField.getText() ) ) {
            this.minMappingQual = Byte.parseByte( minMappingQualityField.getText() );
        } else {
            isValidated = false;
        }
        firePropertyChange( ChangeListeningWizardPanel.PROP_VALIDATE, null, isValidated );
        return isValidated;
    }


    /**
     * @return The read class parameter object including also the minimum phred
     *         scaled mapping quality and the selected strand option.
     */
    public ParametersReadClasses getReadClassParams() {
        List<Classification> excludedClasses = new ArrayList<>();
        if( !checkBoxPerfect.isSelected() ) {
            excludedClasses.add( MappingClass.PERFECT_MATCH );
        }
        if( !checkBoxBestMatch.isSelected() ) {
            excludedClasses.add( MappingClass.BEST_MATCH );
        }
        if( !checkBoxCommon.isSelected() ) {
            excludedClasses.add( MappingClass.COMMON_MATCH );
        }
        if( checkBoxUnique.isSelected() ) {
            excludedClasses.add( FeatureType.MULTIPLE_MAPPED_READ );
        }
        if( !checkBoxSinglePerfect.isSelected() ) {
            excludedClasses.add( MappingClass.SINGLE_PERFECT_MATCH );
        }
        if( !checkBoxSingleBestMatch.isSelected() ) {
            excludedClasses.add( MappingClass.SINGLE_BEST_MATCH );
        }
        return new ParametersReadClasses( excludedClasses, minMappingQual, getSelectedStrandOption() );
    }


    /**
     * @return Converts the selected strand option radio button into the
     *         corresponding byte value from {@link Strand}.
     */
    private Strand getSelectedStrandOption() {
        Strand strandOption = Strand.Feature;
        if( this.strandOppositeRadioButton.isSelected() ) {
            strandOption = Strand.Opposite;
        } else if( this.strandBothRadioButton.isSelected() ) {
            strandOption = Strand.Both;
        }
        return strandOption;
    }


    @Override
    public String getName() {
        return "Read Classification Selection";
    }


    /**
     * Updates the checkboxes for the read classes with the globally stored
     * settings for this wizard. If no settings were stored, the default
     * configuration is chosen.
     */
    private void loadLastParameterSelection() {
        Preferences pref = NbPreferences.forModule( Object.class );
        boolean isPerfectSelected = pref.getBoolean( wizardName + SelectReadClassWizardPanel.PROP_PERFECT_SELECTED, false );
        boolean isBestMatchSelected = pref.getBoolean( wizardName + SelectReadClassWizardPanel.PROP_BEST_MATCH_SELECTED, false );
        boolean isCommonMatchSelected = pref.getBoolean( wizardName + SelectReadClassWizardPanel.PROP_COMMON_MATCH_SELECTED, false );
        boolean isSinglePerfectSelected = pref.getBoolean( wizardName + SelectReadClassWizardPanel.PROP_SINGLE_PERFECT_SELECTED, true );
        boolean isSingleBestMatchSelected = pref.getBoolean( wizardName + SelectReadClassWizardPanel.PROP_SINGLE_BEST_MATCH_SELECTED, true );
        boolean isUniqueSelected = pref.getBoolean( wizardName + SelectReadClassWizardPanel.PROP_UNIQUE_SELECTED, false );
        Strand strandOption = Strand.fromString( pref.get( wizardName + SelectReadClassWizardPanel.PROP_STRAND_OPTION, Strand.Feature.toString() ) );

        this.checkBoxPerfect.setSelected( isPerfectSelected );
        this.checkBoxBestMatch.setSelected( isBestMatchSelected );
        this.checkBoxCommon.setSelected( isCommonMatchSelected );
        this.checkBoxSinglePerfect.setSelected( isSinglePerfectSelected );
        this.checkBoxSingleBestMatch.setSelected( isSingleBestMatchSelected );
        this.checkBoxUnique.setSelected( isUniqueSelected );
        this.minMappingQualityField.setText( pref.get( wizardName + SelectReadClassWizardPanel.PROP_MIN_MAPPING_QUAL, "0" ) );
        this.strandFeatureRadioButton.setSelected( strandOption == Strand.Feature );
        this.strandOppositeRadioButton.setSelected( strandOption == Strand.Opposite );
        this.strandBothRadioButton.setSelected( strandOption == Strand.Both );
    }


}
