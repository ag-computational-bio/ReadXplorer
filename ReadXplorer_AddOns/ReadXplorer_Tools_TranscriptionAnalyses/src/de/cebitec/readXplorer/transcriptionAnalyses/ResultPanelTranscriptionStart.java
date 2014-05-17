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
package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.DetectedFeatures;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.TransStartUnannotated;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.TranscriptionStart;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.tableVisualization.TableComparatorProvider;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickFilter;
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
import org.openide.util.NbBundle;

/**
 * This panel is capable of showing a table with transcription start sites and
 * contains an export button, which exports the data into an excel file.
 *
 * @author -Rolf Hilker-
 */
public class ResultPanelTranscriptionStart extends ResultTablePanel {
    
    private static final long serialVersionUID = 1L;

    public static final String TSS_TOTAL = "Total number of detected TSSs";
    public static final String TSS_CORRECT = "Correct TSS";
    public static final String TSS_UPSTREAM = "TSS with upstream feature";
    public static final String TSS_DOWNSTREAM = "TSS with downstream feature";
    public static final String TSS_LEADERLESS = "Leaderless TSS";
    public static final String TSS_FWD = "TSS on fwd strand";
    public static final String TSS_REV = "TSS on rev strand";
    public static final String TSS_NOVEL = "Novel Transcripts";
    public static final int UNUSED_STATISTICS_VALUE = -1;
    
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
                int posColumnIdx = 0;
                int chroColumnIdx = 2;
                TableUtils.showPosition(tSSTable, posColumnIdx, chroColumnIdx, getBoundsInfoManager());
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
        statisticsMap.put(TSS_LEADERLESS, 0);
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
                "Position", "Track", "Chromosome", "Strand", "Initial Coverage", "Coverage Increase", "Coverage Increase %", "Correct Feature", "Next Upstream Feature", "Dist. Upstream Feature", "Next Downstream Feature", "Dist. Downstream Feature", "Novel Transcript", "Transcript Stop"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Boolean.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tssScrollPane.setViewportView(tSSTable);
        if (tSSTable.getColumnModel().getColumnCount() > 0) {
            tSSTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title0_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title1_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title2_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title4_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title5_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title6_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title7_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title12")); // NOI18N
            tSSTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title8_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title13")); // NOI18N
            tSSTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title9_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title10_1")); // NOI18N
        }

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.exportButton.text_1")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.parametersLabel.text_1")); // NOI18N

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.statisticsButton.text_1")); // NOI18N
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
                .addComponent(parametersLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 1032, Short.MAX_VALUE)
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
        
        this.processResultForExport();
        
        TableExportFileChooser fileChooser = new TableExportFileChooser(TableExportFileChooser.getTableFileExtensions(), tssResult); 
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
     * @param newResult transcription start sites detection result to add
     */
    @Override
    @NbBundle.Messages("ResultPanelTranscriptionStart.parametersLabel.text_1=Parameters: min no read starts:  {0},  min coverage increase percent: {1}%,  max leaderless dist.: {2},  init. low cov. read start max:  {3},  min low cov. read starts: {4},  detect novel transcripts: {5},  transcript extension cov.: {6}")
    public void addResult(final ResultTrackAnalysis newResult) {

        if (newResult instanceof TssDetectionResult) {
            final TssDetectionResult tssResultNew = (TssDetectionResult) newResult;
            final List<TranscriptionStart> tsss = new ArrayList<>(tssResultNew.getResults());

            if (tssResult == null) {
                tssResult = tssResultNew;
            } else {
                tssResult.getResults().addAll(tssResultNew.getResults());
            }
            final ParameterSetTSS tssParameters = (ParameterSetTSS) tssResult.getParameters();
            
            SwingUtilities.invokeLater(new Runnable() {
//because it is not called from the swing dispatch thread
                @Override
                public void run() {

                    final int nbColumns = 14;

                    int noCorrectStarts = 0;
                    int noUpstreamFeature = 0;
                    int noDownstreamFeature = 0;
                    int noFwdFeatures = 0;
                    int noRevFeatures = 0;
                    int noUnannotatedTranscripts = 0;
                    int noLeaderlessTranscripts = 0;

                    DefaultTableModel model = (DefaultTableModel) tSSTable.getModel();
                    String strand;
                    int distance;
                    DetectedFeatures detFeatures;
                    PersistantFeature feature;
                    TransStartUnannotated tSSU;

                    for (TranscriptionStart tss : tsss) {

                        if (tss.isFwdStrand()) {
                            strand = SequenceUtils.STRAND_FWD_STRING;
                            ++noFwdFeatures;
                        } else {
                            strand = SequenceUtils.STRAND_REV_STRING;
                            ++noRevFeatures;
                        }

                        Object[] rowData = new Object[nbColumns];
                        int i = 0;
                        rowData[i++] = tss.getPos();
                        rowData[i++] = tssResultNew.getTrackMap().get(tss.getTrackId());
                        rowData[i++] = tssResult.getChromosomeMap().get(tss.getChromId());
                        rowData[i++] = strand;
                        rowData[i++] = tss.getReadStartsAtPos();
                        rowData[i++] = tss.getCoverageIncrease();
                        rowData[i++] = tss.getPercentIncrease();

                        detFeatures = tss.getDetFeatures();
                        feature = detFeatures.getCorrectStartFeature();
                        if (feature != null) {
                            rowData[i++] = feature.toString();
                            ++noCorrectStarts;
                        } else {
                            rowData[i++] = "-";
                        }
                        feature = detFeatures.getUpstreamFeature();
                        if (feature != null) {
                            rowData[i++] = feature.toString();
                            rowData[i++] = Math.abs(tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop()));
                            ++noUpstreamFeature;
                        } else {
                            rowData[i++] = "-";
                            rowData[i++] = "";
                        }
                        feature = detFeatures.getDownstreamFeature();
                        if (feature != null) {
                            rowData[i++] = feature.toString();
                            distance = Math.abs(tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop()));
                            rowData[i++] = distance;
                            if (distance <= tssParameters.getMaxLeaderlessDistance()) {
                                ++noLeaderlessTranscripts;
                            }
                            ++noDownstreamFeature;
                        } else {
                            rowData[i++] = "-";
                            rowData[i++] = "";
                        }

                        if (tss instanceof TransStartUnannotated) {
                            tSSU = (TransStartUnannotated) tss;
                            rowData[i++] = true;
                            rowData[i++] = tSSU.getDetectedStop();
                            ++noUnannotatedTranscripts;
                        } else {
                        }
                        model.addRow(rowData);
                    }

                    //create statistics
                    statisticsMap.put(TSS_TOTAL, statisticsMap.get(TSS_TOTAL) + tsss.size());
                    statisticsMap.put(TSS_CORRECT, statisticsMap.get(TSS_CORRECT) + noCorrectStarts);
                    statisticsMap.put(TSS_UPSTREAM, statisticsMap.get(TSS_UPSTREAM) + noUpstreamFeature);
                    statisticsMap.put(TSS_DOWNSTREAM, statisticsMap.get(TSS_DOWNSTREAM) + noDownstreamFeature);
                    statisticsMap.put(TSS_LEADERLESS, statisticsMap.get(TSS_LEADERLESS) + noLeaderlessTranscripts + noCorrectStarts);
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
                    parametersLabel.setText(Bundle.ResultPanelTranscriptionStart_parametersLabel_text_1(tssParameters.getMinNoReadStarts(), tssParameters.getMinPercentIncrease(),
                            tssParameters.getMaxLeaderlessDistance(), tssParameters.getMaxLowCovReadStarts(), tssParameters.getMinLowCovReadStarts(), 
                            unannotatedTranscriptDet, tssParameters.getMinTranscriptExtensionCov()));

                }
            });
        }
    }
    
    /**
     * Set the reference viewer needed for updating the currently shown position
     * and extracting the reference sequence.
     * @param referenceViewer the reference viewer belonging to this analysis 
     * result
     */
    public void setReferenceViewer(ReferenceViewer referenceViewer) {
        this.setBoundsInfoManager(referenceViewer.getBoundsInformationManager());
        this.referenceViewer = referenceViewer;
    }

    /**
     * @return The number of detected TSS
     */
    @Override
    public int getDataSize() {
        return this.tssResult.getResults().size();
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
        String promotor;
        
        //get the promotor region for each TSS
        int promotorStart;
        int chromLength = ref.getActiveChromosome().getLength();
        for (TranscriptionStart tSS : this.tssResult.getResults()) {
            if (tSS.isFwdStrand()) {
                promotorStart = tSS.getPos() - 70;
                promotorStart = promotorStart < 0 ? 0 : promotorStart;
                promotor = ref.getActiveChromSequence(promotorStart, tSS.getPos());
            } else {
                promotorStart = tSS.getPos() + 70;
                promotorStart = promotorStart > chromLength ? chromLength : promotorStart;
                promotor = SequenceUtils.getReverseComplement(ref.getActiveChromSequence(tSS.getPos(), promotorStart));
            }
            this.promotorRegions.add(promotor);
        }
        tssResult.setPromotorRegions(promotorRegions);
    }
}
