package de.cebitec.vamp.tools.externalViewer;

import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

public final class OpenExternalViewer implements ActionListener {

    private final List<TrackViewer> context;

    public OpenExternalViewer(List<TrackViewer> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        JList jList = new JList(context.toArray());
        DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(jList, "Choose track to analyse!");
        dd.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
        DialogDisplayer.getDefault().notify(dd);

        if (dd.getValue().equals(DialogDescriptor.OK_OPTION) && !jList.isSelectionEmpty()){
            ExternalViewerTopComponent externalViewer = new ExternalViewerTopComponent();
            externalViewer.setTrackConnector(((TrackViewer) jList.getSelectedValue()).getTrackCon());
            externalViewer.open();
        }
    }
}
