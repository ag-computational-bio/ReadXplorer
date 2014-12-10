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

package de.cebitec.readXplorer.view.login;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.view.dialogMenus.LoadingDialog;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;


/**
 * Action for opening the login wizard.
 *
 * @author ddoppmeier, Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@Messages( {
    "# {0} - error message",
    "LoginWizardAction_ErrorMsg=An error occurred during opening of the DB: {0}",
    "LoginWizardAction_ErrorHeader=Loading Error" } )
public final class LoginWizardAction implements ActionListener {

    private static final long serialVersionUID = 1L;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private LoadingDialog loading;
    private LoginWizardPanel loginPanel;


    @Override
    public void actionPerformed( ActionEvent e ) {
        final CentralLookup cl = CentralLookup.getDefault();
        // check if user is already logged in
        Boolean loggedIn = cl.lookup( LoginCookie.class ) != null ? Boolean.TRUE : Boolean.FALSE;

        if( loggedIn ) { //logout from other db
            LogoutAction logoutAction = new LogoutAction( cl.lookup( LoginCookie.class ) );
            logoutAction.actionPerformed( new ActionEvent( this, 1, "close" ) );
        }

        if( panels == null ) {
            if( this.loginPanel == null ) {
                this.loginPanel = new LoginWizardPanel();
            }
            panels = new ArrayList<>();
            panels.add( loginPanel );
        }
        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( NbBundle.getMessage( LoginWizardAction.class, "TTL_LoginWizardAction" ) );
        Dialog dialog = DialogDisplayer.getDefault().createDialog( wiz );
        dialog.setVisible( true );
        dialog.toFront();
        boolean cancelled = wiz.getValue() != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {
            try {
                this.loading = new LoadingDialog( WindowManager.getDefault().getMainWindow() );

                final Map<String, Object> loginProps = wiz.getProperties();
                //add database path to main window title
                JFrame mainFrame = (JFrame) WindowManager.getDefault().getMainWindow();
                mainFrame.setTitle( mainFrame.getTitle() + " - " + loginProps.get( LoginWizardPanel.PROP_DATABASE ) );
                Thread connectThread = new Thread( new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProjectConnector.getInstance().connect(
                                    (String) loginProps.get( LoginWizardPanel.PROP_ADAPTER ),
                                    (String) loginProps.get( LoginWizardPanel.PROP_DATABASE ),
                                    (String) loginProps.get( LoginWizardPanel.PROP_HOST ),
                                    (String) loginProps.get( LoginWizardPanel.PROP_USER ),
                                    (String) loginProps.get( LoginWizardPanel.PROP_PASSWORD ) );
                            cl.add( new LoginCookie() {
                                @Override
                                public boolean isLoggedIn() {
                                    return true;
                                }


                            } );
                        }
                        catch( SQLException ex ) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message( ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE );
                            nd.setTitle( NbBundle.getMessage( LoginWizardAction.class, "MSG_LoginWizardAction.sqlError" ) );
                            DialogDisplayer.getDefault().notify( nd );
                        }
                        SwingUtilities.invokeLater( new LoadingRunnable() );
                    }


                } );
                connectThread.start();
            }
            catch( Exception ex ) {
                SwingUtilities.invokeLater( new LoadingRunnable() );
                JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), Bundle.LoginWizardAction_ErrorMsg( ex.toString() ),
                                               Bundle.LoginWizardAction_ErrorHeader(), JOptionPane.ERROR_MESSAGE );
            }
        }
    }


    /**
     * Updates the choose button text.
     * <p>
     * @param chooseButtonText
     */
    public void setChooseButtonText( String chooseButtonText ) {
        if( this.loginPanel == null ) {
            this.loginPanel = new LoginWizardPanel();
        }
        ((LoginVisualPanel) this.loginPanel.getComponent()).setChooseButtonText( chooseButtonText );
    }


    /**
     * Runnable for ending the loading dialog after connection to the DB is
     * established.
     */
    private class LoadingRunnable implements Runnable {

        @Override
        public void run() {
            loading.finished();
        }


    }

}
