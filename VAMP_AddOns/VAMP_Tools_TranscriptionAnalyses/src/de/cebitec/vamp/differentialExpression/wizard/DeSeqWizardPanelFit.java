/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.differentialExpression.wizard;

import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class DeSeqWizardPanelFit implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DeSeqVisualPanelFit component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DeSeqVisualPanelFit getComponent() {
        if (component == null) {
            component = new DeSeqVisualPanelFit();
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
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        Map<String, String[]> design = (Map<String, String[]>) wiz.getProperty("design");
        getComponent().updateConditionGroupsList(design.keySet());
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (getComponent().allConditionGroupsAssigned()) {
            wiz.putProperty("fittingGroupOne", getComponent().getFittingGroupOne());
            wiz.putProperty("fittingGroupTwo", getComponent().getFittingGroupTwo());
        }
    }

    @Override
    public void validate() throws WizardValidationException {
        if(!getComponent().allConditionGroupsAssigned()){
            throw new WizardValidationException(null, "Please assigne all conditional groups to a fitting group.", null);       
        }
    }
}
