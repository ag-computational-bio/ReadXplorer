package vamp.dataAdministration;

import java.util.List;
import vamp.importer.ReferenceJob;
import vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public interface ModelInterface {

    public void addListener(ModelListenerI listener);

    public void removeListener(ModelListenerI view);

    public void fetchNecessaryData();

    public List<TrackJobs> getScheduledTrackJobsRun();


    public List<ReferenceJob> getScheduledRefGenJobs();

}
