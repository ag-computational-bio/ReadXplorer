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

package de.cebitec.readxplorer.parser.mappings;


import de.cebitec.readxplorer.parser.common.DiffAndGapResult;
import de.cebitec.readxplorer.parser.common.ParsedClassification;
import de.cebitec.readxplorer.parser.common.ParsedDiff;
import de.cebitec.readxplorer.parser.common.ParsedReferenceGap;
import de.cebitec.readxplorer.parser.common.RefSeqFetcher;
import de.cebitec.readxplorer.utils.MessageSenderI;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMTag;
import org.openide.util.NbBundle;

import static java.util.logging.Level.WARNING;
import static java.util.regex.Pattern.compile;


/**
 * Contains the methods which can be useful for more than one specific parser.
 * <p>
 * @author jstraube, Rolf Hilker
 */
@NbBundle.Messages( { "# {0} - filename",
                      "# {1} - lineNo",
                      "# {2} - read string",
                      "Parser.checkMapping.ErrorReadEmpty=Read sequence could not be parsed in {0} line {1}.Found: {2}" } )
public final class CommonsMappingParser {

    private static final Logger LOG = Logger.getLogger( CommonsMappingParser.class.getName() );

    /*
     * The cigar values are as follows: 0 (M) = alignment match (both, match or
     * mismatch), 1 (I) = insertion, 2 (D) = deletion, 3 (N) = skipped (=
     * ACG...TCG = 3 skipped bases), 4 (S) = soft clipped, 5 (H) = hard clipped
     * (not needed, because these bases are not present in the read sequence!),
     * 6 (P) = padding (a gap in the reference, which is also found in the
     * current record, but there is at least one record, that has an insertion
     * at that position), 7 (=) = sequene match, 8 (X) = sequence mismatch.
     */
    //option H-hard clipped not needed later, because it does not count into the alignment, but if not splitted, a number format exception is triggered
    public static final Pattern CIGAR_PATTERN = compile( "[MIDNSPXH=]+" );
    public static final Pattern DIGIT_PATTERN = compile( "\\d+" );
    public static final Pattern SPACE_REGEX = compile( " " );


    /**
     * Instantiation not allowed.
     */
    private CommonsMappingParser() {

    }


    /**
     * Counts the differences to the reference sequence for a cigar string and
     * the belonging read sequence. If the operation "M" is not used in the
     * cigar, then the read and reference sequence can be null (it is not used
     * in this case). Read and reference sequence are treated case
     * insensitively, so there is no need to transform the case beforehand.
     * <p>
     * @param cigar       the cigar string containing the alignment operations
     * @param readSeq     the read sequence belonging to the cigar and without
     *                    gaps
     * @param refSeq      the reference sequence area belonging to the cigar and
     *                    without gaps
     * @param isRevStrand true, if the ref seq has to be reverse complemented,
     *                    false if the read is on the fwd strand.
     * <p>
     * @return diff and gap result for the read and reference seq pair
     * <p>
     * @throws NumberFormatException thrown if cigar operation count is not an
     *                               integer
     */
    public static int countDiffsAndGaps( final String cigar, final String readSeq, final String refSeq, final boolean isRevStrand ) throws NumberFormatException {

        int differences = 0;
        int refPos = 0;
        int readPos = 0;
        final String[] num = CIGAR_PATTERN.split( cigar );
        final String[] charCigar = DIGIT_PATTERN.split( cigar );

        for( int i = 1; i < charCigar.length; ++i ) {

            final int currentCount = Integer.valueOf( num[i - 1] );
            switch( charCigar[i] ) {
                case "M":
                    //check, count and add diffs for deviating Ms
                    String bases = readSeq.substring( readPos, readPos + currentCount ).toUpperCase();//bases of the read interval under investigation
                    String refBases = refSeq.substring( refPos, refPos + currentCount );//bases of the reference corresponding to the read interval under investigation
                    for( int j = 0; j < bases.length(); ++j ) {
                        if( bases.charAt( j ) != refBases.charAt( j ) ) {
                            ++differences;
                        }
                    }
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "=":
                    //increase position for matches, skipped regions (N) and padded regions (P)
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "X":
                    //count and create diffs for mismatches
                    differences += currentCount;
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "D":
                    // count and add diff gaps for deletions in read
                    differences += currentCount;
                    refPos += currentCount;
                    break;
                case "I":
                    // count and add reference gaps for insertions
                    differences += currentCount;
                    readPos += currentCount;
                    // refPos remains the same
                    break;
                case "N":
                case "P":
                    refPos += currentCount;
                    //readPos remains the same
                    break;
                //H = hard bases are not present in the read string and pos in record, so don't inc. absPos
                case "S":
                    readPos += currentCount;
                    //refPos remains the same
                    break;
                default:
                    break;
            }
        }

        return differences;
    }


