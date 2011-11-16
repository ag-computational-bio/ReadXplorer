package de.cebitec.vamp.util;

/**
 * @author rhilker
 * 
 * 
 */
public enum SequenceComparison {
    
    /** getType() returns 'S' = To be used for substitutions. */
    SUBSTITUTION (SequenceComparison.S),
    /** getType() returns 'M' = To be used for matches. */
    MATCH (SequenceComparison.M),
    /** getType() returns 'D' = To be used for deletions. */
    DELETION (SequenceComparison.D),
    /** getType() returns 'I' = To be used for insertions. */
    INSERTION (SequenceComparison.I),
    /** getType() returns ' ' = To be used for unknown type. */
    UNKNOWN (' ');

    
    /** 'S' = To be used for substitutions. */
    private static final char S = 'S';
    /** 'M' = To be used for matches. */
    private static final char M = 'M';
    /** 'D' = To be used for deletions. */
    private static final char D = 'D';
    /** 'I' = To be used for insertions. */
    private static final char I = 'I';
    
    private char type;
    
    private SequenceComparison(char type) {
        this.type = type;
    }
    
    /**
     * @return the effect type of the current effect.
     */
    public char getType(){
        return type;
    }
    
    /**
     * Returns the desired SequenceComparison for a given char.
     * @param type the type of SequenceComparison to return. If the type is unknown
     * SequenceComparison.MATCH is returned.
     * @return 
     */
    public static SequenceComparison getSequenceComparison(char type){
        switch (type){
            case 'S' :
                return SUBSTITUTION;
            case 'M' :
                return MATCH;
            case 'I' :
                return INSERTION;
            case 'D' :
                return DELETION;
            default :
                return MATCH;
        }
    }
    
    @Override
    public String toString(){
        return String.valueOf(type);
    }
    
}
