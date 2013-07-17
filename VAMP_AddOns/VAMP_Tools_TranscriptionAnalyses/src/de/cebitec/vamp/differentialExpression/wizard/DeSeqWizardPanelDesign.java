/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.differentialExpression.wizard;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private List<PersistantTrack> tracks = null;
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
        List<PersistantTrack> tmpTracks = (List<PersistantTrack>) wiz.getProperty("tracks");
        boolean newTracks = false;
        if (tracks == null) {
            newTracks = true;
        } else {
            for (Iterator<PersistantTrack> it = tmpTracks.iterator(); it.hasNext();) {
                PersistantTrack persistantTrack = it.next();
                if (!tracks.contains(persistantTrack)) {
                    newTracks = true;
                    break;
                }
            }
        }
        if (newTracks) {
            tracks = tmpTracks;
            getComponent().setTracks(tracks);
        }
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
        Set<String> usedKeys = new HashSet<>();
        Vector tableData = getComponent().getTableData();
        if (tableData.size() < 2) {
            throw new WizardValidationException(null, "At least two design elements must be specified.", null);
        }
        for (int j = 0; j < tableData.size(); j++) {
            Vector row = (Vector) tableData.elementAt(j);
            String[] rowAsStringArray = new String[tracks.size()];
            String key = (String) row.elementAt(0);
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(key);
            if(m.find()){
                throw new WizardValidationException(null, "Numbers are not allowed in group names.", null);
            }
            if(!usedKeys.add(key)){
                throw new WizardValidationException(null, "Groups must have individual names.", null);
            }
            boolean differentCondsUsed = false;
            String stringBefore = "";
            for (int i = 1; i < tracks.size() + 1; i++) {
                String currentCell = (String) row.elementAt(i);
                if (currentCell == null) {
                    throw new WizardValidationException(null, "Please fill out the complete row or remove it.", null);
                }
                if (!stringBefore.equals("") && !currentCell.equals(stringBefore)) {
                    differentCondsUsed = true;
                }
                rowAsStringArray[i - 1] = currentCell;
                stringBefore = currentCell;
            }
            if (differentCondsUsed) {
                design.put(key, rowAsStringArray);
            } else {
                throw new WizardValidationException(null, "Each row must have at least two different identifier in it.", null);
            }

        }
    }
}
