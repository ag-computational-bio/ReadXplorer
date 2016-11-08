/*
 * Copyright (C) 2016 Patrick Blumenkamp <patrick.blumenkamp at computational.bio.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Origin from
 * http://stackoverflow.com/questions/198431/how-do-you-compare-two-version-strings-in-java
 *
 * @author Patrick Blumenkamp
 * <patrick.blumenkamp at computational.bio.uni-giessen.de>
 */
public class Version implements Comparable<Version> {

    private static final Pattern VERSION_REGEX = Pattern.compile( "([0-9]+(?:\\.[0-9]+)*)(?:-([0-9a-zA-Z]+(?:\\.[0-9a-zA-Z]+)*))?(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?" );

    private String mainVersion = null;
    private String preRealease = null;
    private String metadata = null;


    public String getMainVersion() {
        return mainVersion;
    }


    public String getMetadata() {
        return metadata;
    }


    public String getPreRealease() {
        return preRealease;
    }


    public final String getVersion() {
        String version = mainVersion;
        if( preRealease != null ) {
            version += "-" + preRealease;
        }
        if( metadata != null ) {
            version += "+" + metadata;
        }

        return version;
    }


    public Version( String version ) {
        if( version == null ) {
            throw new IllegalArgumentException( "Version can not be null" );
        }
        Matcher m = VERSION_REGEX.matcher( version );
        if( !m.matches() ) {
            throw new IllegalArgumentException( "Invalid version format" );
        }
        this.mainVersion = m.group( 1 );
        if( m.group( 2 ) != null && !m.group( 2 ).isEmpty( ) ) {
            this.preRealease = m.group( 2 );
        }
        if( m.group( 3 ) != null && !m.group( 3 ).isEmpty( ) ) {
            this.metadata = m.group( 3 );
        }
    }


    /**
     * Compare two versions. Numbering after an hyphen (e.g. 1.1-alpha or
     * 2.1-1.5) will be ignored. Comparison follows the rules at
     * http://semver.org/
     *
     * @param that
     *
     * @return 1 if this is newer, -1 if that is newer, else 0
     */
    @Override
    public int compareTo( Version that ) {
        if( that == null ) {
            return 1;
        }
        String[] thisParts = this.getMainVersion().split( "\\." );
        String[] thatParts = that.getMainVersion().split( "\\." );
        int length = Math.max( thisParts.length, thatParts.length );
        for( int i = 0; i < length; i++ ) {
            int thisPart = i < thisParts.length
                           ? Integer.parseInt( thisParts[i] ) : 0;
            int thatPart = i < thatParts.length
                           ? Integer.parseInt( thatParts[i] ) : 0;
            if( thisPart < thatPart ) {
                return -1;
            }
            if( thisPart > thatPart ) {
                return 1;
            }
        }

        if( this.getPreRealease() == null && that.getPreRealease() == null ) {
            return 0;
        }
        if( this.getPreRealease() == null && that.getPreRealease() != null ) {
            return 1;
        }
        if( this.getPreRealease() != null && that.getPreRealease() == null ) {
            return -1;
        }

        thisParts = this.getPreRealease().split( "\\." );
        thatParts = that.getPreRealease().split( "\\." );
        for( int i = 0; i < length; i++ ) {
            Object thisPart;
            Object thatPart;

            if( thisParts.length <= i ) {
                thisPart = 0;
            } else if( isInteger( thisParts[i], 10 ) ) {
                thisPart = Integer.parseInt( thisParts[i] );
            } else {
                thisPart = thisParts[i];
            }
            if( thatParts.length <= i ) {
                thatPart = 0;
            } else if( isInteger( thatParts[i], 10 ) ) {
                thatPart = Integer.parseInt( thatParts[i] );
            } else {
                thatPart = thatParts[i];
            }

            int diff;
            if( thisPart instanceof String ) {
                if( thatPart instanceof Integer ) {
                    return 1;
                } else {
                    diff = ((String) thisPart).compareTo( (String) thatPart );
                }
            } else if( thatPart instanceof String ) {
                return -1;
            } else {
                diff = ((Integer) thisPart).compareTo( (Integer) thatPart );
            }
            if( diff != 0 ) {
                return (diff > 0) ? 1 : -1;
            }
        }

        return 0;
    }


    @Override
    public boolean equals( Object that ) {
        if( this == that ) {
            return true;
        }
        if( that == null ) {
            return false;
        }
        if( this.getClass() != that.getClass() ) {
            return false;
        }
        return this.compareTo( (Version) that ) == 0;
    }


    public static boolean isInteger( String s, int radix ) {
        if( s.isEmpty() ) {
            return false;
        }
        for( int i = 0; i < s.length(); i++ ) {
            if( i == 0 && s.charAt( i ) == '-' ) {
                if( s.length() == 1 ) {
                    return false;
                } else {
                    continue;
                }
            }
            if( Character.digit( s.charAt( i ), radix ) < 0 ) {
                return false;
            }
        }
        return true;
    }


}
