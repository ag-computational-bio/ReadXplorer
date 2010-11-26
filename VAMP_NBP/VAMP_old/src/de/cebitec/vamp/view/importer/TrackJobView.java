package de.cebitec.vamp.view.importer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import de.cebitec.vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public class TrackJobView extends javax.swing.JPanel implements ListSelectionListener{

    private List<TrackJobs> tracks;
    private ImportSetupCard parent;
    public final static long serialVersionUID = 774292377;

    /** Creates new form TaskViewerTemplate */
    public TrackJobView() {
        this.init();
    }

    public TrackJobView(ImportSetupCard parent){
        this.parent = parent;
        this.init();
    }

    public TrackJobs getSelectedItem() {
        return tracks.get(jTable1.getSelectedRow());
    }

    private void init(){
        tracks = new ArrayList<TrackJobs>();
        initComponents();
    }

    public void add(TrackJobs trackJob){
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.addRow(new Object[]{
            trackJob.getFile().getName(),
            trackJob.getDescription(),
            trackJob.getRefGen().getDescription()});
        tracks.add(trackJob);
    }

    public void remove(TrackJobs trackJob){
        int index = tracks.indexOf(trackJob);
        tracks.remove(trackJob);

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.removeRow(index);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTable1.getSelectionModel().addListSelectionListener(this);

        setPreferredSize(new java.awt.Dimension(400, 300));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "File", "Description", "Run", "Reference"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel model = (ListSelectionModel) e.getSource();
        if(model.isSelectionEmpty()){
            parent.setRemoveButtonEnabled(false);
        } else {
            parent.setRemoveButtonEnabled(true);
        }
    }

    public boolean IsRowSelected(){
        ListSelectionModel model = jTable1.getSelectionModel();
        return !model.isSelectionEmpty();
    }


}