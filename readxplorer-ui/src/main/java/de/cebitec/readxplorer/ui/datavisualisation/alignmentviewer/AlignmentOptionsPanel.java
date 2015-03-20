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

package de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer;


import de.cebitec.readxplorer.ui.datavisualisation.basepanel.LegendAndOptionsProvider;
import de.cebitec.readxplorer.utils.ColorProperties;
import de.cebitec.readxplorer.utils.Properties;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/**
 * Panel showing general options for an alignment viewer.
 * <p>
 * @author -Rolf Hilker-
 */
public class AlignmentOptionsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Preferences PREF = NbPreferences.forModule( Object.class );

    private final AlignmentViewer alignmentViewer;


    /**
     * Panel showing general options for an alignment viewer.
     * <p>
     * @param alignmentViewer The alignment viewer to which the options panel
     *                        belongs.
     */
    public AlignmentOptionsPanel( AlignmentViewer alignmentViewer ) {
        this.alignmentViewer = alignmentViewer;
        this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        this.setBackground( ColorProperties.LEGEND_BACKGROUND );
        this.initOtherComponents();
    }


    @NbBundle.Messages( { "AlignmentOptionsPanel.General=General:",
                          "AlignmentOptionsPanel.Centering=Enable centering sequence bar",
                          "AlignmentOptionsPanel.Qualities=Show base qualities",
                          "AlignmentOptionsPanel_QualityToolTip=Good quality bases = bright hue, bad quality bases = dark hue",
                          "AlignmentOptionsPanel_BlockHeight=Adjust height of alignments:" } )
    private void initOtherComponents() {
        //create header
        add( LegendAndOptionsProvider.createHeader( Bundle.AlignmentOptionsPanel_General() ) );
        //create mapping filter
        LegendAndOptionsProvider.createMappingQualityFilter( alignmentViewer, this );
        createCenterSeqBarBox();
        createShowBaseQualitiesBox();
        createAdjustAlignmentHeightSpinner();

        updateUI();
    }


    /**
     * Creates a check box for centering the sequence bar when selected.
     */
    private void createCenterSeqBarBox() {
        JPanel generalPanel = LegendAndOptionsProvider.createStandardPanel();
        final JCheckBox centerBox = LegendAndOptionsProvider.createStandardCheckBox( Bundle.AlignmentOptionsPanel_Centering() );
        centerBox.setSelected( true );

        //automatic centering around the sequence bar enabled event
        centerBox.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                JCheckBox scaleBox = (JCheckBox) e.getSource();
                alignmentViewer.setAutomaticCentering( scaleBox.isSelected() );
            }


        } );
        generalPanel.add( centerBox, BorderLayout.WEST );
        add( generalPanel );
    }


    /**
     * Creates a check box for enabling visualization of base qualities when
     * selected.
     */
    private void createShowBaseQualitiesBox() {
        JPanel generalPanel = LegendAndOptionsProvider.createStandardPanel();
        final JCheckBox qualitiesBox = LegendAndOptionsProvider.createStandardCheckBox( Bundle.AlignmentOptionsPanel_Qualities() );
        qualitiesBox.setSelected( PREF.getBoolean( Properties.BASE_QUALITY_OPTION, true ) );
        qualitiesBox.setToolTipText( Bundle.AlignmentOptionsPanel_QualityToolTip() );
        generalPanel.add( qualitiesBox, BorderLayout.WEST );

        //update base qualities property, when the box selection is changed
        qualitiesBox.addActionListener( new ActionListener() {

            //
            @Override
            public void actionPerformed( ActionEvent e ) {
                JCheckBox scaleBox = (JCheckBox) e.getSource();
                PREF.putBoolean( Properties.BASE_QUALITY_OPTION, scaleBox.isSelected() );
            }


        } );

        add( generalPanel );
    }


    /**
     * Creates a JSpinner for adjusting the height of alignment blocks.
     */
    private void createAdjustAlignmentHeightSpinner() {
        JPanel generalPanel = LegendAndOptionsProvider.createStandardPanel();
        JLabel label = LegendAndOptionsProvider.createLabel( Bundle.AlignmentOptionsPanel_BlockHeight(), Font.PLAIN );
        int blockHeight = PREF.getInt( Properties.BLOCK_HEIGHT_OPTION, AlignmentViewer.DEFAULT_BLOCK_HEIGHT );
        JSpinner heightSpinner = LegendAndOptionsProvider.createStandardSpinner(
                new SpinnerNumberModel( blockHeight, 1, 10, 1 ) );
        generalPanel.add( label, BorderLayout.WEST );
        generalPanel.add( heightSpinner, BorderLayout.EAST );

        heightSpinner.addChangeListener( new ChangeListener() {


            @Override
            public void stateChanged( ChangeEvent e ) {
                JSpinner heightSpinner = (JSpinner) e.getSource();
                PREF.putInt( Properties.BLOCK_HEIGHT_OPTION, (Integer) heightSpinner.getValue() );
            }


        } );

        add( generalPanel );
    }


}
