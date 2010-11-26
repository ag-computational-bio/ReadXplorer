package de.cebitec.vamp.parsing.reference;

import de.cebitec.vamp.importer.ReferenceJob;
import de.cebitec.vamp.parsing.common.ParsedReference;
import de.cebitec.vamp.parsing.common.ParserI;
import de.cebitec.vamp.parsing.common.ParsingException;
import de.cebitec.vamp.parsing.reference.Filter.FeatureFilter;

/**
 *
 * @author ddoppmeier
 */
public interface ReferenceParserI extends ParserI {

    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException;

}
