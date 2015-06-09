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


import de.cebitec.readxplorer.api.enums.RegionType;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfo;
import de.cebitec.readxplorer.ui.datavisualisation.HighlightAreaListener;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.sequence.Region;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;


/**
 * Manages the detection and highlighting of regions in a sequence bar. It
 * currently supports start and stop codons and exact sequence region
 * highlighting.
 * <p>
 * @author Rolf Hilker
 */
public class RegionManager {

    private final SequenceBar regionVisualizer; //component that visualizes the regions
    private final AbstractViewer parentViewer; //the viewer, in which the sequence bar is embedded
    private final StartCodonFilter codonFilter;
    private final PatternFilter patternFilter;
    private final HighlightAreaListener highlightListener;
    private Preferences pref;
    private List<Region> cdsRegions;


    /**
     * Manages the detection and highlighting of regions in a sequence bar. It
     * currently supports start and stop codons and exact sequence region
     * highlighting.
     * <p>
     * @param regionVisualizer  the sequence bar, on which the regions are shown
     * @param parentViewer      the viewer, in which the sequence bar is
     *                          embedded
     * @param refGen            the reference genome
     * @param highlightListener the listener for highlighting a sequence of
     *                          choice and displaying a corresponding menu. It
     *                          is needed here, because all regions detected by
     *                          this manager play a special role for this
     *                          listener.
     */
    public RegionManager( SequenceBar regionVisualizer, AbstractViewer parentViewer,
                          PersistentReference refGen, HighlightAreaListener highlightListener ) {

        this.regionVisualizer = regionVisualizer;
        this.parentViewer = parentViewer;
        this.highlightListener = highlightListener;
        this.cdsRegions = new ArrayList<>();

        BoundsInfo bounds = parentViewer.getBoundsInfo();
        this.codonFilter = new StartCodonFilter( bounds.getLogLeft(), bounds.getLogRight(), refGen );
        this.patternFilter = new PatternFilter( bounds.getLogLeft(), bounds.getLogRight(), refGen );
        this.initPrefListener();
    }


    /**
     * Updates the sequence bar according to the genetic code chosen. After
     * changing the genetic code, no start codons are be selected anymore.
     */
    private void initPrefListener() {
        this.pref = NbPreferences.forModule( Object.class );
        this.pref.addPreferenceChangeListener( new PreferenceChangeListener() {

            @Override
            public void preferenceChange( PreferenceChangeEvent evt ) {
                if( evt.getKey().equals( Properties.SEL_GENETIC_CODE ) ) {
                    RegionManager.this.codonFilter.resetCodons();
                    RegionManager.this.findCodons();
                }
            }


        } );
    }


    /**
     * Calculates which start codons should be highlighted and updates the gui.
     * <p>
     * @param i          the index of the codon to update
     * @param isSelected true, if the codon should be selected
     */
    public void showStartCodons( final int i, final boolean isSelected ) {
        this.codonFilter.setStartCodonSelected( i, isSelected );
        this.findCodons();
    }


    /**
     * Calculates which stop codons should be highlighted and updates the gui.
     * <p>
     * @param i          the index of the codon to update
     * @param isSelected true, if the codon should be selected
     */
    public void showStopCodons( final int i, final boolean isSelected ) {
        this.codonFilter.setStopCodonSelected( i, isSelected );
        this.findCodons();
    }


    /**
     * Returns if the codon with the index i is currently selected.
     * <p>
     * @param i the index of the codon
     * <p>
     * @return true, if the codon with the index i is currently selected
     */
    public boolean isStartCodonShown( final int i ) {
        return this.codonFilter.isStartCodonSelected( i );
    }


    /**
     * Detects the occurrences of the given pattern in the currently shown
     * interval or the next occurrence of the pattern in the genome.
     * <p>
     * @param pattern Pattern to search for
     * <p>
     * @return the closest position of the next occurrence of the pattern
     */
    public int showPattern( String pattern ) {
        this.patternFilter.setPattern( pattern );
        return this.findPattern();
    }


    /**
     * If the list of CDS regions is not empty, then they are transformed into a
     * JRegion and displayed in the regionVisualizer (SequenceBar).
     */
    public void showCdsRegions() {
        this.regionVisualizer.removeAll( RegionType.CDS );

        if( !this.cdsRegions.isEmpty() ) {
            for( Region region : this.cdsRegions ) {
                JRegion cdsJRegion = this.regionVisualizer.transformRegionToJRegion( region );
                this.regionVisualizer.add( cdsJRegion );
            }
        }
        this.regionVisualizer.repaint();
    }


    /**
     * Identifies the codons according to the currently selected codons to show
     * and adds JRegions for highlighting into the sequence bar.
     */
    public void findCodons() {
        //create the list of component types, that should be removed (only patterns)
        List<RegionType> typeList = Arrays.asList( RegionType.Start, RegionType.Stop );
        this.regionVisualizer.removeAll( typeList );
        this.highlightListener.clearSpecialRegions();
        this.codonFilter.setInterval( this.parentViewer.getBoundsInfo().getLogLeft(), this.parentViewer.getBoundsInfo().getLogRight() );
        this.regionVisualizer.determineFeatureFrame();
        byte frameCurrFeature = this.regionVisualizer.getFrameCurrFeature();

        this.codonFilter.setAnalysisFrame( frameCurrFeature );
        List<Region> codonHitsToHighlight = this.codonFilter.findRegions();
        for( Region region : codonHitsToHighlight ) {

            JRegion cdsJRegion = this.regionVisualizer.transformRegionToJRegion( region );
            this.highlightListener.addSpecialRegion( cdsJRegion );
            this.regionVisualizer.add( cdsJRegion );
        }
        this.regionVisualizer.repaint();
    }


    /**
     * Identifies the currently in this object stored pattern in the genome
     * sequence.
     * <p>
     * @return position of the next occurrence of the pattern from the current
     *         position on.
     */
    public int findPattern() {
        //create the list of component types, that should be removed (only patterns)
        this.regionVisualizer.removeAll( RegionType.Pattern );
        this.patternFilter.setInterval( this.parentViewer.getBoundsInfo().getLogLeft(), this.parentViewer.getBoundsInfo().getLogRight() );

        List<Region> patternHitsToHighlight = this.patternFilter.findRegions();
        for( Region region : patternHitsToHighlight ) {

            JRegion patternRegion = this.regionVisualizer.transformRegionToJRegion( region );
            this.regionVisualizer.add( patternRegion );
        }
        this.regionVisualizer.repaint();

        if( patternHitsToHighlight.isEmpty() ) {
            return this.patternFilter.findNextOccurrence();
        } else {
            return -2;
        }
    }


    /**
     * Identifies next (closest) occurrence from either forward or reverse
     * strand of a pattern in the current reference genome.
     * <p>
     * @return the position of the next occurrence of the pattern
     */
    public int findNextPatternOccurrence() {
        return this.patternFilter.findNextOccurrence();
    }


    /**
     * Sets a list of cds regions and replaces the list stored in this variable
     * until now.
     * <p>
     * @param cdsRegions the cdsRegions to set
     */
    public void setCdsRegions( List<Region> cdsRegions ) {
        if( this.cdsRegions.containsAll( cdsRegions ) ) {
            this.cdsRegions.removeAll( cdsRegions );
        } else {
            this.cdsRegions = cdsRegions;
        }
        this.showCdsRegions();
    }


}
