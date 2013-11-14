/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.rnaTrimming;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.api.cookies.LoginCookie;
import de.cebitec.readXplorer.mapping.api.MappingApi;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
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


/**
 * Creates a menu item for RNA Trimming
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
@ActionID(
    category = "Tools",
id = "de.cebitec.readXplorer.rnaTrimming.RNATrimAction")
@ActionRegistration(
    displayName = "#CTL_RNATrimAction")
@ActionReference(path = "Menu/Tools", position = 155)
@Messages("CTL_RNATrimAction=Trim upmapped RNA reads in a file")
public final class RNATrimAction implements ActionListener {
    static String PROP_TRIMMETHOD = "PROP_TRIMMETHOD";
    static String PROP_TRIMMAXIMUM = "PROP_TRIMMAXIMUM";
    static String PROP_SOURCEPATH = "PROP_SOURCEPATH";
    static String PROP_REFERENCEPATH = "PROP_REFERENCEPATH";
    static String PROP_MAPPINGPARAM = "PROP_MAPPINGPARAM";    
    
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
                //the actual trimming will be done in RNATrimProcessor 
                new RNATrimProcessor( (String) wizardDescriptor.getProperty(PROP_REFERENCEPATH), 
                        (String) wizardDescriptor.getProperty(PROP_SOURCEPATH), 
                        (Integer) wizardDescriptor.getProperty(PROP_TRIMMAXIMUM),
                        (TrimMethod) wizardDescriptor.getProperty(PROP_TRIMMETHOD),
                        (String) wizardDescriptor.getProperty(PROP_MAPPINGPARAM)
                        );
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
