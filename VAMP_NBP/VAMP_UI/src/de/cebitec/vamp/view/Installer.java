package de.cebitec.vamp.view;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.api.cookies.LoginCookie;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.view.login.LogoutAction;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle.
 * Makes sure that VAMP is not closed while a task is still running
 */
public class Installer extends ModuleInstall {
    
    public static final String VAMP_VERSION = "1.6";
    private static final long serialVersionUID = 1L;
//    private static final Logger logger = Logger.getLogger(Installer.class.getName(), Installer.class.getPackage().getName() + ".Log");

    @Override
    public void restored() {
        //set version number
        System.setProperty("netbeans.buildnumber", VAMP_VERSION);
        

        // redirect systemouts to internal netbeans plattform outputwindow
//        redirectSystemStreams();
        
        //The TopComponent we're interested in isn't immediately available.
        //This method allows us to delay start of our procedure until later.
/*        WindowManager.getDefault().invokeWhenUIReady(new Runnable()
        {
            @Override
            public void run()
            {
                //Locate the Output Window instance
                final String OUTPUT_ID = "output";
                logger.log(Level.FINE, "LOG_FindingWindow", OUTPUT_ID);
                TopComponent outputWindow = WindowManager.getDefault().findTopComponent(OUTPUT_ID);

                //Determine if it is opened
                if (outputWindow != null && outputWindow.isOpened())
                {
                    logger.log(Level.FINE, "LOG_WindowOpen", OUTPUT_ID);
                    final String FOLDER = "Actions/View/";
                    final String INSTANCE_FILE = "org-netbeans-core-actions-LogAction";

                    //Use Lookup to find the instance in the file system
                    logger.log(Level.FINE, "LOG_LookupAction", new Object[]{FOLDER, INSTANCE_FILE});
                    Lookup pathLookup = Lookups.forPath(FOLDER);
                    Template<Action> actionTemplate = new Template<Action>(Action.class, FOLDER + INSTANCE_FILE, null);
                    Result<Action> lookupResult = pathLookup.lookup(actionTemplate);
                    Collection<? extends Action> foundActions = lookupResult.allInstances();

                    //For each instance (should ony be one) call actionPerformed()
                    for (Action action : foundActions)
                    {
                        logger.log(Level.FINE, "LOG_FoundAction", action);
                        action.actionPerformed(null);
                    }
                }
                else
                {
                    logger.log(Level.FINE, "LOG_WindowClosed", OUTPUT_ID);
                }
            }
        });*/
    }

    @Override
    public boolean closing() {
        if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
            NotifyDescriptor nd = new NotifyDescriptor.Message("VAMP is performing a non-interruptible task and may only be closed after it has finished.", NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return Boolean.FALSE;
        }
        else {
            LogoutAction logoutAction = new LogoutAction(CentralLookup.getDefault().lookup(LoginCookie.class));
            logoutAction.actionPerformed(new ActionEvent(this, 1, "close"));

            // close remaining windows
            TopComponent dashboard = WindowManager.getDefault().findTopComponent("DashboardWindowTopComponent");
            for(TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()){
                    if (tc!=dashboard) tc.close();
            }
            // log out before exitting
            ProjectConnector pc = ProjectConnector.getInstance();
            if (pc.isConnected()) { pc.disconnect(); }

            return super.closing();
        }
    }
    
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {

             InputOutput io = IOProvider.getDefault().getIO("Output", true);

            @Override
            public void write(int i) throws IOException {
                io.getOut().print(i);
            }

            @Override
            public void write(byte[] bytes) throws IOException {
                io.getOut().print(new String(bytes));
            }

            @Override
            public void write(byte[] bytes, int off, int len) throws IOException {
                io.getOut().print(new String(bytes, off, len));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

}
