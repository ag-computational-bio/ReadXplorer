
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration.VisualizationWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.controller.VisualizationListener;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.FilterType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.PurposeEnum;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableFormatExporter;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableSettingsWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.filterWizard.FilterTSS;
import de.cebitec.readXplorer.transcriptomeAnalyses.filterWizard.FilterWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MotifSearchModel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MotifSearchPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MultiPurposeTopComponent;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.PromotorAnalysisWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.PromotorSearchParameters;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsAnalysisParameters;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsAnalysisWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsMotifSearchPanel;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observer;
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * This panel is capable of showing a table with transcription start sites and
 * contains an export button, which exports the data into an excel file.
 * Additionally it has now an import button, which imports an excel file and
 * visualize the data to a JPanel.
 *
 * @author -Rolf Hilker-, -jritter-
 */
public class ResultPanelTranscriptionStart extends ResultTablePanel implements
        Observer {

    private static final long serialVersionUID = 1L;
    public static final String TSS_TOTAL = "Total number of detected TSSs";
    public static final String TSS_FWD = "TSS on fwd strand";
    public static final String TSS_REV = "TSS on rev strand";
    public static final String TSS_LEADERLESS = "TSS of leaderless transcript";
    public static final String TSS_PUTATIVE_UNANNOTATED = "TSS putative unannotated";
    public static final String MAPPINGS_MILLION = "Mappings per Million";
    public static final String AVERAGE_MAPPINGS_LENGTH = "Average length of mappings";
    public static final String MAPPINGS_COUNT = "Mappings count";
    public static final String TSS_INTRAGENIC_TSS = "Intragenic TSS";
    public static final String TSS_INTERGENIC_TSS = "Intergenic TSS";
    public static final String BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS = "Background threshold";
    public static final String BACKGROUND_THRESHOLD_MIN_STACKSIZE = "Background threshold";
    public static final String TSS_MANUALLY_SET_THRESHOLD = "Set background threshold manually";
    public static final String TSS_PUTATIVE_ANTISENSE_IN_TOTAL = "Total number of putative antisense TSS";
    public static final String TSS_PUTATIVE_ANTISENSE_OF_5_PRIME_UTR = "Total number of putative antisense TSS in 5'-UTR";
    public static final String TSS_PUTATIVE_ANTISENSE_OF_3_PRIME_UTR = "Total number of putative antisense TSS in 3'-UTR";
    public static final String TSS_PUTATIVE_ANTISENSE_INTRAGENIC = "Total number of putative antisense intragenic TSS";
    public static final String TSS_ASSIGNED_TO_STABLE_RNA = "TSS assigned to stable RNA (t/rRNA)";
    public static final String TSS_FRACTION = "Fraction (used for background threshold calculation, #FP)";
    public static final String TSS_RATIO = "Ratio";
    public static final String TSS_EXCLUSION_OF_INTERNAL_TSS = "Exclude all intragenic TSS";
    public static final String TSS_RANGE_FOR_LEADERLESS_DETECTION = "Classification as leaderelss if distance to TLS is";
    public static final String TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS = "TLS shift detection: Relative distance in relation to CDS length";
    public static final String TSS_NO_PUTATIVE_CDS_SHIFTS = "Number of putative CDS-shifts";
    public static final String TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION = "Exclude TSS with distance from next TLS larger than ";
    public static final String TSS_LIMITATION_FOR_DISTANCE_KEEPING_INTERNAL_TSS = "Limitation for distance between intragenic TSS and next upstream feature TLS";
    public static final String TSS_KEEP_ALL_INTRAGENIC_TSS = "Keep all intragenic TSS, assign TSS to next feature if distance is";
    public static final String TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS = "Keep only intragenic TSS if next feature distance is";
    public final TableType tableType = TableType.TSS_TABLE;
    public static final int UNUSED_STATISTICS_VALUE = -1;
    private BoundsInfoManager boundsInfoManager;
    private ReferenceViewer referenceViewer;
    private TSSDetectionResults tssResult;
    private HashMap<String, Object> statisticsMap;
    private final TableRightClickFilter<UneditableTableModel> tableFilter;
    private final TableRightClickDeletion<DefaultTableModel> rowDeletion = new TableRightClickDeletion<>();
    private HashMap<Integer, TranscriptionStart> tssInHash;
    private MotifSearchModel model;
    private MultiPurposeTopComponent topComponent;
    private ElementsOfInterest elements = null;
    private ProgressHandle progresshandle;
    private List<String> promotorRegions;
    private MotifSearchPanel promotorMotifSearchPanel;
    private RbsMotifSearchPanel rbsMotifSearchPanel;
    private VisualizationListener vizualizationListener;
    String separator = "";
    Integer prefixLength = 0;
    PersistentReference persistantRef;


    /**
     * This panel is capable of showing a table with transcription start sites
     * and contains an export button, which exports the data into an excel file.
     */
    public ResultPanelTranscriptionStart() {
        this.initComponents();
        final int posColumnIdx = 0;
        final int trackColumnIdx = 25;
        final int chromColumnIdx = 1;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumnIdx, trackColumnIdx );
        this.tSSTable.getTableHeader().addMouseListener( tableFilter );
        this.tSSTable.addMouseListener( rowDeletion );
        this.initStatsMap();

        DefaultListSelectionModel listSelectionModel = (DefaultListSelectionModel) this.tSSTable.getSelectionModel();
        listSelectionModel.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showPosition( tSSTable, posColumnIdx, chromColumnIdx, boundsInfoManager );
            }


        } );
    }


    public void setDefaultTableModelToTable( DefaultTableModel model ) {
        this.tSSTable.setModel( model );
    }


    /**
     * Initializes the statistics map.
     */
    private void initStatsMap() {
        statisticsMap = new HashMap<>();
        statisticsMap.put( TSS_TOTAL, 0 );
        statisticsMap.put( TSS_FWD, 0 );
        statisticsMap.put( TSS_REV, 0 );
        statisticsMap.put( TSS_LEADERLESS, 0 );
        statisticsMap.put( TSS_INTRAGENIC_TSS, 0 );
        statisticsMap.put( TSS_INTERGENIC_TSS, 0 );
        statisticsMap.put( TSS_PUTATIVE_ANTISENSE_IN_TOTAL, 0 );
        statisticsMap.put( TSS_PUTATIVE_ANTISENSE_OF_5_PRIME_UTR, 0 );
        statisticsMap.put( TSS_PUTATIVE_ANTISENSE_OF_3_PRIME_UTR, 0 );
        statisticsMap.put( TSS_PUTATIVE_ANTISENSE_INTRAGENIC, 0 );
        statisticsMap.put( TSS_ASSIGNED_TO_STABLE_RNA, 0 );
        statisticsMap.put( MAPPINGS_COUNT, 0.0 );
        statisticsMap.put( AVERAGE_MAPPINGS_LENGTH, 0.0 );
        statisticsMap.put( MAPPINGS_MILLION, 0.0 );
        statisticsMap.put( BACKGROUND_THRESHOLD_MIN_STACKSIZE, 0.0 );
        statisticsMap.put( TSS_NO_PUTATIVE_CDS_SHIFTS, 0 );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tssScrollPane = new javax.swing.JScrollPane();
        tSSTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        statisticsButton = new javax.swing.JButton();
        startChartsOfTssData = new javax.swing.JButton();
        performPromotorAnalysis = new javax.swing.JButton();
        performRbsAnalysis = new javax.swing.JButton();
        performDeletionOfAllFP = new javax.swing.JButton();
        sequinTableExporter = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        tSSTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Position", "Comments", "Direction", "Read Starts", "Rel. Count", "Gene Name", "Gene Locus", "Offset", "Leaderless", "Putative TLS-Shift", "False Positive", "Intragenic TSS", "Intergenic TSS", "Putative Antisense", "Upstream Region Analysis", "Finished", "Gene Start", "Gene Stop", "Gene Length", "Gene Frame", "Gene Product", "Start Codon", "Stop Codon", "Chromosome", "Chrom ID", "Track ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false, false, false, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tSSTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tSSTableMouseClicked(evt);
            }
        });
        tssScrollPane.setViewportView(tSSTable);
        if (tSSTable.getColumnModel().getColumnCount() > 0) {
            tSSTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title0_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title24_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title1_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title2_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title14_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_1_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title15_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title21_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title21_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title6_1_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_2_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title25_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title19_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(14).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title20_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(15).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title23_3")); // NOI18N
            tSSTable.getColumnModel().getColumn(16).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title22_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(17).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title23_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(18).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title24_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(19).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title25_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(20).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title26_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(21).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title27_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(22).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title28_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(23).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title22_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(24).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title23_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(25).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title29_1_1")); // NOI18N
        }

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.exportButton.text_1")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.statisticsButton.text_1")); // NOI18N
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsButtonActionPerformed(evt);
            }
        });

        startChartsOfTssData.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.startChartsOfTssData.text_1")); // NOI18N
        startChartsOfTssData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startChartsOfTssDataActionPerformed(evt);
            }
        });

        performPromotorAnalysis.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.performPromotorAnalysis.text")); // NOI18N
        performPromotorAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performPromotorAnalysisActionPerformed(evt);
            }
        });

        performRbsAnalysis.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.performRbsAnalysis.text")); // NOI18N
        performRbsAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performRbsAnalysisActionPerformed(evt);
            }
        });

        performDeletionOfAllFP.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.performDeletionOfAllFP.text")); // NOI18N
        performDeletionOfAllFP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performDeletionOfAllFPActionPerformed(evt);
            }
        });

        sequinTableExporter.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.sequinTableExporter.text")); // NOI18N
        sequinTableExporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequinTableExporterActionPerformed(evt);
            }
        });

        jButton1.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tssScrollPane)
            .addGroup(layout.createSequentialGroup()
                .addComponent(performRbsAnalysis)
                .addGap(18, 18, 18)
                .addComponent(performPromotorAnalysis)
                .addGap(18, 18, 18)
                .addComponent(startChartsOfTssData)
                .addGap(18, 18, 18)
                .addComponent(statisticsButton)
                .addGap(18, 18, 18)
                .addComponent(performDeletionOfAllFP)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(sequinTableExporter)
                .addGap(18, 18, 18)
                .addComponent(exportButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tssScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(performRbsAnalysis)
                    .addComponent(performPromotorAnalysis)
                    .addComponent(performDeletionOfAllFP)
                    .addComponent(startChartsOfTssData)
                    .addComponent(statisticsButton)
                    .addComponent(exportButton)
                    .addComponent(sequinTableExporter)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

        tssResult.setResults( this.updateTssResults() );

        processResultForExport();
        tssResult.setPromotorRegions( promotorRegions );
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
            TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), this.tssResult );
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog( this, new TssDetectionStatsPanel( statisticsMap ), "TSS Detection Statistics", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_statisticsButtonActionPerformed

    private void tSSTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tSSTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tSSTableMouseClicked

    private void startChartsOfTssDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startChartsOfTssDataActionPerformed
        @SuppressWarnings( "unchecked" )
        VisualizationWizardIterator visualizationWizardIterator = new VisualizationWizardIterator();
        WizardDescriptor wiz = new WizardDescriptor( visualizationWizardIterator );
        visualizationWizardIterator.setWiz( wiz );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( "Visualizations" );

        //action to perform after successfuly finishing the wizard
        if( DialogDisplayer.getDefault().notify( wiz ) == WizardDescriptor.FINISH_OPTION ) {
            final List<TranscriptionStart> currentTss = updateTssResults();
            vizualizationListener = new VisualizationListener( this.persistantRef, wiz, currentTss, tssResult );
            vizualizationListener.actionPerformed( new ActionEvent( this, 1, ChartType.WIZARD.toString() ) );
            if( vizualizationListener.isAbsoluteFrequencyPlotSelected() ) {
                vizualizationListener.actionPerformed( new ActionEvent( this, 2, ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs.toString() ) );
            }
            if( vizualizationListener.isBaseDistributionPlotSelected() ) {
                vizualizationListener.actionPerformed( new ActionEvent( this, 3, ChartType.BASE_DISTRIBUTION.toString() ) );
            }
            this.tssResult.setResults( currentTss );
        }
    }//GEN-LAST:event_startChartsOfTssDataActionPerformed

    private void performDeletionOfAllFPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performDeletionOfAllFPActionPerformed
        // delete all false positive TSS from table and TSS array
        List<TranscriptionStart> tss = this.updateTssResults();
        DefaultTableModel tableModel = (DefaultTableModel) tSSTable.getModel();
        List<Integer> valuesToRemove = new ArrayList<>();
        int columnNo = tSSTable.getRowCount();
        for( int i = 0; i < columnNo; i++ ) {
            Integer posTableAti = (Integer) tSSTable.getValueAt( i, 0 );
            TranscriptionStart ts = tssInHash.get( posTableAti );
            boolean isFalsePositive = (boolean) tSSTable.getValueAt( i, 10 );
            if( isFalsePositive ) {
                tssInHash.remove( posTableAti );
                valuesToRemove.add( i );
                tss.remove( ts );
            }
        }

        for( int i = valuesToRemove.size() - 1; i >= 0; i-- ) {
            Integer x = valuesToRemove.get( i );
            tableModel.removeRow( x );
        }

        tSSTable.setModel( tableModel );
        tSSTable.updateUI();
        tssResult.setResults( tss );

    }//GEN-LAST:event_performDeletionOfAllFPActionPerformed

    private void performPromotorAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performPromotorAnalysisActionPerformed

        PromotorAnalysisWizardIterator wizard = new PromotorAnalysisWizardIterator();
        WizardDescriptor wiz = new WizardDescriptor( wizard );
        wizard.setWiz( wiz );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( NbBundle.getMessage( ResultPanelTranscriptionStart.class, "TTL_MotifSearchWizardTitle" ) );

        //action to perform after successfuly finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {

            boolean takeAllElements = (boolean) wiz.getProperty( ElementsOfInterest.ALL.toString() );
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_LEADERLESS_TRANSCRIPTS.toString() );
            boolean takeOnlyAntisense = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_ANTISENSE_TSS.toString() );
            boolean takeOnlyRealTss = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS.toString() );
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_SELECTED_FOR_UPSTREAM_ANALYSES.toString() );

            if( takeAllElements ) {
                this.elements = ElementsOfInterest.ALL;
            }
            else if( takeOnlyLeaderless ) {
                this.elements = ElementsOfInterest.ONLY_LEADERLESS_TRANSCRIPTS;
            }
            else if( takeOnlyAntisense ) {
                this.elements = ElementsOfInterest.ONLY_ANTISENSE_TSS;
            }
            else if( takeOnlyRealTss ) {
                this.elements = ElementsOfInterest.ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS;
            }
            else if( takeOnlySelectedElements ) {
                this.elements = ElementsOfInterest.ONLY_SELECTED_FOR_UPSTREAM_ANALYSES;
            }

            int minus10MotifWidth = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS10_MOTIF_LENGTH );
            int minus35MotifWidth = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS35_MOTIF_LENGTH );
            int noOfTimes = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_NUMBER_OF_TRYING );
            int minSpacer1 = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER1_LENGTH );
            int minSpacer2 = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER2_LENGTH );
            int seqWidthMinus10 = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_10_REGION );
            int seqWidthMinus35 = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_35_REGION );
            int alternativeSpacer = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ALTERNATIVE_SPACER );
            final int lengthRelToTss = (Integer) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS );

            final PromotorSearchParameters params = new PromotorSearchParameters(
                    minus10MotifWidth, minus35MotifWidth,
                    noOfTimes, minSpacer1, minSpacer2, alternativeSpacer,
                    seqWidthMinus10, seqWidthMinus35, lengthRelToTss );

            final List<TranscriptionStart> starts = updateTssResults();
            this.topComponent = new MultiPurposeTopComponent( PurposeEnum.MOTIF_SEARCH );
            this.topComponent.setLayout( new BorderLayout() );
            this.topComponent.open();
            String type = elements.toString().toLowerCase();
            this.topComponent.setName( "Promotor motif search for " + type + " elements in Table" );

            model = new MotifSearchModel( this.persistantRef );
            promotorMotifSearchPanel = new MotifSearchPanel();
            promotorMotifSearchPanel.registerObserver( this );

            Thread promotorSearch = new Thread( new Runnable() {
                @Override
                public void run() {
                    model.takeUpstreamRegions( elements, starts, lengthRelToTss, false );
                    boolean success = model.utrPromotorAnalysis( params, starts );

                    if( success ) {
                        promotorMotifSearchPanel.setUpstreamRegions( model.getUpstreamRegions() );
                        promotorMotifSearchPanel.setStyledDocumentToRegionOfIntrestMinusTen( model.getRegionOfIntrestMinus10().getStyledDocument() );
                        promotorMotifSearchPanel.setStyledDocumentToRegionOfIntrestMinus35( model.getRegionOfIntrestMinus35().getStyledDocument() );
                        promotorMotifSearchPanel.setMinus10Starts( model.getMinus10MotifStarts() );
                        promotorMotifSearchPanel.setMinus35Starts( model.getMinus35MotifStarts() );
                        promotorMotifSearchPanel.setMinus10Shifts( model.getIdsToMinus10Shifts() );
                        promotorMotifSearchPanel.setMinus35Shifts( model.getIdsToMinus35Shifts() );
                        promotorMotifSearchPanel.setMinus35MotifWidth( params.getMinus35MotifWidth() );
                        promotorMotifSearchPanel.setMinus10MotifWidth( params.getMinusTenMotifWidth() );
                        promotorMotifSearchPanel.setMinus10Input( model.getMinus10Input() );
                        promotorMotifSearchPanel.setMinus35Input( model.getMinus35Input() );
                        promotorMotifSearchPanel.setBioProspOutMinus10( model.getBioProspOutMinus10() );
                        promotorMotifSearchPanel.setBioProspOutMinus35( model.getBioProspOutMinus35() );
                        promotorMotifSearchPanel.setStyledDocToPromotorsFastaPane( model.getColoredPromotorRegions() );
                        promotorMotifSearchPanel.setContributionMinus10Label( "Number of contributing/all sequences (-10 motif): "
                                                                              + (int) model.getContributingCitesForMinus10Motif() + "/" + model.getUpstreamRegions().size() / 2 );
                        promotorMotifSearchPanel.setContributionMinus35Label( "Number of contributing/all sequences (-35 motif): "
                                                                              + (int) model.getContributingCitesForMinus35Motif() + "/" + model.getUpstreamRegions().size() / 2 );

                        String roundedMean = String.format( "%.1f", model.getMeanMinus10SpacerToTSS() );
                        promotorMotifSearchPanel.setMinSpacer1LengthToLabel( roundedMean );

                        promotorMotifSearchPanel.setLogoMinus10( new File( model.getLogoMinus10().getAbsolutePath() + ".eps" ) );
                        promotorMotifSearchPanel.setMinus10LogoToPanel( model.getMinus10logoLabel() );

                        roundedMean = String.format( "%.1f", model.getMeanMinus35SpacerToMinus10() );
                        promotorMotifSearchPanel.setMinSpacer2LengthToLabel( roundedMean );

                        promotorMotifSearchPanel.setLogoMinus35( new File( model.getLogoMinus35().getAbsolutePath() + ".eps" ) );
                        promotorMotifSearchPanel.setMinus35LogoToPanel( model.getMinus35LogoLabel() );

                        promotorMotifSearchPanel.setInfo( model.getInfo() );

                        topComponent.add( promotorMotifSearchPanel, BorderLayout.CENTER );
                        topComponent.updateUI();
                    }
                    else {
                        promotorMotifSearchPanel.setMinus10Input( model.getMinus10Input() );
                        promotorMotifSearchPanel.setMinus35Input( model.getMinus35Input() );
                        promotorMotifSearchPanel.setBioProspOutMinus10( model.getBioProspOutMinus10() );
                        promotorMotifSearchPanel.setBioProspOutMinus35( model.getBioProspOutMinus35() );
                        promotorMotifSearchPanel.setLogoMinus10( model.getLogoMinus10() );
                        promotorMotifSearchPanel.setLogoMinus35( model.getLogoMinus35() );
                        promotorMotifSearchPanel.setInfo( model.getInfo() );
                        topComponent.add( promotorMotifSearchPanel, BorderLayout.CENTER );
                    }
                }


            } );
            promotorSearch.start();

            this.tssResult.setResults( starts );
        }
    }//GEN-LAST:event_performPromotorAnalysisActionPerformed

    private void performRbsAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performRbsAnalysisActionPerformed
        final List<TranscriptionStart> currentTss = updateTssResults();
        RbsAnalysisWizardIterator wizard = new RbsAnalysisWizardIterator();
        WizardDescriptor wiz = new WizardDescriptor( wizard );
        wizard.setWiz( wiz );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( NbBundle.getMessage( ResultPanelTranscriptionStart.class, "TTL_MotifSearchWizardTitle" ) );

        //action to perform after successfuly finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {

            final int lengthRelToTls = (int) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS );
            int motifWidth = (int) wiz.getProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH );
            int noOfTrying = (int) wiz.getProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR );
            int minSpacer = (int) wiz.getProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER );

            final RbsAnalysisParameters params = new RbsAnalysisParameters( lengthRelToTls, motifWidth, noOfTrying, minSpacer );

            topComponent = new MultiPurposeTopComponent( PurposeEnum.MOTIF_SEARCH );
            topComponent.setLayout( new BorderLayout() );
            topComponent.open();
            topComponent.setName( "RBS motif search" );

            model = new MotifSearchModel( this.persistantRef );
            rbsMotifSearchPanel = new RbsMotifSearchPanel();
            rbsMotifSearchPanel.registerObserver( this );

            Thread promotorSearch = new Thread( new Runnable() {
                @Override
                public void run() {
                    model.takeUpstreamRegions( ElementsOfInterest.ELEMENTS_FOR_RBS_ANALYSIS, currentTss, lengthRelToTls, true );
                    boolean returnValue = model.rbsMotifAnalysis( params, currentTss, null );
                    if( returnValue ) {
                        rbsMotifSearchPanel.setUpstreamRegions( model.getUpstreamRegions() );
                        rbsMotifSearchPanel.setRbsShifts( model.getContributedSequencesWithShift() );
                        rbsMotifSearchPanel.setRbsStarts( model.getRbsStarts() );
                        rbsMotifSearchPanel.setBioProspInput( model.getRbsBioProspectorInput() );
                        rbsMotifSearchPanel.setBioProspOut( model.getRbsBioProsFirstHit() );
                        rbsMotifSearchPanel.setContributedSequencesToMotif( "" + (int) model.getContributingCitesForRbsMotif() + "/" + model.getUpstreamRegions().size() / 2 );
                        rbsMotifSearchPanel.setRegionsToAnalyzeToPane( model.getRegionsRelToTLSTextPane().getStyledDocument() );
                        rbsMotifSearchPanel.setRegionOfIntrestToPane( model.getRegionsForMotifSearch().getStyledDocument() );
                        String roundedMean = String.format( "%.1f", model.getMeanSpacerLengthOfRBSMotif() );
                        rbsMotifSearchPanel.setParams( params );
                        rbsMotifSearchPanel.setRegionLengthForBioProspector( params.getSeqLengthToAnalyze() );
                        rbsMotifSearchPanel.setMotifWidth( params.getMotifWidth() );
                        rbsMotifSearchPanel.setMeanSpacerLength( roundedMean );
                        rbsMotifSearchPanel.setSequenceLogo( new File( model.getLogoRbs().getAbsolutePath() + ".eps" ) );
                        rbsMotifSearchPanel.setLogo( model.getRbsLogoLabel() );
                        rbsMotifSearchPanel.setInfo( model.getInfo() );
                        topComponent.add( rbsMotifSearchPanel, BorderLayout.CENTER );
                    }
                    else {
                        rbsMotifSearchPanel.setBioProspInput( model.getRbsBioProspectorInput() );
                        rbsMotifSearchPanel.setBioProspOut( model.getRbsBioProsFirstHit() );
                        rbsMotifSearchPanel.setSequenceLogo( model.getLogoRbs() );
                        rbsMotifSearchPanel.setInfo( model.getInfo() );
                        topComponent.add( rbsMotifSearchPanel, BorderLayout.CENTER );
                    }
                }


            } );
            promotorSearch.start();
            this.tssResult.setResults( currentTss );
        }
    }//GEN-LAST:event_performRbsAnalysisActionPerformed

    private void sequinTableExporterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequinTableExporterActionPerformed

        final String wizardName = "Sequin Feature Table Export";
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add( new SequinTableSettingsWizardPanel( wizardName ) );
//        DataSelectionWizardPanel selection = new DataSelectionWizardPanel(PurposeEnum.CHARTS);
//        panels.add(selection);
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
        wiz.setTitle( "Export of a feature annotation table" );
        if( DialogDisplayer.getDefault().notify( wiz ) == WizardDescriptor.FINISH_OPTION ) {

            progresshandle = ProgressHandleFactory.createHandle( "Export of feature annotation table" );
            final ArrayList<TranscriptionStart> tss = (ArrayList<TranscriptionStart>) this.updateTssResults();
            tssResult.setResults( tss );

            final String featureName = (String) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_FEATURE_NAME );

            final boolean isParsingLocusTagSelected = (boolean) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_PARSING_LOCUS_TAG );

            if( isParsingLocusTagSelected ) {
                separator = (String) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_SEPARATOR );
                prefixLength = (Integer) wiz.getProperty( SequinTableSettingsWizardPanel.SEQUIN_EXPORT_STRAIN_LENGTH );
            }

            ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( new String[]{ "tbl" }, "Table files for Sequin export" ) {
                @Override
                public void save( String fileLocation ) {
                    progresshandle.start( 5 );
                    SequinTableFormatExporter exporter = new SequinTableFormatExporter( new File( fileLocation ), (ArrayList<TranscriptionStart>) tss, null, null, tableType, featureName, separator, prefixLength, isParsingLocusTagSelected ); //To change body of generated methods, choose Tools | Templates.
                    progresshandle.progress( 1 );
                    exporter.start();
                    progresshandle.progress( 2 );
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
        }
    }//GEN-LAST:event_sequinTableExporterActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // filter the table in sub-tables
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add( new FilterWizardPanel( "Filtration" ) );
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
        wiz.setTitle( "Select filter types" );
        if( DialogDisplayer.getDefault().notify( wiz ) == WizardDescriptor.FINISH_OPTION ) {
            List<TranscriptionStart> tss = this.updateTssResults();
            tssResult.setResults( tss );

            boolean filterForMultipleTSS = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_FOR_MULTIPLE_TSS );
            boolean filterForGivenReadStartRange = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_FOR_READSTARTS );
            int readstartsLimit = (int) wiz.getProperty( FilterWizardPanel.PROP_FILTER_READSTARTS );
            boolean filterForSingleTSS = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_FOR_SINGLE_TSS );
            boolean folterOnlyLeaderless = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_ONLY_LEADERLESS );
            boolean filterOnlyAntisense = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_ONLY_ANTISENSE );
            boolean filterOnlyTaggedAsFinish = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_ONLY_TAGGED_AS_FINISH );
            boolean filterOnlyForIntragenic = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_ONLY_INTRAGENIC );
            boolean filterOnlyForItergenic = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_ONLY_INTERGENIC );
            boolean filterOnlyTaggedForUpstreamAnalysis = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_ONLY_TAGGED_FOR_UPSTREAM_ANALYSIS );
            boolean filterOnlyForFalsePositeves = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_FALSE_POSITIVE );

            boolean filterForIntragenicAntisense = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_INTRAGENIC_ANTISENSE );
            boolean filterForFivePrimeUtrAntisense = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_FIVE_PRIME_UTR_ANTISENSE );
            boolean filterForThreePrimeUtrAntisense = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_THREE_PRIME_UTR_ANTISENSE );
            boolean filterForStableRna = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_STABLE_RNA );
            boolean filterForNonStableRnaElements = (boolean) wiz.getProperty( FilterWizardPanel.PROP_FILTER_ALL_NON_STABLE_RNA );

            FilterTSS filter = new FilterTSS();

            if( filterForStableRna ) {
                generateFilteredTable( filter, FilterType.STABLE_RNA, "Detected TSS assigned to stable RNA for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }
            if( filterForThreePrimeUtrAntisense ) {
                generateFilteredTable( filter, FilterType.THREE_PRIME_ANTISENSE, "Detected 3'-UTR antisense TSS for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }
            if( filterForFivePrimeUtrAntisense ) {
                generateFilteredTable( filter, FilterType.FIVE_PRIME_ANTISENSE, "Detected 5'-UTR antisense TSS for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }
            if( filterForIntragenicAntisense ) {
                generateFilteredTable( filter, FilterType.INTRAGENIC_ANTISENSE, "Detected intragenic antisense TSS for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }
            if( filterForGivenReadStartRange ) {
                generateFilteredTable( filter, FilterType.READSTARTS, "Detected TSS with at least " + readstartsLimit + " for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }
            if( filterForMultipleTSS ) {
                generateFilteredTable( filter, FilterType.MULTIPLE, "Detected multiple TSSs for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }
            if( filterForSingleTSS ) {
                generateFilteredTable( filter, FilterType.SINGLE, "Detected single TSSs for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

            if( filterOnlyAntisense ) {
                generateFilteredTable( filter, FilterType.ONLY_ANTISENSE, "Detected antisense TSSs for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

            if( filterOnlyForIntragenic ) {
                generateFilteredTable( filter, FilterType.ONLY_INTRAGENIC, "Detected intragenic TSSs for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

            if( filterOnlyForItergenic ) {
                generateFilteredTable( filter, FilterType.ONLY_INTERGENIC, "Detected intergenic TSSs for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

            if( filterOnlyTaggedAsFinish ) {
                generateFilteredTable( filter, FilterType.FINISHED_TAGGED, "Detected TSSs, which are tagged as finish, for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

            if( filterOnlyTaggedForUpstreamAnalysis ) {
                generateFilteredTable( filter, FilterType.UPSTREMA_ANALYSIS_TAGGED, "Detected TSSs, which are tagged for upstream analysis, for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

            if( folterOnlyLeaderless ) {
                generateFilteredTable( filter, FilterType.ONLY_LEADERLESS, "Detected leaderless TSSs for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

            if( filterOnlyForFalsePositeves ) {
                generateFilteredTable( filter, FilterType.ONLY_FALSE_POSITIVES, "Detected, but tagged as false positive TSSs for ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }
            if( filterForNonStableRnaElements ) {
                generateFilteredTable( filter, FilterType.ONLY_NON_STABLE_RNA, "Detected assigned to non stable (r/tRNA) Features ", tss, (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters(), readstartsLimit );
            }

        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // refresh list of TSS
        this.progresshandle = ProgressHandleFactory.createHandle( "Export Reference File" );
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( new String[]{ "txt" }, "Text file" ) {
            @Override
            public void save( String fileLocation ) {
                progresshandle.start( 3 );

                // update tss list
                final List<TranscriptionStart> currentTss = updateTssResults();
                Map<String, String[]> referenceEntries = new TreeMap<>();
                progresshandle.progress( 1 );

                for( TranscriptionStart transcriptionStart : currentTss ) {
                    PersistentFeature feature = transcriptionStart.getAssignedFeature();

                    if( feature != null ) {
                        if( transcriptionStart.getOffset() == 0 && transcriptionStart.getDist2start() > 0 ) {
                            continue;
                        }
                        String locus = feature.getLocus();
                        if( referenceEntries.containsKey( locus ) ) {
                            Integer start = transcriptionStart.getStartPosition();
                            if( feature.isFwdStrand() ) {
                                Integer stop = feature.getStop();

                                if( Integer.valueOf( referenceEntries.get( locus )[0] ) > start ) {
                                    referenceEntries.get( locus )[0] = "" + (start - 1);
                                }
                                if( Integer.valueOf( referenceEntries.get( locus )[1] ) < stop ) {
                                    referenceEntries.get( locus )[1] = "" + (stop + 1);
                                }
                            }
                            else {
                                Integer stop = feature.getStart();
                                if( Integer.valueOf( referenceEntries.get( locus )[0] ) < start ) {
                                    referenceEntries.get( locus )[0] = "" + (start + 1);
                                }
                                if( Integer.valueOf( referenceEntries.get( locus )[1] ) > stop ) {
                                    referenceEntries.get( locus )[1] = "" + (stop - 1);
                                }
                            }
                            referenceEntries.get( locus )[2] = referenceEntries.get( locus )[2].concat( "-" + transcriptionStart.getStartPosition() );
                            referenceEntries.get( locus )[3] = referenceEntries.get( locus )[3].concat( "-" + transcriptionStart.getOffsetToAssignedFeature() );
                            referenceEntries.get( locus )[4] = referenceEntries.get( locus )[4].concat( "-" + transcriptionStart.getStartRbsMotif() + ";" + transcriptionStart.getRbsMotifWidth() );
                            referenceEntries.get( locus )[5] = referenceEntries.get( locus )[5].concat( "-" + transcriptionStart.getStartMinus10Motif() + ";" + transcriptionStart.getMinus10MotifWidth() );
                            referenceEntries.get( locus )[6] = referenceEntries.get( locus )[6].concat( "-" + transcriptionStart.getStartMinus35Motif() + ";" + transcriptionStart.getMinus35MotifWidth() );
                        }
                        else {
                            Integer start = transcriptionStart.getStartPosition();
                            String[] array = new String[7];
                            array[0] = "" + start;
                            if( feature.isFwdStrand() ) {
                                array[1] = "" + feature.getStop();
                            }
                            else {
                                array[1] = "" + feature.getStart();
                            }
                            array[2] = "" + transcriptionStart.getStartPosition();
                            array[3] = "" + transcriptionStart.getOffsetToAssignedFeature();
                            array[4] = "" + transcriptionStart.getStartRbsMotif() + ";" + transcriptionStart.getRbsMotifWidth();
                            array[5] = "" + transcriptionStart.getStartMinus10Motif() + ";" + transcriptionStart.getMinus10MotifWidth();
                            array[6] = "" + transcriptionStart.getStartMinus35Motif() + ";" + transcriptionStart.getMinus35MotifWidth();
                            referenceEntries.put( locus, array );
                        }
                    }
                }
                progresshandle.progress( 2 );
                writeReferenceFile( fileLocation, referenceEntries );
                progresshandle.progress( 3 );
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
    }//GEN-LAST:event_jButton2ActionPerformed


    /**
     *
     * @param filter            FilterTSS
     * @param type              FilterType
     * @param filteredTableName table name for filteret table
     * @param tss               List<TranscriptionStart>
     * @param params            ParameterSetFiveEnrichedAnalyses
     * @param munNoOfReadStarts the minimum numer of read starts
     */
    private void generateFilteredTable( FilterTSS filter, FilterType type,
                                        String filteredTableName, List<TranscriptionStart> tss,
                                        ParameterSetFiveEnrichedAnalyses params, int munNoOfReadStarts ) {
        List<TranscriptionStart> subTSS;
        if( type == FilterType.READSTARTS ) {
            subTSS = filter.filter( type, tss, params, munNoOfReadStarts );
        }
        else {
            subTSS = filter.filter( type, tss, params, 0 );
        }

        ResultPanelTranscriptionStart transcriptionStartResultPanel = new ResultPanelTranscriptionStart();
//        transcriptionStartResultPanel.setRefAndBoundsManager(this.persistantRef, this.boundsInfoManager);
        transcriptionStartResultPanel.setPersistentReference( this.persistantRef );
        transcriptionStartResultPanel.setBoundsInfoManager( this.boundsInfoManager );
        TSSDetectionResults tssResultNew = new TSSDetectionResults( this.tssResult.getStats(), subTSS, tssResult.getTrackMap(), this.persistantRef );
        tssResultNew.setResults( subTSS );
        tssResultNew.setParameters( this.tssResult.getParameters() );
        transcriptionStartResultPanel.addResult( tssResultNew );
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent( TranscriptomeAnalysesTopComponentTopComponent.PREFERRED_ID );
        TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp = (TranscriptomeAnalysesTopComponentTopComponent) findTopComponent;
        String trackNames = GeneralUtils.generateConcatenatedString( tssResultNew.getTrackNameList(), 120 );
        String panelName = filteredTableName + trackNames + " Hits: " + transcriptionStartResultPanel.getDataSize();
        transcAnalysesTopComp.openAnalysisTab( panelName, transcriptionStartResultPanel );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton performDeletionOfAllFP;
    private javax.swing.JButton performPromotorAnalysis;
    private javax.swing.JButton performRbsAnalysis;
    private javax.swing.JButton sequinTableExporter;
    private javax.swing.JButton startChartsOfTssData;
    private javax.swing.JButton statisticsButton;
    private javax.swing.JTable tSSTable;
    private javax.swing.JScrollPane tssScrollPane;
    // End of variables declaration//GEN-END:variables


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
     * @return The number of detected TSS
     */
    @Override
    public int getDataSize() {
        return this.tssResult.getResults().size();
    }


    @Override
    public void addResult( final ResultTrackAnalysis newResult ) {
        if( newResult instanceof TSSDetectionResults ) {
            final TSSDetectionResults tssResultNew = (TSSDetectionResults) newResult;
            final List<TranscriptionStart> tsss = new ArrayList<>( tssResultNew.getResults() );
            this.tssInHash = new HashMap<>();

            if( tssResult == null ) {
                tssResult = tssResultNew;
            }
            else {
                tssResult.getResults().addAll( tssResultNew.getResults() );
            }

            tssResult.setPromotorRegions( promotorRegions );

            SwingUtilities.invokeLater( new Runnable() { //because it is not called from the swing dispatch thread
                @Override
                public void run() {
                    final int nbColumns = 26;

                    // statistic values
                    int noFwdFeatures = 0;
                    int noRevFeatures = 0;
                    int noLeaderlessFeatures = 0;
                    int noIntragenicTSS = 0;
                    int noPutativeAntisenseInTotal = 0;
                    int noPutativeAntisenseIn5PrimeUTR = 0;
                    int noPutativeAntisenseIn3PrimeUTR = 0;
                    int noPutativeAntisenseIntragenic = 0;
                    int noOfTssAssignedToStableRna = 0;
                    int noPutCdsShifts = 0;
                    int noIntergenic = 0;

                    final DefaultTableModel model = (DefaultTableModel) tSSTable.getModel();

                    PersistentReference ref = persistantRef;
                    String strand;
                    PersistentFeature feature;
                    PersistentFeature nextDownstreamFeature;

                    for( TranscriptionStart tSS : tsss ) {
                        String detectedFeatureStart = "0";
                        String detectedFeatureStop = "0";

                        if( tSS.getAssignedFeature() != null ) {
                            if( tSS.getAssignedFeature().isFwdStrand() ) {
                                detectedFeatureStart = ref.getChromSequence( tSS.getChromId(), tSS.getAssignedFeature().getStart(), tSS.getAssignedFeature().getStart() + 2 );
                                detectedFeatureStop = ref.getChromSequence( tSS.getChromId(), tSS.getAssignedFeature().getStop() - 2, tSS.getAssignedFeature().getStop() );
                            }
                            else {
                                detectedFeatureStart = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), tSS.getAssignedFeature().getStop() - 2, tSS.getAssignedFeature().getStop() ) );
                                detectedFeatureStop = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), tSS.getAssignedFeature().getStart(), tSS.getAssignedFeature().getStart() + 2 ) );
                            }
                        }
                        if( tSS.isFwdStrand() ) {
                            strand = SequenceUtils.STRAND_FWD_STRING;
                            ++noFwdFeatures;
                        }
                        else {
                            strand = SequenceUtils.STRAND_REV_STRING;
                            ++noRevFeatures;
                        }

                        tSS.setDetectedFeatStart( detectedFeatureStart );
                        tSS.setDetectedFeatStop( detectedFeatureStop );

                        if( tSS.isIntergenicTSS() ) {
                            noIntergenic++;
                        }
                        if( tSS.isAssignedToStableRNA() ) {
                            noOfTssAssignedToStableRna++;
                        }
                        if( tSS.isIs3PrimeUtrAntisense() ) {
                            noPutativeAntisenseIn3PrimeUTR++;
                        }

                        if( tSS.isIs5PrimeUtrAntisense() ) {
                            noPutativeAntisenseIn5PrimeUTR++;
                        }
                        if( tSS.isIntragenicAntisense() ) {
                            noPutativeAntisenseIntragenic++;
                        }

                        boolean leaderless = tSS.isLeaderless();
                        if( leaderless ) {
                            ++noLeaderlessFeatures;
                        }
                        final Object[] rowData = new Object[nbColumns];
                        int position = tSS.getStartPosition();
                        tssInHash.put( position, tSS );

                        int i = 0;
                        rowData[i++] = position;
                        rowData[i++] = tSS.getComment();
                        rowData[i++] = strand;
                        rowData[i++] = tSS.getReadStarts();
                        rowData[i++] = tSS.getRelCount();

                        feature = tSS.getDetectedGene();
                        nextDownstreamFeature = tSS.getNextGene();

                        if( feature != null ) {
                            rowData[i++] = feature.toString();
                            rowData[i++] = feature.getLocus();
                            rowData[i++] = tSS.getOffset();
                        }
                        else if( nextDownstreamFeature != null ) {
                            rowData[i++] = nextDownstreamFeature.toString();
                            rowData[i++] = nextDownstreamFeature.getLocus();
                            rowData[i++] = tSS.getOffsetToNextDownstrFeature();
                        }
                        else {
                            rowData[i++] = "-";
                            rowData[i++] = "-";
                            rowData[i++] = 0;
                        }

                        rowData[i++] = leaderless;

                        boolean cdsShift = tSS.isCdsShift();
                        rowData[i++] = cdsShift;
                        if( cdsShift ) {
                            noPutCdsShifts++;
                        }

                        rowData[i++] = tSS.isFalsePositive();

                        rowData[i++] = tSS.isIntragenicTSS();
                        if( tSS.isIntragenicTSS() ) {
                            noIntragenicTSS++;
                        }
                        rowData[i++] = tSS.isIntergenicTSS();

                        // 13
                        rowData[i++] = tSS.isPutativeAntisense();
                        if( tSS.isPutativeAntisense() ) {
                            noPutativeAntisenseInTotal++;
                        }
                        rowData[i++] = tSS.isSelected();
                        rowData[i++] = tSS.isConsideredTSS();

                        // additionally informations about detected gene
                        if( feature != null ) {
                            rowData[i++] = feature.getStartOnStrand();
                            rowData[i++] = feature.getStopOnStrand();
                            rowData[i++] = feature.getStop() - feature.getStart();
                            rowData[i++] = PersistentFeature.Utils.determineFrame( feature );
                            rowData[i++] = feature.getProduct();
                        }
                        else if( nextDownstreamFeature != null ) {
                            rowData[i++] = nextDownstreamFeature.isFwdStrand() ? nextDownstreamFeature.getStart() : nextDownstreamFeature.getStop();
                            rowData[i++] = nextDownstreamFeature.isFwdStrand() ? nextDownstreamFeature.getStop() : nextDownstreamFeature.getStart();
                            rowData[i++] = nextDownstreamFeature.getStop() - nextDownstreamFeature.getStart();
                            rowData[i++] = PersistentFeature.Utils.determineFrame( nextDownstreamFeature );
                            rowData[i++] = nextDownstreamFeature.getProduct();
                        }
                        else {
                            rowData[i++] = 0;
                            rowData[i++] = 0;
                            rowData[i++] = "0";
                            rowData[i++] = 0;
                            rowData[i++] = "-";
                        }

                        rowData[i++] = detectedFeatureStart;
                        rowData[i++] = detectedFeatureStop;
                        rowData[i++] = newResult.getChromosomeMap().get( tSS.getChromId() );
                        rowData[i++] = tSS.getChromId();
                        rowData[i++] = tSS.getTrackId();

                        model.addRow( rowData );
                    }

                    //create statistics
                    statisticsMap.put( TSS_TOTAL, (Integer) statisticsMap.get( TSS_TOTAL ) + tsss.size() );
                    statisticsMap.put( TSS_FWD, (Integer) statisticsMap.get( TSS_FWD ) + noFwdFeatures );
                    statisticsMap.put( TSS_REV, (Integer) statisticsMap.get( TSS_REV ) + noRevFeatures );
                    statisticsMap.put( TSS_LEADERLESS, (Integer) statisticsMap.get( TSS_LEADERLESS ) + noLeaderlessFeatures );
                    statisticsMap.put( TSS_INTRAGENIC_TSS, (Integer) statisticsMap.get( TSS_INTRAGENIC_TSS ) + noIntragenicTSS );
                    statisticsMap.put( TSS_INTERGENIC_TSS, (Integer) statisticsMap.get( TSS_INTERGENIC_TSS ) + noIntergenic );
                    statisticsMap.put( TSS_PUTATIVE_ANTISENSE_IN_TOTAL, (Integer) statisticsMap.get( TSS_PUTATIVE_ANTISENSE_IN_TOTAL ) + noPutativeAntisenseInTotal );
                    statisticsMap.put( TSS_PUTATIVE_ANTISENSE_OF_3_PRIME_UTR, (Integer) statisticsMap.get( TSS_PUTATIVE_ANTISENSE_OF_3_PRIME_UTR ) + noPutativeAntisenseIn3PrimeUTR );
                    statisticsMap.put( TSS_PUTATIVE_ANTISENSE_OF_5_PRIME_UTR, (Integer) statisticsMap.get( TSS_PUTATIVE_ANTISENSE_OF_5_PRIME_UTR ) + noPutativeAntisenseIn5PrimeUTR );
                    statisticsMap.put( TSS_PUTATIVE_ANTISENSE_INTRAGENIC, (Integer) statisticsMap.get( TSS_PUTATIVE_ANTISENSE_INTRAGENIC ) + noPutativeAntisenseIntragenic );
                    statisticsMap.put( TSS_ASSIGNED_TO_STABLE_RNA, (Integer) statisticsMap.get( TSS_ASSIGNED_TO_STABLE_RNA ) + noOfTssAssignedToStableRna );
                    statisticsMap.put( TSS_NO_PUTATIVE_CDS_SHIFTS, (Integer) statisticsMap.get( TSS_NO_PUTATIVE_CDS_SHIFTS ) + noPutCdsShifts );
                    statisticsMap.put( MAPPINGS_COUNT, (Double) statisticsMap.get( MAPPINGS_COUNT ) + tssResultNew.getStats().getMappingCount() );
                    statisticsMap.put( AVERAGE_MAPPINGS_LENGTH, (Double) statisticsMap.get( AVERAGE_MAPPINGS_LENGTH ) + tssResultNew.getStats().getAverageReadLength() );
                    statisticsMap.put( MAPPINGS_MILLION, (Double) statisticsMap.get( MAPPINGS_MILLION ) + tssResultNew.getStats().getMappingsPerMillion() );
                    statisticsMap.put( BACKGROUND_THRESHOLD_MIN_STACKSIZE, (Double) statisticsMap.get( BACKGROUND_THRESHOLD_MIN_STACKSIZE ) + tssResultNew.getStats().getBgThreshold() );

                    tssResultNew.setStatsAndParametersMap( statisticsMap );

                    TableRowSorter<TableModel> sorter = new TableRowSorter<>();
                    tSSTable.setRowSorter( sorter );
                    sorter.setModel( model );
                    TableComparatorProvider.setPersistentTrackComparator( sorter, 1 );
                }


            } );
        }
    }


    /**
     * This method checks all entries for changes in current table model.
     *
     * @return the updated List<TranscriptionStart>
     */
    public List<TranscriptionStart> updateTssResults() {
        List<TranscriptionStart> tss = tssResult.getResults();
        HashMap<Integer, TranscriptionStart> tmpHash = new HashMap<>();
        tmpHash.putAll( this.tssInHash );

        // iterating over all table entries
        for( int i = 0; i < tSSTable.getRowCount(); i++ ) {
            Integer posTableAti = (Integer) tSSTable.getValueAt( i, 0 );
            if( tmpHash.containsKey( posTableAti ) ) {

                // leaderless
                if( (Boolean) tSSTable.getValueAt( i, 8 ) ) {
                    this.tssInHash.get( posTableAti ).setLeaderless( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setLeaderless( false );
                }

                // putative CDS-shift
                if( (Boolean) tSSTable.getValueAt( i, 9 ) ) {
                    this.tssInHash.get( posTableAti ).setCdsShift( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setCdsShift( false );
                }

                // false positive
                if( (Boolean) tSSTable.getValueAt( i, 10 ) ) {
                    this.tssInHash.get( posTableAti ).setFalsePositive( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setFalsePositive( false );
                }

                // inTRAgenic
                if( (Boolean) tSSTable.getValueAt( i, 11 ) ) {
                    this.tssInHash.get( posTableAti ).setIntragenicTSS( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setIntragenicTSS( false );
                }

                // inTERgenic
                if( (Boolean) tSSTable.getValueAt( i, 12 ) ) {
                    this.tssInHash.get( posTableAti ).setIntergenicTSS( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setIntergenicTSS( false );
                }

                // andisense
                if( (Boolean) tSSTable.getValueAt( i, 13 ) ) {
                    this.tssInHash.get( posTableAti ).setPutativeAntisense( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setPutativeAntisense( false );
                }

                // slection for upstream analyses
                if( (Boolean) tSSTable.getValueAt( i, 14 ) ) {
                    this.tssInHash.get( posTableAti ).setSelected( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setSelected( false );
                }

                if( (Boolean) tSSTable.getValueAt( i, 15 ) ) {
                    this.tssInHash.get( posTableAti ).setIsconsideredTSS( true );
                }
                else {
                    this.tssInHash.get( posTableAti ).setIsconsideredTSS( false );
                }

                this.tssInHash.get( posTableAti ).setComment( (String) tSSTable.getValueAt( i, 1 ) );

                tmpHash.remove( posTableAti );
            }
        }
        for( Integer key : tmpHash.keySet() ) {
            TranscriptionStart ts = tmpHash.get( key );
            tssInHash.remove( key );
            tss.remove( ts );
        }
        return tss;
    }


    @Override
    public void update( Object args ) {
        if( args instanceof RbsMotifSearchPanel ) {
            RbsMotifSearchPanel panel = (RbsMotifSearchPanel) args;
            if( this.model == null ) {
                this.model = new MotifSearchModel( this.persistantRef );
                this.model.storeRbsAnalysisResults( panel.getUpstreamRegions(),
                                                    panel.getRbsStarts(), panel.getRbsShifts(),
                                                    panel.getParams(), this.updateTssResults()
                );
            }
            else {
                this.model.storeRbsAnalysisResults( panel.getUpstreamRegions(),
                                                    panel.getRbsStarts(), panel.getRbsShifts(),
                                                    panel.getParams(), this.updateTssResults()
                );
            }
        }

        if( args instanceof MotifSearchPanel ) {
            MotifSearchPanel panel = (MotifSearchPanel) args;
            if( this.model == null ) {
                this.model = new MotifSearchModel( this.persistantRef );
                this.model.storePromoterAnalysisResults( panel.getUpstreamRegions(),
                                                         panel.getMinus10Starts(), panel.getMinus35Starts(),
                                                         panel.getMinus10Shifts(), panel.getMinus35Shifts(),
                                                         panel.getParams(), this.updateTssResults() );
            }
            else {
                this.model.storePromoterAnalysisResults( panel.getUpstreamRegions(),
                                                         panel.getMinus10Starts(), panel.getMinus35Starts(),
                                                         panel.getMinus10Shifts(), panel.getMinus35Shifts(),
                                                         panel.getParams(), this.updateTssResults() );
            }
        }
    }


    /**
     * Prepares the result for output. Any special operations are carried out
     * here. In this case generating the promotor region for each TSS.
     */
    private void processResultForExport() {
        //Generating promotor regions for the TSS
        this.promotorRegions = new ArrayList<>();

        //get reference sequence for promotor regions
        String promotor;

        //get the promotor region for each TSS
        int promotorStart;
        int chromLength = this.persistantRef.getActiveChromosome().getLength();
        for( TranscriptionStart tSS : this.tssResult.getResults() ) {
            if( tSS.isFwdStrand() ) {
                promotorStart = tSS.getStartPosition() - 70;
                promotorStart = promotorStart < 0 ? 0 : promotorStart;
                promotor = this.persistantRef.getActiveChromSequence( promotorStart, tSS.getStartPosition() );
            }
            else {
                promotorStart = tSS.getStartPosition() + 70;
                promotorStart = promotorStart > chromLength ? chromLength : promotorStart;
                promotor = SequenceUtils.getReverseComplement( this.persistantRef.getActiveChromSequence( tSS.getStartPosition(), promotorStart ) );
            }
            this.promotorRegions.add( promotor );
        }
        tssResult.setPromotorRegions( promotorRegions );
    }


    /**
     * For a given list of entries consisting of the locus, start, stop ant the
     * 5'-utr length this method generates a tab-separated file. Each row in
     * represents an entry in the given list.
     *
     * @param fileLocation     absolute filelocation.
     * @param referenceEntries
     */
    private void writeReferenceFile( String fileLocation, Map<String, String[]> referenceEntries ) {

        try( Writer writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( fileLocation ) ) ) ) {

            writer.write( "#Locus\tStart of Transcript\tStop of Transcript\tTSSs\tUTRs\tRBS\tPromotors\n" );
            for( String locus : referenceEntries.keySet() ) {
                writer.write( locus + "\t" + referenceEntries.get( locus )[0] + "\t" + referenceEntries.get( locus )[1] + "\t" + referenceEntries.get( locus )[2] + "\t" + referenceEntries.get( locus )[3] + "\t" + referenceEntries.get( locus )[4] + "\t" + referenceEntries.get( locus )[5] + "\t" + referenceEntries.get( locus )[6] + "\n" );
            }

            // report
        }
        catch( IOException ex ) {
            JOptionPane.showMessageDialog( this, "An error occured during wrtinging of the reference file: " + ex.getMessage(), "Write Reference Exception", JOptionPane.ERROR_MESSAGE );
        }
    }


}
