package de.cebitec.vamp.ui.converter;

import de.cebitec.vamp.util.VisualisationUtils;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
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
    
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    
    @Override
    public void actionPerformed(ActionEvent e) {
  
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{new ConverterWizardPanel()};
        }
        WizardDescriptor wizardDescriptor = new WizardDescriptor(VisualisationUtils.getWizardPanels(panels));
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(ConverterAction.class, "TTL_ConvertWizardTitle"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) { //could test can import property, if more than one pages are included...
            // start conversion
            
            String filePath = (String) wizardDescriptor.getProperty(ConverterAction.PROP_FILEPATH);
            String converterType = (String) wizardDescriptor.getProperty(ConverterAction.PROP_CONVERTER_TYPE);
            String referenceName = (String) wizardDescriptor.getProperty(ConverterAction.PROP_REFERENCE_NAME);
            int referenceLength = (Integer) wizardDescriptor.getProperty(ConverterAction.PROP_REFERENCE_LENGTH);
            
            ConvertThread convertThread = new ConvertThread(filePath, converterType, referenceName, referenceLength);
            convertThread.start();
        }
    }
    

}
