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

package de.cebitec.readxplorer.api.objects;


/**
 * Interface to use for any kind of analysis, which can take advantage of using
 * the given methods.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 * @param <T> Object type of the result of this analysis.
 */
public interface AnalysisI<T> {

    /**
     * @return Returns the results of the analysis.
     */
    public T getResults();


}
