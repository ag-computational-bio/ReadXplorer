package de.cebitec.vamp.view.dialogMenus;

import de.cebitec.vamp.util.FeatureType;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.WizardDescriptor;
import org.openide.util.NbPreferences;

/**
 * Wizard panel for showing and handling the selection of feature types.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SelectFeatureTypeWizardPanel extends ChangeListeningWizardPanel {
    
    public static final String PROP_SELECTED_FEAT_TYPES = "PropSelectedFeatTypes";
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectFeatureTypeVisualPanel component;
    private final String wizardName;

    /**
     * Wizard panel for showing and handling the selection of feature types.
     * @param wizardName the name of the wizard using this wizard panel. It will
     * be used to store the selected settings for this wizard under a unique
     * identifier.
     */
    public SelectFeatureTypeWizardPanel(String wizardName) {
        super("Please select at least one feature type to continue.");
        this.wizardName = wizardName;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectFeatureTypeVisualPanel getComponent() {
        if (component == null) {
            component = new SelectFeatureTypeVisualPanel(wizardName);
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            wiz.putProperty(getPropSelectedFeatTypes(), new HashSet<>(this.component.getSelectedFeatureTypes()));
            this.storePrefs(this.component.getSelectedFeatureTypes());
        }
    }

    /**
     * Stores the selected feature types for this specific wizard for later use,
     * also after restarting the software.
     * @param readClassParams The parameters to store
     */
    private void storePrefs(List<FeatureType> featureTypeList) {
        StringBuilder featTypeString = new StringBuilder(30);
        for (FeatureType type : featureTypeList) {
            featTypeString.append(type.getTypeString()).append(",");
        }
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.put(getPropSelectedFeatTypes(), featTypeString.toString());
    }
    
    /**
     * @return The property string for the selected feature type list for the
     * corresponding wizard.
     */
    public String getPropSelectedFeatTypes() {
        return this.wizardName + PROP_SELECTED_FEAT_TYPES;
    }
    
}
