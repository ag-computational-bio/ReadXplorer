
package de.cebitec.readxplorer.api;


/**
 * A general ReadXplorer RuntimeException.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public abstract class ReadXplorerException extends Exception {

    private static final long serialVersionUID = 1L;

    private String userMsg;
    private Type type;


    /**
     * General ReadXplorer exception inheriting from RuntimeException.
     *
     * @param type  The error type from ReadXplorerException.Type
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    protected ReadXplorerException( Type type, Throwable cause ) {

        this( "", type, cause );

    }


    /**
     *
     * @param msg   the detail message (which is saved for later retrieval by
     *              the {@link #getMessage()} method).
     * @param type  The error type from ReadXplorerException.Type
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    protected ReadXplorerException( String msg, Type type, Throwable cause ) {

        this( "", msg, type, cause );

    }


    /**
     *
     * @param userMsg Message to display to the user.
     * @param msg     the detail message (which is saved for later retrieval by
     *                the {@link #getMessage()} method).
     *
     * @param type    The error type from ReadXplorerException.Type
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method). (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    protected ReadXplorerException( String userMsg, String msg, Type type, Throwable cause ) {

        super( msg, cause );

        this.userMsg = userMsg;
        this.type = type;

    }


    /**
     * @param userMsg A message to show to the user
     */
    public void setUserMsg( String userMsg ) {
        this.userMsg = userMsg;
    }


    /**
     * @return The message to show to the user.
     */
    public String getUserMsg() {
        return userMsg;
    }


    /**
     * @return The exception type from ReadXplorerException.Type
     */
    public Type getType() {
        return type;
    }


    /**
     * Exception type.
     */
    public enum Type {
        Database,
        File,
        Analysis
    }


}
