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

/**
 * Data structure for storing the features belonging to a certain position. E.g.
 * the features belonging to a gene start position. It can contain three different
 * types of features: A correctStartFeature, starting at the position belonging to
 * this element. An upstream- and a downstreamFeature.
 * If they are not stored the return methods return null.
 *
 * @author -Rolf Hilker-
 */
public class DetectedFeatures {
    
    private PersistantFeature upstreamFeature;
    private PersistantFeature correctStartFeature;
    private PersistantFeature downstreamFeature;

    /**
     * Data structure for storing the features belonging to a certain
     * position. E.g. the features belonging to a gene start position. It can
     * contain three different types of features: A correctStartFeature,
     * starting at the position belonging to this element. An upstream- and a
     * downstreamFeature. If they are not stored the return methods return
     * null.
     */
    public DetectedFeatures() {
    }
    
    public PersistantFeature getDownstreamFeature() {
        return this.downstreamFeature;
    }

    public void setDownstreamFeature(PersistantFeature downstreamFeature) {
        this.downstreamFeature = downstreamFeature;
    }

    public PersistantFeature getCorrectStartFeature() {
        return this.correctStartFeature;
    }

    public void setCorrectStartFeature(PersistantFeature correctStartFeature) {
        this.correctStartFeature = correctStartFeature;
    }

    public PersistantFeature getUpstreamFeature() {
        return this.upstreamFeature;
    }

    public void setUpstreamFeature(PersistantFeature upstreamFeature) {
        this.upstreamFeature = upstreamFeature;
    }
    
    
    
    
}
