package de.cebitec.vamp.tools.twoTrackViewer;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import de.cebitec.vamp.view.TopComponentHelper;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class OpenMultiTrackAction implements ActionListener {

    private final ReferenceViewer context;

    public OpenMultiTrackAction(ReferenceViewer context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        AppPanelTopComponent appComp = TopComponentHelper.getActiveTopComp(AppPanelTopComponent.class);
        if (appComp != null) {
            //Get ViewController from AppPanelTopComponent-Lookup
            ViewController viewCon = appComp.getLookup().lookup(ViewController.class);
            viewCon.openDoubleTrack();
        }
    }
}
