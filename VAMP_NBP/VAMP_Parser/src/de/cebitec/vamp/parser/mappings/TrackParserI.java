package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observer;
//import java.util.HashMap;

/**
 * Interface for track parsers.
 *
 * @author ddoppmeier, rhilker
 */
public interface TrackParserI {

    // The comment below originates from the RUN domain. It is left here for a quick restore.

//    public abstract ParsedTrack parseMappings(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID,
//            String sequenceString, Observer observer) throws ParsingException;

        /**
         * Parses a track job into a parsed track.
         * @param trackJob the track job to parse
         * @param sequenceString the sequence string of the reference
         * @param observer an observer of the parsing
         * @return the parsed track with all track information
         * @throws ParsingException exectption if something went wrong during parsing process
         */
       public abstract ParsedTrack parseMappings(TrackJob trackJob, String sequenceString,
               Observer observer) throws ParsingException;

       /**
        * Method for parsing mapping data from a track job into a run data object (ParsedRun).
        * 
        * @param trackJob the track job to parse
        * @return the parsed run containing the run data
        * @throws ParsingException exception if the parsing encountered an error
        * @deprecated Since the RUN domain has been excluded this method is not needed anymore!
        */
       @Deprecated
    public ParsedRun parseMappingforReadData(TrackJob trackJob)throws ParsingException;

}
