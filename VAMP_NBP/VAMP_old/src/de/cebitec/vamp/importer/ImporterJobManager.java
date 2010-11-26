package de.cebitec.vamp.importer;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import de.cebitec.vamp.parsing.mappings.MappingParserI;
import de.cebitec.vamp.parsing.reference.ReferenceParserI;

/**
 *
 * @author ddoppmeier
 */
public class ImporterJobManager implements JobManagerI, ImporterDataModelI {

    private List<ImporterDataModelListenerI> listeners;


    private List<TrackJobs> trackJobsrun;
    private List<ReferenceJob> refGenJobs;

    public ImporterJobManager(){
        listeners = new ArrayList<ImporterDataModelListenerI>();
 
        trackJobsrun = new ArrayList<TrackJobs>();
        refGenJobs = new ArrayList<ReferenceJob>();

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
    public void removeTrackTask(TrackJobs trackJob) {
        trackJobsrun.remove(trackJob);
        // unregister from associated genomes
        for(ReferenceJob r : refGenJobs){
            r.unregisterTrackwithoutRunJob(trackJob);
        }

        for(ImporterDataModelListenerI l : listeners){
            l.trackJobRemovedRun(trackJob);
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
    public List<ReferenceJob> getRefGenJobList(){
        return refGenJobs;
    }
    
    @Override
    public List<TrackJobs> getTrackJobListRun(){
        return trackJobsrun;
    }

    @Override
    public void createTrackTaskWithoutRunJob(MappingParserI parser, File mappingFile, String description, ReferenceJob refGenJob) {
        TrackJobs t2 = new TrackJobs(null, mappingFile, description, refGenJob, parser, new Timestamp(System.currentTimeMillis()));
         refGenJob.registerTrackWithoutRunJob(t2);
                 trackJobsrun.add(t2);
        for(ImporterDataModelListenerI l : listeners){
            l.trackJobAddedRun(t2);
        }
    }

}
