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


import org.openide.WizardDescriptor;


/**
 * A Change listening wizard panel, which can control enabling and disabling
 * of the finish button.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public abstract class ChangeListeningFinishWizardPanel extends ChangeListeningWizardPanel
        implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    /**
     * A Change listening wizard panel, which can control enabling and disabling
     * of the finish button.
     * <p>
     * @param errorMsg The error message to display, in case the required
     *                 information for this wizard panel is not set correctly.
     */
    public ChangeListeningFinishWizardPanel( String errorMsg ) {
        super( errorMsg );
    }


    @Override
    public boolean isFinishPanel() {
        return this.isValid();
    }


}
