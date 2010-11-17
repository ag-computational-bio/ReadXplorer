package vamp.view.importer;

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import vamp.ApplicationController;
import vamp.importer.ImporterDataModelListenerI;
import vamp.importer.ImporterViewListenerI;
import vamp.importer.JobManagerI;
import vamp.importer.ReferenceJob;
import vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public class ImporterViewFrame extends javax.swing.JFrame implements ImporterViewI, ImporterDataModelListenerI {

    private static final long serialVersionUID =  12314313;
    private static String[] cardnames = {"setup", "overview", "progress"};
    private int cardIndex;
    private List<ImporterViewListenerI> importerViewListener;
    private JobManagerI jobManager;
    private boolean allowClosing;

    /** Creates new form ImporterViewFrame */
    public ImporterViewFrame() {
        super(ApplicationController.APPNAME+ " Importer");
        importerViewListener = new ArrayList<ImporterViewListenerI>();
        initComponents();
        cardIndex = 0;
        allowClosing = true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentpane = new javax.swing.JPanel();
        cardPanel = new javax.swing.JPanel();
        setupImportCard1 = new vamp.view.importer.ImportSetupCard(this);
        importOverviewCard1 = new vamp.view.importer.ImportOverviewCard();
        importProgressCard1 = new vamp.view.importer.ImportProgressCard();
        progressBar = new javax.swing.JProgressBar();
        progressBar.setVisible(false);
        nextButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cardPanel.setLayout(new java.awt.CardLayout());
        cardPanel.add(setupImportCard1, "setup");
        cardPanel.add(importOverviewCard1, "overview");
        cardPanel.add(importProgressCard1, "progress");

        nextButton.setText("Next");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        previousButton.setText("Back");
        previousButton.setEnabled(false);
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout contentpaneLayout = new javax.swing.GroupLayout(contentpane);
        contentpane.setLayout(contentpaneLayout);
        contentpaneLayout.setHorizontalGroup(
            contentpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentpaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contentpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contentpaneLayout.createSequentialGroup()
                        .addComponent(previousButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextButton))
                    .addComponent(cardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE))
                .addContainerGap())
        );
        contentpaneLayout.setVerticalGroup(
            contentpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contentpaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(contentpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextButton)
                    .addComponent(previousButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-615)/2, (screenSize.height-430)/2, 615, 430);
    }// </editor-fold>//GEN-END:initComponents

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        CardLayout c = (CardLayout) cardPanel.getLayout();
        cardIndex++;

        // import setup is in progress. show the correct card and enable suitable buttons
        if(cardIndex >= 0 && cardIndex <= 2){
            c.show(cardPanel, cardnames[cardIndex]);
            setButtonStatus(cardIndex);

            if(cardIndex == 1){
                //overview card is shown
                importOverviewCard1.showOverview(jobManager.getRefGenJobList(), jobManager.getTrackJobListRun());
            } else if(cardIndex == 2){
                //import progress card is shown
                for(ImporterViewListenerI l : importerViewListener){
                    allowClosing = false;
                    l.startImport();
                }
            }

        // import done
        } else if (cardIndex == 3){
            for(ImporterViewListenerI l : importerViewListener){
                l.cancelImport();
            }
        }

    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        CardLayout c = (CardLayout) cardPanel.getLayout();
        cardIndex--;
        if(cardIndex >= 0 && cardIndex < 2){
            c.show(cardPanel, cardnames[cardIndex]);
            setButtonStatus(cardIndex);
        }

    }//GEN-LAST:event_previousButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(allowClosing){
            for(ImporterViewListenerI l : importerViewListener){
                l.cancelImport();
            }
        } else {
            JOptionPane.showMessageDialog(this, "A running import process cannot be interrupted, to prevent loss of data!\n" +
                    "Please wait until import has finished!", "Working import process", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_formWindowClosing

    private void setButtonStatus(int card){
        if(card == 0){
            previousButton.setEnabled(false);
            nextButton.setText("Next");
        } else if(card == 1){
            previousButton.setEnabled(true);
            nextButton.setText("Import");
        } else if(card == 2){
            previousButton.setEnabled(false);
            nextButton.setText("Close");
        }

    }


    @Override
    public void addImporterViewListener(ImporterViewListenerI listener) {
        importerViewListener.add(listener);
    }

    @Override
    public void removeImporterViewListener(ImporterViewListenerI listener){
        importerViewListener.remove(listener);
    }

    @Override
    public void setJobManager(JobManagerI taskManager){
        this.jobManager = taskManager;
    }

    @Override
    public void removeJobManager(JobManagerI taskManager){
        this.jobManager = null;
    }

    @Override
    public void importFinished() {
        allowClosing = true;
        this.setEnabled(true);
        nextButton.setEnabled(true);
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }

    public void showNewRefGenDialog(){
        new NewReferenceDialog(this, jobManager).setVisible(true);
    }

    public void showNewTrackDialog(){
        new NewTrackDialog(this, jobManager).setVisible(true);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cardPanel;
    private javax.swing.JPanel contentpane;
    private vamp.view.importer.ImportOverviewCard importOverviewCard1;
    private vamp.view.importer.ImportProgressCard importProgressCard1;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JProgressBar progressBar;
    private vamp.view.importer.ImportSetupCard setupImportCard1;
    // End of variables declaration//GEN-END:variables




    @Override
    public void trackJobAddedRun(TrackJobs trackJob) {
        setupImportCard1.trackJobAdded(trackJob);
    }

    @Override
    public void trackJobRemovedRun(TrackJobs trackJob) {
        setupImportCard1.trackJobRemoved(trackJob);
    }

    @Override
    public void refGenJobAdded(ReferenceJob refGenJob) {
        setupImportCard1.refGenJobAdded(refGenJob);
    }

    @Override
    public void refGenJobRemoved(ReferenceJob refGenJob) {
        setupImportCard1.refGenJobRemoved(refGenJob);
    }

    public void removeTrackJob(TrackJobs trackJob){
        jobManager.removeTrackTask(trackJob);
    }


    public void removeRefGenJob(ReferenceJob refGenJob){
        if (refGenJob.hasRegisteredTrackswithoutrRunJob()) {
            JOptionPane.showMessageDialog(this, "Connot mark selected object for deletion. Please resolve dependencies first!", "Unresolved dependencies", JOptionPane.ERROR_MESSAGE);
        } else {
            jobManager.removeRefGenTask(refGenJob);
        }
    }

    @Override
    public void udateImportStatus(String actualMessage) {
        importProgressCard1.updateImportStatus(actualMessage);
    }

    @Override
    public void startingImport(){
        this.setEnabled(false);
        nextButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
    }

}
