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

package de.cebitec.readxplorer.parser.common;


/**
 *
 * @author ddoppmeier
 */
public class ParsingException extends Exception {

    private static final long serialVersionUID = 423458724;


    /**
     * Creates a new instance of <code>ParsingException</code> without detail
     * message.
     */
    public ParsingException() {
    }


    /**
     * Constructs an instance of <code>ParsingException</code> with the
     * specified detail message.
     * <p>
     * @param msg the detail message.
     */
    public ParsingException( String msg ) {
        super( msg );
    }


    /**
     * Constructs an instance of <code>ParsingException</code> with the
     * specified {@link Throwable}.
     * <p>
     * @param ex the {@link Throwable}.
     */
    public ParsingException( Throwable ex ) {
        super( ex );
    }


}
