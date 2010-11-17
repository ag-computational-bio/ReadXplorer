

/*
 * Results.java
 *
 * Created on 15.09.2010, 10:51:20
 */

package vamp.view.dataVisualisation.readPosition;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jstraube
 */
public class ReadResults extends javax.swing.JPanel implements ListSelectionListener{

    private ReadFrame parent ;
    /** Creates new form Results */
    public ReadResults() {
        initComponents();
    }

    public ReadResults(ReadFrame parent){
        initComponents();
        this.parent = parent;
        DefaultListSelectionModel model = (DefaultListSelectionModel) readname.getSelectionModel();
        model.addListSelectionListener((ListSelectionListener) this);
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
        readname = new javax.swing.JTable();
        jProgressBar1 = new javax.swing.JProgressBar();

        readname.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Readname", "Position", "Errors", "Is best mapping"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(readname);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable readname;
    // End of variables declaration//GEN-END:variables
    public void showProgressBar(boolean b) {
        if(b){
            jProgressBar1.setVisible(true);
            jProgressBar1.setIndeterminate(true);
        } else {
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setVisible(false);
        }
    }

    void searchDone() {
        readname.setEnabled(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        DefaultListSelectionModel model = (DefaultListSelectionModel) readname.getSelectionModel();
        int selectedView = model.getLeadSelectionIndex();
        int selectedModel = readname.convertRowIndexToModel(selectedView);
        int position = (Integer) readname.getModel().getValueAt(selectedModel, 1);
        parent.showPosition(position);
    }

    void addRead(Read r) {
        DefaultTableModel model = (DefaultTableModel) readname.getModel();
        Object[] rowData = new Object[5];
        rowData[0] = r.getReadname();
        rowData[1] = r.getPosition();
        rowData[2] = r.getErrors();
        rowData[3] = r.getisBestMapping();
        model.addRow(rowData);
    }
}
