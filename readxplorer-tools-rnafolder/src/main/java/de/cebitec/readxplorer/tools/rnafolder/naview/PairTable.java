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

package de.cebitec.readxplorer.tools.rnafolder.naview;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;


public class PairTable {

    private int[] pairTable;
    private byte[] bondTypes;
    private int length;
    public static final byte NONE = -1;
    public static final byte NORMAL_BOND = 0;
    public static final byte GU_BOND = 1;
    public static final byte KNOT = 2;
    public static final byte KNOT_GU = 3;
    public static final byte PK1 = 4;
    public static final byte PK2 = 5;
    public static final byte PK3 = 6;
    private static final int GU = 'G' + 'U';
    private static final int GT = 'G' + 'T';


    // DCSE input
    public PairTable( String sequence, String structure, String helices ) {

        final List<String> labels = new ArrayList<>();
        final HashSet<String> pseudoknots = new HashSet<>();
        final HashSet<String> knots = new HashSet<>();

        // extract region labels from the helix numbering
        final int h_idx = helices.indexOf( 'H' );
        if( h_idx < 0 ) {
            throw new IllegalArgumentException( "Missing terminating 'H' character in helix numbering." );
        }
        final String[] blubb = helices.substring( 0, h_idx ).split( "(\\s*-\\s*)+" );
        for( String blubb1 : blubb ) {
            String tmp = blubb1.trim();
            if( tmp.matches( "\\d+('?)" ) ) {
                labels.add( tmp );
            }
        }

        // extract pseudoknot labels
        final int pk_start = structure.lastIndexOf( '#' );
        final int pk_end = structure.lastIndexOf( '&' );
        if( pk_start > 0 && pk_end > 0 && pk_start < pk_end ) {
            StringTokenizer st = new StringTokenizer( structure.substring( pk_start, pk_end ), " " );
            while( st.hasMoreTokens() ) {
                String tmp = st.nextToken().trim();
                if( tmp.matches( "\\d+" ) ) {
                    pseudoknots.add( tmp );
                    pseudoknots.add( tmp + "'" );
                }
            }
        }

        // extract entagled helix (knot) labels
        if( pk_end > 0 && pk_end < structure.length() ) {
            StringTokenizer st = new StringTokenizer( structure.substring( pk_end, structure.length() ), " " );
            while( st.hasMoreTokens() ) {
                String tmp = st.nextToken().trim();
                if( tmp.matches( "\\d+" ) ) {
                    knots.add( tmp );
                    knots.add( tmp + "'" );
                }
            }
        }

        // parse structure
        boolean end = false;
        boolean paired = false;
        int pos = 0;
        int regions = 0;
        int mate, sum;
        final List<Integer> pt_list = new ArrayList<>();
        final List<Byte> type_list = new ArrayList<>();
        final Stack<Integer> s0 = new Stack<>();
        final Stack<Integer> s1 = new Stack<>();
        final Stack<Integer> s2 = new Stack<>();
        for( int i = 0; i < structure.length() && !end; i++ ) {
            switch( structure.charAt( i ) ) {
                case '[':
                    paired = true;
                case '^':
                    regions++;
                    break;
                case ']':
                case '{':
                    paired = false;
                    break;
                case '}':
                    paired = true;
                    break;
                case ' ':
                case '(':
                case ')':
                    break;
                case '|':
                    end = true;
                    break;
                default:
                    if( ++pos >= sequence.length() ) {
                        throw new IllegalArgumentException( "Length of structure exceeds length of sequence!" );
                    }
                    if( paired ) {
                        if( regions > labels.size() ) {
                            throw new IllegalArgumentException( "Structure contains more regions than helix numbering." );
                        }
                        final String label = labels.get( regions - 1 );
                        if( label.endsWith( "'" ) ) {
                            if( pseudoknots.contains( label ) ) {
                                type_list.add( PK1 );
                                if( s1.empty() ) {
                                    throw new IllegalArgumentException( "Unbalanced pseudoknot pair(s) in structure." );
                                }
                                mate = s1.pop();
                            } else {
                                if( s0.empty() ) {
                                    throw new IllegalArgumentException( "Unbalanced base pair(s) in structure." );
                                }
                                mate = s0.pop();
                                sum = sequence.toUpperCase().charAt( pos - 1 ) +
                                         sequence.toUpperCase().charAt( mate );
                                boolean gu = sum == GU || sum == GT;
                                if( knots.contains( label ) ) {
                                    type_list.add( gu ? KNOT_GU : KNOT );
                                    type_list.set( mate, gu ? KNOT_GU : KNOT );
                                } else {
                                    type_list.add( gu ? GU_BOND : NORMAL_BOND );
                                    type_list.set( mate, gu ? GU_BOND : NORMAL_BOND );
                                }
                            }
                            pt_list.set( mate, pos - 1 );
                            pt_list.add( mate );
                        } else {
                            if( pseudoknots.contains( label ) ) {
                                type_list.add( PK1 );
                                s1.push( pos - 1 );
                            } else {
                                type_list.add( NORMAL_BOND );
                                s0.push( pos - 1 );
                            }
                            pt_list.add( -1 );
                        }
                    } else {
                        pt_list.add( -1 );
                        type_list.add( NONE );
                    }
                    break;
            }
        }
        if( !(s0.empty() && s1.empty() && s2.empty()) ) {
            throw new IllegalArgumentException( "Unbalanced pair(s) in structure." );
        }

        // convert lists to arrays
        if( pt_list.size() != type_list.size() ) {
            throw new IllegalArgumentException( "Something went wrong..." );
        }

        length = pt_list.size();
        pairTable = new int[length];
        bondTypes = new byte[length];

        for( int i = 0; i < length; i++ ) {
            pairTable[i] = pt_list.get( i );
            bondTypes[i] = type_list.get( i );
        }
    }