    /**
     * Counts the differences to the reference sequence for a cigar string and
     * the belonging read sequence. If the operation "M" is not used in the
     * cigar, then the read and reference sequence can be null (it is not used
     * in this case). Read and reference sequence are treated case
     * insensitively, so there is no need to transform the case beforehand. All
     * cigar operations need to be uppercase!
     * <p>
     * @param cigar       the cigar string containing the alignment operations
     * @param readSeq     the read sequence belonging to the cigar and without
     *                    gaps
     * @param refSeq      the reference sequence area belonging to the cigar and
     *                    without gaps
     * @param isRevStrand true, if the ref seq has to be reverse complemented,
     *                    false if the read is on the fwd strand.
     * @param start       start of the alignment of read and reference in the
     *                    reference
     * <p>
     * @return diff and gap result for the read and reference seq pair
     * <p>
     * @throws NumberFormatException thrown if cigar operation count is not an
     *                               integer
     */
    public static DiffAndGapResult createDiffsAndGaps( final String cigar, final String readSeq, final String refSeq, final boolean isRevStrand, final int start ) throws NumberFormatException {


        final String[] num = CIGAR_PATTERN.split( cigar );
        final String[] charCigar = DIGIT_PATTERN.split( cigar );
        final List<ParsedDiff> diffs = new ArrayList<>();
        final List<ParsedReferenceGap> gaps = new ArrayList<>();
        final Map<Integer, Integer> gapOrderIndex = new HashMap<>();

        int refPos = 0;
        int readPos = 0;
        int differences = 0;
        for( int i = 1; i < charCigar.length; ++i ) {

            final int currentCount = Integer.valueOf( num[i - 1] );
            switch( charCigar[i] ) {
                case "M":
                    //check, count and add diffs for deviating Ms
                    String bases = readSeq.substring( readPos, readPos + currentCount ).toUpperCase();//bases of the read interval under investigation
                    String refBases = refSeq.substring( refPos, refPos + currentCount ); //bases of the reference belonging to the read interval under investigation
                    for( int j = 0; j < bases.length(); ++j ) {
                        char base = bases.charAt( j );
                        if( base != refBases.charAt( j ) ) {
                            ++differences;
                            if( isRevStrand ) {
                                base = SequenceUtils.getDnaComplement( base );
                            }
                            diffs.add( new ParsedDiff( refPos + j + start, base ) );
                        }
                    }
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "=":
                    //only increase position for matches
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "X":
                    //count and create diffs for mismatches
                    differences += currentCount;
                    for( int j = 0; j < currentCount; ++j ) {
                        char base = readSeq.charAt( readPos + j );
                        if( isRevStrand ) {
                            base = SequenceUtils.getDnaComplement( base );
                        }
                        diffs.add( new ParsedDiff( refPos + j + start, base ) );
                    }
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "D":
                    // count and add diff gaps for deletions in read
                    differences += currentCount;
                    for( int j = 0; j < currentCount; ++j ) {
                        diffs.add( new ParsedDiff( refPos + j + start, '_' ) );
                    }
                    refPos += currentCount;
                    // readPos remains the same
                    break;
                case "I":
                    // count and add reference gaps for insertions
                    differences += currentCount;
                    for( int j = 0; j < currentCount; ++j ) {
                        char base = readSeq.charAt( readPos + j );
                        if( isRevStrand ) {
                            base = SequenceUtils.getDnaComplement( base );
                        }
                        gaps.add( new ParsedReferenceGap( refPos + start, base, getOrderForGap( refPos + start, gapOrderIndex ) ) );
                    }   //refPos remains the same
                    readPos += currentCount;
                    break;
                case "N":
                case "P":
                    //increase position for padded and skipped reference bases
                    refPos += currentCount;
                    //readPos remains the same
                    break;
                //H = hard clipping does not contribute to differences
                case "S":
                    //increase read position for soft clipped bases which are present in the read
                    //refPos remains the same
                    readPos += currentCount;
                    break;
                default:
                    break;
            }
        }

        return new DiffAndGapResult( diffs, gaps, differences );
    }


