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

package de.cebitec.readxplorer.api;


/**
 * A ReadXplorer specific RuntimeException indicating H2/SQL errors.
 *
 * @author Oliver Schwengers
 */
public class FileException extends ReadXplorerException {

    private static final long serialVersionUID = 8835896;


    /**
     * Creates a new instance of <code>FileException</code> without detail
     * message.
     * <p>
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public FileException( Throwable cause ) {
        super( Type.File, cause );
    }


    /**
     * Constructs an instance of <code>FileException</code> with the
     * specified detail message.
     * <p>
     * @param msg   the detail message.
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public FileException( String msg, Throwable cause ) {
        super( msg, Type.File, cause );
    }


    /**
     * Constructs an instance of <code>FileException</code> with the
     * specified detail message.
     * <p>
     * @param userMsg A message to display to the user.
     * @param errMsg  The detail message.
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link #getCause()} method). (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public FileException( String userMsg, String errMsg, Throwable cause ) {
        super( userMsg, errMsg, Type.File, cause );
    }


}
