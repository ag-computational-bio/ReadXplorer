/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.PurposeEnum;
import de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis.DataSelectionWizardPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

public final class PromotorAnalysisWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor wiz;
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private String[] currentPanelsIndex;
    public static final String PROP_WIZARD_NAME = "Promotor analysis";
    public static final String PROP_WORKING_DIR = "workingDir";
    public static final String PROP_PROMOTOR_ANALYSIS_MINUS10_MOTIF_LENGTH = "-10 Motif legth";
    public static final String PROP_PROMOTOR_ANALYSIS_MINUS35_MOTIF_LENGTH = "-35 Motif length";
    public static final String PROP_PROMOTOR_ANALYSIS_NUMBER_OF_TRYING = "number of trying";
    public static final String PROP_PROMOTOR_ANALYSIS_SPACER1_LENGTH = "spacer1";
    public static final String PROP_PROMOTOR_ANALYSIS_SPACER2_LENGTH = "spacer2";
    public static final String PROP_PROMOTOR_ANALYSIS_ALTERNATIVE_SPACER = "alternative spacer";
    public static final String PROP_PROMOTOR_ANALYSIS_PUTATIVE_10_REGION = "putative -10 region length";
    public static final String PROP_PROMOTOR_ANALYSIS_PUTATIVE_35_REGION = "putative -35 region length";
    public static final String PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS = "length for promotor analysis of all elements";
    // Promotor Analysis Properties
    public static final String PROP_PROMOTOR_ANALYSIS_WIRKINGDIRECTORY_PATH = "pathToWorkingDir";

    public PromotorAnalysisWizardIterator() {
        initializePanels();
    }

    /**
     * Initializes all panels needed for this wizard.
     */
    private void initializePanels() {
        if (allPanels == null) {
            allPanels = new ArrayList<>();
            allPanels.add(new DataSelectionWizardPanel(PurposeEnum.PROMOTER_ANALYSIS)); // 0
            allPanels.add(new FivePrimeUTRPromotorSettingsWizardPanel(PROP_WIZARD_NAME)); // 1
            String[] steps = new String[allPanels.size()];
            for (int i = 0; i < allPanels.size(); i++) {
                Component c = allPanels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
            this.currentPanels = new ArrayList<>();
            currentPanels.add(this.allPanels.get(0));
            currentPanels.add(this.allPanels.get(1));
            currentPanelsIndex = new String[]{steps[0], steps[1]};

            Component c = allPanels.get(0).getComponent();
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, currentPanelsIndex);
            }
        }

    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return allPanels.get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + allPanels.size();
    }

    /**
     * @param wiz the wizard, in which this wizard iterator is contained. If it
     * is not set, no properties can be stored, thus it always has to be set.
     */
    public void setWiz(WizardDescriptor wiz) {
        this.wiz = wiz;
    }

    @Override
    public boolean hasNext() {
        return index < allPanels.size() - 1;
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
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed
}
