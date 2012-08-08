package de.cebitec.vamp.util;

import java.awt.Component;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;

/**
 * Class containing general visualization related methods.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class VisualisationUtils {

    private VisualisationUtils() {
    }

    /**
     * Recursive method to get all buttons belonging to a parent component.
     * @param comp the component whose buttons are needed
     * @param buttons the list to be filled with the identified buttons
     */
    public static void getButtons(JComponent comp, List<JButton> buttons) {
        if (comp == null) {
            return;
        }

        for (Component c : comp.getComponents()) {
            if (c instanceof JButton) {
                buttons.add((JButton) c);

            } else if (c instanceof JComponent) {
                VisualisationUtils.getButtons((JComponent) c, buttons);
            }
        }
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance. 
     */
    @SuppressWarnings("unchecked")
    public static WizardDescriptor.Panel<WizardDescriptor>[] getWizardPanels(WizardDescriptor.Panel<WizardDescriptor>[] wizardPanels) {

        String[] steps = new String[wizardPanels.length];
        for (int i = 0; i < wizardPanels.length; i++) {
            Component comp = wizardPanels[i].getComponent();
            // Default step name to component name of panel.
            steps[i] = comp.getName();
            if (comp instanceof JComponent) { // assume Swing components
                JComponent jComp = (JComponent) comp;
                // Sets step number of a component
                jComp.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                // Sets steps names for a panel
                jComp.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                // Turn on subtitle creation on each step
                jComp.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jComp.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                // Turn on numbering of all steps
                jComp.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
            }
        }
        return wizardPanels;
    }
}
