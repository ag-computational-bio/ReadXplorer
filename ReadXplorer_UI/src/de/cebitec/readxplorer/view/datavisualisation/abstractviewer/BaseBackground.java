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

package de.cebitec.readxplorer.view.datavisualisation.abstractviewer;


import de.cebitec.readxplorer.utils.ColorProperties;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;


/**
 * Creates a colored rectangle as background for a DNA base in a base specific
 * color.
 *
 * @author jstraube, Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class BaseBackground extends JComponent {

    private static final long serialVersionUID = 27956465;
    private String base = null;


    /**
     * Creates a colored rectangle as background for a DNA base in a base
     * specific color.
     * <p>
     * @param width  Width of a single base in pixels.
     * @param height Height of a single base in pixels
     * @param base   Base for which the background shall be created
     */
    public BaseBackground( int width, int height, String base ) {
        super();
        this.setSize( new Dimension( width, height ) );
        this.base = base;
    }


    @Override
    protected void paintComponent( Graphics graphics ) {

        super.paintComponent( graphics );
        switch( base ) {
            case "A":
                graphics.setColor( ColorProperties.BACKGROUND_A );
                break;
            case "C":
                graphics.setColor( ColorProperties.BACKGROUND_C );
                break;
            case "G":
                graphics.setColor( ColorProperties.BACKGROUND_G );
                break;
            case "T":
                graphics.setColor( ColorProperties.BACKGROUND_T );
                break;
            case "-":
                graphics.setColor( ColorProperties.BACKGROUND_READGAP );
                break;
            case "N":
                graphics.setColor( ColorProperties.BACKGROUND_N );
                break;
            default:
                graphics.setColor( ColorProperties.BACKGROUND_BASE_UNDEF );
        }

        graphics.fillRect( 0, 0, this.getSize().width - 1, this.getSize().height - 1 );
    }


}
