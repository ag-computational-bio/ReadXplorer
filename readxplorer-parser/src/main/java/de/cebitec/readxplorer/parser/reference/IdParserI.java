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

package de.cebitec.readxplorer.parser.reference;


import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.utils.Observable;
import java.io.File;
import java.util.List;


/**
 * Parser interface for parsers, which only parse sequence identifiers from a
 * file.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface IdParserI extends Observable {

    /**
     * Parses sequence identifiers from a file.
     * <p>
     * @param fileToParse the file to parse
     * <p>
     * @return the list of sequence identifiers
     * <p>
     * @throws ParsingException
     */
    List<String> getSequenceIds( File fileToParse ) throws ParsingException;


}
