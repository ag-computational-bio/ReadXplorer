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

package de.cebitec.readxplorer.parser.mappings;


import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsingException;


/**
 * Interface for classes preprocessing a track job.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface PreprocessorI {

    /**
     * If any preprocessing is available for a track job, it is performed here.
     * <p>
     * @param trackJob the trackjob to preprocess
     * <p>
     * @return any object, depending on the implementing parser class
     * <p>
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    Object preprocessData( TrackJob trackJob ) throws ParsingException, OutOfMemoryError;


}
