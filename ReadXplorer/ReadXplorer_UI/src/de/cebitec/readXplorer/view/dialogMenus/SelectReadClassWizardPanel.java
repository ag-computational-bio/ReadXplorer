package de.cebitec.readXplorer.view.dialogMenus;

import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import java.util.prefs.Preferences;
import org.openide.WizardDescriptor;
import org.openide.util.NbPreferences;

/**
 * Panel for showing and handling all available options for the selection of
 * read classes.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SelectReadClassWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_PERFECT_SELECTED = "PerfectSelected";
    public static final String PROP_BEST_MATCH_SELECTED = "BestMatchSelected";
    public static final String PROP_COMMON_MATCH_SELECTED = "CommonMatchSelected";
    public static final String PROP_UNIQUE_SELECTED = "UniqueSelected";
    public static final String PROP_MIN_MAPPING_QUAL = "minMapQual";
    
    private static final String PROP_READ_CLASS_PARAMS = "ReadClassParams";
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectReadClassVisualPanel component;
    private final String wizardName;

    /**
     * Panel for showing and handling all available options for the selection of
     * read classes.
     * @param wizardName the name of the wizard using this wizard panel. It will
     * be used to store the selected settings for this wizard under a unique 
     * identifier.
     */
    public SelectReadClassWizardPanel(String wizardName) {
        super("Please select at least one read class to continue and enter a value between 0 and 127 as mapping quality!");
        this.wizardName = wizardName;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectReadClassVisualPanel getComponent() {
        if (component == null) {
            component = new SelectReadClassVisualPanel(wizardName);
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            ParametersReadClasses readClassParams = this.component.getReadClassParams();
            wiz.putProperty(getPropReadClassParams(), readClassParams);
            this.storePrefs(readClassParams);
        }
    }

    /**
     * Stores the selected read classes for this specific wizard for later use,
     * also after restarting the software.
     * @param readClassParams The parameters to store
     */
    private void storePrefs(ParametersReadClasses readClassParams) {
        String isPerfectSelected = readClassParams.isPerfectMatchUsed() ? "1" : "0";
        String isBestMatchSelected = readClassParams.isBestMatchUsed() ? "1" : "0";
        String isCommonMatchSelected = readClassParams.isCommonMatchUsed() ? "1" : "0";
        String isUniqueSelected = readClassParams.isOnlyUniqueReads() ? "1" : "0";
        String minMappingQuality = String.valueOf(readClassParams.getMinMappingQual());
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.put(wizardName + PROP_PERFECT_SELECTED, isPerfectSelected);
        pref.put(wizardName + PROP_BEST_MATCH_SELECTED, isBestMatchSelected);
        pref.put(wizardName + PROP_COMMON_MATCH_SELECTED, isCommonMatchSelected);
        pref.put(wizardName + PROP_UNIQUE_SELECTED, isUniqueSelected);
        pref.put(wizardName + PROP_MIN_MAPPING_QUAL, minMappingQuality);
    }
    
    /**
     * @return The property string for the read class parameter set for the 
     * corresponding wizard.
     */
    public String getPropReadClassParams() {
        return this.wizardName + PROP_READ_CLASS_PARAMS;
    }

}
