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

package de.cebitec.readxplorer.ui.datavisualisation.abstractviewer;


import de.cebitec.readxplorer.api.constants.Colors;
import de.cebitec.readxplorer.api.enums.RegionType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;


/**
 * Represents a region, which is highlighted in a certain colour.
 * <p>
 * @author ddoppmeier
 */
public class JRegion extends JComponent {

    private static final long serialVersionUID = 279564654;

    private Color backgroundColor = Colors.START_CODON;
    private RegionType type = RegionType.Start;
    private final int genomeStart;
    private final int genomeStop;


    public JRegion( int length, int height, RegionType type, int genomeStart, int genomeStop ) {
        super();
        this.setSize( new Dimension( length, height ) );
        this.type = type;
        this.genomeStart = genomeStart;
        this.genomeStop = genomeStop;

        if( type == RegionType.Pattern ) {
            this.backgroundColor = Colors.PATTERN;
        } else if( type == RegionType.Stop ) {
            this.backgroundColor = Colors.STOP_CODON;
        } else if( type == RegionType.CDS ) {
            this.backgroundColor = Colors.HIGHLIGHT_FILL;
        } // else { //currently not needed, because start codon color already set.
    }


    @Override
    protected void paintComponent( Graphics graphics ) {
        super.paintComponent( graphics );
        graphics.setColor( this.backgroundColor );
        graphics.fillRect( 0, 0, this.getSize().width - 1, this.getSize().height - 1 );
    }


    /**
     * Sets the background color of this component
     * <p>
     * @param backgroundColor the background color to set
     */
    public void setBackgroundColor( final Color backgroundColor ) {
        this.backgroundColor = backgroundColor;
    }


    public RegionType getType() {
        return this.type;
    }


    /**
     * @return The start position of this region in the genome.
     */
    public int getStart() {
        return genomeStart;
    }


    /**
     * @return The stop position of this region in the genome.
     */
    public int getStop() {
        return genomeStop;
    }


}
