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

package de.cebitec.readXplorer.databackend;


import java.util.concurrent.TimeUnit;


/**
 * A simple StopWatch for measuring execution time
 * <p>
 * @author evgeny
 */
public class StopWatch {

    private long startTime;


    public StopWatch() {
        reset();
    }


    public void reset() {
        this.startTime = System.currentTimeMillis();
    }


    public long getElapsedTime() {
        return System.currentTimeMillis() - this.startTime;
    }


    public String getElapsedTimeAsString() {
        long millis = this.getElapsedTime();
        long secs = TimeUnit.MILLISECONDS.toSeconds( millis );
        long millis_carryover = millis - TimeUnit.SECONDS.toMillis( secs );
        String s = "";
        if( secs > 0 ) {
            s = String.format( "%d s ", secs );
        }
        s += String.format( "%d ms", millis_carryover );
        return s;
    }


}
