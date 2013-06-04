package de.cebitec.vamp.differentialExpression.wizard;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.util.FeatureType;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class SimpleTestWizardPanelNormalization implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SimpleTestVisualPanelNormalization component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SimpleTestVisualPanelNormalization getComponent() {
        if (component == null) {
            component = new SimpleTestVisualPanelNormalization();
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
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        int id = (int) wiz.getProperty("genomeID");
        List<FeatureType> usedFeatures = (List<FeatureType>) wiz.getProperty("featureType");
        ReferenceConnector referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(id);
        int genomeSize = referenceConnector.getRefGenome().getSequence().length();
        List<PersistantFeature> persFeatures = referenceConnector.getFeaturesForRegion(1, genomeSize, usedFeatures);
        getComponent().setFeatureList(persFeatures);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        boolean useHouseKeepingGenesToNormalize = getComponent().useHouseKeepingGenesToNormalize();
        wiz.putProperty("useHouseKeepingGenesToNormalize", useHouseKeepingGenesToNormalize);
        if (useHouseKeepingGenesToNormalize) {
            List<Integer> normalizationFeatures = getComponent().getSelectedFeatures();
            wiz.putProperty("normalizationFeatures", normalizationFeatures);
        }
    }

    @Override
    public void validate() throws WizardValidationException {
    }
}
