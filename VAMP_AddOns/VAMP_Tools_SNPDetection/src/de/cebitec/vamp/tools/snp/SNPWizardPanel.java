package de.cebitec.vamp.tools.snp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * The SNP detection wizard main panel.
 */
public class SNPWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    public static final String PROP_MIN_PERCENT = "minPercent";
    public static final String PROP_MIN_VARYING_BASES = "minNoBases";
    public static final String PROP_VALIDATE = "validate";
    
    private ChangeSupport changeSupport;
    
    private boolean isValidated = true;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SNPVisualPanel component;

    /**
     * The SNP detection wizard main panel.
     * @param referenceId reference id 
     */
    public SNPWizardPanel() {
        this.changeSupport = new ChangeSupport(this);
    }
    
    

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SNPVisualPanel getComponent() {
        if (component == null) {
            component = new SNPVisualPanel();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isFinishPanel() {
        return this.isValidated;
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return isValidated;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(final WizardDescriptor wiz) {
        component.addPropertyChangeListener(SNPWizardPanel.PROP_VALIDATE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                isValidated = (boolean) evt.getNewValue();
                if (isValidated) {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                } else {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Please enter valid parameters (only number and percent values)");
                }
                changeSupport.fireChange();
            }
        });
        
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isFinishPanel()) {
            wiz.putProperty(PROP_MIN_PERCENT, this.component.getMinPercentage());
            wiz.putProperty(PROP_MIN_VARYING_BASES, this.component.getMinVaryingBases());
        }
    }
}
