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
package de.cebitec.readXplorer.view.dataVisualisation;

import de.cebitec.readXplorer.databackend.dataObjects.Mapping;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.util.classification.FeatureType;
import de.cebitec.readXplorer.util.classification.MappingClass;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import java.awt.Rectangle;
import java.util.List;

/**
 * Utility class for common methods to aid painting of data.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class PaintUtilities {

    /**
     * No instantiation allowed.
     */
    private PaintUtilities() {
    }
    
    /**
     * Calculates the physical (pixel) boundaries of some component with a start
     * and stop position in reference coordinates.
     * @param refStart start of the component in reference coordinates
     * @param refStop stop of the component in reference coordinates
     * @param viewer The viewer in which the component is shown later
     * @param phyLeft Left physical (pixel) of the component in the viewer
     * @param height Height to set for the rectangle
     * @return 
     */
    public static Rectangle calcBlockBoundaries(int refStart, int refStop, AbstractViewer viewer, int phyLeft, int height) {
        int startPixel = (int) viewer.getPhysBoundariesForLogPos(refStart).getLeftPhysBound();
        int stopPixel = (int) viewer.getPhysBoundariesForLogPos(refStop).getRightPhysBound();
        int absLength = stopPixel - startPixel;
        int minBlockLength = 3;
        absLength = absLength < minBlockLength ? minBlockLength : absLength;
        return new Rectangle(startPixel - phyLeft, 0, absLength, height);
    }
    
    /**
     * Returns true if the type of the current mapping is in the exclusion list.
     * This means it should not be displayed.
     * @param m the mapping to test, if it should be displayed
     * @param exclusionList
     * @return true, if the mapping should be excluded from being displayed,
     * false otherwise
     */
    public static boolean inExclusionList(Mapping m, List<Classification> exclusionList) {
        return (m.getDifferences() == 0 && exclusionList.contains(MappingClass.PERFECT_MATCH))
                || (m.getDifferences() > 0 && m.isBestMatch() && exclusionList.contains(MappingClass.BEST_MATCH)) 
                || (!m.isUnique() && exclusionList.contains(FeatureType.MULTIPLE_MAPPED_READ))
                || (m.getDifferences() > 0 && !m.isBestMatch() && exclusionList.contains(MappingClass.COMMON_MATCH));
    }
}
