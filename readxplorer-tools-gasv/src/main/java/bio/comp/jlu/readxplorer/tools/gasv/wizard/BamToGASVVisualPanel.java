/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.tools.gasv.wizard;

import bio.comp.jlu.readxplorer.tools.gasv.ParametersBamToGASV;
import de.cebitec.readxplorer.api.objects.JobPanel;
import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import de.cebitec.readxplorer.utils.FileUtils;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.filechooser.ReadXplorerFileChooser;
import javax.swing.JTextField;


/**
 * Creates a new panel to configure the BamToGASV options.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class BamToGASVVisualPanel extends JobPanel {

    private static final long serialVersionUID = 1L;

    private ParametersBamToGASV params;


    /**
     * Creates a new panel to configure the BamToGASV options.
     * <p>
     * @param bamtoGASVParams Initial BamToGASV parameter set.
     */
    public BamToGASVVisualPanel( ParametersBamToGASV bamtoGASVParams ) {
        params = bamtoGASVParams;
        initComponents();

        minMappingQualityField.getDocument().addDocumentListener( createDocumentListener() );
        maxPairLengthField.getDocument().addDocumentListener( createDocumentListener() );
        distPCTField.getDocument().addDocumentListener( createDocumentListener() );
        distSDField.getDocument().addDocumentListener( createDocumentListener() );
        distExactField.getDocument().addDocumentListener( createDocumentListener() );
        distFileField.getDocument().addDocumentListener( createDocumentListener() );
    }


    /**
     * @return The current parameter selection of the panel.
     */
    public ParametersBamToGASV getBamToGASVParams() {
        return params;
    }


    /**
     * The parameter selection to use for the initial state of the panel.
     * <p>
     * @param params The last parameter selection.
     */
    public void setLastParameterSelection( ParametersBamToGASV params ) {
        updateComponentsForParams( params );
        this.params = params;
    }


    /**
     * Updates all components according to the currently set parameter values.
     * <p>
     * @param bamToGASVParams BamToGASV parameter set to apply.
     */
    private void updateComponentsForParams( ParametersBamToGASV bamToGASVParams ) {
        librarySeparatedBox.setSelected( bamToGASVParams.isLibrarySeparated() );
        platformBox.setSelected( bamToGASVParams.getPlatform() );
        writeConcPairsBox.setSelected( bamToGASVParams.isWriteConcordantPairs() );
        writeLowQualBox.setSelected( bamToGASVParams.isWriteLowQualityPairs() );
        minMappingQualityField.setText( String.valueOf( bamToGASVParams.getMinMappingQuality() ) );
        maxPairLengthField.setText( String.valueOf( bamToGASVParams.getMaxPairLength() ) );
        setIntegerWhenValid( distPCTField, bamToGASVParams.getDistPCTValue() );
        setIntegerWhenValid( distSDField, bamToGASVParams.getDistSDValue() );
        distExactField.setText( String.valueOf( bamToGASVParams.getDistExactValue() ) );
        distFileField.setText( bamToGASVParams.getDistFile() );

        switch( bamToGASVParams.getFragmentBoundsMethod() ) {
            case ParametersBamToGASV.FB_METHOD_SD:
                distSDButton.setSelected( true );
                break;
            case ParametersBamToGASV.FB_METHOD_EXACT:
                distExactButton.setSelected( true );
                break;
            case ParametersBamToGASV.FB_METHOD_FILE:
                distFileButton.setSelected( true );
                break;
            case ParametersBamToGASV.FB_METHOD_PCT: //fallthrough to default
            default:
                distPCTButton.setSelected( true );
        }
        updateComponentsForSelection( bamToGASVParams.getFragmentBoundsMethod() );

        switch( bamToGASVParams.getSamValidationStringency() ) {
            case ParametersBamToGASV.STRINGENCY_STRICT:
                valiStrictButton.setSelected( true );
                break;
            case ParametersBamToGASV.STRINGENCY_LENIENT:
                valiLenientButton.setSelected( true );
                break;
            case ParametersBamToGASV.STRINGENCY_SILENT: //fallthrough to default
            default:
                valiSilentButton.setSelected( true );
        }
    }


    /**
     * Only sets the given Integer value as text into the JTextField if it is a
     * value larger than -1.
     * <p>
     * @param textField JTextField whose text shall be updated
     * @param value     The integer value to place in the JTextField
     */
    private void setIntegerWhenValid( JTextField textField, int value ) {
        String text = "";
        if( value > -1 ) {
            text = String.valueOf( value );
        }
        textField.setText( text );
    }


    /**
     * Updates all components according to the selected fragment bounds
     * calculation method.
     * <p>
     * @param fragmentBoundsMethod The method for specifying fragment
     *                             distribution bounds.
     */
    private void updateComponentsForSelection( String fragmentBoundsMethod ) {
        distPCTField.setEditable( ParametersBamToGASV.FB_METHOD_PCT.equals( fragmentBoundsMethod ) );
        distSDField.setEditable( ParametersBamToGASV.FB_METHOD_SD.equals( fragmentBoundsMethod ) );
        distExactField.setEditable( ParametersBamToGASV.FB_METHOD_EXACT.equals( fragmentBoundsMethod ) );
        distChooseButton.setEnabled( ParametersBamToGASV.FB_METHOD_FILE.equals( fragmentBoundsMethod ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "BAMToGASV configuration";
    }


    /** This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stringencyButtonGroup = new javax.swing.ButtonGroup();
        fragmentBoundsButtonGroup = new javax.swing.ButtonGroup();
        librarySeparatedBox = new javax.swing.JCheckBox();
        minMappingQualLabel = new javax.swing.JLabel();
        minMappingQualityField = new javax.swing.JTextField();
        fragentDistLabel = new javax.swing.JLabel();
        maxPairLengthLabel = new javax.swing.JLabel();
        maxPairLengthField = new javax.swing.JTextField();
        platformBox = new javax.swing.JCheckBox();
        writeConcPairsBox = new javax.swing.JCheckBox();
        writeLowQualBox = new javax.swing.JCheckBox();
        validationLabel = new javax.swing.JLabel();
        valiSilentButton = new javax.swing.JRadioButton();
        valiLenientButton = new javax.swing.JRadioButton();
        valiStrictButton = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        distPCTButton = new javax.swing.JRadioButton();
        distSDButton = new javax.swing.JRadioButton();
        distExactButton = new javax.swing.JRadioButton();
        distFileButton = new javax.swing.JRadioButton();
        jSeparator2 = new javax.swing.JSeparator();
        distPCTField = new javax.swing.JTextField();
        distSDField = new javax.swing.JTextField();
        distExactField = new javax.swing.JTextField();
        distFileField = new javax.swing.JTextField();
        distChooseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(librarySeparatedBox, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.librarySeparatedBox.text")); // NOI18N
        librarySeparatedBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                librarySeparatedBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(minMappingQualLabel, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.minMappingQualLabel.text")); // NOI18N

        minMappingQualityField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        minMappingQualityField.setText(org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.minMappingQualityField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fragentDistLabel, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.fragentDistLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(maxPairLengthLabel, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.maxPairLengthLabel.text")); // NOI18N

        maxPairLengthField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxPairLengthField.setText(org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.maxPairLengthField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(platformBox, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.platformBox.text")); // NOI18N
        platformBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(writeConcPairsBox, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.writeConcPairsBox.text")); // NOI18N
        writeConcPairsBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeConcPairsBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(writeLowQualBox, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.writeLowQualBox.text")); // NOI18N
        writeLowQualBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeLowQualBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(validationLabel, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.validationLabel.text")); // NOI18N

        stringencyButtonGroup.add(valiSilentButton);
        valiSilentButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(valiSilentButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.valiSilentButton.text")); // NOI18N
        valiSilentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valiSilentButtonActionPerformed(evt);
            }
        });

        stringencyButtonGroup.add(valiLenientButton);
        org.openide.awt.Mnemonics.setLocalizedText(valiLenientButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.valiLenientButton.text")); // NOI18N
        valiLenientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valiLenientButtonActionPerformed(evt);
            }
        });

        stringencyButtonGroup.add(valiStrictButton);
        org.openide.awt.Mnemonics.setLocalizedText(valiStrictButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.valiStrictButton.text")); // NOI18N
        valiStrictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valiStrictButtonActionPerformed(evt);
            }
        });

        fragmentBoundsButtonGroup.add(distPCTButton);
        distPCTButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(distPCTButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distPCTButton.text")); // NOI18N
        distPCTButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distPCTButtonActionPerformed(evt);
            }
        });

        fragmentBoundsButtonGroup.add(distSDButton);
        org.openide.awt.Mnemonics.setLocalizedText(distSDButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distSDButton.text")); // NOI18N
        distSDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distSDButtonActionPerformed(evt);
            }
        });

        fragmentBoundsButtonGroup.add(distExactButton);
        org.openide.awt.Mnemonics.setLocalizedText(distExactButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distExactButton.text")); // NOI18N
        distExactButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distExactButtonActionPerformed(evt);
            }
        });

        fragmentBoundsButtonGroup.add(distFileButton);
        org.openide.awt.Mnemonics.setLocalizedText(distFileButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distFileButton.text")); // NOI18N
        distFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distFileButtonActionPerformed(evt);
            }
        });

        distPCTField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        distPCTField.setText(org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distPCTField.text")); // NOI18N

        distSDField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        distSDField.setText(org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distSDField.text")); // NOI18N

        distExactField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        distExactField.setText(org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distExactField.text")); // NOI18N

        distFileField.setEditable(false);
        distFileField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        distFileField.setText(org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distFileField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(distChooseButton, org.openide.util.NbBundle.getMessage(BamToGASVVisualPanel.class, "BamToGASVVisualPanel.distChooseButton.text")); // NOI18N
        distChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distChooseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(maxPairLengthLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(maxPairLengthField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(distPCTButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(distPCTField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(distSDButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(distSDField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(distExactButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(distExactField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(distFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(distFileField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(distChooseButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(fragentDistLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(librarySeparatedBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(platformBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(valiLenientButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(valiSilentButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(writeLowQualBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(writeConcPairsBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(validationLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(valiStrictButton, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 115, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(minMappingQualLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(minMappingQualityField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(platformBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librarySeparatedBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(writeConcPairsBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(writeLowQualBox)
                .addGap(9, 9, 9)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minMappingQualityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minMappingQualLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPairLengthLabel)
                    .addComponent(maxPairLengthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fragentDistLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(distPCTButton)
                    .addComponent(distPCTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(distSDButton)
                    .addComponent(distSDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(distExactButton)
                    .addComponent(distExactField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(distFileButton)
                    .addComponent(distFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(distChooseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valiSilentButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valiLenientButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valiStrictButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void distPCTButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distPCTButtonActionPerformed
        if( distPCTButton.isSelected() ) {
            updateComponentsForSelection( ParametersBamToGASV.FB_METHOD_PCT );
            params.setFragmentBoundsMethod( ParametersBamToGASV.FB_METHOD_PCT );
        }
        isRequiredInfoSet();
    }//GEN-LAST:event_distPCTButtonActionPerformed

    private void distSDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distSDButtonActionPerformed
        if( distSDButton.isSelected() ) {
            updateComponentsForSelection( ParametersBamToGASV.FB_METHOD_SD );
            params.setFragmentBoundsMethod( ParametersBamToGASV.FB_METHOD_SD );
        }
        isRequiredInfoSet();
    }//GEN-LAST:event_distSDButtonActionPerformed

    private void distExactButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distExactButtonActionPerformed
        if( distExactButton.isSelected() ) {
            updateComponentsForSelection( ParametersBamToGASV.FB_METHOD_EXACT );
            params.setFragmentBoundsMethod( ParametersBamToGASV.FB_METHOD_EXACT );
        }
        isRequiredInfoSet();
    }//GEN-LAST:event_distExactButtonActionPerformed

    private void distFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distFileButtonActionPerformed
        if( distFileButton.isSelected() ) {
            updateComponentsForSelection( ParametersBamToGASV.FB_METHOD_FILE );
            params.setFragmentBoundsMethod( ParametersBamToGASV.FB_METHOD_FILE );
        }
        isRequiredInfoSet();
    }//GEN-LAST:event_distFileButtonActionPerformed

    private void distChooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distChooseButtonActionPerformed
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( null, "" ) {


            @Override
            public void open( String fileLocation ) {
                distFileField.setText( fileLocation );
            }


            @Override
            public void save( String fileLocation ) {
                throw new UnsupportedOperationException( "Not supported by this filechooser." );
            }


        };
        fileChooser.openFileChooser( ReadXplorerFileChooser.OPEN_DIALOG );
    }//GEN-LAST:event_distChooseButtonActionPerformed

    private void valiSilentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valiSilentButtonActionPerformed
        if( valiSilentButton.isSelected() ) {
            params.setSamValidationStringency( ParametersBamToGASV.STRINGENCY_SILENT );
        }
    }//GEN-LAST:event_valiSilentButtonActionPerformed

    private void valiLenientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valiLenientButtonActionPerformed
        if( valiLenientButton.isSelected() ) {
            params.setSamValidationStringency( ParametersBamToGASV.STRINGENCY_LENIENT );
        }
    }//GEN-LAST:event_valiLenientButtonActionPerformed

    private void valiStrictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valiStrictButtonActionPerformed
        if( valiStrictButton.isSelected() ) {
            params.setSamValidationStringency( ParametersBamToGASV.STRINGENCY_STRICT );
        }
    }//GEN-LAST:event_valiStrictButtonActionPerformed

    private void platformBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformBoxActionPerformed
        params.setPlatform( platformBox.isSelected() );
    }//GEN-LAST:event_platformBoxActionPerformed

    private void librarySeparatedBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_librarySeparatedBoxActionPerformed
        params.setLibrarySeparated( librarySeparatedBox.isSelected() );
    }//GEN-LAST:event_librarySeparatedBoxActionPerformed

    private void writeConcPairsBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeConcPairsBoxActionPerformed
        params.setWriteConcordantPairs( writeConcPairsBox.isSelected() );
    }//GEN-LAST:event_writeConcPairsBoxActionPerformed

    private void writeLowQualBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeLowQualBoxActionPerformed
        params.setWriteLowQualityPairs( writeLowQualBox.isSelected() );
    }//GEN-LAST:event_writeLowQualBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton distChooseButton;
    private javax.swing.JRadioButton distExactButton;
    private javax.swing.JTextField distExactField;
    private javax.swing.JRadioButton distFileButton;
    private javax.swing.JTextField distFileField;
    private javax.swing.JRadioButton distPCTButton;
    private javax.swing.JTextField distPCTField;
    private javax.swing.JRadioButton distSDButton;
    private javax.swing.JTextField distSDField;
    private javax.swing.JLabel fragentDistLabel;
    private javax.swing.ButtonGroup fragmentBoundsButtonGroup;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JCheckBox librarySeparatedBox;
    private javax.swing.JTextField maxPairLengthField;
    private javax.swing.JLabel maxPairLengthLabel;
    private javax.swing.JLabel minMappingQualLabel;
    private javax.swing.JTextField minMappingQualityField;
    private javax.swing.JCheckBox platformBox;
    private javax.swing.ButtonGroup stringencyButtonGroup;
    private javax.swing.JRadioButton valiLenientButton;
    private javax.swing.JRadioButton valiSilentButton;
    private javax.swing.JRadioButton valiStrictButton;
    private javax.swing.JLabel validationLabel;
    private javax.swing.JCheckBox writeConcPairsBox;
    private javax.swing.JCheckBox writeLowQualBox;
    // End of variables declaration//GEN-END:variables


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequiredInfoSet() {

        boolean isValidated = true;

        if( GeneralUtils.isValidByteInput( minMappingQualityField.getText() ) &&
            GeneralUtils.isValidIntegerInput( maxPairLengthField.getText() ) ) {

            params.setMinMappingQuality( Byte.parseByte( minMappingQualityField.getText() ) );
            params.setMaxPairLength( Integer.parseInt( maxPairLengthField.getText() ) );

        } else {
            isValidated = false;
        }

        if( !distPCTField.getText().isEmpty() && GeneralUtils.isValidIntegerInput( distPCTField.getText() ) ) {
            params.setDistPCTValue( Integer.parseInt( distPCTField.getText() ) );
        } else if( distPCTButton.isSelected() ) {
            isValidated = false;
        }

        if( !distSDField.getText().isEmpty() && GeneralUtils.isValidIntegerInput( distSDField.getText() ) ) {
            params.setDistSDValue( Integer.parseInt( distSDField.getText() ) );
        } else if( distSDButton.isSelected() ) {
            isValidated = false;
        }

        if( !distFileField.getText().isEmpty() && FileUtils.fileExistsAndIsReadable( distFileField.getText() ) ) {
            params.setDistFile( distFileField.getText() );
        } else if( distFileButton.isSelected() ) {
            isValidated = false;
        }

        String[] lMinAndMax = distExactField.getText().split( "," );
        if( lMinAndMax.length == 2 &&
            GeneralUtils.isValidIntegerInput( lMinAndMax[0] ) &&
            GeneralUtils.isValidIntegerInput( lMinAndMax[1] ) &&
            Integer.parseInt( lMinAndMax[0] ) < Integer.parseInt( lMinAndMax[1] ) ) {

            params.setDistExactValue( distExactField.getText() );
        } else if( distExactButton.isSelected() ) {
            isValidated = false;
        }

        firePropertyChange( ChangeListeningWizardPanel.PROP_VALIDATE, null, isValidated );
        return isValidated;
    }


}
