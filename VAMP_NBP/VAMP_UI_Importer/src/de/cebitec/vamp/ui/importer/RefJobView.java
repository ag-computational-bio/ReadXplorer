package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.parser.ReferenceJob;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ddoppmeier
 */
public class RefJobView extends javax.swing.JPanel implements ListSelectionListener{

    private static final long serialVersionUID = 1231231;

    private List<ReferenceJob> jobs;
    private boolean hasJobs;

    /** Creates new form TaskViewerTemplate */
    public RefJobView() {
        initComponents();
        this.jobs = new ArrayList<ReferenceJob>();
    }

    public ReferenceJob getSelectedItem() {
        return jobs.get(refTable.getSelectedRow());
    }

    public void add(ReferenceJob refGenJob) {
        DefaultTableModel model = (DefaultTableModel) refTable.getModel();
        model.addRow(new Object[]{refGenJob.getName(), refGenJob.getFile().getName(), refGenJob.getDescription()});
        jobs.add(refGenJob);

        if (!hasJobs){
            hasJobs = true;
            firePropertyChange(ImportSetupCard.PROP_HAS_JOBS, null, hasJobs);
        }
    }

    public void remove(ReferenceJob refGenJob){
        int row = jobs.lastIndexOf(refGenJob);
        jobs.remove(refGenJob);
        DefaultTableModel model = (DefaultTableModel) refTable.getModel();
        model.removeRow(row);

        if (jobs.isEmpty()){
            hasJobs = false;
            firePropertyChange(ImportSetupCard.PROP_HAS_JOBS, null, hasJobs);
        }
    }

    public boolean IsRowSelected(){
        ListSelectionModel model = refTable.getSelectionModel();
        return !model.isSelectionEmpty();
    }

    public boolean hasImportJobs(){
        return !jobs.isEmpty();
    }

    public List<ReferenceJob> getJobs(){
        return jobs;
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
        refTable = new javax.swing.JTable();
        refTable.getSelectionModel().addListSelectionListener(this);

        refTable.setAutoCreateRowSorter(true);
        refTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "File", "Description"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        refTable.setFillsViewportHeight(true);
        refTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(refTable);
        refTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(RefJobView.class, "RefJobView.trackTable.name")); // NOI18N
        refTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(RefJobView.class, "RefJobView.trackTable.file")); // NOI18N
        refTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(RefJobView.class, "RefJobView.trackTable.description")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable refTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel model = (ListSelectionModel) e.getSource();
        if(model.isSelectionEmpty()){
            firePropertyChange(ImportSetupCard.PROP_JOB_SELECTED, null, Boolean.FALSE);
        } else {
            firePropertyChange(ImportSetupCard.PROP_JOB_SELECTED, null, Boolean.TRUE);
        }
    }

}
