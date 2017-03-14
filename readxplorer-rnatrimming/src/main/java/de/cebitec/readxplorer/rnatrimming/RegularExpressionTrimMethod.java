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


import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * RegularExpressionTrimMethod trims a sequence based on the given
 * regular expression.
 * <p>
 * @author Evgeny Anisiforov
 */
public class RegularExpressionTrimMethod extends TrimMethod {

    private Pattern regularexpression;
    private final String regularexpressionTemplate;
    private final int groupnumberMain;
    private final int groupnumberTrimLeft;
    private final int groupnumberTrimRight;
    private final String name;


    public RegularExpressionTrimMethod( String regularexpression, int groupnumberMain, int groupnumberTrimLeft, int groupnumberTrimRight, String name, String shortName ) {
        this.regularexpressionTemplate = regularexpression;
        this.groupnumberMain = groupnumberMain;
        this.groupnumberTrimLeft = groupnumberTrimLeft;
        this.groupnumberTrimRight = groupnumberTrimRight;
        this.name = name;
        this.setMaximumTrimLength( 10 );
        this.setShortName( shortName );
    }


    public void replacePlaceholder( String placeholder, String value ) {
        this.regularexpression = Pattern.compile( regularexpressionTemplate.replace( placeholder, value ) );
    }


    @Override
    public void setMaximumTrimLength( int maximumTrimLength ) {
        super.setMaximumTrimLength( maximumTrimLength );
        this.replacePlaceholder( "%X%", maximumTrimLength + "" );
    }


    @Override
    public TrimMethodResult trim( String sequence ) {
        Matcher matcher = regularexpression.matcher( sequence );
        TrimMethodResult result = new TrimMethodResult( sequence, sequence, 0, 0 );
        if( matcher.find() ) {
            result.setSequence( matcher.group( this.groupnumberMain ) );
            if( this.groupnumberTrimLeft > 0 ) {
                result.setTrimmedCharsFromLeft( matcher.group( this.groupnumberTrimLeft ).length() );
            }
            if( this.groupnumberTrimRight > 0 ) {
                result.setTrimmedCharsFromRight( matcher.group( this.groupnumberTrimRight ).length() );
            }
        }
        //if the pattern does not match, just return the full string
        return result;
    }


    @Override
    public String toString() {
        return this.name;
    }


    public enum Type {

        VARIABLE_RIGHT, VARIABLE_LEFT, VARIABLE_BOTH,
        FIXED_LEFT, FIXED_RIGHT, FIXED_BOTH

    };

    public static final int GROUPNUMBER_UNUSED = -1;

    /*
     * a list of default instances is provided here
     */

    public static RegularExpressionTrimMethod createNewInstance( Type t ) {
        if( t.equals( Type.VARIABLE_RIGHT ) ) {
            return new RegularExpressionTrimMethod( "^(.*?)(A{0,%X%})$", 1, GROUPNUMBER_UNUSED, 2, "trim poly-A from 3' end (right to left) by variable length", "v_r" );
        } else if( t.equals( Type.VARIABLE_LEFT ) ) {
            return new RegularExpressionTrimMethod( "^(A{0,%X%})(.*?)$", 2, 1, GROUPNUMBER_UNUSED, "trim poly-A from 5' end (left to right) by variable length", "v_l" );
        } else if( t.equals( Type.VARIABLE_BOTH ) ) {
            return new HalfedLengthTrimMethod( "^(A{0,%X%})(.*?)(A{0,%X%})$", 2, 1, 3, "trim poly-A from 3' and from 5' end by variable length", "v_lr" );
        } else if( t.equals( Type.FIXED_RIGHT ) ) {
            return new RegularExpressionTrimMethod( "^(.*?)(.{%X%})$", 1, GROUPNUMBER_UNUSED, 2, "trim all nucleotides from 3' end (right to left) by fixed length", "f_r" );
        } else if( t.equals( Type.FIXED_LEFT ) ) {
            return new RegularExpressionTrimMethod( "^(.{%X%})(.*?)$", 2, 1, GROUPNUMBER_UNUSED, "trim all nucleotides from 5' end (left to right) by fixed length", "f_l" );
        } else if( t.equals( Type.FIXED_BOTH ) ) {
            return new HalfedLengthTrimMethod( "^(.{%X%})(.*?)(.{%X%})$", 2, 1, 3, "trim all nucleotides from 3' and from 5' end by fixed length", "f_lr" );
        } else {
            return new HalfedLengthTrimMethod( "^(.{%X%})(.*?)(.{%X%})$", 2, 1, 3, "UNKNOWN", "unkn" );
        }
    }


}
