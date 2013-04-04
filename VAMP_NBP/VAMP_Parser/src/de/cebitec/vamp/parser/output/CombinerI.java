package de.cebitec.vamp.parser.output;

import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;

/**
 * Interface for combining any kind of data.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface CombinerI extends Observable, Observer {
    
    /**
     * Combines some data.
     * @return true, if the method succeeded, false otherwise
     * @throws ParsingException
     * @throws OutOfMemoryError  
     */
    public boolean combineData() throws ParsingException, OutOfMemoryError;
    
}
