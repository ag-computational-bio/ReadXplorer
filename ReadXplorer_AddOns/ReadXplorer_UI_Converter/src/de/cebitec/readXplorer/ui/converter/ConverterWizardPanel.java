package de.cebitec.readXplorer.ui.converter;

import de.cebitec.readXplorer.parser.output.ConverterI;
import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningFinishWizardPanel;
import java.awt.Component;
import org.openide.WizardDescriptor;

/**
 * The wizard panel representing the options for file conversion in ReadXplorer.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class ConverterWizardPanel extends ChangeListeningFinishWizardPanel {

    private ConverterSetupCard converterPanel;
    
    public ConverterWizardPanel() {
        super("");
    }

    @Override
    public Component getComponent() {
        if (this.converterPanel == null) {
            this.converterPanel = new ConverterSetupCard();
        }
        return this.converterPanel;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        // store converter parameters
        if (this.isFinishPanel()) {
            ConverterI converter = converterPanel.getSelectedConverter();
            converter.setDataToConvert(converterPanel.getMappingFiles(), converterPanel.getRefChromosomeName(), converterPanel.getChromosomeLength());
            settings.putProperty(ConverterAction.PROP_CONVERTER_TYPE, converter);
        }
    }
    
}
