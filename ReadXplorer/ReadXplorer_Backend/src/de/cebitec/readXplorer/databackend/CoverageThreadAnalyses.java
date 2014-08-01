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
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.Properties;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This coverage thread should be used for analyses, but not for visualizing
 * data. The thread carries out the queries to receive coverage for a
 * certain interval.
 *
 * @author -Rolf Hilker-
 */
public class CoverageThreadAnalyses extends CoverageThread {
    
    /**
     * Thread for retrieving the coverage for a list of tracks from their 
     * mapping files.
     * @param tracks the tracks handled here
     * @param combineTracks true, if more than one track is added and their
     * coverage should be combined in the results
     */
    public CoverageThreadAnalyses(List<PersistantTrack> tracks, boolean combineTracks) {
        super(tracks, combineTracks);
    }

    @Override
    public void run() {
        
        while (!interrupted()) {

            IntervalRequest request = requestQueue.poll();
            CoverageAndDiffResultPersistant currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0), null, null, request);
            if (request != null) {
                if (request.getDesiredData() == Properties.READ_STARTS) {
                    currentCov = this.loadReadStartsAndCoverageMultiple(request);
                } else if (!currentCov.getCoverage().coversBounds(request.getFrom(), request.getTo())) {
                    if (this.getTrackId2() != 0) {
                        currentCov = this.loadCoverageDouble(request); //at the moment we only need the complete coverage here
                    } else if (this.getTrackId() != 0 || this.canQueryCoverage()) {
                        currentCov = this.loadCoverageMultiple(request);
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
    }
}
