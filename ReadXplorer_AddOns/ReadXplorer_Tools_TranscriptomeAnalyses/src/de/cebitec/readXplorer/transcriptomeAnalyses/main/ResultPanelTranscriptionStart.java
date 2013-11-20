package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickDeletion;
import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.exporter.excel.ExcelExportFileChooser;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.tableVisualization.TableComparatorProvider;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickFilter;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;
import java.awt.BorderLayout;
import java.awt.Color;
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

/**
 * This panel is capable of showing a table with transcription start sites and
 * contains an export button, which exports the data into an excel file.
 * Additionally it has now an import button, which imports an excel file and
 * visualize the data to a JPanel.
 *
 * @author -Rolf Hilker-, -jritter-
 */
public class ResultPanelTranscriptionStart extends ResultTablePanel {

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
    public static final String TSS_INTERNAL = "internal TSS";
    public static final int UNUSED_STATISTICS_VALUE = -1;
    private BoundsInfoManager boundsInfoManager;
    private ReferenceViewer referenceViewer;
    private TSSDetectionResults tssResult;
    private HashMap<String, Integer> statisticsMap;
    private TableRightClickFilter<UneditableTableModel> tableFilter = new TableRightClickFilter<>(UneditableTableModel.class);
    private TableRightClickDeletion<DefaultTableModel> rowDeletion = new TableRightClickDeletion();
    private MotifSearchPanel motifSearch;
    private AppPanelTopComponent appPanelTopComponent;
    private List<String> promotorList;
    private int up, down;
    private HashMap<Integer, TranscriptionStart> tssInHash;
    

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
        importButton = new javax.swing.JButton();
        thresholdLabel = new javax.swing.JLabel();
        mappingsPerMillionLabel = new javax.swing.JLabel();
        mappingMeanLengthLabel = new javax.swing.JLabel();
        mappingCoverageLabel = new javax.swing.JLabel();
        startBioProspectorButton = new javax.swing.JButton();
        startChartsOfTssData = new javax.swing.JButton();

        tSSTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Position", "Direction", "Read starts", "Rel. count", "Gene", "Gene locus", "offset", "Sequence", "Leaderless", "False positive?", "Internal TSS", "gene start", "gene stop", "length in bp", "Frame", "gene product", "start codon sequence", "stop codon sequence", "track IDl"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false
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
        tSSTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title0")); // NOI18N
        tSSTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11")); // NOI18N
        tSSTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title1")); // NOI18N
        tSSTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title2")); // NOI18N
        tSSTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title14")); // NOI18N
        tSSTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title15")); // NOI18N
        tSSTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title20")); // NOI18N
        tSSTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title21")); // NOI18N
        tSSTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title6_1")); // NOI18N
        tSSTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18_2")); // NOI18N
        tSSTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title22")); // NOI18N
        tSSTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title23")); // NOI18N
        tSSTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title24")); // NOI18N
        tSSTable.getColumnModel().getColumn(14).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title25")); // NOI18N
        tSSTable.getColumnModel().getColumn(15).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title26")); // NOI18N
        tSSTable.getColumnModel().getColumn(16).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title27")); // NOI18N
        tSSTable.getColumnModel().getColumn(17).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title28")); // NOI18N
        tSSTable.getColumnModel().getColumn(18).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title29_1")); // NOI18N

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.exportButton.text")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.statisticsButton.text")); // NOI18N
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsButtonActionPerformed(evt);
            }
        });

        importButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.importButton.text")); // NOI18N
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        thresholdLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.thresholdLabel.text")); // NOI18N

        mappingsPerMillionLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.mappingsPerMillionLabel.text")); // NOI18N

        mappingMeanLengthLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.mappingMeanLengthLabel.text")); // NOI18N

        mappingCoverageLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.mappingCoverageLabel.text")); // NOI18N

        startBioProspectorButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.startBioProspectorButton.text")); // NOI18N
        startBioProspectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBioProspectorButtonActionPerformed(evt);
            }
        });

        startChartsOfTssData.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.startChartsOfTssData.text")); // NOI18N
        startChartsOfTssData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startChartsOfTssDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(thresholdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(mappingsPerMillionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(mappingCoverageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mappingMeanLengthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(startChartsOfTssData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(startBioProspectorButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importButton, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(exportButton))
            .addComponent(tssScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tssScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(thresholdLabel)
                        .addComponent(mappingsPerMillionLabel)
                        .addComponent(mappingCoverageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(mappingMeanLengthLabel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(importButton)
                        .addComponent(statisticsButton)
                        .addComponent(startBioProspectorButton)
                        .addComponent(startChartsOfTssData))
                    .addComponent(exportButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

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
        tssResult.setResults(tss);

        ExcelExportFileChooser fileChooser = new ExcelExportFileChooser(new String[]{"xls"}, "xls", tssResult);
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog(this, new TssDetectionStatsPanel(statisticsMap), "TSS Detection Statistics", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_statisticsButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_importButtonActionPerformed

    private void tSSTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tSSTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tSSTableMouseClicked

    private void startBioProspectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBioProspectorButtonActionPerformed

        PromotorSelectionPanel selection = new PromotorSelectionPanel();

        NotifyDescriptor nd = new NotifyDescriptor(
                selection, // instance of your panel
                "Which Promotors do you like to analyze?", // title of the dialog
                NotifyDescriptor.OK_CANCEL_OPTION, // it is Yes/No dialog ...
                NotifyDescriptor.QUESTION_MESSAGE, // ... of a question type => a question mark icon
                null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                // otherwise specify options as:
                //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                NotifyDescriptor.OK_CANCEL_OPTION // default option is "Yes"
                );
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {

            boolean isLeaderlessPromotor = selection.isLeaderLessSelected();
            this.appPanelTopComponent = new AppPanelTopComponent();
            this.appPanelTopComponent.setLayout(new BorderLayout());
            List<TranscriptionStart> tss = tssResult.getResults();
            this.promotorList = new ArrayList<>();
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

            for (int i = 0; i < tSSTable.getRowCount(); i++) {
//            Wenn die Position in tss nicht mit der Pos an der stelle in der Tabelle 
//            übereinstimmt, dann soll der tss aus der tssList raus!
                Integer posTableAti = (Integer) tSSTable.getValueAt(i, 0);
                TranscriptionStart ts = tssInHash.get(posTableAti);

                PersistantFeature feature = ts.getDetectedGene();
                PersistantFeature nextFeature = ts.getNextGene();
                boolean leaderless = ts.isLeaderless();
                boolean isFWD;
                if (isLeaderlessPromotor == true) {
                    if (leaderless == true) {
                        if (feature != null) {
                            promotorList.add(">" + feature.toString() + "\n");
                            isFWD = feature.isFwdStrand();
                        } else {
                            promotorList.add(">" + nextFeature.toString() + "\n");
                            isFWD = nextFeature.isFwdStrand();
                        }

                        String promotor = ts.getSequence();
                        if (isFWD) {
                            promotorList.add(promotor + "\n");
                        } else {
                            String reversedSeq = new StringBuffer(promotor).reverse().toString();
                            promotorList.add(reversedSeq + "\n");
                        }

                    }
                } else {
                    if (leaderless == false) {
                        if (feature != null) {
                            promotorList.add(">" + feature.toString() + "\n");
                            isFWD = feature.isFwdStrand();
                        } else {
                            promotorList.add(">" + nextFeature.toString() + "\n");
                            isFWD = nextFeature.isFwdStrand();
                        }

                        String promotor = ts.getSequence();
                        if (isFWD) {
                            promotorList.add(promotor + "\n");
                        } else {
                            String reversedSeq = new StringBuffer(promotor).reverse().toString();
                            promotorList.add(reversedSeq + "\n");
                        }
                    }
                }

            }
            motifSearch = new MotifSearchPanel(this.promotorList, this.getDown());
            motifSearch.writePromotorsInTextPane();
            this.appPanelTopComponent.add(motifSearch, BorderLayout.CENTER);
            this.appPanelTopComponent.open();
            this.appPanelTopComponent.setName("Motif search");
            this.tssResult.setResults(tss);
        }

    }//GEN-LAST:event_startBioProspectorButtonActionPerformed

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

            //####update tssList! #########################
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
            //###################################

            if (plotChoice.isTssDistribution()) {
                this.appPanelTopComponent = new AppPanelTopComponent();
                this.appPanelTopComponent.setLayout(new BorderLayout());
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

                XYPlot plot = new XYPlot(data);
                double insetsTop = 20.0,
                        insetsLeft = 100.0,
                        insetsBottom = 60.0,
                        insetsRight = 40.0;
                plot.setInsets(new Insets2D.Double(
                        insetsTop, insetsLeft, insetsBottom, insetsRight));
                plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, "distance between TSS and TLS");
                plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, "TSS stacksize");
                plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, 0.0);
                plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, 0.0);


                Color color = new Color(0.0f, 0.3f, 1.0f);
                plot.getPointRenderer(data).setSetting(PointRenderer.COLOR, color);
                this.appPanelTopComponent.add(new InteractivePanel(plot), BorderLayout.CENTER);
                this.appPanelTopComponent.open();
                this.appPanelTopComponent.setName("Distribution of TSS distance to TLS");
            }
            
            if(plotChoice.isAbsoluteFrequency()) {
                this.appPanelTopComponent = new AppPanelTopComponent();
                this.appPanelTopComponent.setLayout(new BorderLayout());
                HashMap<Double, Double> freaquencyOfTSSDistances = new HashMap<>();
                
                DataTable data = new DataTable(Double.class, Double.class);
                // fill data!
                // We want to show the distribution of length between TSS to TLS
                for (TranscriptionStart tSS : tss) {
                    double x = tSS.getOffset();
                    double y = 0;
                    if(freaquencyOfTSSDistances.containsKey(x)) {
                        freaquencyOfTSSDistances.put(x, freaquencyOfTSSDistances.get(x)+1.0);
                    }
                    else {
                        freaquencyOfTSSDistances.put(x, 1.0);
                    }
                }

                for (Double key : freaquencyOfTSSDistances.keySet()) {
                    data.add(key, freaquencyOfTSSDistances.get(key));
                }
                
                BarPlot plot = new BarPlot(data);
//                plot.setBounds(5000, 5000, 500, 500);
//                LogarithmicRenderer2D rendererX = new LogarithmicRenderer2D();
//                LogarithmicRenderer2D rendererY = new LogarithmicRenderer2D();
//                plot.setAxisRenderer(BarPlot.AXIS_X, rendererX);
//                plot.setAxisRenderer(BarPlot.AXIS_Y, rendererY);
                double insetsTop = 20.0,
                        insetsLeft = 100.0,
                        insetsBottom = 60.0,
                        insetsRight = 40.0;
                plot.setInsets(new Insets2D.Double(
                        insetsTop, insetsLeft, insetsBottom, insetsRight));
                plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(LogarithmicRenderer2D.LABEL, "distance between TSS and TLS");
                plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(LogarithmicRenderer2D.LABEL, "Absolute frequency");
                plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(LogarithmicRenderer2D.INTERSECTION, 0.0);
                plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(LogarithmicRenderer2D.INTERSECTION, 0.0);


                Color color = new Color(0.0f, 0.3f, 1.0f);
                plot.getPointRenderer(data).setSetting(PointRenderer.COLOR, color);
                this.appPanelTopComponent.add(new InteractivePanel(plot), BorderLayout.CENTER);
                this.appPanelTopComponent.open();
                this.appPanelTopComponent.setName("Distribution of TSS distance to TLS");
            }
            
            this.tssResult.setResults(tss);
        }


    }//GEN-LAST:event_startChartsOfTssDataActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel mappingCoverageLabel;
    private javax.swing.JLabel mappingMeanLengthLabel;
    private javax.swing.JLabel mappingsPerMillionLabel;
    private javax.swing.JButton startBioProspectorButton;
    private javax.swing.JButton startChartsOfTssData;
    private javax.swing.JButton statisticsButton;
    private javax.swing.JTable tSSTable;
    private javax.swing.JLabel thresholdLabel;
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

            final int nbColumns = 19;

            int noCorrectStarts = 0;
            int noFwdFeatures = 0;
            int noRevFeatures = 0;
            int noLeaderlessFeatures = 0;
            int noInternalTSS = 0;


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
                int position = tSS.getPos();
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
                if(tSS.isInternalTSS()) {
                    noInternalTSS ++;
                }

                // additionally informations about detected gene
                if (feature != null) {
                    rowData[11] = feature.isFwdStrand() ? feature.getStart() : feature.getStop();
                    rowData[12] = feature.isFwdStrand() ? feature.getStop() : feature.getStart();
                    rowData[13] = feature.getStop() - feature.getStart();
                    int start = feature.getStart();
                    if((start % 3) == 0 ) {
                        rowData[14] = 3;
                    } else if (start % 3 == 1) {
                        rowData[14] = 1;
                    } else if (start % 3 == 2) {
                        rowData[14] = 2;
                    }
                    rowData[15] = feature.getProduct();
                } else {
                    rowData[11] = nextGene.isFwdStrand() ? nextGene.getStart() : nextGene.getStop();
                    rowData[12] = nextGene.isFwdStrand() ? nextGene.getStop() : nextGene.getStart();
                    rowData[13] = nextGene.getStop() - nextGene.getStart();
                    int start = nextGene.getStart();
                    if((start % 3) == 0 ) {
                        rowData[14] = 2;
                    } else if (start % 3 == 1) {
                        rowData[14] = 1;
                    } else if (start % 3 == 2) {
                        rowData[14] = 3;
                    }
                    rowData[15] = nextGene.getProduct();
                }

                rowData[16] = tSS.getDetectedFeatStart();
                rowData[17] = tSS.getDetectedFeatStop();
                rowData[18] = tSS.getTrackId();

                SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
                    @Override
                    public void run() {
                        model.addRow(rowData);
                    }
                });
            }

            //create statistics
            ParameterSetFiveEnrichedAnalyses tssParameters = (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters();
            statisticsMap.put(TSS_TOTAL, statisticsMap.get(TSS_TOTAL) + tsss.size());
            statisticsMap.put(TSS_CORRECT, statisticsMap.get(TSS_CORRECT) + noCorrectStarts);
            statisticsMap.put(TSS_FWD, statisticsMap.get(TSS_FWD) + noFwdFeatures);
            statisticsMap.put(TSS_REV, statisticsMap.get(TSS_REV) + noRevFeatures);
            statisticsMap.put(TSS_LEADERLESS, statisticsMap.get(TSS_LEADERLESS) + noLeaderlessFeatures);
            statisticsMap.put(TSS_INTERNAL, statisticsMap.get(TSS_INTERNAL) + noInternalTSS);
//                    statisticsMap.put(MAPPINGS_COUNT, (Integer) (statisticsMap.get(MAPPINGS_COUNT) + tssResultNew.getStats().getMc()));

            tssResultNew.setStatsMap(statisticsMap);

            TableRowSorter<TableModel> sorter = new TableRowSorter<>();
            tSSTable.setRowSorter(sorter);
            sorter.setModel(model);
            TableComparatorProvider.setPersistantTrackComparator(sorter, 1);

            setStatisticLabels(tssResult.getStats());

            setUp(tssParameters.getUpstreamRegion());
            setDown(tssParameters.getDownstreamRegion());
        }
    }

    /**
     *
     * @param statistics
     */
    private void setStatisticLabels(final Statistics statistics) {
        this.mappingsPerMillionLabel.setText("Mappins per Million Reads: " + statistics.getMm());
        String bg = "" + statistics.getBg();
        if (bg.length() > 6) {
            String bgCutted = bg.substring(0, 5);
            this.thresholdLabel.setText("Background Threshold: " + bgCutted);
        } else {
            this.thresholdLabel.setText("Background Threshold: " + bg);
        }
        this.mappingMeanLengthLabel.setText("Mean mapping length: " + statistics.getMml());
        this.mappingCoverageLabel.setText("Mappins coverage: " + statistics.getMc());
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

    /**
     * Return the number of upstream bases before transcription start site
     * position.
     *
     * @return Number of bases before transcription start site.
     */
    public int getUp() {
        return up;
    }

    /**
     *
     * @param up
     */
    public void setUp(int up) {
        this.up = up;
    }

    /**
     * Return the number of downstream bases after transcription start site
     * position.
     *
     * @return Number of bases after transcription start site.
     */
    public int getDown() {
        return down;
    }

    public void setDown(int down) {
        this.down = down;
    }
}
