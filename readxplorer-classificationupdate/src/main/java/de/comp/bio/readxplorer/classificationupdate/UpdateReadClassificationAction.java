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

package de.comp.bio.readxplorer.classificationupdate;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;


/**
 * An action to update the read classification data from older RX versions to
 * the most recent classification.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@ActionID(
         category = "File",
         id = "de.compbio.readxplorer.UpdateReadClassificationAction"
)
@ActionRegistration(
         displayName = "#CTL_UpdateReadClassificationAction"
)
@ActionReference( path = "Menu/File", position = 2000 )
@Messages( "CTL_UpdateReadClassificationAction=Update Read Classification" )
public final class UpdateReadClassificationAction implements ActionListener {

//    private final LoginCookie context;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;


    /**
     * An action to update the read classification data from older RX versions
     * to the most recent classification.
     * <p>
     * @param context The user needs to be logged into a DB to use this action
     */
    public UpdateReadClassificationAction( LoginCookie context ) {
//        this.context = context;
    }


    @Override
    @NbBundle.Messages( "TTL_UpdateWizardTitle=ReadXplorer Classification Update" )
    public void actionPerformed( ActionEvent ev ) {
        if( CentralLookup.getDefault().lookup( SwingWorker.class ) != null ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message( NbBundle.getMessage( UpdateReadClassificationAction.class, "MSG_BackgroundActivity" ), NotifyDescriptor.WARNING_MESSAGE );
            DialogDisplayer.getDefault().notify( nd );
            return;
        }

        if( panels == null ) {
            panels = new ArrayList<>( 1 );
            panels.add( new UpdateReadClassWizardPanel( "" ) );
        }
        WizardDescriptor wizardDescriptor = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat( new MessageFormat( "{0}" ) );
        wizardDescriptor.setTitle( Bundle.TTL_UpdateWizardTitle() );
        Dialog dialog = DialogDisplayer.getDefault().createDialog( wizardDescriptor );
        dialog.setVisible( true );
        dialog.toFront();

        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {
            UpdateThread updateThread = new UpdateThread();
            RequestProcessor rp = new RequestProcessor( "Update Thread", 2 );
            rp.post( updateThread );
        }
    }


}
