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
package de.cebitec.readXplorer.parser.common;

import java.util.Collection;
import java.util.HashMap;

/**
 * Container for the coverage data. Computes the coverage, on creation.
 *
 * @author ddoppmeier, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class CoverageContainer {

    private HashMap<Integer, Integer[]> coverage;
    private static final int BEST_MAPPING_CASE = 0;
    private static final int ZERO_ERROR_CASE = 1;
    private static final int N_ERROR_CASE = 2;
    private static final int FIELDS_PER_CASE = 4; //2 for fwd (all & without duplicates), and 2 rev

    private int coveredPerfectPositions;
    private int coveredBestMatchPositions;
    private int coveredCommonMatchPositions;

    /**
     * Creates a new and empty CoverageContainer.
     */
    public CoverageContainer() {
        this.coverage = new HashMap<>();
        this.coveredPerfectPositions = 0;
        this.coveredBestMatchPositions = 0;
        this.coveredCommonMatchPositions = 0;
    }

    public Collection<Integer> getCoveredPositions() {
        return coverage.keySet();
    }

    /**
     * @param position 
     * @return real coverage
     */
    public int getBestMappingForwardCoverage(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, true, true);
    }
    
    /**
     * 
     * @param position
     * @return number of unique mappings
     */
    public int getNumberOfBestMappingsForward(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, true, false);
    }

    public int getBestMappingReverseCoverage(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, false, true);
    }

    public int getNumberOfBestMappingsReverse(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, false, false);
    }

    public int getZeroErrorMappingsForwardCoverage(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, true, true);
    }

    public int getNumberOfZeroErrorMappingsForward(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, true, false);
    }

    public int getZeroErrorMappingsReverseCoverage(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, false, true);
    }

    public int getNumberOfZeroErrorMappingsReverse(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, false, false);
    }

    public int getNErrorMappingsForwardCoverage(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, true, true);
    }

    public int getNumberOfNErrorMappingsForward(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, true, false);
    }

    public int getNErrorMappingsReverseCoverage(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, false, true);
    }

    public int getNumberOfNErrorMappingsReverse(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, false, false);
    }

    /**
     *
     * @param position Position for which the coverage is requested
     * @param type Type of coverage that is queried. For example: bestMatchCoverage
     * @param forward Coverage on forward strand requested, yes or no,
     * @param mult If true, get the "real" coverage, if false get the number of unique mappings (no redundancy)
     * @return coverage
     */
    private int getCoverageOfType(int position, int type, boolean forward, boolean mult) {

        Integer[] cov = coverage.get(position);
        int baseIDX = type * FIELDS_PER_CASE; // basic index defined by type of coverage
        baseIDX += (forward ? 0 : 2); // if reverse strand requested, increase column by two
        baseIDX += (mult ? 0 : 1); // if multiplied coverage (instead of unique_mapping_coverage) requested do nothing, else increase by one

        return cov[baseIDX];
    }

    /**
     * Clears the coverage container (coverage and position table).
     */
    public void clearCoverageContainer() {
        coverage.clear();
    }
    
    /**
     * @param position the current position to check
     * @return true, if the current position is covered by mappings, false otherwise.
     */
    public boolean positionCovered(int position) {
        return coverage.containsKey(position);
    }

    /**
     * Set the number of positions covered by perfect match mappings.
     * @param coveredPerfectPositions number of positions covered by perfect match mappings
     */
    public void setCoveredPerfectPositions(int coveredPerfectPositions) {
        this.coveredPerfectPositions = coveredPerfectPositions;
    }

    /**
     * This parameter is currently set when calling the "ProjectConnector.storeCoverage()"
     * method. Before that happened it will be null.
     * @return the number of positions covered by perfect match mappings.
     */
    public int getCoveredPerfectPositions() {
        return this.coveredPerfectPositions;
    }

    /**
     * Set the number of positions covered by best match mappings.
     * @param coveredBestMatchPositions 
     */
    public void setCoveredBestMatchPositions(int coveredBestMatchPositions) {
        this.coveredBestMatchPositions = coveredBestMatchPositions;
    }
    
    /**
     * This parameter is currently set when calling the "ProjectConnector.storeCoverage()"
     * method. Before that happened it will be null.
     * @return the number of positions covered by best match mappings.
     */
    public int getCoveredBestMatchPositions() {
        return this.coveredBestMatchPositions;
    }
    
    /**
     * Set the number of positions covered by common match mappings.
     * @param coveredCommonMatchPositions number of positions covered by common match mappings
     */
    public void setCoveredCommonMatchPositions(int coveredCommonMatchPositions) {
        this.coveredCommonMatchPositions = coveredCommonMatchPositions;
    }
        
    /**
     * This parameter is currently set when calling the "ProjectConnector.storeCoverage()"
     * method. Before that happened it will be null.
     * @return the number of positions covered by common match mappings.
     */
    public int getCoveredCommonMatchPositions() {
        return this.coveredCommonMatchPositions;
    }

    public HashMap<Integer, Integer[]> getCoverage() {
        return coverage;
    }

    public void setCoverage(HashMap<Integer, Integer[]> coverage) {
        this.coverage = coverage;
    }
    
}
