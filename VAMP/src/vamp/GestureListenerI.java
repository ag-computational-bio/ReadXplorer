package vamp;

/**
 *
 * @author ddoppmeier
 */
public interface GestureListenerI {


    public void shutDownApplication();

    public void logOff();

    public void login(String adapter, String hostname, String database, String user, String password);



}
