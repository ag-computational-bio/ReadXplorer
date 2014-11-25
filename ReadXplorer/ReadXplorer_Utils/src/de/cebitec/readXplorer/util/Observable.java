package de.cebitec.readXplorer.util;

/**
 * Subject in the Observer pattern.
 * Observer can be registered, removed and notified.
 *
 * @author rhilker
 */
public interface Observable {

    /**
     * Registers the given observer for this observable.
     * @param observer the observer to register
     */
    public void registerObserver(Observer observer);

    /**
     * Removes the given observer from the observable.
     * @param observer the observer to remove
     */
    public void removeObserver(Observer observer);

    /**
     * Notifies all observers currently observing the observable.
     * @param data object for which the notification is created. It is often
     * passed on to the observers.
     */
    public void notifyObservers(Object data);
}
