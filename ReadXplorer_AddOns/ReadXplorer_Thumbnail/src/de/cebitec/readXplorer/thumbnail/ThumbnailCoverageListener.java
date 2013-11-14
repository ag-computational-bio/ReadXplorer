package de.cebitec.readXplorer.thumbnail;

import de.cebitec.readXplorer.databackend.ThreadListener;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfo;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.TrackViewer;

/**
 * Listens for coverage answer from CoverageThread.
 * @author denis
 */
class ThumbnailCoverageListener implements ThreadListener {
    private TrackViewer trackViewer;

    public ThumbnailCoverageListener(TrackViewer trackViewer) {
        this.trackViewer = trackViewer;
    }

   @Override
    public void receiveData(Object resultData) {
       if (resultData instanceof CoverageAndDiffResultPersistant) {
           //Grenzen neu malen
           CoverageAndDiffResultPersistant coverageResult = (CoverageAndDiffResultPersistant) resultData;
           PersistantCoverage coverage = coverageResult.getCoverage();
           int middle = coverage.getLeftBound() + ((coverage.getRightBound() - coverage.getLeftBound()) / 2);
           int width = coverage.getRightBound() - coverage.getLeftBound();
           trackViewer.updateLogicalBounds(new BoundsInfo(coverage.getLeftBound(), coverage.getRightBound(), middle, 1, width));
           trackViewer.receiveData(coverageResult);
       }
    }

    @Override
    public void notifySkipped() {
        //do nothing 
    }

}
