package de.cebitec.vamp.view.dataVisualisation;

import de.cebitec.vamp.view.dataVisualisation.basePanel.AdjustmentPanelListenerI;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;

/**
 *
 * @author ddoppmeier
 */
public class BoundsInfoManager implements AdjustmentPanelListenerI {


    private int currentPosition;
    private int zoomfactor;
    private PersistantReference currentRefGen;

    private List<LogicalBoundsListener> boundListeners;
    private List<SynchronousNavigator> syncedNavigators;

    public BoundsInfoManager(PersistantReference refGen){
        this.currentRefGen = refGen;
        boundListeners = new ArrayList<LogicalBoundsListener>();
        syncedNavigators = new ArrayList<SynchronousNavigator>();
        zoomfactor = 1;
        currentPosition = 1;
    }

    public void addBoundsListener(LogicalBoundsListener a){
        boundListeners.add(a);
        a.updateLogicalBounds(computeBounds(a.getPaintingAreaDimension()));
    }

    public void removeBoundListener(LogicalBoundsListener a){
        if(boundListeners.contains(a)){
            boundListeners.remove(a);
        }
    }

    public void addSynchronousNavigator(SynchronousNavigator navi){
        syncedNavigators.add(navi);
        navi.setCurrentScrollValue(currentPosition);
        navi.setCurrentZoomValue(zoomfactor);
    }

    public void removeSynchronousNavigator(SynchronousNavigator navi){
        if(syncedNavigators.contains(navi)){
            syncedNavigators.remove(navi);
        }
    }

    private void updateListeners(){

        for(LogicalBoundsListener a : boundListeners){
            a.updateLogicalBounds(computeBounds(a.getPaintingAreaDimension()));
        }
    }

    private void updateSynchronousNavigators(){
        for(SynchronousNavigator n : syncedNavigators){
            n.setCurrentScrollValue(currentPosition);
            n.setCurrentZoomValue(zoomfactor);
        }
    }

    public void getUpdatedBoundsInfo(LogicalBoundsListener a){
        a.updateLogicalBounds(computeBounds(a.getPaintingAreaDimension()));
    }

    public BoundsInfo getUpdatedBoundsInfo(Dimension d){
        return computeBounds(d);
    }

    private BoundsInfo computeBounds(Dimension d){
        int logWidth = (int) (d.getWidth() * 0.1 * zoomfactor);

        BoundsInfo tmpBound = new BoundsInfo(1, currentRefGen.getSequence().length(), currentPosition, zoomfactor);
        tmpBound.setLogWidth(logWidth);
        return tmpBound;
    }

    @Override
    public void zoomLevelUpdated(int sliderValue) {
        this.zoomfactor = sliderValue;
        updateSynchronousNavigators();
        updateListeners();
    }

    @Override
    public void navigatorBarUpdated(int scrollbarValue) {
        this.currentPosition = scrollbarValue;
        updateSynchronousNavigators();
        updateListeners();
    }
}
