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
package de.cebitec.readXplorer.util;

import java.awt.Color;

/**
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class ColorUtils {
    
    private ColorUtils() {
    }
    
    /**
     * Calculates and returns an updated color for the saturation and brigthness
     * of the given color according to the given value in relation to the given
     * maximum value.
     * @param currentValue The value to represent by saturation and brigthness
     * @param maxValue The maximum accepted value possible for the currentValue
     * @param origColor The original color to update
     * @return The color with the updated saturation and brigthness 
     * corresponding to the given phred score.
     */
    public static Color getAdaptedColor(int currentValue, int maxValue, Color origColor) {
        float[] hsbColorValues = Color.RGBtoHSB(origColor.getRed(), origColor.getGreen(), origColor.getBlue(), null);
        float sAndB = ColorProperties.MIN_SATURATION_AND_BRIGTHNESS + currentValue * ColorUtils.getSaturationAndBrightnessPerUnit(maxValue);
        return Color.getHSBColor(hsbColorValues[0], sAndB, sAndB);
    }
    
    /**
     * Calculates the saturation and brigthness value of a color per given unit.
     * @param numberOfUnits number of units to be represented in brightness
     * and saturation
     * @return The saturation and brigthness value of a color per given unit.
     */
    public static float getSaturationAndBrightnessPerUnit(int numberOfUnits) {
        return ColorProperties.SPAN_SATURATION_AND_BRIGTHNESS / numberOfUnits;
    }

}
