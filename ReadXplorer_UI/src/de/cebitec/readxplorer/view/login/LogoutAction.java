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

package de.cebitec.readxplorer.view.login;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


public final class LogoutAction implements ActionListener {

    private final LoginCookie context;


    public LogoutAction( LoginCookie context ) {
        this.context = context;
    }


    @Override
    public void actionPerformed( ActionEvent ev ) {
        if( CentralLookup.getDefault().lookup( SwingWorker.class ) != null ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message( NbBundle.getMessage( LogoutAction.class, "MSG_LogoutAction.warning.busy" ), NotifyDescriptor.WARNING_MESSAGE );
            DialogDisplayer.getDefault().notify( nd );

        }
        else {

            //do not close the dashboard window, if opened!
            TopComponent dashboard = WindowManager.getDefault().findTopComponent( "DashboardWindowTopComponent" );
            for( TopComponent tc : WindowManager.getDefault().getRegistry().getOpened() ) {
                if( tc != dashboard ) {
                    tc.close();
                }
//                TopComponent tc1 = WindowManager.getDefault().findTopComponent("RNAFolderTopComponent");
//                if (tc1 != null){ //useful if rna viewer should be openend after closing DB connection
//                    tc.close();
//                }
                //ViewController viewCon - listener auf schließen des zugehörigen top components
            }
            //sometimes the window remains in the same state -> needs a repaint
            //dashboard.repaint();


            if( ProjectConnector.getInstance().isConnected() ) {
                ProjectConnector.getInstance().disconnect();
            }
            //reset main window title
            JFrame mainFrame = (JFrame) WindowManager.getDefault().getMainWindow();
            int index = mainFrame.getTitle().indexOf( '-' );
            if( index > -1 ) {
                mainFrame.setTitle( mainFrame.getTitle().substring( 0, index - 1 ) );
            }

            if( context != null ) {
                CentralLookup.getDefault().remove( context );
            }
        }
    }


}
