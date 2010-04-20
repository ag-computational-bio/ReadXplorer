package vamp.util;

/**
 *
 * @author ddoppmei
 */
public class Benchmark {

    public static void printDiff(long smaller, long greater, String message){
        long diff = greater - smaller;

        int minutes = 0;
        int seconds = 0;
        int millis = 0;

        if(diff > 60000){
            minutes = (int) (diff / 60000);
            diff = diff - (minutes * 60000);
        }

        if(diff > 1000){
            seconds = (int) (diff / 1000);
            diff = diff - (seconds * 1000);
        }

        millis = (int) diff;

        System.out.println(message+" dauerte "+minutes+" minuten, "+seconds+" sekunden, "+millis+" millisekunden");
    }
}
