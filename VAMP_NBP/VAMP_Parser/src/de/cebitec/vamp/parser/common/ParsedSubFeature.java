package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 * @author rhilker
 * 
 * A ParsedSubFeature is a sub feature of a ParsedFeature. Thus, it contains a reference
 * to it's parent feature (featureId) and a start and stop position.
 */
public class ParsedSubFeature {
        
    private int start;
    private int stop;
    private FeatureType type;

    /**
     * Creates a new sub feature.
     * @param start absolute start of the sub feature in regard to the reference genome
     * @param stop absolute stop of the sub feature in regard to the reference genome
     * @param type the of the sub feature
     */
    public ParsedSubFeature(int start, int stop, FeatureType type) {
        this.start = start;
        this.stop = stop;
        this.type = type;
    }

    /**
     * @return the absolute start of the sub feature in regard to the reference genome
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the absolute stop of the sub feature in regard to the reference genome
     */
    public int getStop() {
        return stop;
    }

    /**
     * @return the type of the sub feature.
     */
    public FeatureType getType() {
        return type;
    }    
    
}
