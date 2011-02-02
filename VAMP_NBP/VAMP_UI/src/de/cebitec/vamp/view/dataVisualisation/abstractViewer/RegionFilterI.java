package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public interface RegionFilterI {

    public List<Region> findRegions();

    public void setIntervall(int start, int stop);

}
