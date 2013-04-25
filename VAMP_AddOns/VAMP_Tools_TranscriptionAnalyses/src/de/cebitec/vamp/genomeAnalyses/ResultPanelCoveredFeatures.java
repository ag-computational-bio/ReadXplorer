/*
 * ResultPanelCoveredFeatures.java
 *
 * Created on 27.01.2012, 14:31:15
 */
package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.exporter.excel.ExcelExportFileChooser;
import de.cebitec.vamp.util.TableRightClickFilter;
import de.cebitec.vamp.util.UneditableTableModel;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.tableVisualization.TableUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel showing a result of an analysis filtering for features with a 
 * min and max certain readcount.
 * 
 * @author -Rolf Hilker-
 */
public class ResultPanelCoveredFeatures extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 1L;
    
    public static final String FEATURES_COVERED = "Total number of filtered features";
    public static final String FEATURES_TOTAL = "Total number of reference features";

    private BoundsInfoManager bim;
    private CoveredFeatureResult coveredFeaturesResult;
    private final Map<String, Integer> coveredStatisticsMap;
    private TableRightClickFilter<UneditableTableModel> tableFilter = new TableRightClickFilter<>(UneditableTableModel.class);
    
    
    /**
     * Panel showing a result of an analysis filtering for features with a
     * min and max certain readcount.
     * @param coveredFeaturesParameters parameter set used for this feature filtering
     */
    public ResultPanelCoveredFeatures() {
        initComponents();
        this.coveredFeaturesTable.getTableHeader().addMouseListener(tableFilter);
        this.coveredStatisticsMap = new HashMap<>();
        this.coveredStatisticsMap.put(FEATURES_COVERED, 0);
        
        DefaultListSelectionModel model = (DefaultListSelectionModel) this.coveredFeaturesTable.getSelectionModel();
        model.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                TableUtils.showPosition(coveredFeaturesTable, 0, bim);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coveredFeaturesPane = new javax.swing.JScrollPane();
        coveredFeaturesTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        parametersLabel = new javax.swing.JLabel();
        statisticsButton = new javax.swing.JButton();

        coveredFeaturesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Feature", "Track", "Strand", "Start", "Stop", "Length", "Covered Percent", "Covered Bases"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        coveredFeaturesPane.setViewportView(coveredFeaturesTable);
        coveredFeaturesTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title0")); // NOI18N
        coveredFeaturesTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title7_1")); // NOI18N
        coveredFeaturesTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title7")); // NOI18N
        coveredFeaturesTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title8")); // NOI18N
        coveredFeaturesTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title9")); // NOI18N
        coveredFeaturesTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title10")); // NOI18N
        coveredFeaturesTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title11")); // NOI18N
        coveredFeaturesTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title12")); // NOI18N

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.exportButton.text")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.parametersLabel.text")); // NOI18N

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.statisticsButton.text")); // NOI18N
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
                .addComponent(parametersLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportButton))
            .addComponent(coveredFeaturesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(coveredFeaturesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(statisticsButton)
                    .addComponent(parametersLabel)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        ExcelExportFileChooser fileChooser = new ExcelExportFileChooser(new String[]{"xls"}, "xls", this.coveredFeaturesResult); 
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog(this, new CoveredFeatureStatsPanel(coveredStatisticsMap), "Covered Feature Detection Statistics", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_statisticsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane coveredFeaturesPane;
    private javax.swing.JTable coveredFeaturesTable;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables


    public void setBoundsInfoManager(BoundsInfoManager boundsInformationManager) {
        this.bim = boundsInformationManager;
    }

    /**
     * Adds a list of covered features to this panel.
     * @param coveredFeaturesResultNew 
     */
    public void addCoveredFeatures(final CoveredFeatureResult coveredFeaturesResultNew) {
        final int nbColumns = 8;
        final List<CoveredFeature> features = new ArrayList<>(coveredFeaturesResultNew.getResults());
        
        if (this.coveredFeaturesResult == null) {
            this.coveredFeaturesResult = coveredFeaturesResultNew;
            this.coveredStatisticsMap.put(FEATURES_TOTAL, coveredFeaturesResultNew.getFeatureListSize());
        } else {
            this.coveredFeaturesResult.getResults().addAll(coveredFeaturesResultNew.getResults());
        }
        
        SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
            @Override
            public void run() {
                DefaultTableModel model = (DefaultTableModel) coveredFeaturesTable.getModel();

                PersistantFeature feature;
                for (CoveredFeature coveredFeature : features) {

                    Object[] rowData = new Object[nbColumns];
                    feature = coveredFeature.getCoveredFeature();
                    rowData[0] = feature;
                    rowData[1] = coveredFeaturesResultNew.getTrackMap().get(coveredFeature.getTrackId());
                    rowData[2] = feature.isFwdStrandString();
                    rowData[3] = feature.isFwdStrand() ? feature.getStart() : feature.getStop();
                    rowData[4] = feature.isFwdStrand() ? feature.getStop() : feature.getStart();
                    rowData[5] = feature.getStop() - feature.getStart();
                    rowData[6] = coveredFeature.getPercentCovered();
                    rowData[7] = coveredFeature.getNoCoveredBases();

                    model.addRow(rowData);
                }

                coveredStatisticsMap.put(FEATURES_COVERED, coveredStatisticsMap.get(FEATURES_COVERED)
                        + features.size());
                coveredFeaturesResult.setStatsMap(coveredStatisticsMap);

                TableRowSorter<TableModel> sorter = new TableRowSorter<>();
                coveredFeaturesTable.setRowSorter(sorter);
                sorter.setModel(model);

                ParameterSetCoveredFeatures parameters = ((ParameterSetCoveredFeatures) coveredFeaturesResult.getParameters());
                parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class,
                        "ResultPanelCoveredFeatures.parametersLabel.text", parameters.getMinCoveredPercent(),
                        parameters.getMinCoverageCount()));
            }
        });
    }
    
    /**
     * @return the number of features filtered during the associated analysis
     */
    public int getResultSize() {
        return this.coveredFeaturesResult.getResults().size();
    }
}
