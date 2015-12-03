
package gasv.bamtogasv;


/**
 * Copyright 2010,2012 Benjamin Raphael, Suzanne Sindi, Hsin-Ta Wu, Anna Ritz,
 * Luke Peng, Layla Oesper
 * <p>
 * This file is part of GASV.
 * <p>
 * gasv is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * GASV is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * gasv. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 */
import htsjdk.samtools.SAMFormatException;
import htsjdk.samtools.SAMRecord;


public class GASVPair {

    private int first_start, first_end, second_start, second_end;
    private char first_ori, second_ori;
    private int first_chrom, second_chrom;
    private String readname;
    boolean badChrParse = false;


    /**
     * Constructor. Takes relevant info from a SAMRecord.
     * <p>
     * NEW IN VERSION 2.0: Coordinates are "align_start" and "align_end" for
     * reads, unlike previous version's "align_start" and "align_end+1".
     * <p>
     * @param s        - SAMRecord
     * @param platform - Platform
     * <p>
     * @throws SAMFormatException
     */
    public GASVPair( SAMRecord s, String platform ) throws SAMFormatException {
        readname = s.getReadName();

		// if the mate's chromosome is larger than the query's OR the inferred
        // insert size is < 0, then the mate comes first.
        boolean queryFirst = true;
        //--- Rolf Hilker added this assignment to prevent NullPointerExceptions when * is used as mate ref ----
        String mateRefName = "*".equals( s.getMateReferenceName() ) ? s.getReferenceName() : s.getMateReferenceName();
        //------------------------------------------------------------------------------------------------------
        if( parseChr( s.getReferenceName() ) > parseChr( mateRefName ) ||
            (parseChr( s.getReferenceName() ) == parseChr( mateRefName ) &&
             s.getAlignmentStart() > s.getMateAlignmentStart()) ) {
            queryFirst = false;
        }

        if( queryFirst ) { // position: query ... mate
            first_chrom = parseChr( s.getReferenceName() );
            first_ori = s.getReadNegativeStrandFlag() ? '-' : '+';
            first_start = s.getAlignmentStart();
            first_end = s.getAlignmentEnd();

            second_chrom = parseChr( mateRefName );
            second_ori = s.getMateNegativeStrandFlag() ? '-' : '+';
            second_start = s.getMateAlignmentStart();
            second_end = s.getMateAlignmentStart() + s.getReadLength() - 1;

            // SOLiD platform - flip the orientation of the second read
            if( platform.equals( "solid" ) ) {
                if( s.getFirstOfPairFlag() ) {
                    second_ori = (second_ori == '-') ? '+' : '-';
                }
            } //We reverse the orientation of BOTH strands.
            else if( platform.equals( "matepair" ) ) {
                first_ori = (first_ori == '-') ? '+' : '-';
                second_ori = (second_ori == '-') ? '+' : '-';
            }

        } else { // position: mate ... query
            first_chrom = parseChr( mateRefName );
            first_ori = s.getMateNegativeStrandFlag() ? '-' : '+';
            first_start = s.getMateAlignmentStart();
            first_end = s.getMateAlignmentStart() + s.getReadLength() - 1;

            second_chrom = parseChr( s.getReferenceName() );
            second_ori = s.getReadNegativeStrandFlag() ? '-' : '+';
            second_start = s.getAlignmentStart();
            second_end = s.getAlignmentEnd();

            // SOLiD platform
            if( platform.equals( "solid" ) ) {
                if( s.getSecondOfPairFlag() ) {
                    second_ori = (second_ori == '-') ? '+' : '-';
                }
            } else if( platform.equals( "matepair" ) ) {
                first_ori = (first_ori == '-') ? '+' : '-';
                second_ori = (second_ori == '-') ? '+' : '-';
            }
        }
    }


    // NEW in Version 2.0: insert size is end-start+1 rather than end-start.

    public int getInsertSize() {
        return second_end - first_start + 1;
    }


    public boolean equalConvPair() {
        return first_ori == '+' && second_ori == '-';
    }


    public boolean equalChromosome() {
        return first_chrom == second_chrom;
    }


    public boolean equalStrand() {
        return first_ori == second_ori;
    }


    public Integer getChromosome() {
        return first_chrom;
    }


    public final int parseChr( String str ) {
        if( BAMToGASV.CHR_NAMES == null ) {
            try {
                return Integer.parseInt( str );
            } catch( NumberFormatException e1 ) {
                String origstr = str;
                str = str.replace( "chr", "" ); // remove chr
                str = str.replace( "Chr", "" ); // remove Chr
                str = str.replace( "CHR", "" ); // remove Chr
                str = str.replace( "X", "23" ); // replace X with 23
                str = str.replace( "Y", "24" ); // replace Y with 24
                try {
                    return Integer.parseInt( str );
                } catch( NumberFormatException e2 ) {
                    badChrParse = true;
                    BAMToGASV.NON_DEFAULT_REFS.put( origstr, true );
                }
            }
        } else {
            if( !BAMToGASV.CHR_NAMES.containsKey( str ) ) {
                BAMToGASV.NON_DEFAULT_REFS.put( str, true );
                badChrParse = true;
            }
            return BAMToGASV.CHR_NAMES.get( str );
        }
        return Integer.MIN_VALUE;
    }


    // output need to transfer 'X' and 'Y' into 23 and 24, respectively.

    public String createOutput( VariantType t ) {
        if( t == VariantType.CONC ) {
            return first_chrom + "\t" + first_start + "\t" + second_end;
        } else {
            return readname + "\t" + first_chrom + "\t" + first_start + "\t" + first_end + "\t" + first_ori + "\t" +
                   second_chrom + "\t" + second_start + "\t" + second_end + "\t" + second_ori;
        }
    }

	// output need to transfer 'X' and 'Y' into 23 and 24, respectively.
    // adds '_counter_0_0' to read name.

    public String createOutputAmbig( VariantType t, int counter ) {
        if( t == VariantType.CONC ) {
            return first_chrom + "\t" + first_start + "\t" + second_end;
        } else {
            return readname + "_" + counter + "_0_0\t" + first_chrom + "\t" + first_start + "\t" + first_end + "\t" + first_ori + "\t" +
                   second_chrom + "\t" + second_start + "\t" + second_end + "\t" + second_ori;
        }
    }


    public void printPair() {
        // can use any variant type except concordant to get ESP format.
        System.out.println( createOutput( VariantType.DEL ) );
    }


}
