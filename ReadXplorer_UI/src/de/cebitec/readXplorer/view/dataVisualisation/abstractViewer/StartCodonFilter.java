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

package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.util.CodonUtilities;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.PositionUtils;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Filters for start and stop codons in two ways: First for all
 * available start and stop codons in a specified region and second
 * for all start and stop codons of a given frame for a specified region.
 *
 * @author ddoppmeier, rhilker
 */
public class StartCodonFilter implements RegionFilterI {

    public static final int INIT = 10;
    private static final int INTERVAL_SIZE = 3000000;

    private final List<Region> regions;
    private int absStart;
    private int absStop;
    private final PersistentReference refGen;
    private String sequence;
    private ArrayList<Boolean> selectedStarts;
    private ArrayList<Boolean> selectedStops;
    private Pattern[] startCodons;
    private Pattern[] stopCodons;
    private int frameCurrFeature;


    /**
     * Filters for start and stop codons in two ways: First for all available
     * start
     * codons in a specified region and second for all start codons of a given
     * frame
     * for a specified region.
     * <p>
     * @param absStart start of the region to search in
     * @param absStop  end of the region to search in
     * @param refGen   the reference in which to search
     */
    public StartCodonFilter( int absStart, int absStop, PersistentReference refGen ) {
        this.regions = new ArrayList<>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;

        this.resetCodons();

        this.frameCurrFeature = StartCodonFilter.INIT; //because this is not a frame value
    }


