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
id = "de.cebitec.vamp.genomeAnalyses.OpenCoveredFeaturesAction")
@ActionRegistration(
    iconBase = "de/cebitec/vamp/genomeAnalyses/coveredFeatures.png",
displayName = "#CTL_OpenCoveredFeaturesAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 146),
    @ActionReference(path = "Toolbars/Tools", position = 231)
})
@Messages("CTL_OpenCoveredFeaturesAction=Covered Feature Analysis")
public final class OpenCoveredFeaturesAction implements ActionListener, DataVisualisationI {

    private final ReferenceViewer context;
    private ProjectConnector proCon;
    private int referenceId;
    private List<PersistantTrack> tracks;
    private CoveredFeaturesAnalysisTopComponent coveredAnnoAnalysisTopComp;
    private Map<Integer, AnalysisContainer> trackToAnalysisMap;
    private int finishedCovAnalyses = 0;
    private ResultPanelCoveredFeatures coveredFeaturesResultPanel;
    ParameterSetCoveredFeatures parameters;
    private HashMap<Integer, PersistantTrack> trackMap;

    public OpenCoveredFeaturesAction(ReferenceViewer context) {
        this.context = context;
        this.proCon = ProjectConnector.getInstance();
        this.referenceId = this.context.getReference().getId();
        this.trackToAnalysisMap = new HashMap<>();
    }

    /**
     * Carries out the logic behind the covered feature analysis action. This
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
            this.trackMap = new HashMap<>();
            for (PersistantTrack track : otp.getSelectedTracks()) {
                this.trackMap.put(track.getId(), track);
            }

            this.coveredAnnoAnalysisTopComp = (CoveredFeaturesAnalysisTopComponent) WindowManager.getDefault().findTopComponent("CoveredFeaturesAnalysisTopComponent");
            this.coveredAnnoAnalysisTopComp.open();
            this.runWizarAndCoveredAnnoAnalsysis();
            
        } else if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && otp.getSelectedTracks().isEmpty()) {
            String msg = NbBundle.getMessage(OpenCoveredFeaturesAction.class, "CTL_OpenCoveredFeaturesDetectionInfo",
                    "No track selected. To start a covered feature detection at least one track has to be selected.");
            String title = NbBundle.getMessage(OpenCoveredFeaturesAction.class, "CTL_OpenCoveredFeaturesDetectionInfoTitle", "Info");
            JOptionPane.showMessageDialog(this.context, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void runWizarAndCoveredAnnoAnalsysis() {
        
        @SuppressWarnings("unchecked")
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new CoveredFeaturesWizardPanel());
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(OpenCoveredFeaturesAction.class, "TTL_CoveredFeaturesWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            this.startCoveredFeaturesDetection(wiz);
        } else {
            this.coveredAnnoAnalysisTopComp.close();
        }
    }

    /**
     * Starts the covered feature detection.
     * @param wiz the wizard containing the covered feature detection parameters
     */
    private void startCoveredFeaturesDetection(WizardDescriptor wiz) {
        int minCoveredPercent = (int) wiz.getProperty(CoveredFeaturesWizardPanel.PROP_MIN_COVERED_PERCENT);
        int minCoverageCount = (int) wiz.getProperty(CoveredFeaturesWizardPanel.PROP_MIN_COVERAGE_COUNT);
        parameters = new ParameterSetCoveredFeatures(minCoveredPercent, minCoverageCount);
        
        TrackConnector connector;
        for (PersistantTrack track : this.tracks) {

            connector = ProjectConnector.getInstance().getTrackConnector(track);
            AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler(this, 
                    NbBundle.getMessage(OpenCoveredFeaturesAction.class, "MSG_AnalysesWorker.progress.name")); //every track has its own analysis handlers
            AnalysisCoveredFeatures analysisCoveredFeatures = new AnalysisCoveredFeatures(connector, minCoveredPercent, minCoverageCount);
            covAnalysisHandler.registerObserver(analysisCoveredFeatures);
            covAnalysisHandler.setCoverageNeeded(true);

            trackToAnalysisMap.put(track.getId(), new AnalysisContainer(analysisCoveredFeatures));
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
                
                AnalysisCoveredFeatures analysisCoveredFeatures = trackToAnalysisMap.get(trackId).getAnalysisCoveredFeatures();
                CoveredFeatureResult result = new CoveredFeatureResult(analysisCoveredFeatures.getResults(), trackMap);
                result.setParameters(parameters);
                result.setFeatureListSize(analysisCoveredFeatures.getNoGenomeFeatures());

                if (coveredFeaturesResultPanel == null) {
                    coveredFeaturesResultPanel = new ResultPanelCoveredFeatures();
                    coveredFeaturesResultPanel.setBoundsInfoManager(this.context.getBoundsInformationManager());
                }
                coveredFeaturesResultPanel.addCoveredFeatures(result);

                if (finishedCovAnalyses >= tracks.size()) {
                    String panelName = "Detected covered features for " + trackNames + " (" + coveredFeaturesResultPanel.getResultSize() + " hits)";
                    this.coveredAnnoAnalysisTopComp.openAnalysisTab(panelName, coveredFeaturesResultPanel);
                }
            }
            
        } catch (ClassCastException e) {
            //do nothing, we dont handle other data in this class
        }
    }

    private static class AnalysisContainer { //TODO:even if there is only one analysis here, we can abstract this better later

        private final AnalysisCoveredFeatures analysisCoveredFeatures;

        /**
         * Container class for all available transcription analyses.
         */
        public AnalysisContainer(AnalysisCoveredFeatures analysisCoveredFeatures) {
            this.analysisCoveredFeatures = analysisCoveredFeatures;
        }

        /**
         * @return The transcription start site analysis stored in this
         * container
         */
        public AnalysisCoveredFeatures getAnalysisCoveredFeatures() {
            return analysisCoveredFeatures;
        }
    }
}
