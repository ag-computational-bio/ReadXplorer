package de.cebitec.vamp.transcriptionAnalyses.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Panel for showing and handling all available options for the transcription
 * start site detection.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesTSSWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesTSSVisualPanel component;
    private ChangeSupport changeSupport;
    private boolean isValidated = true;

    /**
     * Panel for showing and handling all available options for the
     * transcription start site detection.
     */
    public TransAnalysesTSSWizardPanel() {
        this.changeSupport = new ChangeSupport(this);
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesTSSVisualPanel getComponent() {
        if (component == null) {
            component = new TransAnalysesTSSVisualPanel();
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
        if (this.isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_AUTO_TSS_PARAMS, this.component.isTssAutomatic());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_TOTAL_INCREASE, this.component.getMinTotalIncrease());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_PERCENT_INCREASE, this.component.getMinTotalPercentIncrease());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_LOW_COV_INIT_COUNT, this.component.getMaxLowCovInitialCount());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_LOW_COV_INC, this.component.getMinLowCovIncrease());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_UNANNOTATED_TRANSCRIPT_DET, this.component.getDetectUnannotatedTranscripts());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_TRANSCRIPT_EXTENSION_COV, this.component.getMinTranscriptExtensionCov());
        }
    }
}
