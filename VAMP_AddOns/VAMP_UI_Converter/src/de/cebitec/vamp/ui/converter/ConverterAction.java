package de.cebitec.vamp.ui.converter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

/**
 * Action for the converter.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */

@ActionID(category = "File",
id = "de.cebitec.vamp.ui.converter.ConverterAction")
@ActionRegistration(iconBase = "de/cebitec/vamp/ui/converter/import.png",
displayName = "#CTL_ConverterAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1487, separatorAfter = 1493),
    @ActionReference(path = "Toolbars/File", position = 300)
})
@Messages("CTL_ConverterAction=Convert Files")
public final class ConverterAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        
        JOptionPane.showInputDialog(new JPanel(), new ConverterPanel(), "", JOptionPane.PLAIN_MESSAGE);
    }
}
