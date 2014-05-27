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
package de.cebitec.readXplorer.differentialExpression.wizard;

import de.cebitec.readXplorer.api.cookies.LoginCookie;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.differentialExpression.BaySeqAnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.DeAnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.DeSeq2AnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.DeSeqAnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.DiffExpResultViewerTopComponent;
import de.cebitec.readXplorer.differentialExpression.ExportOnlyAnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.ExpressTestAnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.GnuR;
import de.cebitec.readXplorer.differentialExpression.Group;
import de.cebitec.readXplorer.differentialExpression.ProcessingLog;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.view.dialogMenus.SelectReadClassWizardPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(category = "Tools", id = "de.cebitec.readXplorer.differentialExpression.DiffExpWizardAction")
@ActionRegistration(displayName = "Differential Gene Expression Analysis")
@ActionReference(path = "Menu/Tools", position = 10)
public final class WizardIterator implements WizardDescriptor.Iterator<WizardDescriptor>, ActionListener {

    private final LoginCookie context;

    public WizardIterator(LoginCookie context) {
        this.context = context;
    }
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> baySeqPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> deSeqTwoCondsPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> deSeqMoreCondsPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> expressTestPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> exportOnlyPanels;
    private String[] deSeqIndex;
    private String[] baySeqIndex;
    private String[] deSeqTwoCondsIndex;
    private String[] deSeqMoreCondsIndex;
    private String[] expressTestIndex;
    private String[] exportOnlyIndex;
    private String[] initialSteps;
    private DeAnalysisHandler.Tool tool;
    private WizardDescriptor wiz;
    private SelectReadClassWizardPanel readClassPanel;

    /**
     * Action, which is performed, when this wizard shall be opened.
     *
     * @param e
     */
    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        initializePanels();
        wiz = new WizardDescriptor(this);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
        wiz.setTitle("Differential Gene Expression Analysis");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            ParametersReadClasses readClassParams = (ParametersReadClasses) wiz.getProperty(this.readClassPanel.getPropReadClassParams());
            List<Group> createdGroups = (List<Group>) wiz.getProperty("createdGroups");
            List<PersistantTrack> selectedTracks = (List<PersistantTrack>) wiz.getProperty("tracks");
            Integer genomeID = (Integer) wiz.getProperty("genomeID");
            int[] replicateStructure = (int[]) wiz.getProperty("replicateStructure");
            File saveFile = (File) wiz.getProperty("saveFile");
            Map<String, String[]> design = (Map<String, String[]>) wiz.getProperty("design");
            Set<FeatureType> featureTypes = (Set<FeatureType>) wiz.getProperty("featureType");
            Integer startOffset = (Integer) wiz.getProperty("startOffset");
            Integer stopOffset = (Integer) wiz.getProperty("stopOffset");
            boolean regardReadOrientation = (boolean) wiz.getProperty("regardReadOrientation");
            DeAnalysisHandler handler = null;

