package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import de.cebitec.vamp.view.dataVisualisation.TranscriptionAnalysesFrameI;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author rhilker
 * 
 * Action for opening a new transcription analyses frame. It opens a track list containing all
 * tracks of the selected reference and creates a new transcription analyses frame
 * when a track was chosen.
 */

@ActionID(category = "Tools",
id = "de.cebitec.vamp.transcriptionAnalyses.OpenTranscriptionAnalysesAction")
@ActionRegistration(iconBase = "de/cebitec/vamp/transcriptionAnalyses/transcriptionAnalyses.png",
displayName = "#CTL_OpenTranscriptionAnalysesAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 142, separatorAfter = 150),
    @ActionReference(path = "Toolbars/Tools", position = 187)
})
@Messages("CTL_OpenTranscriptionAnalysesAction=Transcription Analyses")
public final class OpenTranscriptionAnalysesAction implements ActionListener {

    private final List<TrackViewer> context;

    public OpenTranscriptionAnalysesAction(List<TrackViewer> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        TrackViewer currentTrackViewer = null;
        if (this.context.size() > 1){
            JList trackList = new JList(this.context.toArray());
            DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(trackList, NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "TTL_OpenTranscriptionAnalysesViewer"));
            dd.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
            DialogDisplayer.getDefault().notify(dd);
            if (dd.getValue().equals(DialogDescriptor.OK_OPTION) && !trackList.isSelectionEmpty()){
                currentTrackViewer = (TrackViewer) trackList.getSelectedValue();
            } else {
                return;
            }
        } else {
            // context cannot be emtpy, so no check here
            currentTrackViewer = context.get(0);
        }
        
        
        TranscriptionAnalysesFrameI mainViewer;
        TopComponent[] mainViewers = WindowManager.getDefault().getOpenedTopComponents(WindowManager.getDefault().findMode("editor"));
        for (int i=0; i<mainViewers.length; ++i){
            if (mainViewers[i] instanceof TranscriptionAnalysesFrameI && ((AppPanelTopComponent) mainViewers[i]).getComponentCount() > 0){
                mainViewer = (TranscriptionAnalysesFrameI) mainViewers[i];
                
                if (mainViewer.hasTranscriptionAnalysesTopPanel()) {
                    ((TranscriptionAnalysesTopPanel) mainViewer.getTranscriptionAnalysesTopPanel()).openAnalysisTab(currentTrackViewer);
                } else {
                    TranscriptionAnalysesTopPanel transAnalysesPanel = new TranscriptionAnalysesTopPanel();
                    transAnalysesPanel.openAnalysisTab(currentTrackViewer);
                    transAnalysesPanel.setParent(mainViewer);
                    mainViewer.showTranscriptionAnalysesTopPanel(transAnalysesPanel);
                }
                
                
            }
        }
        
        
//        TranscriptionAnalysesTopComponent transcAnalysesTopComp = (TranscriptionAnalysesTopComponent) WindowManager.getDefault().findTopComponent("TranscriptionAnalysesTopComponent");
//        transcAnalysesTopComp.setTrackConnector((TrackConnector) currentTrackViewer.getTrackCon());
//        transcAnalysesTopComp.openAnalysisTab(currentTrackViewer);
//        transcAnalysesTopComp.open();
    }
}
