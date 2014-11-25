package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.view.dialogMenus.ChangeListeningFinishWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * TrackListPanel displays a list of tracks for a certain reference.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class TrackListPanel extends ChangeListeningFinishWizardPanel {
            
    private int referenceId;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private OpenTrackAmountVisualPanel component;
    

    /**
     * Creates a new track list panel.
     * @param referenceId reference id 
     */
    public TrackListPanel(int referenceId) {
        super("You selected a wrong amount of tracks!");
        this.referenceId = referenceId;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public OpenTrackAmountVisualPanel getComponent() {
        if (component == null) {
            component = new OpenTrackAmountVisualPanel(this.getReferenceId(), this);
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
    public void storeSettings(WizardDescriptor wiz) {
        if (isFinishPanel()) {
            wiz.putProperty(CorrelationAnalysisAction.PROP_SELECTED_TRACKS, this.component.getSelectedTracks());
        }
    }

    /**
     * @return the referenceId
     */
    public int getReferenceId() {
        return referenceId;
    }
}