    /**
     * Creates diffs and gaps for a given read and reference sequence. Both are
     * treated case insensitively, so there is no need to transform the case
     * beforehand.
     * <p>
     * @param readSeq   read whose diffs and gaps are calculated
     * @param refSeq    reference sequence aligned to the read sequence
     * @param start     start position on the whole chromosome (absolute
     *                  position)
     * @param direction direction of the read
     * <p>
     * @return the diff and gap result for the read
     */
    public static DiffAndGapResult createDiffsAndGaps( String readSeq, String refSeq, int start, final byte direction ) {

        final Map<Integer, Integer> gapOrderIndex = new HashMap<>();
        final List<ParsedDiff> diffs = new ArrayList<>();
        final List<ParsedReferenceGap> gaps = new ArrayList<>();
        readSeq = readSeq.toUpperCase();
        refSeq = refSeq.toUpperCase();

        int errors = 0;
        for( int i = 0; i < readSeq.length(); i++ ) {
            if( readSeq.charAt( i ) != refSeq.charAt( i ) ) {
                errors++;
                char base = readSeq.charAt( i );
                if( direction == SequenceUtils.STRAND_REV ) {
                    base = SequenceUtils.getDnaComplement( base );
                }
                if( refSeq.charAt( i ) == '_' ) {
                    // store a lower case char, if this is a gap in genome
                    ParsedReferenceGap gap = new ParsedReferenceGap( start, base, getOrderForGap( start, gapOrderIndex ) );
                    gaps.add( gap );
                    // note: do not increase position. that means that next base of read is mapped
                    // to the same position as this gap. two subsequent gaps map to the same position!
                } else {
                    // store the char from input file, if this is a modification in the read
                    ParsedDiff diff = new ParsedDiff( start, base );
                    diffs.add( diff );
                    ++start;
                }
            } else {
                ++start;
            }
        }

        return new DiffAndGapResult( diffs, gaps, errors );
    }


    /**
     * This method calculates the order of the gaps. For a gap we don't include
     * a new position in the reference genome, but we store the number of gaps
     * for one position of the ref genome.
     * <p>
     * @param gapPos        position of the gap
     * @param gapOrderIndex the gap order index for the current gap (larger the
     *                      more gaps in a row
     * <p>
     * @return the new gap order index for the gap (starting with 0)
     */
    public static int getOrderForGap( int gapPos, Map<Integer, Integer> gapOrderIndex ) {
        if( !gapOrderIndex.containsKey( gapPos ) ) {
            gapOrderIndex.put( gapPos, 0 );
        }
        int order = gapOrderIndex.get( gapPos );

        // increase order for next request
        gapOrderIndex.put( gapPos, order + 1 );

        return order;
    }


    /**
     * This method tries to convert the cigar string to the mapping again
     * because SAM format has no other mapping information
     * <p>
     * @param cigar   contains mapping information of reference and read
     *                sequence M can be a Match or Mismatch, D is a deletion on
     *                the read, I insertion on the read, S softclipped read
     * @param refSeq  reference sequence corresponding to read seq
     * @param readSeq read sequence
     * <p>
     * @return the refSeq with gaps in fact of insertions in the reads
     */
    @NbBundle.Messages( { "# {0} - observed cigar operation", "CommonMethod.CIGAR=CIGAR character is unknown {0}" } )
    public static String[] createMappingOfRefAndRead( String cigar, String refSeq, String readSeq ) {
        // TODO: check this
        String newRefSeqwithGaps = null;
        String newreadSeq = null;

        int refpos = 0;
        int readPos = 0;
        int softclipped = 0;

        final String[] num = CIGAR_PATTERN.split( cigar );
        final String[] charCigar = DIGIT_PATTERN.split( cigar );
        for( int i = 1; i < charCigar.length; i++ ) {
            String op = charCigar[i];
            String numOfBases = num[i - 1];

            switch( op ) {
                case "D":
                case "N":
                case "P":
                    //deletion of the read
                    int numberofDeletion = Integer.parseInt( numOfBases );
                    refpos += numberofDeletion;
                    while( numberofDeletion > 0 ) {
                        if( readSeq.length() != readPos ) {
                            readSeq = readSeq.substring( 0, readPos ).concat( "_" ) + readSeq.substring( readPos, readSeq.length() );
                        } else {
                            readSeq = readSeq.substring( 0, readPos ).concat( "_" );
                        }
                        --numberofDeletion;
                        newreadSeq = readSeq;
                        ++readPos;
                        //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq + "cigar" + cigar);
                    }
                    break;
                case "I":
                    //insertion of the  read
                    int numberOfInsertions = Integer.parseInt( numOfBases );
                    readPos += numberOfInsertions;
                    while( numberOfInsertions > 0 ) {

                        if( refpos != refSeq.length() ) {
                            refSeq = refSeq.substring( 0, refpos ).concat( "_" ) + refSeq.substring( refpos, refSeq.length() );
                        } else {
                            refSeq = refSeq.substring( 0, refpos ).concat( "_" );
                        }
                        newRefSeqwithGaps = refSeq;
                        --numberOfInsertions;
                        ++refpos;

                        //   Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq);
                    }
                    break;
                case "M":
                case "=":
                case "X":
                    //for match/mismatch thr positions just move forward
                    readPos += Integer.parseInt( numOfBases );
                    refpos += Integer.parseInt( numOfBases );
                    newRefSeqwithGaps = refSeq;
                    newreadSeq = readSeq;
                    break;
                case "S":
                    if( i > 1 ) {
                        //soft clipping of the last bases
                        newreadSeq = newreadSeq.substring( 0, readSeq.length() - Integer.parseInt( numOfBases ) );
                    } else {
                        //soft clipping of the first bases
                        readPos += Integer.parseInt( numOfBases );
                        softclipped = Integer.parseInt( numOfBases );
                    }
                    break;
                default: //shoud never happen as SAMRecord validates the cigar
                    LOG.log( WARNING, Bundle.CommonMethod_CIGAR( op ) );
                    break;
            }
        }
        newreadSeq = newreadSeq.substring( softclipped, newreadSeq.length() );
        String[] refAndRead = new String[2];
        refAndRead[0] = newRefSeqwithGaps;
        refAndRead[1] = newreadSeq;
        return refAndRead;
    }


