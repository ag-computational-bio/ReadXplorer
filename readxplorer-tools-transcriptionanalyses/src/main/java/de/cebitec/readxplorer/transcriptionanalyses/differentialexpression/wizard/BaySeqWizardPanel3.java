/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import java.util.List;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;


public class BaySeqWizardPanel3 extends ChangeListeningWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private BaySeqVisualPanel3 component;


    public BaySeqWizardPanel3() {
        super( "You must create at least one group to continue." );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public BaySeqVisualPanel3 getComponent() {
        if( component == null ) {
            component = new BaySeqVisualPanel3();
        }
        return component;
    }


    @Override
    public void readSettings( WizardDescriptor wiz ) {
        // use wiz.getProperty to retrieve previous panel state
        List<PersistentTrack> selectedTracks = (List<PersistentTrack>) wiz.getProperty( "tracks" );
        getComponent().updateTrackList( selectedTracks );
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty( "createdGroups", getComponent().getCreatedGroups() );
    }


    @Override
    public void validate() throws WizardValidationException {
        if( getComponent().noGroupCreated() ) {
            throw new WizardValidationException( null, "You must create at least one group to continue.", null );
        }
    }


}
