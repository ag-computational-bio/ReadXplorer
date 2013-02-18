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
    private int groupnumber;
    private String name;
    
    
    public RegularExpressionTrimMethod(String regularexpression, int groupnumber, String name) {
        this.regularexpression_template = regularexpression;
        this.groupnumber = groupnumber;
        this.name = name;
        this.setMaximumTrimLength(10);
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
    public String trim(String sequence) {
        Matcher matcher = regularexpression.matcher(sequence);
        if (matcher.find()) {
            return matcher.group(this.groupnumber);
        }
        else {
            //if the pattern does not match, just return the full string
            return sequence;
        }
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public enum Type {
        VARIABLE_RIGHT, VARIABLE_LEFT, VARIABLE_BOTH, 
        FIXED_LEFT, FIXED_RIGHT, FIXED_BOTH 
    };
    
    public static RegularExpressionTrimMethod createNewInstance(Type t) {
        if (t.equals(Type.VARIABLE_RIGHT)) 
            return new RegularExpressionTrimMethod("^(.*?)(A{0,%X%})$", 1, "trim poly-A from 3' end (right to left) by variable length");
        else if (t.equals(Type.VARIABLE_LEFT)) 
            return new RegularExpressionTrimMethod("^(A{0,%X%})(.*?)$", 2, "trim poly-A from 5' end (left to right) by variable length");
        else if (t.equals(Type.VARIABLE_BOTH)) 
            return new RegularExpressionTrimMethod("^(A{0,%X%})(.*?)(A{0,%X%})$", 2, "trim poly-A from 3' and from 5' end by variable length");
        else if (t.equals(Type.FIXED_RIGHT)) 
            return new RegularExpressionTrimMethod("^(.*?)(.{%X%})$", 1, "trim all nucleotides from 3' end (right to left) by fixed length");
        else if (t.equals(Type.FIXED_LEFT)) 
            return new RegularExpressionTrimMethod("^(.{%X%})(.*?)$", 2, "trim all nucleotides from 5' end (left to right) by fixed length");
        else if (t.equals(Type.FIXED_BOTH)) 
            return new RegularExpressionTrimMethod("^(.{%X%})(.*?)(.{%X%})$", 2, "trim all nucleotides from 3' and from 5' end by fixed length");
        
        else return new RegularExpressionTrimMethod("^(.{%X%})(.*?)(.{%X%})$", 2, "UNKNOWN");
    }
}
