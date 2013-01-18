package de.cebitec.vamp.parser.reference;

import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.reference.Filter.FeatureFilter;
import de.cebitec.vamp.util.Observable;

/**
 *
 * @author ddoppmeier
 */
public interface ReferenceParserI extends ParserI, Observable {

    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException;

}
