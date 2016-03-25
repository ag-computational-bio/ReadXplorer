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

package de.cebitec.readxplorer.utils;


import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import java.awt.Component;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import static de.cebitec.readxplorer.utils.Bundle.OOM_Header;
import static de.cebitec.readxplorer.utils.Bundle.OOM_Message;


/**
 * Class containing general visualization related methods.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public final class VisualisationUtils {


    /**
     * Instantiation not allowed!
     */
    private VisualisationUtils() {
    }


    /**
     * Recursive method to get all buttons belonging to a parent component.
     * <p>
     * @param comp    the component whose buttons are needed
     * @param buttons the list to be filled with the identified buttons
     */
    public static void getButtons( JComponent comp, List<JButton> buttons ) {
        if( comp == null ) {
            return;
        }

        for( Component c : comp.getComponents() ) {
            if( c instanceof JButton ) {
                buttons.add( (JButton) c );

            } else if( c instanceof JComponent ) {
                VisualisationUtils.getButtons( (JComponent) c, buttons );
            }
        }
    }


    /**
     * Initialize panels representing individual wizard's steps and sets various
     * properties for them influencing wizard appearance for static wizards.
     * <p>
     * @param wizardPanels the panels belonging to the wizard
     * <p>
     * @return the configured wizard panel array
     */
    @SuppressWarnings( "unchecked" )
    public static List<WizardDescriptor.Panel<WizardDescriptor>> getWizardPanels( List<WizardDescriptor.Panel<WizardDescriptor>> wizardPanels ) {

        String[] steps = new String[wizardPanels.size() + 1];
        for( int i = 0; i < wizardPanels.size(); i++ ) {
            Component c = wizardPanels.get( i ).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if( c instanceof JComponent ) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i );
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, steps );
                jc.putClientProperty( WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true );
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DISPLAYED, true );
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_NUMBERED, true );
            }
        }
        steps[steps.length - 1] = "Press 'Finish' to start";
        return wizardPanels;
    }


    /**
     * Lets the current {@link de.cebitec.readxplorer.api.ErrorHandler} handle
     * an out of memory error.
     */
    @NbBundle.Messages( { "OOM_Message=An out of memory error occurred. Please restart the software with more memory.",
                          "OOM_Header=Restart Software" } )
    public static void displayOutOfMemoryError() {
        ErrorHelper.getHandler().handle( new OutOfMemoryError( OOM_Message() ), OOM_Header() );
    }


}