    // Vienna (dot-bracket) input
    public PairTable( String sequence, String structure ) {

        int sp0 = 0;
        int sp1 = 0;
        int sp2 = 0;
        int sp3 = 0;

        if( structure.length() > sequence.length() ) {
            throw new IllegalArgumentException( "Length of structure exceeds length of sequence!" );
        }

        length = structure.length();
        int[] s0 = new int[length];
        int[] s1 = new int[length];
        int[] s2 = new int[length];
        int[] s3 = new int[length];
        pairTable = new int[length];
        bondTypes = new byte[length];

        for( int i = 0; i < length; i++ ) {
            char c;
            switch( c = structure.charAt( i ) ) {
                case '(':
                    s0[sp0++] = i;
                    break;
                case '[':
                    s1[sp1++] = i;
                    break;
                case '{':
                    s2[sp2++] = i;
                    break;
                case '<':
                    s3[sp3++] = i;
                    break;
                case ')':
                    if( --sp0 < 0 ) {
                        throw new IllegalArgumentException( "Unbalanced braces in " +
                                 "dot-bracket string." );
                    }
                    int mate = s0[sp0];
                    pairTable[i] = mate;
                    pairTable[mate] = i;
                    int sum = sequence.toUpperCase().charAt( i ) +
                             sequence.toUpperCase().charAt( mate );
                    boolean gu = sum == GU || sum == GT;
                    bondTypes[i] = bondTypes[mate] = gu ? GU_BOND : NORMAL_BOND;
                    break;
                case ']':
                    if( --sp1 < 0 ) {
                        throw new IllegalArgumentException( "Unbalanced braces in " +
                                 "dot-bracket string." );
                    }
                    mate = s1[sp1];
                    pairTable[i] = mate;
                    pairTable[mate] = i;
                    bondTypes[i] = bondTypes[mate] = PK1;
                    break;
                case '}':
                    if( --sp2 < 0 ) {
                        throw new IllegalArgumentException( "Unbalanced braces in " +
                                 "dot-bracket string." );
                    }
                    mate = s2[sp2];
                    pairTable[i] = mate;
                    pairTable[mate] = i;
                    bondTypes[i] = bondTypes[mate] = PK2;
                    break;
                case '>':
                    if( --sp3 < 0 ) {
                        throw new IllegalArgumentException( "Unbalanced braces in " +
                                 "dot-bracket string." );
                    }
                    mate = s3[sp3];
                    pairTable[i] = mate;
                    pairTable[mate] = i;
                    bondTypes[i] = bondTypes[mate] = PK3;
                    break;
                case ':':
                case '.':
                    pairTable[i] = -1;
                    bondTypes[i] = NONE;
                    break;
                default:
                    throw new IllegalArgumentException( "Unrecognized token '" + c + "' in " +
                             "dot-bracket string." );
            }
        }

        if( sp0 != 0 || sp1 != 0 || sp2 != 0 || sp3 != 0 ) {
            throw new IllegalArgumentException( "Unbalanced braces in " +
                     "dot-bracket string." );
        }

    }


    public int size() {
        return length;
    }


    public int getMate( int pos ) {
        return pairTable[pos];
    }


    public byte getType( int pos ) {
        return bondTypes[pos];
    }


}
