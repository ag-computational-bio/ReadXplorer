package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public interface RegionFilterI {

    /**
     * Identifies regions. Remember to set setFrameCurrFeature in StartCodonFilter
     * first, if only regions of a specified frame should be identified.
     * @return a list of the identified regions
     */
    public List<Region> findRegions();

    /**
     * Sets the interval to use.
     * @param start the leftmost position of the interval
     * @param stop the rightmost position of the interval
     */
    public void setInterval(int start, int stop);

}
