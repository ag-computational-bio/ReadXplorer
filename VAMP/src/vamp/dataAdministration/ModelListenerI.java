package vamp.dataAdministration;

import vamp.importer.ReferenceJob;
import vamp.importer.RunJob;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public interface ModelListenerI {

    public void deselectRefGen(ReferenceJob refGen);

    public void deselectRun(RunJob runJob);

    public void refGenJobAdded(ReferenceJob refGenJob);

    public void runJobAdded(RunJob runJob);

    public void trackJobsAdded(TrackJob trackJob);
}
