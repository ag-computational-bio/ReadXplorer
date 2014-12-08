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
 * Observer in the observer pattern.
 * The observer is updated whenever the update method is called.
 *
 * @author rhilker
 */
public interface Observer {

    /**
     * This method is called whenever the observed object is changed. An
     * application
     * calls an Observable object's notifyObservers method to have all the
     * object's
     * observers notified of the change.
     * <p>
     * @param args the arguments to be send to the observer
     */
    public void update( Object args );


}
