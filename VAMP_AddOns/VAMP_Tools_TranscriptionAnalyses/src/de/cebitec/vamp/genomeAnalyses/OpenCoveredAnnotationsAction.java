package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.AnalysesHandler;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptionAnalyses.OpenTranscriptionAnalysesAction;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.VisualisationUtils;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
    category = "Tools",
id = "de.cebitec.vamp.genomeAnalyses.OpenCoveredAnnotationsAction")
@ActionRegistration(
    iconBase = "de/cebitec/vamp/genomeAnalyses/coveredAnnotations.png",
displayName = "#CTL_OpenCoveredAnnotationsAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 146),
    @ActionReference(path = "Toolbars/Tools", position = 231)
})
@Messages("CTL_OpenCoveredAnnotationsAction=Covered Annotation Analysis")
public final class OpenCoveredAnnotationsAction implements ActionListener, DataVisualisationI {

    private final ReferenceViewer context;
    private ProjectConnector proCon;
    private int referenceId;
    private List<PersistantTrack> tracks;
    private CoveredAnnosAnalysisTopComponent coveredAnnoAnalysisTopComp;
    private Map<Integer, AnalysisContainer> trackToAnalysisMap;
    private int finishedCovAnalyses = 0;
    private ResultPanelCoveredAnnos coveredAnnosResultPanel;
    ParameterSetCoveredAnnos parameters;
    private HashMap<Integer, String> trackList;

    public OpenCoveredAnnotationsAction(ReferenceViewer context) {
        this.context = context;
        this.proCon = ProjectConnector.getInstance();
        this.referenceId = this.context.getReference().getId();
        this.trackToAnalysisMap = new HashMap<>();
    }

    /**
     * Carries out the logic behind the covered annotation analysis action. This
     * means, it opens a configuration wizard and starts the analysis after
     * successfully finishing the wizard.
     * @param ev the event, which is currently not used
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        OpenTrackPanelList otp = new OpenTrackPanelList(referenceId);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "CTL_OpenTrackList"));
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && !otp.getSelectedTracks().isEmpty()) {
            this.tracks = otp.getSelectedTracks();
            this.trackList = new HashMap<>();
            for (PersistantTrack track : otp.getSelectedTracks()) {
                this.trackList.put(track.getId(), track.getDescription());
            }

            this.coveredAnnoAnalysisTopComp = (CoveredAnnosAnalysisTopComponent) WindowManager.getDefault().findTopComponent("CoveredAnnosAnalysisTopComponent");
            this.runWizarAndCoveredAnnoAnalsysis();
            this.coveredAnnoAnalysisTopComp.open();
            
        } else if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && otp.getSelectedTracks().isEmpty()) {
            String msg = NbBundle.getMessage(OpenCoveredAnnotationsAction.class, "CTL_OpenCoveredAnnosDetectionInfo",
                    "No track selected. To start a covered annotation detection at least one track has to be selected.");
            String title = NbBundle.getMessage(OpenCoveredAnnotationsAction.class, "CTL_OpenCoveredAnnosDetectionInfoTitle", "Info");
            JOptionPane.showMessageDialog(this.context, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void runWizarAndCoveredAnnoAnalsysis() {
        
        @SuppressWarnings("unchecked")
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new CoveredAnnosWizardPanel());
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(OpenCoveredAnnotationsAction.class, "TTL_CoveredAnnosWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            this.startCoveredAnnosDetection(wiz);
        }
    }

    /**
     * Starts the covered annotation detection.
     * @param wiz the wizard containing the covered annotation detection parameters
     */
    private void startCoveredAnnosDetection(WizardDescriptor wiz) {
        int minCoveredPercent = (int) wiz.getProperty(CoveredAnnosWizardPanel.PROP_MIN_COVERED_PERCENT);
        int minCoverageCount = (int) wiz.getProperty(CoveredAnnosWizardPanel.PROP_MIN_COVERAGE_COUNT);
        parameters = new ParameterSetCoveredAnnos(minCoveredPercent, minCoverageCount);
        
        TrackConnector connector;
        for (PersistantTrack track : this.tracks) {

            connector = ProjectConnector.getInstance().getTrackConnector(track);
            AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler(this); //every track has its own analysis handlers
            AnalysisCoveredAnnotations analysisCoveredAnnos = new AnalysisCoveredAnnotations(connector, minCoveredPercent, minCoverageCount);
            covAnalysisHandler.registerObserver(analysisCoveredAnnos);
            covAnalysisHandler.setCoverageNeeded(true);

            trackToAnalysisMap.put(track.getId(), new AnalysisContainer(analysisCoveredAnnos));
            covAnalysisHandler.startAnalysis();
        }
    }

    @Override
    public void showData(Object dataTypeObject) {
        try {
            @SuppressWarnings("unchecked")
            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) dataTypeObject;
            int trackId = dataTypePair.getFirst();
            String dataType = dataTypePair.getSecond();

            if (dataType.equals(AnalysesHandler.DATA_TYPE_COVERAGE)) {
                //get track name(s) for tab descriptions
                String trackNames = "";
                if (tracks != null && !tracks.isEmpty()) {
                    for (PersistantTrack track : tracks) {
                        trackNames = trackNames.concat(track.getDescription()).concat(" and ");
                    }
                    trackNames = trackNames.substring(0, trackNames.length() - 5);
                }

                ++finishedCovAnalyses;
                
                AnalysisCoveredAnnotations analysisCoveredAnnos = trackToAnalysisMap.get(trackId).getAnalysisCoveredAnnos();
                CoveredAnnotationResult result = new CoveredAnnotationResult(analysisCoveredAnnos.getResults(), trackList);
                result.setDetectionParameters(parameters);
                result.setAnnotationListSize(analysisCoveredAnnos.getGenomeAnnotationSize());

                if (coveredAnnosResultPanel == null) {
                    coveredAnnosResultPanel = new ResultPanelCoveredAnnos();
                    coveredAnnosResultPanel.setBoundsInfoManager(this.context.getBoundsInformationManager());
                }
                coveredAnnosResultPanel.addCoveredAnnos(result);

                if (finishedCovAnalyses >= tracks.size()) {
                    String panelName = "Detected covered annotations for " + trackNames + " (" + coveredAnnosResultPanel.getResultSize() + " hits)";
                    this.coveredAnnoAnalysisTopComp.openAnalysisTab(panelName, coveredAnnosResultPanel);
                }
            }
            
        } catch (ClassCastException e) {
            //do nothing, we dont handle other data in this class
        }
    }

    private static class AnalysisContainer { //even if there is only one analysis here, we can abstract this better later

        private final AnalysisCoveredAnnotations analysisCoveredAnnos;

        /**
         * Container class for all available transcription analyses.
         */
        public AnalysisContainer(AnalysisCoveredAnnotations analysisCoveredAnnos) {
            this.analysisCoveredAnnos = analysisCoveredAnnos;
        }

        /**
         * @return The transcription start site analysis stored in this
         * container
         */
        public AnalysisCoveredAnnotations getAnalysisCoveredAnnos() {
            return analysisCoveredAnnos;
        }
    }
}
