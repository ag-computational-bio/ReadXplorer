package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;

import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class FivePrimeEnrichedTracksPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private FivePrimeEnrichedTracksVisualPanel component;
    private final String wizardName;
    private final int referenceId;

    public FivePrimeEnrichedTracksPanel(String wizardName, int referenceID) {
        this.wizardName = wizardName;
        this.referenceId = referenceID;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public FivePrimeEnrichedTracksVisualPanel getComponent() {
        if (component == null) {
            component = new FivePrimeEnrichedTracksVisualPanel(wizardName, this.referenceId);
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
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction, component.getFraction());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_RATIO, component.getRatio());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_UPSTREAM, component.getUpstrteam());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_DOWNSTREAM, component.getDownstream());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_INTERNAL_TSS, component.isExcludeInternalTSS());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_TSS_DISTANCE, component.getExcludeTssDistance());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_KEEPINTERNAL_DISTANCE, component.getKeepingInternalTssDistance());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_LIMIT, component.getLeaderlessDistance());
        storePrefs();
    }

    private void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_Fraction, component.getFraction().toString());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_UPSTREAM, component.getUpstrteam().toString());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_DOWNSTREAM, component.getDownstream().toString());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_RATIO, component.getRatio().toString());
        pref.putBoolean(wizardName+TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_INTERNAL_TSS, component.isExcludeInternalTSS());
        pref.putInt(wizardName+TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_TSS_DISTANCE, component.getExcludeTssDistance());
        pref.putInt(wizardName+TranscriptomeAnalysisWizardIterator.PROP_KEEPINTERNAL_DISTANCE, component.getKeepingInternalTssDistance());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_LIMIT, component.getLeaderlessDistance().toString());
    }

    @Override
    public void validate() throws WizardValidationException {
        if (component.getFraction() < 0.0 || component.getFraction() > 1.0) {
            throw new WizardValidationException(null, "Please choose a fraction 0.0 < fraction < 1.0.", null);
        } else if(component.getRatio() < 0) {
            throw new WizardValidationException(null, "Please choose a ratio > 0.", null);
        } else if(component.getKeepingInternalTssDistance() < 0) {
            throw new WizardValidationException(null, "Please choose a keeping distance rel. to TLS > 0.", null);
        }
    }
}
