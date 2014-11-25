package de.cebitec.vamp.ui.converter;

import de.cebitec.vamp.parser.output.ConverterI;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * The wizard panel representing the options for file conversion in ReadXplorer.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class ConverterWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private ConverterSetupCard converterPanel;
    private boolean canConvert;
    private ChangeSupport changeSupport;
    
    public ConverterWizardPanel() {
        this.changeSupport = new ChangeSupport(this);
    }

    @Override
    public Component getComponent() {
        if (this.converterPanel == null) {
            this.converterPanel = new ConverterSetupCard();
        }
        return this.converterPanel;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        return canConvert;
    }

    @Override
    public boolean isFinishPanel() {
        return canConvert;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(WizardDescriptor settings) {
        converterPanel.addPropertyChangeListener(ConverterAction.PROP_CAN_CONVERT, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                canConvert = (Boolean) evt.getNewValue();
                changeSupport.fireChange();
            }
        });
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        // store converter parameters
        ConverterI converter = converterPanel.getSelectedConverter();
        if (isFinishPanel()) {
            converter.setDataToConvert(converterPanel.getMappingFiles(), converterPanel.getReferenceName(), converterPanel.getReferenceLength());
        }
        settings.putProperty(ConverterAction.PROP_CONVERTER_TYPE, converter);
    }
    
}
