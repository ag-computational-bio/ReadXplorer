package vamp.dataAdministration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import vamp.RunningTaskI;
import vamp.databackend.connector.StorageException;
import vamp.databackend.connector.ProjectConnector;
import vamp.importer.ReferenceJob;
import vamp.importer.RunJob;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public class DeletionThread extends SwingWorker implements RunningTaskI{

    private DataAdminController c;
    private List<ReferenceJob> gens;
    private List<TrackJob> tracks;
    private List<RunJob> runs;
    private Set<ReferenceJob> invalidGens;
    private Set<RunJob> invalidRuns;


    public DeletionThread(DataAdminController c, List<RunJob> runs, List<ReferenceJob> gens, List<TrackJob> tracks){
        super();
        this.c = c;
        this.runs = runs;
        this.gens = gens;
        this.tracks = tracks;
        invalidGens = new HashSet<ReferenceJob>();
        invalidRuns = new HashSet<RunJob>();
    }

    @Override
    protected Object doInBackground() {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of data");

        if(!tracks.isEmpty()){
            c.updateDataAdminStatus("Starting deletion of tracks:");
            for(Iterator<TrackJob> it = tracks.iterator(); it.hasNext(); ){
                TrackJob t = it.next();
                try {
                    ProjectConnector.getInstance().deleteTrack(t.getID());
                    c.updateDataAdminStatus("Completed deletion of\""+t.getDescription()+"\"");

                } catch (StorageException ex) {
                    c.updateDataAdminStatus("Deletion of \""+t.getDescription()+"\" failed");
                    // if this track fails, do not delete runs and genomes that are referenced by this track
                    invalidRuns.add(t.getRunJob());
                    invalidGens.add(t.getRefGen());
                    Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            c.updateDataAdminStatus("");
        }

        if(!runs.isEmpty()){
            c.updateDataAdminStatus("Starting deletion of runs:");
            for(Iterator<RunJob> it = runs.iterator(); it.hasNext(); ){
                RunJob r = it.next();
                if(invalidRuns.contains(r)){
                    c.updateDataAdminStatus("Because of a failure during earlier deletion of a track, that references this dataset, \""+r.getDescription()+"\" could not be deleted");
                } else {
                    try {
                        ProjectConnector.getInstance().deleteRun(r.getID());
                        c.updateDataAdminStatus("Completed deletion of\""+r.getDescription()+"\"");
                    } catch (StorageException ex) {
                        c.updateDataAdminStatus("Deletion of \""+r.getDescription()+"\" failed");
                        Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            c.updateDataAdminStatus("");
        }

        if(!gens.isEmpty()){
            c.updateDataAdminStatus("Starting deletion of references:");
            for(Iterator<ReferenceJob> it = gens.iterator(); it.hasNext(); ){
                ReferenceJob r = it.next();
                if(invalidGens.contains(r)){
                    c.updateDataAdminStatus("Because of a failure during earlier deletion of a track, that references this dataset, \""+r.getDescription()+"\" could not be deleted");
                } else {
                    try {
                        ProjectConnector.getInstance().deleteGenome(r.getID());
                        c.updateDataAdminStatus("Completed deletion of \""+r.getDescription()+"\"");
                    } catch (StorageException ex) {
                        c.updateDataAdminStatus("Deletion of \""+r.getDescription()+"\" failed");
                        Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            c.updateDataAdminStatus("");
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Completed Deletion of Data");

        return null;
    }

    @Override
    protected void done(){
        super.done();
        c.updateDataAdminStatus("Finished");
        c.deletionDone(this);
    }

}
