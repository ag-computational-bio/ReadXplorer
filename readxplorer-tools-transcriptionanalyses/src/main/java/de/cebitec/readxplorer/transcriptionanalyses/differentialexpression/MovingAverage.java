/*
 * Copyright (C) 2015 Kai Bernd Stadermann
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
 * @author kstaderm
 */
public class MovingAverage {

    private MovingAverage() {
    }


    public static int[] calculateMovingAverage( int[] numbers, int windowSize ) {
        int[] ret = new int[numbers.length];

        if(windowSize < 2){
            throw new IllegalArgumentException("Window size must be greater one!");
        }
        
        for( int i = 0; i <= numbers.length - windowSize; i++ ) {
            
        }

        return ret;
    }


}
