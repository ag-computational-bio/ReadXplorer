/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.ui.importer.actions;


import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.tables.CsvTableParser;
import de.cebitec.readxplorer.parser.tables.TableParserI;
import de.cebitec.readxplorer.parser.tables.TableType;
import de.cebitec.readxplorer.parser.tables.XlsTranscriptomeTableParser;
import de.cebitec.readxplorer.ui.importer.TranscriptomeTableViewI;
import de.cebitec.readxplorer.ui.importer.datatable.ImportTableWizardPanel;
import de.cebitec.readxplorer.ui.tablevisualization.PosTablePanel;
import de.cebitec.readxplorer.ui.tablevisualization.TableTopComponent;
import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import de.cebitec.readxplorer.ui.visualisation.TableVisualizationHelper;
import de.cebitec.readxplorer.utils.UneditableTableModel;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.supercsv.prefs.CsvPreference;


@ActionID(
         category = "File",
         id = "de.cebitec.readxplorer.ui.importer.actions.ImportTableWizardAction"
)
@ActionRegistration(
         iconBase = "de/cebitec/readxplorer/ui/importer/import.png",
         displayName = "#CTL_ImportTableWizardAction"
)
@ActionReference( path = "Menu/File", position = 1481 )
@NbBundle.Messages( "CTL_ImportTableWizardAction=Import any data table" )
public final class ImportTableWizardAction implements ActionListener {

    private TableTopComponent topComp;


    /**
     * Action to import an arbitrary table into ReadXplorer and display it in a
     * new TopComonent.
     * <p>
     * @param context A LoginCookie to assure, that a DB has already been
     *                opened.
     */
    public ImportTableWizardAction( LoginCookie context ) {
        this.topComp = new TableTopComponent();
    }


    @NbBundle.Messages( { "WizardTitle=Import any data table wizard",
                          "ErrorHeader=Import Table Error" } )
    @Override
    public void actionPerformed( ActionEvent e ) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>( 1 );
        panels.add( new ImportTableWizardPanel() );
        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( Bundle.WizardTitle() );

        //wizard has finished
        if( DialogDisplayer.getDefault().notify( wiz ) == WizardDescriptor.FINISH_OPTION ) {
            final TableType tableType = (TableType) wiz.getProperty( ImportTableWizardPanel.PROP_TABLE_TYPE );
            final String fileLocation = (String) wiz.getProperty( ImportTableWizardPanel.PROP_SELECTED_FILE );
            final String statsFileLocation = (String) wiz.getProperty( ImportTableWizardPanel.PROP_SELECTED_STATS_FILE );
            final PersistentReference ref = (PersistentReference) wiz.getProperty( ImportTableWizardPanel.PROP_SELECTED_REF );
            final boolean autoDelimiter = (boolean) wiz.getProperty( ImportTableWizardPanel.PROP_AUTO_DELEMITER );
            final CsvPreference csvPref = (CsvPreference) wiz.getProperty( ImportTableWizardPanel.PROP_SEL_PREF );
            final TableParserI parser = (TableParserI) wiz.getProperty( ImportTableWizardPanel.PROP_SEL_PARSER );
            final TranscriptomeTableViewI tableView = Lookup.getDefault().lookup( TranscriptomeTableViewI.class );

            if( parser instanceof CsvTableParser ) {
                CsvTableParser csvParser = (CsvTableParser) parser;
                csvParser.setAutoDelimiter( autoDelimiter );
                csvParser.setCsvPref( csvPref );
                csvParser.setTableModel( tableType );
            }

            //parse file in readable format for a table
            final File tableFile = new File( fileLocation );
            try {
//                List<List<?>> tableData = parser.parseTable(tableFile);

                if( tableView != null && (tableType.equals( TableType.OPERON_DETECTION_JR ) ||
                     tableType.equals( TableType.RPKM_ANALYSIS_JR ) ||
                     tableType.equals( TableType.NOVEL_TRANSCRIPT_DETECTION_JR ) ||
                     tableType.equals( TableType.TSS_DETECTION_JR )) ) {

                    //xls handling of transcriptome tables
                    if( parser instanceof XlsTranscriptomeTableParser ) {
                        XlsTranscriptomeTableParser xlsParser = (XlsTranscriptomeTableParser) parser;
                        xlsParser.setTableType( tableType );
                        xlsParser.parseTable( new File( fileLocation ) );
                        DefaultTableModel model = xlsParser.getModel();
                        Map<String, String> secondSheetMap = xlsParser.getSecondSheetMap();
                        Map<String, String> secondSheetMapThirdCol = xlsParser.getSecondSheetMapThirdCol();
                        tableView.processXlsInput( ref, model, secondSheetMap, secondSheetMapThirdCol );

                    } else if( parser instanceof CsvTableParser ) {
                        CsvTableParser csvParser = (CsvTableParser) parser;
                        final File parametersFile = new File( statsFileLocation );
                        List<List<?>> tableData = csvParser.parseTable( tableFile );
                        csvParser.setTableModel( TableType.STATS_TABLE );
                        List<List<?>> tableData2 = parser.parseTable( parametersFile );
                        tableView.processCsvInput( tableData, tableData2, tableType, ref );

                    }
                } else {
                    List<List<?>> tableData = parser.parseTable( tableFile );

                    final UneditableTableModel tableModel = TableUtils.transformDataToTableModel( tableData );

                    //open table visualization panel with given reference for jumping to the position
                    SwingUtilities.invokeLater( new Runnable() { //because it is not called from the swing dispatch thread
                        @Override
                        public void run() {
                            PosTablePanel tablePanel = new PosTablePanel( tableModel, tableType );
                            tablePanel.setReferenceGenome( ref );
                            TableVisualizationHelper.checkAndOpenRefViewer( ref, tablePanel );

                            String panelName = "Imported table from: " + tableFile.getName();
                            topComp = (TableTopComponent) WindowManager.getDefault().findTopComponent( "TableTopComponent" );
                            topComp.open();
                            topComp.openTableTab( panelName, tablePanel );
                        }


                    } );
                }

            } catch( ParsingException ex ) {
                JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), ex.getMessage() + "\nFile: " + fileLocation,
                                               Bundle.ErrorHeader(), JOptionPane.INFORMATION_MESSAGE );
            }
        }
    }


}
