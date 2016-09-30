package de.cebitec.common.sequencetools.geneticcode;

/**
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IllegalGeneticCodeException extends RuntimeException {

    public IllegalGeneticCodeException() {
    }

    IllegalGeneticCodeException(int id) {
        super("No genetic code with id " + id + " available.");
    }

}
