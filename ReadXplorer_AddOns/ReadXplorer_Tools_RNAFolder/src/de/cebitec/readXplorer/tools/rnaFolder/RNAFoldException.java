package de.cebitec.readXplorer.tools.rnaFolder;

/**
 * Should be used to combine all exceptions occuring during the run of RNAFolder.
 * Each exception should contain a user readable string msg which can be displayed
 * in a notifier.
 *
 * @author Rolf Hilker
 */
public class RNAFoldException extends Exception {

    /**
     * Constructs an instance of <code>RNAFoldException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RNAFoldException(String msg) {
        super(msg);
    }

}
