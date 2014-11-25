package de.cebitec.vamp.view.dialogMenus;

import org.openide.WizardDescriptor;

/**
 * A Change listening wizard panel, which can control enabling and disabling
 * of the finish button.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public abstract class ChangeListeningFinishWizardPanel extends ChangeListeningWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    /**
     * A Change listening wizard panel, which can control enabling and disabling
     * of the finish button.
     * @param errorMsg The error message to display, in case the required
     * information for this wizard panel is not set correctly.
     */
    public ChangeListeningFinishWizardPanel(String errorMsg) {
        super(errorMsg);
    }
    
    @Override
    public boolean isFinishPanel() {
        return this.isValid();
    }
    
}
