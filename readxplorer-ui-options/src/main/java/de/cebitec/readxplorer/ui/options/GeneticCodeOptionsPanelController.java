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


/**
 * Controller for genetic code options.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@OptionsPanelController.TopLevelRegistration( categoryName = "#OptionsCategory_Name_GeneticCode",
                                              iconBase = "de/cebitec/readxplorer/ui/options/geneticCode.png",
                                              keywords = "#OptionsCategory_Keywords_GeneticCode",
                                              keywordsCategory = "GeneticCode",
                                              position = 905 )
public final class GeneticCodeOptionsPanelController extends ChangeListeningOptionsPanelController {

    private GeneticCodePanel panel;


    /**
     * {@inheritDoc}
     */
    @Override
    protected OptionsPanel getPanel() {
        if( panel == null ) {
            panel = new GeneticCodePanel( this );
        }
        return panel;
    }


}
