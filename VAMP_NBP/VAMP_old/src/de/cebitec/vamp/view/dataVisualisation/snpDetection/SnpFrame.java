package de.cebitec.vamp.view.dataVisualisation.snpDetection;

import de.cebitec.vamp.objects.Snp;
import de.cebitec.vamp.ApplicationController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackOptionsPanel;
import java.awt.CardLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author ddoppmeier
 */
public class SnpFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = 24962346;
    private BoundsInfoManager boundsManager;
    private TrackOptionsPanel parent;
    private CardLayout cards;
    private PersistantTrack track;
    private List<Snp> snps = new ArrayList<Snp>();
    /** Creates new form SnpFrame */
    public SnpFrame() {
        initComponents();
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Do not use standard constructor!");
    }

    public SnpFrame(final TrackOptionsPanel parent, BoundsInfoManager boundsManager, PersistantTrack track){
        initComponents();
        this.parent = parent;
        this.boundsManager = boundsManager;
        this.track = track;
        cards = (CardLayout) jPanel1.getLayout();
        this.setTitle(track.getDescription());
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }
            @Override
            public void windowClosing(WindowEvent e) {
                SnpFrame.this.parent.snpDetectionClosed();
            }
            @Override
            public void windowClosed(WindowEvent e) {
            }
            @Override
            public void windowIconified(WindowEvent e) {
            }
            @Override
            public void windowDeiconified(WindowEvent e) {
            }
            @Override
            public void windowActivated(WindowEvent e) {
            }
            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
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
        setupPanel1 = new de.cebitec.vamp.view.dataVisualisation.snpDetection.SetupPanel(this);
        results1 = new de.cebitec.vamp.view.dataVisualisation.snpDetection.Results(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(ApplicationController.APPNAME+" SNP Detection");

        jPanel1.setLayout(new java.awt.CardLayout());
        jPanel1.add(setupPanel1, "setup");
        jPanel1.add(results1, "results");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private de.cebitec.vamp.view.dataVisualisation.snpDetection.Results results1;
    private de.cebitec.vamp.view.dataVisualisation.snpDetection.SetupPanel setupPanel1;
    // End of variables declaration//GEN-END:variables

    public void close() {

    }

    public void showPosition(int position){
        boundsManager.navigatorBarUpdated(position);
    }

    private void detectionDone(){
        results1.showProgressBar(false);
        results1.searchDone();
    }

    public void findSnps(final int num, final int percent) {
        cards.show(jPanel1, "results");

        SwingWorker t = new SwingWorker() {

            @Override
            protected Object doInBackground()  {
                TrackConnector con = ProjectConnector.getInstance().getTrackConnector(track.getId());
                 snps = con.findSNPs(percent, num);
                for(Snp s : snps){
                    results1.addSnp(s);
                }
                return null;
            }

            @Override
            protected void done(){
                SnpFrame.this.detectionDone();
            }
        };
        t.execute();
        results1.showProgressBar(true);
    }

    public ArrayList<Snp> getSNPs(){
        return (ArrayList<Snp>) snps;
    }

}
