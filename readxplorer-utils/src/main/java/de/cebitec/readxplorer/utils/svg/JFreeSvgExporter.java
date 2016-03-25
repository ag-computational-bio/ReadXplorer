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
import java.io.File;
import java.io.IOException;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;


/**
 * JFreeSVG SVG exporter implementation.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class JFreeSvgExporter implements SvgExporter {

    SVGGraphics2D svgGenerator = null;


    /**
     * {@inheritDoc }
     */
    @Override
    public void paintToExporter( Component container, Dimension dim ) {
        svgGenerator = new SVGGraphics2D( dim.width, dim.height );
        container.printAll( svgGenerator );
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void exportSvg( String fileLocation ) throws IOException {
        SVGUtils.writeToSVG( new File( fileLocation ), svgGenerator.getSVGElement() );
    }


}
