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
import de.cebitec.readxplorer.utils.CodonUtilities;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.PositionUtils;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.utils.sequence.Region;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Filter for start and stop codons of a DNA sequence.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class StartCodonFilter extends PatternFilter {

    private List<Region> regions;
    private List<Boolean> selectedStarts;
    private List<Boolean> selectedStops;
    private String[] startCodons;
    private String[] stopCodons;


    /**
     * Filter for start and stop codons of a DNA sequence.
     * <p>
     * @param absStart The absolute start of the whole interval to scan. Always
     *                 smaller than <code>absStop</code>.
     * @param absStop  The absolute stop of the whole interval to scan. Always
     *                 larger than <code>absStart</code>.
     * @param refGen   the reference in which to search
     */
    public StartCodonFilter( int absStart, int absStop, PersistentReference refGen ) {
        super( absStart, absStop, refGen );
        this.regions = new ArrayList<>();

        this.resetCodons();
    }


    /**
     * @return Identifies the currently selected start and stop codons in the
     *         given interval of the currently set reference according to the
     *         current filter configuration.
     */
    @Override
    public List<Region> findRegions() {
        regions.clear();
        if( atLeastOneCodonSelected() ) {
            setRegionType( Properties.START );
            startSearch( selectedStarts, startCodons );
            setRegionType( Properties.STOP );
            startSearch( selectedStops, stopCodons );
        }
        return Collections.unmodifiableList( regions );
    }


    /**
     * Configures and runs a search for the next codon of the currently set
     * codon type (see {@link #setRegionType(byte)}) in the same reading frame
     * (and strand) than the given search start position.
     * <p>
     * @param start       The first position in the correct reading frame, on
     *                    which a codon should be detected.
     * <p>
     * @param isFwdStrand <code>true</code> if the codon should be searched on
     *                    the forward strand, <code>false</code> otherwise.
     * <p>
     * @return The next codon position in the reference from the given start
     */
    public Region findNextCodon( int start, boolean isFwdStrand ) {

        int startOnStrand = start;
        int stop = getReference().getActiveChromLength();
        if( !isFwdStrand ) {
            stop = start;
            startOnStrand = 1;
        }

        setInterval( startOnStrand, stop );
        setAnalysisStrand( isFwdStrand ? Strand.Forward : Strand.Reverse );
        if( isRequireSameFrame() ) {
            int frame = isFwdStrand ? PositionUtils.determineFwdFrame( start ) : PositionUtils.determineRevFrame( stop );
            setAnalysisFrame( frame );
        }
        setAnalyzeInRevDirection( !isFwdStrand );
        setAddOffset( false );

        List<Region> codons = new ArrayList<>( findRegions() );
        PositionUtils.sortList( isFwdStrand, codons );
        Region foundCodon = null;
        for( Region codon : codons ) {
            foundCodon = codon;
            break;
        }

        return foundCodon;
    }


    /**
     * Checks if at least one codon is currently selected.
     * <p>
     * @return <code>true</code> if at least one codon is currently selected,
     *         <code>false</code> otherwise.
     */
    private boolean atLeastOneCodonSelected() {
        return selectedStarts.contains( true ) || selectedStops.contains( true );
    }


    /**
     * Starts the pattern search for both strands and all selected codons.
     * <p>
     * @param selectedCodons The list of booleans determining which codons are
     *                       currently selected for the analysis
     * @param codons         The array of codons to search in the current
     *                       reference interval
     */
    private void startSearch( List<Boolean> selectedCodons, String[] codons ) {

        for( int i = 0; i < selectedCodons.size(); i++ ) {
            if( selectedCodons.get( i ) ) {
                setPattern( codons[i] );
                regions.addAll( super.findRegions() );
            }
        }
    }


    /**
     * Sets if all start codons are currently selected.
     * <p>
     * @param isSelected <code>true</code>, if the start codons are all
     *                   selected, <code>false</code> otherwise
     */
    public void setAllStartCodonsSelected( boolean isSelected ) {
        setListSelected( selectedStarts, isSelected );
    }


    /**
     * Sets if all stop codons are currently selected.
     * <p>
     * @param isSelected <code>true</code>, if the stop codons are all selected,
     *                   <code>false</code> otherwise
     */
    public void setAllStopCodonsSelected( boolean isSelected ) {
        setListSelected( selectedStops, isSelected );
    }


    /**
     * Selects or deselects all boolean values in the given list.
     * <p>
     * @param selectionList The list whose boolean values shall be updated
     * @param isSelected    The boolean to assign to each list element
     */
    private void setListSelected( List<Boolean> selectionList, boolean isSelected ) {
        for( int i = 0; i < selectionList.size(); i++ ) {
            selectionList.set( i, isSelected );
        }
    }


    /**
     * Sets if the start codon with the index i is currently selected.
     * <p>
     * @param i          The index of the current start codon
     * @param isSelected <code>true</code>, if the start codon is selected,
     *                   <code>false</code> otherwise
     */
    public void setStartCodonSelected( final int i, final boolean isSelected ) {
        this.selectedStarts.set( i, isSelected );
    }


    /**
     * Returns if the start codon with index i is currently selected.
     * <p>
     * @param i index of the start codon
     * <p>
     * @return <code>true</code> if the start codon with index i is currently
     *         selected, <code>false</code> otherwise
     */
    public boolean isStartCodonSelected( final int i ) {
        return this.selectedStarts.get( i );
    }


    /**
     * Sets if the stop codon with the index i is currently selected.
     * <p>
     * @param i          the index of the current stop codon
     * @param isSelected <code>true</code>, if the stop codon is selected,
     *                   <code>false</code> otherwise
     */
    public void setStopCodonSelected( final int i, final boolean isSelected ) {
        this.selectedStops.set( i, isSelected );
    }


    /**
     * Returns if the stop codon with index i is currently selected.
     * <p>
     * @param i index of the stop codon
     * <p>
     * @return <code>true</code> if the stop codon with index i is currently
     *         selected, <code>false</code> otherwise
     */
    public boolean isStopCodonSelected( final int i ) {
        return this.selectedStops.get( i );
    }


    /**
     * Resets the set of start and stop codons according to the currently
     * selected genetic code.
     */
    public final void resetCodons() {
        Pair<String[], String[]> geneticCode = CodonUtilities.getGeneticCodeArrays();

        startCodons = geneticCode.getFirst();
        stopCodons = geneticCode.getSecond();
        selectedStarts = new ArrayList<>();
        selectedStops = new ArrayList<>();
        for( String startCodon : startCodons ) {
            this.selectedStarts.add( false );
        }
        for( String stopCodon : stopCodons ) {
            this.selectedStops.add( false );
        }
    }


}
