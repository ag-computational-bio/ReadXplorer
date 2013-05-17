package de.cebitec.vamp.transcriptionAnalyses.wizard;

import de.cebitec.vamp.view.dialogMenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;

/**
 * Panel for showing and handling all available options for the feature
 * filter.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesFilterWizardPanel extends ChangeListeningWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesFilterVisualPanel component;

    /**
     * Panel for showing and handling all available options for the feature
     * filter.
     */
    public TransAnalysesFilterWizardPanel() {
        super("Please enter valid parameters (only positive numbers are allowed)");
    }
    
    

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesFilterVisualPanel getComponent() {
        if (component == null) {
            component = new TransAnalysesFilterVisualPanel();
        }
        return component;
    }
    
    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS, this.component.getMinNumberOfReads());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS, this.component.getMaxNumberOfReads());
        }
    }
}
