package de.cebitec.vamp.util.externalTools;

/**
 * Should be used to combine all exceptions occuring during the run of RNAFolder.
 * Each exception should contain a user readable string msg which can be displayed
 * in a notifier.
 *
 * @author Rolf Hilker
 */
public class RNAFolderException extends Exception {

    /**
     * Constructs an instance of <code>RNAFolderException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RNAFolderException(String msg) {
        super(msg);
    }

}
