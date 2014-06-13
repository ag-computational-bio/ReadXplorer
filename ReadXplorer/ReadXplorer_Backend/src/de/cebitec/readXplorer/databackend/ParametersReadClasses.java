/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.util.Properties;
import java.util.List;

/**
 * Creates a parameters set which contains all parameters concerning the usage
 * of ReadXplorer's coverage classes and if only uniquely mapped reads shall be used,
 * or all reads.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParametersReadClasses {
    
    private boolean usePerfectMatch; 
    private boolean useBestMatch;
    private boolean useCommonMatch;
    private boolean useOnlyUniqueReads;
    private final byte minMappingQual;
    private byte strandOption;

    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's coverage classes and if only uniquely mapped reads
     * shall be used, or all reads.
     * @param usePerfectMatch <code>true</code>, if the perfect match class used
     * in this parameter set, <code>false</code> otherwise
     * @param useBestMatch <code>true</code>, if the best match class used in
     * this parameter set, <code>false</code> otherwise
     * @param useCommonMatch <code>true</code>, if the common match class used
     * in this parameter set, <code>false</code> otherwise
     * @param useOnlyUniqueReads <code>true</code>, if only uniquely mapped
     * reads are used in this parameter set, <code>false</code> otherwise
     * @param minMappingQual Minimum phred scaled mapping quality.
     * @param strandOption The strand option: Determines if mappings from the
     * feature strand, from the opposite strand or combined from both strands
     * are used. The values are among<br> 
     * {@link Properties.STRAND_FEATURE}<br>
     * {@link Properties.STRAND_OPPOSITE} and<br> 
     * {@link Properties.STRAND_BOTH}
     */
    public ParametersReadClasses(boolean usePerfectMatch, boolean useBestMatch, boolean useCommonMatch, 
            boolean useOnlyUniqueReads, byte minMappingQual, byte strandOption) {
        this.usePerfectMatch = usePerfectMatch;
        this.useBestMatch = useBestMatch;
        this.useCommonMatch = useCommonMatch;
        this.useOnlyUniqueReads = useOnlyUniqueReads;
        this.minMappingQual = minMappingQual;
        this.strandOption = strandOption;
    }
    
    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's coverage classes and if only uniquely mapped reads
     * shall be used, or all reads.
     * @param usePerfectMatch <code>true</code>, if the perfect match class used
     * in this parameter set, <code>false</code> otherwise
     * @param useBestMatch <code>true</code>, if the best match class used in
     * this parameter set, <code>false</code> otherwise
     * @param useCommonMatch <code>true</code>, if the common match class used
     * in this parameter set, <code>false</code> otherwise
     * @param useOnlyUniqueReads <code>true</code>, if only uniquely mapped
     * reads are used in this parameter set, <code>false</code> otherwise
     * @param minMappingQual Minimum phred scaled mapping quality.
     */
    public ParametersReadClasses(boolean usePerfectMatch, boolean useBestMatch, boolean useCommonMatch, 
            boolean useOnlyUniqueReads, byte minMappingQual) {
        this(usePerfectMatch, useBestMatch, useCommonMatch, useOnlyUniqueReads, minMappingQual, Properties.STRAND_FEATURE);
    }

    /**
     * Constructor with standard values. All read classes and all reads are 
     * included here.
     */
    public ParametersReadClasses() {
        this(true, true, true, false, Byte.valueOf("0"), Properties.STRAND_FEATURE);
    }

    /**
     * @return <code>true</code>, if the perfect match class is included,
     * <code>false</code> otherwise.
     */
    public boolean isPerfectMatchUsed() {
        return usePerfectMatch;
    }

    /**
     * @return <code>true</code>, if the best match class is included,
     * <code>false</code> otherwise.
     */
    public boolean isBestMatchUsed() {
        return useBestMatch;
    }

    /**
     * @return <code>true</code>, if the common match class is included, 
     * <code>false</code> otherwise.
     */
    public boolean isCommonMatchUsed() {
        return useCommonMatch;
    }

    /**
     * @return <code>true</code>, if only unique reads shall be used,
     * <code>false</code> if all reads shall be used.
     */
    public boolean isOnlyUniqueReads() {
        return useOnlyUniqueReads;
    }

    /**
     * @return Minimum phred scaled mapping quality.
     */
    public byte getMinMappingQual() {
        return minMappingQual;
    }

    /**
     * @return The strand option: Determines if mappings from the feature
     * strand, from the opposite strand or combined from both strands are used.
     * The values are among<br>
     * {@link Properties.STRAND_FEATURE}<br>
     * {@link Properties.STRAND_OPPOSITE} and<br>
     * {@link Properties.STRAND_BOTH}
     */
    public byte getStrandOption() {
        return strandOption;
    }
    
    /**
     * Checks if the given classification is allowed to be used.
     * @param classification The classification to check
     * @return true, if the given classification is allowed to be used, false
     * otherwise
     */
    public boolean isClassificationAllowed(int classification) {
        switch (classification) {
            case Properties.PERFECT_COVERAGE : return isPerfectMatchUsed();
            case Properties.BEST_MATCH_COVERAGE : return isBestMatchUsed();
            case Properties.COMPLETE_COVERAGE : return isCommonMatchUsed();
            default: return false;
        }
    }
    
    /**
     * Adds all parameters stored in this object to the given statistics 
     * export data.
     * @param statisticsExportData The table data for an export table
     */
    public void addReadClassParamsToStats(List<List<Object>> statisticsExportData) {
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum mapping quality:", this.getMinMappingQual()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Perfect match class included:", this.isPerfectMatchUsed() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Best match class included:", this.isBestMatchUsed() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Common match class included:", this.isCommonMatchUsed() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Only unique mappings included:", this.isOnlyUniqueReads() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Mapping strand selection:", this.getStrandOptionString()));
    }
    
    /**
     * Creates a String representing the strand option stored in this
     * ParametersReadClasses object.
     * @return A user-friendly representation of the strand option.
     */
    public String getStrandOptionString() {
        String strandOptionString = "Feature/analysis strand"; //default
        if (this.getStrandOption() == Properties.STRAND_OPPOSITE) {
            strandOptionString += "Opposite Strand";
        } else if ( this.getStrandOption() == Properties.STRAND_BOTH || 
                    this.getStrandOption() == Properties.STRAND_BOTH_FWD ||
                    this.getStrandOption() == Properties.STRAND_BOTH_REV) {
            strandOptionString += "Combine both strands";
        }
        return strandOptionString;
    }
    
    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the feature strand option is selected.
     * @return <code>true</code>, if the mappings of the feature strand are
     * taken into account = the feature strand option is chosen.
     * <code>false</code>, if one of the other options is chosen.
     */
    public boolean isStrandFeatureOption() {
        return this.getStrandOption() == Properties.STRAND_FEATURE;
    }
    
    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the opposite strand option is selected.
     * @return <code>true</code>, if the mappings of the opposite strand are
     * taken into account = the opposite strand option is chosen. 
     * <code>false</code>, if one of the other options is chosen.
     */
    public boolean isStrandOppositeOption() {
        return this.getStrandOption() == Properties.STRAND_OPPOSITE;
    }

    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the both strands option is selected.
     * @return <code>true</code>, if the mappings of both strands are taken into
     * account = the comine both strands option is chosen. <code>false</code>,
     * if one of the other options is chosen.
     */
    public boolean isStrandBothOption() {
        return  this.getStrandOption() == Properties.STRAND_BOTH || 
                this.getStrandOption() == Properties.STRAND_BOTH_FWD ||
                this.getStrandOption() == Properties.STRAND_BOTH_REV;
    }
    
    /**
     * Convenience method which can be used instead of {@link #getStrandOption}
     * to query if the both strands option and in forward direction is selected.
     * NOTE: Before using this method always check {@link #isStrandBothOption} 
     * to make sure that strands shall be combined in the first place!
     * @return <code>true</code>, if the mappings of both strands are taken into
     * account and treated as originating from the forward strand.
     * <code>false</code>, if they shall be treated as originating from the 
     * reverse strand OR if another strand option is chosen.
     */
    public boolean isStrandBothFwdOption() {
        return this.getStrandOption() == Properties.STRAND_BOTH_FWD;
    }

    /**
     * The strand option is allowed to be updated, as more specific data can be 
     * collected after the inital selection.
     * @param strandOption The updated strand option, e.g. {@link 
     * Properties.STRAND_BOTH_FWD} or {@link Properties.STRAND_BOTH_REV}.
     */
    public void setStrandOption(byte strandOption) {
        this.strandOption = strandOption;
    }
    
}
