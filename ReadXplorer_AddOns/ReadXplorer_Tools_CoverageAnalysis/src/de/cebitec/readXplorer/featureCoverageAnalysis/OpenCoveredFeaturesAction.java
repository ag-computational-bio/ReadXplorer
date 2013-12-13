package de.cebitec.readXplorer.featureCoverageAnalysis;

import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.OpenTracksVisualPanel;
import de.cebitec.readXplorer.view.dialogMenus.OpenTracksWizardPanel;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import de.cebitec.readXplorer.view.dialogMenus.SelectFeatureTypeWizardPanel;
import de.cebitec.readXplorer.view.dialogMenus.SelectReadClassWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
 * Action for opening the feature coverage analysis. It opens the wizard and
 * runs the analysis after successfully finishing the wizard.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID(
        category = "Tools",
        id = "de.cebitec.readXplorer.genomeAnalyses.OpenCoveredFeaturesAction")
@ActionRegistration(
        iconBase = "de/cebitec/readXplorer/featureCoverageAnalysis/coveredFeatures.png",
        displayName = "#CTL_OpenCoveredFeaturesAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 146),
    @ActionReference(path = "Toolbars/Tools", position = 231)
})
@Messages("CTL_OpenCoveredFeaturesAction=Feature Coverage Analysis")
public final class OpenCoveredFeaturesAction implements ActionListener, DataVisualisationI {

    private static final String PROP_WIZARD_NAME = "FeatureCoverageWiz";
    
    private final ReferenceViewer context;
    private int referenceId;
    private List<PersistantTrack> tracks;
    private CoveredFeaturesAnalysisTopComponent coveredAnnoAnalysisTopComp;
    private Map<Integer, AnalysisCoveredFeatures> trackToAnalysisMap;
    private int finishedCovAnalyses = 0;
    private ResultPanelCoveredFeatures coveredFeaturesResultPanel;
    ParameterSetCoveredFeatures parameters;
    private boolean combineTracks;
    private HashMap<Integer, PersistantTrack> trackMap;
    private OpenTracksWizardPanel openTracksWizPanel;
    private SelectReadClassWizardPanel readClassWizPanel;
    private SelectFeatureTypeWizardPanel featTypeWizPanel; 

    /**
     * Action for opening the feature coverage analysis. It opens the wizard and
     * runs the analysis after successfully finishing the wizard.
     */
    public OpenCoveredFeaturesAction(ReferenceViewer context) {
        this.context = context;
        this.referenceId = this.context.getReference().getId();
        this.trackToAnalysisMap = new HashMap<>();
    }

