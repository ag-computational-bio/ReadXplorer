package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.databackend.IntervalRequest;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Requests drawing for given annotation after TrackViewer is visible.
 * @author denis
 */
public class TrackViewerCompListener extends ComponentAdapter {

    private PersistantAnnotation currentAnnotation;
    private TrackViewer trackV;

    public TrackViewerCompListener(PersistantAnnotation currentAnnotation, TrackViewer trackV) {
        this.currentAnnotation = currentAnnotation;
        this.trackV = trackV;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        //new IntervalRequest
        int startAnnotation = currentAnnotation.getStart();
        int stopAnnotation = currentAnnotation.getStop();
        //currentTrack = (TrackViewer) trackPanel.getViewer();
        ThumbnailCoverageListener covListener = new ThumbnailCoverageListener(trackV);
        trackV.getTrackCon().addCoverageRequest(new IntervalRequest(startAnnotation, stopAnnotation, covListener, Properties.COMPLETE_COVERAGE));
    }
}
