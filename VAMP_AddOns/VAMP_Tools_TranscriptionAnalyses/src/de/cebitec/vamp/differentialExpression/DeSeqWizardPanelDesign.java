/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.ArrayList;
import java.util.List;
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
    private List<String[]> design;

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
    }

    @Override
    public void validate() throws WizardValidationException {
        design = new ArrayList<>();
        Vector tableData = getComponent().getTableData();
        for (int j = 0; j < tableData.size(); j++) {
            Vector row = (Vector) tableData.elementAt(j);
            String[] rowAsString = new String[tracks.size()];
            for (int i = 0; i < tracks.size(); i++) {
                String currentCell = (String) row.elementAt(i);
                if (currentCell == null) {
                    if (j == 0) {
                        throw new WizardValidationException(null, "At least one design element must be specified.", null);
                    } else {
                        throw new WizardValidationException(null, "Please fill out the complete row or remove it.", null);
                    }
                }
                rowAsString[i] = currentCell;
            }
            design.add(rowAsString);
        }
    }
}
