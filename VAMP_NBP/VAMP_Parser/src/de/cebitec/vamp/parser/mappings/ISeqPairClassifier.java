package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;
import de.cebitec.vamp.parser.common.ParsedTrack;

/**
 * Interface for sequence pair classifier implementation.
 * 
 * @author Rolf Hilker
 */
public interface ISeqPairClassifier {
    
    /**
     * Before classification can start the data has to be set.
     * @param track1 fst track with read 1
     * @param track2 scnd track with read 2
     * @param distance insert distance depicting distance of insert between both ADAPTER sequences = whole fragment length
     * @param deviation maximal deviation in % of the distance
     * @param orientation orientation of the pairs: 0 = fr, 1 = rf, 2 = ff/rr
     */
    public void setData(ParsedTrack track1, ParsedTrack track2, int distance, short deviation, byte orientation);
    
    
    /**
     * Carries out calculations and returns the container containing all necessary
     * data for storing the sequence pairs.
     * @return seq pair container
     */
    public ParsedSeqPairContainer classifySeqPairs();

}
