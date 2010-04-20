package vamp.databackend;

import vamp.databackend.dataObjects.PersistantCoverage;

/**
 *
 * @author ddoppmeier
 */
public interface CoverageThreadListener {

    public void receiveCoverage(PersistantCoverage coverage);

}
