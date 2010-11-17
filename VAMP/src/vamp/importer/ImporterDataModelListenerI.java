package vamp.importer;

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
