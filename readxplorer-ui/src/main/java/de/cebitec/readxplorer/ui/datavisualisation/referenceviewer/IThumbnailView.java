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

package de.cebitec.readxplorer.ui.datavisualisation.referenceviewer;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.ui.controller.ViewController;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;


/**
 * @author dkramer
 */
public interface IThumbnailView {

    /**
     * This method is used after selecting an feature for which all tracks for a
     * given reference should be viewed in Thumbnails.
     * <p>
     * @param feature
     * @param refViewer the currently viewed ReferenceViewer
     */
    void addFeatureToList( PersistentFeature feature, ReferenceViewer refViewer );


    void showThumbnailView( ReferenceViewer refViewer );


    void showThumbnailView( ReferenceViewer refViewer, ViewController con );


    void removeAllFeatures( ReferenceViewer refViewer );


    void removeCertainFeature( PersistentFeature feature );


    void showPopUp( PersistentFeature feature, ReferenceViewer refViewer, MouseEvent e, JPopupMenu popUp );


    void showTablePopUp( JTable table, ReferenceViewer refViewer, MouseEvent e );


}
