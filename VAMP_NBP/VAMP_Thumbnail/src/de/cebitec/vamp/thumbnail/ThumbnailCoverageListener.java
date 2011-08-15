package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.databackend.CoverageThreadListener;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfo;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;

/**
 * Listens for coverage answer from CoverageThread.
 * @author denis
 */
class ThumbnailCoverageListener implements CoverageThreadListener{
    private TrackViewer trackViewer;

    public ThumbnailCoverageListener(TrackViewer trackViewer){
        this.trackViewer = trackViewer;
    }

   @Override
    public void receiveCoverage(PersistantCoverage coverage) {
        //Grenzen neu malen
        int middle = coverage.getLeftBound() + ((coverage.getRightBound() - coverage.getLeftBound()) / 2);
        int width = coverage.getRightBound() - coverage.getLeftBound();
        trackViewer.receiveCoverage(coverage);
        trackViewer.updateLogicalBounds(new BoundsInfo(coverage.getLeftBound(), coverage.getRightBound(), middle, 1, width));
    }

}
