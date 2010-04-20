package vamp.databackend.connector;

/**
 *
 * @author ddoppmeier
 */
public class StorageException extends Exception {

    private static final long serialVersionUID = 8835896;

    /**
     * Creates a new instance of <code>StorageException</code> without detail message.
     */
    public StorageException() {
    }


    /**
     * Constructs an instance of <code>StorageException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StorageException(String msg) {
        super(msg);
    }

    public StorageException(Throwable ex){
        super(ex);
    }
}
