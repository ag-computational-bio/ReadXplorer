
package de.cebitec.readxplorer.transcriptomeanalyses.main;

/*
 * GeneStartsResultPanel.java
 *
 * Created on 27.01.2012, 14:31:03
 */

import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.Operon;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.OperonAdjacency;
import de.cebitec.readxplorer.transcriptomeanalyses.enums.TableType;
import de.cebitec.readxplorer.transcriptomeanalyses.featureTableExport.SequinTableFormatExporter;
import de.cebitec.readxplorer.transcriptomeanalyses.featureTableExport.SequinTableSettingsWizardPanel;
import de.cebitec.readXplorer.util.LineWrapCellRenderer;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.tableVisualization.TableComparatorProvider;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickFilter;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;


/**
 * This panel is capable of showing a table with detected operons and contains
 * an export button, which exports the data into an excel file.
 *
 * @author -Rolf Hilker-
 */
public class ResultPanelOperonDetection extends ResultTablePanel implements
        Observer {

    private static final long serialVersionUID = 1L;
    public static final String OPERONS_TOTAL = "Total number of detected operons";
    public static final String OPERONS_TWO_GENES = "Operons with exactly two genes";
    public static final String OPERONS_BIGGEST = "Operon with biggest number of genes";

    public static final String OPERONS_BACKGROUND_THRESHOLD = "Minimum number of spanning reads (Background threshold)";
    public final TableType tableType = TableType.OPERON_TABLE;
    private BoundsInfoManager boundsInfoManager;
    private ReferenceViewer referenceViewer;
    private PersistentReference persistantRef;
    private OperonDetectionResult operonResult;
    private HashMap<String, Object> operonDetStats;
    private final TableRightClickFilter<UneditableTableModel> tableFilter;
    private ProgressHandle progresshandle;
    private HashMap<Integer, Operon> operonsInHash;
    String separator = "";
    Integer prefixLength = 0;


    /**
     * This panel is capable of showing a table with detected operons and
     * contains an export button, which exports the data into an excel file.
     */
    public ResultPanelOperonDetection() {
        initComponents();
        final int posColumnIdx = 0;
        final int trackColumnIdx = 13;
        final int chromColumnIdx = 4;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumnIdx, trackColumnIdx );
        this.operonDetectionTable.getTableHeader().addMouseListener( tableFilter );
        this.initStatsMap();

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.operonDetectionTable.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showPosition( operonDetectionTable, posColumnIdx, chromColumnIdx, boundsInfoManager );
            }


        } );
    }


    /**
     * Initializes the statistics map.
     */
    private void initStatsMap() {
        operonDetStats = new HashMap<>();
        operonDetStats.put( OPERONS_TOTAL, 0 );
        operonDetStats.put( OPERONS_TWO_GENES, 0 );
        operonDetStats.put( OPERONS_BIGGEST, 0 );
        operonDetStats.put( ResultPanelTranscriptionStart.MAPPINGS_COUNT, 0.0 );
        operonDetStats.put( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH, 0.0 );
        operonDetStats.put( ResultPanelTranscriptionStart.MAPPINGS_MILLION, 0.0 );
        operonDetStats.put( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS, 0.0 );

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        operonDetectionTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        statisticsButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        removeFpButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        operonDetectionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Operon Start", "Feature 1", "Feature 2", "Strand", "Start Feature 1", "Start Feature 2", "Fals Positive", "Finished", "Spanning Reads", "Operon String", "Number Of Genes", "Chromosome", "Chromosome ID", "Track", "Track ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, false, true, true, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(operonDetectionTable);
        if (operonDetectionTable.getColumnModel().getColumnCount() > 0) {
            operonDetectionTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title12")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title0")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title7")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title1")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title2")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title8")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title14")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title11")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title6")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title10")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title15")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title3")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title9_1")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title9")); // NOI18N
            operonDetectionTable.getColumnModel().getColumn(14).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title10_1")); // NOI18N
        }

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.exportButton.text")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.statisticsButton.text")); // NOI18N
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsButtonActionPerformed(evt);
            }
        });

        jButton1.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        removeFpButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.removeFpButton.text")); // NOI18N
        removeFpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFpButtonActionPerformed(evt);
            }
        });

        jButton2.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statisticsButton)
                .addGap(18, 18, 18)
                .addComponent(removeFpButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 266, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(exportButton))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(statisticsButton)
                        .addComponent(removeFpButton)
                        .addComponent(jButton2))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(exportButton)
                        .addComponent(jButton1))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        List<Operon> operons = updateOperonResults();
        this.operonResult.setResults( operons );
        TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), operonResult );
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog( this, new OperonDetectionStatsPanel( operonDetStats ), "Operon Detection Statistics", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_statisticsButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        final String wizardName = "Sequin Feature Table Export";
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add( new SequinTableSettingsWizardPanel( wizardName ) );
        String[] steps = new String[panels.size()];
        for( int i = 0; i < panels.size(); i++ ) {
            Component c = panels.get( i ).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if( c instanceof JComponent ) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i );
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, steps );
                jc.putClientProperty( WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true );
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DISPLAYED, true );
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_NUMBERED, true );
            }
        }
        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( panels ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( "Sequin Feature Table Export" );
        if( DialogDisplayer.getDefault().notify( wiz ) == WizardDescriptor.FINISH_OPTION ) {

            final String featureName = (String) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_FEATURE_NAME );
            final boolean isParsingLocusTagSelected = (boolean) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_PARSING_LOCUS_TAG );

            if( isParsingLocusTagSelected ) {
                separator = (String) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_SEPARATOR );
                prefixLength = (Integer) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_STRAIN_LENGTH );
            }
            this.progresshandle = ProgressHandleFactory.createHandle( "Export of feature table!" );
            ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( new String[]{ "tbl" }, "Table files for Sequin export" ) {
                @Override
                public void save( String fileLocation ) {
                    progresshandle.start( 4 );
                    progresshandle.progress( 1 );
                    SequinTableFormatExporter exporter = new SequinTableFormatExporter( new File( fileLocation ), null, (ArrayList<Operon>) operonResult.getResults(), null, tableType, featureName, separator, prefixLength, isParsingLocusTagSelected ); //To change body of generated methods, choose Tools | Templates.
                    progresshandle.progress( 2 );
                    exporter.start();
                    progresshandle.progress( 3 );
                    progresshandle.progress( 4 );
                    JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), "Export was successful!",
                                                   "Export was successful!", JOptionPane.INFORMATION_MESSAGE );
                    progresshandle.finish();
                }


                @Override
                public void open( String fileLocation ) {
                    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
                }


            };

            fileChooser.openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );

        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void removeFpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFpButtonActionPerformed
        // delete all false positive TSS from table and TSS array
        List<Operon> operons = this.updateOperonResults();
        DefaultTableModel tableModel = (DefaultTableModel) operonDetectionTable.getModel();
        List<Integer> valuesToRemove = new ArrayList<>();
        int columnNo = operonDetectionTable.getRowCount();
        for( int i = 0; i < columnNo; i++ ) {
            Integer posTableAti = (Integer) operonDetectionTable.getValueAt( i, 0 );
            Operon op = operonsInHash.get( posTableAti );
            boolean isFalsePositive = (boolean) operonDetectionTable.getValueAt( i, 6 );
            if( isFalsePositive ) {
                operonsInHash.remove( posTableAti );
                valuesToRemove.add( i );
                operons.remove( op );
            }
        }

        for( int i = valuesToRemove.size() - 1; i >= 0; i-- ) {
            Integer x = valuesToRemove.get( i );
            tableModel.removeRow( x );
        }

        operonDetectionTable.setModel( tableModel );
        operonResult.setResults( operons );
    }//GEN-LAST:event_removeFpButtonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.progresshandle = ProgressHandleFactory.createHandle( "Import Reference File" );
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( new String[]{ "txt" }, "Text file" ) {
            @Override
            public void save( String fileLocation ) {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }


            @Override
            public void open( String fileLocation ) {
                progresshandle.start( 3 );
                List<Operon> currentOperons = updateOperonResults();
                HashMap<String, Operon> operons = new HashMap<>();

                for( Operon operon : currentOperons ) {
                    if( operon.isFwd() ) {
                        if( !operons.containsKey( operon.getOperonAdjacencies().get( 0 ).getFeature1().getLocus() ) ) {
                            operons.put( operon.getOperonAdjacencies().get( 0 ).getFeature1().getLocus(), operon );
                        }
                    }
                    else {
                        if( !operons.containsKey( operon.getOperonAdjacencies().get( operon.getOperonAdjacencies().size() - 1 ).getFeature2().getLocus() ) ) {
                            operons.put( operon.getOperonAdjacencies().get( operon.getOperonAdjacencies().size() - 1 ).getFeature2().getLocus(), operon );
                        }
                    }

                }
                currentOperons = new ArrayList<>();

                try( BufferedReader br = new BufferedReader( new FileReader( fileLocation ) ) ) {
                    String line = br.readLine();
                    progresshandle.progress( 1 );
                    if( line.startsWith( "#" ) ) {
                        line = br.readLine();
                    }
                    // skip header line !
                    while( line != null && !line.isEmpty( ) ) {

                        String[] split = line.split( "\t" );
                        String locus = split[0];
                        if( operons.containsKey( locus ) ) {
                            Operon op = operons.get( locus );
                            op.setStartPositionOfTranscript( Integer.valueOf( split[1] ) );

                            String[] tsSites = split[3].split( "-" );
                            String[] minus10Region = split[6].split( "-" );
                            String[] minus35Region = split[7].split( "-" );

                            for( int i = 0; i < tsSites.length; i++ ) {
                                Integer tss = Integer.valueOf( tsSites[i] );
                                ArrayList<Integer[]> entry = new ArrayList<>();
                                op.addTss( tss );

                                String[] promotor10 = minus10Region[i].split( ";" );
                                Integer[] minus10 = new Integer[2];
                                minus10[0] = Integer.valueOf( promotor10[0] );
                                minus10[1] = Integer.valueOf( promotor10[1] );

                                String[] promotor35 = minus35Region[i].split( ";" );
                                Integer[] minus35 = new Integer[2];
                                minus35[0] = Integer.valueOf( promotor35[0] );
                                minus35[1] = Integer.valueOf( promotor35[1] );

                                entry.add( minus35 );
                                entry.add( minus10 );
                                op.addTssToPromotor( tss, minus35, minus10 );
                            }

                            String[] utrs = split[4].split( "-" );
                            for( String utr : utrs ) {
                                op.addUtrs( Integer.valueOf( utr ) );
                            }

                            String[] rbss = split[5].split( "-" );
                            for( String rbs : rbss ) {
                                String[] promotor = rbs.split( ";" );
                                op.addRbs( Integer.valueOf( promotor[0] ), Integer.valueOf( promotor[1] ) );
                            }

                            operons.put( locus, op );
                        }
                        line = br.readLine();
                    }

                }
                catch( FileNotFoundException ex ) {
                    JOptionPane.showMessageDialog( null, "The reference file could not be opend or does not exists." + ex.toString(), "Problem with filehandling", JOptionPane.CLOSED_OPTION );
                }
                catch( IOException ex ) {
                    JOptionPane.showMessageDialog( null, "Problems with the reference file." + ex.toString(), "Problem with IO!", JOptionPane.CLOSED_OPTION );
                }

                operonsInHash = new HashMap<>();
                for( Operon operon : operons.values() ) {
                    currentOperons.add( operon );
                }

                operonResult.setResults( currentOperons );
                addResult( operonResult );
                progresshandle.progress( 2 );
                progresshandle.progress( 3 );
                JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), "Import was successful!",
                                               "Export was successful!", JOptionPane.INFORMATION_MESSAGE );

                progresshandle.finish();
            }


        };

        fileChooser.openFileChooser( ReadXplorerFileChooser.OPEN_DIALOG );


    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable operonDetectionTable;
    private javax.swing.JButton removeFpButton;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables


    /**
     * Adds the data from this OperonDetectionResult to the data already
     * available in this result panel. All statistics etc. are also updated.
     *
     * @param newResult the result to add
     */
    @Override
    public void addResult( ResultTrackAnalysis newResult ) {
        if( newResult instanceof OperonDetectionResult ) {
            OperonDetectionResult operonResultNew = (OperonDetectionResult) newResult;
            final int nbColumns = 16;
            final List<Operon> operons = new ArrayList<>( operonResultNew.getResults() );
            this.operonsInHash = new HashMap<>();

            if( this.operonResult == null ) {
                this.operonResult = operonResultNew;
            }
            else {
                if( this.operonResult.getResults().isEmpty() ) {
                    this.operonResult.getResults().addAll( operonResultNew.getResults() );
                }
            }

            final DefaultTableModel model = (DefaultTableModel) operonDetectionTable.getModel();
            if( model.getRowCount() > 0 ) {
                for( int i = model.getRowCount() - 1; i >= 0; i-- ) {
                    model.removeRow( i );
                }
            }
            SwingUtilities.invokeLater( new Runnable() { //because it is not called from the swing dispatch thread
                @Override
                public void run() {
                    LineWrapCellRenderer lineWrapCellRenderer = new LineWrapCellRenderer();
                    operonDetectionTable.getColumnModel().getColumn( 1 ).setCellRenderer( lineWrapCellRenderer );
                    operonDetectionTable.getColumnModel().getColumn( 2 ).setCellRenderer( lineWrapCellRenderer );
                    operonDetectionTable.getColumnModel().getColumn( 4 ).setCellRenderer( lineWrapCellRenderer );
                    operonDetectionTable.getColumnModel().getColumn( 5 ).setCellRenderer( lineWrapCellRenderer );
                    operonDetectionTable.getColumnModel().getColumn( 8 ).setCellRenderer( lineWrapCellRenderer );

                    int biggestOperon = 0;
                    int numberOfTwoGenesOperons = 0;

                    for( Operon operon : operons ) {
                        String annoName1 = "";
                        String annoName2 = "";
                        String strand;
                        if( operon.isFwd() ) {
                            strand = SequenceUtils.STRAND_FWD_STRING + "\n";
                        }
                        else {
                            strand = SequenceUtils.STRAND_REV_STRING + "\n";
                        }
                        String startAnno1 = "";
                        String startAnno2 = "";
                        String spanningReads = "";

                        int startFirstFeature = 0;
                        int chromID = operon.getOperonAdjacencies().get( 0 ).getFeature1().getChromId();
                        if( operon.getStartPositionOfOperonTranscript() == 0 ) {
                            if( operon.isFwd() ) {
                                startFirstFeature = operon.getOperonAdjacencies().get( 0 ).getFeature1().getStart();
                            }
                            else {
                                startFirstFeature = operon.getOperonAdjacencies().get( operon.getOperonAdjacencies().size() - 1 ).getFeature2().getStop();
                            }
                            operon.setStartPositionOfTranscript( startFirstFeature );
                        }
                        operonsInHash.put( operon.getStartPositionOfOperonTranscript(), operon );

                        if( operon.isFwd() ) {
                            for( OperonAdjacency opAdj : operon.getOperonAdjacencies() ) {

                                annoName1 += opAdj.getFeature1().getLocus() + "\n";
                                annoName2 += opAdj.getFeature2().getLocus() + "\n";
                                startAnno1 += opAdj.getFeature1().getStart() + "\n";
                                startAnno2 += opAdj.getFeature2().getStart() + "\n";

                                spanningReads += opAdj.getSpanningReads() + "\n";
                            }
                        }
                        else {
                            for( int i = operon.getOperonAdjacencies().size() - 1; i >= 0; i-- ) {
                                OperonAdjacency opAdj = operon.getOperonAdjacencies().get( i );

                                annoName1 += opAdj.getFeature2().getLocus() + "\n";
                                annoName2 += opAdj.getFeature1().getLocus() + "\n";
                                startAnno1 += opAdj.getFeature2().getStop() + "\n";
                                startAnno2 += opAdj.getFeature1().getStop() + "\n";
                                spanningReads += opAdj.getSpanningReads() + "\n";
                            }
                        }
                        final Object[] rowData = new Object[nbColumns];
                        int i = 0;
                        rowData[i++] = operon.getStartPositionOfOperonTranscript();
                        rowData[i++] = annoName1;
                        rowData[i++] = annoName2;
                        rowData[i++] = strand;
                        rowData[i++] = startAnno1;
                        rowData[i++] = startAnno2;
                        rowData[i++] = operon.isFalsPositive();
                        rowData[i++] = operon.isConsidered();
                        rowData[i++] = spanningReads;
                        rowData[i++] = operon.toOperonString();
                        rowData[i++] = operon.getNbOfGenes();
                        if( operon.getNbOfGenes() == 2 ) {
                            numberOfTwoGenesOperons++;
                        }

                        if( operon.getNbOfGenes() > biggestOperon ) {
                            biggestOperon = operon.getNbOfGenes();
                        }

                        rowData[i++] = operonResult.getChromosomeMap().get( operon.getOperonAdjacencies().get( 0 ).getFeature1().getChromId() );
                        rowData[i++] = chromID;
                        rowData[i++] = operonResult.getTrackMap().get( operon.getTrackId() );
                        rowData[i++] = operon.getTrackId();
                        if( !annoName1.isEmpty() && !annoName2.isEmpty() ) {
                            model.addRow( rowData );
                        }
                    }

                    TableRowSorter<TableModel> sorter = new TableRowSorter<>();
                    operonDetectionTable.setRowSorter( sorter );
                    sorter.setModel( model );

                    TableComparatorProvider.setStringComparator( sorter, 1 );
                    TableComparatorProvider.setStringComparator( sorter, 2 );
                    TableComparatorProvider.setStringComparator( sorter, 4 );
                    TableComparatorProvider.setStringComparator( sorter, 5 );

                    TableComparatorProvider.setPersistentTrackComparator( sorter, 3 );

                    operonDetStats.put( OPERONS_TOTAL, (Integer) operonDetStats.get( OPERONS_TOTAL ) + operons.size() );
                    operonDetStats.put( OPERONS_TWO_GENES, (Integer) operonDetStats.get( OPERONS_TWO_GENES ) + numberOfTwoGenesOperons );
                    operonDetStats.put( OPERONS_BIGGEST, (Integer) operonDetStats.get( OPERONS_BIGGEST ) + biggestOperon );
                    operonDetStats.put( ResultPanelTranscriptionStart.MAPPINGS_COUNT, (Double) operonDetStats.get( ResultPanelTranscriptionStart.MAPPINGS_COUNT ) + operonResult.getStats().getMappingCount() );
                    operonDetStats.put( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH, (Double) operonDetStats.get( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH ) + operonResult.getStats().getAverageReadLength() );
                    operonDetStats.put( ResultPanelTranscriptionStart.MAPPINGS_MILLION, (Double) operonDetStats.get( ResultPanelTranscriptionStart.MAPPINGS_MILLION ) + operonResult.getStats().getMappingsPerMillion() );
                    operonDetStats.put( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS, (Double) operonDetStats.get( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS ) + operonResult.getStats().getBgThreshold() );

                    operonResult.setStatsAndParametersMap( operonDetStats );
                }


            } );
        }
    }


    /**
     * @return The number of detected operons
     */
    @Override
    public int getDataSize() {
        return this.operonResult.getResults().size();
    }


    /**
     * Set the reference viewer needed for updating the currently shown position
     * and extracting the reference sequence.
     *
     * @param referenceViewer the reference viewer belonging to this analysis
     *                        result
     */
    public void setReferenceViewer( ReferenceViewer referenceViewer ) {
        this.boundsInfoManager = referenceViewer.getBoundsInformationManager();
        this.referenceViewer = referenceViewer;
    }


    public void setPersistentReference( PersistentReference reference ) {
        this.persistantRef = reference;
    }


    /**
     * Updates the list of operons. It checks the three checkboxes in the table
     * if the value is not equal to the value in the operon instance, the it
     * will be set as in the table. This method should allways be executin
     * before working with the operon instances.
     *
     * @return updated operon list.
     */
    private List<Operon> updateOperonResults() {
        List<Operon> operons = operonResult.getResults();
        HashMap<Integer, Operon> tmpHash = new HashMap<>();
        tmpHash.putAll( this.operonsInHash );

        for( int i = 0; i < operonDetectionTable.getRowCount(); i++ ) {
            Integer posTableAti = (Integer) operonDetectionTable.getValueAt( i, 0 );
            if( tmpHash.containsKey( posTableAti ) ) {

                if( (Boolean) operonDetectionTable.getValueAt( i, 7 ) ) {
                    this.operonsInHash.get( posTableAti ).setIsConsidered( true );
                }
                else {
                    this.operonsInHash.get( posTableAti ).setIsConsidered( false );
                }
                if( (Boolean) operonDetectionTable.getValueAt( i, 6 ) ) {
                    this.operonsInHash.get( posTableAti ).setFalsPositive( true );
                }
                else {
                    this.operonsInHash.get( posTableAti ).setFalsPositive( false );
                }

                operonsInHash.get( posTableAti ).setStartPositionOfTranscript( (Integer) operonDetectionTable.getValueAt( i, 0 ) );
                tmpHash.remove( posTableAti );
            }
        }

        for( Integer key : tmpHash.keySet() ) {
            Operon operon = tmpHash.get( key );
            operonsInHash.remove( key );
            operons.remove( operon );
        }
        return operons;
    }


    @Override
    public void update( Object args ) {
//        if (args instanceof RbsMotifSearchPanel) {
//            RbsMotifSearchPanel panel = (RbsMotifSearchPanel) args;
//            if (this.model == null) {
//                this.model = new MotifSearchModel(referenceViewer);
//                this.model.storeRbsAnalysisResults(panel.getUpstreamRegions(),
//                        panel.getRbsStarts(), panel.getRbsShifts(),
//                        panel.getParams(), this.updateTssResults(),
//                        null);
//            } else {
//                this.model.storeRbsAnalysisResults(panel.getUpstreamRegions(),
//                        panel.getRbsStarts(), panel.getRbsShifts(),
//                        panel.getParams(), this.updateTssResults(),
//                        null);
//            }
//        }
//
//        if (args instanceof MotifSearchPanel) {
//            MotifSearchPanel panel = (MotifSearchPanel) args;
//            if (this.model == null) {
//                this.model = new MotifSearchModel(referenceViewer);
//                this.model.storePromoterAnalysisResults(panel.getUpstreamRegions(),
//                        panel.getMinus10Starts(), panel.getMinus35Starts(),
//                        panel.getMinus10Shifts(), panel.getMinus35Shifts(),
//                        panel.getParams(), this.updateTssResults(), null);
//            } else {
//                this.model.storePromoterAnalysisResults(panel.getUpstreamRegions(),
//                        panel.getMinus10Starts(), panel.getMinus35Starts(),
//                        panel.getMinus10Shifts(), panel.getMinus35Shifts(),
//                        panel.getParams(), this.updateTssResults(), null);
//            }
//        }
    }


}
