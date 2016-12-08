/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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
package de.cebitec.readxplorer.ui.options;

import org.netbeans.spi.options.OptionsPanelController;

/**
 * Locations options panel controller.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@OptionsPanelController.SubRegistration(
         displayName = "#AdvancedOption_DisplayName_Locations",
         keywords = "#AdvancedOption_Keywords_Locations",
         keywordsCategory = "Advanced/Directories"
)
public final class LocationsOptionsPanelController extends ChangeListeningOptionsPanelController {

    private LocationsPanel panel;


    /**
     * {@inheritDoc}
     */
    @Override
    protected LocationsPanel getPanel() {
        if( panel == null ) {
            panel = new LocationsPanel( this );
        }
        return panel;
    }


}