    /**
     * Checks SAMRecord read properties necessary for ReadXplorer:
     * <br/>'*' read sequence is not permitted.
     * <p>
     * @param parent   the parent observable to receive messages
     * @param readSeq  the read sequence
     * @param filename the file name of which the mapping originates
     * @param lineNo   the line number in the filelineNo<p>
     * @return true, if the read is consistent, false otherwise
     */
    public static boolean checkReadSam( final MessageSenderI parent,
                                        final String readSeq,
                                        final String filename,
                                        final int lineNo ) {

        boolean isConsistent = true;
        if( SAMRecord.NULL_SEQUENCE_STRING.equals( readSeq ) ) { //RX requires read strings
            parent.sendMsgIfAllowed( Bundle.Parser_checkMapping_ErrorReadEmpty( filename, lineNo, readSeq ) );
            isConsistent = false;
        }
//        Check if read maps beyond reference sequence not needed because we
//        need the CIGAR and SamRecord automatically validates the
//        AlignmentBlocks

//        Check if start > stop not necessary, because SAMRecord calculates the
//        stop based on the start + length

        return isConsistent;
    }


    /**
     * Checks a read for common properties:
     * <br>1. Empty or null read sequence
     * <br>2. mapping beyond the reference sequence length or to negative
     * positions
     * <br>3. a start position larger than the stop position
     * <br>4. an empty read name
     * <br>5. an empty refrence sequence
     * <br>6. an unknown mapping orientation
     * <p>
     * @param parent       the parent observable to receive messages
     * @param readSeq      the read sequence
     * @param readname     the name of the read
     * @param refSeq       reference sequence beloning to the mapping (not the
     *                     complete reference genome)
     * @param refSeqLength the length of the reference sequence
     * @param start        the start of the mapping
     * @param stop         the stop of the mapping
     * @param direction    direction of the mapping
     * @param filename     the file name of which the mapping originates
     * @param lineNo       the line number in the file
     * <p>
     * @return true, if the read is consistent, false otherwise
     */
    @NbBundle.Messages( { "# {0} - filename",
                          "# {1} - lineNo",
                          "# {2} - read name",
                          "Parser.checkMapping.ErrorReadname=Could not read readname in {0} line {1}. Found read name: {2}",
                          "# {0} - filename",
                          "# {1} - lineNo",
                          "Parser.checkMapping.ErrorDirectionJok=Could not parse direction in {0}  line {1}. Must be >> oder <<",
                          "# {0} - filename",
                          "# {1} - lineNo",
                          "# {2} - reference sequence",
                          "Parser.checkMapping.ErrorRef=Reference sequence could not be parsed in {0} line {1}.Found: {2}" } )
    public static boolean checkReadJok(
            final MessageSenderI parent,
            final String readSeq,
            final String readname,
            final String refSeq,
            final int refSeqLength,
            final int start,
            final int stop,
            final int direction,
            final String filename,
            final int lineNo ) {

        boolean isConsistent = CommonsMappingParser.checkRead( parent, refSeqLength, start, stop, filename, lineNo );

        if( readSeq == null || readSeq.isEmpty() ) {
            parent.sendMsgIfAllowed( Bundle.Parser_checkMapping_ErrorReadEmpty( filename, lineNo, readSeq ) );
            isConsistent = false;
        }
        if( readname == null || readname.isEmpty() ) {
            parent.sendMsgIfAllowed( Bundle.Parser_checkMapping_ErrorReadname( filename, lineNo, readname ) );
            isConsistent = false;
        }
        if( direction == 0 ) {
            parent.sendMsgIfAllowed( Bundle.Parser_checkMapping_ErrorDirectionJok( filename, lineNo ) );
            isConsistent = false;
        }
        if( refSeq == null || refSeq.isEmpty() ) {
            parent.sendMsgIfAllowed( Bundle.Parser_checkMapping_ErrorRef( filename, lineNo, refSeq ) );
            isConsistent = false;
        }

        return isConsistent;
    }


