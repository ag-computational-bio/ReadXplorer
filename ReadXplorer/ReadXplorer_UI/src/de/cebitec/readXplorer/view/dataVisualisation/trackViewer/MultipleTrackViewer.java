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
package de.cebitec.readXplorer.view.dataVisualisation.trackViewer;

import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;

/**
 *
 * @author jwinneba
 */
public class MultipleTrackViewer extends TrackViewer {

    private static final long serialVersionUID = 2L;

    public MultipleTrackViewer(BoundsInfoManager boundsManager, BasePanel basePanel, 
            PersistentReference refGen, TrackConnector trackCon, boolean combineTracks) {
        super(boundsManager, basePanel, refGen, trackCon, combineTracks);
    }
    
    

}
