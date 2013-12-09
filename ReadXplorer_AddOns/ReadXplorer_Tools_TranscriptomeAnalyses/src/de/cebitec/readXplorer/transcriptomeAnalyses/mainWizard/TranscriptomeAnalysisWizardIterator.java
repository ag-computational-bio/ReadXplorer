package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.view.dialogMenus.OpenTracksWizardPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

public final class TranscriptomeAnalysisWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    private static final String PROP_WIZARD_NAME = "TransAnalyses";
    // DATA type selection strings
    public static final String PROP_FIVEPRIME_DATASET = "fiveprime";
    public static final String PROP_WHOLEGENOME_DATASET = "wholegenome";
    // Fiveprime data set analyses
    public static final String PROP_TSS_ANALYSIS = "tssAnalysis";
    public static final String PROP_RBS_ANALYSIS = "rbsAnalysis";
    // RBS Analysis properties
    public static final String PROP_RBS_ANALYSIS_BASES_BFORE_I = "numberOfBasesBeforeI";
    public static final String PROP_RBS_ANALYSIS_BASES_AFTER_I = "numberOfBasesAfterI";
    public static final String PROP_RBS_ANALYSIS_WORKINGDIRECTORY_PATH = "pathToWorkinigDir";
    public static final String PROP_PROMOTOR_ANALYSIS = "promotorAnalysis";
    
    public static final String PROP_LEADERLESS_ANALYSIS = "leaderlessAnalysis";
    public static final String PROP_ANTISENSE_ANALYSIS = "antiseseAnalysis";
    // Whole genome data set analyses
    public static final String PROP_NOVEL_ANALYSIS = "novel";
    public static final String PROP_OPERON_ANALYSIS = "operon";
    public static final String PROP_RPKM_ANALYSIS = "rpkm";
    public static final String PROP_LOGRPKM_ANALYSIS = "logRpkm";
    // TSS, Leaderless, Antisense - detection params
    public static final String PROP_Fraction = "fraction";
    public static final String PROP_UPSTREAM = "upstream";
    public static final String PROP_DOWNSTREAM = "downstream";
    public static final String PROP_RATIO = "ratio";
    public static final String PROP_EXCLUDE_INTERNAL_TSS = "excludeInternalTss";
    public static final String PROP_EXCLUDE_TSS_DISTANCE = "excludeTssDistance";
    public static final String PROP_LEADERLESS_LIMIT = "leaderlessLimit";
    public static final String PROP_LEADERLESS_CDSSHIFT = "cdsShiftChoosen";
    public static final String PROP_NORMAL_RPKM_ANALYSIS = "normalRPKMs";
    public static final String PROP_KEEPINTERNAL_DISTANCE = "keepingInternalDistance";
    public static final String PROP_PUTATIVE_UNANNOTATED = "putative unannotated";
    // Wizard descriptors
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> initPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> fivePrimeAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> wholegenomeAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> fivePrimeSelectedAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> wholeGenomeSelectedAnayses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private String[] initPanelsIndex;
    private String[] fivePrimeIndex;
    private String[] wholeGenomeIndex;
    WizardDescriptor wiz;
    private int index;
