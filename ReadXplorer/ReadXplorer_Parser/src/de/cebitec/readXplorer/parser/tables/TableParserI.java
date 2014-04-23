package de.cebitec.readXplorer.parser.tables;

import de.cebitec.readXplorer.parser.common.ParserI;
import de.cebitec.readXplorer.parser.common.ParsingException;
import java.io.File;
import java.util.List;

/**
 * Interface for table parsers.
 * 
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public interface TableParserI extends ParserI {
    
    /**
     * Parses a table into a list of lists of objects. The inner lists represent
     * the data of one row, while the outer list is the list of rows in the
     * table.
     * @return A table into a list of lists of objects. The inner lists
     * represent the data of one row, while the outer list is the list of rows
     * in the table.
     * @param fileToRead The file containing the table to parse.
     * @throws de.cebitec.readXplorer.parser.common.ParsingException
     */
    public List<List<?>> parseTable(File fileToRead) throws ParsingException;
    
}
