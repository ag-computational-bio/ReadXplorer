/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.differentialExpression.wizard;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
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
            throw  new WizardValidationException(null, "Please assign every track to a condition.", null);
        }
    }
}
