package de.cebitec.readXplorer.view.dataVisualisation.basePanel;

import de.cebitec.readXplorer.view.dataVisualisation.SynchronousNavigator;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A Panel to control zoom-level and the currently shown positions of listeners
 * @author ddoppmeier
 */
public class AdjustmentPanel extends JPanel implements SynchronousNavigator {

    public static final long serialVersionUID = 623482568;

    private ExtendedSlider slider;
    private ExtendedScroller scrollbar;

    /**
     * Create an AdjustmentPanel used for managing the displayed area of listeners.
     * @param navigatorMin mostly 1
     * @param navigatorMax maximum value of the navigator, normally the chromosome length
     * @param positionInit 
     * @param zoomInit 
     * @param sliderMax maximal value of the zoom slider
     * @param hasZoomslider 
     * @param hasScrollbar 
     */
    public AdjustmentPanel(int navigatorMin, int navigatorMax, int positionInit, int zoomInit,
            int sliderMax, boolean hasScrollbar, boolean hasZoomslider) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        if (hasScrollbar) {
            scrollbar = new ExtendedScroller(navigatorMin, navigatorMax, positionInit);
            this.add(scrollbar);
        }
        if (hasZoomslider) {
            zoomInit = sliderMax < zoomInit ? sliderMax : zoomInit;
            slider = new ExtendedSlider(1, sliderMax, zoomInit);
            this.add(slider);
        }
        
    }

    /**
     * Adjusts the navigator max value. Needed, when e.g. chromosomes are 
     * switched.
     * @param navigatorMax Sets the navigator max to this value.
     */
    public void setNavigatorMax(int navigatorMax) {
        scrollbar.setMaximum(navigatorMax);
    }

    /**
     *
     * @param listener register this listener to be notified of changes
     */
    public void addAdjustmentListener(AdjustmentPanelListenerI listener) {
        if(scrollbar != null){
            scrollbar.addAdjustmentListener(listener);
        }
        if(slider != null){
            slider.addAdjustmentPanelListener(listener);
        }
    }

    /**
     *
     * @param listener remove the listener, so it is not updated anymore on
     * occurring changes
     */
    public void removeAdjustmentListener(AdjustmentPanelListenerI listener) {
        if(scrollbar != null){
            scrollbar.removeAdjustmentListener(listener);
        }
        if(slider != null){
            slider.removeAdjustmentPanelListener(listener);
        }
    }

    @Override
    public void setCurrentScrollValue(int value) {
        if(scrollbar != null){
            scrollbar.setCurrentScrollValue(value);
        }
    }

    @Override
    public void setCurrentZoomValue(int value) {
        if(slider != null){
            slider.setCurrentZoomValue(value);
        }
    }

}
