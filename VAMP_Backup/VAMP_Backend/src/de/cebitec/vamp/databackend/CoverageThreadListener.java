package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;

/**
 *
 * @author ddoppmeier
 */
public interface CoverageThreadListener {

    public void receiveCoverage(PersistantCoverage coverage);

}
