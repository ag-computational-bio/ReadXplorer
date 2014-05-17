/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.mapping;

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
 * MappingAction adds a menu item to start the mapping of a sequencing data set.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
@ActionID(
    category = "Tools",
id = "de.cebitec.readXplorer.mapping.MappingAction")
@ActionRegistration(
    displayName = "#CTL_MappingAction")
@ActionReference(path = "Menu/Tools", position = 154)
@Messages("CTL_MappingAction=Map reads")
public final class MappingAction implements ActionListener {
    static String PROP_SOURCEPATH = "PROP_SOURCEPATH";
    static String PROP_REFERENCEPATH = "PROP_REFERENCEPATH";
    static String PROP_MAPPINGPARAM = "PROP_MAPPINGPARAM";
    
    
    private final LoginCookie context;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    
    
    
    public MappingAction(LoginCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (CentralLookup.getDefault().lookup(SwingWorker.class) != null){
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(MappingAction.class, "MSG_BackgroundActivity"), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        if (MappingApi.checkMapperConfig()) { 
            WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
            wizardDescriptor.setTitle(NbBundle.getMessage(MappingAction.class, "CTL_MappingAction"));
            Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
            dialog.setVisible(true);
            dialog.toFront();
            boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
            if (!cancelled) {
                new MappingProcessor( (String) wizardDescriptor.getProperty(PROP_REFERENCEPATH), 
                        (String) wizardDescriptor.getProperty(PROP_SOURCEPATH),
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
                        new MappingSelectionPanel(),
                        new MappingOverviewPanel()
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
