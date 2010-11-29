/*
 * ReadFrame.java
 *
 * Created on 15.09.2010, 11:32:37
 */
package de.cebitec.vamp.view.dataVisualisation.readPosition;

import de.cebitec.vamp.objects.Read;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.BlockComponent;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackOptionsPanel;
import java.awt.CardLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author jstraube
 */
public class ReadFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    private BoundsInfoManager boundsManager;
    private TrackOptionsPanel parent;
    private CardLayout cards;
    private PersistantTrack track;
    private BlockComponent block ;
    /** Creates new form ReadFrame */
    public ReadFrame() {
        initComponents();
    }

    public ReadFrame(final TrackOptionsPanel parent, BoundsInfoManager boundsManager, PersistantTrack track) {
        initComponents();
        this.parent = parent;
        this.boundsManager = boundsManager;
        this.track = track;
        cards = (CardLayout) jPanel1.getLayout();
        this.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                ReadFrame.this.parent.readDetecionClosed();
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
        readSearch1 = new de.cebitec.vamp.view.dataVisualisation.readPosition.ReadSearch(this);
        results1 = new de.cebitec.vamp.view.dataVisualisation.readPosition.ReadResults(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Read detection");

        jPanel1.setLayout(new java.awt.CardLayout());
        jPanel1.add(readSearch1, "searchCard");
        jPanel1.add(results1, "resultcard");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 299, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void close() {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private de.cebitec.vamp.view.dataVisualisation.readPosition.ReadSearch readSearch1;
    private de.cebitec.vamp.view.dataVisualisation.readPosition.ReadResults results1;
    // End of variables declaration//GEN-END:variables

    /*
     * the method showPosition is called when a read position is selected
     * the viewer will update their view to this position
     */
    public void showPosition(int position) {
        boundsManager.navigatorBarUpdated(position);
    }

    /*
     * the methode detectionDone is called when all data from
     * the database is loaded
     */
    private void detectionDone() {
        results1.showProgressBar(false);
        results1.searchDone();
    }

    /*
     * this Method is called to detect all reads
     * with the common readnames it should run in
     * background so that man can work with the viewer
     */
    public void findReads(final String read) {
        cards.show(jPanel1, "resultcard");
        SwingWorker t = new SwingWorker() {

            @Override
            protected Object doInBackground() {
                TrackConnector con = ProjectConnector.getInstance().getTrackConnector(track.getId());
                List<Read> reads = con.findReads(read);
                for (Read r : reads) {
                    results1.addRead(r);
                }
                return null;
            }

            @Override
            protected void done() {
                ReadFrame.this.detectionDone();
            }
        };
        t.execute();
        results1.showProgressBar(true);
    }
    
}
