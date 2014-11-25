package de.cebitec.readXplorer.parser.reference;

import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParserI;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.util.Observable;

/**
 *
 * @author ddoppmeier
 */
public interface ReferenceParserI extends ParserI, Observable {

    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException;

}
