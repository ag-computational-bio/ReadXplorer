/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses.wizard;

import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class LeaderlessDetectionPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private LeaderlessDetectionParamsVisualPanel component;
    private final String wizardName;

    public LeaderlessDetectionPanel(String wizardName) {
        this.wizardName = wizardName;
    }
    

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public LeaderlessDetectionParamsVisualPanel getComponent() {
        if (component == null) {
            component = new LeaderlessDetectionParamsVisualPanel(this.wizardName);
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
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_LIMIT, component.getLimitationOfLeaderless());
        wiz.putProperty(TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_CDSSHIFT, component.isCDSShiftChoosen());
        storePrefs();
    }
    
    private void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.putBoolean(wizardName+TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_CDSSHIFT, component.isCDSShiftChoosen());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_LIMIT, component.getLimitationOfLeaderless().toString());
    }
}
