package de.cebitec.vamp.transcriptionAnalyses.wizard;

import de.cebitec.vamp.view.dialogMenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;

/**
 * Wizard panel allowing for selection of the transcription analyses, which
 * should be carried out and whose parameters have to be adjusted in the next
 * steps.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesSelectionWizardPanel extends ChangeListeningWizardPanel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesSelectionVisualPanel component;

    /**
     * Wizard panel allowing for selection of the transcription analyses, which
     * should be carried out and whose parameters have to be adjusted in the
     * next steps.
     */
    public TransAnalysesSelectionWizardPanel() {
        super("Please select at least one transcription analysis to continue!");
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesSelectionVisualPanel getComponent() {
        if (component == null) {
            component = new TransAnalysesSelectionVisualPanel();
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        if (this.isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_TSS_ANALYSIS, this.component.isTSSAnalysisSelected());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_FILTER_ANALYSIS, this.component.isFilterGenesAnalysisSelected());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_OPERON_ANALYSIS, this.component.isOperonAnalysisSelected());
        }
    }
}