    /**
     * Searches and identifies start and stop codons and saves their position
     * in this class' region list.
     */
    private void findSelectedCodons() {
        regions.clear();

        if( this.atLeastOneCodonSelected() ) {
            // extends interval to search to the left and right,
            // to find start/stop codons that overlap current interval boundaries
            int offset = 3;
            int start = absStart - offset;
            int stop = absStop + 2;
            int genomeLength = this.refGen.getActiveChromosome().getLength();

            if( stop > 0 ) {
                if( start <= 0 ) {
                    offset -= Math.abs( start ) + 1;
                    start = 1;
                }
                if( stop > genomeLength ) {
                    stop = genomeLength;
                }
                for( int i = start; i <= stop; i++ ) {
                    if( i + INTERVAL_SIZE <= stop ) {
                        this.sequence = refGen.getActiveChromSequence( i, i + INTERVAL_SIZE );
                    }
                    else {
                        this.sequence = refGen.getActiveChromSequence( i, stop );
                    }
                    i += INTERVAL_SIZE;
                    boolean isFeatureSelected = this.frameCurrFeature != INIT;

                    int index = 0;
                    for( int j = 0; j < this.selectedStarts.size(); ++j ) {
                        if( this.selectedStarts.get( j ) ) {
                            this.matchPattern( sequence, this.startCodons[index++], true, offset, isFeatureSelected, Properties.START );
                            this.matchPattern( sequence, this.startCodons[index++], false, offset, isFeatureSelected, Properties.START );
                        }
                        else {
                            index += 2;
                        }
                    }
                    index = 0;
                    for( int j = 0; j < this.selectedStops.size(); ++j ) {
                        if( this.selectedStops.get( j ) ) {
                            this.matchPattern( sequence, this.stopCodons[index++], true, offset, isFeatureSelected, Properties.STOP );
                            this.matchPattern( sequence, this.stopCodons[index++], false, offset, isFeatureSelected, Properties.STOP );
                        }
                        else {
                            index += 2;
                        }
                    }
                }
            }

        }

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
     * @param restricted      determining if the visualization should be
     *                        restricted to a certain frame
     * @param genomeLength    length of the genome, needed for checking frame on
     *                        the reverse strand
     * @param type            The type of the regions to create. Either
     *                        Region.START or Region.STOP.
     */
    private void matchPattern( String sequence, Pattern p, boolean isForwardStrand, int offset, boolean restricted,
                               int type ) {
        // match forward
        final boolean codonFwdStrand = this.frameCurrFeature > 0;
        if( !restricted || restricted && codonFwdStrand == isForwardStrand ) {
            Matcher m = p.matcher( sequence );
            while( m.find() ) {
                int from = m.start();
                int to = m.end() - 1;
                final int start = absStart - offset + from; // +1 because in matcher each pos is
                final int stop = absStart - offset + to; //shifted by -1 (index starts with 0)
                if( restricted ) {
                    /*
                     * Works because e.g. for positions 1-3 & 6-4:
                     * +1 = (pos 1 - 1) % 3 = 0 -> 0 + 1 = frame +1
                     * +2 = (pos 2 - 1) % 3 = 1 -> 1 + 1 = frame +2
                     * +3 = (pos 3 - 1) % 3 = 2 -> 2 + 1 = frame +3
                     * -1 = (pos 6 - 1) % 3 = 2 -> 2 - 3 = frame -1
                     * -2 = (pos 5 - 1) % 3 = 1 -> 1 - 3 = frame -2
                     * -3 = (pos 4 - 1) % 3 = 0 -> 0 - 3 = frame -3
                     */
                    if( PositionUtils.determineFwdFrame( start ) == this.frameCurrFeature
                        || PositionUtils.determineRevFrame( stop ) == this.frameCurrFeature ) {
                        regions.add( new Region( start, stop, isForwardStrand, type ) );
                    }
                }
                else {
                    regions.add( new Region( start, stop, isForwardStrand, type ) );
                }
            }
        }
    }


    @Override
    public List<Region> findRegions() {
        this.findSelectedCodons();
        return this.regions;
    }


    @Override
    public void setInterval( int start, int stop ) {
        this.absStart = start;
        this.absStop = stop;
    }


    /**
     * Sets if the start codon with the index i is currently selected.
     * <p>
     * @param i          the index of the current start codon
     * @param isSelected true, if the start codon is selected, false otherwise
     */
    public void setStartCodonSelected( final int i, final boolean isSelected ) {
        this.selectedStarts.set( i, isSelected );
    }


    /**
     * Sets if the stop codon with the index i is currently selected.
     * <p>
     * @param i          the index of the current stop codon
     * @param isSelected true, if the stop codon is selected, false otherwise
     */
    public void setStopCodonSelected( final int i, final boolean isSelected ) {
        this.selectedStops.set( i, isSelected );
    }


    /**
     * Returns if the start codon with index i is currently selected.
     * <p>
     * @param i index of the start codon
     * <p>
     * @return true if the start codon with index i is currently selected
     */
    public boolean isStartCodonSelected( final int i ) {
        return this.selectedStarts.get( i );
    }


    /**
     * Returns if the stop codon with index i is currently selected.
     * <p>
     * @param i index of the stop codon
     * <p>
     * @return true if the stop codon with index i is currently selected
     */
    public boolean isStopCodonSelected( final int i ) {
        return this.selectedStops.get( i );
    }


    public int getFrameCurrFeature() {
        return this.frameCurrFeature;
    }


    /**
     * Sets the data needed for the current feature. Currently only the frame is
     * necessary. This always has to be set first in case the action should only
     * show start codons of the correct frame.
     * <p>
     * @param frameCurrFeature the frame of the currently selected feature
     */
    public void setCurrFeatureData( int frameCurrFeature ) {
        this.frameCurrFeature = frameCurrFeature;
    }


    /**
     * Checks if at least one codon is currently selected.
     * <p>
     * @return true if at least one codon is currently selected
     */
    private boolean atLeastOneCodonSelected() {
        for( int i = 0; i < this.selectedStarts.size(); ++i ) {
            if( this.selectedStarts.get( i ) ) {
                return true;
            }
        }
        for( int i = 0; i < this.selectedStops.size(); ++i ) {
            if( this.selectedStops.get( i ) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Resets the set of start and stop codons according to the currently
     * selected genetic code.
     */
    public final void resetCodons() {
        Pair<String[], String[]> geneticCode = CodonUtilities.getGeneticCodeArrays();
        String[] startCodonsNew = geneticCode.getFirst();
        String[] stopCodonsNew = geneticCode.getSecond();

        this.startCodons = new Pattern[startCodonsNew.length * 2];
        this.stopCodons = new Pattern[stopCodonsNew.length * 2];
        this.selectedStarts = new ArrayList<>();
        this.selectedStops = new ArrayList<>();
        int index = 0;
        String codon;
        for( int i = 0; i < startCodonsNew.length; ++i ) {
            codon = startCodonsNew[i];
            this.startCodons[index++] = Pattern.compile( codon );
            this.startCodons[index++] = Pattern.compile( SequenceUtils.getDnaComplement( SequenceUtils.reverseString( codon ) ) );
            this.selectedStarts.add( false );
        }
        index = 0;
        for( int i = 0; i < stopCodonsNew.length; ++i ) {
            codon = stopCodonsNew[i];
            this.stopCodons[index++] = Pattern.compile( codon );
            this.stopCodons[index++] = Pattern.compile( SequenceUtils.getDnaComplement( SequenceUtils.reverseString( codon ) ) );
            this.selectedStops.add( false );
        }
    }


}
