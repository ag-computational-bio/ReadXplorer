/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.rnaTrimming;

import java.util.HashMap;

/**
 * A simple enchancement to the hashmap class to allow counting of values
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
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
