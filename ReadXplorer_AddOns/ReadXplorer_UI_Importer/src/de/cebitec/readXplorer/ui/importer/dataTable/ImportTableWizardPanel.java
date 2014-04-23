package de.cebitec.readXplorer.ui.importer.dataTable;

import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningFinishWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * The wizard panel for choosing the parser to import a table file.
 * 
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class ImportTableWizardPanel extends ChangeListeningFinishWizardPanel {

    public static final String PROP_TABLE_TYPE = "selTableType";
    public static final String PROP_SELECTED_FILE = "selFile";
    public static final String PROP_SELECTED_REF = "selRef";
    public static final String PROP_AUTO_DELEMITER = "autoDelimiter";
    public static final String PROP_SEL_PREF = "selPref";
    public static final String PROP_SEL_PARSER = "selParser";
    
    private ImportTableVisualPanel component;
    
    /**
     * The wizard panel for choosing the parser to import a table file. 
     */
    @NbBundle.Messages("ErrorMsg=Please select a parser and a valid file to import.")
    public ImportTableWizardPanel() {
        super(Bundle.ErrorMsg());
    }

    @Override
    public ImportTableVisualPanel getComponent() {
        if (component == null) {
            component = new ImportTableVisualPanel();
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (this.isValid()) {
            wiz.putProperty(PROP_TABLE_TYPE, this.component.getSelectedTableType());
            wiz.putProperty(PROP_SELECTED_FILE, this.component.getFileLocation());
            wiz.putProperty(PROP_SELECTED_REF, this.component.getReference());
            wiz.putProperty(PROP_AUTO_DELEMITER, this.component.isAutodetectDelimiter());
            wiz.putProperty(PROP_SEL_PREF, this.component.getCsvPref());
            wiz.putProperty(PROP_SEL_PARSER, this.component.getParser());
        }
    }

}
