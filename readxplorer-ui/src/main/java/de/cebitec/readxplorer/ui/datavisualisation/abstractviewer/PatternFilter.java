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


import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.utils.sequence.Region;
import de.cebitec.readxplorer.utils.sequence.SequenceMatcher;
import de.cebitec.readxplorer.utils.sequence.SequenceScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Filters for a given pattern in two ways: First for all available occurrences
 * of the pattern in a given interval of a given DNA sequence and second for the
 * next occurrence of the pattern along a DNA sequence, such as a whole genome
 * sequence.
 *
 * @author ddoppmeier, rhilker
 */
public class PatternFilter implements RegionFilterI {

    public static final int INIT = 10;
    /**
     * Default size of a single scanning step.
     */
    public static final int INTERVAL_SIZE = 3000000;
    private List<Region> matchedPatterns;
    private int absStart;
    private int absStop;
    private final PersistentReference refGen;
    private Pattern pattern;
    private Pattern patternRev;
    private int analysisFrame;
    private byte regionType;
    private Strand analysisStrand;
    private int maxNoResults;
    private boolean analyzeInRevDirection;
    private boolean addOffset;
    private int intervalSize;
    private boolean requireSameFrame;


    /**
     * Filters for a given pattern in two ways: First for all available
     * occurrences of the pattern in a given interval of a given DNA sequence
     * and second for the next occurrence of the pattern along a DNA sequence,
     * such as a whole genome sequence.
     * <p>
     * @param absStart Absolute start of the sequence to analyze
     * @param absStop  Absolute stop of the sequence to analyze
     * @param refGen   Reference genome to analyze
     */
    public PatternFilter( int absStart, int absStop, PersistentReference refGen ) {
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;
        matchedPatterns = new ArrayList<>();
        maxNoResults = 0;
        analysisStrand = Strand.Both;
        regionType = Properties.PATTERN;
        analyzeInRevDirection = false;
        addOffset = true;
        requireSameFrame = false;
        analysisFrame = INIT; //because this is not a frame value
        intervalSize = INTERVAL_SIZE;
    }


    /**
     * @return Identifies the currently set pattern in the given interval of
     *         the currently set reference according to the current filter
     *         configuration.
     */
    @Override
    public List<Region> findRegions() {
        matchedPatterns.clear();
        findPatternInInterval();
        return Collections.unmodifiableList( matchedPatterns );
    }


    /**
     * Identifies the currently set pattern in a given interval of the reference
     * sequence stored in this object according to the current filter
     * configuration. <br/>
     * In the standard case, only the forward strand sequence is analyzed and
     * the patterns are reverse complemented for identifying matches to the
     * reverse strand. When {@link #setAnalyzeInRevDirection()} is set to
     * <code>true</code>, the sequence is reversed for the reverse strand
     * analysis and thus the patterns are complemented to analyze matches to the
     * reverse strand.
     * <p>
     * @return A list of the positions where the pattern matched along the
     *         interval.
     */
    private List<Region> findPatternInInterval() {

        if( this.pattern != null && !this.pattern.toString().isEmpty() ) {

            int start = absStart;
            int stop = absStop;
            if( addOffset ) { //shift by pattern length to both sides
                int offset = this.pattern.toString().length();
                start -= offset;
                stop += offset - 1;
            }

            if( start <= 0 ) {
                start = 1;
            }
            if( stop > this.refGen.getActiveChromLength() ) {
                stop = this.refGen.getActiveChromLength();
            }

            //configure matcher
            SequenceMatcher seqMatcher = new SequenceMatcher();
            seqMatcher.setMaxNoResults( maxNoResults );
            seqMatcher.setAnalyzeInRevDirection( analyzeInRevDirection );
            if( requireSameFrame ) {
                seqMatcher.setRequireSameFrame( requireSameFrame );
                seqMatcher.setTargetFrame( analysisFrame );
            }
            //configure scanner and run analysis
            PatternScanner patternScanner = new PatternScanner( start, stop, intervalSize, seqMatcher );
            patternScanner.setAnalysisStrand( analysisStrand );
            patternScanner.setAnalyzeInRevDirection( analyzeInRevDirection );
            patternScanner.scanSequence();
            matchedPatterns = seqMatcher.getRegions();
        }

        return matchedPatterns;
    }


