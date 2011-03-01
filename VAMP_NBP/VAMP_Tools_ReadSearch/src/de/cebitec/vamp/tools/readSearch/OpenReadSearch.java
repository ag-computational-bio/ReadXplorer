package de.cebitec.vamp.tools.readSearch;

import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public final class OpenReadSearch implements ActionListener {

    private final List<TrackViewer> context;

    public OpenReadSearch(List<TrackViewer> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        TrackViewer currentTrackViewer = null;
        if (context.size() > 1){
            JList trackList = new JList(context.toArray());
        DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(trackList, NbBundle.getMessage(OpenReadSearch.class, "TTL_OpenReadSearch"));
            dd.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
            DialogDisplayer.getDefault().notify(dd);
            if (dd.getValue().equals(DialogDescriptor.OK_OPTION) && !trackList.isSelectionEmpty()){
                currentTrackViewer = (TrackViewer) trackList.getSelectedValue();
            } else {
                return;
            }
        }
        else{
            // context cannot be emtpy, so no check here
            currentTrackViewer = context.get(0);
        }

        ReadSearchTopComponent readSearch = (ReadSearchTopComponent) WindowManager.getDefault().findTopComponent("ReadSearchTopComponent");
        readSearch.resetComponent();
        readSearch.setTrackViewer(currentTrackViewer);
        readSearch.open();
    }
}
