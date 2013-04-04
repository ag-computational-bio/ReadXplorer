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
     * @return true, if the conversion was successful, false otherwise
     * @throws Exception can throw any exception, which has to be specified by the implementation
     */
    public boolean convert() throws Exception;
    
    
    public void setDataToConvert(Object... data) throws IllegalArgumentException;
}
