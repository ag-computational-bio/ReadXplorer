package de.cebitec.vamp.differentialExpression.wizard;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class DeSeqWizardPanelConds implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DeSeqVisualPanelConds component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DeSeqVisualPanelConds getComponent() {
        if (component == null) {
            component = new DeSeqVisualPanelConds();
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
        List<PersistantTrack> selectedTracks = (List<PersistantTrack>) wiz.getProperty("tracks");
        getComponent().updateTrackList(selectedTracks);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (getComponent().conditionsComplete()) {
            Map<String, String[]> conds = new HashMap<>();
            conds.put("twoConds",getComponent().getConditions());
            wiz.putProperty("design", conds);
            wiz.putProperty("workingWithoutReplicates", getComponent().workingWithoutReplicates());
            wiz.putProperty("groupA", getComponent().getGroupA());
            wiz.putProperty("groupB", getComponent().getGroupB());
        }
    }

    @Override
    public void validate() throws WizardValidationException {
        if (!getComponent().conditionsComplete()) {
            throw  new WizardValidationException(null, "Please assigne every track to a condition.", null);
        }
    }
}
