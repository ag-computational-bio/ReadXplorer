/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.ui.datamanagement;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.TrackJob;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Thread for the deletion of tracks and references from the ReadXplorer DB.
 * <p>
 * @author ddoppmeier
 */
public class DeletionThread extends SwingWorker<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger( DeletionThread.class.getName() );

    private final List<ReferenceJob> references;
    private final List<TrackJob> tracks;
    private final Set<ReferenceJob> invalidGens;
    private final InputOutput io;
    private final ProgressHandle ph;
    private int workunits;


    /**
     * Thread for the deletion of tracks and references from the ReadXplorer DB.
     * <p>
     * @param references the list of references to delete
     * @param tracks     the list of tracks to delete
     */
    public DeletionThread( List<ReferenceJob> references, List<TrackJob> tracks ) {
        super();
        this.references = references;
        this.tracks = tracks;
        invalidGens = new HashSet<>();

        this.io = IOProvider.getDefault().getIO( NbBundle.getMessage( DeletionThread.class, "DeletionThread.ouptut.name" ), false );
        this.ph = ProgressHandleFactory.createHandle( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.progress.name" ) );
        this.workunits = this.references.size() + this.tracks.size();
    }


    @Override
    protected Object doInBackground() {
        CentralLookup.getDefault().add( this );
        try {
            io.getOut().reset();
        } catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
        io.select();

        // when deleting only one item there would be always 100% otherwise
        ph.start( workunits == 1 ? 2 : workunits );
        workunits = 0;

        LOG.info( "Starting deletion of data" );

        if( !tracks.isEmpty() ) {
            io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.start.track" ) + ":" );
            ph.progress( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.progress.track" ), workunits );
            for( TrackJob t : tracks ) {
                ph.progress( ++workunits );
                try {
                    ProjectConnector.getInstance().deleteTrack( t.getID() );
                    io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.completed.before" ) + " \"" + t.getDescription() + "\" " + NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.completed.after" ) );

                } catch( DatabaseException ex ) {
                    io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.failed.before" ) + " \"" + t.getDescription() + "\" " + NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.failed.after" ) );
                    // if this track fails, do not delete runs and genomes that are referenced by this track
                    //  invalidRuns.add(t.getRunJob());
                    invalidGens.add( t.getRefGen() );
                    LOG.error( null, ex );
                }
            }
            io.getOut().println( "" );
        }

        if( !references.isEmpty() ) {
            io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.start.ref" ) + ":" );
            ph.progress( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.progress.ref" ), workunits );
            for( ReferenceJob r : references ) {
                ph.progress( ++workunits );
                if( invalidGens.contains( r ) ) {
                    io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.error.before" ) + " \"" + r.getDescription() + "\" " + NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.error.after" ) );
                } else {
                    try {
                        ProjectConnector.getInstance().deleteReference( r.getID() );
                        io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.completed.before" ) + " \"" + r.getDescription() + "\" " + NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.completed.after" ) );
                    } catch( DatabaseException ex ) {
                        io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.failed.before" ) + " \"" + r.getDescription() + "\" " + NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.failed.after" ) );
                        LOG.error( null, ex );
                    }
                }
            }
            io.getOut().println( "" );
        }

        LoggerFactory.getLogger( DeletionThread.class.getName() ).info( "Completed Deletion of Data" );

        return null;
    }


    @Override
    protected void done() {
        super.done();
        ph.progress( workunits );
        io.getOut().println( NbBundle.getMessage( DeletionThread.class, "MSG_DeletionThread.deletion.finished" ) );
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove( this );
    }


}
