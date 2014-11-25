package de.cebitec.vamp.genomeAnalyses;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public abstract class ChangeListeningWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    public static final String PROP_VALIDATE = "validated";
    
    private JPanel component;
    private ChangeSupport changeSupport;
    private boolean isValidated = true;

    public ChangeListeningWizardPanel() {
        this.changeSupport = new ChangeSupport(this);
    }
    
    @Override
    public abstract Component getComponent();

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return this.isValidated;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        this.changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(final WizardDescriptor wiz) {
        if (component != null) {
            component.addPropertyChangeListener(ChangeListeningWizardPanel.PROP_VALIDATE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    isValidated = (boolean) evt.getNewValue();
                    if (isValidated) {
                        wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                    } else {
                        wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Please enter valid parameters (only positive numbers are allowed)");
                    }
                    changeSupport.fireChange();
                }
            });
        }
    }

    @Override
    public abstract void storeSettings(WizardDescriptor settings);
    
}
