package de.cebitec.vamp.parser;

import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJobs;
import java.util.List;

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
