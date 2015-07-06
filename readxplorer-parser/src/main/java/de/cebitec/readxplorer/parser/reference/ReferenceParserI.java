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


import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParserI;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.reference.filter.FeatureFilter;
import de.cebitec.readxplorer.utils.Observable;


/**
 * An interface for parsing references.
 *
 * @author ddoppmeier
 */
public interface ReferenceParserI extends ParserI, Observable {

    /**
     * Parses a reference.
     * @param referenceJob The reference job to parse the reference from
     * @param filter The feature filter to use during parsing
     * @return The parsed reference ready for storing
     * @throws ParsingException
     */
    ParsedReference parseReference( ReferenceJob referenceJob, FeatureFilter filter ) throws ParsingException;


}
