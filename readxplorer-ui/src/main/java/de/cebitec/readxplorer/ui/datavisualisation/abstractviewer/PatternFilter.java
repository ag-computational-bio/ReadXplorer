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

package de.cebitec.readxplorer.ui.datavisualisation.abstractviewer;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.SequenceUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Filters for a given pattern in two ways: First for all
 * available occurrences of the pattern in a given interval of a given DNA
 * sequence
 * and second for the next occurrence of the pattern along a DNA sequence, such
 * as a whole genome sequence.
 *
 * @author ddoppmeier, rhilker
 */
public class PatternFilter implements RegionFilterI {

    public static final int INIT = 10;
    private static final int INTERVAL_SIZE = 3000000;
    private final List<Region> matchedPatterns;
    private int absStart;
    private int absStop;
    private final PersistentReference refGen;
    private String sequence;
    private Pattern pattern;
//    private Pattern patternRev;


    public PatternFilter( int absStart, int absStop, PersistentReference refGen ) {
        this.matchedPatterns = new ArrayList<>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;
    }


    /**
     * Identifies the currently set pattern in a given interval of the reference
     * sequence stored in this object.
     * <p>
     * @return A list of the positions where the pattern matched along the
     *         interval.
     */
    private List<Region> findPatternInInterval() {

        if( this.pattern != null && !this.pattern.toString().isEmpty() ) {

            int offset = this.pattern.toString().length(); //shift by pattern length to left
            int start = this.absStart - offset;
            int stop = this.absStop + offset - 1;

            if( start <= 0 ) {
                offset -= Math.abs( start ) + 1;
                start = 1;
            }
            if( stop > this.refGen.getActiveChromLength() ) {
                stop = this.refGen.getActiveChromLength();
            }

            for( int i = start; i <= stop; i++ ) {
                if( i + INTERVAL_SIZE <= stop ) {
                    this.sequence = refGen.getActiveChromSequence( i, i + INTERVAL_SIZE );
                }
                else {
                    this.sequence = refGen.getActiveChromSequence( i, stop );
                }
                this.matchPattern( this.sequence, this.pattern, true, offset );
                this.sequence = SequenceUtils.getReverseComplement( this.sequence );
                this.matchPattern( this.sequence, this.pattern, false, offset );
                i += INTERVAL_SIZE;
            }
        }
        return this.matchedPatterns;

    }


    /**
     * Identifies next (closest) occurrence from either forward or reverse
     * strand of a pattern in the current reference genome.
     * <p>
     * @return the position of the next occurrence of the pattern
     */
    public int findNextOccurrence() {

        int refLength = this.refGen.getActiveChromosome().getLength();
        int from = -1;
        int from2;
        if( !(this.pattern == null) && !this.pattern.toString().isEmpty() ) {

            int start = this.absStop;

            if( this.absStart <= 0 ) {
                this.absStart = 1;
            }
            if( start > refLength ) {
                start = 1;
            }

            String seq;
            for( int i = start; i <= refLength; i++ ) {
                if( i + INTERVAL_SIZE <= refLength ) {
                    seq = refGen.getActiveChromSequence( i, i + INTERVAL_SIZE );
                }
                else {
                    seq = refGen.getActiveChromSequence( i, refLength );
                }
                i += INTERVAL_SIZE;
                String seqRev = SequenceUtils.getReverseComplement( seq );

                //at first search from current position till end of sequence on both frames
                from = this.matchNextOccurrence( seq, this.pattern );
                from2 = this.matchNextOccurrence( seqRev, this.pattern );

                //then search from 0 to current position on both frames
                if( from == -1 && from2 == -1 && start > 0 ) {
                    for( int j = 1; j <= start; i++ ) {
                        if( from == -1 && from2 == -1 && start > 0 ) {
                            if( j + INTERVAL_SIZE <= start ) {
                                seq = refGen.getActiveChromSequence( j, j + INTERVAL_SIZE );
                            }
                            else {
                                seq = refGen.getActiveChromSequence( j, start );
                            }
                            j += INTERVAL_SIZE;
                            seqRev = SequenceUtils.getReverseComplement( seq );

                            from = this.matchNextOccurrence( seq, this.pattern );
                            from2 = this.matchNextOccurrence( seqRev, this.pattern );
                        }
                        else {
                            start = 1;
                            break;
                        }
                    }
                }

                if( from < from2 && from != -1 || from2 == -1 && from > from2 ) {
                    return from + start;
                }
                else if( from2 != -1 ) {
                    return seq.length() - from2 + start - 1;
                }
                else { /* both are -1*/ }
            }
        }
        return from;
    }


    /**
     * Identifies the end position of the next (closest) occurrence from either
     * forward or reverse strand of a pattern in the current reference genome in
     * the correct reading frame.
     * <p>
     * @param isFwdStrand true, if the next occurrence on the fwd strand is
     *                    needed, false if the next occurrence on the rev strand is needed
     * <p>
     * @return the end position of the next occurrence of the pattern
     */
    public int findNextOccurrenceOnStrand( boolean isFwdStrand ) {
        int start = -1;
        if( this.pattern != null && !this.pattern.toString().isEmpty() ) {
            if( isFwdStrand ) {
                start = this.findNextOnFwdStrand();
            }
            else {
                start = this.findNextOnRevStrand();
            }
        }
        return start;
    }


