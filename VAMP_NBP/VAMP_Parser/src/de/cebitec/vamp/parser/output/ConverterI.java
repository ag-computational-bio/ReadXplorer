package de.cebitec.vamp.parser.output;

import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.util.Observable;

/**
 * Converts the data chosen by the subclasses into another
 * format according to the specific subclass.
 * 
 * @author -Rolf Hilker-
 */
public interface ConverterI extends ParserI, Observable {
    
    /**
     * Converts the data chosen by the subclasses into another format according to
     * the specific subclass.
     * @exception can throw any exception, which has to be specified by the implementation
     */
    public void convert() throws Exception;
    
    
    public void setDataToConvert(Object... data) throws IllegalArgumentException;
}
