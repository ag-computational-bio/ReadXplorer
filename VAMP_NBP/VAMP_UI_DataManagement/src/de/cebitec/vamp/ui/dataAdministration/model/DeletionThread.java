package de.cebitec.vamp.ui.dataAdministration.model;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.StorageException;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJobs;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author ddoppmeier
 */
public class DeletionThread extends SwingWorker<Object, Object>{

    private DataAdminController c;
    private List<ReferenceJob> gens;
    private List<TrackJobs> tracks;
    private Set<ReferenceJob> invalidGens;

    public DeletionThread(DataAdminController c, List<ReferenceJob> gens, List<TrackJobs> tracks){
        super();
        this.c = c;
        this.gens = gens;
        this.tracks = tracks;
        invalidGens = new HashSet<ReferenceJob>();
    }

    @Override
    protected Object doInBackground() {
        CentralLookup.getDefault().add(this);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of data");

        if(!tracks.isEmpty()){
            c.updateDataAdminStatus("Starting deletion of tracks:");
            for(Iterator<TrackJobs> it = tracks.iterator(); it.hasNext(); ){
                TrackJobs t = it.next();
                try {
                    ProjectConnector.getInstance().deleteTrack(t.getID());
                    c.updateDataAdminStatus("Completed deletion of\""+t.getDescription()+"\"");

                } catch (StorageException ex) {
                    c.updateDataAdminStatus("Deletion of \""+t.getDescription()+"\" failed");
                    // if this track fails, do not delete runs and genomes that are referenced by this track
                  //  invalidRuns.add(t.getRunJob());
                    invalidGens.add(t.getRefGen());
                    Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
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

        CentralLookup.getDefault().remove(this);
    }

}
