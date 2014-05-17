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

/*
 * GeneStartsResultPanel.java
 *
 * Created on 27.01.2012, 14:31:03
 */
import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.readXplorer.util.LineWrapCellRenderer;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import de.cebitec.readXplorer.view.tableVisualization.TableComparatorProvider;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * This panel is capable of showing a table with detected operons and
 * contains an export button, which exports the data into an excel file.
 *
 * @author -Rolf Hilker-
 */
public class ResultPanelOperonDetection extends ResultTablePanel {
    
    private static final long serialVersionUID = 1L;
    
    public static final String OPERONS_TOTAL = "Total number of detected operons";
    public static final String OPERONS_WITH_OVERLAPPING_READS = "Operons with reads overlapping only one feature edge";
    public static final String OPERONS_WITH_INTERNAL_READS = "Operons with internal reads";

    private OperonDetectionResult operonResult;
    private HashMap<String, Integer> operonDetStats;
    private TableRightClickFilter<UneditableTableModel> tableFilter = new TableRightClickFilter<>(UneditableTableModel.class);

    /**
     * This panel is capable of showing a table with detected operons and
     * contains an export button, which exports the data into an excel file.
     * @param operonDetParameters parameters used for this operon detection
     */
    public ResultPanelOperonDetection(ParameterSetOperonDet operonDetParameters) {
        initComponents();
        this.operonDetectionTable.getTableHeader().addMouseListener(tableFilter);
        this.initStatsMap();        

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.operonDetectionTable.getSelectionModel();
        model.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int posColumnIdx = 5;
                int chromColumnIdx = 3;
                TableUtils.showPosition(operonDetectionTable, posColumnIdx, chromColumnIdx, getBoundsInfoManager());
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
        parametersLabel = new javax.swing.JLabel();
        statisticsButton = new javax.swing.JButton();

        operonDetectionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Feature 1", "Feature 2", "Track", "Chromosome", "Strand", "Start Feature 1", "Start Feature 2", "Reads Overlap Stop 1", "Reads Overlap Start 2", "Internal Reads", "Spanning Reads"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
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
        jScrollPane1.setViewportView(operonDetectionTable);
        operonDetectionTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title0_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title7_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title9_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.operonDetectionTable.columnModel.title10")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title1_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title2_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title8_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title3_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title4_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title5_1")); // NOI18N
        operonDetectionTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "OperonDetectionResultPanel.operonDetectionTable.columnModel.title6_1")); // NOI18N

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.exportButton.text")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.parametersLabel.text")); // NOI18N

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelOperonDetection.class, "ResultPanelOperonDetection.statisticsButton.text")); // NOI18N
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
                .addComponent(parametersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 439, Short.MAX_VALUE)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportButton))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(parametersLabel)
                        .addComponent(statisticsButton))
                    .addComponent(exportButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        TableExportFileChooser fileChooser = new TableExportFileChooser(TableExportFileChooser.getTableFileExtensions(), operonResult);
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog(this, new OperonDetectionStatsPanel(operonDetStats), "Operon Detection Statistics", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_statisticsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable operonDetectionTable;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Adds the data from this OperonDetectionResult to the data already available
     * in this result panel. All statistics etc. are also updated.
     * @param newResult the result to add
     */
    @Override
    public void addResult(ResultTrackAnalysis newResult) {
        if (newResult instanceof OperonDetectionResult) {
            OperonDetectionResult operonResultNew = (OperonDetectionResult) newResult;
            final int nbColumns = 11;
            final List<Operon> operons = new ArrayList<>(operonResultNew.getResults());

            if (this.operonResult == null) {
                this.operonResult = operonResultNew;
            } else {
                this.operonResult.getResults().addAll(operonResultNew.getResults());
            }

            DefaultTableModel model = (DefaultTableModel) operonDetectionTable.getModel();
            LineWrapCellRenderer lineWrapCellRenderer = new LineWrapCellRenderer();
            operonDetectionTable.getColumnModel().getColumn(0).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(1).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(4).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(5).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(6).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(7).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(8).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(9).setCellRenderer(lineWrapCellRenderer);
            operonDetectionTable.getColumnModel().getColumn(10).setCellRenderer(lineWrapCellRenderer);

            int operonsWithOverlapping = 0;
            int operonsWithInternal = 0;
            boolean hasOverlappingReads;
            boolean hasInternalReads;
            PersistantFeature feat1;

            for (Operon operon : operons) {
                feat1 = operon.getOperonAdjacencies().get(0).getFeature1();
                String annoName1 = "";
                String annoName2 = "";
                String strand = (feat1.isFwdStrandString()) + "\n";
                String startAnno1 = "";
                String startAnno2 = "";
                String readsAnno1 = "";
                String readsAnno2 = "";
                String internalReads = "";
                String spanningReads = "";
                hasOverlappingReads = false;
                hasInternalReads = false;

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
                Object[] rowData = new Object[nbColumns];
                int i = 0;
                rowData[i++] = annoName1;
                rowData[i++] = annoName2;
                rowData[i++] = operonResultNew.getTrackEntry(operon.getTrackId(), false);
                rowData[i++] = operonResult.getChromosomeMap().get(feat1.getChromId());
                rowData[i++] = strand;
                rowData[i++] = startAnno1;
                rowData[i++] = startAnno2;
                rowData[i++] = readsAnno1;
                rowData[i++] = readsAnno2;
                rowData[i++] = internalReads;
                rowData[i++] = spanningReads;
                if (!annoName1.isEmpty() && !annoName2.isEmpty()) {
                    model.addRow(rowData);
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
            for (int i = 3; i < 8; ++i) {
                TableComparatorProvider.setStringComparator(sorter, i);
            }

            parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class,
                    "ResultPanelOperonDetection.parametersLabel.text",
                    ((ParameterSetOperonDet) operonResult.getParameters()).getMinSpanningReads()));

            operonDetStats.put(OPERONS_TOTAL, operonDetStats.get(OPERONS_TOTAL) + operons.size());
            operonDetStats.put(OPERONS_WITH_OVERLAPPING_READS, operonDetStats.get(OPERONS_WITH_OVERLAPPING_READS) + operonsWithOverlapping);
            operonDetStats.put(OPERONS_WITH_INTERNAL_READS, operonDetStats.get(OPERONS_WITH_INTERNAL_READS) + operonsWithInternal);

            operonResult.setStatsMap(operonDetStats);
        }
    }
    
    /**
     * @return The number of detected operons
     */
    @Override
    public int getDataSize() {
        return this.operonResult.getResults().size();
    }
}
