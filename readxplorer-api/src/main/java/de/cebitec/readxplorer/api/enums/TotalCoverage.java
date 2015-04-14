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

package de.cebitec.readxplorer.api.enums;

import de.cebitec.readxplorer.api.Classification;



/**
 * Enumeration for the total coverage in ReadXplorer.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public enum TotalCoverage implements Classification {

    /**
     * Total coverage constant.
     */
    TOTAL_COVERAGE( 0, "Total Coverage" );

    private final int type;
    private final String string;


    private TotalCoverage( int type, String string ) {
        this.type = type;
        this.string = string;
    }


    /**
     * @return the string representation of the total coverage.
     */
    @Override
    public String getString() {
        return this.string;
    }


    /**
     * @return the byte value of the type of the total coverage.
     */
    @Override
    public int getType() {
        return this.type;
    }


    /**
     * @return the string representation of the total coverage.
     */
    @Override
    public String toString() {
        return this.getString();
    }


}
