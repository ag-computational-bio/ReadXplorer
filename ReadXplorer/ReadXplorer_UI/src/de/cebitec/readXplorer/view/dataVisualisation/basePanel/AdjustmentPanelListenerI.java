package de.cebitec.readXplorer.view.dataVisualisation.basePanel;

/**
 * This interface defines listeners for changes in the viewer's control panel.
 * 
 * @author ddoppmeier, rhilker
 */
public interface AdjustmentPanelListenerI {

    /**
     * Notify listeners of changes of the zoom level
     * @param zoomValue new zoom value to be applied
     */
    public void zoomLevelUpdated(int zoomValue);

    /**
     * Notify listeners of changes in the navigation bar
     * @param navigatorBarValue updated current position of the genome, that is
     * to be shown
     */
    public void navigatorBarUpdated(int navigatorBarValue);
    
    /**
     * Notify listeners of the selection of a new chromosome.
     * @param activeChromId Id of the new active chromosome
     */
    public void chromosomeChanged(int activeChromId);

}
