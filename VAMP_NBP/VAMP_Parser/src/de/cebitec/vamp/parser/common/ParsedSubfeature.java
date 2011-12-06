package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 * @author rhilker
 * 
 * A ParsedSubfeature is a subfeature of a ParsedFeature. Thus, it contains a reference
 * to it's parent feature (featureId) and a start and stop position.
 */
public class ParsedSubfeature {
        
    private int start;
    private int stop;
    private FeatureType type;

    /**
     * Creates a new subfeature.
     * @param start absolute start of the subfeature in regard to the reference genome
     * @param stop absolute stop of the subfeature in regard to the reference genome
     * @param type the of the subfeature
     */
    public ParsedSubfeature(int start, int stop, FeatureType type) {
        this.start = start;
        this.stop = stop;
        this.type = type;
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

    /**
     * @return the type of the subfeature.
     */
    public FeatureType getType() {
        return type;
    }    
    
}
