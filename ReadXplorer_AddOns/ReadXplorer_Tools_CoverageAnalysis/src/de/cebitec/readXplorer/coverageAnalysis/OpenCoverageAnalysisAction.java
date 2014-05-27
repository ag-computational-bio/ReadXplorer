/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.coverageAnalysis;

import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.OpenTracksWizardPanel;
import de.cebitec.readXplorer.view.dialogMenus.SelectReadClassWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Action for opening the coverage analysis. It opens the wizard and runs the
 * analysis after successfully finishing the wizard.
 *
 * @author Tobias Zimmermann, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */

@ActionID(
        category = "Tools",
        id = "CoverageAnalysis.OpenCoverageAnalysisAction")
@ActionRegistration(
        iconBase = "de/cebitec/readXplorer/coverageAnalysis/coveredIntervals.png",
        displayName = "#CTL_OpenCoverageAnalysisAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 149),
    @ActionReference(path = "Toolbars/Tools", position = 240)
})
@Messages("CTL_OpenCoverageAnalysisAction=Coverage Analysis")
public final class OpenCoverageAnalysisAction implements ActionListener, DataVisualisationI {

    private static final String PROP_WIZARD_NAME = "CoverageAnalysisWiz";
    private final ReferenceViewer context;
    private int referenceId;
    private List<PersistantTrack> tracks;
    private Map<Integer, AnalysisCoverage> trackToAnalysisMap;
    private ParameterSetCoverageAnalysis parameters;
    private boolean combineTracks;
    private SelectReadClassWizardPanel readClassWizPanel;
    private OpenTracksWizardPanel openTracksPanel;
    private CoverageAnalysisTopComponent coveredAnnoAnalysisTopComp;
    private Map<Integer, PersistantTrack> trackMap;
    private int finishedCovAnalyses = 0;
    private ResultPanelCoverageAnalysis coverageAnalysisResultPanel;

    /**
     * Action for opening the feature coverage analysis. It opens the wizard and
     * runs the analysis after successfully finishing the wizard.
     * @param context the currently active reference viewer
     */
    public OpenCoverageAnalysisAction(ReferenceViewer context) {
        this.context = context;
        this.referenceId = this.context.getReference().getId();
        this.trackToAnalysisMap = new HashMap<>();
    }

