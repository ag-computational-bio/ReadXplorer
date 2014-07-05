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
package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;

import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public interface RegionFilterI {

    /**
     * Identifies regions. Remember to set setFrameCurrFeature in StartCodonFilter
     * first, if only regions of a specified frame should be identified.
     * @return a list of the identified regions
     */
    public List<Region> findRegions();

    /**
     * Sets the interval to use.
     * @param start the leftmost position of the interval
     * @param stop the rightmost position of the interval
     */
    public void setInterval(int start, int stop);

}
