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

package de.cebitec.readxplorer.ui.options;


import org.netbeans.spi.options.OptionsPanelController;


@OptionsPanelController.TopLevelRegistration( categoryName = "#OptionsCategory_Name_Colours",
                                              iconBase = "de/cebitec/readxplorer/ui/options/colorOptions.png",
                                              keywords = "#OptionsCategory_Keywords_Colours",
                                              keywordsCategory = "Colours",
                                              position = 900 )
public final class ColorsOptionsPanelController extends ChangeListeningOptionsPanelController {

    private ColorsPanel panel;


    @Override
    protected OptionsPanel getPanel() {
        if( panel == null ) {
            panel = new ColorsPanel( this );
        }
        return panel;
    }


}
