/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.utils.errorhandling;

import de.cebitec.readxplorer.api.ErrorHandler;


/**
 * Class offering the singleton error handler to use softwarewide.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class ErrorHelper {

    private static ErrorHandler errorHandler;


    /**
     * Do not instantiate.
     */
    private ErrorHelper() {
    }


    /**
     * @return The appropriate handler for the started software version.
     */
    public static ErrorHandler getHandler() {
        if( errorHandler == null ) {
            if( System.getProperty( "guienv" ).equals( "true" ) ) {
                errorHandler = new GuiErrorHandler();
            } else {
                errorHandler = new ConsoleErrorHandler();
            }
        }
        return errorHandler;
    }


}
