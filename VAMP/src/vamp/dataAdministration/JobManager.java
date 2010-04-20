package vamp.dataAdministration;

import java.util.List;
import vamp.importer.ReferenceJob;
import vamp.importer.RunJob;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public interface JobManager {


    public void removeRefGenJob(ReferenceJob refGenJob);

    public void unRemoveRefGenJob(ReferenceJob refGenJob);

    public void removeRunJob(RunJob runJob);

    public void unRemoveRunJob(RunJob runJob);

    public void removeTrackJob(TrackJob trackJob);

    public void unRemoveTrackJob(TrackJob trackJob);

    public List<TrackJob> getScheduledTrackJobs();

    public List<RunJob> getScheduledRunJobs();

    public List<ReferenceJob> getScheduledRefGenJobs();

}
