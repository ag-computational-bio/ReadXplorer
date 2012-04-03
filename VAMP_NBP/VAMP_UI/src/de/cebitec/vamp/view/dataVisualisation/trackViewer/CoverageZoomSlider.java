package de.cebitec.vamp.view.dataVisualisation.trackViewer;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author ddoppmeier
 */
public class CoverageZoomSlider extends JSlider {

    private static final long serialVersionUID = 249753543;

    public CoverageZoomSlider(final TrackViewer trackViewer) {
        super(1, 150, 1);
        trackViewer.verticalZoomLevelUpdated(this.getValue());

        this.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slide = (JSlider) e.getSource();
                slide.setToolTipText(slide.getValue() + "");
                trackViewer.verticalZoomLevelUpdated(CoverageZoomSlider.this.getValue());
            }
        });
    }
}