//    private SelectReadClassWizardPanel readClassPanel;
//    private SelectFeatureTypeWizardPanel featTypePanel;
    private OpenTracksWizardPanel openTracksPanel;
    private ChangeSupport changeSupport;
    private int referenceId;

    public TranscriptomeAnalysisWizardIterator(int referenceId) {
        this.referenceId = referenceId;
        this.changeSupport = new ChangeSupport(this);
        this.initializePanels();
    }

    private void initializePanels() {
        if (allPanels == null) {
            allPanels = new ArrayList<>();
            allPanels.add(new DataSetChoicePanel(PROP_WIZARD_NAME));// 0
            allPanels.add(new WholeTranscriptTracksPanel(PROP_WIZARD_NAME)); // 1
            allPanels.add(new FivePrimeEnrichedTracksPanel(PROP_WIZARD_NAME, referenceId)); // 2
//            allPanels.add(new TssDetectionParamsPanel(PROP_WIZARD_NAME, referenceId)); // 3
//            allPanels.add(new RbsDetectionParamsPanel(PROP_WIZARD_NAME)); // 4
//            allPanels.add(new PromotorDetectionParamPanel(PROP_WIZARD_NAME)); // 5
//            allPanels.add(new LeaderlessDetectionPanel(PROP_WIZARD_NAME)); // 6
            allPanels.add(new AntisenseDetectionParamsPanel(PROP_WIZARD_NAME)); // 7 ,3
            allPanels.add(new NewRegionDetectionParamsPanel(PROP_WIZARD_NAME)); // 8, 4
            allPanels.add(new OperonsDetectionParamsPanel(PROP_WIZARD_NAME)); // 9, 5
            openTracksPanel = new OpenTracksWizardPanel(PROP_WIZARD_NAME, referenceId);
            allPanels.add(openTracksPanel); // 10, 6
//            readClassPanel = new SelectReadClassWizardPanel(PROP_WIZARD_NAME);
//            allPanels.add(readClassPanel); // 11
//            featTypePanel = new SelectFeatureTypeWizardPanel(PROP_WIZARD_NAME);
//            allPanels.add(featTypePanel); // 12


            String[] steps = new String[allPanels.size()];
            for (int i = 0; i < allPanels.size(); i++) {
                Component c = allPanels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
//                    initPanelsIndex = new String[]{steps[0], allPanels.get(1).getComponent().getName(), allPanels.get(2).getComponent().getName(), "..."};
//                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }



            initPanels = new ArrayList<>();
            initPanels.add(this.allPanels.get(6));
            initPanels.add(this.allPanels.get(0));
            initPanels.add(this.allPanels.get(1));

            initPanelsIndex = new String[]{steps[6], steps[0], steps[1], "..."};


            fivePrimeAnalyses = new ArrayList<>();
            fivePrimeAnalyses.add(this.allPanels.get(6));
            fivePrimeAnalyses.add(this.allPanels.get(0));
            fivePrimeAnalyses.add(this.allPanels.get(2));
//            fivePrimeAnalyses.add(this.allPanels.get(3));


            fivePrimeIndex = new String[]{steps[6], steps[0], steps[2], "..."};

            wholegenomeAnalyses = new ArrayList<>();
            wholegenomeAnalyses.add(this.allPanels.get(6));
            wholegenomeAnalyses.add(this.allPanels.get(0));
            wholegenomeAnalyses.add(this.allPanels.get(1));


            wholeGenomeIndex = new String[]{steps[6], steps[0], steps[1], "..."};

            fivePrimeSelectedAnalyses = new ArrayList<>();
            fivePrimeSelectedAnalyses.add(this.allPanels.get(6));
            fivePrimeSelectedAnalyses.add(this.allPanels.get(0));
            fivePrimeSelectedAnalyses.add(this.allPanels.get(2));



            wholeGenomeSelectedAnayses = new ArrayList<>();
            wholeGenomeSelectedAnayses.add(this.allPanels.get(6));
            wholeGenomeSelectedAnayses.add(this.allPanels.get(0));
            wholeGenomeSelectedAnayses.add(this.allPanels.get(1));

            this.currentPanels = initPanels;
            Component c = allPanels.get(6).getComponent();
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, initPanelsIndex);
            }
        }
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return this.currentPanels.get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + this.currentPanels.size();
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

        if (index == 1) { //whole genome dataset
            String[] contentData = initPanelsIndex;

            if ((boolean) wiz.getProperty(PROP_FIVEPRIME_DATASET)) {
                this.currentPanels = this.fivePrimeAnalyses;
                contentData = this.fivePrimeIndex;
            }

            if ((boolean) wiz.getProperty(PROP_WHOLEGENOME_DATASET)) {
                this.currentPanels = this.wholegenomeAnalyses;
                contentData = this.wholeGenomeIndex;
            }

            if (contentData != null) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
        }


        if (index == 2 && (boolean) wiz.getProperty(PROP_FIVEPRIME_DATASET)) { // we are in fifeprime analyses   
            List<String> contentData = new ArrayList<>();
            contentData.add(allPanels.get(6).getComponent().getName());
            contentData.add(allPanels.get(0).getComponent().getName());
            contentData.add(allPanels.get(2).getComponent().getName());
            if ((boolean) wiz.getProperty(PROP_TSS_ANALYSIS) && !fivePrimeSelectedAnalyses.contains(this.allPanels.get(3))) {
//                fivePrimeSelectedAnalyses.add(this.allPanels.get(3));
//                contentData.add(allPanels.get(3).getComponent().getName());
//                fivePrimeSelectedAnalyses.add(this.allPanels.get(6));
//                contentData.add(allPanels.get(6).getComponent().getName());
            }
//            if ((boolean) wiz.getProperty(PROP_ANTISENSE_ANALYSIS) && !fivePrimeSelectedAnalyses.contains(this.allPanels.get(7))) {
//                fivePrimeSelectedAnalyses.add(this.allPanels.get(7));
//                contentData.add(allPanels.get(7).getComponent().getName());
//            }

            this.currentPanels = this.fivePrimeSelectedAnalyses;
            if (!contentData.isEmpty()) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
        }

        if (index == 2 && (boolean) wiz.getProperty(PROP_WHOLEGENOME_DATASET)) { // we are in wholegenome analyses
            List<String> contentData = new ArrayList<>();
            contentData.add(allPanels.get(6).getComponent().getName());
            contentData.add(allPanels.get(0).getComponent().getName());
            contentData.add(allPanels.get(1).getComponent().getName());
            if ((boolean) wiz.getProperty(PROP_NOVEL_ANALYSIS) && !wholeGenomeSelectedAnayses.contains(this.allPanels.get(8))) {
                wholeGenomeSelectedAnayses.add(this.allPanels.get(8));
                contentData.add(allPanels.get(8).getComponent().getName());
            }
            if ((boolean) wiz.getProperty(PROP_OPERON_ANALYSIS) && !wholeGenomeSelectedAnayses.contains(this.allPanels.get(9))) {
                wholeGenomeSelectedAnayses.add(this.allPanels.get(9));
                contentData.add(allPanels.get(9).getComponent().getName());
            }

            this.currentPanels = this.wholeGenomeSelectedAnayses;
            if (!contentData.isEmpty()) {
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
            currentPanels = initPanels;
//            contentData = initPanelsIndex;
            if (contentData != null) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
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

//    /**
//     * @param usingADBTrack true, if the wizard is running on a track stored
//     * completely in the DB, false otherwise.
//     */
//    public void setUsingDBTrack(boolean containsDBTrack) {
//        this.readClassPanel.getComponent().setUsingDBTrack(containsDBTrack);
//    }
    /**
     * @param wiz the wizard, in which this wizard iterator is contained. If it
     * is not set, no properties can be stored, thus it always has to be set.
     */
    public void setWiz(WizardDescriptor wiz) {
        this.wiz = wiz;
    }

    /**
     * @return the wizard, in which this wizard iterator is contained.
     */
    public WizardDescriptor getWiz() {
        return wiz;
    }

//    /**
//     * @return The dynamically generated property name for the read class
//     * selection for this wizard. Can be used to obtain the corresponding read
//     * class parameters.
//     */
//    public String getReadClassPropForWiz() {
//        return this.readClassPanel.getPropReadClassParams();
//    }
//
//    /**
//     * @return The property string for the selected feature type list for the
//     * corresponding wizard.
//     */
//    public String getPropSelectedFeatTypes() {
//        return this.featTypePanel.getPropSelectedFeatTypes();
//    }
    /**
     * @return The list of track selected in this wizard.
     */
    public List<PersistantTrack> getSelectedTracks() {
        return this.openTracksPanel.getComponent().getSelectedTracks();
    }
}
