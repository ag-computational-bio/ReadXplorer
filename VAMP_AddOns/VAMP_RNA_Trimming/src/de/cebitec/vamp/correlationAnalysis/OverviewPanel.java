package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.correlationAnalysis.CorrelationAnalysisAction.CorrelationCoefficient;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.awt.Component;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * This is the panel that displays the OverviewCard.
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class OverviewPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private OverviewCard component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new OverviewCard();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    @SuppressWarnings("unchecked")
    public void readSettings(WizardDescriptor settings) {
        component.showGenereateOverview(
                (CorrelationCoefficient) settings.getProperty(CorrelationAnalysisAction.PROP_CORRELATIONCOEFFICIENT),
                (List<PersistantTrack>) settings.getProperty(CorrelationAnalysisAction.PROP_SELECTED_TRACKS),
                settings.getProperty(CorrelationAnalysisAction.PROP_INTERVALLENGTH).toString(),
                settings.getProperty(CorrelationAnalysisAction.PROP_MINCORRELATION).toString(),
                settings.getProperty(CorrelationAnalysisAction.PROP_MINPEAKCOVERAGE).toString()
                );
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
    }
}
