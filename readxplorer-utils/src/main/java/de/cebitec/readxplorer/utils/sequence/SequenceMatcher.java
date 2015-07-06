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

package de.cebitec.readxplorer.utils.sequence;


import de.cebitec.readxplorer.api.enums.RegionType;
import de.cebitec.readxplorer.utils.PositionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Enables matching a pattern on a sequence, especially for DNA sequences.
 * Provides several configuration options for the matching process.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SequenceMatcher {

    private List<Region> regions;

    private int absoluteStart;
    private int maxNoResults;
    private int targetFrame;
    private boolean requireSameFrame;
    private boolean analyzeInRevDirection;


    /**
     * Enables matching a pattern on a sequence, especially for DNA sequences.
     * Provides several configuration options for the matching process. <br/>
     * Initializes the default start position of the matcher to 1.
     */
    public SequenceMatcher() {
        this( 1 );
    }


    /**
     * Enables matching a pattern on a sequence, especially for DNA sequences.
     * Provides several configuration options for the matching process.
     *
     * @param absoluteStart The absolute search start position in genome
     * coordinates of the matcher
     */
    public SequenceMatcher( int absoluteStart ) {
        this.absoluteStart = absoluteStart;
        regions = new ArrayList<>();
        targetFrame = 0;
        maxNoResults = 0;
        requireSameFrame = false;
    }


    /**
     * Identifies <code>pattern</code> in the given <code>sequence</code> and
     * stores positive results in this class' region list. Note that only the
     * sequence of the fwd strand is scanned. Thus, the input pattern has to be
     * complemented to identify matches on the reverse strand. This, behavior
     * enables skipping of reverse complementing long search sequences. Beware
     * to set all necessary options before running the analysis.
     * <p>
     * @param sequence the sequence to analyze
     * @param pattern pattern to search for (complement for rev strand)
     * @param isForwardStrand <code>true</code> if <code>pattern</code>
     * originates from the fwd strand, <code>false</code> if it originates from
     * the reverse strand
     * @param type The type of the regions to create. Either Region.START or
     * Region.STOP, Properties.PATTERN or Properties.CDS.
     */
    public void matchPattern( String sequence, Pattern pattern, boolean isForwardStrand, RegionType type ) {

        //the if-clause is needed because otherwise data for the fwd strand can be passed and analyzed for frame -2 = nonsense
        if( !requireSameFrame || isForwardStrand && targetFrame > 0 || !isForwardStrand && targetFrame < 0 ) {

            int counter = 0;
            boolean isMaxNoResultsSet = maxNoResults > 0;

            Matcher m = pattern.matcher( sequence );
            while( m.find() ) {
                int from = m.start();
                int to = m.end() - 1;

                //calculate absolute positions according to analysis direction
                final int start;
                final int stop;
                if( analyzeInRevDirection ) { // -1 because reversing the sequence led to shift by 1 in matcher
                    start = absoluteStart + sequence.length() - to - 1;
                    stop = absoluteStart + sequence.length() - from - 1;
                } else {
                    start = absoluteStart + from;
                    stop = absoluteStart + to;
                }

                if( !requireSameFrame
                        || PositionUtils.determineFwdFrame( start ) == targetFrame
                        || PositionUtils.determineRevFrame( stop ) == targetFrame ) {
                    regions.add( new Region( start, stop, isForwardStrand, type ) );
                    counter++;
                }

                if( isMaxNoResultsSet && counter >= maxNoResults ) {
                    break;
                }
            }
        }

        /*
         * Frame calculations works because e.g. for positions 1-3 & 6-4:
         * +1 = (pos 1 - 1) % 3 = 0 -> 0 + 1 = frame +1
         * +2 = (pos 2 - 1) % 3 = 1 -> 1 + 1 = frame +2
         * +3 = (pos 3 - 1) % 3 = 2 -> 2 + 1 = frame +3
         * -1 = (pos 6 - 1) % 3 = 2 -> 2 - 3 = frame -1
         * -2 = (pos 5 - 1) % 3 = 1 -> 1 - 3 = frame -2
         * -3 = (pos 4 - 1) % 3 = 0 -> 0 - 3 = frame -3
         */
    }


    /**
     * @return Absolute search start position in the genome.
     */
    public int getAbsoluteStart() {
        return absoluteStart;
    }


    /**
     * @param absoluteStart Absolute search start position in the genome.
     */
    public void setAbsoluteStart( int absoluteStart ) {
        this.absoluteStart = absoluteStart;
    }


    /**
     * @return The maximum number of pattern matches to calculate. The default
     * value is 0 = no restriction. <br/>
     * E.g. 1 means that only the next hit is searched for the current pattern.
     */
    public int getMaxNoResults() {
        return maxNoResults;
    }


    /**
     * @param maxNoResults The maximum number of pattern matches to calculate.
     * The default value is 0 = no restriction. <br/>
     * E.g. 1 means that only the next hit is searched for the current pattern.
     */
    public void setMaxNoResults( int maxNoResults ) {
        this.maxNoResults = maxNoResults;
    }


    /**
     * @return The reading frame to analyze (-3 to 3 except 0). The default
     * value is 0 = analyze all frames.
     */
    public int getTargetFrame() {
        return targetFrame;
    }


    /**
     * @param targetFrame The reading frame to analyze (-3 to 3 except 0). The
     * default value is 0 = analyze all frames.
     */
    public void setTargetFrame( int targetFrame ) {
        this.targetFrame = targetFrame;
    }


    /**
     * @return <code>true</code> if results should only be calculated for the
     * currently set reading frame, <code>false</code> if both strands should be
     * analyzed. The default value is <code>false</code>.
     */
    public boolean isRequireSameFrame() {
        return requireSameFrame;
    }


    /**
     * @param requireSameFrame <code>true</code> if results should only be
     * calculated for the currently set reading frame, <code>false</code> if
     * both strands should be analyzed. The default value is <code>false</code>.
     */
    public void setRequireSameFrame( boolean requireSameFrame ) {
        this.requireSameFrame = requireSameFrame;
    }


    /**
     * @return <code>true</code> if the sequence shall be analyzed in reverse
     * direction (e.g. for the corresponding stop codon of a start codon on the
     * reverse strand), <code>false</code> if the analysis direction is forward.
     * The default value is <code>false</code>.
     */
    public boolean isAnalyzeInRevDirection() {
        return analyzeInRevDirection;
    }


    /**
     * @param analyzeInRevDirection <code>true</code> if the sequence shall be
     * analyzed in reverse direction (e.g. for the corresponding stop codon of a
     * start codon on the reverse strand), <code>false</code> if the analysis
     * direction is forward. The default value is <code>false</code>.
     */
    public void setAnalyzeInRevDirection( boolean analyzeInRevDirection ) {
        this.analyzeInRevDirection = analyzeInRevDirection;
    }


    /**
     * @return The list of hits calcualted since the matcher creation or the
     * last {@link #clearRegions()} call. Thus, regions from multiple
     * {@link #matchPattern()} calls accumulate in this list until cleared.
     */
    public List<Region> getRegions() {
        return regions;
    }


    /**
     * Clears the list of result regions.
     */
    public void clearRegions() {
        regions.clear();
    }


}
