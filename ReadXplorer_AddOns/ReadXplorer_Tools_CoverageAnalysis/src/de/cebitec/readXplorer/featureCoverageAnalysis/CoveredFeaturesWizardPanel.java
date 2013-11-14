package de.cebitec.readXplorer.featureCoverageAnalysis;

import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;

/**
 * Panel for showing and handling all available options for the covered
 * feature detection.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeaturesWizardPanel extends ChangeListeningWizardPanel {
    
    public static final String PROP_GET_COVERED_FEATURES = "getCoveredFeatures";
    public static final String PROP_MIN_COVERED_PERCENT = "minCoveredPercent";
    public static final String PROP_MIN_COVERAGE_COUNT = "minCoverageCount";
    public static final String PROP_WHATEVER_STRAND = "whateverStrand";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CoveredFeaturesVisualPanel component;
    
    /**
     * Panel for showing and handling all available options for the covered
     * feature detection.
     */
    public CoveredFeaturesWizardPanel() {
        super("Please enter valid parameters (only positive numbers are allowed)");
    }
    
    

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public CoveredFeaturesVisualPanel getComponent() {
        if (component == null) {
            component = new CoveredFeaturesVisualPanel();
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            wiz.putProperty(CoveredFeaturesWizardPanel.PROP_GET_COVERED_FEATURES, this.component.getGetCoveredFeatures());
            wiz.putProperty(CoveredFeaturesWizardPanel.PROP_MIN_COVERED_PERCENT, this.component.getMinCoveredPercent());
            wiz.putProperty(CoveredFeaturesWizardPanel.PROP_MIN_COVERAGE_COUNT, this.component.getMinCoverageCount());
            wiz.putProperty(CoveredFeaturesWizardPanel.PROP_WHATEVER_STRAND, this.component.getIsWhateverStrand());
        }
    }
}
