package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.wizard.TranscriptionAnalysesWizardIterator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Panel for showing and handling all available options for the covered
 * feature detection.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeaturesWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {
    
    public static final String PROP_GET_COVERED_FEATURES = "getCoveredFeatures";
    public static final String PROP_MIN_COVERED_PERCENT = "minCoveredPercent";
    public static final String PROP_MIN_COVERAGE_COUNT = "minCoverageCount";
    public static final String PROP_WHATEVER_STRAND = "whateverStrand";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CoveredFeaturesVisualPanel component;
    private ChangeSupport changeSupport;
    private boolean isValidated = true;
    
    /**
     * Panel for showing and handling all available options for the covered
     * feature detection.
     */
    public CoveredFeaturesWizardPanel() {
        this.changeSupport = new ChangeSupport(this);
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
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return this.isValidated;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        this.changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(final WizardDescriptor wiz) {
        component.addPropertyChangeListener(TranscriptionAnalysesWizardIterator.PROP_VALIDATE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                isValidated = (boolean) evt.getNewValue();
                if (isValidated) {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                } else {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Please enter valid parameters (only positive numbers are allowed)");
                }
                changeSupport.fireChange();
            }
        });
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
