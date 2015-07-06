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
 * This class implements a simple extension to the RegularExpressionTrimMethod,
 * that allowes to use two-sided regular expressions to shorten reads.
 * To keep the results comparable with one-sided shortenings the shortening
 * length
 * is divided by 2.
 * <p>
 * Example: AAAAGGGAAAA
 * --------
 * shorten poly a from right (one side) with length=4
 * will result in AAAAGGG---- (shortened 4 nucleotides in whole)
 * <p>
 * shorten poly a from both sides (two sides) with length=4 will set actual
 * internal length=2
 * will result in --AAGGGAA-- (shortened 4 nucleotides in whole)
 * <p>
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class HalfedLengthTrimMethod extends RegularExpressionTrimMethod {

    public HalfedLengthTrimMethod( String regularexpression, int groupnumberMain, int groupnumberTrimLeft, int groupnumberTrimRight, String name, String shortName ) {
        super( regularexpression, groupnumberMain, groupnumberTrimLeft, groupnumberTrimRight, name, shortName );
    }


    @Override
    public void setMaximumTrimLength( int maximumTrimLength ) {
        super.setMaximumTrimLength( maximumTrimLength / 2 );
    }


    @Override
    public int getMaximumTrimLength() {
        return super.getMaximumTrimLength() * 2;
    }


}
