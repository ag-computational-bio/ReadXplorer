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

package de.cebitec.readxplorer.ui.converter;


import de.cebitec.readxplorer.parser.output.ConverterI;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import java.awt.Dialog;
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
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;


/**
 * Action for the converter.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID( category = "File",
           id = "de.cebitec.readxplorer.ui.converter.ConverterAction" )
@ActionRegistration( iconBase = "de/cebitec/readxplorer/ui/converter/import.png",
                     displayName = "#CTL_ConverterAction" )
@ActionReferences( {
    @ActionReference( path = "Menu/File", position = 1487, separatorAfter = 1493 ),
    @ActionReference( path = "Toolbars/File", position = 300 )
} )
@Messages( "CTL_ConverterAction=Convert Files" )
public final class ConverterAction implements ActionListener {

    public static final String PROP_CAN_CONVERT = "canConvert";
    public static final String PROP_FILEPATH = "filePath";
    public static final String PROP_CONVERTER_TYPE = "converterType";
    public static final String PROP_REFERENCE_NAME = "referenceName";
    public static final String PROP_REFERENCE_LENGTH = "referenceLength";

    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;


    @Override
    @SuppressWarnings( "unchecked" )
    public void actionPerformed( ActionEvent e ) {

        panels = new ArrayList<>( 10 );
        panels.add( new ConverterWizardPanel() );
        WizardDescriptor wizardDescriptor = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat( new MessageFormat( "{0}" ) );
        wizardDescriptor.setTitle( NbBundle.getMessage( ConverterAction.class, "TTL_ConvertWizardTitle" ) );
        Dialog dialog = DialogDisplayer.getDefault().createDialog( wizardDescriptor );
        dialog.setVisible( true );
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) { //could test can import property, if more than one pages are included...

            // start conversion
            ConverterI converter = (ConverterI) wizardDescriptor.getProperty( ConverterAction.PROP_CONVERTER_TYPE );
            ConvertThread convertThread = new ConvertThread( converter );
            convertThread.start();
        }
    }


}
