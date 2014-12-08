
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import java.util.HashMap;
import org.openide.util.NbBundle;


/**
 * Panel for showing the the statistics of an operon detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class OperonDetectionStatsPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private final HashMap<String, Object> operonStatsMap;


    /**
     * Creates new form OperonDetectionStatsPanel
     *
     * @param operonStatsMap result of an operon detection
     */
    public OperonDetectionStatsPanel( HashMap<String, Object> operonStatsMap ) {
        this.operonStatsMap = operonStatsMap;
        this.initComponents();
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

        operonDetectionStatsScrollpane = new javax.swing.JScrollPane();
        operonDetectionStatsTable = new javax.swing.JTable();

        operonDetectionStatsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
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
        operonDetectionStatsScrollpane.setViewportView(operonDetectionStatsTable);
        if (operonDetectionStatsTable.getColumnModel().getColumnCount() > 0) {
            operonDetectionStatsTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(OperonDetectionStatsPanel.class, "OperonDetectionStatsPanel.operonDetectionStatsTable.columnModel.title0_1")); // NOI18N
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(operonDetectionStatsScrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(operonDetectionStatsScrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane operonDetectionStatsScrollpane;
    private javax.swing.JTable operonDetectionStatsTable;
    // End of variables declaration//GEN-END:variables


    private void initAdditionalComponents() {
        double mappingCount = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.MAPPINGS_COUNT );
        double meanMappingLength = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH );
        double mappingsPerMio = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.MAPPINGS_MILLION );
        double backgroundThreshold = (double) this.operonStatsMap.get( ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS );
        operonDetectionStatsTable.setModel( new javax.swing.table.DefaultTableModel(
                new Object[][]{
                    { "Operon Statistics", "" },
                    { ResultPanelOperonDetection.OPERONS_TOTAL,
                      String.valueOf( this.operonStatsMap.get( ResultPanelOperonDetection.OPERONS_TOTAL ) ) },
                    { ResultPanelOperonDetection.OPERONS_TWO_GENES,
                      String.valueOf( this.operonStatsMap.get( ResultPanelOperonDetection.OPERONS_TWO_GENES ) ) },
                    { ResultPanelOperonDetection.OPERONS_BIGGEST,
                      String.valueOf( this.operonStatsMap.get( ResultPanelOperonDetection.OPERONS_BIGGEST ) ) },
                    { "Mapping Statistics", "" },
                    { ResultPanelTranscriptionStart.MAPPINGS_COUNT,
                      String.valueOf( String.format( "%2.2f", mappingCount ) ) },
                    { ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH, String.valueOf( String.format( "%2.2f", meanMappingLength ) ) },
                    { ResultPanelTranscriptionStart.MAPPINGS_MILLION,
                      String.valueOf( String.format( "%2.2f", mappingsPerMio ) ) },
                    { ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS,
                      String.valueOf( String.format( "%2.2f", backgroundThreshold ) ) }
                },
                new String[]{
                    NbBundle.getMessage( OperonDetectionStatsPanel.class, "OperonDetectionStatsPanel.operonDetectionStatsTable.columnModel.title0" ),
                    NbBundle.getMessage( OperonDetectionStatsPanel.class, "OperonDetectionStatsPanel.operonDetectionStatsTable.columnModel.title1" )
                } ) {
                    private static final long serialVersionUID = 1L;
                    Class<?>[] types = new Class<?>[]{
                        java.lang.String.class, java.lang.String.class
                    };
                    boolean[] canEdit = new boolean[]{
                        false, false
                    };


                    @Override
                    public Class<?> getColumnClass( int columnIndex ) {
                        return types[columnIndex];
                    }


                    @Override
                    public boolean isCellEditable( int rowIndex, int columnIndex ) {
                        return canEdit[columnIndex];
                    }


                } );
    }


}
