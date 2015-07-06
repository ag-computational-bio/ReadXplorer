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

package de.cebitec.readxplorer.ui.datavisualisation.basepanel;


/**
 * This interface defines listeners for changes in the viewer's control panel.
 * <p>
 * @author ddoppmeier, rhilker
 */
public interface AdjustmentPanelListenerI {

    /**
     * Notify listeners of changes of the zoom level
     * <p>
     * @param zoomValue new zoom value to be applied
     */
    public void zoomLevelUpdated( int zoomValue );


    /**
     * Notify listeners of changes in the navigation bar
     * <p>
     * @param navigatorBarValue updated current position of the genome, that is
     *                          to be shown
     */
    public void navigatorBarUpdated( int navigatorBarValue );


    /**
     * Notify listeners of the selection of a new chromosome.
     * <p>
     * @param activeChromId Id of the new active chromosome
     */
    public void chromosomeChanged( int activeChromId );


}
