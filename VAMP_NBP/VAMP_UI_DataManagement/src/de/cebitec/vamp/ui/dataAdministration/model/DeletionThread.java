package de.cebitec.vamp.ui.dataAdministration.model;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.StorageException;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJobs;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ddoppmeier
 */
public class DeletionThread extends SwingWorker<Object, Object>{

    private List<ReferenceJob> gens;
    private List<TrackJobs> tracks;
    private Set<ReferenceJob> invalidGens;
    private InputOutput io;
    private ProgressHandle ph;
    private int workunits;

    public DeletionThread(List<ReferenceJob> gens, List<TrackJobs> tracks){
        super();
        this.gens = gens;
        this.tracks = tracks;
        invalidGens = new HashSet<ReferenceJob>();

        this.io = IOProvider.getDefault().getIO("Deletion", false);
        this.ph = ProgressHandleFactory.createHandle("Deletion");
        this.workunits = this.gens.size() + this.tracks.size();
    }

    @Override
    protected Object doInBackground() {
        CentralLookup.getDefault().add(this);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();

        ph.start(workunits);
        workunits = 0;

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting deletion of data");

        if(!tracks.isEmpty()){
            io.getOut().println("Starting deletion of tracks:");
            ph.progress("deletion of tracks", workunits);
            for(Iterator<TrackJobs> it = tracks.iterator(); it.hasNext(); ){
                TrackJobs t = it.next();
                ph.progress(++workunits);
                try {
                    ProjectConnector.getInstance().deleteTrack(t.getID());
                    io.getOut().println("Completed deletion of\""+t.getDescription()+"\"");

                } catch (StorageException ex) {
                    io.getOut().println("Deletion of \""+t.getDescription()+"\" failed");
                    // if this track fails, do not delete runs and genomes that are referenced by this track
                  //  invalidRuns.add(t.getRunJob());
                    invalidGens.add(t.getRefGen());
                    Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            io.getOut().println("");
        }

        if(!gens.isEmpty()){
            io.getOut().println("Starting deletion of references:");
            ph.progress("deletion of references", workunits);
            for(Iterator<ReferenceJob> it = gens.iterator(); it.hasNext(); ){
                ReferenceJob r = it.next();
                ph.progress(++workunits);
                if(invalidGens.contains(r)){
                    io.getOut().println("Because of a failure during earlier deletion of a track, that references this dataset, \""+r.getDescription()+"\" could not be deleted");
                } else {
                    try {
                        ProjectConnector.getInstance().deleteGenome(r.getID());
                        io.getOut().println("Completed deletion of \""+r.getDescription()+"\"");
                    } catch (StorageException ex) {
                        io.getOut().println("Deletion of \""+r.getDescription()+"\" failed");
                        Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            io.getOut().println("");
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Completed Deletion of Data");

        return null;
    }

    @Override
    protected void done(){
        super.done();
        io.getOut().println("Finished");
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove(this);
    }

}
