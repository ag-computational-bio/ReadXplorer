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

package de.cebitec.readXplorer.ui.dataAdministration;


import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.parser.Job;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.ui.dataAdministration.actions.DataAdminWizardAction;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningFinishWizardPanel;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.WizardDescriptor;


public class DataAdminWizardSelectionPanel extends ChangeListeningFinishWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectionCard component;


    public DataAdminWizardSelectionPanel() {
        super( "" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if( component == null ) {
            component = new SelectionCard();
        }
        return component;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public void readSettings( WizardDescriptor settings ) {

        // get deletable references and tracks
        Map<String, List<? extends Job>> possibleJobs = getDeletableReferencesAndTracks();
        List<ReferenceJob> refJobs = (List<ReferenceJob>) possibleJobs.get( "references" );
        List<TrackJob> trackJobs = (List<TrackJob>) possibleJobs.get( "tracks" );
        component.setSelectableJobs( refJobs, trackJobs );

        super.readSettings( settings );
    }


    @Override
    public void storeSettings( WizardDescriptor settings ) {
        settings.putProperty( DataAdminWizardAction.PROP_REFS2DEL, component.getRef2DelJobs() );
        settings.putProperty( DataAdminWizardAction.PROP_TRACK2DEL, component.getTrack2DelJobs() );
    }


    private Map<String, List<? extends Job>> getDeletableReferencesAndTracks() {
        List<ReferenceJob> refJobs = new ArrayList<>();
        List<TrackJob> trackJobs = new ArrayList<>();
        HashMap<Integer, ReferenceJob> indexedRefs = new HashMap<>();

        try {

            List<PersistentReference> refs = ProjectConnector.getInstance().getGenomes();
            for( PersistentReference ref : refs ) {
                // File and parser parameter meaningless in this context
                ReferenceJob r = new ReferenceJob( ref.getId(), null, null, ref.getDescription(), ref.getName(), ref.getTimeStamp() );
                indexedRefs.put( r.getID(), r );
                refJobs.add( r );
            }

            List<PersistentTrack> dbTracks = ProjectConnector.getInstance().getTracks();
            for( PersistentTrack dbTrack : dbTracks ) {
                // File and parser, refgenjob, runjob parameters meaningless in this context
                TrackJob t = new TrackJob( dbTrack.getId(), new File( dbTrack.getFilePath() ),
                                           dbTrack.getDescription(), indexedRefs.get( dbTrack.getRefGenID() ),
                                           null, false, dbTrack.getTimestamp() );

                // register dependent tracks at genome and run
                ReferenceJob gen = indexedRefs.get( dbTrack.getRefGenID() );
                gen.registerTrackWithoutRunJob( t ); //TODO: check if track without run job is still needed
                trackJobs.add( t );
            }

        }
        catch( OutOfMemoryError e ) {
            VisualisationUtils.displayOutOfMemoryError( this.component );
        }

        // fill result map
        Map<String, List<? extends Job>> deletableStuff = new HashMap<>();
        deletableStuff.put( "references", refJobs );
        deletableStuff.put( "tracks", trackJobs );
        return deletableStuff;
    }


}
