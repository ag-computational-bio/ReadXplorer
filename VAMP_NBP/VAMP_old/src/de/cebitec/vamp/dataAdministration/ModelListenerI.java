package de.cebitec.vamp.dataAdministration;

import de.cebitec.vamp.importer.ReferenceJob;
import de.cebitec.vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public interface ModelListenerI {

    public void deselectRefGen(ReferenceJob refGen);


    public void refGenJobAdded(ReferenceJob refGenJob);


    public void trackJobsAdded(TrackJobs trackJob);
}
