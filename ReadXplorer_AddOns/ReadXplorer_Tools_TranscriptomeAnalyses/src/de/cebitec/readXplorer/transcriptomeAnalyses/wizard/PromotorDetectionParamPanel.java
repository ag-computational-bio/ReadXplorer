/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.wizard;

import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class PromotorDetectionParamPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private PromotorDetectionParamVisualPanel component;
    private final String wizardName;

    public PromotorDetectionParamPanel(String wizardName) {
        this.wizardName = wizardName;
    }
    
    

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public PromotorDetectionParamVisualPanel getComponent() {
        if (component == null) {
            component = new PromotorDetectionParamVisualPanel(this.wizardName);
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
        storePrefs();
    }

    /**
     * Stores the selected read classes for this specific wizard for later use,
     * also after restarting the software.
     * @param readClassParams The parameters to store
     */
    private void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER1, component.getSpacer1Length().toString());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER2, component.getSpacer2Length().toString());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SEQOFINTREST1, component.getPutativeMinusTenLength().toString());
        pref.put(wizardName+TranscriptomeAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SEQOFINTREST2, component.getPutativeMinusThirtyFiveLength().toString());
        // TODO store workingDirectoryPath!
    }
    
    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
    }
}