    /**
     * Checks a read for common properties: <br>1. Empty or null read sequence
     * <br>2. mapping beyond the reference sequence length or to negative
     * positions <br>3. a start position larger than the stop position
     * <p>
     * @param parent       the parent observable to receive messages
     * @param refSeqLength the length of the reference sequence
     * @param start        the start of the mapping
     * @param stop         the stop of the mapping
     * @param filename     the file name of which the mapping originates
     * @param lineNo       the line number in the filelineNo<p>
     * @return true, if the read is consistent, false otherwise
     */
    @NbBundle.Messages( { "# {0} - filename",
                          "# {1} - lineNo",
                          "# {2} - start",
                          "# {3} - stop",
                          "# {4} - ref length",
                          "Parser.checkMapping.ErrorReadPosition=Could not read readname in {0} line {1}. Reference is shorter than mapping! Start: {2} Stop: {3} Reference length: {4}",
                          "# {0} - filename",
                          "# {1} - lineNo",
                          "# {2} - start",
                          "# {3} - stop",
                          "Parser.checkMapping.ErrorStartStop=Start bigger than stop in {0}  line {1}. Found start: {2}, stop:{3}" } )
    public static boolean checkRead( final MessageSenderI parent,
                                     final int refSeqLength,
                                     final int start,
                                     final int stop,
                                     final String filename,
                                     final int lineNo ) {

        boolean isConsistent = true;
        if( refSeqLength < start || refSeqLength < stop ) {
            parent.sendMsgIfAllowed( Bundle.Parser_checkMapping_ErrorReadPosition( filename, lineNo, start, stop, refSeqLength ) );
            isConsistent = false;
        }
        if( start >= stop ) {
            parent.sendMsgIfAllowed( Bundle.Parser_checkMapping_ErrorStartStop( filename, lineNo, start, stop ) );
            isConsistent = false;
        }

        return isConsistent;
    }


    /**
     * In fact deletions in the read shift the stop position of the ref genome
     * mapping, we need to count the number of deletions to calculate the stop
     * position of the read in the ref genome.
     * <p>
     * @param cigar         contains mapping information
     * @param startPosition of the mapping
     * @param readLength    the length of the read
     * <p>
     * @return Corrected stop position of the read
     */
    public static int countStopPosition( String cigar, Integer startPosition, Integer readLength ) {

        int numberofDeletion = 0;
        int numberofInsertion = 0;
        int numberofSoftclipped = 0;
        final String[] num = CIGAR_PATTERN.split( cigar );
        final String[] charCigar = DIGIT_PATTERN.split( cigar );
        for( int i = 1; i < charCigar.length; i++ ) {
            String op = charCigar[i];
            int numOfBases = Integer.parseInt( num[i - 1] );
            if( op.contains( "D" ) || op.contains( "N" ) || op.contains( "P" ) ) {
                numberofDeletion += numOfBases;
            }
            if( op.contains( "I" ) ) {
                numberofInsertion += numOfBases;
            }
            if( op.contains( "S" ) ) {
                numberofSoftclipped += numOfBases;
            }
        }

        return startPosition + readLength - 1 + numberofDeletion - numberofInsertion - numberofSoftclipped;

    }


    /**
     * If the read name contains a pair tag, it returns the read name without
     * the pair tag for both read names prior to Casava 1.8 and read names in
     * Casava 1.8 format. If there is no pair tag, the read name remains
     * unchanged.
     * <p>
     * @param readNameFull The read name whose pair tag shall be removed if it
     *                     contains one
     * <p>
     * @return A pair: The First element is a boolean indicating whether the
     *         read name has been modified (<code>true</code>) or not
     *         (<code>false</code>). The second element is the read name without
     *         its pair tag.
     */
    public static Pair<Boolean, String> getReadNameWithoutPairTag( final String readNameFull ) {
        boolean changed = false;
        String readName = readNameFull;
        String[] nameParts = SPACE_REGEX.split( readNameFull );
        if( nameParts.length == 2 && (nameParts[1].startsWith( "1" ) || nameParts[1].startsWith( "2" )) ) {
            readName = nameParts[0];
            changed = true;
        } else {
            final char lastChar = readName.charAt( readName.length() - 1 );
            final char prevLastChar = readName.charAt( readName.length() - 2 );

            if( prevLastChar == Properties.EXT_SEPARATOR &&
                (lastChar == Properties.EXT_A1 || lastChar == Properties.EXT_A2 ||
                 lastChar == Properties.EXT_B1 || lastChar == Properties.EXT_B2) ) {

                readName = readNameFull.substring( 0, readNameFull.length() - 2 );
                changed = true;
            }
        }

        return new Pair<>( changed, readName );
    }


