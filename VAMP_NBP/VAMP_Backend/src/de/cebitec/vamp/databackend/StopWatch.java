/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.databackend;

import java.util.concurrent.TimeUnit;

/**
 * A simple StopWatch for measuring execution time
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
        return System.currentTimeMillis()-this.startTime;
    }

    public String getElapsedTimeAsString() {
        long millis = this.getElapsedTime();
        long secs = TimeUnit.MILLISECONDS.toSeconds(millis);
        long millis_carryover = millis - TimeUnit.SECONDS.toMillis(secs);
        String s = "";
        if (secs>0) s = String.format("%d s ", secs);
        s = s + String.format("%d ms", millis_carryover);
        return s;
    }
    
    
}
