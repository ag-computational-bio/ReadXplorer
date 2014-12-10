
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableFormatExporter;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableSettingsWizardPanel;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.tableVisualization.TableComparatorProvider;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickDeletion;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickFilter;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;


/**
 *
 * @author jritter
 */
public class NovelRegionResultPanel extends ResultTablePanel {

    private NovelRegionResult novelRegionResults;
    private BoundsInfoManager boundsInfoManager;
    private ReferenceViewer referenceViewer;
    private PersistentReference persistantRef;
    private HashMap<String, Object> statisticsMap;
    private final TableRightClickFilter<UneditableTableModel> tableFilter;
    private final TableRightClickDeletion<DefaultTableModel> rowDeletion = new TableRightClickDeletion<>();
    private HashMap<Integer, NovelTranscript> nrInHash;
    public final TableType tableType = TableType.NOVEL_TRANSCRIPTS_TABLE;
    public static final String NOVELREGION_DETECTION_MIN_LENGTH = "Minimal length of novel transcript";
    public static final String NOVELREGION_DETECTION_NO_OF_FEATURES = "Number of detected regions";
    public static final String NOVELREGION_DETECTION_NO_OF_REV_FEATURES = "Number of reverse features";
    public static final String NOVELREGION_DETECTION_NO_OF_FWD_FEATURES = "Number of forward features";
    public static final String NOVELREGION_DETECTION_NO_OF_CISANTISENSE = "Number of cis-antisense features";
    public static final String NOVELREGION_DETECTION_NO_OF_TRANSGENIC = "Number of transgenic features";
    private ProgressHandle progresshandle;
    String separator = "";
    Integer prefixLength = 0;


