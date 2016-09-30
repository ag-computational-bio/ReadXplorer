package de.cebitec.common.sequencetools.geneticcode;

import java.util.HashMap;
import java.util.List;

/**
 * @author rhilker
 *
 * A parsed entry is a container for a parsed ASN1 entry.
 */
class ParsedASN1Entry {

    HashMap<String, List<String>> entry;

    public ParsedASN1Entry(HashMap<String, List<String>> entry){
        this.entry = entry;
    }


    public HashMap<String, List<String>> getEntry() {
        return this.entry;
    }

}
