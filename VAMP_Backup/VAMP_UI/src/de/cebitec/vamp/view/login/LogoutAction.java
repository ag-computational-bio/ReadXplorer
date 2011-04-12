package de.cebitec.vamp.view.login;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.api.ApplicationFrameI;
import de.cebitec.vamp.api.cookies.LoginCookie;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class LogoutAction implements ActionListener {

    private final LoginCookie context;

    public LogoutAction(LoginCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(LogoutAction.class, "MSG_LogoutAction.warning.busy"), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        else {
            if (ProjectConnector.getInstance().isConnected()){
                ProjectConnector.getInstance().disconnect();
            }
            for(TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()){
                if (tc instanceof ApplicationFrameI){
                    tc.close();
                }
            }
            CentralLookup.getDefault().remove(context);
        }
    }
}
