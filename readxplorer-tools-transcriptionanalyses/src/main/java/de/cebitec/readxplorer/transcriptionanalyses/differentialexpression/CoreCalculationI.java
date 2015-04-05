/*
 * Copyright (C) 2015 Agne Matuseviciute
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

/**
 *
 * @author Kai Bernd Stadermann
 */
public interface CoreCalculationI {
    
    /**
     * Calculates the regression for one gene.
     * 
     * @param conditionA Per position count data under condition A.
     * @param conditionB Per position count data under condition B.
     * @return The regression coefficient between A and B.
     * @throws IllegalArgumentException if the input arrays have different length.
     */
    double[] calculate(int[] conditionA, int[] conditionB) throws IllegalArgumentException;
    
}
