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

package de.cebitec.readxplorer.mapping;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.mapping.api.MappingApi;
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


/**
 * MappingAction adds a menu item to start the mapping of a sequencing data set.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
@ActionID(
         category = "Tools",
         id = "de.cebitec.readxplorer.mapping.MappingAction" )
@ActionRegistration(
         displayName = "#CTL_MappingAction" )
@ActionReference( path = "Menu/Tools", position = 154 )
@NbBundle.Messages( "CTL_MappingAction=Map reads" )
public final class MappingAction implements ActionListener {

    static final String PROP_SOURCEPATH = "PROP_SOURCEPATH";
    static final String PROP_REFERENCEPATH = "PROP_REFERENCEPATH";
    static final String PROP_MAPPINGPARAM = "PROP_MAPPINGPARAM";


//    private final LoginCookie context;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;


    /**
     * MappingAction adds a menu item to start the mapping of a sequencing data
     * set.
     * <p>
     * @param context a LoginCookie, because it only works when logged into a DB
     */
    public MappingAction( LoginCookie context ) {
//        this.context = context;
    }


    @Override
    @NbBundle.Messages( "MSG_BackgroundActivity=There is currently other background activity!" )
    public void actionPerformed( ActionEvent ev ) {
        if( CentralLookup.getDefault().lookup( SwingWorker.class ) != null ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message( Bundle.MSG_BackgroundActivity(), NotifyDescriptor.WARNING_MESSAGE );
            DialogDisplayer.getDefault().notify( nd );
            return;
        }
        if( MappingApi.checkMapperConfig() ) {
            if( panels == null ) {
                panels = new ArrayList<>( 2 );
                panels.add( new MappingSelectionPanel() );
                panels.add( new MappingOverviewPanel() );
            }
            WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wiz.setTitleFormat( new MessageFormat( "{0}" ) );
            wiz.setTitle( NbBundle.getMessage( MappingAction.class, "CTL_MappingAction" ) );
            Dialog dialog = DialogDisplayer.getDefault().createDialog( wiz );
            dialog.setVisible( true );
            dialog.toFront();
            boolean cancelled = wiz.getValue() != WizardDescriptor.FINISH_OPTION;
            if( !cancelled ) {
                new MappingProcessor( (String) wiz.getProperty( PROP_REFERENCEPATH ),
                                      (String) wiz.getProperty( PROP_SOURCEPATH ),
                                      (String) wiz.getProperty( PROP_MAPPINGPARAM )
                );
            }
        }
    }


}
