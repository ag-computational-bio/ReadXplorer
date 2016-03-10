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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readxplorer.api.Classification;
import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.enums.MappingClass;
import de.cebitec.readxplorer.api.enums.Strand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Creates a parameters set which contains all parameters concerning the usage
 * of ReadXplorer's coverage classes and if only uniquely mapped reads shall be
 * used, or all reads.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParametersReadClasses {

    private List<Classification> excludedClasses;
    private final byte minMappingQual;
    private Strand strandOption;


    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's coverage classes and if only uniquely mapped reads
     * shall be used, or all reads.
     * <p>
     * @param excludedClasses List of classifications to exclude when this
     *                        parameter set is used
     * @param minMappingQual  Minimum phred scaled mapping quality.
     * @param strandOption    The strand option: Determines if mappings from the
     *                        feature strand, from the opposite strand or
     *                        combined from both strands are used. The values
     *                        are among<br> {@link Strand.Feature}<br>
     *                        {@link Strand.Opposite} and<br>
     *                        {@link Strand.Both}
     */
    public ParametersReadClasses( List<Classification> excludedClasses, byte minMappingQual, Strand strandOption ) {
        this.excludedClasses = new ArrayList<>( excludedClasses );
        this.minMappingQual = minMappingQual;
        this.strandOption = strandOption;
    }


    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's coverage classes and if only uniquely mapped reads
     * shall be used, or all reads.
     * <p>
     * @param excludedClasses List of classifications to exclude when this
     *                        parameter set is used
     * @param minMappingQual  Minimum phred scaled mapping quality.
     */
    public ParametersReadClasses( List<Classification> excludedClasses, byte minMappingQual ) {
        this( excludedClasses, minMappingQual, Strand.Feature );
    }


    /**
     * Constructor with standard values. All read classes and all reads are
     * included here.
     */
    public ParametersReadClasses() {
        this( new ArrayList<>(), (byte) 0, Strand.Feature );
    }


    /**
     * @return Minimum phred scaled mapping quality.
     */
    public byte getMinMappingQual() {
        return minMappingQual;
    }


    /**
     * @return The strand option: Determines if mappings from the feature
     *         strand, from the opposite strand or combined from both strands
     *         are used. The values are among<br> {@link Strand.Feature}<br>
     *         {@link Strand.Opposite} and<br> {@link Strand.Both}
     */
    public Strand getStrandOption() {
        return strandOption;
    }


    /**
     * Checks if the given classification is allowed to be used.
     * <p>
     * @param classification The classification to check
     * <p>
     * @return true, if the given classification is allowed to be used, false
     *         otherwise
     */
    public boolean isClassificationAllowed( Classification classification ) {
        return !excludedClasses.contains( classification );
    }


    /**
     * @return The list of the currently excluded classifications when using
     *         this parameter set.
     */
    public List<Classification> getExcludedClasses() {
        return Collections.unmodifiableList( excludedClasses );
    }


    /**
     * Adds all parameters stored in this object to the given statistics export
     * data.
     * <p>
     * @param statisticsExportData The table data for an export table
     */
    public void addReadClassParamsToStats( List<List<Object>> statisticsExportData ) {
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum mapping quality:", getMinMappingQual() ) );
        List<Classification> classList = new ArrayList<>();
        classList.addAll( Arrays.asList( MappingClass.values() ) );
        for( Classification classType : classList ) {
            String isAllowed = isClassificationAllowed( classType ) ? "yes" : "no";
            statisticsExportData.add( ResultTrackAnalysis.createTableRow( classType + " included:", isAllowed ) );
        }
        String isAllowed = isClassificationAllowed( FeatureType.MULTIPLE_MAPPED_READ ) ? "yes" : "no";
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( FeatureType.MULTIPLE_MAPPED_READ + ":", isAllowed ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Mapping strand selection:", getStrandOptionString() ) );
    }


    /**
     * Creates a String representing the strand option stored in this
     * ParametersReadClasses object.
     * <p>
     * @return A user-friendly representation of the strand option.
     */
    public String getStrandOptionString() {
        String strandOptionString = "Feature/analysis strand"; //default
        if( getStrandOption() == Strand.Opposite ) {
            strandOptionString += "Opposite Strand";
        } else if( getStrandOption() == Strand.Both ||
                 getStrandOption() == Strand.BothForward ||
                 getStrandOption() == Strand.BothReverse ) {
            strandOptionString += "Combine both strands";
        }
        return strandOptionString;
    }


    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the feature strand option is selected.
     * <p>
     * @return <code>true</code>, if the mappings of the feature strand are
     *         taken into account = the feature strand option is chosen.
     *         <code>false</code>, if one of the other options is chosen.
     */
    public boolean isStrandFeatureOption() {
        return getStrandOption() == Strand.Feature;
    }


    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the opposite strand option is selected.
     * <p>
     * @return <code>true</code>, if the mappings of the opposite strand are
     *         taken into account = the opposite strand option is chosen.
     *         <code>false</code>, if one of the other options is chosen.
     */
    public boolean isStrandOppositeOption() {
        return getStrandOption() == Strand.Opposite;
    }


    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the both strands option is selected.
     * <p>
     * @return <code>true</code>, if the mappings of both strands are taken into
     *         account = the combine both strands option is chosen.
     *         <code>false</code>, if one of the other options is chosen.
     */
    public boolean isStrandBothOption() {
        return getStrandOption() == Strand.Both ||
                 getStrandOption() == Strand.BothForward ||
                 getStrandOption() == Strand.BothReverse;
    }


    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the both strands option and in forward direction is selected.
     * NOTE: Before using this method always check {@link #isStrandBothOption}
     * to make sure that strands shall be combined in the first place!
     * <p>
     * @return <code>true</code>, if the mappings of both strands are taken into
     *         account and treated as originating from the forward strand.
     *         <code>false</code>, if they shall be treated as originating from
     *         the reverse strand OR if another strand option is chosen.
     */
    public boolean isStrandBothFwdOption() {
        return getStrandOption() == Strand.BothForward;
    }


    /**
     * The strand option is allowed to be updated, as more specific data can be
     * collected after the inital selection.
     * <p>
     * @param strandOption The updated strand option, e.g. {@link
     * Strand.BothForward} or {@link Strand.BothReverse}.
     */
    public void setStrandOption( Strand strandOption ) {
        this.strandOption = strandOption;
    }


}
