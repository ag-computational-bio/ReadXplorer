package de.cebitec.readXplorer.tools.detailedViewer;

import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.view.TopComponentHelper;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public final class OpenDetailedViewer implements ActionListener {

    private final List<TrackViewer> context;

    public OpenDetailedViewer(List<TrackViewer> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        AppPanelTopComponent parentAppPanel = TopComponentHelper.getActiveTopComp(AppPanelTopComponent.class);
        if (parentAppPanel != null) {
            TrackViewer currentTrackViewer;
            //Get ViewController from AppPanelTopComponent-Lookup
            ViewController viewCon = parentAppPanel.getLookup().lookup(ViewController.class);
            List<BasePanel> trackPanels = viewCon.getOpenTracks();
            List<AbstractViewer> openTrackViewers = this.getTrackViewerList(viewCon.getOpenTracks());

            if (trackPanels.size() > 1) {
                JList trackList = new JList(openTrackViewers.toArray());
                DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(trackList, NbBundle.getMessage(OpenDetailedViewer.class, "CTL_OpenDetailedViewer"));
                dd.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
                DialogDisplayer.getDefault().notify(dd);
                if (dd.getValue().equals(DialogDescriptor.OK_OPTION) && !trackList.isSelectionEmpty()) {
                    currentTrackViewer = (TrackViewer) trackList.getSelectedValue();
                } else {
                    return;
                }
            } else {
                // context cannot be emtpy, so no check here
                currentTrackViewer = (TrackViewer) trackPanels.get(0).getViewer();
            }

            DetailedViewerTopComponent detailedViewer = new DetailedViewerTopComponent(viewCon);
            detailedViewer.setTrackConnector(currentTrackViewer.getTrackCon());
            detailedViewer.open();
        }
    }

    private List<AbstractViewer> getTrackViewerList(List<BasePanel> openTracks) {
        List<AbstractViewer> viewerList = new ArrayList<>();
        for (BasePanel basePanel : openTracks) {
            viewerList.add(basePanel.getViewer());
        }
        return viewerList;
    }
}
