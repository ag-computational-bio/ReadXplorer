package vamp.view.dataAdministration;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import vamp.importer.ReferenceJob;

/**
 *
 * @author ddoppmeier
 */
public class ReferenceView extends javax.swing.JPanel implements TableModelListener{

    private static final long serialVersionUID = 72465263;
    private List<ReferenceJob> jobs;
    private SelectionCard adminPanel;

    /** Creates new form RefGenView */
    public ReferenceView() {
        initComponents();
        jobs = new ArrayList<ReferenceJob>();

    }

    public void setDataAdminPanel(SelectionCard adminPanel){
        this.adminPanel = adminPanel;
    }

    void deselectRefGen(ReferenceJob refGen) {
        int row = jobs.indexOf(refGen);
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setValueAt(false, row, 0);
    }

    void refGenJobAdded(ReferenceJob refGenJob) {
        jobs.add(refGenJob);
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.addRow(new Object[]{false, refGenJob.getName(), refGenJob.getDescription(), refGenJob.getTimestamp()});
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

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Delete", "Name", "Description", "Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false
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
        jTable1.getModel().addTableModelListener(this);

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
    public void tableChanged(TableModelEvent e) {

        int row = e.getFirstRow();
        int column = e.getColumn();

        if(row >= 0 && column >= 0){
            ReferenceJob r = jobs.get(row);
            DefaultTableModel model = (DefaultTableModel) e.getSource();
            boolean selected = (Boolean) model.getValueAt(row, column);

            if(selected){
                // check if it is allowed to be deleted
                if(r.getDependentTracks().isEmpty()){
                    adminPanel.removeRefGenJob(r);
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot mark reference for deletion,\nas long as it is referenced by a track.\nResolve dependencies first!", "Unresolved Dependencies", JOptionPane.ERROR_MESSAGE);
                    model.setValueAt(false, row, column);
                }
            } else {
                adminPanel.unRemoveRefGenJob(r);
            }
        }

    }
}
