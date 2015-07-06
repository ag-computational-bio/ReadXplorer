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

package de.cebitec.readxplorer.tools.coverageanalysis.featurecoverageanalysis;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.TrackResultEntry;


/**
 * Data structure for storing an feature (gene), which is detected as
 * covered and its corresponding data.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeature extends TrackResultEntry {

    private final PersistentFeature coveredFeature;
    private final int featLength;
    private int noCoveredBases;
    private int percentCovered;
    private int meanCoverage = 0;


    /**
     * Data structure for storing an feature (gene), which is detected as
     * covered and its corresponding data.
     * <p>
     * @param coveredFeature the feature which is detected as covered
     * @param noCoveredBases the number of covered bases of this feature
     */
    public CoveredFeature( PersistentFeature coveredFeature, int trackId ) {
        super( trackId );
        this.coveredFeature = coveredFeature;
        this.featLength = coveredFeature.getLength();
    }


    /**
     * Sets the number of covered bases of this feature.
     * <p>
     * @param noCoveredBases the number of covered bases of this feature
     */
    public void setNoCoveredBases( int noCoveredBases ) {
        this.noCoveredBases = noCoveredBases;
        this.percentCovered = (int) ((float) this.noCoveredBases / this.featLength * 100);
    }


    /**
     * @return the feature which is detected as covered
     */
    public PersistentFeature getCoveredFeature() {
        return this.coveredFeature;
    }


    /**
     * @return the number of covered bases of this feature
     */
    public int getNoCoveredBases() {
        return this.noCoveredBases;
    }


    /**
     * @return the percentage of this feature, which is covered
     */
    public int getPercentCovered() {
        return this.percentCovered;
    }


    /**
     * @return The mean coverage of this feature.
     */
    public int getMeanCoverage() {
        return this.meanCoverage;
    }


    /**
     * @param meanCoverage The mean coverage of this feature.
     */
    public void setMeanCoverage( int meanCoverage ) {
        this.meanCoverage = meanCoverage;
    }


}
