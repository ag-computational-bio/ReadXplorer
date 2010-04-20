package vamp.dataAdministration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import vamp.databackend.dataObjects.PersistantReference;
import vamp.databackend.dataObjects.PersistantTrack;
import vamp.databackend.dataObjects.PersistentRun;
import vamp.databackend.connector.ProjectConnector;
import vamp.importer.ReferenceJob;
import vamp.importer.RunJob;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public class Model implements ModelInterface, JobManager {

    private List<ModelListenerI> listeners;
    private List<RunJob> runsToDelete;
    private List<TrackJob> tracksToDelete;
    private List<ReferenceJob> genomesToDelete;



    public Model(){
        listeners = new ArrayList<ModelListenerI>();
        runsToDelete = new ArrayList<RunJob>();
        tracksToDelete = new ArrayList<TrackJob>();
        genomesToDelete = new ArrayList<ReferenceJob>();
    }

    @Override
    public void fetchNecessaryData(){

        HashMap<Long, RunJob> indexedRuns = new HashMap<Long, RunJob>();
        HashMap<Long, ReferenceJob> indexedGens = new HashMap<Long, ReferenceJob>();

        List<PersistentRun> dbRuns = ProjectConnector.getInstance().getRuns();
        for(Iterator<PersistentRun> it = dbRuns.iterator(); it.hasNext() ;){
            PersistentRun dbRun = it.next();
            // File and parser parameter meaningles in this context
            RunJob r = new RunJob(dbRun.getId(), null, dbRun.getDescription(), null, dbRun.getTimestamp());
            indexedRuns.put(r.getID(), r);
            for(ModelListenerI l : listeners){
                l.runJobAdded(r);
            }
        }
        

        List<PersistantReference> dbGens = ProjectConnector.getInstance().getGenomes();
        for(Iterator<PersistantReference> it = dbGens.iterator(); it.hasNext(); ){
            PersistantReference dbGen = it.next();
            // File and parser parameter meaningles in this context
            ReferenceJob r = new ReferenceJob(dbGen.getId(), null, null, dbGen.getDescription(), dbGen.getName(), dbGen.getTimeStamp());
            indexedGens.put(r.getID(), r);
            for(ModelListenerI l : listeners){
                l.refGenJobAdded(r);
            }
        }

        List<PersistantTrack> dbTracks = ProjectConnector.getInstance().getTracks();
        for(Iterator<PersistantTrack> it = dbTracks.iterator(); it.hasNext(); ){
            PersistantTrack dbTrack = it.next();

            // File and parser, refgenjob, runjob parameters meaningles in this context
            TrackJob t = new TrackJob(dbTrack.getId(), null, dbTrack.getDescription(),
                    indexedRuns.get(dbTrack.getRunID()), indexedGens.get(dbTrack.getRefGenID()),
                    null, dbTrack.getTimestamp());

            // register dependent tracks at genome and run
            ReferenceJob gen = indexedGens.get(dbTrack.getRefGenID());
            gen.registerTrack(t);
            RunJob run = indexedRuns.get(dbTrack.getRunID());
            run.registerTrack(t);

            for(ModelListenerI l : listeners){
                l.trackJobsAdded(t);
            }
        }

    }

    @Override
    public void addListener(ModelListenerI listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ModelListenerI listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeRefGenJob(ReferenceJob refGenJob) {
        genomesToDelete.add(refGenJob);
    }

    @Override
    public void unRemoveRefGenJob(ReferenceJob refGenJob) {
        if(genomesToDelete.contains(refGenJob)){
            genomesToDelete.remove(refGenJob);
        }
    }

    @Override
    public void removeRunJob(RunJob runJob) {
        runsToDelete.add(runJob);
    }

    @Override
    public void unRemoveRunJob(RunJob runJob) {
        if(runsToDelete.contains(runJob)){
            runsToDelete.remove(runJob);
        }
    }

    @Override
    public void removeTrackJob(TrackJob trackJob) {
        tracksToDelete.add(trackJob);

        // unregister dependencies
        trackJob.getRefGen().unregisterTrack(trackJob);
        trackJob.getRunJob().unregisterTrack(trackJob);
    }

    @Override
    public void unRemoveTrackJob(TrackJob trackJob) {
        if(tracksToDelete.contains(trackJob)){
            tracksToDelete.remove(trackJob);

            // re-register dependencies
            trackJob.getRefGen().registerTrack(trackJob);
            for(ModelListenerI l : listeners){
                l.deselectRefGen(trackJob.getRefGen());
            }
            trackJob.getRunJob().registerTrack(trackJob);
            for(ModelListenerI l : listeners){
                l.deselectRun(trackJob.getRunJob());
            }
        }
    }

    @Override
    public List<TrackJob> getScheduledTrackJobs() {
        return tracksToDelete;
    }

    @Override
    public List<RunJob> getScheduledRunJobs() {
        return runsToDelete;
    }

    @Override
    public List<ReferenceJob> getScheduledRefGenJobs() {
        return genomesToDelete;
    }
    
}
