package vamp.importer;

/**
 *
 * @author ddoppmeier
 */
public interface ImporterDataModelListenerI {

    public void runJobAdded(RunJob runJob);

    public void runJobRemoved(RunJob runJob);

    public void trackJobAdded(TrackJob trackJob);

    public void trackJobRemoved(TrackJob trackJob);

    public void refGenJobAdded(ReferenceJob refGenJob);

    public void refGenJobRemoved(ReferenceJob refGenJob);

}
