/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.util;

/**
 *
 * @author Rolf Hilker
 *
 * Contains all global accessible util methods.
 */
public final class Utils {

    private Utils(){
        //Do not instantiate
    }

    /**
     * Reverses a string.
     * @param string the string to reverse
     * @return the reversed string
     */
    public static String reverseString(final String string){
        String revString = "";
        for (int i=string.length()-1; i>=0; --i){
            revString = revString+string.charAt(i);
        }
        return revString;
    }

    /**
     * Complements a DNA String.
     * @param string the string to complement
     * @return the complemented string
     */
    public static String complementDNA(final String string){
        String complement = "";
        char currChar;
        for (int i = 0; i < string.length(); i++) {
            currChar = string.charAt(i);

            switch (currChar){
                case 'c': complement = complement.concat("g"); break;
                case 'g': complement = complement.concat("c"); break;
                case 't': complement = complement.concat("a"); break;
                case 'a': complement = complement.concat("t"); break;
                default : complement = complement.concat(String.valueOf(currChar));
            }
        }
        return complement;
    }
}
