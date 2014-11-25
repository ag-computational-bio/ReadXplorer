package de.cebitec.vamp.view.dialogMenus;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.List;
import org.openide.WizardDescriptor;

/**
 * A WizardPanel for opening tracks.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class OpenTracksWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_SELECTED_TRACKS = "PROP_SELECTED_TRACKS";
    public static final String PROP_COMBINE_TRACKS = "PROP_COMBINE_TRACKS";
    
    private OpenTracksVisualPanel component;
    private String wizardName;
    private final int referenceId;
    private SelectReadClassVisualPanel readClassVisualPanel;
    
    /**
     * A WizardPanel for opening tracks.
     * @param wizardName the name of the corresponding wizard
     * @param referenceId the unique id of the reference genome
     */
    public OpenTracksWizardPanel(String wizardName, int referenceId) {
        super("Please select at least one track to continue.");
        this.wizardName = wizardName;
        this.referenceId = referenceId;
    }
    
    /**
     * @return The OpenTracksVisualPanel, displaying the content of this panel.
     */
    @Override
    public OpenTracksVisualPanel getComponent() {
        if (component == null) {
            component = new OpenTracksVisualPanel(referenceId);
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            List<PersistantTrack> tracks = this.component.getSelectedTracks();
            wiz.putProperty(getPropSelectedTracks(), tracks);
            wiz.putProperty(getPropCombineTracks(), this.component.isCombineTracks());
            if (this.readClassVisualPanel != null) {
                this.readClassVisualPanel.setUsingDBTrack(PersistantTrack.checkForDBTrack(tracks));
            }
        }
    }

    /**
     * @return The property string for the selected tracks list for the
     * corresponding wizard.
     */
    public String getPropSelectedTracks() {
        return this.wizardName + PROP_SELECTED_TRACKS;
    }
    
    /**
     * @return The property string for the combination of tracks for the
     * corresponding wizard.
     */
    public String getPropCombineTracks() {
        return this.wizardName + PROP_COMBINE_TRACKS;
    }
    
    /**
     * @param readClassVisualPanel A ReadClassVisualPanel, which shall be 
     * informed, if a track completely stored in the DB is used in the current
     * track selection.
     */
    public void setReadClassVisualPanel(SelectReadClassVisualPanel readClassVisualPanel) {
        this.readClassVisualPanel = readClassVisualPanel;
    }
}
