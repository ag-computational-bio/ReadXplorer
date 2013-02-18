/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import java.util.HashMap;

/**
 * A simple enchancement to the hashmap class to allow simple counting of values
 * @author Evgeny Anisiforov
 */
public class MapCounter<T> extends HashMap<T,Integer> {
    public Integer incrementCount(T key) {
        Integer counter = this.get(key);
        if (counter==null) counter = 0;
        counter++;
        this.put(key, counter);
        return counter;
    }
}
