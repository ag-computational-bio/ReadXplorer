package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.ui.importer.actions.ImportWizardAction;
import java.awt.Component;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ddoppmeier
 */
public class ImportSetupCard extends javax.swing.JPanel {

    private static final long serialVersionUID = 127732323;

    private boolean canImport;

    /** Creates new form SetupImportCard */
    public ImportSetupCard() {
        initComponents();
        refJobView1.addPropertyChangeListener(RefJobView.PROP_JOB_SELECTED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ((Boolean) evt.getNewValue()){
                    removeJob.setEnabled(true);
                }
                else {
                    removeJob.setEnabled(false);
                }
            }
        });
        refJobView1.addPropertyChangeListener(RefJobView.PROP_HAS_JOBS, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setCanImport((Boolean) evt.getNewValue());
            }
        });
        trackJobView1.addPropertyChangeListener(TrackJobView.PROP_JOB_SELECTED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ((Boolean) evt.getNewValue()){
                    removeJob.setEnabled(true);
                }
                else{
                    removeJob.setEnabled(false);
                }
            }
        });
        trackJobView1.addPropertyChangeListener(RefJobView.PROP_HAS_JOBS, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setCanImport((Boolean) evt.getNewValue());
            }
        });
    }

    void refGenJobAdded(ReferenceJob refGenJob) {
        refJobView1.add(refGenJob);
    }

    void refGenJobRemoved(ReferenceJob refGenJob) {
        refJobView1.remove(refGenJob);
    }

    void setRemoveButtonEnabled(boolean b) {
        removeJob.setEnabled(b);
    }

    void trackJobAdded(TrackJobs trackJob) {
        trackJobView1.add(trackJob);
    }

    void trackJobRemoved(TrackJobs trackJob) {
        trackJobView1.remove(trackJob);
    }

    public void setCanImport(boolean canOrcannot){
        canImport = canOrcannot;
        firePropertyChange(ImportWizardAction.PROP_CAN_IMPORT, null, canImport);
    }

    public List<ReferenceJob> getRefJobList(){
        return refJobView1.getJobs();
    }

    public List<TrackJobs> getTrackJobList(){
        return trackJobView1.getJobs();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportSetupCard.class, "CTL_ImportSetupCard.name");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        refJobView1 = new de.cebitec.vamp.ui.importer.RefJobView();
        trackJobView1 = new de.cebitec.vamp.ui.importer.TrackJobView();
        newJob = new javax.swing.JButton();
        removeJob = new javax.swing.JButton();

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jTabbedPane1.addTab("References", refJobView1);
        jTabbedPane1.addTab("Tracks", trackJobView1);

        newJob.setText("Add");
        newJob.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newJobActionPerformed(evt);
            }
        });

        removeJob.setText("remove");
        removeJob.setEnabled(false);
        removeJob.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeJobActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(304, Short.MAX_VALUE)
                .addComponent(removeJob)
                .addGap(7, 7, 7)
                .addComponent(newJob)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newJob, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeJob, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newJobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newJobActionPerformed
        Component c = jTabbedPane1.getSelectedComponent();
        if(c == null){
        } else if(c instanceof RefJobView) {
            NewReferenceDialogPanel nrdp = new NewReferenceDialogPanel();
            DialogDescriptor refGenDialog = new DialogDescriptor(nrdp, "Reference Dialog", true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.CANCEL_OPTION, null);
            Dialog diaDisp = DialogDisplayer.getDefault().createDialog(refGenDialog);
            diaDisp.setVisible(true);

            while(refGenDialog.getValue() == DialogDescriptor.OK_OPTION && !nrdp.isRequiredInfoSet()){
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Please fill out the complete form!", NotifyDescriptor.INFORMATION_MESSAGE));
                diaDisp.setVisible(true);
            }
            if (refGenDialog.getValue() == DialogDescriptor.OK_OPTION && nrdp.isRequiredInfoSet()){
                refJobView1.add(new ReferenceJob(null, nrdp.getReferenceFile(), nrdp.getParser(), nrdp.getDescription(), nrdp.getReferenceName(), new Timestamp(System.currentTimeMillis())));
            }
        } else if(c instanceof TrackJobView){
            NewTrackDialogPanel ntdp = new NewTrackDialogPanel();
            ntdp.setReferenceJobs(refJobView1.getJobs());
            DialogDescriptor trackDialog = new DialogDescriptor(ntdp, "Track Dialog", true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.CANCEL_OPTION, null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(trackDialog);
            dialog.setVisible(true);

            while(trackDialog.getValue() == DialogDescriptor.OK_OPTION && !ntdp.isRequiredInfoSet()){
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Please fill out the complete form!", NotifyDescriptor.INFORMATION_MESSAGE));
                dialog.setVisible(true);
            }
            if (trackDialog.getValue() == DialogDescriptor.OK_OPTION && ntdp.isRequiredInfoSet()){
                ReferenceJob refJob = ntdp.getReferenceJob();
                TrackJobs trackJob = new TrackJobs(null, ntdp.gettMappingFile(), ntdp.getDescription(), refJob, ntdp.getParser(), new Timestamp(System.currentTimeMillis()));
                refJob.registerTrackWithoutRunJob(trackJob);
                trackJobView1.add(trackJob);
            }
        }
}//GEN-LAST:event_newJobActionPerformed

    private void removeJobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeJobActionPerformed
        Component c = jTabbedPane1.getSelectedComponent();
        if(c == null){
        } else if(c instanceof RefJobView) {
            ReferenceJob job = refJobView1.getSelectedItem();
            if (job.hasRegisteredTrackswithoutrRunJob()){
                NotifyDescriptor nd = new NotifyDescriptor.Message("Cannot mark selected object for deletion. Please resolve dependencies first!", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
            else{
                refJobView1.remove(job);
            }
        } else if(c instanceof TrackJobView){
            trackJobView1.remove(trackJobView1.getSelectedItem());
        }
    }//GEN-LAST:event_removeJobActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        Component c = jTabbedPane1.getSelectedComponent();
        boolean isSelected = false;
        if(c instanceof RefJobView){
            if(refJobView1.IsRowSelected()){
                isSelected = true;
            }
        }
        else if (c instanceof TrackJobView){
            if (trackJobView1.IsRowSelected()){
                isSelected = true;
            }
        }

        if(isSelected){
            setRemoveButtonEnabled(true);
        } else{
            setRemoveButtonEnabled(false);
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton newJob;
    private de.cebitec.vamp.ui.importer.RefJobView refJobView1;
    private javax.swing.JButton removeJob;
    private de.cebitec.vamp.ui.importer.TrackJobView trackJobView1;
    // End of variables declaration//GEN-END:variables

}
