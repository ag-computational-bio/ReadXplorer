
package de.cebitec.readXplorer.util;

/**
 * An interface for classes, which are capable of sending messages.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public interface MessageSenderI {
    
    /**
     * Sends a message to someone/somewhere, if it is allowed.
     * @param msg the message to send
     */
    public void sendMsgIfAllowed(String msg);
    
}
