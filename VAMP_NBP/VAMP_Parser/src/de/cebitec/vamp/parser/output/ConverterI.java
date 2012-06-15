package de.cebitec.vamp.parser.output;

/**
 * @author -Rolf Hilker-
 * 
 * Converts the data chosen by the subclasses into another
 * format according to the specific subclass.
 */
public interface ConverterI {
    
    /**
     * Converts the data chosen by the subclasses into another format according to
     * the specific subclass.
     * @exception can throw any exception, which has to be specified by the implementation
     */
    public void convert() throws Exception;
}
