package vamp.view.dataVisualisation.basePanel;

import vamp.view.dataVisualisation.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author ddoppmeier
 */
public class ExtendedSlider extends JSlider implements SynchronousNavigator{

    private final static long serialVersionUID = 2347624;

    private int current;

    private List<AdjustmentPanelListenerI> listeners;

    public ExtendedSlider(int min, int max, int init){
        super(JSlider.HORIZONTAL, min, max, init);
        this.current = init;
        listeners = new ArrayList<AdjustmentPanelListenerI>();
        this.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int newValue = ExtendedSlider.this.getValue();
                if(newValue != current){
                    current = newValue;
                    updateListeners();
                }
            }
        });
    }


    public void addAdjustmentPanelListener(AdjustmentPanelListenerI listener){
        listeners.add(listener);
        listener.zoomLevelUpdated(current);
    }

    public void removeAdjustmentPanelListener(AdjustmentPanelListenerI listener){
        listeners.remove(listener);
    }

    @Override
    public void setCurrentScrollValue(int value) {
    }

    @Override
    public void setCurrentZoomValue(int value) {
        current = value;
        this.setValue(current);

    }

    private void updateListeners(){
        for(AdjustmentPanelListenerI l : listeners){
            l.zoomLevelUpdated(current);
        }
    }

}
