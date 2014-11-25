/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

/**
 * This class implements a simple extension to the RegularExpressionTrimMethod,
 * that allowes to use two-sided regular expressions to shorten reads.
 * To keep the results comparable with one-sided shortenings the shortening length
 * is divided by 2.
 * 
 * Example:  AAAAGGGAAAA
 * --------
 * shorten poly a from right (one side) with length=4
 * will result in AAAAGGG---- (shortened 4 nucleotides in whole)
 * 
 * shorten poly a from both sides (two sides) with length=4 will set actual internal length=2
 * will result in --AAGGGAA-- (shortened 4 nucleotides in whole)
 * 
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class HalfedLengthTrimMethod extends RegularExpressionTrimMethod {
    public HalfedLengthTrimMethod(String regularexpression, int groupnumber_main, int groupnumber_trimLeft, int groupnumber_trimRight, String name, String shortName) {
        super(regularexpression, groupnumber_main, groupnumber_trimLeft, groupnumber_trimRight, name, shortName);
    }
    
    @Override
    public void setMaximumTrimLength(int maximumTrimLength) {
        super.setMaximumTrimLength(maximumTrimLength/2);
    }
    
    @Override
    public int getMaximumTrimLength() {
        return super.getMaximumTrimLength()*2;
    }
}
