package de.cebitec.readXplorer.parser.common;

/**
 *
 * @author ddoppmeier
 */
public class ParsingException extends Exception {

    private static final long serialVersionUID = 423458724;

    /**
     * Creates a new instance of <code>ParsingException</code> without detail message.
     */
    public ParsingException() {
    }

    /**
     * Constructs an instance of <code>ParsingException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ParsingException(String msg) {
        super(msg);
    }

    public ParsingException(Throwable ex){
        super(ex);
    }
}
