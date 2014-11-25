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
