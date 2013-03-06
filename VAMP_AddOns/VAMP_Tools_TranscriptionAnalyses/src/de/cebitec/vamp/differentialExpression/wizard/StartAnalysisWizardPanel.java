package de.cebitec.vamp.differentialExpression.wizard;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.util.FeatureType;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class StartAnalysisWizardPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private StartAnalysisVisualPanel component;
    private Integer genomeID;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public StartAnalysisVisualPanel getComponent() {
        if (component == null) {
            component = new StartAnalysisVisualPanel(this);
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
        // use wiz.getProperty to retrieve previous panel state
        genomeID = (Integer) wiz.getProperty("genomeID");
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (getComponent().isCheckBoxchecked()) {
            //TODO: Input validation
            String path = getComponent().getSavePath();
            File file = new File(path);
            wiz.putProperty("saveFile", file);
        }
        FeatureType feature = getComponent().getFeatureType();
        wiz.putProperty("featureType", feature);
    }

    @Override
    public void validate() throws WizardValidationException {
        FeatureType feature = getComponent().getFeatureType();
        if (feature != FeatureType.ANY) {
            ReferenceConnector referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(genomeID);
            if (!referenceConnector.hasFeatures(feature)) {
                throw new WizardValidationException(null, "The selected reference genome does not contain annotations of the type " + feature.getTypeString() + ".", null);
            }
        }
    }
}
