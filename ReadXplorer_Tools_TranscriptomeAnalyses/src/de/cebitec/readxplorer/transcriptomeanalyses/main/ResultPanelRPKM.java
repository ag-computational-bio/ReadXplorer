
package de.cebitec.readxplorer.transcriptomeanalyses.main;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.RPKMvalue;
import de.cebitec.readxplorer.ui.visualisation.reference.ReferenceFeatureTopComp;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readxplorer.view.analysis.ResultTablePanel;
import de.cebitec.readxplorer.view.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.view.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.view.tablevisualization.TableUtils;
import de.cebitec.readxplorer.view.tablevisualization.tablefilter.TableRightClickFilter;
import java.util.HashMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * Panel showing a result of an analysis filtering for features with a min and
 * max certain readcount.
 *
 * @author -Rolf Hilker-
 */
public class ResultPanelRPKM extends ResultTablePanel {

    private static final long serialVersionUID = 1L;
    public static final String RETURNED_FEATURES = "Total number of returned features";
    public static final String FEATURES_TOTAL = "Total number of reference features";
    private RPKMAnalysisResult rpkmCalcResult;
    private final HashMap<String, Object> filterStatisticsMap;
    private PersistentFeature feature;
    private final boolean statistics = false;
    private BoundsInfoManager boundsInfoManager;
    private ReferenceViewer referenceViewer;
    private final TableRightClickFilter<UneditableTableModel> tableFilter;
    private ReferenceFeatureTopComp refComp;


    /**
     * Panel showing a result of an analysis filtering for features with a min
     * and max certain readcount.
     */
    public ResultPanelRPKM() {
        initComponents();
        final int trackColumnIdx = 12;
        final int posColumnIdx = 3;
        final int chromColumnIdx = 10;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumnIdx, trackColumnIdx );
        this.rpkmTable.getTableHeader().addMouseListener( tableFilter );
        this.filterStatisticsMap = new HashMap<>();
        this.filterStatisticsMap.put( RETURNED_FEATURES, 0 );
//        this.refComp = ReferenceFeatureTopComp.findInstance();

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.rpkmTable.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
//                showFeatureStartPosition();
                TableUtils.showPosition( rpkmTable, posColumnIdx, chromColumnIdx, boundsInfoManager );
//                refComp.showTableFeature(rpkmTable, 0);
            }


        } );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        rpkmTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();

        rpkmTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Feature", "Feature Type", "Strand", "Feature Start", "Feature Stop", "Feature Length", "Longest Detected 5'-UTR Length", "RPKM", "Log-RPKM", "Mapped Total", "Chromosome", "Chrom. ID", "Track", "Track ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Long.class, java.lang.Long.class, java.lang.Long.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
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
        jScrollPane1.setViewportView(rpkmTable);
        if (rpkmTable.getColumnModel().getColumnCount() > 0) {
            rpkmTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title0_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title5_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title9_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title1_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title2_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title7_1_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title13_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title9_2")); // NOI18N
            rpkmTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title10_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title12_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title4_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title10_2")); // NOI18N
            rpkmTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title8_1")); // NOI18N
            rpkmTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.rpkmTable.columnModel.title11_1")); // NOI18N
        }

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelRPKM.class, "ResultPanelRPKM.exportButton.text_1")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(exportButton))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1442, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), rpkmCalcResult );
    }//GEN-LAST:event_exportButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable rpkmTable;
    // End of variables declaration//GEN-END:variables


    /**
     * Updates the navigator bar of all viewers to the start position of the
     * selected feature.
     */
    private void showFeatureStartPosition() {
        DefaultListSelectionModel model = (DefaultListSelectionModel) this.rpkmTable.getSelectionModel();
        int selectedView = model.getLeadSelectionIndex();
        int selectedModel = this.rpkmTable.convertRowIndexToModel( selectedView );
        feature = (PersistentFeature) this.rpkmTable.getModel().getValueAt( selectedModel, 0 );
        int pos = feature.getStartOnStrand();

        getBoundsInfoManager().navigatorBarUpdated( pos );
    }


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


    /**
     * Adds a list of features with read count and RPKM values to this panel.
     *
     * @param newResult the new result to add
     */
    @Override
    public void addResult( ResultTrackAnalysis newResult ) {

        if( newResult instanceof RPKMAnalysisResult ) {
            RPKMAnalysisResult rpkmCalcResultNew = (RPKMAnalysisResult) newResult;
            final int nbColumns = 14;

            if( this.rpkmCalcResult == null ) {
                this.rpkmCalcResult = rpkmCalcResultNew;
                this.filterStatisticsMap.put( FEATURES_TOTAL, rpkmCalcResultNew.getNoGenomeFeatures() );
            }
            else {
                this.rpkmCalcResult.getResults().addAll( rpkmCalcResultNew.getResults() );
            }

//            SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
//                @Override
//                public void run() {
            DefaultTableModel model = (DefaultTableModel) rpkmTable.getModel();

            PersistentFeature feat;
            for( RPKMvalue rpkm : rpkmCalcResult.getResults() ) {
                feat = rpkm.getFeature();
                Object[] rowData = new Object[nbColumns];
                int i = 0;
                rowData[i++] = feat.getLocus();
                rowData[i++] = feat.getType();
                rowData[i++] = feat.isFwdStrandString();
                rowData[i++] = feat.isFwdStrand() ? feat.getStart() : feat.getStop();
                rowData[i++] = feat.isFwdStrand() ? feat.getStop() : feat.getStart();
                rowData[i++] = ((feat.getStop() + 1) - (feat.getStart() + 1)) + 1;
                rowData[i++] = rpkm.getLongestKnownUtrLength();
                rowData[i++] = rpkm.getRPKM();
                rowData[i++] = rpkm.getLogRpkm();
                rowData[i++] = rpkm.getReadCount();
                rowData[i++] = rpkmCalcResult.getChromosomeMap().get( feat.getChromId() );
                rowData[i++] = feat.getChromId();
                rowData[i++] = rpkmCalcResult.getTrackEntry( rpkm.getTrackId(), false );
                rowData[i++] = rpkm.getTrackId();

                model.addRow( rowData );
            }

            TableRowSorter<TableModel> sorter = new TableRowSorter<>();
            rpkmTable.setRowSorter( sorter );
            sorter.setModel( model );
//                }
//            });
        }
    }


    /**
     * @return the number of features filtered during the associated analysis
     */
    @Override
    public int getDataSize() {
        return this.rpkmCalcResult.getResults().size();
    }


}
