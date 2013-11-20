package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;

import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class DataSetChoicePanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DataSetChoiceVisualPanel component;
    private final String wizardName;

    public DataSetChoicePanel(String wizardName) {
        this.wizardName = wizardName;
    }
    
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DataSetChoiceVisualPanel getComponent() {
        if (component == null) {
            component = new DataSetChoiceVisualPanel(wizardName);
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
        // use wiz.getProperty to retrieve previous panel state
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_FIVEPRIME_DATASET, this.component.isFiveEnrichedTrack());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_WHOLEGENOME_DATASET, this.component.isWholeGenomeTrack());
        storePrefs();
    }

    /**
     * Stores the selected read classes for this specific wizard for later use,
     * also after restarting the software.
     * @param readClassParams The parameters to store
     */
    private void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.putBoolean(wizardName + TranscriptomeAnalysisWizardIterator.PROP_WHOLEGENOME_DATASET, this.component.isWholeGenomeTrack());
        pref.putBoolean(wizardName + TranscriptomeAnalysisWizardIterator.PROP_FIVEPRIME_DATASET, this.component.isFiveEnrichedTrack());
    }

    @Override
    public void validate() throws WizardValidationException {
        // one of the checkBoxes in component have to be choosen!
        if (component.isFiveEnrichedTrack() && component.isWholeGenomeTrack()) {
            throw new WizardValidationException(null, "Please selct one of the given data set types.", null);
        }
    }
}