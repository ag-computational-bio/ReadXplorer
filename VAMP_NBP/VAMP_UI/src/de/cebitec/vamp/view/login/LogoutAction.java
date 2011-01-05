package de.cebitec.vamp.view.login;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.windows.WindowManager;

public final class LogoutAction implements ActionListener {

    private final ViewController context;

    public LogoutAction(ViewController context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        if (ProjectConnector.getInstance().isConnected()){
            ProjectConnector.getInstance().disconnect();
        }
        WindowManager.getDefault().findTopComponent("AppPanelTopComponent").close();
        CentralLookup.getDefault().remove(context);
    }
}
