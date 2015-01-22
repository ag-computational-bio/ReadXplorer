/*
 * Copyright (C) 2015 tronic3
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

@OptionsPanelController.SubRegistration(
        displayName = "#AdvancedOption_DisplayName_Directories",
        keywords = "#AdvancedOption_Keywords_Directories",
        keywordsCategory = "Advanced/Directories"
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_Directories=Directories", "AdvancedOption_Keywords_Directories=Directories"})
public final class DirectoriesOptionsPanelController extends ChangeListeningOptionsPanelController {

    private DirectoriesPanel panel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected  DirectoriesPanel getPanel() {
        if (panel == null) {
            panel = new DirectoriesPanel(this);
        }
        return panel;
    }

}
