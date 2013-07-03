/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegularExpressionTrimMethod will trim a sequence based on the given 
 * regular expression
 * @author Evgeny Anisiforov
 */
public class RegularExpressionTrimMethod extends TrimMethod {
    
    private Pattern regularexpression;
    private String regularexpression_template;
    private int groupnumber_main;
    private int groupnumber_trimLeft;
    private int groupnumber_trimRight;
    private String name;
    
    
    public RegularExpressionTrimMethod(String regularexpression, int groupnumber_main, int groupnumber_trimLeft, int groupnumber_trimRight, String name, String shortName) {
        this.regularexpression_template = regularexpression;
        this.groupnumber_main = groupnumber_main;
        this.groupnumber_trimLeft = groupnumber_trimLeft;
        this.groupnumber_trimRight = groupnumber_trimRight;
        this.name = name;
        this.setMaximumTrimLength(10);
        this.setShortName(shortName);
    }
    
    public void replacePlaceholder(String placeholder, String value) {
        this.regularexpression = Pattern.compile(regularexpression_template.replace(placeholder, value));
    }
    
    @Override
    public void setMaximumTrimLength(int maximumTrimLength) {
        super.setMaximumTrimLength(maximumTrimLength);
        this.replacePlaceholder("%X%", maximumTrimLength+"");
    }
    
    @Override
    public TrimMethodResult trim(String sequence) {
        Matcher matcher = regularexpression.matcher(sequence);
        TrimMethodResult result = new TrimMethodResult(sequence, 0, 0);
        if (matcher.find()) {
            result.setSequence(matcher.group(this.groupnumber_main));
            if (this.groupnumber_trimLeft>0) {
                result.setTrimmedCharsFromLeft(matcher.group(this.groupnumber_trimLeft).length());
            }
            if (this.groupnumber_trimRight>0) {
                result.setTrimmedCharsFromRight(matcher.group(this.groupnumber_trimRight).length());
            }
        }
        //else {
        //if the pattern does not match, just return the full string
        return result;
        //}
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public enum Type {
        VARIABLE_RIGHT, VARIABLE_LEFT, VARIABLE_BOTH, 
        FIXED_LEFT, FIXED_RIGHT, FIXED_BOTH 
    };
    
    public final static int GROUPNUMBER_UNUSED = -1; 
    
    public static RegularExpressionTrimMethod createNewInstance(Type t) {
        if (t.equals(Type.VARIABLE_RIGHT)) 
            return new RegularExpressionTrimMethod("^(.*?)(A{0,%X%})$", 1, GROUPNUMBER_UNUSED, 2, "trim poly-A from 3' end (right to left) by variable length", "v_r");
        else if (t.equals(Type.VARIABLE_LEFT)) 
            return new RegularExpressionTrimMethod("^(A{0,%X%})(.*?)$", 2, 1, GROUPNUMBER_UNUSED, "trim poly-A from 5' end (left to right) by variable length", "v_l");
        else if (t.equals(Type.VARIABLE_BOTH)) 
            return new HalfedLengthTrimMethod("^(A{0,%X%})(.*?)(A{0,%X%})$", 2, 1, 3, "trim poly-A from 3' and from 5' end by variable length", "v_lr");
        else if (t.equals(Type.FIXED_RIGHT)) 
            return new RegularExpressionTrimMethod("^(.*?)(.{%X%})$", 1, GROUPNUMBER_UNUSED, 2, "trim all nucleotides from 3' end (right to left) by fixed length", "f_r");
        else if (t.equals(Type.FIXED_LEFT)) 
            return new RegularExpressionTrimMethod("^(.{%X%})(.*?)$", 2, 1, GROUPNUMBER_UNUSED, "trim all nucleotides from 5' end (left to right) by fixed length", "f_l");
        else if (t.equals(Type.FIXED_BOTH)) 
            return new HalfedLengthTrimMethod("^(.{%X%})(.*?)(.{%X%})$", 2, 1, 3, "trim all nucleotides from 3' and from 5' end by fixed length", "f_lr");
        
        else return new HalfedLengthTrimMethod("^(.{%X%})(.*?)(.{%X%})$", 2, 1, 3, "UNKNOWN", "unkn");
    }
}