            if (tool == DeAnalysisHandler.Tool.BaySeq) {
                UUID key = GnuR.SecureGnuRInitiliser.reserveGnuRinstance();
                handler = new BaySeqAnalysisHandler(selectedTracks, createdGroups, genomeID,
                        replicateStructure, saveFile, featureTypes, startOffset, stopOffset, readClassParams, regardReadOrientation, key);
            } else if (tool == DeAnalysisHandler.Tool.DeSeq || tool == DeAnalysisHandler.Tool.DeSeq2) {
                UUID key = GnuR.SecureGnuRInitiliser.reserveGnuRinstance();
                boolean moreThanTwoConditions = (boolean) wiz.getProperty("moreThanTwoConditions");
                boolean workingWithoutReplicates = (boolean) wiz.getProperty("workingWithoutReplicates");

                List<String> fittingGroupOne = null;
                List<String> fittingGroupTwo = null;
                if (moreThanTwoConditions) {
                    fittingGroupOne = (List<String>) wiz.getProperty("fittingGroupOne");
                    fittingGroupTwo = (List<String>) wiz.getProperty("fittingGroupTwo");
                }
                if (tool == DeAnalysisHandler.Tool.DeSeq) {
                    handler = new DeSeqAnalysisHandler(selectedTracks, design, moreThanTwoConditions, fittingGroupOne,
                            fittingGroupTwo, genomeID, workingWithoutReplicates,
                            saveFile, featureTypes, startOffset, stopOffset, readClassParams, regardReadOrientation, key);
                } else {
                    handler = new DeSeq2AnalysisHandler(selectedTracks, design, moreThanTwoConditions, fittingGroupOne,
                            fittingGroupTwo, genomeID, workingWithoutReplicates,
                            saveFile, featureTypes, startOffset, stopOffset, readClassParams, regardReadOrientation, key);
                }
            } else if (tool == DeAnalysisHandler.Tool.ExpressTest) {
                List<Integer> groupAList = (List<Integer>) wiz.getProperty("groupA");
                boolean workingWithoutReplicates = (boolean) wiz.getProperty("workingWithoutReplicates");
                int[] groupA = new int[groupAList.size()];
                for (int i = 0; i < groupA.length; i++) {
                    groupA[i] = groupAList.get(i);
                }

                List<Integer> groupBList = (List<Integer>) wiz.getProperty("groupB");
                int[] groupB = new int[groupBList.size()];
                for (int i = 0; i < groupB.length; i++) {
                    groupB[i] = groupBList.get(i);
                }

                boolean useHouseKeepingGenesToNormalize = (boolean) wiz.getProperty("useHouseKeepingGenesToNormalize");
                List<Integer> normalizationFeatures = null;
                if (useHouseKeepingGenesToNormalize) {
                    normalizationFeatures = (List<Integer>) wiz.getProperty("normalizationFeatures");
                }

                handler = new ExpressTestAnalysisHandler(selectedTracks, groupA, groupB, genomeID,
                        workingWithoutReplicates, saveFile, featureTypes, startOffset, stopOffset,
                        readClassParams, regardReadOrientation, normalizationFeatures);
            } else if (tool == DeAnalysisHandler.Tool.ExportCountTable) {
                handler = new ExportOnlyAnalysisHandler(selectedTracks, genomeID, saveFile, featureTypes, startOffset, stopOffset, readClassParams, regardReadOrientation);
            }

