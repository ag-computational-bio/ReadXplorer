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

package de.cebitec.readxplorer.utils;


/**
 * Utils for checking the operating system.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class OsUtils {

    private static String os = null;
    private static boolean isWindows = false;


    /**
     * @return Name of the operating system.
     */
    public static String getOsName() {
        if( os == null ) {
            os = System.getProperty( "os.name" );
            isWindows = os.toLowerCase().startsWith( "windows" );
        }
        return os;
    }


    /**
     * @return <code>true</code> if the current system is a Windows system,
     *         <code>false</code> otherwise.
     */
    public static boolean isWindows() {
        getOsName();
        return isWindows;
    }


    /**
     * Do not instantiate.
     */
    private OsUtils() {
    }


}
