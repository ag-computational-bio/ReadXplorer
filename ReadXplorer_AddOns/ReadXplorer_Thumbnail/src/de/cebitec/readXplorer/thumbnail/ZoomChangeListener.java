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

import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.TrackViewer;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author dkramer
 */
class ZoomChangeListener implements ChangeListener {

    private Map<TrackViewer,CoverageZoomSlider> sliderMap;
    

    public ZoomChangeListener() {
        sliderMap = new HashMap<>();
    }

    public void addMapValue(TrackViewer track, CoverageZoomSlider slider){
        sliderMap.put(track, slider);
    }
    

    @Override
    public void stateChanged(ChangeEvent e) {
        for (TrackViewer trackViewer : sliderMap.keySet()) {
            CoverageZoomSlider source = (CoverageZoomSlider) e.getSource();
            trackViewer.verticalZoomLevelUpdated(source.getValue());
            CoverageZoomSlider change = sliderMap.get(trackViewer);
            change.setValue(source.getValue());
        }

            
    }

}
