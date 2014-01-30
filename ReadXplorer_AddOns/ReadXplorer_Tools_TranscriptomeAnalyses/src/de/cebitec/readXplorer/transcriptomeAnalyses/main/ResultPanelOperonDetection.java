package de.cebitec.readXplorer.transcriptomeAnalyses.main;

/*
 * GeneStartsResultPanel.java
 *
 * Created on 27.01.2012, 14:31:03
 */
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.PromotorSearchParameters;
import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.exporter.excel.ExcelExportFileChooser;
import de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration.ChartsGenerationSelectChatTypeWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration.PlotGenerator;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableFormatExporter;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.SequinTableSettingsWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.FivePrimeUTRPromotorSettingsWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MotifSearchModel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.MotifSearchPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.PromotorAnalysisWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsAnalysisParameters;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsAnalysisWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.RbsAnalysisWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.SequenceLengthSelectionForMotifAnalysis;
import de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch.SequenceLengthSelectionWizardPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis.DataSelectionWizardPanel;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.util.LineWrapCellRenderer;
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
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * This panel is capable of showing a table with detected operons and contains
 * an export button, which exports the data into an excel file.
 *
 * @author -Rolf Hilker-
 */
public class ResultPanelOperonDetection extends ResultTablePanel {

    private static final long serialVersionUID = 1L;
    public static final String OPERONS_TOTAL = "Total number of detected operons";
    public static final String OPERONS_WITH_OVERLAPPING_READS = "Operons with reads overlapping only one feature edge";
    public static final String OPERONS_WITH_INTERNAL_READS = "Operons with internal reads";
    public static final String OPERONS_BACKGROUND_THRESHOLD = "Minimum number of spanning reads (Background threshold)";
    public final TableType tableType = TableType.OPERON_TABLE;
    private BoundsInfoManager boundsInfoManager;
    private ReferenceViewer referenceViewer;
    private OperonDetectionResult operonResult;
    private HashMap<String, Object> operonDetStats;
    private TableRightClickFilter<UneditableTableModel> tableFilter = new TableRightClickFilter<>(UneditableTableModel.class);
    private ProgressHandle progresshandle;
    private HashMap<Integer, Operon> operonsInHash;
    private ElementsOfInterest elements = null;
    private MotifSearchModel motifSearch;
    private AppPanelTopComponent appPanelTopComponent;

