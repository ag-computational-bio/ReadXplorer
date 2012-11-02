package de.cebitec.vamp.ui.converter;

import de.cebitec.vamp.parser.output.ConverterI;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * The wizard panel representing the options for file conversion in VAMP.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class ConverterWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private ConverterSetupCard converterPanel;
    private boolean canConvert;
    private final Set<ChangeListener> listeners = new HashSet<>(1); // or can use ChangeSupport in NB 6.0, but how?!?
    
    public ConverterWizardPanel() {
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
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
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
                fireChangeEvent();
            }
        });
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        // store converter parameters
        ConverterI converter = converterPanel.getSelectedConverter();
        converter.setDataToConvert(converterPanel.getFilePath(), converterPanel.getReferenceName(), converterPanel.getReferenceLength());
        settings.putProperty(ConverterAction.PROP_CONVERTER_TYPE, converter);
    }
    
}
