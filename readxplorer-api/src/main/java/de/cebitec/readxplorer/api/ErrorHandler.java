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

package de.cebitec.readxplorer.api;


/**
 * Interface for exception handling and exception logging implementations.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface ErrorHandler {

    /**
     * Appropriately handles a throwable. This includes showing the Throwable
     * message to the user.
     *
     * @param t Throwable to handle
     */
    public void handle( Throwable t );


    /**
     * Appropriately handles a throwable. This includes logging the Throwable
     * and showing the Throwable message to the user. The header
     * is useful to display something else as message header than the Throwable
     * type (default).
     *
     * @param t      Throwable to handle
     * @param header Header for displaying a different header than the default.
     */
    public void handle( Throwable t, String header );
    

}
