package de.cebitec.readXplorer.ui.importer.actions;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.api.cookies.LoginCookie;
import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.tables.CsvTableParser;
import de.cebitec.readXplorer.parser.tables.TableType;
import de.cebitec.readXplorer.parser.tables.TableParserI;
import de.cebitec.readXplorer.ui.importer.dataTable.ImportTableWizardPanel;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.tableVisualization.PosTablePanel;
import de.cebitec.readXplorer.view.tableVisualization.TableTopComponent;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.supercsv.prefs.CsvPreference;

@ActionID(
        category = "File",
        id = "de.cebitec.readXplorer.ui.importer.actions.ImportTableWizardAction"
)
@ActionRegistration(
        iconBase = "de/cebitec/readXplorer/ui/importer/import.png",
        displayName = "#CTL_ImportTableWizardAction"
)
@ActionReference(path = "Menu/File", position = 1481)
@NbBundle.Messages("CTL_ImportTableWizardAction=Import any data table")
public final class ImportTableWizardAction implements ActionListener {

    private TableTopComponent topComp;

    /**
     * Action to import an arbitrary table into ReadXplorer and display it in a
     * new TopComonent.
     * @param context A LoginCookie to assure, that a DB has already been
     * opened.
     */
    public ImportTableWizardAction(LoginCookie context) {
        this.topComp = new TableTopComponent();
    }

    @NbBundle.Messages({"WizardTitle=Import any data table wizard",
            "ErrorHeader=Import Table Error"})
    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new ImportTableWizardPanel());
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.WizardTitle());
        
        //wizard has finished
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final TableType tableType = (TableType) wiz.getProperty(ImportTableWizardPanel.PROP_TABLE_TYPE);
            final String fileLocation = (String) wiz.getProperty(ImportTableWizardPanel.PROP_SELECTED_FILE);
            final PersistantReference ref = (PersistantReference) wiz.getProperty(ImportTableWizardPanel.PROP_SELECTED_REF);
            final boolean autoDelimiter = (boolean) wiz.getProperty(ImportTableWizardPanel.PROP_AUTO_DELEMITER);
            final CsvPreference csvPref = (CsvPreference) wiz.getProperty(ImportTableWizardPanel.PROP_SEL_PREF);
            
            CsvTableParser parser = new CsvTableParser();
            parser.setAutoDelimiter(autoDelimiter);
            parser.setCsvPref(csvPref);
            parser.setTableModel(tableType.getName());
            
            //parse file in readable format for a table
            final File tableFile = new File(fileLocation);
            try {
                List<List<?>> tableData = parser.parseTable(tableFile);
                final UneditableTableModel tableModel = TableUtils.transformDataToTableModel(tableData);

                //open table visualization panel with given reference for jumping to the position
                SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
                    @Override
                    public void run() {
                        PosTablePanel tablePanel = new PosTablePanel(tableModel);
                        tablePanel.setReferenceGenome(ref);
                        tablePanel.setTableType(tableType);
                        checkAndOpenRefViewer(ref, tablePanel);

                        String panelName = "Imported table from: " + tableFile.getName();
                        topComp = (TableTopComponent) WindowManager.getDefault().findTopComponent("TableTopComponent");
                        topComp.open();
                        topComp.openTableTab(panelName, tablePanel);
                    }
                });
            } catch (ParsingException ex) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), ex.getMessage() + "\nFile: " + fileLocation,
                        Bundle.ErrorHeader(), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void checkAndOpenRefViewer(PersistantReference ref, PosTablePanel tablePanel) {
        @SuppressWarnings("unchecked")
        Collection<ViewController> viewControllers = (Collection<ViewController>) CentralLookup.getDefault().lookupAll(ViewController.class);
        boolean alreadyOpen = false;
        for (ViewController tmpVCon : viewControllers) {
            if (tmpVCon.getCurrentRefGen().equals(ref)) {
                alreadyOpen = true;
                tablePanel.setBoundsInfoManager(tmpVCon.getBoundsManager());
                break;
            }
        }
        
        if (!alreadyOpen) {
            //open reference genome now
            AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
            appPanelTopComponent.open();
            ViewController viewController = appPanelTopComponent.getLookup().lookup(ViewController.class);
            viewController.openGenome(ref);
            appPanelTopComponent.setName(viewController.getDisplayName());
            appPanelTopComponent.requestActive();
            tablePanel.setBoundsInfoManager(viewController.getBoundsManager());
        }
    }

}
