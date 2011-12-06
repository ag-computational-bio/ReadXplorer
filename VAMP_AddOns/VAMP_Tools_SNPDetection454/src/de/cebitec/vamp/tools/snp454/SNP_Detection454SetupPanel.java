/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SNP_Detection454SetupPanel.java
 *
 * Created on 03.06.2011, 14:48:20
 */
package de.cebitec.vamp.tools.snp454;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.dataObjects.Snp454;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author msmith
 */
public class SNP_Detection454SetupPanel extends javax.swing.JPanel {
    
    
    private static final long serialVersionUID = 1L;
    private TrackConnector con;
    private List<Snp454> snps;
    
    public static final String PROP_SNPS_LOADED = "snpsLoaded";

    /** Creates new form SNP_Detection454SetupPanel */
    public SNP_Detection454SetupPanel() {
        initComponents();
        snps = new ArrayList<Snp454>();
    }
    
    private class Snp454Worker extends SwingWorker<List<Snp454>, Object> {

        private int percent;
        private int num;
        private ProgressHandle ph;

        Snp454Worker(int percent, int num) {
            this.percent = percent;
            this.num = num;
            this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(Snp454Worker.class, "MSG_SNP454Worker.progress.name"));
        }

        @Override
        protected List<Snp454> doInBackground() {
            CentralLookup.getDefault().add(this);

            ph.start();

            //methods handling this snp detection were deleted, since the new
            //snp detection does the same, if back included it has to adapt to 
            //the new snp detection
//            snps = con.findSNPs454(percent, num);
            return snps;
        }

        @Override
        protected void done() {
            CentralLookup.getDefault().remove(this);

            ph.finish();
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        perVar = new javax.swing.JSpinner();
        absNum = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SNP_Detection454SetupPanel.class, "SNP_Detection454SetupPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SNP_Detection454SetupPanel.class, "SNP_Detection454SetupPanel.jLabel2.text")); // NOI18N

        perVar.setValue(45);

        absNum.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        absNum.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        absNum.setText(org.openide.util.NbBundle.getMessage(SNP_Detection454SetupPanel.class, "SNP_Detection454SetupPanel.absNum.text")); // NOI18N

        searchButton.setText(org.openide.util.NbBundle.getMessage(SNP_Detection454SetupPanel.class, "SNP_Detection454SetupPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(SNP_Detection454SetupPanel.class, "SNP_Detection454SetupPanel.jTextArea1.text")); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(8, 8, 8)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(absNum, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                            .addComponent(perVar, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)))
                    .addComponent(searchButton))
                .addGap(22, 22, 22))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(perVar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(absNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(searchButton)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        String absN = absNum.getText();
        if(isValidNumer(absN)){
            final int num = Integer.parseInt(absN);
            final int percent = (Integer) perVar.getValue();

            RequestProcessor rp = new RequestProcessor("SNP454 Threads", 2);
            final Task snpTask = rp.post(new Snp454Worker(percent, num));
            snpTask.addTaskListener(new TaskListener() {

                @Override
                public void taskFinished(org.openide.util.Task task) {
                    firePropertyChange(PROP_SNPS_LOADED, null, snps);
                    snpTask.removeTaskListener(this);
                    searchButton.setEnabled(true);
                }
            });
            searchButton.setEnabled(false);
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SNP_Detection454SetupPanel.class, "MSG_SNP_Detection454SetupPanel.error"), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

    }                                            

    private boolean isValidNumer(String num){
        try{
            int tmp = Integer.parseInt(num);
            if(tmp > 0){
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException ex){
            return false;
        } 
    }//GEN-LAST:event_searchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField absNum;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JSpinner perVar;
    private javax.swing.JButton searchButton;
    // End of variables declaration//GEN-END:variables

public List<Snp454> getSnps() {
        return snps;
    }

    public void setCon(TrackConnector con) {
        this.con = con;
    }

}
