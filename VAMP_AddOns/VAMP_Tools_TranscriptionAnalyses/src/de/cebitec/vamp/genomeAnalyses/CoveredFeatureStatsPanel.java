package de.cebitec.vamp.genomeAnalyses;

import java.util.Map;
import org.openide.util.NbBundle;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeatureStatsPanel extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 1L;
    private Map<String, Integer> featureStatsMap;

    /**
     * Creates new form CoveredFeatureStatsPanel
     */
    public CoveredFeatureStatsPanel(Map<String, Integer> featureStatsMap) {
        this.initComponents();
        this.featureStatsMap = featureStatsMap;
        this.initAdditionalComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coveredFeatureStatsScrollpane = new javax.swing.JScrollPane();
        coveredFeatureStatsTable = new javax.swing.JTable();

        coveredFeatureStatsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        coveredFeatureStatsScrollpane.setViewportView(coveredFeatureStatsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(coveredFeatureStatsScrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(coveredFeatureStatsScrollpane, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane coveredFeatureStatsScrollpane;
    private javax.swing.JTable coveredFeatureStatsTable;
    // End of variables declaration//GEN-END:variables

    private void initAdditionalComponents() {
        coveredFeatureStatsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                    {ResultPanelCoveredFeatures.FEATURES_COVERED, String.valueOf(this.featureStatsMap.get(ResultPanelCoveredFeatures.FEATURES_COVERED))},
                    {ResultPanelCoveredFeatures.FEATURES_TOTAL, String.valueOf(this.featureStatsMap.get(ResultPanelCoveredFeatures.FEATURES_TOTAL))}
                },
                new String[]{
                    NbBundle.getMessage(CoveredFeatureStatsPanel.class, "CoveredFeatureStatsPanel.coveredFeatureStatsTable.columnModel.title0"),
                    NbBundle.getMessage(CoveredFeatureStatsPanel.class, "CoveredFeatureStatsPanel.coveredFeatureStatsTable.columnModel.title1")
                }) {
            private static final long serialVersionUID = 1L;
            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
    }
}
