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

package de.cebitec.readxplorer.utils;


/**
 *
 * @author ddoppmei
 */
public final class Benchmark {

    private static final int MILLISECONDS_PER_HOUR = 3600000;
    private static final int MILLISECONDS_PER_MINUTE = 60000;


    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private Benchmark() {
    }


    /**
     * Benchmarks something in time.
     * <p>
     * @param startTime the start time of the benchmark in milliseconds
     * @param finishTime the finish time of the benchmark in milliseconds
     * @param message the message to concatenate with the result
     * <p>
     * @return the message concatenated with the time difference between both
     * time points separated by hours, minutes, seconds and milliseconds
     */
    public static String calculateDuration( long startTime, long finishTime, String message ) {

        int diff = (int) (finishTime - startTime);

        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        if( diff > MILLISECONDS_PER_HOUR ) { //milliseconds per hour
            hours = diff / MILLISECONDS_PER_HOUR;
            diff -= hours * MILLISECONDS_PER_HOUR;
        }

        if( diff > MILLISECONDS_PER_MINUTE ) { // milliseconds per minute
            minutes = diff / MILLISECONDS_PER_MINUTE;
            diff -= minutes * MILLISECONDS_PER_MINUTE;
        }

        if( diff > 1000 ) { // milliseconds per second
            seconds = diff / 1000;
            diff -= seconds * 1000;
        }


        StringBuilder sb = new StringBuilder( message );
        if( hours > 0 ) {
            sb.append( hours ).append( " h " );
        }
        if( minutes > 0 ) {
            sb.append( minutes ).append( " min " );
        }
        if( seconds > 0 ) {
            sb.append( seconds ).append( " s " );
        }
        if( diff > 0 ) {
            sb.append( diff ).append( " millis " );
        }
        return sb.toString();

    }


}
