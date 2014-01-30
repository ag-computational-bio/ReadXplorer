package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.PromotorSearchParameters;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsAnalysisParameters;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MotifSearchPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MotifSearchModel;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickDeletion;
import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.ChromosomeObserver;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.exporter.excel.ExcelExportFileChooser;
import de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration.ChartsGenerationSelectChatTypeWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration.PlotGenerator;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.PurposeEnum;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableFormatExporter;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableSettingsWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MultiPurposeTopComponent;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.PromotorAnalysisWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis.DataSelectionWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsAnalysisWizardIterator;
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
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.ui.InteractivePanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
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
import org.openide.util.NbBundle;

/**
 * This panel is capable of showing a table with transcription start sites and
 * contains an export button, which exports the data into an excel file.
 * Additionally it has now an import button, which imports an excel file and
 * visualize the data to a JPanel.
 *
 * @author -Rolf Hilker-, -jritter-
 */
public class ResultPanelTranscriptionStart extends ResultTablePanel implements Observer {

    private static final long serialVersionUID = 1L;
    public static final String TSS_TOTAL = "Total number of detected TSSs";
    public static final String TSS_CORRECT = "Correct TSS";
    public static final String TSS_FWD = "TSS on fwd strand";
    public static final String TSS_REV = "TSS on rev strand";
    public static final String TSS_LEADERLESS = "TSS without 5'UTR";
    public static final String TSS_PUTATIVE_UNANNOTATED = "TSS putative unannotated";
    public static final String MAPPINGS_MILLION = "Mappings per Million";
    public static final String MAPPINGS_MEAN_LENGTH = "Mean of Mappings length";
    public static final String MAPPINGS_COUNT = "Mappings count";
    public static final String TSS_INTERNAL = "Internal TSS";
    public static final String BACKGROUND_THRESHOLD = "Background threshold";
    public static final String TSS_PUTATIVE_ANTISENSE = "Putative antisense";
    public static final String TSS_FRACTION = "Choosen fraction";
    public static final String TSS_RATIO = "Choosen ratio";
    public static final String TSS_EXCLUSION_OF_INTERNAL_TSS = "Choosen exclusion of all internal TSS";
    public static final String TSS_RANGE_FOR_LEADERLESS_DETECTION = "Range for Leaderless detection";
    public static final String TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS = "Percentage value for CDS-shift analysis";
    public static final String TSS_NO_PUTATIVE_CDS_SHIFTS = "Number of putative cds-shifts";
    public static final String TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION = "Limitation for distance between TSS and TLS";
    public static final String TSS_LIMITATION_FOR_DISTANCE_KEEPING_INTERNAL_TSS = "Limitation for distance between internal TSS and next upstream feature TLS";
    public static final String BIN_SIZE = "size of bin";
    public final TableType tableType = TableType.TSS_TABLE;
    public static final int UNUSED_STATISTICS_VALUE = -1;
    private BoundsInfoManager boundsInfoManager;
    private ReferenceViewer referenceViewer;
    private TSSDetectionResults tssResult;
    private HashMap<String, Object> statisticsMap;
    private TableRightClickFilter<UneditableTableModel> tableFilter = new TableRightClickFilter<>(UneditableTableModel.class);
    private TableRightClickDeletion<DefaultTableModel> rowDeletion = new TableRightClickDeletion();
    private List<String> promotorList;
    private HashMap<Integer, TranscriptionStart> tssInHash;
    private MotifSearchModel model;
    private MultiPurposeTopComponent topComponent;
    private ElementsOfInterest elements = null;
    private PlotGenerator gen;
    private ProgressHandle progresshandle;
    private List<String> promotorRegions;

