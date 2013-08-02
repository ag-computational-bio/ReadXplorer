/*
 * ResultPanelTranscriptionStart.java
 *
 * Created on 27.01.2012, 14:31:03
 */
package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.exporter.excel.ExcelExportFileChooser;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.DetectedFeatures;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TransStartUnannotated;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TranscriptionStart;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.util.TableRightClickFilter;
import de.cebitec.vamp.util.UneditableTableModel;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.tableVisualization.TableComparatorProvider;
import de.cebitec.vamp.view.tableVisualization.TableUtils;
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

/**
 * This panel is capable of showing a table with transcription start sites and
 * contains an export button, which exports the data into an excel file.
 *
 * @author -Rolf Hilker-
 */
public class ResultPanelTranscriptionStart extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 1L;

    public static final String TSS_TOTAL = "Total number of detected TSSs";
    public static final String TSS_CORRECT = "Correct TSS";
    public static final String TSS_UPSTREAM = "TSS with upstream feature";
    public static final String TSS_DOWNSTREAM = "TSS with downstream feature";
    public static final String TSS_FWD = "TSS on fwd strand";
    public static final String TSS_REV = "TSS on rev strand";
    public static final String TSS_NOVEL = "Novel Transcripts";
    public static final int UNUSED_STATISTICS_VALUE = -1;
    
    private BoundsInfoManager boundsInfoManager;
    private List<String> promotorRegions;
    private ReferenceViewer referenceViewer;
    private TssDetectionResult tssResult;
    private HashMap<String, Integer> statisticsMap;
    private TableRightClickFilter<UneditableTableModel> tableFilter = new TableRightClickFilter<>(UneditableTableModel.class);
    
    
    /**
     * This panel is capable of showing a table with transcription start sites
     * and contains an export button, which exports the data into an excel file.
     */
    public ResultPanelTranscriptionStart() {
        this.initComponents();
        this.tSSTable.getTableHeader().addMouseListener(tableFilter);
        this.initStatsMap();
       
        DefaultListSelectionModel model = (DefaultListSelectionModel) this.tSSTable.getSelectionModel();
        model.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                TableUtils.showPosition(tSSTable, 0, boundsInfoManager);
            }
        });
    }

    /**
     * Initializes the statistics map.
     */
    private void initStatsMap() {
        statisticsMap = new HashMap<>();
        statisticsMap.put(TSS_TOTAL, 0);
        statisticsMap.put(TSS_CORRECT, 0);
        statisticsMap.put(TSS_UPSTREAM, 0);
        statisticsMap.put(TSS_DOWNSTREAM, 0);
        statisticsMap.put(TSS_FWD, 0);
        statisticsMap.put(TSS_REV, 0);
        statisticsMap.put(TSS_NOVEL, 0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tssScrollPane = new javax.swing.JScrollPane();
        tSSTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        parametersLabel = new javax.swing.JLabel();
        statisticsButton = new javax.swing.JButton();

        tSSTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Position", "Track", "Strand", "Initial Coverage", "Coverage Increase", "Coverage Increase %", "Correct Feature", "Next Upstream Feature", "Next Downstream Feature", "Unannotated Transcript", "Transcript Stop"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tssScrollPane.setViewportView(tSSTable);
        tSSTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title0")); // NOI18N
        tSSTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11")); // NOI18N
        tSSTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title1")); // NOI18N
        tSSTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title2")); // NOI18N
        tSSTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title4")); // NOI18N
        tSSTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title5")); // NOI18N
        tSSTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title6")); // NOI18N
        tSSTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title7")); // NOI18N
        tSSTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title8")); // NOI18N
        tSSTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title9")); // NOI18N
        tSSTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title10")); // NOI18N

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.exportButton.text")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.parametersLabel.text")); // NOI18N

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.statisticsButton.text")); // NOI18N
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(parametersLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 664, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportButton))
            .addComponent(tssScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tssScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(parametersLabel)
                    .addComponent(statisticsButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        
        this.promotorRegions = new ArrayList<>();
        
        //get reference sequence for promotor regions
        PersistantReference ref = this.referenceViewer.getReference();
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(ref.getId());
        String sequence = refConnector.getRefGenome().getSequence();
        String promotor;
        
        //get the promotor region for each TSS
        int promotorStart;
        int refSeqLength = ref.getSequence().length();
        for (TranscriptionStart tSS : this.tssResult.getResults()) {
            if (tSS.isFwdStrand()) {
                promotorStart = tSS.getPos() - 70;
                promotorStart = promotorStart < 0 ? 0 : promotorStart;
                promotor = sequence.substring(promotorStart, tSS.getPos());
            } else {
                promotorStart = tSS.getPos() + 70;
                promotorStart = promotorStart > refSeqLength ? refSeqLength : promotorStart;
                promotor = SequenceUtils.getReverseComplement(sequence.substring(tSS.getPos(), promotorStart));
            }
            this.promotorRegions.add(promotor);
        }
        tssResult.setPromotorRegions(promotorRegions);
        
        ExcelExportFileChooser fileChooser = new ExcelExportFileChooser(new String[]{"xls"}, "xls", tssResult); 
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog(this, new TssDetectionStatsPanel(statisticsMap), "TSS Detection Statistics", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_statisticsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JButton statisticsButton;
    private javax.swing.JTable tSSTable;
    private javax.swing.JScrollPane tssScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Adds a list of transcription start site objects to this panel's table.
     * @param tssResultNew transcription start sites detection result to add
     */
    public void addTSSs(final TssDetectionResult tssResultNew) {

        final List<TranscriptionStart> tsss = new ArrayList<>(tssResultNew.getResults());

        if (tssResult == null) {
            tssResult = tssResultNew;
        } else {
            tssResult.getResults().addAll(tssResultNew.getResults());
        }
        SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
            @Override
            public void run() {

                final int nbColumns = 11;

                int noCorrectStarts = 0;
                int noUpstreamFeature = 0;
                int noDownstreamFeature = 0;
                int noFwdFeatures = 0;
                int noRevFeatures = 0;
                int noUnannotatedTranscripts = 0;

                DefaultTableModel model = (DefaultTableModel) tSSTable.getModel();
                String strand;
                DetectedFeatures detFeatures;
                PersistantFeature feature;
                TransStartUnannotated tSSU;

                for (TranscriptionStart tSS : tsss) {

                    if (tSS.isFwdStrand()) {
                        strand = SequenceUtils.STRAND_FWD_STRING;
                        ++noFwdFeatures;
                    } else {
                        strand = SequenceUtils.STRAND_REV_STRING;
                        ++noRevFeatures;
                    }

                    Object[] rowData = new Object[nbColumns];
                    rowData[0] = tSS.getPos();
                    rowData[1] = tssResultNew.getTrackMap().get(tSS.getTrackId());
                    rowData[2] = strand;
                    rowData[3] = tSS.getReadStartsAtPos();
                    rowData[4] = tSS.getCoverageIncrease();
                    rowData[5] = tSS.getPercentIncrease();

                    detFeatures = tSS.getDetFeatures();
                    feature = detFeatures.getCorrectStartFeature();
                    if (feature != null) {
                        rowData[6] = feature.toString();
                        ++noCorrectStarts;
                    } else {
                        rowData[6] = "-";
                    }
                    feature = detFeatures.getUpstreamFeature();
                    if (feature != null) {
                        rowData[7] = feature.toString();
                        ++noUpstreamFeature;
                    } else {
                        rowData[7] = "-";
                    }
                    feature = detFeatures.getDownstreamFeature();
                    if (feature != null) {
                        rowData[8] = feature.toString();
                        ++noDownstreamFeature;
                    } else {
                        rowData[8] = "-";
                    }

                    if (tSS instanceof TransStartUnannotated) {
                        tSSU = (TransStartUnannotated) tSS;
                        rowData[9] = true;
                        rowData[10] = tSSU.getDetectedStop();
                        ++noUnannotatedTranscripts;
                    } else {
                    }
                    model.addRow(rowData);
                }

                //create statistics

                ParameterSetTSS tssParameters = (ParameterSetTSS) tssResult.getParameters();
                statisticsMap.put(TSS_TOTAL, statisticsMap.get(TSS_TOTAL) + tsss.size());
                statisticsMap.put(TSS_CORRECT, statisticsMap.get(TSS_CORRECT) + noCorrectStarts);
                statisticsMap.put(TSS_UPSTREAM, statisticsMap.get(TSS_UPSTREAM) + noUpstreamFeature);
                statisticsMap.put(TSS_DOWNSTREAM, statisticsMap.get(TSS_DOWNSTREAM) + noDownstreamFeature);
                statisticsMap.put(TSS_FWD, statisticsMap.get(TSS_FWD) + noFwdFeatures);
                statisticsMap.put(TSS_REV, statisticsMap.get(TSS_REV) + noRevFeatures);
                if (tssParameters.isPerformUnannotatedTranscriptDet()) {
                    statisticsMap.put(TSS_NOVEL, statisticsMap.get(TSS_NOVEL) + noUnannotatedTranscripts);
                } else {
                    statisticsMap.put(TSS_NOVEL, ResultPanelTranscriptionStart.UNUSED_STATISTICS_VALUE);
                }
                tssResultNew.setStatsMap(statisticsMap);

                TableRowSorter<TableModel> sorter = new TableRowSorter<>();
                tSSTable.setRowSorter(sorter);
                sorter.setModel(model);
                TableComparatorProvider.setPersistantTrackComparator(sorter, 1);

                String unannotatedTranscriptDet = tssParameters.isPerformUnannotatedTranscriptDet() ? "yes" : "no";
                parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class,
                        "ResultPanelTranscriptionStart.parametersLabel.text", tssParameters.getMinNoReadStarts(), tssParameters.getMinPercentIncrease(),
                        tssParameters.getMaxLowCovReadStarts(), tssParameters.getMinLowCovReadStarts(), unannotatedTranscriptDet,
                        tssParameters.getMinTranscriptExtensionCov()));

            }
        });
    }
    
    /**
     * Set the reference viewer needed for updating the currently shown position
     * and extracting the reference sequence.
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
    public int getResultSize() {
        return this.tssResult.getResults().size();
    }
}
