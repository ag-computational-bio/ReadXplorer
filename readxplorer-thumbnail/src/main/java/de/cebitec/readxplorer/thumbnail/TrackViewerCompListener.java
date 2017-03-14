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

package de.cebitec.readxplorer.thumbnail;


import de.cebitec.readxplorer.databackend.IntervalRequest;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.ui.datavisualisation.trackviewer.TrackViewer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


/**
 * Requests drawing for given feature after TrackViewer is visible.
 * <p>
 * @author denis
 */
public class TrackViewerCompListener extends ComponentAdapter {

    private final PersistentFeature currentFeature;
    private final TrackViewer trackV;


    public TrackViewerCompListener( PersistentFeature currentFeature, TrackViewer trackV ) {
        this.currentFeature = currentFeature;
        this.trackV = trackV;
    }


    @Override
    public void componentResized( ComponentEvent e ) {
        //new IntervalRequest
        int startFeature = currentFeature.getStart();
        int stopFeature = currentFeature.getStop();
        ThumbnailCoverageListener covListener = new ThumbnailCoverageListener( trackV );
        trackV.getTrackCon().addCoverageRequest( new IntervalRequest( startFeature, stopFeature, trackV.getReference().getActiveChromId(), covListener, false ) );
    }


}