    /**
     * This panel is capable of showing a table with transcription start sites
     * and contains an export button, which exports the data into an excel file.
     */
    public ResultPanelTranscriptionStart() {
        this.initComponents();
        this.tSSTable.getTableHeader().addMouseListener(tableFilter);
        this.tSSTable.addMouseListener(rowDeletion);
        this.initStatsMap();

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.tSSTable.getSelectionModel();
        model.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int posColumnIdx = 0;
                int chromColumnIdx = 1;
                TableUtils.showPosition(tSSTable, posColumnIdx, chromColumnIdx, boundsInfoManager);
            }
        });
    }

    public void setDefaultTableModelToTable(DefaultTableModel model) {
        this.tSSTable.setModel(model);
    }

    /**
     * Initializes the statistics map.
     */
    private void initStatsMap() {
        statisticsMap = new HashMap<>();
        statisticsMap.put(TSS_TOTAL, 0);
        statisticsMap.put(TSS_CORRECT, 0);
        statisticsMap.put(TSS_FWD, 0);
        statisticsMap.put(TSS_REV, 0);
        statisticsMap.put(TSS_LEADERLESS, 0);
        statisticsMap.put(TSS_INTERNAL, 0);
        statisticsMap.put(TSS_PUTATIVE_ANTISENSE, 0);
        statisticsMap.put(MAPPINGS_COUNT, 0.0);
        statisticsMap.put(MAPPINGS_MEAN_LENGTH, 0.0);
        statisticsMap.put(MAPPINGS_MILLION, 0.0);
        statisticsMap.put(BACKGROUND_THRESHOLD, 0.0);
        statisticsMap.put(TSS_NO_PUTATIVE_CDS_SHIFTS, 0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
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

        tSSTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Position", "Chromosome", "Comments", "Direction", "Read starts", "Rel. count", "Gene name", "Gene locus", "offset", "Leaderless", "CDS-Shift", "False positive", "Internal TSS", "Putative Antisense", "Upstream region analysis", "Finished", "Gene start", "Gene stop", "Gene length", "Gene Frame", "Gene product", "Start codon", "Stop codon", "Chrom ID", "Track ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false, false, false, false, false, false, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false
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
        tSSTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title0_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title22_2")); // NOI18N
        tSSTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title24_2")); // NOI18N
        tSSTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title1_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title2_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title14_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_1_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title15_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title21_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title21_2")); // NOI18N
        tSSTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title6_1_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_2_1")); // NOI18N
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
        tSSTable.getColumnModel().getColumn(23).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title23_2")); // NOI18N
        tSSTable.getColumnModel().getColumn(24).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title29_1_1")); // NOI18N

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 427, Short.MAX_VALUE)
                .addComponent(sequinTableExporter)
                .addGap(18, 18, 18)
                .addComponent(exportButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tssScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(performRbsAnalysis)
                    .addComponent(performPromotorAnalysis)
                    .addComponent(performDeletionOfAllFP)
                    .addComponent(startChartsOfTssData)
                    .addComponent(statisticsButton)
                    .addComponent(exportButton)
                    .addComponent(sequinTableExporter))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

        List<TranscriptionStart> tss = this.updateTssResults();
        tssResult.setResults(tss);

        processResultForExport();
        tssResult.setPromotorRegions(promotorRegions);

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
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            ExcelExportFileChooser fileChooser = new ExcelExportFileChooser(new String[]{"xls"}, "xls", this.tssResult);
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog(this, new TssDetectionStatsPanel(statisticsMap), "TSS Detection Statistics", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_statisticsButtonActionPerformed

    private void tSSTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tSSTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tSSTableMouseClicked

    private void startChartsOfTssDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startChartsOfTssDataActionPerformed
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new ChartsGenerationSelectChatTypeWizardPanel());
        panels.add(new DataSelectionWizardPanel(PurposeEnum.CHARTS));
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final List<TranscriptionStart> currentTss = updateTssResults();


            boolean takeAllElements = (boolean) wiz.getProperty(ElementsOfInterest.ALL.toString());
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_LEADERLESS.toString());
            boolean takeOnlyAntisense = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_ANTISENSE.toString());
            boolean takeOnlyNonLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_NONE_LEADERLESS.toString());
            boolean takeOnlyRealTss = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_REAL_TSS.toString());
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_SELECTED.toString());

            boolean isAbsoluteFrequencyPlot = (boolean) wiz.getProperty(ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs.toString());
            boolean isBaseDistributionPlot = (boolean) wiz.getProperty(ChartType.BASE_DISTRIBUTION.toString());

            final int lengthRelToTls = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_REGION_LENGTH);
            final int binSize = (int) wiz.getProperty(BIN_SIZE);

            if (takeAllElements) {
                elements = ElementsOfInterest.ALL;
            } else if (takeOnlyLeaderless) {
                elements = ElementsOfInterest.ONLY_LEADERLESS;
            } else if (takeOnlyAntisense) {
                elements = ElementsOfInterest.ONLY_ANTISENSE;
            } else if (takeOnlyNonLeaderless) {
                elements = ElementsOfInterest.ONLY_NONE_LEADERLESS;
            } else if (takeOnlyRealTss) {
                elements = ElementsOfInterest.ONLY_REAL_TSS;
            } else if (takeOnlySelectedElements) {
                elements = ElementsOfInterest.ONLY_SELECTED;
            }

            if (isAbsoluteFrequencyPlot) {
//                appPanelTopComponent = new AppPanelTopComponent();
                topComponent = new MultiPurposeTopComponent(PurposeEnum.CHARTS);
                topComponent.setLayout(new BorderLayout());
                topComponent.open();
                topComponent.setName("Distribution of 5′-UTR lengths");
                Thread plotGeneration = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PlotGenerator gen = new PlotGenerator();
                        List<DataTable> dataList = gen.prepareData(ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs, elements, currentTss, referenceViewer, lengthRelToTls, binSize);
                        ParameterSetFiveEnrichedAnalyses tssParameters = (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters();
                        double minXvalue = -Double.valueOf(tssParameters.getLeaderlessLimit());
                        InteractivePanel panel = gen.generateBarPlot(dataList.get(0), "5′-UTR lenght (distance between TSS and TLS)", "Absolute frequency", minXvalue);
                        topComponent.add(panel, BorderLayout.CENTER);
                    }
                });
                plotGeneration.start();
            }

            if (isBaseDistributionPlot) {
                topComponent = new MultiPurposeTopComponent(PurposeEnum.CHARTS);
                topComponent.setLayout(new BorderLayout());
                topComponent.open();
                topComponent.setName("GA content distribution");
                Thread plotGeneration = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gen = new PlotGenerator();
                        List<DataTable> dataList = gen.prepareData(ChartType.BASE_DISTRIBUTION, elements, currentTss, referenceViewer, lengthRelToTls, binSize);
                        InteractivePanel panel = gen.generateOverlappedAreaPlot(dataList.get(0), dataList.get(1), "upstream position relative to start codon (nt)", "purine/pyrimidine distribution (relative abbundance)");
                        topComponent.add(panel, BorderLayout.CENTER);
                    }
                });
                plotGeneration.start();
            }

            this.tssResult.setResults(currentTss);
        }
    }//GEN-LAST:event_startChartsOfTssDataActionPerformed

    private void performDeletionOfAllFPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performDeletionOfAllFPActionPerformed
        // delete all false positive TSS from table and TSS array
        List<TranscriptionStart> tss = this.updateTssResults();
        DefaultTableModel tableModel = (DefaultTableModel) tSSTable.getModel();
        List<Integer> valuesToRemove = new ArrayList<>();
        int columnNo = tSSTable.getRowCount();
        for (int i = 0; i < columnNo; i++) {
            Integer posTableAti = (Integer) tSSTable.getValueAt(i, 0);
            TranscriptionStart ts = tssInHash.get(posTableAti);
            boolean isFalsePositive = (boolean) tSSTable.getValueAt(i, 10);
            if (isFalsePositive) {
                tssInHash.remove(posTableAti);
                valuesToRemove.add(i);
                tss.remove(ts);
            }
        }

        for (int i = valuesToRemove.size() - 1; i >= 0; i--) {
            Integer x = valuesToRemove.get(i);
            tableModel.removeRow(x);
        }

        tSSTable.setModel(tableModel);
        tSSTable.updateUI();
        tssResult.setResults(tss);

    }//GEN-LAST:event_performDeletionOfAllFPActionPerformed

    private void performPromotorAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performPromotorAnalysisActionPerformed

        PromotorAnalysisWizardIterator wizard = new PromotorAnalysisWizardIterator();
        WizardDescriptor wiz = new WizardDescriptor(wizard);
        wizard.setWiz(wiz);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(ResultPanelTranscriptionStart.class, "TTL_MotifSearchWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {

            boolean takeAllElements = (boolean) wiz.getProperty(ElementsOfInterest.ALL.toString());
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_LEADERLESS.toString());
            boolean takeOnlyAntisense = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_ANTISENSE.toString());
            boolean takeOnlyNonLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_NONE_LEADERLESS.toString());
            boolean takeOnlyRealTss = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_REAL_TSS.toString());
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_SELECTED.toString());

            if (takeAllElements) {
                this.elements = ElementsOfInterest.ALL;
            } else if (takeOnlyLeaderless) {
                this.elements = ElementsOfInterest.ONLY_LEADERLESS;
            } else if (takeOnlyAntisense) {
                this.elements = ElementsOfInterest.ONLY_ANTISENSE;
            } else if (takeOnlyNonLeaderless) {
                this.elements = ElementsOfInterest.ONLY_NONE_LEADERLESS;
            } else if (takeOnlyRealTss) {
                this.elements = ElementsOfInterest.ONLY_REAL_TSS;
            } else if (takeOnlySelectedElements) {
                this.elements = ElementsOfInterest.ONLY_SELECTED;
            }

            int minus10MotifWidth = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS10_MOTIF_LENGTH);
            int minus35MotifWidth = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS35_MOTIF_LENGTH);
            int noOfTimes = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_NUMBER_OF_TRYING);
            int minSpacer1 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER1_LENGTH);
            int minSpacer2 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER2_LENGTH);
            int seqWidthMinus10 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_10_REGION);
            int seqWidthMinus35 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_35_REGION);
            int alternativeSpacer = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ALTERNATIVE_SPACER);
            final int lengthRelToTss = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS);

            final PromotorSearchParameters params = new PromotorSearchParameters(
                    minus10MotifWidth, minus35MotifWidth,
                    noOfTimes, minSpacer1, minSpacer2, alternativeSpacer,
                    seqWidthMinus10, seqWidthMinus35, lengthRelToTss);


            final List<TranscriptionStart> starts = updateTssResults();
            this.topComponent = new MultiPurposeTopComponent(PurposeEnum.MOTIF_SEARCH);
            this.topComponent.setLayout(new BorderLayout());
            this.topComponent.open();
            String type = elements.toString().toLowerCase();
            this.topComponent.setName("Promotor motif search for " + type + " elements in Table");
            Thread promotorSearch = new Thread(new Runnable() {
                @Override
                public void run() {
                    model = new MotifSearchModel(referenceViewer);
                    model.takeUpstreamRegions(elements, starts, lengthRelToTss, false);
                    MotifSearchPanel promotorMotifSearchPanel = model.utrPromotorAnalysis(params, starts, null);
                    promotorMotifSearchPanel.updateUI();
                    topComponent.add(promotorMotifSearchPanel, BorderLayout.CENTER);
                    topComponent.updateUI();
                }
            });
            promotorSearch.start();

            this.tssResult.setResults(starts);
        }
    }//GEN-LAST:event_performPromotorAnalysisActionPerformed

    private void performRbsAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performRbsAnalysisActionPerformed
        final List<TranscriptionStart> currentTss = updateTssResults();
        RbsAnalysisWizardIterator wizard = new RbsAnalysisWizardIterator();
        WizardDescriptor wiz = new WizardDescriptor(wizard);
        wizard.setWiz(wiz);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(ResultPanelTranscriptionStart.class, "TTL_MotifSearchWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {

            boolean takeAllElements = (boolean) wiz.getProperty(ElementsOfInterest.ALL.toString());
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_LEADERLESS.toString());
            boolean takeOnlyAntisense = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_ANTISENSE.toString());
            boolean takeOnlyNonLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_NONE_LEADERLESS.toString());
            boolean takeOnlyRealTss = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_REAL_TSS.toString());
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_SELECTED.toString());

            if (takeAllElements) {
                elements = ElementsOfInterest.ALL;
            } else if (takeOnlyLeaderless) {
                elements = ElementsOfInterest.ONLY_LEADERLESS;
            } else if (takeOnlyAntisense) {
                elements = ElementsOfInterest.ONLY_ANTISENSE;
            } else if (takeOnlyNonLeaderless) {
                elements = ElementsOfInterest.ONLY_NONE_LEADERLESS;
            } else if (takeOnlyRealTss) {
                elements = ElementsOfInterest.ONLY_REAL_TSS;
            } else if (takeOnlySelectedElements) {
                elements = ElementsOfInterest.ONLY_SELECTED;
            }