    /**
     * Carries out the logic behind the coverage analysis action. This means, it
     * opens a configuration wizard and starts the analysis after successfully
     * finishing the wizard.
     * @param ev the event, which is currently not used
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        this.runWizarAndCoverageAnnoAnalysis();
    }

    /**
     * Runs the wizard and starts the feature coverage anaylsis, if the wizard
     * finished successfully.
     */
    private void runWizarAndCoverageAnnoAnalysis() {

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        this.openTracksPanel = new OpenTracksWizardPanel(PROP_WIZARD_NAME, referenceId);
        this.readClassWizPanel = new SelectReadClassWizardPanel(PROP_WIZARD_NAME);
        this.openTracksPanel.setReadClassVisualPanel(readClassWizPanel.getComponent());
        panels.add(openTracksPanel);
        panels.add(new CoverageAnalysisWizardPanel());
        panels.add(readClassWizPanel);
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(OpenCoverageAnalysisAction.class, "CoverageAnalysisAction_Title"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        List<PersistantTrack> selectedTracks = openTracksPanel.getComponent().getSelectedTracks();
        if (!cancelled && !selectedTracks.isEmpty()) {
            this.tracks = selectedTracks;
            this.trackMap = ProjectConnector.getTrackMap(tracks);
            this.trackToAnalysisMap = new HashMap<>();
            this.finishedCovAnalyses = 0;
            this.coverageAnalysisResultPanel = null;

            if (this.coveredAnnoAnalysisTopComp == null) {
                this.coveredAnnoAnalysisTopComp = (CoverageAnalysisTopComponent) WindowManager.getDefault().findTopComponent("CoverageAnalysisTopComponent");
            }
            this.coveredAnnoAnalysisTopComp.open();
            this.startCoverageDetection(wiz);
        }
    }

    /**
     * Starts the feature coverage analysis.
     * @param wiz the wizard containing the coverage analysis parameters
     */
    private void startCoverageDetection(WizardDescriptor wiz) {
        //get parameters
        int minCoverageCount = (int) wiz.getProperty(CoverageAnalysisWizardPanel.MIN_COVERAGE_COUNT);
        boolean isSumCoverage = (boolean) wiz.getProperty(CoverageAnalysisWizardPanel.SUM_COVERAGE);
        boolean detectCoveredIntervals = (boolean) wiz.getProperty(CoverageAnalysisWizardPanel.COVERED_INTERVALS);
        ParametersReadClasses readClassesParams = (ParametersReadClasses) wiz.getProperty(readClassWizPanel.getPropReadClassParams());
        this.combineTracks = (boolean) wiz.getProperty(openTracksPanel.getPropCombineTracks());

        parameters = new ParameterSetCoverageAnalysis(minCoverageCount, isSumCoverage, detectCoveredIntervals, readClassesParams);

        TrackConnector connector;
        if (!combineTracks) {
            for (PersistantTrack track : this.tracks) {
                try {
                    connector = (new SaveFileFetcherForGUI()).getTrackConnector(track);
                } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                    SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                    continue;
                }

                this.createAnalysis(connector, readClassesParams);
            }
        } else {
            try {
                connector = (new SaveFileFetcherForGUI()).getTrackConnector(tracks, combineTracks);
                this.createAnalysis(connector, readClassesParams);
                
            } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                SaveFileFetcherForGUI.showPathSelectionErrorMsg();
            }
        }
    }
    
    /**
     * Creates the analysis for a TrackConnector.
     * @param connector the connector
     * @param readClassesParams the read class parameters
     */
    private void createAnalysis(TrackConnector connector, ParametersReadClasses readClassesParams) {
        AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler(this,
                NbBundle.getMessage(OpenCoverageAnalysisAction.class, "MSG_AnalysesWorker.progress.name"), readClassesParams);

        AnalysisCoverage analysisCoverage = new AnalysisCoverage(connector, parameters);
        covAnalysisHandler.registerObserver(analysisCoverage);
        covAnalysisHandler.setCoverageNeeded(true);
        trackToAnalysisMap.put(connector.getTrackID(), analysisCoverage);
        covAnalysisHandler.startAnalysis();
    }

    /**
     * Shows the results in the corresponding top component.
     * @param dataTypeObject the pair describing the result data. It has to
     * consist of the track id as the first element and the data type string as
     * the second element.
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

                AnalysisCoverage analysisCoverage = trackToAnalysisMap.get(trackId);
                analysisCoverage.finishAnalysis();
                final CoverageAnalysisResult result = new CoverageAnalysisResult(analysisCoverage.getResults(), trackMap, referenceId, combineTracks);
           
                result.setParameters(parameters);
                
                SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
                    @Override
                    public void run() {

                        if (coverageAnalysisResultPanel == null) {
                            coverageAnalysisResultPanel = new ResultPanelCoverageAnalysis();
                            coverageAnalysisResultPanel.setBoundsInfoManager(context.getBoundsInformationManager());
                        }
                        coverageAnalysisResultPanel.addCoverageAnalysis(result);

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
                            if (parameters.isDetectCoveredIntervals()) {
                                title = "Detected covered intervals for ";
                            } else {
                                title = "Detected uncovered intervals for ";
                            }
                            String panelName = title + trackNames + " (" + coverageAnalysisResultPanel.getResultSize() + " hits)";
                            coveredAnnoAnalysisTopComp.openAnalysisTab(panelName, coverageAnalysisResultPanel);
                        }
                    }
                });
            }

        } catch (ClassCastException e) {
            //do nothing, we dont handle other data in this class
        }

    }
}
