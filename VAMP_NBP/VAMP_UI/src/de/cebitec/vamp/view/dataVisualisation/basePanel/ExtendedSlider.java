package de.cebitec.vamp.view.dataVisualisation.basePanel;

import de.cebitec.vamp.view.dataVisualisation.SynchronousNavigator;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSlider;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;

/**
 *
 * @author ddoppmeier
 */
public class ExtendedSlider extends JSlider implements SynchronousNavigator {

    private final static long serialVersionUID = 2347624;

    private int current;

    private List<AdjustmentPanelListenerI> listeners;

    public ExtendedSlider(int min, int max, int init){
        super(JSlider.HORIZONTAL, min, max, init);
        this.current = init;
        listeners = new ArrayList<AdjustmentPanelListenerI>();
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int newValue = ExtendedSlider.this.getValue();
                if(newValue != current){
                    current = newValue;
                    updateListeners();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        
//        this.addChangeListener(new ChangeListener() {
//
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                int newValue = ExtendedSlider.this.getValue();
//                if(newValue != current){
//                    current = newValue;
//                    updateListeners();
//                }
//            }
//        });
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
