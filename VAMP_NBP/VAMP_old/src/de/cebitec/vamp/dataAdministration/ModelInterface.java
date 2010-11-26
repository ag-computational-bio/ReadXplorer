package de.cebitec.vamp.dataAdministration;

import de.cebitec.vamp.importer.ReferenceJob;
import de.cebitec.vamp.importer.TrackJobs;
import java.util.List;

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
