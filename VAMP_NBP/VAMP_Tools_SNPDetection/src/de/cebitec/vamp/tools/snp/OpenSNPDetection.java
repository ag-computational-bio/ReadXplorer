package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.windows.WindowManager;

public final class OpenSNPDetection implements ActionListener {

    private final List<TrackViewer> context;

    public OpenSNPDetection(List<TrackViewer> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        JList jList = new JList(context.toArray());
        Confirmation dd = new DialogDescriptor.Confirmation(jList, "Choose track to analyse!");
        DialogDisplayer.getDefault().notify(dd);

        if (dd.getValue().equals(DialogDescriptor.OK_OPTION)){
            SNP_DetectionTopComponent snpDetection = (SNP_DetectionTopComponent) WindowManager.getDefault().findTopComponent("SNP_DetectionTopComponent");
            snpDetection.open();
            snpDetection.setTrackViewer((TrackViewer) jList.getSelectedValue());
        }
    }
}
