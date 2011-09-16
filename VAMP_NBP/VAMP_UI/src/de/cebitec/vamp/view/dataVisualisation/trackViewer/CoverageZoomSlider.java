package de.cebitec.vamp.view.dataVisualisation.trackViewer;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author ddoppmeier
 */
public class CoverageZoomSlider extends JSlider{

    private static final long serialVersionUID = 249753543;

    public CoverageZoomSlider(final TrackViewer trackViewer){
        super(1,100,1); //TODO: adjust to max coverage value in current view!
        trackViewer.verticalZoomLevelUpdated(this.getValue());
        this.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                trackViewer.verticalZoomLevelUpdated(CoverageZoomSlider.this.getValue());
            }
        });
    }

}
