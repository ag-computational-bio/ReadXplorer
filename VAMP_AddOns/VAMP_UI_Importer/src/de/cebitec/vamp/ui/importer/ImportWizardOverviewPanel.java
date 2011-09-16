package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.ui.importer.actions.ImportWizardAction;
import java.awt.Component;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ImportWizardOverviewPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ImportOverviewCard importOverviewCard;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (importOverviewCard == null) {
            importOverviewCard = new ImportOverviewCard();
        }
        return importOverviewCard;
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
        // load jobs to be imported
        List<ReferenceJob> refJobs = (List<ReferenceJob>) settings.getProperty(ImportWizardAction.PROP_REFJOBLIST);
        List<TrackJob> trackJobs = (List<TrackJob>) settings.getProperty(ImportWizardAction.PROP_TRACKJOBLIST);
        List<SeqPairJobContainer> seqPairJobs = (List<SeqPairJobContainer>) settings.getProperty(ImportWizardAction.PROP_SEQPAIRJOBLIST);

        importOverviewCard.showOverview(refJobs, trackJobs, seqPairJobs);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
    }
}
