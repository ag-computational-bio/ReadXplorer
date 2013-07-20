package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.AnalysesHandler;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptionAnalyses.wizard.TranscriptionAnalysesWizardIterator;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/**
 * Action for opening a new transcription analyses frame. It opens a track list
 * containing all tracks of the selected reference and creates a new
 * transcription analyses frame when a track was chosen.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
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
public final class OpenTranscriptionAnalysesAction implements ActionListener, DataVisualisationI {

    private TranscriptionAnalysesTopComponent transcAnalysesTopComp;
    private final ReferenceViewer refViewer;
    private int referenceId;
    private List<PersistantTrack> tracks;
    private int finishedCovAnalyses = 0;
    private int finishedMappingAnalyses = 0;
    private ParameterSetTSS parametersTss;
    private ParameterSetOperonDet parametersOperonDet;
    private ParameterSetRPKM parametersRPKM;
    private Map<Integer, PersistantTrack> trackMap;
    private Map<Integer, AnalysisContainer> trackToAnalysisMap;
    private ResultPanelTranscriptionStart transcriptionStartResultPanel;
    private ResultPanelOperonDetection operonResultPanel;
    private ResultPanelRPKM rpkmResultPanel;
    
    private boolean performTSSAnalysis;
    private boolean performOperonAnalysis;
    private boolean performRPKMAnalysis;
    private boolean autoTssParamEstimation = false;
    private int minTotalIncrease = 0;
    private int minPercentIncrease = 0;
    private int maxLowCovInitCount = 0;
    private int minLowCovIncrease = 0;
    private boolean performUnannotatedTranscriptDet = false;
    private int minTranscriptExtensionCov = 0;
    private int minNumberReads = 0;
    private int maxNumberReads = 0;
    private boolean autoOperonParamEstimation = false;
    private int minSpanningReads = 0;
    private String readClassPropString;
    private Set<FeatureType> selFeatureTypes = new HashSet<>();
    private String selFeatureTypesPropString;

    /**
     * Action for opening a new transcription analyses frame. It opens a track
     * list containing all tracks of the selected reference and creates a new
     * transcription analyses frame when a track was chosen.
     * @param context the context of the action: the reference viewer which is
     * connected with this analysis
     */
    public OpenTranscriptionAnalysesAction(ReferenceViewer context) {
        this.refViewer = context;
        this.referenceId = this.refViewer.getReference().getId();
        this.transcAnalysesTopComp = (TranscriptionAnalysesTopComponent) WindowManager.getDefault().findTopComponent("TranscriptionAnalysesTopComponent");
        this.trackToAnalysisMap = new HashMap<>();
    }

    /**
     * Carries out the logic behind the transcription analyses action. This
     * means, it opens a configuration wizard and starts the analyses after
     * successfully finishing the wizard.
     *
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

            this.transcAnalysesTopComp.open();
            this.runWizardAndTranscriptionAnalysis();

        } else {
            String msg = NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfo",
                    "No track selected. To start a transcription analysis at least one track has to be selected.");
            String title = NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfoTitle", "Info");
            JOptionPane.showMessageDialog(this.refViewer, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Initializes the setup wizard for the transcription analyses.
     */
    private void runWizardAndTranscriptionAnalysis() {
        @SuppressWarnings("unchecked")
        TranscriptionAnalysesWizardIterator transWizardIterator = new TranscriptionAnalysesWizardIterator();
        boolean containsDBTrack = PersistantTrack.checkForDBTrack(this.tracks);
        transWizardIterator.setUsingDBTrack(containsDBTrack);
        this.readClassPropString = transWizardIterator.getReadClassPropForWiz();
        this.selFeatureTypesPropString = transWizardIterator.getPropSelectedFeatTypes();
        WizardDescriptor wiz = new WizardDescriptor(transWizardIterator);
        transWizardIterator.setWiz(wiz);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "TTL_TransAnalysesWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            this.startTransciptionAnalyses(wiz);
        } else {
            if (!this.transcAnalysesTopComp.hasComponents()) {
                this.transcAnalysesTopComp.close();
            }
        }
    }

    /**
     * Starts the transcription analyses.
     *
     * @param wiz the wizard containing the transcription analyses parameters
     */
    @SuppressWarnings("unchecked")
    private void startTransciptionAnalyses(WizardDescriptor wiz) {

        //obtain all analysis parameters
        performTSSAnalysis = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_TSS_ANALYSIS);
        performOperonAnalysis = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_OPERON_ANALYSIS);
        performRPKMAnalysis = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_RPKM_ANALYSIS);
        
        ParametersReadClasses readClassesParams = (ParametersReadClasses) wiz.getProperty(readClassPropString);

        if (performTSSAnalysis) { //set values depending on the selected analysis functions (avoiding null pointers)
            autoTssParamEstimation = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_AUTO_TSS_PARAMS);
            minTotalIncrease = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_TOTAL_INCREASE);
            minPercentIncrease = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_PERCENT_INCREASE);
            maxLowCovInitCount = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_LOW_COV_INIT_COUNT);
            minLowCovIncrease = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_LOW_COV_INC);
            performUnannotatedTranscriptDet = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_UNANNOTATED_TRANSCRIPT_DET);
            minTranscriptExtensionCov = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_TRANSCRIPT_EXTENSION_COV);
        }
        if (performOperonAnalysis) {
            autoOperonParamEstimation = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_AUTO_OPERON_PARAMS);
            minSpanningReads = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_SPANNING_READS);
        }
        if (performRPKMAnalysis) {
            minNumberReads = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS);
            maxNumberReads = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS);
            selFeatureTypes = (Set<FeatureType>) wiz.getProperty(selFeatureTypesPropString);
        }
        //create parameter set for each analysis
        parametersTss = new ParameterSetTSS(performTSSAnalysis, autoTssParamEstimation, performUnannotatedTranscriptDet,
                minTotalIncrease, minPercentIncrease, maxLowCovInitCount, minLowCovIncrease, minTranscriptExtensionCov);
        parametersOperonDet = new ParameterSetOperonDet(performOperonAnalysis, minSpanningReads, autoOperonParamEstimation);
        parametersRPKM = new ParameterSetRPKM(performRPKMAnalysis, minNumberReads, maxNumberReads, selFeatureTypes);


        TrackConnector connector;
        for (PersistantTrack track : this.tracks) {
            AnalysisTranscriptionStart analysisTSS = null;
            AnalysisOperon analysisOperon = null;
            AnalysisRPKM analysisRPKM = null;
            
            //TODO: Error handling
            try {
                connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
            } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
                continue;
            }

            AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler(this,
                    NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "MSG_AnalysesWorker.progress.name"), readClassesParams); //every track has its own analysis handlers
            AnalysesHandler mappingAnalysisHandler = connector.createAnalysisHandler(this,
                    NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "MSG_AnalysesWorker.progress.name"), readClassesParams);

            if (parametersTss.isPerformTSSAnalysis()) {

                if (parametersTss.isPerformUnannotatedTranscriptDet()) {
                    analysisTSS = new AnalysisUnannotatedTransStart(connector, parametersTss);
                } else {
                    analysisTSS = new AnalysisTranscriptionStart(connector, parametersTss);
                }
                covAnalysisHandler.registerObserver(analysisTSS);
                covAnalysisHandler.setCoverageNeeded(true);
            }
            if (parametersOperonDet.isPerformOperonAnalysis()) {
                analysisOperon = new AnalysisOperon(connector, parametersOperonDet.getMinSpanningReads(), parametersOperonDet.isAutoOperonParamEstimation());

                mappingAnalysisHandler.registerObserver(analysisOperon);
                mappingAnalysisHandler.setMappingsNeeded(true);
            }
            if (parametersRPKM.isPerformRPKMAnalysis()) {
                analysisRPKM = new AnalysisRPKM(connector, parametersRPKM);
                
                mappingAnalysisHandler.registerObserver(analysisRPKM);
                mappingAnalysisHandler.setMappingsNeeded(true);
            }

            trackToAnalysisMap.put(track.getId(), new AnalysisContainer(analysisTSS, analysisOperon, analysisRPKM));
            covAnalysisHandler.startAnalysis();
            mappingAnalysisHandler.startAnalysis();
        }
    }

    /**
     * Visualizes the data handed over to this method as defined by the
     * implementation.
     *
     * @param dataTypeObject the data object to visualize.
     */
    @Override
    public void showData(Object dataTypeObject) {
        try {
            @SuppressWarnings("unchecked")
            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) dataTypeObject;
            int trackId = dataTypePair.getFirst();
            String dataType = dataTypePair.getSecond();

            //get track name(s) for tab descriptions
            String trackNames;

            if (parametersTss.isPerformTSSAnalysis() && dataType.equals(AnalysesHandler.DATA_TYPE_COVERAGE)) {

                ++finishedCovAnalyses;

                //TODO: bp window of neighboring TSS parameter

                AnalysisTranscriptionStart analysisTSS = trackToAnalysisMap.get(trackId).getAnalysisTSS();
                parametersTss = analysisTSS.getParametersTSS(); //if automatic is on, the parameters are different now
                if (transcriptionStartResultPanel == null) {
                    transcriptionStartResultPanel = new ResultPanelTranscriptionStart();
                    transcriptionStartResultPanel.setReferenceViewer(this.refViewer);
                }

                TssDetectionResult tssResult = new TssDetectionResult(analysisTSS.getResults(), trackMap);
                tssResult.setParameters(parametersTss);
                transcriptionStartResultPanel.addTSSs(tssResult);

                if (finishedCovAnalyses >= tracks.size()) {
                    trackNames = GeneralUtils.generateConcatenatedString(tssResult.getTrackNameList(), 120);
                    String panelName = "Detected TSSs for " + trackNames + " (" + transcriptionStartResultPanel.getResultSize() + " hits)";
                    this.transcAnalysesTopComp.openAnalysisTab(panelName, transcriptionStartResultPanel);
                }
            }
            if (dataType.equals(AnalysesHandler.DATA_TYPE_MAPPINGS)) {
                ++finishedMappingAnalyses;

                if (parametersOperonDet.isPerformOperonAnalysis()) {

                    if (operonResultPanel == null) {
                        operonResultPanel = new ResultPanelOperonDetection(parametersOperonDet);
                        operonResultPanel.setBoundsInfoManager(this.refViewer.getBoundsInformationManager());
                    }
                    OperonDetectionResult operonDetectionResult = new OperonDetectionResult(trackMap,
                            trackToAnalysisMap.get(trackId).getAnalysisOperon().getResults());
                    operonDetectionResult.setParameters(parametersOperonDet);
                    operonResultPanel.addDetectedOperons(operonDetectionResult);

                    if (finishedMappingAnalyses >= tracks.size()) {
                        trackNames = GeneralUtils.generateConcatenatedString(operonDetectionResult.getTrackNameList(), 120);
                        String panelName = "Detected operons for " + trackNames + " (" + operonResultPanel.getResultSize() + " hits)";
                        this.transcAnalysesTopComp.openAnalysisTab(panelName, operonResultPanel);
                    }
                }
                
                if (parametersRPKM.isPerformRPKMAnalysis()) {
                    AnalysisRPKM rpkmAnalysis = trackToAnalysisMap.get(trackId).getAnalysisRPKM();
                    if (rpkmResultPanel == null) {
                        rpkmResultPanel = new ResultPanelRPKM();
                        rpkmResultPanel.setBoundsInfoManager(this.refViewer.getBoundsInformationManager());
                    }
                    RPKMAnalysisResult rpkmAnalysisResult = new RPKMAnalysisResult(trackMap,
                            trackToAnalysisMap.get(trackId).getAnalysisRPKM().getResults());
                    rpkmAnalysisResult.setParameters(parametersRPKM);
                    rpkmAnalysisResult.setNoGenomeFeatures(rpkmAnalysis.getNoGenomeFeatures());
                    rpkmResultPanel.addRPKMvalues(rpkmAnalysisResult);

                    if (finishedMappingAnalyses >= tracks.size()) {
                        trackNames = GeneralUtils.generateConcatenatedString(rpkmAnalysisResult.getTrackNameList(), 120);
                        String panelName = "RPKM and read count values for " + trackNames + " (" + rpkmResultPanel.getResultSize() + " hits)";
                        this.transcAnalysesTopComp.openAnalysisTab(panelName, rpkmResultPanel);
                    }
                }
            }
        } catch (ClassCastException e) {
            //do nothing, we dont handle other data in this class
        }

    }

    /**
     * Container class for all available transcription analyses.
     */
    private class AnalysisContainer {

        private final AnalysisTranscriptionStart analysisTSS;
        private final AnalysisOperon analysisOperon;
        private final AnalysisRPKM analysisRPKM;

        /**
         * Container class for all available transcription analyses.
         */

        public AnalysisContainer(AnalysisTranscriptionStart analysisTSS, AnalysisOperon analysisOperon, AnalysisRPKM analysisRPKM) {
            this.analysisTSS = analysisTSS;
            this.analysisOperon = analysisOperon;
            this.analysisRPKM = analysisRPKM;
        }

        /**
         * @return The transcription start site analysis stored in this
         * container
         */
        public AnalysisTranscriptionStart getAnalysisTSS() {
            return analysisTSS;
        }

        /**
         * @return The operon detection stored in this container
         */
        public AnalysisOperon getAnalysisOperon() {
            return analysisOperon;
        }        
        
        public AnalysisRPKM getAnalysisRPKM() {
            return analysisRPKM;
        }
    }
}
