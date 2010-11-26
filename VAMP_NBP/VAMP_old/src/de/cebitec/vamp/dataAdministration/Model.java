package de.cebitec.vamp.dataAdministration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.dataObjects.PersistentRun;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.importer.ReferenceJob;
import de.cebitec.vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public class Model implements ModelInterface, JobManager {

    private List<ModelListenerI> listeners;
    private List<TrackJobs> tracksToDelete;
    private List<ReferenceJob> genomesToDelete;



    public Model(){
        listeners = new ArrayList<ModelListenerI>();
        tracksToDelete = new ArrayList<TrackJobs>();
        genomesToDelete = new ArrayList<ReferenceJob>();
    }

    @Override
    public void fetchNecessaryData(){
  
      
        HashMap<Long, ReferenceJob> indexedGens = new HashMap<Long, ReferenceJob>();
        
        List<PersistentRun> dbRuns = ProjectConnector.getInstance().getRuns();

        

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
            TrackJobs t = new TrackJobs(dbTrack.getId(), null, dbTrack.getDescription(),
                    indexedGens.get(dbTrack.getRefGenID()),
                    null, dbTrack.getTimestamp());

            // register dependent tracks at genome and run
            ReferenceJob gen = indexedGens.get(dbTrack.getRefGenID());
            gen.registerTrackWithoutRunJob(t);

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
    public List<ReferenceJob> getScheduledRefGenJobs() {
        return genomesToDelete;
    }
    @Override
    public void removeTrackJobRun(TrackJobs trackJob) {
                tracksToDelete.add(trackJob);

        // unregister dependencies
        trackJob.getRefGen().unregisterTrackwithoutRunJob(trackJob);
    }

    @Override
    public void unRemoveTrackJobRun(TrackJobs trackJob) {
               if(tracksToDelete.contains(trackJob)){
            tracksToDelete.remove(trackJob);

            // re-register dependencies
            trackJob.getRefGen().registerTrackWithoutRunJob(trackJob);
            for(ModelListenerI l : listeners){
                l.deselectRefGen(trackJob.getRefGen());
            }
        }
    }

    @Override
    public List<TrackJobs> getScheduledTrackJobsRun() {
         return tracksToDelete;
    }
    
}
