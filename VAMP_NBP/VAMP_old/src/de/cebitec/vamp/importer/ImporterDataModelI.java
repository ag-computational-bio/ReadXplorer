package de.cebitec.vamp.importer;

import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public interface ImporterDataModelI {

    public void addTaskListener(ImporterDataModelListenerI view);

    public List<ReferenceJob> getRefGenJobList();


    public List<TrackJobs> getTrackJobListRun();


}
