package vamp.dataAdministration;

import vamp.importer.ReferenceJob;
import vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public interface ModelListenerI {

    public void deselectRefGen(ReferenceJob refGen);


    public void refGenJobAdded(ReferenceJob refGenJob);


    public void trackJobsAdded(TrackJobs trackJob);
}