    /**
     * This panel is capable of showing a table with detected operons and
     * contains an export button, which exports the data into an excel file.
     */
    public ResultPanelOperonDetection() {
        initComponents();
        this.operonDetectionTable.getTableHeader().addMouseListener(tableFilter);
        this.initStatsMap();

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.operonDetectionTable.getSelectionModel();
        model.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int posColumnIdx = 6;
                int chromColumnIdx = 4;
                TableUtils.showPosition(operonDetectionTable, posColumnIdx, chromColumnIdx, boundsInfoManager);
            }
        });
    }

    /**
     * Initializes the statistics map.
     */
    private void initStatsMap() {
        operonDetStats = new HashMap<>();
        operonDetStats.put(OPERONS_TOTAL, 0);
        operonDetStats.put(OPERONS_WITH_OVERLAPPING_READS, 0);
        operonDetStats.put(OPERONS_WITH_INTERNAL_READS, 0);
        operonDetStats.put(ResultPanelTranscriptionStart.MAPPINGS_COUNT, 0.0);
        operonDetStats.put(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH, 0.0);
        operonDetStats.put(ResultPanelTranscriptionStart.MAPPINGS_MILLION, 0.0);
        operonDetStats.put(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD, 0.0);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        operonDetectionTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        statisticsButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        rbsButton = new javax.swing.JButton();
        promotorAnalysisButton = new javax.swing.JButton();

        operonDetectionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Operon Start", "Feature 1", "Feature 2", "Track", "Chromosome", "Strand", "Start Feature 1", "Start Feature 2", "Upstream Analysis", "Finished", "Spanning Reads", "Operon string", "Chromosome ID", "Track ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, false, false, false, true, true, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(operonDetectionTable);
        operonDetectionTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title12")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title0")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title7")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title9")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title3")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title2")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title8")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title13")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title11")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title6")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title10")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title9_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title10_1")); // NOI18N

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

        rbsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.rbsButton.text")); // NOI18N
        rbsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbsButtonActionPerformed(evt);
            }
        });

        promotorAnalysisButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.promotorAnalysisButton.text")); // NOI18N
        promotorAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                promotorAnalysisButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbsButton)
                .addGap(18, 18, 18)
                .addComponent(promotorAnalysisButton)
                .addGap(18, 18, 18)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(exportButton))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 840, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(statisticsButton)
                        .addComponent(rbsButton)
                        .addComponent(promotorAnalysisButton))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(exportButton)
                        .addComponent(jButton1))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        List<Operon> operons = updateOperonResults();
        this.operonResult.setResults(operons);
        ExcelExportFileChooser fileChooser = new ExcelExportFileChooser(new String[]{"xls"}, "xls", operonResult);
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog(this, new OperonDetectionStatsPanel(operonDetStats), "Operon Detection Statistics", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_statisticsButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        final String wizardName = "Sequin Feature Table Export";
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new SequinTableSettingsWizardPanel(wizardName));
//        DataSelectionWizardPanel selection = new DataSelectionWizardPanel();
//        selection.getComponent().disableTF();
//        panels.add(selection);
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
        wiz.setTitle("Sequin Feature Table Export");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {

            final String featureName = (String) wiz.getProperty(SequinTableSettingsWizardPanel.SEQUIN_EXPORT_FEATURE_NAME);
            this.progresshandle = ProgressHandleFactory.createHandle("Export of feature table!");
            ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser(new String[]{"tbl"}, "Table files for Sequin export") {
                @Override
                public void save(String fileLocation) {
                    progresshandle.start(4);
                    progresshandle.progress(1);
                    SequinTableFormatExporter exporter = new SequinTableFormatExporter(new File(fileLocation), null, (ArrayList<Operon>) operonResult.getResults(), null, tableType, featureName); //To change body of generated methods, choose Tools | Templates.
                    progresshandle.progress(2);
                    exporter.start();
                    progresshandle.progress(3);
                }

                @Override
                public void open(String fileLocation) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };

            fileChooser.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
            progresshandle.progress(4);
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Export was successfull!",
                    "Export was successfull!", JOptionPane.INFORMATION_MESSAGE);
            progresshandle.finish();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void rbsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbsButtonActionPerformed
        List<Operon> updatedOperonList = updateOperonResults();
        operonResult.setResults(updatedOperonList);

        // RBS ANalysis
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new SequenceLengthSelectionWizardPanel(RbsAnalysisWizardIterator.PROP_WIZARD_NAME));
        panels.add(new RbsAnalysisWizardPanel(RbsAnalysisWizardIterator.PROP_WIZARD_NAME));
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
//            File workingDir = (File) wiz.getProperty(RbsAnalysisWizardIterator.PROP_WORKING_DIR);
            final int lengthRelToTls = (int) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS);
            int motifWidth = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH);
            int noOfTrying = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR);
            int minSpacer = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER);

            final RbsAnalysisParameters params = new RbsAnalysisParameters(lengthRelToTls, motifWidth, noOfTrying, minSpacer);
            final List<Operon> filteredForSelected = new ArrayList<>();
            for (Operon operon : updatedOperonList) {
                if (operon.isForUpstreamAnalysisMarked()) {
                    filteredForSelected.add(operon);
                }
            }

            appPanelTopComponent = new AppPanelTopComponent();
            appPanelTopComponent.setLayout(new BorderLayout());
            appPanelTopComponent.open();
            appPanelTopComponent.setName("Distribution of TSS distance to TLS");
            Thread rbsMotifSearchThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    motifSearch = new MotifSearchModel(referenceViewer);
                    motifSearch.takeSubRegionsForOperonAnalysis(filteredForSelected, lengthRelToTls, true);
                    motifSearch.rbsMotifAnalysis(params, null, updateOperonResults());
                    appPanelTopComponent.add(motifSearch.getRbsMotifSearchPanel(), BorderLayout.CENTER);
                }
            });
            rbsMotifSearchThread.start();

        }
    }//GEN-LAST:event_rbsButtonActionPerformed

    private void promotorAnalysisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_promotorAnalysisButtonActionPerformed
        List<Operon> updatedOperonList = updateOperonResults();
        operonResult.setResults(updatedOperonList);

        // Promotor ANalysis
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new SequenceLengthSelectionWizardPanel(RbsAnalysisWizardIterator.PROP_WIZARD_NAME));
        panels.add(new FivePrimeUTRPromotorSettingsWizardPanel(PromotorAnalysisWizardIterator.PROP_WIZARD_NAME));
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

            final List<Operon> filteredForSelected = new ArrayList<>();
            for (Operon operon : updatedOperonList) {
                if (operon.isForUpstreamAnalysisMarked()) {
                    filteredForSelected.add(operon);
                }
            }

            appPanelTopComponent = new AppPanelTopComponent();
            appPanelTopComponent.setLayout(new BorderLayout());
            appPanelTopComponent.open();
            appPanelTopComponent.setName("Distribution of TSS distance to TLS");
            Thread rbsMotifSearchThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    motifSearch = new MotifSearchModel(referenceViewer);
                    motifSearch.takeSubRegionsForOperonAnalysis(filteredForSelected, lengthRelToTss, false);
                    MotifSearchPanel panel = motifSearch.utrPromotorAnalysis(params, null, updateOperonResults());
                    appPanelTopComponent.add(panel, BorderLayout.CENTER);
                }
            });
            rbsMotifSearchThread.start();
        }
    }//GEN-LAST:event_promotorAnalysisButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable operonDetectionTable;
    private javax.swing.JButton promotorAnalysisButton;
    private javax.swing.JButton rbsButton;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Adds the data from this OperonDetectionResult to the data already
     * available in this result panel. All statistics etc. are also updated.
     *
     * @param newResult the result to add
     */
    @Override
    public void addResult(ResultTrackAnalysis newResult) {
        if (newResult instanceof OperonDetectionResult) {
            OperonDetectionResult operonResultNew = (OperonDetectionResult) newResult;
            final int nbColumns = 15;
            final List<Operon> operons = new ArrayList<>(operonResultNew.getResults());
            this.operonsInHash = new HashMap<>();

            if (this.operonResult == null) {
                this.operonResult = operonResultNew;
            } else {
                this.operonResult.getResults().addAll(operonResultNew.getResults());
            }

            final DefaultTableModel model = (DefaultTableModel) operonDetectionTable.getModel();
            LineWrapCellRenderer lineWrapCellRenderer = new LineWrapCellRenderer();
            operonDetectionTable.getColumnModel().getColumn(0).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(1).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(3).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(4).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(5).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(6).setCellRenderer(lineWrapCellRenderer);

            int operonsWithOverlapping = 0;
            int operonsWithInternal = 0;
            boolean hasOverlappingReads;
            boolean hasInternalReads;

            for (Operon operon : operons) {
                String annoName1 = "";
                String annoName2 = "";
                String strand;
                if (operon.isFwd()) {
                    strand = SequenceUtils.STRAND_FWD_STRING + "\n";
                } else {
                    strand = SequenceUtils.STRAND_REV_STRING + "\n";
                }
                String startAnno1 = "";
                String startAnno2 = "";
                String readsAnno1 = "";
                String readsAnno2 = "";
                String internalReads = "";
                String spanningReads = "";
                hasOverlappingReads = false;
                hasInternalReads = false;

                int startFirstFeature = 0;
                int chromID = operon.getOperonAdjacencies().get(0).getFeature1().getChromId();
                if (operon.getStartPositionOfTranscript() == 0) {
                    startFirstFeature = operon.getOperonAdjacencies().get(0).getFeature1().isFwdStrand() ? operon.getOperonAdjacencies().get(0).getFeature1().getStart() : operon.getOperonAdjacencies().get(0).getFeature1().getStop();
                    operon.setStartPositionOfTranscript(startFirstFeature);
                }
                this.operonsInHash.put(operon.getStartPositionOfTranscript(), operon);
                for (OperonAdjacency opAdj : operon.getOperonAdjacencies()) {
                    annoName1 += opAdj.getFeature1().toString() + "\n";
                    annoName2 += opAdj.getFeature2().toString() + "\n";
                    startAnno1 += opAdj.getFeature1().getStart() + "\n";
                    startAnno2 += opAdj.getFeature2().getStart() + "\n";
                    readsAnno1 += opAdj.getReadsFeature1() + "\n";
                    readsAnno2 += opAdj.getReadsFeature2() + "\n";
                    internalReads += opAdj.getInternalReads() + "\n";
                    spanningReads += opAdj.getSpanningReads() + "\n";

                    hasInternalReads = opAdj.getInternalReads() > 0;
                    hasOverlappingReads = opAdj.getReadsFeature1() > 0 || opAdj.getReadsFeature2() > 0;

                }
                final Object[] rowData = new Object[nbColumns];
                int i = 0;
                rowData[i++] = operon.getStartPositionOfTranscript();
                rowData[i++] = annoName1;
                rowData[i++] = annoName2;
                rowData[i++] = operonResultNew.getTrackMap().get(operon.getTrackId());
                rowData[i++] = operonResultNew.getChromosomeMap().get(operon.getOperonAdjacencies().get(0).getFeature1().getChromId());
                rowData[i++] = strand;
                rowData[i++] = startAnno1;
                rowData[i++] = startAnno2;
                rowData[i++] = operon.isForUpstreamAnalysisMarked();
                rowData[i++] = operon.isConsidered();
//                rowData[6] = readsAnno1;
//                rowData[7] = readsAnno2;
//                rowData[8] = internalReads;
                rowData[i++] = spanningReads;
                rowData[i++] = operon.toOperonString();
                rowData[i++] = chromID;
                rowData[i++] = operon.getTrackId();
                if (!annoName1.isEmpty() && !annoName2.isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
                        @Override
                        public void run() {
                            model.addRow(rowData);
                        }
                    });
//                    model.addRow(rowData);
                }

                if (hasOverlappingReads) {
                    ++operonsWithOverlapping;
                }
                if (hasInternalReads) {
                    ++operonsWithInternal;
                }

            }

            TableRowSorter<TableModel> sorter = new TableRowSorter<>();
            operonDetectionTable.setRowSorter(sorter);
            sorter.setModel(model);
