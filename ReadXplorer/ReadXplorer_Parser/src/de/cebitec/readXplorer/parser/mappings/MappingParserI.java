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
package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParserI;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.StatsContainer;
import java.util.Map;

/**
 * Interface to be implemented for all mapping parsers.
 *
 * @author ddoppmeier, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public interface MappingParserI extends ParserI, Observable, PreprocessorI {

    /**
     * Parses the input determined by the track job.
     * @param trackJob the track job to parse
     * @param chromLengthMap the map of chromosome names to chromosome length
     * @return the parsed data object
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    public Object parseInput(TrackJob trackJob, Map<String, Integer> chromLengthMap) throws ParsingException, OutOfMemoryError;
    
    /**
     * Converts some data for the given track job and the given reference.
     * @param trackJob the track job whose data needs to be converted
     * @param chromLengthMap the mapping of chromosome name to chromosome length
     * for this track
     * @return Any object the specific implementation needs
     * @throws ParsingException
     * @throws OutOfMemoryError  
     */
    public Object convert(TrackJob trackJob, Map<String, Integer> chromLengthMap) throws ParsingException, OutOfMemoryError;
        
    /**
     * Sets the given stats container to this parser. Then this parser can
     * store statistics.
     * @param statsContainer the stats container to set
     */
    public void setStatsContainer(StatsContainer statsContainer);

}
