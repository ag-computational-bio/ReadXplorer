package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * @author rhilker
 * 
 * Action for opening a new snp detection. It opens a track list containing all
 * tracks of the selected reference and creates a new snp detection setup top component
 * when tracks were selected.
 */
@ActionID(category = "Tools",
id = "de.cebitec.vamp.tools.snp.OpenSnpDetectionAction")
@ActionRegistration(iconBase = "de/cebitec/vamp/tools/snp/snpDetection.png",
displayName = "#CTL_OpenSNPDetection")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 125),
    @ActionReference(path = "Toolbars/Tools", position = 100)
})
@Messages("CTL_OpenSnpDetectionAction=OpenSnpDetectionAction")
public final class OpenSnpDetectionAction implements ActionListener {

    private final ReferenceViewer context;

    public OpenSnpDetectionAction(ReferenceViewer context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        OpenTrackPanelList otp = new OpenTrackPanelList(context.getReference().getId());
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, NbBundle.getMessage(OpenSnpDetectionAction.class, "CTL_OpenTrackList"));
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && !otp.getSelectedTracks().isEmpty()) {
            List<Integer> trackIds = new ArrayList<Integer>();
            for (PersistantTrack track : otp.getSelectedTracks()) {
                trackIds.add(track.getId());
            }
            SNP_DetectionTopComponent snpDetection = (SNP_DetectionTopComponent) WindowManager.getDefault().findTopComponent("SNP_DetectionTopComponent");
            snpDetection.openDetectionTab(context, trackIds);
            snpDetection.open();
        } else if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && otp.getSelectedTracks().isEmpty()) {
            String msg = NbBundle.getMessage(OpenSnpDetectionAction.class, "CTL_OpenSNPDetectionInfo", 
                    "No track selected. To start a SNP detection at least one track has to be selected.");
            String title = NbBundle.getMessage(OpenSnpDetectionAction.class, "CTL_OpenSNPDetectionInfoTitle", "Info");
            JOptionPane.showMessageDialog(this.context, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }

    }
}
