package de.cebitec.vamp.util;

/**
 *
 * @author ddoppmei
 */
public class Benchmark {

    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private Benchmark() {
    }

    /**
     * Benchmarks something in time.
     * @param startTime the start time of the benchmark in milliseconds
     * @param finishTime the finish time of the benchmark in milliseconds
     * @param message the message to concatenate with the result
     * @return the message concatenated with the time difference between both 
     * time points separated by hours, minutes, seconds and milliseconds
     */
    public static String calculateDuration(long startTime, long finishTime, String message){
        
        long diff = finishTime - startTime;

        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        int millis;
        
        if (diff > 3600000) { //milliseconds per hour
            hours = (int) (diff / 3600000);
            diff -= (hours * 3600000);
        }

        if (diff > 60000) { // milliseconds per minute
            minutes = (int) (diff / 60000);
            diff -= (minutes * 60000);
        }

        if (diff > 1000) { // milliseconds per second
            seconds = (int) (diff / 1000);
            diff -= (seconds * 1000);
        }

        millis = (int) diff; //milliseconds

        String benchmark = message;
        if (hours > 0) {
            benchmark = benchmark.concat(hours + " h ");
        }
        if (minutes > 0) {
            benchmark = benchmark.concat(minutes + " min ");
        }
        if (seconds > 0) {
            benchmark = benchmark.concat(seconds + " s ");
        }
        if (millis > 0) {
            benchmark = benchmark.concat(millis + " millis");
        }
        return benchmark;
    }

}
