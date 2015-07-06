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

package de.cebitec.readxplorer.rnatrimming;


/**
 * A trim method describes the way a Read-Sequence can be trimmed
 * with the given maximum length
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public abstract class TrimMethod {

    private int maximumTrimLength;

    private String shortName;


    /**
     * @return the maximumTrimLength
     */
    public int getMaximumTrimLength() {
        return maximumTrimLength;
    }


    /**
     * @param maximumTrimLength the maximumTrimLength to set
     */
    public void setMaximumTrimLength( int maximumTrimLength ) {
        this.maximumTrimLength = maximumTrimLength;
    }


    public abstract TrimMethodResult trim( String sequence );


    /**
     * the shortname is filename-safe (i.e. alphanumeric, no spaces)
     * <p>
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }


    /**
     * the shortname should be filename-safe (i.e. alphanumeric, no spaces)
     * <p>
     * @param shortName the shortName to set
     */
    public void setShortName( String shortName ) {
        this.shortName = shortName;
    }


    /**
     * @return the short description can be used as part of the filename
     *         to save the results of trimming
     */
    public String getShortDescription() {
        return this.getShortName() + "_" + Integer.toString( this.getMaximumTrimLength() );
    }


}
