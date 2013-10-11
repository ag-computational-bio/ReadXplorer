/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses.wizard;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class WholeTranscriptTracksPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private WholeTranscriptTracksVisualPanel component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public WholeTranscriptTracksVisualPanel getComponent() {
        if (component == null) {
            component = new WholeTranscriptTracksVisualPanel();
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
        if (this.component.isRPKM()) {
            wiz.putProperty("rpkm", WholeTranscriptTracksVisualPanel.PROP_ANALYSIS_RPKM);
        }
        if (this.component.isNewRegions()) {
            wiz.putProperty("novel", WholeTranscriptTracksVisualPanel.PROP_ANALYSIS_NOVELREGION);
        }
        if (this.component.isOperonDetection()) {
            wiz.putProperty("operon", WholeTranscriptTracksVisualPanel.PROP_ANALYSIS_OPERON);
        }
    }

    @Override
    public void validate() throws WizardValidationException {
        
        if (!this.component.isRPKM() && !this.component.isOperonDetection() && !this.component.isNewRegions()) {
            throw new WizardValidationException(null, "Please selct at least one of the given analysis types.", null);
        }
    }
}
