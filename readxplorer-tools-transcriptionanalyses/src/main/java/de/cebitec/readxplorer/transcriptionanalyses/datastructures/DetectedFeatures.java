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

package de.cebitec.readxplorer.transcriptionanalyses.datastructures;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;


/**
 * Data structure for storing the features belonging to a certain position. E.g.
 * the features belonging to a gene start position. It can contain three
 * different
 * types of features: A correctStartFeature, starting at the position belonging
 * to
 * this element. An upstream- and a downstreamFeature.
 * If they are not stored the return methods return null.
 *
 * @author -Rolf Hilker-
 */
public class DetectedFeatures {

    private PersistentFeature upstreamFeature;
    private PersistentFeature correctStartFeature;
    private PersistentFeature downstreamFeature;
    private boolean isLeaderless;


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


    public PersistentFeature getDownstreamFeature() {
        return this.downstreamFeature;
    }


    public void setDownstreamFeature( PersistentFeature downstreamFeature ) {
        this.downstreamFeature = downstreamFeature;
    }


    public PersistentFeature getCorrectStartFeature() {
        return this.correctStartFeature;
    }


    public void setCorrectStartFeature( PersistentFeature correctStartFeature ) {
        this.correctStartFeature = correctStartFeature;
    }


    public PersistentFeature getUpstreamFeature() {
        return this.upstreamFeature;
    }


    public void setUpstreamFeature( PersistentFeature upstreamFeature ) {
        this.upstreamFeature = upstreamFeature;
    }


    /**
     * @return true, if this start has a downstream feature and is classified as
     *         leaderless, false otherwise.
     */
    public boolean isLeaderless() {
        return isLeaderless;
    }


    /**
     * @param isLeaderless Set true, if this start has a downstream feature and
     *                     is classified as leaderless, set false otherwise.
     */
    public void setIsLeaderless( boolean isLeaderless ) {
        this.isLeaderless = isLeaderless;
    }


}
