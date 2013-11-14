package de.cebitec.readXplorer.parser.common;

/**
 * Interface for all data parsers.
 *
 * @author ddoppmei
 */
public interface ParserI {

    /**
     * @return name of the parser.
     */
    public String getName();

    /**
     * @return file extensions supported by this parser.
     */
    public String[] getFileExtensions();
    
    /**
     * @return input file description string.
     */
    public String getInputFileDescription();

}
