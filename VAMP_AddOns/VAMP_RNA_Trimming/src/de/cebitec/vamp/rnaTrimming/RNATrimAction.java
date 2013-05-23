/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.api.cookies.LoginCookie;
import de.cebitec.vamp.correlationAnalysis.CorrelationResultPanel;
import de.cebitec.vamp.mapping.api.MappingApi;
import de.cebitec.vamp.rnaTrimming.TrimMethod;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;



@ActionID(
    category = "Tools",
id = "de.cebitec.vamp.rnaTrimming.RNATrimAction")
@ActionRegistration(
    displayName = "#CTL_RNATrimAction")
@ActionReference(path = "Menu/Tools", position = 154)
@Messages("CTL_RNATrimAction=Trim upmapped RNA reads in a file")
public final class RNATrimAction implements ActionListener {
    static String PROP_TRIMMETHOD = "PROP_TRIMMETHOD";
    static String PROP_TRIMMAXIMUM = "PROP_TRIMMAXIMUM";
    static String PROP_SOURCEPATH = "PROP_SOURCEPATH";
    static String PROP_REFERENCEPATH = "PROP_REFERENCEPATH";
    
    
    private final LoginCookie context;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    
    
    
    public RNATrimAction(LoginCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (MappingApi.checkMapperConfig()) { 
        
            if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(RNATrimAction.class, "MSG_BackgroundActivity"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }

            WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
            wizardDescriptor.setTitle(NbBundle.getMessage(RNATrimAction.class, "TTL_RNATrimAction.title"));
            Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
            dialog.setVisible(true);
            dialog.toFront();
            boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
            if (!cancelled) {
                new RNATrimProcessor( (String) wizardDescriptor.getProperty(PROP_REFERENCEPATH), 
                        (String) wizardDescriptor.getProperty(PROP_SOURCEPATH), 
                        (Integer) wizardDescriptor.getProperty(PROP_TRIMMAXIMUM),
                        (TrimMethod) wizardDescriptor.getProperty(PROP_TRIMMETHOD)
                        );





                /*List<ReferenceJob> refs2del = (List<ReferenceJob>) wizardDescriptor.getProperty(DataAdminWizardAction.PROP_REFS2DEL);
                List<TrackJob> tracks2del = (List<TrackJob>) wizardDescriptor.getProperty(DataAdminWizardAction.PROP_TRACK2DEL);

                DeletionThread dt = new DeletionThread(refs2del, tracks2del);
                RequestProcessor rp = new RequestProcessor("Deletion Threads", 2);
                rp.post(dt);*/
            }
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
                        new RNATrimSelectionPanel(),
                        new RNATrimOverviewPanel()
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
