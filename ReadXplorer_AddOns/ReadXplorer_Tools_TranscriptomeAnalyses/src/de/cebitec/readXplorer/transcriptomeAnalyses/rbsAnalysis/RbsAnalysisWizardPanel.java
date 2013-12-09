/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis;

import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class RbsAnalysisWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private RbsAnalysisVisualPanel component;
    private String wizardName;
    private ReferenceViewer referenceViewer;
    private List<TranscriptionStart> tss;

    public RbsAnalysisWizardPanel(String wizardName, ReferenceViewer referenceViewer, List<TranscriptionStart> tss) {
        this.wizardName = wizardName;
        this.referenceViewer = referenceViewer;
        this.tss = tss;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public RbsAnalysisVisualPanel getComponent() {
        if (component == null) {
            component = new RbsAnalysisVisualPanel(this.wizardName, this.referenceViewer, this.tss);
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
        wiz.putProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_REGION_LENGTH, component.getRegionLengthForMotifAnalysis());
        wiz.putProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH, component.getExpectedMotifWidth());
        wiz.putProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR, component.getNoOfTrying());
        wiz.putProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER, component.getMinSpacer());
        wiz.putProperty(RbsAnalysisWizardIterator.PROP_WORKING_DIR, component.getWorkingDir());
        storePrefs();
    }

    private void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.put(wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_REGION_LENGTH, component.getRegionLengthForMotifAnalysis().toString());
        pref.put(wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH, component.getExpectedMotifWidth().toString());
        pref.put(wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER, component.getMinSpacer().toString());
    }
}
