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

    /**
     * Creates a parameter set which contains all parameters concerning the 
     * usage of ReadXplorer's coverage classes and if only uniquely mapped reads shall
     * be used, or all reads.
     * @param usePerfectMatch <cc>true</cc>, if the perfect match class used in
     * this parameter set, <cc>false</cc> otherwise
     * @param useBestMatch <cc>true</cc>, if the best match class used in
     * this parameter set, <cc>false</cc> otherwise
     * @param useCommonMatch cc>true</cc>, if the common match class used in
     * this parameter set, <cc>false</cc> otherwise
     * @param useOnlyUniqueReads <cc>true</cc>, if only uniquely mapped reads
     * are used in this parameter set, <cc>false</cc> otherwise
     * @param minMappingQual Minimum phred scaled mapping quality.
     */
    public ParametersReadClasses(boolean usePerfectMatch, boolean useBestMatch, boolean useCommonMatch, boolean useOnlyUniqueReads, byte minMappingQual) {
        this.usePerfectMatch = usePerfectMatch;
        this.useBestMatch = useBestMatch;
        this.useCommonMatch = useCommonMatch;
        this.useOnlyUniqueReads = useOnlyUniqueReads;
        this.minMappingQual = minMappingQual;
    }

    /**
     * Constructor with standard values. All read classes and all reads are 
     * included here.
     */
    public ParametersReadClasses() {
        this(true, true, true, false, Byte.valueOf("0"));
    }

    /**
     * @return <cc>true</cc>, if the perfect match class is included,
     * <cc>false</cc> otherwise.
     */
    public boolean isPerfectMatchUsed() {
        return usePerfectMatch;
    }

    /**
     * @return <cc>true</cc>, if the best match class is included,
     * <cc>false</cc> otherwise.
     */
    public boolean isBestMatchUsed() {
        return useBestMatch;
    }

    /**
     * @return <cc>true</cc>, if the common match class is included, 
     * <cc>false</cc> otherwise.
     */
    public boolean isCommonMatchUsed() {
        return useCommonMatch;
    }

    /**
     * @return <cc>true</cc>, if only unique reads shall be used,
     * <cc>false</cc> if all reads shall be used.
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
    }
}
