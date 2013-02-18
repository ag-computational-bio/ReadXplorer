/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeff
 */
public class RegularExpressionTrimMethodTest {
    
    public RegularExpressionTrimMethodTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void hello() {
        RegularExpressionTrimMethod method;
        String sequence = "AAAGGGCTTGCTAAAAA";        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.FIXED_LEFT);
        method.setMaximumTrimLength(3);
        assertEquals("GGGCTTGCTAAAAA", method.trim(sequence));
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.FIXED_RIGHT);
        method.setMaximumTrimLength(6);
        assertEquals("AAAGGGCTTGC", method.trim(sequence));
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.FIXED_BOTH);
        method.setMaximumTrimLength(3);
        assertEquals("GGGCTTGCTAA", method.trim(sequence));
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.VARIABLE_LEFT);
        method.setMaximumTrimLength(3);
        assertEquals("GGGCTTGCTAAAAA", method.trim(sequence));
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.VARIABLE_RIGHT);
        method.setMaximumTrimLength(6);
        assertEquals("AAAGGGCTTGCT", method.trim(sequence));
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.VARIABLE_BOTH);
        method.setMaximumTrimLength(6);
        assertEquals("GGGCTTGCT", method.trim(sequence));
    }
}