    /**
     * Carries out the logic behind the feature coverage analysis action. This
     * means, it opens a configuration wizard and starts the analysis after
     * successfully finishing the wizard.
     * @param ev the event, which is currently not used
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        this.runWizarAndCoveredAnnoAnalsysis();
    }

    /**
     * Runs the wizard and starts the feature coverage anaylsis, if the wizard
     * finished successfully.
     */
    private void runWizarAndCoveredAnnoAnalsysis() {

        @SuppressWarnings("unchecked")
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        this.openTracksWizPanel = new OpenTracksWizardPanel(PROP_WIZARD_NAME, referenceId);
        this.readClassWizPanel = new SelectReadClassWizardPanel(PROP_WIZARD_NAME);
        this.featTypeWizPanel = new SelectFeatureTypeWizardPanel(PROP_WIZARD_NAME);
        this.openTracksWizPanel.setReadClassVisualPanel(readClassWizPanel.getComponent());
        panels.add(openTracksWizPanel);
        panels.add(new CoveredFeaturesWizardPanel());
        panels.add(readClassWizPanel);
        panels.add(featTypeWizPanel);
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(OpenCoveredFeaturesAction.class, "TTL_CoveredFeaturesWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            OpenTracksVisualPanel tracksVisPanel = openTracksWizPanel.getComponent();
            if (!tracksVisPanel.getAllMarkedNodes().isEmpty()) {
                this.tracks = tracksVisPanel.getSelectedTracks();
                this.trackMap = new HashMap<>();
                this.trackToAnalysisMap = new HashMap<>();
                this.finishedCovAnalyses = 0;
                this.coveredFeaturesResultPanel = null;
                for (PersistantTrack track : this.tracks) {
                    this.trackMap.put(track.getId(), track);
                }

                if (this.coveredAnnoAnalysisTopComp == null) {
                    this.coveredAnnoAnalysisTopComp = (CoveredFeaturesAnalysisTopComponent) WindowManager.getDefault().findTopComponent("CoveredFeaturesAnalysisTopComponent");
                }
                this.coveredAnnoAnalysisTopComp.open();
                this.startCoveredFeaturesDetection(wiz);

            } else {
                String msg = NbBundle.getMessage(OpenCoveredFeaturesAction.class, "CTL_OpenCoveredFeaturesDetectionInfo",
                        "No track selected. To start a feature coverage analysis at least one track has to be selected.");
                String title = NbBundle.getMessage(OpenCoveredFeaturesAction.class, "CTL_OpenCoveredFeaturesDetectionInfoTitle", "Info");
                JOptionPane.showMessageDialog(this.context, msg, title, JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Starts the feature coverage analysis.
     * @param wiz the wizard containing the feature coverage analysis parameters
     */
    private void startCoveredFeaturesDetection(WizardDescriptor wiz) {
        //get parameters
        boolean getCoveredFeatures = (boolean) wiz.getProperty(CoveredFeaturesWizardPanel.PROP_GET_COVERED_FEATURES);
        int minCoveredPercent = (int) wiz.getProperty(CoveredFeaturesWizardPanel.PROP_MIN_COVERED_PERCENT);
        int minCoverageCount = (int) wiz.getProperty(CoveredFeaturesWizardPanel.PROP_MIN_COVERAGE_COUNT);
        boolean whateverStrand = (boolean) wiz.getProperty(CoveredFeaturesWizardPanel.PROP_WHATEVER_STRAND);
        ParametersReadClasses readClassesParams = (ParametersReadClasses) wiz.getProperty(readClassWizPanel.getPropReadClassParams());
        this.combineTracks = (boolean) wiz.getProperty(openTracksWizPanel.getPropCombineTracks());
        @SuppressWarnings("unchecked")
        Set<FeatureType> selFeatureTypes = (Set<FeatureType>) wiz.getProperty(featTypeWizPanel.getPropSelectedFeatTypes());
        parameters = new ParameterSetCoveredFeatures(minCoveredPercent, minCoverageCount, 
                whateverStrand, getCoveredFeatures, readClassesParams, selFeatureTypes);

        TrackConnector connector;
        if (!combineTracks) {
            for (PersistantTrack track : this.tracks) {
                try {
                    connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
                } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                    JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
                    continue;
                }

                //every track has its own analysis handlers
                this.createAnalysis(connector, readClassesParams);
            }
        } else {
            try {
                connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(tracks, combineTracks);
                this.createAnalysis(connector, readClassesParams);  //every track has its own analysis handlers

            } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Creates the analysis for a TrackConnector.
     *
     * @param connector the connector
     * @param readClassesParams the read class parameters
     */
    private void createAnalysis(TrackConnector connector, ParametersReadClasses readClassesParams) {
        AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler(this,
                NbBundle.getMessage(OpenCoveredFeaturesAction.class, "MSG_AnalysesWorker.progress.name"), readClassesParams); //every track has its own analysis handlers
        AnalysisCoveredFeatures analysisCoveredFeatures = new AnalysisCoveredFeatures(connector, parameters);
        covAnalysisHandler.registerObserver(analysisCoveredFeatures);
        covAnalysisHandler.setCoverageNeeded(true);

        trackToAnalysisMap.put(connector.getTrackID(), analysisCoveredFeatures);
        covAnalysisHandler.startAnalysis();
    }

    /**
     * Shows the results in the corresponding top component.
     * @param dataTypeObject the pair describing the result data. It has to 
     * consist of the track id as the first element and the data type string
     * as the second element.
     */
    @Override
    public void showData(Object dataTypeObject) {
        try {
            @SuppressWarnings("unchecked")
            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) dataTypeObject;
            int trackId = dataTypePair.getFirst();
            String dataType = dataTypePair.getSecond();

            if (dataType.equals(AnalysesHandler.DATA_TYPE_COVERAGE)) {

                ++finishedCovAnalyses;

                AnalysisCoveredFeatures analysisCoveredFeatures = trackToAnalysisMap.get(trackId);
                final CoveredFeatureResult result = new CoveredFeatureResult(analysisCoveredFeatures.getResults(), 
                        trackMap, referenceId, combineTracks);
                result.setParameters(parameters);
                Map<String, Integer> statsMap = new HashMap<>();
                statsMap.put(ResultPanelCoveredFeatures.FEATURES_TOTAL, analysisCoveredFeatures.getNoGenomeFeatures());
                result.setStatsMap(statsMap);

                SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
                    @Override
                    public void run() {

                        if (coveredFeaturesResultPanel == null) {
                            coveredFeaturesResultPanel = new ResultPanelCoveredFeatures();
                            coveredFeaturesResultPanel.setBoundsInfoManager(context.getBoundsInformationManager());
                        }
                        coveredFeaturesResultPanel.addCoveredFeatures(result);

                        if (finishedCovAnalyses >= tracks.size() || combineTracks) {

                            //get track name(s) for tab descriptions
                            String trackNames = "";
                            if (tracks != null && !tracks.isEmpty()) {
                                for (PersistantTrack track : tracks) {
                                    trackNames = trackNames.concat(track.getDescription()).concat(" and ");
                                }
                                trackNames = trackNames.substring(0, trackNames.length() - 5);
                                if (trackNames.length() > 120) {
                                    trackNames = trackNames.substring(0, 120).concat("...");
                                }
                            }

                            String title;
                            if (parameters.isGetCoveredFeatures()) {
                                title = "Detected covered features for ";
                            } else {
                                title = "Detected uncovered features for ";
                            }
                            String panelName = title + trackNames + " (" + coveredFeaturesResultPanel.getResultSize() + " hits)";
                            coveredAnnoAnalysisTopComp.openAnalysisTab(panelName, coveredFeaturesResultPanel);
                        }
                    }
                });
            }

        } catch (ClassCastException e) {
            //do nothing, we dont handle other data in this class
        }
    }
}
