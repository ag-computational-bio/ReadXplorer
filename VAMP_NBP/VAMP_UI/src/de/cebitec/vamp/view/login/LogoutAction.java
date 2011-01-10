package de.cebitec.vamp.view.login;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;

public final class LogoutAction implements ActionListener {

    private final ViewController context;

    public LogoutAction(ViewController context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
            NotifyDescriptor nd = new NotifyDescriptor.Message("<html>VAMP is performing a non-interruptible task on the database.</br>You may not logout until it has finished.</html>", NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        else {
            if (ProjectConnector.getInstance().isConnected()){
                ProjectConnector.getInstance().disconnect();
            }
            WindowManager.getDefault().findTopComponent("AppPanelTopComponent").close();
            CentralLookup.getDefault().remove(context);
        }
    }
}