//            File workingDir = (File) wiz.getProperty(RbsAnalysisWizardIterator.PROP_WORKING_DIR);

            final int lengthRelToTls = (int) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS);
            int motifWidth = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH);
            int noOfTrying = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR);
            int minSpacer = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER);

//            final RbsAnalysisParameters params = new RbsAnalysisParameters(workingDir, lengthRelToTls, motifWidth, noOfTrying, minSpacer);
            final RbsAnalysisParameters params = new RbsAnalysisParameters(lengthRelToTls, motifWidth, noOfTrying, minSpacer);

//            appPanelTopComponent = new AppPanelTopComponent();
            topComponent = new MultiPurposeTopComponent(PurposeEnum.MOTIF_SEARCH);
            topComponent.setLayout(new BorderLayout());
            topComponent.open();
            String type = elements.toString().toLowerCase();
            topComponent.setName("RBS motif search for " + type + " elements in Table");

            Thread promotorSearch = new Thread(new Runnable() {
                @Override
                public void run() {
                    model = new MotifSearchModel(referenceViewer);
                    model.takeUpstreamRegions(elements, currentTss, lengthRelToTls, true);
                    model.rbsMotifAnalysis(params, currentTss, null);
                    topComponent.add(model.getRbsMotifSearchPanel(), BorderLayout.CENTER);
                }
            });
            promotorSearch.start();
            this.tssResult.setResults(currentTss);
        }
    }//GEN-LAST:event_performRbsAnalysisActionPerformed

    private void sequinTableExporterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequinTableExporterActionPerformed

        final String wizardName = "Sequin Feature Table Export";
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new SequinTableSettingsWizardPanel(wizardName));
        DataSelectionWizardPanel selection = new DataSelectionWizardPanel(PurposeEnum.SEQUIN_EXPORT);
        selection.getComponent().disableTF();
        panels.add(selection);
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {

            progresshandle = ProgressHandleFactory.createHandle("Export of feature table");
            final ArrayList<TranscriptionStart> tss = (ArrayList<TranscriptionStart>) this.updateTssResults();
            tssResult.setResults(tss);


            final String featureName = (String) wiz.getProperty(SequinTableSettingsWizardPanel.SEQUIN_EXPORT_FEATURE_NAME);
            boolean takeAllElements = (boolean) wiz.getProperty(ElementsOfInterest.ALL.toString());
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_LEADERLESS.toString());
            boolean takeOnlyAntisense = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_ANTISENSE.toString());
            boolean takeOnlyNonLeaderless = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_NONE_LEADERLESS.toString());
            boolean takeOnlyRealTss = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_REAL_TSS.toString());
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty(ElementsOfInterest.ONLY_SELECTED.toString());

            final List<TranscriptionStart> filteredTssList = new ArrayList<>();
            if (takeAllElements) {
                elements = ElementsOfInterest.ALL;
                filteredTssList.addAll(tss);
            } else if (takeOnlyLeaderless) {
                elements = ElementsOfInterest.ONLY_LEADERLESS;
                for (TranscriptionStart ts : tss) {
                    if (ts.isLeaderless()) {
                        filteredTssList.add(ts);
                    }
                }
            } else if (takeOnlyAntisense) {
                elements = ElementsOfInterest.ONLY_ANTISENSE;
                for (TranscriptionStart ts : tss) {
                    if (ts.isPutativeAntisense()) {
                        filteredTssList.add(ts);
                    }
                }
            } else if (takeOnlyNonLeaderless) {
                elements = ElementsOfInterest.ONLY_NONE_LEADERLESS;
                for (TranscriptionStart ts : tss) {
                    if (!ts.isLeaderless()) {
                        filteredTssList.add(ts);
                    }
                }
            } else if (takeOnlyRealTss) {
                elements = ElementsOfInterest.ONLY_REAL_TSS;
                for (TranscriptionStart ts : tss) {
                    if (!ts.isLeaderless() && !ts.isInternalTSS() && !ts.isPutativeAntisense()) {
                        filteredTssList.add(ts);
                    }
                }
            } else if (takeOnlySelectedElements) {
                elements = ElementsOfInterest.ONLY_SELECTED;
                for (TranscriptionStart ts : tss) {
                    if (ts.isSelected()) {
                        filteredTssList.add(ts);
                    }
                }
            }




            ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser(new String[]{"tbl"}, "Table files for Sequin export") {
                @Override
                public void save(String fileLocation) {
                    progresshandle.start(5);
                    SequinTableFormatExporter exporter = new SequinTableFormatExporter(new File(fileLocation), (ArrayList<TranscriptionStart>) filteredTssList, null, null, tableType, featureName); //To change body of generated methods, choose Tools | Templates.
                    progresshandle.progress(1);
                    exporter.start();
                    progresshandle.progress(2);
                }

                @Override
                public void open(String fileLocation) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };


            fileChooser.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
            progresshandle.progress(3);
            progresshandle.progress(5);

            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Export was successfull!",
                    "Export was successfull!", JOptionPane.INFORMATION_MESSAGE);
            progresshandle.finish();
        }
    }//GEN-LAST:event_sequinTableExporterActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
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
     * result
     */
    public void setReferenceViewer(ReferenceViewer referenceViewer) {
        this.boundsInfoManager = referenceViewer.getBoundsInformationManager();
        this.referenceViewer = referenceViewer;
    }

    /**
     * @return The number of detected TSS
     */
    @Override
    public int getDataSize() {
        return this.tssResult.getResults().size();
    }

    @Override
    public void addResult(ResultTrackAnalysis newResult) {
        if (newResult instanceof TSSDetectionResults) {
            final TSSDetectionResults tssResultNew = (TSSDetectionResults) newResult;
            final List<TranscriptionStart> tsss = new ArrayList<>(tssResultNew.getResults());
            this.tssInHash = new HashMap<>();

            if (tssResult == null) {
                tssResult = tssResultNew;
            } else {
                tssResult.getResults().addAll(tssResultNew.getResults());
            }

            final int nbColumns = 25;

            // statistic values
            int noCorrectStarts = 0;
            int noFwdFeatures = 0;
            int noRevFeatures = 0;
            int noLeaderlessFeatures = 0;
            int noInternalTSS = 0;
            int noPutativeAntisense = 0;
            int noPutCdsShifts = 0;
            int noTssUnannotated = 0;


            final DefaultTableModel model = (DefaultTableModel) tSSTable.getModel();

            String strand;
            PersistantFeature feature;
            PersistantFeature nextDownstreamFeature;

            for (TranscriptionStart tSS : tsss) {

                if (tSS.isFwdStrand()) {
                    strand = SequenceUtils.STRAND_FWD_STRING;
                    ++noFwdFeatures;
                } else {
                    strand = SequenceUtils.STRAND_REV_STRING;
                    ++noRevFeatures;
                }

                boolean leaderless = tSS.isLeaderless();
                if (leaderless) {
                    ++noLeaderlessFeatures;
                }
                final Object[] rowData = new Object[nbColumns];
                int position = tSS.getStartPosition();
                this.tssInHash.put(position, tSS);

                int i = 0;
                // 0
                rowData[i++] = position;
                // 1
                rowData[i++] = newResult.getChromosomeMap().get(tSS.getChromId());
                // 2
                rowData[i++] = tSS.getComment();
                // 3
                rowData[i++] = strand;
                // 4
                rowData[i++] = tSS.getReadStarts();
                // 5
                rowData[i++] = tSS.getRelCount();

                feature = tSS.getDetectedGene();
                nextDownstreamFeature = tSS.getNextGene();

                if (feature != null) {
                    // 6
                    rowData[i++] = feature.toString();
                    // 7
                    rowData[i++] = feature.getLocus();
                    // 8
                    rowData[i++] = tSS.getOffset();
                    ++noCorrectStarts;
                } else if (nextDownstreamFeature != null) {
                    rowData[i++] = nextDownstreamFeature.toString();
                    rowData[i++] = nextDownstreamFeature.getLocus();
                    rowData[i++] = tSS.getNextOffset();
                } 
                else {
                    rowData[i++] = "-";
                    rowData[i++] = "-";
                    rowData[i++] = "-";
                    noTssUnannotated++;
                }

                // 9
                rowData[i++] = leaderless;

                // 10
                boolean cdsShift = tSS.isCdsShift();
                rowData[i++] = cdsShift;
                if (cdsShift) {
                    noPutCdsShifts++;
                }

                // 11
                rowData[i++] = false;

                // 12
                rowData[i++] = tSS.isInternalTSS();
                if (tSS.isInternalTSS()) {
                    noInternalTSS++;
                }

                // 13
                rowData[i++] = tSS.isPutativeAntisense();
                if (tSS.isPutativeAntisense()) {
                    noPutativeAntisense++;
                }

                // 14
                rowData[i++] = tSS.isSelected();

                // 15
                rowData[i++] = tSS.isConsideredTSS();

                // additionally informations about detected gene
                if (feature != null) {
                    rowData[i++] = feature.isFwdStrand() ? feature.getStart() : feature.getStop();
                    rowData[i++] = feature.isFwdStrand() ? feature.getStop() : feature.getStart();
                    rowData[i++] = feature.getStop() - feature.getStart();
                    int start = feature.isFwdStrand() ? feature.getStart() : feature.getStop();
                    if ((start % 3) == 0) {
                        rowData[i++] = 3;
                    } else if (start % 3 == 1) {
                        rowData[i++] = 1;
                    } else if (start % 3 == 2) {
                        rowData[i++] = 2;
                    }
                    rowData[i++] = feature.getProduct();
                } else if (nextDownstreamFeature != null) {
                    rowData[i++] = nextDownstreamFeature.isFwdStrand() ? nextDownstreamFeature.getStart() : nextDownstreamFeature.getStop();
                    rowData[i++] = nextDownstreamFeature.isFwdStrand() ? nextDownstreamFeature.getStop() : nextDownstreamFeature.getStart();
                    rowData[i++] = nextDownstreamFeature.getStop() - nextDownstreamFeature.getStart();
                    int start = nextDownstreamFeature.isFwdStrand() ? nextDownstreamFeature.getStart() : nextDownstreamFeature.getStop();
                    if ((start % 3) == 0) {
                        rowData[i++] = 3;
                    } else if (start % 3 == 1) {
                        rowData[i++] = 1;
                    } else if (start % 3 == 2) {
                        rowData[i++] = 2;
                    }
                    rowData[i++] = nextDownstreamFeature.getProduct();
                } 
                else {
                    rowData[i++] = "-";
                    rowData[i++] = "-";
                    rowData[i++] = "-";
                    rowData[i++] = "-";
                    rowData[i++] = "-";
                }

                rowData[i++] = tSS.getDetectedFeatStart();
                rowData[i++] = tSS.getDetectedFeatStop();
                rowData[i++] = tSS.getChromId();
                rowData[i++] = tSS.getTrackId();

                SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
                    @Override
                    public void run() {
                        model.addRow(rowData);
                    }
                });
            }

            //create statistics
            ParameterSetFiveEnrichedAnalyses tssParameters = (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters();
            statisticsMap.put(TSS_TOTAL, (Integer) statisticsMap.get(TSS_TOTAL) + tsss.size());
            statisticsMap.put(TSS_CORRECT, (Integer) statisticsMap.get(TSS_CORRECT) + noCorrectStarts);
            statisticsMap.put(TSS_FWD, (Integer) statisticsMap.get(TSS_FWD) + noFwdFeatures);
            statisticsMap.put(TSS_REV, (Integer) statisticsMap.get(TSS_REV) + noRevFeatures);
            statisticsMap.put(TSS_LEADERLESS, (Integer) statisticsMap.get(TSS_LEADERLESS) + noLeaderlessFeatures);
            statisticsMap.put(TSS_INTERNAL, (Integer) statisticsMap.get(TSS_INTERNAL) + noInternalTSS);
            statisticsMap.put(TSS_PUTATIVE_ANTISENSE, (Integer) statisticsMap.get(TSS_PUTATIVE_ANTISENSE) + noPutativeAntisense);
            statisticsMap.put(MAPPINGS_COUNT, (Double) statisticsMap.get(MAPPINGS_COUNT) + tssResultNew.getStats().getMc());
            statisticsMap.put(MAPPINGS_MEAN_LENGTH, (Double) statisticsMap.get(MAPPINGS_MEAN_LENGTH) + tssResultNew.getStats().getMml());
            statisticsMap.put(MAPPINGS_MILLION, (Double) statisticsMap.get(MAPPINGS_MILLION) + tssResultNew.getStats().getMm());
            statisticsMap.put(BACKGROUND_THRESHOLD, (Double) statisticsMap.get(BACKGROUND_THRESHOLD) + tssResultNew.getStats().getBgThreshold());
            statisticsMap.put(TSS_NO_PUTATIVE_CDS_SHIFTS, (Integer) statisticsMap.get(TSS_NO_PUTATIVE_CDS_SHIFTS) + noPutCdsShifts);


            tssResultNew.setStatsAndParametersMap(statisticsMap);

            TableRowSorter<TableModel> sorter = new TableRowSorter<>();
            tSSTable.setRowSorter(sorter);
            sorter.setModel(model);
            TableComparatorProvider.setPersistantTrackComparator(sorter, 1);

        }
    }

    /**
     * Returns Promotors in List format. So the feature name of the coresponding
     * promotor sequence represents the header: >feature_name. And the following
     * Sequence corresponds to the previous header.
     *
     * @return list with alternately header, sequence Entries.
     */
    public List<String> getPromotorsAsFastaInList() {
        return promotorList;
    }

    public void setFastaList(List<String> fastaList) {
        this.promotorList = fastaList;
    }

    private List<TranscriptionStart> updateTssResults() {
        List<TranscriptionStart> tss = tssResult.getResults();
        HashMap<Integer, TranscriptionStart> tmpHash = new HashMap<>();
        tmpHash.putAll(this.tssInHash);

        for (int i = 0; i < tSSTable.getRowCount(); i++) {
            Integer posTableAti = (Integer) tSSTable.getValueAt(i, 0);
            if (tmpHash.containsKey(posTableAti)) {

                if ((Boolean) tSSTable.getValueAt(i, 13)) {
                    this.tssInHash.get(posTableAti).setSelected(true);
                } else {
                    this.tssInHash.get(posTableAti).setSelected(false);
                }
                if ((Boolean) tSSTable.getValueAt(i, 9)) {
                    this.tssInHash.get(posTableAti).setCdsShift(true);
                } else {
                    this.tssInHash.get(posTableAti).setCdsShift(false);
                }

                if ((Boolean) tSSTable.getValueAt(i, 14)) {
                    this.tssInHash.get(posTableAti).setIsconsideredTSS(true);
                } else {
                    this.tssInHash.get(posTableAti).setIsconsideredTSS(false);
                }

                this.tssInHash.get(posTableAti).setComment((String) tSSTable.getValueAt(i, 2));

                tmpHash.remove(posTableAti);
            }
        }
        for (Integer key : tmpHash.keySet()) {
            TranscriptionStart ts = tmpHash.get(key);
            tssInHash.remove(key);
            tss.remove(ts);
        }
        return tss;
    }

    @Override
    public void update(Object args) {
//        if(args instanceof List<Object>) {
//            List<Object> list = (List<Object>) args;
//            
//        }
//        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Prepares the result for output. Any special operations are carried out
     * here. In this case generating the promotor region for each TSS.
     */
    private void processResultForExport() {
        //Generating promotor regions for the TSS
        this.promotorRegions = new ArrayList<>();

        //get reference sequence for promotor regions
        PersistantReference ref = this.referenceViewer.getReference();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(ref.getId());
        ChromosomeObserver chromObserver = new ChromosomeObserver();
        String chromSeq = refConnector.getRefGenome().getActiveChromSequence(chromObserver);
        String promotor;

        //get the promotor region for each TSS
        int promotorStart;
        int chromLength = chromSeq.length();
        for (TranscriptionStart tSS : this.tssResult.getResults()) {
            if (tSS.isFwdStrand()) {
                promotorStart = tSS.getStartPosition() - 70;
                promotorStart = promotorStart < 0 ? 0 : promotorStart;
                promotor = chromSeq.substring(promotorStart, tSS.getStartPosition());
            } else {
                promotorStart = tSS.getStartPosition() + 70;
                promotorStart = promotorStart > chromLength ? chromLength : promotorStart;
                promotor = SequenceUtils.getReverseComplement(chromSeq.substring(tSS.getStartPosition(), promotorStart));
            }
            this.promotorRegions.add(promotor);
        }
        tssResult.setPromotorRegions(promotorRegions);
        refConnector.getRefGenome().getActiveChromosome().removeObserver(chromObserver);
    }
}
