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


/**
 * Provides methods to scan a given region in several steps. The code to execute
 * has to be added by the implementations extending this class.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public abstract class SequenceScanner {

    private boolean done;
    private final int intervalSize;
    private final int totalStart;
    private final int totalStop;
    private boolean analyzeInRevDirection;


    /**
     * Provides methods to scan a given region in several steps according to the
     * given <code>intervalSize</code>.
     * <p>
     * @param totalStart The absolute start of the whole interval to scan.
     * Always smaller than {@link getTotalStop()}.
     * @param totalStop The absolute stop of the whole interval to scan. Always
     * larger than {@link getTotalStart()}.
     * @param intervalSize The size of each scanning step. Enables splitting the
     * whole region to analyze in smaller parts. Especially useful when results
     * are expected to be located closeby.
     */
    public SequenceScanner( int totalStart, int totalStop, int intervalSize ) {
        this.totalStart = totalStart;
        this.totalStop = totalStop;
        this.intervalSize = intervalSize;
        done = false;
    }


    /**
     * @return The size of each scanning step. Enables splitting the whole
     * region to analyze in smaller parts. Especially useful when results are
     * expected to be located closeby.
     */
    public int getIntervalSize() {
        return intervalSize;
    }


    /**
     * @return The absolute start of the whole interval to scan. Always smaller
     * than {@link getTotalStop()}.
     */
    public int getTotalStart() {
        return totalStart;
    }


    /**
     * @return The absolute stop of the whole interval to scan. Always larger
     * than {@link getTotalStart()}.
     */
    public int getTotalStop() {
        return totalStop;
    }


    /**
     * @return <code>true</code> if the scanning shall be performed in reverse
     * direction (e.g. for the corresponding stop codon of a start codon on the
     * reverse strand), <code>false</code> if the scanning direction is forward.
     * The default value is <code>false</code>.
     */
    public boolean isAnalyzeInRevDirection() {
        return analyzeInRevDirection;
    }


    /**
     * @param analyzeInRevDirection <code>true</code> if the scanning shall be
     * performed in reverse direction (e.g. for the corresponding stop codon of
     * a start codon on the reverse strand), <code>false</code> if the scanning
     * direction is forward. The default value is <code>false</code>.
     */
    public void setAnalyzeInRevDirection( boolean analyzeInRevDirection ) {
        this.analyzeInRevDirection = analyzeInRevDirection;
    }


    /**
     * @return <code>true</code> when the scanning process is done,
     * <code>false</code> otherwise.
     */
    public boolean isDone() {
        return done;
    }


    /**
     * @param done <code>true</code> when the scanning process is done,
     * <code>false</code> otherwise.
     */
    public void setDone( boolean done ) {
        this.done = done;
    }


    /**
     * Starts the scanning process of the defined interval using the current
     * scanner configuration.
     */
    public void scanSequence() {
        if( analyzeInRevDirection ) {
            for( int i = totalStop; i > totalStart; i -= intervalSize ) {
                if( !done ) {
                    int start = calcStart( i );
                    executeNextScan( start, i );
                } else {
                    break;
                }
            }

        } else {
            for( int i = totalStart; i < totalStop; i += intervalSize ) {
                if( !done ) {
                    int stop = calcStop( i );
                    executeNextScan( i, stop );
                } else {
                    break;
                }
            }
        }
    }


    /**
     * When running the scanning process in reverse direction, the start
     * position is reduced by the analysis interval size. Additionally, the
     * method assures that the start position is never smaller than the
     * <code>totalStart</code> position.
     * <p>
     * @param stopPos The stop position of the current interval
     * <p>
     * @return The revised start position
     */
    private int calcStart( int stopPos ) {
        int start = stopPos - intervalSize;
        if( start < totalStart ) {
            start = totalStart;
        }
        return start;
    }


    /**
     * When running the scanning process in forward direction, the start
     * position is increased by the analysis interval size. Additionally, the
     * method assures that the stop position is never larger than the
     * <code>totalStop</code> position.
     * <p>
     * @param startPos The start position of the current interval
     * <p>
     * @return The revised stop position
     */
    private int calcStop( int startPos ) {
        int stop = startPos + intervalSize;
        if( stop > totalStop ) {
            stop = totalStop;
        }
        return stop;
    }


    /**
     * Executes whatever code is required when a certain interval is scanned.
     * <p>
     * @param currentStart The start of the current interval. Always smaller
     * than <code>currentStop</code>
     * @param currentStop The stop of the current interval. Always larger than
     * <code>currentStart</code>
     */
    public abstract void executeNextScan( int currentStart, int currentStop );


}
