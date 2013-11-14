package de.cebitec.readXplorer.parser.common;

/**
 *
 * @author ddoppmeier
 * @deprecated not used anymore, since read names are not stored in the db.
 */
public class ParsedRead {

    private String sequence;
    private String name;

    public ParsedRead(String name, String sequence){
        this.name = name;
        this.sequence = sequence;
    }

    public String getSequence(){
        return sequence;
    }

    public String getName() {
        return name;
    }

}