    /**
     * Calculates the pair tag for a given sam record. It checks the
     * ReadPairedFlag of the sam record, the ending of the read name for read
     * names prior to CASAVA 1.8 and CASAVA > 1.8 formatted read names for an
     * appropriate pair flag until it is found. If no paired read tag can be
     * deduced, the method returns a neutral pairTag for single end mapped
     * reads.
     * <p>
     * @param record the record to check for a pair tag
     * <p>
     * @return Either '1' for first read of pair, '2' for second read of pair or
     *         '0' for a single end mapping
     */
    public static char getReadPairTag( final SAMRecord record ) {

        char pairTag = Properties.EXT_UNDEFINED;

        //if paired read flag is set, we can directly use it
        if( record.getReadPairedFlag() ) {
            pairTag = record.getFirstOfPairFlag() ? Properties.EXT_A1 : Properties.EXT_A2;

        } else {
            final String readName = record.getReadName();
            if( readName.length() > 2 ) {

                final char lastChar = readName.charAt( readName.length() - 1 );
                final char prevLastChar = readName.charAt( readName.length() - 2 );

                if( prevLastChar == Properties.EXT_SEPARATOR ) {
                    if( lastChar == Properties.EXT_A1 || lastChar == Properties.EXT_B1 ) {
                        pairTag = Properties.EXT_A1;

                    } else if( lastChar == Properties.EXT_A2 || lastChar == Properties.EXT_B2 ) {
                        pairTag = Properties.EXT_A2;
                    }
                } else {

                    //check for casava > 1.8 paired read
                    String[] nameParts = SPACE_REGEX.split( readName );
                    if( nameParts.length == 2 ) {
                        if( nameParts[1].startsWith( Properties.EXT_A1_STRING ) ) {
                            pairTag = Properties.EXT_A1;
                        } else if( nameParts[1].startsWith( Properties.EXT_A2_STRING ) ) {
                            pairTag = Properties.EXT_A2;
                        }
                    }
                }
            }
        }
        return pairTag;
    }


    /**
     * Checks if a read name is written in the casava format > 1.8, in which the
     * pair tag appears after a blank in the read name.
     * <p>
     * @param readName read name to check
     * <p>
     * @return true, if the read is in the casava format > 1.8, false otherwise
     */
    public static boolean isCasavaLarger1Dot8Format( final String readName ) {
        String[] nameParts = SPACE_REGEX.split( readName );
        return nameParts.length == 2 && (nameParts[1].startsWith( Properties.EXT_A1_STRING ) ||
                                         nameParts[1].startsWith( Properties.EXT_A2_STRING ));
    }


    /**
     * Adds a {@link Properties.EXT_A1} or {@link Properties.EXT_A2) at the end
     * of the given records read name, if it is a paired read and does not
     * already contain a paired read ending.
     * <p>
     * @param record the record whose read name should be elongated, if it is a
     *               paired read
     * <p>
     * @return The elongated read name or the original one, if it already had a
     *         paired read ending
     */
    public static String elongatePairedReadName( final SAMRecord record ) {

        String readName = record.getReadName();
        final char pairTag = readName.charAt( readName.length() - 1 );
        if( record.getReadPairedFlag() && pairTag != Properties.EXT_A1 && pairTag != Properties.EXT_B1 &&
            pairTag != Properties.EXT_A2 && pairTag != Properties.EXT_B2 &&
            !isCasavaLarger1Dot8Format( readName ) ) {
            readName += "/" + (record.getFirstOfPairFlag() ? Properties.EXT_A1 : Properties.EXT_A2);
        }
        return readName;
    }


    /**
     * Checks if the read has a proper pair tag including Casava > 1.8 formatted
     * reads. If a proper pair tag is available, nothing is changed. If not, a
     * new ending is added to the record's read name according to
     * <code>isFstFile</code>.
     * <p>
     * @param record The record to check and update
     */
    public static void checkOrRemovePairTag( final SAMRecord record ) {
        Pair<Boolean, String> readNamePair = CommonsMappingParser.getReadNameWithoutPairTag( record.getReadName() );
        if( readNamePair.getFirst() ) {
            record.setReadName( readNamePair.getSecond() );
        }
    }


