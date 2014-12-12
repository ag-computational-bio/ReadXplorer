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

package de.cebitec.readxplorer.ui.dialogmenus;


/**
 * Interface designed for classes showing an RNAFolder view.
 *
 * @author Rolf Hilker
 */
public interface RNAFolderI {

    /**
     * Creates a new RNAFolder view.
     * <p>
     * @param sequenceToFold the DNA or RNA sequence to fold
     * @param header         the description of the folded sequence to use as
     *                       header
     */
    public void showRNAFolderView( String sequenceToFold, String header );


}
