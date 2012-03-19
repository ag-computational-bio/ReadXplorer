package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 * @author rhilker
 * 
 * A ParsedSubAnnotation is a sub annotation of a ParsedAnnotation. Thus, it contains a reference
 * to it's parent annotation (annotationId) and a start and stop position.
 */
public class ParsedSubAnnotation {
        
    private int start;
    private int stop;
    private FeatureType type;

    /**
     * Creates a new sub annotation.
     * @param start absolute start of the sub annotation in regard to the reference genome
     * @param stop absolute stop of the sub annotation in regard to the reference genome
     * @param type the of the sub annotation
     */
    public ParsedSubAnnotation(int start, int stop, FeatureType type) {
        this.start = start;
        this.stop = stop;
        this.type = type;
    }

    /**
     * @return the absolute start of the sub annotation in regard to the reference genome
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the absolute stop of the sub annotation in regard to the reference genome
     */
    public int getStop() {
        return stop;
    }

    /**
     * @return the type of the sub annotation.
     */
    public FeatureType getType() {
        return type;
    }    
    
}
