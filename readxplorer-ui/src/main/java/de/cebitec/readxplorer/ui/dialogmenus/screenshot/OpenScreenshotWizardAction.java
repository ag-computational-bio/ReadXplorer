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

package de.cebitec.readxplorer.ui.dialogmenus.screenshot;


import de.cebitec.readxplorer.utils.ScreenshotUtils;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/**
 * Action for storing a screenshot of any of the TopComponents, which are
 * currently open in ReadXplorer.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@ActionID(
         category = "Visualisation",
         id = "de.cebitec.readxplorer.ui.dialogmenus.screenshot.OpenScreenshotWizardAction" )
@ActionRegistration(
         iconBase = "de/cebitec/readxplorer/ui/dialogmenus/screenshot/screenshot.png",
         displayName = "#CTL_OpenScreenshotWizardAction" )
@ActionReferences( {
    @ActionReference( path = "Menu/Visualisation", position = 700 ),
    @ActionReference( path = "Toolbars/Visualisation", position = 487 ),
    @ActionReference( path = "Shortcuts", name = "D-P" )
} )
@Messages( { "CTL_OpenScreenshotWizardAction=Open Screenshot Wizard",
             "ScreenshotWizardTitle=Screenshot Wizard" } )
public final class OpenScreenshotWizardAction implements ActionListener {

    /**
     * Action for storing a screenshot of any of the TopComponents, which are
     * currently open in ReadXplorer.
     */
    public OpenScreenshotWizardAction() {
    }


    /**
     * Opens a wizard displaying all available options for storing screenshots.
     * <p>
     * @param e event is currently not used
     */
    @Override
    public void actionPerformed( ActionEvent e ) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add( new ScreenshotWizardPanel() );
        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( Bundle.ScreenshotWizardTitle() );
        if( DialogDisplayer.getDefault().notify( wiz ) == WizardDescriptor.FINISH_OPTION ) {
            TopComponent comp = (TopComponent) wiz.getProperty( ScreenshotWizardPanel.PROP_SELECTED_TOP_COMP );
            Rectangle oldBounds = comp.getBounds();
            ScreenshotUtils.saveScreenshot( comp );
            comp.setBounds( oldBounds );
            comp.validate();
        }
    }


}
