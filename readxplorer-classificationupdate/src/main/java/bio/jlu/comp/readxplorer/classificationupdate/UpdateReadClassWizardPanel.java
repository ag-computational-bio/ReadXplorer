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

package bio.jlu.comp.readxplorer.classificationupdate;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningFinishWizardPanel;
import java.awt.Component;
import org.openide.WizardDescriptor;


/**
 * A wizard panel displaying a message to decide, if the read classification
 * for a DB shall be updated.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
class UpdateReadClassWizardPanel extends ChangeListeningFinishWizardPanel {

    private UpdateReadClassVisualPanel component;


    /**
     * A wizard panel displaying a message to decide, if the read classification
     * for a DB shall be updated.
     * <p>
     * @param errorMsg possible error message, none necessary here
     */
    UpdateReadClassWizardPanel( String errorMsg ) {
        super( errorMsg );
    }


    @Override
    public Component getComponent() {
        if( component == null ) {
            component = new UpdateReadClassVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor settings ) {
        //no settings to store, just run the action for the current DB
    }


}
