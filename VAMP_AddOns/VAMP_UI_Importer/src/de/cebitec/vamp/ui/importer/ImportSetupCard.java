package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.api.objects.NewJobDialogI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.SeqPairJobContainer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.ui.importer.actions.ImportWizardAction;
import de.cebitec.vamp.view.dialogMenus.ImportTrackBasePanel;
import java.awt.Component;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Allows to create new jobs to import and displays the summary of all currently
 * created jobs.
 *
 * @author ddoppmeier, rhilker
 */
public class ImportSetupCard extends javax.swing.JPanel {

    private static final long serialVersionUID = 127732323;
    private boolean canImport;
    public static final String PROP_HAS_JOBS = "hasJobs";
    public static final String PROP_JOB_SELECTED = "jobSelected";
    private int trackID = 0;

    /**
     * Allows to create new jobs to import and displays the summary of all
     * currently created jobs.
     */
    public ImportSetupCard() {
        initComponents();
        refJobView.addPropertyChangeListener(this.getJobPropListener());
        trackJobView.addPropertyChangeListener(this.getJobPropListener());
        seqPairTrackJobsView.addPropertyChangeListener(this.getJobPropListener());
        trackID = ProjectConnector.getInstance().getLatestTrackId();
    }

    private PropertyChangeListener getJobPropListener() {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PROP_JOB_SELECTED)) {
                    if ((Boolean) evt.getNewValue()) {
                        removeJob.setEnabled(true);
                    } else {
                        removeJob.setEnabled(false);
                    }
                } else if (evt.getPropertyName().equals(PROP_HAS_JOBS)) {
                    setCanImport((Boolean) evt.getNewValue());
                }
            }
        };
    }

    void setRemoveButtonEnabled(boolean b) {
        removeJob.setEnabled(b);
    }

    public void setCanImport(boolean canOrcannot) {
        canImport = canOrcannot;
        firePropertyChange(ImportWizardAction.PROP_CAN_IMPORT, null, canImport);
    }

    public List<ReferenceJob> getRefJobList() {
        return refJobView.getJobs();
    }

    public List<TrackJob> getTrackJobList() {
        return trackJobView.getJobs();
    }

    public List<SeqPairJobContainer> getSeqPairTrackJobList() {
        return seqPairTrackJobsView.getJobs();
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
        refJobView = new de.cebitec.vamp.ui.importer.RefJobView();
        trackJobView = new de.cebitec.vamp.ui.importer.TrackJobView();
        seqPairTrackJobsView = new de.cebitec.vamp.ui.importer.SeqPairJobView();
        addJob = new javax.swing.JButton();
        removeJob = new javax.swing.JButton();

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jTabbedPane1.addTab("References", refJobView);
        jTabbedPane1.addTab("Tracks", trackJobView);

        final de.cebitec.vamp.parser.mappings.SeqPairClassifierI seqPairCalculator = Lookup.getDefault().lookup(de.cebitec.vamp.parser.mappings.SeqPairClassifierI.class);
        if (seqPairCalculator != null) {
            jTabbedPane1.addTab("Read Pair Tracks", seqPairTrackJobsView);
        }

        addJob.setText(org.openide.util.NbBundle.getMessage(ImportSetupCard.class, "ImportSetupCard.button.newJob")); // NOI18N
        addJob.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJobActionPerformed(evt);
            }
        });

        removeJob.setText(org.openide.util.NbBundle.getMessage(ImportSetupCard.class, "ImportSetupCard.button.removeJob")); // NOI18N
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
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(333, Short.MAX_VALUE)
                .addComponent(removeJob)
                .addGap(7, 7, 7)
                .addComponent(addJob)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addJob, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeJob, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Automatically uses the correct track id for the trackjobs which are created here.
     * @param evt 
     */
    private void addJobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJobActionPerformed
        Component c = jTabbedPane1.getSelectedComponent();
        if (c != null) {
            String title;
            NewJobDialogI dialogPane;

            // initialise NewJobDialog parameters
            if (c instanceof RefJobView) {
                title = NbBundle.getMessage(ImportSetupCard.class, "TTL_ImportSetupCard.dialog.title.reference");
                dialogPane = new NewReferenceDialogPanel();
            } else if (c instanceof SeqPairJobView) {
                title = NbBundle.getMessage(ImportSetupCard.class, "TTL_ImportSetupCard.dialog.title.seqPairTrack");
                dialogPane = new NewSeqPairTracksDialogPanel();
                ((NewSeqPairTracksDialogPanel) dialogPane).setReferenceJobs(refJobView.getJobs());
            } else if (c instanceof TrackJobView) {
                title = NbBundle.getMessage(ImportSetupCard.class, "TTL_ImportSetupCard.dialog.title.track");
                dialogPane = new NewTrackDialogPanel();
                ((NewTrackDialogPanel) dialogPane).setReferenceJobs(this.refJobView.getJobs());
            } else {
                title = null;
                dialogPane = null;
            }

            // create dialog
            DialogDescriptor newDialog = new DialogDescriptor(dialogPane, title, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(newDialog);
            dialog.setVisible(true);

            // keep the dialog open until the required info is provided or the dialog is canceled
            while (newDialog.getValue() == DialogDescriptor.OK_OPTION && !dialogPane.isRequiredInfoSet()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ImportSetupCard.class, "MSG_ImportSetupCard.dialog.fillout"), NotifyDescriptor.INFORMATION_MESSAGE));
                dialog.setVisible(true);
            }

            // do dialog specific stuff
            if (newDialog.getValue() == DialogDescriptor.OK_OPTION && dialogPane.isRequiredInfoSet()) {
                if (dialogPane instanceof NewReferenceDialogPanel) {
                    NewReferenceDialogPanel nrdp = (NewReferenceDialogPanel) dialogPane;
                    refJobView.add(nrdp.getReferenceJob());
                
                } else if (dialogPane instanceof NewSeqPairTracksDialogPanel) {
                    NewSeqPairTracksDialogPanel seqPairPane = (NewSeqPairTracksDialogPanel) dialogPane;
                    
                    if (seqPairPane.useMultipleImport()) {
                        List<File> mappingFiles1 = seqPairPane.getMappingFiles();
                        List<File> mappingFiles2 = seqPairPane.getMappingFiles2();

                        int largestSize = mappingFiles1.size() > mappingFiles2.size() ? mappingFiles1.size() : mappingFiles2.size();
                        
                        for (int i = 0; i < largestSize; ++i) {
                            File file1 = null;
                            File file2 = null;
                            if (i < mappingFiles1.size()) {
                                file1 = mappingFiles1.get(i);
                            }
                            if (i < mappingFiles2.size()) {
                                file2 = mappingFiles2.get(i);
                            }
                            this.addSeqPairJobToList(seqPairPane, file1, file2);
                        }
                    } else {
                        this.addSeqPairJobToList(seqPairPane, seqPairPane.getMappingFile1(), seqPairPane.getMappingFile2());
                    }

                } else if (dialogPane instanceof NewTrackDialogPanel) {
                    NewTrackDialogPanel newTrackPanel = (NewTrackDialogPanel) dialogPane;
                    
                    for (File mappingFile : newTrackPanel.getMappingFiles()) {

                        TrackJob trackJob = this.createTrackJob(newTrackPanel, mappingFile);
                        trackJob.setIsSorted(newTrackPanel.isFileSorted());
                        trackJobView.add(trackJob);
                    }
                } else if (dialogPane instanceof NewPositionTableDialog) {
                    NewPositionTableDialog posTableDialog = (NewPositionTableDialog) dialogPane;
                    ReferenceJob refJob = posTableDialog.getReferenceJob();
                    TrackJob parentTrackJob = posTableDialog.getParentTrack();
                    
                    TrackJob trackJob = new TrackJob(parentTrackJob.getID(), 
                            parentTrackJob.isDbUsed(), 
                            posTableDialog.getMappingFile(), 
                            "", 
                            refJob, 
                            posTableDialog.getCurrentParser(), 
                            true, 
                            new Timestamp(System.currentTimeMillis()));
                    
                    refJob.registerTrackWithoutRunJob(trackJob);
                }

            }
        } else {
            // do nothing
        }
}//GEN-LAST:event_addJobActionPerformed

    private void removeJobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeJobActionPerformed
        Component c = jTabbedPane1.getSelectedComponent();
        if (c == null) {
        } else if (c instanceof RefJobView) {
            ReferenceJob job = refJobView.getSelectedItem();
            if (job.hasRegisteredTrackswithoutrRunJob()) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ImportSetupCard.class, "MSG_ImportSetupCard.dialog.problem.dependency"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                refJobView.remove(job);
            }
        } else if (c instanceof SeqPairJobView) {
            seqPairTrackJobsView.remove(seqPairTrackJobsView.getSelectedItem());
        } else if (c instanceof TrackJobView) {
            trackJobView.remove(trackJobView.getSelectedItem());
        }
    }//GEN-LAST:event_removeJobActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        Component c = jTabbedPane1.getSelectedComponent();
        boolean isSelected = false;
        if (c instanceof RefJobView) {
            if (refJobView.IsRowSelected()) {
                isSelected = true;
            }
        } else if (c instanceof SeqPairJobView) {
            if (seqPairTrackJobsView.isRowSelected()) {
                isSelected = true;
            }
        } else if (c instanceof TrackJobView) {
            if (trackJobView.IsRowSelected()) {
                isSelected = true;
            }
        }

        if (isSelected) {
            setRemoveButtonEnabled(true);
        } else {
            setRemoveButtonEnabled(false);
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addJob;
    private javax.swing.JTabbedPane jTabbedPane1;
    private de.cebitec.vamp.ui.importer.RefJobView refJobView;
    private javax.swing.JButton removeJob;
    private de.cebitec.vamp.ui.importer.SeqPairJobView seqPairTrackJobsView;
    private de.cebitec.vamp.ui.importer.TrackJobView trackJobView;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates and adds a sequence pair job to the list of sequence pair jobs.
     * @param seqPairPane 
     */
    private void addSeqPairJobToList(NewSeqPairTracksDialogPanel seqPairPane, File mappingFile1, File mappingFile2) {

        if (mappingFile1 == null) {
            mappingFile1 = mappingFile2;
            mappingFile2 = null;
        }
        
        TrackJob trackJob1 = this.createTrackJob(seqPairPane, mappingFile1);
        TrackJob trackJob2 = null;
        if (mappingFile2 != null) {
            trackJob2 = this.createTrackJob(seqPairPane, mappingFile2);
        }

        this.seqPairTrackJobsView.add(new SeqPairJobContainer(trackJob1, trackJob2,
                seqPairPane.getDistance(), seqPairPane.getDeviation(), seqPairPane.getOrientation()));
    }
    
    /**
     * Creates a new track job for a sequence pair import.
     * @param importPanel panel with details
     * @param mappingFile mapping file to add to the track job
     * @param useMultipleImport true, if multiple files were selected in the panel
     * @return the new track job
     */
    private TrackJob createTrackJob(ImportTrackBasePanel importPanel, File mappingFile) {
        ReferenceJob refJob = importPanel.getReferenceJob();
        TrackJob trackJob = new TrackJob(trackID++, importPanel.isDbUsed(),
                mappingFile,
                importPanel.useMultipleImport() && mappingFile != null ? mappingFile.getName() : importPanel.getTrackName(),
                importPanel.getReferenceJob(),
                importPanel.getCurrentParser(),
                importPanel.isAlreadyImported(),
                new Timestamp(System.currentTimeMillis()));
        refJob.registerTrackWithoutRunJob(trackJob);
        return trackJob;
    }
}
