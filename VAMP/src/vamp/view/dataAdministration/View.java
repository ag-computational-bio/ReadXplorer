package vamp.view.dataAdministration;

import vamp.dataAdministration.ViewListenerI;
import vamp.dataAdministration.ModelListenerI;
import vamp.dataAdministration.JobManager;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import vamp.ApplicationController;
import vamp.importer.ReferenceJob;
import vamp.importer.RunJob;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public class View extends javax.swing.JFrame implements ViewI, ModelListenerI {

    private static final long serialVersionUID = 72340125;
    private List<ViewListenerI> listeners;
    private JobManager jobManager;
    private boolean allowClosing;

    /** Creates new form DataAdminFrame */
    public View() {
        initComponents();
        listeners = new ArrayList<ViewListenerI>();
        allowClosing = true;
    }

    public void showOverviewCard() {
        CardLayout c = (CardLayout) jPanel1.getLayout();
        overviewCard1.showGenereateOverview(jobManager.getScheduledRefGenJobs(), jobManager.getScheduledRunJobs(), jobManager.getScheduledTrackJobs());
        c.show(jPanel1, "overview");
    }

    public void showProgressCard(){
        CardLayout c = (CardLayout) jPanel1.getLayout();
        c.show(jPanel1, "progress");
    }

    public void finish(){
        for(ViewListenerI l : listeners){
            l.cancelDataAdmin();
        }
    }

    void showSelectionCard() {
        CardLayout c = (CardLayout) jPanel1.getLayout();
        c.show(jPanel1, "selection");
    }

    void startDeletion() {
        allowClosing = false;
        for(ViewListenerI l : listeners){
            l.startDeletion();
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

        jPanel1 = new javax.swing.JPanel();
        dataAdminPanel1 = new vamp.view.dataAdministration.SelectionCard();
        dataAdminPanel1.setView(this);
        overviewCard1 = new vamp.view.dataAdministration.OverviewCard();
        overviewCard1.setDataAdminView(this);
        progressCard1 = new vamp.view.dataAdministration.ProgressCard();
        progressCard1.setDataAdminView(this);
        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar1.setVisible(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(ApplicationController.APPNAME+ " Datamanagement");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setLayout(new java.awt.CardLayout());
        jPanel1.add(dataAdminPanel1, "selection");
        jPanel1.add(overviewCard1, "overview");
        jPanel1.add(progressCard1, "progress");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13))
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-459)/2, (screenSize.height-431)/2, 459, 431);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(allowClosing){
            for(ViewListenerI l : listeners){
                l.cancelDataAdmin();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Cannot close program while job is running!", "Running job", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_formWindowClosing



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private vamp.view.dataAdministration.SelectionCard dataAdminPanel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private vamp.view.dataAdministration.OverviewCard overviewCard1;
    private vamp.view.dataAdministration.ProgressCard progressCard1;
    // End of variables declaration//GEN-END:variables


    @Override
    public void addDataAdminViewListenerI(ViewListenerI listener) {
        listeners.add(listener);
    }

    @Override
    public void removeDataAdminViewListenerI(ViewListenerI listener) {
        listeners.add(listener);
    }

    @Override
    public void addDataAdminJobManager(JobManager jobmanager){
        this.jobManager = jobmanager;
    }

    @Override
    public void refGenJobAdded(ReferenceJob refGenJob) {
        dataAdminPanel1.refGenJobAdded(refGenJob);
    }

    @Override
    public void runJobAdded(RunJob runJob) {
        dataAdminPanel1.runJobAdded(runJob);
    }

    @Override
    public void trackJobsAdded(TrackJob trackJob) {
        dataAdminPanel1.trackJobAdded(trackJob);
    }


    public void removeRefGenJob(ReferenceJob refGenJob){
        jobManager.removeRefGenJob(refGenJob);
    }

    public void unRemoveRefGenJob(ReferenceJob refGenJob){
        jobManager.unRemoveRefGenJob(refGenJob);
    }

    public void removeRunJob(RunJob runJob){
        jobManager.removeRunJob(runJob);
    }

    public void unRemoveRunJob(RunJob runJob){
        jobManager.unRemoveRunJob(runJob);
    }

    public void removeTrackJob(TrackJob trackJob){
        jobManager.removeTrackJob(trackJob);
    }

    public void unRemoveTrackJob(TrackJob trackJob){
        jobManager.unRemoveTrackJob(trackJob);
    }

    @Override
    public void deselectRefGen(ReferenceJob refGen) {
        dataAdminPanel1.deselectRefGen(refGen);
    }

    @Override
    public void deselectRun(RunJob runJob) {
        dataAdminPanel1.deselectRun(runJob);
    }

    @Override
    public void startingDeletion() {
        jProgressBar1.setVisible(true);
        jProgressBar1.setIndeterminate(true);
        progressCard1.setButtonsEnabled(false);
    }

    @Override
    public void deletionFinished() {
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setVisible(false);
        progressCard1.setButtonsEnabled(true);
        allowClosing = true;
    }

    @Override
    public void updateDeletionStatus(String message) {
        progressCard1.updateDeletionStatus(message);
    }

}
