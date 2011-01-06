package de.cebitec.vamp.view;

import de.cebitec.centrallookup.CentralLookup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;

public final class ExitVampAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
            NotifyDescriptor nd = new NotifyDescriptor.Message("VAMP is performing a non-interruptible task and may only be closed after it has finished.", NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        LifecycleManager.getDefault().exit();
    }
}
