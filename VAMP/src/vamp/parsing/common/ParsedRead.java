package vamp.parsing.common;

/**
 *
 * @author ddoppmeier
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
