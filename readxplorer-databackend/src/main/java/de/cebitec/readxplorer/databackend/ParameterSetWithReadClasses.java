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

package de.cebitec.readxplorer.databackend;


/**
 * A parameter set which contains read classification parameters.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParameterSetWithReadClasses {

    private ParametersReadClasses readClassParams;


    /**
     * A parameter set which contains read classification parameters.
     * <p>
     * @param readClassParams The read classification parameters to set.
     */
    public ParameterSetWithReadClasses( ParametersReadClasses readClassParams ) {
        this.readClassParams = readClassParams;
    }


    /**
     * @return The read classification parameters, which shall be taken into
     *         account for this analysis.
     */
    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }


    /**
     * @param readClassParams The read classification parameters, which shall be
     *                        taken into account for this analysis.
     */
    public void setReadClassParams( ParametersReadClasses readClassParams ) {
        this.readClassParams = readClassParams;
    }


}
