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

package de.cebitec.readxplorer.ui.datavisualisation.trackviewer;


import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.basepanel.BasePanel;


/**
 * Class to distinguish MultipleTrackViewers from TrackViewers. This is a
 * MultipleTrackViewers integrating multiple tracks as one data set.
 * <p>
 * @author jwinneba
 */
public class MultipleTrackViewer extends TrackViewer {

    private static final long serialVersionUID = 2L;


    /**
     * Class to distinguish MultipleTrackViewers from TrackViewers. This is a
     * MultipleTrackViewers integrating multiple tracks as one data set.
     * <p>
     * @param boundsManager manager for component bounds
     * @param basePanel     The BasePanel on which the viewer is painted.
     * @param refGen        reference genome
     * @param trackCon      database connection to one track, that is displayed
     * @param combineTracks <code>true</code>, if the coverage of the tracks
     *                      contained in the track connector should be combined,
     *                      <code>false</code> otherwise.
     */
    public MultipleTrackViewer( BoundsInfoManager boundsManager, BasePanel basePanel,
                                PersistentReference refGen, TrackConnector trackCon, boolean combineTracks ) {
        super( boundsManager, basePanel, refGen, trackCon, combineTracks );
    }


}
