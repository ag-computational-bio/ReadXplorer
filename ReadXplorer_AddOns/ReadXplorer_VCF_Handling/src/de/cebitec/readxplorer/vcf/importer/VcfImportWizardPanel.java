package de.cebitec.readxplorer.vcf.importer;

import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningFinishWizardPanel;
import org.openide.WizardDescriptor;

/**
 * The wizard panel for importing VCF files.
 *
 * @author marend, vetz, Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
class VcfImportWizardPanel extends ChangeListeningFinishWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private VcfImportVisualPanel component;
    public static final String PROP_SELECTED_FILE = "selected File";
    public static final String PROP_SELECTED_REF = "selected Reference";
    public boolean validated = true;
    
   /**
    * The wizard panel for importing VCF files.
    */
    public VcfImportWizardPanel() {
        super("Select a file to continue.");
    }
    

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public VcfImportVisualPanel getComponent() {
        if (component == null) {
            component = new VcfImportVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        if (isValid()) {
            wiz.putProperty(VcfImportWizardPanel.PROP_SELECTED_FILE, this.component.getVcfFile());
            wiz.putProperty(VcfImportWizardPanel.PROP_SELECTED_REF, this.component.getReference());
        }
    }

}
