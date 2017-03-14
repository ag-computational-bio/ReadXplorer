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

package de.cebitec.readxplorer.ui.datamanagement.actions;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.ui.datamanagement.DataAdminWizardOverviewPanel;
import de.cebitec.readxplorer.ui.datamanagement.DataAdminWizardSelectionPanel;
import de.cebitec.readxplorer.ui.datamanagement.DeletionThread;
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
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


@ActionID(
         category = "File",
         id = "de.cebitec.readxplorer.ui.datamanagement.actions.DataAdminWizardAction"
)
@ActionRegistration(
         iconBase = "de/cebitec/readxplorer/ui/datamanagement/manage.png",
         displayName = "#CTL_DataAdminWizardAction"
)
@ActionReferences( {
    @ActionReference( path = "Menu/File", position = 1462 ),
    @ActionReference( path = "Toolbars/Management", position = 401 )
} )
@NbBundle.Messages( "CTL_DataAdminWizardAction=Manage data" )
public final class DataAdminWizardAction implements ActionListener {

//    private final LoginCookie context;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    public static final String PROP_REFS2DEL = "refdel";
    public static final String PROP_TRACK2DEL = "trackdel";


    public DataAdminWizardAction( LoginCookie context ) {
//        this.context = context;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public void actionPerformed( ActionEvent ev ) {
        if( CentralLookup.getDefault().lookup( SwingWorker.class ) != null ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message( NbBundle.getMessage( DataAdminWizardAction.class, "MSG_BackgroundActivity" ), NotifyDescriptor.WARNING_MESSAGE );
            DialogDisplayer.getDefault().notify( nd );
            return;
        }
        if( panels == null ) {
            panels = new ArrayList<>( 2 );
            panels.add( new DataAdminWizardSelectionPanel() );
            panels.add( new DataAdminWizardOverviewPanel() );
        }
        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( NbBundle.getMessage( DataAdminWizardAction.class, "TTL_DataAdminWizardAction.title" ) );
        Dialog dialog = DialogDisplayer.getDefault().createDialog( wiz );
        dialog.setVisible( true );
        dialog.toFront();
        boolean cancelled = wiz.getValue() != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {
            List<ReferenceJob> refs2del = (List<ReferenceJob>) wiz.getProperty( DataAdminWizardAction.PROP_REFS2DEL );
            List<TrackJob> tracks2del = (List<TrackJob>) wiz.getProperty( DataAdminWizardAction.PROP_TRACK2DEL );
            DeletionThread dt = new DeletionThread( refs2del, tracks2del );
            RequestProcessor rp = new RequestProcessor( "Deletion Threads", 2 );
            rp.post( dt );
        }
    }


}