    /**
     * @return Identifies the next occurrence of pattern "p" in the given
     *         "sequence" on the fwd strand starting in the desired reading frame and
     *         returns its end position.
     */
    private int findNextOnFwdStrand() {
        boolean isCorrectFrame = false;
        String seq;
        int refLength = this.refGen.getActiveChromosome().getLength();
        int patternLength = this.pattern.toString().length();
        int start = this.absStart + patternLength; //start with the stop+1 pos of current codon
        int from;
        int end;
        while( start < refLength && !isCorrectFrame ) {
            if( start + INTERVAL_SIZE <= refLength ) {
                end = start + INTERVAL_SIZE;
            }
            else {
                end = refLength;
            }
            seq = refGen.getActiveChromSequence( start, end ); //TODO: first check whole seq object for fitting hits
            from = this.matchNextOccurrence( seq, this.pattern );
            if( from != -1 ) {
                start += from;
                isCorrectFrame = (start % 3 == this.absStart % 3);
                if( isCorrectFrame ) {
                    break;
                }
            }
            else {
                start += INTERVAL_SIZE - patternLength;
            }
            ++start; //we want to continue at the first possible hit position
        }
        start = start >= refLength && !isCorrectFrame ? -1 : start + patternLength - 1; //set to -1 if no hit in correct frame was found
        return start;
    }


    /**
     * @return Identifies the next occurrence of pattern "p" in the given
     *         "sequence" on the rev strand starting in the desired reading frame and
     *         returns its end position.
     */
    private int findNextOnRevStrand() {
        boolean isCorrectFrame = false;
        String seq;
        String seqRev;
        int patternLength = this.pattern.toString().length();
        int start = this.absStart - patternLength;
        int from;
        int end;
        while( start > 0 && !isCorrectFrame ) {
            if( start - INTERVAL_SIZE > 0 ) { //sequence we start with
                end = start - INTERVAL_SIZE;
            }
            else {
                end = 1;
            } //reverse complement dna and start with the stop pos of current codon
            seq = refGen.getActiveChromSequence( end, start );
            seqRev = SequenceUtils.getReverseComplement( seq );
            from = this.matchNextOccurrence( seqRev, this.pattern );
            if( from != -1 ) {
                start -= from;
                isCorrectFrame = (start % 3 == this.absStart % 3);
                if( isCorrectFrame ) {
                    break;
                }
            }
            else {
                start -= INTERVAL_SIZE + patternLength;
            }
            --start;
        }
        start = start <= 0 && !isCorrectFrame ? -1 : start - patternLength + 1; //set to -1 if no hit in correct frame was found
        return start;
    }


    /**
     * Identifies pattern "p" in the given "sequence" and stores positive
     * results
     * in this class' region list.
     * <p>
     * @param sequence        the sequence to analyse
     * @param p               pattern to search for
     * @param isForwardStrand if pattern is fwd or rev
     * @param offset          offset needed for storing the correct region
     *                        positions
     */
    private void matchPattern( String sequence, Pattern p, boolean isForwardStrand, int offset ) {

        final Matcher m = p.matcher( sequence );
        while( m.find() ) { //If you also want to find the second AAA in AAAA, then use m.find(lastFrom);
            int from = m.start();
            int to = m.end() - 1;
            if( isForwardStrand ) {
                from = absStart - offset + from;
                to = absStart - offset + to;
            }
            else {
                int end = from;
                from = absStart - offset + sequence.length() - to - 1;
                to = absStart - offset + sequence.length() - end - 1;
            }
            matchedPatterns.add( new Region( from, to, isForwardStrand, Properties.PATTERN ) );
        }
    }


    /**
     * Identifies the next occurrence of pattern "p" in the given "sequence" and
     * returns its position.
     * <p>
     * @param sequence the sequence to analyse
     * @param p        pattern to search for
     * <p>
     * @return The position of the next occurrence of pattern "p" in the given
     *         "sequence". -1 if the pattern cannot be found.
     */
    private int matchNextOccurrence( String sequence, Pattern p ) {

        Matcher m = p.matcher( sequence );
        if( m.find() ) {
            return m.start();
        }
        return -1;
    }


    @Override
    public List<Region> findRegions() {
        matchedPatterns.clear();
        findPatternInInterval();
        return Collections.unmodifiableList( matchedPatterns );
    }


    @Override
    public void setInterval( int start, int stop ) {
        absStart = start;
        absStop = stop;
    }


    /**
     * @param pattern Pattern to search for
     */
    public final void setPattern( String pattern ) {
        this.pattern = Pattern.compile( pattern );
//        this.patternRev = Pattern.compile(SequenceUtils.getDnaComplement(SequenceUtils.reverseString(pattern)));
    }


}
