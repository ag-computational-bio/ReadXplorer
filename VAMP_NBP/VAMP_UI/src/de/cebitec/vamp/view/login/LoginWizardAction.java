package de.cebitec.vamp.view.login;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.api.cookies.LoginCookie;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import javax.swing.JComponent;
import org.h2.jdbc.JdbcSQLException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class LoginWizardAction implements ActionListener{

    private static final long serialVersionUID = 1L;

    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    @Override
    public void actionPerformed(ActionEvent e) {
        CentralLookup cl = CentralLookup.getDefault();
        // check if user is already logged in
        Boolean loggedIn = cl.lookup(LoginCookie.class) != null ? Boolean.TRUE : Boolean.FALSE;

        if (loggedIn){
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(LoginWizardAction.class, "MSG_LoginWizardAction.info.doubleLogin"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            // TODO: find a way to do an automatic logout below
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(LoginWizardAction.class, "MSG_LoginWizardAction.warning.doubleLogin"), NotifyDescriptor.WARNING_MESSAGE));
            return;
        }

        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(LoginWizardAction.class, "TTL_LoginWizardAction"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // log out before logging into another database
//            if (loggedIn){
//                LogoutAction logoutAction = Utilities.actionsGlobalContext().lookup(LogoutAction.class);
//                LogoutAction logoutAction = Lookups.forPath("Actions/File/").lookup(LogoutAction.class);
//                logoutAction.actionPerformed(null);
//            }
            
            Map<String, Object> loginProps = wizardDescriptor.getProperties();
            try {
                ProjectConnector.getInstance().connect((String) loginProps.get(LoginWizardPanel.PROP_ADAPTER), (String) loginProps.get(LoginWizardPanel.PROP_HOST), (String) loginProps.get(LoginWizardPanel.PROP_DATABASE), (String) loginProps.get(LoginWizardPanel.PROP_USER), (String) loginProps.get(LoginWizardPanel.PROP_PASSWORD));
                cl.add(new LoginCookie() {

                    @Override
                    public boolean isLoggedIn() {
                        return true;
                    }
                });
            } catch (SQLException ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(LogoutAction.class, "MSG_LoginWizardAction.sqlError"), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new LoginWizardPanel()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.FALSE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

}
