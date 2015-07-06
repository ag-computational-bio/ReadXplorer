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

package de.cebitec.readxplorer.ui.visualisation;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.tablevisualization.TablePanel;
import java.util.Collection;


/**
 * A utility class providing methods for visualizing tables.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class TableVisualizationHelper {

    /**
     * Do not instantiate.
     */
    private TableVisualizationHelper() {
    }


    /**
     * A utility method for connecting a TablePanel to a corresponding
     * ReferenceViewer. It first checks if a ReferenceViewer for the given
     * PersistentReference is already open. If this is not the case, a new
     * AppPanelTopComponent with the desired ReferenceViewer is opened. In both
     * case, the BoundsInfoManager of the ReferenceViewer is added to the given
     * TablePanel, enabling updating the viewer position from the TablePanel.
     * <p>
     * @param ref        Reference for which the viewer shall be checked
     * @param tablePanel Any table panel, which shall be able to update
     *                   genomic positions of the reference
     */
    public static void checkAndOpenRefViewer( PersistentReference ref, TablePanel tablePanel ) {
        @SuppressWarnings( "unchecked" )
        Collection<ViewController> viewControllers = (Collection<ViewController>) CentralLookup.getDefault().lookupAll( ViewController.class );
        boolean alreadyOpen = false;
        for( ViewController tmpVCon : viewControllers ) {
            if( tmpVCon.getCurrentRefGen().equals( ref ) ) {
                alreadyOpen = true;
                tablePanel.setBoundsInfoManager( tmpVCon.getBoundsManager() );
                break;
            }
        }

        if( !alreadyOpen ) {
            //open reference genome now
            AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
            appPanelTopComponent.open();
            ViewController viewController = appPanelTopComponent.getLookup().lookup( ViewController.class );
            viewController.openGenome( ref );
            appPanelTopComponent.setName( viewController.getDisplayName() );
            appPanelTopComponent.requestActive();
            tablePanel.setBoundsInfoManager( viewController.getBoundsManager() );
        }
    }


}
