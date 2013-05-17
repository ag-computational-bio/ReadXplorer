package de.cebitec.vamp.transcriptionAnalyses.wizard;

import de.cebitec.vamp.view.dialogMenus.SelectReadClassWizardPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.ChangeSupport;

/**
 * Wizard page iterator for a transcription analyses wizard.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public final class TranscriptionAnalysesWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    // Example of invoking this wizard:
    // @ActionID(category="...", id="...")
    // @ActionRegistration(displayName="...")
    // @ActionReference(path="Menu/...")
    // public static ActionListener run() {
    //     return new ActionListener() {
    //         @Override public void actionPerformed(ActionEvent e) {
    //             WizardDescriptor wiz = new WizardDescriptor(new TranscriptionAnalysesWizardIterator());
    //             // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
    //             // {1} will be replaced by WizardDescriptor.Iterator.name()
    //             wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
    //             wiz.setTitle("...dialog title...");
    //             if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
    //                 ...do something...
    //             }
    //         }
    //     };
    // }
    
    public static final String PROP_TSS_ANALYSIS = "tssAnalysis";
    public static final String PROP_FILTER_ANALYSIS = "filterAnalysis";
    public static final String PROP_OPERON_ANALYSIS = "operon";
    public static final String PROP_UNANNOTATED_TRANSCRIPT_DET = "unannotatedTranscriptDetection";
    public static final String PROP_AUTO_TSS_PARAMS = "automaticsTSSParameterEstimation";
    public static final String PROP_AUTO_OPERON_PARAMS = "automaticOperonParameterEstimation";
    public static final String PROP_MIN_TOTAL_INCREASE = "minTotalIncrease";
    public static final String PROP_MIN_PERCENT_INCREASE = "minTotalPercentIncrease";
    public static final String PROP_MAX_LOW_COV_INIT_COUNT = "maxLowCovInitialCount";
    public static final String PROP_MIN_LOW_COV_INC = "minLowCovIncrease";
    public static final String PROP_MIN_TRANSCRIPT_EXTENSION_COV = "minTranscriptExtensionCov";
    public static final String PROP_MIN_NUMBER_READS = "minNumberReads";
    public static final String PROP_MAX_NUMBER_READS = "maxNumberReads";
    public static final String PROP_MIN_SPANNING_READS = "minNumberSpanningReads";
    
    private static final String PROP_WIZARD_NAME = "TransAnalyses";
    private int index;
    private ChangeSupport changeSupport;
    private WizardDescriptor wiz;
    private String[] steps;
    private static final String FINISH_MSG = "Press 'Finish' to start";
    
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    
    private TransAnalysesSelectionWizardPanel selectionPanel;
    private TransAnalysesTSSWizardPanel tSSPanel;
    private TransAnalysesFilterWizardPanel filterPanel;
    private TransAnalysesOperonWizardPanel operonPanel;
    private SelectReadClassWizardPanel readClassPanel;
    
    private Map<WizardDescriptor.Panel<WizardDescriptor>, Integer> panelToStepMap = new HashMap<>();
    

    /**
     * Wizard page iterator for a transcription analyses wizard. In order to use
     * it correctly, the wizard, in which this iterator is used has to be set.
     */
    public TranscriptionAnalysesWizardIterator() {
        this.changeSupport = new ChangeSupport(this);
        this.initializePanels();
    }

    /**
     * @return the sequence of all wizard panels of this wizard
     */
    private List<WizardDescriptor.Panel<WizardDescriptor>> initializePanels() {
        if (allPanels == null) {
            allPanels = new ArrayList<>();
    
            selectionPanel = new TransAnalysesSelectionWizardPanel();
            readClassPanel = new SelectReadClassWizardPanel(PROP_WIZARD_NAME);
            tSSPanel = new TransAnalysesTSSWizardPanel();
            filterPanel = new TransAnalysesFilterWizardPanel();
            operonPanel = new TransAnalysesOperonWizardPanel();

            allPanels.add(selectionPanel);
            allPanels.add(readClassPanel);
            allPanels.add(tSSPanel);
            allPanels.add(filterPanel);
            allPanels.add(operonPanel);
            
            this.panelToStepMap.put(selectionPanel, 0);
            this.panelToStepMap.put(readClassPanel, 1);
            this.panelToStepMap.put(tSSPanel, 2);
            this.panelToStepMap.put(filterPanel, 3);
            this.panelToStepMap.put(operonPanel, 4);
            
            this.steps = new String[allPanels.size() + 1];
            for (int i = 0; i < allPanels.size(); i++) {
                Component c = allPanels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
            steps[steps.length - 1] = FINISH_MSG;
            
            String[] initiallyShownSteps = new String[]{steps[0], steps[1], "...", steps[steps.length - 1]};
            selectionPanel.getComponent().putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, initiallyShownSteps);
            
            currentPanels = new ArrayList<>();
            currentPanels.add(selectionPanel);
            currentPanels.add(readClassPanel);
        }
        return allPanels;
    }

    /**
     * @return the current wizard panel
     */
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
        if (index == 0) {
            this.updatePanelList(selectionPanel.getComponent().isTSSAnalysisSelected(), tSSPanel);
            this.updatePanelList(selectionPanel.getComponent().isFilterGenesAnalysisSelected(), filterPanel);
            this.updatePanelList(selectionPanel.getComponent().isOperonAnalysisSelected(), operonPanel);

            String[] newStepArray = new String[0];
            List<String> newSteps = new ArrayList<>();
            for (Panel<WizardDescriptor> panel : currentPanels) {
                newSteps.add(this.steps[this.panelToStepMap.get(panel)]);
            }
            newSteps.add(FINISH_MSG);
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, newSteps.toArray(newStepArray));
        }
        
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        ++index;
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        --index;
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
        this.changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.changeSupport.removeChangeListener(l);
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    /**
     * @param wiz the wizard, in which this wizard iterator is contained.
     * If it is not set, no properties can be stored, thus it always has to be
     * set.
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

    /**
     * Updates the wizard's panel list with the given analysis panel and it's 
     * steps with the newStep string, if the given analysis is selected and
     * the analysis panel is not already contained in the wizard panel list.
     * @param analysisSelected true, if the analysis is selected, false otherwise
     * @param analysisPanel the analysis panel to add to the list of panels
     * @param newSteps the list of steps for this wizard
     * @param newStep the new step, which should be added to the step list in case
     *              the analysis is selected
     */
    private void updatePanelList(boolean analysisSelected, Panel<WizardDescriptor> analysisPanel) {
        if (analysisSelected) {
            if (!currentPanels.contains(analysisPanel)) {
                currentPanels.add(analysisPanel);
            }           
        } else if (currentPanels.contains(analysisPanel)) {
            currentPanels.remove(analysisPanel);
        }
    }

    /**
     * @param usingADBTrack true, if the wizard is run on a track stored
     * completely in the DB, false otherwise.
     */
    public void setUsingDBTrack(boolean containsDBTrack) {
        this.readClassPanel.getComponent().setUsingDBTrack(containsDBTrack);
    }
    
    /**
     * @return The dynamically generated property name for the read class 
     * selection for this wizard. Can be used to obtain the corresponding
     * read class parameters.
     */
    public String getReadClassPropForWiz() {
        return this.readClassPanel.getPropReadClassParams();
    }

}
