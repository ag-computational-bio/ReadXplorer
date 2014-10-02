/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.thumbnail;

import de.cebitec.readXplorer.databackend.ThreadListener;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResult;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
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
       if (resultData instanceof CoverageAndDiffResult) {
           //Grenzen neu malen
           CoverageAndDiffResult coverageResult = (CoverageAndDiffResult) resultData;
           CoverageManager coverage = coverageResult.getCovManager();
           int middle = coverage.getLeftBound() + ((coverage.getRightBound() - coverage.getLeftBound()) / 2);
           int width = coverage.getRightBound() - coverage.getLeftBound();
           trackViewer.updateLogicalBounds(new BoundsInfo(coverage.getLeftBound(), 
                   coverage.getRightBound(), 
                   middle, 
                   1, 
                   trackViewer.getReference().getActiveChromId(), 
                   width));
           trackViewer.receiveData(coverageResult);
       }
    }

    @Override
    public void notifySkipped() {
        //do nothing 
    }

}
