package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsingException;

/**
 * Interface for classes preprocessing a track job.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface PreprocessorI {
    
    /**
     * If any preprocessing is available for a track job, it is performed here.
     * @param trackJob the trackjob to preprocess
     * @return any object, depending on the implementing parser class
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError;
    
}
