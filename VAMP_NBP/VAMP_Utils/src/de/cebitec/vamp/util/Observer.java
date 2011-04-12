package de.cebitec.vamp.util;

/**
 * Observer in the observer pattern.
 * The observer is updated whenever the update method is called.
 *
 * @author rhilker
 */
public interface Observer {

    /**
     * This method is called whenever the observed object is changed. An application
     * calls an Observable object's notifyObservers method to have all the object's
     * observers notified of the change.
     * @param args the arguments to be send to the observer
     */
    public void update(Object args);

}
