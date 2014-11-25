package de.cebitec.vamp.ui.importer.actions;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.api.cookies.LoginCookie;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.ui.importer.ImportThread;
import de.cebitec.vamp.ui.importer.ImportWizardSetupPanel;
import de.cebitec.vamp.ui.importer.ImportWizardOverviewPanel;
import de.cebitec.vamp.parser.ReadPairJobContainer;
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

public final class ImportWizardAction implements ActionListener {

    private final LoginCookie context;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    public static final String PROP_CAN_IMPORT = "canImport";
    public static final String PROP_REFJOBLIST = "referenceJob";
    public static final String PROP_TRACKJOBLIST = "trackJobList";
    public static final String PROP_READPAIRJOBLIST = "readPairJobList";
    public static final String PROP_READPAIRDIST = "readPairDistance";
    public static final String PROP_READPAIRDEVIATION = "readPairDeviation";
    public static final String PROP_POSITIONTABLELIST = "positionTableList";

    public ImportWizardAction(LoginCookie context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent ev) {
        if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ImportWizardAction.class, "MSG_BackgroundActivity"), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(ImportWizardAction.class, "TTL_ImportWizardTitle"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // do something
            List<ReferenceJob> refJobs = (List<ReferenceJob>) wizardDescriptor.getProperty(PROP_REFJOBLIST);
            List<TrackJob> trackJobs = (List<TrackJob>) wizardDescriptor.getProperty(PROP_TRACKJOBLIST);
             //since sequence pair jobs have their own parser it can be distinguished later
            List<ReadPairJobContainer> seqPairJobs = (List<ReadPairJobContainer>) wizardDescriptor.getProperty(PROP_READPAIRJOBLIST);

            ImportThread i = new ImportThread(refJobs, trackJobs, seqPairJobs);
            RequestProcessor rp = new RequestProcessor("Import Threads", 2);
            rp.post(i);
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
                        new ImportWizardSetupPanel(),
                        new ImportWizardOverviewPanel(),
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
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
