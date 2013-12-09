package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickDeletion;
import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.exporter.excel.ExcelExportFileChooser;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.plots.PlotGenerator;
import de.cebitec.readXplorer.transcriptomeAnalyses.promotorAnalysis.PromotorAnalysisWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis.RbsAnalysisWizardIterator;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.tableVisualization.TableComparatorProvider;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickFilter;
import de.erichseifert.gral.data.DataTable;
import java.awt.BorderLayout;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
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
    public static final String TSS_CHOOSEN_UPSTREAM_REGION = "Upstream region relativ to TSS";
    public static final String TSS_CHOOSEN_DOWNSTREAM_REGION = "Downstream region relative to TSS";
    public static final String TSS_EXCLUSION_OF_INTERNAL_TSS = "Choosen exclusion of all internal TSS";
    public static final String TSS_RANGE_FOR_LEADERLESS_DETECTION = "Range for Leaderless detection";
    public static final String TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION = "Limitation for distance between TSS and TLS";
    public static final String TSS_LIMITATION_FOR_DISTANCE_KEEPING_INTERNAL_TSS = "Limitation for distance between internal TSS and next upstream feature TLS";
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
    private AppPanelTopComponent appPanelTopComponent;

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
                TableUtils.showPosition(tSSTable, 0, boundsInfoManager);
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

        tSSTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Position", "Direction", "Read starts", "Rel. count", "Gene name", "Gene locus", "offset", "Sequence", "Leaderless", "False positive", "Internal TSS", "Putative Antisense", "Upstream analysis", "Gene start", "Gene stop", "Gene length", "Gene Frame", "Gene product", "Start codon", "Stop codon", "Track ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false, false
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
        tSSTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title1_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title2_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title14_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_1_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title15_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title20_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title21_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title6_1_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_2_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title19_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title20_2")); // NOI18N
        tSSTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title22_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(14).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title23_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(15).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title24_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(16).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title25_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(17).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title26_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(18).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title27_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(19).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title28_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(20).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title29_1_1")); // NOI18N

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(performDeletionOfAllFP)
                .addGap(426, 426, 426)
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
                    .addComponent(exportButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

        List<TranscriptionStart> tss = this.updateTssResults();
        tssResult.setResults(tss);

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
        PlottChoicePanel plotChoice = new PlottChoicePanel();

        NotifyDescriptor nd = new NotifyDescriptor(
                plotChoice, // instance of your panel
                "Choose one or more Chart creations", // title of the dialog
                NotifyDescriptor.OK_CANCEL_OPTION, // it is Yes/No dialog ...
                NotifyDescriptor.INFORMATION_MESSAGE, // ... of a question type => a question mark icon
                null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                // otherwise specify options as:
                //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                NotifyDescriptor.OK_CANCEL_OPTION // default option is "Yes"
                );
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            PlotGenerator gen = new PlotGenerator();
            //####update tssList! #########################
            List<TranscriptionStart> tss = this.updateTssResults();
            //###################################

            if (plotChoice.isTssDistribution()) {
                DataTable data = new DataTable(Double.class, Double.class);
                // fill data!
                // We want to show the distribution of length between TSS to TLS
                for (TranscriptionStart tSS : tss) {
                    double x = tSS.getOffset();
//                    if(tSS.isLeaderless() && x == 0) {
//                        x = -tSS.getDist2start();
//                    }

                    double y = tSS.getReadStarts();
                    data.add(x, y);
                }

                gen.generateYXPlot(data, "distance between TSS and TLS", "TSS stacksize", "Distribution of TSS distance to TLS");
            }

            if (plotChoice.isAbsoluteFrequency()) {
                int bin = plotChoice.getBin();
                // ermittel den höchsten x wert, teile ihn durch den bin, schaue dann
                // in welchen bin ein x reinkommt...
                HashMap<Double, Double> freaquencyOfTSSDistances = new HashMap<>();
                double smallestValue = 0;
                double biggestValue = 0;
                for (TranscriptionStart tSS : tss) {
                    double x = tSS.getOffset();
                    if (tSS.isLeaderless() && x == 0) {
                        x = -tSS.getDist2start();
                    }

                    if (x < smallestValue) {
                        smallestValue = x;
                    }

                    if (x > biggestValue) {
                        biggestValue = x;
                    }
                }

                // Und dann??
                DataTable data = new DataTable(Double.class, Double.class);
                // fill data!
                // We want to show the distribution of length between TSS to TLS
                for (TranscriptionStart tSS : tss) {

                    double x = tSS.getOffset();
                    if (tSS.isLeaderless()) {
                        x = -tSS.getDist2start();
                    }
                    if (freaquencyOfTSSDistances.containsKey(x)) {
                        freaquencyOfTSSDistances.put(x, freaquencyOfTSSDistances.get(x) + 1.0);
                    } else {
                        freaquencyOfTSSDistances.put(x, 1.0);
                    }
                }

                for (Double key : freaquencyOfTSSDistances.keySet()) {
                    data.add(key, freaquencyOfTSSDistances.get(key));
                }

                gen.generateBarPlot(data, "distance between TSS and TLS", "Absolute frequency", "Distribution of TSS distance to TLS");
            }

            this.tssResult.setResults(tss);
        }


    }//GEN-LAST:event_startChartsOfTssDataActionPerformed

    private void performDeletionOfAllFPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performDeletionOfAllFPActionPerformed
        // delete all false positive TSS from table and TSS array
        List<TranscriptionStart> tss = this.updateTssResults();
        DefaultTableModel tableModel = (DefaultTableModel) tSSTable.getModel();
        for (int i = 0; i < tSSTable.getRowCount(); i++) {
            Integer posTableAti = (Integer) tSSTable.getValueAt(i, 0);
            TranscriptionStart ts = tssInHash.get(posTableAti);
            boolean isFalsePositive = (boolean) tSSTable.getValueAt(i, 9);
            if (isFalsePositive) {
                tssInHash.remove(posTableAti);
                tableModel.removeRow(i);
                tss.remove(ts);
            }
        }

        tSSTable.setModel(tableModel);
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

            boolean takeAllElements = (boolean) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ALL_ELEMENTS);
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_LEADERLESS);
            boolean takeOnlyAntisense = (boolean) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_ANTISENSE);
            boolean takeOnlyNonLeaderless = (boolean) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_NON_LEADERLESS);
            boolean takeOnlyRealTss = (boolean) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_REAL_TSS);
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ONLY_SELECTED);

            int minus10MotifWidth = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS10_MOTIF_LENGTH);
            int minus35MotifWidth = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS35_MOTIF_LENGTH);
            int noOfTimes = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_NUMBER_OF_TRYING);
            int minSpacer1 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER1_LENGTH);
            int minSpacer2 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER2_LENGTH);
            int seqWidthMinus10 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_10_REGION);
            int seqWidthMinus35 = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_35_REGION);
            final int lengthRelToTss = (Integer) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS);

            File workingDir = (File) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_WORKING_DIR);

            final PromotorSearchParameters params = new PromotorSearchParameters(
                    minus10MotifWidth, minus35MotifWidth,
                    noOfTimes, minSpacer1, minSpacer2,
                    seqWidthMinus10, seqWidthMinus35, lengthRelToTss, workingDir);


            final List<TranscriptionStart> starts = updateTssResults();
            if (takeAllElements) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("Promotor motif search for all elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromAllElementsIntoAccount(starts, lengthRelToTss, false);
                        model.utrPromotorAnalysis(params);
                        appPanelTopComponent.add(model.getPromotorMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();

            }

            if (takeOnlyAntisense) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("Promotor motif search for all antisense elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromAntisenseElementsIntoAccount(starts, lengthRelToTss, false);
                        model.utrPromotorAnalysis(params);
                        appPanelTopComponent.add(model.getPromotorMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            if (takeOnlyLeaderless) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("Promotor motif search for all leaderless elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromLeaderlessElementsIntoAccount(starts, lengthRelToTss, false);
                        model.utrPromotorAnalysis(params);
                        appPanelTopComponent.add(model.getPromotorMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            if (takeOnlyNonLeaderless) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("Promotor motif search for all non/leaderless elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromNonLeaderlessElementsIntoAccount(starts, lengthRelToTss, false);
                        model.utrPromotorAnalysis(params);
                        appPanelTopComponent.add(model.getPromotorMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            if (takeOnlyRealTss) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("Promotor motif search for all real-TSS elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromRealTssElementsIntoAccount(starts, lengthRelToTss, false);
                        model.utrPromotorAnalysis(params);
                        appPanelTopComponent.add(model.getPromotorMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            if (takeOnlySelectedElements) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("Promotor motif search for all selected upstream-analysis elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);

                        HashMap<Integer, TranscriptionStart> tmpHash = new HashMap<>();
                        tmpHash.putAll(tssInHash);
                        List<TranscriptionStart> tssForAnalysis = new ArrayList<>();

                        for (int i = 0; i < tSSTable.getRowCount(); i++) {
                            Integer posTableAti = (Integer) tSSTable.getValueAt(i, 0);
                            boolean isSelected = (boolean) tSSTable.getValueAt(i, 12);
                            if (tmpHash.containsKey(posTableAti) && !isSelected) {
                                tmpHash.remove(posTableAti);
                            }
                        }
                        for (Integer key : tmpHash.keySet()) {
                            TranscriptionStart ts = tmpHash.get(key);
                            tssForAnalysis.add(ts);
                        }

                        model.takeUpstreamRegionsFromSelectedElements(tssForAnalysis, lengthRelToTss, false);
                        model.utrPromotorAnalysis(params);
                        appPanelTopComponent.add(model.getPromotorMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            this.tssResult.setResults(starts);
        }
    }//GEN-LAST:event_performPromotorAnalysisActionPerformed

    private void performRbsAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performRbsAnalysisActionPerformed
        final List<TranscriptionStart> currentTss = updateTssResults();
        RbsAnalysisWizardIterator wizard = new RbsAnalysisWizardIterator(this.referenceViewer, currentTss);
        WizardDescriptor wiz = new WizardDescriptor(wizard);
        wizard.setWiz(wiz);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(ResultPanelTranscriptionStart.class, "TTL_MotifSearchWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {



            boolean takeAllElements = (boolean) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_ANALYSIS_ALL_ELEMENTS);
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_ANALYSIS_ONLY_LEADERLESS);
            boolean takeOnlyAntisense = (boolean) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_ANALYSIS_ONLY_ANTISENSE);
            boolean takeOnlyNonLeaderless = (boolean) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_ANALYSIS_ONLY_NON_LEADERLESS);
            boolean takeOnlyRealTss = (boolean) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_ANALYSIS_REAL_TSS);
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_ANALYSIS_ONLY_SELECTED);

            File workingDir = (File) wiz.getProperty(RbsAnalysisWizardIterator.PROP_WORKING_DIR);
            final int lengthRelToTls = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_REGION_LENGTH);
            int motifWidth = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH);
            int noOfTrying = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR);
            int minSpacer = (int) wiz.getProperty(RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER);

            final RbsAnalysisParameters params = new RbsAnalysisParameters(workingDir, lengthRelToTls, motifWidth, noOfTrying, minSpacer);


            if (takeAllElements) {

                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("RBS motif search for all elements in Table");

                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromAllElementsIntoAccount(currentTss, lengthRelToTls, true);
                        model.rbsMotifAnalysis(params);
                        appPanelTopComponent.add(model.getRbsMotifSearchPanel(), BorderLayout.CENTER);

                    }
                });
                promotorSearch.start();

            }

            if (takeOnlyAntisense) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("RBS motif search for all antisense elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromAntisenseElementsIntoAccount(currentTss, lengthRelToTls, true);
                        model.rbsMotifAnalysis(params);
                        appPanelTopComponent.add(model.getRbsMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            if (takeOnlyLeaderless) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("RBS motif search for all leaderless elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromLeaderlessElementsIntoAccount(currentTss, lengthRelToTls, true);
                        model.rbsMotifAnalysis(params);
                        appPanelTopComponent.add(model.getRbsMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();

            }

            if (takeOnlyNonLeaderless) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("RBS motif search for all non-leaderless elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromNonLeaderlessElementsIntoAccount(currentTss, lengthRelToTls, true);
                        model.rbsMotifAnalysis(params);
                        appPanelTopComponent.add(model.getRbsMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            if (takeOnlyRealTss) {
                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("RBS motif search for real TSS elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model = new MotifSearchModel(referenceViewer);
                        model.takeUpstreamRegionsFromRealTssElementsIntoAccount(currentTss, lengthRelToTls, true);
                        model.rbsMotifAnalysis(params);
                        appPanelTopComponent.add(model.getRbsMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }

            if (takeOnlySelectedElements) {

                appPanelTopComponent = new AppPanelTopComponent();
                appPanelTopComponent.setLayout(new BorderLayout());
                appPanelTopComponent.open();
                appPanelTopComponent.setName("RBS motif search for upstream analysis selected elements in Table");
                Thread promotorSearch = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        model = new MotifSearchModel(referenceViewer);
                        HashMap<Integer, TranscriptionStart> tmpHash = new HashMap<>();
                        tmpHash.putAll(tssInHash);
                        List<TranscriptionStart> tssForAnalysis = new ArrayList<>();

                        for (int i = 0; i < tSSTable.getRowCount(); i++) {
                            Integer posTableAti = (Integer) tSSTable.getValueAt(i, 0);
                            boolean isSelected = (boolean) tSSTable.getValueAt(i, 12);
                            if (tmpHash.containsKey(posTableAti) && !isSelected) {
                                tmpHash.remove(posTableAti);
                            }
                        }
                        for (Integer key : tmpHash.keySet()) {
                            TranscriptionStart ts = tmpHash.get(key);
                            tssForAnalysis.add(ts);
                        }
                        model.takeUpstreamRegionsFromSelectedElements(tssForAnalysis, lengthRelToTls, false);
                        model.rbsMotifAnalysis(params);
                        appPanelTopComponent.add(model.getRbsMotifSearchPanel(), BorderLayout.CENTER);
                    }
                });
                promotorSearch.start();
            }
        }
        this.tssResult.setResults(currentTss);
    }//GEN-LAST:event_performRbsAnalysisActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JButton performDeletionOfAllFP;
    private javax.swing.JButton performPromotorAnalysis;
    private javax.swing.JButton performRbsAnalysis;
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
    public int getResultSize() {
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

            final int nbColumns = 21;

            int noCorrectStarts = 0;
            int noFwdFeatures = 0;
            int noRevFeatures = 0;
            int noLeaderlessFeatures = 0;
            int noInternalTSS = 0;
            int noPutativeAntisense = 0;


            final DefaultTableModel model = (DefaultTableModel) tSSTable.getModel();

            String strand;
            PersistantFeature feature;
            PersistantFeature nextGene;

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
                rowData[0] = position;
                rowData[1] = strand;
                rowData[2] = tSS.getReadStarts();
                rowData[3] = tSS.getRelCount();

                feature = tSS.getDetectedGene();
                nextGene = tSS.getNextGene();

                if (feature != null) {
                    rowData[4] = feature.toString();
                    rowData[5] = feature.getLocus();
                    rowData[6] = tSS.getOffset();
                    ++noCorrectStarts;
                } else {
                    rowData[4] = nextGene.toString();
                    rowData[5] = nextGene.getLocus();
                    rowData[6] = tSS.getNextOffset();
                }

                rowData[7] = tSS.getSequence();

                rowData[8] = leaderless;
                rowData[9] = false;
                rowData[10] = tSS.isInternalTSS();
                if (tSS.isInternalTSS()) {
                    noInternalTSS++;
                }

                rowData[11] = tSS.isPutativeAntisense();
                if (tSS.isPutativeAntisense()) {
                    noPutativeAntisense++;
                }

                rowData[12] = false;
                // additionally informations about detected gene
                if (feature != null) {
                    rowData[13] = feature.isFwdStrand() ? feature.getStart() : feature.getStop();
                    rowData[14] = feature.isFwdStrand() ? feature.getStop() : feature.getStart();
                    rowData[15] = feature.getStop() - feature.getStart();
                    int start = feature.getStart();
                    if ((start % 3) == 0) {
                        rowData[16] = 3;
                    } else if (start % 3 == 1) {
                        rowData[16] = 1;
                    } else if (start % 3 == 2) {
                        rowData[16] = 2;
                    }
                    rowData[17] = feature.getProduct();
                } else {
                    rowData[13] = nextGene.isFwdStrand() ? nextGene.getStart() : nextGene.getStop();
                    rowData[14] = nextGene.isFwdStrand() ? nextGene.getStop() : nextGene.getStart();
                    rowData[15] = nextGene.getStop() - nextGene.getStart();
                    int start = nextGene.getStart();
                    if ((start % 3) == 0) {
                        rowData[16] = 2;
                    } else if (start % 3 == 1) {
                        rowData[16] = 1;
                    } else if (start % 3 == 2) {
                        rowData[16] = 3;
                    }
                    rowData[17] = nextGene.getProduct();
                }

                rowData[18] = tSS.getDetectedFeatStart();
                rowData[19] = tSS.getDetectedFeatStop();
                rowData[20] = tSS.getTrackId();

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
            statisticsMap.put(MAPPINGS_MEAN_LENGTH, (Double) statisticsMap.get(MAPPINGS_MEAN_LENGTH) + tssResultNew.getStats().getMc());
            statisticsMap.put(MAPPINGS_MILLION, (Double) statisticsMap.get(MAPPINGS_MILLION) + tssResultNew.getStats().getMc());
            statisticsMap.put(BACKGROUND_THRESHOLD, (Double) statisticsMap.get(BACKGROUND_THRESHOLD) + tssResultNew.getStats().getMc());

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
