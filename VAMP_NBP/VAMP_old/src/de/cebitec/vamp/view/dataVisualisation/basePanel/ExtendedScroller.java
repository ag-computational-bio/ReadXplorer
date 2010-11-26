package de.cebitec.vamp.view.dataVisualisation.basePanel;

import de.cebitec.vamp.view.dataVisualisation.SynchronousNavigator;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollBar;
import javax.swing.JSlider;

/**
 *
 * @author ddoppmeier
 */
public class ExtendedScroller extends JScrollBar implements SynchronousNavigator{

    private final static long serialVersionUID = 7416234;

    private int currentValue;
    private List<AdjustmentPanelListenerI> listeners;


    public ExtendedScroller(int min, int max, int init){
        super(JSlider.HORIZONTAL, init, 0, min, max);
        this.setBlockIncrement(1000);
        this.setUnitIncrement(10);

        this.currentValue = init;
        listeners = new ArrayList<AdjustmentPanelListenerI>();
        this.addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                int newValue = ExtendedScroller.this.getValue();
                if(newValue != currentValue){
                    currentValue = newValue;
                    updateListeners();
                }
            }
        });
    }


        /**
     *
     * @param listener register this listener to be notified of changes
     */
    public void addAdjustmentListener(AdjustmentPanelListenerI listener){
        listeners.add(listener);
        listener.navigatorBarUpdated(currentValue);
    }

        /**
     *
     * @param listener remove the listener, so it is not updated anymore on
     * accuring changes
     */
    public void removeAdjustmentListener(AdjustmentPanelListenerI listener){
        if(listeners.contains(listener)){
            listeners.remove(listener);
        }
    }

    private void updateListeners(){
        for(AdjustmentPanelListenerI l : listeners){
            l.navigatorBarUpdated(currentValue);
        }
    }

    @Override
    public void setCurrentScrollValue(int value) {
        this.currentValue = value;
        this.setValue(currentValue);
    }

    @Override
    public void setCurrentZoomValue(int value) {
        
    }


}
