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

package de.cebitec.readxplorer.ui.dialogmenus;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.util.List;
import org.openide.WizardDescriptor;


/**
 * A WizardPanel for opening tracks.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class OpenTracksWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_SELECTED_TRACKS = "PROP_SELECTED_TRACKS";
    public static final String PROP_COMBINE_TRACKS = "PROP_COMBINE_TRACKS";

    private OpenTracksVisualPanel component;
    private final String wizardName;
    private final int referenceId;


    /**
     * A WizardPanel for opening tracks.
     * <p>
     * @param wizardName  the name of the corresponding wizard
     * @param referenceId the unique id of the reference genome
     */
    public OpenTracksWizardPanel( String wizardName, int referenceId ) {
        super( "Please select at least one track to continue." );
        this.wizardName = wizardName;
        this.referenceId = referenceId;
    }


    /**
     * @return The OpenTracksVisualPanel, displaying the content of this panel.
     */
    @Override
    public OpenTracksVisualPanel getComponent() {
        if( component == null ) {
            component = new OpenTracksVisualPanel( referenceId );
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            List<PersistentTrack> tracks = this.component.getSelectedTracks();
            wiz.putProperty( getPropSelectedTracks(), tracks );
            wiz.putProperty( getPropCombineTracks(), this.component.isCombineTracks() );
        }
    }


    /**
     * @return The property string for the selected tracks list for the
     *         corresponding wizard.
     */
    public String getPropSelectedTracks() {
        return this.wizardName + PROP_SELECTED_TRACKS;
    }


    /**
     * @return The property string for the combination of tracks for the
     *         corresponding wizard.
     */
    public String getPropCombineTracks() {
        return this.wizardName + PROP_COMBINE_TRACKS;
    }


}
