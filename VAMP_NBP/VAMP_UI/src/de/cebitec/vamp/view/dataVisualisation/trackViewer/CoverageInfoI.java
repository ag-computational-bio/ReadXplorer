package de.cebitec.vamp.view.dataVisualisation.trackViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;

/**
 *
 * @author jwinneba
 */
public interface CoverageInfoI {

    public void setCoverage(PersistantCoverage coverage);

    public PersistantCoverage getCoverage();

}
