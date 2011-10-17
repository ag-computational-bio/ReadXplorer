/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.tools.snp454;

import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "Tools",
id = "de.cebitec.vamp.tools.snp454.OpenSNPDetection454")
@ActionRegistration(iconBase = "de/cebitec/vamp/tools/snp454/snpDetection.png",
displayName = "#CTL_OpenSNPDetection454")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 153),
    @ActionReference(path = "Toolbars/Tools", position = 225)
})
@Messages("CTL_OpenSNPDetection454=SNP-Detection454")
public final class OpenSNPDetection454 implements ActionListener {
    
    private final List<TrackViewer> context;

    public OpenSNPDetection454(List<TrackViewer> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TrackViewer currentTrackViewer = null;
        if (context.size() > 1){
            JList trackList = new JList(context.toArray());
            DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(trackList, NbBundle.getMessage(OpenSNPDetection454.class, "TTL_OpenSNPDetection"));
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

        SNP_Detection454TopComponent snpDetection = (SNP_Detection454TopComponent) WindowManager.getDefault().findTopComponent("SNP_Detection454TopComponent");
        snpDetection.openDetectionTab(currentTrackViewer);
        snpDetection.open();
    }
}
