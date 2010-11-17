package vamp.importer;

import java.io.File;
import java.util.List;
import vamp.parsing.mappings.MappingParserI;
import vamp.parsing.reference.ReferenceParserI;

/**
 *
 * @author ddoppmeier
 */
public interface JobManagerI {

    // listener management
    public void addTaskListener(ImporterDataModelListenerI listener);

    public void removeTaskListener(ImporterDataModelListenerI listener);


    // remove jobs from manager
    public void removeRefGenTask(ReferenceJob refGenJob);


    public void removeTrackTask(TrackJobs trackJob);


    // create tasks from 'primitives'
    public void createRefGenTask(ReferenceParserI parser, File refGenFile, String description, String name);


    public void createTrackTaskWithoutRunJob(MappingParserI parser, File mappingFile, String description, ReferenceJob refGenJob);

    // get jobs that are created from scratch
    public List<ReferenceJob> getRefGenJobList();

    public List<TrackJobs> getTrackJobListRun();

}
