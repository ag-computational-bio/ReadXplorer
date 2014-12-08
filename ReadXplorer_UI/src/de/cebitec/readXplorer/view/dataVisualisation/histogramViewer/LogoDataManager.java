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

package de.cebitec.readXplorer.view.dataVisualisation.histogramViewer;


import de.cebitec.readXplorer.databackend.dataObjects.Difference;
import de.cebitec.readXplorer.databackend.dataObjects.ReferenceGap;
import de.cebitec.readXplorer.view.dataVisualisation.GenomeGapManager;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Manager for all diffs in a specific interval defined by absStart and width.
 * After setting the coverage for each position and adding all diffs and gaps,
 * it can return the count of each DNA base or gap for each position in the
 * interval.
 *
 * @author ddoppmeier, rhilker
 */
public class LogoDataManager {

    private static final int NO_BASE_TYPES = 7;
    private static final int NO_BASE_FIELDS = NO_BASE_TYPES * 2;
    private static final int MATCH = 0;
    private static final int A = 1;
    private static final int C = 2;
    private static final int G = 3;
    private static final int T = 4;
    private static final int N = 5;
    private static final int READGAP = 6;

    private int[][] counts; //array for the positions in the interval and all 7 base types (fwd and rev = 14)
    private int absStart;
    private int stop;
    private int width;
    private int upperCutoff;
    private int maxFoundCoverage;


    /**
     * Manager for all diffs in a specific interval defined by absStart and
     * width. After setting the coverage for each position and adding all diffs
     * and gaps, it can return the count of each DNA base or gap for each
     * position in the interval.
     * <p>
     * @param absStart start position of the currently viewed interval
     * @param width    length of the currently viewed interval
     */
    public LogoDataManager( int absStart, int width ) {
        this.absStart = absStart;
        this.stop = absStart + width - 1;
        this.width = width;

        if( this.width < 1 ) {
            this.width = 1;
            this.stop = absStart;
        }
        this.upperCutoff = this.stop;
        this.maxFoundCoverage = 0;

        counts = new int[this.width][NO_BASE_FIELDS];
    }


    /**
     * Retrieves the count for the given base (row) at the given position for
     * the given strand for the interval stored in this logo data manager.
     * <p>
     * @param position      the position whose base count is needed
     * @param forwardStrand true, if the base is on the fwd strand, false
     *                      otherwise
     * @param row           the row value for the needed base at the given
     *                      position
     * <p>
     * @return the coverage value for the given row (base) at the given position
     *         and strand
     */
    private int getCountForPos( int position, boolean forwardStrand, int row ) {
        if( !forwardStrand ) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }


    public int getNumOfMatchesAt( int position, boolean forwardStrand ) {
        return getCountForPos( position, forwardStrand, MATCH );
    }


    public int getNumOfAAt( int position, boolean forwardStrand ) {
        return getCountForPos( position, forwardStrand, A );
    }


    public int getNumOfCAt( int position, boolean forwardStrand ) {
        return getCountForPos( position, forwardStrand, C );
    }


    public int getNumOfGAt( int position, boolean forwardStrand ) {
        return getCountForPos( position, forwardStrand, G );
    }


    public int getNumOfTAt( int position, boolean forwardStrand ) {
        return getCountForPos( position, forwardStrand, T );
    }


    public int getNumOfNAt( int position, boolean forwardStrand ) {
        return getCountForPos( position, forwardStrand, N );
    }


    public int getNumOfReadGapsAt( int position, boolean forwardStrand ) {
        return getCountForPos( position, forwardStrand, READGAP );
    }


    /**
     * Sets the coverage value for a matching base to the reference genome.
     * <p>
     * @param position      position whose coverage will be stored now
     * @param value         match coverage value to store
     * @param forwardStrand true, if this is a coverage value for the fwd strand
     */
    public void setCoverageAt( int position, int value, boolean forwardStrand ) {
        int row = MATCH;
        if( !forwardStrand ) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        maxFoundCoverage = (value > maxFoundCoverage ? value : maxFoundCoverage);
        counts[position][row] = value;
    }


    /**
     * If a base differs from the reference genome, add it to this manager.
     * <p>
     * @param diff     diff to add
     * @param position relative position at which the diff occurs, taking into
     *                 account previous gaps and insertions
     */
    public void addExtendedPersistentDiff( Difference diff, int position ) {
        char base = diff.getBase();
        int row = 0;
        switch( base ) {
            case 'A':
                row = A;
                break;
            case 'C':
                row = C;
                break;
            case 'G':
                row = G;
                break;
            case 'T':
                row = T;
                break;
            case 'N':
                row = N;
                break;
            case '-':
                row = READGAP;
                break;
            default:
                Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "found unknown base {0}", base );
        }

        int column = MATCH;
        if( !diff.isForwardStrand() ) {
            row += NO_BASE_TYPES;
            column += NO_BASE_TYPES;
        }

        int count = diff.getCount();
        position -= absStart;

        // increase counts for current base
        counts[position][row] += count;

        // decrease match coverage
        counts[position][column] -= count;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "width: " ).append( width ).append( ", height: " ).append( NO_BASE_FIELDS ).append( "\n" );

        for( int i = 0; i < width; i++ ) {
            int pos = absStart + i;
            sb.append( "pos: " ).append( pos ).append( "\t" );
            for( int j = 0; j < NO_BASE_FIELDS; j++ ) {
                sb.append( counts[i][j] ).append( "\t" );
            }
            sb.append( "\n" );
        }

        return sb.toString();
    }


    /**
     * Add all gaps for the currently viewed interval.
     * <p>
     * @param gaps       the collection of gaps to add
     * @param gapManager the genome gap manager for the given gaps
     */
    public void addGaps( Collection<ReferenceGap> gaps, GenomeGapManager gapManager ) {
        for( ReferenceGap gap : gaps ) {
            int origPos = gap.getPosition();

            if( origPos > this.absStart && origPos < this.stop ) {

                char base = gap.getBase();
                int count = gap.getCount();
                int shiftedPos = origPos + gapManager.getNumOfGapsSmaller( origPos );
                shiftedPos += gap.getOrder();

                // gaps have been loaded with original bounds
                // so, some of them may be outside of the interval, this LogoDataManager
                // manages. Skip those gaps
                if( shiftedPos > upperCutoff ) {
                    continue;
                }
                shiftedPos -= absStart;

                int row = 0;
                switch( base ) {
                    case 'A':
                        row = A;
                        break;
                    case 'C':
                        row = C;
                        break;
                    case 'G':
                        row = G;
                        break;
                    case 'T':
                        row = T;
                        break;
                    case 'N':
                        row = N;
                        break;
                    default:
                        Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "found unknown base {0}", base );
                }

                if( !gap.isForwardStrand() ) {
                    row += NO_BASE_TYPES;
                }

                counts[shiftedPos][row] += count;

            }
            else if( origPos > this.stop ) {
                break;
            }
        }
    }


    /**
     * @return The highest coverage observed in the interval stored in this
     *         logo data manager.
     */
    public int getMaxFoundCoverage() {
        return maxFoundCoverage;
    }


}
