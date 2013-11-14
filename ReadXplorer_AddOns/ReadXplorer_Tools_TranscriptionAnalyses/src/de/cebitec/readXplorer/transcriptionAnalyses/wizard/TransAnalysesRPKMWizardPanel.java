package de.cebitec.readXplorer.transcriptionAnalyses.wizard;

import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;
import java.awt.Component;
import org.openide.WizardDescriptor;

/**
 * Panel for showing and handling all available options for the operon
 * detection.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesRPKMWizardPanel extends ChangeListeningWizardPanel {

    private TransAnalysesRPKMVisualPanel component;

    /**
     * Panel for showing and handling all available options for the operon
     * detection.
     */
    public TransAnalysesRPKMWizardPanel() {
        super("Please enter valid parameters (only positive numbers are allowed)");
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new TransAnalysesRPKMVisualPanel();
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS, this.component.getMinReadCount());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS, this.component.getMaxReadCount());
        }
    }
    
}