    /**
     * Creates new form NovelRegionResultPanel
     */
    public NovelRegionResultPanel() {
        initComponents();
        initStatsMap();
        final int posColumnIdx = 0;
        final int trackColumnIdx = 11;
        final int chromColumnIdx = 2;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumnIdx, trackColumnIdx );
        this.novelRegionTable.getTableHeader().addMouseListener( tableFilter );
        this.novelRegionTable.addMouseListener( rowDeletion );

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.novelRegionTable.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showPosition( novelRegionTable, posColumnIdx, chromColumnIdx, boundsInfoManager );
            }


        } );
    }


    /**
     * Initializes the statistics map.
     */
    private void initStatsMap() {
        statisticsMap = new HashMap<>();
        statisticsMap.put( NOVELREGION_DETECTION_NO_OF_FEATURES, 0 );
        statisticsMap.put( NOVELREGION_DETECTION_NO_OF_REV_FEATURES, 0 );
        statisticsMap.put( NOVELREGION_DETECTION_NO_OF_FWD_FEATURES, 0 );
        statisticsMap.put( NOVELREGION_DETECTION_NO_OF_CISANTISENSE, 0 );
        statisticsMap.put( NOVELREGION_DETECTION_NO_OF_TRANSGENIC, 0 );
        statisticsMap.put( ResultPanelTranscriptionStart.MAPPINGS_COUNT, 0.0 );
        statisticsMap.put( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH, 0.0 );
        statisticsMap.put( ResultPanelTranscriptionStart.MAPPINGS_MILLION, 0.0 );
        statisticsMap.put( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE, 0.0 );
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
        novelRegionTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        removeAllFalsePosButton = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        statisticsButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        novelRegionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Putative Start", "Strand", "False Positive", "Selection for Blast", "Finished", "Site", "Dropoff Position", "Length", "Sequence", "Chromosome", "Chrom. ID", "Track", "Track ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true, true, false, false, false, true, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(novelRegionTable);
        if (novelRegionTable.getColumnModel().getColumnCount() > 0) {
            novelRegionTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title0_1")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title1_1")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title3_1_1")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title7")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title12")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title2_1_1")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title5")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title6")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title8")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title10")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title11")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title9")); // NOI18N
            novelRegionTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.novelRegionTable.columnModel.title4_1")); // NOI18N
        }

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.jButton1.text_1")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeAllFalsePosButton, org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.removeAllFalsePosButton.text")); // NOI18N
        removeAllFalsePosButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllFalsePosButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(statisticsButton, org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.statisticsButton.text")); // NOI18N
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(NovelRegionResultPanel.class, "NovelRegionResultPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statisticsButton)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addGap(18, 18, 18)
                .addComponent(removeAllFalsePosButton)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(removeAllFalsePosButton)
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(statisticsButton)
                    .addComponent(jButton2)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        List<NovelTranscript> tss = this.updateNovelRegionResults();
        novelRegionResults.setResults( tss );

        NotificationWhenExportingPanel notification = new NotificationWhenExportingPanel();
        NotifyDescriptor nd = new NotifyDescriptor(
                notification, // instance of your panel
                "ATTENTION!", NotifyDescriptor.OK_CANCEL_OPTION, // it is Yes/No dialog ...
                NotifyDescriptor.INFORMATION_MESSAGE, // ... of a question type => a question mark icon
                null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                // otherwise specify options as:
                //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                NotifyDescriptor.OK_CANCEL_OPTION // default option is "Yes"
        );
        if( DialogDisplayer.getDefault().notify( nd ) == NotifyDescriptor.OK_OPTION ) {
            TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), this.novelRegionResults );
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog( this, new NovelRegionDetectionStatsPanel( statisticsMap ), "Novel Region Detection Statistics", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_statisticsButtonActionPerformed

    private void removeAllFalsePosButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllFalsePosButtonActionPerformed
        // delete all false positive detected novel regions from table and novelregion array
        List<NovelTranscript> novelRegions = this.updateNovelRegionResults();
        DefaultTableModel tableModel = (DefaultTableModel) novelRegionTable.getModel();
        List<Integer> valuesToRemove = new ArrayList<>();

        for( int i = 0; i < novelRegionTable.getRowCount(); i++ ) {
            Integer posTableAti = (Integer) novelRegionTable.getValueAt( i, 0 );
            NovelTranscript nr = nrInHash.get( posTableAti );
            boolean isFalsePositive = (boolean) novelRegionTable.getValueAt( i, 2 );
            if( isFalsePositive ) {
                nrInHash.remove( posTableAti );
                valuesToRemove.add( i );
                novelRegions.remove( nr );
            }
        }

        for( int i = valuesToRemove.size() - 1; i >= 0; i-- ) {
            Integer x = valuesToRemove.get( i );
            tableModel.removeRow( x );
        }

        novelRegionTable.setModel( tableModel );
        novelRegionTable.updateUI();
        novelRegionResults.setResults( novelRegions );
    }//GEN-LAST:event_removeAllFalsePosButtonActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        this.progresshandle = ProgressHandleFactory.createHandle( "Export to Fasta!" );
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( new String[]{ "fna", "faa", "ffn" }, "FASTA file" ) {
            @Override
            public void save( String fileLocation ) {
                progresshandle.start( 5 );
                List<String> sequences = new ArrayList<>();
                List<NovelTranscript> novelRegions = updateNovelRegionResults();
                progresshandle.progress( 1 );
                for( int i = 0; i < novelRegionTable.getRowCount(); i++ ) {
                    Integer posTableAti = (Integer) novelRegionTable.getValueAt( i, 0 );
                    NovelTranscript nr = nrInHash.get( posTableAti );
                    if( nr.isFwdDirection() ) {
                        sequences.add( "Start\t" + nr.getStartPosition() + "\tLength\t" + nr.getLength() + "\tDirection\tfwd" );
                    }
                    else {
                        sequences.add( "Start\t" + nr.getStartPosition() + "\tLength\t" + nr.getLength() + "\tDirection\trev" );
                    }
                    sequences.add( nr.getSequence() );
                    writeFastaFileForBlast( fileLocation, sequences );
                }
                progresshandle.progress( 2 );
                novelRegionResults.setResults( novelRegions );
                progresshandle.progress( 3 );
                progresshandle.progress( 5 );

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


    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        this.progresshandle = ProgressHandleFactory.createHandle( "Export to Fasta!" );
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( new String[]{ "fna", "faa", "ffn" }, "FASTA file" ) {
            @Override
            public void save( String fileLocation ) {
                progresshandle.start( 5 );
                List<String> sequences = new ArrayList<>();
                List<NovelTranscript> novelRegions = updateNovelRegionResults();
                progresshandle.progress( 1 );
                for( int i = 0; i < novelRegionTable.getRowCount(); i++ ) {
                    Integer posTableAti = (Integer) novelRegionTable.getValueAt( i, 0 );
                    NovelTranscript nr = nrInHash.get( posTableAti );
                    boolean isSelected = (boolean) novelRegionTable.getValueAt( i, 3 );
                    if( isSelected ) {
                        if( nr.isFwdDirection() ) {
                            sequences.add( "Start\t" + nr.getStartPosition() + "\tLength\t" + nr.getLength() + "\tDirection\tfwd" );
                        }
                        else {
                            sequences.add( "Start\t" + nr.getStartPosition() + "\tLength\t" + nr.getLength() + "\tDirection\trev" );
                        }
                        sequences.add( nr.getSequence() );
                        writeFastaFileForBlast( fileLocation, sequences );
                    }
                }
                progresshandle.progress( 2 );
                novelRegionResults.setResults( novelRegions );
                progresshandle.progress( 3 );
                progresshandle.progress( 5 );

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

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
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
                    progresshandle.start( 5 );
                    progresshandle.progress( 1 );
                    SequinTableFormatExporter exporter = new SequinTableFormatExporter( new File( fileLocation ), null, null, (ArrayList<NovelTranscript>) novelRegionResults.getResults(), tableType, featureName, separator, prefixLength, isParsingLocusTagSelected ); //To change body of generated methods, choose Tools | Templates.
                    progresshandle.progress( 2 );
                    exporter.start();
                    progresshandle.progress( 3 );
                    progresshandle.progress( 4 );
                    progresshandle.progress( 5 );

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
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable novelRegionTable;
    private javax.swing.JButton removeAllFalsePosButton;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables


    @Override
    public void addResult( ResultTrackAnalysis newResult ) {
        if( newResult instanceof NovelRegionResult ) {
            final NovelRegionResult novelRegResults = (NovelRegionResult) newResult;
            final List<NovelTranscript> novelReggions = new ArrayList<>( novelRegResults.getResults() );
            this.nrInHash = new HashMap<>();

            if( novelRegionResults == null ) {
                novelRegionResults = novelRegResults;
            }
            else {
                novelRegionResults.getResults().addAll( novelRegResults.getResults() );
            }
            SwingUtilities.invokeLater( new Runnable() { //because it is not called from the swing dispatch thread
                @Override
                public void run() {
                    final int nbColumns = 13;
                    int noFwdFeatures = 0;
                    int noRevFeatures = 0;
                    int noCisAntisense = 0;
                    int noTransgene = 0;

                    final DefaultTableModel model = (DefaultTableModel) novelRegionTable.getModel();

                    String strand;

                    for( NovelTranscript nr : novelReggions ) {

                        if( nr.isFwdDirection() ) {
                            strand = SequenceUtils.STRAND_FWD_STRING;
                            ++noFwdFeatures;
                        }
                        else {
                            strand = SequenceUtils.STRAND_REV_STRING;
                            ++noRevFeatures;
                        }

                        final Object[] rowData = new Object[nbColumns];
                        int position = nr.getStartPosition();
                        nrInHash.put( position, nr );
                        int i = 0;
                        rowData[i++] = position;
                        rowData[i++] = strand;
                        rowData[i++] = false;
                        rowData[i++] = false;
                        rowData[i++] = nr.isConsidered();
                        rowData[i++] = nr.getLocation();
                        if( nr.getLocation().equals( "cis-antisense" ) ) {
                            ++noCisAntisense;
                        }
                        else {
                            ++noTransgene;
                        }
                        rowData[i++] = nr.getDropOffPos();
                        rowData[i++] = nr.getLength();
                        rowData[i++] = nr.getSequence();
                        rowData[i++] = novelRegResults.getChromosomeMap().get( nr.getChromId() );
                        rowData[i++] = nr.getChromId();
                        rowData[i++] = novelRegResults.getTrackMap().get( nr.getTrackId() );
                        rowData[i++] = nr.getTrackId();

                        model.addRow( rowData );

                    }

                    //create statistics
                    statisticsMap.put( NOVELREGION_DETECTION_NO_OF_FEATURES, (Integer) statisticsMap.get( NOVELREGION_DETECTION_NO_OF_FEATURES ) + novelReggions.size() );
                    statisticsMap.put( NOVELREGION_DETECTION_NO_OF_REV_FEATURES, (Integer) statisticsMap.get( NOVELREGION_DETECTION_NO_OF_REV_FEATURES ) + noRevFeatures );
                    statisticsMap.put( NOVELREGION_DETECTION_NO_OF_FWD_FEATURES, (Integer) statisticsMap.get( NOVELREGION_DETECTION_NO_OF_FWD_FEATURES ) + noFwdFeatures );
                    statisticsMap.put( NOVELREGION_DETECTION_NO_OF_CISANTISENSE, (Integer) statisticsMap.get( NOVELREGION_DETECTION_NO_OF_CISANTISENSE ) + noCisAntisense );
                    statisticsMap.put( NOVELREGION_DETECTION_NO_OF_TRANSGENIC, (Integer) statisticsMap.get( NOVELREGION_DETECTION_NO_OF_TRANSGENIC ) + noTransgene );
                    statisticsMap.put( ResultPanelTranscriptionStart.MAPPINGS_COUNT, (Double) statisticsMap.get( ResultPanelTranscriptionStart.MAPPINGS_COUNT ) + novelRegResults.getStats().getMappingCount() );
                    statisticsMap.put( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH, (Double) statisticsMap.get( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH ) + novelRegResults.getStats().getAverageReadLength() );
                    statisticsMap.put( ResultPanelTranscriptionStart.MAPPINGS_MILLION, (Double) statisticsMap.get( ResultPanelTranscriptionStart.MAPPINGS_MILLION ) + novelRegResults.getStats().getMappingsPerMillion() );
                    statisticsMap.put( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE, (Double) statisticsMap.get( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE ) + novelRegResults.getStats().getBgThreshold() );

                    novelRegResults.setStatsAndParametersMap( statisticsMap );

                    TableRowSorter<TableModel> sorter = new TableRowSorter<>();
                    novelRegionTable.setRowSorter( sorter );
                    sorter.setModel( model );
                    TableComparatorProvider.setPersistentTrackComparator( sorter, 1 );
                }


            } );
        }
    }


    @Override
    public int getDataSize() {
        return this.novelRegionResults.getResults().size();
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
     *
     * @return a new list of NovelRegion instances.
     */
    private List<NovelTranscript> updateNovelRegionResults() {
        List<NovelTranscript> novelRegions = novelRegionResults.getResults();
        HashMap<Integer, NovelTranscript> tmpHash = new HashMap<>();
        tmpHash.putAll( this.nrInHash );

        for( int i = 0; i < novelRegionTable.getRowCount(); i++ ) {
            Integer posTableAti = (Integer) novelRegionTable.getValueAt( i, 0 );
            if( tmpHash.containsKey( posTableAti ) ) {

                if( (Boolean) novelRegionTable.getValueAt( i, 4 ) ) {
                    this.nrInHash.get( posTableAti ).setIsConsidered( true );
                }
                else {
                    this.nrInHash.get( posTableAti ).setIsConsidered( false );
                }
                if( (Boolean) novelRegionTable.getValueAt( i, 2 ) ) {
                    this.nrInHash.get( posTableAti ).setFalsePositive( true );
                }
                else {
                    this.nrInHash.get( posTableAti ).setFalsePositive( false );
                }

                tmpHash.remove( posTableAti );
            }
        }
        for( Integer key : tmpHash.keySet() ) {
            NovelTranscript nr = tmpHash.get( key );
            nrInHash.remove( key );
            novelRegions.remove( nr );
        }
        return novelRegions;
    }


    /**
     * Write the sequences of interest into a file in fasta format.
     *
     * @param fileLocation   Determination of the output.
     * @param seqsOfInterest Sequences of interest.
     */
    private void writeFastaFileForBlast( String fileLocation, List<String> seqsOfInterest ) {
        Writer writer = null;
        int cnt = 1;
        try {
            writer = new BufferedWriter( new OutputStreamWriter(
                    new FileOutputStream( fileLocation ) ) );
            for( String sequence : seqsOfInterest ) {
                if( cnt == 1 ) {
                    writer.write( ">" + sequence + "\n" );
                    cnt = 0;
                }
                else {
                    writer.write( sequence + "\n" );
                    cnt = 1;
                }
            }

            // report
        }
        catch( FileNotFoundException ex ) {
            Exceptions.printStackTrace( ex );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
        finally {
            try {
                writer.close();
            }
            catch( Exception ex ) {
            }
        }
    }


}
