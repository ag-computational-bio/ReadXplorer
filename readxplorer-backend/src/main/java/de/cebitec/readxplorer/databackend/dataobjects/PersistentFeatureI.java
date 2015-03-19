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

package de.cebitec.readxplorer.databackend.dataobjects;


import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.sequence.GenomicRange;


/**
 * Interface for all persistent features.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface PersistentFeatureI extends GenomicRange {

    /**
     * @return Start of the feature. Always the smaller value among start and
     *         stop.
     */
    @Override
    public int getStart();


    /**
     * @return Stop of the feature. Always the larger value among start and
     *         stop.
     */
    @Override
    public int getStop();


    /**
     * @return Type of the feature among {@link FeatureType}s.
     */
    public FeatureType getType();


}
