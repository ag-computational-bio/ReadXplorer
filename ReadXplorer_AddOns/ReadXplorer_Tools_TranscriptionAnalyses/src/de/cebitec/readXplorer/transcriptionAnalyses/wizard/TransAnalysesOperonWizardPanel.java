package de.cebitec.readXplorer.transcriptionAnalyses.wizard;

import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;

/**
 * Panel for showing and handling all available options for the operon detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesOperonWizardPanel extends ChangeListeningWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesOperonVisualPanel component;

    /**
     * Panel for showing and handling all available options for the operon
     * detection.
     */
    public TransAnalysesOperonWizardPanel() {
        super("Please enter valid parameters (only positive numbers are allowed)");
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesOperonVisualPanel getComponent() {
        if (component == null) {
            component = new TransAnalysesOperonVisualPanel();
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_AUTO_OPERON_PARAMS, this.component.isOperonAutomatic());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_SPANNING_READS, this.component.getMinSpanningReads());
        }
    }
}
