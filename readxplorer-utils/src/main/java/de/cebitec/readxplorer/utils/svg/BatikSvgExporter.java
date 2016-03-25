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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


/**
 * Batik SVG exporter implementation.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class BatikSvgExporter implements SvgExporter {

    private SVGGraphics2D svgGenerator = null;


    /**
     * {@inheritDoc }
     */
    @Override
    public void paintToExporter( Component container, Dimension dim ) {

        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument( svgNS, "svg", null );
        svgGenerator = new SVGGraphics2D( document );
        svgGenerator.setSVGCanvasSize( dim );
        container.paintAll( svgGenerator );
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void exportSvg( String fileLocation ) throws IOException {
        try( Writer out = new OutputStreamWriter( new FileOutputStream( fileLocation ), "UTF-8" ); ) {
            svgGenerator.stream( out, false );
        }
    }


}
