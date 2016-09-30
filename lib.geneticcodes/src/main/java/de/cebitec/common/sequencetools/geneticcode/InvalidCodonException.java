package de.cebitec.common.sequencetools.geneticcode;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class InvalidCodonException extends Exception {

    /**
     * Creates a new instance of
     * <code>InvalidCodonException</code> without detail message.
     */
    public InvalidCodonException() {
    }

    /**
     * Constructs an instance of
     * <code>InvalidCodonException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidCodonException(String msg) {
        super(msg);
    }
}
