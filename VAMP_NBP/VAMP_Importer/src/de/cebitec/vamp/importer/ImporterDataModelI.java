package de.cebitec.vamp.importer;

import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.parser.ReferenceJob;
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
