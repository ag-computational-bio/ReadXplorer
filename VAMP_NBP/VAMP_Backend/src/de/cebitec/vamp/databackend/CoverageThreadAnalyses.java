package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.VisualisationUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This coverage thread should be used for analyses, but not for visualizing data.
 * The thread carries out the database querries to receive coverage for a certain interval.
 *
 * @author -Rolf Hilker-
 */
public class CoverageThreadAnalyses extends CoverageThread {

    private long trackID2;
    
    /**
     * Thread for retrieving the coverage for a list of tracks either from the
     * db or directly from their mapping files.
     * @param tracks the tracks handled here
     * @param combineTracks true, if more than one track is added and their
     * coverage should be combined in the results
     */
    public CoverageThreadAnalyses(List<PersistantTrack> tracks, boolean combineTracks) {
        super(tracks, combineTracks);
        
        if (tracks.size() == 2) {
            this.trackID2 = tracks.get(1).getId();
        }
    }

    @Override
    public void run() {
        
        try {
            while (!interrupted()) {

                IntervalRequest request = requestQueue.poll();
                CoverageAndDiffResultPersistant currentCov = new CoverageAndDiffResultPersistant(null, null, null, false, 0, 0);
                if (request != null) {
                    if (request.getDesiredData() == Properties.READ_STARTS) {
                        currentCov = this.loadReadStartsAndCoverage(request);
                    } else
                    if (!currentCov.getCoverage().coversBounds(request.getFrom(), request.getTo())) {
                        if (trackID2 != 0) {
                            currentCov = this.loadCoverageDouble(request); //at the moment we only need the complete coverage here
                        } else {
                            if (request.getWhichTrackNeeded() == Properties.NORMAL) {
                                currentCov = this.loadCoverage(request);
                            }
                        }
                    }
                    request.getSender().receiveData(currentCov);
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CoverageThreadAnalyses.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Out of memory")) {
                VisualisationUtils.displayOutOfMemoryError(JOptionPane.getRootFrame());
                this.interrupt();
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
