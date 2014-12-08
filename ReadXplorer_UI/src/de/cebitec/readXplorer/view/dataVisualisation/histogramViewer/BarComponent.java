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

package de.cebitec.readXplorer.view.dataVisualisation.histogramViewer;


import de.cebitec.readXplorer.util.ColorProperties;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;


/**
 * Visual component representing a bar (rectangle) in a certain color.
 *
 * @author ddoppmeier
 */
public class BarComponent extends JComponent {

    private final static long serialVersionUID = 38461064;

    private final int height;
    private final int width;
    private final Color color;


    /**
     * Visual component representing a bar (rectangle) in a certain color.
     * <p>
     * @param height height of the rectangle
     * @param width  width of the rectangle
     * @param color  color of the rectangle
     */
    public BarComponent( int height, int width, Color color ) {
        super();
        this.height = height;
        this.width = width;
        this.color = color;
    }


    @Override
    public void paintComponent( Graphics graphics ) {
        graphics.setColor( color );
        graphics.fillRect( 0, 0, width - 1, height - 1 );

        graphics.setColor( ColorProperties.BLOCK_BORDER );
        graphics.drawRect( 0, 0, width - 1, height - 1 );
    }


}
