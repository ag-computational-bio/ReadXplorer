package de.cebitec.vamp.ui.dataAdministration.actions;

import de.cebitec.vamp.ui.dataAdministration.DataAdminWizardSelectionPanel;
import de.cebitec.vamp.ui.dataAdministration.DataAdminWizardOverviewPanel;
import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.ui.dataAdministration.DeletionThread;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class DataAdminWizardAction implements ActionListener {

    private final ViewController context;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    public static final String PROP_REFS2DEL = "refdel";
    public static final String PROP_TRACK2DEL = "trackdel";

    public DataAdminWizardAction(ViewController context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent ev) {
        if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(DataAdminWizardAction.class, "MSG_BackgroundActivity"), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(DataAdminWizardAction.class, "TTL_DataAdminWizardAction.title"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            List<ReferenceJob> refs2del = (List<ReferenceJob>) wizardDescriptor.getProperty(DataAdminWizardAction.PROP_REFS2DEL);
            List<TrackJobs> tracks2del = (List<TrackJobs>) wizardDescriptor.getProperty(DataAdminWizardAction.PROP_TRACK2DEL);

            DeletionThread dt = new DeletionThread(refs2del, tracks2del);
            RequestProcessor rp = new RequestProcessor("Deletion Threads", 2);
            rp.post(dt);
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new DataAdminWizardSelectionPanel(),
                        new DataAdminWizardOverviewPanel()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }
}
