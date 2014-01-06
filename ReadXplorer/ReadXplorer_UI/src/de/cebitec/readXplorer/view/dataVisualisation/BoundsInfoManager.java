package de.cebitec.readXplorer.view.dataVisualisation;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.AdjustmentPanelListenerI;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the bounds information for a reference sequence.
 *
 * @author ddoppmeier, rhilker
 */
public class BoundsInfoManager implements AdjustmentPanelListenerI {

    private int currentHorizontalPosition;
    private int zoomfactor;
    private PersistantReference refGenome;
    private List<LogicalBoundsListener> boundListeners;
    private List<SynchronousNavigator> syncedNavigators;

    /**
     * Manages the bounds information for a reference sequence.
     * @param refGenome The reference genome whose bounds are handled.
     */
    public BoundsInfoManager(PersistantReference refGenome) {
        this.refGenome = refGenome;
        this.boundListeners = new ArrayList<>();
        this.syncedNavigators = new ArrayList<>();
        this.zoomfactor = 1;
        this.currentHorizontalPosition = 1;
        Observer chromChangeObserver = new Observer() { //observer for chromosome changes from elsewhere
            @Override
            public void update(Object args) {
                updateLogicalListeners();
                updateSynchronousNavigators();
            }
        };
        refGenome.registerObserver(chromChangeObserver);
    }

    public void addBoundsListener(LogicalBoundsListener a) {
        boundListeners.add(a);
        if (a.isPaintingAreaAvailable()) {
            a.updateLogicalBounds(computeBounds(a.getPaintingAreaDimension()));
        }
    }

    public void removeBoundListener(LogicalBoundsListener a) {
        if (boundListeners.contains(a)) {
            boundListeners.remove(a);
        }
    }

    /**
     * Add a navigator (panel providing e.g. scrolling and zooming).
     * @param navi navigator to add to the list of listeners
     */
    public void addSynchronousNavigator(SynchronousNavigator navi) {
        syncedNavigators.add(navi);
        navi.setCurrentScrollValue(currentHorizontalPosition);
        navi.setCurrentZoomValue(zoomfactor);
    }

    /**
     * Remove a navigator (panel providing e.g. scrolling and zooming).
     * @param navi navigator to remove from the list of listeners
     */
    public void removeSynchronousNavigator(SynchronousNavigator navi) {
        if (syncedNavigators.contains(navi)) {
            syncedNavigators.remove(navi);
        }
    }

    private void updateLogicalListeners() {
        for (LogicalBoundsListener a : boundListeners) {
            if (a.isPaintingAreaAvailable()) {
                a.updateLogicalBounds(computeBounds(a.getPaintingAreaDimension()));
            }
        }
    }

    /**
     * Update all navigators (panels providing e.g. scrolling and zooming) in
     * the list.
     */
    private void updateSynchronousNavigators() {
        for (SynchronousNavigator n : syncedNavigators) {
            n.setCurrentScrollValue(currentHorizontalPosition);
            n.setCurrentZoomValue(zoomfactor);
        }
    }

    public void getUpdatedBoundsInfo(LogicalBoundsListener a) {

        if (a.isPaintingAreaAvailable()) {
            a.updateLogicalBounds(computeBounds(a.getPaintingAreaDimension()));
        }
    }

    /**
     * @param d
     * @return The current horizontal bounds in connection to the reference 
     * sequence.
     */
    public BoundsInfo getUpdatedBoundsInfo(Dimension d) {
        return computeBounds(d);
    }

    /**
     * Compute the horizontal bounds in connection to the reference sequence.
     * @param d dimension
     * @return BoundsInfo object containing current bounds
     */
    private BoundsInfo computeBounds(Dimension d) {
        int logWidth = (int) (d.getWidth() * 0.1 * zoomfactor);

        BoundsInfo bounds = new BoundsInfo(1, refGenome.getActiveChromLength(), 
                currentHorizontalPosition, 
                zoomfactor, 
                refGenome.getActiveChromId(),
                logWidth);
        return bounds;
    }

    /**
     * Notify listeners of changes of the zoom level.
     * @param sliderValue new zoom value to be applied
     */
    @Override
    public void zoomLevelUpdated(int sliderValue) {
        this.zoomfactor = sliderValue;
        this.updateSynchronousNavigators();
        this.updateLogicalListeners();
    }

    /**
     * Notify listeners of changes of the currently centered genome position.
     * @param scrollbarValue position in the genome to center
     */
    @Override
    public void navigatorBarUpdated(int scrollbarValue) {
        this.currentHorizontalPosition = scrollbarValue;
        this.updateSynchronousNavigators();
        this.updateLogicalListeners();
    }

    /**
     * Notify listeners of changes of the currently active chromosome.
     * @param activeChromId Id of the new active chromosome
     */
    @Override
    public void chromosomeChanged(int activeChromId) {
        this.refGenome.setActiveChromId(activeChromId);
    }
}
