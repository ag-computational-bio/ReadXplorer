package de.cebitec.vamp.ui.converter;

import de.cebitec.vamp.parser.output.ConverterI;
import de.cebitec.vamp.util.VisualisationUtils;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
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
    
    public static final String PROP_CAN_CONVERT = "canConvert";
    public static final String PROP_FILEPATH = "filePath";
    public static final String PROP_CONVERTER_TYPE = "converterType";
    public static final String PROP_REFERENCE_NAME = "referenceName";
    public static final String PROP_REFERENCE_LENGTH = "referenceLength";
    
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    
    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
  
        panels = new ArrayList<>();
        panels.add(new ConverterWizardPanel());
        WizardDescriptor wizardDescriptor = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(ConverterAction.class, "TTL_ConvertWizardTitle"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) { //could test can import property, if more than one pages are included...
            
            // start conversion
            ConverterI converter = (ConverterI) wizardDescriptor.getProperty(ConverterAction.PROP_CONVERTER_TYPE);
            ConvertThread convertThread = new ConvertThread(converter);
            convertThread.start();
        }
    }
    

}