    /**
     * Converts the the decimal number (flag) into binary code and checks if 4
     * is 1 or 0
     * <p>
     * @param flag          The flag to check
     * @param startPosition start pos of mapping
     * <p>
     * @return true, if it is a mapped sequence, false otherwise
     */
    public static boolean isMappedSequence( final int flag, final int startPosition ) {
        boolean isMapped = true;
        if( flag >= 4 ) {
            String binaryValue = Integer.toBinaryString( flag );
            int binaryLength = binaryValue.length();
            String b = binaryValue.substring( binaryLength - 3, binaryLength - 2 );

            isMapped = !b.equals( "1" ) && startPosition != 0;
        }
        return isMapped;
    }


    /**
     * Adds the classification data (type and number of mapped positions) to the
     * sam records. Use this method, if the number of differences is already
     * known.
     * <p>
     * @param recordToDiffMap map of sam records to their number of mismatches
     *                        to update with classification data
     * @param classification  the classification data for the current list of
     *                        records.
     * <p>
     * @throws AssertionError thrown if something could not be asserted
     */
    public static void addClassificationData( final Map<SAMRecord, Integer> recordToDiffMap,
                                              final ParsedClassification classification ) throws AssertionError {

        final int lowestDiffRate = classification.getMinMismatches();
        final Map<Integer, Integer> mismatchCountMap = classification.getMismatchCountMap();

        for( Map.Entry<SAMRecord, Integer> entry : recordToDiffMap.entrySet() ) {
            final SAMRecord record = entry.getKey();
            final int differences = entry.getValue();
            final int nextMappingPos = classification.getNextMappingStart( record.getAlignmentStart() );

            if( nextMappingPos > 0 ) {
                record.setAttribute( SAMTag.CP.name(), nextMappingPos );
                record.setAttribute( SAMTag.CC.name(), "=" );
            }

            int sameMismatchCount = 1;
            if( mismatchCountMap.containsKey( differences ) ) {
                sameMismatchCount = mismatchCountMap.get( differences );
            }

            if( differences == 0 ) { //perfect mapping
                if( sameMismatchCount == 1 ) {
                    record.setAttribute( Properties.TAG_READ_CLASS, MappingClass.SINGLE_PERFECT_MATCH.getTypeByte() );
                } else {
                    record.setAttribute( Properties.TAG_READ_CLASS, MappingClass.PERFECT_MATCH.getTypeByte() );
                }

            } else if( differences == lowestDiffRate ) { //best match mapping
                if( sameMismatchCount == 1 ) {
                    record.setAttribute( Properties.TAG_READ_CLASS, MappingClass.SINGLE_BEST_MATCH.getTypeByte() );
                } else {
                    record.setAttribute( Properties.TAG_READ_CLASS, MappingClass.BEST_MATCH.getTypeByte() );
                }

            } else if( differences > lowestDiffRate ) { //common mapping
                record.setAttribute( Properties.TAG_READ_CLASS, MappingClass.COMMON_MATCH.getTypeByte() );

            } else { //meaning: differences < lowestDiffRate
                throw new AssertionError( "Cannot contain less than the lowest diff rate number of differences!" );
            }
            record.setAttribute( Properties.TAG_MAP_COUNT, classification.getNumberOccurrences() );
        }
    }


    /**
     * Adds the classificationData (type and number of mapped positions) to the
     * SAM records in the diffMap. After extending the SAM records, they are
     * written by the given writer. The diffMap is cleared after writing the
     * data.
     * <p>
     * @param diffMap            map of sam records to the number of differences
     *                           to the reference
     * @param classificationData parsed classification data to add to the
     *                           records
     * @param samBamWriter       writer to write the SAM records to
     */
    public static void writeSamRecord( final Map<SAMRecord, Integer> diffMap, ParsedClassification classificationData,
                                       final SAMFileWriter samBamWriter ) {

        //store data and clear data structure, if new read name is reached - file needs to be sorted by read name
        CommonsMappingParser.addClassificationData( diffMap, classificationData );
        for( SAMRecord rec : diffMap.keySet() ) {
            samBamWriter.addAlignment( rec );
        }

        //reset data structures for next read name
        diffMap.clear();
    }


