package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.databackend.IntervalRequest;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Requests drawing for given feature after TrackViewer is visible.
 * @author denis
 */
public class TrackViewerCompListener extends ComponentAdapter {

    private PersistantFeature currentFeature;
    private TrackViewer trackV;

    public TrackViewerCompListener(PersistantFeature currentFeature, TrackViewer trackV) {
        this.currentFeature = currentFeature;
        this.trackV = trackV;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        //new IntervalRequest
        int startFeature = currentFeature.getStart();
        int stopFeature = currentFeature.getStop();
        ThumbnailCoverageListener covListener = new ThumbnailCoverageListener(trackV);
        trackV.getTrackCon().addCoverageRequest(new IntervalRequest(startFeature, stopFeature, covListener));
    }
}