//            for (int i = 0; i < model.getColumnCount(); ++i) {
//                TableComparatorProvider.setStringComparator(sorter, i);
//            }
            TableComparatorProvider.setPersistantTrackComparator(sorter, 1);

            operonDetStats.put(OPERONS_TOTAL, (Integer) operonDetStats.get(OPERONS_TOTAL) + operons.size());
            operonDetStats.put(OPERONS_WITH_OVERLAPPING_READS, (Integer) operonDetStats.get(OPERONS_WITH_OVERLAPPING_READS) + operonsWithOverlapping);
            operonDetStats.put(OPERONS_WITH_INTERNAL_READS, (Integer) operonDetStats.get(OPERONS_WITH_INTERNAL_READS) + operonsWithInternal);
            operonDetStats.put(ResultPanelTranscriptionStart.MAPPINGS_COUNT, (Double) operonDetStats.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT) + operonResultNew.getStats().getMc());
            operonDetStats.put(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH, (Double) operonDetStats.get(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH) + operonResultNew.getStats().getMml());
            operonDetStats.put(ResultPanelTranscriptionStart.MAPPINGS_MILLION, (Double) operonDetStats.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION) + operonResultNew.getStats().getMm());
            operonDetStats.put(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD, (Double) operonDetStats.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD) + operonResultNew.getStats().getBgThreshold());

            operonResult.setStatsAndParametersMap(operonDetStats);
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
     * result
     */
    public void setReferenceViewer(ReferenceViewer referenceViewer) {
        this.boundsInfoManager = referenceViewer.getBoundsInformationManager();
        this.referenceViewer = referenceViewer;
    }

    /**
     *
     * @return
     */
    private List<Operon> updateOperonResults() {
        List<Operon> operons = operonResult.getResults();
        HashMap<Integer, Operon> tmpHash = new HashMap<>();
        tmpHash.putAll(this.operonsInHash);

        for (int i = 0; i < operonDetectionTable.getRowCount(); i++) {
            Integer posTableAti = (Integer) operonDetectionTable.getValueAt(i, 0);
            if (tmpHash.containsKey(posTableAti)) {

                if ((Boolean) operonDetectionTable.getValueAt(i, 9)) {
                    this.operonsInHash.get(posTableAti).setIsConsidered(true);
                } else {
                    this.operonsInHash.get(posTableAti).setIsConsidered(false);
                }

                operonsInHash.get(posTableAti).setStartPositionOfTranscript((Integer) operonDetectionTable.getValueAt(i, 0));
                operonsInHash.get(posTableAti).setForUpstreamAnalysisMarked((Boolean) operonDetectionTable.getValueAt(i, 8));
                tmpHash.remove(posTableAti);
            }
        }

        for (Integer key : tmpHash.keySet()) {
            Operon operon = tmpHash.get(key);
            operonsInHash.remove(key);
            operons.remove(operon);
        }
        return operons;
    }
}
