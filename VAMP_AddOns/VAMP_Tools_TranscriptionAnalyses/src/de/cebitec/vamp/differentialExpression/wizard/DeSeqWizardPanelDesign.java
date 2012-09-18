/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.differentialExpression.wizard;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class DeSeqWizardPanelDesign implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DeSeqVisualPanelDesign component;
    private List<PersistantTrack> tracks;
    private Map<String, String[]> design;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DeSeqVisualPanelDesign getComponent() {
        if (component == null) {
            component = new DeSeqVisualPanelDesign();
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
        tracks = (List<PersistantTrack>) wiz.getProperty("tracks");
        getComponent().setTracks(tracks);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty("design", design);
        //TODO: Check this and set boolean appropiatly
        wiz.putProperty("workingWithoutReplicates", false);
    }

    @Override
    public void validate() throws WizardValidationException {
        design = new HashMap<>();
        Vector tableData = getComponent().getTableData();
        for (int j = 0; j < tableData.size(); j++) {
            Vector row = (Vector) tableData.elementAt(j);
            String[] rowAsStringArray = new String[tracks.size()];
            String key = "";
            for (int i = 0; i < tracks.size(); i++) {
                String currentCell = (String) row.elementAt(i);
                if (currentCell == null) {
                    if (j < 3) {
                        throw new WizardValidationException(null, "At least three design elements must be specified.", null);
                    } else {
                        throw new WizardValidationException(null, "Please fill out the complete row or remove it.", null);
                    }
                }
                if (i == 0) {
                    key = currentCell;
                } else {
                    rowAsStringArray[i] = currentCell;
                }
            }
            design.put(key, rowAsStringArray);
        }
    }
}
