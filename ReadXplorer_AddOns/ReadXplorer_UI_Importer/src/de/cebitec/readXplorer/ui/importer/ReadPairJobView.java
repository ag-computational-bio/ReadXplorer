/*
 * ReadPairJobView.java
 *
 * Created on 18.05.2011, 14:57:49
 */
package de.cebitec.readXplorer.ui.importer;

import de.cebitec.readXplorer.parser.ReadPairJobContainer;
import de.cebitec.readXplorer.parser.TrackJob;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * View for showing all read pair jobs ready for import.
 *
 * @author Rolf Hilker
 */
public class ReadPairJobView extends javax.swing.JPanel implements ListSelectionListener {

    public final static long serialVersionUID = 774342377;
    private List<ReadPairJobContainer> readPairJobContainerList;
    private boolean hasJobs;

    /** View for showing all read pair jobs ready for import. */
    public ReadPairJobView() {
        readPairJobContainerList = new ArrayList<>();
        initComponents();
    }

    /**
     * @return the selected read pair job
     */
    public ReadPairJobContainer getSelectedItem() {
        return readPairJobContainerList.get(readPairJobTable.getSelectedRow());
    }

    /**
     * Add a new readuence pair job to the table view.
     * @param readPairJobContainer the container with the job to add
     * @return the updated read pair job table
     */
    public JTable add(ReadPairJobContainer readPairJobContainer){
        DefaultTableModel model = (DefaultTableModel) readPairJobTable.getModel();
        
        String orientation = "fr";
        TrackJob trackJob = readPairJobContainer.getTrackJob1();
        if (     readPairJobContainer.getOrientation() == 1){ orientation = "rf"; }
        else if (readPairJobContainer.getOrientation() == 2){ orientation = "ff/rr"; }
        String file2Name = readPairJobContainer.getTrackJob2() != null 
                ? readPairJobContainer.getTrackJob2().getFile().getName() : "-";
       
        model.addRow(new Object[] {
                trackJob.getFile().getName(),
                file2Name,
                trackJob.getDescription(),
                trackJob.getRefGen().getDescription(), 
                readPairJobContainer.getDistance(),
                readPairJobContainer.getDeviation(), 
                orientation,
                trackJob.isAlreadyImported()});
        
        this.readPairJobContainerList.add(readPairJobContainer);

        if (!hasJobs){
            hasJobs = true;
            firePropertyChange(ImportSetupCard.PROP_HAS_JOBS, null, hasJobs);
        }
        return readPairJobTable;
    }

    /**
     * Removes the selected readuence pair job.
     * @param readPairJobContainer the container of the read pair job to remove
     */
    public void remove(ReadPairJobContainer readPairJobContainer){
        int index = readPairJobContainerList.indexOf(readPairJobContainer);
        readPairJobContainerList.remove(readPairJobContainer);

        DefaultTableModel model = (DefaultTableModel) readPairJobTable.getModel();
        model.removeRow(index);

        if (readPairJobContainerList.isEmpty()){
            hasJobs = false;
            firePropertyChange(ImportSetupCard.PROP_HAS_JOBS, null, hasJobs);
        }
    }

    /**
     * @return the list of readuence pair jobs
     */
    public List<ReadPairJobContainer> getJobs(){
        return readPairJobContainerList;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel model = (ListSelectionModel) e.getSource();
        if(model.isSelectionEmpty()){
            firePropertyChange(ImportSetupCard.PROP_JOB_SELECTED, null, Boolean.FALSE);
        } else {
            firePropertyChange(ImportSetupCard.PROP_JOB_SELECTED, null, Boolean.TRUE);
        }
    }

    public boolean isRowSelected(){
        ListSelectionModel model = readPairJobTable.getSelectionModel();
        return !model.isSelectionEmpty();
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
        readPairJobTable = new javax.swing.JTable();
        readPairJobTable.getSelectionModel().addListSelectionListener(this);

        readPairJobTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "File 1", "File 2", "Description", "Reference", "Distance", "% Deviation", "Orientation", "Already imported"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class
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
        readPairJobTable.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(readPairJobTable);
        readPairJobTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "ReadPairJobView.readPairJobTable.columnModel.title0")); // NOI18N
        readPairJobTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "SeqPairJobView.trackTable.columnModel.title6")); // NOI18N
        readPairJobTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "SeqPairJobView.trackTable.columnModel.title1")); // NOI18N
        readPairJobTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "SeqPairJobView.trackTable.columnModel.title2")); // NOI18N
        readPairJobTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "SeqPairJobView.trackTable.columnModel.title3")); // NOI18N
        readPairJobTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "SeqPairJobView.trackTable.columnModel.title4")); // NOI18N
        readPairJobTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "SeqPairJobView.trackTable.columnModel.title5")); // NOI18N
        readPairJobTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ReadPairJobView.class, "ReadPairJobView.readPairJobTable.columnModel.title7")); // NOI18N

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
    private javax.swing.JTable readPairJobTable;
    // End of variables declaration//GEN-END:variables


}
