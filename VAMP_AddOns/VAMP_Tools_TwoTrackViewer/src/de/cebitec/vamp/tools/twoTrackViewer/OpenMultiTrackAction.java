package de.cebitec.vamp.tools.twoTrackViewer;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import de.cebitec.vamp.view.TopComponentHelper;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanelFactory;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

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
            PersistantReference ref = appComp.getReferenceViewer().getReference();

            OpenTrackPanelList otp = new OpenTrackPanelList(ref.getId());
            JPanel contentPanel = new JPanel(new BorderLayout());
            JCheckBox optionBox = new JCheckBox("Combine multiple tracks");
            optionBox.setToolTipText("When checked, more than two tracks can be chosen and the coverage of all tracks is combined"
                    + " and looks like the ordinary track viewer for one track.");
            contentPanel.add(otp, BorderLayout.CENTER);
            contentPanel.add(optionBox, BorderLayout.SOUTH);


            DialogDescriptor dialogDescriptor = new DialogDescriptor(contentPanel, "Open Multiple Tracks");
            Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            openRefGenDialog.setVisible(true);

            // check if two tracks were selected
            boolean okSelected = false;
            if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && (otp.getSelectedTracks().size() == 2
                    || optionBox.isSelected() && otp.getSelectedTracks().size() > 1)) {
                okSelected = true;
            } else if (!(dialogDescriptor.getValue().equals(DialogDescriptor.CANCEL_OPTION)
                    || dialogDescriptor.getValue().equals(DialogDescriptor.CLOSED_OPTION))) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Please select exactly TWO tracks in standard mode or at least two track in combined mode!",
                        NotifyDescriptor.INFORMATION_MESSAGE));
                this.actionPerformed(ev);
            }
            if (okSelected) {
                BasePanelFactory factory = viewCon.getBasePanelFac();
                factory.getMultipleTracksBasePanel(otp.getSelectedTracks(), ref, optionBox.isSelected());
            }
        }
    }
}
