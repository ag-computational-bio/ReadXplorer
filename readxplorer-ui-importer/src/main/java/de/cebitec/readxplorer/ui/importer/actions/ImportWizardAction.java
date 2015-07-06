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

package de.cebitec.readxplorer.ui.importer.actions;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.parser.ReadPairJobContainer;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.ui.importer.ImportThread;
import de.cebitec.readxplorer.ui.importer.ImportWizardOverviewPanel;
import de.cebitec.readxplorer.ui.importer.ImportWizardSetupPanel;
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


/**
 * Action for importing data into ReadXplorer.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@ActionID(
         category = "File",
         id = "de.cebitec.readxplorer.ui.importer.actions.ImportWizardAction"
)
@ActionRegistration(
         iconBase = "de/cebitec/readxplorer/ui/importer/import.png",
         displayName = "#CTL_ImportWizardAction"
)
@ActionReferences( {
    @ActionReference( path = "Menu/File", position = 1450, separatorAfter = 1475 ),
    @ActionReference( path = "Toolbars/Management", position = 400 )
} )
@NbBundle.Messages( "CTL_ImportWizardAction=Import data" )
public final class ImportWizardAction implements ActionListener {

//    private final LoginCookie context;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    public static final String PROP_CAN_IMPORT = "canImport";
    public static final String PROP_REFJOBLIST = "referenceJob";
    public static final String PROP_TRACKJOBLIST = "trackJobList";
    public static final String PROP_READPAIRJOBLIST = "readPairJobList";
    public static final String PROP_READPAIRDIST = "readPairDistance";
    public static final String PROP_READPAIRDEVIATION = "readPairDeviation";
    public static final String PROP_POSITIONTABLELIST = "positionTableList";


    /**
     * Creates an action for importing data into ReadXplorer.
     * <p>
     * @param context Only available, if already logged into a database
     */
    public ImportWizardAction( LoginCookie context ) {
//        this.context = context;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    @NbBundle.Messages( "TTL_ImportWizardTitle=ReadXplorer Import" )
    public void actionPerformed( ActionEvent ev ) {
        if( CentralLookup.getDefault().lookup( SwingWorker.class ) != null ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message( NbBundle.getMessage( ImportWizardAction.class, "MSG_BackgroundActivity" ), NotifyDescriptor.WARNING_MESSAGE );
            DialogDisplayer.getDefault().notify( nd );
            return;
        }

        if( panels == null ) {
            panels = new ArrayList<>( 2 );
            panels.add( new ImportWizardSetupPanel() );
            panels.add( new ImportWizardOverviewPanel() );
        }
        WizardDescriptor wizardDescriptor = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat( new MessageFormat( "{0}" ) );
        wizardDescriptor.setTitle( Bundle.TTL_ImportWizardTitle() );
        Dialog dialog = DialogDisplayer.getDefault().createDialog( wizardDescriptor );
        dialog.setVisible( true );
        dialog.toFront();

        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {
            // get paramters
            List<ReferenceJob> refJobs = (List<ReferenceJob>) wizardDescriptor.getProperty( PROP_REFJOBLIST );
            List<TrackJob> trackJobs = (List<TrackJob>) wizardDescriptor.getProperty( PROP_TRACKJOBLIST );
            //since read pair jobs have their own parser it can be distinguished later
            List<ReadPairJobContainer> readPairJobs = (List<ReadPairJobContainer>) wizardDescriptor.getProperty( PROP_READPAIRJOBLIST );

            ImportThread i = new ImportThread( refJobs, trackJobs, readPairJobs );
            RequestProcessor rp = new RequestProcessor( "Import Threads", 2 );
            rp.post( i );
        }
    }


}
