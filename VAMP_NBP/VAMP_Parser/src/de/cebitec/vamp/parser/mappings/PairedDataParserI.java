package de.cebitec.vamp.parser.mappings;

import java.util.HashMap;

/**
 * Interface contains the additional methods a parser for sequence pairs needs.
 *
 * @author Rolf Hilker
 */
public interface PairedDataParserI {

    public HashMap<String, Integer> getSeqIDToReadNameMap();

}
