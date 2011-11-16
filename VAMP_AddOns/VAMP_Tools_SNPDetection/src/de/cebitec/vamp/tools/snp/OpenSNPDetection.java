package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import org.openide.windows.WindowManager;

public final class OpenSNPDetection implements ActionListener {

    private final List<ReferenceViewer> context;

    public OpenSNPDetection(List<ReferenceViewer> context) {
        this.context = context;
    }
    
//    public OpenSNPDetection(String context) {
//        
//    }

    @Override
    public void actionPerformed(ActionEvent ev) {
//        TrackViewer currentTrackViewer = null;
//        if (context.size() > 1){
//            JList trackList = new JList(context.toArray());
//            DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(trackList, NbBundle.getMessage(OpenSNPDetection.class, "TTL_OpenSNPDetection"));
//            dd.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
//            DialogDisplayer.getDefault().notify(dd);
//            if (dd.getValue().equals(DialogDescriptor.OK_OPTION) && !trackList.isSelectionEmpty()){
//                currentTrackViewer = (TrackViewer) trackList.getSelectedValue();
//            } else {
//                return;
//            }
//        }
//        else{
//            // context cannot be emtpy, so no check here
//            currentTrackViewer = context.get(0);
//        }

        SNP_DetectionTopComponent snpDetection = (SNP_DetectionTopComponent) WindowManager.getDefault().findTopComponent("SNP_DetectionTopComponent");
        //snpDetection.openDetectionTab(currentTrackViewer);
        snpDetection.openDetectionTab(context.get(0));
        snpDetection.open();
    }
    
}
