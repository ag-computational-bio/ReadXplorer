package vamp.importer;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vamp.parsing.mappings.MappingParserI;
import vamp.parsing.reads.RunParserI;
import vamp.parsing.reference.ReferenceParserI;

/**
 *
 * @author ddoppmeier
 */
public class ImporterJobManager implements JobManagerI, ImporterDataModelI {

    private List<ImporterDataModelListenerI> listeners;

    private List<TrackJob> trackJobs;
    private List<ReferenceJob> refGenJobs;
    private List<RunJob> runJobs;

    public ImporterJobManager(){
        listeners = new ArrayList<ImporterDataModelListenerI>();
        trackJobs = new ArrayList<TrackJob>();
        refGenJobs = new ArrayList<ReferenceJob>();
        runJobs = new ArrayList<RunJob>();
    }


    @Override
    public void addTaskListener(ImporterDataModelListenerI listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTaskListener(ImporterDataModelListenerI listener) {
        listeners.remove(listener);
    }


    @Override
    public void removeRefGenTask(ReferenceJob refGenJob) {
        refGenJobs.remove(refGenJob);

        for(ImporterDataModelListenerI l : listeners){
            l.refGenJobRemoved(refGenJob);
        }
    }

    @Override
    public void removeRunTask(RunJob runJob) {

        runJobs.remove(runJob);
        for(ImporterDataModelListenerI l : listeners){
            l.runJobRemoved(runJob);
        }
        
    }

    @Override
    public void removeTrackTask(TrackJob trackJob) {
        trackJobs.remove(trackJob);

        // unregister from associated runs
        for(RunJob r : runJobs){
            r.unregisterTrack(trackJob);
        }

        // unregister from associated genomes
        for(ReferenceJob r : refGenJobs){
            r.unregisterTrack(trackJob);
        }

        for(ImporterDataModelListenerI l : listeners){
            l.trackJobRemoved(trackJob);
        }
    }


    @Override
    public void createRefGenTask(ReferenceParserI parser, File refGenFile, String description, String name) {
        ReferenceJob r = new ReferenceJob(null,refGenFile, parser, description, name, new Timestamp(System.currentTimeMillis()));
        refGenJobs.add(r);
        for(ImporterDataModelListenerI i : listeners){
            i.refGenJobAdded(r);
        }
    }

    @Override
    public void createRunTask(RunParserI parser, File readFile, String description) {
        // since this is for new runs (to be imported) id cannot be assigned.
        // even if set, it would ne ignored
        RunJob r = new RunJob(null, readFile, description, parser, new Timestamp(System.currentTimeMillis()));
        runJobs.add(r);
        for(ImporterDataModelListenerI l : listeners){
            l.runJobAdded(r);
        }
    }

    @Override
    public void createTrackTask(MappingParserI parser, File mappingFile, String description, RunJob runJob, ReferenceJob refGenJob) {
        TrackJob t = new TrackJob(null, mappingFile, description, runJob, refGenJob, parser, new Timestamp(System.currentTimeMillis()));
        runJob.registerTrack(t);
        refGenJob.registerTrack(t);

        trackJobs.add(t);
        for(ImporterDataModelListenerI l : listeners){
            l.trackJobAdded(t);
        }
    }

    @Override
    public List<RunJob> getRunJobList(){
        return runJobs;
    }
    
    @Override
    public List<ReferenceJob> getRefGenJobList(){
        return refGenJobs;
    }
    
    @Override
    public List<TrackJob> getTrackJobList(){
        return trackJobs;
    }


}
