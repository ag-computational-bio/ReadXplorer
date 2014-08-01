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
package de.cebitec.readXplorer.options;

import org.netbeans.spi.options.OptionsPanelController;

/**
 * 
 * @author Evgeny Anisiforov
 */
@OptionsPanelController.TopLevelRegistration(categoryName = "#OptionsCategory_Name_ObjectCache",  
iconBase = "de/cebitec/readXplorer/options/objectCache.png",
keywords = "#OptionsCategory_Keywords_ObjectCache",
keywordsCategory = "ObjectCache")
@org.openide.util.NbBundle.Messages({"OptionsCategory_Name_ObjectCache=Object Cache", "OptionsCategory_Keywords_ObjectCache=Object Cache"})
public final class ObjectCachePanelController extends ChangeListeningOptionsPanelController {

    private ObjectCachePanel panel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected OptionsPanel getPanel() {
        if (panel == null) {
            panel = new ObjectCachePanel(this); 
        }
        return panel;
    }

}
