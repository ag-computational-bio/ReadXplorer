
package de.cebitec.readxplorer.api;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public abstract class ReadXplorerException extends RuntimeException {

    private String userMsg;
    private Type type;


    protected ReadXplorerException( Type type, Throwable cause ) {

        this( "", type, cause );

    }


    protected ReadXplorerException( String msg, Type type, Throwable cause ) {

        this( "", msg, type, cause );

    }


    protected ReadXplorerException( String userMsg, String msg, Type type, Throwable cause ) {

        super( msg, cause );

        this.userMsg = userMsg;
        this.type    = type;

    }





    public void setUserMsg( String userMsg ) {
        this.userMsg = userMsg;
    }


    public String getUserMsg() {
        return userMsg;
    }


    public Type getType() {
        return type;
    }




    public enum Type {
        Database,
        File,
        Analysis
    }


}
