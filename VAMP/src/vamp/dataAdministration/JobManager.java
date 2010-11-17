package vamp.dataAdministration;

import java.util.List;
import vamp.importer.ReferenceJob;
import vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public interface JobManager {


    public void removeRefGenJob(ReferenceJob refGenJob);

    public void unRemoveRefGenJob(ReferenceJob refGenJob);

        public void removeTrackJobRun(TrackJobs trackJob);

    public void unRemoveTrackJobRun(TrackJobs trackJob);

    public List<TrackJobs> getScheduledTrackJobsRun();


    public List<ReferenceJob> getScheduledRefGenJobs();

}
