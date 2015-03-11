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

package de.cebitec.readxplorer.ui.datavisualisation.basepanel;


import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.utils.ColorProperties;
import de.cebitec.readxplorer.utils.GeneralUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.util.NbBundle;


/**
 * A provider for panels containing generally available options.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class LegendAndOptionsProvider {

    /**
     * A provider for panels containing generally available options.
     */
    private LegendAndOptionsProvider() {
    }


    /**
     * Creates a mapping quality (phred scale) filter.
     * <p>
     * @param viewer      The viewer for to which the filter shall be added
     * @param parentPanel The panel in which the filter shall be embedded
     */
    @NbBundle.Messages( "MinMappingQualityText=Filter by min. mapping quality" )
    public static void createMappingQualityFilter( final AbstractViewer viewer, JPanel parentPanel ) {
        final JPanel optionPanel = new JPanel();
        optionPanel.setLayout( new BoxLayout( optionPanel, BoxLayout.X_AXIS ) );
        optionPanel.setBackground( ColorProperties.LEGEND_BACKGROUND );
        final JTextField minMappingQualityField = new JTextField( "0" );
        minMappingQualityField.setMinimumSize( new Dimension( 50, 20 ) );
        minMappingQualityField.setPreferredSize( new Dimension( 50, 20 ) );
        minMappingQualityField.setMaximumSize( new Dimension( 50, 20 ) );
        final JButton minMappingQualityButton = new JButton( Bundle.MinMappingQualityText() );

        //create listener for updating the viewer when the filter value has been changed
        minMappingQualityButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                updateMinMappingQuality( viewer, minMappingQualityField );
            }


        } );
        optionPanel.add( minMappingQualityField );
        optionPanel.add( createPlaceholder() );
        optionPanel.add( minMappingQualityButton );
        parentPanel.add( optionPanel, BorderLayout.WEST );
    }


    /**
     * Updates the minimum mapping quality value of the given viewer if the
     * input is valid. Otherwise an error message is displayed.
     * <p>
     * @param viewer                 The viewer to update
     * @param minMappingQualityField The minimum mapping quality to set
     */
    private static void updateMinMappingQuality( AbstractViewer viewer, JTextField minMappingQualityField ) {
        if( GeneralUtils.isValidByteInput( minMappingQualityField.getText() ) ) {
            byte minMappingQual = Byte.parseByte( minMappingQualityField.getText().trim() );
            viewer.setMinMappingQuality( minMappingQual );
            viewer.setNewDataRequestNeeded( true );
            viewer.boundsChangedHook();
        } else if( !minMappingQualityField.getText().isEmpty() ) {
            JOptionPane.showMessageDialog( minMappingQualityField, "Please enter a valid mapping quality value! (1-127)", "Invalid Position", JOptionPane.ERROR_MESSAGE );
        }
    }


    /**
     * @return A placeholder with width 3 and height 20.
     */
    public static JPanel createPlaceholder() {
        JPanel placeholder = new JPanel();
        placeholder.setBackground( ColorProperties.LEGEND_BACKGROUND );
        placeholder.setMinimumSize( new Dimension( 3, 20 ) );
        placeholder.setPreferredSize( new Dimension( 3, 20 ) );
        return placeholder;
    }


    /**
     * @param text Text to place on the label.
     * <p>
     * @return A label with ColorProperties.LEGEND_BACKGROUND background color
     *         and font Arial in 11 with the given text.
     */
    public static JLabel createLabel( String text ) {
        final JLabel label = new JLabel( text );
        label.setBackground( ColorProperties.LEGEND_BACKGROUND );
        label.setFont( new Font( "Arial", Font.BOLD, 11 ) );
        return label;
    }


    /**
     * @return A JPanel with ColorProperties.LEGEND_BACKGROUND background color
     *         and a standard border layout.
     */
    public static JPanel createStandardPanel() {
        final JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( ColorProperties.LEGEND_BACKGROUND );
        return panel;
    }


    /**
     * @param text The text to place on the check box
     * <p>
     * @return A JCheckBox with ColorProperties.LEGEND_BACKGROUND background
     *         color.
     */
    public static JCheckBox createStandardCheckBox( String text ) {
        final JCheckBox checkBox = new JCheckBox( text );
        checkBox.setBackground( ColorProperties.LEGEND_BACKGROUND );
        return checkBox;
    }


    /**
     * Creates a panel to use as header for some other component.
     * <p>
     * @param text The text to place on the header panel
     * <p>
     * @return The header panel
     */
    public static JPanel createHeader( String text ) {
        JLabel header = LegendAndOptionsProvider.createLabel( text );
        final JPanel headerPanel = LegendAndOptionsProvider.createStandardPanel();
        headerPanel.add( header, BorderLayout.CENTER );
        headerPanel.setPreferredSize( new Dimension( headerPanel.getPreferredSize().width, headerPanel.getPreferredSize().height + 2 ) );
        return headerPanel;
    }


}
