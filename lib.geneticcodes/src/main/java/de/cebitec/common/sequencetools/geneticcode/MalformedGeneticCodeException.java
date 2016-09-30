package de.cebitec.common.sequencetools.geneticcode;

/**
 * @author rhilker
 *
 * Exception indicates that the data for creating a genetic code is incorrect.
 */
public class MalformedGeneticCodeException extends Exception {

    public MalformedGeneticCodeException(String msg) {
        super(msg);
    }

}
