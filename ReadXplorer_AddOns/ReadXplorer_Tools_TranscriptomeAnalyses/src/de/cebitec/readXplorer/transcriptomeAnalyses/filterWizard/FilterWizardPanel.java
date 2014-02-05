/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.filterWizard;

import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.TranscriptomeAnalysisWizardIterator;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class FilterWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private String wizardName;
    public static final String PROP_FILTER_FOR_READSTARTS = "filter for read starts";
    public static final String PROP_FILTER_READSTARTS = "read starts";
    public static final String PROP_FILTER_FOR_SINGLE_TSS = "filter for single TSS";
    public static final String PROP_FILTER_FOR_MULTIPLE_TSS_WITH_SHIFTS = "filter for multiple shifts in TSS positions";
    public static final String PROP_FILTER_WITH_MIN_SHIFT = "shift";
    public static final String PROP_FILTER_FOR_MULTIPLE_TSS = "filter for multiple TSS";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private FilterVisualPanel component;

    public FilterWizardPanel(String wizardName) {
        this.wizardName = wizardName;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public FilterVisualPanel getComponent() {
        if (component == null) {
            component = new FilterVisualPanel(wizardName);
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
        wiz.putProperty(FilterWizardPanel.PROP_FILTER_FOR_MULTIPLE_TSS, component.isMultipleSelected());
        wiz.putProperty(FilterWizardPanel.PROP_FILTER_FOR_MULTIPLE_TSS_WITH_SHIFTS, component.isMultipleWithShiftsSelected());
        wiz.putProperty(FilterWizardPanel.PROP_FILTER_FOR_READSTARTS, component.isExtractionOfTSSWithAtLeastRSSelected());
        wiz.putProperty(FilterWizardPanel.PROP_FILTER_FOR_SINGLE_TSS, component.isSingleSelected());
        wiz.putProperty(FilterWizardPanel.PROP_FILTER_READSTARTS, component.getAtLeastReadStarts());
        wiz.putProperty(FilterWizardPanel.PROP_FILTER_WITH_MIN_SHIFT, component.getShift());
        storePrefs();
    }

    private void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.put(wizardName + FilterWizardPanel.PROP_FILTER_READSTARTS, component.getAtLeastReadStarts().toString());
        pref.put(wizardName + FilterWizardPanel.PROP_FILTER_WITH_MIN_SHIFT, component.getShift().toString());
    }

}
