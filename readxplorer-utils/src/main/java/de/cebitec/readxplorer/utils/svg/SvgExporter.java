/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.utils.svg;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;


/**
 * Interface for easily exchanging SVG exporter classes.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface SvgExporter {

    /**
     * Paints the whole given component on the internal SVG exporter instance.
     *
     * @param component The component to export as SVG
     * @param dim       The dimension of the component to paint
     */
    void paintToExporter( Component component, Dimension dim );


    /**
     * Exports the previously painted component to an SVG file.
     *
     * @param fileLocation The file to store
     *
     * @throws IOException If something fails
     */
    void exportSvg( String fileLocation ) throws IOException;


}
