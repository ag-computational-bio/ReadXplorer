package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.AnalysesHandler;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredFeature;
import de.cebitec.vamp.transcriptionAnalyses.wizard.TranscriptionAnalysesWizardIterator;
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
    private ParameterSetFilteredFeatures parametersFilterFeatures;
    private ParameterSetOperonDet parametersOperonDet;
    private Map<Integer, PersistantTrack> trackMap;
    private Map<Integer, AnalysisContainer> trackToAnalysisMap;
    private ResultPanelTranscriptionStart transcriptionStartResultPanel;
    private ResultPanelFilteredFeatures filteredFeatureResultPanel;
    private ResultPanelOperonDetection operonResultPanel;
    private boolean performTSSAnalysis;
    private boolean performFilterAnalysis;
    private boolean performOperonAnalysis;
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

    /**
     * Action for opening a new transcription analyses frame. It opens a track
     * list containing all tracks of the selected reference and creates a new
     * transcription analyses frame when a track was chosen.
     *
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
     *
     * @param trackIds the list of track ids for which the transcription
     * analyses have to be carried out
     */
    private void runWizardAndTranscriptionAnalysis() {
        @SuppressWarnings("unchecked")
        TranscriptionAnalysesWizardIterator transWizardIterator = new TranscriptionAnalysesWizardIterator();
        boolean containsDBTrack = PersistantTrack.checkForDBTrack(this.tracks);
        transWizardIterator.setUsingDBTrack(containsDBTrack);
        this.readClassPropString = transWizardIterator.getReadClassPropForWiz();
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
    private void startTransciptionAnalyses(WizardDescriptor wiz) {

        //obtain all analysis parameters
        performTSSAnalysis = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_TSS_ANALYSIS);
        performFilterAnalysis = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_FILTER_ANALYSIS);
        performOperonAnalysis = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_OPERON_ANALYSIS);
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
        if (performFilterAnalysis) {
            minNumberReads = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS);
            maxNumberReads = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS);
        }
        if (performOperonAnalysis) {
            autoOperonParamEstimation = (boolean) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_AUTO_OPERON_PARAMS);
            minSpanningReads = (int) wiz.getProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_SPANNING_READS);
        }
        //create parameter set for each analysis
        parametersTss = new ParameterSetTSS(performTSSAnalysis, autoTssParamEstimation, performUnannotatedTranscriptDet,
                minTotalIncrease, minPercentIncrease, maxLowCovInitCount, minLowCovIncrease, minTranscriptExtensionCov);
        parametersFilterFeatures = new ParameterSetFilteredFeatures(performFilterAnalysis, minNumberReads, maxNumberReads);
        parametersOperonDet = new ParameterSetOperonDet(performOperonAnalysis, minSpanningReads, autoOperonParamEstimation);


        TrackConnector connector;
        for (PersistantTrack track : this.tracks) {
            AnalysisTranscriptionStart analysisTSS = null;
            AnalysisFilterFeatures analysisFilteredGenes = null;
            AnalysisOperon analysisOperon = null;
            //TODO: Error handling
            try {
                connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
            } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                return;
            }
            AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler(this,
                    NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "MSG_AnalysesWorker.progress.name"), readClassesParams); //every track has its own analysis handlers
            AnalysesHandler mappingAnalysisHandler = connector.createAnalysisHandler(this,
                    NbBundle.getMessage(OpenTranscriptionAnalysesAction.class, "MSG_AnalysesWorker.progress.name"), readClassesParams);

            if (parametersTss.isPerformTSSAnalysis()) {

                if (parametersTss.isPerformUnannotatedTranscriptDet()) {
                    analysisTSS = new AnalysisUnannotatedTransStart(connector, parametersTss.getMinTotalIncrease(),
                            parametersTss.getMinPercentIncrease(), parametersTss.getMaxLowCovInitCount(), parametersTss.getMinLowCovIncrease(),
                            parametersTss.isAutoTssParamEstimation(), parametersTss.getMinTranscriptExtensionCov());
                } else {
                    analysisTSS = new AnalysisTranscriptionStart(connector, parametersTss.getMinTotalIncrease(),
                            parametersTss.getMinPercentIncrease(), parametersTss.getMaxLowCovInitCount(), parametersTss.getMinLowCovIncrease(),
                            parametersTss.isAutoTssParamEstimation());
                }
                covAnalysisHandler.registerObserver(analysisTSS);
                covAnalysisHandler.setCoverageNeeded(true);
            }
            if (parametersFilterFeatures.isPerformFilterAnalysis()) {
                analysisFilteredGenes = new AnalysisFilterFeatures(connector, parametersFilterFeatures.getMinNumberReads(), parametersFilterFeatures.getMaxNumberReads());

                mappingAnalysisHandler.registerObserver(analysisFilteredGenes);
                mappingAnalysisHandler.setMappingsNeeded(true);
            }
            if (parametersOperonDet.isPerformOperonAnalysis()) {
                analysisOperon = new AnalysisOperon(connector, parametersOperonDet.getMinSpanningReads(), parametersOperonDet.isAutoOperonParamEstimation());

                mappingAnalysisHandler.registerObserver(analysisOperon);
                mappingAnalysisHandler.setMappingsNeeded(true);
            }

            trackToAnalysisMap.put(track.getId(), new AnalysisContainer(analysisTSS, analysisFilteredGenes, analysisOperon));
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
                parametersTss.setMinTotalIncrease(analysisTSS.getIncreaseReadCount()); //if automatic is on, the parameters are different now
                parametersTss.setMinPercentIncrease(analysisTSS.getIncreaseReadPercent());
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

                if (parametersFilterFeatures.isPerformFilterAnalysis()) {

                    AnalysisFilterFeatures filterFeatureAnalysis = trackToAnalysisMap.get(trackId).getAnalysisFilteredFeatures();
                    List<FilteredFeature> filteredGenes = filterFeatureAnalysis.getResults();


                    if (filteredFeatureResultPanel == null) {
                        filteredFeatureResultPanel = new ResultPanelFilteredFeatures();
                        filteredFeatureResultPanel.setBoundsInfoManager(this.refViewer.getBoundsInformationManager());
                    }

                    FilteredFeaturesResult filteredFeatResult = new FilteredFeaturesResult(trackMap, filteredGenes);
                    filteredFeatResult.setParameters(parametersFilterFeatures);
                    filteredFeatResult.setNoGenomeFeatures(filterFeatureAnalysis.getNoGenomeFeatures());
                    filteredFeatureResultPanel.addFilteredFeatures(filteredFeatResult);

                    if (finishedMappingAnalyses >= tracks.size()) {
                        trackNames = GeneralUtils.generateConcatenatedString(filteredFeatResult.getTrackNameList(), 120);
                        String panelName = "Filtered features for " + trackNames + " (" + filteredFeatureResultPanel.getResultSize() + " hits)";
                        this.transcAnalysesTopComp.openAnalysisTab(panelName, filteredFeatureResultPanel);
                    }

                    //TODO: prozentualer increase
                    //feature finden/ändern

                }
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
        private final AnalysisFilterFeatures analysisFilteredGenes;
        private final AnalysisOperon analysisOperon;

        /**
         * Container class for all available transcription analyses.
         */
        public AnalysisContainer(AnalysisTranscriptionStart analysisTSS, AnalysisFilterFeatures analysisFilteredGenes, AnalysisOperon analysisOperon) {
            this.analysisTSS = analysisTSS;
            this.analysisFilteredGenes = analysisFilteredGenes;
            this.analysisOperon = analysisOperon;
        }

        /**
         * @return The transcription start site analysis stored in this
         * container
         */
        public AnalysisTranscriptionStart getAnalysisTSS() {
            return analysisTSS;
        }

        /**
         * @return The filter features analysis stored in this container
         */
        public AnalysisFilterFeatures getAnalysisFilteredFeatures() {
            return analysisFilteredGenes;
        }

        /**
         * @return The operon detection stored in this container
         */
        public AnalysisOperon getAnalysisOperon() {
            return analysisOperon;
        }
    }
}
