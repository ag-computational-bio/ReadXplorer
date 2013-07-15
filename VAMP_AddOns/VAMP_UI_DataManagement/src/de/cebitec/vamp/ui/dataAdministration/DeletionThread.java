package de.cebitec.vamp.ui.dataAdministration;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.StorageException;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJob;
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
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Thread for the deletion of tracks and references from the ReadXplorer DB.
 *
 * @author ddoppmeier
 */
public class DeletionThread extends SwingWorker<Object, Object> {

    private List<ReferenceJob> references;
    private List<TrackJob> tracks;
    private Set<ReferenceJob> invalidGens;
    private InputOutput io;
    private ProgressHandle ph;
    private int workunits;

    /**
     * Thread for the deletion of tracks and references from the ReadXplorer DB.
     * @param references the list of references to delete
     * @param tracks the list of tracks to delete
     */
    public DeletionThread(List<ReferenceJob> references, List<TrackJob> tracks) {
        super();
        this.references = references;
        this.tracks = tracks;
        invalidGens = new HashSet<>();

        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(DeletionThread.class, "DeletionThread.ouptut.name"), false);
        this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.progress.name"));
        this.workunits = this.references.size() + this.tracks.size();
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

        // when deleting only one item there would be always 100% otherwise
        ph.start(workunits == 1 ? 2 : workunits);
        workunits = 0;

        Logger.getLogger(DeletionThread.class.getName()).log(Level.INFO, "Starting deletion of data");

        if (!tracks.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.start.track") + ":");
            ph.progress(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.progress.track"), workunits);
            for (Iterator<TrackJob> it = tracks.iterator(); it.hasNext();) {
                TrackJob t = it.next();
                ph.progress(++workunits);
                try {
                    ProjectConnector.getInstance().deleteTrack(t.getID());
                    io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.completed.before") + " \"" + t.getDescription() + "\" " + NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.completed.after"));

                } catch (StorageException ex) {
                    io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.failed.before") + " \"" + t.getDescription() + "\" " + NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.failed.after"));
                    // if this track fails, do not delete runs and genomes that are referenced by this track
                    //  invalidRuns.add(t.getRunJob());
                    invalidGens.add(t.getRefGen());
                    Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            io.getOut().println("");
        }

        if (!references.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.start.ref") + ":");
            ph.progress(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.progress.ref"), workunits);
            for (Iterator<ReferenceJob> it = references.iterator(); it.hasNext();) {
                ReferenceJob r = it.next();
                ph.progress(++workunits);
                if (invalidGens.contains(r)) {
                    io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.error.before") + " \"" + r.getDescription() + "\" " + NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.error.after"));
                } else {
                    try {
                        ProjectConnector.getInstance().deleteGenome(r.getID());
                        io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.completed.before") + " \"" + r.getDescription() + "\" " + NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.completed.after"));
                    } catch (StorageException ex) {
                        io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.failed.before") + " \"" + r.getDescription() + "\" " + NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.failed.after"));
                        Logger.getLogger(DeletionThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            io.getOut().println("");
        }

        Logger.getLogger(DeletionThread.class.getName()).log(Level.INFO, "Completed Deletion of Data");

        return null;
    }

    @Override
    protected void done() {
        super.done();
        ph.progress(workunits);
        io.getOut().println(NbBundle.getMessage(DeletionThread.class, "MSG_DeletionThread.deletion.finished"));
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove(this);
    }
}
