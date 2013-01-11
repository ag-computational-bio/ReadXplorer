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
     * various properties for them influencing wizard appearance for static
     * wizards. 
     * @param wizardPanels the panels belonging to the wizard
     * @return the configured wizard panel array
     */
    @SuppressWarnings("unchecked")
    public static List<WizardDescriptor.Panel<WizardDescriptor>> getWizardPanels(List<WizardDescriptor.Panel<WizardDescriptor>> wizardPanels) {

        String[] steps = new String[wizardPanels.size()];
        for (int i = 0; i < wizardPanels.size(); i++) {
            Component c = wizardPanels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        return wizardPanels;
    }
}
