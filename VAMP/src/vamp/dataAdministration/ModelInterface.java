package vamp.dataAdministration;

import java.util.List;
import vamp.importer.ReferenceJob;
import vamp.importer.RunJob;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public interface ModelInterface {

    public void addListener(ModelListenerI listener);

    public void removeListener(ModelListenerI view);

    public void fetchNecessaryData();

    public List<TrackJob> getScheduledTrackJobs();

    public List<RunJob> getScheduledRunJobs();

    public List<ReferenceJob> getScheduledRefGenJobs();

}
