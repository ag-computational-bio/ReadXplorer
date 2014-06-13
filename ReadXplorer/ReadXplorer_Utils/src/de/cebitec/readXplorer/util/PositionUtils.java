/* 
 * Copyright (C) 2014 Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
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

/**
 * Designed for methods handling any kind of position specific functionality.
 *
 * @author rhilker
 */
public class PositionUtils {

    /**
     * Converts a position string to the corresponding integer position.
     * @param posString position as string, which might include a '_' 
     * @return corresponding position value as integer
     */
    public static int convertPosition(String posString) {
        if (posString.contains("_")) {
            posString = posString.substring(0, posString.lastIndexOf('_'));
        }
        return Integer.parseInt(posString);
    }
}
