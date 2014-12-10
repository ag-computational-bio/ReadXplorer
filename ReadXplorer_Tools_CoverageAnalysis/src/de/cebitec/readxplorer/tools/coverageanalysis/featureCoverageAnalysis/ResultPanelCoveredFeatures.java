/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.tools.coverageanalysis.featureCoverageAnalysis;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readxplorer.ui.visualisation.reference.ReferenceFeatureTopComp;
import de.cebitec.readxplorer.utils.UneditableTableModel;
import de.cebitec.readxplorer.view.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.view.tablevisualization.TableUtils;
import de.cebitec.readxplorer.view.tablevisualization.tablefilter.TableRightClickFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * Panel showing a result of an analysis filtering for features with a
 * min and max certain readcount.
 * <p>
 * @author -Rolf Hilker-
 */
public class ResultPanelCoveredFeatures extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    public static final String FEATURES_COVERED = "Total number of covered features";
    public static final String FEATURES_TOTAL = "Total number of reference features";

    private BoundsInfoManager bim;
    private CoveredFeatureResult coveredFeaturesResult;
    private Map<String, Integer> coveredStatisticsMap;
    private final TableRightClickFilter<UneditableTableModel> tableFilter;
    private ReferenceFeatureTopComp refFeatureComp;


    /**
     * Panel showing a result of an analysis filtering for features with a
     * min and max certain readcount.
     * <p>
     * @param coveredFeaturesParameters parameter set used for this feature
     *                                  filtering
     */
    public ResultPanelCoveredFeatures() {
        initComponents();
        final int posColumnIdx = 0;
        final int trackColumnIdx = 1;
        final int chromColumnIdx = 2;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumnIdx, trackColumnIdx );
        this.coveredFeaturesTable.getTableHeader().addMouseListener( tableFilter );
        this.coveredStatisticsMap = new HashMap<>();
        this.refFeatureComp = ReferenceFeatureTopComp.findInstance();

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.coveredFeaturesTable.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showPosition( coveredFeaturesTable, posColumnIdx, chromColumnIdx, bim );
                refFeatureComp.showTableFeature( coveredFeaturesTable, 0 );
            }


        } );
    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
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
                "Feature", "Track", "Chromosome", "Strand", "Start", "Stop", "Length", "Mean Coverage", "Covered Percent", "Covered Bases"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        coveredFeaturesPane.setViewportView(coveredFeaturesTable);
        if (coveredFeaturesTable.getColumnModel().getColumnCount() > 0) {
            coveredFeaturesTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title0")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title7_1_1")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title8_1")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title7_2")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title8_2")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title9_2")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title10_1")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title9")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title11_1")); // NOI18N
            coveredFeaturesTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoveredFeatures.class, "ResultPanelCoveredFeatures.coveredFeaturesTable.columnModel.title12_1")); // NOI18N
        }

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
            .addComponent(coveredFeaturesPane)
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
        TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), this.coveredFeaturesResult );
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog( this, new CoveredFeatureStatsPanel( coveredStatisticsMap ), "Feature Coverage Analysis Statistics", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_statisticsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane coveredFeaturesPane;
    private javax.swing.JTable coveredFeaturesTable;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables


    public void setBoundsInfoManager( BoundsInfoManager boundsInformationManager ) {
        this.bim = boundsInformationManager;
    }


    /**
     * Adds a list of covered features to this panel.
     * <p>
     * @param coveredFeaturesResultNew
     */
    public void addCoveredFeatures( final CoveredFeatureResult coveredFeaturesResultNew ) {
        tableFilter.setTrackMap( coveredFeaturesResultNew.getTrackMap() );

        final int nbColumns = 10;
        final List<CoveredFeature> features = new ArrayList<>( coveredFeaturesResultNew.getResults() );

        if( this.coveredFeaturesResult == null ) {
            this.coveredFeaturesResult = coveredFeaturesResultNew;
            this.coveredStatisticsMap = coveredFeaturesResult.getStatsMap();
            this.coveredStatisticsMap.put( FEATURES_COVERED, 0 );
        }
        else {
            this.coveredFeaturesResult.getResults().addAll( coveredFeaturesResultNew.getResults() );
        }

        DefaultTableModel model = (DefaultTableModel) coveredFeaturesTable.getModel();

        PersistentFeature feature;
        for( CoveredFeature coveredFeature : features ) {

            Object[] rowData = new Object[nbColumns];
            int i = 0;
            feature = coveredFeature.getCoveredFeature();
            rowData[i++] = feature;
            rowData[i++] = coveredFeaturesResultNew.getTrackEntry( coveredFeature.getTrackId(), false );
            rowData[i++] = coveredFeaturesResultNew.getChromosomeMap().get( feature.getChromId() );
            rowData[i++] = feature.isFwdStrandString();
            rowData[i++] = feature.getStartOnStrand();
            rowData[i++] = feature.getStopOnStrand();
            rowData[i++] = feature.getLength();
            rowData[i++] = coveredFeature.getMeanCoverage();
            rowData[i++] = coveredFeature.getPercentCovered();
            rowData[i++] = coveredFeature.getNoCoveredBases();

            model.addRow( rowData );
        }

        coveredStatisticsMap.put( FEATURES_COVERED, coveredStatisticsMap.get( FEATURES_COVERED )
                                                    + features.size() );
        coveredFeaturesResult.setStatsMap( coveredStatisticsMap );

        TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        coveredFeaturesTable.setRowSorter( sorter );
        sorter.setModel( model );

        ParameterSetCoveredFeatures parameters = ((ParameterSetCoveredFeatures) coveredFeaturesResult.getParameters());
        String strandOption = parameters.getReadClassParams().getStrandOptionString();
        String coveredFeatures = parameters.isGetCoveredFeatures() ? "no" : "yes";
        parametersLabel.setText( org.openide.util.NbBundle.getMessage( ResultPanelCoveredFeatures.class,
                                                                       "ResultPanelCoveredFeatures.parametersLabel.text", parameters.getMinCoveredPercent(),
                                                                       parameters.getMinCoverageCount(), strandOption, coveredFeatures ) );
    }


    /**
     * @return the number of features filtered during the associated analysis
     */
    public int getResultSize() {
        return this.coveredFeaturesResult.getResults().size();
    }


}
