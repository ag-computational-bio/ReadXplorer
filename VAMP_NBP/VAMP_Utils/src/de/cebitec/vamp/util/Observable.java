package de.cebitec.vamp.util;

/**
 * Subject in the Observer pattern.
 * Observer can be registered, removed and notified.
 *
 * @author rhilker
 */
public interface Observable {

    public void registerObserver(Observer observer);

    public void removeObserver(Observer observer);

    public void notifyObservers(Object data);
}
