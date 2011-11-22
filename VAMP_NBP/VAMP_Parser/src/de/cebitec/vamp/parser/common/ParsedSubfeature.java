package de.cebitec.vamp.parser.common;

/**
 * @author rhilker
 * 
 * A ParsedSubfeature is a subfeature of a ParsedFeature. Thus, it contains a reference
 * to it's parent feature (featureId) and a start and stop position.
 */
public class ParsedSubfeature {
        
    private int start;
    private int stop;

    /**
     * Creates a new subfeature.
     * @param start absolute start of the subfeature in regard to the reference genome
     * @param stop absolute stop of the subfeature in regard to the reference genome
     */
    public ParsedSubfeature(int start, int stop) {
        this.start = start;
        this.stop = stop;
    }

    /**
     * @return the absolute start of the subfeature in regard to the reference genome
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the absolute stop of the subfeature in regard to the reference genome
     */
    public int getStop() {
        return stop;
    }
    
}
