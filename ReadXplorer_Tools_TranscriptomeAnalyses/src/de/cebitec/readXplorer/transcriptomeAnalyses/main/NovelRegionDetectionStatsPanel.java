
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import java.util.HashMap;
import org.openide.util.NbBundle;


/**
 * Panel for showing the the statistics of a TSS detection result.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class NovelRegionDetectionStatsPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private HashMap<String, Object> statisticsMap;


    /**
     * Panel for showing the the statistics of a TSS detection result.
     *
     * @param statisticsMap the snp results statistics to display
     */
    public NovelRegionDetectionStatsPanel( HashMap<String, Object> statisticsMap ) {
        this.statisticsMap = statisticsMap;
        initComponents();
        this.initAdditionalComponents();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tssDetectionStatsScrollpane = new javax.swing.JScrollPane();
        tssDetectionStatsTable = new javax.swing.JTable();

        tssDetectionStatsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "", "Count"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tssDetectionStatsScrollpane.setViewportView(tssDetectionStatsTable);
        if (tssDetectionStatsTable.getColumnModel().getColumnCount() > 0) {
            tssDetectionStatsTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionDetectionStatsPanel.class, "NovelRegionDetectionStatsPanel.tssDetectionStatsTable.columnModel.title0")); // NOI18N
            tssDetectionStatsTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(NovelRegionDetectionStatsPanel.class, "NovelRegionDetectionStatsPanel.tssDetectionStatsTable.columnModel.title1_1")); // NOI18N
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tssDetectionStatsScrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tssDetectionStatsScrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane tssDetectionStatsScrollpane;
    private javax.swing.JTable tssDetectionStatsTable;
    // End of variables declaration//GEN-END:variables


    private void initAdditionalComponents() {
        double mappingCount = (double) this.statisticsMap.get( ResultPanelTranscriptionStart.MAPPINGS_COUNT );
        double meanMappingLength = (double) this.statisticsMap.get( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH );
        double mappingsPerMio = (double) this.statisticsMap.get( ResultPanelTranscriptionStart.MAPPINGS_MILLION );
        double backgroundThreshold = (double) this.statisticsMap.get( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE );
        tssDetectionStatsTable.setModel( new javax.swing.table.DefaultTableModel(
                new Object[][]{
                    { "Novel Transcript Statistics", "" },
                    { NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FEATURES, String.valueOf( this.statisticsMap.get( NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FEATURES ) ) },
                    { NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_REV_FEATURES, String.valueOf( this.statisticsMap.get( NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_REV_FEATURES ) ) },
                    { NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FWD_FEATURES, String.valueOf( this.statisticsMap.get( NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FWD_FEATURES ) ) },
                    { NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_CISANTISENSE, String.valueOf( this.statisticsMap.get( NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_CISANTISENSE ) ) },
                    { NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_TRANSGENIC, String.valueOf( this.statisticsMap.get( NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_TRANSGENIC ) ) },
                    { "Mapping Statistics", "" },
                    { ResultPanelTranscriptionStart.MAPPINGS_COUNT, String.valueOf( String.format( "%2.2f", mappingCount ) ) },
                    { ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH, String.valueOf( String.format( "%2.2f", meanMappingLength ) ) },
                    { ResultPanelTranscriptionStart.MAPPINGS_MILLION, String.valueOf( String.format( "%2.2f", mappingsPerMio ) ) },
                    { ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE, String.valueOf( String.format( "%2.2f", backgroundThreshold ) ) } },
                new String[]{
                    NbBundle.getMessage( NovelRegionDetectionStatsPanel.class, "TssDetectionStatsPanel.tssDetectionStatsTable.columnModel.title0" ),
                    NbBundle.getMessage( NovelRegionDetectionStatsPanel.class, "TssDetectionStatsPanel.tssDetectionStatsTable.columnModel.title1" )
                } ) {
                    private static final long serialVersionUID = 1L;
                    Class[] types = new Class[]{
                        java.lang.String.class, java.lang.String.class
                    };
                    boolean[] canEdit = new boolean[]{
                        false, false
                    };


                    @Override
                    public Class getColumnClass( int columnIndex ) {
                        return types[columnIndex];
                    }


                    @Override
                    public boolean isCellEditable( int rowIndex, int columnIndex ) {
                        return canEdit[columnIndex];
                    }


                } );
    }


}