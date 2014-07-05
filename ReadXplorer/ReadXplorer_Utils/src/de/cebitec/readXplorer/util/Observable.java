/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
