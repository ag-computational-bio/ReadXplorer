package de.cebitec.readXplorer.tools.doubleTrackViewer;

import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.view.TopComponentHelper;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
        id = "de.cebitec.readXplorer.tools.doubleTrackViewer.OpenDoubleTrackAction")
@ActionRegistration(iconBase = "de/cebitec/readXplorer/tools/doubleTrackViewer/doubleTrack.png",
        displayName = "#CTL_OpenDoubleTrackAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 162),
    @ActionReference(path = "Toolbars/Tools", position = 287)
})
@NbBundle.Messages("CTL_OpenDoubleTrackAction=Double Track Viewer")
public final class OpenDoubleTrackAction implements ActionListener {

    private final ReferenceViewer context;

    public OpenDoubleTrackAction(ReferenceViewer context) {
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
