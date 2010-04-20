package vamp.parsing.reference;

import vamp.importer.ReferenceJob;
import vamp.parsing.common.*;
import vamp.parsing.reference.Filter.FeatureFilter;

/**
 *
 * @author ddoppmeier
 */
public interface ReferenceParserI extends ParserI {

    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException;

}
