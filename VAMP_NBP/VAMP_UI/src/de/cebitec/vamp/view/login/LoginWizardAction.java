package de.cebitec.vamp.view.login;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import java.awt.Component;
import java.awt.Dialog;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class LoginWizardAction extends CallableSystemAction{

    private static final long serialVersionUID = 1L;

    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    @Override
    public void performAction() {
        CentralLookup cl = CentralLookup.getDefault();
        // check if user is already logged in
        Boolean loggedIn = cl.lookup(ViewController.class) != null ? Boolean.TRUE : Boolean.FALSE;

        if (loggedIn){
            NotifyDescriptor nd = new NotifyDescriptor.Message("You are already logged into a VAMP database. If you log into another database your current connection will be closed.", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            // TODO find a way to do an automatic logout below
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Please log out first.", NotifyDescriptor.WARNING_MESSAGE));
            return;
        }

        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Project Login");
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
                ProjectConnector.getInstance().connect((String) loginProps.get("adapter"), (String) loginProps.get("hostname"), (String) loginProps.get("database"), (String) loginProps.get("user"), (String) loginProps.get("password"));
                // TODO get rid of ViewController
                ViewController con = ViewController.getInstance();
                cl.add(con);
                WindowManager.getDefault().findTopComponent("AppPanelTopComponent").open();
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
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
                        new LoginWizardPanel1()
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

    @Override
    public String getName() {
        return "Login";
    }

    @Override
    public String iconResource() {
        return "de/cebitec/vamp/resources/flower.png";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
