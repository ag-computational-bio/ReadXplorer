package de.cebitec.readXplorer.parser;

import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public interface JobManager {

    public void removeRefGenJob(ReferenceJob refGenJob);

    public void unRemoveRefGenJob(ReferenceJob refGenJob);

    public void removeTrackJobRun(TrackJob trackJob);

    public void unRemoveTrackJobRun(TrackJob trackJob);

    public List<TrackJob> getScheduledTrackJobsRun();

    public List<ReferenceJob> getScheduledRefGenJobs();

}