            DiffExpResultViewerTopComponent diffExpResultViewerTopComponent = new DiffExpResultViewerTopComponent(handler, tool);
            diffExpResultViewerTopComponent.open();
            diffExpResultViewerTopComponent.requestActive();
            handler.registerObserver(diffExpResultViewerTopComponent);
            ProcessingLog.getInstance().setProperties(wiz.getProperties());
            handler.start();
        }
    }

    private void initializePanels() {
        if (allPanels == null) {
            this.readClassPanel = new SelectReadClassWizardPanel("DiffExprWiz");
            allPanels = new ArrayList<>();
            allPanels.add(new ChooseWizardPanel());
            allPanels.add(new DeSeqWizardPanel1());
            allPanels.add(new SelectTrackWizardPanel());
            allPanels.add(new BaySeqWizardPanel2());
            allPanels.add(new BaySeqWizardPanel3());
            allPanels.add(new DeSeqWizardPanelDesign());
            allPanels.add(new DeSeqWizardPanelFit());
            allPanels.add(new DeSeqWizardPanelConds());
            allPanels.add(new GeneralSettingsWizardPanel());
            allPanels.add(new ExpressTestWizardPanelNormalization());
            allPanels.add(readClassPanel);
            allPanels.add(new StartAnalysisWizardPanel());
            String[] steps = new String[allPanels.size()];
            for (int i = 0; i < allPanels.size(); i++) {
                Component c = allPanels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    initialSteps = new String[]{steps[0], "..."};
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, initialSteps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
            baySeqPanels = new ArrayList<>();
            baySeqPanels.add(allPanels.get(0));
            baySeqPanels.add(allPanels.get(2));
            baySeqPanels.add(allPanels.get(3));
            baySeqPanels.add(allPanels.get(4));
            baySeqPanels.add(allPanels.get(8));
            baySeqPanels.add(allPanels.get(10));
            baySeqPanels.add(allPanels.get(11));
            baySeqIndex = new String[]{steps[0], steps[2], steps[3], steps[4], steps[8], steps[10], steps[11]};

            deSeqIndex = new String[]{steps[0], steps[1], "..."};

            deSeqTwoCondsPanels = new ArrayList<>();
            deSeqTwoCondsPanels.add(allPanels.get(0));
            deSeqTwoCondsPanels.add(allPanels.get(1));
            deSeqTwoCondsPanels.add(allPanels.get(2));
            deSeqTwoCondsPanels.add(allPanels.get(7));
            deSeqTwoCondsPanels.add(allPanels.get(8));
            deSeqTwoCondsPanels.add(allPanels.get(10));
            deSeqTwoCondsPanels.add(allPanels.get(11));
            deSeqTwoCondsIndex = new String[]{steps[0], steps[1], steps[2], steps[7], steps[8], steps[10], steps[11]};

            deSeqMoreCondsPanels = new ArrayList<>();
            deSeqMoreCondsPanels.add(allPanels.get(0));
            deSeqMoreCondsPanels.add(allPanels.get(1));
            deSeqMoreCondsPanels.add(allPanels.get(2));
            deSeqMoreCondsPanels.add(allPanels.get(5));
            deSeqMoreCondsPanels.add(allPanels.get(6));
            deSeqMoreCondsPanels.add(allPanels.get(8));
            deSeqMoreCondsPanels.add(allPanels.get(10));
            deSeqMoreCondsPanels.add(allPanels.get(11));
            deSeqMoreCondsIndex = new String[]{steps[0], steps[1], steps[2], steps[5], steps[6], steps[8], steps[10], steps[11]};

            expressTestPanels = new ArrayList<>();
            expressTestPanels.add(allPanels.get(0));
            expressTestPanels.add(allPanels.get(2));
            expressTestPanels.add(allPanels.get(7));
            expressTestPanels.add(allPanels.get(8));
            expressTestPanels.add(allPanels.get(9));
            expressTestPanels.add(allPanels.get(10));
            expressTestPanels.add(allPanels.get(11));
            expressTestIndex = new String[]{steps[0], steps[2], steps[7], steps[8], steps[9], steps[10], steps[11]};

            exportOnlyPanels = new ArrayList<>();
            exportOnlyPanels.add(allPanels.get(0));
            exportOnlyPanels.add(allPanels.get(2));
            exportOnlyPanels.add(allPanels.get(8));
            exportOnlyPanels.add(allPanels.get(10));
            exportOnlyIndex = new String[]{steps[0], steps[2], steps[8], steps[10]};

            currentPanels = baySeqPanels;
        }
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return currentPanels.get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + currentPanels.size();
    }

    @Override
    public boolean hasNext() {
        return index < currentPanels.size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (index == 0) {
            String[] contentData = null;
            tool = (DeAnalysisHandler.Tool) wiz.getProperty("tool");
            if (tool == DeAnalysisHandler.Tool.DeSeq || tool == DeAnalysisHandler.Tool.DeSeq2) {
                currentPanels = deSeqTwoCondsPanels;
                contentData = deSeqIndex;
            }
            if (tool == DeAnalysisHandler.Tool.BaySeq) {
                currentPanels = baySeqPanels;
                contentData = baySeqIndex;
            }
            if (tool == DeAnalysisHandler.Tool.ExpressTest) {
                currentPanels = expressTestPanels;
                contentData = expressTestIndex;
            }
            if (tool == DeAnalysisHandler.Tool.ExportCountTable) {
                currentPanels = exportOnlyPanels;
                contentData = exportOnlyIndex;
            }

            if (contentData != null) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
        }
        if ((index == 1) && (tool == DeAnalysisHandler.Tool.DeSeq)) {
            String[] contentData;
            boolean moreThanTwoConditions = (boolean) wiz.getProperty("moreThanTwoConditions");
            if (moreThanTwoConditions) {
                currentPanels = deSeqMoreCondsPanels;
                contentData = deSeqMoreCondsIndex;
            } else {
                contentData = deSeqTwoCondsIndex;
            }
            if (contentData != null) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
        }
        index++;
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        String[] contentData = null;
        if (index == 1) {
            contentData = initialSteps;
        }
        if ((index == 2) && (tool == DeAnalysisHandler.Tool.DeSeq)) {
            contentData = deSeqIndex;
        }
        if (contentData != null) {
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
        }
        index--;
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
   
    /**
     * @return The dynamically generated property name for the read class
     * selection for this wizard. Can be used to obtain the corresponding read
     * class parameters.
     */
    public String getReadClassPropForWiz() {
        return this.readClassPanel.getPropReadClassParams();
    }
}
