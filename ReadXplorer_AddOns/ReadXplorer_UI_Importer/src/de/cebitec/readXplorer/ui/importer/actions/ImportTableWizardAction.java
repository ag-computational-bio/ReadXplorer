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
package de.cebitec.readXplorer.ui.importer.actions;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.api.cookies.LoginCookie;
import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.tables.CsvTableParser;
import de.cebitec.readXplorer.parser.tables.TableParserI;
import de.cebitec.readXplorer.parser.tables.TableType;
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
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;
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
     *
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
            final TableParserI parser = (TableParserI) wiz.getProperty(ImportTableWizardPanel.PROP_SEL_PARSER);

            if (parser instanceof CsvTableParser) {
                CsvTableParser csvParser = (CsvTableParser) parser;
                csvParser.setAutoDelimiter(autoDelimiter);
                csvParser.setCsvPref(csvPref);
                if (tableType == TableType.TSS_DETECTION_JR) {
                    CellProcessor[] TSS_CELL_PROCESSOR = getTssCellProcessor();
                    csvParser.setCellProscessors(TSS_CELL_PROCESSOR);
                }
                csvParser.setTableModel(tableType.getName());
            }

            //parse file in readable format for a table
            final File tableFile = new File(fileLocation);
            try {
                List<List<?>> tableData = parser.parseTable(tableFile);
                final UneditableTableModel tableModel = TableUtils.transformDataToTableModel(tableData);

                //open table visualization panel with given reference for jumping to the position
                SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
                    @Override
                    public void run() {
                        if (tableType == TableType.TSS_DETECTION_JR) {
//                            ExcelImporter importer = new ExcelImporter();
//                            imorter.setUpTSSTable(List<List<?>> fstSheet, List<List<?>> sndSheet,
//                                    ReferenceViewer refViewer, 
//                                    TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp):
                        } else {
                            PosTablePanel tablePanel = new PosTablePanel(tableModel);
                            tablePanel.setReferenceGenome(ref);
                            tablePanel.setTableType(tableType);
                            checkAndOpenRefViewer(ref, tablePanel);

                            String panelName = "Imported table from: " + tableFile.getName();
                            topComp = (TableTopComponent) WindowManager.getDefault().findTopComponent("TableTopComponent");
                            topComp.open();
                            topComp.openTableTab(panelName, tablePanel);
                        }
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

    /**
     * Generates a cellprocessor for the tss analysis result table.
     *
     * @return List of CellProcessors.
     */
    private CellProcessor[] getTssCellProcessor() {

        return new CellProcessor[]{
            new ParseInt(), // Position
            new ParseBool(), // Strand
            null, // Comment
            new ParseInt(), // Read Starts
            new ParseDouble(), // Rel. Count
            null, // Feature Name
            null, // Feature Locus
            new ParseInt(), // Offset
            new ParseInt(), // Dist. To Start
            new ParseInt(), // Dist. To Stop
            null, // Sequence
            new ParseBool(), // Leaderless
            new ParseBool(), // Putative TLS-Shift
            new ParseBool(), // Intragenic TSS
            new ParseBool(), // Intergenic TSS
            new ParseBool(), // Putative Antisense
            new ParseBool(), // Putative 5'-UTR Antisense
            new ParseBool(), // Putative 3'-UTR Antisense
            new ParseBool(), // Putative Intragenic Antisense
            new ParseBool(), // Assigned To Stable RNA
            new ParseBool(), // False Positive
            new ParseBool(), // Selected For Upstream Region Analysis
            new ParseBool(), // Finished
            new ParseInt(), // Gene Start
            new ParseInt(), // Gene Stop
            new ParseInt(), // Gene Length In Bp	
            new ParseInt(), // Frame
            null, // Gene Product
            null, // Start Codon
            null, // Stop Codon
            null, // Chromosome	
            new ParseInt(), // Chrom ID	
            new ParseInt() //Track ID
        };
    }

}
