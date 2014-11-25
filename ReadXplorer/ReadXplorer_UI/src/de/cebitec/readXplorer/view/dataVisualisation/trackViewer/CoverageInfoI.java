package de.cebitec.readXplorer.view.dataVisualisation.trackViewer;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;

/**
 *
 * @author jwinneba
 */
public interface CoverageInfoI {

    public void setCoverage(PersistantCoverage coverage);

    public PersistantCoverage getCoverage();

}
