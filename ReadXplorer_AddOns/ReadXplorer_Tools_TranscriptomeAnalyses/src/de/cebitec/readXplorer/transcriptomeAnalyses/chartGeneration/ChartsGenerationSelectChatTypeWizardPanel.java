/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ResultPanelTranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis.RbsAnalysisWizardIterator;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ChartsGenerationSelectChatTypeWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ChartsGenerationSelectChatTypeVisualPanel component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public ChartsGenerationSelectChatTypeVisualPanel getComponent() {
        if (component == null) {
            component = new ChartsGenerationSelectChatTypeVisualPanel();
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
        wiz.putProperty(ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs.toString(), component.isAbsoluteFrequency());
        wiz.putProperty(ChartType.BASE_DISTRIBUTION.toString(), component.isBaseDistribution());
        wiz.putProperty(ChartType.DISTRIBUTION_OF_ALL_TSS_OFFSETS_LENGTH.toString(), component.isTssDistribution());
        wiz.putProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_REGION_LENGTH, component.getRange());
        wiz.putProperty(ResultPanelTranscriptionStart.BIN_SIZE, component.getBin());
    }
}
