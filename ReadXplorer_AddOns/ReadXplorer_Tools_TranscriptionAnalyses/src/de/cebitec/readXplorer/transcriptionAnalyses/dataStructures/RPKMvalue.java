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
package de.cebitec.readXplorer.transcriptionAnalyses.dataStructures;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.TrackResultEntry;

/**
 * Data storage for RPKM and read count values of a reference feature.
 *
 * @author Martin Tötsches, Rolf Hilker
 */
public class RPKMvalue extends TrackResultEntry {
    
    private PersistantFeature feature;
    private double rpkm;
    private int readCount;
    
    /**
     * Data storage for RPKM and read count values of a reference feature.
     * @param feature feature for which the values shall be stored
     * @param rpkm the RPKM value for this feature
     * @param readCount the raw read count for this feature
     * @param trackId the trackId for which these result values where calculated
     */
    public RPKMvalue(PersistantFeature feature, double rpkm, int readCount, int trackId) {
        super(trackId);
        this.feature = feature;
        this.rpkm = rpkm;
        this.readCount = readCount;
    }

    /**
     * @return the RPKM value for this feature.
     */
    public double getRPKM() {
        return rpkm;
    }

    /**
     * @param rpkm the RPKM value for this feature
     */
    public void setRpkm(double rpkm) {
        this.rpkm = rpkm;
    }

    /**
     * @return the feature for which the values shall be stored.
     */
    public PersistantFeature getFeature() {
        return feature;
    }

    /**
     * @param feature feature for which the values shall be stored
     */
    public void setFeature(PersistantFeature feature) {
        this.feature = feature;
    }

    /**
     * @return the raw read count for this feature.
     */
    public int getReadCount() {
        return readCount;
    }

    /**
     * @param readCount the raw read count for this feature
     */
    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }
}