    /**
     * Checks if a mapping contains consistent data. For consistent mappings,
     * the ReadXplorer classification data is created and stored in the given
     * classificationData.
     * <p>
     * @param record             record to classify
     * @param messageSender      Sender who should be updated, if errors occur
     * @param chromLengthMap     chromosome length map
     * @param fileName           mapping file name from which the record
     *                           originates
     * @param lineNo             the line number of the current record in the
     *                           file
     * @param refSeqFetcher      a fetcher for the reference sequence
     * @param diffMap            map of sam records to the number of differences
     *                           to the reference, is updated by this method
     * @param classificationData object in which the classification data is
     *                           stored by this method
     * <p>
     * @return <code>true</code>, if the mapping data is consistent,
     *         <code>false</code> otherwise
     */
    public static boolean classifyRead( final SAMRecord record, MessageSenderI messageSender,
                                        final Map<String, Integer> chromLengthMap,
                                        final String fileName,
                                        final int lineNo,
                                        final RefSeqFetcher refSeqFetcher,
                                        final Map<SAMRecord, Integer> diffMap,
                                        final ParsedClassification classificationData ) {
        final String readSeq = record.getReadString();
        boolean isConsistent = CommonsMappingParser.checkReadSam( messageSender, readSeq, fileName, lineNo );

//            ++noSkippedReads;
//            continue; //continue, and ignore read, if it contains inconsistent information

        if( isConsistent ) {

//            The cigar values are as follows:
//            0 (M) = alignment match (both, match or mismatch),
//            1 (I) = insertion, 2 (D) = deletion,
//            3 (N) = skipped,
//            4 (S) = soft clipped,
//            5 (H) = hard clipped,
//            6 (P) = padding,
//            7 (=) = sequene match,
//            8 (X) = sequence mismatch.
//            H not needed, because these bases are not present in the read
//            sequence!

            //count differences to reference
            final int start = record.getAlignmentStart();
            final int stop = record.getAlignmentEnd();
            String refSeq = refSeqFetcher.getSubSequence( record.getReferenceName(), start, stop );
            boolean isRevStrand = record.getReadNegativeStrandFlag();
            final String cigar = record.getCigarString();
            DiffAndGapResult diffGapResult = CommonsMappingParser.createDiffsAndGaps( cigar, readSeq, refSeq, isRevStrand, start );
            int mismatches = diffGapResult.getDifferences();
            diffMap.put( record, mismatches );
            classificationData.addReadStart( start );
            classificationData.updateMinMismatches( mismatches );
            classificationData.updateMismatchCountMap( mismatches );
        }
        return isConsistent;
    }

//    /**
//     * converts the the decimal number into binary code and checks if 16 is 1 or 0
//     * @param flag contains information wheater the read is mapped on the rev or fw strand
//     * @return wheater the read is mapped on the rev or fw strand
//     */
//    public static boolean isForwardRead(int flag) {
//        boolean isForward = true;
//        if (flag >= 16) {
//            String binaryValue = Integer.toBinaryString(flag);
//            int binaryLength = binaryValue.length();
//            String b = binaryValue.substring(binaryLength - 5, binaryLength - 4);
//
//            if (b.equals("1")) {
//                isForward = false;
//            } else {
//                isForward = true;
//            }
//        }
//        return isForward;
//    }

//    /**
//     * TODO Can be used for homopolymer snp detection, to flag snps in homopolymers. needed?
//     * @param genome
//     * @param snp
//     * @return
//     */
//        public static boolean snpHasStretch(String genome, int snp) {
//        String beforeSNP = genome.substring(0, 1);
//
//        if (snp == 1) {
//            beforeSNP = genome.substring(snp - 1, snp);
//        } else
//        if (snp == 2) {
//            beforeSNP = genome.substring(snp - 2, snp);
//        } else
//        if (snp == 3) {
//            beforeSNP = genome.substring(snp - 3, snp);
//        } else
//        if (snp >= 4) {
//            beforeSNP = genome.substring(snp - 4, snp);
//        }
//        System.out.println("before" + beforeSNP);
//        String afterSNP = genome.substring(snp, snp + 4);
//        System.out.println("afterSnp:" + afterSNP);
//        boolean hasStretch = false;
//        if (beforeSNP.matches("[atgc]{4,8}") || afterSNP.matches("[atgc]{4,8}")) {
//            hasStretch = true;
//        }
//        if (beforeSNP.matches("[atgc]{1,8}") && afterSNP.matches("[atgc]{3,8}")) {
//            System.out.println("1-3" + hasStretch);
//        }
//        if (beforeSNP.matches("[atgc]{3,8}")) {
//            String charBefore = beforeSNP.substring(beforeSNP.length() - 1, beforeSNP.length());
//            System.out.println("charbefore " + charBefore);
//            String charAfter = afterSNP.substring(0, 1);
//            String regex = charBefore.concat("{1}");
//            if (charAfter.matches(regex)) {
//                System.out.println("3-1" + hasStretch);
//            }
//        }
//        if (afterSNP.matches("[atgc]{3,8}")) {
//            String charBefore = beforeSNP.substring(beforeSNP.length() - 1, beforeSNP.length());
//            String regex = afterSNP.substring(0, 1).concat("{1}");
//            if (charBefore.matches(regex)) {
//                System.out.println("3-1" + hasStretch + " " + regex);
//            }
//        }
//        System.out.println(hasStretch);
//        return hasStretch;
//    }
}