    /**
     * Identifies next (closest) occurrence from either forward or reverse
     * strand of a pattern in the current reference genome in the interval
     * following up to the current interval.
     * <p>
     * @return the position of the next occurrence of the pattern in the
     *         interval following up to the current interval.
     */
    public int findNextOccurrence() {

        int refLength = this.refGen.getActiveChromLength();
        int from = -1;
        if( !(this.pattern == null) && !this.pattern.toString().isEmpty() && absStop > 0 ) {

            int start = this.absStop;

            if( this.absStart <= 0 ) {
                this.absStart = 1;
            }
            if( start > refLength ) {
                start = 1;
            }

            //at first search from current position till end of sequence on both frames
            SequenceMatcher seqMatcher = new SequenceMatcher();
            seqMatcher.setMaxNoResults( 1 );
            seqMatcher.setAnalyzeInRevDirection( analyzeInRevDirection );

            PatternPosScanner patternScanner = new PatternPosScanner( start, refLength, intervalSize, seqMatcher );
            patternScanner.scanSequence();
            from = patternScanner.getPatternStart();

            //if nothing found search from 1 to current position on both frames
            if( from == -1 ) {
                PatternPosScanner patternScanner2 = new PatternPosScanner( 1, start, intervalSize, seqMatcher );
                patternScanner2.scanSequence();
                from = patternScanner2.getPatternStart();
            }
        }

        return from;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setInterval( int start, int stop ) {
        absStart = start;
        absStop = stop;
    }


    /**
     * @param intervalSize The size of each scanning step. Enables splitting the
     *                     whole region to analyze in smaller parts. Especially
     *                     useful when results are expected to be located
     *                     closeby. The default value is {@link #INTERVAL_SIZE}.
     */
    public void setIntervalSize( int intervalSize ) {
        this.intervalSize = intervalSize;
    }


    /**
     * @param pattern Pattern string to search for. The pattern is revised for
     *                analyzing the reverse strand automatically. In the
     *                standard case, only the forward strand sequence is
     *                analyzed and the patterns are reverse complemented for
     *                identifying matches to the reverse strand. When
     *                {@link #setAnalyzeInRevDirection()} is set to
     *                <code>true</code>, the sequence is reversed for the
     *                reverse strand analysis and thus the patterns are
     *                complemented to analyze matches to the reverse strand.
     */
    public final void setPattern( String pattern ) {
        this.pattern = Pattern.compile( pattern );
        setRevPattern( pattern );
    }


    /**
     * Revises the pattern for the reverse strand analysis according to the
     * current filter configuration.
     * <p>
     * @param pattern The pattern to revise
     */
    private void setRevPattern( String pattern ) {
        if( analyzeInRevDirection ) {
            patternRev = Pattern.compile( SequenceUtils.getDnaComplement( pattern ) );
        } else {
            patternRev = Pattern.compile( SequenceUtils.getReverseComplement( pattern ) );
        }
    }


    /**
     * @return The maximum number of pattern matches to calculate. The default
     *         value is 0 = no restriction. <br/>
     * E.g. 1 means that only the next hit is searched for the current pattern.
     */
    public int getMaxNoResults() {
        return maxNoResults;
    }


    /**
     * @param maxNoResults The maximum number of pattern matches to calculate.
     *                     The default value is 0 = no restriction. <br/>
     * E.g. 1 means that only the next hit is searched for the current pattern.
     */
    public void setMaxNoResults( int maxNoResults ) {
        this.maxNoResults = maxNoResults;
    }


    /**
     * @return The reading frame to analyze for the current pattern
     */
    public int getAnalysisFrame() {
        return this.analysisFrame;
    }


    /**
     * Sets the reading frame needed for the current analysis. This always has
     * to be set first in case the analysis should only return start codons of
     * the correct frame. <br/>
     * This method also sets the appropriate analysis strand (see
     * {@link #setAnalysisStrand(byte)}) and if the same frame is required (see
     * {@link #setRequireSameFrame(boolean)}).
     * <p>
     * @param analysisFrame the frame to analyze for the pattern
     */
    public void setAnalysisFrame( int analysisFrame ) {
        this.analysisFrame = analysisFrame;
        requireSameFrame = analysisFrame != 0 && analysisFrame != INIT;

        if( analysisFrame < 0 ) {
            analysisStrand = Strand.Reverse;
        } else if( !requireSameFrame || analysisFrame == 0 ) {
            analysisStrand = Strand.Both;
        } else {
            analysisStrand = Strand.Forward;
        }
    }


    /**
     * @return A value among {@link SequenceUtils#STRAND_FWD},
     * {@link SequenceUtils#STRAND_REV} and 0 to indicate using both strands. 0
     *         is also the default value, thus does not have to be set explictly.
     */
    public Strand getAnalysisStrand() {
        return analysisStrand;
    }


    /**
     * Determine if only one strand should be used for the analysis or both.
     * <p>
     * @param strand A value among {@link SequenceUtils#STRAND_FWD},
     * {@link SequenceUtils#STRAND_REV} and 0 to indicate using both strands. 0
     *               is also the default value, thus does not have to be set
     *               explictly.
     */
    public void setAnalysisStrand( Strand strand ) {
        this.analysisStrand = strand;
    }


    /**
     * @param analyzeInRevDirection <code>true</code> if the sequence shall be
     *                              analyzed in reverse direction (e.g. for the
     *                              corresponding stop codon of a start codon on
     *                              the reverse strand), <code>false</code> if
     *                              the analysis direction is forward. The
     *                              default value is <code>false</code>.
     */
    public void setAnalyzeInRevDirection( boolean analyzeInRevDirection ) {
        this.analyzeInRevDirection = analyzeInRevDirection;
        if( pattern != null ) {
            setRevPattern( pattern.toString() );
        }
    }


    /**
     * @return The type to use for the identified regions
     */
    public byte getRegionType() {
        return regionType;
    }


    /**
     * @param regionType The type to use for the identified regions
     */
    public void setRegionType( byte regionType ) {
        this.regionType = regionType;
    }


    /**
     * @return <code>true</code> if an offset of the pattern length should be
     *         added to the start and stop positions of the search interval. This is
     *         useful when hits partly overlapping the actual interval borders have to
     *         be identified as well. <code>false</code> if the interval borders are
     *         used as is.
     */
    public boolean isAddOffset() {
        return addOffset;
    }


    /**
     * @param addOffset <code>true</code> if an offset of the pattern length
     *                  should be added to the start and stop positions of the
     *                  search interval. This is useful when hits partly
     *                  overlapping the actual interval borders have to be
     *                  identified as well. <code>false</code> if the interval
     *                  borders are used as is.
     */
    public void setAddOffset( boolean addOffset ) {
        this.addOffset = addOffset;
    }


    /**
     * @return <code>true</code> if results should only be calculated for the
     *         currently set reading frame, <code>false</code> if both strands
     *         should be analyzed. The default value is <code>false</code>.
     */
    public boolean isRequireSameFrame() {
        return requireSameFrame;
    }


    /**
     * @param requireSameFrame <code>true</code> if results should only be
     *                         calculated for the currently set reading frame,
     *                         <code>false</code> if both strands should be
     *                         analyzed. The default value is
     *                         <code>false</code>.
     */
    public void setRequireSameFrame( boolean requireSameFrame ) {
        this.requireSameFrame = requireSameFrame;
    }


    /**
     * @return The reference used in this filter.
     */
    PersistentReference getReference() {
        return refGen;
    }


    /**
     * A scanner for patterns in a sequence.
     */
    private class PatternScanner extends SequenceScanner {

        private final SequenceMatcher seqMatcher;
        private Strand analysisStrand;


        /**
         * A scanner for patterns in a sequence. By default both strands are
         * analyzed for the pattern.
         * <p>
         * @param totalStart   The absolute start of the whole interval to scan.
         *                     Always smaller than {@link getTotalStop()}.
         * @param totalStop    The absolute stop of the whole interval to scan.
         *                     Always larger than {@link getTotalStart()}.
         * @param intervalSize The size of each scanning step. Enables splitting
         *                     the whole region to analyze in smaller parts.
         *                     Especially useful when results are expected to be
         *                     located closeby.
         * @param seqMatcher   The sequence matcher to perfom the actual pattern
         *                     matching
         */
        public PatternScanner( int totalStart, int totalStop, int intervalSize, SequenceMatcher seqMatcher ) {
            super( totalStart, totalStop, intervalSize );
            this.seqMatcher = seqMatcher;
            analysisStrand = Strand.Both;
        }


        /**
         * Configures and runs the sequence matcher and calculates all extra
         * data for the currently set sequence interval.
         * <p>
         * @param currentStart The start of the currently analyzed interval
         * @param currentStop  The stop of the currently analyzed interval
         */
        @Override
        public void executeNextScan( int currentStart, int currentStop ) {
            String seq = refGen.getActiveChromSequence( currentStart, currentStop );

            if( isAnalyzeInRevDirection() ) {
                seq = SequenceUtils.reverseString( seq );
            }

            seqMatcher.clearRegions();
            seqMatcher.setAbsoluteStart( currentStart );

            if( analysisStrand != Strand.Reverse ) { //run analysis only on selected strand(s)
                seqMatcher.matchPattern( seq, pattern, true, regionType );
            }
            if( analysisStrand != Strand.Forward ) {
                seqMatcher.matchPattern( seq, patternRev, false, regionType );
            }

            performExtraCalculations();
        }


        /**
         * Determine if only one strand should be used for the analysis or both.
         * <p>
         * @param analysisStrand A value among {@link SequenceUtils#STRAND_FWD},
         * {@link SequenceUtils#STRAND_REV} and 0 to indicate using both
         *                       strands. 0 is also the default value, thus does
         *                       not have to be set explictly.
         */
        public void setAnalysisStrand( Strand analysisStrand ) {
            this.analysisStrand = analysisStrand;
        }


        /**
         * Allows performing extra calculations. This class checks whether the
         * {@link #getMaxNoResults()} value has been set and exceeded. If so,
         * the scanning process can be stopped and the result is ready. Also
         * designed for subclasses to overwrite.
         */
        void performExtraCalculations() {
            if( maxNoResults > 0 && seqMatcher.getRegions().size() >= maxNoResults ) {
                setDone( true );
            }
        }


    }


    /**
     * A scanner for patter start positions in a DNA sequence.
     */
    private class PatternPosScanner extends PatternScanner {

        private int patternStart;
        private final SequenceMatcher seqMatcher;


        /**
         * A scanner for patter start positions in a DNA sequence.
         * <p>
         * @param totalStart   The absolute start of the whole interval to scan.
         *                     Always smaller than {@link getTotalStop()}.
         * @param totalStop    The absolute stop of the whole interval to scan.
         *                     Always larger than {@link getTotalStart()}.
         * @param intervalSize The size of each scanning step. Enables splitting
         *                     the whole region to analyze in smaller parts.
         *                     Especially useful when results are expected to be
         *                     located closeby.
         * @param seqMatcher   The sequence matcher to perfom the actual pattern
         *                     matching
         */
        public PatternPosScanner( int totalStart, int totalStop, int intervalSize, SequenceMatcher seqMatcher ) {
            super( totalStart, totalStop, intervalSize, seqMatcher );
            this.seqMatcher = seqMatcher;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void executeNextScan( int currentStart, int currentStop ) {
            super.executeNextScan( currentStart, currentStop );
        }


        /**
         * Performing extra calculations. This class checks if already a
         * pattern hit has been identified. If so, the scanning process can be
         * stopped and the result is ready.
         */
        @Override
        void performExtraCalculations() {
            patternStart = findCorrectStart( seqMatcher );
            if( patternStart > 0 ) {
                setDone( true );
            }
        }


        /**
         * Calculate and return the smallest start position of the pattern
         * handed to the <code>seqMatcher</code> or -1 if there is no result for
         * the current analyzed interval.
         * <p>
         * @param seqMatcher The
         *                   {@link de.cebitec.readxplorer.utils.sequence.SequenceMatcher}
         *                   containing the analysis results
         * <p>
         * @return The smallest start position of the pattern handed to the
         *         <code>seqMatcher</code> or -1 if there is no result for the
         *         current analyzed interval.
         */
        private int findCorrectStart( SequenceMatcher seqMatcher ) {
            int from = -1;
            List<Region> regions = seqMatcher.getRegions();
            if( regions.size() == 1 ) {
                from = regions.get( 0 ).getStart();
            } else if( regions.size() == 2 ) {
                int start1 = regions.get( 0 ).getStart();
                int start2 = regions.get( 1 ).getStart();
                from = start1 < start2 ? start1 : start2;
            }
            return from;
        }


        /**
         * @return The start of the identified pattern hit. If no hit has been
         *         identified it returns -1.
         */
        public int getPatternStart() {
            return patternStart;
        }


    }

}
