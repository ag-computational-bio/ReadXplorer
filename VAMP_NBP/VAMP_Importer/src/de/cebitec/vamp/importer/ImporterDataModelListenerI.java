package de.cebitec.vamp.importer;

import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.parser.ReferenceJob;

/**
 *
 * @author ddoppmeier
 */
public interface ImporterDataModelListenerI {

    public void trackJobAddedRun(TrackJobs trackJob);

    public void trackJobRemovedRun(TrackJobs trackJob);

    public void refGenJobAdded(ReferenceJob refGenJob);

    public void refGenJobRemoved(ReferenceJob refGenJob);

}
