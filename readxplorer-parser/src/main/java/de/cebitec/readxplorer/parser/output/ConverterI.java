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

package de.cebitec.readxplorer.parser.output;


import de.cebitec.readxplorer.parser.common.ParserI;
import de.cebitec.readxplorer.utils.Observable;


/**
 * Converts the data chosen by the subclasses into another format according to
 * the specific subclass.
 * <p>
 * @author -Rolf Hilker-
 */
public interface ConverterI extends ParserI, Observable {

    /**
     * Converts the data chosen by the subclasses into another format according
     * to the specific subclass.
     * <p>
     * @return true, if the conversion was successful, false otherwise
     * <p>
     * @throws Exception can throw any exception, which has to be specified by
     * the implementation
     */
    boolean convert() throws Exception;


    /**
     * Sets the data to convert. Depends on the implementation.
     * @param data The data to set - as many objects as necessary for the
     *             implementation.
     * <p>
     * @throws IllegalArgumentException
     */
    void setDataToConvert( Object... data ) throws IllegalArgumentException;


}
