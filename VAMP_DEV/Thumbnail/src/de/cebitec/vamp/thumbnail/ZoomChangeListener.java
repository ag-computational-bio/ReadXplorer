/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
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
        sliderMap = new HashMap<TrackViewer, CoverageZoomSlider>();
    }

    public void addMapValue(TrackViewer track, CoverageZoomSlider slider){
        sliderMap.put(track, slider);
    }
    

    @Override
    public void stateChanged(ChangeEvent e) {
        for(TrackViewer trackViewer:  sliderMap.keySet()){
            CoverageZoomSlider source = (CoverageZoomSlider) e.getSource();
            trackViewer.verticalZoomLevelUpdated(source.getValue());
            CoverageZoomSlider change = sliderMap.get(trackViewer);
            change.setValue(source.getValue());
        }

            
    }

}
