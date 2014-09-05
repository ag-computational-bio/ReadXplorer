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
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.util.classification.FeatureType;
import de.cebitec.readXplorer.util.classification.MappingClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Creates a parameters set which contains all parameters concerning the usage
 * of ReadXplorer's coverage classes and if only uniquely mapped reads shall be used,
 * or all reads.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParametersReadClasses {
    
    private final Preferences pref = NbPreferences.forModule(Object.class);
    private boolean useExtendedClassification = pref.getBoolean(Properties.VIEWER_CLASSIFICATION, false); //TODO: change this for future version
    private List<Classification> excludedClasses;
    private final byte minMappingQual;
    private byte strandOption;

    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's coverage classes and if only uniquely mapped reads
     * shall be used, or all reads.
     * @param excludedClasses List of classifications to exclude when this 
     * parameter set is used
     * @param minMappingQual Minimum phred scaled mapping quality.
     * @param strandOption The strand option: Determines if mappings from the
     * feature strand, from the opposite strand or combined from both strands
     * are used. The values are among<br> 
     * {@link Properties.STRAND_FEATURE}<br>
     * {@link Properties.STRAND_OPPOSITE} and<br> 
     * {@link Properties.STRAND_BOTH}
     */
    public ParametersReadClasses(List<Classification> excludedClasses, byte minMappingQual, byte strandOption) {
        this.excludedClasses = excludedClasses;
        this.minMappingQual = minMappingQual;
        this.strandOption = strandOption;
    }
    
    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's coverage classes and if only uniquely mapped reads
     * shall be used, or all reads.
     * @param excludedClasses List of classifications to exclude when this 
     * parameter set is used
     * @param minMappingQual Minimum phred scaled mapping quality.
     */
    public ParametersReadClasses(List<Classification> excludedClasses, byte minMappingQual) {
        this(excludedClasses, minMappingQual, Properties.STRAND_FEATURE);
    }

    /**
     * Constructor with standard values. All read classes and all reads are 
     * included here.
     */
    public ParametersReadClasses() {
        this(new ArrayList<Classification>(), Byte.valueOf("0"), Properties.STRAND_FEATURE);
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
    public boolean isClassificationAllowed(Classification classification) {
        boolean isAllowed;
        if (useExtendedClassification) { //easy default case
            isAllowed = !this.excludedClasses.contains(classification);
        
        } else {
            if (classification.equals(MappingClass.SINGLE_PERFECT_MATCH)) {
                isAllowed = !this.excludedClasses.contains(MappingClass.PERFECT_MATCH);
            } else if (classification.equals(MappingClass.SINGLE_BEST_MATCH)) {
                isAllowed = !this.excludedClasses.contains(MappingClass.BEST_MATCH);
            } else if (classification.equals(MappingClass.SINGLE_COMMON_MATCH)) {
               isAllowed = !this.excludedClasses.contains(MappingClass.COMMON_MATCH); 
            } else {
                isAllowed = !this.excludedClasses.contains(classification);
            }
        }
        return isAllowed;
    }

    /**
     * @return The list of the currently excluded classifications when using
     * this parameter set.
     */
    public List<Classification> getExcludedClasses() {
        return this.excludedClasses;
    }
    
    /**
     * Adds all parameters stored in this object to the given statistics 
     * export data.
     * @param statisticsExportData The table data for an export table
     */
    public void addReadClassParamsToStats(List<List<Object>> statisticsExportData) {
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum mapping quality:", this.getMinMappingQual()));
        List<Classification> classList = new ArrayList<>();
        classList.addAll(Arrays.asList(MappingClass.values()));
        classList.add(FeatureType.MULTIPLE_MAPPED_READ);
        for (Classification classType : classList) {
            String isAllowed = isClassificationAllowed(classType) ? "yes" : "no";
            statisticsExportData.add(ResultTrackAnalysis.createTableRow(classType.getTypeString() + " included:", isAllowed));
        }
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
