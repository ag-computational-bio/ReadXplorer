package vamp.importer;

import java.io.File;
import java.util.List;
import vamp.parsing.mappings.MappingParserI;
import vamp.parsing.reads.RunParserI;
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

    public void removeRunTask(RunJob runJob);

    public void removeTrackTask(TrackJob trackJob);


    // create tasks from 'primitives'
    public void createRefGenTask(ReferenceParserI parser, File refGenFile, String description, String name);

    public void createRunTask(RunParserI parser, File readFile, String description);

    public void createTrackTask(MappingParserI parser, File mappingFile, String description, RunJob runJob, ReferenceJob refGenJob);


    // get jobs that are created from scratch
    public List<ReferenceJob> getRefGenJobList();

    public List<RunJob> getRunJobList();

    public List<TrackJob> getTrackJobList();

}
