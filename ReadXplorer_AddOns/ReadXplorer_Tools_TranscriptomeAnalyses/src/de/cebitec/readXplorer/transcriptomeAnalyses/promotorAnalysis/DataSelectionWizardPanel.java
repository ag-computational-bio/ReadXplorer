/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.promotorAnalysis;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class DataSelectionWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DataSelectionVisualPanel component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DataSelectionVisualPanel getComponent() {
        if (component == null) {
            component = new DataSelectionVisualPanel();
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
        wiz.putProperty(ElementsOfInterest.ALL.toString(), component.isAllElements());
        wiz.putProperty(ElementsOfInterest.ONLY_ANTISENSE.toString(), component.isOnlyAntisenseElements());
        wiz.putProperty(ElementsOfInterest.ONLY_LEADERLESS.toString(), component.isOnlyLeaderlessElements());
        wiz.putProperty(ElementsOfInterest.ONLY_NONE_LEADERLESS.toString(), component.isOnlyNonLeaderlessElements());
        wiz.putProperty(ElementsOfInterest.ONLY_REAL_TSS.toString(), component.isOnlyRealTSS());
        wiz.putProperty(ElementsOfInterest.ONLY_SELECTED.toString(), component.isOnlySelected());
//        wiz.putProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ALL_ELEMENTS, component.isAllElements());
//        wiz.putProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_ANTISENSE, component.isOnlyAntisenseElements());
//        wiz.putProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_LEADERLESS, component.isOnlyLeaderlessElements());
//        wiz.putProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_NON_LEADERLESS, component.isOnlyNonLeaderlessElements());
//        wiz.putProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_REAL_TSS, component.isOnlyRealTSS());
        wiz.putProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS, component.getLengthRelativeToTss());
//        wiz.putProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_SELECTED, component.isOnlySelected());

    }
}
