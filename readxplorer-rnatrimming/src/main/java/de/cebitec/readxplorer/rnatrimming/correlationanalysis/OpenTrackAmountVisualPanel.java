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

package de.cebitec.readxplorer.rnatrimming.correlationanalysis;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import de.cebitec.readxplorer.ui.dialogmenus.OpenTracksVisualPanel;


/**
 * A track selection panel requiring the selection of a certain amount of tracks
 * to proceed.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class OpenTrackAmountVisualPanel extends OpenTracksVisualPanel {

    private static final long serialVersionUID = 1L;

    private Integer selectedAmount;
    private final TrackListPanel parent;


    /**
     * A track selection panel requiring the selection of a certain amount of
     * tracks to proceed.
     * <p>
     * @param referenceID id of the reference genome
     * @param parent      the parent track list panel
     */
    public OpenTrackAmountVisualPanel( int referenceID, TrackListPanel parent ) {
        super( referenceID );
        this.parent = parent;
        this.selectedAmount = -1;
    }


    @Override
    public boolean isRequiredInfoSet() {
        boolean isRequiredInfoSet = super.isRequiredInfoSet() && this.getSelectAmount() > -1
                                    && this.getSelectAmount() == this.getSelectedTracks().size();
        if( !isRequiredInfoSet ) {
            this.parent.setErrorMsg( "Please select " + this.getSelectAmount() + " tracks! (You selected "
                                     + this.getAllMarkedNodes().size() + ")" );
        }
        firePropertyChange( ChangeListeningWizardPanel.PROP_VALIDATE, null, isRequiredInfoSet );
        return isRequiredInfoSet;
    }


    /**
     * @return the maximumAmount
     */
    public int getSelectAmount() {
        return selectedAmount;
    }


    /**
     * @param maximumAmount the maximumAmount to set
     */
    public void setSelectAmount( Integer maximumAmount ) {
        this.selectedAmount = maximumAmount;
    }


}
