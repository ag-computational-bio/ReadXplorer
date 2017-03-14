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

package de.cebitec.readxplorer.ui;


import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
 * A TopComponent, which returns its name when the <cc>toString()</cc> method is
 * invoced.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class TopComponentExtended extends TopComponent {

    private static final long serialVersionUID = 1L;


    /**
     * A TopComponent, which returns its name when the <cc>toString()</cc>
     * method is invoced.
     */
    public TopComponentExtended() {
    }


    /**
     * A TopComponent, which returns its name when the <cc>toString()</cc>
     * method is invoced.
     * <p>
     * @param lookup the lookup
     */
    public TopComponentExtended( Lookup lookup ) {
        super( lookup );
    }


    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }


    /**
     * @return The name of this extended TopComponent.
     */
    @Override
    public String toString() {
        return this.getName();
    }


}
