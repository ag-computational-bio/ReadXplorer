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

/**
 * A region marked by a start and stop position and if it should
 * be read in fwd or reverse direction. Furthermore, it holds the type of the
 * region.
 * 
 * @author ddoppmeier, rhilker
 */
public class Region {

    private int start;
    private int stop;
    private boolean isForwardStrand;
    private int type;

    /**
     * A region marked by a start and stop position and if it should be read in
     * fwd or reverse direction. Furthermore, it holds the type of the region.
     * @param start the start of the region as base position
     * @param stop the stop of the region as base position
     * @param isForwardStrand true, if it is on the fwd strand, false otherwise
     * @param type type of the region. Use Properties.CDS, Properties.START, Properties.STOP,
     *          Properties.PATTERN or Properties.ALL
     */
    public Region(int start, int stop, boolean isForwardStrand, int type){
        this.start = start;
        this.stop = stop;
        this.isForwardStrand = isForwardStrand;
        this.type = type;
    }

    /**
     * @return the start of this region = the starting position in the genome.
     */
    public int getStart() {
        return this.start;
    }

    /**
     * @return the stop of this region. = the ending position in the genome.
     */
    public int getStop() {
        return this.stop;
    }

    /**
     * @return true, if the region is located on the fwd strand, false otherwise
     */
    public boolean isForwardStrand(){
        return this.isForwardStrand;
    }

    /**
     * @return the type of the region. Either Properties.CDS, Properties.START, Properties.STOP,
     *          Properties.PATTERN or Properties.ALL
     */
    public int getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Region) {
            Region otherRegion = (Region) other;
            if (    otherRegion.isForwardStrand() == this.isForwardStrand()
                    && otherRegion.getType() == this.getType()
                    && otherRegion.getStart() == this.getStart() 
                    && otherRegion.getStop() == this.getStop()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.start;
        hash = 23 * hash + this.stop;
        hash = 23 * hash + (this.isForwardStrand ? 1 : 0);
        hash = 23 * hash + this.type;
        return hash;
    }

}
